package com.example.flowtimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class CharacterSelectActivity extends AppCompatActivity {

    private ImageView imgRabbit;
    private ImageView imgDog;
    private ImageView imgBear;
    private Button btnGoMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.characterselectactivity);

        imgRabbit = findViewById(R.id.imgRabbit);
        imgDog = findViewById(R.id.imgDog);
        imgBear = findViewById(R.id.imgBear);
        btnGoMain = findViewById(R.id.btnGoMain);

        imgRabbit.setOnClickListener(v -> selectCharacter("rabbit"));
        imgDog.setOnClickListener(v -> selectCharacter("dog"));
        imgBear.setOnClickListener(v -> selectCharacter("bear"));
        btnGoMain.setOnClickListener(v -> openMain());
    }

    private void selectCharacter(String character) {
        SessionManager sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserIdentifier();
        SharedPreferences preferences = getSharedPreferences("game_data", MODE_PRIVATE);
        preferences.edit()
                .putString(userId + "_character", character)
                .putBoolean(userId + "_isFirst", false)
                .apply();
        startActivity(new Intent(this, GameActivity.class));
        finish();
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
