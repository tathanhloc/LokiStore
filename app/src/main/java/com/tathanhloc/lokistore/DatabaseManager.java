package com.tathanhloc.lokistore;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tathanhloc.lokistore.models.Category;
import com.tathanhloc.lokistore.models.Product;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;


    public DatabaseManager(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        database = dbHelper.getWritableDatabase();
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Phương thức xử lý Users
    public boolean checkLogin(String email, String password) {
        Cursor cursor = null;
        try {
            cursor = database.query(DatabaseHelper.TABLE_USERS,
                    new String[]{"user_id"},
                    "email = ? AND password = ?",
                    new String[]{email, password},
                    null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean registerUser(String email, String password, String fullName) {
        // Check if the email already exists
        if (isEmailExists(email)) {
            return false;
        }

        // Insert the new user into the database
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        values.put("full_name", fullName);  // Sửa lại fullName thành full_name theo DB schema

        long result = database.insert(DatabaseHelper.TABLE_USERS, null, values);  // Sử dụng TABLE_USERS từ DatabaseHelper
        return result != -1;
    }

    private boolean isEmailExists(String email) {
        Cursor cursor = database.query(DatabaseHelper.TABLE_USERS,  // Sử dụng TABLE_USERS thay vì "users"
                new String[]{"email"},
                "email = ?",
                new String[]{email},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }


    // Phương thức xử lý Products
    public long addProduct(Product product) {
        ContentValues values = new ContentValues();
        values.put("product_code", product.getProductCode());
        values.put("product_name", product.getProductName());
        values.put("category_id", product.getCategoryId());
        values.put("price", product.getPrice());
        values.put("description", product.getDescription());
        values.put("image_path", product.getImagePath());

        return database.insert(DatabaseHelper.TABLE_PRODUCTS, null, values);
    }

    public int updateProduct(Product product) {
        ContentValues values = new ContentValues();
        values.put("product_code", product.getProductCode());
        values.put("product_name", product.getProductName());
        values.put("category_id", product.getCategoryId());
        values.put("price", product.getPrice());
        values.put("description", product.getDescription());
        values.put("image_path", product.getImagePath());

        return database.update(DatabaseHelper.TABLE_PRODUCTS, values,
                "product_id = ?", new String[]{String.valueOf(product.getId())});
    }

    public void deleteProduct(int productId) {
        database.delete(DatabaseHelper.TABLE_PRODUCTS, "product_id = ?",
                new String[]{String.valueOf(productId)});
    }

public Product getProduct(int id) {
    Product product = null;
    Cursor cursor = database.query(DatabaseHelper.TABLE_PRODUCTS,
            null,
            "product_id = ?",
            new String[]{String.valueOf(id)},
            null, null, null);

    if (cursor != null && cursor.moveToFirst()) {
        product = new Product();
        int productIdIndex = cursor.getColumnIndex("product_id");
        int productCodeIndex = cursor.getColumnIndex("product_code");
        int productNameIndex = cursor.getColumnIndex("product_name");
        int categoryIdIndex = cursor.getColumnIndex("category_id");
        int priceIndex = cursor.getColumnIndex("price");
        int descriptionIndex = cursor.getColumnIndex("description");
        int imagePathIndex = cursor.getColumnIndex("image_path");

        if (productIdIndex >= 0) product.setId(cursor.getInt(productIdIndex));
        if (productCodeIndex >= 0) product.setProductCode(cursor.getString(productCodeIndex));
        if (productNameIndex >= 0) product.setProductName(cursor.getString(productNameIndex));
        if (categoryIdIndex >= 0) product.setCategoryId(cursor.getInt(categoryIdIndex));
        if (priceIndex >= 0) product.setPrice(cursor.getDouble(priceIndex));
        if (descriptionIndex >= 0) product.setDescription(cursor.getString(descriptionIndex));
        if (imagePathIndex >= 0) product.setImagePath(cursor.getString(imagePathIndex));

        cursor.close();
    }
    return product;
}

public List<Product> getAllProducts() {
    List<Product> products = new ArrayList<>();
    Cursor cursor = database.query(DatabaseHelper.TABLE_PRODUCTS,
            null, null, null, null, null, null);

    if (cursor != null && cursor.moveToFirst()) {
        do {
            Product product = new Product();
            int productIdIndex = cursor.getColumnIndex("product_id");
            int productCodeIndex = cursor.getColumnIndex("product_code");
            int productNameIndex = cursor.getColumnIndex("product_name");
            int categoryIdIndex = cursor.getColumnIndex("category_id");
            int priceIndex = cursor.getColumnIndex("price");
            int descriptionIndex = cursor.getColumnIndex("description");
            int imagePathIndex = cursor.getColumnIndex("image_path");

            if (productIdIndex >= 0) product.setId(cursor.getInt(productIdIndex));
            if (productCodeIndex >= 0) product.setProductCode(cursor.getString(productCodeIndex));
            if (productNameIndex >= 0) product.setProductName(cursor.getString(productNameIndex));
            if (categoryIdIndex >= 0) product.setCategoryId(cursor.getInt(categoryIdIndex));
            if (priceIndex >= 0) product.setPrice(cursor.getDouble(priceIndex));
            if (descriptionIndex >= 0) product.setDescription(cursor.getString(descriptionIndex));
            if (imagePathIndex >= 0) product.setImagePath(cursor.getString(imagePathIndex));

            products.add(product);
        } while (cursor.moveToNext());
        cursor.close();
    }
    return products;
}

public List<Product> getProductsByCategory(int categoryId) {
    List<Product> products = new ArrayList<>();
    Cursor cursor = database.query(DatabaseHelper.TABLE_PRODUCTS,
            null,
            "category_id = ?",
            new String[]{String.valueOf(categoryId)},
            null, null, null);

    if (cursor != null && cursor.moveToFirst()) {
        do {
            Product product = new Product();
            int productIdIndex = cursor.getColumnIndex("product_id");
            int productCodeIndex = cursor.getColumnIndex("product_code");
            int productNameIndex = cursor.getColumnIndex("product_name");
            int categoryIdIndex = cursor.getColumnIndex("category_id");
            int priceIndex = cursor.getColumnIndex("price");
            int descriptionIndex = cursor.getColumnIndex("description");
            int imagePathIndex = cursor.getColumnIndex("image_path");

            if (productIdIndex >= 0) product.setId(cursor.getInt(productIdIndex));
            if (productCodeIndex >= 0) product.setProductCode(cursor.getString(productCodeIndex));
            if (productNameIndex >= 0) product.setProductName(cursor.getString(productNameIndex));
            if (categoryIdIndex >= 0) product.setCategoryId(cursor.getInt(categoryIdIndex));
            if (priceIndex >= 0) product.setPrice(cursor.getDouble(priceIndex));
            if (descriptionIndex >= 0) product.setDescription(cursor.getString(descriptionIndex));
            if (imagePathIndex >= 0) product.setImagePath(cursor.getString(imagePathIndex));

            products.add(product);
        } while (cursor.moveToNext());
        cursor.close();
    }
    return products;
}

    // Phương thức xử lý Categories
public List<Category> getAllCategories() {
    List<Category> categories = new ArrayList<>();
    Cursor cursor = database.query(DatabaseHelper.TABLE_CATEGORIES,
            null, null, null, null, null, null);

    if (cursor != null && cursor.moveToFirst()) {
        do {
            Category category = new Category();
            int categoryIdIndex = cursor.getColumnIndex("category_id");
            int categoryCodeIndex = cursor.getColumnIndex("category_code");
            int categoryNameIndex = cursor.getColumnIndex("category_name");

            if (categoryIdIndex >= 0) category.setId(cursor.getInt(categoryIdIndex));
            if (categoryCodeIndex >= 0) category.setCategoryCode(cursor.getString(categoryCodeIndex));
            if (categoryNameIndex >= 0) category.setCategoryName(cursor.getString(categoryNameIndex));

            categories.add(category);
        } while (cursor.moveToNext());
        cursor.close();
    }
    return categories;
}

    public long addCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put("category_code", category.getCategoryCode());
        values.put("category_name", category.getCategoryName());
        return database.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
    }

    public int updateCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put("category_name", category.getCategoryName());
        return database.update(DatabaseHelper.TABLE_CATEGORIES, values,
                "category_id = ?", new String[]{String.valueOf(category.getId())});
    }

    public void deleteCategory(int categoryId) {
        database.delete(DatabaseHelper.TABLE_CATEGORIES, "category_id = ?",
                new String[]{String.valueOf(categoryId)});
    }


    public int getCategoryCount() {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CATEGORIES, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public String getUserFullName(String email) {
        Cursor cursor = database.query(DatabaseHelper.TABLE_USERS,
                new String[]{"full_name"},
                "email = ?",
                new String[]{email},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String fullName = cursor.getString(0);
            cursor.close();
            return fullName;
        }
        return null;
    }

}