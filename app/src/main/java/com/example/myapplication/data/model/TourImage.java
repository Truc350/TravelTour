package com.example.myapplication.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

@Entity(tableName = "tour_images",
        foreignKeys = @ForeignKey(entity = Tour.class,
                parentColumns = "id",
                childColumns = "tour_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("tour_id")})
public class TourImage implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("id")
    private int id;

    @ColumnInfo(name = "tour_id")
    private int tourId;

    @SerializedName(value = "image_url", alternate = {"image"})
    @ColumnInfo(name = "image_url")
    private String imageUrl;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTourId() { return tourId; }
    public void setTourId(int tourId) { this.tourId = tourId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
