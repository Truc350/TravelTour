package com.example.myapplication;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị tab "Chuyến đi" (My Trips) gồm:
 * - Các chuyến đi sắp tới (Upcoming): hiển thị vé tàu giống screenshot với thanh chọn ngày ngang.
 * - Lịch sử chuyến đi (History): hiển thị các chuyến đi đã hoàn thành trong quá khứ.
 */
public class MyTripsFragment extends Fragment {

    // Views
    private LinearLayout tabUpcoming, tabHistory;
    private TextView tvTabUpcomingText, tvTabHistoryText;
    private View tabUpcomingIndicator, tabHistoryIndicator;
    private View layoutDateRibbonContainer;
    private LinearLayout layoutDateTabsContainer;
    private RecyclerView rvTickets;
    private View layoutNoTrips;
    private TextView tvEmptyTitle, tvEmptySubtitle;

    // Data lists
    public static List<BookedTripAdapter.TripItem> additionalTrips = new ArrayList<>();
    private List<BookedTripAdapter.TripItem> allTrips = new ArrayList<>();
    private List<BookedTripAdapter.TripItem> displayedTrips = new ArrayList<>();
    private BookedTripAdapter adapter;

    // State
    private boolean isHistoryMode = false;
    private String selectedDate = "09/09"; // Mặc định khớp screenshot

    // Cấu trúc ngày cho thanh cuộn ngang
    private static class DateTab {
        String label; // ví dụ "CN-07/09"
        String filterVal; // ví dụ "07/09"

        DateTab(String label, String filterVal) {
            this.label = label;
            this.filterVal = filterVal;
        }
    }

