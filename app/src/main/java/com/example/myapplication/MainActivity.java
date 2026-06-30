package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;

import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Màn hình chính của ứng dụng TravelTour.
 * Quản lý thanh điều hướng dưới cùng và hiển thị trang Account khi người dùng chọn tab tương ứng.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bật chế độ hiển thị toàn màn hình
        EdgeToEdge.enable(this);

        // Gắn giao diện XML vào Activity
        setContentView(R.layout.activity_main);

        // Xử lý khoảng cách an toàn với thanh hệ thống (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Đặt padding đáy bằng 0 để BottomNavigationView tự xử lý khoảng cách
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Nạp trang Home làm mặc định khi ứng dụng khởi chạy
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new Home())
                    .commit();
        }

        // Lấy tham chiếu đến thanh điều hướng dưới cùng
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        String navigateTo = getIntent().getStringExtra("navigate_to");
        if ("MyTrips".equals(navigateTo)) {
            bottomNavigation.setSelectedItemId(R.id.nav_trip);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new MyTripsFragment())
                    .commit();
        } else {
            // Đặt tab "Home" được chọn mặc định
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        // Xử lý deep link khi mở ứng dụng từ thông báo
        handleDeepLink(getIntent());

        // Lắng nghe sự kiện khi người dùng bấm vào từng tab điều hướng
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Xóa tất cả các Fragment khỏi back stack khi chuyển tab chính
            getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);

            if (itemId == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame, new Home())
                        .commit();
            } else if (itemId == R.id.nav_favorite) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame, new Wishlist())
                        .commit();
            } else if (itemId == R.id.nav_trip) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame, new MyTripsFragment())
                        .commit();
            } else if (itemId == R.id.nav_notification) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame, new Notification())
                        .commit();
            } else if (itemId == R.id.nav_account) {
                android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                int currentUserId = prefs.getInt("current_user_id", -1);
                if (currentUserId == -1) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    return false;
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame, new Account())
                        .commit();
            }

            return true;
        });
        ApiService apiService =
                RetrofitClient.getClient()
                        .create(ApiService.class);
        apiService.getTours().enqueue(new Callback<List<Tour>>() {

            @Override
            public void onResponse(Call<List<Tour>> call,
                                   Response<List<Tour>> response) {

                Log.d("DJANGO_API",
                        "HTTP CODE = " + response.code());

                if (response.isSuccessful()) {

                    List<Tour> tours = response.body();

                    if (tours != null) {

                        Log.d("DJANGO_API",
                                "So tour = " + tours.size());

                    } else {

                        Log.e("DJANGO_API",
                                "Response body NULL");
                    }

                } else {

                    Log.e("DJANGO_API",
                            "Response fail");
                }
            }

            @Override
            public void onFailure(Call<List<Tour>> call,
                                  Throwable t) {

                Log.e("DJANGO_API",
                        "Retrofit Error", t);
            }
        });

        // Initialize Firebase Messaging & Sync FCM Token
        initFirebaseMessaging();
    }

    private void initFirebaseMessaging() {
        // 1. Create Notification Channel
        com.example.myapplication.service.NotificationHelper.createNotificationChannel(this);

        // 2. Request Permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(
                        this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101
                );
            }
        }

        // 3. Fetch current FCM token and send to server
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        android.util.Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    android.util.Log.d("FCM", "FCM Token: " + token);

                    // Save token locally
                    android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                    prefs.edit().putString("fcm_token", token).apply();

                    // Sync to server if user is logged in
                    int userId = prefs.getInt("current_user_id", -1);
                    if (userId != -1) {
                        syncTokenToServer(userId, token);
                    }
                });
    }

    private void syncTokenToServer(int userId, String token) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        java.util.Map<String, Object> fields = new java.util.HashMap<>();
        fields.put("fcm_token", token);

        apiService.patchUser(userId, fields).enqueue(new retrofit2.Callback<com.example.myapplication.data.model.User>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myapplication.data.model.User> call, retrofit2.Response<com.example.myapplication.data.model.User> response) {
                if (response.isSuccessful()) {
                    android.util.Log.d("FCM", "Synced FCM token on MainActivity startup");
                } else {
                    android.util.Log.e("FCM", "Failed to sync token. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myapplication.data.model.User> call, Throwable t) {
                android.util.Log.e("FCM", "Error syncing token: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent == null) return;
        String action = intent.getStringExtra("action");
        if ("open_voucher".equals(action)) {
            android.util.Log.d("MainActivity", "Deep link matching: open_voucher");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new MyVouchers())
                    .addToBackStack(null)
                    .commit();
        }
    }
}
