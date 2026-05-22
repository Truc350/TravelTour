package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment hiển thị chi tiết của một tour cụ thể.
 */
public class DetailTour extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp giao diện XML detail_tour vào Fragment
        View view = inflater.inflate(R.layout.detail_tour, container, false);

        String tourType = "";
        if (getArguments() != null) {
            tourType = getArguments().getString("tour_type", "");
        }

        // Lấy tham chiếu đến các view trong chi tiết
        ImageView ivHero = view.findViewById(R.id.ivHero);
        TextView tvTourTitle = view.findViewById(R.id.tvTourTitle);
        TextView tvTourPrice = view.findViewById(R.id.tvTourPrice);
        TextView tvAirlineBadge = view.findViewById(R.id.tvAirlineBadge);
        TextView tvRatingScore = view.findViewById(R.id.tvRatingScore);
        TextView tvRatingStatus = view.findViewById(R.id.tvRatingStatus);
        TextView tvReviewsCount = view.findViewById(R.id.tvReviewsCount);
        View btnBack = view.findViewById(R.id.btnBack);

        // Gán dữ liệu tương ứng theo tour đã chọn
        if ("taiwan".equals(tourType)) {
            if (ivHero != null) ivHero.setImageResource(R.drawable.img_taiwan_tour);
            if (tvTourTitle != null) {
                tvTourTitle.setText("Tour Đài Loan 5N4Đ: HCM - Cao Hùng - Đài Trung - Đài Bắc - Đảo Hoà Bình");
            }
            if (tvTourPrice != null) tvTourPrice.setText("14.390.000đ");
            if (tvAirlineBadge != null) tvAirlineBadge.setText("Bamboo Airways");
            if (tvRatingScore != null) tvRatingScore.setText("9,0");
            if (tvRatingStatus != null) tvRatingStatus.setText("Rất tốt");
            if (tvReviewsCount != null) tvReviewsCount.setText("4 đánh giá");
        } else if ("singapore".equals(tourType)) {
            if (ivHero != null) ivHero.setImageResource(R.drawable.img_singapore_tour);
            if (tvTourTitle != null) {
                tvTourTitle.setText("Tour Singapore - Malaysia 5N4Đ: HCM - Singapore - Kuala Lumpur - Genting");
            }
            if (tvTourPrice != null) tvTourPrice.setText("12.890.000đ");
            if (tvAirlineBadge != null) tvAirlineBadge.setText("Singapore Airlines");
            if (tvRatingScore != null) tvRatingScore.setText("9,1");
            if (tvRatingStatus != null) tvRatingStatus.setText("Xuất sắc");
            if (tvReviewsCount != null) tvReviewsCount.setText("10 đánh giá");
        }

        // Sự kiện nút quay lại (Back)
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Ẩn thanh BottomNavigationView của MainActivity khi xem chi tiết
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
        // Hiện lại thanh BottomNavigationView khi thoát màn hình chi tiết
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.bottomNavigation);
            if (nav != null) {
                nav.setVisibility(View.VISIBLE);
            }
        }
    }
}
