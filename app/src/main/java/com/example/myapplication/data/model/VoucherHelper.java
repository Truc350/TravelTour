package com.example.myapplication.data.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class VoucherHelper {

    public static class AppVoucher {
        @com.google.gson.annotations.SerializedName("code")
        public String code;
        @com.google.gson.annotations.SerializedName("title")
        public String title;
        @com.google.gson.annotations.SerializedName("discount_val")
        public String discountVal;
        @com.google.gson.annotations.SerializedName("discount_label")
        public String discountLabel;
        @com.google.gson.annotations.SerializedName("description")
        public String desc;
        @com.google.gson.annotations.SerializedName("expiry")
        public String expiry;
        @com.google.gson.annotations.SerializedName("status")
        public String status; // e.g., "Còn hiệu lực", "Sắp hết hạn"
        @com.google.gson.annotations.SerializedName("remaining_count")
        public int remainingCount;
        @com.google.gson.annotations.SerializedName("color_hex")
        public String colorHex;
        @com.google.gson.annotations.SerializedName("max_discount")
        public long maxDiscount; // Giới hạn giảm tối đa (VND), 0 = không giới hạn
        @com.google.gson.annotations.SerializedName("id")
        public int id;
        @com.google.gson.annotations.SerializedName("is_saved")
        public boolean isSaved;
        @com.google.gson.annotations.SerializedName("is_used")
        public boolean isUsed;


        public AppVoucher(String code, String title, String discountVal, String discountLabel, String desc, String expiry, String status, int remainingCount, String colorHex) {
            this.code = code;
            this.title = title;
            this.discountVal = discountVal;
            this.discountLabel = discountLabel;
            this.desc = desc;
            this.expiry = expiry;
            this.status = status;
            this.remainingCount = remainingCount;
            this.colorHex = colorHex;
            this.maxDiscount = 0;
        }

        public AppVoucher(String code, String title, String discountVal, String discountLabel, String desc, String expiry, String status, int remainingCount, String colorHex, long maxDiscount) {
            this(code, title, discountVal, discountLabel, desc, expiry, status, remainingCount, colorHex);
            this.maxDiscount = maxDiscount;
        }
    }

    private static final List<AppVoucher> AVAILABLE_VOUCHERS = new ArrayList<>();

    static {
        AVAILABLE_VOUCHERS.add(new AppVoucher("VIVUMUAHAN", "Khuyến Mãi Mùa Hè 2026", "15%", "VOUCHER", "Giảm 15% tối đa 150k cho các tour du lịch miền Nam.", "HSD: 31/08/2026", "Còn hiệu lực", 120, "#319795", 150000L));
        AVAILABLE_VOUCHERS.add(new AppVoucher("VIVU50K", "Vi Vu Mọi Nơi", "50k", "GIẢM GIÁ", "Giảm trực tiếp 50k cho đơn hàng từ 1.000k trở lên.", "HSD: 31/12/2026", "Còn hiệu lực", 45, "#E53E3E", 0L));
        AVAILABLE_VOUCHERS.add(new AppVoucher("CHILLTOUR20", "Chill Cùng Bạn Bè", "20%", "VOUCHER", "Giảm 20% tối đa 300k cho nhóm từ 4 người trở lên.", "HSD: 31/10/2026", "Còn hiệu lực", 80, "#DD6B20", 300000L));
        AVAILABLE_VOUCHERS.add(new AppVoucher("FIRSTTRIP", "Hành Trình Đầu Tiên", "100k", "GIẢM GIÁ", "Áp dụng cho booking đầu tiên trên ứng dụng Chill Tour.", "HSD: 30/06/2026", "Sắp hết hạn", 12, "#6B46C1", 0L));
    }

    public static List<AppVoucher> getAvailableVouchers() {
        return AVAILABLE_VOUCHERS;
    }

    public static boolean isVoucherSaved(Context context, String userContact, String code) {
        if (userContact == null || userContact.isEmpty()) return false;
        SharedPreferences prefs = context.getSharedPreferences("SavedVouchers_" + userContact, Context.MODE_PRIVATE);
        return prefs.getBoolean(code, false);
    }

    public static void saveVoucher(Context context, String userContact, String code) {
        if (userContact == null || userContact.isEmpty()) return;
        SharedPreferences prefs = context.getSharedPreferences("SavedVouchers_" + userContact, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(code, true).apply();
    }

    public static List<AppVoucher> getSavedVouchers(Context context, String userContact) {
        if (userContact == null || userContact.isEmpty()) return new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences("SavedVouchers_" + userContact, Context.MODE_PRIVATE);
        List<AppVoucher> saved = new ArrayList<>();
        for (AppVoucher v : AVAILABLE_VOUCHERS) {
            if (prefs.getBoolean(v.code, false)) {
                saved.add(v);
            }
        }
        return saved;
    }

    public static List<AppVoucher> getUsedVouchers(Context context, String userContact) {
        if (userContact == null || userContact.isEmpty()) return new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences("UsedVouchers_" + userContact, Context.MODE_PRIVATE);
        List<AppVoucher> used = new ArrayList<>();
        for (AppVoucher v : AVAILABLE_VOUCHERS) {
            if (prefs.getBoolean(v.code, false)) {
                used.add(v);
            }
        }
        return used;
    }

    public static class UserVoucherResponse {
        @com.google.gson.annotations.SerializedName("id")
        public int id;
        @com.google.gson.annotations.SerializedName("user")
        public int user;
        @com.google.gson.annotations.SerializedName("voucher")
        public int voucher;
        @com.google.gson.annotations.SerializedName("voucher_detail")
        public AppVoucher voucherDetail;
        @com.google.gson.annotations.SerializedName("is_used")
        public boolean isUsed;
        @com.google.gson.annotations.SerializedName("saved_at")
        public String savedAt;
    }
}
