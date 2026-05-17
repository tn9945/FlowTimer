package com.example.flowtimer.focus;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import com.example.flowtimer.StrictFocusAccessibilityService;

import java.util.List;

public final class FocusPermissionHelper {

    private FocusPermissionHelper() {
    }

    public static boolean hasUsageAccess(Context context) {
        return UsageAccessHelper.hasUsageAccess(context);
    }

    public static boolean hasAccessibilityAccess(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager == null) {
            return false;
        }
        List<AccessibilityServiceInfo> services = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        String targetServiceName = StrictFocusAccessibilityService.class.getName();
        for (AccessibilityServiceInfo serviceInfo : services) {
            if (serviceInfo.getResolveInfo() != null && serviceInfo.getResolveInfo().serviceInfo != null) {
                String serviceName = serviceInfo.getResolveInfo().serviceInfo.name;
                if (targetServiceName.equals(serviceName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void openUsageAccessSettings(Context context) {
        UsageAccessHelper.openSettings(context);
    }

    public static void openAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
