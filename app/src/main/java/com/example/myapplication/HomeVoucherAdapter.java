package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import com.example.myapplication.data.model.VoucherHelper;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;


public class HomeVoucherAdapter extends RecyclerView.Adapter<HomeVoucherAdapter.ViewHolder> {

    private final List<VoucherHelper.AppVoucher> voucherList;
    private final String userContact;
    private final OnVoucherSavedListener saveListener;

    public interface OnVoucherSavedListener {
        void onVoucherSaved(String code);
    }

    public HomeVoucherAdapter(List<VoucherHelper.AppVoucher> voucherList, String userContact, OnVoucherSavedListener saveListener) {
        this.voucherList = voucherList;
        this.userContact = userContact;
        this.saveListener = saveListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VoucherHelper.AppVoucher item = voucherList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvDiscountVal.setText(item.discountVal);
        holder.tvDiscountLabel.setText(item.discountLabel);
        holder.tvVoucherTitle.setText(item.title);
        holder.tvVoucherStatus.setText(item.status);
        holder.tvVoucherExpiry.setText(item.expiry);
        holder.tvVoucherRemaining.setText("Còn " + item.remainingCount + " lượt");

        try {
            holder.layoutLeftBlock.setBackgroundColor(Color.parseColor(item.colorHex));
        } catch (Exception e) {
            holder.layoutLeftBlock.setBackgroundColor(Color.parseColor("#319795"));
        }

        // Check if voucher is already saved by the user
        boolean isSaved = item.isSaved;
        if (isSaved) {
            holder.btnSaveVoucher.setText("Đã lưu");
            holder.btnSaveVoucher.setEnabled(false);
            holder.btnSaveVoucher.setBackgroundResource(R.drawable.bg_pref_unselected);
            holder.btnSaveVoucher.setTextColor(Color.parseColor("#A0AEC0"));
        } else {
            holder.btnSaveVoucher.setText("Lưu");
            holder.btnSaveVoucher.setEnabled(true);
            holder.btnSaveVoucher.setBackgroundResource(R.drawable.bg_pref_selected);
            holder.btnSaveVoucher.setTextColor(Color.WHITE);
            holder.btnSaveVoucher.setOnClickListener(v -> {
                SharedPreferences prefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                int currentUserId = prefs.getInt("current_user_id", -1);
                if (currentUserId == -1) {
                    Toast.makeText(context, "Vui lòng đăng nhập để lưu voucher!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                    return;
                }

                holder.btnSaveVoucher.setEnabled(false);
                ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                java.util.Map<String, Object> body = new java.util.HashMap<>();
                body.put("user", currentUserId);
                body.put("voucher", item.id);
                apiService.saveUserVoucher(body).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            item.isSaved = true;
                            Toast.makeText(context, "Đã lưu voucher: " + item.code + " vào ví!", Toast.LENGTH_SHORT).show();
                            if (saveListener != null) {
                                saveListener.onVoucherSaved(item.code);
                            }
                        } else {
                            holder.btnSaveVoucher.setEnabled(true);
                            Toast.makeText(context, "Lưu voucher thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        holder.btnSaveVoucher.setEnabled(true);
                        Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View layoutLeftBlock;
        TextView tvDiscountVal, tvDiscountLabel;
        TextView tvVoucherTitle, tvVoucherStatus, tvVoucherRemaining, tvVoucherExpiry;
        AppCompatButton btnSaveVoucher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutLeftBlock = itemView.findViewById(R.id.layoutLeftBlock);
            tvDiscountVal = itemView.findViewById(R.id.tvDiscountVal);
            tvDiscountLabel = itemView.findViewById(R.id.tvDiscountLabel);
            tvVoucherTitle = itemView.findViewById(R.id.tvVoucherTitle);
            tvVoucherStatus = itemView.findViewById(R.id.tvVoucherStatus);
            tvVoucherRemaining = itemView.findViewById(R.id.tvVoucherRemaining);
            tvVoucherExpiry = itemView.findViewById(R.id.tvVoucherExpiry);
            btnSaveVoucher = itemView.findViewById(R.id.btnSaveVoucher);
        }
    }
}
