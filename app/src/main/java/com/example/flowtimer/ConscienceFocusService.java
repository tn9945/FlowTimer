package com.example.flowtimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.flowtimer.focus.ActiveFocusSessionStore;
import com.example.flowtimer.focus.DurationFormatter;

public class ConscienceFocusService extends Service {

    private static final String CHANNEL_ID = "conscience_focus_timer_channel";
    private static final int NOTIFICATION_ID = 2601;

    private ActiveFocusSessionStore activeFocusSessionStore;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateNotification();
            handler.postDelayed(this, 1000L);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        activeFocusSessionStore = new ActiveFocusSessionStore(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!activeFocusSessionStore.isRunning()) {
            stopSelf();
            return START_NOT_STICKY;
        }
        startForeground(NOTIFICATION_ID, createNotification());
        handler.removeCallbacks(timerRunnable);
        handler.post(timerRunnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(timerRunnable);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateNotification() {
        if (!activeFocusSessionStore.isRunning()) {
            stopSelf();
            return;
        }
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, createNotification());
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        boolean paused = activeFocusSessionStore.isPaused();
        String timeText = activeFocusSessionStore.isTimerMode()
                ? "남은 시간 " + DurationFormatter.formatClock(activeFocusSessionStore.getRemainDurationMillis())
                : "집중 시간 " + DurationFormatter.formatClock(activeFocusSessionStore.getElapsedDurationMillis());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.flow_timer_app_icon_transparent)
                .setContentTitle(paused ? "양심 집중 모드 일시정지 중" : "양심 집중 모드 실행 중")
                .setContentText(timeText)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        builder.addAction(0, paused ? "재개" : "일시정지", createActionPendingIntent(paused ? FocusNotificationActionReceiver.ACTION_RESUME_CONSCIENCE : FocusNotificationActionReceiver.ACTION_PAUSE_CONSCIENCE, 2001));
        builder.addAction(0, "집중 종료", createActionPendingIntent(FocusNotificationActionReceiver.ACTION_STOP_CONSCIENCE, 2002));
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
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "양심 집중 타이머", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("양심 집중 모드의 진행 시간을 표시합니다.");
        channel.setSound(null, null);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}
