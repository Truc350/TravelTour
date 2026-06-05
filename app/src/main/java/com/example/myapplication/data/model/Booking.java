package com.example.myapplication.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookings",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = TourDeparture.class,
                        parentColumns = "id",
                        childColumns = "departure_id",
                        onDelete = ForeignKey.RESTRICT)
        },
        indices = {@Index("user_id"), @Index("departure_id")})
public class Booking {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "departure_id")
    private int departureId;

    @ColumnInfo(name = "booking_date")
    private String bookingDate;

    @ColumnInfo(name = "status")
    private String status; // "PENDING", "CONFIRMED", "CANCELLED"

    @ColumnInfo(name = "total_price")
    private double totalPrice;

    public Booking(int userId, int departureId, String bookingDate, String status, double totalPrice) {
        this.userId = userId;
        this.departureId = departureId;
        this.bookingDate = bookingDate;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getDepartureId() { return departureId; }
    public void setDepartureId(int departureId) { this.departureId = departureId; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}
