from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.shortcuts import get_object_or_404
from tours.models import ChatSession
from tours.chatbot.chat_service import TourChatService
from tours.chatbot.session_manager import ChatSessionManager
import uuid

# Khởi tạo instance TourChatService dùng chung
chat_service = TourChatService()

class ChatbotAPIView(APIView):
    """
    API gửi câu hỏi của người dùng và nhận câu trả lời từ AI Chatbot.
    
    Endpoint: POST /api/chatbot/chat/
    Body:
    {
        "message": "Tôi muốn tìm tour đi Đà Lạt dưới 5 triệu",
        "session_id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",  (Tùy chọn)
        "user_id": 1                                           (Tùy chọn)
    }
    Response:
    {
        "session_id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
        "response": "Chào bạn, dưới đây là một số tour đi Đà Lạt phù hợp...",
        "tours": [
            {
                "id": 5,
                "code": "DL05",
                "title": "Tour Đà Lạt 3 ngày 2 đêm",
                "discount_price": 4200000.0,
                ...
            }
        ]
    }
    """
    def post(self, request, *args, **kwargs):
        message = request.data.get("message", "").strip()
        session_id = request.data.get("session_id", None)
        user_id = request.data.get("user_id", None)

        if not message:
            return Response(
                {"error": "Trường 'message' không được để trống."},
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            # Xử lý tin nhắn thông qua chat service
            result = chat_service.process_user_message(
                message_content=message,
                session_id_str=session_id,
                user_id=user_id
            )
            return Response(result, status=status.HTTP_200_OK)
            
        except Exception as e:
            return Response(
                {"error": f"Lỗi hệ thống chatbot: {str(e)}"},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )


class ChatHistoryAPIView(APIView):
    """
    API lấy toàn bộ lịch sử tin nhắn của một phiên chat.
    
    Endpoint: GET /api/chatbot/history/<session_id>/
    Response:
    [
        {
            "role": "user",
            "content": "Tôi muốn đi biển",
            "timestamp": "2026-06-30T11:30:00.123456Z"
        },
        {
            "role": "assistant",
            "content": "Chào bạn! Hiện tại Chill Tour có các tour đi biển như sau...",
            "timestamp": "2026-06-30T11:30:02.987654Z"
        }
    ]
    """
    def get(self, request, session_id, *args, **kwargs):
        try:
            session_uuid = uuid.UUID(session_id)
            session = get_object_or_404(ChatSession, session_id=session_uuid)
            
            # Lấy tất cả tin nhắn lịch sử (không giới hạn để client xem đầy đủ)
            history = ChatSessionManager.get_chat_history_messages(session, limit=None)
            return Response(history, status=status.HTTP_200_OK)
            
        except ValueError:
            return Response(
                {"error": "Định dạng session_id không hợp lệ (phải là UUID)."},
                status=status.HTTP_400_BAD_REQUEST
            )


class ChatSessionDeleteAPIView(APIView):
    """
    API xóa một phiên chat (xóa lịch sử hội thoại).
    
    Endpoint: DELETE /api/chatbot/session/<session_id>/
    Response:
    {
        "message": "Đã xóa phiên hội thoại thành công."
    }
    """
    def delete(self, request, session_id, *args, **kwargs):
        success = ChatSessionManager.delete_session(session_id)
        if success:
            return Response(
                {"message": "Đã xóa phiên hội thoại thành công."},
                status=status.HTTP_200_OK
            )
        return Response(
            {"error": "Phiên hội thoại không tồn tại hoặc session_id không hợp lệ."},
            status=status.HTTP_404_NOT_FOUND
        )
