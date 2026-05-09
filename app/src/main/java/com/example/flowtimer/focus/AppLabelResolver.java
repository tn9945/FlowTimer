package com.example.flowtimer.focus;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public final class AppLabelResolver {

    private AppLabelResolver() {
    }

    public static String resolve(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return "알 수 없는 앱";
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            CharSequence label = packageManager.getApplicationLabel(applicationInfo);
            if (label != null && label.length() > 0) {
                return label.toString();
            }
        } catch (Exception ignored) {
        }
        int lastDotIndex = packageName.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < packageName.length() - 1) {
            return packageName.substring(lastDotIndex + 1);
        }
        return packageName;
    }
}
