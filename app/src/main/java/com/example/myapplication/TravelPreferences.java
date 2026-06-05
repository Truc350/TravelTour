package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.utils.FlowLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment xử lý trang "Sở thích du lịch" của người dùng.
 * Cho phép chọn/bỏ chọn các tùy chọn hạng sao, phong cách, thương hiệu, loại hình nơi ở và tiện ích.
 * Trạng thái được lưu tự động vào SharedPreferences theo từng người dùng.
 */
public class TravelPreferences extends Fragment {

    private SharedPreferences prefStore;
    private DatabaseHelper dbHelper;
    private String userContactPrefix = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travel_preferences, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        prefStore = requireContext().getSharedPreferences("TravelPreferences", Context.MODE_PRIVATE);

        // Xác định người dùng đang đăng nhập
        SharedPreferences sessionPrefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String contact = sessionPrefs.getString("current_user_contact", "");
        if (contact.isEmpty()) {
            contact = dbHelper.getLastUserContact();
        }
        userContactPrefix = contact;

        // Thiết lập nút quay lại
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        // Ánh xạ các nhóm lựa chọn và thiết lập sự kiện chọn tag
        ViewGroup layoutStars = view.findViewById(R.id.layoutStars);
        FlowLayout flowStyles = view.findViewById(R.id.flowStyles);
        FlowLayout flowBrands = view.findViewById(R.id.flowBrands);
        FlowLayout flowAccommodations = view.findViewById(R.id.flowAccommodations);
        FlowLayout flowAmenities = view.findViewById(R.id.flowAmenities);

        setupViewGroupSelection(layoutStars, "stars");
        setupViewGroupSelection(flowStyles, "styles");
        setupViewGroupSelection(flowBrands, "brands");
        setupViewGroupSelection(flowAccommodations, "accommodations");
        setupViewGroupSelection(flowAmenities, "amenities");

        return view;
    }

    private void setupViewGroupSelection(ViewGroup viewGroup, String categoryKey) {
        if (viewGroup == null) return;
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                final TextView tv = (TextView) child;
                final String value = tv.getText().toString();

                // Đọc trạng thái ban đầu từ SharedPreferences
                boolean isSelected = isPreferenceSelected(categoryKey, value);
                setTagSelected(tv, isSelected);

                // Gắn sự kiện click
                tv.setOnClickListener(v -> {
                    boolean currentSelectedState = (Boolean) tv.getTag();
                    boolean newSelectedState = !currentSelectedState;
                    setTagSelected(tv, newSelectedState);
                    togglePreference(categoryKey, value, newSelectedState);
                });
            }
        }
    }

    private void setTagSelected(TextView tv, boolean selected) {
        if (selected) {
            tv.setBackgroundResource(R.drawable.bg_pref_selected);
            tv.setTextColor(Color.WHITE);
            tv.setTag(true); // Lưu trạng thái hiện tại vào Tag
        } else {
            tv.setBackgroundResource(R.drawable.bg_pref_unselected);
            tv.setTextColor(Color.parseColor("#4A5568")); // Slate/dark grey text
            tv.setTag(false);
        }
    }

    private boolean isPreferenceSelected(String categoryKey, String value) {
        String key = userContactPrefix + "_" + categoryKey;
        if (!prefStore.contains(key)) {
            // Thiết lập giá trị mặc định giống như trong hình screenshot
            if ("styles".equals(categoryKey) && "Hiện đại".equals(value)) return true;
            if ("brands".equals(categoryKey) && "Vinpearl".equals(value)) return true;
            if ("accommodations".equals(categoryKey) && "Biệt thự (Villa)".equals(value)) return true;
            if ("amenities".equals(categoryKey) && "Phòng gia đình".equals(value)) return true;
            return false;
        }

        String currentPrefs = prefStore.getString(key, "");
        if (currentPrefs.isEmpty()) {
            return false;
        }

        String[] parts = currentPrefs.split(",");
        for (String part : parts) {
            if (part.trim().equals(value.trim())) {
                return true;
            }
        }
        return false;
    }

    private void togglePreference(String categoryKey, String value, boolean add) {
        String key = userContactPrefix + "_" + categoryKey;
        List<String> items = new ArrayList<>();

        if (prefStore.contains(key)) {
            String currentPrefs = prefStore.getString(key, "");
            if (!currentPrefs.isEmpty()) {
                String[] parts = currentPrefs.split(",");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        items.add(trimmed);
                    }
                }
            }
        } else {
            // Nạp giá trị mặc định ban đầu nếu chưa được lưu lần nào
            if ("styles".equals(categoryKey)) items.add("Hiện đại");
            else if ("brands".equals(categoryKey)) items.add("Vinpearl");
            else if ("accommodations".equals(categoryKey)) items.add("Biệt thự (Villa)");
            else if ("amenities".equals(categoryKey)) items.add("Phòng gia đình");
        }

        if (add) {
            if (!items.contains(value)) {
                items.add(value);
            }
        } else {
            items.remove(value);
        }

        // Chuyển danh sách thành chuỗi phân tách bằng dấu phẩy
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i));
            if (i < items.size() - 1) {
                sb.append(",");
            }
        }

        prefStore.edit().putString(key, sb.toString()).apply();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Ẩn thanh BottomNavigationView khi vào trang Sở thích du lịch
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
        // Hiện lại thanh BottomNavigationView khi rời khỏi trang Sở thích du lịch
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.bottomNavigation);
            if (nav != null) {
                nav.setVisibility(View.VISIBLE);
            }
        }
    }
}
