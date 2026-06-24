package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.model.VoucherHelper;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends Fragment {

    private int selectedDay = 23;
    private int selectedMonth = java.util.Calendar.MAY;
    private int selectedYear = 2026;

    private TourAdapter adapterUuDai, adapterMienBac, adapterMienTrung, adapterMienNam;
    private HomeVoucherAdapter voucherAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, container, false);

        // --- Setup search fields (giữ nguyên) ---
        View layoutDestination = view.findViewById(R.id.layoutDestination);
        EditText etDestination = view.findViewById(R.id.etDestination);

        View.OnClickListener openSearchListener = v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame, new SearchDestination())
                        .addToBackStack(null).commit();
            }
        };
        if (layoutDestination != null) layoutDestination.setOnClickListener(openSearchListener);
        if (etDestination != null) etDestination.setOnClickListener(openSearchListener);

        getParentFragmentManager().setFragmentResultListener("destination_request", this, (key, result) -> {
            String dest = result.getString("selected_destination");
            if (dest != null && etDestination != null) etDestination.setText(dest);
        });

        View layoutDepartureDate = view.findViewById(R.id.layoutDepartureDate);
        TextView tvDepartureDate = view.findViewById(R.id.tvDepartureDate);

        getParentFragmentManager().setFragmentResultListener("date_request", this, (key, result) -> {
            selectedDay = result.getInt("day");
            selectedMonth = result.getInt("month");
            selectedYear = result.getInt("year");
            if (tvDepartureDate != null)
                tvDepartureDate.setText(String.format("%02d tháng %02d", selectedDay, selectedMonth + 1));
        });

        if (layoutDepartureDate != null) {
            layoutDepartureDate.setOnClickListener(v -> {
                CalendarBottomSheet sheet = CalendarBottomSheet.newInstance(selectedDay, selectedMonth, selectedYear);
                sheet.show(getParentFragmentManager(), "CalendarBottomSheet");
            });
        }

        View layoutOriginCity = view.findViewById(R.id.layoutOriginCity);
        TextView tvOriginCity = view.findViewById(R.id.tvOriginCity);

        getParentFragmentManager().setFragmentResultListener("origin_request", this, (key, result) -> {
            String origin = result.getString("selected_origin");
            if (origin != null && tvOriginCity != null) tvOriginCity.setText(origin);
        });

        if (layoutOriginCity != null) {
            layoutOriginCity.setOnClickListener(v -> {
                if (getParentFragmentManager() != null)
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.contentFrame, new SearchOrigin())
                            .addToBackStack(null).commit();
            });
        }

        View btnFindTour = view.findViewById(R.id.btnFindTour);
        if (btnFindTour != null) {
            btnFindTour.setOnClickListener(v -> {
                String destination = etDestination != null ? etDestination.getText().toString().trim() : "";
                String origin = tvOriginCity != null ? tvOriginCity.getText().toString().trim() : "";
                SearchResult searchResultFragment = new SearchResult();
                Bundle args = new Bundle();
                args.putString("destination", destination);
                args.putString("origin", origin);
                args.putInt("day", selectedDay);
                args.putInt("month", selectedMonth);
                args.putInt("year", selectedYear);
                searchResultFragment.setArguments(args);
                if (getParentFragmentManager() != null)
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.contentFrame, searchResultFragment)
                            .addToBackStack(null).commit();
            });
        }

        // --- Setup RecyclerViews ---
        RecyclerView rvUuDai = view.findViewById(R.id.rvTourUuDai);
        RecyclerView rvMienBac = view.findViewById(R.id.rvTourMienBac);
        RecyclerView rvMienTrung = view.findViewById(R.id.rvTourMienTrung);
        RecyclerView rvMienNam = view.findViewById(R.id.rvTourMienNam);

        adapterUuDai = new TourAdapter(new ArrayList<>(), this::openDetail);
        adapterMienBac = new TourAdapter(new ArrayList<>(), this::openDetail);
        adapterMienTrung = new TourAdapter(new ArrayList<>(), this::openDetail);
        adapterMienNam = new TourAdapter(new ArrayList<>(), this::openDetail);

        setupRecyclerView(rvUuDai, adapterUuDai);
        setupRecyclerView(rvMienBac, adapterMienBac);
        setupRecyclerView(rvMienTrung, adapterMienTrung);
        setupRecyclerView(rvMienNam, adapterMienNam);

        // --- Setup Voucher Section ---
        RecyclerView rvHomeVouchers = view.findViewById(R.id.rvHomeVouchers);
        android.content.SharedPreferences sessionPrefs = requireContext().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE);
        String currentContact = sessionPrefs.getString("current_user_contact", "");
        if (currentContact.isEmpty()) {
            currentContact = new DatabaseHelper(requireContext()).getLastUserContact();
        }
        final String finalUserContact = currentContact;
        final List<VoucherHelper.AppVoucher> availableVouchers = new ArrayList<>(VoucherHelper.getAvailableVouchers());

        // Build BottomSheet Dialog for all vouchers
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_all_vouchers_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        RecyclerView rvAllVouchers = sheetView.findViewById(R.id.rvAllVouchers);
        rvAllVouchers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        // Shared change listener to update both RecyclerViews when a voucher is saved
        HomeVoucherAdapter.OnVoucherSavedListener voucherSaveListener = new HomeVoucherAdapter.OnVoucherSavedListener() {
            @Override
            public void onVoucherSaved(String code) {
                if (voucherAdapter != null) {
                    voucherAdapter.notifyDataSetChanged();
                }
                if (rvAllVouchers.getAdapter() != null) {
                    rvAllVouchers.getAdapter().notifyDataSetChanged();
                }
            }
        };

        voucherAdapter = new HomeVoucherAdapter(availableVouchers, finalUserContact, voucherSaveListener);
        setupRecyclerView(rvHomeVouchers, voucherAdapter);

        DetailVoucherAdapter detailAdapter = new DetailVoucherAdapter(availableVouchers, finalUserContact, voucherSaveListener);
        rvAllVouchers.setAdapter(detailAdapter);

        sheetView.findViewById(R.id.btnCloseBottomSheet).setOnClickListener(v -> bottomSheetDialog.dismiss());

        View btnAllVouchers = view.findViewById(R.id.btnAllVouchers);
        if (btnAllVouchers != null) {
            btnAllVouchers.setOnClickListener(v -> {
                if (voucherAdapter != null) voucherAdapter.notifyDataSetChanged();
                detailAdapter.notifyDataSetChanged();
                bottomSheetDialog.show();
            });
        }

        loadVouchers(availableVouchers, voucherAdapter, detailAdapter);

        // --- Gọi API ---
        loadTours();

        return view;
    }

    private void setupRecyclerView(RecyclerView rv, RecyclerView.Adapter<?> adapter) {
        if (rv == null) return;
        rv.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);
    }

    private void loadTours() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getTours().enqueue(new Callback<List<Tour>>() {
            @Override
            public void onResponse(Call<List<Tour>> call, Response<List<Tour>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Tour> all = response.body();
                    Log.d("DJANGO_API", "Tổng tour = " + all.size());

                    List<Tour> listUuDai = new ArrayList<>();
                    List<Tour> listMienBac = new ArrayList<>();
                    List<Tour> listMienTrung = new ArrayList<>();
                    List<Tour> listMienNam = new ArrayList<>();

                    for (Tour tour : all) {
                        // Ưu đãi: Giá khuyến mãi nhỏ hơn giá gốc và lớn hơn 0
                        if (tour.getDiscountPrice() > 0 && tour.getDiscountPrice() < tour.getOriginalPrice()) {
                            listUuDai.add(tour);
                        }

                        // Phân loại theo vùng miền dựa vào title
                        String title = tour.getTitle();
                        if (title != null) {
                            String lowerTitle = title.toLowerCase();
                            if (lowerTitle.contains("bắc") || lowerTitle.contains("bac") ||
                                    lowerTitle.contains("sapa") || lowerTitle.contains("hà nội") || lowerTitle.contains("hạ long")) {
                                listMienBac.add(tour);
                            } else if (lowerTitle.contains("trung") || lowerTitle.contains("đà nẵng") ||
                                    lowerTitle.contains("nha trang") || lowerTitle.contains("hội an") || lowerTitle.contains("huế")) {
                                listMienTrung.add(tour);
                            } else if (lowerTitle.contains("nam") || lowerTitle.contains("miền tây") ||
                                    lowerTitle.contains("phú quốc") || lowerTitle.contains("cần thơ") || lowerTitle.contains("hcm")) {
                                listMienNam.add(tour);
                            }
                        }
                    }

                    adapterUuDai.updateData(listUuDai);
                    adapterMienBac.updateData(listMienBac);
                    adapterMienTrung.updateData(listMienTrung);
                    adapterMienNam.updateData(listMienNam);
                }
            }

            @Override
            public void onFailure(Call<List<Tour>> call, Throwable t) {
                Log.e("DJANGO_API", "Lỗi: " + t.getMessage());
            }
        });
    }

    private void openDetail(Tour tour) {
        DetailTour detailFragment = new DetailTour();
        Bundle args = new Bundle();
        // SỬA: chỉ truyền tour_id (int) qua Bundle, KHÔNG truyền cả object Tour qua Serializable.
        // DetailTour sẽ tự gọi API lấy tour mới nhất theo id, đảm bảo luôn có đầy đủ
        // departures / itineraries / images mới nhất từ Django, tránh dữ liệu cũ/thiếu.
        args.putInt("tour_id", tour.getId());
        detailFragment.setArguments(args);
        if (getParentFragmentManager() != null)
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, detailFragment)
                    .addToBackStack(null).commit();
    }

    private void loadVouchers(final List<VoucherHelper.AppVoucher> availableVouchers,
                              final HomeVoucherAdapter homeAdapter,
                              final DetailVoucherAdapter detailAdapter) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("current_user_id", 1);
        apiService.getVouchers(currentUserId, null).enqueue(new Callback<List<VoucherHelper.AppVoucher>>() {
            @Override
            public void onResponse(Call<List<VoucherHelper.AppVoucher>> call, Response<List<VoucherHelper.AppVoucher>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<VoucherHelper.AppVoucher> backendVouchers = response.body();
                    Log.d("DJANGO_API", "Tải voucher thành công, số lượng = " + backendVouchers.size());
                    if (!backendVouchers.isEmpty()) {
                        availableVouchers.clear();
                        availableVouchers.addAll(backendVouchers);
                        if (homeAdapter != null) {
                            homeAdapter.notifyDataSetChanged();
                        }
                        if (detailAdapter != null) {
                            detailAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<VoucherHelper.AppVoucher>> call, Throwable t) {
                Log.e("DJANGO_API", "Lỗi tải voucher từ API: " + t.getMessage());
            }
        });
    }
}