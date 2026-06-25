package com.example.myapplication;

import android.graphics.Paint;
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

import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment hiển thị kết quả tìm kiếm các tour du lịch.
 * Hỗ trợ bộ lọc động, sắp xếp theo giá thời gian thực và chuyển tiếp đến chi tiết tour.
 */
public class SearchResult extends Fragment {

    private String destination = "";
    private String origin = "";
    private int day = 23;
    private int month = java.util.Calendar.MAY;
    private int year = 2026;

    private List<Tour> filteredList = new ArrayList<>();

    private LinearLayout containerTours;
    private View layoutNoResults;
    private TextView tvSearchTitle;
    private TextView tvSearchSubtitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Nhận dữ liệu truyền từ Trang chủ
        if (getArguments() != null) {
            destination = getArguments().getString("destination", "");
            origin = getArguments().getString("origin", "");
            day = getArguments().getInt("day", 23);
            month = getArguments().getInt("month", java.util.Calendar.MAY);
            year = getArguments().getInt("year", 2026);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_result, container, false);

        // Khởi tạo views
        ImageView btnBack = view.findViewById(R.id.btnBack);
        tvSearchTitle = view.findViewById(R.id.tvSearchTitle);
        tvSearchSubtitle = view.findViewById(R.id.tvSearchSubtitle);
        containerTours = view.findViewById(R.id.containerTours);
        layoutNoResults = view.findViewById(R.id.layoutNoResults);
        View btnSort = view.findViewById(R.id.btnSort);
        View btnFilter = view.findViewById(R.id.btnFilter);

        // Thiết lập tiêu đề và mô tả header phụ
        if (tvSearchTitle != null) {
            tvSearchTitle.setText(destination.isEmpty() || destination.equals("Bạn muốn đi đâu?") ? "Tất cả điểm đến" : destination);
        }
        if (tvSearchSubtitle != null) {
            tvSearchSubtitle.setText(formatSubtitle(day, month, year, origin));
        }

