package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment đại diện cho Trang chủ.
 * Quản lý giao diện tìm kiếm và hiển thị danh sách các tour ưu đãi.
 */
public class Home extends Fragment {

    private int selectedDay = 23;
    private int selectedMonth = java.util.Calendar.MAY; // Tháng 5 (0-indexed)
    private int selectedYear = 2026;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp giao diện XML home vào Fragment
        View view = inflater.inflate(R.layout.home, container, false);

        // Lấy các view tìm kiếm địa điểm
        View layoutDestination = view.findViewById(R.id.layoutDestination);
        EditText etDestination = view.findViewById(R.id.etDestination);

        // Thiết lập sự kiện click để mở màn hình tìm kiếm địa điểm
        View.OnClickListener openSearchListener = v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame, new SearchDestination())
                        .addToBackStack(null)
                        .commit();
            }
        };

        if (layoutDestination != null) {
            layoutDestination.setOnClickListener(openSearchListener);
        }
        if (etDestination != null) {
            etDestination.setOnClickListener(openSearchListener);
        }

        // Đăng ký nhận kết quả từ màn hình chọn địa điểm
        getParentFragmentManager().setFragmentResultListener("destination_request", this, (requestKey, result) -> {
            String selectedDest = result.getString("selected_destination");
            if (selectedDest != null && etDestination != null) {
                etDestination.setText(selectedDest);
            }
        });

        // Lấy các view chọn ngày khởi hành
        View layoutDepartureDate = view.findViewById(R.id.layoutDepartureDate);
        TextView tvDepartureDate = view.findViewById(R.id.tvDepartureDate);

        // Đăng ký nhận kết quả chọn ngày từ Bottom Sheet
        getParentFragmentManager().setFragmentResultListener("date_request", this, (requestKey, result) -> {
            selectedDay = result.getInt("day");
            selectedMonth = result.getInt("month");
            selectedYear = result.getInt("year");

            if (tvDepartureDate != null) {
                String formattedDate = String.format("%02d tháng %02d", selectedDay, selectedMonth + 1);
                tvDepartureDate.setText(formattedDate);
            }
        });

        // Thiết lập sự kiện click mở Lịch chọn ngày khởi hành
        if (layoutDepartureDate != null) {
            layoutDepartureDate.setOnClickListener(v -> {
                CalendarBottomSheet bottomSheet = CalendarBottomSheet.newInstance(selectedDay, selectedMonth, selectedYear);
                bottomSheet.show(getParentFragmentManager(), "CalendarBottomSheet");
            });
        }

        // Lấy các view chọn nơi khởi hành
        View layoutOriginCity = view.findViewById(R.id.layoutOriginCity);
        TextView tvOriginCity = view.findViewById(R.id.tvOriginCity);

        // Đăng ký nhận kết quả chọn nơi khởi hành
        getParentFragmentManager().setFragmentResultListener("origin_request", this, (requestKey, result) -> {
            String selectedOrigin = result.getString("selected_origin");
            if (selectedOrigin != null && tvOriginCity != null) {
                tvOriginCity.setText(selectedOrigin);
            }
        });

        // Thiết lập sự kiện click mở bộ lọc chọn nơi khởi hành
        if (layoutOriginCity != null) {
            layoutOriginCity.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.contentFrame, new SearchOrigin())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        // Lấy các thẻ tour ưu đãi
        View cardTaiwan = view.findViewById(R.id.cardTaiwanTour);
        View cardSingapore = view.findViewById(R.id.cardSingaporeTour);

        // Lấy các thẻ tour miền Bắc
        View cardSapa = view.findViewById(R.id.cardSapaTour);
        View cardHalong = view.findViewById(R.id.cardHalongTour);

        // Lấy các thẻ tour miền Trung
        View cardDanang = view.findViewById(R.id.cardDanangTour);
        View cardNhatrang = view.findViewById(R.id.cardNhatrangTour);

        // Lấy các thẻ tour miền Nam
        View cardPhuquoc = view.findViewById(R.id.cardPhuquocTour);
        View cardMientay = view.findViewById(R.id.cardMientayTour);

        // Thiết lập sự kiện click chuyển sang màn hình chi tiết tour
        if (cardTaiwan != null) {
            cardTaiwan.setOnClickListener(v -> openDetail("taiwan"));
        }
        if (cardSingapore != null) {
            cardSingapore.setOnClickListener(v -> openDetail("singapore"));
        }

        // Tour miền Bắc
        if (cardSapa != null) {
            cardSapa.setOnClickListener(v -> openDetail("sapa"));
        }
        if (cardHalong != null) {
            cardHalong.setOnClickListener(v -> openDetail("halong"));
        }

        // Tour miền Trung
        if (cardDanang != null) {
            cardDanang.setOnClickListener(v -> openDetail("danang"));
        }
        if (cardNhatrang != null) {
            cardNhatrang.setOnClickListener(v -> openDetail("nhatrang"));
        }

        // Tour miền Nam
        if (cardPhuquoc != null) {
            cardPhuquoc.setOnClickListener(v -> openDetail("phuquoc"));
        }
        if (cardMientay != null) {
            cardMientay.setOnClickListener(v -> openDetail("mientay"));
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
