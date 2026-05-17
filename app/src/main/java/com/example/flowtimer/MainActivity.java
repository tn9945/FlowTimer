package com.example.flowtimer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.flowtimer.focus.ActiveFocusSessionStore;
import com.example.flowtimer.focus.AppUsageAnalyzer;
import com.example.flowtimer.focus.DurationFormatter;
import com.example.flowtimer.focus.FocusAnalysisResult;
import com.example.flowtimer.focus.FocusQuestManager;
import com.example.flowtimer.focus.FocusRepository;
import com.example.flowtimer.focus.RewardManager;
import com.example.flowtimer.focus.RewardResult;
import com.example.flowtimer.focus.UsageAccessHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_STOP_CONSCIENCE_FOCUS = "extra_stop_conscience_focus";

    private SessionManager sessionManager;
    private FocusRepository focusRepository;
    private ActiveFocusSessionStore activeFocusSessionStore;
    private ExecutorService analysisExecutor;
    private final RewardManager rewardManager = new RewardManager();
    private FocusQuestManager focusQuestManager;

    private TextView tvWelcome;
    private TextView tvUserId;
    private TextView tvTimer;
    private TextView tvDailyQuest;
    private TextView tvFocusStreak;
    private Button btnStartFocus;
    private Button btnResetFocus;
    private Button btnStats;
    private Button btnPermissionStatus;
    private Button btnGameStart;
    private Button btnLogout;
    private TextView tvWithdraw;
    private TextView tvDeveloperMode;

    private ActivityResultLauncher<String> notificationPermissionLauncher;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isCurrentUserSessionRunning()) {
                tvTimer.setText(DurationFormatter.formatClock(activeFocusSessionStore.getElapsedDurationMillis()));
                timerHandler.postDelayed(this, 1000L);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            openLoginAndFinish();
            return;
        }

        focusRepository = new FocusRepository(this);
        activeFocusSessionStore = new ActiveFocusSessionStore(this);
        focusQuestManager = new FocusQuestManager(this);
        analysisExecutor = Executors.newSingleThreadExecutor();

        registerPermissionLaunchers();
        bindViews();
        bindUserInfo();
        bindActions();
        requestNotificationPermissionIfNeeded();
        handleFocusModeIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleFocusModeIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncFocusSessionUi();
        bindQuestStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        if (analysisExecutor != null) {
            analysisExecutor.shutdown();
        }
    }

    private void registerPermissionLaunchers() {
        notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                Toast.makeText(this, "알림 권한이 거부되어 집중 타이머 알림이 표시되지 않을 수 있습니다.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserId = findViewById(R.id.tvUserId);
        tvTimer = findViewById(R.id.tvTimer);
        tvDailyQuest = findViewById(R.id.tvDailyQuest);
        tvFocusStreak = findViewById(R.id.tvFocusStreak);
        btnStartFocus = findViewById(R.id.btnStartFocus);
        btnResetFocus = findViewById(R.id.btnResetFocus);
        btnStats = findViewById(R.id.btnStats);
        btnPermissionStatus = findViewById(R.id.btnPermissionStatus);
        btnGameStart = findViewById(R.id.btnGameStart);
        btnLogout = findViewById(R.id.btnLogout);
        tvWithdraw = findViewById(R.id.tvWithdraw);
        tvDeveloperMode = findViewById(R.id.tvDeveloperMode);
    }

    private void bindUserInfo() {
        tvWelcome.setText(sessionManager.getUserName() + "님, 환영합니다.");
        tvUserId.setText("ID: " + sessionManager.getUserIdentifier());
        btnGameStart.setText("Game");
        bindQuestStatus();
    }

    private void bindQuestStatus() {
        if (focusQuestManager == null) {
            return;
        }
        tvDailyQuest.setText(focusQuestManager.getDailyQuestText());
        tvFocusStreak.setText(focusQuestManager.getStreakText() + " · 오늘 차단 " + focusQuestManager.getTodayBlockedCount() + "회");
    }

    private void bindActions() {
        btnStartFocus.setOnClickListener(v -> {
            if (isCurrentUserSessionRunning()) {
                stopFocusSession();
            } else {
                startActivity(new Intent(this, FocusModeSelectActivity.class));
            }
        });
        btnResetFocus.setOnClickListener(v -> togglePauseFocusSession());
        btnStats.setOnClickListener(v -> startActivity(new Intent(this, FocusStatsActivity.class)));
        btnPermissionStatus.setOnClickListener(v -> startActivity(new Intent(this, PermissionStatusActivity.class)));
        btnGameStart.setOnClickListener(v -> {
            String userId = sessionManager.getUserIdentifier();
            SharedPreferences preferences = getSharedPreferences("game_data", MODE_PRIVATE);
            boolean isFirst = preferences.getBoolean(userId + "_isFirst", true);
            if (isFirst) {
                startActivity(new Intent(this, CharacterSelectActivity.class));
            } else {
                startActivity(new Intent(this, GameActivity.class));
            }
        });
        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            Toast.makeText(this, "성공적으로 로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
            openLoginAndFinish();
        });
        tvWithdraw.setOnClickListener(v -> startActivity(new Intent(this, WithdrawActivity.class)));
        tvDeveloperMode.setOnClickListener(v -> startActivity(new Intent(this, DeveloperModeActivity.class)));
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("알림 권한 안내")
                .setMessage("집중 타이머를 알림과 잠금 화면에 표시하려면 알림 권한이 필요합니다.\n\n권한 요청 창에서 허용을 선택해 주십시오.")
                .setPositiveButton("권한 요청", (dialog, which) -> notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                .setNegativeButton("나중에", null)
                .show();
    }

    private void handleFocusModeIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if (intent.getBooleanExtra(EXTRA_STOP_CONSCIENCE_FOCUS, false)) {
            intent.removeExtra(EXTRA_STOP_CONSCIENCE_FOCUS);
            if (isCurrentUserSessionRunning()) {
                stopFocusSession();
            }
            return;
        }
        if (intent.getBooleanExtra(FocusModeSelectActivity.EXTRA_START_FREE_FOCUS, false)) {
            intent.removeExtra(FocusModeSelectActivity.EXTRA_START_FREE_FOCUS);
            if (!isCurrentUserSessionRunning()) {
                attemptStartFocusSession();
            }
        }
    }

    private boolean isCurrentUserSessionRunning() {
        return activeFocusSessionStore.isRunningForUser(sessionManager.getUserIdentifier());
    }

    private void syncFocusSessionUi() {
        timerHandler.removeCallbacks(timerRunnable);
        if (isCurrentUserSessionRunning()) {
            btnStartFocus.setText("집중 종료");
            btnStartFocus.setBackgroundResource(R.drawable.bg_button_red);
            btnStartFocus.setBackgroundTintList(null);
            btnResetFocus.setVisibility(View.VISIBLE);
            btnResetFocus.setText(activeFocusSessionStore.isPaused() ? "재개" : "일시정지");
            btnResetFocus.setEnabled(true);
            tvTimer.setText(DurationFormatter.formatClock(activeFocusSessionStore.getElapsedDurationMillis()));
            if (!activeFocusSessionStore.isPaused()) {
                timerHandler.post(timerRunnable);
            }
        } else {
            btnStartFocus.setText("집중 시작");
            btnStartFocus.setBackgroundResource(R.drawable.bg_button_focus);
            btnStartFocus.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.flow_button_focus));
            btnResetFocus.setVisibility(View.GONE);
            tvTimer.setText("00:00");
        }
    }

    private void attemptStartFocusSession() {
        if (!UsageAccessHelper.hasUsageAccess(this)) {
            showUsageAccessDialog();
            return;
        }
        activeFocusSessionStore.saveRunningSession(sessionManager.getUserIdentifier(), System.currentTimeMillis());
        startConscienceFocusService();
        Toast.makeText(this, "양심 집중 모드를 시작합니다.", Toast.LENGTH_SHORT).show();
        syncFocusSessionUi();
    }

    private void togglePauseFocusSession() {
        if (!isCurrentUserSessionRunning()) {
            return;
        }
        if (activeFocusSessionStore.isPaused()) {
            activeFocusSessionStore.resume();
            Toast.makeText(this, "집중 타이머를 다시 시작하였습니다.", Toast.LENGTH_SHORT).show();
        } else {
            activeFocusSessionStore.pause();
            Toast.makeText(this, "집중 타이머를 일시정지하였습니다.", Toast.LENGTH_SHORT).show();
        }
        startConscienceFocusService();
        syncFocusSessionUi();
    }

    private void startConscienceFocusService() {
        Intent intent = new Intent(this, ConscienceFocusService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void stopConscienceFocusService() {
        stopService(new Intent(this, ConscienceFocusService.class));
    }

    private void stopFocusSession() {
        long startTimeMillis = activeFocusSessionStore.getStartTimeMillis();
        if (startTimeMillis <= 0L) {
            activeFocusSessionStore.clear();
            stopConscienceFocusService();
            syncFocusSessionUi();
            return;
        }

        long endTimeMillis = System.currentTimeMillis();
        long durationMillis = activeFocusSessionStore.getElapsedDurationMillis();
        activeFocusSessionStore.clear();
        stopConscienceFocusService();
        focusQuestManager.recordFocus(durationMillis, 0);
        bindQuestStatus();
        syncFocusSessionUi();
        setMainActionEnabled(false);
        Toast.makeText(this, "집중 기록을 분석하고 있습니다.", Toast.LENGTH_SHORT).show();

        analysisExecutor.execute(() -> {
            FocusAnalysisResult result = new AppUsageAnalyzer().analyze(this, startTimeMillis, endTimeMillis);
            RewardResult rewardResult = rewardManager.calculate(result);
            runOnUiThread(() -> applyGameReward(rewardResult));
            focusRepository.saveSession(sessionManager.getUserIdentifier(), sessionManager.getUserName(), result, rewardResult, sessionId -> {
                setMainActionEnabled(true);
                Intent resultIntent = new Intent(this, FocusResultActivity.class);
                resultIntent.putExtra(FocusResultActivity.EXTRA_SESSION_ID, sessionId);
                startActivity(resultIntent);
            });
        });
    }

    private void setMainActionEnabled(boolean enabled) {
        btnStartFocus.setEnabled(enabled);
        btnResetFocus.setEnabled(enabled && isCurrentUserSessionRunning());
        btnStats.setEnabled(enabled);
        btnPermissionStatus.setEnabled(enabled);
        btnGameStart.setEnabled(enabled);
        btnLogout.setEnabled(enabled);
        tvWithdraw.setEnabled(enabled);
        tvDeveloperMode.setEnabled(enabled);
    }

    private void showUsageAccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("사용 기록 접근 권한 안내")
                .setMessage("양심 집중 모드에서 앱 사용 기록을 분석하려면 사용 기록 접근 권한이 필요합니다.\n\n설정 화면에서 FlowTimer를 선택한 뒤 허용해 주십시오.")
                .setPositiveButton("설정 화면으로 이동", (dialog, which) -> UsageAccessHelper.openSettings(this))
                .setNegativeButton("취소", null)
                .show();
    }

    private void applyGameReward(RewardResult rewardResult) {
        if (rewardResult.getCoin() <= 0 && rewardResult.getExp() <= 0 && rewardResult.getTimeMinutes() <= 0) {
            return;
        }
        SharedPreferences preferences = getSharedPreferences("game_data", MODE_PRIVATE);
        String userId = sessionManager.getUserIdentifier();
        int currentCoin = preferences.getInt(userId + "_coin", 0);
        int currentExp = preferences.getInt(userId + "_exp", 0);
        int currentTime = preferences.getInt(userId + "_time", 0);
        preferences.edit()
                .putInt(userId + "_coin", currentCoin + rewardResult.getCoin())
                .putInt(userId + "_exp", currentExp + rewardResult.getExp())
                .putInt(userId + "_time", currentTime + rewardResult.getTimeMinutes())
                .apply();
        Toast.makeText(this, "집중 결과에 따라 게임 보상이 지급되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void openLoginAndFinish() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
