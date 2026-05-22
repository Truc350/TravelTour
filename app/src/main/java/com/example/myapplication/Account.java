package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment đại diện cho trang Account của Quyên.
 * Quản lý các sự kiện click chọn chức năng trong trang Account.
 */
public class Account extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp giao diện XML account vào Fragment
        View view = inflater.inflate(R.layout.account, container, false);

        // Thiết lập sự kiện click cho các nút chức năng (bạn có thể mở rộng xử lý sau này)
        view.findViewById(R.id.btnMyProfile).setOnClickListener(v -> {
            // Xử lý khi nhấn "Hồ sơ của tôi"
        });

        view.findViewById(R.id.btnPassengerInfo).setOnClickListener(v -> {
            // Xử lý khi nhấn "Thông tin hành khách" -> Chuyển sang màn hình PassengerList
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new PassengerList())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.btnTravelPreferences).setOnClickListener(v -> {
            // Xử lý khi nhấn "Sở thích du lịch"
        });

        view.findViewById(R.id.btnMyVouchers).setOnClickListener(v -> {
            // Xử lý khi nhấn "Voucher của tôi"
        });

        view.findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            // Xử lý khi nhấn "Đổi mật khẩu"
        });

        view.findViewById(R.id.btnZaloSupport).setOnClickListener(v -> {
            // Xử lý khi nhấn "Hỗ trợ qua Zalo TravelTour"
        });

        view.findViewById(R.id.btnTerms).setOnClickListener(v -> {
            // Xử lý khi nhấn "Điều khoản và điều kiện" -> Chuyển sang màn hình TermsAndConditions
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new TermsAndConditions())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.btnPrivacyPolicy).setOnClickListener(v -> {
            // Xử lý khi nhấn "Chính sách quyền riêng tư"
        });

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            // Hiện hộp thoại xác nhận trước khi đăng xuất
            new AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        // Chuyển sang LoginActivity và xóa toàn bộ back stack
                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        return view;
    }
}
