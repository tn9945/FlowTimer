package com.example.flowtimer.focus;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FocusQuestManager {

    private static final String PREF_NAME = "focus_quest_data";
    private static final String KEY_LAST_FOCUS_DATE = "last_focus_date";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_TODAY_MINUTES = "today_minutes";
    private static final String KEY_TODAY_BLOCKED = "today_blocked";
    private static final String KEY_TODAY_SESSIONS = "today_sessions";
    private static final String KEY_TODAY_STRICT_SESSIONS = "today_strict_sessions";
    private static final String KEY_TODAY_TIMER_SESSIONS = "today_timer_sessions";
    private static final int DAILY_TARGET_MINUTES = 50;
    private static final int DAILY_TARGET_SESSIONS = 2;
    private static final int DAILY_BLOCK_LIMIT = 3;

    private final SharedPreferences preferences;

    public FocusQuestManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void recordFocus(long durationMillis, int blockedCount) {
        recordFocus(durationMillis, blockedCount, false, false);
    }

    public void recordFocus(long durationMillis, int blockedCount, boolean strictMode, boolean timerMode) {
        String today = todayKey();
        String lastDate = preferences.getString(KEY_LAST_FOCUS_DATE, "");
        int todayMinutes = today.equals(lastDate) ? preferences.getInt(KEY_TODAY_MINUTES, 0) : 0;
        int todayBlocked = today.equals(lastDate) ? preferences.getInt(KEY_TODAY_BLOCKED, 0) : 0;
        int todaySessions = today.equals(lastDate) ? preferences.getInt(KEY_TODAY_SESSIONS, 0) : 0;
        int todayStrictSessions = today.equals(lastDate) ? preferences.getInt(KEY_TODAY_STRICT_SESSIONS, 0) : 0;
        int todayTimerSessions = today.equals(lastDate) ? preferences.getInt(KEY_TODAY_TIMER_SESSIONS, 0) : 0;
        int streak = preferences.getInt(KEY_STREAK, 0);
        if (!today.equals(lastDate)) {
            streak = isYesterday(lastDate) ? streak + 1 : 1;
        }
        preferences.edit()
                .putString(KEY_LAST_FOCUS_DATE, today)
                .putInt(KEY_STREAK, streak)
                .putInt(KEY_TODAY_MINUTES, todayMinutes + (int) (durationMillis / 60000L))
                .putInt(KEY_TODAY_BLOCKED, todayBlocked + blockedCount)
                .putInt(KEY_TODAY_SESSIONS, todaySessions + 1)
                .putInt(KEY_TODAY_STRICT_SESSIONS, todayStrictSessions + (strictMode ? 1 : 0))
                .putInt(KEY_TODAY_TIMER_SESSIONS, todayTimerSessions + (timerMode ? 1 : 0))
                .apply();
    }

    public String getDailyQuestText() {
        int minutes = getTodayMinutes();
        int sessions = getTodaySessions();
        int strictSessions = getTodayStrictSessions();
        int blocked = getTodayBlockedCount();
        StringBuilder builder = new StringBuilder();
        builder.append("오늘 집중 ").append(Math.min(minutes, DAILY_TARGET_MINUTES)).append("/").append(DAILY_TARGET_MINUTES).append("분");
        builder.append("\n세션 ").append(Math.min(sessions, DAILY_TARGET_SESSIONS)).append("/").append(DAILY_TARGET_SESSIONS).append("회");
        builder.append("\n강제 집중 ").append(strictSessions > 0 ? "완료" : "1회 필요");
        builder.append("\n차단 시도 ").append(blocked).append("/").append(DAILY_BLOCK_LIMIT).append("회 이하 유지");
        return builder.toString();
    }

    public String getStreakText() {
        return "연속 집중 " + preferences.getInt(KEY_STREAK, 0) + "일";
    }

    public int getTodayBlockedCount() {
        return isTodayStored() ? preferences.getInt(KEY_TODAY_BLOCKED, 0) : 0;
    }

    public int getTodayMinutes() {
        return isTodayStored() ? preferences.getInt(KEY_TODAY_MINUTES, 0) : 0;
    }

    public int getTodaySessions() {
        return isTodayStored() ? preferences.getInt(KEY_TODAY_SESSIONS, 0) : 0;
    }

    public int getTodayStrictSessions() {
        return isTodayStored() ? preferences.getInt(KEY_TODAY_STRICT_SESSIONS, 0) : 0;
    }

    public int getTodayTimerSessions() {
        return isTodayStored() ? preferences.getInt(KEY_TODAY_TIMER_SESSIONS, 0) : 0;
    }

    private boolean isTodayStored() {
        return todayKey().equals(preferences.getString(KEY_LAST_FOCUS_DATE, ""));
    }

    private String todayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private boolean isYesterday(String dateKey) {
        if (dateKey == null || dateKey.isEmpty()) {
            return false;
        }
        long yesterdayMillis = System.currentTimeMillis() - 86400000L;
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(yesterdayMillis));
        return yesterday.equals(dateKey);
    }
}
