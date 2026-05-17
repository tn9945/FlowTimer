package com.example.flowtimer;

import android.app.AlertDialog;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.focus.DurationFormatter;
import com.example.flowtimer.focus.StrictFocusPackagePolicy;
import com.example.flowtimer.focus.StrictFocusSessionStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StrictFocusActivity extends AppCompatActivity {

    private static final String PREF_NAME = "strict_focus_session";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_RUNNING = "running";
    private static final String KEY_MODE_TYPE = "mode_type";

    private TextView tvStrictModeTitle;
    private TextView tvStrictTimer;
    private TextView tvStrictTarget;
    private TextView tvStrictBlocked;
    private TextView tvStrictGuide;
    private Button btnOpenAllowedApps;
    private Button btnFinishStrictFocus;
    private StrictFocusSessionStore strictFocusSessionStore;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Handler returnHandler = new Handler(Looper.getMainLooper());
    private long startTimeMillis;
    private String strictModeType = FocusModeSelectActivity.STRICT_MODE_ALLOWED_APPS;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimer();
            checkForegroundApp();
            timerHandler.postDelayed(this, 1000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_strict_focus);

        strictFocusSessionStore = new StrictFocusSessionStore(this);
        bindViews();
        prepareBackBlock();
        restoreOrStartSession();
        bindModeUi();
        startFocusGuardService();
        bindActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.post(timerRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
        scheduleReturnAttempts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        returnHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (isStrictFocusRunning()) {
            scheduleReturnAttempts();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus && isStrictFocusRunning()) {
            scheduleReturnAttempts();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isStrictFocusRunning() && !isFinishing()) {
            String currentPackage = getForegroundPackageName();
            if (currentPackage != null && !isAllowedPackage(currentPackage)) {
                scheduleReturnAttempts();
            }
        }
    }

    private void bindViews() {
        tvStrictModeTitle = findViewById(R.id.tvStrictModeTitle);
        tvStrictTimer = findViewById(R.id.tvStrictTimer);
        tvStrictTarget = findViewById(R.id.tvStrictTarget);
        tvStrictBlocked = findViewById(R.id.tvStrictBlocked);
        tvStrictGuide = findViewById(R.id.tvStrictGuide);
        btnOpenAllowedApps = findViewById(R.id.btnOpenAllowedApps);
        btnFinishStrictFocus = findViewById(R.id.btnFinishStrictFocus);
    }

    private void prepareBackBlock() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });
    }

    private void restoreOrStartSession() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean running = preferences.getBoolean(KEY_RUNNING, false);
        long savedStartTime = preferences.getLong(KEY_START_TIME, 0L);
        String requestedMode = getIntent().getStringExtra(FocusModeSelectActivity.EXTRA_STRICT_MODE_TYPE);
        strictModeType = requestedMode != null ? requestedMode : preferences.getString(KEY_MODE_TYPE, FocusModeSelectActivity.STRICT_MODE_ALLOWED_APPS);
        if (running && savedStartTime > 0L) {
            startTimeMillis = savedStartTime;
        } else if (strictFocusSessionStore.isRunning() && strictFocusSessionStore.getStartTimeMillis() > 0L) {
            startTimeMillis = strictFocusSessionStore.getStartTimeMillis();
            preferences.edit()
                    .putBoolean(KEY_RUNNING, true)
                    .putLong(KEY_START_TIME, startTimeMillis)
                    .putString(KEY_MODE_TYPE, strictModeType)
                    .apply();
        } else {
            startTimeMillis = System.currentTimeMillis();
            strictFocusSessionStore.start(startTimeMillis, 25L * 60L * 1000L);
            preferences.edit()
                    .putBoolean(KEY_RUNNING, true)
                    .putLong(KEY_START_TIME, startTimeMillis)
                    .putString(KEY_MODE_TYPE, strictModeType)
                    .apply();
        }
    }

    private void bindModeUi() {
        tvStrictModeTitle.setText("강제 집중 모드");
        tvStrictGuide.setText("선택한 앱만 예외적으로 사용할 수 있습니다.");
        btnOpenAllowedApps.setText("허용 앱 열기");
    }

    private void startFocusGuardService() {
        Intent intent = new Intent(this, FocusGuardService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void stopFocusGuardService() {
        stopService(new Intent(this, FocusGuardService.class));
    }

    private void bindActions() {
        btnOpenAllowedApps.setOnClickListener(v -> showAllowedAppLauncherDialog());
        btnFinishStrictFocus.setOnClickListener(v -> finishStrictFocus("강제 집중 모드가 종료되었습니다."));
    }

    private void updateTimer() {
        long elapsed = strictFocusSessionStore.getElapsedMillis();
        tvStrictTimer.setText(DurationFormatter.formatClock(elapsed));
        tvStrictTarget.setText("목표 " + DurationFormatter.formatShortDuration(strictFocusSessionStore.getTargetDurationMillis())
                + " · 남은 시간 " + DurationFormatter.formatShortDuration(strictFocusSessionStore.getRemainMillis()));
        tvStrictBlocked.setText("차단된 앱 실행 시도 " + strictFocusSessionStore.getBlockedTotalCount() + "회");
        if (strictFocusSessionStore.isTargetReached()) {
            tvStrictTarget.setText("목표 시간을 달성하였습니다. 계속 집중할 수 있습니다.");
        }
    }

    private void showAllowedAppLauncherDialog() {
        List<String> launchablePackages = getLaunchableAllowedPackages();
        if (launchablePackages.isEmpty()) {
            Toast.makeText(this, "실행 가능한 허용 앱이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        PackageManager packageManager = getPackageManager();
        String[] appNames = new String[launchablePackages.size()];
        for (int i = 0; i < launchablePackages.size(); i++) {
            appNames[i] = getAppName(packageManager, launchablePackages.get(i));
        }
        new AlertDialog.Builder(this)
                .setTitle("허용 앱 열기")
                .setItems(appNames, (dialog, which) -> openAllowedApp(launchablePackages.get(which)))
                .setNegativeButton("취소", null)
                .show();
    }

    private List<String> getLaunchableAllowedPackages() {
        Set<String> allowedPackages = getSharedPreferences(AllowedAppsActivity.PREF_NAME, MODE_PRIVATE)
                .getStringSet(AllowedAppsActivity.KEY_ALLOWED_PACKAGES, new HashSet<>());
        PackageManager packageManager = getPackageManager();
        List<String> result = new ArrayList<>();
        for (String packageName : allowedPackages) {
            if (packageName.equals(getPackageName()) || StrictFocusPackagePolicy.isAllowedPackage(this, packageName) && packageManager.getLaunchIntentForPackage(packageName) == null) {
                continue;
            }
            if (packageManager.getLaunchIntentForPackage(packageName) != null) {
                result.add(packageName);
            }
        }
        return result;
    }

    private void openAllowedApp(String packageName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null) {
            Toast.makeText(this, "선택한 앱을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(launchIntent);
    }

    private String getAppName(PackageManager packageManager, String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    private void checkForegroundApp() {
        String packageName = getForegroundPackageName();
        if (packageName == null || isAllowedPackage(packageName)) {
            return;
        }
        recordAndOpenBlockedScreen(packageName);
    }

    private void scheduleReturnAttempts() {
        returnHandler.postDelayed(this::blockOnlyDisallowedForegroundApp, 120L);
        returnHandler.postDelayed(this::blockOnlyDisallowedForegroundApp, 350L);
        returnHandler.postDelayed(this::blockOnlyDisallowedForegroundApp, 800L);
    }

    private void blockOnlyDisallowedForegroundApp() {
        if (!isStrictFocusRunning()) {
            return;
        }
        String currentPackage = getForegroundPackageName();
        if (currentPackage == null || isAllowedPackage(currentPackage)) {
            return;
        }
        recordAndOpenBlockedScreen(currentPackage);
    }

    private void recordAndOpenBlockedScreen(String packageName) {
        String appName = resolveAppName(packageName);
        strictFocusSessionStore.addBlockedApp(packageName, appName);
        openBlockedScreen(appName);
    }

    private void openBlockedScreen(String appName) {
        Intent intent = new Intent(this, BlockedAppActivity.class);
        intent.putExtra(BlockedAppActivity.EXTRA_BLOCKED_APP_NAME, appName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private String resolveAppName(String packageName) {
        if (packageName == null) {
            return "이 앱";
        }
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(packageName, 0);
            return getPackageManager().getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "이 앱";
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

    private boolean isAllowedPackage(String packageName) {
        return StrictFocusPackagePolicy.isAllowedPackage(this, packageName);
    }

    private boolean isStrictFocusRunning() {
        return strictFocusSessionStore.isRunning() || getSharedPreferences(PREF_NAME, MODE_PRIVATE).getBoolean(KEY_RUNNING, false);
    }

    private void finishStrictFocus(String message) {
        strictFocusSessionStore.finishAndSaveSummary();
        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().clear().apply();
        stopFocusGuardService();
        timerHandler.removeCallbacks(timerRunnable);
        returnHandler.removeCallbacksAndMessages(null);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, StrictFocusResultActivity.class);
        startActivity(intent);
        finish();
    }
}
