class ChatbotPromptBuilder:
    """
    ChatbotPromptBuilder xây dựng System Prompt và định dạng Context 
    cho RAG (Retrieval-Augmented Generation).
    """

    @staticmethod
    def get_system_instruction(tours_context_str: str) -> str:
        """
        Trả về system prompt chỉ định vai trò, nhiệm vụ và các ràng buộc nghiêm ngặt cho LLM.
        """
        system_instruction = (
            "Bạn là một trợ lý ảo tư vấn tour du lịch chuyên nghiệp, thân thiện của TravelTour.\n"
            "Nhiệm vụ của bạn là hỗ trợ khách hàng tìm kiếm và giải đáp thắc mắc về các tour du lịch TRONG NƯỚC.\n\n"
            
            "QUY TẮC BẮT BUỘC:\n"
            "1. CHỈ TƯ VẤN CÁC TOUR DU LỊCH TRONG NƯỚC CÓ SẴN TRONG CƠ SỞ DỮ LIỆU (Context) cung cấp bên dưới.\n"
            "2. KHÔNG ĐƯỢC TỰ TẠO TOUR MỚI. Không bịa đặt thông tin về giá cả, hành trình, ngày đi hay dịch vụ đi kèm.\n"
            "3. Nếu không tìm thấy tour nào phù hợp trong danh sách Context, hoặc danh sách Context trống, bạn PHẢI trả lời lịch sự rằng hiện chưa có tour phù hợp với yêu cầu của khách hàng (Ví dụ: 'Hiện tại hệ thống chưa có tour phù hợp với yêu cầu này của bạn. Bạn có muốn tham khảo các tour nổi bật khác không?'). Tuyệt đối không được tự ý giới thiệu tour khác ngoài Context.\n"
            "4. Nếu khách hàng đưa ra yêu cầu thiếu thông tin (ví dụ: chỉ hỏi chung chung 'tôi muốn đi du lịch'), hãy phân tích ý định và ĐẶT CÂU HỎI THÊM để làm rõ thông tin cần thiết như: điểm xuất phát mong muốn, ngân sách (dưới bao nhiêu triệu), số ngày đi (ví dụ: 3 ngày 2 đêm), hoặc đi cùng ai (gia đình có trẻ em, cặp đôi...) để gợi ý chính xác.\n"
            "5. Không bịa thông tin. Trả lời tự nhiên, trôi chảy, lịch sự như một nhân viên tư vấn thực thụ.\n"
            "6. Chỉ tư vấn du lịch trong nước Việt Nam. Từ chối lịch sự nếu khách hàng hỏi về các địa danh hoặc tour nước ngoài.\n\n"
            
            "DANH SÁCH TOUR DU LỊCH PHÙ HỢP TỪ DATABASE (CONTEXT):\n"
            "--------------------------------------------------\n"
            f"{tours_context_str}\n"
            "--------------------------------------------------\n\n"
            "Hãy phân tích lịch sử trò chuyện và câu hỏi mới nhất để trả lời khách hàng một cách tối ưu nhất."
        )
        return system_instruction

    @staticmethod
    def format_tours_context(search_results: list) -> str:
        """
        Chuyển đổi danh sách kết quả tìm kiếm từ FAISS sang chuỗi văn bản chi tiết làm Context cho LLM.
        """
        if not search_results:
            return "Không tìm thấy tour nào phù hợp trong cơ sở dữ liệu."

        context_parts = []
        for i, res in enumerate(search_results, 1):
            tour = res['tour']
            score = res['score']
            
            # Lấy thông tin lịch trình
            itineraries = list(tour.itineraries.all().order_by('day_number'))
            itinerary_txt = ""
            if itineraries:
                itinerary_txt = "\n    Lịch trình:" + "".join(
                    [f"\n      - Ngày {it.day_number}: {it.title} - {it.description}" for it in itineraries]
                )

            # Lấy thông tin các đợt khởi hành
            departures = list(tour.departures.all()[:3])  # Lấy tối đa 3 lịch đi gần nhất
            departure_txt = ""
            if departures:
                departure_txt = "\n    Lịch khởi hành và giá:" + "".join(
                    [f"\n      + Ngày {d.departure_date} ({d.hour_departure}) - Giá: {int(d.price):,} VNĐ (Còn {d.available_seats} chỗ)" for d in departures]
                )

            tour_info = (
                f"{i}. Tour ID: {tour.id}\n"
                f"  Mã tour: {tour.code}\n"
                f"  Tên tour: {tour.title}\n"
                f"  Mô tả: {tour.description}\n"
                f"  Giá gốc: {int(tour.original_price):,} VNĐ\n"
                f"  Giá khuyến mãi hiện tại: {int(tour.discount_price):,} VNĐ\n"
                f"  Đánh giá: {tour.rating_score}/5 ({tour.reviews_count} đánh giá)\n"
                f"  Nhà cung cấp: {tour.provider}\n"
                f"  Ghi chú: {tour.note or 'Không có'}\n"
                f"  Chi tiết bao gồm: {tour.description_tour_include or 'Liên hệ hỗ trợ'}"
                f"{itinerary_txt}"
                f"{departure_txt}\n"
                f"  (Độ tương đồng ngữ nghĩa: {score:.2f})\n"
            )
            context_parts.append(tour_info)

        return "\n".join(context_parts)
