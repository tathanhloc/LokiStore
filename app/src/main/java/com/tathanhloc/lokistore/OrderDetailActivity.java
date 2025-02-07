package com.tathanhloc.lokistore;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tathanhloc.lokistore.models.Order;
import com.tathanhloc.lokistore.models.OrderDetail;

import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView tvOrderCode, tvOrderDate, tvDeliveryDate, tvTotalAmount, tvNote, tvCreatedBy;
    private RecyclerView rvOrderDetails;
    private OrderDetailAdapter detailAdapter;  // Sử dụng OrderDetailAdapter
    private DatabaseManager dbManager;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Khởi tạo DatabaseManager
        dbManager = new DatabaseManager(this);

        // Khởi tạo views
        initViews();

        // Thiết lập toolbar
        setupToolbar();

        // Lấy orderId từ intent và load dữ liệu
        int orderId = getIntent().getIntExtra("ORDER_ID", -1);
        if (orderId != -1) {
            loadOrderDetails(orderId);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvOrderCode = findViewById(R.id.tvOrderCode);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvDeliveryDate = findViewById(R.id.tvDeliveryDate);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvNote = findViewById(R.id.tvNote);
        tvCreatedBy = findViewById(R.id.tvCreatedBy);
        rvOrderDetails = findViewById(R.id.rvOrderDetails);

        // Thiết lập RecyclerView
        rvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        detailAdapter = new OrderDetailAdapter(this);  // Sử dụng adapter mới
        rvOrderDetails.setAdapter(detailAdapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đơn hàng");
        }
    }


    private void loadOrderDetails(int orderId) {
        // Load thông tin đơn hàng
        currentOrder = dbManager.getOrder(orderId);
        if (currentOrder != null) {
            displayOrderInfo();
            loadOrderItems(orderId);
        }
    }

    private void displayOrderInfo() {
        if (currentOrder != null) {
            // Log để kiểm tra thông tin
            Log.d("OrderDetail", "OrderId: " + currentOrder.getOrderId());
            Log.d("OrderDetail", "UserId: " + currentOrder.getUserId());

            tvOrderCode.setText("Mã đơn: " + currentOrder.getOrderCode());
            tvOrderDate.setText("Ngày đặt: " + currentOrder.getOrderDate());
            tvDeliveryDate.setText("Ngày giao: " + currentOrder.getDeliveryDate());
            tvTotalAmount.setText(String.format("Tổng tiền: %,.0f VNĐ", currentOrder.getTotalAmount()));

            // Xử lý phần người tạo
            int userId = currentOrder.getUserId();
            String createdBy = dbManager.getUserFullNameById(userId);
            Log.d("OrderDetail", "CreatedBy: " + createdBy);  // Log để kiểm tra

            if (createdBy != null && !createdBy.isEmpty()) {
                tvCreatedBy.setText("Người tạo: " + createdBy);
                tvCreatedBy.setVisibility(View.VISIBLE);
            } else {
                tvCreatedBy.setText("Người tạo: Không xác định");
                Log.w("OrderDetail", "Could not find user name for ID: " + userId);
            }

            // Xử lý ghi chú
            if (currentOrder.getNote() != null && !currentOrder.getNote().isEmpty()) {
                tvNote.setText("Ghi chú: " + currentOrder.getNote());
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }
        } else {
            Log.e("OrderDetail", "currentOrder is null");
        }
    }

    private void loadOrderItems(int orderId) {
        // Load chi tiết đơn hàng
        List<OrderDetail> details = dbManager.getOrderDetails(orderId);
        detailAdapter.setOrderDetails(details);
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