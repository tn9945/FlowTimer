package com.example.flowtimer.focus;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StrictFocusSessionStore {

    private static final String PREF_NAME = "strict_focus_session";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_RUNNING = "running";
    private static final String KEY_TARGET_DURATION = "target_duration";
    private static final String KEY_BLOCKED_LOG = "blocked_log";
    private static final String KEY_LAST_SUMMARY = "last_summary";
    private static final String ITEM_SEPARATOR = ";;";
    private static final String FIELD_SEPARATOR = "\t";

    private final SharedPreferences preferences;

    public StrictFocusSessionStore(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void start(long startTimeMillis, long targetDurationMillis) {
        preferences.edit()
                .putBoolean(KEY_RUNNING, true)
                .putLong(KEY_START_TIME, startTimeMillis)
                .putLong(KEY_TARGET_DURATION, targetDurationMillis)
                .putString(KEY_BLOCKED_LOG, "")
                .apply();
    }

    public boolean isRunning() {
        return preferences.getBoolean(KEY_RUNNING, false);
    }

    public long getStartTimeMillis() {
        return preferences.getLong(KEY_START_TIME, 0L);
    }

    public long getTargetDurationMillis() {
        return preferences.getLong(KEY_TARGET_DURATION, 25L * 60L * 1000L);
    }

    public long getElapsedMillis() {
        long start = getStartTimeMillis();
        if (start <= 0L) {
            return 0L;
        }
        return Math.max(0L, System.currentTimeMillis() - start);
    }

    public long getRemainMillis() {
        return Math.max(0L, getTargetDurationMillis() - getElapsedMillis());
    }

    public boolean isTargetReached() {
        return getElapsedMillis() >= getTargetDurationMillis();
    }

    public void addBlockedApp(String packageName, String appName) {
        String safePackage = sanitize(packageName);
        String safeName = sanitize(appName == null || appName.trim().isEmpty() ? "이 앱" : appName);
        String item = System.currentTimeMillis() + FIELD_SEPARATOR + safePackage + FIELD_SEPARATOR + safeName;
        String current = preferences.getString(KEY_BLOCKED_LOG, "");
        String next = current == null || current.isEmpty() ? item : current + ITEM_SEPARATOR + item;
        preferences.edit().putString(KEY_BLOCKED_LOG, next).apply();
    }

    public List<BlockedAppSummary> getBlockedSummaries() {
        Map<String, BlockedAppSummary> summaryMap = new LinkedHashMap<>();
        String log = preferences.getString(KEY_BLOCKED_LOG, "");
        if (log == null || log.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String[] items = log.split(ITEM_SEPARATOR);
        for (String item : items) {
            String[] fields = item.split(FIELD_SEPARATOR);
            if (fields.length < 3) {
                continue;
            }
            String packageName = fields[1];
            String appName = fields[2];
            BlockedAppSummary old = summaryMap.get(packageName);
            if (old == null) {
                summaryMap.put(packageName, new BlockedAppSummary(packageName, appName, 1));
            } else {
                summaryMap.put(packageName, new BlockedAppSummary(packageName, old.getAppName(), old.getCount() + 1));
            }
        }
        return new ArrayList<>(summaryMap.values());
    }

    public int getBlockedTotalCount() {
        int count = 0;
        for (BlockedAppSummary summary : getBlockedSummaries()) {
            count += summary.getCount();
        }
        return count;
    }

    public String buildSummaryText() {
        List<BlockedAppSummary> summaries = getBlockedSummaries();
        if (summaries.isEmpty()) {
            return "차단된 앱 실행 시도가 없습니다.";
        }
        StringBuilder builder = new StringBuilder();
        for (BlockedAppSummary summary : summaries) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(summary.getAppName()).append(" ").append(summary.getCount()).append("회");
        }
        return builder.toString();
    }

    public void finishAndSaveSummary() {
        String summary = "총 집중 시간: " + DurationFormatter.formatShortDuration(getElapsedMillis())
                + "\n목표 시간: " + DurationFormatter.formatShortDuration(getTargetDurationMillis())
                + "\n차단 앱 실행 시도: " + getBlockedTotalCount() + "회\n" + buildSummaryText();
        preferences.edit()
                .putString(KEY_LAST_SUMMARY, summary)
                .putBoolean(KEY_RUNNING, false)
                .remove(KEY_BLOCKED_LOG)
                .remove(KEY_START_TIME)
                .remove(KEY_TARGET_DURATION)
                .apply();
    }

    public String getLastSummary() {
        return preferences.getString(KEY_LAST_SUMMARY, "최근 강제 집중 기록이 없습니다.");
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(ITEM_SEPARATOR, " ").replace(FIELD_SEPARATOR, " ").replace("\n", " ");
    }

    public static class BlockedAppSummary {
        private final String packageName;
        private final String appName;
        private final int count;

        public BlockedAppSummary(String packageName, String appName, int count) {
            this.packageName = packageName;
            this.appName = appName;
            this.count = count;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getAppName() {
            return appName;
        }

        public int getCount() {
            return count;
        }
    }
}
