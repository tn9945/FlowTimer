package com.example.flowtimer.focus;

public class FocusScoreCalculator {

    public long calculateEffectiveFocusDuration(long activeDurationMillis,
                                                long studyDurationMillis,
                                                long neutralDurationMillis,
                                                long distractionDurationMillis) {
        long weightedNeutral = neutralDurationMillis / 2L;
        long adjusted = studyDurationMillis + weightedNeutral - (distractionDurationMillis / 3L);
        long upperBound = Math.max(0L, activeDurationMillis);
        return Math.max(0L, Math.min(adjusted, upperBound));
    }

    public int calculateFocusScore(long totalDurationMillis,
                                   long activeDurationMillis,
                                   long studyDurationMillis,
                                   long distractionDurationMillis,
                                   int appSwitchCount) {
        if (totalDurationMillis <= 0L || activeDurationMillis <= 0L) {
            return 0;
        }
        double activeRatio = (double) activeDurationMillis / (double) totalDurationMillis;
        double studyRatio = (double) studyDurationMillis / (double) activeDurationMillis;
        double distractionRatio = (double) distractionDurationMillis / (double) activeDurationMillis;
        double switchPenalty = Math.min(25.0, appSwitchCount * 1.8);
        double rawScore = 25.0 + (activeRatio * 35.0) + (studyRatio * 35.0) - (distractionRatio * 30.0) - switchPenalty;
        int score = (int) Math.round(rawScore);
        if (score < 0) {
            return 0;
        }
        return Math.min(score, 100);
    }
}
