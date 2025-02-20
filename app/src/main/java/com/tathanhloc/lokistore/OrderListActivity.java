package com.tathanhloc.lokistore;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tathanhloc.lokistore.models.Order;
import com.tathanhloc.lokistore.models.OrderDetail;
import java.util.ArrayList;
import java.util.List;
public class OrderListActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private DatabaseManager dbManager;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        dbManager = new DatabaseManager(this);
        initViews();
        loadOrders();
    }

    private void initViews() {


        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new OrderAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Setup SearchView
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Tìm kiếm đơn hàng...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });

        // Setup FAB
        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddOrderDialog());
    }

private void performSearch(String query) {
    List<Order> searchResults = dbManager.searchOrders(query);
    adapter.setOrders(searchResults);
}

    private void loadOrders() {
        List<Order> orders = dbManager.getAllOrders();
        adapter.setOrders(orders);
    }

    private void showAddOrderDialog() {
        OrderDialog dialog = new OrderDialog(this, null, (order, details) -> {
            long orderId = dbManager.addOrder(order);
            if (orderId > 0) {
                for (OrderDetail detail : details) {
                    detail.setOrderId((int) orderId);
                    dbManager.addOrderDetail(detail);
                }
                loadOrders();
                Toast.makeText(OrderListActivity.this,
                        "Thêm đơn hàng thành công", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("ORDER_ID", order.getOrderId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders(); // Reload orders when returning to this screen
    }
    @Override
    public void onOrderEdit(Order order) {
        if (!dbManager.isOrderEditable(order.getOrderDate())) {
            Toast.makeText(this, "Chỉ có thể chỉnh sửa đơn hàng trong ngày tạo",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if ("COMPLETED".equals(order.getStatus())) {
            Toast.makeText(this, "Không thể chỉnh sửa đơn hàng đã hoàn thành",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        OrderDialog dialog = new OrderDialog(this, order, (updatedOrder, details) -> {
            updatedOrder.setStatus(order.getStatus()); // Giữ nguyên trạng thái
            if (dbManager.updateOrder(updatedOrder)) {
                // Xóa chi tiết đơn hàng cũ
                dbManager.deleteOrderDetails(order.getOrderId());

                // Thêm chi tiết đơn hàng mới
                for (OrderDetail detail : details) {
                    detail.setOrderId(order.getOrderId());
                    dbManager.addOrderDetail(detail);
                }

                loadOrders();
                Toast.makeText(this, "Cập nhật đơn hàng thành công",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể cập nhật đơn hàng",
                        Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    @Override
    public void onOrderDelete(Order order) {
        if (!dbManager.isOrderEditable(order.getOrderDate())) {
            Toast.makeText(this, "Chỉ có thể xóa đơn hàng trong ngày tạo",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if ("COMPLETED".equals(order.getStatus())) {
            Toast.makeText(this, "Không thể xóa đơn hàng đã hoàn thành",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đơn hàng này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (dbManager.deleteOrder(order.getOrderId())) {
                        loadOrders();
                        Toast.makeText(this, "Đã xóa đơn hàng",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Không thể xóa đơn hàng",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}