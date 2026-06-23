package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment hiển thị danh sách địa điểm gần đây và địa điểm HOT.
 * Cho phép tìm kiếm bộ lọc trực quan và chọn địa điểm đẩy về trang chủ.
 */
public class SearchDestination extends Fragment {

    public static class DestinationItem {
        public String name;
        public String toursCount;
        public int imageResId;

        public DestinationItem(String name, String toursCount, int imageResId) {
            this.name = name;
            this.toursCount = toursCount;
            this.imageResId = imageResId;
        }
    }
    private static final String PREFS_NAME = "search_history";
    private static final String KEY_HISTORY = "history";
    private static final int MAX_HISTORY = 10;
    private static final int DEFAULT_SHOW = 3;
    private boolean isShowingAllHistory = false;

    private List<DestinationItem> recentList = new ArrayList<>();
    private List<DestinationItem> hotList = new ArrayList<>();
    private List<Tour> allTours = new ArrayList<>();

    private LinearLayout containerRecent;
    private LinearLayout containerHot;
    private LinearLayout containerSearchResults;
    private View layoutRecentSection;
    private View layoutHotHeader;
    private View layoutSearchResultsSection;
    private View layoutNoResults;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo danh sách địa điểm gần đây
        recentList.add(new DestinationItem("Phú Quốc", "13 tours", R.drawable.img_phuquoc_tour));
        recentList.add(new DestinationItem("Vịnh Hạ Long", "22 tours", R.drawable.img_halong_tour));
        recentList.add(new DestinationItem("Sapa", "18 tours", R.drawable.img_sapa_tour));

        // Khởi tạo danh sách địa điểm HOT
        hotList.add(new DestinationItem("Phú Quốc", "13 tours", R.drawable.img_phuquoc_tour));
        hotList.add(new DestinationItem("Miền Bắc", "59 tours", R.drawable.img_sapa_tour));
        hotList.add(new DestinationItem("Singapore", "38 tours", R.drawable.img_singapore_tour));
        hotList.add(new DestinationItem("Đài Loan", "32 tours", R.drawable.img_taiwan_tour));
        hotList.add(new DestinationItem("Đà Nẵng", "45 tours", R.drawable.img_danang_tour));
        hotList.add(new DestinationItem("Nha Trang", "25 tours", R.drawable.img_nhatrang_tour));

