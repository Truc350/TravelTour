package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.model.Favorite;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailTour extends Fragment {

    private Tour tour;
    private String tourType = "";
    private int currentUserId = -1;
    private boolean isFavorite = false;
    private int favoriteId = -1;
    private ImageButton btnFavorite;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp giao diện XML detail_tour vào Fragment
        View view = inflater.inflate(R.layout.detail_tour, container, false);

        apiService = RetrofitClient.getClient().create(ApiService.class);
        btnFavorite = view.findViewById(R.id.btnFavorite);

        if (getArguments() != null) {
            tour = (Tour) getArguments().getSerializable("tour_object");
            tourType = getArguments().getString("tour_type", "");
        }

        // Lấy session đăng nhập hiện tại
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = prefs.getInt("current_user_id", -1);
        String contact = prefs.getString("current_user_contact", "");

        if (currentUserId == -1 && !contact.isEmpty()) {
            apiService.getUsers().enqueue(new Callback<List<com.example.myapplication.data.model.User>>() {
                @Override
                public void onResponse(Call<List<com.example.myapplication.data.model.User>> call, Response<List<com.example.myapplication.data.model.User>> response) {
                    if (getContext() == null || !isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        boolean found = false;
                        for (com.example.myapplication.data.model.User u : response.body()) {
                            if (contact.equals(u.getContact())) {
                                currentUserId = u.getId();
                                prefs.edit().putInt("current_user_id", currentUserId).apply();
                                checkFavoriteStatus();
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            registerUserOnServer(contact);
                        }
                    }
                }
                @Override
                public void onFailure(Call<List<com.example.myapplication.data.model.User>> call, Throwable t) {}
            });
        }

        // Lấy tham chiếu đến các view trong chi tiết
        TextView tvTourTitle = view.findViewById(R.id.tvTourTitle);
        TextView tvTourPrice = view.findViewById(R.id.tvTourPrice);
        TextView tvAirlineBadge = view.findViewById(R.id.tvAirlineBadge);
        TextView tvRatingScore = view.findViewById(R.id.tvRatingScore);
        TextView tvRatingStatus = view.findViewById(R.id.tvRatingStatus);
        TextView tvReviewsCount = view.findViewById(R.id.tvReviewsCount);
        View btnBack = view.findViewById(R.id.btnBack);

        // Thiết lập sự kiện nút yêu thích
        setupFavoriteButton();

        // Gán dữ liệu tương ứng theo tour đã chọn
        if (tour != null) {
            bindTourData(view, inflater);
        } else if (!tourType.isEmpty()) {
            if ("taiwan".equals(tourType)) {
                setupImageSlider(view, null, R.drawable.img_taiwan_tour);
                if (tvTourTitle != null) tvTourTitle.setText("Tour Đài Loan 5N4Đ: HCM - Cao Hùng - Đài Trung - Đài Bắc - Đảo Hoà Bình");
                if (tvTourPrice != null) tvTourPrice.setText("14.390.000đ");
                if (tvAirlineBadge != null) tvAirlineBadge.setText("Bamboo Airways");
                if (tvRatingScore != null) tvRatingScore.setText("9,0");
                if (tvRatingStatus != null) tvRatingStatus.setText("Rất tốt");
                if (tvReviewsCount != null) tvReviewsCount.setText("4 đánh giá");
            } else if ("singapore".equals(tourType)) {
                setupImageSlider(view, null, R.drawable.img_singapore_tour);
                if (tvTourTitle != null) tvTourTitle.setText("Tour Singapore - Malaysia 5N4Đ: HCM - Singapore - Kuala Lumpur - Genting");
                if (tvTourPrice != null) tvTourPrice.setText("12.890.000đ");
                if (tvAirlineBadge != null) tvAirlineBadge.setText("Singapore Airlines");
                if (tvRatingScore != null) tvRatingScore.setText("9,1");
                if (tvRatingStatus != null) tvRatingStatus.setText("Xuất sắc");
                if (tvReviewsCount != null) tvReviewsCount.setText("10 đánh giá");
            } else if ("sapa".equals(tourType)) {
                setupImageSlider(view, null, R.drawable.img_sapa_tour);
                if (tvTourTitle != null) tvTourTitle.setText("Tour Sapa 3N2Đ: Hà Nội - Bản Cát Cát - Chinh Phục Đỉnh Fansipan");
                if (tvTourPrice != null) tvTourPrice.setText("3.290.000đ");
                if (tvAirlineBadge != null) tvAirlineBadge.setText("Xe giường nằm cabin VIP");
                if (tvRatingScore != null) tvRatingScore.setText("9,2");
                if (tvRatingStatus != null) tvRatingStatus.setText("Tuyệt vời");
                if (tvReviewsCount != null) tvReviewsCount.setText("15 đánh giá");
            } else if ("halong".equals(tourType)) {
                setupImageSlider(view, null, R.drawable.img_halong_tour);
                if (tvTourTitle != null) tvTourTitle.setText("Tour Vịnh Hạ Long 2N1Đ: Nghỉ Dưỡng Trên Du Thuyền Sang Trọng");
                if (tvTourPrice != null) tvTourPrice.setText("2.590.000đ");
                if (tvAirlineBadge != null) tvAirlineBadge.setText("Du Thuyền 5 Sao cao cấp");
                if (tvRatingScore != null) tvRatingScore.setText("9,4");
                if (tvRatingStatus != null) tvRatingStatus.setText("Xuất sắc");
                if (tvReviewsCount != null) tvReviewsCount.setText("25 đánh giá");
            } else if ("danang".equals(tourType)) {
                setupImageSlider(view, null, R.drawable.img_danang_tour);
                if (tvTourTitle != null) tvTourTitle.setText("Tour Đà Nẵng - Hội An - Bà Nà Hills 4N3Đ Trọn Gói Giá Tốt");
                if (tvTourPrice != null) tvTourPrice.setText("4.890.000đ");
                if (tvAirlineBadge != null) tvAirlineBadge.setText("Vietnam Airlines");
                if (tvRatingScore != null) tvRatingScore.setText("9,3");
                if (tvRatingStatus != null) tvRatingStatus.setText("Xuất sắc");
                if (tvReviewsCount != null) tvReviewsCount.setText("30 đánh giá");
            } else if ("nhatrang".equals(tourType)) {
                setupImageSlider(view, null, R.drawable.img_nhatrang_tour);
                if (tvTourTitle != null) tvTourTitle.setText("Tour Nha Trang 3N2Đ: Khám Phá Vịnh San Hô - VinWonders Trọn Gói");
                if (tvTourPrice != null) tvTourPrice.setText("3.190.000đ");
                if (tvAirlineBadge != null) tvAirlineBadge.setText("VietJet Air");
                if (tvRatingScore != null) tvRatingScore.setText("9,0");
                if (tvRatingStatus != null) tvRatingStatus.setText("Rất tốt");
                if (tvReviewsCount != null) tvReviewsCount.setText("12 đánh giá");
            } else if ("phuquoc".equals(tourType)) {
                setupImageSlider(view, null, R.drawable.img_phuquoc_tour);
                if (tvTourTitle != null) tvTourTitle.setText("Tour Phú Quốc 3N2Đ: Khám Phá Địa Trung Hải - Grand World Trọn Gói");
                if (tvTourPrice != null) tvTourPrice.setText("4.590.000đ");
                if (tvAirlineBadge != null) tvAirlineBadge.setText("Vietnam Airlines");
                if (tvRatingScore != null) tvRatingScore.setText("9,5");
                if (tvRatingStatus != null) tvRatingStatus.setText("Xuất sắc");
                if (tvReviewsCount != null) tvReviewsCount.setText("40 đánh giá");
            } else if ("mientay".equals(tourType)) {
                setupImageSlider(view, null, R.drawable.img_mientay_tour);
                if (tvTourTitle != null) tvTourTitle.setText("Tour Miền Tây Sông Nước 2N1Đ: Mỹ Tho - Cần Thơ - Chợ Nổi Cái Răng");
                if (tvTourPrice != null) tvTourPrice.setText("1.890.000đ");
                if (tvAirlineBadge != null) tvAirlineBadge.setText("Xe du lịch đời mới máy lạnh");
                if (tvRatingScore != null) tvRatingScore.setText("8,8");
                if (tvRatingStatus != null) tvRatingStatus.setText("Rất tốt");
                if (tvReviewsCount != null) tvReviewsCount.setText("18 đánh giá");
            }
        }

        // Tải tour đầy đủ từ database qua API nếu tour hiện tại đang null
        if (tour == null && !tourType.isEmpty()) {
            apiService.getTours().enqueue(new Callback<List<Tour>>() {
                @Override
                public void onResponse(Call<List<Tour>> call, Response<List<Tour>> response) {
                    if (getContext() == null || !isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        for (Tour t : response.body()) {
                            if (tourType.equalsIgnoreCase(getNormalizedTourType(t.getCode(), t.getTitle()))) {
                                tour = t;
                                break;
                            }
                        }
                        if (tour != null) {
                            bindTourData(view, inflater);
                            checkFavoriteStatus();
                        }
                    }
                }
                @Override
                public void onFailure(Call<List<Tour>> call, Throwable t) {}
            });
        } else if (currentUserId != -1 && tour != null) {
            checkFavoriteStatus();
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
        View.OnClickListener openDepartureListener = v -> {
            String titleStr = tvTourTitle != null ? tvTourTitle.getText().toString() : "";
            android.content.Intent intent = new android.content.Intent(requireContext(), DepartureActivity.class);
            intent.putExtra("tour_title", titleStr);
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

    private void registerUserOnServer(String contact) {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        java.util.Map<String, String> userMap = dbHelper.getUserDetails(contact);
        String name = "Người dùng";
        String password = "123";
        if (userMap != null) {
            if (userMap.get("name") != null) name = userMap.get("name");
            if (userMap.get("password") != null) password = userMap.get("password");
        } else {
            password = dbHelper.getPassword(contact);
            if (password.isEmpty()) password = "123";
        }

        com.example.myapplication.data.model.User u = new com.example.myapplication.data.model.User(name, contact, password, "");
        apiService.registerUser(u).enqueue(new Callback<com.example.myapplication.data.model.User>() {
            @Override
            public void onResponse(Call<com.example.myapplication.data.model.User> call, Response<com.example.myapplication.data.model.User> response) {
                if (getContext() == null || !isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    currentUserId = response.body().getId();
                    requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                            .edit().putInt("current_user_id", currentUserId).apply();
                    checkFavoriteStatus();
                }
            }
            @Override
            public void onFailure(Call<com.example.myapplication.data.model.User> call, Throwable t) {}
        });
    }

    private void bindTourData(View view, LayoutInflater inflater) {
        if (tour == null) return;

        TextView tvTourTitle = view.findViewById(R.id.tvTourTitle);
        TextView tvTourPrice = view.findViewById(R.id.tvTourPrice);
        TextView tvAirlineBadge = view.findViewById(R.id.tvAirlineBadge);
        TextView tvRatingScore = view.findViewById(R.id.tvRatingScore);
        TextView tvRatingStatus = view.findViewById(R.id.tvRatingStatus);
        TextView tvReviewsCount = view.findViewById(R.id.tvReviewsCount);

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
        // Xử lý hình ảnh (load bằng Glide vào ViewPager2)
        java.util.List<String> imageUrls = new java.util.ArrayList<>();
        if (tour.getImages() != null) {
            for (com.example.myapplication.data.model.TourImage img : tour.getImages()) {
                imageUrls.add(img.getImageUrl());
            }
        }
        String resolvedType = getNormalizedTourType(tour.getCode(), tour.getTitle());
        int imageResId = R.drawable.img_taiwan_tour;
        if ("singapore".equals(resolvedType)) imageResId = R.drawable.img_singapore_tour;
        else if ("sapa".equals(resolvedType)) imageResId = R.drawable.img_sapa_tour;
        else if ("halong".equals(resolvedType)) imageResId = R.drawable.img_halong_tour;
        else if ("danang".equals(resolvedType)) imageResId = R.drawable.img_danang_tour;
        else if ("nhatrang".equals(resolvedType)) imageResId = R.drawable.img_nhatrang_tour;
        else if ("phuquoc".equals(resolvedType)) imageResId = R.drawable.img_phuquoc_tour;
        else if ("mientay".equals(resolvedType)) imageResId = R.drawable.img_mientay_tour;

        setupImageSlider(view, imageUrls, imageResId);

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
            
            // Sao chép và sắp xếp danh sách lịch trình theo dayNumber tăng dần
            java.util.List<com.example.myapplication.data.model.TourItinerary> itineraryList = 
                    new java.util.ArrayList<>(tour.getItineraries());
            java.util.Collections.sort(itineraryList, (a, b) -> Integer.compare(a.getDayNumber(), b.getDayNumber()));

            for (com.example.myapplication.data.model.TourItinerary itinerary : itineraryList) {
                View itemItinerary = inflater.inflate(R.layout.item_itinerary, itineraryContainer, false);
                
                View layoutHeader = itemItinerary.findViewById(R.id.layoutHeader);
                View layoutDetails = itemItinerary.findViewById(R.id.layoutDetails);
                TextView tvDayNumber = itemItinerary.findViewById(R.id.tvDayNumber);
                TextView tvDayTitle = itemItinerary.findViewById(R.id.tvDayTitle);
                TextView tvDayDescription = itemItinerary.findViewById(R.id.tvDayDescription);
                ImageView ivDayImage = itemItinerary.findViewById(R.id.ivDayImage);
                ImageView ivExpandedDayImage = itemItinerary.findViewById(R.id.ivExpandedDayImage);
                ImageView ivChevron = itemItinerary.findViewById(R.id.ivChevron);

                if (tvDayNumber != null) {
                    tvDayNumber.setText("Ngày " + itinerary.getDayNumber());
                }
                if (tvDayTitle != null) {
                    tvDayTitle.setText(itinerary.getTitle());
                }
                if (tvDayDescription != null) {
                    tvDayDescription.setText(itinerary.getDescription());
                }
                
                // Lấy ảnh tương ứng từ danh sách ảnh của Tour
                if (tour.getImages() != null && !tour.getImages().isEmpty()) {
                    int size = tour.getImages().size();
                    int dayNum = itinerary.getDayNumber();
                    int imgIndex = (dayNum - 1) % size;
                    if (imgIndex < 0) {
                        imgIndex = 0;
                    }
                    String dayImageUrl = tour.getImages().get(imgIndex).getImageUrl();
                    if (dayImageUrl != null && !dayImageUrl.isEmpty()) {
                        if (dayImageUrl.startsWith("/")) {
                            dayImageUrl = "http://10.0.2.2:8000" + dayImageUrl;
                        }
                        if (ivDayImage != null) {
                            com.bumptech.glide.Glide.with(this)
                                    .load(dayImageUrl)
                                    .placeholder(R.drawable.img_taiwan_tour)
                                    .centerCrop()
                                    .into(ivDayImage);
                        }
                        if (ivExpandedDayImage != null) {
                            com.bumptech.glide.Glide.with(this)
                                    .load(dayImageUrl)
                                    .placeholder(R.drawable.img_taiwan_tour)
                                    .centerCrop()
                                    .into(ivExpandedDayImage);
                        }
                    }
                }

                // Sự kiện click mở rộng / thu gọn
                if (layoutHeader != null && layoutDetails != null) {
                    layoutHeader.setOnClickListener(v -> {
                        if (layoutDetails.getVisibility() == View.GONE) {
                            layoutDetails.setVisibility(View.VISIBLE);
                            if (ivChevron != null) {
                                ivChevron.animate().rotation(180f).setDuration(200).start();
                            }
                        } else {
                            layoutDetails.setVisibility(View.GONE);
                            if (ivChevron != null) {
                                ivChevron.animate().rotation(0f).setDuration(200).start();
                            }
                        }
                    });
                }

                itineraryContainer.addView(itemItinerary);
            }
        }
    }

    private void setupFavoriteButton() {
        if (btnFavorite == null) return;

        // Initialize state (grey heart outline)
        updateFavoriteUI(false);

        btnFavorite.setOnClickListener(v -> {
            if (currentUserId == -1) {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (tour == null) {
                Toast.makeText(requireContext(), "Đang tải dữ liệu tour, vui lòng thử lại sau!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isFavorite) {
                // Remove favorite
                apiService.removeFavorite(favoriteId).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (getContext() == null || !isAdded()) return;
                        if (response.isSuccessful()) {
                            isFavorite = false;
                            favoriteId = -1;
                            updateFavoriteUI(false);
                            Toast.makeText(requireContext(), "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Lỗi khi xóa yêu thích!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (getContext() == null || !isAdded()) return;
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Add favorite
                Favorite fav = new Favorite(currentUserId, tour.getId());
                apiService.addFavorite(fav).enqueue(new Callback<Favorite>() {
                    @Override
                    public void onResponse(Call<Favorite> call, Response<Favorite> response) {
                        if (getContext() == null || !isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            isFavorite = true;
                            favoriteId = response.body().getId();
                            updateFavoriteUI(true);
                            Toast.makeText(requireContext(), "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Lỗi khi thêm yêu thích!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Favorite> call, Throwable t) {
                        if (getContext() == null || !isAdded()) return;
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void checkFavoriteStatus() {
        if (currentUserId == -1 || tour == null) return;

        apiService.getFavorites().enqueue(new Callback<List<Favorite>>() {
            @Override
            public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                if (getContext() == null || !isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    for (Favorite f : response.body()) {
                        if (f.getUserId() == currentUserId && f.getTourId() == tour.getId()) {
                            isFavorite = true;
                            favoriteId = f.getId();
                            updateFavoriteUI(true);
                            return;
                        }
                    }
                }
                isFavorite = false;
                favoriteId = -1;
                updateFavoriteUI(false);
            }

            @Override
            public void onFailure(Call<List<Favorite>> call, Throwable t) {}
        });
    }

    private void updateFavoriteUI(boolean liked) {
        if (btnFavorite == null) return;
        if (liked) {
            btnFavorite.setImageResource(R.drawable.ic_heart);
            btnFavorite.setColorFilter(android.graphics.Color.parseColor("#E53935"));
        } else {
            btnFavorite.setImageResource(R.drawable.ic_heart_outline);
            btnFavorite.setColorFilter(android.graphics.Color.parseColor("#757575"));
        }
    }

    public static String getNormalizedTourType(String code, String title) {
        if (code == null) return "taiwan";
        String c = code.toLowerCase().trim();
        if (c.contains("taiwan")) return "taiwan";
        if (c.contains("singapore")) return "singapore";
        if (c.contains("sapa")) return "sapa";
        if (c.contains("halong")) return "halong";
        if (c.contains("danang")) return "danang";
        if (c.contains("nhatrang")) return "nhatrang";
        if (c.contains("phuquoc")) return "phuquoc";
        if (c.contains("mientay")) return "mientay";

        if (title != null) {
            String t = title.toLowerCase();
            if (t.contains("đài loan") || t.contains("taiwan")) return "taiwan";
            if (t.contains("singapore")) return "singapore";
            if (t.contains("sapa")) return "sapa";
            if (t.contains("hạ long") || t.contains("ha long")) return "halong";
            if (t.contains("đà nẵng") || t.contains("da nang")) return "danang";
            if (t.contains("nha trang")) return "nhatrang";
            if (t.contains("phú quốc") || t.contains("phu quoc")) return "phuquoc";
            if (t.contains("miền tây") || t.contains("mien tay")) return "mientay";
        }
        return c;
    }

    private void setupImageSlider(View view, java.util.List<String> imageUrls, int fallbackResId) {
        androidx.viewpager2.widget.ViewPager2 vpImages = view.findViewById(R.id.vpImages);
        TextView tvPhotoCount = view.findViewById(R.id.tvPhotoCount);
        if (vpImages == null) return;

        // Bắt buộc luôn hiển thị đúng 5 ảnh bằng cách lặp lại nếu danh sách ít
        java.util.List<String> sliderImages = new java.util.ArrayList<>();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                sliderImages.add(imageUrls.get(i % imageUrls.size()));
            }
        } else {
            for (int i = 0; i < 5; i++) {
                sliderImages.add("");
            }
        }

        ImageSliderAdapter adapter = new ImageSliderAdapter(sliderImages, fallbackResId);
        vpImages.setAdapter(adapter);

        if (tvPhotoCount != null) {
            tvPhotoCount.setText("1/5");
        }

        vpImages.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (tvPhotoCount != null) {
                    tvPhotoCount.setText((position + 1) + "/5");
                }
            }
        });
    }

    private class ImageSliderAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {
        private final java.util.List<String> imageUrls;
        private final int fallbackResId;

        public ImageSliderAdapter(java.util.List<String> imageUrls, int fallbackResId) {
            this.imageUrls = imageUrls;
            this.fallbackResId = fallbackResId;
        }

        @NonNull
        @Override
        public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new SliderViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
            String url = imageUrls.get(position);
            ImageView imageView = (ImageView) holder.itemView;
            if (url != null && !url.isEmpty()) {
                if (url.startsWith("/")) {
                    url = "http://10.0.2.2:8000" + url;
                }
                com.bumptech.glide.Glide.with(DetailTour.this)
                        .load(url)
                        .placeholder(fallbackResId)
                        .centerCrop()
                        .into(imageView);
            } else {
                imageView.setImageResource(fallbackResId);
            }
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        class SliderViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            public SliderViewHolder(@NonNull View itemView) {
                super(itemView);
            }
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