    private List<DateTab> dateTabs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_trips, container, false);

        initViews(view);
        initData();
        setupTabs();
        setupDateRibbon();
        setupRecyclerView();

        // Mặc định hiển thị tab Sắp tới, ngày 09/09
        selectTab(false);

        return view;
    }

    private void initViews(View view) {
        tabUpcoming = view.findViewById(R.id.tabUpcoming);
        tabHistory = view.findViewById(R.id.tabHistory);
        tvTabUpcomingText = view.findViewById(R.id.tvTabUpcomingText);
        tvTabHistoryText = view.findViewById(R.id.tvTabHistoryText);
        tabUpcomingIndicator = view.findViewById(R.id.tabUpcomingIndicator);
        tabHistoryIndicator = view.findViewById(R.id.tabHistoryIndicator);
        
        layoutDateRibbonContainer = view.findViewById(R.id.layoutDateRibbonContainer);
        layoutDateTabsContainer = view.findViewById(R.id.layoutDateTabsContainer);
        rvTickets = view.findViewById(R.id.rvTickets);
        layoutNoTrips = view.findViewById(R.id.layoutNoTrips);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = view.findViewById(R.id.tvEmptySubtitle);

        ImageView btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }
    }

    private void initData() {
        // 1. Dữ liệu các Ngày cho thanh cuộn ngang (Khớp thiết kế)
        dateTabs.clear();
        dateTabs.add(new DateTab("CN-07/09", "07/09"));
        dateTabs.add(new DateTab("T2-08/09", "08/09"));
        dateTabs.add(new DateTab("T3-09/09", "09/09")); // Ngày active mặc định
        dateTabs.add(new DateTab("T4-10/09", "10/09"));
        dateTabs.add(new DateTab("T5-11/09", "11/09"));
        dateTabs.add(new DateTab("T6-12/09", "12/09"));
        dateTabs.add(new DateTab("T7-13/09", "13/09"));

        // 2. Dữ liệu Chuyến đi mẫu phong phú
        allTrips.clear();
        
        // --- CHUYẾN ĐI SẮP TỚI (isHistory = false) ---
        // Ngày 09/09
        allTrips.add(new BookedTripAdapter.TripItem(
                "SAPA09",
                "Tour Sapa 3N2Đ: Hà Nội - Bản Cát Cát - Đỉnh Fansipan",
                "Sắp đi",
                "07:00",
                "13:00",
                "Hà Nội",
                "Sapa",
                "3 ngày 2 đêm",
                "3.290.000 đ",
                "09/09",
                false,
                "sapa"
        ));
        allTrips.add(new BookedTripAdapter.TripItem(
                "TAIWAN09",
                "Tour Đài Loan 5N4Đ: HCM - Cao Hùng - Đài Bắc",
                "Sắp đi",
                "08:30",
                "13:00",
                "TP. Hồ Chí Minh",
                "Cao Hùng (Taiwan)",
                "5 ngày 4 đêm",
                "14.390.000 đ",
                "09/09",
                false,
                "taiwan"
        ));

        // Ngày 08/09
        allTrips.add(new BookedTripAdapter.TripItem(
                "DANANG08",
                "Tour Đà Nẵng - Hội An - Bà Nà Hills 4N3Đ Trọn Gói",
                "Chờ xác nhận",
                "12:00",
                "13:20",
                "Hà Nội",
                "Đà Nẵng",
                "4 ngày 3 đêm",
                "4.890.000 đ",
                "08/09",
                false,
                "danang"
        ));

        // Ngày 10/09
        allTrips.add(new BookedTripAdapter.TripItem(
                "SING10",
                "Tour Singapore - Malaysia 5N4Đ Trọn Gói Cao Cấp",
                "Sắp đi",
                "10:00",
                "13:30",
                "TP. Hồ Chí Minh",
                "Singapore",
                "5 ngày 4 đêm",
                "12.890.000 đ",
                "10/09",
                false,
                "singapore"
        ));

        // --- LỊCH SỬ CHUYẾN ĐI ĐÃ ĐI (isHistory = true) ---
        allTrips.add(new BookedTripAdapter.TripItem(
                "PHUQUOC25",
                "Tour Phú Quốc 3N2Đ: Khám Phá Địa Trung Hải",
                "Đã đi",
                "14:00",
                "15:00",
                "TP. Hồ Chí Minh",
                "Phú Quốc",
                "3 ngày 2 đêm",
                "4.590.000 đ",
                "25/08",
                true,
                "phuquoc"
        ));
        allTrips.add(new BookedTripAdapter.TripItem(
                "HALONG15",
                "Tour Vịnh Hạ Long 2N1Đ: Nghỉ Dưỡng Du Thuyền 5 Sao",
                "Đã đi",
                "08:00",
                "11:30",
                "Hà Nội",
                "Vịnh Hạ Long",
                "2 ngày 1 đêm",
                "2.590.000 đ",
                "15/08",
                true,
                "halong"
        ));
        allTrips.add(new BookedTripAdapter.TripItem(
                "SAPATRIP",
                "Tour Sapa 3N2Đ: Chinh Phục Đỉnh Fansipan",
                "Đã hoàn thành",
                "07:00",
                "13:00",
                "Hà Nội (Mỹ Đình)",
                "Sapa (Trung tâm)",
                "3 ngày 2 đêm",
                "3.290.000 đ",
                "15/08",
                true,
                "sapa"
        ));
        allTrips.addAll(0, additionalTrips);
    }

    private void setupTabs() {
        tabUpcoming.setOnClickListener(v -> selectTab(false));
        tabHistory.setOnClickListener(v -> selectTab(true));
    }

    private void selectTab(boolean isHistory) {
        this.isHistoryMode = isHistory;

        if (isHistory) {
            // Tab Lịch sử Active
            tvTabHistoryText.setTextColor(Color.parseColor("#185FA5"));
            tvTabHistoryText.setTypeface(null, Typeface.BOLD);
            tabHistoryIndicator.setVisibility(View.VISIBLE);

            tvTabUpcomingText.setTextColor(Color.parseColor("#718096"));
            tvTabUpcomingText.setTypeface(null, Typeface.NORMAL);
            tabUpcomingIndicator.setVisibility(View.INVISIBLE);

            // Ẩn thanh cuộn ngang ngày
            layoutDateRibbonContainer.setVisibility(View.GONE);
        } else {
            // Tab Sắp tới Active
            tvTabUpcomingText.setTextColor(Color.parseColor("#185FA5"));
            tvTabUpcomingText.setTypeface(null, Typeface.BOLD);
            tabUpcomingIndicator.setVisibility(View.VISIBLE);

            tvTabHistoryText.setTextColor(Color.parseColor("#718096"));
            tvTabHistoryText.setTypeface(null, Typeface.NORMAL);
            tabHistoryIndicator.setVisibility(View.INVISIBLE);

            // Hiện thanh cuộn ngang ngày
            layoutDateRibbonContainer.setVisibility(View.VISIBLE);
        }

        filterAndDisplayTrips();
    }

    private void setupDateRibbon() {
        if (getContext() == null) return;
        layoutDateTabsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < dateTabs.size(); i++) {
            final DateTab tab = dateTabs.get(i);
            View tabView = inflater.inflate(R.layout.item_date_tab, layoutDateTabsContainer, false);

            TextView tvDateText = tabView.findViewById(R.id.tvDateText);
            View indicator = tabView.findViewById(R.id.indicator);

            tvDateText.setText(tab.label);

            // Cập nhật trạng thái hiển thị active/inactive ban đầu
            boolean isActive = tab.filterVal.equals(selectedDate);
            updateDateTabUI(tvDateText, indicator, isActive);

            tabView.setOnClickListener(v -> {
                selectedDate = tab.filterVal;
                // Cập nhật giao diện toàn bộ các tab ngày
                for (int j = 0; j < layoutDateTabsContainer.getChildCount(); j++) {
                    View child = layoutDateTabsContainer.getChildAt(j);
                    TextView childText = child.findViewById(R.id.tvDateText);
                    View childIndicator = child.findViewById(R.id.indicator);
                    boolean childActive = dateTabs.get(j).filterVal.equals(selectedDate);
                    updateDateTabUI(childText, childIndicator, childActive);
                }
                // Lọc lại danh sách
                filterAndDisplayTrips();
            });

            layoutDateTabsContainer.addView(tabView);
        }
    }

    private void updateDateTabUI(TextView textView, View indicator, boolean isActive) {
        if (isActive) {
            textView.setTextColor(Color.parseColor("#00B4D8")); // Màu cyan active
            textView.setTypeface(null, Typeface.BOLD);
            indicator.setVisibility(View.VISIBLE);
        } else {
            textView.setTextColor(Color.parseColor("#718096")); // Màu xám inactive
            textView.setTypeface(null, Typeface.NORMAL);
            indicator.setVisibility(View.INVISIBLE);
        }
    }

    private void setupRecyclerView() {
        rvTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new BookedTripAdapter(getContext(), displayedTrips, item -> {
            // Xử lý khi nhấn vào vé tàu hỏa hoặc chuyến đi
            if (item.isHistory) {
                Toast.makeText(getContext(), "Đặt lại chuyến " + item.trainName, Toast.LENGTH_SHORT).show();
            } else {
                // Hiển thị một vé điện tử giả lập siêu đẹp cho chuyến đi sắp tới!
                showTicketDialog(item);
            }
        });
        rvTickets.setAdapter(adapter);
    }

    private void filterAndDisplayTrips() {
        displayedTrips.clear();

        if (isHistoryMode) {
            // Lấy tất cả lịch sử chuyến đi
            for (BookedTripAdapter.TripItem item : allTrips) {
                if (item.isHistory) {
                    displayedTrips.add(item);
                }
            }
        } else {
            // Lọc các chuyến đi sắp tới theo ngày đang chọn
            for (BookedTripAdapter.TripItem item : allTrips) {
                if (!item.isHistory && item.date.equals(selectedDate)) {
                    displayedTrips.add(item);
                }
            }
        }

        // Đổ dữ liệu vào Adapter
        adapter.notifyDataSetChanged();

        // Xử lý hiển thị Placeholder trống nếu không tìm thấy dữ liệu
        if (displayedTrips.isEmpty()) {
            layoutNoTrips.setVisibility(View.VISIBLE);
            rvTickets.setVisibility(View.GONE);

            if (isHistoryMode) {
                tvEmptyTitle.setText("Lịch sử trống");
                tvEmptySubtitle.setText("Bạn chưa có chuyến đi nào hoàn thành trong lịch sử.");
            } else {
                tvEmptyTitle.setText("Không có chuyến đi");
                tvEmptySubtitle.setText("Bạn không có chuyến đi nào đã đặt vào ngày " + selectedDate + ".");
            }
        } else {
            layoutNoTrips.setVisibility(View.GONE);
            rvTickets.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hiển thị Vé Điện Tử thông minh cho người dùng khi click "Chọn" hoặc xem chi tiết chuyến đi
     */
    private void showTicketDialog(BookedTripAdapter.TripItem item) {
        if (getActivity() == null) return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_electronic_ticket, null);

        // Nạp và gán dữ liệu chi tiết vé
        TextView tvDlgTrainName = dialogView.findViewById(R.id.tvDlgTrainName);
        TextView tvDlgDepTime = dialogView.findViewById(R.id.tvDlgDepTime);
        TextView tvDlgDepStation = dialogView.findViewById(R.id.tvDlgDepStation);
        TextView tvDlgDuration = dialogView.findViewById(R.id.tvDlgDuration);
        TextView tvDlgArrTime = dialogView.findViewById(R.id.tvDlgArrTime);
        TextView tvDlgArrStation = dialogView.findViewById(R.id.tvDlgArrStation);
        TextView tvDlgDate = dialogView.findViewById(R.id.tvDlgDate);
        TextView tvDlgSeat = dialogView.findViewById(R.id.tvDlgSeat);
        TextView tvDlgPrice = dialogView.findViewById(R.id.tvDlgPrice);
        TextView tvDlgTicketCode = dialogView.findViewById(R.id.tvDlgTicketCode);

        if (tvDlgTrainName != null) tvDlgTrainName.setText(item.trainName);
        if (tvDlgDepTime != null) tvDlgDepTime.setText(item.depTime);
        if (tvDlgDepStation != null) tvDlgDepStation.setText(item.depStation);
        if (tvDlgDuration != null) tvDlgDuration.setText(item.duration);
        if (tvDlgArrTime != null) tvDlgArrTime.setText(item.arrTime);
        if (tvDlgArrStation != null) tvDlgArrStation.setText(item.arrStation);
        if (tvDlgDate != null) tvDlgDate.setText(item.date + "/2026");
        if (tvDlgPrice != null) tvDlgPrice.setText(item.price);
        
        // Mock seat and ticket code based on ID
        if (tvDlgSeat != null) {
            tvDlgSeat.setText(item.tourType.toUpperCase() + "-" + Math.abs(item.id.hashCode() % 1000));
        }
        String ticketCode = "TKT-2026-" + item.date.replace("/", "") + "-" + item.tourType.toUpperCase();
        if (tvDlgTicketCode != null) {
            tvDlgTicketCode.setText(ticketCode);
        }

        builder.setView(dialogView)
               .setPositiveButton("Đóng", (dialog, id) -> dialog.dismiss());
        
        android.app.AlertDialog alertDialog = builder.create();

        ImageView imgQrCode = dialogView.findViewById(R.id.imgDlgQrCode);
        View btnViewTour = dialogView.findViewById(R.id.btnDlgViewTour);

        // Sinh QR Code động dựa trên thông tin vé chi tiết
        if (imgQrCode != null) {
            String qrPayload = "VÉ TOUR ĐIỆN TỬ\n" +
                    "Mã vé: " + ticketCode + "\n" +
                    "Tour: " + item.trainName + "\n" +
                    "Khởi hành: " + item.depTime + " " + item.date + "/2026\n" +
                    "Hành trình: " + item.depStation + " -> " + item.arrStation + "\n" +
                    "Thời gian: " + item.duration + "\n" +
                    "Giá vé: " + item.price;
            android.graphics.Bitmap qrBitmap = QrCodeGenerator.generateQrCode(qrPayload, 400, 400);
            if (qrBitmap != null) {
                imgQrCode.setImageBitmap(qrBitmap);
            }
        }

        View.OnClickListener openTourDetail = v -> {
            alertDialog.dismiss();
            
            DetailTour detailFragment = new DetailTour();
            Bundle args = new Bundle();
            args.putString("tour_type", item.tourType);
            detailFragment.setArguments(args);
            
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, detailFragment)
                    .addToBackStack(null)
                    .commit();
        };

        if (imgQrCode != null) {
            imgQrCode.setOnClickListener(openTourDetail);
        }
        if (btnViewTour != null) {
            btnViewTour.setOnClickListener(openTourDetail);
        }

        alertDialog.show();
    }
}
