package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * BƯỚC 5.3: DTO (Data Transfer Object) / Model Request đại diện cho dữ liệu gửi lên Django API.
 * Lớp này gom tất cả thông tin booking thu thập được từ BookingInfoActivity và PaymentActivity
 * để gửi đi trong request body của POST request '/api/bookings/'.
 * Các thuộc tính bao gồm:
 * - user: ID người dùng (UserSession).
 * - departure: ID chuyến khởi hành.
 * - bookingDate / departureHour: Ngày đặt, giờ khởi hành.
 * - status: Trạng thái thanh toán (mặc định "CONFIRMED").
 * - totalPrice: Tổng tiền sau khi đã trừ voucher.
 * - tourId / voucherCode: Mã tour, mã giảm giá.
 * - customerName / customerPhone / customerEmail: Thông tin khách hàng.
 * - isInvoiceRequested: Cờ yêu cầu xuất hóa đơn điện tử (gửi email SMTP do Django backend thực hiện).
 */
public class BookingRequest {
    @SerializedName("user")
    public int user;

    @SerializedName("departure")
    public int departure;

    @SerializedName("booking_date")
    public String bookingDate;

    @SerializedName("departure_hour")
    public String departureHour;

    @SerializedName("status")
    public String status;

    @SerializedName("total_price")
    public double totalPrice;

    @SerializedName("tour_id")
    public int tourId;

    @SerializedName("voucher_code")
    public String voucherCode;

    @SerializedName("customer_name")
    public String customerName;

    @SerializedName("customer_phone")
    public String customerPhone;

    @SerializedName("customer_email")
    public String customerEmail;

    @SerializedName("is_invoice_requested")
    public boolean isInvoiceRequested;

    public BookingRequest(int user, int departure, String bookingDate, String departureHour, String status, double totalPrice, int tourId, String voucherCode, String customerName, String customerPhone, String customerEmail, boolean isInvoiceRequested) {
        this.user = user;
        this.departure = departure;
        this.bookingDate = bookingDate;
        this.departureHour = departureHour;
        this.status = status;
        this.totalPrice = totalPrice;
        this.tourId = tourId;
        this.voucherCode = voucherCode;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.isInvoiceRequested = isInvoiceRequested;
    }
}
