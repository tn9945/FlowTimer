package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FocusModeSelectActivity extends AppCompatActivity {

    public static final String EXTRA_START_FREE_FOCUS = "extra_start_free_focus";
    public static final String EXTRA_STRICT_MODE_TYPE = "extra_strict_mode_type";
    public static final String STRICT_MODE_ALLOWED_APPS = "allowed_apps";

    private Button btnForceFocusMode;
    private Button btnConscienceFocusMode;
    private Button btnCancelFocusMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_mode_select);

        bindViews();
        bindActions();
    }

    private void bindViews() {
        btnForceFocusMode = findViewById(R.id.btnForceFocusMode);
        btnConscienceFocusMode = findViewById(R.id.btnConscienceFocusMode);
        btnCancelFocusMode = findViewById(R.id.btnCancelFocusMode);
    }

    private void bindActions() {
        btnForceFocusMode.setOnClickListener(v -> {
            Intent intent = new Intent(this, FocusStartConfigActivity.class);
            intent.putExtra(FocusStartConfigActivity.EXTRA_MODE, FocusStartConfigActivity.MODE_STRICT);
            startActivity(intent);
            finish();
        });
        btnConscienceFocusMode.setOnClickListener(v -> {
            Intent intent = new Intent(this, FocusStartConfigActivity.class);
            intent.putExtra(FocusStartConfigActivity.EXTRA_MODE, FocusStartConfigActivity.MODE_CONSCIENCE);
            startActivity(intent);
            finish();
        });
        btnCancelFocusMode.setOnClickListener(v -> finish());
    }
}
