package com.tathanhloc.lokistore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardActivity extends AppCompatActivity {

    private CardView cardProducts;      // Quản lý sản phẩm
    private CardView cardCategories;    // Quản lý loại
    private CardView cardOrders;        // Quản lý đơn đặt hàng
    private TextView userNameText;      // Hiển thị tên người dùng

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        initializeViews();
        setupclickListeners();

        userNameText = findViewById(R.id.userNameText);
        String userName = getIntent().getStringExtra("USER_NAME");
        if (userName != null) {
            userNameText.setText(userName);
        }
    }

    private void initializeViews() {
        cardProducts = findViewById(R.id.cardProducts);
        cardCategories = findViewById(R.id.cardCategories);
        cardOrders = findViewById(R.id.cardOrders);

    }
    private void setupclickListeners() {
        cardProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ProductListActivity.class);
                startActivity(intent);
            }
        });

        cardCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, CategoryListActivity.class);
                startActivity(intent);
            }
        });

        cardOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, OrderListActivity.class);
                startActivity(intent);
            }
        });


    }
}

