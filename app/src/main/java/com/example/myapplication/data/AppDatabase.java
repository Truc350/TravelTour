package com.example.myapplication.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.data.dao.BookingDao;
import com.example.myapplication.data.dao.FavoriteDao;
import com.example.myapplication.data.dao.NotificationDao;
import com.example.myapplication.data.dao.PassengerDao;
import com.example.myapplication.data.dao.TourDao;
import com.example.myapplication.data.dao.UserDao;
import com.example.myapplication.data.model.Booking;
import com.example.myapplication.data.model.Favorite;
import com.example.myapplication.data.model.Notification;
import com.example.myapplication.data.model.Passenger;
import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.model.TourDeparture;
import com.example.myapplication.data.model.TourImage;
import com.example.myapplication.data.model.TourItinerary;
import com.example.myapplication.data.model.User;

@Database(entities = {
        User.class,
        Tour.class,
        TourImage.class,
        TourItinerary.class,
        TourDeparture.class,
        Booking.class,
        Passenger.class,
        Favorite.class,
        Notification.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract TourDao tourDao();
    public abstract BookingDao bookingDao();
    public abstract PassengerDao passengerDao();
    public abstract FavoriteDao favoriteDao();
    public abstract NotificationDao notificationDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "travel_tour_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
