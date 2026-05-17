package com.example.flowtimer.focus;

import android.content.Context;
import android.content.SharedPreferences;

public class ActiveFocusSessionStore {

    public static final String MODE_STOPWATCH = "stopwatch";
    public static final String MODE_TIMER = "timer";

    private static final String PREF_NAME = "focus_session_store";
    private static final String KEY_RUNNING = "running";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_PAUSED = "paused";
    private static final String KEY_PAUSED_AT = "paused_at";
    private static final String KEY_ACCUMULATED_PAUSED_TIME = "accumulated_paused_time";
    private static final String KEY_TIMER_MODE = "timer_mode";
    private static final String KEY_TARGET_DURATION = "target_duration";

    private final SharedPreferences preferences;

    public ActiveFocusSessionStore(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveRunningSession(String userId, long startTimeMillis) {
        saveRunningSession(userId, startTimeMillis, MODE_STOPWATCH, 0L);
    }

    public void saveRunningSession(String userId, long startTimeMillis, String timerMode, long targetDurationMillis) {
        preferences.edit()
                .putBoolean(KEY_RUNNING, true)
                .putString(KEY_USER_ID, userId)
                .putLong(KEY_START_TIME, startTimeMillis)
                .putBoolean(KEY_PAUSED, false)
                .putLong(KEY_PAUSED_AT, 0L)
                .putLong(KEY_ACCUMULATED_PAUSED_TIME, 0L)
                .putString(KEY_TIMER_MODE, MODE_TIMER.equals(timerMode) ? MODE_TIMER : MODE_STOPWATCH)
                .putLong(KEY_TARGET_DURATION, Math.max(0L, targetDurationMillis))
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

    public String getTimerMode() {
        return preferences.getString(KEY_TIMER_MODE, MODE_STOPWATCH);
    }

    public boolean isTimerMode() {
        return MODE_TIMER.equals(getTimerMode()) && getTargetDurationMillis() > 0L;
    }

    public long getTargetDurationMillis() {
        return preferences.getLong(KEY_TARGET_DURATION, 0L);
    }

    public long getRemainDurationMillis() {
        if (!isTimerMode()) {
            return 0L;
        }
        return Math.max(0L, getTargetDurationMillis() - getElapsedDurationMillis());
    }

    public boolean isTimerFinished() {
        return isTimerMode() && getElapsedDurationMillis() >= getTargetDurationMillis();
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
