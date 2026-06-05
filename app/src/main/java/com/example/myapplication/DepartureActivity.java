package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Activity hiển thị lịch khởi hành, chọn số lượng hành khách và tính tổng giá tour.
 */
public class DepartureActivity extends AppCompatActivity {

    // ===== Số lượng mặc định =====
    private int adultCount = 1;
    private int childCount = 0;
    private int infantCount = 1;

    // Giá mỗi người lớn và trẻ nhỏ (VND)
    private static final long ADULT_PRICE  = 5_490_000L;
    private static final long INFANT_PRICE = 5_490_000L;

    // Tên tour nhận từ Intent
    private String tourTitle = "";

    // ===== Views =====
    private TextView tvAdultCount, tvChildCount, tvInfantCount, tvTotalPrice;
    private TextView btnAdultMinus, btnAdultPlus;
    private TextView btnChildMinus, btnChildPlus;
    private TextView btnInfantMinus, btnInfantPlus;

    // Chips chọn ngày
    private LinearLayout chipDate1, chipDate2, chipDate3, chipDateAll;
    private TextView tvChipDate1, tvChipDate2, tvChipDate3, tvChipDateAll;
    private int selectedChipIndex = 1; // Ngày 28-06 được chọn mặc định

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_departure);

        // Nhận dữ liệu tour từ Intent
        if (getIntent() != null) {
            tourTitle = getIntent().getStringExtra("tour_title");
        }

        // Xử lý khoảng cách với thanh hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        updateCounterUI();
        selectChip(1); // Mặc định chọn ngày 28-06

        // Cập nhật tên tour nếu có nhận được từ Intent
        if (tourTitle != null && !tourTitle.isEmpty()) {
            TextView tvTourName = findViewById(R.id.tv_tour_name);
            if (tvTourName != null) {
                tvTourName.setText(tourTitle);
            }
        }
    }

    // ===== Khởi tạo các View =====
    private void initViews() {
        tvAdultCount  = findViewById(R.id.tv_adult_count);
        tvChildCount  = findViewById(R.id.tv_child_count);
        tvInfantCount = findViewById(R.id.tv_infant_count);
        tvTotalPrice  = findViewById(R.id.tv_total_price);

        btnAdultMinus  = findViewById(R.id.btn_adult_minus);
        btnAdultPlus   = findViewById(R.id.btn_adult_plus);
        btnChildMinus  = findViewById(R.id.btn_child_minus);
        btnChildPlus   = findViewById(R.id.btn_child_plus);
        btnInfantMinus = findViewById(R.id.btn_infant_minus);
        btnInfantPlus  = findViewById(R.id.btn_infant_plus);

        chipDate1   = findViewById(R.id.chip_date_1);
        chipDate2   = findViewById(R.id.chip_date_2);
        chipDate3   = findViewById(R.id.chip_date_3);
        chipDateAll = findViewById(R.id.chip_date_all);

        tvChipDate1   = findViewById(R.id.tv_chip_date_1);
        tvChipDate2   = findViewById(R.id.tv_chip_date_2);
        tvChipDate3   = findViewById(R.id.tv_chip_date_3);
        tvChipDateAll = findViewById(R.id.tv_chip_date_all);
    }

    // ===== Gắn sự kiện click =====
    private void setupListeners() {
        // Nút quay lại
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Bộ đếm Người lớn
        btnAdultMinus.setOnClickListener(v -> {
            if (adultCount > 1) { adultCount--; updateCounterUI(); }
        });
        btnAdultPlus.setOnClickListener(v -> { adultCount++; updateCounterUI(); });

        // Bộ đếm Trẻ em
        btnChildMinus.setOnClickListener(v -> {
            if (childCount > 0) { childCount--; updateCounterUI(); }
        });
        btnChildPlus.setOnClickListener(v -> { childCount++; updateCounterUI(); });

        // Bộ đếm Trẻ nhỏ
        btnInfantMinus.setOnClickListener(v -> {
            if (infantCount > 0) { infantCount--; updateCounterUI(); }
        });
        btnInfantPlus.setOnClickListener(v -> { infantCount++; updateCounterUI(); });

        // Chips chọn ngày
        chipDate1.setOnClickListener(v -> selectChip(0));
        chipDate2.setOnClickListener(v -> selectChip(1));
        chipDate3.setOnClickListener(v -> selectChip(2));
        chipDateAll.setOnClickListener(v -> selectChip(3));

        // Nút Liên hệ tư vấn
        findViewById(R.id.btn_contact).setOnClickListener(v ->
                Toast.makeText(this, "Đang kết nối tư vấn viên...", Toast.LENGTH_SHORT).show()
        );

        // Nút Yêu cầu đặt tour (Mở màn hình thông tin đặt tour bước 2/3)
        findViewById(R.id.btn_book).setOnClickListener(v -> {
            long total = adultCount * ADULT_PRICE + infantCount * INFANT_PRICE;
            Intent intent = new Intent(this, BookingInfoActivity.class);
            intent.putExtra("tour_title", tourTitle);
            intent.putExtra("adult_count", adultCount);
            intent.putExtra("child_count", childCount);
            intent.putExtra("infant_count", infantCount);
            intent.putExtra("total_price", total);
            startActivity(intent);
        });
    }

    /**
     * Cập nhật hiển thị chip ngày được chọn (đổi màu nền và chữ).
     */
    private void selectChip(int index) {
        selectedChipIndex = index;

        LinearLayout[] chips     = {chipDate1, chipDate2, chipDate3, chipDateAll};
        TextView[]     chipTexts = {tvChipDate1, tvChipDate2, tvChipDate3, tvChipDateAll};

        for (int i = 0; i < chips.length; i++) {
            boolean isSelected = (i == selectedChipIndex);

            chips[i].setBackgroundResource(isSelected
                    ? R.drawable.bg_chip_selected
                    : R.drawable.bg_chip_unselected);

            chipTexts[i].setTextColor(isSelected
                    ? 0xFF185FA5   // Xanh đậm khi chọn
                    : 0xFF777777); // Xám khi chưa chọn

            chipTexts[i].setTypeface(null, isSelected
                    ? android.graphics.Typeface.BOLD
                    : android.graphics.Typeface.NORMAL);
        }
    }

    /**
     * Cập nhật số lượng hiển thị, trạng thái nút "−" và tổng tiền.
     */
    private void updateCounterUI() {
        tvAdultCount.setText(String.valueOf(adultCount));
        tvChildCount.setText(String.valueOf(childCount));
        tvInfantCount.setText(String.valueOf(infantCount));

        // Nút trừ chỉ active khi count > giới hạn tối thiểu
        setMinusActive(btnAdultMinus, adultCount > 1);
        setMinusActive(btnChildMinus, childCount > 0);
        setMinusActive(btnInfantMinus, infantCount > 0);

        // Tính và hiển thị tổng tiền (người lớn + trẻ nhỏ)
        long total = adultCount * ADULT_PRICE + infantCount * INFANT_PRICE;
        tvTotalPrice.setText(formatVnd(total));
    }

    /**
     * Bật/tắt nút trừ bằng cách đổi background và alpha.
     */
    private void setMinusActive(TextView btn, boolean active) {
        btn.setBackgroundResource(active
                ? R.drawable.bg_count_btn
                : R.drawable.bg_count_btn_disabled);
        btn.setTextColor(0xFFFFFFFF);
        btn.setAlpha(active ? 1.0f : 0.45f);
    }

    /**
     * Định dạng số tiền theo kiểu Việt Nam: 12.300.000đ
     */
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
