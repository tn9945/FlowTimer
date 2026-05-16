package com.example.flowtimer.focus;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.flowtimer.data.AppDatabase;
import com.example.flowtimer.data.AppUsageRecordDao;
import com.example.flowtimer.data.AppUsageRecordEntity;
import com.example.flowtimer.data.FocusSessionDao;
import com.example.flowtimer.data.FocusSessionEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FocusRepository {

    public interface SaveCallback {
        void onSaved(long sessionId);
    }

    public interface SessionDetailCallback {
        void onLoaded(FocusSessionEntity session, List<AppUsageRecordEntity> records);
    }

    public interface StatsCallback {
        void onLoaded(FocusStatsSnapshot snapshot);
    }

    public interface ActionCallback {
        void onComplete();
    }

    private final FocusSessionDao focusSessionDao;
    private final AppUsageRecordDao appUsageRecordDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public FocusRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        focusSessionDao = database.focusSessionDao();
        appUsageRecordDao = database.appUsageRecordDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void saveSession(String userId, String userName, FocusAnalysisResult result, RewardResult rewardResult, SaveCallback callback) {
        executorService.execute(() -> {
            FocusSessionEntity session = new FocusSessionEntity(
                    userId,
                    userName,
                    result.getStartTimeMillis(),
                    result.getEndTimeMillis(),
                    result.getTotalDurationMillis(),
                    result.getActiveDurationMillis(),
                    result.getBreakDurationMillis(),
                    result.getStudyDurationMillis(),
                    result.getDistractionDurationMillis(),
                    result.getNeutralDurationMillis(),
                    result.getEffectiveFocusDurationMillis(),
                    result.getFocusScore(),
                    rewardResult.getCoin(),
                    rewardResult.getExp(),
                    rewardResult.getTimeMinutes(),
                    result.getAppSwitchCount(),
                    result.getAppUsageSummaries().size(),
                    result.getTopAppName(),
                    result.getTopAppDurationMillis()
            );
            long sessionId = focusSessionDao.insert(session);
            List<AppUsageRecordEntity> records = new ArrayList<>();
            for (AppUsageSummary summary : result.getAppUsageSummaries()) {
                records.add(new AppUsageRecordEntity(
                        sessionId,
                        userId,
                        summary.getPackageName(),
                        summary.getAppName(),
                        summary.getDurationMillis(),
                        summary.getLaunchCount(),
                        summary.getCategory()
                ));
            }
            appUsageRecordDao.insertAll(records);
            mainHandler.post(() -> callback.onSaved(sessionId));
        });
    }

    public void loadSessionDetail(long sessionId, SessionDetailCallback callback) {
        executorService.execute(() -> {
            FocusSessionEntity session = focusSessionDao.findById(sessionId);
            List<AppUsageRecordEntity> records = appUsageRecordDao.findBySessionId(sessionId);
            mainHandler.post(() -> callback.onLoaded(session, records));
        });
    }

    public void loadStats(String userId, StatsCallback callback) {
        executorService.execute(() -> {
            List<FocusSessionEntity> sessions = focusSessionDao.findAllByUserId(userId);
            List<AppUsageRecordEntity> appRecords = appUsageRecordDao.findAllByUserId(userId);
            long totalFocus = 0L;
            long totalBreak = 0L;
            long totalStudy = 0L;
            long totalDistraction = 0L;
            float scoreSum = 0f;
            Map<String, Long> dailyMap = new LinkedHashMap<>();
            Map<String, Long> weeklyMap = new LinkedHashMap<>();
            Map<String, Long> monthlyMap = new LinkedHashMap<>();
            Map<String, Long> hourlyMap = new LinkedHashMap<>();
            Map<String, Long> appDurationMap = new LinkedHashMap<>();
            Map<String, String> appLabelMap = new LinkedHashMap<>();
            Map<String, FocusStatsSnapshot.DailyFocusChartItem> chartMap = createRecentChartBase();

            for (FocusSessionEntity session : sessions) {
                long focusValue = session.getEffectiveFocusDurationMillis() > 0L ? session.getEffectiveFocusDurationMillis() : session.getActiveDurationMillis();
                totalFocus += focusValue;
                totalBreak += session.getBreakDurationMillis();
                totalStudy += session.getStudyDurationMillis();
                totalDistraction += session.getDistractionDurationMillis();
                scoreSum += session.getFocusScore();
                String dayKey = DurationFormatter.formatDate(session.getStartTimeMillis());
                String weekKey = DurationFormatter.formatWeek(session.getStartTimeMillis());
                String monthKey = DurationFormatter.formatMonth(session.getStartTimeMillis());
                String hourKey = DurationFormatter.formatHour(session.getStartTimeMillis());
                dailyMap.put(dayKey, dailyMap.getOrDefault(dayKey, 0L) + focusValue);
                weeklyMap.put(weekKey, weeklyMap.getOrDefault(weekKey, 0L) + focusValue);
                monthlyMap.put(monthKey, monthlyMap.getOrDefault(monthKey, 0L) + focusValue);
                hourlyMap.put(hourKey, hourlyMap.getOrDefault(hourKey, 0L) + focusValue);
                if (chartMap.containsKey(dayKey)) {
                    FocusStatsSnapshot.DailyFocusChartItem old = chartMap.get(dayKey);
                    chartMap.put(dayKey, new FocusStatsSnapshot.DailyFocusChartItem(
                            dayKey,
                            old.getTotalDurationMillis() + session.getTotalDurationMillis(),
                            old.getEffectiveFocusDurationMillis() + focusValue,
                            old.getDistractionDurationMillis() + session.getDistractionDurationMillis()
                    ));
                }
            }

            for (AppUsageRecordEntity record : appRecords) {
                String packageKey = record.getPackageName();
                if (packageKey == null || packageKey.trim().isEmpty()) {
                    packageKey = record.getAppName();
                }
                appDurationMap.put(packageKey, appDurationMap.getOrDefault(packageKey, 0L) + record.getDurationMillis());
                if (!appLabelMap.containsKey(packageKey)) {
                    appLabelMap.put(packageKey, record.getAppName());
                }
            }

            List<FocusStatsSnapshot.StatItem> dailyItems = toSortedStatItems(dailyMap);
            List<FocusStatsSnapshot.StatItem> weeklyItems = toSortedStatItems(weeklyMap);
            List<FocusStatsSnapshot.StatItem> monthlyItems = toSortedStatItems(monthlyMap);
            List<FocusStatsSnapshot.StatItem> hourlyItems = toSortedStatItems(hourlyMap);
            List<FocusStatsSnapshot.StatItem> appItems = toSortedAppItems(appDurationMap, appLabelMap);
            List<FocusStatsSnapshot.DailyFocusChartItem> chartItems = new ArrayList<>(chartMap.values());
            float averageScore = sessions.isEmpty() ? 0f : scoreSum / sessions.size();

            FocusStatsSnapshot snapshot = new FocusStatsSnapshot(totalFocus, totalBreak, totalStudy, totalDistraction, averageScore, sessions.size(), dailyItems, weeklyItems, monthlyItems, hourlyItems, appItems, chartItems);
            mainHandler.post(() -> callback.onLoaded(snapshot));
        });
    }

    public void clearAllFocusData(String userId, ActionCallback callback) {
        executorService.execute(() -> {
            appUsageRecordDao.deleteByUserId(userId);
            focusSessionDao.deleteByUserId(userId);
            mainHandler.post(callback::onComplete);
        });
    }

    public void generateSampleData(String userId, String userName, ActionCallback callback) {
        executorService.execute(() -> {
            long now = System.currentTimeMillis();
            for (int i = 0; i < 7; i++) {
                long start = now - ((long) (i + 1) * 86_400_000L) + 7_200_000L;
                long total = 3_600_000L + (i * 300_000L);
                long breakTime = 300_000L + (i * 60_000L);
                long studyTime = 1_800_000L + (i * 180_000L);
                long distractionTime = 300_000L + (i * 30_000L);
                long neutralTime = Math.max(0L, total - breakTime - studyTime - distractionTime);
                long effectiveFocus = Math.max(0L, studyTime + (neutralTime / 2L) - (distractionTime / 3L));
                int score = Math.min(100, 60 + (i * 4));
                FocusSessionEntity session = new FocusSessionEntity(
                        userId,
                        userName,
                        start,
                        start + total,
                        total,
                        total - breakTime,
                        breakTime,
                        studyTime,
                        distractionTime,
                        neutralTime,
                        effectiveFocus,
                        score,
                        5 + i,
                        8 + i,
                        (int) (effectiveFocus / 60_000L),
                        2 + i,
                        3,
                        "Chrome",
                        studyTime
                );
                long sessionId = focusSessionDao.insert(session);
                List<AppUsageRecordEntity> records = new ArrayList<>();
                records.add(new AppUsageRecordEntity(sessionId, userId, "com.android.chrome", "Chrome", studyTime, 2, FocusCategory.STUDY));
                records.add(new AppUsageRecordEntity(sessionId, userId, "com.google.android.youtube", "YouTube", distractionTime, 1, FocusCategory.DISTRACTION));
                records.add(new AppUsageRecordEntity(sessionId, userId, "com.google.android.apps.docs", "Google Docs", neutralTime, 1, FocusCategory.NEUTRAL));
                appUsageRecordDao.insertAll(records);
            }
            mainHandler.post(callback::onComplete);
        });
    }

    private Map<String, FocusStatsSnapshot.DailyFocusChartItem> createRecentChartBase() {
        Map<String, FocusStatsSnapshot.DailyFocusChartItem> result = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        for (int i = 0; i < 7; i++) {
            String label = DurationFormatter.formatDate(calendar.getTimeInMillis());
            result.put(label, new FocusStatsSnapshot.DailyFocusChartItem(label, 0L, 0L, 0L));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return result;
    }

    private List<FocusStatsSnapshot.StatItem> toSortedStatItems(Map<String, Long> values) {
        List<FocusStatsSnapshot.StatItem> items = new ArrayList<>();
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            items.add(new FocusStatsSnapshot.StatItem(entry.getKey(), entry.getValue()));
        }
        items.sort(Comparator.comparingLong(FocusStatsSnapshot.StatItem::getDurationMillis).reversed());
        return items;
    }

    private List<FocusStatsSnapshot.StatItem> toSortedAppItems(Map<String, Long> durationMap, Map<String, String> labelMap) {
        List<FocusStatsSnapshot.StatItem> items = new ArrayList<>();
        for (Map.Entry<String, Long> entry : durationMap.entrySet()) {
            String key = entry.getKey();
            items.add(new FocusStatsSnapshot.StatItem(labelMap.getOrDefault(key, key), entry.getValue(), key));
        }
        items.sort(Comparator.comparingLong(FocusStatsSnapshot.StatItem::getDurationMillis).reversed());
        return items;
    }
}
