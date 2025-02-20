package com.tathanhloc.lokistore;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_NAME = "username";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_LAST_USER_NAME = "last_username";
    private static final String KEY_LAST_USER_EMAIL = "last_user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";



    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void createLoginSession(String email, String fullName) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_NAME, fullName);
        editor.putString(KEY_USER_EMAIL, email);
        // Lưu thông tin người dùng cuối cùng
        editor.putString(KEY_LAST_USER_NAME, fullName);
        editor.putString(KEY_LAST_USER_EMAIL, email);
        editor.apply();
    }

    public void logout() {
        // Chỉ xóa thông tin đăng nhập hiện tại, giữ lại thông tin người dùng cuối
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_EMAIL);
        editor.apply();

        // Xóa thông tin đăng nhập được lưu
        SharedPreferences loginPrefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor loginEditor = loginPrefs.edit();
        loginEditor.clear();
        loginEditor.apply();
    }

    public void clearAllData() {
        // Xóa tất cả dữ liệu, bao gồm cả thông tin người dùng cuối
        editor.clear();
        editor.apply();

        // Xóa thông tin đăng nhập được lưu
        SharedPreferences loginPrefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor loginEditor = loginPrefs.edit();
        loginEditor.clear();
        loginEditor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public String getLastUserName() {
        return sharedPreferences.getString(KEY_LAST_USER_NAME, "");
    }

    public String getLastUserEmail() {
        return sharedPreferences.getString(KEY_LAST_USER_EMAIL, "");
    }

    public void saveUserName(String userName) {
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    public long getLoginTime() {
        return sharedPreferences.getLong("login_timestamp", 0);
    }

    public void saveLoginTime(long loginTime) {
        editor.putLong("login_timestamp", loginTime);
        editor.apply();
    }

    public void clearSession() {
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_EMAIL);
        editor.remove("login_timestamp");
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    public void setBiometricEnabled(boolean enabled) {
        editor.putBoolean(KEY_BIOMETRIC_ENABLED, enabled);
        editor.apply();
    }

    public boolean isBiometricEnabled() {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }


    public void saveLastUserEmail(String email) {
        editor.putString(KEY_LAST_USER_EMAIL, email);
        editor.apply();
    }


}