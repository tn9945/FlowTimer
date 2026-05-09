package com.example.flowtimer;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.flowtimer.data.UserEntity;

public class SessionManager {

    private static final String PREF_NAME = "flowtimer_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_IDENTIFIER = "user_identifier";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveLogin(UserEntity user) {
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putLong(KEY_USER_ID, user.getId())
                .putString(KEY_USER_NAME, user.getName())
                .putString(KEY_USER_IDENTIFIER, user.getUserId())
                .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1L);
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }

    public String getUserIdentifier() {
        return preferences.getString(KEY_USER_IDENTIFIER, "");
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}
