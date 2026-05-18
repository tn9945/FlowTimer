package com.example.flowtimer.focus;

public class FocusScoreCalculator {

    public long calculateEffectiveFocusDuration(long activeDurationMillis,
                                                long studyDurationMillis,
                                                long neutralDurationMillis,
                                                long distractionDurationMillis) {
        return Math.max(0L, activeDurationMillis - distractionDurationMillis);
    }

    public int calculateFocusScore(long totalDurationMillis,
                                   long focusDurationMillis,
                                   long distractionDurationMillis,
                                   int appSwitchCount) {
        if (totalDurationMillis <= 0L) {
            return 0;
        }
        double focusRatio = (double) Math.max(0L, focusDurationMillis) / (double) totalDurationMillis;
        double distractionRatio = (double) Math.max(0L, distractionDurationMillis) / (double) totalDurationMillis;
        double switchPenalty = Math.min(10.0, appSwitchCount * 0.8);
        int score = (int) Math.round((focusRatio * 100.0) - (distractionRatio * 20.0) - switchPenalty);
        if (score < 0) {
            return 0;
        }
        return Math.min(score, 100);
    }

    public int calculateFocusScore(long totalDurationMillis,
                                   long activeDurationMillis,
                                   long studyDurationMillis,
                                   long distractionDurationMillis,
                                   int appSwitchCount) {
        long focusDurationMillis = Math.max(0L, activeDurationMillis - distractionDurationMillis);
        return calculateFocusScore(totalDurationMillis, focusDurationMillis, distractionDurationMillis, appSwitchCount);
    }
}
