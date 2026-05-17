package com.example.flowtimer;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;

import com.example.flowtimer.focus.StrictFocusPackagePolicy;
import com.example.flowtimer.focus.StrictFocusSessionStore;

public class StrictFocusAccessibilityService extends AccessibilityService {

    private long lastBlockedTimeMillis = 0L;
    private String lastBlockedPackageName = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }
        StrictFocusSessionStore store = new StrictFocusSessionStore(this);
        if (!store.isRunning()) {
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
        blockPackage(store, packageName);
    }

    @Override
    public void onInterrupt() {
    }

    private void blockPackage(StrictFocusSessionStore store, String packageName) {
        long now = System.currentTimeMillis();
        if (packageName.equals(lastBlockedPackageName) && now - lastBlockedTimeMillis < 800L) {
            return;
        }
        String appName = resolveAppName(packageName);
        store.addBlockedApp(packageName, appName);
        lastBlockedPackageName = packageName;
        lastBlockedTimeMillis = now;
        performGlobalAction(GLOBAL_ACTION_BACK);
        Intent intent = new Intent(this, BlockedAppActivity.class);
        intent.putExtra(BlockedAppActivity.EXTRA_BLOCKED_APP_NAME, appName);
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
