package com.example.flowtimer.focus;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppUsageAnalyzer {

    private final FocusScoreCalculator focusScoreCalculator = new FocusScoreCalculator();

    public FocusAnalysisResult analyze(Context context, long startTimeMillis, long endTimeMillis) {
        return analyze(context, startTimeMillis, endTimeMillis, 0L);
    }

    public FocusAnalysisResult analyze(Context context, long startTimeMillis, long endTimeMillis, long manualBreakDurationMillis) {
        long safeEnd = Math.max(endTimeMillis, startTimeMillis);
        long totalDuration = Math.max(0L, safeEnd - startTimeMillis);
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            long breakDuration = Math.min(totalDuration, Math.max(0L, manualBreakDurationMillis));
            long focusDuration = Math.max(0L, totalDuration - breakDuration);
            int score = focusScoreCalculator.calculateFocusScore(totalDuration, focusDuration, 0L, 0);
            return new FocusAnalysisResult(startTimeMillis, safeEnd, totalDuration, focusDuration, breakDuration, 0L, 0L, 0L, focusDuration, score, 0, new ArrayList<>());
        }

        UsageEvents usageEvents = usageStatsManager.queryEvents(startTimeMillis, safeEnd);
        UsageEvents.Event event = new UsageEvents.Event();
        Map<String, AppUsageSummary> appUsageMap = new HashMap<>();
        PackageManager packageManager = context.getPackageManager();

        String currentPackage = null;
        long currentStart = -1L;
        String lastForegroundPackage = null;
        int appSwitchCount = 0;

        while (usageEvents != null && usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            int eventType = event.getEventType();
            long eventTime = event.getTimeStamp();
            boolean isForeground = eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || eventType == UsageEvents.Event.ACTIVITY_RESUMED;
            boolean isBackground = eventType == UsageEvents.Event.MOVE_TO_BACKGROUND || eventType == UsageEvents.Event.ACTIVITY_PAUSED || eventType == UsageEvents.Event.ACTIVITY_STOPPED;
            String packageName = event.getPackageName();

            if (isForeground) {
                if (currentPackage != null && currentStart >= 0L && eventTime > currentStart) {
                    addDuration(context, appUsageMap, packageManager, currentPackage, eventTime - currentStart);
                }

                if (packageName != null && shouldTrackPackage(context, packageName)) {
                    AppUsageSummary summary = getOrCreateSummary(appUsageMap, packageManager, packageName);
                    summary.incrementLaunchCount();
                    if (lastForegroundPackage != null && !lastForegroundPackage.equals(packageName)) {
                        appSwitchCount++;
                    }
                    lastForegroundPackage = packageName;
                    currentPackage = packageName;
                    currentStart = eventTime;
                } else {
                    currentPackage = null;
                    currentStart = -1L;
                }
                continue;
            }

            if (isBackground && currentPackage != null && currentPackage.equals(packageName) && currentStart >= 0L) {
                addDuration(context, appUsageMap, packageManager, currentPackage, eventTime - currentStart);
                currentPackage = null;
                currentStart = -1L;
            }
        }

        if (currentPackage != null && currentStart >= 0L && safeEnd > currentStart) {
            addDuration(context, appUsageMap, packageManager, currentPackage, safeEnd - currentStart);
        }

        List<AppUsageSummary> summaries = new ArrayList<>(appUsageMap.values());
        summaries.sort(Comparator.comparingLong(AppUsageSummary::getDurationMillis).reversed());
        long breakDuration = Math.min(totalDuration, Math.max(0L, manualBreakDurationMillis));
        long distractionDuration = 0L;
        for (AppUsageSummary summary : summaries) {
            distractionDuration += summary.getDurationMillis();
        }
        distractionDuration = Math.min(Math.max(0L, totalDuration - breakDuration), distractionDuration);
        long effectiveFocusDuration = Math.max(0L, totalDuration - breakDuration - distractionDuration);
        int focusScore = focusScoreCalculator.calculateFocusScore(totalDuration, effectiveFocusDuration, distractionDuration, appSwitchCount);
        return new FocusAnalysisResult(startTimeMillis, safeEnd, totalDuration, Math.max(0L, totalDuration - breakDuration), breakDuration, 0L, distractionDuration, 0L, effectiveFocusDuration, focusScore, appSwitchCount, summaries);
    }

    private void addDuration(Context context, Map<String, AppUsageSummary> appUsageMap, PackageManager packageManager, String packageName, long duration) {
        if (!shouldTrackPackage(context, packageName)) {
            return;
        }
        AppUsageSummary summary = getOrCreateSummary(appUsageMap, packageManager, packageName);
        summary.addDuration(duration);
    }

    private AppUsageSummary getOrCreateSummary(Map<String, AppUsageSummary> appUsageMap, PackageManager packageManager, String packageName) {
        AppUsageSummary summary = appUsageMap.get(packageName);
        if (summary != null) {
            return summary;
        }
        String label = resolveAppLabel(packageManager, packageName);
        AppUsageSummary newSummary = new AppUsageSummary(packageName, label, FocusCategory.DISTRACTION);
        appUsageMap.put(packageName, newSummary);
        return newSummary;
    }

    private boolean shouldTrackPackage(Context context, String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        if (packageName.equals(context.getPackageName())) {
            return false;
        }
        if (packageName.startsWith("com.android.systemui") || packageName.startsWith("com.google.android.permissioncontroller")) {
            return false;
        }
        return !packageName.contains("launcher");
    }

    private String resolveAppLabel(PackageManager packageManager, String packageName) {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            CharSequence label = packageManager.getApplicationLabel(applicationInfo);
            if (label != null) {
                return label.toString();
            }
        } catch (Exception ignored) {
        }
        return packageName;
    }
}
