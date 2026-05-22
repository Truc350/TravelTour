package com.example.myapplication;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;

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
            String argType = getArguments().getString("tour_type");
            if (argType != null) {
                tourType = argType;
            }
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
        } else {
            // Mặc định hoặc Tour Miền Trung (central)
            if (ivHero != null) ivHero.setImageResource(R.drawable.img);
            if (tvTourTitle != null) {
                tvTourTitle.setText("Tour Miền Trung 4N3Đ: HCM - Đà Nẵng - Hội An - Huế - Quảng Bình - Động Phong Nha");
            }
            if (tvTourPrice != null) tvTourPrice.setText("7.990.000đ");
            if (tvAirlineBadge != null) tvAirlineBadge.setText("Bamboo Airways");
            if (tvRatingScore != null) tvRatingScore.setText("9,2");
            if (tvRatingStatus != null) tvRatingStatus.setText("Xuất sắc");
            if (tvReviewsCount != null) tvReviewsCount.setText("12 đánh giá");
        }

        // Sự kiện nút quay lại (Back)
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        // Sự kiện nút Yêu thích (Favorite)
        ImageButton btnFavorite = view.findViewById(R.id.btnFavorite);
        if (btnFavorite != null) {
            final String prefKey = "fav_" + tourType;
            if (getContext() != null) {
                SharedPreferences prefs = getContext().getSharedPreferences("TravelTourPrefs", Context.MODE_PRIVATE);
                boolean isFavorite = prefs.getBoolean(prefKey, false);

                // Update UI initial state
                if (isFavorite) {
                    btnFavorite.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
                } else {
                    btnFavorite.clearColorFilter();
                }

                btnFavorite.setOnClickListener(v -> {
                    boolean newFavState = !prefs.getBoolean(prefKey, false);
                    prefs.edit().putBoolean(prefKey, newFavState).apply();
                    if (newFavState) {
                        btnFavorite.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
                        Toast.makeText(getContext(), "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    } else {
                        btnFavorite.clearColorFilter();
                        Toast.makeText(getContext(), "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // Cấu hình Tabs "Thông tin cần lưu ý"
        setupNotesTabs(view, tourType);

        // Cấu hình các Tour liên quan
        setupRelatedTours(view, tourType);

        return view;
    }

    private void setupNotesTabs(View view, String tourType) {
        View layoutTabInclusions = view.findViewById(R.id.layoutTabInclusions);
        View layoutTabExclusions = view.findViewById(R.id.layoutTabExclusions);
        View layoutTabTerms = view.findViewById(R.id.layoutTabTerms);

        if (layoutTabInclusions != null && layoutTabExclusions != null && layoutTabTerms != null) {
            // Mặc định chọn tab inclusions lúc đầu
            updateTabs("inclusions", tourType, view);

            layoutTabInclusions.setOnClickListener(v -> updateTabs("inclusions", tourType, view));
            layoutTabExclusions.setOnClickListener(v -> updateTabs("exclusions", tourType, view));
            layoutTabTerms.setOnClickListener(v -> updateTabs("terms", tourType, view));
        }
    }

    private void updateTabs(String activeTab, String tourType, View view) {
        TextView tvTabInclusions = view.findViewById(R.id.tvTabInclusions);
        TextView tvTabExclusions = view.findViewById(R.id.tvTabExclusions);
        TextView tvTabTerms = view.findViewById(R.id.tvTabTerms);

        View viewTabInclusionsIndicator = view.findViewById(R.id.viewTabInclusionsIndicator);
        View viewTabExclusionsIndicator = view.findViewById(R.id.viewTabExclusionsIndicator);
        View viewTabTermsIndicator = view.findViewById(R.id.viewTabTermsIndicator);

        TextView tvNotesContent = view.findViewById(R.id.tvNotesContent);

        if (getContext() == null || tvTabInclusions == null || tvTabExclusions == null || tvTabTerms == null || tvNotesContent == null) return;

        int activeColor = ContextCompat.getColor(getContext(), R.color.cyan_active);
        int inactiveColor = ContextCompat.getColor(getContext(), R.color.grey_text);

        if ("inclusions".equals(activeTab)) {
            tvTabInclusions.setTextColor(activeColor);
            tvTabInclusions.setTypeface(null, Typeface.BOLD);
            if (viewTabInclusionsIndicator != null) viewTabInclusionsIndicator.setVisibility(View.VISIBLE);

            tvTabExclusions.setTextColor(inactiveColor);
            tvTabExclusions.setTypeface(null, Typeface.NORMAL);
            if (viewTabExclusionsIndicator != null) viewTabExclusionsIndicator.setVisibility(View.INVISIBLE);

            tvTabTerms.setTextColor(inactiveColor);
            tvTabTerms.setTypeface(null, Typeface.NORMAL);
            if (viewTabTermsIndicator != null) viewTabTermsIndicator.setVisibility(View.INVISIBLE);

            tvNotesContent.setText(getInclusionsText(tourType));
        } else if ("exclusions".equals(activeTab)) {
            tvTabInclusions.setTextColor(inactiveColor);
            tvTabInclusions.setTypeface(null, Typeface.NORMAL);
            if (viewTabInclusionsIndicator != null) viewTabInclusionsIndicator.setVisibility(View.INVISIBLE);

            tvTabExclusions.setTextColor(activeColor);
            tvTabExclusions.setTypeface(null, Typeface.BOLD);
            if (viewTabExclusionsIndicator != null) viewTabExclusionsIndicator.setVisibility(View.VISIBLE);

            tvTabTerms.setTextColor(inactiveColor);
            tvTabTerms.setTypeface(null, Typeface.NORMAL);
            if (viewTabTermsIndicator != null) viewTabTermsIndicator.setVisibility(View.INVISIBLE);

            tvNotesContent.setText(getExclusionsText(tourType));
        } else if ("terms".equals(activeTab)) {
            tvTabInclusions.setTextColor(inactiveColor);
            tvTabInclusions.setTypeface(null, Typeface.NORMAL);
            if (viewTabInclusionsIndicator != null) viewTabInclusionsIndicator.setVisibility(View.INVISIBLE);

            tvTabExclusions.setTextColor(inactiveColor);
            tvTabExclusions.setTypeface(null, Typeface.NORMAL);
            if (viewTabExclusionsIndicator != null) viewTabExclusionsIndicator.setVisibility(View.INVISIBLE);

            tvTabTerms.setTextColor(activeColor);
            tvTabTerms.setTypeface(null, Typeface.BOLD);
            if (viewTabTermsIndicator != null) viewTabTermsIndicator.setVisibility(View.VISIBLE);

            tvNotesContent.setText(getTermsText(tourType));
        }
    }

    private String getInclusionsText(String tourType) {
        if ("taiwan".equals(tourType)) {
            return "Vận Chuyển:\n- Vé máy bay khứ hồi HCM - Đài Bắc / Cao Hùng - HCM của Bamboo Airways.\n- Hành lý xách tay 07kg + ký gửi 20kg.\n- Xe du lịch đời mới máy lạnh suốt tuyến.\n\nLưu Trú:\n- Khách sạn tiêu chuẩn 3-4 sao Đài Loan (2 khách/phòng, lẻ nam/nữ ngủ phòng 3).\n\nKhác:\n- Ăn uống theo chương trình (bao gồm các bữa đặc sản địa phương).\n- Vé tham quan các điểm trong chương trình: Công viên Dã Liễu, Hồ Nhật Nguyệt...\n- HDV tiếng Việt nhiệt tình suốt tuyến.\n- Bảo hiểm du lịch quốc tế tối đa 1.000.000.000đ/vụ.";
        } else if ("singapore".equals(tourType)) {
            return "Vận Chuyển:\n- Vé máy bay khứ hồi HCM - Singapore / Kuala Lumpur - HCM của Singapore Airlines.\n- Hành lý xách tay 07kg + 20kg ký gửi.\n- Xe đưa đón tham quan theo chương trình.\n\nLưu Trú:\n- Khách sạn 3-4 sao trung tâm (2 khách/phòng, phòng 3 nếu lẻ).\n\nKhác:\n- Các bữa ăn theo chương trình.\n- Vé vào cổng các điểm tham quan.\n- HDV địa phương & trưởng đoàn từ Việt Nam.\n- Bảo hiểm du lịch quốc tế.";
        } else {
            return "Vận Chuyển:\n- Vé máy bay khứ hồi Bamboo Airways. Thuế và phí sân bay. bao gồm hành lý 07kg xách tay + 20kg ký gửi\n- Xe du lịch đưa đón tham quan theo chương trình.\n- Thuyền đi Động Phong Nha.\n\nLưu Trú:\n- Khách sạn 4 sao địa phương, tiêu chuẩn 2-3 khách/phòng.\n\nKhác:\n- Ăn các bữa theo chương trình.\n- Vé tại các điểm tham quan.\n- Hướng dẫn viên tiếng Việt theo đoàn suốt tuyến.\n- Phục vụ 01 chai 0.5l/khách /ngày\n- Bảo hiểm du lịch: 20.000.000đ/vụ";
        }
    }

    private String getExclusionsText(String tourType) {
        if ("taiwan".equals(tourType)) {
            return "Chi phí cá nhân:\n- Hộ chiếu còn hạn trên 6 tháng.\n- Chi phí cá nhân, nước uống tự gọi.\n- Tiền tip bắt buộc cho HDV và tài xế: 5 USD/khách/ngày.\n- Chi phí visa tái nhập Việt Nam đối với khách nước ngoài.\n- Phụ thu phòng đơn.";
        } else if ("singapore".equals(tourType)) {
            return "Chi phí cá nhân:\n- Hộ chiếu, chi tiêu cá nhân ngoài chương trình.\n- Tiền tip bắt buộc cho HDV và tài xế: 5 USD/khách/ngày.\n- Phụ thu phòng đơn.";
        } else {
            return "Chi phí cá nhân:\n- Ăn uống ngoài chương trình, nước uống tự gọi trong bữa ăn...\n- Chi phí cá nhân: giặt ủi, điện thoại...\n- Tiền tip cho HDV và tài xế (khoảng 150.000đ/khách/ngày)\n- Phụ thu phòng đơn (nếu có yêu cầu ngủ riêng)";
        }
    }

    private String getTermsText(String tourType) {
        if ("taiwan".equals(tourType)) {
            return "Chính sách trẻ em:\n- Trẻ em dưới 2 tuổi: 30% giá tour (không có giường riêng).\n- Trẻ em từ 2 đến dưới 11 tuổi: 85% giá tour (không giường riêng), 95% giá tour (có giường riêng).\n- Trẻ em từ 11 tuổi trở lên: 100% giá tour.";
        } else if ("singapore".equals(tourType)) {
            return "Chính sách trẻ em:\n- Trẻ em dưới 2 tuổi: 30% giá tour.\n- Trẻ em từ 2 - dưới 11 tuổi: 85% giá tour (không giường), 95% giá tour (có giường).\n- Trẻ em từ 11 tuổi: 100% giá tour.";
        } else {
            return "Chính sách trẻ em:\n- Trẻ em dưới 2 tuổi: 10% giá tour\n- Trẻ em từ 2 - dưới 11 tuổi: 90% giá tour (ngủ chung giường với bố mẹ)\n- Trẻ em từ 11 tuổi trở lên: tính như người lớn\n- Quy định hủy tour: hủy trước 15 ngày mất 50%, hủy trước 7 ngày mất 100% giá tour.";
        }
    }

    private void setupRelatedTours(View view, String tourType) {
        View cardRelatedCentral = view.findViewById(R.id.cardRelatedCentral);
        View cardRelatedDynamic = view.findViewById(R.id.cardRelatedDynamic);

        ImageView ivRelatedImage1 = view.findViewById(R.id.ivRelatedImage1);
        TextView tvRelatedTitle1 = view.findViewById(R.id.tvRelatedTitle1);
        TextView tvRelatedDuration1 = view.findViewById(R.id.tvRelatedDuration1);
        TextView tvRelatedPrice1 = view.findViewById(R.id.tvRelatedPrice1);

        ImageView ivRelatedImage2 = view.findViewById(R.id.ivRelatedImage2);
        TextView tvRelatedTitle2 = view.findViewById(R.id.tvRelatedTitle2);
        TextView tvRelatedDuration2 = view.findViewById(R.id.tvRelatedDuration2);
        TextView tvRelatedRating2 = view.findViewById(R.id.tvRelatedRating2);
        TextView tvRelatedPrice2 = view.findViewById(R.id.tvRelatedPrice2);

        if ("taiwan".equals(tourType)) {
            // Card 1 is Central, Card 2 is Singapore
            if (ivRelatedImage1 != null) ivRelatedImage1.setImageResource(R.drawable.img);
            if (tvRelatedTitle1 != null) tvRelatedTitle1.setText("Tour Miền Trung 4N3Đ: HCM - Huế - Quảng Bình - Đà Nẵng -...");
            if (tvRelatedDuration1 != null) tvRelatedDuration1.setText("4 ngày 3 đêm");
            if (tvRelatedPrice1 != null) tvRelatedPrice1.setText("6.990.000đ");

            if (ivRelatedImage2 != null) ivRelatedImage2.setImageResource(R.drawable.img_singapore_tour);
            if (tvRelatedTitle2 != null) tvRelatedTitle2.setText("Tour Singapore - Malaysia 5N4Đ: HCM - Singapore - Kuala Lumpur...");
            if (tvRelatedDuration2 != null) tvRelatedDuration2.setText("5 ngày 4 đêm");
            if (tvRelatedRating2 != null) tvRelatedRating2.setText("9,1");
            if (tvRelatedPrice2 != null) tvRelatedPrice2.setText("12.890.000đ");

            if (cardRelatedCentral != null) cardRelatedCentral.setOnClickListener(v -> navigateToTour("central"));
            if (cardRelatedDynamic != null) cardRelatedDynamic.setOnClickListener(v -> navigateToTour("singapore"));

        } else if ("singapore".equals(tourType)) {
            // Card 1 is Central, Card 2 is Taiwan
            if (ivRelatedImage1 != null) ivRelatedImage1.setImageResource(R.drawable.img);
            if (tvRelatedTitle1 != null) tvRelatedTitle1.setText("Tour Miền Trung 4N3Đ: HCM - Huế - Quảng Bình - Đà Nẵng -...");
            if (tvRelatedDuration1 != null) tvRelatedDuration1.setText("4 ngày 3 đêm");
            if (tvRelatedPrice1 != null) tvRelatedPrice1.setText("6.990.000đ");

            if (ivRelatedImage2 != null) ivRelatedImage2.setImageResource(R.drawable.img_taiwan_tour);
            if (tvRelatedTitle2 != null) tvRelatedTitle2.setText("Tour Đài Loan 5N4Đ: HCM - Cao Hùng - Đài Trung - Đài Bắc -...");
            if (tvRelatedDuration2 != null) tvRelatedDuration2.setText("5 ngày 4 đêm");
            if (tvRelatedRating2 != null) tvRelatedRating2.setText("9,0");
            if (tvRelatedPrice2 != null) tvRelatedPrice2.setText("14.390.000đ");

            if (cardRelatedCentral != null) cardRelatedCentral.setOnClickListener(v -> navigateToTour("central"));
            if (cardRelatedDynamic != null) cardRelatedDynamic.setOnClickListener(v -> navigateToTour("taiwan"));

        } else {
            // Current is Central (or empty). Show Singapore as Card 1 and Taiwan as Card 2
            if (ivRelatedImage1 != null) ivRelatedImage1.setImageResource(R.drawable.img_singapore_tour);
            if (tvRelatedTitle1 != null) tvRelatedTitle1.setText("Tour Singapore - Malaysia 5N4Đ: HCM - Singapore - Kuala Lumpur...");
            if (tvRelatedDuration1 != null) tvRelatedDuration1.setText("5 ngày 4 đêm");
            if (tvRelatedPrice1 != null) tvRelatedPrice1.setText("12.890.000đ");

            if (ivRelatedImage2 != null) ivRelatedImage2.setImageResource(R.drawable.img_taiwan_tour);
            if (tvRelatedTitle2 != null) tvRelatedTitle2.setText("Tour Đài Loan 5N4Đ: HCM - Cao Hùng - Đài Trung - Đài Bắc -...");
            if (tvRelatedDuration2 != null) tvRelatedDuration2.setText("5 ngày 4 đêm");
            if (tvRelatedRating2 != null) tvRelatedRating2.setText("9,0");
            if (tvRelatedPrice2 != null) tvRelatedPrice2.setText("14.390.000đ");

            if (cardRelatedCentral != null) cardRelatedCentral.setOnClickListener(v -> navigateToTour("singapore"));
            if (cardRelatedDynamic != null) cardRelatedDynamic.setOnClickListener(v -> navigateToTour("taiwan"));
        }
    }

    private void navigateToTour(String tourType) {
        DetailTour newFragment = new DetailTour();
        Bundle args = new Bundle();
        args.putString("tour_type", tourType);
        newFragment.setArguments(args);

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, newFragment)
                    .addToBackStack(null)
                    .commit();
        }
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
