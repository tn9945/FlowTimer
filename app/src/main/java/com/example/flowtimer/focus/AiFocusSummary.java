package com.example.flowtimer.focus;

public class AiFocusSummary {

    private final String summaryText;
    private final String warningText;
    private final String adviceText;

    public AiFocusSummary(String summaryText, String warningText, String adviceText) {
        this.summaryText = summaryText;
        this.warningText = warningText;
        this.adviceText = adviceText;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public String getWarningText() {
        return warningText;
    }

    public String getAdviceText() {
        return adviceText;
    }
}
