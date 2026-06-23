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
import java.util.ArrayList;

import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.model.Favorite;
import com.example.myapplication.data.model.User;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("current_user_id", -1);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        if (currentUserId != -1) {
            fetchFavoritesForUser(inflater, apiService, currentUserId);
        } else {
            // Tìm ID người dùng từ contact trên server
            apiService.getUsers().enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                    if (getContext() == null || !isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        int matchedUserId = -1;
                        for (User u : response.body()) {
                            if (contact.equals(u.getContact())) {
                                matchedUserId = u.getId();
                                break;
                            }
                        }
                        if (matchedUserId != -1) {
                            prefs.edit().putInt("current_user_id", matchedUserId).apply();
                            fetchFavoritesForUser(inflater, apiService, matchedUserId);
                        } else {
                            layoutEmptyState.setVisibility(View.VISIBLE);
                            scrollViewFavoriteList.setVisibility(View.GONE);
                        }
                    } else {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        scrollViewFavoriteList.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<List<User>> call, Throwable t) {
                    if (getContext() == null || !isAdded()) return;
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    scrollViewFavoriteList.setVisibility(View.GONE);
                }
            });
        }
    }

    private void fetchFavoritesForUser(LayoutInflater inflater, ApiService apiService, int userId) {
        apiService.getFavorites().enqueue(new Callback<List<Favorite>>() {
            @Override
            public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                if (getContext() == null || !isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<Favorite> allFavorites = response.body();
                    List<Integer> userFavTourIds = new ArrayList<>();
                    for (Favorite f : allFavorites) {
                        if (f.getUserId() == userId) {
                            userFavTourIds.add(f.getTourId());
                        }
                    }

                    if (userFavTourIds.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        scrollViewFavoriteList.setVisibility(View.GONE);
                    } else {
                        // Tải danh sách Tour để khớp tourId thành tour_type (code)
                        apiService.getTours().enqueue(new Callback<List<Tour>>() {
                            @Override
                            public void onResponse(Call<List<Tour>> call, Response<List<Tour>> response) {
                                if (getContext() == null || !isAdded()) return;
                                if (response.isSuccessful() && response.body() != null) {
                                    List<Tour> allTours = response.body();
                                    List<Tour> favoriteTours = new ArrayList<>();
                                    for (Tour tour : allTours) {
                                        if (userFavTourIds.contains(tour.getId())) {
                                            favoriteTours.add(tour);
                                        }
                                    }
                                    renderFavoritesList(inflater, favoriteTours);
                                } else {
                                    layoutEmptyState.setVisibility(View.VISIBLE);
                                    scrollViewFavoriteList.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Tour>> call, Throwable t) {
                                if (getContext() == null || !isAdded()) return;
                                layoutEmptyState.setVisibility(View.VISIBLE);
                                scrollViewFavoriteList.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    scrollViewFavoriteList.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Favorite>> call, Throwable t) {
                if (getContext() == null || !isAdded()) return;
                layoutEmptyState.setVisibility(View.VISIBLE);
                scrollViewFavoriteList.setVisibility(View.GONE);
            }
        });
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

    private void renderFavoritesList(LayoutInflater inflater, List<Tour> favoriteTours) {
        if (favoriteTours.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            scrollViewFavoriteList.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            scrollViewFavoriteList.setVisibility(View.VISIBLE);
            favoriteContainer.removeAllViews();

            java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN"));

            for (Tour tour : favoriteTours) {
                // Nạp tệp giao diện item_tour_card
                View itemView = inflater.inflate(R.layout.item_tour_card, favoriteContainer, false);

                // Ánh xạ các views trong card
                ImageView ivTourImage = itemView.findViewById(R.id.ivTourImage);
                TextView tvRibbonBadge = itemView.findViewById(R.id.tvRibbonBadge);
                TextView tvTourTitle = itemView.findViewById(R.id.tvTourTitle);
                TextView tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
                TextView tvNewPrice = itemView.findViewById(R.id.tvNewPrice);
                TextView btnViewTour = itemView.findViewById(R.id.btnViewTour);

                String tourType = getNormalizedTourType(tour.getCode(), tour.getTitle());

                // Gán dữ liệu tương ứng theo tourType
                int imageResId = R.drawable.img_taiwan_tour;
                if ("singapore".equals(tourType)) {
                    imageResId = R.drawable.img_singapore_tour;
                } else if ("sapa".equals(tourType)) {
                    imageResId = R.drawable.img_sapa_tour;
                } else if ("halong".equals(tourType)) {
                    imageResId = R.drawable.img_halong_tour;
                } else if ("danang".equals(tourType)) {
                    imageResId = R.drawable.img_danang_tour;
                } else if ("nhatrang".equals(tourType)) {
                    imageResId = R.drawable.img_nhatrang_tour;
                } else if ("phuquoc".equals(tourType)) {
                    imageResId = R.drawable.img_phuquoc_tour;
                } else if ("mientay".equals(tourType)) {
                    imageResId = R.drawable.img_mientay_tour;
                }

                // Gán dữ liệu vào views
                if (ivTourImage != null) {
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
                                .placeholder(imageResId)
                                .centerCrop()
                                .into(ivTourImage);
                    } else {
                        ivTourImage.setImageResource(imageResId);
                    }
                }
                if (tvRibbonBadge != null) tvRibbonBadge.setText(tour.getProvider());
                if (tvTourTitle != null) tvTourTitle.setText(tour.getTitle());
                if (tvNewPrice != null) tvNewPrice.setText(formatter.format(tour.getDiscountPrice()) + "đ");
                
                if (tvOldPrice != null) {
                    tvOldPrice.setText(formatter.format(tour.getOriginalPrice()));
                    // Áp dụng gạch ngang giá cũ
                    tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                }

                // Sự kiện click mở chi tiết tour
                View.OnClickListener openDetailListener = v -> {
                    DetailTour detailFragment = new DetailTour();
                    Bundle args = new Bundle();
                    args.putSerializable("tour_object", tour);
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
