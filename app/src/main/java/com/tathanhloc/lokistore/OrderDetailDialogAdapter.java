package com.tathanhloc.lokistore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tathanhloc.lokistore.models.OrderDetail;
import com.tathanhloc.lokistore.models.Product;
import java.util.List;

public class OrderDetailDialogAdapter extends RecyclerView.Adapter<OrderDetailDialogAdapter.ViewHolder> {
    private final List<OrderDetail> details;
    private final OnItemDeleteListener listener;
    private final DatabaseManager dbManager;
    private final Context context;

    public interface OnItemDeleteListener {
        void onDelete(int position);
    }

    public OrderDetailDialogAdapter(Context context, List<OrderDetail> details, OnItemDeleteListener listener) {
        this.context = context;
        this.details = details;
        this.listener = listener;
        this.dbManager = new DatabaseManager(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail_dialog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDetail detail = details.get(position);
        Product product = dbManager.getProduct(detail.getProductId());

        holder.tvProductName.setText(product.getProductName());
        holder.tvPrice.setText(String.format("%,.0f VNĐ", detail.getPrice()));
        holder.tvQuantity.setText("x" + detail.getQuantity());
        holder.tvAmount.setText(String.format("%,.0f VNĐ", detail.getAmount()));

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvProductName;
        final TextView tvPrice;
        final TextView tvQuantity;
        final TextView tvAmount;
        final ImageButton btnDelete;

        ViewHolder(View view) {
            super(view);
            tvProductName = view.findViewById(R.id.tvProductName);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvQuantity = view.findViewById(R.id.tvQuantity);
            tvAmount = view.findViewById(R.id.tvAmount);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }
}