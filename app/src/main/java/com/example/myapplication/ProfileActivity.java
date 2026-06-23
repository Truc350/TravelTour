package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText edtFullname, edtEmail, edtOtp;
    private DatabaseHelper dbHelper;
    private String currentContact = "";
    private TextView btnSendOtp;
    private LinearLayout layoutOtpInput;
    private androidx.appcompat.widget.AppCompatButton btnVerifyOtp;
    
    // OTP cho số điện thoại
    private EditText edtPhone, edtOtpPhone;
    private TextView btnSendOtpPhone;
    private LinearLayout layoutOtpInputPhone;
    private androidx.appcompat.widget.AppCompatButton btnVerifyOtpPhone;
    
    private LinearLayout btnDob, btnGender;
    private TextView tvDob, tvGender;

    private ImageView imgProfileAvatar;
    private static final int PICK_IMAGE_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Nút quay lại
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // 2. Các thành phần OTP Email
        edtEmail = findViewById(R.id.edt_email);
        btnSendOtp = findViewById(R.id.btn_send_otp);
        layoutOtpInput = findViewById(R.id.layout_otp_input);
        edtOtp = findViewById(R.id.edt_otp);
        btnVerifyOtp = findViewById(R.id.btn_verify_otp);

        // Hiển thị nút Gửi OTP khi người dùng thay đổi Email
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Đổi email thì hiện nút gửi OTP, ẩn ô nhập OTP cũ nếu có
                btnSendOtp.setVisibility(View.VISIBLE);
                layoutOtpInput.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bấm nút Gửi OTP
        btnSendOtp.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Đã gửi mã OTP đến " + email, Toast.LENGTH_SHORT).show();
            // Hiện vùng nhập OTP
            layoutOtpInput.setVisibility(View.VISIBLE);
            btnSendOtp.setVisibility(View.GONE);
        });

        // Bấm nút Xác nhận OTP
        btnVerifyOtp.setOnClickListener(v -> {
            String otp = edtOtp.getText().toString().trim();
            if (otp.equals("123456")) { // Mã giả lập
                Toast.makeText(this, "Xác nhận thành công!", Toast.LENGTH_SHORT).show();
                layoutOtpInput.setVisibility(View.GONE);
                edtOtp.setText("");
                // Đổi màu để thấy đã xác thực
                edtEmail.setTextColor(0xFF185FA5); // Màu xanh
                edtEmail.clearFocus();
            } else {
                Toast.makeText(this, "Mã OTP không đúng. Thử 123456", Toast.LENGTH_SHORT).show();
            }
        });

        // ================= OTP SỐ ĐIỆN THOẠI =================
        edtPhone = findViewById(R.id.edt_phone);
        btnSendOtpPhone = findViewById(R.id.btn_send_otp_phone);
        layoutOtpInputPhone = findViewById(R.id.layout_otp_input_phone);
        edtOtpPhone = findViewById(R.id.edt_otp_phone);
        btnVerifyOtpPhone = findViewById(R.id.btn_verify_otp_phone);

        edtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSendOtpPhone.setVisibility(View.VISIBLE);
                layoutOtpInputPhone.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSendOtpPhone.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Đã gửi mã OTP đến " + phone, Toast.LENGTH_SHORT).show();
            layoutOtpInputPhone.setVisibility(View.VISIBLE);
            btnSendOtpPhone.setVisibility(View.GONE);
        });

        btnVerifyOtpPhone.setOnClickListener(v -> {
            String otp = edtOtpPhone.getText().toString().trim();
            if (otp.equals("123456")) { // Mã giả lập
                Toast.makeText(this, "Xác nhận thành công!", Toast.LENGTH_SHORT).show();
                layoutOtpInputPhone.setVisibility(View.GONE);
                edtOtpPhone.setText("");
                edtPhone.setTextColor(0xFF185FA5);
                edtPhone.clearFocus();
            } else {
                Toast.makeText(this, "Mã OTP không đúng. Thử 123456", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Ngày sinh (DatePicker)
        btnDob = findViewById(R.id.btn_dob);
        tvDob = findViewById(R.id.tv_dob);
        
        btnDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        tvDob.setText(date);
                        tvDob.setTextColor(0xFF333333);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // 4. Giới tính (AlertDialog)
        btnGender = findViewById(R.id.btn_gender);
        tvGender = findViewById(R.id.tv_gender);
        
        btnGender.setOnClickListener(v -> {
            String[] genders = {"Nam", "Nữ"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chọn giới tính");
            builder.setItems(genders, (dialog, which) -> {
                tvGender.setText(genders[which]);
                tvGender.setTextColor(0xFF333333);
            });
            builder.show();
        });

        // Khởi tạo views bổ sung và DatabaseHelper
        dbHelper = new DatabaseHelper(this);
        edtFullname = findViewById(R.id.edt_fullname);
        imgProfileAvatar = findViewById(R.id.img_profile_avatar);

        // Chọn ảnh đại diện
        findViewById(R.id.btn_change_avatar).setOnClickListener(v -> selectAvatarImage());
        imgProfileAvatar.setOnClickListener(v -> selectAvatarImage());

        // Lấy phiên làm việc hiện tại
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentContact = prefs.getString("current_user_contact", "");

        // Tải dữ liệu từ SQLite
        if (!currentContact.isEmpty()) {
            Map<String, String> userDetails = dbHelper.getUserDetails(currentContact);
            if (userDetails != null) {
                if (userDetails.get("name") != null) {
                    edtFullname.setText(userDetails.get("name"));
                }
                
                String contact = userDetails.get("contact");
                if (contact != null) {
                    if (contact.contains("@")) {
                        edtEmail.setText(contact);
                        edtPhone.setText("");
                    } else {
                        edtPhone.setText(contact);
                        edtEmail.setText("");
                    }
                }
                
                String dob = userDetails.get("dob");
                if (dob != null && !dob.isEmpty()) {
                    tvDob.setText(dob);
                    tvDob.setTextColor(0xFF333333);
                }
                
                String gender = userDetails.get("gender");
                if (gender != null && !gender.isEmpty()) {
                    tvGender.setText(gender);
                    tvGender.setTextColor(0xFF333333);
                }

                String avatarPath = userDetails.get("avatar");
                loadAvatarImage(avatarPath, imgProfileAvatar);
            }
        }

        // Bấm nút Lưu trên Toolbar
        TextView btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void saveProfileChanges() {
        String name = edtFullname.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String dob = tvDob.getText().toString().trim();
        String gender = tvGender.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Họ tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Quyết định contact mới dựa trên việc nhập Email hay Sđt
        String newContact = "";
        if (!email.isEmpty()) {
            newContact = email;
        } else if (!phone.isEmpty()) {
            newContact = phone;
        } else {
            Toast.makeText(this, "Vui lòng nhập Email hoặc Số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu email / sđt đã thay đổi, kiểm tra xem nó có bị trùng với người dùng khác không
        if (!newContact.equals(currentContact)) {
            if (dbHelper.checkUserExists(newContact)) {
                Toast.makeText(this, "Email hoặc số điện thoại mới đã được sử dụng bởi tài khoản khác!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Chuẩn hóa giới tính và ngày sinh hiển thị mặc định
        if (dob.equals("Nhập ngày sinh của bạn")) {
            dob = "";
        }
        if (gender.equals("Chọn giới tính")) {
            gender = "";
        }

        // Cập nhật SQLite
        boolean success = dbHelper.updateUserProfile(currentContact, name, newContact, dob, gender);
        if (success) {
            // Cập nhật lại phiên đăng nhập SharedPreferences
            getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                    .putString("current_user_contact", newContact)
                    .apply();
            
            Toast.makeText(this, "Cập nhật thông tin hồ sơ thành công!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng ProfileActivity và quay lại Account Fragment
        } else {
            Toast.makeText(this, "Cập nhật thông tin thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectAvatarImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            String savedPath = saveAvatarToInternalStorage(selectedImageUri);
            if (savedPath != null) {
                // Lưu vào cơ sở dữ liệu
                dbHelper.updateUserAvatar(currentContact, savedPath);
                // Hiển thị ảnh vừa chọn
                loadAvatarImage(savedPath, imgProfileAvatar);
                Toast.makeText(this, "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể lưu ảnh đại diện!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveAvatarToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadAvatarImage(String path, ImageView imageView) {
        if (path != null && !path.isEmpty()) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (myBitmap != null) {
                    imageView.setImageBitmap(myBitmap);
                    imageView.setImageTintList(null);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setPadding(0, 0, 0, 0);
                    return;
                }
            }
        }
        imageView.setImageResource(R.drawable.ic_account);
        imageView.setImageTintList(ColorStateList.valueOf(0xFF185FA5));
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        int padding = (int) (12 * imageView.getResources().getDisplayMetrics().density);
        imageView.setPadding(padding, padding, padding, padding);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
