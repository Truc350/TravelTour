package com.example.myapplication.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tours")
public class Tour {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "code")
    private String code;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "original_price")
    private double originalPrice;

    @ColumnInfo(name = "discount_price")
    private double discountPrice;

    @ColumnInfo(name = "provider")
    private String provider;

    @ColumnInfo(name = "rating_score")
    private double ratingScore;

    @ColumnInfo(name = "reviews_count")
    private int reviewsCount;

    @ColumnInfo(name = "description_tour_include")
    private String descriptionTourInclude;

    @ColumnInfo(name = "note")
    private String note;

    public Tour(String code, String title, String description, double originalPrice, double discountPrice, String provider, double ratingScore, int reviewsCount, String descriptionTourInclude, String note) {
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
}
