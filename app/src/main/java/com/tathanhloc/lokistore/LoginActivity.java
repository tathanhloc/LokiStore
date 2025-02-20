package com.tathanhloc.lokistore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    // UI Components
    private TextView tvUsername;
    private TextInputLayout emailInputLayout;
    private EditText emailEditText;
    private EditText passwordEditText;
    private ImageButton btnBiometric;
    private Button loginButton;
    private MaterialCheckBox rememberMeCheckBox;
    private ProgressBar progressBar;

    // Managers and Utils
    private DatabaseManager dbManager;
    private SessionManager sessionManager;
    private SharedPreferences sharedPreferences;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Button btnSwitchAccount;


    // Constants for SharedPreferences
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_LOGIN_TIMESTAMP = "login_timestamp";
    private static final String KEY_LAST_LOGGED_USER = "last_logged_user";
    private static final long TEN_DAYS_IN_MILLIS = 10 * 24 * 60 * 60 * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeManagers();
        if (checkExistingSession()) {
            return;
        }
        initializeViews();
        setupBiometricAuth();
        checkLastLoginState();
        setupClickListeners();
        btnBiometric = findViewById(R.id.btnBiometric);

        checkBiometricAvailability();
    }

    private void initializeManagers() {
        sessionManager = new SessionManager(this);
        dbManager = new DatabaseManager(this);
        dbManager.open();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void initializeViews() {
        tvUsername = findViewById(R.id.tvUsername);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        btnBiometric = findViewById(R.id.btnBiometric);
        loginButton = findViewById(R.id.loginButton);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        progressBar = findViewById(R.id.progressBar);
        btnSwitchAccount = findViewById(R.id.btnSwitchAccount);

    }

    private boolean checkExistingSession() {
        if (sessionManager.isLoggedIn() && isSessionValid()) {
            startDashboardActivity();
            return true;
        }
        sessionManager.logout();
        return false;
    }


    private void checkLastLoginState() {
        String lastUserName = sessionManager.getLastUserName();
        String lastUserEmail = sessionManager.getLastUserEmail();

        if (!lastUserName.isEmpty() && !lastUserEmail.isEmpty()) {
            tvUsername.setText(lastUserName);
            tvUsername.setVisibility(View.VISIBLE);
            emailInputLayout.setVisibility(View.GONE);
            btnSwitchAccount.setVisibility(View.VISIBLE);
            emailEditText.setText(lastUserEmail);

            // Kiểm tra và hiển thị nút sinh trắc học
            checkBiometricAvailability();
        } else {
            tvUsername.setVisibility(View.GONE);
            emailInputLayout.setVisibility(View.VISIBLE);
            btnBiometric.setVisibility(View.GONE);
            btnSwitchAccount.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> {
            String email;
            if (emailInputLayout.getVisibility() == View.VISIBLE) {
                email = emailEditText.getText().toString();
            } else {
                email = sessionManager.getLastUserEmail(); // Lấy email của người dùng cuối
            }
            String password = passwordEditText.getText().toString();

            if (validateInput(email, password)) {
                attemptLogin(email, password);
            }

        });

        // Xử lý đăng ký và quên mật khẩu giữ nguyên
        TextView registerText = findViewById(R.id.registerText);
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnSwitchAccount.setOnClickListener(v -> {
            // Xóa toàn bộ thông tin session hiện tại
            sessionManager.clearAllData();

            // Chuyển về giao diện đăng nhập lần đầu
            tvUsername.setVisibility(View.GONE);
            emailInputLayout.setVisibility(View.VISIBLE);
            btnBiometric.setVisibility(View.GONE);
            btnSwitchAccount.setVisibility(View.GONE);

            // Xóa thông tin đã điền
            emailEditText.setText("");
            passwordEditText.setText("");
        });
    }

    private void attemptLogin(final String email, final String password) {
        showLoading();

        try {
            // Kiểm tra thông tin đăng nhập
            if (dbManager.checkLogin(email, password)) {
                String fullName = dbManager.getUserFullName(email);

                if (rememberMeCheckBox.isChecked()) {
                    // Lưu phiên đăng nhập và thông tin nếu tích "Ghi nhớ đăng nhập"
                    sessionManager.createLoginSession(email, fullName);
                    sessionManager.saveLoginTime(System.currentTimeMillis());
                    saveLoginState(email, password, fullName);
                } else {
                    // Tạo phiên đăng nhập tạm thời mà không lưu thông tin
                    sessionManager.createLoginSession(email, fullName);
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

    private void checkBiometricAvailability() {
        // Log để kiểm tra từng bước
        Log.d("BiometricDebug", "Last Email: " + sessionManager.getLastUserEmail());

        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG);

        Log.d("BiometricDebug", "Can Authenticate: " + canAuthenticate);

        String lastEmail = sessionManager.getLastUserEmail();
        boolean isBiometricEnabled = dbManager.isBiometricEnabled(lastEmail);

        Log.d("BiometricDebug", "Biometric Enabled: " + isBiometricEnabled);

        if (!lastEmail.isEmpty() &&
                isBiometricEnabled &&
                canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            btnBiometric.setVisibility(View.VISIBLE);
            Log.d("BiometricDebug", "Biometric Button Visible");
        } else {
            btnBiometric.setVisibility(View.GONE);
            Log.d("BiometricDebug", "Biometric Button Hidden");
        }
    }
    private void setupBiometricAuth() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS) {

            BiometricPrompt.AuthenticationCallback authCallback =
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(
                                @NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);

                            String lastEmail = sessionManager.getLastUserEmail();
                            if (dbManager.isBiometricEnabled(lastEmail)) {
                                // Tự động đăng nhập với email đã lưu
                                String fullName = dbManager.getUserFullName(lastEmail);
                                sessionManager.createLoginSession(lastEmail, fullName);
                                startDashboardActivity();
                            }
                        }

                        @Override
                        public void onAuthenticationError(int errorCode,
                                                          @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(LoginActivity.this,
                                    "Xác thực sinh trắc học thất bại: " + errString,
                                    Toast.LENGTH_SHORT).show();
                        }
                    };

            biometricPrompt = new BiometricPrompt(this,
                    ContextCompat.getMainExecutor(this), authCallback);

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Đăng nhập sinh trắc học")
                    .setSubtitle("Sử dụng vân tay để đăng nhập")
                    .setNegativeButtonText("Hủy")
                    .build();

            btnBiometric.setOnClickListener(v -> {
                if (dbManager.isBiometricEnabled(sessionManager.getLastUserEmail())) {
                    biometricPrompt.authenticate(promptInfo);
                } else {
                    Toast.makeText(this,
                            "Tính năng sinh trắc học chưa được bật cho tài khoản này",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }




    private void startDashboardActivity() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void saveLoginState(String email, String password, String fullName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_LAST_LOGGED_USER, fullName);
        editor.putBoolean(KEY_REMEMBER_ME, true);
        editor.putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
    }

    private void clearLoginState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASSWORD);
        editor.remove(KEY_REMEMBER_ME);
        editor.remove(KEY_LOGIN_TIMESTAMP);
        // Không xóa KEY_LAST_LOGGED_USER để giữ trạng thái người dùng cuối
        editor.apply();
    }

    private boolean isSessionValid() {
        long loginTime = sessionManager.getLoginTime();
        return System.currentTimeMillis() - loginTime <= TEN_DAYS_IN_MILLIS;
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;

        if (emailInputLayout.getVisibility() == View.VISIBLE) {
            if (email.isEmpty()) {
                emailEditText.setError("Vui lòng nhập email");
                isValid = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Email không hợp lệ");
                isValid = false;
            }
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Vui lòng nhập mật khẩu");
            isValid = false;
        }

        return isValid;
    }

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