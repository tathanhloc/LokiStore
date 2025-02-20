package com.tathanhloc.lokistore;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private TextView tvOrderCode, tvOrderDate, tvDeliveryDate, tvTotalAmount, tvNote, tvCreatedBy, tvStatus;
    private RecyclerView rvOrderDetails;
    private OrderDetailAdapter detailAdapter;
    private DatabaseManager dbManager;
    private Order currentOrder;
    private Button btnCompleteOrder;
    private LinearLayout bottomButtonContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        dbManager = new DatabaseManager(this);
        initViews();

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
        tvStatus = findViewById(R.id.tvStatus);
        btnCompleteOrder = findViewById(R.id.btnCompleteOrder);
        bottomButtonContainer = findViewById(R.id.bottomButtonContainer);

        btnCompleteOrder.setOnClickListener(v -> showCompleteOrderDialog());

        // Thiết lập RecyclerView
        rvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        detailAdapter = new OrderDetailAdapter(this);  // Sử dụng adapter mới
        rvOrderDetails.setAdapter(detailAdapter);
    }

    private void showCompleteOrderDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hoàn thành")
                .setMessage("Bạn có chắc chắn muốn xác nhận hoàn thành đơn hàng này không?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    completeOrder();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    private void completeOrder() {
        if (currentOrder != null) {
            if (dbManager.updateOrderStatus(currentOrder.getOrderId(), "COMPLETED")) {
                // Cập nhật lại đối tượng currentOrder với trạng thái mới
                currentOrder = dbManager.getOrder(currentOrder.getOrderId());

                // Gọi displayOrderInfo để cập nhật UI
                displayOrderInfo();

                // Load lại chi tiết đơn hàng
                loadOrderItems(currentOrder.getOrderId());

                Toast.makeText(this, "Đã cập nhật trạng thái đơn hàng", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể cập nhật trạng thái đơn hàng", Toast.LENGTH_SHORT).show();
            }
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
            tvOrderCode.setText("Mã đơn: " + currentOrder.getOrderCode());
            tvOrderDate.setText("Ngày đặt: " + currentOrder.getOrderDate());
            tvDeliveryDate.setText("Ngày giao: " + currentOrder.getDeliveryDate());
            tvTotalAmount.setText(String.format("Tổng tiền: %,.0f VNĐ", currentOrder.getTotalAmount()));

            // Hiển thị trạng thái
            if ("COMPLETED".equals(currentOrder.getStatus())) {
                tvStatus.setText("Trạng thái: Đã hoàn thành");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                bottomButtonContainer.setVisibility(View.GONE);
            } else {
                tvStatus.setText("Trạng thái: Đang xử lý");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                bottomButtonContainer.setVisibility(View.VISIBLE);
            }

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