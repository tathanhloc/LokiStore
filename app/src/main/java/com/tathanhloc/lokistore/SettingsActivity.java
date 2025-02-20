package com.tathanhloc.lokistore;

import android.app.AlertDialog;
import androidx.biometric.BiometricPrompt;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat switchBiometric;
    private DatabaseManager dbManager;
    private SessionManager sessionManager;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbManager = new DatabaseManager(this);
        sessionManager = new SessionManager(this);
        switchBiometric = findViewById(R.id.switchBiometric);

        // Kiểm tra nếu thiết bị hỗ trợ sinh trắc học
        if (BiometricManager.from(this).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG)
                != BiometricManager.BIOMETRIC_SUCCESS) {
            switchBiometric.setEnabled(false);
            return;
        }

        // Lấy trạng thái hiện tại
        String currentEmail = sessionManager.getUserEmail();
        switchBiometric.setChecked(dbManager.isBiometricEnabled(currentEmail));

        setupBiometricPrompt();

        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Yêu cầu xác thực sinh trắc học và mật khẩu
                showPasswordConfirmDialog();
            } else {
                // Tắt tính năng sinh trắc học
                dbManager.disableBiometric(currentEmail);
                sessionManager.setBiometricEnabled(false);
            }
        });
    }

    private void setupBiometricPrompt() {
        BiometricPrompt.AuthenticationCallback authCallback =
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);

                        String currentEmail = sessionManager.getUserEmail();
                        if (currentEmail != null && !currentEmail.isEmpty()) {
                            dbManager.enableBiometric(currentEmail);
                            sessionManager.setBiometricEnabled(true);

                            runOnUiThread(() -> Toast.makeText(SettingsActivity.this,
                                    "Đã bật đăng nhập sinh trắc học", Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onAuthenticationError(int errorCode,
                                                      @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        switchBiometric.setChecked(false);
                        Toast.makeText(SettingsActivity.this,
                                "Lỗi xác thực: " + errString, Toast.LENGTH_SHORT).show();
                    }
                };

        biometricPrompt = new BiometricPrompt(this,
                ContextCompat.getMainExecutor(this), authCallback);

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực sinh trắc học")
                .setSubtitle("Sử dụng vân tay để bật tính năng đăng nhập sinh trắc học")
                .setNegativeButtonText("Hủy")
                .build();
    }

    private void showPasswordConfirmDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_confirm_password, null);
        EditText edtPassword = view.findViewById(R.id.edtPassword);

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận mật khẩu")
                .setView(view)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String password = edtPassword.getText().toString();
                    String email = sessionManager.getUserEmail();

                    if (dbManager.checkLogin(email, password)) {
                        // Mật khẩu đúng, tiến hành xác thực sinh trắc học
                        biometricPrompt.authenticate(promptInfo);
                    } else {
                        Toast.makeText(this, "Mật khẩu không đúng",
                                Toast.LENGTH_SHORT).show();
                        switchBiometric.setChecked(false);
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    switchBiometric.setChecked(false);
                })
                .setCancelable(false)
                .show();
    }
}