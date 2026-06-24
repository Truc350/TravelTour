package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model request dùng để gửi dữ liệu tạo Booking mới lên Django API.
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

    public BookingRequest(int user, int departure, String bookingDate, String departureHour, String status, double totalPrice, int tourId, String voucherCode) {
        this.user = user;
        this.departure = departure;
        this.bookingDate = bookingDate;
        this.departureHour = departureHour;
        this.status = status;
        this.totalPrice = totalPrice;
        this.tourId = tourId;
        this.voucherCode = voucherCode;
    }
}
