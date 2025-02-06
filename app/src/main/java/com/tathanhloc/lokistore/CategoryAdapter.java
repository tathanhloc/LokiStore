package com.tathanhloc.lokistore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tathanhloc.lokistore.models.Category;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private Context context;
    private List<Category> categories;
    private OnCategoryClickListener listener;

    // Interface để xử lý sự kiện click
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        // Hiển thị thông tin category
        holder.tvCategoryCode.setText(category.getCategoryCode());
        holder.tvCategoryName.setText(category.getCategoryName());

        // Xử lý sự kiện click để edit
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    // Phương thức hỗ trợ cho swipe delete
    public Category getCategoryAt(int position) {
        return categories.get(position);
    }

    public void removeItem(int position) {
        categories.remove(position);
        notifyItemRemoved(position);
    }

    // Cập nhật danh sách
    public void updateData(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    // ViewHolder class
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryCode;
        TextView tvCategoryName;

        CategoryViewHolder(View itemView) {
            super(itemView);
            tvCategoryCode = itemView.findViewById(R.id.tvCategoryCode);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }

}