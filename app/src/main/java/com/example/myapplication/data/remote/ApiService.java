package com.example.myapplication.data.remote;

import android.app.DownloadManager;

import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.model.TourDeparture;
import com.example.myapplication.data.model.User;
import com.example.myapplication.data.model.Passenger;
import com.example.myapplication.data.model.Favorite;
import com.example.myapplication.data.model.BookingRequest;
import com.example.myapplication.data.model.BookingResponse;
import com.example.myapplication.data.model.VoucherHelper;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public interface ApiService {

    @GET("api/tours/")
    Call<List<Tour>> getTours();

    @GET("api/tour-departures/")
    Call<List<TourDeparture>> getTourDepartures(@Query("tour_id") int tourId);

    // BƯỚC 5.1b: Lấy danh sách booking từ server Django
    @GET("api/bookings/")
    Call<List<BookingResponse>> getBookings();

    // BƯỚC 5.3a: Gửi yêu cầu tạo booking mới lên server Django qua phương thức POST '/api/bookings/'
    // Nhận DTO BookingRequest từ body request và trả về DTO BookingResponse chứa ID booking thật
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

    @POST("api/behaviors/")
    Call<Void> logBehavior(@Body java.util.Map<String, Object> behavior);


    @GET("api/tours/")
    Call<List<Tour>> searchTours(
            @Query("destination") String destination,
            @Query("origin") String origin,
            @Query("day") int day,
            @Query("month") int month,
            @Query("year") int year
    );

    @GET("api/vouchers/")
    Call<List<VoucherHelper.AppVoucher>> getVouchers(
            @Query("user_id") Integer userId,
            @Query("saved_only") Boolean savedOnly
    );

    @POST("api/user-vouchers/")
    Call<Void> saveUserVoucher(@Body java.util.Map<String, Object> body);

    @GET("api/user-vouchers/")
    Call<List<VoucherHelper.UserVoucherResponse>> getUserVouchers(@Query("user_id") Integer userId);

    @GET("api/reviews/")
    Call<List<com.example.myapplication.data.model.Review>> getReviews();

    @POST("api/reviews/")
    Call<Void> createReview(@Body java.util.Map<String, Object> reviewData);

    @Multipart
    @POST("api/tours/visual-search/")
    Call<List<Tour>> searchToursByImage(@Part MultipartBody.Part image);

    @POST("api/chatbot/chat/")
    Call<com.example.myapplication.data.model.ChatbotResponse> chatWithBot(@Body com.example.myapplication.data.model.ChatbotRequest request);
}