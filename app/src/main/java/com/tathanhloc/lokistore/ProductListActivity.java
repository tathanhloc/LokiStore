package com.tathanhloc.lokistore;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
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
import java.util.List;

public class ProductListActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private Spinner categorySpinner;
    private DatabaseManager dbManager;

    private AlertDialog dialog;
    private ImageView imgProductPhoto;
    private String selectedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        initViews();
        setupDatabase();
        loadCategories();
        loadProducts();
        setupListeners();
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
        selectAllCategory.setId(-1); // Use a unique ID for "Select All"
        selectAllCategory.setCategoryName("Select All");
        categories.add(0, selectAllCategory); // Add "Select All" at the beginning

        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void loadProducts() {
        List<Product> products = dbManager.getAllProducts();
        adapter = new ProductAdapter(this, products, new ProductAdapter.ProductListener() {
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
                    // "Select All" option selected
                    products = dbManager.getAllProducts();
                } else {
                    // Specific category selected
                    products = dbManager.getProductsByCategory(category.getId());
                }
                adapter.updateData(products);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

//    private void showProductDialog(Product product) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View view = LayoutInflater.from(this).inflate(R.layout.dialog_product, null);
//        builder.setView(view);
//
//        // Ánh xạ các view trong dialog
//        TextInputEditText edtProductCode = view.findViewById(R.id.edtProductCode);
//        TextInputEditText edtProductName = view.findViewById(R.id.edtProductName);
//        TextInputEditText edtPrice = view.findViewById(R.id.edtPrice);
//        TextInputEditText edtDescription = view.findViewById(R.id.edtDescription);
//        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
//        imgProductPhoto = view.findViewById(R.id.imgProductPhoto);
//        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
//        Button btnSave = view.findViewById(R.id.btnSave);
//        Button btnCancel = view.findViewById(R.id.btnCancel);
//
//        // Setup category spinner
//        loadCategories();
//
//        // Nếu là edit, set dữ liệu
//        if (product != null) {
//            edtProductCode.setText(product.getProductCode());
//            edtProductName.setText(product.getProductName());
//            edtPrice.setText(String.valueOf(product.getPrice()));
//            edtDescription.setText(product.getDescription());
//            if (product.getImagePath() != null) {
//                selectedImagePath = product.getImagePath();
//                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
//                if (bitmap != null) {
//                    imgProductPhoto.setImageBitmap(bitmap);
//                }
//            }
//            // TODO: Set selected category in spinner
//        }
//
//        btnSelectImage.setOnClickListener(v -> checkPermissionAndPickImage());
//
//        btnSave.setOnClickListener(v -> {
//            // Validate và lưu dữ liệu
//            String code = edtProductCode.getText().toString();
//            String name = edtProductName.getText().toString();
//            String priceStr = edtPrice.getText().toString();
//            String description = edtDescription.getText().toString();
//            Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
//
//            if (validateInput(code, name, priceStr)) {
//                double price = Double.parseDouble(priceStr);
//
//                if (product == null) {
//                    // Thêm mới
//                    Product newProduct = new Product(0, selectedCategory.getId(), code, name,
//                            price, description, selectedImagePath);
//                    dbManager.addProduct(newProduct);
//                } else {
//                    // Cập nhật
//                    product.setProductCode(code);
//                    product.setProductName(name);
//                    product.setPrice(price);
//                    product.setDescription(description);
//                    product.setCategoryId(selectedCategory.getId());
//                    product.setImagePath(selectedImagePath);
//                    dbManager.updateProduct(product);
//                }
//
//                loadProducts();
//                dialog.dismiss();
//            }
//        });
//
//        btnCancel.setOnClickListener(v -> dialog.dismiss());
//
//        dialog = builder.create();
//        dialog.show();
//    }
private void reloadProductsByCurrentCategory() {
    Category selectedCategory = (Category) categorySpinner.getSelectedItem();
    if (selectedCategory != null) {
        List<Product> products = dbManager.getProductsByCategory(selectedCategory.getId());
        adapter.updateData(products);
    }
}

    private void showProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_product, null);
        builder.setView(view);

        // Ánh xạ các view trong dialog
        TextInputEditText edtProductCode = view.findViewById(R.id.edtProductCode);
        TextInputEditText edtProductName = view.findViewById(R.id.edtProductName);
        TextInputEditText edtPrice = view.findViewById(R.id.edtPrice);
        TextInputEditText edtDescription = view.findViewById(R.id.edtDescription);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        imgProductPhoto = view.findViewById(R.id.imgProductPhoto);
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Setup category spinner trong dialog
        List<Category> categories = dbManager.getAllCategories();
        if (categories == null || categories.isEmpty()) {
            Toast.makeText(this, "Không có danh mục sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Nếu là edit, set dữ liệu cho các trường
        if (product != null) {
            edtProductCode.setText(product.getProductCode());
            edtProductName.setText(product.getProductName());
            edtPrice.setText(String.valueOf(product.getPrice()));
            edtDescription.setText(product.getDescription());

            // Load và hiển thị ảnh sản phẩm nếu có
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                selectedImagePath = product.getImagePath();
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                if (bitmap != null) {
                    imgProductPhoto.setImageBitmap(bitmap);
                }
            }

            // Set selected category trong spinner
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == product.getCategoryId()) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }

        // Xử lý chọn ảnh
        btnSelectImage.setOnClickListener(v -> checkPermissionAndPickImage());

        // Xử lý lưu thông tin sản phẩm
        btnSave.setOnClickListener(v -> {
            // Lấy dữ liệu từ form
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
                    // Thêm sản phẩm mới
                    Product newProduct = new Product(
                            0,
                            categoryId,
                            code,
                            name,
                            price,
                            description,
                            selectedImagePath
                    );
                    dbManager.addProduct(newProduct);
                    Toast.makeText(this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                } else {
                    // Cập nhật sản phẩm
                    product.setProductCode(code);
                    product.setProductName(name);
                    product.setPrice(price);
                    product.setDescription(description);
                    product.setCategoryId(categoryId);
                    product.setImagePath(selectedImagePath);
                    dbManager.updateProduct(product);
                    Toast.makeText(this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                }

                loadProducts(); // Reload lại toàn bộ danh sách
                dialog.dismiss();
            }
        });

        // Xử lý hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Hiển thị dialog
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
                    dbManager.deleteProduct(product.getId());
                    loadProducts();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            pickImage();
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                // Lưu ảnh vào thư mục ứng dụng
                InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                File outputFile = new File(getFilesDir(), "product_" + System.currentTimeMillis() + ".jpg");
                FileOutputStream outputStream = new FileOutputStream(outputFile);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                selectedImagePath = outputFile.getAbsolutePath();
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                imgProductPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                Toast.makeText(this, "Cần quyền truy cập để chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}