package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.myapplication.data.model.Passenger;

import java.util.List;

@Dao
public interface PassengerDao {

    @Insert
    long insertPassenger(Passenger passenger);

    @Insert
    void insertPassengers(List<Passenger> passengers);

    @Update
    int updatePassenger(Passenger passenger);

    @Delete
    int deletePassenger(Passenger passenger);

    @Query("SELECT * FROM passengers WHERE booking_id = :bookingId")
    List<Passenger> getPassengersForBooking(int bookingId);

    @Query("SELECT * FROM passengers WHERE id = :id LIMIT 1")
    Passenger getPassengerById(int id);
}
