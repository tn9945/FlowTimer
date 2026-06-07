package com.example.flowtimer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ShopActivity extends AppCompatActivity {

    private static final int LIMIT = 5;
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
        if (!active()) return;
        resetDay(); bind(); refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferences != null && plants != null && active()) { resetDay(); refresh(); }
    }

    private void bind() {
        plants = findViewById(R.id.txtPlant); woods = findViewById(R.id.txtWood); fences = findViewById(R.id.txtFence); seeds = findViewById(R.id.txtSeed);
        wheatLimit = findViewById(R.id.txtWheatLimit); plumLimit = findViewById(R.id.txtPlumLimit);
        Button back = findViewById(R.id.btnBack); Button buyWheat = findViewById(R.id.btnBuySeed); Button buyPlum = findViewById(R.id.btnBuyPlumSeed);
        Button tradeWheat = findViewById(R.id.btnTradeWheatSeed); Button tradePlum = findViewById(R.id.btnTradePlumSeed); Button tradeWood = findViewById(R.id.btnTradeTree); Button tradeFence = findViewById(R.id.btnTradeFence);
        back.setOnClickListener(v -> finish()); buyWheat.setOnClickListener(v -> buy("wheat_seeds", "today_wheat_seed_buy", "밀")); buyPlum.setOnClickListener(v -> buy("plum_seeds", "today_plum_seed_buy", "자두"));
        tradeWheat.setOnClickListener(v -> crop("wheat", "wheat_seeds", "밀")); tradePlum.setOnClickListener(v -> crop("plum", "plum_seeds", "자두"));
        tradeWood.setOnClickListener(v -> trade("wood", 300, "hardWood", 100, "나무조각이 부족합니다.")); tradeFence.setOnClickListener(v -> trade("hardWood", 500, "fence", 1, "단단한 나무조각이 부족합니다."));
    }

    private void buy(String seed, String counter, String name) {
        if (!active()) return; int count = get(counter); if (count >= LIMIT) { toast("무료 구매 한도를 초과하였습니다."); return; }
        add(seed, 1); put(counter, count + 1); refresh(); toast(name + " 씨앗 구매를 완료하였습니다.");
    }

    private void crop(String crop, String seed, String name) {
        if (!active()) return; if (get(crop) < 2) { toast(name + "가 부족합니다."); return; } add(crop, -2); add(seed, 1); refresh();
    }

    private void trade(String source, int cost, String target, int reward, String message) {
        if (!active()) return; if (get(source) < cost) { toast(message); return; } add(source, -cost); add(target, reward); refresh();
    }

    private void resetDay() {
        Calendar c = Calendar.getInstance(); long today = c.get(Calendar.YEAR) * 1000L + c.get(Calendar.DAY_OF_YEAR);
        if (preferences.getLong(prefix + "free_seed_day", 0L) == today) return;
        preferences.edit().putLong(prefix + "free_seed_day", today).putInt(prefix + "today_wheat_seed_buy", 0).putInt(prefix + "today_plum_seed_buy", 0).apply();
    }

    private void refresh() {
        plants.setText("밀 : " + get("wheat") + " | 자두 : " + get("plum")); woods.setText("나무조각 : " + get("wood") + " | 단단한 나무조각 : " + get("hardWood")); fences.setText("울타리 : " + get("fence"));
        seeds.setText("보유 밀 씨앗 : " + get("wheat_seeds") + "개 | 자두 씨앗 : " + get("plum_seeds") + "개"); wheatLimit.setText("밀 씨앗 무료\n(현황: " + get("today_wheat_seed_buy") + " / " + LIMIT + " 제한)"); plumLimit.setText("자두 씨앗 무료\n(현황: " + get("today_plum_seed_buy") + " / " + LIMIT + " 제한)");
    }

    private boolean active() { if (preferences.getLong(prefix + "game_end_time", 0L) > System.currentTimeMillis()) return true; toast("게임 시간이 종료되었습니다."); finish(); return false; }
    private int get(String key) { return preferences.getInt(prefix + key, 0); }
    private void put(String key, int value) { preferences.edit().putInt(prefix + key, value).apply(); }
    private void add(String key, int amount) { put(key, get(key) + amount); }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
