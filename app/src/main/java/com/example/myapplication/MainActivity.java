package com.example.myapplication;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FrameLayout contentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Đặt padding đáy bằng 0 để BottomNavigationView tự xử lý khoảng cách
            // với thanh điều hướng hệ thống, tránh bị che khuất hoặc bị khuyết
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Lấy tham chiếu đến thanh chứa nội dung và thanh điều hướng
        contentFrame = findViewById(R.id.contentFrame);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        // Lắng nghe sự kiện khi người dùng bấm vào từng tab điều hướng
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            contentFrame.removeAllViews();

            if (itemId == R.id.nav_favorite) {
                // Nạp giao diện danh sách yêu thích vào contentFrame
                LayoutInflater.from(this).inflate(R.layout.wishlist, contentFrame, true);
                return true;
            } else {
                // Hiển thị màn hình tạm thời cho các tab khác
                String title = "";
                if (itemId == R.id.nav_home) {
                    title = "Trang chủ";
                } else if (itemId == R.id.nav_trip) {
                    title = "Chuyến đi";
                } else if (itemId == R.id.nav_notification) {
                    title = "Thông báo";
                } else if (itemId == R.id.nav_account) {
                    title = "Tài khoản";
                }

                TextView placeholderText = new TextView(this);
                placeholderText.setText(title);
                placeholderText.setTextSize(20f);
                placeholderText.setTextColor(0xFF333333);
                placeholderText.setGravity(Gravity.CENTER);
                
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
                contentFrame.addView(placeholderText, params);
                return true;
            }
        });

        // Đặt tab "Yêu thích" làm mặc định khi ứng dụng khởi chạy
        bottomNavigation.setSelectedItemId(R.id.nav_favorite);
    }
}