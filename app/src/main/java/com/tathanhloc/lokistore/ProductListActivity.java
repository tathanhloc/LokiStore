package com.tathanhloc.lokistore;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.tathanhloc.lokistore.models.Category;
import com.tathanhloc.lokistore.models.Product;
import android.widget.AdapterView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import androidx.cursoradapter.widget.CursorAdapter;

public class ProductListActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private static final String IMAGE_DIR = "product_images";

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private Spinner categorySpinner;
    private DatabaseManager dbManager;
    private AlertDialog dialog;
    private ImageView imgProductPhoto;
    private String selectedImagePath;
    private File imageDir;

    private SearchView searchView;
    private List<Product> allProducts = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private CursorAdapter suggestionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Tạo thư mục lưu ảnh
        imageDir = new File(getFilesDir(), IMAGE_DIR);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        initViews();
        setupDatabase();
        loadCategories();
        loadProducts();
        setupListeners();
        setupSearchView();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.productRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        categorySpinner = findViewById(R.id.categorySpinner);
        findViewById(R.id.fabAddProduct).setOnClickListener(v -> showProductDialog(null));
    }

    private void setupDatabase() {
        dbManager = new DatabaseManager(this);
        dbManager.open();
    }

    private void loadCategories() {
        List<Category> categories = dbManager.getAllCategories();
        Category selectAllCategory = new Category();
        selectAllCategory.setId(-1);
        selectAllCategory.setCategoryName("Tất cả");
        categories.add(0, selectAllCategory);

        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void loadProducts() {
        allProducts = dbManager.getAllProducts();
        adapter = new ProductAdapter(this, allProducts, new ProductAdapter.ProductListener() {
            @Override
            public void onEditClick(Product product) {
                showProductDialog(product);
            }

            @Override
            public void onDeleteClick(Product product) {
                showDeleteConfirmation(product);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category category = (Category) parent.getItemAtPosition(position);
                List<Product> products;
                if (category.getId() == -1) {
                    products = dbManager.getAllProducts();
                } else {
                    products = dbManager.getProductsByCategory(category.getId());
                }
                adapter.updateData(products);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private boolean isImageFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.startsWith("image/");
    }

    private Bitmap resizeImage(Bitmap bitmap) {
        int maxSize = 1024;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        if (ratio < 1) {
            width = Math.round(width * ratio);
            height = Math.round(height * ratio);
            return Bitmap.createScaledBitmap(bitmap, width, height, true);
        }
        return bitmap;
    }

    private void deleteOldImage(String imagePath) {
        if (imagePath != null) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
    }

    private void showProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_product, null);
        builder.setView(view);

        TextInputEditText edtProductCode = view.findViewById(R.id.edtProductCode);
        TextInputEditText edtProductName = view.findViewById(R.id.edtProductName);
        TextInputEditText edtPrice = view.findViewById(R.id.edtPrice);
        TextInputEditText edtDescription = view.findViewById(R.id.edtDescription);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        imgProductPhoto = view.findViewById(R.id.imgProductPhoto);
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        List<Category> categories = dbManager.getAllCategories();
        if (categories == null || categories.isEmpty()) {
            Toast.makeText(this, "Không có danh mục sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        if (product != null) {
            edtProductCode.setText(product.getProductCode());
            edtProductName.setText(product.getProductName());
            edtPrice.setText(String.valueOf(product.getPrice()));
            edtDescription.setText(product.getDescription());

            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                selectedImagePath = product.getImagePath();
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                if (bitmap != null) {
                    imgProductPhoto.setImageBitmap(bitmap);
                }
            }

            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == product.getCategoryId()) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }

        btnSelectImage.setOnClickListener(v -> pickImage());

        btnSave.setOnClickListener(v -> {
            String code = edtProductCode.getText().toString().trim();
            String name = edtProductName.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();

            Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
            if (selectedCategory == null) {
                Toast.makeText(this, "Vui lòng chọn loại sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }

            int categoryId = selectedCategory.getId();
            if (categoryId <= 0) {
                Toast.makeText(this, "Loại sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (validateInput(code, name, priceStr)) {
                double price = Double.parseDouble(priceStr);

                if (product == null) {
                    Product newProduct = new Product(
                            0, categoryId, code, name, price, description,
                            selectedImagePath, 1);
                    dbManager.addProduct(newProduct);
                    Toast.makeText(this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                } else {
                    String oldImagePath = product.getImagePath();
                    if (selectedImagePath != null && !selectedImagePath.equals(oldImagePath)) {
                        deleteOldImage(oldImagePath);
                    }

                    product.setProductCode(code);
                    product.setProductName(name);
                    product.setPrice(price);
                    product.setDescription(description);
                    product.setCategoryId(categoryId);
                    product.setImagePath(selectedImagePath);
                    dbManager.updateProduct(product);
                    Toast.makeText(this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                }

                loadProducts();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog = builder.create();
        dialog.show();
    }

    private boolean validateInput(String code, String name, String price) {
        if (code.isEmpty() || name.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Double.parseDouble(price);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showDeleteConfirmation(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa sản phẩm này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteOldImage(product.getImagePath());
                    dbManager.deleteProduct(product.getId());
                    loadProducts();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            try {
                if (!isImageFile(selectedImage)) {
                    Toast.makeText(this, "Vui lòng chọn file ảnh", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                bitmap = resizeImage(bitmap);

                if (selectedImagePath != null) {
                    deleteOldImage(selectedImagePath);
                }

                String fileName = "product_" + System.currentTimeMillis() + ".jpg";
                File outputFile = new File(imageDir, fileName);

                try (FileOutputStream out = new FileOutputStream(outputFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                }

                selectedImagePath = outputFile.getAbsolutePath();
                imgProductPhoto.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi xử lý ảnh: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupSearchView() {
        searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0) {
                    updateSuggestions(newText);
                } else {
                    adapter.updateData(allProducts);
                }
                return true;
            }
        });

        suggestionsAdapter = new CursorAdapter(this, null, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(
                        android.R.layout.simple_dropdown_item_1line, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView textView = view.findViewById(android.R.id.text1);
                int columnIndex = cursor.getColumnIndex("suggestion");
                if (columnIndex >= 0) {
                    String suggestion = cursor.getString(columnIndex);
                    textView.setText(suggestion);
                }
            }
        };

        searchView.setSuggestionsAdapter(suggestionsAdapter);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = searchView.getSuggestionsAdapter().getCursor();
                if (cursor != null) {
                    cursor.moveToPosition(position);
                    int columnIndex = cursor.getColumnIndex("suggestion");
                    if (columnIndex >= 0) {
                        String suggestion = cursor.getString(columnIndex);
                        searchView.setQuery(suggestion, true);
                    }
                }
                return true;
            }
        });
    }

    private void updateSuggestions(String query) {
        suggestions.clear();
        String queryLower = query.toLowerCase();

        for (Product product : allProducts) {
            String productName = product.getProductName().toLowerCase();
            if (productName.startsWith(queryLower)) {
                suggestions.add(product.getProductName());
            } else if (productName.contains(queryLower)) {
                int startIndex = productName.indexOf(queryLower);
                if (startIndex > 0) {
                    String beforeMatch = product.getProductName().substring(0, startIndex);
                    String match = product.getProductName().substring(startIndex);
                    suggestions.add(beforeMatch + match);
                }
            }
        }

        String[] columns = {"_id", "suggestion"};
        MatrixCursor cursor = new MatrixCursor(columns);
        for (int i = 0; i < suggestions.size(); i++) {
            cursor.addRow(new Object[]{i, suggestions.get(i)});
        }

        searchView.getSuggestionsAdapter().changeCursor(cursor);
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        String queryLower = query.toLowerCase();

        for (Product product : allProducts) {
            if (product.getProductName().toLowerCase().contains(queryLower)) {
                filteredList.add(product);
            }
        }

        adapter.updateData(filteredList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}