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
        // Ngày 09/09 (Trùng y hệt screenshot của user)
        allTrips.add(new BookedTripAdapter.TripItem(
                "HP1",
                "Tàu hoa phượng đỏ - HP1",
                "Còn 99 chỗ",
                "06:00",
                "08:25",
                "Ga Hà Nội",
                "Ga Hải Phòng",
                "2g25p",
                "109.000 đ",
                "09/09",
                false
        ));
        allTrips.add(new BookedTripAdapter.TripItem(
                "LP3",
                "Tàu hoa phượng đỏ - LP3",
                "Còn 99 chỗ",
                "09:20",
                "12:00",
                "Ga Hà Nội",
                "Ga Hải Phòng",
                "2g40p",
                "109.000 đ",
                "09/09",
                false
        ));

        // Ngày 08/09
        allTrips.add(new BookedTripAdapter.TripItem(
                "HP3",
                "Tàu hoa phượng đỏ - HP3",
                "Còn 45 chỗ",
                "14:00",
                "16:15",
                "Ga Hà Nội",
                "Ga Hải Phòng",
                "2g15p",
                "109.000 đ",
                "08/09",
                false
        ));

        // Ngày 10/09
        allTrips.add(new BookedTripAdapter.TripItem(
                "LP5",
                "Tàu hoa phượng đỏ - LP5",
                "Còn 12 chỗ",
                "18:15",
                "20:50",
                "Ga Hà Nội",
                "Ga Hải Phòng",
                "2g35p",
                "109.000 đ",
                "10/09",
                false
        ));

        // --- LỊCH SỬ CHUYẾN ĐI ĐÃ ĐI (isHistory = true) ---
        allTrips.add(new BookedTripAdapter.TripItem(
                "LP1",
                "Tàu hoa phượng đỏ - LP1",
                "Đã đi",
                "06:00",
                "08:25",
                "Ga Hà Nội",
                "Ga Hải Phòng",
                "2g25p",
                "109.000 đ",
                "01/09",
                true
        ));
        allTrips.add(new BookedTripAdapter.TripItem(
                "HP5",
                "Tàu hoa phượng đỏ - HP5",
                "Đã đi",
                "15:00",
                "17:30",
                "Ga Hà Nội",
                "Ga Hải Phòng",
                "2g30p",
                "109.000 đ",
                "25/08",
                true
        ));
        allTrips.add(new BookedTripAdapter.TripItem(
                "SAPATRIP",
                "Chuyến đi Sapa - Fansipan 3N2Đ",
                "Đã hoàn thành",
                "07:00",
                "13:00",
                "Hà Nội (Mỹ Đình)",
                "Sapa (Trung tâm)",
                "6g00p",
                "3.290.000 đ",
                "15/08",
                true
        ));
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
            int coach = (item.id.hashCode() % 6) + 1;
            int seat = (item.id.hashCode() % 64) + 1;
            tvDlgSeat.setText("Toa " + Math.abs(coach) + " / Ghế " + Math.abs(seat));
        }
        if (tvDlgTicketCode != null) {
            tvDlgTicketCode.setText("TKT-2026-" + item.date.replace("/", "") + "-" + item.id);
        }

        // Đổi nút bấm dialog thành đóng hoặc dùng thiết lập mặc định của AlertDialog
        builder.setView(dialogView)
               .setPositiveButton("Đóng", (dialog, id) -> dialog.dismiss())
               .show();
    }
}
