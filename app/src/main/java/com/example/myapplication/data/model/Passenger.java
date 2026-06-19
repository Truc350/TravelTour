package com.example.myapplication.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "passengers",
        foreignKeys = @ForeignKey(entity = Booking.class,
                parentColumns = "id",
                childColumns = "booking_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("booking_id")})
public class Passenger {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "booking_id")
    @SerializedName("booking")
    private Integer bookingId;

    @ColumnInfo(name = "salutation")
    private String salutation;

    @ColumnInfo(name = "fullname")
    private String fullname;

    @ColumnInfo(name = "birthdate")
    private String birthdate;

    @ColumnInfo(name = "nationality")
    private String nationality;

    @ColumnInfo(name = "issuing_country")
    @SerializedName("issuing_country")
    private String issuingCountry;

    @ColumnInfo(name = "expiry_date")
    @SerializedName("expiry_date")
    private String expiryDate;

    @ColumnInfo(name = "id_or_passport")
    @SerializedName("id_or_passport")
    private String idOrPassport;

    @ColumnInfo(name = "status")
    @SerializedName("status")
    private String status = "PENDING";

    public Passenger(Integer bookingId, String salutation, String fullname, String birthdate, String nationality, String issuingCountry, String expiryDate, String idOrPassport) {
        this.bookingId = bookingId;
        this.salutation = salutation;
        this.fullname = fullname;
        this.birthdate = birthdate;
        this.nationality = nationality;
        this.issuingCountry = issuingCountry;
        this.expiryDate = expiryDate;
        this.idOrPassport = idOrPassport;
        this.status = "PENDING";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public String getSalutation() { return salutation; }
    public void setSalutation(String salutation) { this.salutation = salutation; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getIssuingCountry() { return issuingCountry; }
    public void setIssuingCountry(String issuingCountry) { this.issuingCountry = issuingCountry; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getIdOrPassport() { return idOrPassport; }
    public void setIdOrPassport(String idOrPassport) { this.idOrPassport = idOrPassport; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
