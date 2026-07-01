from .rag_engine import TourRAGEngine
from .prompt_builder import ChatbotPromptBuilder
from .session_manager import ChatSessionManager
from django.db.models import Q
from tours.models import Tour
import logging
import unicodedata

logger = logging.getLogger(__name__)


def _strip_accents(text: str) -> str:
    """
    Bỏ dấu tiếng Việt để so sánh không phân biệt dấu.
    Ví dụ: 'Vũng Tàu' -> 'Vung Tau', 'vung tau' -> 'vung tau'
    """
    return ''.join(
        c for c in unicodedata.normalize('NFD', text)
        if unicodedata.category(c) != 'Mn'
    )


REGION_KEYWORDS = {
    "mien_bac": [
        "ha noi", "ha long", "sapa", "sa pa", "ninh binh",
        "moc chau", "ha giang", "cao bang", "yen bai", "lao cai", "quang ninh",
        "hai phong", "tam dao", "mien bac",
        "hà nội", "hạ long", "ninh bình", "mộc châu", "hà giang", "cao bằng",
        "yên bái", "lào cai", "quảng ninh", "hải phòng", "tam đảo", "miền bắc",
    ],
    "mien_trung": [
        "da nang", "hue", "hoi an", "quang binh", "phong nha", "quy nhon",
        "nha trang", "khanh hoa", "da lat", "lam dong", "phu yen", "mien trung",
        "đà nẵng", "huế", "hội an", "quảng bình", "quy nhơn",
        "khánh hòa", "đà lạt", "lâm đồng", "phú yên", "miền trung",
    ],
    "mien_nam": [
        "ho chi minh", "sai gon", "vung tau", "can tho", "phu quoc",
        "mui ne", "ben tre", "chau doc", "mien tay", "mien nam",
        "hồ chí minh", "sài gòn", "vũng tàu", "cần thơ", "phú quốc",
        "mũi né", "bến tre", "châu đốc", "miền tây", "miền nam",
    ],
    "nuoc_ngoai": [
        "thai lan", "han quoc", "nhat ban", "singapore", "malaysia",
        "trung quoc", "dai loan", "chau au", "nuoc ngoai",
        "thái lan", "hàn quốc", "nhật bản", "trung quốc",
        "đài loan", "châu âu", "nước ngoài",
    ],
}

CHEAPEST_KEYWORDS = ["re nhat", "gia re", "gia thap", "rẻ nhất", "giá rẻ", "giá thấp"]
TOP_RATED_KEYWORDS = ["danh gia cao", "tot nhat", "review cao", "đánh giá cao", "tốt nhất"]

# Các từ phổ biến không phải địa danh, dùng để lọc khi extract tên điểm đến
DESTINATION_STOP_WORDS = {
    "tour", "toi", "minh", "ban", "muon", "di", "den",
    "toi", "o", "ve", "cho", "xem", "co", "nao",
    "khong", "hoi", "tim", "kiem",
    "gioi", "thieu", "goi", "y", "duoc",
    "nay", "kia", "do", "the", "can",
    "voi", "thich", "hay", "hoac", "nhat",
    "re", "gia", "ngay", "dem", "3n2d", "4n3d",
    "limousine", "muot", "sang", "chat", "tuyet", "dep",
    "tôi", "mình", "bạn", "muốn", "đi", "đến",
    "tới", "ở", "về", "cho", "xem", "có", "nào",
    "không", "hỏi", "tìm", "kiếm",
    "giới", "thiệu", "gợi", "ý", "được",
    "này", "kia", "đó", "thế", "cần",
    "với", "thích", "hay", "hoặc", "nhất",
    "rẻ", "giá", "ngày", "đêm",
    "mượt", "đẹp",
}


def detect_intent(message: str):
    """
    Nhận diện ý định có cấu trúc (sort / filter) trước khi rơi vào semantic search.

    Returns:
        dict hoặc None:
          - {"type": "cheapest"}
          - {"type": "top_rated"}
          - {"type": "region", "region": "mien_nam", "matched_keywords": ["vũng tàu", "vung tau"]}
          - None  => câu hỏi tự do
    """
    text = message.lower()
    if any(kw in text for kw in CHEAPEST_KEYWORDS):
        return {"type": "cheapest"}
    if any(kw in text for kw in TOP_RATED_KEYWORDS):
        return {"type": "top_rated"}
    for region, keywords in REGION_KEYWORDS.items():
        matched = [kw for kw in keywords if kw in text]
        if matched:
            return {"type": "region", "region": region, "matched_keywords": matched}
    return None


