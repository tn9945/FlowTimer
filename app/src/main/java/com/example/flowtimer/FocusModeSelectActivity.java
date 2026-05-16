package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FocusModeSelectActivity extends AppCompatActivity {

    public static final String EXTRA_START_FREE_FOCUS = "extra_start_free_focus";

    private Button btnStrictMode;
    private Button btnFreeMode;
    private Button btnCancelFocusMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_mode_select);

        bindViews();
        bindActions();
    }

    private void bindViews() {
        btnStrictMode = findViewById(R.id.btnStrictMode);
        btnFreeMode = findViewById(R.id.btnFreeMode);
        btnCancelFocusMode = findViewById(R.id.btnCancelFocusMode);
    }

    private void bindActions() {
        btnStrictMode.setOnClickListener(v -> {
            Intent intent = new Intent(this, StrictFocusActivity.class);
            startActivity(intent);
            finish();
        });
        btnFreeMode.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(EXTRA_START_FREE_FOCUS, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        btnCancelFocusMode.setOnClickListener(v -> finish());
    }
}
