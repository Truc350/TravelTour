import uuid
from tours.models import ChatSession, ChatMessage, User

class ChatSessionManager:
    """
    ChatSessionManager quản lý việc lưu trữ, truy xuất và xóa lịch sử trò chuyện
    của khách hàng trong cơ sở dữ liệu.
    """

    @staticmethod
    def get_or_create_session(session_id_str: str = None, user_id: int = None) -> ChatSession:
        """
        Lấy session hiện tại hoặc tạo mới nếu chưa tồn tại.
        
        Args:
            session_id_str: Chuỗi UUID đại diện phiên chat (tùy chọn)
            user_id: ID người dùng nếu đã đăng nhập (tùy chọn)
            
        Returns:
            ChatSession: Đối tượng phiên chat
        """
        session = None
        user = None
        
        if user_id:
            try:
                user = User.objects.get(id=user_id)
            except User.DoesNotExist:
                pass

        if session_id_str:
            try:
                # Kiểm tra định dạng UUID hợp lệ
                session_uuid = uuid.UUID(session_id_str)
                session = ChatSession.objects.filter(session_id=session_uuid).first()
            except ValueError:
                # Nếu chuỗi truyền vào không hợp lệ UUID, ta sẽ tạo mới
                pass

        if not session:
            # Tạo mới phiên chat
            session = ChatSession.objects.create(user=user)
        else:
            # Nếu tìm thấy session nhưng chưa gán user (mà user_id có truyền), ta cập nhật user
            if user and not session.user:
                session.user = user
                session.save()

        return session

    @staticmethod
    def get_chat_history_messages(session: ChatSession, limit: int = 10) -> list:
        """
        Lấy lịch sử hội thoại gần nhất dưới dạng danh sách dict thích hợp cho LLM.
        """
        messages = ChatMessage.objects.filter(session=session).order_by('timestamp')
        
        # Giới hạn số lượng tin nhắn lịch sử để tránh quá tải token prompt
        if limit:
            # Lấy N tin nhắn cuối cùng nhưng vẫn giữ đúng thứ tự thời gian tăng dần
            messages_count = messages.count()
            if messages_count > limit:
                messages = messages[messages_count - limit:]

        history = []
        for msg in messages:
            history.append({
                "role": msg.role,
                "content": msg.content,
                "timestamp": msg.timestamp.isoformat()
            })
        return history

    @staticmethod
    def save_message(session: ChatSession, role: str, content: str) -> ChatMessage:
        """
        Lưu một tin nhắn mới vào cơ sở dữ liệu.
        """
        message = ChatMessage.objects.create(
            session=session,
            role=role,
            content=content
        )
        # Cập nhật trường updated_at của session để đẩy lên đầu hàng đợi
        session.save()  # Kích hoạt auto_now
        return message

    @staticmethod
    def delete_session(session_id_str: str) -> bool:
        """
        Xóa một phiên trò chuyện cùng toàn bộ tin nhắn liên quan.
        """
        try:
            session_uuid = uuid.UUID(session_id_str)
            session = ChatSession.objects.filter(session_id=session_uuid).first()
            if session:
                session.delete()  # On delete cascade sẽ tự động xóa các ChatMessage liên quan
                return True
        except ValueError:
            pass
        return False
