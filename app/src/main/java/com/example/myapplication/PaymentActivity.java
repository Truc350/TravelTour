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
    private boolean isInvoiceRequested = false;

    private String fullName = "";
    private String phone = "";
    private String email = "";
    private int adultCount = 1;
    private int childCount = 0;
    private int infantCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        // Xử lý System Bar Padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Đọc dữ liệu từ Intent
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
            fullName = intent.getStringExtra("full_name");
            phone = intent.getStringExtra("phone");
            email = intent.getStringExtra("email");
        }

        // Tạo mã đơn hàng ngẫu nhiên để tăng tính thực tế (hoặc dùng mã mẫu DL0091642)
        generateOrderId();

        initViews();
        setupListeners();
        displayData();
        selectPaymentMethod(0); // Chọn mặc định Chuyển khoản QR
    }

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
        // Nút Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Chọn phương thức thanh toán
        layoutPayQr.setOnClickListener(v -> selectPaymentMethod(0));
        layoutPayCard.setOnClickListener(v -> selectPaymentMethod(1));
        layoutPayMomo.setOnClickListener(v -> selectPaymentMethod(2));
        layoutPayAtm.setOnClickListener(v -> selectPaymentMethod(3));

        // Nút Xác nhận thanh toán
        btnConfirmPayment.setOnClickListener(v -> {
            if (rbPayQr.isChecked()) {
                showVietQrDialog();
            } else if (rbPayMomo.isChecked()) {
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

    private void selectPaymentMethod(int index) {
        // Reset tất cả các RadioButton và background
        rbPayQr.setChecked(index == 0);
        rbPayCard.setChecked(index == 1);
        rbPayMomo.setChecked(index == 2);
        rbPayAtm.setChecked(index == 3);

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

    private void showVietQrDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

        // Đổ thông tin chuyển khoản mẫu lên Dialog
        String formattedPrice = formatVnd(totalPrice);
        tvQrAmount.setText(formattedPrice);
        tvQrInfo.setText("Thanh toan tour " + orderId);

        // Tạo đường dẫn VietQR Quick Link
        String qrUrl = "https://img.vietqr.io/image/mbbank-113366668888-compact2.png?amount=" + totalPrice 
                + "&addInfo=Thanh%20toan%20tour%20" + orderId + "&accountName=LAM%20DAI";

        // Tải ảnh mã QR động bằng Glide
        Glide.with(this)
                .load(qrUrl)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        if (pbQrLoading != null) pbQrLoading.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
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

        // Đếm ngược 60 giây giả lập kiểm tra giao dịch thời gian thực
        CountDownTimer timer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secs = millisUntilFinished / 1000;
                if (tvPaymentStatus != null) {
                    tvPaymentStatus.setText("Đang chờ quét mã... (" + secs + "s)");
                }
            }

            @Override
            public void onFinish() {
                if (tvPaymentStatus != null) {
                    tvPaymentStatus.setText("Xác minh giao dịch thành công!");
                    tvPaymentStatus.setTextColor(Color.parseColor("#388E3C")); // Màu xanh lá
                }
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                        onPaymentSuccess();
                    }
                }, 1000);
            }
        };
        timer.start();

        dialog.setOnDismissListener(dialogInterface -> timer.cancel());

        if (btnQrConfirm != null) {
            btnQrConfirm.setOnClickListener(v -> {
                timer.cancel();
                dialog.dismiss();
                onPaymentSuccess();
            });
        }
    }

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
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                onPaymentSuccess();
            }
        }, 2000);
    }

    private void onPaymentSuccess() {
        // Gọi API tạo Booking mới trên Django Backend
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int currentUserId = prefs.getInt("current_user_id", 1); // mặc định user 1 (Ngọc Quyên)
        int depId = departureId > 0 ? departureId : 1; // mặc định departure 1 nếu chưa được gán chính xác
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault());
        String currentDate = sdf.format(new java.util.Date());

        com.example.myapplication.data.model.BookingRequest request = new com.example.myapplication.data.model.BookingRequest(
                currentUserId,
                depId,
                currentDate,
                departureTime != null && !departureTime.isEmpty() ? departureTime : "08:00",
                "CONFIRMED", // đã xác nhận/đã thanh toán
                totalPrice,
                tourId
        );

        apiService.createBooking(request).enqueue(new retrofit2.Callback<com.example.myapplication.data.model.BookingResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myapplication.data.model.BookingResponse> call, retrofit2.Response<com.example.myapplication.data.model.BookingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("DJANGO_API", "Tạo booking thành công trên server! ID = " + response.body().id);
                    Toast.makeText(PaymentActivity.this, "Đã lưu thông tin đặt tour thành công vào Django!", Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.e("DJANGO_API", "Không thể tạo booking trên server! Code = " + response.code());
                    Toast.makeText(PaymentActivity.this, "Lỗi phản hồi từ Django! Mã lỗi: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myapplication.data.model.BookingResponse> call, Throwable t) {
                android.util.Log.e("DJANGO_API", "Lỗi kết nối khi tạo booking: " + t.getMessage(), t);
                Toast.makeText(PaymentActivity.this, "Không thể kết nối với máy chủ Django!", Toast.LENGTH_LONG).show();
            }
        });

        BookedTripAdapter.TripItem newTrip = new BookedTripAdapter.TripItem(
                orderId,
                tourTitle,
                "Đã thanh toán",
                departureTime != null && !departureTime.isEmpty() ? departureTime : "08:00",
                "Dự kiến",
                "Điểm đi",
                "Điểm đến",
                adultCount + " người lớn" + (childCount > 0 ? ", " + childCount + " trẻ em" : ""),
                formatVnd(totalPrice),
                currentDate,
                false,
                "tour"
        );
        newTrip.tourId = tourId;


        // Lưu vào danh sách tĩnh của MyTripsFragment
        MyTripsFragment.additionalTrips.add(0, newTrip);

        Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();

        // Gửi email nếu tích vào ô xuất hóa đơn
        if (isInvoiceRequested) {
            String subject = "[Chill Tour] Xác nhận đặt tour & Yêu cầu xuất hóa đơn - Đơn hàng " + orderId;
            String emailBody = "Kính gửi Quý khách " + fullName + ",\n\n" +
                    "Cảm ơn Quý khách đã tin tưởng và lựa chọn dịch vụ của Chill Tour. Chúng tôi xin xác nhận đã tiếp nhận thanh toán của Quý khách với thông tin chi tiết như sau:\n\n" +
                    "--------------------------------------------------\n" +
                    "THÔNG TIN ĐƠN ĐẶT TOUR:\n" +
                    "- Mã đơn hàng: " + orderId + "\n" +
                    "- Tên tour: " + tourTitle + "\n" +
                    "- Số lượng khách: " + adultCount + " người lớn" + (childCount > 0 ? ", " + childCount + " trẻ em" : "") + "\n" +
                    "- Giờ khởi hành: " + (departureTime != null && !departureTime.isEmpty() ? departureTime : "08:00") + "\n" +
                    "- Tổng thanh toán: " + formatVnd(totalPrice) + "\n" +
                    "- Phương thức thanh toán: Chuyển khoản VietQR\n" +
                    "--------------------------------------------------\n\n" +
                    "Yêu cầu xuất hóa đơn đỏ (VAT) của Quý khách đã được tiếp nhận thành công. Bộ phận kế toán của Chill Tour sẽ sớm liên hệ với Quý khách qua email này để xác nhận thông tin doanh nghiệp (Tên công ty, MST, Địa chỉ) và tiến hành xuất hóa đơn điện tử trong vòng 24 giờ làm việc.\n\n" +
                    "Mọi thắc mắc cần hỗ trợ gấp, Quý khách vui lòng liên hệ hotline chăm sóc khách hàng của chúng tôi.\n\n" +
                    "Chúc Quý khách có một chuyến đi tuyệt vời!\n\n" +
                    "Trân trọng,\n" +
                    "Đội ngũ Chill Tour";

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(android.net.Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
            try {
                startActivity(Intent.createChooser(emailIntent, "Gửi email xác nhận hóa đơn qua..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "Không tìm thấy ứng dụng gửi mail trên thiết bị.", Toast.LENGTH_SHORT).show();
            }
        }

        // Chuyển sang MainActivity với tab Chuyến đi (MyTrips)
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        mainIntent.putExtra("navigate_to", "MyTrips");
        startActivity(mainIntent);
        finish();
    }
}
