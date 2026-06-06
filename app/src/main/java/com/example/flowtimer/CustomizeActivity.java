package com.example.flowtimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CustomizeActivity extends AppCompatActivity {

    private SharedPreferences rewardPreferences;
    private String userPrefix;
    private TextView txtPoint;
    private RadioGroup timeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);

        userPrefix = new SessionManager(this).getUserIdentifier() + "_";
        rewardPreferences = getSharedPreferences("game_data", MODE_PRIVATE);
        initializePoints();
        txtPoint = findViewById(R.id.txtPoint);
        timeGroup = findViewById(R.id.timeGroup);
        Button btnStartGame = findViewById(R.id.btnStartGame);
        btnStartGame.setOnClickListener(v -> startGame());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePointDisplay();
    }

    private void startGame() {
        int selectedId = timeGroup.getCheckedRadioButtonId();
        long durationSeconds = 300L;
        int pointCost = 50;
        if (selectedId == R.id.radio10) {
            durationSeconds = 600L;
            pointCost = 100;
        } else if (selectedId == R.id.radio15) {
            durationSeconds = 900L;
            pointCost = 150;
        }
        int points = rewardPreferences.getInt(userPrefix + "coin", 0);
        if (points < pointCost) {
            Toast.makeText(this, "포인트가 부족합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        rewardPreferences.edit().putInt(userPrefix + "coin", points - pointCost).apply();
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_IS_NEW_GAME, true);
        intent.putExtra(GameActivity.EXTRA_DURATION_SECONDS, durationSeconds);
        startActivity(intent);
        finish();
    }

    private void initializePoints() {
        SharedPreferences farmPreferences = getSharedPreferences("pet_game_data", MODE_PRIVATE);
        String initializedKey = userPrefix + "points_initialized";
        if (!farmPreferences.getBoolean(initializedKey, false)) {
            String coinKey = userPrefix + "coin";
            if (rewardPreferences.getInt(coinKey, 0) <= 0) {
                rewardPreferences.edit().putInt(coinKey, 500).apply();
            }
            farmPreferences.edit().putBoolean(initializedKey, true).apply();
        }
    }

    private void updatePointDisplay() {
        txtPoint.setText("현재 포인트 : " + rewardPreferences.getInt(userPrefix + "coin", 0));
    }
}
