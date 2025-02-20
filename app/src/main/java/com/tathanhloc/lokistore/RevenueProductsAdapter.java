package com.tathanhloc.lokistore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.tathanhloc.lokistore.models.CategoryStatistics;

import java.util.List;

public class RevenueProductsAdapter extends RecyclerView.Adapter<RevenueProductsAdapter.ViewHolder> {
    private final List<CategoryStatistics> revenueProducts;

    public RevenueProductsAdapter(List<CategoryStatistics> revenueProducts) {
        this.revenueProducts = revenueProducts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_revenue_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CategoryStatistics product = revenueProducts.get(position);
        holder.tvProductName.setText(product.getCategoryName());
        holder.tvRevenue.setText(String.format("%,.0f VNĐ", product.getRevenue()));
    }

    @Override
    public int getItemCount() {
        return revenueProducts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvProductName;
        public TextView tvRevenue;

        public ViewHolder(View view) {
            super(view);
            tvProductName = view.findViewById(R.id.tvProductName);
            tvRevenue = view.findViewById(R.id.tvRevenue);
        }
    }
}