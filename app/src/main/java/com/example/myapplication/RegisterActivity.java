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

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout layoutName, layoutContact, layoutPassword;
    private TextInputEditText etName, etContact, etPassword;
    private AppCompatButton btnRegister;
    private ImageView btnBack;
    private MaterialButton btnFacebook, btnGoogle;
    private TextView tvTerms, tvLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

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
        setupTermsLink();
        setupLoginLink();
        setupInputWatchers();
    }

    private void initViews() {
        layoutName = findViewById(R.id.layout_name);
        layoutContact = findViewById(R.id.layout_contact);
        layoutPassword = findViewById(R.id.layout_password);

        etName = findViewById(R.id.et_name);
        etContact = findViewById(R.id.et_contact);
        etPassword = findViewById(R.id.et_password);

        btnRegister = findViewById(R.id.btn_register);
        btnBack = findViewById(R.id.btn_back);
        btnFacebook = findViewById(R.id.btn_facebook);
        btnGoogle = findViewById(R.id.btn_google);
        tvTerms = findViewById(R.id.tv_terms);
        tvLogin = findViewById(R.id.tv_login);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> validateAndRegister());
        btnFacebook.setOnClickListener(v -> 
            Toast.makeText(RegisterActivity.this, R.string.msg_facebook_click, Toast.LENGTH_SHORT).show()
        );

        // Google Login
        btnGoogle.setOnClickListener(v -> 
            Toast.makeText(RegisterActivity.this, R.string.msg_google_click, Toast.LENGTH_SHORT).show()
        );
    }
    private void validateAndRegister() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String contact = etContact.getText() != null ? etContact.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        boolean isValid = true;
        // 1. Full Name Validation
        if (name.isEmpty()) {
            layoutName.setError(getString(R.string.error_empty_name));
            isValid = false;
        } else {
            layoutName.setError(null);
        }
        // 2. Phone / Email Validation
        if (contact.isEmpty()) {
            layoutContact.setError(getString(R.string.error_empty_contact));
            isValid = false;
        } else if (!isValidContact(contact)) {
            layoutContact.setError(getString(R.string.error_invalid_contact));
            isValid = false;
        } else {
            layoutContact.setError(null);
        }
        // 3. Password Validation
        if (password.isEmpty()) {
            layoutPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (password.length() < 6) {
            layoutPassword.setError(getString(R.string.error_invalid_password));
            isValid = false;
        } else {
            layoutPassword.setError(null);
        }

        // Action when validation succeeds
        if (isValid) {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            btnRegister.setEnabled(false);
            Toast.makeText(this, "Đang đăng ký...", Toast.LENGTH_SHORT).show();

            apiService.getUsers().enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<User> users = response.body();
                        boolean exists = false;
                        for (User u : users) {
                            if (contact.equals(u.getContact())) {
                                exists = true;
                                break;
                            }
                        }

                        if (exists) {
                            btnRegister.setEnabled(true);
                            layoutContact.setError(getString(R.string.error_contact_exists));
                        } else {
                            User newUser = new User(name, contact, password, "");
                            apiService.registerUser(newUser).enqueue(new Callback<User>() {
                                @Override
                                public void onResponse(Call<User> call, Response<User> response) {
                                    btnRegister.setEnabled(true);
                                    if (response.isSuccessful()) {
                                        // Lưu cục bộ làm cache
                                        dbHelper.addUser(name, contact, password);

                                        User registeredUser = response.body();
                                        int userId = (registeredUser != null) ? registeredUser.getId() : -1;

                                        Toast.makeText(RegisterActivity.this, R.string.msg_register_success, Toast.LENGTH_LONG).show();
                                        getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                                                .putString("current_user_contact", contact)
                                                .putInt("current_user_id", userId)
                                                .apply();

                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại trên máy chủ!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<User> call, Throwable t) {
                                    btnRegister.setEnabled(true);
                                    Toast.makeText(RegisterActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Không thể tải danh sách tài khoản từ máy chủ!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<User>> call, Throwable t) {
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean isValidContact(String contact) {
        // Match standard Email addresses
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(contact).matches()) {
            return true;
        }
        // Match standard Phone numbers: 9 to 11 digits, can start with optional country code prefix (+ or digits)
        return contact.matches("^(\\+?\\d{1,3})?\\d{9,11}$");
    }

    private void setupTermsLink() {
        String fullText = getString(R.string.terms_disclaimer);
        String clickablePart = getString(R.string.terms_clickable_part);
        
        SpannableString spannableString = new SpannableString(fullText);
        int start = fullText.indexOf(clickablePart);
        
        if (start != -1) {
            int end = start + clickablePart.length();
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Toast.makeText(RegisterActivity.this, R.string.msg_click_terms, Toast.LENGTH_SHORT).show();
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
        
        tvTerms.setText(spannableString);
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupLoginLink() {
        String fullText = getString(R.string.link_login_prompt) + getString(R.string.link_login_clickable);
        String clickablePart = getString(R.string.link_login_clickable);
        
        SpannableString spannableString = new SpannableString(fullText);
        int start = fullText.indexOf(clickablePart);
        
        if (start != -1) {
            int end = start + clickablePart.length();
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
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
        
        tvLogin.setText(spannableString);
        tvLogin.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupInputWatchers() {
        // Clear errors automatically as soon as the user starts correcting their inputs
        etName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    layoutName.setError(null);
                }
            }
        });

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

    // A simple interface to avoid boilerplate in TextWatcher implementations
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
