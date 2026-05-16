package com.example.flowtimer.focus;

import android.content.Context;

import com.example.flowtimer.data.AppUsageRecordEntity;
import com.example.flowtimer.data.FocusSessionEntity;

import java.util.List;

public class AiFocusSummaryRepository {

    public AiFocusSummary createLocalSummary(Context context, FocusSessionEntity session, List<AppUsageRecordEntity> records) {
        if (session == null) {
            return new AiFocusSummary("집중 기록을 불러오지 못했습니다.", "분석할 집중 데이터가 없습니다.", "집중을 다시 진행한 뒤 결과를 확인해 주십시오.");
        }

        long totalDuration = session.getTotalDurationMillis();
        long distractionDuration = session.getDistractionDurationMillis();
        long studyDuration = session.getStudyDurationMillis();
        int score = session.getFocusScore();
        int switchCount = session.getAppSwitchCount();
        int distractionPercent = totalDuration > 0L ? Math.round((distractionDuration * 100f) / totalDuration) : 0;
        String topAppText = resolveTopAppText(context, records);

        String summaryText = "이번 집중 세션은 총 " + DurationFormatter.formatShortDuration(totalDuration)
                + " 동안 진행되었고, 집중 점수는 " + score + "점입니다. 학습 앱 사용 시간은 "
                + DurationFormatter.formatShortDuration(studyDuration) + ", 방해 앱 사용 시간은 "
                + DurationFormatter.formatShortDuration(distractionDuration) + "입니다.";

        String warningText = createWarningText(distractionDuration, distractionPercent, switchCount, topAppText);
        String adviceText = createAdviceText(distractionDuration, distractionPercent, switchCount);

        return new AiFocusSummary(summaryText, warningText, adviceText);
    }

    private String resolveTopAppText(Context context, List<AppUsageRecordEntity> records) {
        if (records == null || records.isEmpty()) {
            return "기록된 앱 없음";
        }
        AppUsageRecordEntity topRecord = records.get(0);
        String appName = AppDisplayHelper.resolveAppName(context, topRecord.getPackageName(), topRecord.getAppName());
        return appName + "(" + DurationFormatter.formatShortDuration(topRecord.getDurationMillis()) + ")";
    }

    private String createWarningText(long distractionDuration, int distractionPercent, int switchCount, String topAppText) {
        if (distractionDuration <= 0L && switchCount <= 1) {
            return "이번 세션에서는 집중을 크게 방해한 앱 사용이 거의 확인되지 않았습니다.";
        }
        if (distractionPercent >= 40) {
            return "방해 앱 사용 비율이 " + distractionPercent + "%로 높게 나타났습니다. 특히 가장 오래 사용한 앱은 " + topAppText + "입니다.";
        }
        if (switchCount >= 10) {
            return "앱 전환 횟수가 " + switchCount + "회로 많아 집중 흐름이 자주 끊겼을 가능성이 있습니다.";
        }
        return "일부 방해 앱 사용이 확인되었습니다. 가장 오래 사용한 앱은 " + topAppText + "입니다.";
    }

    private String createAdviceText(long distractionDuration, int distractionPercent, int switchCount) {
        if (distractionDuration <= 0L && switchCount <= 1) {
            return "현재 집중 패턴을 유지하는 것이 좋습니다. 다음 세션에서도 동일한 환경을 유지해 보십시오.";
        }
        if (distractionPercent >= 40) {
            return "다음 집중에서는 상호작용 금지 모드를 사용하거나, 집중 시작 전 방해 가능성이 높은 앱 알림을 비활성화하는 것이 좋습니다.";
        }
        if (switchCount >= 10) {
            return "앱 전환을 줄이기 위해 필요한 앱만 미리 열어 두고, 집중 중에는 화면 이동을 최소화하는 것이 좋습니다.";
        }
        return "방해 앱 사용 시간이 더 늘어나지 않도록 다음 세션에서는 허용 앱을 줄이거나 짧은 집중 시간부터 다시 시작하는 것이 좋습니다.";
    }
}
