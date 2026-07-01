package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;

/**
 * Activity bước 3/3: Chọn phương thức thanh toán và xác nhận đơn hàng.
 */
public class PaymentActivity extends AppCompatActivity {

    private TextView tvToolbarOrderId, tvPaymentDeadline, tvOrderIdSummary, tvPaymentTotalPrice, tvPassengerCountSummary;
    private Button btnConfirmPayment;

    // Các cụm phương thức thanh toán
    private LinearLayout layoutPayQr, layoutPayCard, layoutPayMomo, layoutPayAtm;
    private RadioButton rbPayQr, rbPayCard, rbPayMomo, rbPayAtm;

    private String tourTitle = "";
    private int tourId = -1;
    private int departureId = -1;
    private int totalGuests = 1;
    private long totalPrice = 0;
    private String orderId = "";
    private String departureTime = "";
    private String departureDate = "";
    private boolean isInvoiceRequested = false;

    private String fullName = "";
    private String phone = "";
    private String email = "";
    private int adultCount = 1;
    private int childCount = 0;
    private int infantCount = 0;

    // BƯỚC 2 TRONG LUỒNG THANH TOÁN: Khởi tạo PaymentActivity, nhận dữ liệu và chuẩn bị UI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        // Xử lý System Bar Padding để tránh UI đè lên status/navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // BƯỚC 2.1: Nhận toàn bộ dữ liệu đơn hàng được truyền qua Intent từ BookingInfoActivity.java
        Intent intent = getIntent();
        if (intent != null) {
            tourTitle = intent.getStringExtra("tour_title");
            tourId = intent.getIntExtra("tour_id", -1);
            departureId = intent.getIntExtra("departure_id", -1);
            adultCount = intent.getIntExtra("adult_count", 1);
            childCount = intent.getIntExtra("child_count", 0);
            infantCount = intent.getIntExtra("infant_count", 0);
            totalGuests = adultCount + childCount + infantCount;
            totalPrice = intent.getLongExtra("total_price", 10980000L);
            isInvoiceRequested = intent.getBooleanExtra("is_invoice_requested", false);
            departureTime = intent.getStringExtra("departure_time");
            departureDate = intent.getStringExtra("departure_date");
            fullName = intent.getStringExtra("full_name");
            phone = intent.getStringExtra("phone");
            email = intent.getStringExtra("email");
        }

        // BƯỚC 2.2: Tạo mã đơn hàng client ngẫu nhiên (DL0xxxxxxx) phục vụ hiển thị UI cục bộ
        generateOrderId();

