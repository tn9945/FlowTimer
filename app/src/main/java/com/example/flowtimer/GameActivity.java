package com.example.flowtimer;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private TextView tvCoin;
    private TextView tvExp;
    private TextView tvTime;
    private Button btnGacha;
    private Button btnCustomize;
    private Button btnGoMain;

    private int coin;
    private int exp;
    private int time;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamemain);

        tvCoin = findViewById(R.id.tvCoin);
        tvExp = findViewById(R.id.tvExp);
        tvTime = findViewById(R.id.tvTime);
        btnGacha = findViewById(R.id.btnGacha);
        btnCustomize = findViewById(R.id.btnCustomize);
        btnGoMain = findViewById(R.id.btnGoMainBottom);

        SessionManager sessionManager = new SessionManager(this);
        userId = sessionManager.getUserIdentifier();

        loadGameData();
        bindCharacter();
        updateUI();

        btnGoMain.setOnClickListener(v -> openMain());
        btnGacha.setOnClickListener(v -> openGachaDialog());
        btnCustomize.setOnClickListener(v -> startActivity(new Intent(this, CustomizeActivity.class)));
    }

    private void bindCharacter() {
        SharedPreferences preferences = getSharedPreferences("game_data", MODE_PRIVATE);
        String character = preferences.getString(userId + "_character", "rabbit");
        ImageView imgCharacter = findViewById(R.id.imgCharacter);
        if ("dog".equals(character)) {
            imgCharacter.setImageResource(R.drawable.dog);
        } else if ("bear".equals(character)) {
            imgCharacter.setImageResource(R.drawable.bear);
        } else {
            imgCharacter.setImageResource(R.drawable.rabbit);
        }
    }

    private void openGachaDialog() {
        double random = Math.random();
        String resultTitle;

        if (random < 0.60) {
            int winCoin = ((int) (Math.random() * 10) * 10) + 10;
            coin += winCoin;
            resultTitle = winCoin + "원 당첨";
        } else if (random < 0.80) {
            int winCoin = ((int) (Math.random() * 40) * 10) + 110;
            coin += winCoin;
            resultTitle = "대박 " + winCoin + "원 당첨";
        } else if (random < 0.93) {
            exp += 5;
            resultTitle = "안경 아이템 획득";
        } else {
            exp += 10;
            resultTitle = "레어 의상 획득";
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_gacha, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();

        TextView tvResult = dialogView.findViewById(R.id.tvGachaResult);
        Button btnConfirm = dialogView.findViewById(R.id.btnGachaConfirm);
        tvResult.setText(resultTitle);
        btnConfirm.setOnClickListener(v -> {
            saveGameData();
            updateUI();
            alertDialog.dismiss();
        });

        alertDialog.show();
        if (alertDialog.getWindow() != null) {
            WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            alertDialog.getWindow().setAttributes(params);
        }
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void loadGameData() {
        SharedPreferences preferences = getSharedPreferences("game_data", MODE_PRIVATE);
        coin = preferences.getInt(userId + "_coin", 0);
        exp = preferences.getInt(userId + "_exp", 0);
        time = preferences.getInt(userId + "_time", 0);
    }

    private void saveGameData() {
        SharedPreferences preferences = getSharedPreferences("game_data", MODE_PRIVATE);
        preferences.edit()
                .putInt(userId + "_coin", coin)
                .putInt(userId + "_exp", exp)
                .putInt(userId + "_time", time)
                .apply();
    }

    private void updateUI() {
        tvCoin.setText("💰 " + coin);
        tvExp.setText("⭐ " + exp);
        tvTime.setText("⏱ " + time + "분");
    }
}
