package com.example.myapplication;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InvoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_invoice);

        // Xử lý khoảng cách an toàn với thanh hệ thống (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Xử lý nút quay lại
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Nhận dữ liệu truyền từ Intent
        String tourTitle = getIntent().getStringExtra("tour_title");
        long totalPrice = getIntent().getLongExtra("total_price", 0);

        android.widget.TextView tvTourTitle = findViewById(R.id.tv_invoice_tour_title);
        android.widget.TextView tvTotalPrice = findViewById(R.id.tv_invoice_total_price);

        if (tvTourTitle != null && tourTitle != null && !tourTitle.isEmpty()) {
            tvTourTitle.setText(tourTitle);
        }

        if (tvTotalPrice != null && totalPrice > 0) {
            tvTotalPrice.setText("Tổng tiền: " + formatVnd(totalPrice));
        }
    }

    private String formatVnd(long price) {
        String raw = String.valueOf(price);
        StringBuilder sb = new StringBuilder();
        int len = raw.length();
        for (int i = 0; i < len; i++) {
            sb.append(raw.charAt(i));
            int remaining = len - i - 1;
            if (remaining > 0 && remaining % 3 == 0) {
                sb.append('.');
            }
        }
        return sb + "đ";
    }
}
