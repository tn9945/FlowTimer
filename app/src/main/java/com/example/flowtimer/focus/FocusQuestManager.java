package com.example.flowtimer.focus;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private static final String KEY_REWARD_DATE = "reward_date";
    private static final String KEY_REWARD_COIN = "reward_coin";
    private static final String KEY_REWARD_EXP = "reward_exp";
    private static final String KEY_REWARD_CLAIMED_DATE = "reward_claimed_date";
    private static final int DAILY_TARGET_MINUTES = 50;
    private static final int DAILY_TARGET_SESSIONS = 2;
    private static final int DAILY_BLOCK_LIMIT = 3;

    private final SharedPreferences preferences;

    public FocusQuestManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prepareTodayReward();
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

    public List<QuestItem> getDailyQuestItems() {
        List<QuestItem> items = new ArrayList<>();
        int minutes = getTodayMinutes();
        int sessions = getTodaySessions();
        int strictSessions = getTodayStrictSessions();
        int blocked = getTodayBlockedCount();
        items.add(new QuestItem("오늘 집중 " + Math.min(minutes, DAILY_TARGET_MINUTES) + "/" + DAILY_TARGET_MINUTES + "분", minutes >= DAILY_TARGET_MINUTES ? QuestState.SUCCESS : QuestState.PROGRESS));
        items.add(new QuestItem("세션 " + Math.min(sessions, DAILY_TARGET_SESSIONS) + "/" + DAILY_TARGET_SESSIONS + "회", sessions >= DAILY_TARGET_SESSIONS ? QuestState.SUCCESS : QuestState.PROGRESS));
        items.add(new QuestItem("강제 집중 " + (strictSessions > 0 ? "완료" : "1회 필요"), strictSessions > 0 ? QuestState.SUCCESS : QuestState.PROGRESS));
        items.add(new QuestItem("차단 시도 " + blocked + "/" + DAILY_BLOCK_LIMIT + "회 이하 유지", blocked <= DAILY_BLOCK_LIMIT ? QuestState.SUCCESS : QuestState.FAIL));
        return items;
    }

    public String getDailyQuestText() {
        StringBuilder builder = new StringBuilder();
        for (QuestItem item : getDailyQuestItems()) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(item.getText());
        }
        return builder.toString();
    }

    public boolean isAllDailyQuestCompleted() {
        for (QuestItem item : getDailyQuestItems()) {
            if (item.getState() != QuestState.SUCCESS) {
                return false;
            }
        }
        return true;
    }

    public boolean isTodayRewardClaimed() {
        return todayKey().equals(preferences.getString(KEY_REWARD_CLAIMED_DATE, ""));
    }

    public int getTodayRewardCoin() {
        prepareTodayReward();
        return preferences.getInt(KEY_REWARD_COIN, 15);
    }

    public int getTodayRewardExp() {
        prepareTodayReward();
        return preferences.getInt(KEY_REWARD_EXP, 10);
    }

    public boolean claimTodayRewardIfAvailable() {
        if (!isAllDailyQuestCompleted() || isTodayRewardClaimed()) {
            return false;
        }
        preferences.edit().putString(KEY_REWARD_CLAIMED_DATE, todayKey()).apply();
        return true;
    }

    public String getRewardText() {
        if (isTodayRewardClaimed()) {
            return "오늘 목표 보상 지급 완료";
        }
        return "오늘 목표 전체 달성 보상: 코인 " + getTodayRewardCoin() + " / 경험치 " + getTodayRewardExp();
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

    private void prepareTodayReward() {
        String today = todayKey();
        if (today.equals(preferences.getString(KEY_REWARD_DATE, ""))) {
            return;
        }
        int seed = Math.abs(today.hashCode());
        int coin = 15 + (seed % 16);
        int exp = 10 + ((seed / 7) % 16);
        preferences.edit()
                .putString(KEY_REWARD_DATE, today)
                .putInt(KEY_REWARD_COIN, coin)
                .putInt(KEY_REWARD_EXP, exp)
                .apply();
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

    public enum QuestState {
        SUCCESS,
        FAIL,
        PROGRESS
    }

    public static class QuestItem {
        private final String text;
        private final QuestState state;

        public QuestItem(String text, QuestState state) {
            this.text = text;
            this.state = state;
        }

        public String getText() {
            return text;
        }

        public QuestState getState() {
            return state;
        }
    }
}
