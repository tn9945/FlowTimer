package com.example.flowtimer;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;

import com.example.flowtimer.focus.StrictFocusPackagePolicy;

public class StrictFocusAccessibilityService extends AccessibilityService {

    private static final String STRICT_SESSION_PREF_NAME = "strict_focus_session";
    private static final String KEY_RUNNING = "running";
    private long lastBlockedTimeMillis = 0L;
    private String lastBlockedPackageName = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }
        if (!isStrictFocusRunning()) {
            return;
        }
        int eventType = event.getEventType();
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            return;
        }
        String packageName = event.getPackageName().toString();
        if (StrictFocusPackagePolicy.isAllowedPackage(this, packageName)) {
            return;
        }
        blockPackage(packageName);
    }

    @Override
    public void onInterrupt() {
    }

    private boolean isStrictFocusRunning() {
        return getSharedPreferences(STRICT_SESSION_PREF_NAME, MODE_PRIVATE).getBoolean(KEY_RUNNING, false);
    }

    private void blockPackage(String packageName) {
        long now = System.currentTimeMillis();
        if (packageName.equals(lastBlockedPackageName) && now - lastBlockedTimeMillis < 800L) {
            return;
        }
        lastBlockedPackageName = packageName;
        lastBlockedTimeMillis = now;
        performGlobalAction(GLOBAL_ACTION_BACK);
        Intent intent = new Intent(this, BlockedAppActivity.class);
        intent.putExtra(BlockedAppActivity.EXTRA_BLOCKED_APP_NAME, resolveAppName(packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private String resolveAppName(String packageName) {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "이 앱";
        }
    }
}
