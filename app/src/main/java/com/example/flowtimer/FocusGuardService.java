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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.HashSet;
import java.util.Set;

public class FocusGuardService extends Service {

    private static final String CHANNEL_ID = "strict_focus_guard_channel";
    private static final int NOTIFICATION_ID = 2501;
    private static final String STRICT_SESSION_PREF_NAME = "strict_focus_session";
    private static final String KEY_RUNNING = "running";
    private static final String KEY_ESCAPE_COUNT = "escape_count";
    private static final String KEY_MODE_TYPE = "mode_type";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable guardRunnable = new Runnable() {
        @Override
        public void run() {
            guardStrictFocusScreen();
            handler.postDelayed(this, 1000L);
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
        Intent intent = new Intent(this, StrictFocusActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
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
        String modeType = getSharedPreferences(STRICT_SESSION_PREF_NAME, MODE_PRIVATE)
                .getString(KEY_MODE_TYPE, FocusModeSelectActivity.STRICT_MODE_FULL_BLOCK);
        if (!FocusModeSelectActivity.STRICT_MODE_ALLOWED_APPS.equals(modeType)) {
            return false;
        }
        Set<String> allowedPackages = getSharedPreferences(AllowedAppsActivity.PREF_NAME, MODE_PRIVATE)
                .getStringSet(AllowedAppsActivity.KEY_ALLOWED_PACKAGES, new HashSet<>());
        return allowedPackages.contains(packageName);
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

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.flow_timer_app_icon_transparent)
                .setContentTitle("상호작용 금지 모드 실행 중")
                .setContentText("집중 중에는 타이머 화면을 유지합니다.")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "상호작용 금지 모드", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("상호작용 금지 집중 상태를 유지하는 알림입니다.");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}
