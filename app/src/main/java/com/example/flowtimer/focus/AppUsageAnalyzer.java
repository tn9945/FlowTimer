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

    private final FocusClassifier focusClassifier = new FocusClassifier();
    private final FocusScoreCalculator focusScoreCalculator = new FocusScoreCalculator();

    public FocusAnalysisResult analyze(Context context, long startTimeMillis, long endTimeMillis) {
        long safeEnd = Math.max(endTimeMillis, startTimeMillis);
        long totalDuration = Math.max(0L, safeEnd - startTimeMillis);
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return new FocusAnalysisResult(startTimeMillis, safeEnd, totalDuration, totalDuration, 0L, 0L, 0L, 0L, totalDuration, 0, 0, new ArrayList<>());
        }

        UsageEvents usageEvents = usageStatsManager.queryEvents(startTimeMillis, safeEnd);
        UsageEvents.Event event = new UsageEvents.Event();
        Map<String, AppUsageSummary> appUsageMap = new HashMap<>();
        PackageManager packageManager = context.getPackageManager();

        String currentPackage = null;
        long currentStart = -1L;
        String lastForegroundPackage = null;
        long breakStart = -1L;
        long breakDuration = 0L;
        int appSwitchCount = 0;

        while (usageEvents != null && usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            int eventType = event.getEventType();
            long eventTime = event.getTimeStamp();

            if (eventType == UsageEvents.Event.SCREEN_NON_INTERACTIVE || eventType == UsageEvents.Event.KEYGUARD_SHOWN) {
                if (breakStart < 0L) {
                    breakStart = eventTime;
                }
                continue;
            }

            if (eventType == UsageEvents.Event.SCREEN_INTERACTIVE || eventType == UsageEvents.Event.KEYGUARD_HIDDEN) {
                if (breakStart >= 0L) {
                    breakDuration += Math.max(0L, eventTime - breakStart);
                    breakStart = -1L;
                }
                continue;
            }

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

        if (breakStart >= 0L) {
            breakDuration += Math.max(0L, safeEnd - breakStart);
        }

        List<AppUsageSummary> summaries = new ArrayList<>(appUsageMap.values());
        summaries.sort(Comparator.comparingLong(AppUsageSummary::getDurationMillis).reversed());
        long activeDuration = Math.max(0L, totalDuration - breakDuration);
        long studyDuration = 0L;
        long distractionDuration = 0L;
        long neutralDuration = 0L;
        for (AppUsageSummary summary : summaries) {
            if (FocusCategory.STUDY.equals(summary.getCategory())) {
                studyDuration += summary.getDurationMillis();
            } else if (FocusCategory.DISTRACTION.equals(summary.getCategory())) {
                distractionDuration += summary.getDurationMillis();
            } else {
                neutralDuration += summary.getDurationMillis();
            }
        }
        long effectiveFocusDuration = focusScoreCalculator.calculateEffectiveFocusDuration(activeDuration, studyDuration, neutralDuration, distractionDuration);
        int focusScore = focusScoreCalculator.calculateFocusScore(totalDuration, activeDuration, studyDuration, distractionDuration, appSwitchCount);
        return new FocusAnalysisResult(startTimeMillis, safeEnd, totalDuration, activeDuration, Math.min(breakDuration, totalDuration), studyDuration, distractionDuration, neutralDuration, effectiveFocusDuration, focusScore, appSwitchCount, summaries);
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
        String category = focusClassifier.classify(packageName, label);
        AppUsageSummary newSummary = new AppUsageSummary(packageName, label, category);
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
