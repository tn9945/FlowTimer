package com.example.flowtimer.focus;

import android.content.Context;
import android.content.SharedPreferences;

public class ActiveFocusSessionStore {

    private static final String PREF_NAME = "focus_session_store";
    private static final String KEY_RUNNING = "running";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_START_TIME = "start_time";

    private final SharedPreferences preferences;

    public ActiveFocusSessionStore(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveRunningSession(String userId, long startTimeMillis) {
        preferences.edit()
                .putBoolean(KEY_RUNNING, true)
                .putString(KEY_USER_ID, userId)
                .putLong(KEY_START_TIME, startTimeMillis)
                .apply();
    }

    public boolean isRunningForUser(String userId) {
        return preferences.getBoolean(KEY_RUNNING, false)
                && userId != null
                && userId.equals(preferences.getString(KEY_USER_ID, ""));
    }

    public long getStartTimeMillis() {
        return preferences.getLong(KEY_START_TIME, 0L);
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
