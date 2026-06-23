package com.example.myapplication;

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
    }
}
