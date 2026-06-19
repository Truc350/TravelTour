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
    private int totalGuests = 1;
    private long totalPrice = 0;
    private String orderId = "";
    private boolean isInvoiceRequested = false;

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
            int adult = intent.getIntExtra("adult_count", 1);
            int child = intent.getIntExtra("child_count", 0);
            int infant = intent.getIntExtra("infant_count", 0);
            totalGuests = adult + child + infant;
            totalPrice = intent.getLongExtra("total_price", 10980000L);
            isInvoiceRequested = intent.getBooleanExtra("is_invoice_requested", false);
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
            Toast.makeText(this, "Đang xử lý giao dịch...", Toast.LENGTH_SHORT).show();

            // Chuyển sang màn hình hóa đơn thành công (InvoiceActivity)
            Intent invoiceIntent = new Intent(this, InvoiceActivity.class);
            invoiceIntent.putExtra("tour_title", tourTitle);
            invoiceIntent.putExtra("total_price", totalPrice);
            invoiceIntent.putExtra("order_id", orderId);
            invoiceIntent.putExtra("is_invoice_requested", isInvoiceRequested);
            startActivity(invoiceIntent);
            finish();
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
        tvPassengerCountSummary.setText("Tour Trọn Gói • " + totalGuests + " khách");

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
}
