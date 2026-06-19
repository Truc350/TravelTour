package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Map;

/**
 * Màn hình nhập/chỉnh sửa thông tin Xuất hóa đơn đỏ (Hóa đơn điện tử).
 */
public class EditInvoiceActivity extends AppCompatActivity {

    private EditText edtCompany, edtTaxCode, edtAddress, edtEmail;
    private DatabaseHelper dbHelper;
    private String currentContact = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_invoice);

        // System bar padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        edtCompany = findViewById(R.id.edt_invoice_company);
        edtTaxCode = findViewById(R.id.edt_invoice_tax_code);
        edtAddress = findViewById(R.id.edt_invoice_address);
        edtEmail = findViewById(R.id.edt_invoice_email);

        dbHelper = new DatabaseHelper(this);

        // Back button
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Get current logged-in contact
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentContact = prefs.getString("current_user_contact", "");

        // Pre-fill existing data from database
        if (!currentContact.isEmpty()) {
            Map<String, String> userDetails = dbHelper.getUserDetails(currentContact);
            if (userDetails != null) {
                if (userDetails.get("invoice_company") != null) {
                    edtCompany.setText(userDetails.get("invoice_company"));
                }
                if (userDetails.get("invoice_tax_code") != null) {
                    edtTaxCode.setText(userDetails.get("invoice_tax_code"));
                }
                if (userDetails.get("invoice_address") != null) {
                    edtAddress.setText(userDetails.get("invoice_address"));
                }
                if (userDetails.get("invoice_email") != null) {
                    edtEmail.setText(userDetails.get("invoice_email"));
                }
            }
        }

        // Save button
        findViewById(R.id.btn_save_invoice).setOnClickListener(v -> saveInvoiceInfo());
    }

    private void saveInvoiceInfo() {
        String company = edtCompany.getText().toString().trim();
        String taxCode = edtTaxCode.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        // If user wants to save details, validate them
        if (!TextUtils.isEmpty(company) || !TextUtils.isEmpty(taxCode) || !TextUtils.isEmpty(address) || !TextUtils.isEmpty(email)) {
            if (TextUtils.isEmpty(company)) {
                edtCompany.setError("Vui lòng nhập tên công ty");
                edtCompany.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(taxCode)) {
                edtTaxCode.setError("Vui lòng nhập mã số thuế");
                edtTaxCode.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(address)) {
                edtAddress.setError("Vui lòng nhập địa chỉ công ty");
                edtAddress.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(email)) {
                edtEmail.setError("Vui lòng nhập email nhận hóa đơn");
                edtEmail.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Email không đúng định dạng");
                edtEmail.requestFocus();
                return;
            }
        }

        if (currentContact.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy phiên đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SQLite
        boolean success = dbHelper.updateUserInvoiceInfo(currentContact, company, taxCode, address, email);
        if (success) {
            Toast.makeText(this, "Đã lưu thông tin hoá đơn điện tử!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Lưu thông tin thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }
}
