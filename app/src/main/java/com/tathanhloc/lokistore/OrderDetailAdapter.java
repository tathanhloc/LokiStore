package com.tathanhloc.lokistore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tathanhloc.lokistore.models.OrderDetail;
import com.tathanhloc.lokistore.models.Product;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    private Context context;
    private List<OrderDetail> orderDetails;
    private DatabaseManager dbManager;

    public OrderDetailAdapter(Context context) {
        this.context = context;
        this.orderDetails = new ArrayList<>();
        this.dbManager = new DatabaseManager(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetail detail = orderDetails.get(position);
        Product product = dbManager.getProduct(detail.getProductId());

        if (product != null) {
            holder.tvProductName.setText(product.getProductName());
            holder.tvPrice.setText(String.format("Đơn giá: %,.0f VNĐ", detail.getPrice()));
            holder.tvQuantity.setText(String.format("Số lượng: %d", detail.getQuantity()));
            holder.tvAmount.setText(String.format("Thành tiền: %,.0f VNĐ", detail.getAmount()));
        }
    }

    @Override
    public int getItemCount() {
        return orderDetails != null ? orderDetails.size() : 0;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvPrice;
        TextView tvQuantity;
        TextView tvAmount;

        ViewHolder(View view) {
            super(view);
            tvProductName = view.findViewById(R.id.tvProductName);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvQuantity = view.findViewById(R.id.tvQuantity);
            tvAmount = view.findViewById(R.id.tvAmount);
        }
    }
}