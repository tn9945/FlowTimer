package com.example.flowtimer.focus;

public class RewardResult {

    private final int coin;
    private final int exp;
    private final int timeMinutes;

    public RewardResult(int coin, int exp, int timeMinutes) {
        this.coin = coin;
        this.exp = exp;
        this.timeMinutes = timeMinutes;
    }

    public int getCoin() {
        return coin;
    }

    public int getExp() {
        return exp;
    }

    public int getTimeMinutes() {
        return timeMinutes;
    }
}
