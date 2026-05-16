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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.focus.DurationFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StrictFocusActivity extends AppCompatActivity {

    private static final long STRICT_FOCUS_DURATION_MILLIS = 25L * 60L * 1000L;
    private static final String PREF_NAME = "strict_focus_session";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_RUNNING = "running";
    private static final String KEY_ESCAPE_COUNT = "escape_count";
    private static final String KEY_MODE_TYPE = "mode_type";

    private TextView tvStrictModeTitle;
    private TextView tvStrictTimer;
    private TextView tvStrictGuide;
    private TextView tvStrictStatus;
    private Button btnOpenAllowedApps;
    private Button btnFinishStrictFocus;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Handler returnHandler = new Handler(Looper.getMainLooper());
    private long startTimeMillis;
    private String strictModeType = FocusModeSelectActivity.STRICT_MODE_FULL_BLOCK;

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
            increaseEscapeCount();
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
            if (currentPackage == null || !isAllowedPackage(currentPackage)) {
                increaseEscapeCount();
                scheduleReturnAttempts();
            }
        }
    }

    private void bindViews() {
        tvStrictModeTitle = findViewById(R.id.tvStrictModeTitle);
        tvStrictTimer = findViewById(R.id.tvStrictTimer);
        tvStrictGuide = findViewById(R.id.tvStrictGuide);
        tvStrictStatus = findViewById(R.id.tvStrictStatus);
        btnOpenAllowedApps = findViewById(R.id.btnOpenAllowedApps);
        btnFinishStrictFocus = findViewById(R.id.btnFinishStrictFocus);
    }

    private void prepareBackBlock() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(StrictFocusActivity.this, "상호작용 금지 모드에서는 뒤로가기를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restoreOrStartSession() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean running = preferences.getBoolean(KEY_RUNNING, false);
        long savedStartTime = preferences.getLong(KEY_START_TIME, 0L);
        String requestedMode = getIntent().getStringExtra(FocusModeSelectActivity.EXTRA_STRICT_MODE_TYPE);
        if (requestedMode != null) {
            strictModeType = requestedMode;
        } else {
            strictModeType = preferences.getString(KEY_MODE_TYPE, FocusModeSelectActivity.STRICT_MODE_FULL_BLOCK);
        }
        if (running && savedStartTime > 0L) {
            startTimeMillis = savedStartTime;
        } else {
            startTimeMillis = System.currentTimeMillis();
            preferences.edit()
                    .putBoolean(KEY_RUNNING, true)
                    .putLong(KEY_START_TIME, startTimeMillis)
                    .putInt(KEY_ESCAPE_COUNT, 0)
                    .putString(KEY_MODE_TYPE, strictModeType)
                    .apply();
        }
    }

    private void bindModeUi() {
        if (isAllowedAppsMode()) {
            tvStrictModeTitle.setText("허용 앱 제한 집중 중");
            tvStrictGuide.setText("허용 앱 열기 버튼으로 선택한 앱만 사용할 수 있습니다.\n\n허용되지 않은 앱으로 이동하면 타이머 화면으로 복귀합니다.");
            btnOpenAllowedApps.setVisibility(View.VISIBLE);
        } else {
            tvStrictModeTitle.setText("완전 차단 집중 중");
            tvStrictGuide.setText("집중 중에는 타이머 화면만 유지합니다.\n\n전화와 메시지를 제외한 화면 이동은 집중 방해 시도로 기록됩니다.");
            btnOpenAllowedApps.setVisibility(View.GONE);
        }
    }

    private boolean isAllowedAppsMode() {
        return FocusModeSelectActivity.STRICT_MODE_ALLOWED_APPS.equals(strictModeType);
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
        btnFinishStrictFocus.setOnClickListener(v -> finishStrictFocus("상호작용 금지 모드 집중이 종료되었습니다."));
    }

    private void updateTimer() {
        long elapsed = System.currentTimeMillis() - startTimeMillis;
        long remain = STRICT_FOCUS_DURATION_MILLIS - elapsed;
        if (remain <= 0L) {
            tvStrictTimer.setText("00:00");
            finishStrictFocus("상호작용 금지 모드 집중 시간이 완료되었습니다.");
            return;
        }
        tvStrictTimer.setText(DurationFormatter.formatClock(remain));
        int escapeCount = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getInt(KEY_ESCAPE_COUNT, 0);
        tvStrictStatus.setText("허용되지 않은 화면 이탈 시도 " + escapeCount + "회가 기록되었습니다.");
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
            if (packageName.equals(getPackageName())) {
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
        increaseEscapeCount();
        forceReturnToStrictScreen();
    }

    private void scheduleReturnAttempts() {
        returnHandler.postDelayed(this::forceReturnToStrictScreen, 120L);
        returnHandler.postDelayed(this::forceReturnToStrictScreen, 350L);
        returnHandler.postDelayed(this::forceReturnToStrictScreen, 800L);
    }

    private void forceReturnToStrictScreen() {
        if (!isStrictFocusRunning()) {
            return;
        }
        String currentPackage = getForegroundPackageName();
        if (currentPackage != null && isAllowedPackage(currentPackage)) {
            return;
        }
        Intent intent = new Intent(this, StrictFocusActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
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
        Set<String> allowedPackages = getSharedPreferences(AllowedAppsActivity.PREF_NAME, MODE_PRIVATE)
                .getStringSet(AllowedAppsActivity.KEY_ALLOWED_PACKAGES, new HashSet<>());
        if (packageName.equals(getPackageName())) {
            return true;
        }
        if (!isAllowedAppsMode()) {
            return false;
        }
        return allowedPackages.contains(packageName);
    }

    private void increaseEscapeCount() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int count = preferences.getInt(KEY_ESCAPE_COUNT, 0);
        preferences.edit().putInt(KEY_ESCAPE_COUNT, count + 1).apply();
    }

    private boolean isStrictFocusRunning() {
        return getSharedPreferences(PREF_NAME, MODE_PRIVATE).getBoolean(KEY_RUNNING, false);
    }

    private void finishStrictFocus(String message) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().clear().apply();
        stopFocusGuardService();
        timerHandler.removeCallbacks(timerRunnable);
        returnHandler.removeCallbacksAndMessages(null);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
