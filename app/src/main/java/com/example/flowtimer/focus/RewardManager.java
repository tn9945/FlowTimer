package com.example.flowtimer.focus;

public class RewardManager {

    public RewardResult calculate(FocusAnalysisResult result) {
        int effectiveMinutes = (int) (result.getEffectiveFocusDurationMillis() / 60000L);
        int coin = Math.max(0, (effectiveMinutes / 5) + (result.getFocusScore() / 20));
        int exp = Math.max(0, (effectiveMinutes / 3) + (result.getFocusScore() / 12));
        int time = Math.max(0, effectiveMinutes);
        if (result.getDistractionDurationMillis() > result.getActiveDurationMillis() / 2L) {
            coin /= 2;
            exp /= 2;
        }
        return new RewardResult(coin, exp, time);
    }
}
