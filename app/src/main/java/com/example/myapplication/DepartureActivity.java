package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.model.TourDeparture;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class DepartureActivity extends AppCompatActivity {

    // ===== Số lượng mặc định =====
    private int adultCount  = 1;
    private int childCount  = 0;
    private int infantCount = 0;

    private long adultPrice  = 5_490_000L;
    private long childPrice  = 0L;
    private long infantPrice = 0L;

    // Dữ liệu tour
    private String tourTitle = "";
    private int    tourId    = -1;
    // Ngày đang chọn (hiển thị trên chip hoặc sau khi chọn từ Calendar)
    private String selectedDateStr = ""; // "dd-MM-yyyy" hoặc "yyyy-MM-dd"

    // ===== Views =====
    private TextView tvAdultCount, tvChildCount, tvInfantCount, tvTotalPrice;
    private TextView tvAdultPrice, tvChildPrice, tvInfantPrice;
    private TextView btnAdultMinus, btnAdultPlus;
    private TextView btnChildMinus, btnChildPlus;
    private TextView btnInfantMinus, btnInfantPlus;
    private TextView tvSelectedDate;
    private TextView tvSeatsLeft;

    // Chip containers (tối đa 3 ngày từ DB + "Tất cả")
    private LinearLayout chipDate1, chipDate2, chipDate3, chipDateAll;
    private TextView tvChipDate1, tvChipDate2, tvChipDate3, tvChipDateAll;
    // Danh sách ngày khởi hành lấy từ DB
    private final List<String> departureDates = new ArrayList<>(); // "yyyy-MM-dd"
    private final List<TourDeparture> departureList = new ArrayList<>();
    private int selectedChipIndex = -1;
    private String selectedTime = "";
    private LinearLayout layoutTimeChips;
    private static final String[] PROPOSED_TIMES = {"07:30", "09:00", "13:30", "15:00"};
    private static final String[] CUSTOM_TIMES = {"08:00", "14:00"};
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final SimpleDateFormat DB_FORMAT   = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DISP_FORMAT = new SimpleDateFormat("EE, dd-MM", new Locale("vi", "VN"));
    private static final SimpleDateFormat SHORT_FMT   = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_departure);

        // Nhận dữ liệu tour từ Intent
        if (getIntent() != null) {
            tourTitle  = getIntent().getStringExtra("tour_title");
            tourId     = getIntent().getIntExtra("tour_id", -1);
            long priceFromIntent = getIntent().getLongExtra("adult_price", 0L);
            if (priceFromIntent > 0) adultPrice = priceFromIntent;
        }

        if (tourTitle == null) tourTitle = "";
        // Tính giá trẻ em và trẻ nhỏ
        recalcChildInfantPrice();

        // Xử lý khoảng cách với thanh hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        updatePriceLabels();
        updateCounterUI();

        // Hiển thị tên tour
        if (!tourTitle.isEmpty()) {
            TextView tvTourName = findViewById(R.id.tv_tour_name);
            if (tvTourName != null) tvTourName.setText(tourTitle);
        }
        // Lắng nghe kết quả từ CalendarBottomSheet khi người dùng chọn ngày "Tất cả"
        getSupportFragmentManager().setFragmentResultListener(
                "date_request", this,
                (requestKey, result) -> {
                    int day   = result.getInt("day");
                    int month = result.getInt("month");
                    int year  = result.getInt("year");
                    // Tạo chuỗi ngày được chọn
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, day);
                    selectedDateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                    String display  = DISP_FORMAT.format(cal.getTime());
                    // Bỏ chọn chip cố định
                    selectedChipIndex = 3; // vẫn là chip "Tất cả" nhưng hiển thị ngày đã chọn
                    tvChipDateAll.setText(display);
                    populateTimeChips(CUSTOM_TIMES);
                    clampPassengerCount();
                    refreshChipUI();
                    updateCounterUI();
                });
        // Load ngày khởi hành từ Room DB
        loadDepartureDates();
    }

    // ===== Khởi tạo các View =====
    private void initViews() {
        tvAdultCount  = findViewById(R.id.tv_adult_count);
        tvChildCount  = findViewById(R.id.tv_child_count);
        tvInfantCount = findViewById(R.id.tv_infant_count);
        tvTotalPrice  = findViewById(R.id.tv_total_price);

        tvAdultPrice  = findViewById(R.id.tv_adult_price);
        tvChildPrice  = findViewById(R.id.tv_child_price);
        tvInfantPrice = findViewById(R.id.tv_infant_price);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        tvSeatsLeft    = findViewById(R.id.tv_seats_left);
        layoutTimeChips = findViewById(R.id.layout_time_chips);
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
        btnAdultPlus.setOnClickListener(v -> {
            int baseSeats = getAvailableSeatsForSelectedDate();
            int totalSelected = adultCount + childCount;
            if (totalSelected >= baseSeats) {
                Toast.makeText(this, "Không thể đặt quá số chỗ còn nhận (" + baseSeats + " chỗ)!", Toast.LENGTH_SHORT).show();
                return;
            }
            adultCount++;
            updateCounterUI();
        });

        // Bộ đếm Trẻ em (5-9t)
        btnChildMinus.setOnClickListener(v -> {
            if (childCount > 0) { childCount--; updateCounterUI(); }
        });
        btnChildPlus.setOnClickListener(v -> {
            int baseSeats = getAvailableSeatsForSelectedDate();
            int totalSelected = adultCount + childCount;
            if (totalSelected >= baseSeats) {
                Toast.makeText(this, "Không thể đặt quá số chỗ còn nhận (" + baseSeats + " chỗ)!", Toast.LENGTH_SHORT).show();
                return;
            }
            childCount++;
            updateCounterUI();
        });

        // Bộ đếm Trẻ nhỏ (<5t)
        btnInfantMinus.setOnClickListener(v -> {
            if (infantCount > 0) { infantCount--; updateCounterUI(); }
        });
        btnInfantPlus.setOnClickListener(v -> {
            if (infantCount >= 10) {
                Toast.makeText(this, "Tối đa 10 trẻ nhỏ!", Toast.LENGTH_SHORT).show();
                return;
            }
            infantCount++;
            updateCounterUI();
        });

        // Chips chọn ngày
        chipDate1.setOnClickListener(v -> selectChip(0));
        chipDate2.setOnClickListener(v -> selectChip(1));
        chipDate3.setOnClickListener(v -> selectChip(2));
        // Chip "Tất cả" mở CalendarBottomSheet
        chipDateAll.setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();
            CalendarBottomSheet sheet = CalendarBottomSheet.newInstance(
                    today.get(Calendar.DAY_OF_MONTH),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.YEAR)
            );
            sheet.show(getSupportFragmentManager(), "CalendarSheet");
        });

        // Nút Liên hệ tư vấn – mở Zalo ChillTour
        findViewById(R.id.btn_contact).setOnClickListener(v -> {
            try {
                android.net.Uri uri = android.net.Uri.parse("https://zalo.me/0858342303");
                Intent zaloIntent = new Intent(Intent.ACTION_VIEW, uri);
                zaloIntent.setPackage("com.zing.zalo");
                startActivity(zaloIntent);
            } catch (android.content.ActivityNotFoundException e) {
                // Zalo chưa cài – mở trình duyệt
                android.net.Uri uri = android.net.Uri.parse("https://zalo.me/0858342303");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        // Nút Đặt tour ngay
        findViewById(R.id.btn_book).setOnClickListener(v -> {
            if (selectedDateStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày khởi hành!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedTime.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn giờ khởi hành!", Toast.LENGTH_SHORT).show();
                return;
            }

            android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            int currentUserId = prefs.getInt("current_user_id", -1);
            if (currentUserId == -1) {
                Toast.makeText(this, "Vui lòng đăng nhập trước khi tiếp tục!", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.putExtra("return_to_caller", true);
                startActivityForResult(loginIntent, 123);
                return;
            }

            proceedToBookingInfo();
        });
    }
    private void loadDepartureDates() {
        executor.execute(() -> {
            List<TourDeparture> departures = new ArrayList<>();
            if (tourId > 0) {
                try {
                    AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                    List<TourDeparture> allDeps = db.tourDao().getDeparturesForTour(tourId);
                    Date today = new Date();
                    for (TourDeparture dep : allDeps) {
                        try {
                            Date depDate = DB_FORMAT.parse(dep.getDepartureDate());
                            if (depDate != null && !depDate.before(today)) {
                                departures.add(dep);
                            }
                        } catch (ParseException ignored) {}
                    }
                } catch (Exception ignored) {}
            }
            if (departures.isEmpty()) {
                Calendar cal = Calendar.getInstance();
                for (int i = 0; i < 3; i++) {
                    String dateStr = DB_FORMAT.format(cal.getTime());
                    departures.add(new TourDeparture(tourId, dateStr, 14, (double) adultPrice));
                    cal.add(Calendar.DAY_OF_MONTH, 7);
                }
            }
            // Giới hạn chỉ hiện tối đa 3 ngày trên chip
            while (departures.size() > 3) departures.remove(departures.size() - 1);
            mainHandler.post(() -> bindDepartureChips(departures));
        });
    }
    private void bindDepartureChips(List<TourDeparture> departures) {
        departureList.clear();
        departureList.addAll(departures);
        departureDates.clear();
        for (TourDeparture dep : departures) {
            departureDates.add(dep.getDepartureDate());
        }
        LinearLayout[] chips     = {chipDate1, chipDate2, chipDate3};
        TextView[]     chipTexts = {tvChipDate1, tvChipDate2, tvChipDate3};
        for (int i = 0; i < chips.length; i++) {
            if (i < departureDates.size()) {
                chips[i].setVisibility(android.view.View.VISIBLE);
                String formatted = formatDateForChip(departureDates.get(i));
                chipTexts[i].setText(formatted);
            } else {
                chips[i].setVisibility(android.view.View.GONE);
            }
        }
        // Mặc định chọn chip đầu tiên nếu có
        if (!departureDates.isEmpty()) {
            selectChip(0);
        }
    }
    private String formatDateForChip(String dbDate) {
        try {
            Date d = DB_FORMAT.parse(dbDate);
            if (d != null) return DISP_FORMAT.format(d);
        } catch (ParseException ignored) {}
        return dbDate;
    }
    private int getAvailableSeatsForSelectedDate() {
        if (selectedDateStr.isEmpty()) return 0;
        String normalizedDate = selectedDateStr;
        if (selectedDateStr.contains("-") && selectedDateStr.indexOf("-") == 2) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date date = sdf.parse(selectedDateStr);
                if (date != null) {
                    normalizedDate = DB_FORMAT.format(date);
                }
            } catch (ParseException ignored) {}
        }
        for (TourDeparture dep : departureList) {
            if (dep.getDepartureDate().equals(normalizedDate)) {
                return dep.getAvailableSeats();
            }
        }
        return 14;
    }
    private void clampPassengerCount() {
        int baseSeats = getAvailableSeatsForSelectedDate();
        int totalSelected = adultCount + childCount;
        if (totalSelected > baseSeats) {
            while (adultCount + childCount > baseSeats) {
                if (childCount > 0) {
                    childCount--;
                } else if (adultCount > 1) {
                    adultCount--;
                } else {
                    adultCount = Math.max(0, baseSeats);
                    break;
                }
            }
            Toast.makeText(this, "Số chỗ trống tối đa cho ngày này là: " + baseSeats + " chỗ", Toast.LENGTH_SHORT).show();
        }
    }
    private void selectChip(int index) {
        if (index >= 0 && index < departureDates.size()) {
            selectedChipIndex = index;
            selectedDateStr   = departureDates.get(index);
            updateSelectedDateLabel();
            populateTimeChips(PROPOSED_TIMES);
        }
        clampPassengerCount();
        refreshChipUI();
        updateCounterUI();
    }
    private void populateTimeChips(String[] times) {
        if (layoutTimeChips == null) return;
        layoutTimeChips.removeAllViews();

        // Kiểm tra nếu ngày chọn là hôm nay thì lọc bỏ giờ đã qua
        Calendar now = Calendar.getInstance();
        int nowHour   = now.get(Calendar.HOUR_OF_DAY);
        int nowMinute = now.get(Calendar.MINUTE);

        boolean isToday = false;
        if (!selectedDateStr.isEmpty()) {
            try {
                Date selDate = DB_FORMAT.parse(selectedDateStr);
                if (selDate != null) {
                    Calendar selCal = Calendar.getInstance();
                    selCal.setTime(selDate);
                    isToday = selCal.get(Calendar.YEAR)  == now.get(Calendar.YEAR)
                           && selCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                           && selCal.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH);
                }
            } catch (ParseException ignored) {}
        }

        // Xây danh sách giờ còn hiệu lực
        List<String> validTimes = new ArrayList<>();
        for (String time : times) {
            if (isToday) {
                String[] parts = time.split(":");
                if (parts.length == 2) {
                    try {
                        int h = Integer.parseInt(parts[0]);
                        int m = Integer.parseInt(parts[1]);
                        // Giữ lại nếu giờ:phút > thời điểm hiện tại
                        if (h > nowHour || (h == nowHour && m > nowMinute)) {
                            validTimes.add(time);
                        }
                        // else: bỏ qua – giờ đã qua
                    } catch (NumberFormatException ignored) {
                        validTimes.add(time);
                    }
                } else {
                    validTimes.add(time);
                }
            } else {
                validTimes.add(time);
            }
        }

        if (!validTimes.isEmpty()) {
            selectedTime = validTimes.get(0);
        } else {
            selectedTime = "";
        }

        for (int i = 0; i < validTimes.size(); i++) {
            final String time = validTimes.get(i);
            final TextView tvChip = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dpToPx(40)
            );
            params.setMargins(0, 0, dpToPx(8), 0);
            tvChip.setLayoutParams(params);
            tvChip.setGravity(android.view.Gravity.CENTER);
            tvChip.setPadding(dpToPx(16), 0, dpToPx(16), 0);
            tvChip.setText(time);
            tvChip.setTextSize(14);
            boolean isSelected = time.equals(selectedTime);
            tvChip.setBackgroundResource(isSelected
                    ? R.drawable.bg_chip_selected
                    : R.drawable.bg_chip_unselected);
            tvChip.setTextColor(isSelected ? 0xFF185FA5 : 0xFF777777);
            tvChip.setTypeface(null, isSelected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            tvChip.setClickable(true);
            tvChip.setFocusable(true);
            tvChip.setOnClickListener(v -> {
                selectedTime = time;
                for (int j = 0; j < layoutTimeChips.getChildCount(); j++) {
                    TextView child = (TextView) layoutTimeChips.getChildAt(j);
                    boolean sel = child.getText().toString().equals(selectedTime);
                    child.setBackgroundResource(sel
                            ? R.drawable.bg_chip_selected
                            : R.drawable.bg_chip_unselected);
                    child.setTextColor(sel ? 0xFF185FA5 : 0xFF777777);
                    child.setTypeface(null, sel ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
                }
            });
            layoutTimeChips.addView(tvChip);
        }

        // Nếu tất cả giờ trong ngày hôm nay đều đã qua, thông báo cho người dùng
        if (isToday && validTimes.isEmpty()) {
            Toast.makeText(this, "Các giờ khởi hành hôm nay đã qua, vui lòng chọn ngày khác!", Toast.LENGTH_LONG).show();
        }
    }
    
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
    private void refreshChipUI() {
        LinearLayout[] chips     = {chipDate1, chipDate2, chipDate3, chipDateAll};
        TextView[]     chipTexts = {tvChipDate1, tvChipDate2, tvChipDate3, tvChipDateAll};

        for (int i = 0; i < chips.length; i++) {
            boolean isSelected = (i == selectedChipIndex);
            chips[i].setBackgroundResource(isSelected
                    ? R.drawable.bg_chip_selected
                    : R.drawable.bg_chip_unselected);
            chipTexts[i].setTextColor(isSelected
                    ? 0xFF185FA5
                    : 0xFF777777);
            chipTexts[i].setTypeface(null, isSelected
                    ? android.graphics.Typeface.BOLD
                    : android.graphics.Typeface.NORMAL);
        }
    }
    private void updateSelectedDateLabel() {
        if (tvSelectedDate == null) return;
        if (selectedDateStr.isEmpty()) {
            tvSelectedDate.setText("Chưa chọn ngày khởi hành");
            tvSelectedDate.setTextColor(0xFF999999);
        } else {
            try {
                Date d = DB_FORMAT.parse(selectedDateStr);
                String display = d != null ? SHORT_FMT.format(d) : selectedDateStr;
                tvSelectedDate.setText("Ngày khởi hành: " + display);
            } catch (ParseException e) {
                tvSelectedDate.setText("Ngày khởi hành: " + selectedDateStr);
            }
            tvSelectedDate.setTextColor(0xFF185FA5);
        }
    }
    private void recalcChildInfantPrice() {
        childPrice  = adultPrice / 2;
        infantPrice = 0L;
    }
    private void updatePriceLabels() {
        if (tvAdultPrice  != null) tvAdultPrice.setText("x " + formatVnd(adultPrice));
        if (tvChildPrice  != null) tvChildPrice.setText("x " + formatVnd(childPrice));
        if (tvInfantPrice != null) tvInfantPrice.setText("Miễn phí");
    }
    private long calculateTotal() {
        return adultCount * adultPrice + childCount * childPrice + infantCount * infantPrice;
    }

    /**
     * Cập nhật số lượng hiển thị, trạng thái nút "−" và tổng tiền.
     */
    private void updateCounterUI() {
        tvAdultCount.setText(String.valueOf(adultCount));
        tvChildCount.setText(String.valueOf(childCount));
        tvInfantCount.setText(String.valueOf(infantCount));

        // Nút trừ chỉ active khi count > giới hạn tối thiểu
        setMinusActive(btnAdultMinus,  adultCount  > 1);
        setMinusActive(btnChildMinus,  childCount  > 0);
        setMinusActive(btnInfantMinus, infantCount > 0);

        // Tính và hiển thị tổng tiền real-time
        tvTotalPrice.setText(formatVnd(calculateTotal()));
        // Cập nhật nhãn ngày đang chọn
        updateSelectedDateLabel();
        // Hiển thị số chỗ còn nhận tương ứng sau khi giảm theo số khách đã chọn (không trừ trẻ nhỏ <5t)
        int baseSeats = getAvailableSeatsForSelectedDate();
        int totalSelected = adultCount + childCount;
        int seatsLeft = Math.max(0, baseSeats - totalSelected);
        if (tvSeatsLeft != null) {
            tvSeatsLeft.setText("Còn nhận " + seatsLeft + " chỗ");
        }
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
    private void proceedToBookingInfo() {
        long total = calculateTotal();
        int departureId = -1;
        for (TourDeparture dep : departureList) {
            if (dep.getDepartureDate().equals(selectedDateStr)) {
                departureId = dep.getId();
                break;
            }
        }
        if (departureId == -1 && !departureList.isEmpty()) {
            departureId = departureList.get(0).getId();
        }

        Intent intent = new Intent(this, BookingInfoActivity.class);
        intent.putExtra("tour_title",    tourTitle);
        intent.putExtra("tour_id",       tourId);
        intent.putExtra("departure_id",   departureId);
        intent.putExtra("departure_date", selectedDateStr);
        intent.putExtra("departure_time", selectedTime);
        intent.putExtra("adult_count",   adultCount);
        intent.putExtra("child_count",   childCount);
        intent.putExtra("infant_count",  infantCount);
        intent.putExtra("adult_price",   adultPrice);
        intent.putExtra("child_price",   childPrice);
        intent.putExtra("infant_price",  infantPrice);
        intent.putExtra("total_price",   total);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            proceedToBookingInfo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
