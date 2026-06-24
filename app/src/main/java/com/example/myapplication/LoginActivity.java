package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.myapplication.data.model.User;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout layoutContact, layoutPassword;
    private TextInputEditText etContact, etPassword;
    private AppCompatButton btnLogin;
    private ImageView btnBack;
    private MaterialButton btnFacebook, btnGoogle, btnLoginSms;
    private TextView tvSignup, tvForgotPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Apply Window Insets for edge-to-edge layout padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        initViews();
        dbHelper = new DatabaseHelper(this);

        // Setup Interactive Features
        setupListeners();
        setupSignupLink();
        setupInputWatchers();
    }

    private void initViews() {
        layoutContact = findViewById(R.id.layout_contact);
        layoutPassword = findViewById(R.id.layout_password);

        etContact = findViewById(R.id.et_contact);
        etPassword = findViewById(R.id.et_password);

        btnLogin = findViewById(R.id.btn_login);
        btnBack = findViewById(R.id.btn_back);
        btnFacebook = findViewById(R.id.btn_facebook);
        btnGoogle = findViewById(R.id.btn_google);
        btnLoginSms = findViewById(R.id.btn_login_sms);
        tvSignup = findViewById(R.id.tv_signup);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnLogin.setOnClickListener(v -> validateAndLogin());
        btnFacebook.setOnClickListener(v -> 
            Toast.makeText(LoginActivity.this, R.string.msg_facebook_click, Toast.LENGTH_SHORT).show()
        );
        btnGoogle.setOnClickListener(v -> 
            Toast.makeText(LoginActivity.this, R.string.msg_google_click, Toast.LENGTH_SHORT).show()
        );
        btnLoginSms.setOnClickListener(v -> 
            Toast.makeText(LoginActivity.this, R.string.msg_login_sms_click, Toast.LENGTH_SHORT).show()
        );
        tvForgotPassword.setOnClickListener(v -> 
            Toast.makeText(LoginActivity.this, R.string.msg_forgot_password_click, Toast.LENGTH_SHORT).show()
        );
    }

    private void validateAndLogin() {
        String contact = etContact.getText() != null ? etContact.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        boolean isValid = true;

        // 1. Phone / Email Validation
        if (contact.isEmpty()) {
            layoutContact.setError(getString(R.string.error_empty_contact_login));
            isValid = false;
        } else if (!isValidContact(contact)) {
            layoutContact.setError(getString(R.string.error_invalid_contact));
            isValid = false;
        } else {
            layoutContact.setError(null);
        }

        // 2. Password Validation
        if (password.isEmpty()) {
            layoutPassword.setError(getString(R.string.error_empty_password_login));
            isValid = false;
        } else {
            layoutPassword.setError(null);
        }

        // Action when validation succeeds
        if (isValid) {
            // Ưu tiên kiểm tra trong SQLite nội bộ trước (hỗ trợ các tài khoản offline/test)
            if (dbHelper.checkUserCredentials(contact, password)) {
                Toast.makeText(LoginActivity.this, R.string.msg_login_success, Toast.LENGTH_LONG).show();
                int userId = dbHelper.getUserIdByContact(contact);
                getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                        .putString("current_user_contact", contact)
                        .putInt("current_user_id", userId)
                        .apply();
                boolean returnToCaller = getIntent().getBooleanExtra("return_to_caller", false);
                if (returnToCaller) {
                    setResult(RESULT_OK);
                    finish();
                    return;
                }
                boolean redirectToDeparture = getIntent().getBooleanExtra("redirect_to_departure", false);
                if (redirectToDeparture) {
                    Intent intent = new Intent(LoginActivity.this, DepartureActivity.class);
                    intent.putExtra("tour_id", getIntent().getIntExtra("tour_id", -1));
                    intent.putExtra("tour_title", getIntent().getStringExtra("tour_title"));
                    intent.putExtra("adult_price", getIntent().getLongExtra("adult_price", 0L));
                    startActivity(intent);
                    finish();
                    return;
                }
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            // Nếu không có dưới local, gọi API lên Server
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            btnLogin.setEnabled(false);
            Toast.makeText(this, "Đang đăng nhập...", Toast.LENGTH_SHORT).show();

            apiService.getUsers().enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                    btnLogin.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        List<User> users = response.body();
                        boolean authenticated = false;
                        String userName = "";
                        int userId = -1;
                        for (User u : users) {
                            if (contact.equals(u.getContact()) && password.equals(u.getPassword())) {
                                authenticated = true;
                                userName = u.getName();
                                userId = u.getId();
                                break;
                            }
                        }

                        if (authenticated) {
                            // Sync cục bộ làm cache
                            if (!dbHelper.checkUserExists(contact)) {
                                dbHelper.addUser(userName, contact, password);
                            }

                            Toast.makeText(LoginActivity.this, R.string.msg_login_success, Toast.LENGTH_LONG).show();
                            // Store session in SharedPreferences
                            getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                                    .putString("current_user_contact", contact)
                                    .putInt("current_user_id", userId)
                                    .apply();
                            boolean returnToCaller = getIntent().getBooleanExtra("return_to_caller", false);
                            if (returnToCaller) {
                                setResult(RESULT_OK);
                                finish();
                                return;
                            }
                            boolean redirectToDeparture = getIntent().getBooleanExtra("redirect_to_departure", false);
                            if (redirectToDeparture) {
                                Intent intent = new Intent(LoginActivity.this, DepartureActivity.class);
                                intent.putExtra("tour_id", getIntent().getIntExtra("tour_id", -1));
                                intent.putExtra("tour_title", getIntent().getStringExtra("tour_title"));
                                intent.putExtra("adult_price", getIntent().getLongExtra("adult_price", 0L));
                                startActivity(intent);
                                finish(); // Close LoginActivity
                                return;
                            }


                            // Proactively sync FCM token if exists
                            String fcmToken = getSharedPreferences("UserSession", MODE_PRIVATE).getString("fcm_token", null);
                            if (fcmToken != null) {
                                java.util.Map<String, Object> fields = new java.util.HashMap<>();
                                fields.put("fcm_token", fcmToken);
                                apiService.patchUser(userId, fields).enqueue(new Callback<User>() {
                                    @Override
                                    public void onResponse(Call<User> call, Response<User> response) {}
                                    @Override
                                    public void onFailure(Call<User> call, Throwable t) {}
                                });
                            }

                            // Navigate to MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Close LoginActivity
                        } else {
                            layoutPassword.setError(getString(R.string.error_login_failed));
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Không thể kết nối xác thực tài khoản!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<User>> call, Throwable t) {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean isValidContact(String contact) {
        // Match standard Email addresses
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(contact).matches()) {
            return true;
        }
        // Match standard Phone numbers: 9 to 11 digits, can start with optional country code prefix
        return contact.matches("^(\\+?\\d{1,3})?\\d{9,11}$");
    }

    private void setupSignupLink() {
        String fullText = getString(R.string.link_signup_prompt) + getString(R.string.link_signup_clickable);
        String clickablePart = getString(R.string.link_signup_clickable);
        
        SpannableString spannableString = new SpannableString(fullText);
        int start = fullText.indexOf(clickablePart);
        
        if (start != -1) {
            int end = start + clickablePart.length();
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    // Navigate to RegisterActivity
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(getColor(R.color.primary_blue));
                    ds.setUnderlineText(true);
                }
            };
            spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        tvSignup.setText(spannableString);
        tvSignup.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupInputWatchers() {
        etContact.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    layoutContact.setError(null);
                }
            }
        });

        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    layoutPassword.setError(null);
                }
            }
        });
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
