package com.example.flowtimer;

import android.content.Intent;
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

public class StrictFocusActivity extends AppCompatActivity {

    private static final long STRICT_FOCUS_DURATION_MILLIS = 25L * 60L * 1000L;
    private static final String PREF_NAME = "strict_focus_session";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_RUNNING = "running";

    private TextView tvStrictTimer;
    private Button btnFinishStrictFocus;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTimeMillis;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimer();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isStrictFocusRunning() && !isFinishing()) {
            Intent intent = new Intent(this, StrictFocusActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

    private void bindViews() {
        tvStrictTimer = findViewById(R.id.tvStrictTimer);
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
        boolean running = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getBoolean(KEY_RUNNING, false);
        long savedStartTime = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getLong(KEY_START_TIME, 0L);
        if (running && savedStartTime > 0L) {
            startTimeMillis = savedStartTime;
        } else {
            startTimeMillis = System.currentTimeMillis();
            getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
                    .putBoolean(KEY_RUNNING, true)
                    .putLong(KEY_START_TIME, startTimeMillis)
                    .apply();
        }
    }

    private void bindActions() {
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
    }

    private boolean isStrictFocusRunning() {
        return getSharedPreferences(PREF_NAME, MODE_PRIVATE).getBoolean(KEY_RUNNING, false);
    }

    private void finishStrictFocus(String message) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().clear().apply();
        timerHandler.removeCallbacks(timerRunnable);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
