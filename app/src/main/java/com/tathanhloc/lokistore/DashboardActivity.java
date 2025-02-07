package com.tathanhloc.lokistore;

import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DashboardActivity extends AppCompatActivity {
    // UI Components
    private CardView cardProducts;      // Quản lý sản phẩm
    private CardView cardCategories;    // Quản lý loại
    private CardView cardOrders;        // Quản lý đơn đặt hàng
    private TextView userNameText;      // Hiển thị tên người dùng
    private CardView cardStatistics;    // Thống kê
    private CardView cardLogout;        // Đăng xuất

    // Session manager để quản lý phiên đăng nhập
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (!sessionManager.isLoggedIn()) {
            // Nếu chưa đăng nhập, chuyển về màn hình login
            redirectToLogin();
            return;
        }

        // Khởi tạo và thiết lập các thành phần giao diện
        initializeViews();
        setupClickListeners();
        displayUserInfo();
    }

    private void initializeViews() {
        cardProducts = findViewById(R.id.cardProducts);
        cardCategories = findViewById(R.id.cardCategories);
        cardOrders = findViewById(R.id.cardOrders);
        cardStatistics = findViewById(R.id.cardStats);
        cardLogout = findViewById(R.id.cardLogout);
        userNameText = findViewById(R.id.userNameText);
    }

    private void displayUserInfo() {
        // Lấy thông tin người dùng từ SessionManager
        String userName = sessionManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            userNameText.setText(userName);
        }
    }

    private void setupClickListeners() {
        // Thiết lập click listener cho các card
        setupProductsCard();
        setupCategoriesCard();
        setupOrdersCard();
        setupStatisticsCard();
        setupLogoutCard();
    }

    private void setupProductsCard() {
        cardProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, ProductListActivity.class));
            }
        });
    }

    private void setupCategoriesCard() {
        cardCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, CategoryListActivity.class));
            }
        });
    }

    private void setupOrdersCard() {
        cardOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, OrderListActivity.class));
            }
        });
    }

    private void setupStatisticsCard() {
        cardStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, StatisticsActivity.class));
            }
        });
    }

    private void setupLogoutCard() {
        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performLogout() {
        try {
            // Thực hiện đăng xuất
            sessionManager.logout();

            // Tạo intent mới để chuyển về màn hình login
            Intent intent = new Intent(this, LoginActivity.class);
            // Xóa toàn bộ activity stack và tạo task mới
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Kết thúc activity hiện tại trước khi chuyển màn hình
            finish();

            // Chuyển về màn hình login
            startActivity(intent);

            // Hiển thị thông báo đăng xuất thành công
            Toast.makeText(getApplicationContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Có lỗi xảy ra khi đăng xuất", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void redirectToLogin() {
        try {
            // Đảm bảo xóa dữ liệu đăng nhập trước khi chuyển màn hình
            sessionManager.logout();

            // Tạo intent với flags mới
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Khởi động activity mới
            startActivity(intent);

            // Kết thúc activity hiện tại
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu có lỗi, thử phương pháp đơn giản hơn
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Chỉ kiểm tra khi sessionManager đã được khởi tạo
        if (sessionManager != null && !sessionManager.isLoggedIn()) {
            // Nếu không còn đăng nhập, chuyển về màn hình login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}