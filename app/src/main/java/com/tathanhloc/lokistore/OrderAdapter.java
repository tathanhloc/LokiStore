package com.tathanhloc.lokistore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.tathanhloc.lokistore.models.Order;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orders;
    private OnOrderClickListener listener;
    private DatabaseManager dbManager;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onOrderEdit(Order order);
        void onOrderDelete(Order order);
    }

    public OrderAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
        this.dbManager = new DatabaseManager(context);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_card, parent, false);
        return new OrderViewHolder(view);
    }

@Override
public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
    Order order = orders.get(position);
    String userName = dbManager.getUserFullNameById(order.getUserId());

    holder.tvOrderCode.setText(order.getOrderCode());
    holder.tvOrderDate.setText("Ngày đặt: " + order.getOrderDate());
    holder.tvDeliveryDate.setText("Ngày giao: " + order.getDeliveryDate());
    holder.tvTotalAmount.setText(String.format("%,.0f VNĐ", order.getTotalAmount()));
    holder.tvCreatedBy.setText("Người tạo: " + (userName != null ? userName : "Không xác định"));

    if (order.getNote() != null && !order.getNote().isEmpty()) {
        holder.tvNote.setText("Ghi chú: " + order.getNote());
        holder.tvNote.setVisibility(View.VISIBLE);
    } else {
        holder.tvNote.setVisibility(View.GONE);
    }

    boolean isEditable = isOrderEditable(order.getOrderDate());
    holder.ivEditStatus.setImageResource(isEditable ? R.drawable.ic_edit_enabled : R.drawable.ic_edit_disabled);

    holder.cardView.setOnClickListener(v -> {
        if (listener != null) {
            listener.onOrderClick(order);
        }
    });

    String status = order.getStatus();
    if ("COMPLETED".equals(status)) {
        holder.tvStatus.setText("Hoàn thành");
        holder.tvStatus.setTextColor(Color.GREEN);
    } else {
        holder.tvStatus.setText("Đang xử lý");
        holder.tvStatus.setTextColor(Color.BLUE);
    }

    if ("COMPLETED".equals(status)) {
        holder.tvStatus.setText("Hoàn thành");
        holder.tvStatus.setTextColor(Color.GREEN);
    } else {
        holder.tvStatus.setText("Đang xử lý");
        holder.tvStatus.setTextColor(Color.BLUE);
    }

    holder.cardView.setOnLongClickListener(v -> {
        showPopupMenu(v, order);
        return true;
    });
}
    private void showPopupMenu(View view, Order order) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.menu_order_item);

        // Kiểm tra điều kiện để enable/disable menu items
        boolean isEditable = isOrderEditable(order.getOrderDate());
        popup.getMenu().findItem(R.id.menu_edit).setEnabled(isEditable);
        popup.getMenu().findItem(R.id.menu_delete).setEnabled(isEditable);

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_edit) {
                if (isEditable) {
                    if (listener != null) listener.onOrderEdit(order);
                } else {
                    Toast.makeText(context, "Không thể chỉnh sửa đơn hàng trong quá khứ",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (item.getItemId() == R.id.menu_delete) {
                if (isEditable) {
                    if (listener != null) listener.onOrderDelete(order);
                } else {
                    Toast.makeText(context, "Không thể xóa đơn hàng trong quá khứ",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        popup.show();
    }
    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    private boolean isOrderEditable(String orderDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(orderDate);
            Date today = new Date();
            return sdf.format(date).equals(sdf.format(today));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvOrderCode;
        TextView tvOrderDate;
        TextView tvDeliveryDate;
        TextView tvTotalAmount;
        TextView tvCreatedBy;
        TextView tvNote;
        TextView tvStatus;
        ImageView ivEditStatus;

        OrderViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.cardView);
            tvOrderCode = view.findViewById(R.id.tvOrderCode);
            tvOrderDate = view.findViewById(R.id.tvOrderDate);
            tvDeliveryDate = view.findViewById(R.id.tvDeliveryDate);
            tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
            tvCreatedBy = view.findViewById(R.id.tvCreatedBy);
            tvNote = view.findViewById(R.id.tvNote);
            tvStatus = view.findViewById(R.id.tvStatus);
            ivEditStatus = view.findViewById(R.id.ivEditStatus);
        }
    }
}