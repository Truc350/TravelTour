import os
import faiss
import numpy as np
import pickle
from django.conf import settings
from sentence_transformers import SentenceTransformer
from tours.models import Tour

class TourRAGEngine:
    """
    TourRAGEngine quản lý việc tạo Vector Embedding bằng model BAAI/bge-m3 
    và thực hiện tìm kiếm ngữ nghĩa (semantic search) bằng FAISS.
    """
    def __init__(self, model_name="BAAI/bge-m3", index_dir=None):
        self.model_name = model_name
        self.index_dir = index_dir or os.path.join(settings.BASE_DIR, "faiss_tour_index")
        self.index_path = os.path.join(self.index_dir, "tour_faiss.index")
        self.mapping_path = os.path.join(self.index_dir, "tour_mapping.pkl")
        
        # Load SentenceTransformer model
        # Dùng CPU mặc định hoặc GPU nếu khả dụng
        print(f"[RAG Engine] Loading model {self.model_name}...")
        self.model = SentenceTransformer(self.model_name)
        self.dimension = self.model.get_sentence_embedding_dimension()
        print(f"[RAG Engine] Model loaded. Dimension: {self.dimension}")

        # Tải index nếu đã có sẵn
        self.index = None
        self.tour_ids = []
        self.load_index()

    def get_tour_text_representation(self, tour: Tour) -> str:
        """
        Tạo chuỗi văn bản đại diện cho Tour để làm đầu vào cho Embedding.
        Kết hợp các trường thông tin quan trọng: Tiêu đề, Mô tả, Giá, Lịch trình.
        """
        itineraries = list(tour.itineraries.all().order_by('day_number'))
        itinerary_str = ""
        if itineraries:
            itinerary_str = " Lịch trình chi tiết: " + "; ".join(
                [f"Ngày {it.day_number}: {it.title} - {it.description}" for it in itineraries]
            )

        text = (
            f"Tour du lịch: {tour.title}. "
            f"Mô tả: {tour.description}. "
            f"Giá gốc: {int(tour.original_price):,} VNĐ. "
            f"Giá khuyến mãi/giá hiện tại: {int(tour.discount_price):,} VNĐ. "
            f"Nhà cung cấp: {tour.provider}."
            f"{itinerary_str}"
        )
        return text

    def build_index(self):
        """
        Đọc tất cả Tour từ database, chuyển thành embeddings và build index FAISS.
        Lưu index và ánh xạ ID ra file.
        """
        # Lọc các tour hợp lệ (ở đây chỉ tư vấn tour có trong database)
        tours = list(Tour.objects.all())
        if not tours:
            print("[RAG Engine] No tours found in database to build index.")
            return False

        print(f"[RAG Engine] Encoding {len(tours)} tours...")
        texts = [self.get_tour_text_representation(tour) for tour in tours]
        embeddings = self.model.encode(texts, show_progress_bar=True, normalize_embeddings=True)
        embeddings = np.array(embeddings).astype('float32')

        # Khởi tạo FAISS index (sử dụng L2 distance hoặc Inner Product. Do normalize embeddings nên Inner Product tương đương Cosine Similarity)
        index = faiss.IndexFlatIP(self.dimension)
        index.add(embeddings)

        self.index = index
        self.tour_ids = [tour.id for tour in tours]

        # Đảm bảo thư mục lưu trữ tồn tại
        os.makedirs(self.index_dir, exist_ok=True)
        
        # Lưu index
        faiss.write_index(self.index, self.index_path)
        
        # Lưu mapping IDs
        with open(self.mapping_path, 'wb') as f:
            pickle.dump(self.tour_ids, f)

        print(f"[RAG Engine] Successfully built and saved FAISS index at {self.index_dir}")
        return True

    def load_index(self):
        """
        Tải index FAISS và file mapping ID từ ổ đĩa nếu tồn tại.
        """
        if os.path.exists(self.index_path) and os.path.exists(self.mapping_path):
            try:
                self.index = faiss.read_index(self.index_path)
                with open(self.mapping_path, 'rb') as f:
                    self.tour_ids = pickle.load(f)
                print(f"[RAG Engine] Successfully loaded FAISS index. Total tours: {len(self.tour_ids)}")
                return True
            except Exception as e:
                print(f"[RAG Engine] Error loading index: {e}")
        else:
            print("[RAG Engine] FAISS index file not found. Run command build_tour_index.")
        return False

    def search_tours(self, query: str, top_k: int = 3, threshold: float = 0.3):
        """
        Tìm kiếm ngữ nghĩa các tour du lịch phù hợp nhất với câu hỏi/yêu cầu.
        
        Args:
            query: Câu hỏi tự nhiên của người dùng
            top_k: Số lượng kết quả tối đa
            threshold: Ngưỡng điểm tương đồng tối thiểu (cosine similarity)
            
        Returns:
            list[dict]: Danh sách tour phù hợp kèm score tương đồng.
        """
        if self.index is None or not self.tour_ids:
            # Thử load lại
            if not self.load_index():
                return []

        # Tạo embedding cho query
        query_vector = self.model.encode([query], normalize_embeddings=True)
        query_vector = np.array(query_vector).astype('float32')

        # Search trong FAISS
        scores, indices = self.index.search(query_vector, top_k)
        
        results = []
        for score, idx in zip(scores[0], indices[0]):
            if idx == -1:
                continue
            
            # Score của Inner Product với normalized vectors chính là Cosine Similarity
            # Khoảng giá trị từ -1 đến 1. Thông thường > 0.3 là có sự liên quan ngữ nghĩa.
            if score < threshold:
                continue
                
            tour_id = self.tour_ids[idx]
            try:
                tour = Tour.objects.get(id=tour_id)
                results.append({
                    'tour': tour,
                    'score': float(score)
                })
            except Tour.DoesNotExist:
                continue

        return results
