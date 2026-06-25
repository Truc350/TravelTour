package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.data.model.VoucherHelper;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.myapplication.DatabaseHelper;


/**
 * Activity bước 2/3: Điền thông tin khách hàng và tiến hành đặt tour.
 */
public class BookingInfoActivity extends AppCompatActivity {

    private String selectedVoucherCode = "";

    private EditText etFullName, etPhone, etEmail, etOtherRequests;
    private TextView tvPassengerSummary, tvPriceSummary, tvDiscountSummary;
    private CheckBox cbRequestInvoice;
    private Button btnSubmitBooking;

    private String tourTitle = "";
    private int tourId = -1;
    private int departureId = -1;
    private int adultCount = 1;
    private int childCount = 0;
    private int infantCount = 0;
    private long totalPrice = 0;
    private long discountAmount = 0;

    private String departureTime = "";
    private String departureDate = "";
    private boolean isInvoiceRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_info);

        // Xử lý khoảng cách an toàn hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Đọc dữ liệu truyền từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            tourTitle = intent.getStringExtra("tour_title");
            tourId = intent.getIntExtra("tour_id", -1);
            departureId = intent.getIntExtra("departure_id", -1);
            adultCount = intent.getIntExtra("adult_count", 1);
            childCount = intent.getIntExtra("child_count", 0);
            infantCount = intent.getIntExtra("infant_count", 0);
            totalPrice = intent.getLongExtra("total_price", 5490000L);
            departureTime = intent.getStringExtra("departure_time");
            departureDate = intent.getStringExtra("departure_date");
            selectedVoucherCode = intent.getStringExtra("voucher_code");
            if (selectedVoucherCode == null) {
                selectedVoucherCode = "";
            }
            discountAmount = intent.getLongExtra("discount_amount", 0);
        }

        initViews();
        prefillUserInfo();
        setupListeners();
        displaySummary();


    }

    private void prefillUserInfo() {
        android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String contact = prefs.getString("current_user_contact", "");
        if (!contact.isEmpty()) {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            java.util.Map<String, String> userDetails = dbHelper.getUserDetails(contact);
            if (userDetails != null) {
                String name = userDetails.get("name");
                if (name != null && !name.isEmpty()) {
                    etFullName.setText(name);
                }
                if (contact.contains("@")) {
                    etEmail.setText(contact);
                } else {
                    String invEmail = userDetails.get("invoice_email");
                    if (invEmail != null && !invEmail.isEmpty()) {
                        etEmail.setText(invEmail);
                    }
                }
            }
        }
    }

    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etOtherRequests = findViewById(R.id.et_other_requests);

        tvPassengerSummary = findViewById(R.id.tv_passenger_summary);
        tvPriceSummary = findViewById(R.id.tv_price_summary);
        tvDiscountSummary = findViewById(R.id.tv_discount_summary);
        cbRequestInvoice = findViewById(R.id.cb_request_invoice);
        btnSubmitBooking = findViewById(R.id.btn_submit_booking);
    }

    private void setupListeners() {
        // Nút Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Real-time Phone validation
        etPhone.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String phone = s.toString().trim();
                if (phone.isEmpty()) {
                    etPhone.setError(null);
                } else if (!phone.startsWith("0")) {
                    etPhone.setError("Số điện thoại phải bắt đầu bằng số 0");
                } else if (phone.length() != 10) {
                    etPhone.setError("Số điện thoại phải có đúng 10 số");
                } else {
                    etPhone.setError(null);
                }
            }
        });

        // Real-time Email validation
        etEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String email = s.toString().trim();
                if (email.isEmpty()) {
                    etEmail.setError(null);
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Email không đúng định dạng");
                } else {
                    etEmail.setError(null);
                }
            }
        });

        // Nút Yêu cầu đặt tour
        btnSubmitBooking.setOnClickListener(v -> submitBooking());
    }

    private void displaySummary() {
        // Tạo chuỗi tóm tắt hành khách
        StringBuilder passengerStr = new StringBuilder();
        if (adultCount > 0) {
            passengerStr.append(adultCount).append(" người lớn");
        }
        if (childCount > 0) {
            if (passengerStr.length() > 0) passengerStr.append(", ");
            passengerStr.append(childCount).append(" trẻ em");
        }
        if (infantCount > 0) {
            if (passengerStr.length() > 0) passengerStr.append(", ");
            passengerStr.append(infantCount).append(" trẻ nhỏ");
        }
        tvPassengerSummary.setText(passengerStr.toString());

        // Hiển thị giảm giá nếu có voucher
        if (discountAmount > 0 && !selectedVoucherCode.isEmpty()) {
            if (tvDiscountSummary != null) {
                tvDiscountSummary.setVisibility(View.VISIBLE);
                tvDiscountSummary.setText("Giảm " + formatVnd(discountAmount) + " (" + selectedVoucherCode + ")");
            }
        } else {
            if (tvDiscountSummary != null) {
                tvDiscountSummary.setVisibility(View.GONE);
            }
        }

        // Cập nhật giá hiển thị
        long finalPrice = Math.max(0, totalPrice - discountAmount);
        tvPriceSummary.setText(formatVnd(finalPrice));
    }



    private void submitBooking() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        } else if (!phone.startsWith("0")) {
            etPhone.setError("Số điện thoại phải bắt đầu bằng số 0");
            etPhone.requestFocus();
            return;
        } else if (phone.length() != 10) {
            etPhone.setError("Số điện thoại phải có đúng 10 số");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không đúng định dạng");
            etEmail.requestFocus();
            return;
        }

        if (cbRequestInvoice != null) {
            isInvoiceRequested = cbRequestInvoice.isChecked();
        }

        long finalPrice = Math.max(0, totalPrice - discountAmount);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("tour_title", tourTitle);
        intent.putExtra("tour_id", tourId);
        intent.putExtra("departure_id", departureId);
        intent.putExtra("adult_count", adultCount);
        intent.putExtra("child_count", childCount);
        intent.putExtra("infant_count", infantCount);
        intent.putExtra("total_price", finalPrice);
        intent.putExtra("is_invoice_requested", isInvoiceRequested);
        intent.putExtra("departure_time", departureTime);
        intent.putExtra("departure_date", departureDate);
        intent.putExtra("full_name", fullName);
        intent.putExtra("phone", phone);
        intent.putExtra("email", email);
        intent.putExtra("voucher_code", selectedVoucherCode);
        startActivity(intent);
        finish();
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
