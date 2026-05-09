package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.data.AppUsageRecordEntity;
import com.example.flowtimer.data.FocusSessionEntity;
import com.example.flowtimer.focus.AppDisplayHelper;
import com.example.flowtimer.focus.DurationFormatter;
import com.example.flowtimer.focus.FocusCategory;
import com.example.flowtimer.focus.FocusRepository;

import java.util.List;

public class FocusResultActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION_ID = "extra_session_id";

    private TextView tvSessionRange;
    private TextView tvTotalDuration;
    private TextView tvFocusDuration;
    private TextView tvBreakDuration;
    private TextView tvStudyDuration;
    private TextView tvDistractionDuration;
    private TextView tvScore;
    private TextView tvSwitchCount;
    private TextView tvTopApp;
    private TextView tvReward;
    private LinearLayout layoutAppRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_result);

        tvSessionRange = findViewById(R.id.tvSessionRange);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        tvFocusDuration = findViewById(R.id.tvFocusDuration);
        tvBreakDuration = findViewById(R.id.tvBreakDuration);
        tvStudyDuration = findViewById(R.id.tvStudyDuration);
        tvDistractionDuration = findViewById(R.id.tvDistractionDuration);
        tvScore = findViewById(R.id.tvScore);
        tvSwitchCount = findViewById(R.id.tvSwitchCount);
        tvTopApp = findViewById(R.id.tvTopApp);
        tvReward = findViewById(R.id.tvReward);
        layoutAppRecords = findViewById(R.id.layoutAppRecords);

        Button btnGoMain = findViewById(R.id.btnGoMain);
        btnGoMain.setOnClickListener(v -> openMain());

        long sessionId = getIntent().getLongExtra(EXTRA_SESSION_ID, -1L);
        if (sessionId <= 0L) {
            finish();
            return;
        }

        FocusRepository repository = new FocusRepository(this);
        repository.loadSessionDetail(sessionId, this::bindResult);
    }

    private void bindResult(FocusSessionEntity session, List<AppUsageRecordEntity> records) {
        if (session == null) {
            finish();
            return;
        }
        String rangeText = DurationFormatter.formatDateTime(session.getStartTimeMillis()) + " ~ " + DurationFormatter.formatDateTime(session.getEndTimeMillis());
        tvSessionRange.setText(rangeText);
        tvTotalDuration.setText(DurationFormatter.formatDuration(session.getTotalDurationMillis()));
        tvFocusDuration.setText(DurationFormatter.formatDuration(session.getEffectiveFocusDurationMillis() > 0L ? session.getEffectiveFocusDurationMillis() : session.getActiveDurationMillis()));
        tvBreakDuration.setText(DurationFormatter.formatDuration(session.getBreakDurationMillis()));
        tvStudyDuration.setText(DurationFormatter.formatDuration(session.getStudyDurationMillis()));
        tvDistractionDuration.setText(DurationFormatter.formatDuration(session.getDistractionDurationMillis()));
        tvScore.setText(session.getFocusScore() + "점");
        tvSwitchCount.setText(String.valueOf(session.getAppSwitchCount()));
        tvReward.setText("코인 " + session.getRewardCoin() + " / 경험치 " + session.getRewardExp() + " / 시간 " + session.getRewardMinutes() + "분");

        if (records != null && !records.isEmpty()) {
            AppUsageRecordEntity topRecord = records.get(0);
            String topName = AppDisplayHelper.resolveAppName(this, topRecord.getPackageName(), topRecord.getAppName());
            tvTopApp.setText(topName + " · " + DurationFormatter.formatShortDuration(topRecord.getDurationMillis()));
        } else {
            tvTopApp.setText("기록 없음 · 00분 00초");
        }

        layoutAppRecords.removeAllViews();
        if (records == null || records.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("기록된 앱 사용 내역이 없습니다.");
            emptyView.setTextSize(15f);
            int padding = dp(12);
            emptyView.setPadding(padding, padding, padding, padding);
            layoutAppRecords.addView(emptyView);
            return;
        }
        for (AppUsageRecordEntity record : records) {
            layoutAppRecords.addView(createRecordRow(record));
        }
    }

    private LinearLayout createRecordRow(AppUsageRecordEntity record) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        int padding = dp(12);
        row.setPadding(padding, padding, padding, padding);

        ImageView iconView = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(28), dp(28));
        iconParams.rightMargin = dp(10);
        iconView.setLayoutParams(iconParams);
        iconView.setImageDrawable(AppDisplayHelper.resolveAppIcon(this, record.getPackageName()));
        row.addView(iconView);

        TextView textView = new TextView(this);
        String appName = AppDisplayHelper.resolveAppName(this, record.getPackageName(), record.getAppName());
        textView.setText(appName + " · " + toCategoryLabel(record.getCategory()) + " · " + DurationFormatter.formatShortDuration(record.getDurationMillis()) + " · 실행 " + record.getLaunchCount() + "회");
        textView.setTextSize(15f);
        row.addView(textView);
        return row;
    }

    private String toCategoryLabel(String category) {
        if (FocusCategory.STUDY.equals(category)) {
            return "학습";
        }
        if (FocusCategory.DISTRACTION.equals(category)) {
            return "방해";
        }
        return "중립";
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
