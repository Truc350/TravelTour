package com.example.myapplication;

import android.graphics.Bitmap;
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
        boolean isInvoiceRequested = getIntent().getBooleanExtra("is_invoice_requested", false);
        int bookingId = getIntent().getIntExtra("booking_id", 0);
        String departureDate = getIntent().getStringExtra("departure_date");
        String departureTime = getIntent().getStringExtra("departure_time");
        String tourImageUrl = getIntent().getStringExtra("tour_image_url");

        if (departureTime == null || departureTime.isEmpty()) {
            departureTime = "08:00";
        }
        if (departureDate == null || departureDate.isEmpty()) {
            departureDate = "Chưa rõ";
        }

        android.widget.TextView tvTourTitle = findViewById(R.id.tv_invoice_tour_title);
        android.widget.TextView tvTotalPrice = findViewById(R.id.tv_invoice_total_price);

        if (tvTourTitle != null && tourTitle != null && !tourTitle.isEmpty()) {
            tvTourTitle.setText(tourTitle);
        }

        if (tvTotalPrice != null && totalPrice > 0) {
            tvTotalPrice.setText("Tổng tiền: " + formatVnd(totalPrice));
        }

        // --- Tạo mã QR Code chuyến đi ---
        String bookingCode = "DL0" + bookingId;
        String confirmationCode = "CFM-" + (100000 + bookingId);
        String normalizedTitle = removeAccents(tourTitle != null ? tourTitle : "Tour Du Lich");
        String qrPayload = "=== VE DIEN TU TRAVELTOUR ===\n" +
                "Ma ve: " + bookingCode + "\n" +
                "Ma xac nhan: " + confirmationCode + "\n" +
                "Tour: " + normalizedTitle + "\n" +
                "Ngay khoi hanh: " + departureDate + "\n" +
                "Gio khoi hanh: " + departureTime + "\n" +
                "Gia ve: " + formatVnd(totalPrice) + "\n" +
                "Trang thai: Da thanh toan\n" +
                "============================";

        ImageView ivQrCode = findViewById(R.id.iv_invoice_qr);
        if (ivQrCode != null) {
            Bitmap qrBitmap = QrCodeGenerator.generateQrCode(qrPayload, 500, 500);
            if (qrBitmap != null) {
                ivQrCode.setImageBitmap(qrBitmap);
            }
        }

        android.widget.TextView tvInvoiceCode = findViewById(R.id.tv_invoice_code);
        if (tvInvoiceCode != null) {
            tvInvoiceCode.setText(bookingCode);
        }

        // --- Tải hình ảnh của Tour bằng Glide ---
        ImageView ivTourImage = findViewById(R.id.iv_invoice_tour_image);
        if (ivTourImage != null) {
            if (tourImageUrl != null && !tourImageUrl.isEmpty()) {
                if (tourImageUrl.startsWith("/")) {
                    tourImageUrl = "http://10.0.2.2:8000" + tourImageUrl;
                }
                com.bumptech.glide.Glide.with(this)
                        .load(tourImageUrl)
                        .placeholder(R.drawable.img)
                        .centerCrop()
                        .into(ivTourImage);
            } else {
                ivTourImage.setImageResource(R.drawable.img);
            }
        }

        // Xử lý hiển thị thông tin hóa đơn điện tử
        android.widget.LinearLayout layoutInvoiceDetails = findViewById(R.id.layout_invoice_details_block);
        if (isInvoiceRequested && layoutInvoiceDetails != null) {
            android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            String contact = prefs.getString("current_user_contact", "");
            if (!contact.isEmpty()) {
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                java.util.Map<String, String> userDetails = dbHelper.getUserDetails(contact);
                if (userDetails != null) {
                    layoutInvoiceDetails.setVisibility(android.view.View.VISIBLE);
                    
                    android.widget.TextView tvCustName = findViewById(R.id.tv_invoice_cust_name);
                    android.widget.TextView tvCustContact = findViewById(R.id.tv_invoice_cust_contact);
                    android.widget.TextView tvCompName = findViewById(R.id.tv_invoice_comp_name);
                    android.widget.TextView tvTaxCode = findViewById(R.id.tv_invoice_tax_code);
                    android.widget.TextView tvCompAddress = findViewById(R.id.tv_invoice_comp_address);
                    android.widget.TextView tvCompEmail = findViewById(R.id.tv_invoice_comp_email);

                    if (tvCustName != null && userDetails.get("name") != null) {
                        tvCustName.setText("Khách hàng: " + userDetails.get("name"));
                    }
                    if (tvCustContact != null && userDetails.get("contact") != null) {
                        tvCustContact.setText("Liên hệ: " + userDetails.get("contact"));
                    }
                    if (tvCompName != null && userDetails.get("invoice_company") != null) {
                        tvCompName.setText("Công ty: " + userDetails.get("invoice_company"));
                    }
                    if (tvTaxCode != null && userDetails.get("invoice_tax_code") != null) {
                        tvTaxCode.setText("Mã số thuế: " + userDetails.get("invoice_tax_code"));
                    }
                    if (tvCompAddress != null && userDetails.get("invoice_address") != null) {
                        tvCompAddress.setText("Địa chỉ: " + userDetails.get("invoice_address"));
                    }
                    if (tvCompEmail != null && userDetails.get("invoice_email") != null) {
                        tvCompEmail.setText("Email nhận HĐ: " + userDetails.get("invoice_email"));
                    }
                }
            }
        } else if (layoutInvoiceDetails != null) {
            layoutInvoiceDetails.setVisibility(android.view.View.GONE);
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

    private String removeAccents(String text) {
        if (text == null) return "";
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String temp = pattern.matcher(normalized).replaceAll("");
        return temp.replaceAll("đ", "d").replaceAll("Đ", "D");
    }
}
