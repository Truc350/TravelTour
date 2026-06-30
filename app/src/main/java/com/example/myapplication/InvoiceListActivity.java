package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.model.BookingResponse;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceListActivity extends AppCompatActivity {

    private RecyclerView rvInvoiceList;
    private LinearLayout layoutEmpty;
    private InvoiceAdapter adapter;
    private List<BookingResponse> invoiceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_invoice_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nút Back
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        rvInvoiceList = findViewById(R.id.rv_invoice_list);
        layoutEmpty = findViewById(R.id.layout_empty_invoices);

        rvInvoiceList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvoiceAdapter(invoiceList);
        rvInvoiceList.setAdapter(adapter);

        loadInvoicesFromServer();
    }

    private void loadInvoicesFromServer() {
        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("current_user_id", -1);

        if (currentUserId == -1) {
            showEmptyState(true);
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBookings().enqueue(new Callback<List<BookingResponse>>() {
            @Override
            public void onResponse(Call<List<BookingResponse>> call, Response<List<BookingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingResponse> bookings = response.body();
                    invoiceList.clear();
                    for (BookingResponse b : bookings) {
                        // Lọc lấy các hoá đơn (đặt chỗ thành công) thuộc về user hiện tại
                        if (b.user == currentUserId) {
                            invoiceList.add(b);
                        }
                    }

                    if (invoiceList.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        // Sắp xếp danh sách hoá đơn theo ID giảm dần (mới nhất lên đầu)
                        java.util.Collections.sort(invoiceList, (b1, b2) -> Integer.compare(b2.id, b1.id));
                        showEmptyState(false);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Log.e("INVOICE_LIST", "Failed to load bookings, code = " + response.code());
                    Toast.makeText(InvoiceListActivity.this, "Không thể tải danh sách hóa đơn từ máy chủ", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<List<BookingResponse>> call, Throwable t) {
                Log.e("INVOICE_LIST", "Error fetching bookings: " + t.getMessage(), t);
                Toast.makeText(InvoiceListActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvInvoiceList.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvInvoiceList.setVisibility(View.VISIBLE);
        }
    }

    // ==========================================
    // RECYCLERVIEW ADAPTER & HOLDER
    // ==========================================
    private class InvoiceAdapter extends RecyclerView.Adapter<InvoiceViewHolder> {

        private List<BookingResponse> list;

        public InvoiceAdapter(List<BookingResponse> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
            return new InvoiceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
            BookingResponse booking = list.get(position);
            holder.bind(booking);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private class InvoiceViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCode, tvStatus, tvTourTitle, tvDate, tvPrice;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tv_item_invoice_code);
            tvStatus = itemView.findViewById(R.id.tv_item_invoice_status);
            tvTourTitle = itemView.findViewById(R.id.tv_item_invoice_tour_title);
            tvDate = itemView.findViewById(R.id.tv_item_invoice_date);
            tvPrice = itemView.findViewById(R.id.tv_item_invoice_price);
        }

        public void bind(BookingResponse booking) {
            String code = "DL0" + booking.id;
            tvCode.setText(code);

            String statusStr = "Chờ duyệt";
            if ("CONFIRMED".equalsIgnoreCase(booking.status)) {
                statusStr = "Đã thanh toán";
                tvStatus.setBackgroundResource(android.R.color.transparent);
                tvStatus.setBackgroundColor(0xFFE8F5E9); // light green
                tvStatus.setTextColor(0xFF4CAF50); // green
            } else if ("CANCELLED".equalsIgnoreCase(booking.status)) {
                statusStr = "Đã hủy";
                tvStatus.setBackgroundResource(android.R.color.transparent);
                tvStatus.setBackgroundColor(0xFFFFEBEE); // light red
                tvStatus.setTextColor(0xFFE53935); // red
            } else if ("COMPLETED".equalsIgnoreCase(booking.status)) {
                statusStr = "Đã hoàn thành";
                tvStatus.setBackgroundResource(android.R.color.transparent);
                tvStatus.setBackgroundColor(0xFFE3F2FD); // light blue
                tvStatus.setTextColor(0xFF1E88E5); // blue
            }
            tvStatus.setText(statusStr);

            String title = "Tour Du Lịch";
            String date = "Chưa rõ";
            if (booking.departureDetail != null) {
                if (booking.departureDetail.tourDetail != null) {
                    title = booking.departureDetail.tourDetail.getTitle();
                }
                if (booking.departureDetail.departureDate != null) {
                    date = booking.departureDetail.departureDate;
                }
            } else if (booking.bookingDate != null) {
                date = booking.bookingDate;
            }

            tvTourTitle.setText(title);
            tvDate.setText("Khởi hành: " + date);
            tvPrice.setText(formatVnd((long) booking.totalPrice));

            final String finalTitle = title;
            final String finalDate = date;
            final String finalStatus = statusStr;

            String imageUrl = null;
            if (booking.departureDetail != null && booking.departureDetail.tourDetail != null) {
                com.example.myapplication.data.model.Tour t = booking.departureDetail.tourDetail;
                if (t.getImages() != null && !t.getImages().isEmpty()) {
                    imageUrl = t.getImages().get(0).getImageUrl();
                }
            }
            final String finalImageUrl = imageUrl;

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(InvoiceListActivity.this, InvoiceActivity.class);
                intent.putExtra("booking_id", booking.id);
                intent.putExtra("tour_title", finalTitle);
                intent.putExtra("total_price", (long) booking.totalPrice);
                intent.putExtra("is_invoice_requested", booking.isInvoiceRequested);
                intent.putExtra("departure_date", finalDate);
                intent.putExtra("departure_time", booking.departureHour);
                intent.putExtra("status", finalStatus);
                intent.putExtra("tour_image_url", finalImageUrl);
                startActivity(intent);
            });
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
}
