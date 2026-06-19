package com.example.myapplication.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "favorites",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Tour.class,
                        parentColumns = "id",
                        childColumns = "tour_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("user_id"), @Index("tour_id")})
public class Favorite {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    @SerializedName("user")
    private int userId;

    @ColumnInfo(name = "tour_id")
    @SerializedName("tour")
    private int tourId;

    public Favorite(int userId, int tourId) {
        this.userId = userId;
        this.tourId = tourId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTourId() { return tourId; }
    public void setTourId(int tourId) { this.tourId = tourId; }
}
