package com.example.flowtimer.focus;

public class AppUsageSummary {

    private final String packageName;
    private final String appName;
    private final String category;
    private long durationMillis;
    private int launchCount;

    public AppUsageSummary(String packageName, String appName, String category) {
        this.packageName = packageName;
        this.appName = appName;
        this.category = category;
    }

    public void addDuration(long value) {
        durationMillis += Math.max(0L, value);
    }

    public void incrementLaunchCount() {
        launchCount++;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public String getCategory() {
        return category;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public int getLaunchCount() {
        return launchCount;
    }
}
