package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.data.model.Booking;

import java.util.List;

@Dao
public interface BookingDao {

    @Insert
    long insertBooking(Booking booking);

    @Update
    int updateBooking(Booking booking);

    @Query("SELECT * FROM bookings WHERE user_id = :userId ORDER BY booking_date DESC")
    List<Booking> getBookingsByUser(int userId);

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    Booking getBookingById(int id);
}
