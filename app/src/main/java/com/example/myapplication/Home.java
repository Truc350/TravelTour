package com.example.myapplication;

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

        // --- Gọi API ---
        loadTours();

        return view;
    }

    private void setupRecyclerView(RecyclerView rv, TourAdapter adapter) {
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

                    // Hiện tại chưa có field phân loại → hiển thị tất cả ở mỗi section
                    // Sau này thêm field "region" vào Django thì filter tại đây
                    adapterUuDai.updateData(all);
                    adapterMienBac.updateData(all);
                    adapterMienTrung.updateData(all);
                    adapterMienNam.updateData(all);
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
        args.putString("tour_type", tour.getCode()); // dùng code tour
        detailFragment.setArguments(args);
        if (getParentFragmentManager() != null)
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, detailFragment)
                    .addToBackStack(null).commit();
    }
}