package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.EditText;
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
    private EditText etHours;
    private EditText etMinutes;
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
        etHours = findViewById(R.id.etHours);
        etMinutes = findViewById(R.id.etMinutes);
        tvTimerInputGuide = findViewById(R.id.tvTimerInputGuide);
        btnStartConfiguredFocus = findViewById(R.id.btnStartConfiguredFocus);
        btnCancelConfig = findViewById(R.id.btnCancelConfig);

        etHours.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        etMinutes.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        rgTimerMode.setOnCheckedChangeListener((group, checkedId) -> updateTimerInputState());
        btnStartConfiguredFocus.setOnClickListener(v -> proceed());
        btnCancelConfig.setOnClickListener(v -> finish());
        updateTimerInputState();
    }

    private void updateTimerInputState() {
        boolean timerMode = rgTimerMode.getCheckedRadioButtonId() == R.id.rbTimerMode;
        etHours.setEnabled(timerMode);
        etMinutes.setEnabled(timerMode);
        tvTimerInputGuide.setEnabled(timerMode);
        float alpha = timerMode ? 1f : 0.45f;
        etHours.setAlpha(alpha);
        etMinutes.setAlpha(alpha);
        tvTimerInputGuide.setAlpha(alpha);
    }

    private void proceed() {
        String timerMode = rgTimerMode.getCheckedRadioButtonId() == R.id.rbTimerMode ? ActiveFocusSessionStore.MODE_TIMER : ActiveFocusSessionStore.MODE_STOPWATCH;
        long targetDuration = 0L;
        if (ActiveFocusSessionStore.MODE_TIMER.equals(timerMode)) {
            int hours = parseNumber(etHours.getText().toString());
            int minutes = parseNumber(etMinutes.getText().toString());
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

    private int parseNumber(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
