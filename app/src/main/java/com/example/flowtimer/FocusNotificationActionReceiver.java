package com.example.flowtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.flowtimer.focus.ActiveFocusSessionStore;

public class FocusNotificationActionReceiver extends BroadcastReceiver {

    public static final String ACTION_PAUSE_CONSCIENCE = "com.example.flowtimer.action.PAUSE_CONSCIENCE";
    public static final String ACTION_RESUME_CONSCIENCE = "com.example.flowtimer.action.RESUME_CONSCIENCE";
    public static final String ACTION_STOP_CONSCIENCE = "com.example.flowtimer.action.STOP_CONSCIENCE";
    public static final String ACTION_STOP_STRICT = "com.example.flowtimer.action.STOP_STRICT";

    private static final String STRICT_SESSION_PREF_NAME = "strict_focus_session";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (ACTION_PAUSE_CONSCIENCE.equals(action)) {
            new ActiveFocusSessionStore(context).pause();
            restartConscienceService(context);
            return;
        }
        if (ACTION_RESUME_CONSCIENCE.equals(action)) {
            new ActiveFocusSessionStore(context).resume();
            restartConscienceService(context);
            return;
        }
        if (ACTION_STOP_CONSCIENCE.equals(action)) {
            openMainForStop(context);
            return;
        }
        if (ACTION_STOP_STRICT.equals(action)) {
            context.getSharedPreferences(STRICT_SESSION_PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
            context.stopService(new Intent(context, FocusGuardService.class));
            openMain(context);
        }
    }

    private void restartConscienceService(Context context) {
        Intent serviceIntent = new Intent(context, ConscienceFocusService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private void openMainForStop(Context context) {
        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.putExtra(MainActivity.EXTRA_STOP_CONSCIENCE_FOCUS, true);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(activityIntent);
    }

    private void openMain(Context context) {
        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(activityIntent);
    }
}
