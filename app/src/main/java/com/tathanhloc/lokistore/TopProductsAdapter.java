package com.tathanhloc.lokistore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tathanhloc.lokistore.models.CategoryStatistics;

import java.util.List;

public class TopProductsAdapter extends RecyclerView.Adapter<TopProductsAdapter.ViewHolder> {
    private List<CategoryStatistics> topProducts;

    public TopProductsAdapter(List<CategoryStatistics> topProducts) {
        this.topProducts = topProducts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryStatistics product = topProducts.get(position);
        holder.tvProductName.setText(product.getCategoryName()); // Tên sản phẩm
        holder.tvProductRevenue.setText(String.format("%,.0f VNĐ", product.getRevenue()));
        holder.tvProductRank.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() {
        return topProducts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvProductRevenue;
        TextView tvProductRank;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductRevenue = itemView.findViewById(R.id.tvRevenue);
            tvProductRank = itemView.findViewById(R.id.tvRank);
        }
    }
}