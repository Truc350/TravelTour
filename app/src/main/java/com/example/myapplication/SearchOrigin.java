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
 * Fragment hiển thị danh sách địa điểm khởi hành.
 * Hỗ trợ tìm kiếm bộ lọc trực quan và chọn địa điểm khởi hành đẩy về trang chủ.
 */
public class SearchOrigin extends Fragment {

    private List<String> originList = new ArrayList<>();
    private LinearLayout containerOrigins;
    private View layoutNoResults;
    private View layoutSubheader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo danh sách địa điểm khởi hành giống hệt như ảnh
        originList.add("Tất cả");
        originList.add("Hồ Chí Minh");
        originList.add("Hà Nội");
        originList.add("Cần Thơ");
        originList.add("Nha Trang");
        originList.add("Đà Lạt");
        originList.add("Đà Nẵng");
        originList.add("Phú Quốc");
        originList.add("Côn Đảo");
        originList.add("Thái Lan");
        originList.add("Singapore");
        originList.add("Malaysia");
        originList.add("Hội An");
        originList.add("Huế");
        originList.add("Phú Yên");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_origin, container, false);

        ImageView btnBack = view.findViewById(R.id.btnBack);
        EditText etSearchOrigin = view.findViewById(R.id.etSearchOrigin);
        ImageView btnClearSearch = view.findViewById(R.id.btnClearSearch);

        containerOrigins = view.findViewById(R.id.containerOrigins);
        layoutNoResults = view.findViewById(R.id.layoutNoResults);
        layoutSubheader = view.findViewById(R.id.layoutSubheader);

        // Nút quay lại
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        // Sự kiện xóa nhanh
        if (btnClearSearch != null && etSearchOrigin != null) {
            btnClearSearch.setOnClickListener(v -> etSearchOrigin.setText(""));
        }

        // Lắng nghe thay đổi chữ tìm kiếm
        if (etSearchOrigin != null) {
            etSearchOrigin.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    if (btnClearSearch != null) {
                        btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    filterOrigins(query);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Hiện danh sách mặc định ban đầu
        filterOrigins("");

        return view;
    }

    private void filterOrigins(String query) {
        if (getContext() == null || containerOrigins == null) return;

        containerOrigins.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        boolean hasMatches = false;

        for (String originName : originList) {
            if (query.isEmpty() || originName.toLowerCase().contains(query.toLowerCase())) {
                hasMatches = true;
                View itemView = inflater.inflate(R.layout.item_origin_search, containerOrigins, false);
                TextView tvOriginName = itemView.findViewById(R.id.tvOriginName);

                if (tvOriginName != null) {
                    tvOriginName.setText(originName);
                }

                // Gửi kết quả về khi bấm chọn
                itemView.setOnClickListener(v -> {
                    Bundle result = new Bundle();
                    result.putString("selected_origin", originName);
                    getParentFragmentManager().setFragmentResult("origin_request", result);
                    getParentFragmentManager().popBackStack();
                });

                containerOrigins.addView(itemView);
            }
        }

        if (layoutSubheader != null) {
            layoutSubheader.setVisibility(hasMatches ? View.VISIBLE : View.GONE);
        }
        if (layoutNoResults != null) {
            layoutNoResults.setVisibility(hasMatches ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Ẩn thanh BottomNavigationView khi xem tìm kiếm nơi khởi hành
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
        // Hiện lại thanh BottomNavigationView khi thoát
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.bottomNavigation);
            if (nav != null) {
                nav.setVisibility(View.VISIBLE);
            }
        }
    }
}
