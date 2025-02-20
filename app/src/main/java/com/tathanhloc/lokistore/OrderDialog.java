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
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
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
    private AlertDialog dialog;

    public interface OnOrderSaveListener {
        void onOrderSave(Order order, List<OrderDetail> details);
    }

    public OrderDialog(@NonNull Context context, Order order, OnOrderSaveListener listener) {
        super(context);
        this.currentOrder = order;
        this.listener = listener;
        this.dbManager = new DatabaseManager(context);
        this.sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }
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
        rvOrderDetails.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        });
        rvOrderDetails.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(250)  // Điều chỉnh chiều cao tùy ý
        ));
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

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_product_selection, null);
        builder.setView(view);

        RecyclerView rvProducts = view.findViewById(R.id.rvProducts);
        EditText edtSearch = view.findViewById(R.id.edtSearch);

        // Setup RecyclerView
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        List<Product> products = dbManager.getAllProducts();

        ProductSelectionAdapter adapter = new ProductSelectionAdapter(getContext(), products, product -> {
            // Khi sản phẩm được chọn
            showQuantityDialog(product);
            dialog.dismiss();
        });

        rvProducts.setAdapter(adapter);

        // Setup search
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialog = builder.create();
        dialog.show();
    }
    private void showQuantityDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_quantity_input, null);
        builder.setView(view);

        EditText edtQuantity = view.findViewById(R.id.edtQuantity);
        TextView tvProductName = view.findViewById(R.id.tvProductName);
        TextView tvPrice = view.findViewById(R.id.tvPrice);

        tvProductName.setText(product.getProductName());
        tvPrice.setText(String.format("Đơn giá: %,.0f VNĐ", product.getPrice()));

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String quantityStr = edtQuantity.getText().toString();
            if (!quantityStr.isEmpty()) {
                int quantity = Integer.parseInt(quantityStr);
                addProductToOrder(product, quantity);
            }
        });
        builder.setNegativeButton("Hủy", null);

        builder.show();
    }
    private void addProductToOrder(Product product, int quantity) {
        // Kiểm tra xem sản phẩm đã tồn tại trong giỏ hàng chưa
        boolean productExists = false;
        for (OrderDetail existingDetail : orderDetails) {
            if (existingDetail.getProductId() == product.getId()) {
                // Nếu sản phẩm đã tồn tại, cập nhật số lượng
                int newQuantity = existingDetail.getQuantity() + quantity;
                existingDetail.setQuantity(newQuantity);
                productExists = true;
                detailAdapter.notifyDataSetChanged();
                break;
            }
        }

        // Nếu sản phẩm chưa tồn tại, thêm mới
        if (!productExists) {
            OrderDetail detail = new OrderDetail();
            detail.setProductId(product.getId());
            detail.setQuantity(quantity);
            detail.setPrice(product.getPrice());
            orderDetails.add(detail);
            detailAdapter.notifyItemInserted(orderDetails.size() - 1);
        }

        // Cập nhật tổng tiền
        updateTotalAmount();
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
            order.setStatus("PENDING"); // Set trạng thái mặc định cho đơn hàng mới
        } else {
            order = currentOrder; // Giữ nguyên thông tin cũ của đơn hàng
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