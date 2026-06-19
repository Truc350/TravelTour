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
import com.example.myapplication.data.model.Tour;

public class DetailTour extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp giao diện XML detail_tour vào Fragment
        View view = inflater.inflate(R.layout.detail_tour, container, false);

        Tour tour = null;
        String tourType = "";
        if (getArguments() != null) {
            tour = (Tour) getArguments().getSerializable("tour_object");
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
        if (tour != null) {
            if (tvTourTitle != null) {
                tvTourTitle.setText(tour.getTitle());
            }
            if (tvTourPrice != null) {
                java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN"));
                tvTourPrice.setText(formatter.format(tour.getDiscountPrice()) + "đ");
            }
            if (tvAirlineBadge != null) {
                tvAirlineBadge.setText(tour.getProvider());
            }
            if (tvRatingScore != null) {
                tvRatingScore.setText(String.format(java.util.Locale.US, "%.1f", tour.getRatingScore()));
            }
            if (tvRatingStatus != null) {
                double score = tour.getRatingScore();
                if (score >= 9.0) tvRatingStatus.setText("Xuất sắc");
                else if (score >= 8.0) tvRatingStatus.setText("Tuyệt vời");
                else if (score >= 7.0) tvRatingStatus.setText("Rất tốt");
                else tvRatingStatus.setText("Tốt");
            }
            if (tvReviewsCount != null) {
                tvReviewsCount.setText(tour.getReviewsCount() + " đánh giá");
            }
            // Xử lý hình ảnh (load bằng Glide nếu có URL)
            if (ivHero != null) {
                String imageUrl = null;
                if (tour.getImages() != null && !tour.getImages().isEmpty()) {
                    imageUrl = tour.getImages().get(0).getImageUrl();
                }
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    if (imageUrl.startsWith("/")) {
                        imageUrl = "http://10.0.2.2:8000" + imageUrl;
                    }
                    com.bumptech.glide.Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.img_taiwan_tour)
                            .centerCrop()
                            .into(ivHero);
                } else {
                    // Fallback theo mã tour hoặc mặc định
                    String code = tour.getCode();
                    if (code != null) {
                        if (code.toLowerCase().contains("taiwan")) ivHero.setImageResource(R.drawable.img_taiwan_tour);
                        else if (code.toLowerCase().contains("singapore")) ivHero.setImageResource(R.drawable.img_singapore_tour);
                        else if (code.toLowerCase().contains("sapa")) ivHero.setImageResource(R.drawable.img_sapa_tour);
                        else if (code.toLowerCase().contains("halong")) ivHero.setImageResource(R.drawable.img_halong_tour);
                        else if (code.toLowerCase().contains("danang")) ivHero.setImageResource(R.drawable.img_danang_tour);
                        else if (code.toLowerCase().contains("nhatrang")) ivHero.setImageResource(R.drawable.img_nhatrang_tour);
                        else if (code.toLowerCase().contains("phuquoc")) ivHero.setImageResource(R.drawable.img_phuquoc_tour);
                        else if (code.toLowerCase().contains("mientay")) ivHero.setImageResource(R.drawable.img_mientay_tour);
                        else ivHero.setImageResource(R.drawable.img_taiwan_tour); // default
                    } else {
                        ivHero.setImageResource(R.drawable.img_taiwan_tour);
                    }
                }
            }

            // Gán thông tin khởi hành, mã tour, trải nghiệm chi tiết
            TextView tvDepartureLocation = view.findViewById(R.id.tvDepartureLocation);
            if (tvDepartureLocation != null) {
                String title = tour.getTitle();
                if (title != null) {
                    if (title.toUpperCase().contains("HÀ NỘI") || title.toUpperCase().contains("HN ")) {
                        tvDepartureLocation.setText("Hà Nội");
                    } else if (title.toUpperCase().contains("HCM") || title.toUpperCase().contains("HỒ CHÍ MINH") || title.toUpperCase().contains("SÀI GÒN")) {
                        tvDepartureLocation.setText("Hồ Chí Minh");
                    } else if (title.toUpperCase().contains("ĐÀ NẴNG")) {
                        tvDepartureLocation.setText("Đà Nẵng");
                    } else {
                        tvDepartureLocation.setText("Hồ Chí Minh");
                    }
                }
            }

            TextView tvTourCode = view.findViewById(R.id.tvTourCode);
            if (tvTourCode != null) {
                tvTourCode.setText(tour.getCode());
            }

            TextView tvTourDescription = view.findViewById(R.id.tvTourDescription);
            if (tvTourDescription != null && tour.getDescription() != null) {
                tvTourDescription.setText(tour.getDescription());
            }

            // Xử lý các tab Điều khoản, Giá bao gồm, Giá không bao gồm
            android.widget.LinearLayout layoutTabInclusions = view.findViewById(R.id.layoutTabInclusions);
            android.widget.LinearLayout layoutTabExclusions = view.findViewById(R.id.layoutTabExclusions);
            android.widget.LinearLayout layoutTabTerms = view.findViewById(R.id.layoutTabTerms);

            TextView tvTabInclusions = view.findViewById(R.id.tvTabInclusions);
            TextView tvTabExclusions = view.findViewById(R.id.tvTabExclusions);
            TextView tvTabTerms = view.findViewById(R.id.tvTabTerms);

            View viewTabInclusionsIndicator = view.findViewById(R.id.viewTabInclusionsIndicator);
            View viewTabExclusionsIndicator = view.findViewById(R.id.viewTabExclusionsIndicator);
            View viewTabTermsIndicator = view.findViewById(R.id.viewTabTermsIndicator);

            TextView tvNotesContent = view.findViewById(R.id.tvNotesContent);

            int colorActive = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.cyan_active);
            int colorGrey = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.grey_text);

            final String inclusionsText = tour.getDescriptionTourInclude() != null ? tour.getDescriptionTourInclude() : "";
            final String exclusionsText = "- Chi phí cá nhân, ăn uống ngoài chương trình.\n- Tiền tip cho HDV và tài xế.\n- Thuế VAT (nếu không có trong giá tour).";
            final String termsText = tour.getNote() != null ? tour.getNote() : "";

            // Mặc định chọn tab "Giá bao gồm"
            if (tvNotesContent != null) {
                tvNotesContent.setText(inclusionsText);
            }

            if (layoutTabInclusions != null) {
                layoutTabInclusions.setOnClickListener(v -> {
                    if (tvNotesContent != null) tvNotesContent.setText(inclusionsText);
                    if (tvTabInclusions != null) tvTabInclusions.setTextColor(colorActive);
                    if (tvTabExclusions != null) tvTabExclusions.setTextColor(colorGrey);
                    if (tvTabTerms != null) tvTabTerms.setTextColor(colorGrey);
                    if (viewTabInclusionsIndicator != null) viewTabInclusionsIndicator.setVisibility(View.VISIBLE);
                    if (viewTabExclusionsIndicator != null) viewTabExclusionsIndicator.setVisibility(View.INVISIBLE);
                    if (viewTabTermsIndicator != null) viewTabTermsIndicator.setVisibility(View.INVISIBLE);
                });
            }

            if (layoutTabExclusions != null) {
                layoutTabExclusions.setOnClickListener(v -> {
                    if (tvNotesContent != null) tvNotesContent.setText(exclusionsText);
                    if (tvTabInclusions != null) tvTabInclusions.setTextColor(colorGrey);
                    if (tvTabExclusions != null) tvTabExclusions.setTextColor(colorActive);
                    if (tvTabTerms != null) tvTabTerms.setTextColor(colorGrey);
                    if (viewTabInclusionsIndicator != null) viewTabInclusionsIndicator.setVisibility(View.INVISIBLE);
                    if (viewTabExclusionsIndicator != null) viewTabExclusionsIndicator.setVisibility(View.VISIBLE);
                    if (viewTabTermsIndicator != null) viewTabTermsIndicator.setVisibility(View.INVISIBLE);
                });
            }

            if (layoutTabTerms != null) {
                layoutTabTerms.setOnClickListener(v -> {
                    if (tvNotesContent != null) tvNotesContent.setText(termsText);
                    if (tvTabInclusions != null) tvTabInclusions.setTextColor(colorGrey);
                    if (tvTabExclusions != null) tvTabExclusions.setTextColor(colorGrey);
                    if (tvTabTerms != null) tvTabTerms.setTextColor(colorActive);
                    if (viewTabInclusionsIndicator != null) viewTabInclusionsIndicator.setVisibility(View.INVISIBLE);
                    if (viewTabExclusionsIndicator != null) viewTabExclusionsIndicator.setVisibility(View.INVISIBLE);
                    if (viewTabTermsIndicator != null) viewTabTermsIndicator.setVisibility(View.VISIBLE);
                });
            }

            // Gán lịch trình động từ database/API
            android.widget.LinearLayout itineraryContainer = view.findViewById(R.id.itineraryContainer);
            if (itineraryContainer != null && tour.getItineraries() != null && !tour.getItineraries().isEmpty()) {
                itineraryContainer.removeAllViews();
                for (com.example.myapplication.data.model.TourItinerary itinerary : tour.getItineraries()) {
                    View itemItinerary = inflater.inflate(R.layout.item_itinerary, itineraryContainer, false);
                    
                    TextView tvDayNumber = itemItinerary.findViewById(R.id.tvDayNumber);
                    TextView tvDayTitle = itemItinerary.findViewById(R.id.tvDayTitle);
                    ImageView ivDayImage = itemItinerary.findViewById(R.id.ivDayImage);

                    if (tvDayNumber != null) {
                        tvDayNumber.setText("Ngày " + itinerary.getDayNumber());
                    }
                    if (tvDayTitle != null) {
                        tvDayTitle.setText(itinerary.getTitle());
                    }
                    
                    // Lấy ảnh tương ứng từ danh sách ảnh của Tour
                    if (ivDayImage != null && tour.getImages() != null && !tour.getImages().isEmpty()) {
                        int imgIndex = (itinerary.getDayNumber() - 1) % tour.getImages().size();
                        String dayImageUrl = tour.getImages().get(imgIndex).getImageUrl();
                        if (dayImageUrl != null && !dayImageUrl.isEmpty()) {
                            if (dayImageUrl.startsWith("/")) {
                                dayImageUrl = "http://10.0.2.2:8000" + dayImageUrl;
                            }
                            com.bumptech.glide.Glide.with(this)
                                    .load(dayImageUrl)
                                    .placeholder(R.drawable.img_taiwan_tour)
                                    .centerCrop()
                                    .into(ivDayImage);
                        }
                    }

                    itineraryContainer.addView(itemItinerary);
                }
            }
        } else if (!tourType.isEmpty()) {
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
