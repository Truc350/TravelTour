package com.example.myapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.data.model.VoucherHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách voucher của tôi.
 * Hỗ trợ lọc voucher theo trạng thái (Chưa sử dụng / Lịch sử), sao chép mã và nhập mã mới.
 */
public class MyVouchers extends Fragment {

    private TextView btnTabActive, btnTabHistory;
    private LinearLayout voucherContainer;
    private View layoutEmptyVouchers;
    private List<VoucherItem> voucherList = new ArrayList<>();
    private String activeTab = "ACTIVE"; // "ACTIVE" hoặc "USED"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_vouchers, container, false);

        // Ánh xạ views
        btnTabActive = view.findViewById(R.id.btnTabActive);
        btnTabHistory = view.findViewById(R.id.btnTabHistory);
        voucherContainer = view.findViewById(R.id.voucherContainer);
        layoutEmptyVouchers = view.findViewById(R.id.layoutEmptyVouchers);

        // Quay lại
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Khởi tạo danh sách dữ liệu từ API
        loadVouchersFromApi();

        // Thiết lập sự kiện chọn tab
        btnTabActive.setOnClickListener(v -> switchTab("ACTIVE"));
        btnTabHistory.setOnClickListener(v -> switchTab("USED"));

        return view;
    }

    private void loadVouchersFromApi() {
        if (getContext() == null) return;
        SharedPreferences sessionPrefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int currentUserId = sessionPrefs.getInt("current_user_id", -1);

        voucherList.clear();
        voucherList.add(new VoucherItem("SUMMER50", "50%", "VOUCHER", "Vé Xe Giá Rẻ Hè", "Mã giảm giá 50% vé xe limousine đi các tỉnh miền Bắc.", "Hết hiệu lực", "USED", "#718096")); // Xám

        if (currentUserId == -1) {
            renderVouchers();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getUserVouchers(currentUserId).enqueue(new Callback<List<VoucherHelper.UserVoucherResponse>>() {
            @Override
            public void onResponse(Call<List<VoucherHelper.UserVoucherResponse>> call, Response<List<VoucherHelper.UserVoucherResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (VoucherHelper.UserVoucherResponse uvr : response.body()) {
                        VoucherHelper.AppVoucher av = uvr.voucherDetail;
                        if (av != null) {
                            boolean alreadyExists = false;
                            for (VoucherItem existing : voucherList) {
                                if (existing.code.equalsIgnoreCase(av.code)) {
                                    alreadyExists = true;
                                    break;
                                }
                            }
                            if (!alreadyExists) {
                                voucherList.add(new VoucherItem(
                                        av.code,
                                        av.discountVal,
                                        av.discountLabel,
                                        av.title,
                                        av.desc,
                                        av.expiry,
                                        uvr.isUsed ? "USED" : "ACTIVE",
                                        av.colorHex
                                ));
                            }
                        }
                    }
                }
                renderVouchers();
            }

            @Override
            public void onFailure(Call<List<VoucherHelper.UserVoucherResponse>> call, Throwable t) {
                renderVouchers();
            }
        });
    }

    private void switchTab(String tabStatus) {
        activeTab = tabStatus;
        if ("ACTIVE".equals(tabStatus)) {
            btnTabActive.setBackgroundResource(R.drawable.bg_pref_selected);
            btnTabActive.setTextColor(Color.WHITE);
            btnTabHistory.setBackgroundResource(R.drawable.bg_pref_unselected);
            btnTabHistory.setTextColor(Color.parseColor("#4A5568"));
        } else {
            btnTabHistory.setBackgroundResource(R.drawable.bg_pref_selected);
            btnTabHistory.setTextColor(Color.WHITE);
            btnTabActive.setBackgroundResource(R.drawable.bg_pref_unselected);
            btnTabActive.setTextColor(Color.parseColor("#4A5568"));
        }
        renderVouchers();
    }

    private void renderVouchers() {
        voucherContainer.removeAllViews();
        List<VoucherItem> filteredList = new ArrayList<>();
        for (VoucherItem item : voucherList) {
            if (item.status.equals(activeTab)) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()) {
            layoutEmptyVouchers.setVisibility(View.VISIBLE);
        } else {
            layoutEmptyVouchers.setVisibility(View.GONE);
            LayoutInflater inflater = LayoutInflater.from(requireContext());

            for (VoucherItem item : filteredList) {
                View card = inflater.inflate(R.layout.item_voucher, voucherContainer, false);

                // Ánh xạ các trường
                View layoutLeftBlock = card.findViewById(R.id.layoutLeftBlock);
                TextView tvDiscountVal = card.findViewById(R.id.tvDiscountVal);
                TextView tvDiscountLabel = card.findViewById(R.id.tvDiscountLabel);
                TextView tvVoucherTitle = card.findViewById(R.id.tvVoucherTitle);
                TextView tvVoucherDesc = card.findViewById(R.id.tvVoucherDesc);
                TextView tvVoucherExpiry = card.findViewById(R.id.tvVoucherExpiry);
                TextView tvVoucherCode = card.findViewById(R.id.tvVoucherCode);
                TextView btnAction = card.findViewById(R.id.btnVoucherAction);

                // Thiết lập dữ liệu
                tvDiscountVal.setText(item.discountVal);
                tvDiscountLabel.setText(item.discountLabel);
                tvVoucherTitle.setText(item.title);
                tvVoucherDesc.setText(item.desc);
                tvVoucherExpiry.setText(item.expiry);
                tvVoucherCode.setText(item.code);

                // Thiết lập màu sắc và trạng thái dựa trên status
                if ("ACTIVE".equals(item.status)) {
                    layoutLeftBlock.setBackgroundColor(Color.parseColor(item.colorHex));
                    btnAction.setEnabled(true);
                    btnAction.setText("Sao chép");
                    btnAction.setBackgroundResource(R.drawable.bg_pref_selected);
                    btnAction.setTextColor(Color.WHITE);
                    btnAction.setOnClickListener(v -> copyToClipboard(item.code));
                } else {
                    layoutLeftBlock.setBackgroundColor(Color.parseColor("#A0AEC0")); // Màu xám cho voucher cũ
                    btnAction.setEnabled(false);
                    btnAction.setText("Đã dùng");
                    btnAction.setBackgroundResource(R.drawable.bg_pref_unselected);
                    btnAction.setTextColor(Color.parseColor("#A0AEC0"));
                }

                voucherContainer.addView(card);
            }
        }
    }

    private void copyToClipboard(String code) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Voucher Code", code);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "Đã sao chép mã: " + code + " vào bộ nhớ tạm", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.bottomNavigation);
            if (nav != null) {
                nav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVouchersFromApi();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.bottomNavigation);
            if (nav != null) {
                nav.setVisibility(View.VISIBLE);
            }
        }
    }

    // Lớp đại diện cho từng Voucher
    private static class VoucherItem {
        String code;
        String discountVal;
        String discountLabel;
        String title;
        String desc;
        String expiry;
        String status; // "ACTIVE" hoặc "USED"
        String colorHex;

        VoucherItem(String code, String discountVal, String discountLabel, String title, String desc, String expiry, String status, String colorHex) {
            this.code = code;
            this.discountVal = discountVal;
            this.discountLabel = discountLabel;
            this.title = title;
            this.desc = desc;
            this.expiry = expiry;
            this.status = status;
            this.colorHex = colorHex;
        }
    }
}
