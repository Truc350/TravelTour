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
        } else if ("sapa".equals(tourType)) {
            if (ivHero != null) ivHero.setImageResource(R.drawable.img_sapa_tour);
            if (tvTourTitle != null) {
                tvTourTitle.setText("Tour Sapa 3N2Đ: Hà Nội - Bản Cát Cát - Chinh Phục Đỉnh Fansipan");
            }
            if (tvTourPrice != null) tvTourPrice.setText("3.290.000đ");
            if (tvAirlineBadge != null) tvAirlineBadge.setText("Xe giường nằm cabin VIP");
            if (tvRatingScore != null) tvRatingScore.setText("9,2");
            if (tvRatingStatus != null) tvRatingStatus.setText("Tuyệt vời");
            if (tvReviewsCount != null) tvReviewsCount.setText("15 đánh giá");
        } else if ("halong".equals(tourType)) {
            if (ivHero != null) ivHero.setImageResource(R.drawable.img_halong_tour);
            if (tvTourTitle != null) {
                tvTourTitle.setText("Tour Vịnh Hạ Long 2N1Đ: Nghỉ Dưỡng Trên Du Thuyền Sang Trọng");
            }
            if (tvTourPrice != null) tvTourPrice.setText("2.590.000đ");
            if (tvAirlineBadge != null) tvAirlineBadge.setText("Du Thuyền 5 Sao cao cấp");
            if (tvRatingScore != null) tvRatingScore.setText("9,4");
            if (tvRatingStatus != null) tvRatingStatus.setText("Xuất sắc");
            if (tvReviewsCount != null) tvReviewsCount.setText("25 đánh giá");
        } else if ("danang".equals(tourType)) {
            if (ivHero != null) ivHero.setImageResource(R.drawable.img_danang_tour);
            if (tvTourTitle != null) {
                tvTourTitle.setText("Tour Đà Nẵng - Hội An - Bà Nà Hills 4N3Đ Trọn Gói Giá Tốt");
            }
            if (tvTourPrice != null) tvTourPrice.setText("4.890.000đ");
            if (tvAirlineBadge != null) tvAirlineBadge.setText("Vietnam Airlines");
            if (tvRatingScore != null) tvRatingScore.setText("9,3");
            if (tvRatingStatus != null) tvRatingStatus.setText("Xuất sắc");
            if (tvReviewsCount != null) tvReviewsCount.setText("30 đánh giá");
        } else if ("nhatrang".equals(tourType)) {
            if (ivHero != null) ivHero.setImageResource(R.drawable.img_nhatrang_tour);
            if (tvTourTitle != null) {
                tvTourTitle.setText("Tour Nha Trang 3N2Đ: Khám Phá Vịnh San Hô - VinWonders Trọn Gói");
            }
            if (tvTourPrice != null) tvTourPrice.setText("3.190.000đ");
            if (tvAirlineBadge != null) tvAirlineBadge.setText("VietJet Air");
            if (tvRatingScore != null) tvRatingScore.setText("9,0");
            if (tvRatingStatus != null) tvRatingStatus.setText("Rất tốt");
            if (tvReviewsCount != null) tvReviewsCount.setText("12 đánh giá");
        } else if ("phuquoc".equals(tourType)) {
            if (ivHero != null) ivHero.setImageResource(R.drawable.img_phuquoc_tour);
            if (tvTourTitle != null) {
                tvTourTitle.setText("Tour Phú Quốc 3N2Đ: Khám Phá Địa Trung Hải - Grand World Trọn Gói");
            }
            if (tvTourPrice != null) tvTourPrice.setText("4.590.000đ");
            if (tvAirlineBadge != null) tvAirlineBadge.setText("Vietnam Airlines");
            if (tvRatingScore != null) tvRatingScore.setText("9,5");
            if (tvRatingStatus != null) tvRatingStatus.setText("Xuất sắc");
            if (tvReviewsCount != null) tvReviewsCount.setText("40 đánh giá");
        } else if ("mientay".equals(tourType)) {
            if (ivHero != null) ivHero.setImageResource(R.drawable.img_mientay_tour);
            if (tvTourTitle != null) {
                tvTourTitle.setText("Tour Miền Tây Sông Nước 2N1Đ: Mỹ Tho - Cần Thơ - Chợ Nổi Cái Răng");
            }
            if (tvTourPrice != null) tvTourPrice.setText("1.890.000đ");
            if (tvAirlineBadge != null) tvAirlineBadge.setText("Xe du lịch đời mới máy lạnh");
            if (tvRatingScore != null) tvRatingScore.setText("8,8");
            if (tvRatingStatus != null) tvRatingStatus.setText("Rất tốt");
            if (tvReviewsCount != null) tvReviewsCount.setText("18 đánh giá");
        }

        // Sự kiện nút quay lại (Back)
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        // Sự kiện mở Lịch khởi hành khi nhấn vào các phần tử tương ứng
        final String finalTourTitle = tvTourTitle != null ? tvTourTitle.getText().toString() : "";
        View.OnClickListener openDepartureListener = v -> {
            android.content.Intent intent = new android.content.Intent(requireContext(), DepartureActivity.class);
            intent.putExtra("tour_title", finalTourTitle);
            startActivity(intent);
        };

        View headerDeparture = view.findViewById(R.id.headerDeparture);
        if (headerDeparture != null) {
            headerDeparture.setOnClickListener(openDepartureListener);
        }

        View sectionDeparture = view.findViewById(R.id.sectionDeparture);
        if (sectionDeparture != null) {
            sectionDeparture.setOnClickListener(openDepartureListener);
        }

        View btnSelectDate = view.findViewById(R.id.btnSelectDate);
        if (btnSelectDate != null) {
            btnSelectDate.setOnClickListener(openDepartureListener);
        }

        View btnBookTour = view.findViewById(R.id.btnBookTour);
        if (btnBookTour != null) {
            btnBookTour.setOnClickListener(openDepartureListener);
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
