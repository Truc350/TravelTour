package com.example.myapplication.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

@Entity(tableName = "tour_departures",
        foreignKeys = @ForeignKey(entity = Tour.class,
                parentColumns = "id",
                childColumns = "tour_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("tour_id")})
public class TourDeparture implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private int id;

    @ColumnInfo(name = "tour_id")
    @SerializedName("tour")
    private int tourId;

    @ColumnInfo(name = "departure_date")
    @SerializedName("departure_date")
    private String departureDate; // You can use String for ISO 8601 dates or Long for timestamps

    @ColumnInfo(name = "available_seats")
    @SerializedName("available_seats")
    private int availableSeats;

    @ColumnInfo(name = "price")
    @SerializedName("price")
    private double price;

    public TourDeparture(int tourId, String departureDate, int availableSeats, double price) {
        this.tourId = tourId;
        this.departureDate = departureDate;
        this.availableSeats = availableSeats;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTourId() { return tourId; }
    public void setTourId(int tourId) { this.tourId = tourId; }

    public String getDepartureDate() { return departureDate; }
    public void setDepartureDate(String departureDate) { this.departureDate = departureDate; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
