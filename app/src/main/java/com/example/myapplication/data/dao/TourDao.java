package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.model.TourDeparture;
import com.example.myapplication.data.model.TourImage;
import com.example.myapplication.data.model.TourItinerary;

import java.util.List;

@Dao
public interface TourDao {

    @Insert
    long insertTour(Tour tour);

    @Insert
    void insertTourImages(List<TourImage> images);

    @Insert
    void insertTourItineraries(List<TourItinerary> itineraries);

    @Insert
    void insertTourDepartures(List<TourDeparture> departures);

    @Query("SELECT * FROM tours")
    List<Tour> getAllTours();

    @Query("SELECT * FROM tours WHERE id = :id LIMIT 1")
    Tour getTourById(int id);

    @Query("SELECT * FROM tour_images WHERE tour_id = :tourId")
    List<TourImage> getImagesForTour(int tourId);

    @Query("SELECT * FROM tour_itineraries WHERE tour_id = :tourId ORDER BY day_number ASC")
    List<TourItinerary> getItineraryForTour(int tourId);

    @Query("SELECT * FROM tour_departures WHERE tour_id = :tourId ORDER BY departure_date ASC")
    List<TourDeparture> getDeparturesForTour(int tourId);
    
    @Query("SELECT * FROM tours WHERE title LIKE '%' || :query || '%'")
    List<Tour> searchTours(String query);
}
