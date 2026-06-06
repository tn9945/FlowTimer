package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CustomizeActivity extends AppCompatActivity {

    private PetGameStore petGameStore;
    private TextView txtPoint;
    private RadioGroup timeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);

        petGameStore = new PetGameStore(this);
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

        if (!petGameStore.spendPoints(pointCost)) {
            Toast.makeText(this, "포인트가 부족합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_IS_NEW_GAME, true);
        intent.putExtra(GameActivity.EXTRA_DURATION_SECONDS, durationSeconds);
        startActivity(intent);
        finish();
    }

    private void updatePointDisplay() {
        txtPoint.setText("현재 포인트 : " + petGameStore.getPoints());
    }
}
