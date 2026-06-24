package com.example.myapplication.data.remote;

import android.app.DownloadManager;

import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.model.User;
import com.example.myapplication.data.model.Passenger;
import com.example.myapplication.data.model.Favorite;
import com.example.myapplication.data.model.BookingRequest;
import com.example.myapplication.data.model.BookingResponse;
import com.example.myapplication.data.model.VoucherHelper;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/tours/")
    Call<List<Tour>> getTours();

    // Bookings CRUD
    @GET("api/bookings/")
    Call<List<BookingResponse>> getBookings();

    @POST("api/bookings/")
    Call<BookingResponse> createBooking(@Body BookingRequest bookingRequest);

    @PATCH("api/bookings/{id}/")
    Call<BookingResponse> patchBooking(@Path("id") int id, @Body java.util.Map<String, Object> fields);

    // User authentication & registration
    @GET("api/users/")
    Call<List<User>> getUsers();

    @PATCH("api/users/{id}/")
    Call<User> patchUser(@Path("id") int id, @Body Map<String, Object> fields);

    @POST("api/users/")
    Call<User> registerUser(@Body User user);
    @GET("api/notifications/")
    Call<List<com.example.myapplication.data.model.Notification>> getNotifications();
    @PATCH("api/notifications/{id}/")
    Call<com.example.myapplication.data.model.Notification> patchNotification(@Path("id") int id, @Body Map<String, Object> fields);

    // Passengers CRUD
    @GET("api/passengers/")
    Call<List<Passenger>> getPassengers();

    @GET("api/passengers/{id}/")
    Call<Passenger> getPassengerById(@Path("id") int id);

    @POST("api/passengers/")
    Call<Passenger> addPassenger(@Body Passenger passenger);

    @PUT("api/passengers/{id}/")
    Call<Passenger> updatePassenger(@Path("id") int id, @Body Passenger passenger);

    @DELETE("api/passengers/{id}/")
    Call<Void> deletePassenger(@Path("id") int id);

    // Favorites CRUD
    @GET("api/favorites/")
    Call<List<Favorite>> getFavorites();

    @POST("api/favorites/")
    Call<Favorite> addFavorite(@Body Favorite favorite);

    @DELETE("api/favorites/{id}/")
    Call<Void> removeFavorite(@Path("id") int id);

    @GET("api/tours/")
    Call<List<Tour>> searchTours(
            @Query("destination") String destination,
            @Query("origin") String origin,
            @Query("day") int day,
            @Query("month") int month,
            @Query("year") int year
    );

    @GET("api/vouchers/")
    Call<List<VoucherHelper.AppVoucher>> getVouchers();

    @GET("api/reviews/")
    Call<List<com.example.myapplication.data.model.Review>> getReviews();

    @POST("api/reviews/")
    Call<Void> createReview(@Body java.util.Map<String, Object> reviewData);
}