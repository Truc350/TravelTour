package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

/**
 * Fragment đại diện cho danh sách yêu thích (Wishlist).
 * Tải động danh sách tour đã thả tim từ SQLite theo từng người dùng.
 */
public class Wishlist extends Fragment {

    private View layoutEmptyState;
    private View scrollViewFavoriteList;
    private LinearLayout favoriteContainer;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wishlist, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Ánh xạ views
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        scrollViewFavoriteList = view.findViewById(R.id.scrollViewFavoriteList);
        favoriteContainer = view.findViewById(R.id.favoriteContainer);

        // Lấy session đăng nhập hiện tại
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String contact = prefs.getString("current_user_contact", "");
        if (contact.isEmpty()) {
            contact = dbHelper.getLastUserContact();
        }

        if (!contact.isEmpty()) {
            loadFavorites(inflater, contact);
        } else {
            // Trường hợp lỗi chưa đăng nhập
            layoutEmptyState.setVisibility(View.VISIBLE);
            scrollViewFavoriteList.setVisibility(View.GONE);
        }

        return view;
    }

    private void loadFavorites(LayoutInflater inflater, String contact) {
        List<String> favoriteTourTypes = dbHelper.getFavoriteTours(contact);

        if (favoriteTourTypes.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            scrollViewFavoriteList.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            scrollViewFavoriteList.setVisibility(View.VISIBLE);
            favoriteContainer.removeAllViews();

            for (String tourType : favoriteTourTypes) {
                // Nạp tệp giao diện item_tour_card
                View itemView = inflater.inflate(R.layout.item_tour_card, favoriteContainer, false);

                // Ánh xạ các views trong card
                ImageView ivTourImage = itemView.findViewById(R.id.ivTourImage);
                TextView tvRibbonBadge = itemView.findViewById(R.id.tvRibbonBadge);
                TextView tvTourTitle = itemView.findViewById(R.id.tvTourTitle);
                TextView tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
                TextView tvNewPrice = itemView.findViewById(R.id.tvNewPrice);
                TextView btnViewTour = itemView.findViewById(R.id.btnViewTour);

                // Gán dữ liệu tương ứng theo tourType
                int imageResId = R.drawable.img;
                String ribbonBadge = "";
                String title = "";
                String oldPrice = "";
                String newPrice = "";

                if ("taiwan".equals(tourType)) {
                    imageResId = R.drawable.img_taiwan_tour;
                    ribbonBadge = "Bamboo Airways";
                    title = "Tour Đài Loan 5N4Đ: HCM - Cao Hùng - Đài Trung - Đài Bắc - Đảo Hoà Bình";
                    oldPrice = "15.500.000";
                    newPrice = "14.390.000";
                } else if ("singapore".equals(tourType)) {
                    imageResId = R.drawable.img_singapore_tour;
                    ribbonBadge = "Singapore Airlines";
                    title = "Tour Singapore - Malaysia 5N4Đ: HCM - Singapore - Kuala Lumpur - Genting";
                    oldPrice = "13.900.000";
                    newPrice = "12.890.000";
                } else if ("sapa".equals(tourType)) {
                    imageResId = R.drawable.img_sapa_tour;
                    ribbonBadge = "Xe giường nằm cabin VIP";
                    title = "Tour Sapa 3N2Đ: Hà Nội - Bản Cát Cát - Chinh Phục Đỉnh Fansipan";
                    oldPrice = "3.790.000";
                    newPrice = "3.290.000";
                } else if ("halong".equals(tourType)) {
                    imageResId = R.drawable.img_halong_tour;
                    ribbonBadge = "Du Thuyền 5 Sao cao cấp";
                    title = "Tour Vịnh Hạ Long 2N1Đ: Nghỉ Dưỡng Trên Du Thuyền Sang Trọng";
                    oldPrice = "2.990.000";
                    newPrice = "2.590.000";
                } else if ("danang".equals(tourType)) {
                    imageResId = R.drawable.img_danang_tour;
                    ribbonBadge = "Vietnam Airlines";
                    title = "Tour Đà Nẵng - Hội An - Bà Nà Hills 4N3Đ Trọn Gói Giá Tốt";
                    oldPrice = "5.490.000";
                    newPrice = "4.890.000";
                } else if ("nhatrang".equals(tourType)) {
                    imageResId = R.drawable.img_nhatrang_tour;
                    ribbonBadge = "VietJet Air";
                    title = "Tour Nha Trang 3N2Đ: Khám Phá Vịnh San Hô - VinWonders Trọn Gói";
                    oldPrice = "3.590.000";
                    newPrice = "3.190.000";
                } else if ("phuquoc".equals(tourType)) {
                    imageResId = R.drawable.img_phuquoc_tour;
                    ribbonBadge = "Vietnam Airlines";
                    title = "Tour Phú Quốc 3N2Đ: Khám Phá Địa Trung Hải - Grand World Trọn Gói";
                    oldPrice = "5.190.000";
                    newPrice = "4.590.000";
                } else if ("mientay".equals(tourType)) {
                    imageResId = R.drawable.img_mientay_tour;
                    ribbonBadge = "Xe du lịch đời mới";
                    title = "Tour Miền Tây Sông Nước 2N1Đ: Mỹ Tho - Cần Thơ - Chợ Nổi Cái Răng";
                    oldPrice = "2.190.000";
                    newPrice = "1.890.000";
                }

                // Gán dữ liệu vào views
                if (ivTourImage != null) ivTourImage.setImageResource(imageResId);
                if (tvRibbonBadge != null) tvRibbonBadge.setText(ribbonBadge);
                if (tvTourTitle != null) tvTourTitle.setText(title);
                if (tvNewPrice != null) tvNewPrice.setText(newPrice);
                
                if (tvOldPrice != null) {
                    tvOldPrice.setText(oldPrice);
                    // Áp dụng gạch ngang giá cũ
                    tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                }

                // Sự kiện click mở chi tiết tour
                View.OnClickListener openDetailListener = v -> {
                    DetailTour detailFragment = new DetailTour();
                    Bundle args = new Bundle();
                    args.putString("tour_type", tourType);
                    detailFragment.setArguments(args);

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.contentFrame, detailFragment)
                            .addToBackStack(null)
                            .commit();
                };

                if (btnViewTour != null) btnViewTour.setOnClickListener(openDetailListener);
                itemView.setOnClickListener(openDetailListener);

                // Thêm view vào danh sách cuộn
                favoriteContainer.addView(itemView);
            }
        }
    }
}
