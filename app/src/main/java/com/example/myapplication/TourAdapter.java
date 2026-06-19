package com.example.myapplication;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.model.Tour;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {

    private List<Tour> tourList;
    private OnTourClickListener listener;

    public interface OnTourClickListener {
        void onTourClick(Tour tour);
    }

    public TourAdapter(List<Tour> tourList, OnTourClickListener listener) {
        this.tourList = tourList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tour_card, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour tour = tourList.get(position);

        // Tiêu đề
        holder.tvTourTitle.setText(tour.getTitle());

        // Format số tiền
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        // Giá gốc (gạch ngang)
        holder.tvOldPrice.setText(formatter.format(tour.getOriginalPrice()));
        holder.tvOldPrice.setPaintFlags(
                holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
        );

        // Giá khuyến mãi
        holder.tvNewPrice.setText(formatter.format(tour.getDiscountPrice()));

        // Provider làm badge
        holder.tvRibbonBadge.setText(tour.getProvider());

        // Nút Xem tour
        holder.btnViewTour.setOnClickListener(v -> {
            if (listener != null) listener.onTourClick(tour);
        });

        // Load hình ảnh bằng Glide
        if (holder.ivTourImage != null) {
            String imageUrl = null;
            if (tour.getImages() != null && !tour.getImages().isEmpty()) {
                imageUrl = tour.getImages().get(0).getImageUrl();
            }
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (imageUrl.startsWith("/")) {
                    imageUrl = "http://10.0.2.2:8000" + imageUrl;
                }
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.img_taiwan_tour) // hiển thị tạm trong lúc tải
                        .centerCrop()
                        .into(holder.ivTourImage);
            } else {
                // Đặt ảnh mặc định nếu không có ảnh
                holder.ivTourImage.setImageResource(R.drawable.img_taiwan_tour);
            }
        }

        // Click cả card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTourClick(tour);
        });
    }

    @Override
    public int getItemCount() {
        return tourList != null ? tourList.size() : 0;
    }

    public void updateData(List<Tour> newList) {
        this.tourList = newList;
        notifyDataSetChanged();
    }

    static class TourViewHolder extends RecyclerView.ViewHolder {
        TextView tvTourTitle, tvOldPrice, tvNewPrice, tvRibbonBadge, btnViewTour;
        android.widget.ImageView ivTourImage;

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTourTitle    = itemView.findViewById(R.id.tvTourTitle);
            tvOldPrice     = itemView.findViewById(R.id.tvOldPrice);
            tvNewPrice     = itemView.findViewById(R.id.tvNewPrice);
            tvRibbonBadge  = itemView.findViewById(R.id.tvRibbonBadge);
            btnViewTour    = itemView.findViewById(R.id.btnViewTour);
            ivTourImage    = itemView.findViewById(R.id.ivTourImage);
        }
    }
}