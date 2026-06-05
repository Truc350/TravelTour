package com.example.myapplication.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tour_images",
        foreignKeys = @ForeignKey(entity = Tour.class,
                parentColumns = "id",
                childColumns = "tour_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("tour_id")})
public class TourImage {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "tour_id")
    private int tourId;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    public TourImage(int tourId, String imageUrl) {
        this.tourId = tourId;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTourId() { return tourId; }
    public void setTourId(int tourId) { this.tourId = tourId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
