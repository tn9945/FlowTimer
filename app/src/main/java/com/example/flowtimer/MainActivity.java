package com.example.flowtimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.focus.ActiveFocusSessionStore;
import com.example.flowtimer.focus.AppUsageAnalyzer;
import com.example.flowtimer.focus.DurationFormatter;
import com.example.flowtimer.focus.FocusAnalysisResult;
import com.example.flowtimer.focus.FocusRepository;
import com.example.flowtimer.focus.RewardManager;
import com.example.flowtimer.focus.RewardResult;
import com.example.flowtimer.focus.UsageAccessHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private FocusRepository focusRepository;
    private ActiveFocusSessionStore activeFocusSessionStore;
    private ExecutorService analysisExecutor;
    private final RewardManager rewardManager = new RewardManager();

    private TextView tvWelcome;
    private TextView tvUserId;
    private TextView tvTimer;
    private Button btnStartFocus;
    private Button btnResetFocus;
    private Button btnStats;
    private Button btnGameStart;
    private Button btnLogout;
    private TextView tvWithdraw;
    private TextView tvDeveloperMode;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isCurrentUserSessionRunning()) {
                long startTimeMillis = activeFocusSessionStore.getStartTimeMillis();
                tvTimer.setText(DurationFormatter.formatClock(System.currentTimeMillis() - startTimeMillis));
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
        analysisExecutor = Executors.newSingleThreadExecutor();

        bindViews();
        bindUserInfo();
        bindActions();
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

    private void bindViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserId = findViewById(R.id.tvUserId);
        tvTimer = findViewById(R.id.tvTimer);
        btnStartFocus = findViewById(R.id.btnStartFocus);
        btnResetFocus = findViewById(R.id.btnResetFocus);
        btnStats = findViewById(R.id.btnStats);
        btnGameStart = findViewById(R.id.btnGameStart);
        btnLogout = findViewById(R.id.btnLogout);
        tvWithdraw = findViewById(R.id.tvWithdraw);
        tvDeveloperMode = findViewById(R.id.tvDeveloperMode);
    }

    private void bindUserInfo() {
        tvWelcome.setText(sessionManager.getUserName() + "님, 환영합니다.");
        tvUserId.setText("ID: " + sessionManager.getUserIdentifier());
        btnGameStart.setText("Game");
    }

    private void bindActions() {
        btnStartFocus.setOnClickListener(v -> {
            if (isCurrentUserSessionRunning()) {
                stopFocusSession();
            } else {
                startActivity(new Intent(this, FocusModeSelectActivity.class));
            }
        });
        btnResetFocus.setOnClickListener(v -> cancelFocusSession());
        btnStats.setOnClickListener(v -> startActivity(new Intent(this, FocusStatsActivity.class)));
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

    private void handleFocusModeIntent(Intent intent) {
        if (intent == null) {
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
            btnResetFocus.setVisibility(View.VISIBLE);
            long startTimeMillis = activeFocusSessionStore.getStartTimeMillis();
            tvTimer.setText(DurationFormatter.formatClock(System.currentTimeMillis() - startTimeMillis));
            timerHandler.post(timerRunnable);
        } else {
            btnStartFocus.setText("집중 시작");
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
        Toast.makeText(this, "상호작용 허용 모드 집중 기록을 시작합니다.", Toast.LENGTH_SHORT).show();
        syncFocusSessionUi();
    }

    private void stopFocusSession() {
        long startTimeMillis = activeFocusSessionStore.getStartTimeMillis();
        if (startTimeMillis <= 0L) {
            activeFocusSessionStore.clear();
            syncFocusSessionUi();
            return;
        }

        long endTimeMillis = System.currentTimeMillis();
        activeFocusSessionStore.clear();
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

    private void cancelFocusSession() {
        if (!isCurrentUserSessionRunning()) {
            tvTimer.setText("00:00");
            btnResetFocus.setVisibility(View.GONE);
            return;
        }
        activeFocusSessionStore.clear();
        syncFocusSessionUi();
        Toast.makeText(this, "진행 중인 집중 기록이 초기화되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void setMainActionEnabled(boolean enabled) {
        btnStartFocus.setEnabled(enabled);
        btnResetFocus.setEnabled(enabled);
        btnStats.setEnabled(enabled);
        btnGameStart.setEnabled(enabled);
        btnLogout.setEnabled(enabled);
        tvWithdraw.setEnabled(enabled);
        tvDeveloperMode.setEnabled(enabled);
    }

    private void showUsageAccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("권한 설정 안내")
                .setMessage("집중 기록을 시작하려면 사용 기록 접근 권한을 허용해야 합니다. 사용 기록 접근 설정 화면으로 이동합니다.")
                .setPositiveButton("이동", (dialog, which) -> UsageAccessHelper.openSettings(this))
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
