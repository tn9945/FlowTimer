package com.example.flowtimer.focus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FocusAnalysisResult {

    private final long startTimeMillis;
    private final long endTimeMillis;
    private final long totalDurationMillis;
    private final long activeDurationMillis;
    private final long breakDurationMillis;
    private final long studyDurationMillis;
    private final long distractionDurationMillis;
    private final long neutralDurationMillis;
    private final long effectiveFocusDurationMillis;
    private final int focusScore;
    private final int appSwitchCount;
    private final List<AppUsageSummary> appUsageSummaries;

    public FocusAnalysisResult(long startTimeMillis,
                               long endTimeMillis,
                               long totalDurationMillis,
                               long activeDurationMillis,
                               long breakDurationMillis,
                               long studyDurationMillis,
                               long distractionDurationMillis,
                               long neutralDurationMillis,
                               long effectiveFocusDurationMillis,
                               int focusScore,
                               int appSwitchCount,
                               List<AppUsageSummary> appUsageSummaries) {
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
        this.totalDurationMillis = totalDurationMillis;
        this.activeDurationMillis = activeDurationMillis;
        this.breakDurationMillis = breakDurationMillis;
        this.studyDurationMillis = studyDurationMillis;
        this.distractionDurationMillis = distractionDurationMillis;
        this.neutralDurationMillis = neutralDurationMillis;
        this.effectiveFocusDurationMillis = effectiveFocusDurationMillis;
        this.focusScore = focusScore;
        this.appSwitchCount = appSwitchCount;
        this.appUsageSummaries = new ArrayList<>(appUsageSummaries);
    }

    public long getStartTimeMillis() { return startTimeMillis; }
    public long getEndTimeMillis() { return endTimeMillis; }
    public long getTotalDurationMillis() { return totalDurationMillis; }
    public long getActiveDurationMillis() { return activeDurationMillis; }
    public long getBreakDurationMillis() { return breakDurationMillis; }
    public long getStudyDurationMillis() { return studyDurationMillis; }
    public long getDistractionDurationMillis() { return distractionDurationMillis; }
    public long getNeutralDurationMillis() { return neutralDurationMillis; }
    public long getEffectiveFocusDurationMillis() { return effectiveFocusDurationMillis; }
    public int getFocusScore() { return focusScore; }
    public int getAppSwitchCount() { return appSwitchCount; }
    public List<AppUsageSummary> getAppUsageSummaries() { return Collections.unmodifiableList(appUsageSummaries); }

    public String getTopAppName() {
        if (appUsageSummaries.isEmpty()) {
            return "기록 없음";
        }
        return appUsageSummaries.get(0).getAppName();
    }

    public long getTopAppDurationMillis() {
        if (appUsageSummaries.isEmpty()) {
            return 0L;
        }
        return appUsageSummaries.get(0).getDurationMillis();
    }
}
