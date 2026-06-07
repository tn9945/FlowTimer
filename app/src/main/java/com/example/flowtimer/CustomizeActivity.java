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
    private String prefix;
    private TextView txtPoint;
    private RadioGroup timeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);
        prefix = new SessionManager(this).getUserIdentifier() + "_";
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
        txtPoint.setText("현재 포인트 : " + rewardPreferences.getInt(prefix + "coin", 0));
    }

    private void startGame() {
        int selected = timeGroup.getCheckedRadioButtonId();
        long seconds = selected == R.id.radio15 ? 900L : selected == R.id.radio10 ? 600L : 300L;
        int cost = selected == R.id.radio15 ? 150 : selected == R.id.radio10 ? 100 : 50;
        int points = rewardPreferences.getInt(prefix + "coin", 0);
        if (points < cost) {
            Toast.makeText(this, "포인트가 부족합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        rewardPreferences.edit().putInt(prefix + "coin", points - cost).apply();
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_NEW_GAME, true);
        intent.putExtra(GameActivity.EXTRA_DURATION_SECONDS, seconds);
        startActivity(intent);
        finish();
    }

    private void initializePoints() {
        String coinKey = prefix + "coin";
        if (!rewardPreferences.contains(coinKey)) {
            int points = getSharedPreferences("GamePrefs", MODE_PRIVATE).getInt("points", 500);
            rewardPreferences.edit().putInt(coinKey, points).apply();
        }
    }
}
