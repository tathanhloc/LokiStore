package com.tathanhloc.lokistore;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tathanhloc.lokistore.models.Category;
import java.util.List;

public class CategoryListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private DatabaseManager dbManager;
    private FloatingActionButton fabAddCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        initViews();
        setupDatabase();
        loadCategories();
        setupRecyclerView();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.categoryRecyclerView);
        fabAddCategory = findViewById(R.id.fabAddCategory);

        fabAddCategory.setOnClickListener(v -> showCategoryDialog(null));
    }

    private void setupDatabase() {
        dbManager = new DatabaseManager(this);
        dbManager.open();
    }

    private void loadCategories() {
        List<Category> categories = dbManager.getAllCategories();
        adapter = new CategoryAdapter(this, categories, category -> {
            // Xử lý khi click vào category
            showCategoryDialog(category);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final Category category = adapter.getCategoryAt(position);

                new AlertDialog.Builder(CategoryListActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa danh mục này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            dbManager.deleteCategory(category.getId());
                            adapter.removeItem(position);
                            Toast.makeText(CategoryListActivity.this, "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> {
                            adapter.notifyItemChanged(position);
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);

                    if (dX < 0) {  // Swipe left
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    private void showCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_category, null);
        builder.setView(view);

        TextInputEditText edtCategoryName = view.findViewById(R.id.edtCategoryName);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Set title và dữ liệu nếu là edit
        if (category != null) {
            ((TextView) view.findViewById(R.id.dialogTitle)).setText("Sửa loại sản phẩm");
            edtCategoryName.setText(category.getCategoryName());
        }

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = edtCategoryName.getText().toString().trim();
            if (name.isEmpty()) {
                edtCategoryName.setError("Vui lòng nhập tên danh mục");
                return;
            }

            if (category == null) {
                // Thêm mới
                String code = generateCategoryCode();
                Category newCategory = new Category();
                newCategory.setCategoryCode(code);
                newCategory.setCategoryName(name);
                long result = dbManager.addCategory(newCategory);
                if (result != -1) {
                    Toast.makeText(this, "Thêm danh mục thành công", Toast.LENGTH_SHORT).show();
                    loadCategories();  // Reload danh sách
                }
            } else {
                // Cập nhật
                category.setCategoryName(name);
                int result = dbManager.updateCategory(category);
                if (result > 0) {
                    Toast.makeText(this, "Cập nhật danh mục thành công", Toast.LENGTH_SHORT).show();
                    loadCategories();  // Reload danh sách
                }
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private String generateCategoryCode() {
        // Lấy số lượng category hiện tại + 1
        int count = dbManager.getCategoryCount() + 1;
        return String.format("LOKI%03d", count);  // Format: DM001, DM002, ...
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}