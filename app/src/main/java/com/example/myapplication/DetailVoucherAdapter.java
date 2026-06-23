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

import com.example.myapplication.data.model.VoucherHelper;

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
        boolean isSaved = VoucherHelper.isVoucherSaved(context, userContact, item.code);
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
                VoucherHelper.saveVoucher(context, userContact, item.code);
                Toast.makeText(context, "Đã lưu voucher: " + item.code + " vào ví!", Toast.LENGTH_SHORT).show();
                if (saveListener != null) {
                    saveListener.onVoucherSaved(item.code);
                }
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
