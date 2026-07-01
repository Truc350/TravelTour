package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * BƯỚC 5.5a: Adapter hiển thị danh sách các chuyến đi (vé tàu hoặc tour) đã đặt và lịch sử chuyến đi.
 * Vai trò trong luồng: Render các đối tượng TripItem thành giao diện dạng card để hiển thị trong RecyclerView của MyTripsFragment.
 */
public class BookedTripAdapter extends RecyclerView.Adapter<BookedTripAdapter.TripViewHolder> {

    private final List<TripItem> tripList;
    private final Context context;
    private final OnTripClickListener clickListener;

    public interface OnTripClickListener {
        void onTripClick(TripItem item);
    }

    /**
     * Lớp dữ liệu (Data Class) cục bộ đại diện cho một vé/chuyến đi hiển thị trên UI.
     * Khi thanh toán thành công, một TripItem mới được khởi tạo và thêm vào MyTripsFragment.additionalTrips.
     */
    public static class TripItem {
        public String id;
        public String trainName;
        public String statusBadge;
        public String depTime;
        public String arrTime;
        public String depStation;
        public String arrStation;
        public String duration;
        public String price;
        public String date; // định dạng "dd/MM" ví dụ "09/09"
        public boolean isHistory; // true nếu là chuyến đi trong lịch sử
        public String tourType; // loại tour (ví dụ "sapa", "taiwan")
        public int tourId = -1;
        public int userId = -1;


        public TripItem(String id, String trainName, String statusBadge, String depTime, String arrTime,
                        String depStation, String arrStation, String duration, String price, String date, boolean isHistory, String tourType) {
            this.id = id;
            this.trainName = trainName;
            this.statusBadge = statusBadge;
            this.depTime = depTime;
            this.arrTime = arrTime;
            this.depStation = depStation;
            this.arrStation = arrStation;
            this.duration = duration;
            this.price = price;
            this.date = date;
            this.isHistory = isHistory;
            this.tourType = tourType;
        }
    }

    public BookedTripAdapter(Context context, List<TripItem> tripList, OnTripClickListener clickListener) {
        this.context = context;
        this.tripList = tripList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booked_trip_card, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        TripItem item = tripList.get(position);

        holder.tvTrainName.setText(item.trainName);
        holder.tvDepTime.setText(item.depTime);
        holder.tvArrTime.setText(item.arrTime);
        holder.tvDepStation.setText(item.depStation);
        holder.tvArrStation.setText(item.arrStation);
        holder.tvDuration.setText(item.duration);
        holder.tvPriceValue.setText(item.price);

        // Thiết lập trạng thái và phong cách dựa trên loại chuyến đi (Sắp tới / Lịch sử)
        if (item.isHistory) {
            // Lịch sử chuyến đi
            if ("Đã hủy".equals(item.statusBadge)) {
                holder.tvStatusBadge.setText("Đã hủy");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#C62828")); // đỏ đậm
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_red); // nền đỏ nhạt
            } else {
                holder.tvStatusBadge.setText("Đã hoàn thành");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#FFFFFF")); // trắng
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_score_green); // nền xanh đặc
            }

            // Thay đổi nút Chọn thành Chi tiết
            holder.btnAction.setText("Chi tiết");
            holder.btnAction.setBackgroundResource(R.drawable.bg_chip_selected_cyan);
            holder.btnAction.setTextColor(Color.parseColor("#00B4D8"));

            // Ẩn bớt các tag ưu đãi cho lịch sử sạch hơn
            holder.layoutPromoTags.setVisibility(View.GONE);

            // Đặt độ mờ nhẹ cho thẻ lịch sử
            holder.itemView.setAlpha(0.85f);
        } else {
            // Chuyến đi sắp tới - màu badge theo trạng thái
            String badge = item.statusBadge != null ? item.statusBadge : "Chờ duyệt";
            if ("Đã thanh toán".equals(badge) || "CONFIRMED".equalsIgnoreCase(badge)) {
                holder.tvStatusBadge.setText("Đã thanh toán");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#185FA5")); // xanh dương
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_blue);
            } else if ("Đã hủy".equals(badge) || "CANCELLED".equalsIgnoreCase(badge)) {
                holder.tvStatusBadge.setText("Đã hủy");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#C62828")); // đỏ đậm
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_red);
            } else {
                // Chờ duyệt / PENDING
                holder.tvStatusBadge.setText("Chờ duyệt");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#E65100")); // cam đậm
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_orange_badge);
            }

            holder.btnAction.setText("Xem vé");
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_orange);
            holder.btnAction.setTextColor(Color.WHITE);

            holder.layoutPromoTags.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(1.0f);
        }

        // Sự kiện click nút bấm hành động
        holder.btnAction.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTripClick(item);
            }
        });

        // Sự kiện click toàn bộ thẻ
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTripClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tvTrainName, tvStatusBadge;
        TextView tvDepTime, tvDepStation;
        TextView tvArrTime, tvArrStation;
        TextView tvDuration, tvPriceValue;
        TextView btnAction;
        LinearLayout layoutPromoTags;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTrainName = itemView.findViewById(R.id.tvTrainName);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvDepTime = itemView.findViewById(R.id.tvDepTime);
            tvDepStation = itemView.findViewById(R.id.tvDepStation);
            tvArrTime = itemView.findViewById(R.id.tvArrTime);
            tvArrStation = itemView.findViewById(R.id.tvArrStation);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvPriceValue = itemView.findViewById(R.id.tvPriceValue);
            btnAction = itemView.findViewById(R.id.btnAction);
            layoutPromoTags = itemView.findViewById(R.id.layoutPromoTags);
        }
    }
}
