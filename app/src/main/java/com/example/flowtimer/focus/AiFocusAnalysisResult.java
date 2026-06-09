package com.example.flowtimer.focus;

import java.util.List;

public class AiFocusAnalysisResult {
    private final String summary;
    private final List<String> strengths;
    private final List<String> improvements;
    private final String generatedAt;

    public AiFocusAnalysisResult(String summary, List<String> strengths, List<String> improvements, String generatedAt) {
        this.summary = summary;
        this.strengths = strengths;
        this.improvements = improvements;
        this.generatedAt = generatedAt;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public List<String> getImprovements() {
        return improvements;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }
}
