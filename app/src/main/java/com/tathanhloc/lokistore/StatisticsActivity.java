package com.tathanhloc.lokistore;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.tathanhloc.lokistore.models.CategoryStatistics;
import com.tathanhloc.lokistore.models.OrderStatistics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {
    private TextView tvTotalOrders, tvTotalRevenue;
    private LineChart lineChart;
    private PieChart pieChart;
    private DatabaseManager dbManager;

    private RadioGroup radioGroupTimeFilter;
    private Button btnStartDate, btnEndDate;

    private String startDate = "";
    private String endDate = "";
    private String currentFilter = "day";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Khởi tạo DatabaseManager
        dbManager = new DatabaseManager(this);

        // Khởi tạo views và setup
        initViews();
        setupToolbar();
        setupListeners();

        // Thiết lập ngày mặc định (từ đầu tháng đến hiện tại)
        Calendar cal = Calendar.getInstance();
        endDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());

        // Set về ngày đầu tháng
        cal.set(Calendar.DAY_OF_MONTH, 1);
        startDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());

        // Cập nhật text cho buttons
        btnStartDate.setText("Từ: " + startDate);
        btnEndDate.setText("Đến: " + endDate);

        // Thiết lập radio button mặc định và loại thống kê
        radioGroupTimeFilter.check(R.id.radioDay);
        currentFilter = "day";

        // Setup biểu đồ
        setupCharts();

        // Load dữ liệu thống kê
        updateStatistics();
    }

    private void setupCharts() {
        // Setup Line Chart
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        // Setup Pie Chart
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(35f);
        pieChart.setTransparentCircleRadius(40f);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setDrawEntryLabels(true);
    }

    private void initViews() {
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        lineChart = findViewById(R.id.lineChart);
        pieChart = findViewById(R.id.pieChart);
        radioGroupTimeFilter = findViewById(R.id.radioGroupTimeFilter);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thống kê");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadStatistics() {
        // Tải thống kê tổng quan
        loadOverviewStatistics();

        // Tải biểu đồ doanh thu
        loadRevenueChart();

        // Tải biểu đồ danh mục
        loadCategoryChart();
    }



    private void loadOverviewStatistics() {
        // Lấy tổng số đơn hàng
        int totalOrders = dbManager.getTotalOrders();
        tvTotalOrders.setText("Tổng số đơn hàng: " + totalOrders);

        // Lấy tổng doanh thu
        double totalRevenue = dbManager.getTotalRevenue();
        tvTotalRevenue.setText(String.format("Tổng doanh thu: %,.0f VNĐ", totalRevenue));
    }

    private void loadRevenueChart() {
        // Thiết lập biểu đồ doanh thu
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        // Lấy dữ liệu doanh thu theo ngày
        List<Entry> entries = new ArrayList<>();
        List<OrderStatistics> revenueData = dbManager.getRevenueByDate();

        for (int i = 0; i < revenueData.size(); i++) {
            OrderStatistics stat = revenueData.get(i);
            entries.add(new Entry(i, (float) stat.getRevenue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void loadCategoryChart() {
        // Thiết lập biểu đồ danh mục
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(35f);
        pieChart.setTransparentCircleRadius(40f);

        // Lấy dữ liệu theo danh mục
        List<PieEntry> entries = new ArrayList<>();
        List<CategoryStatistics> categoryData = dbManager.getRevenueByCategory();

        for (CategoryStatistics stat : categoryData) {
            entries.add(new PieEntry((float) stat.getRevenue(), stat.getCategoryName()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Danh mục");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupListeners() {
        radioGroupTimeFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioDay) {
                currentFilter = "day";
            } else if (checkedId == R.id.radioMonth) {
                currentFilter = "month";
            } else if (checkedId == R.id.radioYear) {
                currentFilter = "year";
            }
            updateStatistics();
        });

        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));
    }
    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();

        // Nếu đã có ngày được chọn trước đó, sử dụng ngày đó
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(isStartDate ? startDate : endDate);
            if (date != null) {
                calendar.setTime(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(selectedDate.getTime());

                    if (isStartDate) {
                        startDate = date;
                        btnStartDate.setText("Từ: " + date);
                    } else {
                        endDate = date;
                        btnEndDate.setText("Đến: " + date);
                    }

                    updateStatistics();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void updateStatistics() {
        if (startDate.isEmpty() || endDate.isEmpty()) {
            return;
        }

        try {
            // Log để debug
            Log.d("Statistics", "Updating statistics for period: " + startDate + " to " + endDate);

            // Cập nhật thống kê tổng quan
            int totalOrders = dbManager.getTotalOrdersByTimeRange(startDate, endDate);
            double totalRevenue = dbManager.getTotalRevenueByTimeRange(startDate, endDate);

            Log.d("Statistics", "Total Orders: " + totalOrders);
            Log.d("Statistics", "Total Revenue: " + totalRevenue);

            tvTotalOrders.setText("Tổng số đơn hàng: " + totalOrders);
            tvTotalRevenue.setText(String.format("Tổng doanh thu: %,.0f VNĐ", totalRevenue));

            // Cập nhật biểu đồ
            updateRevenueChart();
            updateCategoryChart();

        } catch (Exception e) {
            Log.e("Statistics", "Error updating statistics", e);
            Toast.makeText(this, "Lỗi khi cập nhật dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRevenueChart() {
        List<OrderStatistics> statistics = dbManager.getRevenueByTimeRange(startDate, endDate, currentFilter);
        if (statistics == null || statistics.isEmpty()) {
            lineChart.clear();
            lineChart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < statistics.size(); i++) {
            OrderStatistics stat = statistics.get(i);
            entries.add(new Entry(i, (float) stat.getRevenue()));
            labels.add(stat.getDate());
        }

        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%,.0f VNĐ", value);
            }
        });

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels) {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size() && labels.get(index) != null) {
                    return labels.get(index);
                } else {
                    return ""; // Return an empty string if the label is null or out of bounds
                }
            }
        });
        lineChart.getXAxis().setLabelRotationAngle(45f); // Xoay nhãn 45 độ để dễ đọc
        lineChart.animateX(1000);
        lineChart.invalidate();
    }
    private void updateCategoryChart() {
        // Lấy dữ liệu thống kê theo danh mục trong khoảng thời gian
        List<CategoryStatistics> categoryData = dbManager.getCategoryRevenueByTimeRange(startDate, endDate);
        List<PieEntry> entries = new ArrayList<>();

        // Tạo entries cho biểu đồ
        for (CategoryStatistics stat : categoryData) {
            // Chỉ thêm các danh mục có doanh thu > 0
            if (stat.getRevenue() > 0) {
                entries.add(new PieEntry((float) stat.getRevenue(), stat.getCategoryName()));
            }
        }

        // Tạo dataset
        PieDataSet dataSet = new PieDataSet(entries, "Danh mục");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        // Thiết lập dữ liệu cho biểu đồ
        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));

        // Cập nhật biểu đồ
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(40f);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setDrawEntryLabels(true);

        // Thêm chú thích
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        pieChart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);

        pieChart.invalidate(); // Refresh biểu đồ
    }
}