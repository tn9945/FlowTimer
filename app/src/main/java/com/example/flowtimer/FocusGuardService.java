package com.example.flowtimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.flowtimer.focus.DurationFormatter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FocusGuardService extends Service {

    private static final String CHANNEL_ID = "strict_focus_guard_channel";
    private static final int NOTIFICATION_ID = 2501;
    private static final String STRICT_SESSION_PREF_NAME = "strict_focus_session";
    private static final String KEY_RUNNING = "running";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_ESCAPE_COUNT = "escape_count";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable guardRunnable = new Runnable() {
        @Override
        public void run() {
            guardStrictFocusScreen();
            updateNotification();
            handler.postDelayed(this, 700L);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        handler.removeCallbacks(guardRunnable);
        handler.post(guardRunnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(guardRunnable);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void guardStrictFocusScreen() {
        if (!isStrictFocusRunning()) {
            stopSelf();
            return;
        }
        String packageName = getForegroundPackageName();
        if (packageName == null || isAllowedPackage(packageName)) {
            return;
        }
        increaseEscapeCount();
        openBlockedScreen(packageName);
    }

    private void openBlockedScreen(String packageName) {
        Intent intent = new Intent(this, BlockedAppActivity.class);
        intent.putExtra(BlockedAppActivity.EXTRA_BLOCKED_APP_NAME, getAppName(packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private String getAppName(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "이 앱";
        }
    }

    private void updateNotification() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, createNotification());
        }
    }

    private boolean isStrictFocusRunning() {
        return getSharedPreferences(STRICT_SESSION_PREF_NAME, MODE_PRIVATE).getBoolean(KEY_RUNNING, false);
    }

    private String getForegroundPackageName() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return null;
        }
        long endTime = System.currentTimeMillis();
        long startTime = endTime - 10000L;
        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);
        UsageEvents.Event event = new UsageEvents.Event();
        String packageName = null;
        long lastEventTime = 0L;
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND && event.getTimeStamp() >= lastEventTime) {
                packageName = event.getPackageName();
                lastEventTime = event.getTimeStamp();
            }
        }
        return packageName;
    }

    private boolean isAllowedPackage(String packageName) {
        if (packageName == null) {
            return false;
        }
        if (packageName.equals(getPackageName())) {
            return true;
        }
        if (isHomeLauncherPackage(packageName)) {
            return true;
        }
        Set<String> allowedPackages = getSharedPreferences(AllowedAppsActivity.PREF_NAME, MODE_PRIVATE)
                .getStringSet(AllowedAppsActivity.KEY_ALLOWED_PACKAGES, new HashSet<>());
        return allowedPackages.contains(packageName);
    }

    private boolean isHomeLauncherPackage(String packageName) {
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<android.content.pm.ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (android.content.pm.ResolveInfo resolveInfo : resolveInfos) {
            if (resolveInfo.activityInfo != null && packageName.equals(resolveInfo.activityInfo.packageName)) {
                return true;
            }
        }
        android.content.pm.ResolveInfo defaultHome = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return defaultHome != null && defaultHome.activityInfo != null && packageName.equals(defaultHome.activityInfo.packageName);
    }

    private void increaseEscapeCount() {
        SharedPreferences preferences = getSharedPreferences(STRICT_SESSION_PREF_NAME, MODE_PRIVATE);
        int count = preferences.getInt(KEY_ESCAPE_COUNT, 0);
        preferences.edit().putInt(KEY_ESCAPE_COUNT, count + 1).apply();
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, StrictFocusActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long startTimeMillis = getSharedPreferences(STRICT_SESSION_PREF_NAME, MODE_PRIVATE).getLong(KEY_START_TIME, 0L);
        long elapsed = startTimeMillis > 0L ? Math.max(0L, System.currentTimeMillis() - startTimeMillis) : 0L;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.flow_timer_app_icon_transparent)
                .setContentTitle("강제 집중 모드 실행 중")
                .setContentText("집중 시간 " + DurationFormatter.formatClock(elapsed))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.addAction(0, "집중 종료", createActionPendingIntent(FocusNotificationActionReceiver.ACTION_STOP_STRICT, 2502));
        return builder.build();
    }

    private PendingIntent createActionPendingIntent(String action, int requestCode) {
        Intent intent = new Intent(this, FocusNotificationActionReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "강제 집중 타이머", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("강제 집중 모드의 진행 시간을 표시합니다.");
        channel.setSound(null, null);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}
