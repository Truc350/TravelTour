from .rag_engine import TourRAGEngine
from .llm_engine import OllamaLLMEngine
from .prompt_builder import ChatbotPromptBuilder
from .session_manager import ChatSessionManager
import logging

logger = logging.getLogger(__name__)

class TourChatService:
    """
    TourChatService đóng vai trò Orchestrator (Điều phối viên) chính.
    
    Quy trình xử lý một tin nhắn:
    1. Nhận tin nhắn từ API, tải/tạo session & lưu tin nhắn User vào DB.
    2. Gọi RAG Engine thực hiện Semantic Search để tìm Tour liên quan ngữ nghĩa trong DB.
    3. Định dạng danh sách Tour thành Context String.
    4. Xây dựng System Prompt chứa các chỉ dẫn ràng buộc và Context.
    5. Đọc lịch sử hội thoại gần nhất.
    6. Gọi LLM Engine (Ollama/Qwen) để sinh câu trả lời.
    7. Lưu câu trả lời của Chatbot vào DB và trả về cho client.
    """
    def __init__(self):
        self.rag_engine = TourRAGEngine()
        self.llm_engine = OllamaLLMEngine()

    def process_user_message(self, message_content: str, session_id_str: str = None, user_id: int = None) -> dict:
        """
        Xử lý tin nhắn đầu vào của người dùng và trả về phản hồi từ chatbot.
        
        Args:
            message_content: Nội dung tin nhắn của người dùng
            session_id_str: UUID dạng chuỗi đại diện phiên chat
            user_id: ID người dùng (nếu có)
            
        Returns:
            dict: {
                'session_id': str,
                'response': str,
                'found_tours': list[dict] (Danh sách tour gợi ý)
            }
        """
        # 1. Quản lý phiên hội thoại & lưu tin nhắn người dùng
        session = ChatSessionManager.get_or_create_session(session_id_str, user_id)
        ChatSessionManager.save_message(session, role='user', content=message_content)

        # 2. RAG: Tìm kiếm ngữ nghĩa tour phù hợp trong database
        # Ngưỡng tương đồng threshold mặc định là 0.32 để tránh gợi ý tour không liên quan.
        search_results = self.rag_engine.search_tours(message_content, top_k=3, threshold=0.32)

        # 3. Tạo Context & Prompt
        tours_context_str = ChatbotPromptBuilder.format_tours_context(search_results)
        system_instruction = ChatbotPromptBuilder.get_system_instruction(tours_context_str)

        # 4. Lấy lịch sử hội thoại gần nhất (tối đa 8 tin nhắn trước đó để giữ context tốt nhất)
        history_messages = ChatSessionManager.get_chat_history_messages(session, limit=8)

        # 5. LLM sinh câu trả lời
        response_text = self.llm_engine.generate_response(
            system_instruction=system_instruction,
            messages=history_messages,
            temperature=0.25 # Nhiệt độ thấp để model bám sát context và tránh ảo tưởng (hallucination)
        )

        # 6. Lưu phản hồi của Chatbot vào cơ sở dữ liệu
        ChatSessionManager.save_message(session, role='assistant', content=response_text)

        # 7. Định dạng danh sách tour để trả về API (giúp Android có thể hiển thị dạng thẻ/slide trực quan)
        found_tours_data = []
        for res in search_results:
            tour = res['tour']
            # Lấy ảnh đại diện đầu tiên nếu có
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
                "score": res['score']
            })

        return {
            "session_id": str(session.session_id),
            "response": response_text,
            "tours": found_tours_data
        }
