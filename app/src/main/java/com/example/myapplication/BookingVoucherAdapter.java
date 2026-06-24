package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.model.VoucherHelper;

import java.util.List;

public class BookingVoucherAdapter extends RecyclerView.Adapter<BookingVoucherAdapter.ViewHolder> {

    private final List<VoucherHelper.AppVoucher> vouchers;
    private final List<Long> computedDiscounts;
    private final OnVoucherSelectedListener listener;
    private int selectedPosition = -1; // vị trí voucher đang được chọn
    private long maxDiscount = 0; // mức giảm lớn nhất của các voucher hợp lệ

    public interface OnVoucherSelectedListener {
        void onVoucherSelected(VoucherHelper.AppVoucher voucher, long computedDiscount);
    }

    public BookingVoucherAdapter(List<VoucherHelper.AppVoucher> vouchers, List<Long> computedDiscounts, OnVoucherSelectedListener listener) {
        this.vouchers = vouchers;
        this.computedDiscounts = computedDiscounts;
        this.listener = listener;
        
        // Tìm mức giảm giá lớn nhất để đề xuất động
        if (computedDiscounts != null) {
            for (long d : computedDiscounts) {
                if (d > maxDiscount) {
                    maxDiscount = d;
                }
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VoucherHelper.AppVoucher voucher = vouchers.get(position);
        long discount = computedDiscounts.get(position);

        holder.tvDiscountVal.setText(voucher.discountVal);
        holder.tvDiscountLabel.setText(voucher.discountLabel);
        holder.tvTitle.setText(voucher.title);
        holder.tvDesc.setText(voucher.desc);
        holder.tvExpiry.setText(voucher.expiry);

        // Màu nền cột trái
        try {
            holder.layoutLeft.setBackgroundColor(Color.parseColor(voucher.colorHex));
        } catch (Exception e) {
            holder.layoutLeft.setBackgroundColor(Color.parseColor("#FF3D00"));
        }

        // Badge "Đề xuất" chỉ hiện ở voucher có mức giảm tốt nhất (không fix cứng vị trí)
        if (discount == maxDiscount && discount > 0) {
            holder.tvLeftRecommended.setVisibility(View.VISIBLE);
        } else {
            holder.tvLeftRecommended.setVisibility(View.GONE);
        }

        // Hiển thị mức giảm giá tính toán thực tế
        holder.tvComputedDiscount.setText("Giảm " + formatVnd(discount));

        // Trạng thái chọn
        boolean isSelected = (position == selectedPosition);
        holder.ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        // Viền xanh khi chọn
        holder.cardVoucher.setCardElevation(isSelected ? 8f : 3f);
        if (isSelected) {
            holder.cardVoucher.setCardBackgroundColor(Color.parseColor("#F0FFF4")); // nền xanh nhạt
        } else {
            holder.cardVoucher.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        // Click vào Card để chọn voucher
        holder.cardVoucher.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onVoucherSelected(voucher, discount);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    /** Bỏ chọn tất cả (dùng khi nhấn "Không dùng voucher") */
    public void clearSelection() {
        int prev = selectedPosition;
        selectedPosition = -1;
        if (prev >= 0) notifyItemChanged(prev);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutLeft;
        CardView cardVoucher;
        ImageView ivSelected;
        TextView tvDiscountVal, tvDiscountLabel, tvTitle, tvDesc, tvExpiry, tvLeftRecommended, tvComputedDiscount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // CardView là root element của item layout
            cardVoucher = (CardView) itemView;
            layoutLeft = itemView.findViewById(R.id.layoutLeft);
            ivSelected = itemView.findViewById(R.id.ivSelected);
            tvDiscountVal = itemView.findViewById(R.id.tvDiscountVal);
            tvDiscountLabel = itemView.findViewById(R.id.tvDiscountLabel);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            tvLeftRecommended = itemView.findViewById(R.id.tvLeftRecommended);
            tvComputedDiscount = itemView.findViewById(R.id.tvComputedDiscount);
        }
    }
}
