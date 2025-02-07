// LoginActivity.java
package com.tathanhloc.lokistore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.checkbox.MaterialCheckBox;

public class LoginActivity extends AppCompatActivity {
    // UI Components
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private MaterialCheckBox rememberMeCheckBox;
    private ProgressBar progressBar;

    // Managers and Utils
    private DatabaseManager dbManager;
    private SessionManager sessionManager;
    private SharedPreferences sharedPreferences;

    // Constants for SharedPreferences
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_LOGIN_TIMESTAMP = "login_timestamp";
    private static final long TEN_DAYS_IN_MILLIS = 10 * 24 * 60 * 60 * 1000L; // 10 days in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo các manager
        initializeManagers();

        // Kiểm tra nếu người dùng đã đăng nhập
        if (checkExistingSession()) {
            return;
        }

        // Khởi tạo các thành phần UI
        initializeViews();

        // Kiểm tra trạng thái đăng nhập được lưu trước đó
        checkSavedLoginState();

        // Thiết lập các sự kiện click
        setupClickListeners();
    }

    private void initializeManagers() {
        sessionManager = new SessionManager(this);
        dbManager = new DatabaseManager(this);
        dbManager.open();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private boolean checkExistingSession() {
        if (sessionManager.isLoggedIn()) {
            // Kiểm tra thời gian đăng nhập
            if (isSessionValid()) {
                startDashboardActivity();
                return true;
            } else {
                // Phiên đăng nhập hết hạn
                sessionManager.logout();
                clearLoginState();
            }
        }
        return false;
    }

    private boolean isSessionValid() {
        long loginTime = sessionManager.getLoginTime();
        return System.currentTimeMillis() - loginTime <= TEN_DAYS_IN_MILLIS;
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Xử lý đăng nhập
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!isNetworkAvailable()) {
//                    Toast.makeText(LoginActivity.this,
//                            "Vui lòng kiểm tra kết nối mạng", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (validateInput(email, password)) {
                    attemptLogin(email, password);
                }
            }
        });

        // Xử lý đăng ký
        TextView registerText = findViewById(R.id.registerText);
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        // Xử lý quên mật khẩu
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void checkSavedLoginState() {
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
        long loginTimestamp = sharedPreferences.getLong(KEY_LOGIN_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();

        if (rememberMe && (currentTime - loginTimestamp) < TEN_DAYS_IN_MILLIS) {
            String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
            String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");

            // Điền thông tin đăng nhập đã lưu
            emailEditText.setText(savedEmail);
            passwordEditText.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);

            // Tự động đăng nhập nếu thông tin hợp lệ
            if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
                attemptLogin(savedEmail, savedPassword);
            }
        }
    }

    private void attemptLogin(final String email, final String password) {
        showLoading();

        try {
            // Kiểm tra thông tin đăng nhập
            if (dbManager.checkLogin(email, password)) {
                String fullName = dbManager.getUserFullName(email);

                // Lưu phiên đăng nhập vào SessionManager
                sessionManager.createLoginSession(email, fullName);
                sessionManager.saveLoginTime(System.currentTimeMillis());

                // Lưu thông tin đăng nhập nếu chọn "Ghi nhớ đăng nhập"
                if (rememberMeCheckBox.isChecked()) {
                    saveLoginState(email, password);
                } else {
                    clearLoginState();
                }

                startDashboardActivity();
            } else {
                Toast.makeText(this, "Email hoặc mật khẩu không đúng",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Có lỗi xảy ra khi đăng nhập",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            hideLoading();
        }
    }

    private void startDashboardActivity() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void saveLoginState(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putBoolean(KEY_REMEMBER_ME, true);
        editor.putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
    }

    private void clearLoginState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;

        if (email.isEmpty()) {
            emailEditText.setError("Vui lòng nhập email");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email không hợp lệ");
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Vui lòng nhập mật khẩu");
            isValid = false;
        }

        return isValid;
    }

//    private boolean isNetworkAvailable() {
//        try {
//            ConnectivityManager connectivityManager = (ConnectivityManager)
//                    getSystemService(Context.CONNECTIVITY_SERVICE);
//            if (connectivityManager != null) {
//                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//            }
//            return false;
//        } catch (SecurityException e) {
//            // Xử lý trường hợp không có quyền
//            Toast.makeText(this, "Vui lòng cấp quyền truy cập mạng cho ứng dụng",
//                    Toast.LENGTH_LONG).show();
//            return true; // Trả về true để cho phép tiếp tục đăng nhập
//        }
//    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
        }
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}