        // Nạp danh sách tour mẫu làm dự phòng (fallback) để hiển thị tức thời
        initializeMockTours();
    }

    private void initializeMockTours() {
//        allTours.clear();
//        allTours.add(new Tour("phuquoc", "Tour Phú Quốc 3N2Đ: HCM - Phú Quốc - Câu Cá Lặn Ngắm San Hô - Nam Đảo", "Khám phá đảo ngọc Phú Quốc", 5290000, 4990000, "Vietjet Air", 4.8, 12, "", ""));
//        allTours.add(new Tour("phuquoc", "Tour Phú Quốc 3N2Đ: HCM - Phú Quốc - Khám Phá Vinpearl Safari & Grand World", "Khám Phá Vinpearl Safari & Grand World", 6190000, 5490000, "Gồm Vé Vinpearl Safari", 4.9, 24, "", ""));
//        allTours.add(new Tour("nhatrang", "Tour Nha Trang 3N2Đ: Khám Phá Vịnh San Hô - VinWonders Trọn Gói", "Nha Trang biển hẹn", 3690000, 3190000, "VietJet Air", 4.7, 15, "", ""));
//        allTours.add(new Tour("danang", "Tour Đà Nẵng - Hội An - Bà Nà Hills 4N3Đ Trọn Gói Giá Tốt", "Đà Nẵng Hội An Bà Nà Hills", 5490000, 4890000, "Vietnam Airlines", 4.8, 30, "", ""));
//        allTours.add(new Tour("sapa", "Tour Sapa 3N2Đ: Hà Nội - Bản Cát Cát - Chinh Phục Đỉnh Fansipan", "Sapa sương mù", 3790000, 3290000, "Xe VIP Cabin", 4.9, 18, "", ""));
//        allTours.add(new Tour("halong", "Tour Vịnh Hạ Long 2N1Đ: Nghỉ Dưỡng Trên Du Thuyền Sang Trọng 5 Sao", "Hạ Long du thuyền 5 sao", 2990000, 2590000, "Du Thuyền 5 Sao", 4.9, 25, "", ""));
//        allTours.add(new Tour("singapore", "Tour Singapore - Malaysia 5N4Đ: HCM - Singapore - Kuala Lumpur - Genting", "Singapore Malaysia", 13890000, 12890000, "Singapore Airlines", 4.7, 38, "", ""));
//        allTours.add(new Tour("taiwan", "Tour Đài Loan 5N4Đ: HCM - Cao Hùng - Đài Trung - Đài Bắc - Đảo Hoà Bình", "Đài Loan 5 ngày 4 đêm", 15390000, 14390000, "Bamboo Airways", 4.8, 32, "", ""));
//
//        // Thêm các tour Vũng Tàu tương tự như trong ảnh mẫu thứ 2
//        allTours.add(new Tour("vungtau", "Tour Miền Nam 4N3Đ: Hà Nội - Sài Gòn - Mũi Né - Vũng Tàu", "Tour liên tuyến Miền Nam", 4500000, 3990000, "Xe du lịch", 4.6, 8, "", ""));
//        allTours.add(new Tour("vungtau", "Tour Vũng Tàu Trong Ngày: HCM - Tượng Chúa - Bạch Dinh - Mũi Nghinh Phong", "Tour Vũng Tàu 1 ngày giá rẻ", 990000, 790000, "Xe du lịch", 4.7, 16, "", ""));
//        allTours.add(new Tour("vungtau", "Tour Vũng Tàu Trong Ngày: Khám Phá Thành Phố Biển Xinh Đẹp", "Tour Vũng Tàu trọn gói", 890000, 690000, "Xe du lịch", 4.8, 10, "", ""));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_destination, container, false);

        // Tham chiếu các view chính
        ImageView btnBack = view.findViewById(R.id.btnBack);
        EditText etSearchDest = view.findViewById(R.id.etSearchDest);
        ImageView btnClearSearch = view.findViewById(R.id.btnClearSearch);

        containerRecent = view.findViewById(R.id.containerRecent);
        containerHot = view.findViewById(R.id.containerHot);
        containerSearchResults = view.findViewById(R.id.containerSearchResults);
        
        layoutRecentSection = view.findViewById(R.id.layoutRecentSection);
        layoutHotHeader = view.findViewById(R.id.layoutHotHeader);
        layoutSearchResultsSection = view.findViewById(R.id.layoutSearchResultsSection);
        layoutNoResults = view.findViewById(R.id.layoutNoResults);

        // Nút quay lại
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        // Sự kiện xóa nội dung ô tìm kiếm nhanh
        if (btnClearSearch != null && etSearchDest != null) {
            btnClearSearch.setOnClickListener(v -> etSearchDest.setText(""));
        }

        // Lắng nghe thay đổi chữ nhập vào ô tìm kiếm
        if (etSearchDest != null) {
            etSearchDest.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    if (btnClearSearch != null) {
                        btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    
                    if (query.isEmpty()) {
                        showDefaultSuggestions();
                    } else {
                        showCustomQuerySuggestion(query);
                        searchToursFromBackend(query);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Lắng nghe nút Tìm kiếm trên bàn phím (IME_ACTION_SEARCH)
            etSearchDest.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    String query = etSearchDest.getText().toString().trim();
                    if (!query.isEmpty()) {
                        saveSearchHistory(query);
                        Bundle result = new Bundle();
                        result.putString("selected_destination", query);
                        getParentFragmentManager().setFragmentResult("destination_request", result);
                        getParentFragmentManager().popBackStack();
                        return true;
                    }
                }
                return false;
            });
        }

        // Tải danh sách mặc định khi mới mở màn hình
        showDefaultSuggestions();

        // Tải toàn bộ Tour động từ Django API (không cần thiết nữa vì ta search trực tiếp)
        // loadAllTours();

        return view;
    }

    private void searchToursFromBackend(String query) {
        if (getContext() == null) return;
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        // Gửi từ khóa query lên server thông qua tham số destination
        apiService.searchTours(query, "", 0, 0, 0).enqueue(new Callback<List<Tour>>() {
            @Override
            public void onResponse(Call<List<Tour>> call, Response<List<Tour>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Tour> tours = response.body();
                    handleSearchResults(query, tours);
                } else {
                    handleSearchResults(query, new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Tour>> call, Throwable t) {
                Log.e("SEARCH_DEST", "Lỗi tìm kiếm từ API: " + t.getMessage());
                // Fallback: tìm local nếu API lỗi
                handleSearchResults(query, filterLocal(query));
            }
        });
    }

    private List<Tour> filterLocal(String query) {
        String normQuery = removeAccents(query);
        List<Tour> result = new ArrayList<>();
        for (Tour tour : allTours) {
            String normTitle = removeAccents(tour.getTitle());
            String normDesc = removeAccents(tour.getDescription());
            if (normTitle.contains(normQuery) || normDesc.contains(normQuery)) {
                result.add(tour);
            }
        }
        return result;
    }

    private String removeAccents(String text) {
        if (text == null) return "";
        String s1 = "ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚÝàáâãèéêìíòóôõùúýĂăĐđĨĩŨũƠơƯưẠạẢảẤấẦầẨẩẪẫẬậẮắẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợỤụỦủỨứỪừỬửỮữỰựỲỳỶỷỸỹỴỵ";
        String s0 = "AAAAEECIIOOOUYaaaaeeciiooouyAaDdIiUuOoUuAaAaAaAaAaAaAaAaAaAaAaAaEeEeEeEeEeEeEeEeIiIiOoOoOoOoOoOoOoOoOoOoOoOoUuUuUuUuUuUuUuYyYyYyYy";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int idx = s1.indexOf(c);
            if (idx != -1) {
                sb.append(s0.charAt(idx));
            } else {
                sb.append(c);
            }
        }
        return sb.toString().toLowerCase();
    }

    private String detectDestination(Tour tour, String query) {
        if (query != null && !query.isEmpty()) {
            return query.substring(0, 1).toUpperCase() + query.substring(1).toLowerCase();
        }
        String title = removeAccents(tour.getTitle());
        if (title.contains("phu quoc")) return "Phú Quốc";
        if (title.contains("ha long") || title.contains("halong")) return "Vịnh Hạ Long";
        if (title.contains("nha trang")) return "Nha Trang";
        if (title.contains("da nang")) return "Đà Nẵng";
        if (title.contains("sapa")) return "Sapa";
        if (title.contains("vung tau")) return "Vũng Tàu";
        if (title.contains("singapore")) return "Singapore";
        if (title.contains("taiwan") || title.contains("dai loan")) return "Đài Loan";
        if (title.contains("mien tay")) return "Miền Tây";
        if (title.contains("hon son")) return "Hòn Sơn";
        return tour.getTitle();
    }

    private void showDefaultSuggestions() {
        if (getContext() == null || containerRecent == null || containerHot == null) return;

        containerRecent.removeAllViews();
        containerHot.removeAllViews();
        if (containerSearchResults != null) containerSearchResults.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (layoutSearchResultsSection != null) layoutSearchResultsSection.setVisibility(View.GONE);
        if (layoutNoResults != null) layoutNoResults.setVisibility(View.GONE);
        if (layoutHotHeader != null) layoutHotHeader.setVisibility(View.VISIBLE);
        if (containerHot != null) containerHot.setVisibility(View.VISIBLE);

        // ✅ Hiển thị lịch sử tìm kiếm
        List<String> history = getSearchHistory();
        if (history.isEmpty()) {
            if (layoutRecentSection != null) layoutRecentSection.setVisibility(View.GONE);
        } else {
            if (layoutRecentSection != null) layoutRecentSection.setVisibility(View.VISIBLE);

            // Header "Gần đây" + nút Xóa
            View recentHeader = inflater.inflate(R.layout.item_recent_header, containerRecent, false);
            TextView tvClear = recentHeader != null ? recentHeader.findViewById(R.id.tvClearHistory) : null;
            if (tvClear != null) {
                tvClear.setOnClickListener(v -> {
                    clearSearchHistory();
                    showDefaultSuggestions();
                });
            }
            if (recentHeader != null) containerRecent.addView(recentHeader);

            // Hiện 3 cái gần nhất (hoặc tất cả nếu đang xem thêm)
            int showCount = isShowingAllHistory ? Math.min(history.size(), MAX_HISTORY) : Math.min(history.size(), DEFAULT_SHOW);
            for (int i = 0; i < showCount; i++) {
                final String keyword = history.get(i);
                addHistoryItemView(containerRecent, keyword, inflater);
            }

            // Nút "Xem thêm" nếu có hơn 3 lịch sử và chưa xem tất cả
            if (history.size() > DEFAULT_SHOW && !isShowingAllHistory) {
                View btnMore = inflater.inflate(R.layout.item_show_more, containerRecent, false);
                TextView tvMore = btnMore != null ? btnMore.findViewById(R.id.tvShowMore) : null;
                if (tvMore != null) {
                    tvMore.setText("Xem thêm " + (history.size() - DEFAULT_SHOW) + " tìm kiếm");
                    tvMore.setOnClickListener(v -> {
                        isShowingAllHistory = true;
                        showDefaultSuggestions();
                    });
                }
                if (btnMore != null) containerRecent.addView(btnMore);
            } else if (isShowingAllHistory && history.size() > DEFAULT_SHOW) {
                // Nút "Thu gọn"
                View btnLess = inflater.inflate(R.layout.item_show_more, containerRecent, false);
                TextView tvLess = btnLess != null ? btnLess.findViewById(R.id.tvShowMore) : null;
                if (tvLess != null) {
                    tvLess.setText("Thu gọn");
                    tvLess.setOnClickListener(v -> {
                        isShowingAllHistory = false;
                        showDefaultSuggestions();
                    });
                }
                if (btnLess != null) containerRecent.addView(btnLess);
            }
        }

        for (DestinationItem item : hotList) {
            addDestinationView(containerHot, item, inflater);
        }
    }

    private void addHistoryItemView(LinearLayout container, String keyword, LayoutInflater inflater) {
        View itemView = inflater.inflate(R.layout.item_destination_search, container, false);
        ImageView ivThumb = itemView.findViewById(R.id.ivDestThumb);
        TextView tvName = itemView.findViewById(R.id.tvDestName);
        TextView tvCount = itemView.findViewById(R.id.tvDestToursCount);

        if (ivThumb != null) {
            ivThumb.setImageResource(android.R.drawable.ic_menu_recent_history);
            ivThumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int padding = (int) (12 * getResources().getDisplayMetrics().density);
            ivThumb.setPadding(padding, padding, padding, padding);
        }
        if (tvName != null) tvName.setText(keyword);
        if (tvCount != null) tvCount.setText("Tìm kiếm gần đây");

        itemView.setOnClickListener(v -> {
            saveSearchHistory(keyword);
            Bundle result = new Bundle();
            result.putString("selected_destination", keyword);
            getParentFragmentManager().setFragmentResult("destination_request", result);
            getParentFragmentManager().popBackStack();
        });

        container.addView(itemView);
    }

    private void displaySearchResults(List<Tour> results, String query) {
        if (getContext() == null || containerSearchResults == null) return;

        containerSearchResults.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (results.isEmpty()) {
            if (layoutSearchResultsSection != null) layoutSearchResultsSection.setVisibility(View.GONE);
            if (layoutNoResults != null) layoutNoResults.setVisibility(View.VISIBLE);
            return;
        }

        if (layoutSearchResultsSection != null) layoutSearchResultsSection.setVisibility(View.VISIBLE);
        if (layoutNoResults != null) layoutNoResults.setVisibility(View.GONE);
        Map<String, List<Tour>> groupedTours = new HashMap<>();
        for (Tour tour : results) {
            String destName = detectDestination(tour, query);
            List<Tour> list = groupedTours.get(destName);
            if (list == null) {
                list = new ArrayList<>();
                groupedTours.put(destName, list);
            }
            list.add(tour);
        }

        for (Map.Entry<String, List<Tour>> entry : groupedTours.entrySet()) {
            final String destName = entry.getKey();
            List<Tour> toursList = entry.getValue();

            View headerView = inflater.inflate(R.layout.item_search_group_header, containerSearchResults, false);
            TextView tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
            TextView tvHeaderToursCount = headerView.findViewById(R.id.tvHeaderToursCount);

            if (tvHeaderName != null) tvHeaderName.setText(destName);
            if (tvHeaderToursCount != null) tvHeaderToursCount.setText(toursList.size() + " Tours");

            headerView.setOnClickListener(v -> {
                saveSearchHistory(query);
                Bundle resultBundle = new Bundle();
                resultBundle.putString("selected_destination", destName);
                getParentFragmentManager().setFragmentResult("destination_request", resultBundle);
                getParentFragmentManager().popBackStack();
            });

            containerSearchResults.addView(headerView);

            for (final Tour tour : toursList) {
                View tourView = inflater.inflate(R.layout.item_search_tour_row, containerSearchResults, false);
                TextView tvTourRowTitle = tourView.findViewById(R.id.tvTourRowTitle);
                if (tvTourRowTitle != null) tvTourRowTitle.setText(tour.getTitle());

                tourView.setOnClickListener(v -> {
                    saveSearchHistory(query);
                    Bundle resultBundle = new Bundle();
                    resultBundle.putString("selected_destination", tour.getTitle());
                    getParentFragmentManager().setFragmentResult("destination_request", resultBundle);
                    getParentFragmentManager().popBackStack();
                });

                containerSearchResults.addView(tourView);
            }
        }
    }

    private void addDestinationView(LinearLayout container, DestinationItem item, LayoutInflater inflater) {
        View itemView = inflater.inflate(R.layout.item_destination_search, container, false);

        ImageView ivThumb = itemView.findViewById(R.id.ivDestThumb);
        TextView tvName = itemView.findViewById(R.id.tvDestName);
        TextView tvCount = itemView.findViewById(R.id.tvDestToursCount);

        if (ivThumb != null) {
            ivThumb.setImageResource(item.imageResId);
        }
        if (tvName != null) {
            tvName.setText(item.name);
        }
        if (tvCount != null) {
            tvCount.setText(item.toursCount);
        }

        // Bấm chọn địa điểm: gửi kết quả và quay lại Home Fragment
        itemView.setOnClickListener(v -> {
            saveSearchHistory(item.name);
            Bundle result = new Bundle();
            result.putString("selected_destination", item.name);
            getParentFragmentManager().setFragmentResult("destination_request", result);
            getParentFragmentManager().popBackStack();
        });

        container.addView(itemView);
    }

    private void showCustomQuerySuggestion(String query) {
        if (getContext() == null || containerRecent == null) return;

        containerRecent.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (layoutRecentSection != null) layoutRecentSection.setVisibility(View.VISIBLE);
        if (layoutHotHeader != null) layoutHotHeader.setVisibility(View.GONE);
        if (containerHot != null) containerHot.setVisibility(View.GONE);
        if (layoutSearchResultsSection != null) layoutSearchResultsSection.setVisibility(View.GONE);
        if (layoutNoResults != null) layoutNoResults.setVisibility(View.GONE);

        View itemView = inflater.inflate(R.layout.item_destination_search, containerRecent, false);
        ImageView ivThumb = itemView.findViewById(R.id.ivDestThumb);
        TextView tvName = itemView.findViewById(R.id.tvDestName);
        TextView tvCount = itemView.findViewById(R.id.tvDestToursCount);

        if (ivThumb != null) {
            ivThumb.setImageResource(R.drawable.ic_location_pin);
            ivThumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int padding = (int) (12 * getResources().getDisplayMetrics().density);
            ivThumb.setPadding(padding, padding, padding, padding);
        }
        if (tvName != null) {
            tvName.setText(query);
        }
        if (tvCount != null) {
            tvCount.setText("Bấm để tìm kiếm");
        }

        itemView.setOnClickListener(v -> {
            saveSearchHistory(query);
            Bundle result = new Bundle();
            result.putString("selected_destination", query);
            getParentFragmentManager().setFragmentResult("destination_request", result);
            getParentFragmentManager().popBackStack();
        });

        containerRecent.addView(itemView);
    }

    private void handleSearchResults(String query, List<Tour> results) {
        if (getActivity() == null || containerSearchResults == null) return;
        getActivity().runOnUiThread(() -> {
            if (results.isEmpty()) {
                if (layoutSearchResultsSection != null)
                    layoutSearchResultsSection.setVisibility(View.GONE);
                if (layoutNoResults != null)
                    layoutNoResults.setVisibility(View.GONE);
            } else {
                if (layoutRecentSection != null)
                    layoutRecentSection.setVisibility(View.VISIBLE);
                if (layoutSearchResultsSection != null)
                    layoutSearchResultsSection.setVisibility(View.VISIBLE);
                if (layoutNoResults != null)
                    layoutNoResults.setVisibility(View.GONE);
                displaySearchResults(results, query);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Ẩn thanh BottomNavigationView khi vào trang tìm kiếm
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
        // Hiện lại thanh BottomNavigationView khi thoát trang tìm kiếm
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.bottomNavigation);
            if (nav != null) {
                nav.setVisibility(View.VISIBLE);
            }
        }
    }
    private List<String> getSearchHistory() {
        if (getContext() == null) return new ArrayList<>();
        android.content.SharedPreferences prefs = getContext()
                .getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY, "");
        List<String> list = new ArrayList<>();
        if (!json.isEmpty()) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(json);
                for (int i = 0; i < arr.length(); i++) {
                    list.add(arr.getString(i));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        return list;
    }

    private void saveSearchHistory(String query) {
        if (getContext() == null || query.trim().isEmpty()) return;
        List<String> history = getSearchHistory();
        history.remove(query); // xóa nếu đã có để tránh trùng
        history.add(0, query); // thêm vào đầu
        if (history.size() > MAX_HISTORY) {
            history = history.subList(0, MAX_HISTORY);
        }
        try {
            org.json.JSONArray arr = new org.json.JSONArray(history);
            getContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                    .edit().putString(KEY_HISTORY, arr.toString()).apply();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void clearSearchHistory() {
        if (getContext() == null) return;
        getContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .edit().remove(KEY_HISTORY).apply();
    }
}
