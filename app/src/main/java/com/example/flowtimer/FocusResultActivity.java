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
import com.example.flowtimer.focus.AiFocusSummary;
import com.example.flowtimer.focus.AiFocusSummaryRepository;
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
    private TextView tvAiSummary;
    private TextView tvAiWarning;
    private TextView tvAiAdvice;
    private FocusResultDonutView viewResultDonut;
    private LinearLayout layoutAppRecords;
    private final AiFocusSummaryRepository aiFocusSummaryRepository = new AiFocusSummaryRepository();

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
        tvAiSummary = findViewById(R.id.tvAiSummary);
        tvAiWarning = findViewById(R.id.tvAiWarning);
        tvAiAdvice = findViewById(R.id.tvAiAdvice);
        viewResultDonut = findViewById(R.id.viewResultDonut);
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
        long focusDuration = session.getEffectiveFocusDurationMillis() > 0L ? session.getEffectiveFocusDurationMillis() : session.getActiveDurationMillis();
        String rangeText = DurationFormatter.formatDateTime(session.getStartTimeMillis()) + " ~ " + DurationFormatter.formatDateTime(session.getEndTimeMillis());
        tvSessionRange.setText(rangeText);
        tvTotalDuration.setText("총 측정 시간: " + DurationFormatter.formatDuration(session.getTotalDurationMillis()));
        tvFocusDuration.setText("순집중 시간: " + DurationFormatter.formatDuration(focusDuration));
        tvBreakDuration.setText("휴식 시간: " + DurationFormatter.formatDuration(session.getBreakDurationMillis()));
        tvStudyDuration.setText("학습 앱 사용 시간: " + DurationFormatter.formatDuration(session.getStudyDurationMillis()));
        tvDistractionDuration.setText("방해 앱 사용 시간: " + DurationFormatter.formatDuration(session.getDistractionDurationMillis()));
        tvScore.setText("집중 점수: " + session.getFocusScore() + "점");
        tvSwitchCount.setText("앱 전환 횟수: " + session.getAppSwitchCount());
        tvReward.setText("획득 보상: 코인 " + session.getRewardCoin() + " / 경험치 " + session.getRewardExp() + " / 시간 " + session.getRewardMinutes() + "분");
        viewResultDonut.setData(focusDuration, session.getBreakDurationMillis(), session.getDistractionDurationMillis(), session.getFocusScore());

        if (records != null && !records.isEmpty()) {
            AppUsageRecordEntity topRecord = records.get(0);
            String topName = AppDisplayHelper.resolveAppName(this, topRecord.getPackageName(), topRecord.getAppName());
            tvTopApp.setText("가장 오래 사용한 앱: " + topName + " · " + DurationFormatter.formatShortDuration(topRecord.getDurationMillis()));
        } else {
            tvTopApp.setText("가장 오래 사용한 앱: 기록 없음");
        }

        bindAiSummary(session, records);
        bindAppRecords(records);
    }

    private void bindAiSummary(FocusSessionEntity session, List<AppUsageRecordEntity> records) {
        AiFocusSummary aiSummary = aiFocusSummaryRepository.createLocalSummary(this, session, records);
        tvAiSummary.setText(aiSummary.getSummaryText());
        tvAiWarning.setText(aiSummary.getWarningText());
        tvAiAdvice.setText(aiSummary.getAdviceText());
    }

    private void bindAppRecords(List<AppUsageRecordEntity> records) {
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