def search_by_destination(message: str):
    """
    Tìm kiếm tour theo tên điểm đến bằng cách lọc title__icontains.
    Dùng khi không match được region intent.

    Returns:
        list[Tour]: tối đa 5 tour, hoặc [] nếu không tìm thấy.
    """
    words = message.lower().split()
    keywords = [
        w.strip(".,!?;:'\"") for w in words
        if len(w.strip(".,!?;:'\"")) > 2
        and w.strip(".,!?;:'\"") not in DESTINATION_STOP_WORDS
    ]
    if not keywords:
        return []

    q_filter = Q()
    for kw in keywords:
        q_filter |= Q(title__icontains=kw)

    return list(Tour.objects.filter(q_filter).distinct()[:5])


class TourChatService:
    def __init__(self):
        self.rag_engine = TourRAGEngine()

    def process_user_message(self, message_content: str, session_id_str: str = None, user_id: int = None) -> dict:
        session = ChatSessionManager.get_or_create_session(session_id_str, user_id)
        ChatSessionManager.save_message(session, role='user', content=message_content)

        intent = detect_intent(message_content)
        tours_qs = []
        response_text = ""

        if intent and intent["type"] == "cheapest":
            tours_qs = list(Tour.objects.order_by('discount_price')[:5])
            response_text = (
                "Đây là các tour có giá tốt nhất hiện tại bên mình, bạn tham khảo nhé!"
                if tours_qs else
                "Hiện tại chưa có tour nào trong hệ thống, bạn quay lại sau nhé!"
            )

        elif intent and intent["type"] == "top_rated":
            tours_qs = list(Tour.objects.order_by('-rating_score')[:5])
            response_text = (
                "Đây là các tour được khách hàng đánh giá cao nhất, bạn tham khảo nhé!"
                if tours_qs else
                "Hiện tại chưa có tour nào trong hệ thống, bạn quay lại sau nhé!"
            )

        elif intent and intent["type"] == "region":
            all_region_kws = REGION_KEYWORDS[intent["region"]]

            # ─── Ưu tiên 1: Tìm theo từ khóa cụ thể người dùng đã nhập ────────────────
            # Mở rộng sang cả dạng có dấu lẫn không dấu của địa danh đó.
            # Ví dụ: user gõ "vung tau" → matched = ["vung tau"]
            #        → expand thêm "vũng tàu" vì _strip_accents("vũng tàu") == "vung tau"
            #        → filter title/description ICONTAINS ("vung tau" OR "vũng tàu")
            specific_keywords = intent.get("matched_keywords", [])
            expanded = set(specific_keywords)
            for matched_kw in specific_keywords:
                matched_plain = _strip_accents(matched_kw)
                for region_kw in all_region_kws:
                    if _strip_accents(region_kw) == matched_plain:
                        expanded.add(region_kw)

            q_filter = Q()
            for kw in expanded:
                q_filter |= Q(title__icontains=kw) | Q(description__icontains=kw)
            tours_qs = list(Tour.objects.filter(q_filter).distinct()[:5])

            # ─── Ưu tiên 2: Fallback tìm toàn vùng nếu không có kết quả ────────────────
            if not tours_qs:
                q_filter = Q()
                for kw in all_region_kws:
                    q_filter |= Q(title__icontains=kw) | Q(description__icontains=kw)
                tours_qs = list(Tour.objects.filter(q_filter).distinct()[:5])

            response_text = (
                "Đây là các tour phù hợp với điểm đến bạn quan tâm, bạn tham khảo nhé!"
                if tours_qs else
                "Xin lỗi, hiện tại chưa có tour nào ở khu vực này. Bạn thử khu vực khác xem sao nhé!"
            )

        else:
            # ─── Ưu tiên 1: Tìm trực tiếp theo tên điểm đến trong tiêu đề tour ────────
            tours_qs = search_by_destination(message_content)
            if tours_qs:
                response_text = "Đây là các tour phù hợp với điểm đến bạn quan tâm, bạn tham khảo nhé!"
            else:
                # ─── Ưu tiên 2: RAG semantic search ──────────────────────────────────────
                search_results = self.rag_engine.search_tours(message_content, top_k=3, threshold=0.32)
                tours_qs = [res['tour'] for res in search_results]
                response_text = (
                    "Tôi tìm thấy một số tour có thể phù hợp với yêu cầu của bạn dưới đây. Bạn tham khảo nhé!"
                    if tours_qs else
                    "Xin lỗi, hiện tại tôi chưa tìm thấy tour nào phù hợp với yêu cầu của bạn. Bạn thử thay đổi từ khóa xem sao nhé!"
                )

        ChatSessionManager.save_message(session, role='assistant', content=response_text)

        found_tours_data = []
        for tour in tours_qs:
            first_image = tour.images.first()
            image_url = first_image.image_url if first_image else ""
            found_tours_data.append({
                "id": tour.id,
                "code": tour.code,
                "title": tour.title,
                "discount_price": float(tour.discount_price),
                "original_price": float(tour.original_price),
                "rating_score": tour.rating_score,
                "image_url": image_url,
                "provider": tour.provider,
            })

        return {
            "session_id": str(session.session_id),
            "response": response_text,
            "tours": found_tours_data,
        }
