package com.tathanhloc.lokistore;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tathanhloc.lokistore.models.Category;
import com.tathanhloc.lokistore.models.CategoryStatistics;
import com.tathanhloc.lokistore.models.Order;
import com.tathanhloc.lokistore.models.OrderDetail;
import com.tathanhloc.lokistore.models.OrderStatistics;
import com.tathanhloc.lokistore.models.Product;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseManager {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private SimpleDateFormat dbDateFormat;



    public DatabaseManager(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        database = dbHelper.getWritableDatabase();
        dbDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

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
                    new String[]{"password"},
                    "email = ?",
                    new String[]{email},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String storedPassword = cursor.getString(0);

                // Log the passwords for debugging
                Log.d("LoginDebug", "Stored Password: " + storedPassword);
                Log.d("LoginDebug", "Entered Password: " + password);

                // Verify the password
                return PasswordSecurityManager.verifyPassword(password, storedPassword);
            }
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean registerUser(String email, String password, String fullName) {
        // Kiểm tra độ mạnh mật khẩu
        if (!PasswordSecurityManager.isPasswordStrong(password)) {
            throw new IllegalArgumentException("Mật khẩu không đủ mạnh");
        }

        // Kiểm tra email đã tồn tại
        if (isEmailExists(email)) {
            return false;
        }

        // Mã hóa mật khẩu
        String hashedPassword = PasswordSecurityManager.hashPassword(password);

        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", hashedPassword);
        values.put("full_name", fullName);

        long result = database.insert(DatabaseHelper.TABLE_USERS, null, values);
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
        values.put("is_active", product.getIsActive());

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

    public boolean deleteProduct(int productId) {
        ContentValues values = new ContentValues();
        values.put("is_active", 0);
        return database.update(DatabaseHelper.TABLE_PRODUCTS, values,
                "product_id = ?", new String[]{String.valueOf(productId)}) > 0;
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
                null,
                "is_active = 1 OR is_active IS NULL", // Thêm điều kiện này
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                products.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return products;
    }

private Product cursorToProduct(Cursor cursor) {
    Product product = new Product();
    product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("product_id")));
    product.setProductCode(cursor.getString(cursor.getColumnIndexOrThrow("product_code")));
    product.setProductName(cursor.getString(cursor.getColumnIndexOrThrow("product_name")));
    product.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
    product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
    product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
    product.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("image_path")));
    product.setIsActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")));
    return product;
}

    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> products = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_PRODUCTS,
                null,
                "category_id = ? AND (is_active = 1 OR is_active IS NULL)", // Thêm điều kiện này
                new String[]{String.valueOf(categoryId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                products.add(cursorToProduct(cursor));
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

    public Product getProductById(int productId) {
        Product product = null;
        Cursor cursor = database.query(DatabaseHelper.TABLE_PRODUCTS,
                null,
                "product_id = ?", // Không kiểm tra is_active
                new String[]{String.valueOf(productId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            product = cursorToProduct(cursor);
            cursor.close();
        }
        return product;
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

    public boolean deleteCategory(int categoryId) {
        ContentValues values = new ContentValues();
        values.put("is_active", 0);
        return database.update(DatabaseHelper.TABLE_CATEGORIES, values,
                "category_id = ?", new String[]{String.valueOf(categoryId)}) > 0;
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

    // Phương thức xử lý Orders
    public long addOrder(Order order) {
        ContentValues values = new ContentValues();
        values.put("order_code", order.getOrderCode());
        values.put("user_id", order.getUserId());
        values.put("order_date", order.getOrderDate());
        values.put("delivery_date", order.getDeliveryDate());
        values.put("total_amount", order.getTotalAmount());
        values.put("note", order.getNote());

        return database.insert(DatabaseHelper.TABLE_ORDERS, null, values);
    }
    public boolean updateOrder(Order order) {
        // Bỏ kiểm tra isOrderEditable vì sẽ kiểm tra trong OrderListActivity
        ContentValues values = new ContentValues();
        values.put("delivery_date", order.getDeliveryDate());
        values.put("total_amount", order.getTotalAmount());
        values.put("note", order.getNote());
        values.put("status", order.getStatus());

        int result = database.update(DatabaseHelper.TABLE_ORDERS, values,
                "order_id = ?", new String[]{String.valueOf(order.getOrderId())});
        return result > 0;
    }
    public boolean deleteOrder(int orderId) {
        // Bỏ kiểm tra isOrderEditable vì sẽ kiểm tra trong OrderListActivity
        database.beginTransaction();
        try {
            // Xóa chi tiết đơn hàng trước
            database.delete(DatabaseHelper.TABLE_ORDER_DETAILS,
                    "order_id = ?", new String[]{String.valueOf(orderId)});

            // Sau đó xóa đơn hàng
            int result = database.delete(DatabaseHelper.TABLE_ORDERS,
                    "order_id = ?", new String[]{String.valueOf(orderId)});

            if (result > 0) {
                database.setTransactionSuccessful();
                return true;
            }
            return false;
        } finally {
            database.endTransaction();
        }
    }

    public Order getOrder(int orderId) {
        Order order = null;
        Cursor cursor = database.query(DatabaseHelper.TABLE_ORDERS,
                null,
                "order_id = ?",
                new String[]{String.valueOf(orderId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            order = new Order();
            order.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow("order_id")));
            order.setOrderCode(cursor.getString(cursor.getColumnIndexOrThrow("order_code")));
            order.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow("order_date")));
            order.setDeliveryDate(cursor.getString(cursor.getColumnIndexOrThrow("delivery_date")));
            order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")));
            order.setNote(cursor.getString(cursor.getColumnIndexOrThrow("note")));
            order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status"))); // Thêm dòng này
            cursor.close();
        }
        return order;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_ORDERS,
                null, null, null, null, null, "order_date DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                orders.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return orders;
    }

    // Phương thức xử lý OrderDetails
    public long addOrderDetail(OrderDetail detail) {
        ContentValues values = new ContentValues();
        values.put("order_id", detail.getOrderId());
        values.put("product_id", detail.getProductId());
        values.put("quantity", detail.getQuantity());
        values.put("price", detail.getPrice());
        values.put("amount", detail.getAmount());

        return database.insert(DatabaseHelper.TABLE_ORDER_DETAILS, null, values);
    }

    public List<OrderDetail> getOrderDetails(int orderId) {
        List<OrderDetail> details = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_ORDER_DETAILS,
                null,
                "order_id = ?",
                new String[]{String.valueOf(orderId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                details.add(cursorToOrderDetail(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return details;
    }

    public boolean isOrderEditable(String orderDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(orderDate);
            Date today = new Date();

            // Chỉ so sánh ngày, không quan tâm đến thời gian
            Calendar calOrder = Calendar.getInstance();
            calOrder.setTime(date);
            calOrder.set(Calendar.HOUR_OF_DAY, 0);
            calOrder.set(Calendar.MINUTE, 0);
            calOrder.set(Calendar.SECOND, 0);
            calOrder.set(Calendar.MILLISECOND, 0);

            Calendar calToday = Calendar.getInstance();
            calToday.setTime(today);
            calToday.set(Calendar.HOUR_OF_DAY, 0);
            calToday.set(Calendar.MINUTE, 0);
            calToday.set(Calendar.SECOND, 0);
            calToday.set(Calendar.MILLISECOND, 0);

            return calOrder.getTimeInMillis() == calToday.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Order cursorToOrder(Cursor cursor) {
        Order order = new Order();
        order.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow("order_id")));
        order.setOrderCode(cursor.getString(cursor.getColumnIndexOrThrow("order_code")));
        order.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow("order_date")));
        order.setDeliveryDate(cursor.getString(cursor.getColumnIndexOrThrow("delivery_date")));
        order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")));
        order.setNote(cursor.getString(cursor.getColumnIndexOrThrow("note")));
        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        return order;
    }
    private OrderDetail cursorToOrderDetail(Cursor cursor) {
        int detailId = cursor.getInt(cursor.getColumnIndexOrThrow("detail_id"));
        int orderId = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
        int productId = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));

        return new OrderDetail(detailId, orderId, productId, quantity, price);
    }

    public String generateOrderCode() {
        String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ORDERS +
                        " WHERE order_code LIKE 'DH" + date + "%'", null);

        int sequence = 1;
        if (cursor != null && cursor.moveToFirst()) {
            sequence = cursor.getInt(0) + 1;
            cursor.close();
        }

        return String.format("DH%s%03d", date, sequence);
    }
    public String getUserFullNameById(int userId) {
        Cursor cursor = null;
        try {
            cursor = database.query(DatabaseHelper.TABLE_USERS,
                    new String[]{"full_name"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);

            // Thêm log để kiểm tra
            Log.d("DatabaseManager", "Getting name for userId: " + userId);

            if (cursor != null && cursor.moveToFirst()) {
                String fullName = cursor.getString(0);
                Log.d("DatabaseManager", "Found name: " + fullName);
                return fullName;
            } else {
                Log.d("DatabaseManager", "No user found for id: " + userId);
                return null;
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error getting user name: " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
//    public int getTotalOrders() {
//        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ORDERS, null);
//        int count = 0;
//        if (cursor.moveToFirst()) {
//            count = cursor.getInt(0);
//        }
//        cursor.close();
//        return count;
//    }
//
//    public double getTotalRevenue() {
//        Cursor cursor = database.rawQuery("SELECT SUM(total_amount) FROM " + DatabaseHelper.TABLE_ORDERS, null);
//        double total = 0;
//        if (cursor.moveToFirst()) {
//            total = cursor.getDouble(0);
//        }
//        cursor.close();
//        return total;
//    }
//
//    public List<OrderStatistics> getRevenueByDate() {
//        List<OrderStatistics> statistics = new ArrayList<>();
//        String query = "SELECT order_date, SUM(total_amount) as revenue " +
//                "FROM " + DatabaseHelper.TABLE_ORDERS +
//                " GROUP BY order_date ORDER BY order_date";
//
//        Cursor cursor = database.rawQuery(query, null);
//        if (cursor.moveToFirst()) {
//            do {
//                String date = cursor.getString(0);
//                double revenue = cursor.getDouble(1);
//                statistics.add(new OrderStatistics(date, revenue));
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        return statistics;
//    }
//
//    public List<CategoryStatistics> getRevenueByCategory() {
//        List<CategoryStatistics> statistics = new ArrayList<>();
//        String query = "SELECT c.category_name, SUM(od.amount) as revenue " +
//                "FROM " + DatabaseHelper.TABLE_ORDER_DETAILS + " od " +
//                "JOIN " + DatabaseHelper.TABLE_PRODUCTS + " p ON od.product_id = p.product_id " +
//                "JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c ON p.category_id = c.category_id " +
//                "GROUP BY c.category_id";
//
//        Cursor cursor = database.rawQuery(query, null);
//        if (cursor.moveToFirst()) {
//            do {
//                String categoryName = cursor.getString(0);
//                double revenue = cursor.getDouble(1);
//                statistics.add(new CategoryStatistics(categoryName, revenue));
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        return statistics;
//    }

    // Thêm các phương thức helper cho việc chuyển đổi ngày
    private String convertDisplayDateToDbDate(String displayDate) throws ParseException {
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = displayFormat.parse(displayDate);
        return dbFormat.format(date);
    }

    private String convertDbDateToDisplayDate(String dbDate) throws ParseException {
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = dbFormat.parse(dbDate);
        return displayFormat.format(date);
    }



public boolean updateOrderStatus(int orderId, String status) {
    ContentValues values = new ContentValues();
    values.put("status", status);
    int rowsAffected = database.update(DatabaseHelper.TABLE_ORDERS, values,
            "order_id = ?", new String[]{String.valueOf(orderId)});
    return rowsAffected > 0;
}
    public List<Order> searchOrders(String query) {
        List<Order> orders = new ArrayList<>();
        String searchQuery = "SELECT * FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE order_code LIKE ? OR note LIKE ? ORDER BY order_date DESC";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};

        Cursor cursor = database.rawQuery(searchQuery, selectionArgs);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                orders.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return orders;
    }

    public boolean deleteOrderDetails(int orderId) {
        return database.delete(DatabaseHelper.TABLE_ORDER_DETAILS,
                "order_id = ?",
                new String[]{String.valueOf(orderId)}) > 0;
    }

    // Thêm phương thức để lấy tổng doanh thu theo sản phẩm (không giới hạn thời gian)
    public List<CategoryStatistics> getRevenueByProduct() {
        List<CategoryStatistics> statistics = new ArrayList<>();
        String query = "SELECT p.product_name, SUM(od.amount) as revenue " +
                "FROM " + DatabaseHelper.TABLE_ORDER_DETAILS + " od " +
                "JOIN " + DatabaseHelper.TABLE_PRODUCTS + " p ON od.product_id = p.product_id " +
                "GROUP BY p.product_id, p.product_name " +
                "ORDER BY revenue DESC";

        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String productName = cursor.getString(0);
                double revenue = cursor.getDouble(1);
                statistics.add(new CategoryStatistics(productName, revenue));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return statistics;
    }

    // Thêm phương thức để lấy chi tiết doanh thu của từng sản phẩm theo ngày
    public List<OrderStatistics> getProductRevenueByDate(int productId, String startDate, String endDate) {
        List<OrderStatistics> statistics = new ArrayList<>();
        String query = "SELECT o.order_date, SUM(od.amount) as revenue " +
                "FROM " + DatabaseHelper.TABLE_ORDER_DETAILS + " od " +
                "JOIN " + DatabaseHelper.TABLE_ORDERS + " o ON od.order_id = o.order_id " +
                "WHERE od.product_id = ? AND o.order_date BETWEEN ? AND ? " +
                "GROUP BY o.order_date " +
                "ORDER BY o.order_date";

        Cursor cursor = database.rawQuery(query,
                new String[]{String.valueOf(productId), startDate, endDate});

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                double revenue = cursor.getDouble(1);
                statistics.add(new OrderStatistics(date, revenue));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return statistics;
    }
    public void enableBiometric(String email) {
        ContentValues values = new ContentValues();
        values.put("biometric_enabled", 1);
        database.update(DatabaseHelper.TABLE_USERS, values,
                "email = ?", new String[]{email});
    }

    public void disableBiometric(String email) {
        ContentValues values = new ContentValues();
        values.put("biometric_enabled", 0);
        database.update(DatabaseHelper.TABLE_USERS, values,
                "email = ?", new String[]{email});
    }

    public boolean isBiometricEnabled(String email) {
        Cursor cursor = null;
        try {
            cursor = database.query(DatabaseHelper.TABLE_USERS,
                    new String[]{"biometric_enabled"},
                    "email = ?",
                    new String[]{email},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("biometric_enabled");
                if (columnIndex >= 0) {
                    return cursor.getInt(columnIndex) == 1;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error checking biometric status", e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public List<OrderStatistics> getAllRevenue() {
        List<OrderStatistics> statistics = new ArrayList<>();
        String query = "SELECT date(order_date) as date, SUM(total_amount) as revenue " +
                "FROM " + DatabaseHelper.TABLE_ORDERS +
                " GROUP BY date(order_date) ORDER BY date(order_date)";

        try {
            Cursor cursor = database.rawQuery(query, null);

            Log.d("DatabaseManager", "Query: " + query);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(0);
                    double revenue = cursor.getDouble(1);
                    Log.d("DatabaseManager", "Result: date=" + date + ", revenue=" + revenue);
                    statistics.add(new OrderStatistics(date, revenue));
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error getting all revenue", e);
            e.printStackTrace();
        }

        return statistics;
    }

    public List<CategoryStatistics> getRevenueByProduct(String startDate, String endDate) {
        List<CategoryStatistics> statistics = new ArrayList<>();
        String query = "SELECT p.product_name, SUM(od.amount) AS revenue " +
                "FROM order_details od " +
                "JOIN orders o ON od.order_id = o.id " +
                "JOIN products p ON od.product_id = p.id " +
                "WHERE o.order_date BETWEEN ? AND ? " +
                "GROUP BY p.id " +
                "ORDER BY revenue DESC " +
                "LIMIT 10";

        Cursor cursor = database.rawQuery(query, new String[]{startDate, endDate});
        int productNameColumnIndex = cursor.getColumnIndex("product_name");
        int revenueColumnIndex = cursor.getColumnIndex("revenue");

        while (cursor.moveToNext()) {
            String productName = cursor.getString(productNameColumnIndex);
            double revenue = cursor.getDouble(revenueColumnIndex);
            statistics.add(new CategoryStatistics(productName, revenue));
        }
        cursor.close();
        return statistics;
    }
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private Date parseDate(String dateString) {
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e("DatabaseManager", "Error parsing date: " + dateString, e);
            return null;
        }
    }



    // mới
//    public List<OrderStatistics> getRevenueByTimeRange(String startDate, String endDate, String groupBy) {
//        List<OrderStatistics> statistics = new ArrayList<>();
//        String query;
//
//        try {
//            switch (groupBy) {
//                case "day":
//                    query = "SELECT strftime('%d/%m/%Y', date(order_date)) as date, " +
//                            "SUM(total_amount) as revenue " +
//                            "FROM " + DatabaseHelper.TABLE_ORDERS + " " +
//                            "WHERE date(order_date) BETWEEN date(?) AND date(?) " +
//                            "GROUP BY date(order_date) " +
//                            "ORDER BY date(order_date)";
//                    break;
//                case "month":
//                    query = "SELECT strftime('%m/%Y', order_date) as date, " +
//                            "SUM(total_amount) as revenue " +
//                            "FROM " + DatabaseHelper.TABLE_ORDERS + " " +
//                            "WHERE date(order_date) BETWEEN date(?) AND date(?) " +
//                            "GROUP BY strftime('%Y-%m', order_date) " +
//                            "ORDER BY date(order_date)";
//                    break;
//                case "year":
//                    query = "SELECT strftime('%Y', order_date) as date, " +
//                            "SUM(total_amount) as revenue " +
//                            "FROM " + DatabaseHelper.TABLE_ORDERS + " " +
//                            "WHERE date(order_date) BETWEEN date(?) AND date(?) " +
//                            "GROUP BY strftime('%Y', order_date) " +
//                            "ORDER BY date(order_date)";
//                    break;
//                default:
//                    return statistics;
//            }
//
//            Cursor cursor = database.rawQuery(query, new String[]{startDate, endDate});
//
//            if (cursor != null && cursor.moveToFirst()) {
//                do {
//                    String date = cursor.getString(0);
//                    double revenue = cursor.getDouble(1);
//                    statistics.add(new OrderStatistics(date, revenue));
//                } while (cursor.moveToNext());
//                cursor.close();
//            }
//        } catch (Exception e) {
//            Log.e("DatabaseManager", "Error getting revenue by time range", e);
//        }
//
//        return statistics;
//    }
//
//    public double getTotalRevenueByTimeRange(String startDate, String endDate) {
//        double totalRevenue = 0;
//        String query = "SELECT SUM(total_amount) " +
//                "FROM " + DatabaseHelper.TABLE_ORDERS + " " +
//                "WHERE date(order_date) BETWEEN date(?) AND date(?)";
//
//        try {
//            Cursor cursor = database.rawQuery(query, new String[]{startDate, endDate});
//            if (cursor != null && cursor.moveToFirst()) {
//                totalRevenue = cursor.getDouble(0);
//                cursor.close();
//            }
//        } catch (Exception e) {
//            Log.e("DatabaseManager", "Error calculating total revenue", e);
//        }
//
//        return totalRevenue;
//    }
//
//    public int getTotalOrdersByTimeRange(String startDate, String endDate) {
//        int totalOrders = 0;
//        String query = "SELECT COUNT(*) " +
//                "FROM " + DatabaseHelper.TABLE_ORDERS + " " +
//                "WHERE date(order_date) BETWEEN date(?) AND date(?)";
//
//        try {
//            Cursor cursor = database.rawQuery(query, new String[]{startDate, endDate});
//            if (cursor != null && cursor.moveToFirst()) {
//                totalOrders = cursor.getInt(0);
//                cursor.close();
//            }
//        } catch (Exception e) {
//            Log.e("DatabaseManager", "Error counting total orders", e);
//        }
//
//        return totalOrders;
//    }

//    public List<CategoryStatistics> getCategoryRevenueByTimeRange(String startDate, String endDate) {
//        List<CategoryStatistics> statistics = new ArrayList<>();
//        String query = "SELECT c.category_name, SUM(od.amount) as revenue " +
//                "FROM " + DatabaseHelper.TABLE_ORDER_DETAILS + " od " +
//                "JOIN " + DatabaseHelper.TABLE_ORDERS + " o ON od.order_id = o.order_id " +
//                "JOIN " + DatabaseHelper.TABLE_PRODUCTS + " p ON od.product_id = p.product_id " +
//                "JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c ON p.category_id = c.category_id " +
//                "WHERE date(o.order_date) BETWEEN date(?) AND date(?) " +
//                "AND p.is_active = 1 " +
//                "GROUP BY c.category_id, c.category_name " +
//                "ORDER BY revenue DESC";
//
//        try {
//            Cursor cursor = database.rawQuery(query, new String[]{startDate, endDate});
//            if (cursor != null && cursor.moveToFirst()) {
//                do {
//                    String categoryName = cursor.getString(0);
//                    double revenue = cursor.getDouble(1);
//                    statistics.add(new CategoryStatistics(categoryName, revenue));
//                } while (cursor.moveToNext());
//                cursor.close();
//            }
//        } catch (Exception e) {
//            Log.e("DatabaseManager", "Error getting category statistics", e);
//        }
//
//        return statistics;
//    }

    public List<CategoryStatistics> getTopProducts(String startDate, String endDate, int limit) {
        List<CategoryStatistics> statistics = new ArrayList<>();
        String query = "SELECT p.product_name, SUM(od.amount) as revenue " +
                "FROM " + DatabaseHelper.TABLE_ORDER_DETAILS + " od " +
                "JOIN " + DatabaseHelper.TABLE_ORDERS + " o ON od.order_id = o.order_id " +
                "JOIN " + DatabaseHelper.TABLE_PRODUCTS + " p ON od.product_id = p.product_id " +
                "WHERE date(o.order_date) BETWEEN date(?) AND date(?) " +
                "GROUP BY p.product_id, p.product_name " +
                "ORDER BY revenue DESC " +
                "LIMIT ?";

        try {
            Cursor cursor = database.rawQuery(query,
                    new String[]{startDate, endDate, String.valueOf(limit)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String productName = cursor.getString(0);
                    double revenue = cursor.getDouble(1);
                    statistics.add(new CategoryStatistics(productName, revenue));
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error getting top products", e);
        }

        return statistics;
    }

    public int getOrderCountByStatus(String status, String startDate, String endDate) {
        int count = 0;
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE status = ? AND date(order_date) BETWEEN date(?) AND date(?)";

        try {
            Cursor cursor = database.rawQuery(query, new String[]{status, startDate, endDate});
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error getting order count by status", e);
        }

        return count;
    }

    public double getRevenueByStatus(String status, String startDate, String endDate) {
        double revenue = 0;
        String query = "SELECT SUM(total_amount) FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE status = ? AND date(order_date) BETWEEN date(?) AND date(?)";

        try {
            Cursor cursor = database.rawQuery(query, new String[]{status, startDate, endDate});
            if (cursor != null && cursor.moveToFirst()) {
                revenue = cursor.getDouble(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error getting revenue by status", e);
        }

        return revenue;
    }


    // Thêm phương thức để lấy tổng doanh thu theo sản phẩm (không giới hạn thời gian)
    public int getTotalOrders() {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ORDERS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public double getTotalRevenue() {
        Cursor cursor = database.rawQuery("SELECT SUM(total_amount) FROM " + DatabaseHelper.TABLE_ORDERS, null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public List<OrderStatistics> getRevenueByDate() {
        List<OrderStatistics> statistics = new ArrayList<>();
        String query = "SELECT order_date, SUM(total_amount) as revenue " +
                "FROM " + DatabaseHelper.TABLE_ORDERS +
                " GROUP BY order_date ORDER BY order_date";

        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                double revenue = cursor.getDouble(1);
                statistics.add(new OrderStatistics(date, revenue));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return statistics;
    }

    public List<CategoryStatistics> getRevenueByCategory() {
        List<CategoryStatistics> statistics = new ArrayList<>();
        String query = "SELECT c.category_name, SUM(od.amount) as revenue " +
                "FROM " + DatabaseHelper.TABLE_ORDER_DETAILS + " od " +
                "JOIN " + DatabaseHelper.TABLE_PRODUCTS + " p ON od.product_id = p.product_id " +
                "JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c ON p.category_id = c.category_id " +
                "GROUP BY c.category_id";

        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String categoryName = cursor.getString(0);
                double revenue = cursor.getDouble(1);
                statistics.add(new CategoryStatistics(categoryName, revenue));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return statistics;
    }
    public List<OrderStatistics> getRevenueByTimeRange(String startDate, String endDate, String groupBy) {
        List<OrderStatistics> statistics = new ArrayList<>();
        String query;

        switch (groupBy) {
            case "day":
                // Thống kê theo ngày
                query = "SELECT strftime('%d/%m/%Y', order_date) as date, SUM(total_amount) as revenue " +
                        "FROM " + DatabaseHelper.TABLE_ORDERS +
                        " WHERE order_date BETWEEN ? AND ? " +
                        "GROUP BY order_date ORDER BY order_date";
                break;
            case "month":
                // Thống kê theo tháng
                query = "SELECT strftime('%m/%Y', order_date) as date, SUM(total_amount) as revenue " +
                        "FROM " + DatabaseHelper.TABLE_ORDERS +
                        " WHERE order_date BETWEEN ? AND ? " +
                        "GROUP BY strftime('%m/%Y', order_date) " +
                        "ORDER BY strftime('%Y-%m', order_date)";
                break;
            case "year":
                // Thống kê theo năm
                query = "SELECT strftime('%Y', order_date) as date, SUM(total_amount) as revenue " +
                        "FROM " + DatabaseHelper.TABLE_ORDERS +
                        " WHERE order_date BETWEEN ? AND ? " +
                        "GROUP BY strftime('%Y', order_date) " +
                        "ORDER BY date";
                break;
            default:
                return statistics;
        }

        Cursor cursor = database.rawQuery(query, new String[]{startDate, endDate});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                double revenue = cursor.getDouble(1);
                statistics.add(new OrderStatistics(date, revenue));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return statistics;
    }
    public double getTotalRevenueByTimeRange(String startDate, String endDate) {
        String query = "SELECT SUM(total_amount) FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE order_date BETWEEN ? AND ?";

        Cursor cursor = database.rawQuery(query, new String[]{startDate, endDate});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }
    public int getTotalOrdersByTimeRange(String startDate, String endDate) {
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE order_date BETWEEN ? AND ?";

        Cursor cursor = database.rawQuery(query, new String[]{startDate, endDate});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    public List<CategoryStatistics> getCategoryRevenueByTimeRange(String startDate, String endDate) {
        List<CategoryStatistics> statistics = new ArrayList<>();

        String query = "SELECT c.category_name, SUM(od.amount) as revenue " +
                "FROM " + DatabaseHelper.TABLE_ORDER_DETAILS + " od " +
                "JOIN " + DatabaseHelper.TABLE_ORDERS + " o ON od.order_id = o.order_id " +
                "JOIN " + DatabaseHelper.TABLE_PRODUCTS + " p ON od.product_id = p.product_id " +
                "JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c ON p.category_id = c.category_id " +
                "WHERE o.order_date BETWEEN ? AND ? " +
                "GROUP BY c.category_id, c.category_name " +
                "ORDER BY revenue DESC";

        Cursor cursor = database.rawQuery(query, new String[]{startDate, endDate});

        if (cursor.moveToFirst()) {
            do {
                String categoryName = cursor.getString(0);
                double revenue = cursor.getDouble(1);
                statistics.add(new CategoryStatistics(categoryName, revenue));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return statistics;
    }
// cụm tính năng mới
public List<CategoryStatistics> getTopProductsByRevenue(int limit) {
    List<CategoryStatistics> topProducts = new ArrayList<>();

    try {
        String query = "SELECT p.product_name, " +
                "SUM(od.quantity * od.price) as revenue, " +
                "SUM(od.quantity) as total_quantity " +
                "FROM " + DatabaseHelper.TABLE_ORDER_DETAILS + " od " +
                "JOIN " + DatabaseHelper.TABLE_ORDERS + " o ON od.order_id = o.order_id " +
                "JOIN " + DatabaseHelper.TABLE_PRODUCTS + " p ON od.product_id = p.product_id " +
                "GROUP BY p.product_id, p.product_name " +
                "ORDER BY revenue DESC " +
                "LIMIT ?";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(limit)});

        // Debug logging
        Log.d("TopProducts", "Total Cursor Count: " + cursor.getCount());

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int productNameIndex = cursor.getColumnIndex("product_name");
                int revenueIndex = cursor.getColumnIndex("revenue");
                int totalQuantityIndex = cursor.getColumnIndex("total_quantity");

                if (productNameIndex >= 0 && revenueIndex >= 0 && totalQuantityIndex >= 0) {
                    String productName = cursor.getString(productNameIndex);
                    double revenue = cursor.getDouble(revenueIndex);
                    int totalQuantity = cursor.getInt(totalQuantityIndex);

                    CategoryStatistics productStat = new CategoryStatistics(productName, revenue);
                    productStat.setQuantity(totalQuantity);
                    topProducts.add(productStat);

                    // Additional debug logging
                    Log.d("TopProducts", "Product: " + productName + ", Revenue: " + revenue + ", Quantity: " + totalQuantity);
                } else {
                    Log.e("TopProducts", "Column not found in cursor");
                }
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Log.d("TopProducts", "No products found");
        }
    } catch (Exception e) {
        Log.e("TopProducts", "Error retrieving top products", e);
    }

    return topProducts;
}

    // Thêm phương thức hỗ trợ chuyển đổi ngày
    private String convertDisplayDateToDbDate_2(String displayDate) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = displayFormat.parse(displayDate);
            return dbFormat.format(date);
        } catch (ParseException e) {
            Log.e("DatabaseManager", "Error converting date", e);
            return displayDate; // Trả về nguyên gốc nếu lỗi
        }
    }
    public List<CategoryStatistics> getProductRevenuePercentages(String startDate, String endDate) {
        List<CategoryStatistics> productPercentages = new ArrayList<>();
        String query = "SELECT p.product_name, " +
                "SUM(od.amount) as product_revenue, " +
                "(SUM(od.amount) * 100.0 / (SELECT SUM(total_amount) FROM orders WHERE date(order_date) BETWEEN date(?) AND date(?))) as revenue_percentage " +
                "FROM " + DatabaseHelper.TABLE_ORDER_DETAILS + " od " +
                "JOIN " + DatabaseHelper.TABLE_ORDERS + " o ON od.order_id = o.order_id " +
                "JOIN " + DatabaseHelper.TABLE_PRODUCTS + " p ON od.product_id = p.product_id " +
                "WHERE date(o.order_date) BETWEEN date(?) AND date(?) " +
                "GROUP BY p.product_id, p.product_name " +
                "ORDER BY product_revenue DESC";

        try {
            Cursor cursor = database.rawQuery(query,
                    new String[]{startDate, endDate, startDate, endDate});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String productName = cursor.getString(0);
                    double revenue = cursor.getDouble(1);
                    double percentage = cursor.getDouble(2);

                    CategoryStatistics stat = new CategoryStatistics(productName, revenue);
                    stat.setPercentage(percentage);
                    productPercentages.add(stat);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error calculating product revenue percentages", e);
        }

        return productPercentages;
    }
}