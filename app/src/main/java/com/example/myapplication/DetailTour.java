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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.model.Favorite;
import com.example.myapplication.data.model.Review;

import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailTour extends Fragment {

    private Tour tour;
    private String tourType = "";
    // SỬA: id tour nhận từ Bundle (thay cho việc nhận cả object Tour qua Serializable)
    private int tourIdArg = -1;
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
            // SỬA: chỉ đọc tour_id (int) từ Bundle. Không còn getSerializable("tour_object").
            tourIdArg = getArguments().getInt("tour_id", -1);
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

        // SỬA: luôn nạp dữ liệu demo theo tourType trước (nếu có) để UI không trống trong lúc
        // chờ API, sau đó tải tour thật (đầy đủ departures/itineraries/images) theo tourIdArg
        // hoặc tourType, rồi bindTourData() sẽ ghi đè lại với dữ liệu thật.
        if (!tourType.isEmpty()) {
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

        // SỬA: luôn fetch tour mới nhất từ API Django (không dùng object truyền qua Serializable).
        // Ưu tiên tìm theo tourIdArg; nếu không có id (mở từ tourType demo), tìm theo tourType.
        loadTourFromApi(view, inflater);

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
            // Truyền tour_id và giá người lớn cho DepartureActivity
            if (tour != null) {
                intent.putExtra("tour_id", tour.getId());
                intent.putExtra("adult_price", (long) tour.getDiscountPrice());
            }
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

    /**
     * SỬA (hàm mới): luôn gọi API Django để lấy Tour mới nhất, đầy đủ departures/itineraries/images.
     * - Nếu có tourIdArg hợp lệ: tìm tour theo id.
     * - Nếu không (trường hợp mở bằng tourType demo, không có id): tìm theo tourType.
     */
    private void loadTourFromApi(View view, LayoutInflater inflater) {
        apiService.getTours().enqueue(new Callback<List<Tour>>() {
            @Override
            public void onResponse(Call<List<Tour>> call, Response<List<Tour>> response) {
                if (getContext() == null || !isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Tour found = null;
                    if (tourIdArg > 0) {
                        for (Tour t : response.body()) {
                            if (t.getId() == tourIdArg) {
                                found = t;
                                break;
                            }
                        }
                    } else if (!tourType.isEmpty()) {
                        for (Tour t : response.body()) {
                            if (tourType.equalsIgnoreCase(getNormalizedTourType(t.getCode(), t.getTitle()))) {
                                found = t;
                                break;
                            }
                        }
                    }

                    if (found != null) {
                        tour = found;
                        bindTourData(view, inflater);
                        checkFavoriteStatus();
                        loadRelatedTours(view, tour);
                        logUserBehavior(currentUserId, tour.getId(), "VIEW");
                    } else if (tourIdArg > 0) {
                        Toast.makeText(requireContext(), "Không tìm thấy thông tin tour!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Không thể tải dữ liệu tour, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Tour>> call, Throwable t) {
                if (getContext() == null || !isAdded()) return;
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

        // Render đánh giá động từ CSDL
        android.widget.LinearLayout reviewsContainer = view.findViewById(R.id.reviewsContainer);
        android.widget.TextView tvEmptyReviews = view.findViewById(R.id.tvEmptyReviews);

        if (reviewsContainer != null) {
            reviewsContainer.removeAllViews();
            java.util.List<Review> reviewsList = tour.getReviews();
            if (reviewsList != null && !reviewsList.isEmpty()) {
                if (tvEmptyReviews != null) tvEmptyReviews.setVisibility(View.GONE);
                
                // Hiển thị tối đa 3 đánh giá gần đây ở màn hình chi tiết tour
                int limit = Math.min(3, reviewsList.size());
                for (int i = 0; i < limit; i++) {
                    Review r = reviewsList.get(i);
                    View itemReview = inflater.inflate(R.layout.item_review, reviewsContainer, false);

                    TextView tvName = itemReview.findViewById(R.id.tvReviewerName);
                    TextView tvScore = itemReview.findViewById(R.id.tvReviewScore);
                    TextView tvDate = itemReview.findViewById(R.id.tvReviewDate);
                    TextView tvComment = itemReview.findViewById(R.id.tvReviewComment);
                    TextView tvSentiment = itemReview.findViewById(R.id.tvSentiment);

                    if (tvName != null) tvName.setText(r.getUserName());
                    if (tvScore != null)
                        tvScore.setText(String.format(java.util.Locale.US, "%.1f", (double) r.getRating()));
                    if (tvDate != null && r.getCreatedAt() != null) {
                        String dateStr = r.getCreatedAt();
                        if (dateStr.length() >= 10) {
                            dateStr = dateStr.substring(0, 10);
                        }
                        tvDate.setText(dateStr);
                    }
                    if (tvComment != null) tvComment.setText(r.getComment());

                    if (tvSentiment != null) {
                        String commentText = r.getComment() != null ? r.getComment() : "";
                        String sentiment = analyzeSentiment(commentText, r.getRating());
                        tvSentiment.setText(sentiment);
                        if ("Tích cực".equals(sentiment)) {
                            tvSentiment.setBackgroundResource(R.drawable.bg_score_green);
                        } else if ("Tiêu cực".equals(sentiment)) {
                            tvSentiment.setBackgroundResource(R.drawable.bg_badge_red);
                        } else {
                            tvSentiment.setBackgroundResource(R.drawable.bg_badge_dark);
                        }
                    }

                    reviewsContainer.addView(itemReview);
                }
            } else {
                if (tvEmptyReviews != null) tvEmptyReviews.setVisibility(View.VISIBLE);
            }
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

        // Gán lịch khởi hành động từ database/API
        android.widget.LinearLayout departureListContainer = view.findViewById(R.id.departureListContainer);
        if (departureListContainer != null) {
            departureListContainer.removeAllViews();

            if (tour.getDepartures() != null && !tour.getDepartures().isEmpty()) {
                // Sắp xếp lịch khởi hành theo ngày
                java.util.List<com.example.myapplication.data.model.TourDeparture> departureList =
                        new java.util.ArrayList<>(tour.getDepartures());
                java.text.SimpleDateFormat sdfSource = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.text.SimpleDateFormat sdfDest = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

                java.util.Collections.sort(departureList, (a, b) -> {
                    try {
                        java.util.Date dateA = sdfSource.parse(a.getDepartureDate());
                        java.util.Date dateB = sdfSource.parse(b.getDepartureDate());
                        if (dateA != null && dateB != null) return dateA.compareTo(dateB);
                    } catch (Exception e) {}
                    return 0;
                });

                java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN"));

                for (com.example.myapplication.data.model.TourDeparture departure : departureList) {
                    View itemDeparture = inflater.inflate(R.layout.item_departure_row, departureListContainer, false);

                    TextView tvDate = itemDeparture.findViewById(R.id.tvDate);
                    TextView tvStatus = itemDeparture.findViewById(R.id.tvStatus);
                    TextView tvPrice = itemDeparture.findViewById(R.id.tvPrice);

                    if (tvDate != null) {
                        try {
                            java.util.Date d = sdfSource.parse(departure.getDepartureDate());
                            if (d != null) {
                                tvDate.setText(sdfDest.format(d));
                            } else {
                                tvDate.setText(departure.getDepartureDate());
                            }
                        } catch (Exception e) {
                            tvDate.setText(departure.getDepartureDate());
                        }
                    }

                    if (tvStatus != null) {
                        if (departure.getAvailableSeats() > 0) {
                            tvStatus.setText("Còn chỗ");
                            tvStatus.setTextColor(android.graphics.Color.parseColor("#388E3C"));
                        } else {
                            tvStatus.setText("Hết chỗ");
                            tvStatus.setTextColor(android.graphics.Color.parseColor("#C62828"));
                        }
                    }

                    if (tvPrice != null) {
                        tvPrice.setText(formatter.format(departure.getPrice()) + "đ");
                    }

                    departureListContainer.addView(itemDeparture);
                }
            } else {
                // SỬA: hiển thị thông báo rõ ràng khi tour chưa có lịch khởi hành,
                // thay vì để bảng trống không rõ nguyên nhân như trước.
                TextView tvEmpty = new TextView(requireContext());
                tvEmpty.setText("Chưa có lịch khởi hành cho tour này");
                tvEmpty.setTextColor(android.graphics.Color.parseColor("#999999"));
                tvEmpty.setTextSize(14);
                tvEmpty.setPadding(dp(12), dp(12), dp(12), dp(12));
                departureListContainer.addView(tvEmpty);
            }
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private void setupFavoriteButton() {
        if (btnFavorite == null) return;

        // Initialize state (grey heart outline)
        updateFavoriteUI(false);

        btnFavorite.setOnClickListener(v -> {
            if (currentUserId == -1) {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
                android.content.Intent intent = new android.content.Intent(requireContext(), LoginActivity.class);
                intent.putExtra("return_to_caller", true);
                startActivity(intent);
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

    private void logUserBehavior(int userId, int tourId, String behaviorType) {
        if (tourId == -1) return;
        java.util.HashMap<String, Object> body = new java.util.HashMap<>();
        body.put("user", userId);
        body.put("tour", tourId);
        body.put("behavior_type", behaviorType);

        apiService.logBehavior(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    android.util.Log.d("BEHAVIOR", "Logged " + behaviorType + " successfully for tour " + tourId);
                } else {
                    android.util.Log.e("BEHAVIOR", "Failed to log behavior. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                android.util.Log.e("BEHAVIOR", "Error logging behavior: " + t.getMessage());
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
            if (t.contains("quy nhơn") || t.contains("quy nhon")) return "quynhon";
            if (t.contains("đà lạt") || t.contains("da lat")) return "dalat";
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

    private String getTourRegion(String title) {
        if (title == null) return "";
        String lowerTitle = title.toLowerCase();

        if (lowerTitle.contains("bắc") || lowerTitle.contains("bac") ||
                lowerTitle.contains("sapa") || lowerTitle.contains("hà nội") || lowerTitle.contains("hạ long") ||
                lowerTitle.contains("fansipan") || lowerTitle.contains("cát cát") || lowerTitle.contains("lào cai")) {
            return "Miền Bắc";
        } else if (lowerTitle.contains("trung") || lowerTitle.contains("đà nẵng") ||
                lowerTitle.contains("nha trang") || lowerTitle.contains("hội an") || lowerTitle.contains("huế") ||
                lowerTitle.contains("quảng bình") || lowerTitle.contains("phong nha") || lowerTitle.contains("quy nhơn") ||
                lowerTitle.contains("đà lạt")) {
            return "Miền Trung";
        } else if (lowerTitle.contains("nam") || lowerTitle.contains("miền tây") ||
                lowerTitle.contains("phú quốc") || lowerTitle.contains("cần thơ") || lowerTitle.contains("hcm") ||
                lowerTitle.contains("sài gòn") || lowerTitle.contains("vũng tàu") || lowerTitle.contains("mỹ tho") ||
                lowerTitle.contains("chợ nổi")) {
            return "Miền Nam";
        } else if (lowerTitle.contains("đài loan") || lowerTitle.contains("taiwan") ||
                lowerTitle.contains("singapore") || lowerTitle.contains("malaysia")) {
            return "Nước Ngoài";
        }
        return "";
    }

    private String getSpecificDestination(String title) {
        if (title == null) return "";
        String lowerTitle = title.toLowerCase();

        if (lowerTitle.contains("hà nội") || lowerTitle.contains("ha noi") || lowerTitle.contains("hn")) {
            return "hanoi";
        }
        if (lowerTitle.contains("sapa") || lowerTitle.contains("sa pa")) {
            return "sapa";
        }
        if (lowerTitle.contains("hạ long") || lowerTitle.contains("ha long")) {
            return "halong";
        }
        if (lowerTitle.contains("ninh bình") || lowerTitle.contains("ninh binh")) {
            return "ninhbinh";
        }
        if (lowerTitle.contains("đà nẵng") || lowerTitle.contains("da nang")) {
            return "danang";
        }
        if (lowerTitle.contains("hội an") || lowerTitle.contains("hoi an")) {
            return "hoian";
        }
        if (lowerTitle.contains("huế") || lowerTitle.contains("hue")) {
            return "hue";
        }
        if (lowerTitle.contains("nha trang")) {
            return "nhatrang";
        }
        if (lowerTitle.contains("quy nhơn") || lowerTitle.contains("quy nhon")) {
            return "quynhon";
        }
        if (lowerTitle.contains("đà lạt") || lowerTitle.contains("da lat")) {
            return "dalat";
        }
        if (lowerTitle.contains("phú quốc") || lowerTitle.contains("phu quoc")) {
            return "phuquoc";
        }
        if (lowerTitle.contains("cần thơ") || lowerTitle.contains("can tho") ||
                lowerTitle.contains("miền tây") || lowerTitle.contains("mien tay")) {
            return "mientay";
        }
        if (lowerTitle.contains("hồ chí minh") || lowerTitle.contains("ho chi minh") ||
                lowerTitle.contains("sài gòn") || lowerTitle.contains("sai gon") || lowerTitle.contains("hcm")) {
            return "hcm";
        }
        if (lowerTitle.contains("đài loan") || lowerTitle.contains("taiwan")) {
            return "taiwan";
        }
        if (lowerTitle.contains("singapore")) {
            return "singapore";
        }
        if (lowerTitle.contains("malaysia")) {
            return "malaysia";
        }
        return "";
    }

    private String parseDuration(String title, int itinerariesCount) {
        if (title != null) {
            String upper = title.toUpperCase();
            if (upper.contains("5N4Đ") || upper.contains("5N4D")) return "5 ngày 4 đêm";
            if (upper.contains("3N2Đ") || upper.contains("3N2D")) return "3 ngày 2 đêm";
            if (upper.contains("2N1Đ") || upper.contains("2N1D")) return "2 ngày 1 đêm";
            if (upper.contains("4N3Đ") || upper.contains("4N3D")) return "4 ngày 3 đêm";
            if (upper.contains("6N5Đ") || upper.contains("6N5D")) return "6 ngày 5 đêm";
        }
        if (itinerariesCount > 0) {
            return itinerariesCount + " ngày " + (itinerariesCount - 1) + " đêm";
        }
        return "3 ngày 2 đêm";
    }

    private void loadRelatedTours(View view, Tour currentTour) {
        if (currentTour == null || view == null) return;

        View sectionRelatedTours = view.findViewById(R.id.sectionRelatedTours);
        View spacerRelatedTours = view.findViewById(R.id.spacerRelatedTours);
        androidx.recyclerview.widget.RecyclerView rvRelatedTours = view.findViewById(R.id.rvRelatedTours);

        String currentRegion = getTourRegion(currentTour.getTitle());
        String currentDest = getSpecificDestination(currentTour.getTitle());

        apiService.getTours().enqueue(new Callback<java.util.List<Tour>>() {
            @Override
            public void onResponse(Call<java.util.List<Tour>> call, Response<java.util.List<Tour>> response) {
                if (getContext() == null || !isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    java.util.List<Tour> allTours = response.body();
                    java.util.List<TourScore> candidates = new java.util.ArrayList<>();

                    for (Tour t : allTours) {
                        // Exclude the current tour
                        if (t.getId() == currentTour.getId()) {
                            continue;
                        }

                        int score = 0;
                        String tRegion = getTourRegion(t.getTitle());
                        String tDest = getSpecificDestination(t.getTitle());

                        // Match destination city
                        if (!currentDest.isEmpty() && currentDest.equals(tDest)) {
                            score += 20;
                        }
                        // Match region
                        if (!currentRegion.isEmpty() && currentRegion.equals(tRegion)) {
                            score += 10;
                        }
                        // Add rating score as tie-breaker
                        score += (int) (t.getRatingScore() * 1.0);

                        // Only consider positive score (must match region or city)
                        if (score > t.getRatingScore()) {
                            candidates.add(new TourScore(t, score));
                        }
                    }

                    // Sort candidates descending by score
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        candidates.sort((c1, c2) -> Integer.compare(c2.score, c1.score));
                    } else {
                        java.util.Collections.sort(candidates, (c1, c2) -> Integer.compare(c2.score, c1.score));
                    }

                    // Limit to top 5 related tours
                    java.util.List<Tour> displayList = new java.util.ArrayList<>();
                    for (int i = 0; i < Math.min(5, candidates.size()); i++) {
                        displayList.add(candidates.get(i).tour);
                    }

                    if (displayList.isEmpty()) {
                        if (sectionRelatedTours != null) sectionRelatedTours.setVisibility(View.GONE);
                        if (spacerRelatedTours != null) spacerRelatedTours.setVisibility(View.GONE);
                    } else {
                        if (sectionRelatedTours != null) sectionRelatedTours.setVisibility(View.VISIBLE);
                        if (spacerRelatedTours != null) spacerRelatedTours.setVisibility(View.VISIBLE);

                        if (rvRelatedTours != null) {
                            rvRelatedTours.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(
                                    getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
                            RelatedTourAdapter adapter = new RelatedTourAdapter(displayList);
                            rvRelatedTours.setAdapter(adapter);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<java.util.List<Tour>> call, Throwable t) {
                if (getContext() == null || !isAdded()) return;
                if (sectionRelatedTours != null) sectionRelatedTours.setVisibility(View.GONE);
                if (spacerRelatedTours != null) spacerRelatedTours.setVisibility(View.GONE);
            }
        });
    }

    private class RelatedTourAdapter extends RecyclerView.Adapter<RelatedTourAdapter.ViewHolder> {
        private final java.util.List<Tour> list;

        RelatedTourAdapter(java.util.List<Tour> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_related_tour, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Tour tour = list.get(position);
            holder.tvTitle.setText(tour.getTitle());

            java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN"));
            holder.tvPrice.setText(formatter.format(tour.getDiscountPrice()) + "đ");

            int itinerariesCount = tour.getItineraries() != null ? tour.getItineraries().size() : 0;
            holder.tvDuration.setText(parseDuration(tour.getTitle(), itinerariesCount));
            holder.tvRating.setText(String.format(java.util.Locale.US, "%.1f", tour.getRatingScore()));

            String imageUrl = null;
            if (tour.getImages() != null && !tour.getImages().isEmpty()) {
                imageUrl = tour.getImages().get(0).getImageUrl();
            }
            if (imageUrl == null || imageUrl.isEmpty()) {
                String resolvedType = getNormalizedTourType(tour.getCode(), tour.getTitle());
                int imageResId = R.drawable.img_taiwan_tour;
                if ("singapore".equals(resolvedType)) imageResId = R.drawable.img_singapore_tour;
                else if ("sapa".equals(resolvedType)) imageResId = R.drawable.img_sapa_tour;
                else if ("halong".equals(resolvedType)) imageResId = R.drawable.img_halong_tour;
                else if ("danang".equals(resolvedType)) imageResId = R.drawable.img_danang_tour;
                else if ("nhatrang".equals(resolvedType)) imageResId = R.drawable.img_nhatrang_tour;
                else if ("phuquoc".equals(resolvedType)) imageResId = R.drawable.img_phuquoc_tour;
                else if ("mientay".equals(resolvedType)) imageResId = R.drawable.img_mientay_tour;
                holder.ivImage.setImageResource(imageResId);
            } else {
                if (imageUrl.startsWith("/")) {
                    imageUrl = "http://10.0.2.2:8000" + imageUrl;
                }
                com.bumptech.glide.Glide.with(DetailTour.this)
                        .load(imageUrl)
                        .placeholder(R.drawable.img_taiwan_tour)
                        .centerCrop()
                        .into(holder.ivImage);
            }

            holder.itemView.setOnClickListener(v -> {
                // SỬA: mở DetailTour bằng tour_id thay vì putSerializable cả object Tour,
                // để màn chi tiết của tour liên quan cũng luôn tải dữ liệu mới nhất từ API.
                DetailTour detailFragment = new DetailTour();
                Bundle args = new Bundle();
                args.putInt("tour_id", tour.getId());
                detailFragment.setArguments(args);
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.contentFrame, detailFragment)
                            .addToBackStack(null).commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivImage;
            final TextView tvTitle, tvDuration, tvRating, tvPrice;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.ivRelatedImage);
                tvTitle = itemView.findViewById(R.id.tvRelatedTitle);
                tvDuration = itemView.findViewById(R.id.tvRelatedDuration);
                tvRating = itemView.findViewById(R.id.tvRelatedRating);
                tvPrice = itemView.findViewById(R.id.tvRelatedPrice);
            }
        }
    }

    private static class TourScore {
        final Tour tour;
        final int score;

        TourScore(Tour tour, int score) {
            this.tour = tour;
            this.score = score;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            currentUserId = prefs.getInt("current_user_id", -1);
            checkFavoriteStatus();
        }
    }

    private String analyzeSentiment(String text, int rating) {
        if (text == null || text.trim().isEmpty()) {
            if (rating >= 4) return "Tích cực";
            if (rating <= 2) return "Tiêu cực";
            return "Trung lập";
        }
        String lowerText = text.toLowerCase();

        String[] positiveWords = {"tuyệt", "tốt", "hay", "đẹp", "xuất sắc", "thích", "ngon", "ok", "hài lòng", "đỉnh", "10 điểm", "chất lượng", "vui", "tuyet", "tot", "dep", "xuat sac", "thich", "hai long", "dinh", "chat luong"};
        String[] negativeWords = {"tệ", "chán", "kém", "buồn", "thất vọng", "xấu", "dở", "đắt", "không tốt", "không hay", "lừa đảo", "te", "chan", "kem", "buon", "that vong", "xau", "do", "dat", "khong tot", "khong hay", "lua dao", "đắt đỏ", "dịch vụ kém"};

        int posScore = 0;
        int negScore = 0;

        for (String word : positiveWords) {
            if (lowerText.contains(word)) posScore++;
        }
        for (String word : negativeWords) {
            if (lowerText.contains(word)) negScore++;
        }

        if (posScore > negScore) {
            return "Tích cực";
        } else if (negScore > posScore) {
            return "Tiêu cực";
        } else {
            if (rating >= 4) return "Tích cực";
            if (rating <= 2) return "Tiêu cực";
            return "Trung lập";
        }
    }

}