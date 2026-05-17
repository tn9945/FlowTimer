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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.flowtimer.focus.DurationFormatter;
import com.example.flowtimer.focus.StrictFocusPackagePolicy;
import com.example.flowtimer.focus.StrictFocusSessionStore;

public class FocusGuardService extends Service {

    private static final String CHANNEL_ID = "strict_focus_guard_channel";
    private static final int NOTIFICATION_ID = 2501;

    private StrictFocusSessionStore strictFocusSessionStore;
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
        strictFocusSessionStore = new StrictFocusSessionStore(this);
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
        if (!strictFocusSessionStore.isRunning()) {
            stopSelf();
            return;
        }
        String packageName = getForegroundPackageName();
        if (packageName == null || StrictFocusPackagePolicy.isAllowedPackage(this, packageName)) {
            return;
        }
        String appName = getAppName(packageName);
        strictFocusSessionStore.addBlockedApp(packageName, appName);
        openBlockedScreen(appName);
    }

    private void openBlockedScreen(String appName) {
        Intent intent = new Intent(this, BlockedAppActivity.class);
        intent.putExtra(BlockedAppActivity.EXTRA_BLOCKED_APP_NAME, appName);
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

    private Notification createNotification() {
        Intent intent = new Intent(this, StrictFocusActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long elapsed = strictFocusSessionStore.getElapsedMillis();
        long remain = strictFocusSessionStore.getRemainMillis();
        String content = strictFocusSessionStore.isTargetReached()
                ? "목표 시간을 달성하였습니다. 집중 시간 " + DurationFormatter.formatClock(elapsed)
                : "집중 시간 " + DurationFormatter.formatClock(elapsed) + " · 남은 시간 " + DurationFormatter.formatShortDuration(remain);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.flow_timer_app_icon_transparent)
                .setContentTitle("강제 집중 모드 실행 중")
                .setContentText(content)
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
