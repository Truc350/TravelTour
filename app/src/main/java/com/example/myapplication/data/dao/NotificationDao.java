package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.data.model.Notification;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert
    long insertNotification(Notification notification);

    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY id DESC")
    List<Notification> getNotificationsByUser(int userId);

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :notificationId")
    void markAsRead(int notificationId);
    
    @Query("UPDATE notifications SET is_read = 1 WHERE user_id = :userId")
    void markAllAsRead(int userId);
}
