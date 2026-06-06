package com.example.flowtimer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ShopActivity extends AppCompatActivity {

    private static final int DAILY_FREE_LIMIT = 5;
    private SharedPreferences preferences;
    private String prefix;
    private TextView plants;
    private TextView woods;
    private TextView fences;
    private TextView seeds;
    private TextView wheatLimit;
    private TextView plumLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        prefix = new SessionManager(this).getUserIdentifier() + "_";
        preferences = getSharedPreferences("pet_game_data", MODE_PRIVATE);
        if (!checkActive()) return;
        resetDailyLimit();
        bindViews();
        bindActions();
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferences != null && plants != null && checkActive()) {
            resetDailyLimit();
            refresh();
        }
    }

    private void bindViews() {
        plants = findViewById(R.id.txtPlant);
        woods = findViewById(R.id.txtWood);
        fences = findViewById(R.id.txtFence);
        seeds = findViewById(R.id.txtSeed);
        wheatLimit = findViewById(R.id.txtWheatLimit);
        plumLimit = findViewById(R.id.txtPlumLimit);
    }

    private void bindActions() {
        Button back = findViewById(R.id.btnBack);
        Button buyWheat = findViewById(R.id.btnBuySeed);
        Button buyPlum = findViewById(R.id.btnBuyPlumSeed);
        Button tradeWheat = findViewById(R.id.btnTradeWheatSeed);
        Button tradePlum = findViewById(R.id.btnTradePlumSeed);
        Button tradeWood = findViewById(R.id.btnTradeTree);
        Button tradeFence = findViewById(R.id.btnTradeFence);
        back.setOnClickListener(v -> finish());
        buyWheat.setOnClickListener(v -> buyFree("wheatSeed", "todayWheatSeedBuy"));
        buyPlum.setOnClickListener(v -> buyFree("plumSeed", "todayPlumSeedBuy"));
        tradeWheat.setOnClickListener(v -> tradeCrop("wheat", "wheatSeed", "밀"));
        tradePlum.setOnClickListener(v -> tradeCrop("plum", "plumSeed", "자두"));
        tradeWood.setOnClickListener(v -> trade("wood", 300, "hardWood", 100, "나무조각이 부족합니다."));
        tradeFence.setOnClickListener(v -> trade("hardWood", 500, "fence", 1, "단단한나무조각이 부족합니다."));
    }

    private void buyFree(String seed, String counter) {
        if (!checkActive()) return;
        int count = getInt(counter);
        if (count >= DAILY_FREE_LIMIT) {
            toast("무료 구매 한도를 초과하였습니다.");
            return;
        }
        add(seed, 1);
        putInt(counter, count + 1);
        refresh();
    }

    private void tradeCrop(String crop, String seed, String name) {
        if (!checkActive()) return;
        if (getInt(crop) < 2) {
            toast(name + "가 부족합니다.");
            return;
        }
        add(crop, -2);
        add(seed, 1);
        refresh();
    }

    private void trade(String source, int sourceAmount, String target, int targetAmount, String message) {
        if (!checkActive()) return;
        if (getInt(source) < sourceAmount) {
            toast(message);
            return;
        }
        add(source, -sourceAmount);
        add(target, targetAmount);
        refresh();
    }

    private void refresh() {
        plants.setText("밀 : " + getInt("wheat") + " | 자두 : " + getInt("plum"));
        woods.setText("나무조각 : " + getInt("wood") + " | 단단한나무조각 : " + getInt("hardWood"));
        fences.setText("울타리 : " + getInt("fence"));
        seeds.setText("보유 밀 씨앗 : " + getInt("wheatSeed") + "개 | 자두 씨앗 : " + getInt("plumSeed") + "개");
        wheatLimit.setText("밀 씨앗 무료 (현황: " + getInt("todayWheatSeedBuy") + " / " + DAILY_FREE_LIMIT + ")");
        plumLimit.setText("자두 씨앗 무료 (현황: " + getInt("todayPlumSeedBuy") + " / " + DAILY_FREE_LIMIT + ")");
    }

    private void resetDailyLimit() {
        Calendar calendar = Calendar.getInstance();
        long today = calendar.get(Calendar.YEAR) * 1000L + calendar.get(Calendar.DAY_OF_YEAR);
        if (preferences.getLong(prefix + "freeSeedDay", 0L) == today) return;
        preferences.edit().putLong(prefix + "freeSeedDay", today)
                .putInt(prefix + "todayWheatSeedBuy", 0)
                .putInt(prefix + "todayPlumSeedBuy", 0).apply();
    }

    private boolean checkActive() {
        if (preferences.getLong(prefix + "game_end_time", 0L) > System.currentTimeMillis()) return true;
        toast("게임 시간이 종료되었습니다.");
        finish();
        return false;
    }

    private int getInt(String key) { return preferences.getInt(prefix + key, 0); }
    private void putInt(String key, int value) { preferences.edit().putInt(prefix + key, value).apply(); }
    private void add(String key, int value) { putInt(key, getInt(key) + value); }
    private void toast(String text) { Toast.makeText(this, text, Toast.LENGTH_SHORT).show(); }
}