        // Click nút Quay lại
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        // Thiết lập sự kiện click sắp xếp & bộ lọc
        if (btnSort != null) {
            btnSort.setOnClickListener(v -> showSortDialog());
        }
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
        }

        // Lấy danh sách tour động từ Django API
        fetchFilteredTours(destination, origin);

        return view;
    }

    /**
     * Định dạng phụ đề: Thứ ngày tháng năm | Nơi khởi hành
     */
    private String formatSubtitle(int day, int month, int year, String originVal) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month, day);
        int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);

        String dayOfWeekStr;
        switch (dayOfWeek) {
            case java.util.Calendar.SUNDAY:
                dayOfWeekStr = "Chủ Nhật";
                break;
            default:
                dayOfWeekStr = "Thứ " + dayOfWeek;
                break;
        }

        String formattedDate = String.format(Locale.US, "%02d-%02d-%d", day, month + 1, year);
        if (originVal == null || originVal.trim().isEmpty() || originVal.equals("Khởi hành từ")) {
            originVal = "Hồ Chí Minh";
        }
        return dayOfWeekStr + ", " + formattedDate + " | " + originVal;
    }

    /**
     * Tải danh sách tour động từ API Django Backend
     */
    private void fetchFilteredTours(String destVal, String originVal) {
        Log.d("DEBUG_SEARCH", "Bắt đầu fetchFilteredTours: destination='" + destVal + "', origin='" + originVal + "', date=" + day + "/" + month + "/" + year);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.searchTours(destVal, originVal, day, month, year).enqueue(new Callback<List<Tour>>() {
            @Override
            public void onResponse(Call<List<Tour>> call, Response<List<Tour>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("DEBUG_SEARCH", "onResponse: Thành công! Trả về " + response.body().size() + " tour.");
                    filteredList.clear();
                    filteredList.addAll(response.body());
                    displayTours();
                } else {
                    Log.e("DEBUG_SEARCH", "onResponse: Thất bại! Code=" + response.code() + ", Message=" + response.message());
                    filteredList.clear();
                    displayTours();
                }
            }

            @Override
            public void onFailure(Call<List<Tour>> call, Throwable t) {
                Log.e("DEBUG_SEARCH", "onFailure: Lỗi kết nối API! Cụ thể: " + t.getMessage(), t);
                filteredList.clear();
                displayTours();
            }
        });
    }

    /**
     * Ánh xạ hình ảnh cục bộ dựa trên mã code của tour
     */
    private int getTourImageRes(String code) {
        if (code == null) return R.drawable.img_phuquoc_tour; // mặc định
        String normalizedCode = code.toLowerCase();
        if (normalizedCode.contains("phuquoc")) return R.drawable.img_phuquoc_tour;
        if (normalizedCode.contains("nhatrang")) return R.drawable.img_nhatrang_tour;
        if (normalizedCode.contains("danang")) return R.drawable.img_danang_tour;
        if (normalizedCode.contains("sapa")) return R.drawable.img_sapa_tour;
        if (normalizedCode.contains("halong")) return R.drawable.img_halong_tour;
        if (normalizedCode.contains("singapore")) return R.drawable.img_singapore_tour;
        if (normalizedCode.contains("taiwan")) return R.drawable.img_taiwan_tour;
        return R.drawable.img_phuquoc_tour; // fallback
    }

    /**
     * Render các thẻ tour động vào container
     */
    private void displayTours() {
        if (getContext() == null || containerTours == null) return;

        containerTours.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (filteredList.isEmpty()) {
            if (layoutNoResults != null) layoutNoResults.setVisibility(View.VISIBLE);
            return;
        }

        if (layoutNoResults != null) layoutNoResults.setVisibility(View.GONE);

        for (Tour tour : filteredList) {
            View cardView = inflater.inflate(R.layout.item_tour_card, containerTours, false);

            ImageView ivTourImage = cardView.findViewById(R.id.ivTourImage);
            TextView tvRibbonBadge = cardView.findViewById(R.id.tvRibbonBadge);
            TextView tvTourTitle = cardView.findViewById(R.id.tvTourTitle);
            TextView tvOldPrice = cardView.findViewById(R.id.tvOldPrice);
            TextView tvNewPrice = cardView.findViewById(R.id.tvNewPrice);
            View btnViewTour = cardView.findViewById(R.id.btnViewTour);

            if (ivTourImage != null) {
                int imageResId = getTourImageRes(tour.getCode());
                ivTourImage.setImageResource(imageResId);
            }

            if (tvRibbonBadge != null) {
                if (tour.getProvider() == null || tour.getProvider().isEmpty()) {
                    tvRibbonBadge.setVisibility(View.GONE);
                } else {
                    tvRibbonBadge.setVisibility(View.VISIBLE);
                    tvRibbonBadge.setText(tour.getProvider());
                }
            }

            if (tvTourTitle != null) {
                tvTourTitle.setText(tour.getTitle());
            }

            if (tvOldPrice != null) {
                tvOldPrice.setText(formatPrice(tour.getOriginalPrice()));
                // Tạo hiệu ứng gạch ngang giá cũ
                tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            if (tvNewPrice != null) {
                tvNewPrice.setText(formatPrice(tour.getDiscountPrice()));
            }

            // Thiết lập sự kiện click Xem tour
            View.OnClickListener openDetailListener = v -> {
                DetailTour detailFragment = new DetailTour();
                Bundle args = new Bundle();
                args.putSerializable("tour_object", tour);
                args.putString("tour_type", tour.getCode());
                args.putInt("tour_id", tour.getId());
                detailFragment.setArguments(args);

                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame, detailFragment)
                        .addToBackStack(null)
                        .commit();
                }
            };

            cardView.setOnClickListener(openDetailListener);
            if (btnViewTour != null) {
                btnViewTour.setOnClickListener(openDetailListener);
            }

            containerTours.addView(cardView);
        }
    }

    /**
     * Định dạng tiền tệ phân tách dấu chấm (e.g. 4.990.000)
     */
    private String formatPrice(double price) {
        return String.format(Locale.US, "%,.0f", price).replace(",", ".");
    }

    /**
     * Hiển thị bảng chọn sắp xếp giá thời gian thực
     */
    private void showSortDialog() {
        String[] options = {
            "Mặc định (Tìm kiếm)",
            "Giá từ thấp đến cao",
            "Giá từ cao đến thấp"
        };

        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Sắp xếp theo")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    fetchFilteredTours(destination, origin);
                } else if (which == 1) {
                    Collections.sort(filteredList, (t1, t2) -> Double.compare(t1.getDiscountPrice(), t2.getDiscountPrice()));
                    displayTours();
                } else if (which == 2) {
                    Collections.sort(filteredList, (t1, t2) -> Double.compare(t2.getDiscountPrice(), t1.getDiscountPrice()));
                    displayTours();
                }
            })
            .show();
    }

    /**
     * Hiển thị bộ lọc tùy chọn dịch vụ thời gian thực
     */
    private void showFilterDialog() {
        String[] filterTypes = {
                "Lọc theo khoảng giá",
                "Lọc theo hãng vận chuyển",
                "Lọc theo số ngày",
                "Xóa bộ lọc (hiển thị tất cả)"
        };

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Tùy chọn lọc")
                .setItems(filterTypes, (dialog, which) -> {
                    if (which == 0) showPriceFilterDialog();
                    else if (which == 1) showProviderFilterDialog();
                    else if (which == 2) showDurationFilterDialog();
                    else fetchFilteredTours(destination, origin);
                })
                .show();
    }
    private void showDurationFilterDialog() {
        String[] durations = {
                "Trong ngày (1 ngày)",
                "2 ngày 1 đêm",
                "3 ngày 2 đêm",
                "4 ngày 3 đêm",
                "5 ngày trở lên"
        };

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Lọc theo số ngày")
                .setItems(durations, (dialog, which) -> {
                    String[] keywords = {"trong ngày", "2n1", "3n2", "4n3", "5n"};
                    // Tìm theo title chứa keyword số ngày
                    String[] titleKeywords;
                    if (which == 0) titleKeywords = new String[]{"trong ngày", "1 ngày"};
                    else if (which == 1) titleKeywords = new String[]{"2n1", "2 ngày 1"};
                    else if (which == 2) titleKeywords = new String[]{"3n2", "3 ngày 2"};
                    else if (which == 3) titleKeywords = new String[]{"4n3", "4 ngày 3"};
                    else titleKeywords = new String[]{"5n4", "5 ngày", "6n", "7n"};

                    List<Tour> temp = new ArrayList<>();
                    for (Tour t : filteredList) {
                        if (t.getTitle() != null) {
                            String lowerTitle = t.getTitle().toLowerCase();
                            for (String kw : titleKeywords) {
                                if (lowerTitle.contains(kw)) {
                                    temp.add(t);
                                    break;
                                }
                            }
                        }
                    }
                    filteredList = temp;
                    displayTours();
                })
                .show();
    }

    private void showPriceFilterDialog() {
        String[] priceRanges = {
                "Dưới 2.000.000 VND",
                "2.000.000 - 5.000.000 VND",
                "5.000.000 - 10.000.000 VND",
                "Trên 10.000.000 VND"
        };

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Lọc theo khoảng giá")
                .setItems(priceRanges, (dialog, which) -> {
                    double minPrice = 0, maxPrice = Double.MAX_VALUE;
                    if (which == 0) { minPrice = 0; maxPrice = 2_000_000; }
                    else if (which == 1) { minPrice = 2_000_000; maxPrice = 5_000_000; }
                    else if (which == 2) { minPrice = 5_000_000; maxPrice = 10_000_000; }
                    else if (which == 3) { minPrice = 10_000_000; maxPrice = Double.MAX_VALUE; }

                    final double fMin = minPrice, fMax = maxPrice;
                    List<Tour> temp = new ArrayList<>();
                    for (Tour t : filteredList) {
                        double price = t.getDiscountPrice() > 0 ? t.getDiscountPrice() : t.getOriginalPrice();
                        if (price >= fMin && price <= fMax) temp.add(t);
                    }
                    filteredList = temp;
                    displayTours();
                })
                .show();
    }
    private void showProviderFilterDialog() {
        String[] options = {
                "Vietjet Air",
                "Vietnam Airlines",
                "Bamboo Airways",
                "Nhà xe / Phương tiện khác"
        };

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Lọc theo hãng vận chuyển")
                .setItems(options, (dialog, which) -> {
                    String[] keywords = {"vietjet", "vietnam airlines", "bamboo", "xe"};
                    String keyword = keywords[which];
                    List<Tour> temp = new ArrayList<>();
                    for (Tour t : filteredList) {
                        if (t.getProvider() != null && t.getProvider().toLowerCase().contains(keyword)) {
                            temp.add(t);
                        }
                    }
                    filteredList = temp;
                    displayTours();
                })
                .show();
    }


    @Override
    public void onStart() {
        super.onStart();
        // Ẩn BottomNavigationView của MainActivity
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
        // Hiện lại BottomNavigationView khi rời đi
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.bottomNavigation);
            if (nav != null) {
                nav.setVisibility(View.VISIBLE);
            }
        }
    }
}
