package com.example.myapplication.data.remote;

import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.model.User;
import com.example.myapplication.data.model.Passenger;
import com.example.myapplication.data.model.Favorite;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @GET("api/tours/")
    Call<List<Tour>> getTours();

    // User authentication & registration
    @GET("api/users/")
    Call<List<User>> getUsers();

    @POST("api/users/")
    Call<User> registerUser(@Body User user);

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
}