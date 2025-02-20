package com.tathanhloc.lokistore;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

public class DashboardStatisticsActivity extends AppCompatActivity {

    private CardView cardProductRevenue;
    private CardView cardCategoryRevenue;
    private CardView cardDailyRevenue;
    private CardView cardMonthlyRevenue;
    private CardView cardYearlyRevenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_statistics);

        initViews();
        setupListeners();
        setupToolbar();
    }

    private void initViews() {
        cardProductRevenue = findViewById(R.id.cardProductRevenue);
        cardCategoryRevenue = findViewById(R.id.cardCategoryRevenue);
        cardDailyRevenue = findViewById(R.id.cardDailyRevenue);
        cardMonthlyRevenue = findViewById(R.id.cardMonthlyRevenue);
        cardYearlyRevenue = findViewById(R.id.cardYearlyRevenue);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thống kê doanh thu");
        }
    }

    private void setupListeners() {
        cardProductRevenue.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("STATISTICS_TYPE", "PRODUCT");
            startActivity(intent);
        });

        cardCategoryRevenue.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("STATISTICS_TYPE", "CATEGORY");
            startActivity(intent);
        });

        cardDailyRevenue.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("STATISTICS_TYPE", "DAILY");
            startActivity(intent);
        });

        cardMonthlyRevenue.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("STATISTICS_TYPE", "MONTHLY");
            startActivity(intent);
        });

        cardYearlyRevenue.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("STATISTICS_TYPE", "YEARLY");
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}