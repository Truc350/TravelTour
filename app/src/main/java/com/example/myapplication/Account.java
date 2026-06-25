package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Map;
import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment đại diện cho trang Account của Quyên.
 * Quản lý các sự kiện click chọn chức năng trong trang Account.
 */
public class Account extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvUserName;
    private ImageView imgAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp giao diện XML account vào Fragment
        View view = inflater.inflate(R.layout.account, container, false);
        dbHelper = new DatabaseHelper(requireContext());

        tvUserName = view.findViewById(R.id.tv_user_name);
        imgAvatar = view.findViewById(R.id.img_avatar);

        loadUserData();

        // Thiết lập sự kiện click cho các nút chức năng (bạn có thể mở rộng xử lý sau này)
        view.findViewById(R.id.btnMyProfile).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ProfileActivity.class));
        });

        view.findViewById(R.id.btnMyInvoices).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), InvoiceListActivity.class));
        });

        view.findViewById(R.id.btnPassengerInfo).setOnClickListener(v -> {
            // Xử lý khi nhấn "Thông tin hành khách" -> Chuyển sang màn hình PassengerList
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new PassengerList())
                    .addToBackStack(null)
                    .commit();
        });

//        view.findViewById(R.id.btnTravelPreferences).setOnClickListener(v -> {
//            // Xử lý khi nhấn "Sở thích du lịch" -> Chuyển sang màn hình TravelPreferences
//            requireActivity().getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.contentFrame, new TravelPreferences())
//                    .addToBackStack(null)
//                    .commit();
//        });

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
            try {
                android.net.Uri uri = android.net.Uri.parse("https://zalo.me/0858342303");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } catch (Exception e) {
                android.widget.Toast.makeText(requireContext(), "Không thể mở Zalo. Vui lòng cài đặt ứng dụng Zalo!", android.widget.Toast.LENGTH_SHORT).show();
            }
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
                        // Chuyển sang MainActivity (về trạng thái khách) và xóa toàn bộ back stack
                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        if (getContext() == null || tvUserName == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String contact = prefs.getString("current_user_contact", "");
        if (!contact.isEmpty()) {
            Map<String, String> userDetails = dbHelper.getUserDetails(contact);
            if (userDetails != null) {
                if (userDetails.get("name") != null) {
                    tvUserName.setText(userDetails.get("name"));
                }
                
                // Tải ảnh đại diện
                String avatarPath = userDetails.get("avatar");
                loadAvatarImage(avatarPath, imgAvatar);
            }
        }
    }

    private void loadAvatarImage(String path, ImageView imageView) {
        if (path != null && !path.isEmpty() && getContext() != null) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (myBitmap != null) {
                    imageView.setImageBitmap(myBitmap);
                    imageView.setImageTintList(null);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setPadding(0, 0, 0, 0);
                    return;
                }
            }
        }
        // Giao diện mặc định nếu chưa chọn ảnh đại diện
        imageView.setImageResource(R.drawable.ic_account);
        imageView.setImageTintList(ColorStateList.valueOf(0xFF185FA5));
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        int padding = (int) (16 * imageView.getResources().getDisplayMetrics().density);
        imageView.setPadding(padding, padding, padding, padding);
    }
}
