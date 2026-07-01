package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * BƯỚC 5.4a: DTO (Data Transfer Object) / Model Response đại diện cho dữ liệu phản hồi từ Django API.
 * Sau khi Django lưu thành công Booking vào database, nó sẽ trả về thông tin booking vừa tạo,
 * quan trọng nhất là trường 'id' (mã đơn hàng thực tế tăng tự động trong cơ sở dữ liệu) và chi tiết chuyến đi.
 */
public class BookingResponse {
    @SerializedName("id")
    public int id;

    @SerializedName("user")
    public int user;

    @SerializedName("departure")
    public int departure;

    @SerializedName("booking_date")
    public String bookingDate;

    @SerializedName("departure_hour")
    public String departureHour;

    @SerializedName("status")
    public String status; // PENDING, CONFIRMED, CANCELLED

    @SerializedName("total_price")
    public double totalPrice;

    @SerializedName("voucher_code")
    public String voucherCode;

    @SerializedName("departure_detail")
    public DepartureDetail departureDetail;

    @SerializedName("is_invoice_requested")
    public boolean isInvoiceRequested;

    @SerializedName("customer_name")
    public String customerName;

    @SerializedName("customer_phone")
    public String customerPhone;

    @SerializedName("customer_email")
    public String customerEmail;

    public static class DepartureDetail {
        @SerializedName("id")
        public int id;

        @SerializedName("tour")
        public int tour;

        @SerializedName("departure_date")
        public String departureDate;

        @SerializedName("available_seats")
        public int availableSeats;

        @SerializedName("price")
        public double price;

        @SerializedName("tour_detail")
        public Tour tourDetail;
    }
}
