package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.focus.StrictFocusSessionStore;

public class StrictFocusResultActivity extends AppCompatActivity {

    private TextView tvStrictResultSummary;
    private Button btnGoMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strict_focus_result);

        tvStrictResultSummary = findViewById(R.id.tvStrictResultSummary);
        btnGoMain = findViewById(R.id.btnGoMain);

        StrictFocusSessionStore store = new StrictFocusSessionStore(this);
        tvStrictResultSummary.setText(store.getLastSummary());
        btnGoMain.setOnClickListener(v -> openMain());
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
