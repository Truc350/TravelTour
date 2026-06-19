package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Map;

import com.example.myapplication.data.model.Passenger;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment hiển thị danh sách thông tin hành khách (Hình 1).
 * Hỗ trợ tải dữ liệu động từ SQLite và quản lý Sửa / Xóa hành khách.
 */
public class PassengerList extends Fragment {

    private View layoutEmptyState;
    private View scrollViewPassengerList;
    private LinearLayout passengerContainer;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.passenger_list, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Ánh xạ views
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        scrollViewPassengerList = view.findViewById(R.id.scrollViewPassengerList);
        passengerContainer = view.findViewById(R.id.passengerContainer);

        // Nút quay lại màn hình tài khoản
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Nút thêm thông tin mới -> chuyển sang màn hình biểu mẫu ở chế độ Thêm mới (-1)
        view.findViewById(R.id.btnAddNewPassenger).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, PassengerForm.newInstance(-1))
                    .addToBackStack(null)
                    .commit();
        });

        // Tải danh sách hành khách lên màn hình
        loadPassengerList(inflater);

        return view;
    }

    private void loadPassengerList(LayoutInflater inflater) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getPassengers().enqueue(new Callback<List<Passenger>>() {
            @Override
            public void onResponse(Call<List<Passenger>> call, Response<List<Passenger>> response) {
                if (getContext() == null || !isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Passenger> passengers = response.body();

                    if (passengers.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        scrollViewPassengerList.setVisibility(View.GONE);
                    } else {
                        layoutEmptyState.setVisibility(View.GONE);
                        scrollViewPassengerList.setVisibility(View.VISIBLE);
                        passengerContainer.removeAllViews();

                        for (Passenger passenger : passengers) {
                            // Nạp tệp giao diện item_passenger
                            View itemView = inflater.inflate(R.layout.item_passenger, passengerContainer, false);

                            // Ánh xạ
                            TextView tvCardName = itemView.findViewById(R.id.tvCardName);
                            TextView tvCardIdOrPassport = itemView.findViewById(R.id.tvCardIdOrPassport);
                            TextView tvCardNationality = itemView.findViewById(R.id.tvCardNationality);
                            TextView tvCardStatus = itemView.findViewById(R.id.tvCardStatus);
                            View btnEdit = itemView.findViewById(R.id.btnEditPassenger);
                            View btnDelete = itemView.findViewById(R.id.btnDeletePassenger);

                            final int passengerId = passenger.getId();

                            // Gán thông tin hành khách lên card
                            String fullNameDisplay = passenger.getSalutation() + " " + passenger.getFullname();
                            tvCardName.setText(fullNameDisplay);
                            tvCardIdOrPassport.setText("Số CCCD/Passport: " + passenger.getIdOrPassport());
                            tvCardNationality.setText("Quốc tịch: " + passenger.getNationality() + " | Hạn: " + passenger.getExpiryDate());

                            // Thiết lập trạng thái duyệt
                            String status = passenger.getStatus();
                            if (status == null) status = "PENDING";
                            switch (status.toUpperCase()) {
                                case "VERIFIED":
                                    tvCardStatus.setText("Đã duyệt ✓");
                                    tvCardStatus.setTextColor(0xFF2E7D32); // Dark Green
                                    break;
                                case "REJECTED":
                                    tvCardStatus.setText("Bị từ chối ✗");
                                    tvCardStatus.setTextColor(0xFFC62828); // Dark Red
                                    break;
                                default:
                                    tvCardStatus.setText("Chờ duyệt •");
                                    tvCardStatus.setTextColor(0xFFE65100); // Dark Orange
                                    break;
                            }

                            // Lắng nghe sự kiện Sửa
                            btnEdit.setOnClickListener(v -> {
                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.contentFrame, PassengerForm.newInstance(passengerId))
                                        .addToBackStack(null)
                                        .commit();
                            });

                            // Lắng nghe sự kiện Xóa kèm theo hộp thoại xác nhận
                            btnDelete.setOnClickListener(v -> {
                                new AlertDialog.Builder(requireContext())
                                        .setTitle("Xóa thông tin hành khách")
                                        .setMessage("Bạn có chắc chắn muốn xóa thông tin hành khách " + passenger.getFullname() + " không?")
                                        .setPositiveButton("Xóa", (dialog, which) -> {
                                            apiService.deletePassenger(passengerId).enqueue(new Callback<Void>() {
                                                @Override
                                                public void onResponse(Call<Void> call, Response<Void> response) {
                                                    if (getContext() == null) return;
                                                    // Xóa cục bộ làm cache
                                                    dbHelper.deletePassenger(passengerId);
                                                    Toast.makeText(requireContext(), "Đã xóa thông tin hành khách!", Toast.LENGTH_SHORT).show();
                                                    loadPassengerList(inflater); // Tải lại danh sách sau khi xóa thành công
                                                }

                                                @Override
                                                public void onFailure(Call<Void> call, Throwable t) {
                                                    if (getContext() == null) return;
                                                    Toast.makeText(requireContext(), "Lỗi khi xóa trên máy chủ!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        })
                                        .setNegativeButton("Hủy", null)
                                        .show();
                            });

                            // Thêm card view vào container
                            passengerContainer.addView(itemView);
                        }
                    }
                } else {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    scrollViewPassengerList.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Lỗi tải danh sách từ máy chủ!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Passenger>> call, Throwable t) {
                if (getContext() == null || !isAdded()) return;
                layoutEmptyState.setVisibility(View.VISIBLE);
                scrollViewPassengerList.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
