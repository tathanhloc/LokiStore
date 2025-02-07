package com.tathanhloc.lokistore;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tathanhloc.lokistore.models.Order;
import com.tathanhloc.lokistore.models.OrderDetail;
import com.tathanhloc.lokistore.models.Product;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDialog extends Dialog {
    private TextView dialogTitle;
    private TextView tvOrderCode;
    private TextView tvOrderDate;
    private EditText edtDeliveryDate;
    private EditText edtNote;
    private RecyclerView rvOrderDetails;
    private TextView tvTotalAmount;
    private Button btnAddItem;
    private Button btnCancel;
    private Button btnSave;

    private OrderDetailDialogAdapter detailAdapter;
    private List<OrderDetail> orderDetails;
    private Order currentOrder;
    private DatabaseManager dbManager;
    private SimpleDateFormat sdf;
    private OnOrderSaveListener listener;

    public interface OnOrderSaveListener {
        void onOrderSave(Order order, List<OrderDetail> details);
    }

    public OrderDialog(@NonNull Context context, Order order, OnOrderSaveListener listener) {
        super(context);
        this.currentOrder = order;
        this.listener = listener;
        this.dbManager = new DatabaseManager(context);
        this.sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_order);

        initViews();
        setupListeners();
        initData();
    }

    private void initViews() {
        dialogTitle = findViewById(R.id.dialogTitle);
        tvOrderCode = findViewById(R.id.tvOrderCode);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        edtDeliveryDate = findViewById(R.id.edtDeliveryDate);
        edtNote = findViewById(R.id.edtNote);
        rvOrderDetails = findViewById(R.id.rvOrderDetails);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        // Setup RecyclerView
        rvOrderDetails.setLayoutManager(new LinearLayoutManager(getContext()));
        orderDetails = new ArrayList<>();
        detailAdapter = new OrderDetailDialogAdapter(
                getContext(),  // Thêm context vào đây
                orderDetails,
                position -> {  // OnItemDeleteListener
                    orderDetails.remove(position);
                    detailAdapter.notifyItemRemoved(position);
                    updateTotalAmount();
                }
        );
        rvOrderDetails.setAdapter(detailAdapter);
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveOrder());
        btnAddItem.setOnClickListener(v -> showAddItemDialog());
        edtDeliveryDate.setOnClickListener(v -> showDatePicker());
    }

    private void initData() {
        if (currentOrder == null) {
            // Tạo đơn hàng mới
            dialogTitle.setText("Thêm đơn hàng mới");
            String newOrderCode = dbManager.generateOrderCode();
            tvOrderCode.setText("Mã đơn: " + newOrderCode);
            tvOrderDate.setText("Ngày đặt: " + sdf.format(new Date()));
        } else {
            // Chỉnh sửa đơn hàng
            dialogTitle.setText("Chỉnh sửa đơn hàng");
            tvOrderCode.setText("Mã đơn: " + currentOrder.getOrderCode());
            tvOrderDate.setText("Ngày đặt: " + currentOrder.getOrderDate());
            edtDeliveryDate.setText(currentOrder.getDeliveryDate());
            edtNote.setText(currentOrder.getNote());

            // Load chi tiết đơn hàng
            orderDetails.addAll(dbManager.getOrderDetails(currentOrder.getOrderId()));
            detailAdapter.notifyDataSetChanged();
            updateTotalAmount();
        }
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    edtDeliveryDate.setText(sdf.format(selectedDate.getTime()));
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_order_item, null);

        AutoCompleteTextView spinnerProduct = view.findViewById(R.id.spinnerProduct);
        EditText edtQuantity = view.findViewById(R.id.edtQuantity);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvAmount = view.findViewById(R.id.tvAmount);

        List<Product> products = dbManager.getAllProducts();
        ArrayAdapter<Product> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, products);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduct.setAdapter(adapter);

        Product[] selectedProduct = new Product[1];
        spinnerProduct.setOnItemClickListener((parent, view1, position, id) -> {
            selectedProduct[0] = products.get(position);
            tvPrice.setText(String.format("Đơn giá: %,.0f VNĐ", selectedProduct[0].getPrice()));

            // Tính thành tiền nếu đã nhập số lượng
            String quantityStr = edtQuantity.getText().toString();
            if (!quantityStr.isEmpty()) {
                int quantity = Integer.parseInt(quantityStr);
                double amount = selectedProduct[0].getPrice() * quantity;
                tvAmount.setText(String.format("Thành tiền: %,.0f VNĐ", amount));
            }
        });

        edtQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (selectedProduct[0] != null && !s.toString().isEmpty()) {
                    try {
                        int quantity = Integer.parseInt(s.toString());
                        double amount = selectedProduct[0].getPrice() * quantity;
                        tvAmount.setText(String.format("Thành tiền: %,.0f VNĐ", amount));
                    } catch (NumberFormatException e) {
                        tvAmount.setText("Thành tiền: 0 VNĐ");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        builder.setView(view)
                .setTitle("Thêm sản phẩm")
                .setPositiveButton("Thêm", (dialog, which) -> {
                    if (selectedProduct[0] == null) {
                        Toast.makeText(getContext(), "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String quantityStr = edtQuantity.getText().toString();
                    if (quantityStr.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int quantity = Integer.parseInt(quantityStr);
                    OrderDetail detail = new OrderDetail();
                    detail.setProductId(selectedProduct[0].getId());
                    detail.setQuantity(quantity);
                    detail.setPrice(selectedProduct[0].getPrice());

                    orderDetails.add(detail);
                    detailAdapter.notifyItemInserted(orderDetails.size() - 1);
                    updateTotalAmount();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateTotalAmount() {
        double total = 0;
        for (OrderDetail detail : orderDetails) {
            total += detail.getAmount();
        }
        tvTotalAmount.setText(String.format("Tổng tiền: %,.0f VNĐ", total));
    }

    private void saveOrder() {
        if (orderDetails.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng thêm sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edtDeliveryDate.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ngày giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        Order order;
        if (currentOrder == null) {
            order = new Order();
            order.setOrderCode(dbManager.generateOrderCode());
            order.setOrderDate(sdf.format(new Date()));
            order.setUserId(getCurrentUserId());
        } else {
            order = currentOrder;
        }

        order.setDeliveryDate(edtDeliveryDate.getText().toString());
        order.setNote(edtNote.getText().toString());

        double totalAmount = 0;
        for (OrderDetail detail : orderDetails) {
            totalAmount += detail.getAmount();
        }
        order.setTotalAmount(totalAmount);

        if (listener != null) {
            listener.onOrderSave(order, orderDetails);
        }
        dismiss();
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getInt("userId", 1); // Default to 1 if not found
    }
}