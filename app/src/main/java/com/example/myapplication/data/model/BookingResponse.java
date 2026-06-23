package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model wrapper để hứng dữ liệu Booking lồng ghép từ Django API.
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

    @SerializedName("departure_detail")
    public DepartureDetail departureDetail;

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
