package com.example.flowtimer;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.focus.AppDisplayHelper;
import com.example.flowtimer.focus.DurationFormatter;
import com.example.flowtimer.focus.FocusRepository;
import com.example.flowtimer.focus.FocusStatsSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FocusStatsActivity extends AppCompatActivity {

    private static final String PERIOD_DAILY = "daily";
    private static final String PERIOD_WEEKLY = "weekly";
    private static final String PERIOD_MONTHLY = "monthly";

    private TextView tvTotalSession;
    private TextView tvTotalFocus;
    private TextView tvTotalBreak;
    private TextView tvTotalDistraction;
    private TextView tvAverageScore;
    private TextView tvPeriodTitle;
    private WeeklyFocusBarChartView chartRecentSevenDays;
    private LinearLayout layoutOverall;
    private LinearLayout layoutDaily;
    private LinearLayout layoutHourly;
    private LinearLayout layoutApp;
    private Button btnPeriodDaily;
    private Button btnPeriodWeekly;
    private Button btnPeriodMonthly;

    private FocusStatsSnapshot snapshot;
    private String selectedPeriod = PERIOD_DAILY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_stats);

        tvTotalSession = findViewById(R.id.tvTotalSession);
        tvTotalFocus = findViewById(R.id.tvTotalFocus);
        tvTotalBreak = findViewById(R.id.tvTotalBreak);
        tvTotalDistraction = findViewById(R.id.tvTotalDistraction);
        tvAverageScore = findViewById(R.id.tvAverageScore);
        tvPeriodTitle = findViewById(R.id.tvPeriodTitle);
        chartRecentSevenDays = findViewById(R.id.chartRecentSevenDays);
        layoutOverall = findViewById(R.id.layoutOverallStats);
        layoutDaily = findViewById(R.id.layoutDailyStats);
        layoutHourly = findViewById(R.id.layoutHourlyStats);
        layoutApp = findViewById(R.id.layoutAppStats);
        btnPeriodDaily = findViewById(R.id.btnPeriodDaily);
        btnPeriodWeekly = findViewById(R.id.btnPeriodWeekly);
        btnPeriodMonthly = findViewById(R.id.btnPeriodMonthly);

        Button btnGoMain = findViewById(R.id.btnGoMain);
        Button btnDailyDetail = findViewById(R.id.btnDailyDetail);
        Button btnHourlyDetail = findViewById(R.id.btnHourlyDetail);
        Button btnAppDetail = findViewById(R.id.btnAppDetail);

        btnGoMain.setOnClickListener(v -> openMain());
        btnDailyDetail.setOnClickListener(v -> openDetail(FocusStatsDetailActivity.TYPE_PERIOD));
        btnHourlyDetail.setOnClickListener(v -> openDetail(FocusStatsDetailActivity.TYPE_HOURLY));
        btnAppDetail.setOnClickListener(v -> openDetail(FocusStatsDetailActivity.TYPE_APP));
        btnPeriodDaily.setOnClickListener(v -> updatePeriod(PERIOD_DAILY));
        btnPeriodWeekly.setOnClickListener(v -> updatePeriod(PERIOD_WEEKLY));
        btnPeriodMonthly.setOnClickListener(v -> updatePeriod(PERIOD_MONTHLY));

        SessionManager sessionManager = new SessionManager(this);
        FocusRepository repository = new FocusRepository(this);
        repository.loadStats(sessionManager.getUserIdentifier(), this::bindSnapshot);
    }

    private void bindSnapshot(FocusStatsSnapshot snapshot) {
        this.snapshot = snapshot;
        tvTotalSession.setText(String.valueOf(snapshot.getTotalSessionCount()));
        tvTotalFocus.setText(DurationFormatter.formatDuration(snapshot.getTotalFocusDurationMillis()));
        tvTotalBreak.setText(DurationFormatter.formatDuration(snapshot.getTotalBreakDurationMillis()));
        tvTotalDistraction.setText(DurationFormatter.formatDuration(snapshot.getTotalDistractionDurationMillis()));
        tvAverageScore.setText(DurationFormatter.formatScore(snapshot.getAverageFocusScore()));
        chartRecentSevenDays.setItems(snapshot.getRecentDailyChartItems());
        refreshVisibleStats();
    }

    private void updatePeriod(String period) {
        selectedPeriod = period;
        updatePeriodButtons();
        refreshVisibleStats();
    }

    private void refreshVisibleStats() {
        if (snapshot == null) {
            return;
        }
        updatePeriodButtons();
        List<FocusStatsSnapshot.StatItem> periodItems = getSelectedPeriodItems();
        tvPeriodTitle.setText(getPeriodTitle());
        bindOverallSection(snapshot, periodItems);
        bindSection(layoutDaily, periodItems, false);
        bindSection(layoutHourly, snapshot.getHourlyItems(), false);
        bindSection(layoutApp, snapshot.getAppItems(), true);
    }

    private void updatePeriodButtons() {
        applySelected(btnPeriodDaily, PERIOD_DAILY.equals(selectedPeriod));
        applySelected(btnPeriodWeekly, PERIOD_WEEKLY.equals(selectedPeriod));
        applySelected(btnPeriodMonthly, PERIOD_MONTHLY.equals(selectedPeriod));
    }

    private void applySelected(Button button, boolean selected) {
        button.setSelected(selected);
        button.setTypeface(button.getTypeface(), selected ? Typeface.BOLD : Typeface.NORMAL);
        button.setAlpha(selected ? 1f : 0.72f);
    }

    private String getPeriodTitle() {
        if (PERIOD_WEEKLY.equals(selectedPeriod)) {
            return "주차별 통계";
        }
        if (PERIOD_MONTHLY.equals(selectedPeriod)) {
            return "월별 통계";
        }
        return "날짜별 통계";
    }

    private List<FocusStatsSnapshot.StatItem> getSelectedPeriodItems() {
        if (snapshot == null) {
            return new ArrayList<>();
        }
        if (PERIOD_WEEKLY.equals(selectedPeriod)) {
            return snapshot.getWeeklyItems();
        }
        if (PERIOD_MONTHLY.equals(selectedPeriod)) {
            return snapshot.getMonthlyItems();
        }
        return snapshot.getDailyItems();
    }

    private void bindOverallSection(FocusStatsSnapshot snapshot, List<FocusStatsSnapshot.StatItem> periodItems) {
        layoutOverall.removeAllViews();
        if (periodItems.isEmpty() && snapshot.getHourlyItems().isEmpty() && snapshot.getAppItems().isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("표시할 기록이 없습니다.");
            emptyView.setPadding(dp(12), dp(12), dp(12), dp(12));
            layoutOverall.addView(emptyView);
            return;
        }
        layoutOverall.addView(createOverviewCard(getPeriodTitle() + " 상위 기록", periodItems, false));
        layoutOverall.addView(createOverviewCard("시간대별 상위 기록", snapshot.getHourlyItems(), false));
        layoutOverall.addView(createOverviewCard("앱별 상위 기록", snapshot.getAppItems(), true));
    }

    private LinearLayout createOverviewCard(String title, List<FocusStatsSnapshot.StatItem> items, boolean includeIcon) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(dp(12), dp(12), dp(12), dp(12));

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(16f);
        titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);
        wrapper.addView(titleView);

        if (items == null || items.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("표시할 기록이 없습니다.");
            emptyView.setPadding(0, dp(10), 0, 0);
            wrapper.addView(emptyView);
            return wrapper;
        }

        long maxDuration = items.get(0).getDurationMillis();
        int count = Math.min(items.size(), 3);
        for (int i = 0; i < count; i++) {
            wrapper.addView(createStatRow(items.get(i), maxDuration, includeIcon));
        }
        return wrapper;
    }

    private void bindSection(LinearLayout container, List<FocusStatsSnapshot.StatItem> items, boolean includeIcon) {
        container.removeAllViews();
        if (items == null || items.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("표시할 기록이 없습니다.");
            emptyView.setPadding(dp(12), dp(12), dp(12), dp(12));
            container.addView(emptyView);
            return;
        }

        long maxDuration = items.get(0).getDurationMillis();
        int count = Math.min(items.size(), 5);
        for (int i = 0; i < count; i++) {
            container.addView(createStatRow(items.get(i), maxDuration, includeIcon));
        }
    }

    private LinearLayout createStatRow(FocusStatsSnapshot.StatItem item, long maxDuration, boolean includeIcon) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(10), dp(10), dp(10), dp(10));

        LinearLayout labelRow = new LinearLayout(this);
        labelRow.setOrientation(LinearLayout.HORIZONTAL);
        labelRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        if (includeIcon) {
            ImageView iconView = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
            iconParams.rightMargin = dp(8);
            iconView.setLayoutParams(iconParams);
            Drawable icon = AppDisplayHelper.resolveAppIcon(this, item.getPackageName());
            iconView.setImageDrawable(icon);
            labelRow.addView(iconView);
        }

        TextView label = new TextView(this);
        String displayLabel = includeIcon ? AppDisplayHelper.resolveAppName(this, item.getPackageName(), item.getLabel()) : item.getLabel();
        label.setText(displayLabel + " · " + DurationFormatter.formatShortDuration(item.getDurationMillis()));
        label.setTextSize(15f);
        labelRow.addView(label);
        row.addView(labelRow);

        LinearLayout barTrack = new LinearLayout(this);
        LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(10));
        trackParams.topMargin = dp(8);
        barTrack.setLayoutParams(trackParams);
        barTrack.setBackgroundColor(getColor(R.color.flow_line));

        View barFill = new View(this);
        float ratio = maxDuration <= 0L ? 0f : (float) item.getDurationMillis() / (float) maxDuration;
        int baseWidth = Math.round(getResources().getDisplayMetrics().widthPixels * 0.55f);
        LinearLayout.LayoutParams fillParams = new LinearLayout.LayoutParams(Math.max(dp(8), (int) (baseWidth * ratio)), dp(10));
        barFill.setLayoutParams(fillParams);
        barFill.setBackgroundColor(getColor(R.color.flow_sky));
        barTrack.addView(barFill);
        row.addView(barTrack);

        return row;
    }

    private void openDetail(String type) {
        if (snapshot == null) {
            return;
        }
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        ArrayList<String> packages = new ArrayList<>();
        List<FocusStatsSnapshot.StatItem> source;
        String title;
        if (FocusStatsDetailActivity.TYPE_PERIOD.equals(type)) {
            source = getSelectedPeriodItems();
            title = getPeriodTitle();
        } else if (FocusStatsDetailActivity.TYPE_HOURLY.equals(type)) {
            source = snapshot.getHourlyItems();
            title = "시간대별 통계";
        } else {
            source = snapshot.getAppItems();
            title = "앱별 통계";
        }
        for (FocusStatsSnapshot.StatItem item : source) {
            labels.add(item.getLabel());
            values.add(DurationFormatter.formatDuration(item.getDurationMillis()));
            packages.add(item.getPackageName() == null ? "" : item.getPackageName());
        }
        Intent intent = new Intent(this, FocusStatsDetailActivity.class);
        intent.putExtra(FocusStatsDetailActivity.EXTRA_TYPE, type);
        intent.putExtra(FocusStatsDetailActivity.EXTRA_TITLE, title);
        intent.putStringArrayListExtra(FocusStatsDetailActivity.EXTRA_LABELS, labels);
        intent.putStringArrayListExtra(FocusStatsDetailActivity.EXTRA_VALUES, values);
        intent.putStringArrayListExtra(FocusStatsDetailActivity.EXTRA_PACKAGES, packages);
        startActivity(intent);
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
