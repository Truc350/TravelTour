package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

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

        // Nạp trang Account làm mặc định khi ứng dụng khởi chạy
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new Account())
                    .commit();
        }

        // Lấy tham chiếu đến thanh điều hướng dưới cùng
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        // Đặt tab "Account" được chọn mặc định
        bottomNavigation.setSelectedItemId(R.id.nav_account);

        // Lắng nghe sự kiện khi người dùng bấm vào từng tab điều hướng
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Khi nhấn tab Account: nạp giao diện trang cá nhân
            if (itemId == R.id.nav_account) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame, new Account())
                        .commit();
            }

            // Trả về true để tab được sáng màu khi chọn
            return true;
        });
    }
}