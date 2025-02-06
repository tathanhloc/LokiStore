package com.tathanhloc.lokistore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SalesDB";
    private static final int DATABASE_VERSION = 2;
    private Context context;
    private static DatabaseHelper instance;


    // Bảng Users
    public static final String TABLE_USERS = "users";
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + "user_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "email TEXT UNIQUE NOT NULL,"
            + "password TEXT NOT NULL,"
            + "full_name TEXT NOT NULL"
            + ")";
    // Bảng Product Categories (Loại sản phẩm)
    public static final String TABLE_CATEGORIES = "categories";
    private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE " + TABLE_CATEGORIES + "("
            + "category_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "category_code TEXT UNIQUE NOT NULL,"
            + "category_name TEXT NOT NULL"
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
            + "image_path TEXT,"  // Thêm trường lưu đường dẫn ảnh
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

    private boolean isDatabaseExists() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}