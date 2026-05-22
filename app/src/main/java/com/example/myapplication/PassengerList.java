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
        List<Map<String, String>> passengers = dbHelper.getAllPassengers();

        if (passengers.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            scrollViewPassengerList.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            scrollViewPassengerList.setVisibility(View.VISIBLE);

            passengerContainer.removeAllViews();

            for (Map<String, String> passenger : passengers) {
                // Nạp tệp giao diện item_passenger
                View itemView = inflater.inflate(R.layout.item_passenger, passengerContainer, false);

                // Ánh xạ
                TextView tvCardName = itemView.findViewById(R.id.tvCardName);
                TextView tvCardIdOrPassport = itemView.findViewById(R.id.tvCardIdOrPassport);
                TextView tvCardNationality = itemView.findViewById(R.id.tvCardNationality);
                View btnEdit = itemView.findViewById(R.id.btnEditPassenger);
                View btnDelete = itemView.findViewById(R.id.btnDeletePassenger);

                final int passengerId = Integer.parseInt(passenger.get("id"));

                // Gán thông tin hành khách lên card
                String fullNameDisplay = passenger.get("salutation") + " " + passenger.get("fullname");
                tvCardName.setText(fullNameDisplay);
                tvCardIdOrPassport.setText("Số CCCD/Passport: " + passenger.get("id_or_passport"));
                tvCardNationality.setText("Quốc tịch: " + passenger.get("nationality") + " | Hạn: " + passenger.get("expiry_date"));

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
                            .setMessage("Bạn có chắc chắn muốn xóa thông tin hành khách " + passenger.get("fullname") + " không?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                if (dbHelper.deletePassenger(passengerId)) {
                                    Toast.makeText(requireContext(), "Đã xóa thông tin hành khách!", Toast.LENGTH_SHORT).show();
                                    loadPassengerList(inflater); // Tải lại danh sách sau khi xóa thành công
                                } else {
                                    Toast.makeText(requireContext(), "Lỗi khi xóa. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                });

                // Thêm card view vào container
                passengerContainer.addView(itemView);
            }
        }
    }
}