        // Ánh xạ các thành phần giao diện từ layout activity_payment.xml
        initViews();
        // Đăng ký các sự kiện tương tác của người dùng
        setupListeners();
        // Hiển thị tóm tắt thông tin đơn hàng lên giao diện
        displayData();
        // Mặc định chọn phương thức thanh toán đầu tiên: Chuyển khoản QR VietQR
        selectPaymentMethod(0); 
    }

    // BƯỚC 2.2: Hàm tự động sinh mã đơn hàng hiển thị dạng DL0 + 7 chữ số ngẫu nhiên.
    // Lưu ý: Đây là mã định danh tạm thời trên client, ID thực của booking sẽ do Django Database tự tăng.
    private void generateOrderId() {
        int randomNum = (int) (Math.random() * 9000000) + 1000000; // 7 chữ số
        orderId = "DL0" + randomNum;
    }

    private void initViews() {
        tvToolbarOrderId = findViewById(R.id.tv_toolbar_order_id);
        tvPaymentDeadline = findViewById(R.id.tv_payment_deadline);
        tvOrderIdSummary = findViewById(R.id.tv_order_id_summary);
        tvPaymentTotalPrice = findViewById(R.id.tv_payment_total_price);
        tvPassengerCountSummary = findViewById(R.id.tv_passenger_count_summary);
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);

        layoutPayQr = findViewById(R.id.layout_pay_qr);
        layoutPayCard = findViewById(R.id.layout_pay_card);
        layoutPayMomo = findViewById(R.id.layout_pay_momo);
        layoutPayAtm = findViewById(R.id.layout_pay_atm);

        rbPayQr = findViewById(R.id.rb_pay_qr);
        rbPayCard = findViewById(R.id.rb_pay_card);
        rbPayMomo = findViewById(R.id.rb_pay_momo);
        rbPayAtm = findViewById(R.id.rb_pay_atm);
    }

    private void setupListeners() {
        // Nút quay lại (Back) màn hình trước
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // BƯỚC 3: Người dùng chọn phương thức thanh toán bằng cách nhấn vào các LinearLayout tương ứng
        layoutPayQr.setOnClickListener(v -> selectPaymentMethod(0));   // Chuyển khoản QR VietQR
        layoutPayCard.setOnClickListener(v -> selectPaymentMethod(1)); // Thẻ tín dụng quốc tế
        layoutPayMomo.setOnClickListener(v -> selectPaymentMethod(2)); // Ví điện tử MoMo
        layoutPayAtm.setOnClickListener(v -> selectPaymentMethod(3));  // Thẻ ATM nội địa

        // BƯỚC 4: Người dùng nhấn nút "Xác nhận thanh toán"
        btnConfirmPayment.setOnClickListener(v -> {
            if (rbPayQr.isChecked()) {
                // Nếu chọn QR -> Chuyển đến BƯỚC 4a: Hiển thị dialog VietQR để quét mã
                showVietQrDialog();
            } else if (rbPayMomo.isChecked()) {
                // Nếu chọn các ví/thẻ -> Chuyển đến BƯỚC 4b: Giả lập kết nối cổng thanh toán
                showProcessingPaymentDialog("Ví MoMo");
            } else if (rbPayCard.isChecked()) {
                showProcessingPaymentDialog("Thẻ tín dụng");
            } else if (rbPayAtm.isChecked()) {
                showProcessingPaymentDialog("Thẻ ATM");
            } else {
                showProcessingPaymentDialog("Cổng thanh toán");
            }
        });
    }

    private void displayData() {
        // Hiển thị mã đơn hàng
        tvToolbarOrderId.setText(orderId);
        tvOrderIdSummary.setText("Mã đơn: " + orderId);

        // Hiển thị tổng tiền
        String formattedPrice = formatVnd(totalPrice);
        tvPaymentTotalPrice.setText(formattedPrice);
        btnConfirmPayment.setText("Xác nhận thanh toán " + formattedPrice);

        // Hiển thị số lượng khách
        if (departureTime != null && !departureTime.isEmpty()) {
            tvPassengerCountSummary.setText("Tour Trọn Gói • " + totalGuests + " khách • " + departureTime);
        } else {
            tvPassengerCountSummary.setText("Tour Trọn Gói • " + totalGuests + " khách");
        }

        // Tính và hiển thị thời hạn thanh toán (24 giờ kể từ thời điểm hiện tại)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 24);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        String deadlineStr = sdf.format(cal.getTime());
        tvPaymentDeadline.setText(deadlineStr);
    }

    // BƯỚC 3.1: Đổi trạng thái RadioButton và background viền của các item phương thức thanh toán
    private void selectPaymentMethod(int index) {
        // Reset tất cả các RadioButton: Chỉ checked phần tử trùng với index được truyền vào
        rbPayQr.setChecked(index == 0);
        rbPayCard.setChecked(index == 1);
        rbPayMomo.setChecked(index == 2);
        rbPayAtm.setChecked(index == 3);

        // Thay đổi background drawable tương ứng (bg_payment_item_selected là viền xanh đậm, bg_payment_item_unselected là viền xám nhạt)
        layoutPayQr.setBackgroundResource(index == 0 ? R.drawable.bg_payment_item_selected : R.drawable.bg_payment_item_unselected);
        layoutPayCard.setBackgroundResource(index == 1 ? R.drawable.bg_payment_item_selected : R.drawable.bg_payment_item_unselected);
        layoutPayMomo.setBackgroundResource(index == 2 ? R.drawable.bg_payment_item_selected : R.drawable.bg_payment_item_unselected);
        layoutPayAtm.setBackgroundResource(index == 3 ? R.drawable.bg_payment_item_selected : R.drawable.bg_payment_item_unselected);
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
        return sb + " đ";
    }

    // BƯỚC 4a: Thanh toán QR VietQR
    // Hiển thị Dialog chứa mã QR động được lấy từ API VietQR, cho phép người dùng copy thông tin TK
    private void showVietQrDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Nạp layout dialog_vietqr.xml vào view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_vietqr, null);
        builder.setView(dialogView);

        ImageView imgVietQrCode = dialogView.findViewById(R.id.img_vietqr_code);
        ProgressBar pbQrLoading = dialogView.findViewById(R.id.pb_qr_loading);
        TextView tvPaymentStatus = dialogView.findViewById(R.id.tv_payment_status);
        TextView tvQrAccountNo = dialogView.findViewById(R.id.tv_qr_account_no);
        TextView tvQrAccountName = dialogView.findViewById(R.id.tv_qr_account_name);
        TextView tvQrAmount = dialogView.findViewById(R.id.tv_qr_amount);
        TextView tvQrInfo = dialogView.findViewById(R.id.tv_qr_info);

        View btnCopyAccountNo = dialogView.findViewById(R.id.btn_copy_account_no);
        View btnCopyAccountName = dialogView.findViewById(R.id.btn_copy_account_name);
        View btnCopyAmount = dialogView.findViewById(R.id.btn_copy_amount);
        View btnCopyInfo = dialogView.findViewById(R.id.btn_copy_info);
        Button btnQrConfirm = dialogView.findViewById(R.id.btn_qr_confirm);

        // Hiển thị các thông tin chuyển khoản tương ứng
        String formattedPrice = formatVnd(totalPrice);
        tvQrAmount.setText(formattedPrice);
        tvQrInfo.setText("Thanh toan tour " + orderId);

        // Tạo URL động của API VietQR Quick Link (sử dụng VietQR API để sinh QR Code có sẵn Số tiền, Tên TK, Số TK, Nội dung chuyển khoản)
        String qrUrl = "https://img.vietqr.io/image/mbbank-113366668888-compact2.png?amount=" + totalPrice 
                + "&addInfo=Thanh%20toan%20tour%20" + orderId + "&accountName=LAM%20DAI";

        // Sử dụng thư viện Glide để tải ảnh QR code từ URL trên mạng về và gán vào ImageView
        Glide.with(this)
                .load(qrUrl)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        // Ẩn ProgressBar nếu tải ảnh thất bại
                        if (pbQrLoading != null) pbQrLoading.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        // Ẩn ProgressBar khi ảnh đã tải thành công và hiển thị lên ImageView
                        if (pbQrLoading != null) pbQrLoading.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imgVietQrCode);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Xử lý sự kiện Sao chép
        View.OnClickListener copyListener = v -> {
            String textToCopy = "";
            String label = "";
            int id = v.getId();
            if (id == R.id.btn_copy_account_no) {
                textToCopy = "113366668888";
                label = "Số tài khoản";
            } else if (id == R.id.btn_copy_account_name) {
                textToCopy = "LAM DAI";
                label = "Chủ tài khoản";
            } else if (id == R.id.btn_copy_amount) {
                textToCopy = String.valueOf(totalPrice);
                label = "Số tiền";
            } else if (id == R.id.btn_copy_info) {
                textToCopy = "Thanh toan tour " + orderId;
                label = "Nội dung chuyển khoản";
            }

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(label, textToCopy);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Đã sao chép " + label + " vào bộ nhớ tạm!", Toast.LENGTH_SHORT).show();
            }
        };

        if (btnCopyAccountNo != null) btnCopyAccountNo.setOnClickListener(copyListener);
        if (btnCopyAccountName != null) btnCopyAccountName.setOnClickListener(copyListener);
        if (btnCopyAmount != null) btnCopyAmount.setOnClickListener(copyListener);
        if (btnCopyInfo != null) btnCopyInfo.setOnClickListener(copyListener);

        // Đặt trạng thái chờ quét mã chuyển khoản
        if (tvPaymentStatus != null) {
            tvPaymentStatus.setText("Đang chờ quét mã chuyển khoản...");
        }

        if (btnQrConfirm != null) {
            btnQrConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                onPaymentSuccess();
            });
        }
    }

    // BƯỚC 4b: Thanh toán ví điện tử, thẻ ATM/Tín dụng (Giả lập xử lý cổng thanh toán trực tuyến)
    private void showProcessingPaymentDialog(String methodName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 50, 60, 50);
        layout.setGravity(android.view.Gravity.CENTER);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, 0, 30);

        TextView textView = new TextView(this);
        textView.setText("Đang kết nối tới cổng thanh toán " + methodName + "...");
        textView.setTextSize(14);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setTextColor(Color.parseColor("#2D3748"));

        layout.addView(progressBar);
        layout.addView(textView);
        builder.setView(layout);
        // Thiết lập không cho phép người dùng tự tắt dialog (phải chờ hoàn tất)
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // BƯỚC 4.2b: Chờ 2000ms (2 giây) giả lập kết nối và phản hồi từ cổng thanh toán thành công, tự động chuyển đến BƯỚC 5
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                onPaymentSuccess();
            }
        }, 2000);
    }

    // BƯỚC 5: Xử lý sau khi thanh toán thành công (Bao gồm gọi API Django, lưu chuyến đi local và chuyển màn hình)
    private void onPaymentSuccess() {
        // BƯỚC 5.1: Khởi tạo ApiService từ RetrofitClient để chuẩn bị thực hiện REST API call đến Django Server
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        
        // BƯỚC 5.2: Lấy thông tin ID người dùng hiện tại đang lưu trong bộ nhớ SharedPreferences ("UserSession")
        android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int currentUserId = prefs.getInt("current_user_id", -1); // mặc định -1 nếu là khách vãng lai
        
        int depId = departureId > 0 ? departureId : 1; // mặc định chuyến đi ID 1 nếu intent không truyền đúng
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault());
        String currentDate = sdf.format(new java.util.Date());

        // Lấy lại các thông tin booking được lưu trong Intent từ BookingInfoActivity
        String voucherCode = getIntent().getStringExtra("voucher_code");
        if (voucherCode == null) {
            voucherCode = "";
        }

        String customerName = getIntent().getStringExtra("full_name");
        String customerPhone = getIntent().getStringExtra("phone");
        String customerEmail = getIntent().getStringExtra("email");
        boolean isInvoiceRequested = getIntent().getBooleanExtra("is_invoice_requested", false);

        // BƯỚC 5.3: Tạo đối tượng DTO BookingRequest (Data Transfer Object) chứa thông tin booking để gửi lên API
        com.example.myapplication.data.model.BookingRequest request = new com.example.myapplication.data.model.BookingRequest(
                currentUserId,
                depId,
                currentDate,
                departureTime != null && !departureTime.isEmpty() ? departureTime : "08:00",
                "CONFIRMED", // Trạng thái đặt tour: Đã thanh toán (CONFIRMED)
                totalPrice,
                tourId,
                voucherCode,
                customerName,
                customerPhone,
                customerEmail,
                isInvoiceRequested
        );

        // BƯỚC 5.4: Sử dụng Retrofit gửi POST Request lên endpoint '/api/bookings/' của Django Backend
        // Gọi bất đồng bộ (Asynchronous) thông qua enqueue()
        apiService.createBooking(request).enqueue(new retrofit2.Callback<com.example.myapplication.data.model.BookingResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myapplication.data.model.BookingResponse> call, retrofit2.Response<com.example.myapplication.data.model.BookingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Thành công: Server lưu vào CSDL và sinh khóa chính auto-increment (id) trả về
                    android.util.Log.d("DJANGO_API", "Tạo booking thành công trên server! ID = " + response.body().id);
                } else {
                    // Thất bại từ phía server (ví dụ: HTTP 400 Bad Request, Validation error)
                    android.util.Log.e("DJANGO_API", "Không thể tạo booking trên server! Code = " + response.code());
                    Toast.makeText(PaymentActivity.this, "Lỗi phản hồi từ Django! Mã lỗi: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myapplication.data.model.BookingResponse> call, Throwable t) {
                // Thất bại do mất kết nối mạng hoặc server Django không chạy
                android.util.Log.e("DJANGO_API", "Lỗi kết nối khi tạo booking: " + t.getMessage(), t);
                Toast.makeText(PaymentActivity.this, "Không thể kết nối với máy chủ Django!", Toast.LENGTH_LONG).show();
            }
        });

        // Định dạng lại ngày khởi hành để hiển thị trên Card danh sách local
        String formattedDepDate = currentDate;
        if (departureDate != null && !departureDate.isEmpty()) {
            try {
                java.text.SimpleDateFormat dbFmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Date d = dbFmt.parse(departureDate);
                if (d != null) {
                    java.text.SimpleDateFormat tabFmt = new java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault());
                    formattedDepDate = tabFmt.format(d);
                }
            } catch (Exception e) {
                try {
                    java.text.SimpleDateFormat dbFmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                    java.util.Date d = dbFmt.parse(departureDate);
                    if (d != null) {
                        java.text.SimpleDateFormat tabFmt = new java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault());
                        formattedDepDate = tabFmt.format(d);
                    }
                } catch (Exception ignored) {}
            }
        }

        // BƯỚC 5.5: Tạo chuyến đi local bằng đối tượng BookedTripAdapter.TripItem
        BookedTripAdapter.TripItem newTrip = new BookedTripAdapter.TripItem(
                orderId,
                tourTitle,
                "Đã thanh toán",
                departureTime != null && !departureTime.isEmpty() ? departureTime : "08:00",
                "Dự kiến",
                "Thời gian đi",
                "Thời gian đến",
                adultCount + " người lớn" + (childCount > 0 ? ", " + childCount + " trẻ em" : ""),
                formatVnd(totalPrice),
                formattedDepDate,
                false, // isHistory = false (Chuyến đi sắp tới)
                "tour"
        );
        newTrip.tourId = tourId;
        newTrip.userId = currentUserId;

        // BƯỚC 5.6: Thêm chuyến đi mới vào đầu danh sách tĩnh MyTripsFragment.additionalTrips
        // Danh sách tĩnh này chia sẻ dữ liệu tức thời trong ứng dụng Android (sẽ bị mất khi tắt app, chuyến đi lưu trữ thực tế trên Django DB)
        MyTripsFragment.additionalTrips.add(0, newTrip);

        Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();

        // BƯỚC 5.7: Nếu người dùng yêu cầu hóa đơn, hiển thị Toast (phần gửi email thực tế do server Django xử lý thông qua SMTP)
        if (isInvoiceRequested) {
            Toast.makeText(this, "Hóa đơn điện tử đang được gửi đến email " + email + "!", Toast.LENGTH_LONG).show();
        }

        // BƯỚC 5.8: Khởi động MainActivity và tự động điều hướng sang tab "Chuyến đi" (MyTripsFragment)
        // Dùng flag CLEAR_TOP | NEW_TASK để dọn dẹp các Activity trung gian (BookingInfoActivity, PaymentActivity)
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        mainIntent.putExtra("navigate_to", "MyTrips"); // Gửi tín hiệu điều hướng tab
        startActivity(mainIntent);
        finish(); // Đóng PaymentActivity
    }
}
