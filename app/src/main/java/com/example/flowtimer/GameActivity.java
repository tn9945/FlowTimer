package com.example.flowtimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    public static final String EXTRA_NEW_GAME = "extra_new_game";
    public static final String EXTRA_DURATION_SECONDS = "extra_duration_seconds";
    private static final int[] FARMS = {R.id.farm1, R.id.farm2, R.id.farm3, R.id.farm4, R.id.farm5, R.id.farm6, R.id.farm7, R.id.farm8, R.id.farm9, R.id.farm10, R.id.farm11, R.id.farm12};
    private static final int[] TREES = {R.id.tree1, R.id.tree2, R.id.tree3, R.id.tree4};
    private static final int[] HARD_TREES = {R.id.hardTree1, R.id.hardTree2, R.id.hardTree3, R.id.hardTree4};
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences preferences;
    private String prefix;
    private TextView timer;
    private TextView crops;
    private TextView woods;
    private boolean expired;
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (active()) {
                refresh();
                handler.postDelayed(this, 1000L);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamemain);
        prefix = new SessionManager(this).getUserIdentifier() + "_";
        preferences = getSharedPreferences("pet_game_data", MODE_PRIVATE);
        migrate();
        if (getIntent().getBooleanExtra(EXTRA_NEW_GAME, false)) {
            putLong("game_end_time", System.currentTimeMillis() + getIntent().getLongExtra(EXTRA_DURATION_SECONDS, 300L) * 1000L);
        } else if (!hasActiveGame()) {
            startActivity(new Intent(this, CustomizeActivity.class));
            finish();
            return;
        }
        timer = findViewById(R.id.txtTimer);
        crops = findViewById(R.id.txtCrop);
        woods = findViewById(R.id.txtWoodResource);
        bind();
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(ticker);
        if (preferences != null && timer != null && active()) handler.postDelayed(ticker, 1000L);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ticker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(ticker);
    }

    private void bind() {
        for (int i = 0; i < FARMS.length; i++) {
            int index = i + 1;
            findViewById(FARMS[i]).setOnClickListener(v -> farm(index));
        }
        for (int i = 0; i < TREES.length; i++) {
            String tree = "tree" + (i + 1);
            String hardTree = "hardTree" + (i + 1);
            findViewById(TREES[i]).setOnClickListener(v -> tree(tree, 10, 5, 180000L));
            findViewById(HARD_TREES[i]).setOnClickListener(v -> tree(hardTree, 20, 10, 300000L));
        }
        findViewById(R.id.btnShop).setOnClickListener(v -> { if (active()) startActivity(new Intent(this, ShopActivity.class)); });
        findViewById(R.id.btnHome).setOnClickListener(v -> finish());
    }

    private void farm(int index) {
        if (!active()) return;
        if (!getBoolean("farm_unlocked_" + index, index <= 6)) {
            new AlertDialog.Builder(this).setTitle("밭 해금").setMessage("울타리 10개와 나무조각 200개를 사용하여 해금하시겠습니까?")
                    .setPositiveButton("해금", (d, w) -> {
                        if (getInt("fence") < 10 || getInt("wood") < 200) { toast("재료가 부족합니다."); return; }
                        add("fence", -10); add("wood", -200); putBoolean("farm_unlocked_" + index, true); refresh();
                    }).setNegativeButton("취소", null).show();
            return;
        }
        long started = getLong("start_" + index);
        String type = getString("type_" + index);
        if (started == 0L || type.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("작물 선택").setItems(new String[]{"밀 심기", "자두 심기"}, (d, w) -> plant(index, w == 0 ? "wheat" : "plum")).show();
            return;
        }
        long wait = "wheat".equals(type) ? 10000L : 30000L;
        if (System.currentTimeMillis() - started < wait) { toast("작물이 자라고 있습니다."); return; }
        putLong("start_" + index, 0L); putString("type_" + index, ""); add(type, 1); refresh(); toast("수확을 완료하였습니다.");
    }

    private void plant(int index, String type) {
        String seed = "wheat".equals(type) ? "wheat_seeds" : "plum_seeds";
        if (getInt(seed) <= 0) { toast("보유한 씨앗이 없습니다."); return; }
        add(seed, -1); putLong("start_" + index, System.currentTimeMillis()); putString("type_" + index, type); refresh(); toast("작물 심기를 완료하였습니다.");
    }

    private void tree(String id, int reward, int max, long cooldown) {
        if (!active()) return;
        if (!getBoolean(id + "_unlocked", initialTree(id))) {
            new AlertDialog.Builder(this).setTitle("숲 해금").setMessage("밀 30개를 사용하여 해금하시겠습니까?")
                    .setPositiveButton("해금", (d, w) -> { if (getInt("wheat") < 30) { toast("밀이 부족합니다."); return; } add("wheat", -30); putBoolean(id + "_unlocked", true); refresh(); })
                    .setNegativeButton("취소", null).show();
            return;
        }
        long now = System.currentTimeMillis(); int clicks = getInt(id + "_clicks");
        if (clicks >= max && now - getLong(id + "_last") < cooldown) { toast("현재 쿨타임입니다."); return; }
        if (clicks >= max) clicks = 0;
        clicks++; putInt(id + "_clicks", clicks); add(id.startsWith("hard") ? "hardWood" : "wood", reward); if (clicks >= max) putLong(id + "_last", now); refresh();
    }

    private void refresh() {
        long left = Math.max(0L, getLong("game_end_time") - System.currentTimeMillis());
        timer.setText(String.format(Locale.KOREA, "남은 시간 : %02d분 %02d초", left / 60000L, left / 1000L % 60L));
        crops.setText("밀: " + getInt("wheat") + "개 | 자두: " + getInt("plum") + "개\n밀 씨앗: " + getInt("wheat_seeds") + "개 | 자두 씨앗: " + getInt("plum_seeds") + "개");
        woods.setText("나무조각: " + getInt("wood") + "개 | 단단한 나무조각: " + getInt("hardWood") + "개 | 울타리: " + getInt("fence") + "개");
        for (int i = 0; i < FARMS.length; i++) updateFarm(i + 1, FARMS[i]);
        for (int i = 0; i < TREES.length; i++) { updateTree("tree" + (i + 1), TREES[i], R.drawable.tree, 5, 180000L); updateTree("hardTree" + (i + 1), HARD_TREES[i], R.drawable.hard_tree, 10, 300000L); }
    }

    private void updateFarm(int index, int viewId) {
        ImageView view = findViewById(viewId);
        if (!getBoolean("farm_unlocked_" + index, index <= 6)) { view.setImageResource(R.drawable.lock); view.setAlpha(0.5f); return; }
        view.setAlpha(1f); long started = getLong("start_" + index); String type = getString("type_" + index);
        if (started == 0L || type.isEmpty()) { view.setImageResource(R.drawable.dirt); return; }
        long elapsed = System.currentTimeMillis() - started;
        if ("wheat".equals(type)) view.setImageResource(elapsed < 5000L ? R.drawable.crop_seed : elapsed < 10000L ? R.drawable.crop_stage1 : R.drawable.crop_complete);
        else view.setImageResource(elapsed < 15000L ? R.drawable.crop_seed_2 : elapsed < 30000L ? R.drawable.crop_stage2 : R.drawable.crop_complete2);
    }

    private void updateTree(String id, int viewId, int drawable, int max, long cooldown) {
        ImageView view = findViewById(viewId);
        if (!getBoolean(id + "_unlocked", initialTree(id))) { view.setImageResource(R.drawable.lock); view.setAlpha(0.4f); return; }
        view.setImageResource(drawable); view.setAlpha(getInt(id + "_clicks") >= max && System.currentTimeMillis() - getLong(id + "_last") < cooldown ? 0.3f : 1f);
    }

    private boolean hasActiveGame() {
        return getLong("game_end_time") > System.currentTimeMillis();
    }

    private boolean active() {
        if (hasActiveGame()) return true;
        if (!expired && preferences != null && !isFinishing()) {
            expired = true; handler.removeCallbacks(ticker); preferences.edit().remove(prefix + "game_end_time").apply();
            new AlertDialog.Builder(this).setTitle("게임 종료").setMessage("게임 시간이 종료되었습니다.").setCancelable(false).setPositiveButton("확인", (d, w) -> finish()).show();
        }
        return false;
    }

    private void migrate() {
        if (preferences.getBoolean(prefix + "initialized", false)) return;
        SharedPreferences old = getSharedPreferences("GamePrefs", MODE_PRIVATE); SharedPreferences.Editor editor = preferences.edit();
        String[] keys = {"wheat", "plum", "wood", "hardWood", "fence", "wheat_seeds", "plum_seeds", "today_wheat_seed_buy", "today_plum_seed_buy"};
        for (String key : keys) if (old.contains(key)) editor.putInt(prefix + key, old.getInt(key, 0));
        for (int i = 1; i <= 12; i++) { copyLong(old, editor, "start_" + i); copyString(old, editor, "type_" + i); copyBoolean(old, editor, "farm_unlocked_" + i); }
        for (int i = 1; i <= 4; i++) { copyTree(old, editor, "tree" + i); copyTree(old, editor, "hardTree" + i); }
        if (old.contains("last_buy_date")) editor.putLong(prefix + "free_seed_day", old.getLong("last_buy_date", 0L));
        editor.putBoolean(prefix + "initialized", true).apply();
    }

    private void copyTree(SharedPreferences old, SharedPreferences.Editor editor, String id) { copyBoolean(old, editor, id + "_unlocked"); copyInt(old, editor, id + "_clicks"); copyLong(old, editor, id + "_last"); }
    private void copyInt(SharedPreferences old, SharedPreferences.Editor editor, String key) { if (old.contains(key)) editor.putInt(prefix + key, old.getInt(key, 0)); }
    private void copyLong(SharedPreferences old, SharedPreferences.Editor editor, String key) { if (old.contains(key)) editor.putLong(prefix + key, old.getLong(key, 0L)); }
    private void copyString(SharedPreferences old, SharedPreferences.Editor editor, String key) { if (old.contains(key)) editor.putString(prefix + key, old.getString(key, "")); }
    private void copyBoolean(SharedPreferences old, SharedPreferences.Editor editor, String key) { if (old.contains(key)) editor.putBoolean(prefix + key, old.getBoolean(key, false)); }

    private boolean initialTree(String id) { return "tree1".equals(id) || "tree2".equals(id) || "hardTree1".equals(id); }
    private int getInt(String key) { return preferences.getInt(prefix + key, 0); }
    private void putInt(String key, int value) { preferences.edit().putInt(prefix + key, value).apply(); }
    private void add(String key, int amount) { putInt(key, getInt(key) + amount); }
    private long getLong(String key) { return preferences.getLong(prefix + key, 0L); }
    private void putLong(String key, long value) { preferences.edit().putLong(prefix + key, value).apply(); }
    private String getString(String key) { return preferences.getString(prefix + key, ""); }
    private void putString(String key, String value) { preferences.edit().putString(prefix + key, value).apply(); }
    private boolean getBoolean(String key, boolean value) { return preferences.getBoolean(prefix + key, value); }
    private void putBoolean(String key, boolean value) { preferences.edit().putBoolean(prefix + key, value).apply(); }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
