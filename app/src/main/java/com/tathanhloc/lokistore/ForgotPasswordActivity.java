package com.tathanhloc.lokistore;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText emailEditText;
    private MaterialButton resetPasswordButton;
    private ProgressBar progressBar;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateEmail()) {
                    sendResetPasswordEmail();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private boolean validateEmail() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Vui lòng nhập email");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email không hợp lệ");
            return false;
        }

        return true;
    }

    private void sendResetPasswordEmail() {
        String email = emailEditText.getText().toString().trim();

        // Hiển thị loading
        progressBar.setVisibility(View.VISIBLE);
        resetPasswordButton.setEnabled(false);

        // TODO: Thêm logic gửi email đặt lại mật khẩu thực tế ở đây
        // Giả lập delay 2 giây
        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        // Ẩn loading
                        progressBar.setVisibility(View.GONE);
                        resetPasswordButton.setEnabled(true);

                        // Hiển thị thông báo thành công
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Link đặt lại mật khẩu đã được gửi đến email của bạn",
                                Toast.LENGTH_LONG).show();

                        // Đóng màn hình sau 1 giây
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                    }
                                }, 1000);
                    }
                }, 2000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}