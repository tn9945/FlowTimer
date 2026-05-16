package com.example.flowtimer.focus;

import android.content.Context;
import android.content.SharedPreferences;

public class ActiveFocusSessionStore {

    private static final String PREF_NAME = "focus_session_store";
    private static final String KEY_RUNNING = "running";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_PAUSED = "paused";
    private static final String KEY_PAUSED_AT = "paused_at";
    private static final String KEY_ACCUMULATED_PAUSED_TIME = "accumulated_paused_time";

    private final SharedPreferences preferences;

    public ActiveFocusSessionStore(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveRunningSession(String userId, long startTimeMillis) {
        preferences.edit()
                .putBoolean(KEY_RUNNING, true)
                .putString(KEY_USER_ID, userId)
                .putLong(KEY_START_TIME, startTimeMillis)
                .putBoolean(KEY_PAUSED, false)
                .putLong(KEY_PAUSED_AT, 0L)
                .putLong(KEY_ACCUMULATED_PAUSED_TIME, 0L)
                .apply();
    }

    public boolean isRunningForUser(String userId) {
        return preferences.getBoolean(KEY_RUNNING, false)
                && userId != null
                && userId.equals(preferences.getString(KEY_USER_ID, ""));
    }

    public boolean isRunning() {
        return preferences.getBoolean(KEY_RUNNING, false);
    }

    public String getUserId() {
        return preferences.getString(KEY_USER_ID, "");
    }

    public long getStartTimeMillis() {
        return preferences.getLong(KEY_START_TIME, 0L);
    }

    public boolean isPaused() {
        return preferences.getBoolean(KEY_PAUSED, false);
    }

    public void pause() {
        if (!isRunning() || isPaused()) {
            return;
        }
        preferences.edit()
                .putBoolean(KEY_PAUSED, true)
                .putLong(KEY_PAUSED_AT, System.currentTimeMillis())
                .apply();
    }

    public void resume() {
        if (!isRunning() || !isPaused()) {
            return;
        }
        long pausedAt = preferences.getLong(KEY_PAUSED_AT, 0L);
        long accumulated = preferences.getLong(KEY_ACCUMULATED_PAUSED_TIME, 0L);
        long additionalPausedTime = pausedAt > 0L ? Math.max(0L, System.currentTimeMillis() - pausedAt) : 0L;
        preferences.edit()
                .putBoolean(KEY_PAUSED, false)
                .putLong(KEY_PAUSED_AT, 0L)
                .putLong(KEY_ACCUMULATED_PAUSED_TIME, accumulated + additionalPausedTime)
                .apply();
    }

    public long getElapsedDurationMillis() {
        long startTimeMillis = getStartTimeMillis();
        if (startTimeMillis <= 0L) {
            return 0L;
        }
        long now = isPaused() ? preferences.getLong(KEY_PAUSED_AT, System.currentTimeMillis()) : System.currentTimeMillis();
        long accumulatedPausedTime = preferences.getLong(KEY_ACCUMULATED_PAUSED_TIME, 0L);
        return Math.max(0L, now - startTimeMillis - accumulatedPausedTime);
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
