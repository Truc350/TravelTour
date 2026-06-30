import os
# Thiết lập mirror Hugging Face để tải nhanh hơn gấp 10 lần ở khu vực châu Á
os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"

from sentence_transformers import SentenceTransformer
print("=== CHƯƠNG TRÌNH TẢI MÔ HÌNH AI (ĐÃ KÈM MIRROR SIÊU TỐC) ===")
print("Đang kết nối tới máy chủ tải mô hình...")
model = SentenceTransformer("all-MiniLM-L6-v2")
print("=== THÀNH CÔNG ===")
print("Mô hình đã được tải và lưu vào máy tính của bạn!")
