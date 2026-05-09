package com.example.flowtimer.focus;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public final class AppDisplayHelper {

    private AppDisplayHelper() {
    }

    public static String resolveAppName(Context context, String packageName, String fallbackName) {
        if (packageName != null && !packageName.isEmpty()) {
            try {
                PackageManager packageManager = context.getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                CharSequence label = packageManager.getApplicationLabel(applicationInfo);
                if (label != null) {
                    String text = label.toString().trim();
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (fallbackName != null) {
            String text = fallbackName.trim();
            if (!text.isEmpty() && !text.contains(".")) {
                return text;
            }
        }
        return prettifyPackageName(packageName, fallbackName);
    }

    public static Drawable resolveAppIcon(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageName != null && !packageName.isEmpty()) {
                return packageManager.getApplicationIcon(packageName);
            }
            return packageManager.getDefaultActivityIcon();
        } catch (Exception ignored) {
            return context.getPackageManager().getDefaultActivityIcon();
        }
    }

    private static String prettifyPackageName(String packageName, String fallbackName) {
        String source = packageName;
        if (source == null || source.trim().isEmpty()) {
            source = fallbackName;
        }
        if (source == null || source.trim().isEmpty()) {
            return "알 수 없는 앱";
        }
        String value = source.trim();
        if (value.contains(".")) {
            value = value.substring(value.lastIndexOf('.') + 1);
        }
        if (value.isEmpty()) {
            return "알 수 없는 앱";
        }
        String normalized = value.replace('_', ' ').replace('-', ' ').trim();
        if (normalized.isEmpty()) {
            return "알 수 없는 앱";
        }
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
