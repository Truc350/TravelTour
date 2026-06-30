import ollama
import logging
from django.conf import settings

logger = logging.getLogger(__name__)

class OllamaLLMEngine:
    """
    OllamaLLMEngine quản lý kết nối và gọi API tới Ollama Server 
    để sinh câu trả lời bằng mô hình Qwen3 8B (hoặc fallback).
    """
    def __init__(self, model_name=None, host=None):
        # Cho phép cấu hình qua settings hoặc biến môi trường, mặc định dùng qwen3:8b hoặc qwen2.5
        self.model_name = model_name or getattr(settings, "OLLAMA_MODEL", "qwen3:8b")
        self.host = host or getattr(settings, "OLLAMA_HOST", "http://localhost:11434")
        
        # Khởi tạo client Ollama với host tuỳ chỉnh
        self.client = ollama.Client(host=self.host)
        print(f"[LLM Engine] Khởi tạo Ollama Client với model: {self.model_name}, host: {self.host}")

    def generate_response(self, system_instruction: str, messages: list, temperature: float = 0.3) -> str:
        """
        Gọi Ollama Chat API để lấy câu trả lời.
        
        Args:
            system_instruction: Prompt hệ thống định hướng hành vi
            messages: Lịch sử tin nhắn dạng danh sách dict [{'role': 'user'/'assistant', 'content': '...'}]
            temperature: Độ sáng tạo của model (thấp để tránh hallucination)
            
        Returns:
            str: Phản hồi của LLM
        """
        # Xây dựng payload messages đầy đủ cho Ollama
        ollama_messages = [{"role": "system", "content": system_instruction}]
        
        for msg in messages:
            ollama_messages.append({
                "role": msg["role"],
                "content": msg["content"]
            })

        try:
            response = self.client.chat(
                model=self.model_name,
                messages=ollama_messages,
                options={
                    "temperature": temperature,
                    "top_p": 0.9,
                }
            )
            
            if response and 'message' in response and 'content' in response['message']:
                return response['message']['content'].strip()
            
            return "Xin lỗi, tôi gặp sự cố khi xử lý câu trả lời."
            
        except Exception as e:
            logger.error(f"[LLM Engine] Lỗi kết nối Ollama: {e}")
            print(f"[LLM Engine] Lỗi kết nối Ollama: {e}")
            
            # Thử gọi fallback model nhẹ hơn (ví dụ: qwen2.5:0.5b hoặc qwen2.5:7b) nếu qwen3:8b không có sẵn
            if "not found" in str(e).lower() and self.model_name != "qwen2.5:7b":
                print(f"[LLM Engine] Model {self.model_name} không tìm thấy. Thử fallback sang qwen2.5:7b...")
                try:
                    self.model_name = "qwen2.5:7b"
                    response = self.client.chat(
                        model=self.model_name,
                        messages=ollama_messages,
                        options={"temperature": temperature}
                    )
                    return response['message']['content'].strip()
                except Exception as ex:
                    print(f"[LLM Engine] Lỗi fallback: {ex}")
            
            return "Tôi không thể kết nối tới máy chủ trí tuệ nhân tạo (Ollama). Vui lòng kiểm tra lại dịch vụ Ollama."
