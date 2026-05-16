package com.example.flowtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {

    private static final String STRICT_SESSION_PREF_NAME = "strict_focus_session";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_RUNNING = "running";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        if (!isStrictFocusRecoverable(context)) {
            clearStrictFocusSession(context);
            return;
        }
        startFocusGuardService(context);
        openStrictFocusScreen(context);
    }

    private boolean isStrictFocusRecoverable(Context context) {
        boolean running = context.getSharedPreferences(STRICT_SESSION_PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_RUNNING, false);
        long startTimeMillis = context.getSharedPreferences(STRICT_SESSION_PREF_NAME, Context.MODE_PRIVATE).getLong(KEY_START_TIME, 0L);
        return running && startTimeMillis > 0L;
    }

    private void clearStrictFocusSession(Context context) {
        context.getSharedPreferences(STRICT_SESSION_PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }

    private void startFocusGuardService(Context context) {
        Intent serviceIntent = new Intent(context, FocusGuardService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private void openStrictFocusScreen(Context context) {
        Intent activityIntent = new Intent(context, StrictFocusActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(activityIntent);
    }
}
