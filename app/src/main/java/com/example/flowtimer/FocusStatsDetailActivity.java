package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.focus.AppDisplayHelper;

import java.util.ArrayList;

public class FocusStatsDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_LABELS = "extra_labels";
    public static final String EXTRA_VALUES = "extra_values";
    public static final String EXTRA_PACKAGES = "extra_packages";
    public static final String TYPE_PERIOD = "period";
    public static final String TYPE_HOURLY = "hourly";
    public static final String TYPE_APP = "app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_stats_detail);

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        LinearLayout layoutDetail = findViewById(R.id.layoutDetailItems);
        Button btnGoStats = findViewById(R.id.btnGoStats);
        Button btnGoMain = findViewById(R.id.btnGoMain);
        btnGoStats.setOnClickListener(v -> openStats());
        btnGoMain.setOnClickListener(v -> openMain());

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        boolean isAppType = TYPE_APP.equals(type);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        tvTitle.setText(title == null || title.trim().isEmpty() ? "상세 통계" : title + " 상세 기록");

        ArrayList<String> labels = getIntent().getStringArrayListExtra(EXTRA_LABELS);
        ArrayList<String> values = getIntent().getStringArrayListExtra(EXTRA_VALUES);
        ArrayList<String> packages = getIntent().getStringArrayListExtra(EXTRA_PACKAGES);
        layoutDetail.removeAllViews();
        if (labels == null || values == null || labels.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("표시할 상세 기록이 없습니다.");
            emptyView.setPadding(dp(12), dp(12), dp(12), dp(12));
            layoutDetail.addView(emptyView);
            return;
        }
        for (int i = 0; i < labels.size(); i++) {
            String pkg = packages != null && packages.size() > i ? packages.get(i) : "";
            if (isAppType) {
                layoutDetail.addView(createAppRow(i + 1, pkg, labels.get(i), values.get(i)));
            } else {
                TextView row = new TextView(this);
                row.setText((i + 1) + ". " + labels.get(i) + " · " + values.get(i));
                row.setTextSize(15f);
                row.setPadding(dp(12), dp(10), dp(12), dp(10));
                layoutDetail.addView(row);
            }
        }
    }

    private LinearLayout createAppRow(int index, String packageName, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));

        ImageView iconView = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(28), dp(28));
        iconParams.rightMargin = dp(10);
        iconView.setLayoutParams(iconParams);
        iconView.setImageDrawable(AppDisplayHelper.resolveAppIcon(this, packageName));
        row.addView(iconView);

        TextView textView = new TextView(this);
        textView.setText(index + ". " + AppDisplayHelper.resolveAppName(this, packageName, label) + " · " + value);
        textView.setTextSize(15f);
        row.addView(textView);
        return row;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    private void openStats() {
        startActivity(new Intent(this, FocusStatsActivity.class));
        finish();
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
