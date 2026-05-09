package com.example.flowtimer.focus;

import com.example.flowtimer.data.AppUsageRecordEntity;
import com.example.flowtimer.data.FocusSessionEntity;

import java.util.ArrayList;
import java.util.List;

public class FocusSessionBundle {

    private final FocusSessionEntity session;
    private final List<AppUsageRecordEntity> appRecords;

    public FocusSessionBundle(FocusSessionEntity session, List<AppUsageRecordEntity> appRecords) {
        this.session = session;
        this.appRecords = appRecords == null ? new ArrayList<>() : appRecords;
    }

    public FocusSessionEntity getSession() {
        return session;
    }

    public List<AppUsageRecordEntity> getAppRecords() {
        return appRecords;
    }
}
