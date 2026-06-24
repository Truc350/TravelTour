package com.example.myapplication.service;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.data.model.User;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM_Service";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String title = "";
        String body = "";

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        } 
        // Fallback to data payload if notification is null
        else if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("message");
        }

        if (title == null || title.isEmpty()) {
            title = "Thông báo mới";
        }
        if (body == null || body.isEmpty()) {
            body = "Bạn có một thông điệp mới từ Chill Tour.";
        }

        Log.d(TAG, "Message Notification Title: " + title);
        Log.d(TAG, "Message Notification Body: " + body);

        // Display the status bar notification
        NotificationHelper.showNotification(this, title, body);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // 1. Store the token locally in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        prefs.edit().putString("fcm_token", token).apply();

        // 2. If user is logged in, send token to Django server
        int userId = prefs.getInt("current_user_id", -1);
        if (userId != -1) {
            sendTokenToServer(userId, token);
        }
    }

    private void sendTokenToServer(int userId, String token) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Map<String, Object> fields = new HashMap<>();
        fields.put("fcm_token", token);

        apiService.patchUser(userId, fields).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM Token successfully synced to Django for userId: " + userId);
                } else {
                    Log.e(TAG, "Failed to sync FCM Token to Django. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error syncing FCM Token to Django: " + t.getMessage());
            }
        });
    }
}
