package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment biểu mẫu cập nhật thông tin hành khách (Hình 2).
 * Hỗ trợ cả hai chế độ: Thêm mới và Chỉnh sửa hành khách sẵn có qua SQLite.
 */
public class PassengerForm extends Fragment {

    private TextView tvToolbarTitle;
    private TextView tvSalutation;
    private EditText etFullName;
    private TextView tvBirthDate;
    private TextView tvNationality;
    private TextView tvIssuingCountry;
    private TextView tvExpiryDate;
    private EditText etIdOrPassport;

    private DatabaseHelper dbHelper;
    private int passengerId = -1; // -1 đại diện cho chế độ thêm mới

    private final String[] salutations = {"Ông", "Bà", "Anh", "Chị"};
    private final String[] countries = {"Việt Nam", "Singapore", "Thái Lan", "Malaysia", "Hàn Quốc", "Nhật Bản", "Mỹ", "Úc", "Pháp"};

    public static PassengerForm newInstance(int passengerId) {
        PassengerForm fragment = new PassengerForm();
        Bundle args = new Bundle();
        args.putInt("passenger_id", passengerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.passenger_form, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Lấy ID hành khách nếu có (chế độ chỉnh sửa)
        if (getArguments() != null) {
            passengerId = getArguments().getInt("passenger_id", -1);
        }

        // Ánh xạ các trường nhập liệu
        tvToolbarTitle = view.findViewById(R.id.tvToolbarTitle);
        tvSalutation = view.findViewById(R.id.tvSalutation);
        etFullName = view.findViewById(R.id.etFullName);
        tvBirthDate = view.findViewById(R.id.tvBirthDate);
        tvNationality = view.findViewById(R.id.tvNationality);
        tvIssuingCountry = view.findViewById(R.id.tvIssuingCountry);
        tvExpiryDate = view.findViewById(R.id.tvExpiryDate);
        etIdOrPassport = view.findViewById(R.id.etIdOrPassport);

        // Nút quay lại màn hình danh sách hành khách
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Thiết lập sự kiện chọn Danh xưng
        view.findViewById(R.id.btnSelectSalutation).setOnClickListener(v -> {
            showChoiceDialog("Chọn danh xưng", salutations, tvSalutation);
        });

        // Thiết lập sự kiện chọn Ngày sinh
        view.findViewById(R.id.btnSelectBirthDate).setOnClickListener(v -> {
            showDatePicker(tvBirthDate, true);
        });

        // Thiết lập sự kiện chọn Quốc tịch
        view.findViewById(R.id.btnSelectNationality).setOnClickListener(v -> {
            showChoiceDialog("Chọn quốc tịch", countries, tvNationality);
        });

        // Thiết lập sự kiện chọn Quốc gia cấp
        view.findViewById(R.id.btnSelectIssuingCountry).setOnClickListener(v -> {
            showChoiceDialog("Chọn quốc gia cấp", countries, tvIssuingCountry);
        });

        // Thiết lập sự kiện chọn Ngày hết hạn
        view.findViewById(R.id.btnSelectExpiryDate).setOnClickListener(v -> {
            showDatePicker(tvExpiryDate, false);
        });

        // Thiết lập sự kiện bấm nút Lưu thay đổi
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            savePassengerData();
        });

        // Nếu ở chế độ chỉnh sửa, nạp thông tin cũ lên form
        if (passengerId != -1) {
            loadPassengerData();
        }

        return view;
    }

    private void loadPassengerData() {
        Map<String, String> passenger = dbHelper.getPassenger(passengerId);
        if (passenger != null) {
            tvToolbarTitle.setText("Cập nhật thông tin");
            
            tvSalutation.setText(passenger.get("salutation"));
            tvSalutation.setTextColor(0xFF333333);
            
            etFullName.setText(passenger.get("fullname"));
            
            tvBirthDate.setText(passenger.get("birthdate"));
            tvBirthDate.setTextColor(0xFF333333);
            
            tvNationality.setText(passenger.get("nationality"));
            tvNationality.setTextColor(0xFF333333);
            
            tvIssuingCountry.setText(passenger.get("issuing_country"));
            tvIssuingCountry.setTextColor(0xFF333333);
            
            tvExpiryDate.setText(passenger.get("expiry_date"));
            tvExpiryDate.setTextColor(0xFF333333);
            
            etIdOrPassport.setText(passenger.get("id_or_passport"));
        }
    }

    private void showChoiceDialog(String title, String[] items, TextView textView) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(items, (dialog, which) -> {
                    textView.setText(items[which]);
                    textView.setTextColor(0xFF333333); // Đổi thành màu chữ đậm khi đã chọn
                })
                .show();
    }

    private void showDatePicker(TextView textView, boolean isBirthDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Trạng thái ngày sinh mặc định lùi lại 20 năm, ngày hết hạn tiến tới 5 năm
        if (isBirthDate) {
            year -= 20;
        } else {
            year += 5;
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    textView.setText(formattedDate);
                    textView.setTextColor(0xFF333333); // Đổi thành màu chữ đậm
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void savePassengerData() {
        String salutation = tvSalutation.getText().toString();
        String fullName = etFullName.getText().toString().trim();
        String birthDate = tvBirthDate.getText().toString();
        String nationality = tvNationality.getText().toString();
        String issuingCountry = tvIssuingCountry.getText().toString();
        String expiryDate = tvExpiryDate.getText().toString();
        String idOrPassport = etIdOrPassport.getText().toString().trim();

        // Kiểm tra tính hợp lệ của các dữ liệu nhập
        if (salutation.equals("Chọn danh xưng") || fullName.isEmpty() 
                || birthDate.equals("Sinh ngày") || nationality.equals("Quốc tịch")
                || issuingCountry.equals("Quốc gia cấp") || expiryDate.equals("Ngày hết hạn")
                || idOrPassport.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ các trường thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success;
        if (passengerId == -1) {
            // Thêm mới
            success = dbHelper.addPassenger(salutation, fullName, birthDate, nationality, issuingCountry, expiryDate, idOrPassport);
        } else {
            // Chỉnh sửa
            success = dbHelper.updatePassenger(passengerId, salutation, fullName, birthDate, nationality, issuingCountry, expiryDate, idOrPassport);
        }

        if (success) {
            Toast.makeText(requireContext(), "Lưu thông tin hành khách thành công!", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        } else {
            Toast.makeText(requireContext(), "Lỗi khi lưu thông tin. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }
}
