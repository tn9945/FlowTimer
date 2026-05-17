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
    private static final int DAILY_TARGET_MINUTES = 25;

    private final SharedPreferences preferences;

    public FocusQuestManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void recordFocus(long durationMillis, int blockedCount) {
        String today = todayKey();
        String lastDate = preferences.getString(KEY_LAST_FOCUS_DATE, "");
        int todayMinutes = today.equals(lastDate) ? preferences.getInt(KEY_TODAY_MINUTES, 0) : 0;
        int todayBlocked = today.equals(lastDate) ? preferences.getInt(KEY_TODAY_BLOCKED, 0) : 0;
        int streak = preferences.getInt(KEY_STREAK, 0);
        if (!today.equals(lastDate)) {
            streak = isYesterday(lastDate) ? streak + 1 : 1;
        }
        preferences.edit()
                .putString(KEY_LAST_FOCUS_DATE, today)
                .putInt(KEY_STREAK, streak)
                .putInt(KEY_TODAY_MINUTES, todayMinutes + (int) (durationMillis / 60000L))
                .putInt(KEY_TODAY_BLOCKED, todayBlocked + blockedCount)
                .apply();
    }

    public String getDailyQuestText() {
        String today = todayKey();
        int minutes = today.equals(preferences.getString(KEY_LAST_FOCUS_DATE, "")) ? preferences.getInt(KEY_TODAY_MINUTES, 0) : 0;
        int remain = Math.max(0, DAILY_TARGET_MINUTES - minutes);
        if (remain <= 0) {
            return "오늘 퀘스트 완료: 25분 이상 집중하였습니다.";
        }
        return "오늘 퀘스트: " + remain + "분 더 집중하면 완료됩니다.";
    }

    public String getStreakText() {
        return "연속 집중 " + preferences.getInt(KEY_STREAK, 0) + "일";
    }

    public int getTodayBlockedCount() {
        String today = todayKey();
        return today.equals(preferences.getString(KEY_LAST_FOCUS_DATE, "")) ? preferences.getInt(KEY_TODAY_BLOCKED, 0) : 0;
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
