package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment đại diện cho Trang chủ.
 * Quản lý giao diện tìm kiếm và hiển thị danh sách các tour ưu đãi.
 */
public class Home extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp giao diện XML home vào Fragment
        View view = inflater.inflate(R.layout.home, container, false);

        // Lấy các thẻ tour ưu đãi
        View cardTaiwan = view.findViewById(R.id.cardTaiwanTour);
        View cardSingapore = view.findViewById(R.id.cardSingaporeTour);

        // Thiết lập sự kiện click chuyển sang màn hình chi tiết tour
        if (cardTaiwan != null) {
            cardTaiwan.setOnClickListener(v -> openDetail("taiwan"));
        }
        if (cardSingapore != null) {
            cardSingapore.setOnClickListener(v -> openDetail("singapore"));
        }

        return view;
    }

    private void openDetail(String tourType) {
        DetailTour detailFragment = new DetailTour();
        Bundle args = new Bundle();
        args.putString("tour_type", tourType);
        detailFragment.setArguments(args);

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, detailFragment)
                    .addToBackStack(null) // Cho phép quay lại bằng phím Back
                    .commit();
        }
    }
}
