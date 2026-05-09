package com.example.flowtimer.focus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DurationFormatter {

    private static final Locale LOCALE = Locale.KOREA;

    private DurationFormatter() {
    }

    public static String formatClock(long elapsedMillis) {
        long totalSeconds = Math.max(0L, elapsedMillis / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0L) {
            return String.format(LOCALE, "%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(LOCALE, "%02d:%02d", minutes, seconds);
    }

    public static String formatDuration(long elapsedMillis) {
        long totalSeconds = Math.max(0L, elapsedMillis / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        return String.format(LOCALE, "%02d시간 %02d분 %02d초", hours, minutes, seconds);
    }

    public static String formatShortDuration(long elapsedMillis) {
        long totalSeconds = Math.max(0L, elapsedMillis / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0L) {
            return String.format(LOCALE, "%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(LOCALE, "%02d:%02d", minutes, seconds);
    }

    public static String formatDateTime(long timeMillis) {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", LOCALE).format(new Date(timeMillis));
    }

    public static String formatDate(long timeMillis) {
        return new SimpleDateFormat("yyyy.MM.dd", LOCALE).format(new Date(timeMillis));
    }

    public static String formatHour(long timeMillis) {
        return new SimpleDateFormat("HH시", LOCALE).format(new Date(timeMillis));
    }

    public static String formatMonth(long timeMillis) {
        return new SimpleDateFormat("yyyy.MM", LOCALE).format(new Date(timeMillis));
    }

    public static String formatWeek(long timeMillis) {
        Calendar calendar = Calendar.getInstance(LOCALE);
        calendar.setTimeInMillis(timeMillis);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        return String.format(LOCALE, "%d년 %02d주차", year, week);
    }

    public static String formatScore(float score) {
        return String.format(LOCALE, "%.1f점", score);
    }
}
