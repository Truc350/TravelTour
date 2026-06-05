package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment đại diện cho trang Account của Quyên.
 * Quản lý các sự kiện click chọn chức năng trong trang Account.
 */
public class Account extends Fragment {

    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp giao diện XML account vào Fragment
        View view = inflater.inflate(R.layout.account, container, false);
        dbHelper = new DatabaseHelper(requireContext());

        // Hiển thị tên người dùng thực tế
        TextView tvUserName = view.findViewById(R.id.tv_user_name);
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String contact = prefs.getString("current_user_contact", "");
        if (!contact.isEmpty()) {
            Map<String, String> userDetails = dbHelper.getUserDetails(contact);
            if (userDetails != null && userDetails.get("name") != null) {
                tvUserName.setText(userDetails.get("name"));
            }
        }

        // Thiết lập sự kiện click cho các nút chức năng (bạn có thể mở rộng xử lý sau này)
        view.findViewById(R.id.btnMyProfile).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ProfileActivity.class));
        });

        view.findViewById(R.id.btnPassengerInfo).setOnClickListener(v -> {
            // Xử lý khi nhấn "Thông tin hành khách" -> Chuyển sang màn hình PassengerList
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new PassengerList())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.btnTravelPreferences).setOnClickListener(v -> {
            // Xử lý khi nhấn "Sở thích du lịch" -> Chuyển sang màn hình TravelPreferences
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new TravelPreferences())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.btnMyVouchers).setOnClickListener(v -> {
            // Xử lý khi nhấn "Voucher của tôi" -> Chuyển sang màn hình MyVouchers
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new MyVouchers())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            // Xử lý khi nhấn "Đổi mật khẩu" -> Chuyển sang màn hình ChangePassword
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new ChangePassword())
                    .addToBackStack(null)
                    .commit();
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
            // Xử lý khi nhấn "Chính sách quyền riêng tư" -> Chuyển sang màn hình PrivacyPolicy
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new PrivacyPolicy())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            // Hiện hộp thoại xác nhận trước khi đăng xuất
            new AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        // Xóa phiên đăng nhập trong SharedPreferences
                        requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                                .edit()
                                .clear()
                                .apply();
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
