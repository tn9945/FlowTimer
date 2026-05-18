package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.focus.ActiveFocusSessionStore;

public class FocusStartConfigActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_STRICT = "strict";
    public static final String MODE_CONSCIENCE = "conscience";
    public static final String EXTRA_TIMER_MODE = "extra_timer_mode";
    public static final String EXTRA_TARGET_DURATION = "extra_target_duration";

    private RadioGroup rgTimerMode;
    private LinearLayout layoutTimerPicker;
    private NumberPicker npHours;
    private NumberPicker npMinutes;
    private TextView tvTimerInputGuide;
    private Button btnStartConfiguredFocus;
    private Button btnCancelConfig;
    private String focusMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_start_config);

        focusMode = getIntent().getStringExtra(EXTRA_MODE);
        if (focusMode == null) {
            focusMode = MODE_CONSCIENCE;
        }

        rgTimerMode = findViewById(R.id.rgTimerMode);
        layoutTimerPicker = findViewById(R.id.layoutTimerPicker);
        npHours = findViewById(R.id.npHours);
        npMinutes = findViewById(R.id.npMinutes);
        tvTimerInputGuide = findViewById(R.id.tvTimerInputGuide);
        btnStartConfiguredFocus = findViewById(R.id.btnStartConfiguredFocus);
        btnCancelConfig = findViewById(R.id.btnCancelConfig);

        setupPickers();
        rgTimerMode.setOnCheckedChangeListener((group, checkedId) -> updateTimerInputState());
        btnStartConfiguredFocus.setOnClickListener(v -> proceed());
        btnCancelConfig.setOnClickListener(v -> finish());
        updateTimerInputState();
    }

    private void setupPickers() {
        npHours.setMinValue(0);
        npHours.setMaxValue(15);
        npHours.setValue(1);
        npHours.setWrapSelectorWheel(false);

        npMinutes.setMinValue(0);
        npMinutes.setMaxValue(59);
        npMinutes.setValue(0);
        npMinutes.setWrapSelectorWheel(true);
    }

    private void updateTimerInputState() {
        boolean timerMode = rgTimerMode.getCheckedRadioButtonId() == R.id.rbTimerMode;
        layoutTimerPicker.setVisibility(timerMode ? View.VISIBLE : View.GONE);
        tvTimerInputGuide.setEnabled(timerMode);
    }

    private void proceed() {
        String timerMode = rgTimerMode.getCheckedRadioButtonId() == R.id.rbTimerMode ? ActiveFocusSessionStore.MODE_TIMER : ActiveFocusSessionStore.MODE_STOPWATCH;
        long targetDuration = 0L;
        if (ActiveFocusSessionStore.MODE_TIMER.equals(timerMode)) {
            int hours = npHours.getValue();
            int minutes = npMinutes.getValue();
            int totalMinutes = (hours * 60) + minutes;
            if (totalMinutes < 1 || totalMinutes > 900) {
                Toast.makeText(this, "타이머는 최소 1분, 최대 15시간까지 설정할 수 있습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            targetDuration = totalMinutes * 60L * 1000L;
        }

        if (MODE_STRICT.equals(focusMode)) {
            Intent intent = new Intent(this, AllowedAppsActivity.class);
            intent.putExtra(FocusModeSelectActivity.EXTRA_STRICT_MODE_TYPE, FocusModeSelectActivity.STRICT_MODE_ALLOWED_APPS);
            intent.putExtra(EXTRA_TIMER_MODE, timerMode);
            intent.putExtra(EXTRA_TARGET_DURATION, targetDuration);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(FocusModeSelectActivity.EXTRA_START_FREE_FOCUS, true);
            intent.putExtra(EXTRA_TIMER_MODE, timerMode);
            intent.putExtra(EXTRA_TARGET_DURATION, targetDuration);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        finish();
    }
}
