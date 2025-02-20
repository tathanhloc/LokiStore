package com.tathanhloc.lokistore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tathanhloc.lokistore.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ViewHolder> {
    private Context context;
    private List<Product> products;
    private List<Product> filteredProducts;
    private OnProductSelectedListener listener;

    public interface OnProductSelectedListener {
        void onProductSelected(Product product);
    }

    public ProductSelectionAdapter(Context context, List<Product> products, OnProductSelectedListener listener) {
        this.context = context;
        this.products = products;
        this.filteredProducts = new ArrayList<>(products);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = filteredProducts.get(position);

        // Load hình ảnh sản phẩm
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(product.getImagePath());
            if (bitmap != null) {
                holder.imgProduct.setImageBitmap(bitmap);
            } else {
                holder.imgProduct.setImageResource(R.drawable.placeholder_image);
            }
        } else {
            holder.imgProduct.setImageResource(R.drawable.placeholder_image);
        }

        holder.tvProductName.setText(product.getProductName());
        holder.tvProductPrice.setText(String.format("%,.0f VNĐ", product.getPrice()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductSelected(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredProducts.size();
    }

    public void filter(String query) {
        filteredProducts.clear();
        if (query.isEmpty()) {
            filteredProducts.addAll(products);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Product product : products) {
                if (product.getProductName().toLowerCase().contains(lowerQuery) ||
                        product.getProductCode().toLowerCase().contains(lowerQuery)) {
                    filteredProducts.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        TextView tvProductPrice;

        ViewHolder(View view) {
            super(view);
            imgProduct = view.findViewById(R.id.imgProduct);
            tvProductName = view.findViewById(R.id.tvProductName);
            tvProductPrice = view.findViewById(R.id.tvProductPrice);
        }
    }
}