package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.focus.DurationFormatter;
import com.example.flowtimer.focus.StrictFocusSessionStore;

public class BlockedAppActivity extends AppCompatActivity {

    public static final String EXTRA_BLOCKED_APP_NAME = "extra_blocked_app_name";

    private TextView tvBlockedMessage;
    private TextView tvBlockedFocusInfo;
    private TextView tvBlockedRemainInfo;
    private Button btnReturnStrictFocus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_app);

        tvBlockedMessage = findViewById(R.id.tvBlockedMessage);
        tvBlockedFocusInfo = findViewById(R.id.tvBlockedFocusInfo);
        tvBlockedRemainInfo = findViewById(R.id.tvBlockedRemainInfo);
        btnReturnStrictFocus = findViewById(R.id.btnReturnStrictFocus);

        String appName = getIntent().getStringExtra(EXTRA_BLOCKED_APP_NAME);
        if (appName == null || appName.trim().isEmpty()) {
            appName = "이 앱";
        }
        tvBlockedMessage.setText(appName + "은 집중하는 동안 사용할 수 없습니다.");
        bindFocusInfo();

        btnReturnStrictFocus.setOnClickListener(v -> returnToStrictFocus());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindFocusInfo();
    }

    private void bindFocusInfo() {
        StrictFocusSessionStore store = new StrictFocusSessionStore(this);
        long displayTime = store.isTimerMode() ? store.getRemainMillis() : store.getElapsedMillis();
        tvBlockedFocusInfo.setText((store.isTimerMode() ? "남은 집중 시간 " : "현재 집중 시간 ") + DurationFormatter.formatClock(displayTime));
        if (store.isTimerMode()) {
            if (store.isTargetReached()) {
                tvBlockedRemainInfo.setText("목표 시간을 달성하였습니다.");
            } else {
                tvBlockedRemainInfo.setText("목표까지 남은 시간 " + DurationFormatter.formatShortDuration(store.getRemainMillis()));
            }
        } else {
            tvBlockedRemainInfo.setText("확인 버튼을 누르면 타이머 화면으로 돌아갑니다.");
        }
    }

    private void returnToStrictFocus() {
        Intent intent = new Intent(this, StrictFocusActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
