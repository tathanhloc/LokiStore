// SessionManager.java
package com.tathanhloc.lokistore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_NAME = "username";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.contains(KEY_USER_NAME);
    }

    public void logout() {
        // Xóa dữ liệu trong SharedPreferences
        editor.clear();
        editor.commit();
        SharedPreferences loginPrefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor loginEditor = loginPrefs.edit();
        loginEditor.clear();
        loginEditor.commit();
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
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

    public void createLoginSession(String email, String fullName) {
        editor.putString(KEY_USER_NAME, fullName);
        editor.apply();
    }
}