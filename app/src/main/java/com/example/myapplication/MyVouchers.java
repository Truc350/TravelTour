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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách voucher của tôi.
 * Hỗ trợ lọc voucher theo trạng thái (Chưa sử dụng / Lịch sử), sao chép mã và nhập mã mới.
 */
public class MyVouchers extends Fragment {

    private EditText etPromoCode;
    private View btnApplyPromo;
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
        etPromoCode = view.findViewById(R.id.etPromoCode);
        btnApplyPromo = view.findViewById(R.id.btnApplyPromo);
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

        // Khởi tạo danh sách dữ liệu mock
        initMockVouchers();

        // Thiết lập sự kiện chọn tab
        btnTabActive.setOnClickListener(v -> switchTab("ACTIVE"));
        btnTabHistory.setOnClickListener(v -> switchTab("USED"));

        // Áp dụng mã
        btnApplyPromo.setOnClickListener(v -> applyPromoCode());

        // Hiển thị danh sách ban đầu
        renderVouchers();

        return view;
    }

    private void initMockVouchers() {
        voucherList.clear();
        voucherList.add(new VoucherItem("WELCOME50", "50k", "GIẢM GIÁ", "Voucher Chào Mừng", "Áp dụng cho khách hàng mới đăng ký tài khoản. Đơn tối thiểu 200k.", "HSD: 31/12/2026", "ACTIVE", "#6B46C1")); // Tím
        voucherList.add(new VoucherItem("CHILLTOUR10", "10%", "GIẢM GIÁ", "Khám Phá Hè Rực Rỡ", "Giảm 10% tối đa 200k cho các tour Đà Nẵng, Phú Quốc, Nha Trang.", "HSD: 30/09/2026", "ACTIVE", "#DD6B20")); // Cam
        voucherList.add(new VoucherItem("HOTEL15", "15%", "VOUCHER", "Khách Sạn Sang Xịn", "Giảm 15% khi đặt phòng khách sạn Vinpearl hoặc Intercontinental qua app.", "HSD: 30/11/2026", "ACTIVE", "#319795")); // Teal/Cyan
        voucherList.add(new VoucherItem("SUMMER50", "50%", "VOUCHER", "Vé Xe Giá Rẻ Hè", "Mã giảm giá 50% vé xe limousine đi các tỉnh miền Bắc.", "Hết hiệu lực", "USED", "#718096")); // Xám
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

    private void applyPromoCode() {
        String code = etPromoCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập mã voucher!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem mã đã tồn tại và chưa dùng chưa
        VoucherItem matchedItem = null;
        for (VoucherItem item : voucherList) {
            if (item.code.equalsIgnoreCase(code)) {
                matchedItem = item;
                break;
            }
        }

        if (matchedItem != null) {
            if ("ACTIVE".equals(matchedItem.status)) {
                Toast.makeText(requireContext(), "Áp dụng thành công mã voucher: " + code, Toast.LENGTH_LONG).show();
                etPromoCode.setText("");
                // Switch to active tab to see it
                switchTab("ACTIVE");
            } else {
                Toast.makeText(requireContext(), "Mã voucher này đã được sử dụng hoặc hết hạn!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Trường hợp người dùng nhập mã voucher bí mật hợp lệ khác
            if ("TRAVEL2026".equals(code) || "CHILLTOUR".equals(code)) {
                VoucherItem newItem = new VoucherItem(code, "100k", "QUÀ TẶNG", "Ưu Đãi Đặc Biệt 2026", "Tặng ngay 100.000đ khi đặt bất kỳ tour du lịch nào.", "HSD: 31/12/2026", "ACTIVE", "#E53E3E");
                voucherList.add(0, newItem);
                Toast.makeText(requireContext(), "Chúc mừng! Bạn đã nhận voucher đặc biệt: " + code, Toast.LENGTH_LONG).show();
                etPromoCode.setText("");
                switchTab("ACTIVE");
            } else {
                Toast.makeText(requireContext(), "Mã voucher không hợp lệ. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
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
