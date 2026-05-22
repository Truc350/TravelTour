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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

    private List<Tour> tourDatabase = new ArrayList<>();
    private List<Tour> filteredList = new ArrayList<>();

    private LinearLayout containerTours;
    private View layoutNoResults;
    private TextView tvSearchTitle;
    private TextView tvSearchSubtitle;

    // Lớp chứa dữ liệu Tour mô phỏng
    static class Tour {
        String id;
        String title;
        int imageRes;
        String badgeText;
        int oldPrice;
        int newPrice;
        String destination;

        public Tour(String id, String title, int imageRes, String badgeText, int oldPrice, int newPrice, String destination) {
            this.id = id;
            this.title = title;
            this.imageRes = imageRes;
            this.badgeText = badgeText;
            this.oldPrice = oldPrice;
            this.newPrice = newPrice;
            this.destination = destination;
        }
    }

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

        // Khởi tạo Database Tour mô phỏng phong phú
        initializeDatabase();
    }

    private void initializeDatabase() {
        // Phú Quốc (Đúng y hệt ảnh screenshot yêu cầu)
        tourDatabase.add(new Tour(
            "phuquoc",
            "Tour Phú Quốc 3N2Đ: HCM - Phú Quốc - Câu Cá Lặn Ngắm San Hô - Nam Đảo",
            R.drawable.img_phuquoc_tour,
            "Vietjet Air",
            5290000,
            4990000,
            "Phú Quốc"
        ));
        tourDatabase.add(new Tour(
            "phuquoc",
            "Tour Phú Quốc 3N2Đ: HCM - Phú Quốc - Khám Phá Vinpearl Safari & Grand World",
            R.drawable.img_vinpearl_safari,
            "Gồm Vé Vinpearl Safari",
            6190000,
            5490000,
            "Phú Quốc"
        ));

        // Nha Trang
        tourDatabase.add(new Tour(
            "nhatrang",
            "Tour Nha Trang 3N2Đ: Khám Phá Vịnh San Hô - VinWonders Trọn Gói",
            R.drawable.img_nhatrang_tour,
            "VietJet Air",
            3690000,
            3190000,
            "Nha Trang"
        ));

        // Đà Nẵng
        tourDatabase.add(new Tour(
            "danang",
            "Tour Đà Nẵng - Hội An - Bà Nà Hills 4N3Đ Trọn Gói Giá Tốt",
            R.drawable.img_danang_tour,
            "Vietnam Airlines",
            5490000,
            4890000,
            "Đà Nẵng"
        ));

        // Sapa
        tourDatabase.add(new Tour(
            "sapa",
            "Tour Sapa 3N2Đ: Hà Nội - Bản Cát Cát - Chinh Phục Đỉnh Fansipan",
            R.drawable.img_sapa_tour,
            "Xe VIP Cabin",
            3790000,
            3290000,
            "Sapa"
        ));

        // Vịnh Hạ Long
        tourDatabase.add(new Tour(
            "halong",
            "Tour Vịnh Hạ Long 2N1Đ: Nghỉ Dưỡng Trên Du Thuyền Sang Trọng 5 Sao",
            R.drawable.img_halong_tour,
            "Du Thuyền 5 Sao",
            2990000,
            2590000,
            "Hạ Long"
        ));

        // Singapore
        tourDatabase.add(new Tour(
            "singapore",
            "Tour Singapore - Malaysia 5N4Đ: HCM - Singapore - Kuala Lumpur - Genting",
            R.drawable.img_singapore_tour,
            "Singapore Airlines",
            13890000,
            12890000,
            "Singapore"
        ));

        // Đài Loan
        tourDatabase.add(new Tour(
            "taiwan",
            "Tour Đài Loan 5N4Đ: HCM - Cao Hùng - Đài Trung - Đài Bắc - Đảo Hoà Bình",
            R.drawable.img_taiwan_tour,
            "Bamboo Airways",
            15390000,
            14390000,
            "Đài Loan"
        ));
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

        // Lọc danh sách tour phù hợp và hiển thị
        filterAndDisplayTours(destination);

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
     * Lọc danh sách theo địa điểm tìm kiếm
     */
    private void filterAndDisplayTours(String query) {
        filteredList.clear();
        String searchKey = query.toLowerCase().trim();

        if (searchKey.isEmpty() || searchKey.equals("bạn muốn đi đâu?") || searchKey.equals("tất cả")) {
            filteredList.addAll(tourDatabase);
        } else {
            for (Tour tour : tourDatabase) {
                if (tour.destination.toLowerCase().contains(searchKey) ||
                    tour.title.toLowerCase().contains(searchKey)) {
                    filteredList.add(tour);
                }
            }
        }
        displayTours();
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
                ivTourImage.setImageResource(tour.imageRes);
            }

            if (tvRibbonBadge != null) {
                if (tour.badgeText == null || tour.badgeText.isEmpty()) {
                    tvRibbonBadge.setVisibility(View.GONE);
                } else {
                    tvRibbonBadge.setVisibility(View.VISIBLE);
                    tvRibbonBadge.setText(tour.badgeText);
                }
            }

            if (tvTourTitle != null) {
                tvTourTitle.setText(tour.title);
            }

            if (tvOldPrice != null) {
                tvOldPrice.setText(formatPrice(tour.oldPrice));
                // Tạo hiệu ứng gạch ngang giá cũ
                tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            if (tvNewPrice != null) {
                tvNewPrice.setText(formatPrice(tour.newPrice));
            }

            // Thiết lập sự kiện click Xem tour
            View.OnClickListener openDetailListener = v -> {
                DetailTour detailFragment = new DetailTour();
                Bundle args = new Bundle();
                args.putString("tour_type", tour.id);
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
    private String formatPrice(int price) {
        return String.format(Locale.US, "%,d", price).replace(",", ".");
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
                    filterAndDisplayTours(destination);
                } else if (which == 1) {
                    Collections.sort(filteredList, (t1, t2) -> Integer.compare(t1.newPrice, t2.newPrice));
                    displayTours();
                } else if (which == 2) {
                    Collections.sort(filteredList, (t1, t2) -> Integer.compare(t2.newPrice, t1.newPrice));
                    displayTours();
                }
            })
            .show();
    }

    /**
     * Hiển thị bộ lọc tùy chọn dịch vụ thời gian thực
     */
    private void showFilterDialog() {
        String[] options = {
            "Tất cả dịch vụ",
            "Hãng Vietjet Air",
            "Gồm vé Vinpearl Safari"
        };

        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Tùy chọn lọc")
            .setItems(options, (dialog, which) -> {
                filterAndDisplayTours(destination); // Khôi phục danh sách chuẩn trước khi lọc
                if (which == 1) {
                    List<Tour> temp = new ArrayList<>();
                    for (Tour t : filteredList) {
                        if (t.badgeText.toLowerCase().contains("vietjet")) {
                            temp.add(t);
                        }
                    }
                    filteredList = temp;
                } else if (which == 2) {
                    List<Tour> temp = new ArrayList<>();
                    for (Tour t : filteredList) {
                        if (t.badgeText.toLowerCase().contains("safari")) {
                            temp.add(t);
                        }
                    }
                    filteredList = temp;
                }
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
