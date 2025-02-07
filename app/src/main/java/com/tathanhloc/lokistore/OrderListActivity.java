package com.tathanhloc.lokistore;

import android.content.Intent;
import android.os.Bundle;
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
        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý đơn hàng");
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new OrderAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Setup FAB
        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddOrderDialog());
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
}