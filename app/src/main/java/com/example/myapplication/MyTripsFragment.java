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
    private String selectedDate = "ALL"; // Mặc định hiển thị Tất cả chuyến đi

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

        // Thêm mục "Tất cả" trước tiên để xem tất cả chuyến đi
        dateTabs.add(new DateTab("Tất cả", "ALL"));

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

            cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }

        allTrips.clear();
        loadBookingsFromServer();
    }

    private void loadBookingsFromServer() {
        if (getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("current_user_id", -1); // default to -1 if guest

        if (currentUserId == -1) {
            allTrips.clear();
            displayedTrips.clear();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            layoutNoTrips.setVisibility(View.VISIBLE);
            rvTickets.setVisibility(View.GONE);
            tvEmptyTitle.setText("Chưa đăng nhập");
            tvEmptySubtitle.setText("Vui lòng đăng nhập để xem thông tin chuyến đi của bạn.");
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getBookings().enqueue(new Callback<List<BookingResponse>>() {
            @Override
            public void onResponse(Call<List<BookingResponse>> call, Response<List<BookingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingResponse> bookings = response.body();
                    allTrips.clear();

                    // Thêm additionalTrips local trước (lọc theo user đăng nhập)
                    for (BookedTripAdapter.TripItem localItem : additionalTrips) {
                        if (localItem.userId == currentUserId) {
                            allTrips.add(localItem);
                        }
                    }

                    for (BookingResponse b : bookings) {
                        // Lọc chỉ lấy booking của user hiện tại
                        if (b.user == currentUserId) {
                            String code = "DL0" + b.id;
                            String trainName = "Tour Du Lịch";
                            String tourType = "tour";
                            String depStation = "Thời gian đi";
                            String arrStation = "Thời gian đến";
                            
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
                            } else if ("COMPLETED".equalsIgnoreCase(b.status)) {
                                statusBadge = "Đã hoàn thành";
                            }

                            String priceFormatted = formatVndPrice((long) b.totalPrice);

                            String depDateForTab = "09/09";
                            if (b.departureDetail != null && b.departureDetail.departureDate != null) {
                                depDateForTab = formatDepartureDateToDdMm(b.departureDetail.departureDate);
                            } else if (b.bookingDate != null) {
                                depDateForTab = formatDepartureDateToDdMm(b.bookingDate);
                            }

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
                                    depDateForTab,
                                    "CANCELLED".equalsIgnoreCase(b.status) || "COMPLETED".equalsIgnoreCase(b.status), // coi như history nếu đã hủy hoặc completed
                                    tourType
                            );
                            if (b.departureDetail != null) {
                                if (b.departureDetail.tourDetail != null) {
                                    item.tourId = b.departureDetail.tourDetail.getId();
                                } else {
                                    item.tourId = b.departureDetail.tour;
                                }
                            }
                            item.userId = currentUserId;
                            allTrips.add(item);
                        }
                    }

                    filterAndDisplayTrips();
                } else {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu chuyến đi từ máy chủ!", Toast.LENGTH_SHORT).show();
                    // Fallback to local additionalTrips
                    allTrips.clear();
                    for (BookedTripAdapter.TripItem localItem : additionalTrips) {
                        if (localItem.userId == currentUserId) {
                            allTrips.add(localItem);
                        }
                    }
                    filterAndDisplayTrips();
                }
            }

            @Override
            public void onFailure(Call<List<BookingResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Không thể kết nối máy chủ để tải chuyến đi!", Toast.LENGTH_SHORT).show();
                // Fallback to local additionalTrips
                allTrips.clear();
                for (BookedTripAdapter.TripItem localItem : additionalTrips) {
                    if (localItem.userId == currentUserId) {
                        allTrips.add(localItem);
                    }
                }
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
        return sb + " d";
    }

    /** Bỏ dấu tiếng Việt để QR code chỉ chứa ASCII thuần, tránh lỗi font khi quét */
    private String removeAccents(String text) {
        if (text == null) return "";
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                         .replace("đ", "d").replace("Đ", "D");
    }

    private String formatDepartureDateToDdMm(String departureDateStr) {
        if (departureDateStr == null) return "09/09";
        try {
            java.text.SimpleDateFormat fromServerFmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date date = fromServerFmt.parse(departureDateStr);
            java.text.SimpleDateFormat toTabFmt = new java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault());
            return toTabFmt.format(date);
        } catch (Exception e) {
            try {
                java.text.SimpleDateFormat fromServerFmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Date date = fromServerFmt.parse(departureDateStr);
                java.text.SimpleDateFormat toTabFmt = new java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault());
                return toTabFmt.format(date);
            } catch (Exception ex) {
                // Nếu là định dạng dd/MM/yyyy hoặc tương tự thì thử tìm cách parse
                if (departureDateStr.contains("/")) {
                    String[] parts = departureDateStr.split("/");
                    if (parts.length >= 2) {
                        return parts[0] + "/" + parts[1];
                    }
                }
                return departureDateStr;
            }
        }
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
            // Hiển thị một vé điện tử giả lập cho chuyến đi!
            showTicketDialog(item);
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
            // Lọc các chuyến đi sắp tới theo ngày đang chọn hoặc hiển thị tất cả
            for (BookedTripAdapter.TripItem item : allTrips) {
                if (!item.isHistory && ("ALL".equals(selectedDate) || item.date.equals(selectedDate))) {
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
                if ("ALL".equals(selectedDate)) {
                    tvEmptySubtitle.setText("Bạn chưa đặt chuyến đi nào sắp tới.");
                } else {
                    tvEmptySubtitle.setText("Bạn không có chuyến đi nào đã đặt vào ngày " + selectedDate + ".");
                }
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
        TextView tvDlgHeaderSubtitle = dialogView.findViewById(R.id.tvDlgHeaderSubtitle);

        if (tvDlgTrainName != null) tvDlgTrainName.setText(item.trainName);
        if (tvDlgDepTime != null) tvDlgDepTime.setText(item.depTime);
        if (tvDlgDepStation != null) tvDlgDepStation.setText(item.depStation);
        if (tvDlgDuration != null) tvDlgDuration.setText(item.duration);
        if (tvDlgArrTime != null) tvDlgArrTime.setText(item.arrTime);
        if (tvDlgArrStation != null) tvDlgArrStation.setText(item.arrStation);
        if (tvDlgDate != null) tvDlgDate.setText(item.date + "/2026");
        if (tvDlgPrice != null) tvDlgPrice.setText(item.price);
        if (tvDlgHeaderSubtitle != null) {
            tvDlgHeaderSubtitle.setText(item.isHistory ? "Chuyến đi đã hoàn thành" : "Chuyến đi sắp tới");
        }
        
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
        TextView btnDlgAction = dialogView.findViewById(R.id.btnDlgAction);

        if (btnDlgAction != null) {
            btnDlgAction.setVisibility(View.VISIBLE);
            if (item.isHistory) {
                btnDlgAction.setText("ĐÁNH GIÁ TOUR");
                btnDlgAction.setOnClickListener(v -> {
                    alertDialog.dismiss();
                    showRatingDialog(item);
                });
            } else {
                btnDlgAction.setText("XÁC NHẬN CHUYẾN ĐI THÀNH CÔNG");
                btnDlgAction.setOnClickListener(v -> {
                    alertDialog.dismiss();
                    confirmTripSuccess(item);
                });
            }
        }

        // Sinh QR Code - nội dung plain text thông tin vé
        if (imgQrCode != null) {
            String confirmCode = "CFM-" + (100000 + Math.abs(item.id.hashCode() % 900000));
            try {
                if (item.id.startsWith("DL0")) {
                    int bid = Integer.parseInt(item.id.substring(3));
                    confirmCode = "CFM-" + (100000 + bid);
                }
            } catch (Exception ignored) {}

            String depDate = item.date + "/2026";
            if (item.depStation != null && item.depStation.startsWith("Khởi hành: ")) {
                depDate = item.depStation.substring(11);
            }

            String qrPayload =
                    "=== VE DIEN TU TRAVELTOUR ===\n" +
                    "Ma ve: " + item.id + "\n" +
                    "Ma xac nhan: " + confirmCode + "\n" +
                    "Tour: " + removeAccents(item.trainName) + "\n" +
                    "Ngay khoi hanh: " + depDate + "\n" +
                    "Gio khoi hanh: " + item.depTime + "\n" +
                    "Gia ve: " + removeAccents(item.price) + "\n" +
                    "Trang thai: " + removeAccents(item.statusBadge) + "\n" +
                    "============================";

            android.graphics.Bitmap qrBitmap = QrCodeGenerator.generateQrCode(qrPayload, 400, 400);
            if (qrBitmap != null) {
                imgQrCode.setImageBitmap(qrBitmap);
            }
        }


        View.OnClickListener openTourDetail = v -> {
            alertDialog.dismiss();
            
            DetailTour detailFragment = new DetailTour();
            Bundle args = new Bundle();
            // Ưu tiên dùng tourId thực từ server; fallback dùng tour_type cho local items
            if (item.tourId > 0) {
                args.putInt("tour_id", item.tourId);
            } else {
                args.putString("tour_type", item.tourType);
            }
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

    private void confirmTripSuccess(BookedTripAdapter.TripItem item) {
        // 1. Cập nhật offline cục bộ trong additionalTrips (nếu tồn tại)
        for (BookedTripAdapter.TripItem localItem : additionalTrips) {
            if (localItem.id.equals(item.id)) {
                localItem.isHistory = true;
                localItem.statusBadge = "Đã hoàn thành";
                break;
            }
        }

        // 2. Trích xuất ID booking để cập nhật trên Django Backend
        int bookingId = -1;
        try {
            if (item.id.startsWith("DL0")) {
                bookingId = Integer.parseInt(item.id.substring(3));
            }
        } catch (Exception ignored) {}

        if (bookingId != -1) {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            java.util.Map<String, Object> fields = new java.util.HashMap<>();
            fields.put("status", "COMPLETED");

            apiService.patchBooking(bookingId, fields).enqueue(new Callback<BookingResponse>() {
                @Override
                public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                    if (getContext() == null) return;
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Đã cập nhật trạng thái chuyến đi hoàn thành trên Django!", Toast.LENGTH_SHORT).show();
                        // Nạp lại dữ liệu từ máy chủ để đồng bộ hóa
                        loadBookingsFromServer();
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi cập nhật trạng thái lên Django!", Toast.LENGTH_SHORT).show();
                        filterAndDisplayTrips();
                    }
                }

                @Override
                public void onFailure(Call<BookingResponse> call, Throwable t) {
                    if (getContext() == null) return;
                    Toast.makeText(getContext(), "Lỗi kết nối khi cập nhật trạng thái chuyến đi!", Toast.LENGTH_SHORT).show();
                    filterAndDisplayTrips();
                }
            });
        } else {
            // Đối với các chuyến đi local, chỉ cần lọc lại và hiển thị
            filterAndDisplayTrips();
        }
    }

    private void showRatingDialog(BookedTripAdapter.TripItem item) {
        if (getActivity() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("current_user_id", 1);
        int tourId = item.tourId > 0 ? item.tourId : 1;

        // Hiển thị tiến trình kiểm tra
        android.app.ProgressDialog checkProgress = new android.app.ProgressDialog(getContext());
        checkProgress.setMessage("Đang kiểm tra lịch sử đánh giá...");
        checkProgress.setCancelable(false);
        checkProgress.show();

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getReviews().enqueue(new Callback<List<com.example.myapplication.data.model.Review>>() {
            @Override
            public void onResponse(Call<List<com.example.myapplication.data.model.Review>> call, Response<List<com.example.myapplication.data.model.Review>> response) {
                checkProgress.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    boolean alreadyRated = false;
                    for (com.example.myapplication.data.model.Review r : response.body()) {
                        if (r.getUserId() == currentUserId && r.getTourId() == tourId) {
                            alreadyRated = true;
                            break;
                        }
                    }
                    if (alreadyRated) {
                        Toast.makeText(getContext(), "Bạn đã đánh giá tour này rồi! Không thể đánh giá thêm.", Toast.LENGTH_LONG).show();
                    } else {
                        displayRatingDialog(item);
                    }
                } else {
                    displayRatingDialog(item);
                }
            }

            @Override
            public void onFailure(Call<List<com.example.myapplication.data.model.Review>> call, Throwable t) {
                checkProgress.dismiss();
                displayRatingDialog(item);
            }
        });
    }

    private void displayRatingDialog(BookedTripAdapter.TripItem item) {
        if (getActivity() == null) return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        TextView tvRatingTourName = dialogView.findViewById(R.id.tvRatingTourName);
        if (tvRatingTourName != null) {
            tvRatingTourName.setText(item.trainName);
        }

        TextView tvStar1 = dialogView.findViewById(R.id.tvStar1);
        TextView tvStar2 = dialogView.findViewById(R.id.tvStar2);
        TextView tvStar3 = dialogView.findViewById(R.id.tvStar3);
        TextView tvStar4 = dialogView.findViewById(R.id.tvStar4);
        TextView tvStar5 = dialogView.findViewById(R.id.tvStar5);
        
        TextView[] starViews = {tvStar1, tvStar2, tvStar3, tvStar4, tvStar5};
        final int[] currentRating = {0}; // rating state

        for (int i = 0; i < starViews.length; i++) {
            final int starIndex = i;
            if (starViews[i] != null) {
                starViews[i].setOnClickListener(v -> {
                    currentRating[0] = starIndex + 1;
                    for (int j = 0; j < starViews.length; j++) {
                        if (starViews[j] != null) {
                            if (j <= starIndex) {
                                starViews[j].setText("★"); // filled star
                            } else {
                                starViews[j].setText("☆"); // empty star
                            }
                        }
                    }
                });
            }
        }

        android.widget.EditText etRatingComment = dialogView.findViewById(R.id.etRatingComment);

        android.app.AlertDialog ratingDialog = builder.create();

        View btnCancel = dialogView.findViewById(R.id.btnRatingCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> ratingDialog.dismiss());
        }

        View btnSubmit = dialogView.findViewById(R.id.btnRatingSubmit);
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                if (currentRating[0] == 0) {
                    Toast.makeText(getContext(), "Vui lòng chọn số sao đánh giá!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String comment = "";
                if (etRatingComment != null) {
                    comment = etRatingComment.getText().toString().trim();
                }
                ratingDialog.dismiss();
                
                // Hiển thị tiến trình gửi
                android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(getContext());
                progressDialog.setMessage("Đang gửi đánh giá của bạn...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                SharedPreferences prefs = getContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                int currentUserId = prefs.getInt("current_user_id", 1);
                int tourId = item.tourId > 0 ? item.tourId : 1;

                java.util.Map<String, Object> reviewData = new java.util.HashMap<>();
                reviewData.put("tour", tourId);
                reviewData.put("user", currentUserId);
                reviewData.put("rating", currentRating[0]);
                reviewData.put("comment", comment);

                ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                apiService.createReview(reviewData).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Đã gửi đánh giá thành công! Cảm ơn bạn đã phản hồi.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Gửi đánh giá thất bại! Mã lỗi: " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Lỗi kết nối khi gửi đánh giá!", Toast.LENGTH_LONG).show();
                    }
                });
            });
        }

        ratingDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookingsFromServer();
    }
}
