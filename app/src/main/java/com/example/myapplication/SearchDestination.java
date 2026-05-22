package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.ArrayList;
import java.util.List;

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

    private List<DestinationItem> recentList = new ArrayList<>();
    private List<DestinationItem> hotList = new ArrayList<>();

    private LinearLayout containerRecent;
    private LinearLayout containerHot;
    private View layoutRecentSection;
    private View layoutHotHeader;
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
        hotList.add(new DestinationItem("Miền Bắc", "59 tours", R.drawable.img_sapa_tour)); // Re-use sapa image for Miền Bắc
        hotList.add(new DestinationItem("Singapore", "38 tours", R.drawable.img_singapore_tour));
        hotList.add(new DestinationItem("Đài Loan", "32 tours", R.drawable.img_taiwan_tour));
        hotList.add(new DestinationItem("Đà Nẵng", "45 tours", R.drawable.img_danang_tour));
        hotList.add(new DestinationItem("Nha Trang", "25 tours", R.drawable.img_nhatrang_tour));
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
        layoutRecentSection = view.findViewById(R.id.layoutRecentSection);
        layoutHotHeader = view.findViewById(R.id.layoutHotHeader);
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
                    filterDestinations(query);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Tải danh sách mặc định khi mới mở màn hình
        filterDestinations("");

        return view;
    }

    private void filterDestinations(String query) {
        if (getContext() == null || containerRecent == null || containerHot == null) return;

        // Xóa sạch view cũ
        containerRecent.removeAllViews();
        containerHot.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        boolean hasRecentMatches = false;
        boolean hasHotMatches = false;

        // 1. Lọc và nạp phần "Gần đây"
        for (DestinationItem item : recentList) {
            if (query.isEmpty() || item.name.toLowerCase().contains(query.toLowerCase())) {
                hasRecentMatches = true;
                addDestinationView(containerRecent, item, inflater);
            }
        }

        // 2. Lọc và nạp phần "Địa điểm HOT"
        for (DestinationItem item : hotList) {
            if (query.isEmpty() || item.name.toLowerCase().contains(query.toLowerCase())) {
                hasHotMatches = true;
                addDestinationView(containerHot, item, inflater);
            }
        }

        // 3. Xử lý hiển thị ẩn hiện các section và màn hình rỗng
        if (layoutRecentSection != null) {
            layoutRecentSection.setVisibility(hasRecentMatches ? View.VISIBLE : View.GONE);
        }
        if (layoutHotHeader != null) {
            layoutHotHeader.setVisibility(hasHotMatches ? View.VISIBLE : View.GONE);
        }
        if (containerHot != null) {
            containerHot.setVisibility(hasHotMatches ? View.VISIBLE : View.GONE);
        }

        if (layoutNoResults != null) {
            layoutNoResults.setVisibility((!hasRecentMatches && !hasHotMatches) ? View.VISIBLE : View.GONE);
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
            Bundle result = new Bundle();
            result.putString("selected_destination", item.name);
            getParentFragmentManager().setFragmentResult("destination_request", result);
            getParentFragmentManager().popBackStack();
        });

        container.addView(itemView);
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
}
