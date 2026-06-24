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

public class DetailVoucherAdapter extends RecyclerView.Adapter<DetailVoucherAdapter.ViewHolder> {

    private final List<VoucherHelper.AppVoucher> voucherList;
    private final String userContact;
    private final HomeVoucherAdapter.OnVoucherSavedListener saveListener;

    public DetailVoucherAdapter(List<VoucherHelper.AppVoucher> voucherList, String userContact, HomeVoucherAdapter.OnVoucherSavedListener saveListener) {
        this.voucherList = voucherList;
        this.userContact = userContact;
        this.saveListener = saveListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VoucherHelper.AppVoucher item = voucherList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvDiscountVal.setText(item.discountVal);
        holder.tvDiscountLabel.setText(item.discountLabel);
        holder.tvVoucherTitle.setText(item.title);
        holder.tvVoucherDesc.setText(item.desc);
        holder.tvVoucherExpiry.setText(item.expiry);
        holder.tvVoucherCode.setText(item.code);

        try {
            holder.layoutLeftBlock.setBackgroundColor(Color.parseColor(item.colorHex));
        } catch (Exception e) {
            holder.layoutLeftBlock.setBackgroundColor(Color.parseColor("#319795"));
        }

        // Check if voucher is already saved by the user
        boolean isSaved = item.isSaved;
        if (isSaved) {
            holder.btnVoucherAction.setText("Đã lưu");
            holder.btnVoucherAction.setEnabled(false);
            holder.btnVoucherAction.setBackgroundResource(R.drawable.bg_pref_unselected);
            holder.btnVoucherAction.setTextColor(Color.parseColor("#A0AEC0"));
        } else {
            holder.btnVoucherAction.setText("Lưu");
            holder.btnVoucherAction.setEnabled(true);
            holder.btnVoucherAction.setBackgroundResource(R.drawable.bg_pref_selected);
            holder.btnVoucherAction.setTextColor(Color.WHITE);
            holder.btnVoucherAction.setOnClickListener(v -> {
                SharedPreferences prefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                int currentUserId = prefs.getInt("current_user_id", -1);
                if (currentUserId == -1) {
                    Toast.makeText(context, "Vui lòng đăng nhập để lưu voucher!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                    return;
                }

                holder.btnVoucherAction.setEnabled(false);
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
                            holder.btnVoucherAction.setEnabled(true);
                            Toast.makeText(context, "Lưu voucher thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        holder.btnVoucherAction.setEnabled(true);
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
        TextView tvVoucherTitle, tvVoucherDesc, tvVoucherExpiry, tvVoucherCode;
        AppCompatButton btnVoucherAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutLeftBlock = itemView.findViewById(R.id.layoutLeftBlock);
            tvDiscountVal = itemView.findViewById(R.id.tvDiscountVal);
            tvDiscountLabel = itemView.findViewById(R.id.tvDiscountLabel);
            tvVoucherTitle = itemView.findViewById(R.id.tvVoucherTitle);
            tvVoucherDesc = itemView.findViewById(R.id.tvVoucherDesc);
            tvVoucherExpiry = itemView.findViewById(R.id.tvVoucherExpiry);
            tvVoucherCode = itemView.findViewById(R.id.tvVoucherCode);
            btnVoucherAction = itemView.findViewById(R.id.btnVoucherAction);
        }
    }
}
