package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment xử lý logic thay đổi mật khẩu của người dùng.
 * Quản lý kiểm tra dữ liệu nhập vào, đối chiếu mật khẩu cũ và cập nhật mật khẩu mới qua SQLite.
 */
public class ChangePassword extends Fragment {

    private TextInputLayout layoutOldPassword, layoutNewPassword, layoutConfirmPassword;
    private TextInputEditText etOldPassword, etNewPassword, etConfirmPassword;
    private AppCompatButton btnSubmit;
    private View btnBack;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.change_password, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Ánh xạ các View từ XML
        initViews(view);

        // Thiết lập sự kiện Input Watcher để xóa thông báo lỗi khi bắt đầu gõ
        setupInputWatchers();

        // Thiết lập sự kiện click cho các nút
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        layoutOldPassword = view.findViewById(R.id.layout_old_password);
        layoutNewPassword = view.findViewById(R.id.layout_new_password);
        layoutConfirmPassword = view.findViewById(R.id.layout_confirm_password);

        etOldPassword = view.findViewById(R.id.et_old_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);

        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        // Nút quay lại
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        // Nút Đổi mật khẩu
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> validateAndChangePassword());
        }
    }

    private void validateAndChangePassword() {
        String oldPasswordInput = etOldPassword.getText() != null ? etOldPassword.getText().toString().trim() : "";
        String newPasswordInput = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
        String confirmPasswordInput = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // Lấy session contact hiện tại
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String contact = prefs.getString("current_user_contact", "");

        // Nếu trống (do chạy thẳng từ MainActivity), dùng tài khoản cuối cùng trong DB làm fallback
        if (contact.isEmpty()) {
            contact = dbHelper.getLastUserContact();
        }

        // Nếu DB chưa có tài khoản nào thì hiển thị lỗi
        if (contact.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin tài khoản hợp lệ. Vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
            return;
        }

        boolean isValid = true;

        // 1. Kiểm tra mật khẩu cũ
        if (oldPasswordInput.isEmpty()) {
            layoutOldPassword.setError("Vui lòng nhập mật khẩu cũ");
            isValid = false;
        } else {
            String currentPasswordInDb = dbHelper.getPassword(contact);
            if (!oldPasswordInput.equals(currentPasswordInDb)) {
                layoutOldPassword.setError("Mật khẩu cũ không chính xác");
                isValid = false;
            } else {
                layoutOldPassword.setError(null);
            }
        }

        // 2. Kiểm tra mật khẩu mới
        if (newPasswordInput.isEmpty()) {
            layoutNewPassword.setError("Vui lòng nhập mật khẩu mới");
            isValid = false;
        } else if (newPasswordInput.length() < 6) {
            layoutNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
            isValid = false;
        } else {
            layoutNewPassword.setError(null);
        }

        // 3. Kiểm tra nhập lại mật khẩu mới
        if (confirmPasswordInput.isEmpty()) {
            layoutConfirmPassword.setError("Vui lòng nhập lại mật khẩu mới");
            isValid = false;
        } else if (!confirmPasswordInput.equals(newPasswordInput)) {
            layoutConfirmPassword.setError("Mật khẩu nhập lại không trùng khớp");
            isValid = false;
        } else {
            layoutConfirmPassword.setError(null);
        }

        // Nếu tất cả hợp lệ, cập nhật vào SQLite
        if (isValid) {
            boolean isUpdated = dbHelper.updatePassword(contact, newPasswordInput);
            if (isUpdated) {
                Toast.makeText(requireContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            } else {
                Toast.makeText(requireContext(), "Đổi mật khẩu thất bại. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupInputWatchers() {
        etOldPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    layoutOldPassword.setError(null);
                }
            }
        });

        etNewPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    layoutNewPassword.setError(null);
                }
            }
        });

        etConfirmPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    layoutConfirmPassword.setError(null);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Ẩn thanh BottomNavigationView khi vào trang Đổi mật khẩu
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.bottomNavigation);
            if (nav != null) {
                nav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Hiện lại thanh BottomNavigationView khi rời khỏi trang Đổi mật khẩu
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.bottomNavigation);
            if (nav != null) {
                nav.setVisibility(View.VISIBLE);
            }
        }
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
