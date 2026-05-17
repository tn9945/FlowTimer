package com.example.flowtimer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowtimer.focus.DurationFormatter;
import com.example.flowtimer.focus.FocusQuestManager;
import com.example.flowtimer.focus.FocusRepository;
import com.example.flowtimer.focus.FocusStatsSnapshot;

public class WeeklyReportActivity extends AppCompatActivity {

    private TextView tvWeeklyReport;
    private Button btnGoStats;
    private Button btnGoMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_report);

        tvWeeklyReport = findViewById(R.id.tvWeeklyReport);
        btnGoStats = findViewById(R.id.btnGoStats);
        btnGoMain = findViewById(R.id.btnGoMain);

        SessionManager sessionManager = new SessionManager(this);
        FocusRepository repository = new FocusRepository(this);
        repository.loadStats(sessionManager.getUserIdentifier(), this::bindReport);

        btnGoStats.setOnClickListener(v -> startActivity(new Intent(this, FocusStatsActivity.class)));
        btnGoMain.setOnClickListener(v -> finish());
    }

    private void bindReport(FocusStatsSnapshot snapshot) {
        FocusQuestManager questManager = new FocusQuestManager(this);
        String bestDay = "기록 없음";
        long bestFocus = 0L;
        long totalWeekFocus = 0L;
        long totalWeekDistraction = 0L;
        for (FocusStatsSnapshot.DailyFocusChartItem item : snapshot.getRecentDailyChartItems()) {
            totalWeekFocus += item.getEffectiveFocusDurationMillis();
            totalWeekDistraction += item.getDistractionDurationMillis();
            if (item.getEffectiveFocusDurationMillis() > bestFocus) {
                bestFocus = item.getEffectiveFocusDurationMillis();
                bestDay = item.getLabel();
            }
        }
        String report = "이번 주 집중 요약\n\n"
                + "총 실제 집중 시간: " + DurationFormatter.formatShortDuration(totalWeekFocus) + "\n"
                + "총 방해 시간: " + DurationFormatter.formatShortDuration(totalWeekDistraction) + "\n"
                + "가장 집중이 잘 된 날: " + bestDay + "\n"
                + "현재 " + questManager.getStreakText() + "\n"
                + "오늘 차단된 앱 실행 시도: " + questManager.getTodayBlockedCount() + "회\n\n"
                + createAdvice(totalWeekFocus, totalWeekDistraction);
        tvWeeklyReport.setText(report);
    }

    private String createAdvice(long focusMillis, long distractionMillis) {
        if (focusMillis <= 0L) {
            return "이번 주 집중 기록이 아직 부족합니다. 15분 목표부터 시작해 보는 것이 좋습니다.";
        }
        if (distractionMillis > focusMillis / 2L) {
            return "방해 시간이 높은 편입니다. 다음 집중에서는 강제 집중 모드와 허용 앱 제한을 사용하는 것이 좋습니다.";
        }
        if (distractionMillis == 0L) {
            return "방해 시간이 거의 없습니다. 현재 집중 환경을 유지하는 것이 좋습니다.";
        }
        return "집중 흐름은 유지되고 있습니다. 다음 주에는 목표 시간을 조금 늘려 보는 것이 좋습니다.";
    }
}
