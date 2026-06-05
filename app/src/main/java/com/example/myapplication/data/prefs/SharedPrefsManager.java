package com.example.myapplication.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private static final String PREF_NAME = "TravelTourPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_CONTACT = "user_contact";

    private final SharedPreferences sharedPreferences;

    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserSession(int userId, String contact) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_CONTACT, contact);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public String getUserContact() {
        return sharedPreferences.getString(KEY_USER_CONTACT, null);
    }

    public void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
