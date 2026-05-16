package com.example.flowtimer.focus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FocusStatsSnapshot {

    public static class StatItem {
        private final String label;
        private final long durationMillis;
        private final String packageName;

        public StatItem(String label, long durationMillis) {
            this(label, durationMillis, null);
        }

        public StatItem(String label, long durationMillis, String packageName) {
            this.label = label;
            this.durationMillis = durationMillis;
            this.packageName = packageName;
        }

        public String getLabel() { return label; }
        public long getDurationMillis() { return durationMillis; }
        public String getPackageName() { return packageName; }
    }

    public static class DailyFocusChartItem {
        private final String label;
        private final long totalDurationMillis;
        private final long effectiveFocusDurationMillis;
        private final long distractionDurationMillis;

        public DailyFocusChartItem(String label, long totalDurationMillis, long effectiveFocusDurationMillis, long distractionDurationMillis) {
            this.label = label;
            this.totalDurationMillis = totalDurationMillis;
            this.effectiveFocusDurationMillis = effectiveFocusDurationMillis;
            this.distractionDurationMillis = distractionDurationMillis;
        }

        public String getLabel() { return label; }
        public long getTotalDurationMillis() { return totalDurationMillis; }
        public long getEffectiveFocusDurationMillis() { return effectiveFocusDurationMillis; }
        public long getDistractionDurationMillis() { return distractionDurationMillis; }
    }

    private final long totalFocusDurationMillis;
    private final long totalBreakDurationMillis;
    private final long totalStudyDurationMillis;
    private final long totalDistractionDurationMillis;
    private final float averageFocusScore;
    private final int totalSessionCount;
    private final List<StatItem> dailyItems;
    private final List<StatItem> weeklyItems;
    private final List<StatItem> monthlyItems;
    private final List<StatItem> hourlyItems;
    private final List<StatItem> appItems;
    private final List<DailyFocusChartItem> recentDailyChartItems;

    public FocusStatsSnapshot(long totalFocusDurationMillis,
                              long totalBreakDurationMillis,
                              long totalStudyDurationMillis,
                              long totalDistractionDurationMillis,
                              float averageFocusScore,
                              int totalSessionCount,
                              List<StatItem> dailyItems,
                              List<StatItem> weeklyItems,
                              List<StatItem> monthlyItems,
                              List<StatItem> hourlyItems,
                              List<StatItem> appItems,
                              List<DailyFocusChartItem> recentDailyChartItems) {
        this.totalFocusDurationMillis = totalFocusDurationMillis;
        this.totalBreakDurationMillis = totalBreakDurationMillis;
        this.totalStudyDurationMillis = totalStudyDurationMillis;
        this.totalDistractionDurationMillis = totalDistractionDurationMillis;
        this.averageFocusScore = averageFocusScore;
        this.totalSessionCount = totalSessionCount;
        this.dailyItems = new ArrayList<>(dailyItems);
        this.weeklyItems = new ArrayList<>(weeklyItems);
        this.monthlyItems = new ArrayList<>(monthlyItems);
        this.hourlyItems = new ArrayList<>(hourlyItems);
        this.appItems = new ArrayList<>(appItems);
        this.recentDailyChartItems = new ArrayList<>(recentDailyChartItems);
    }

    public long getTotalFocusDurationMillis() { return totalFocusDurationMillis; }
    public long getTotalBreakDurationMillis() { return totalBreakDurationMillis; }
    public long getTotalStudyDurationMillis() { return totalStudyDurationMillis; }
    public long getTotalDistractionDurationMillis() { return totalDistractionDurationMillis; }
    public float getAverageFocusScore() { return averageFocusScore; }
    public int getTotalSessionCount() { return totalSessionCount; }
    public List<StatItem> getDailyItems() { return Collections.unmodifiableList(dailyItems); }
    public List<StatItem> getWeeklyItems() { return Collections.unmodifiableList(weeklyItems); }
    public List<StatItem> getMonthlyItems() { return Collections.unmodifiableList(monthlyItems); }
    public List<StatItem> getHourlyItems() { return Collections.unmodifiableList(hourlyItems); }
    public List<StatItem> getAppItems() { return Collections.unmodifiableList(appItems); }
    public List<DailyFocusChartItem> getRecentDailyChartItems() { return Collections.unmodifiableList(recentDailyChartItems); }
}
