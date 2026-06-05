package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import com.example.myapplication.data.model.Favorite;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert
    long insertFavorite(Favorite favorite);

    @Query("DELETE FROM favorites WHERE user_id = :userId AND tour_id = :tourId")
    void deleteFavorite(int userId, int tourId);

    @Query("SELECT * FROM favorites WHERE user_id = :userId")
    List<Favorite> getFavoritesByUser(int userId);

    @Query("SELECT COUNT(*) FROM favorites WHERE user_id = :userId AND tour_id = :tourId")
    int isFavorite(int userId, int tourId);
}
