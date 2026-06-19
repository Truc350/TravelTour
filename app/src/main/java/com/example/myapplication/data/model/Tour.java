package com.example.myapplication.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(tableName = "tours")
public class Tour implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @SerializedName("code")
    @ColumnInfo(name = "code")
    private String code;

    @SerializedName("title")
    @ColumnInfo(name = "title")
    private String title;

    @SerializedName("description")
    @ColumnInfo(name = "description")
    private String description;

    @SerializedName("original_price")
    @ColumnInfo(name = "original_price")
    private double originalPrice;

    @SerializedName("discount_price")
    @ColumnInfo(name = "discount_price")
    private double discountPrice;

    @SerializedName("provider")
    @ColumnInfo(name = "provider")
    private String provider;

    @SerializedName("rating_score")
    @ColumnInfo(name = "rating_score")
    private double ratingScore;

    @SerializedName("reviews_count")
    @ColumnInfo(name = "reviews_count")
    private int reviewsCount;

    @SerializedName("description_tour_include")
    @ColumnInfo(name = "description_tour_include")
    private String descriptionTourInclude;

    @SerializedName("note")
    @ColumnInfo(name = "note")
    private String note;

    @SerializedName("region")
    @ColumnInfo(name = "region")
    private String region;

    @SerializedName("images")
    @androidx.room.Ignore
    private java.util.List<TourImage> images;

    @SerializedName("itineraries")
    @androidx.room.Ignore
    private java.util.List<TourItinerary> itineraries;

    public Tour(String code, String title, String description, double originalPrice, double discountPrice, String provider, double ratingScore, int reviewsCount, String descriptionTourInclude, String note, String region) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.provider = provider;
        this.ratingScore = ratingScore;
        this.reviewsCount = reviewsCount;
        this.descriptionTourInclude = descriptionTourInclude;
        this.note = note;
        this.region = region;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }

    public double getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(double discountPrice) { this.discountPrice = discountPrice; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public double getRatingScore() { return ratingScore; }
    public void setRatingScore(double ratingScore) { this.ratingScore = ratingScore; }

    public int getReviewsCount() { return reviewsCount; }
    public void setReviewsCount(int reviewsCount) { this.reviewsCount = reviewsCount; }

    public String getDescriptionTourInclude() { return descriptionTourInclude; }
    public void setDescriptionTourInclude(String descriptionTourInclude) { this.descriptionTourInclude = descriptionTourInclude; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public java.util.List<TourImage> getImages() { return images; }
    public void setImages(java.util.List<TourImage> images) { this.images = images; }

    public java.util.List<TourItinerary> getItineraries() { return itineraries; }
    public void setItineraries(java.util.List<TourItinerary> itineraries) { this.itineraries = itineraries; }
}
