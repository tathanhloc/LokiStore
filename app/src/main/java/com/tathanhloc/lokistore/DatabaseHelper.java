package com.tathanhloc.lokistore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SalesDB";
    private static final int DATABASE_VERSION = 6;
    private Context context;
    private static DatabaseHelper instance;

    // Bảng Users
    public static final String TABLE_USERS = "users";
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + "user_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "email TEXT UNIQUE NOT NULL,"
            + "password TEXT NOT NULL,"
            + "full_name TEXT NOT NULL,"
            + "biometric_enabled INTEGER DEFAULT 0"
            + ")";

    // Bảng Product Categories (Loại sản phẩm)
    public static final String TABLE_CATEGORIES = "categories";
    private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE " + TABLE_CATEGORIES + "("
            + "category_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "category_code TEXT UNIQUE NOT NULL,"
            + "category_name TEXT NOT NULL,"
            + "is_active INTEGER DEFAULT 1" // 1: active, 0: deleted
            + ")";

    // Bảng Products - Đã thêm trường image
    public static final String TABLE_PRODUCTS = "products";
    private static final String CREATE_TABLE_PRODUCTS = "CREATE TABLE " + TABLE_PRODUCTS + "("
            + "product_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "product_code TEXT UNIQUE NOT NULL,"
            + "product_name TEXT NOT NULL,"
            + "category_id INTEGER,"
            + "price REAL NOT NULL,"
            + "description TEXT,"
            + "image_path TEXT,"
            + "is_active INTEGER DEFAULT 1," // 1: active, 0: deleted
            + "FOREIGN KEY(category_id) REFERENCES categories(category_id)"
            + ")";

    // Bảng Orders
    public static final String TABLE_ORDERS = "orders";
    private static final String CREATE_TABLE_ORDERS = "CREATE TABLE " + TABLE_ORDERS + "("
            + "order_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "order_code TEXT UNIQUE NOT NULL,"
            + "user_id INTEGER,"
            + "order_date TEXT NOT NULL,"
            + "delivery_date TEXT,"
            + "total_amount REAL NOT NULL,"
            + "note TEXT,"
            + "status TEXT DEFAULT 'PENDING',"
            + "FOREIGN KEY(user_id) REFERENCES users(user_id)"
            + ")";

    // Bảng Order Details
    public static final String TABLE_ORDER_DETAILS = "order_details";
    private static final String CREATE_TABLE_ORDER_DETAILS = "CREATE TABLE " + TABLE_ORDER_DETAILS + "("
            + "detail_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "order_id INTEGER,"
            + "product_id INTEGER,"
            + "quantity INTEGER NOT NULL,"
            + "price REAL NOT NULL,"
            + "amount REAL NOT NULL,"
            + "FOREIGN KEY(order_id) REFERENCES orders(order_id),"
            + "FOREIGN KEY(product_id) REFERENCES products(product_id)"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_PRODUCTS);
        db.execSQL(CREATE_TABLE_ORDERS);
        db.execSQL(CREATE_TABLE_ORDER_DETAILS);

        // Thêm dữ liệu mẫu
        insertDefaultData(db);
    }

    private void insertDefaultData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO users (email, password, full_name) VALUES ('admin@gmail.com', 'admin', 'Administrator')");
        db.execSQL("INSERT INTO categories (category_code, category_name) VALUES ('DM01', 'Điện thoại')");
        db.execSQL("INSERT INTO categories (category_code, category_name) VALUES ('DM02', 'Laptop')");
        db.execSQL("INSERT INTO categories (category_code, category_name) VALUES ('DM03', 'Máy tính bảng')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Kiểm tra và nâng cấp từng phiên bản
        if (oldVersion < 5) {
            // Bước 1: Nâng cấp cột biometric
            upgradeBiometricColumn(db);

            // Bước 2: Backup và nâng cấp bảng orders
            upgradeOrdersTable(db);

            // Bước 3: Thêm cột is_active cho products và categories
            upgradeProductAndCategoryTables(db);

            // Bước 4: Mã hóa mật khẩu người dùng
            upgradeUserPasswords(db);
        }
    }

    private void upgradeBiometricColumn(SQLiteDatabase db) {
        try {
            Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_USERS + ")", null);
            boolean columnExists = false;

            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex("name");
                if (nameIndex != -1) {
                    while (cursor.moveToNext()) {
                        String columnName = cursor.getString(nameIndex);
                        if ("biometric_enabled".equals(columnName)) {
                            columnExists = true;
                            break;
                        }
                    }
                }
                cursor.close();
            }

            // Chỉ thêm cột nếu chưa tồn tại
            if (!columnExists) {
                db.execSQL("ALTER TABLE " + TABLE_USERS +
                        " ADD COLUMN biometric_enabled INTEGER DEFAULT 0");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi thêm cột biometric_enabled", e);
        }
    }

    private void upgradeOrdersTable(SQLiteDatabase db) {
        // Backup dữ liệu orders
        db.execSQL("CREATE TABLE orders_backup AS SELECT * FROM " + TABLE_ORDERS);

        // Xóa bảng cũ
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);

        // Tạo bảng mới với cột status
        db.execSQL(CREATE_TABLE_ORDERS);

        // Khôi phục dữ liệu
        db.execSQL("INSERT INTO " + TABLE_ORDERS +
                " (order_id, order_code, user_id, order_date, delivery_date, total_amount, note) " +
                "SELECT order_id, order_code, user_id, order_date, delivery_date, total_amount, note " +
                "FROM orders_backup");

        // Xóa bảng backup
        db.execSQL("DROP TABLE IF EXISTS orders_backup");
    }

    private void upgradeProductAndCategoryTables(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTS +
                    " ADD COLUMN is_active INTEGER DEFAULT 1");
        } catch (Exception e) {
            // Bỏ qua nếu cột đã tồn tại
            Log.w("DatabaseHelper", "Cột is_active đã tồn tại ở bảng products");
        }

        try {
            db.execSQL("ALTER TABLE " + TABLE_CATEGORIES +
                    " ADD COLUMN is_active INTEGER DEFAULT 1");
        } catch (Exception e) {
            // Bỏ qua nếu cột đã tồn tại
            Log.w("DatabaseHelper", "Cột is_active đã tồn tại ở bảng categories");
        }
    }

    private void upgradeUserPasswords(SQLiteDatabase db) {
        try {
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{"user_id", "email", "password"},
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                    String plainPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));

                    // Mã hóa mật khẩu
                    String hashedPassword = PasswordSecurityManager.hashPassword(plainPassword);

                    // Cập nhật mật khẩu đã mã hóa
                    ContentValues values = new ContentValues();
                    values.put("password", hashedPassword);

                    db.update(TABLE_USERS, values,
                            "user_id = ?",
                            new String[]{String.valueOf(userId)});

                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseUpgrade", "Lỗi mã hóa mật khẩu", e);
        }
    }
}