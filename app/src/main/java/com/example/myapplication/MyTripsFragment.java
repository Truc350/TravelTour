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

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;
import com.example.myapplication.data.model.BookingResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        // 1. Dữ liệu các Ngày cho thanh cuộn ngang được tạo động xung quanh ngày hiện tại (2 ngày trước và 4 ngày sau)
        dateTabs.clear();

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -2); // bắt đầu từ 2 ngày trước

        java.text.SimpleDateFormat filterFmt = new java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            String dayOfWeekStr;
            switch (dayOfWeek) {
                case java.util.Calendar.SUNDAY:
                    dayOfWeekStr = "CN";
                    break;
                default:
                    dayOfWeekStr = "T" + dayOfWeek;
                    break;
            }

            String filterVal = filterFmt.format(cal.getTime());
            String label = dayOfWeekStr + "-" + filterVal;

            dateTabs.add(new DateTab(label, filterVal));

            // Đặt ngày hôm nay làm mặc định (ngày thứ 3 trong danh sách, tức i = 2)
            if (i == 2) {
                selectedDate = filterVal;
            }

            cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }

        allTrips.clear();
        loadBookingsFromServer();
    }

    private void loadBookingsFromServer() {
        if (getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("current_user_id", 1); // default user 1

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBookings().enqueue(new Callback<List<BookingResponse>>() {
            @Override
            public void onResponse(Call<List<BookingResponse>> call, Response<List<BookingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingResponse> bookings = response.body();
                    allTrips.clear();

                    // Thêm additionalTrips local trước
                    allTrips.addAll(additionalTrips);

                    for (BookingResponse b : bookings) {
                        // Lọc chỉ lấy booking của user hiện tại
                        if (b.user == currentUserId) {
                            String code = "DL0" + b.id;
                            String trainName = "Tour Du Lịch";
                            String tourType = "tour";
                            String depStation = "Hồ Chí Minh";
                            String arrStation = "Điểm đến";
                            
                            if (b.departureDetail != null) {
                                if (b.departureDetail.tourDetail != null) {
                                    trainName = b.departureDetail.tourDetail.getTitle();
                                    tourType = b.departureDetail.tourDetail.getCode();
                                    arrStation = b.departureDetail.tourDetail.getTitle();
                                }
                                if (b.departureDetail.departureDate != null) {
                                    depStation = "Khởi hành: " + b.departureDetail.departureDate;
                                }
                            }

                            String statusBadge = "Chờ duyệt";
                            if ("CONFIRMED".equalsIgnoreCase(b.status)) {
                                statusBadge = "Đã thanh toán";
                            } else if ("CANCELLED".equalsIgnoreCase(b.status)) {
                                statusBadge = "Đã hủy";
                            }

                            String priceFormatted = formatVndPrice((long) b.totalPrice);

                            BookedTripAdapter.TripItem item = new BookedTripAdapter.TripItem(
                                    code,
                                    trainName,
                                    statusBadge,
                                    b.departureHour != null ? b.departureHour : "08:00",
                                    "Dự kiến",
                                    depStation,
                                    arrStation,
                                    "Trọn gói",
                                    priceFormatted,
                                    b.bookingDate != null ? b.bookingDate : "09/09",
                                    "CANCELLED".equalsIgnoreCase(b.status), // coi như history nếu đã hủy hoặc completed
                                    tourType
                            );
                            allTrips.add(item);
                        }
                    }

                    filterAndDisplayTrips();
                } else {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu chuyến đi từ máy chủ!", Toast.LENGTH_SHORT).show();
                    // Fallback to local additionalTrips
                    allTrips.clear();
                    allTrips.addAll(additionalTrips);
                    filterAndDisplayTrips();
                }
            }

            @Override
            public void onFailure(Call<List<BookingResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Không thể kết nối máy chủ để tải chuyến đi!", Toast.LENGTH_SHORT).show();
                // Fallback to local additionalTrips
                allTrips.clear();
                allTrips.addAll(additionalTrips);
                filterAndDisplayTrips();
            }
        });
    }

    private String formatVndPrice(long price) {
        String raw = String.valueOf(price);
        StringBuilder sb = new StringBuilder();
        int len = raw.length();
        for (int i = 0; i < len; i++) {
            sb.append(raw.charAt(i));
            int remaining = len - i - 1;
            if (remaining > 0 && remaining % 3 == 0) {
                sb.append('.');
            }
        }
        return sb + " đ";
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
