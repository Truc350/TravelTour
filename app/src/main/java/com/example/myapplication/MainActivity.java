package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Màn hình chính của ứng dụng TravelTour.
 * Chứa thanh điều hướng dưới cùng để chuyển đổi giữa các màn hình.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bật chế độ hiển thị toàn màn hình (vẽ xuyên qua thanh hệ thống)
        EdgeToEdge.enable(this);

        // Gắn giao diện XML vào Activity
        setContentView(R.layout.activity_main);

        // Xử lý khoảng cách an toàn với thanh hệ thống (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Đặt padding đáy bằng 0 để BottomNavigationView tự xử lý khoảng cách
            // với thanh điều hướng hệ thống, tránh bị che khuất hoặc bị khuyết
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Lấy tham chiếu đến thanh điều hướng dưới cùng
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        // Lắng nghe sự kiện khi người dùng bấm vào từng tab điều hướng
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Xử lý khi chọn tab Trang Chủ
                return true;

            } else if (itemId == R.id.nav_favorite) {
                // Xử lý khi chọn tab Yêu Thích
                return true;

            } else if (itemId == R.id.nav_trip) {
                // Xử lý khi chọn tab Chuyến Đi
                return true;

            } else if (itemId == R.id.nav_notification) {
                // Xử lý khi chọn tab Thông Báo
                return true;

            } else if (itemId == R.id.nav_account) {
                // Xử lý khi chọn tab Tài Khoản
                return true;
            }

            // Không xử lý nếu không khớp tab nào
            return false;
        });
    }
}