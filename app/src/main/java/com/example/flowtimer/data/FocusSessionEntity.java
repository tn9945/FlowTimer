package com.example.flowtimer.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_sessions", indices = {@Index("userId")})
public class FocusSessionEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String userId;
    private String userName;
    private long startTimeMillis;
    private long endTimeMillis;
    private long totalDurationMillis;
    private long activeDurationMillis;
    private long breakDurationMillis;
    private long studyDurationMillis;
    private long distractionDurationMillis;
    private long neutralDurationMillis;
    private long effectiveFocusDurationMillis;
    private int focusScore;
    private int rewardCoin;
    private int rewardExp;
    private int rewardMinutes;
    private int appSwitchCount;
    private int appCount;
    private String topAppName;
    private long topAppDurationMillis;

    public FocusSessionEntity(String userId,
                              String userName,
                              long startTimeMillis,
                              long endTimeMillis,
                              long totalDurationMillis,
                              long activeDurationMillis,
                              long breakDurationMillis,
                              long studyDurationMillis,
                              long distractionDurationMillis,
                              long neutralDurationMillis,
                              long effectiveFocusDurationMillis,
                              int focusScore,
                              int rewardCoin,
                              int rewardExp,
                              int rewardMinutes,
                              int appSwitchCount,
                              int appCount,
                              String topAppName,
                              long topAppDurationMillis) {
        this.userId = userId;
        this.userName = userName;
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
        this.totalDurationMillis = totalDurationMillis;
        this.activeDurationMillis = activeDurationMillis;
        this.breakDurationMillis = breakDurationMillis;
        this.studyDurationMillis = studyDurationMillis;
        this.distractionDurationMillis = distractionDurationMillis;
        this.neutralDurationMillis = neutralDurationMillis;
        this.effectiveFocusDurationMillis = effectiveFocusDurationMillis;
        this.focusScore = focusScore;
        this.rewardCoin = rewardCoin;
        this.rewardExp = rewardExp;
        this.rewardMinutes = rewardMinutes;
        this.appSwitchCount = appSwitchCount;
        this.appCount = appCount;
        this.topAppName = topAppName;
        this.topAppDurationMillis = topAppDurationMillis;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public long getStartTimeMillis() { return startTimeMillis; }
    public void setStartTimeMillis(long startTimeMillis) { this.startTimeMillis = startTimeMillis; }
    public long getEndTimeMillis() { return endTimeMillis; }
    public void setEndTimeMillis(long endTimeMillis) { this.endTimeMillis = endTimeMillis; }
    public long getTotalDurationMillis() { return totalDurationMillis; }
    public void setTotalDurationMillis(long totalDurationMillis) { this.totalDurationMillis = totalDurationMillis; }
    public long getActiveDurationMillis() { return activeDurationMillis; }
    public void setActiveDurationMillis(long activeDurationMillis) { this.activeDurationMillis = activeDurationMillis; }
    public long getBreakDurationMillis() { return breakDurationMillis; }
    public void setBreakDurationMillis(long breakDurationMillis) { this.breakDurationMillis = breakDurationMillis; }
    public long getStudyDurationMillis() { return studyDurationMillis; }
    public void setStudyDurationMillis(long studyDurationMillis) { this.studyDurationMillis = studyDurationMillis; }
    public long getDistractionDurationMillis() { return distractionDurationMillis; }
    public void setDistractionDurationMillis(long distractionDurationMillis) { this.distractionDurationMillis = distractionDurationMillis; }
    public long getNeutralDurationMillis() { return neutralDurationMillis; }
    public void setNeutralDurationMillis(long neutralDurationMillis) { this.neutralDurationMillis = neutralDurationMillis; }
    public long getEffectiveFocusDurationMillis() { return effectiveFocusDurationMillis; }
    public void setEffectiveFocusDurationMillis(long effectiveFocusDurationMillis) { this.effectiveFocusDurationMillis = effectiveFocusDurationMillis; }
    public int getFocusScore() { return focusScore; }
    public void setFocusScore(int focusScore) { this.focusScore = focusScore; }
    public int getRewardCoin() { return rewardCoin; }
    public void setRewardCoin(int rewardCoin) { this.rewardCoin = rewardCoin; }
    public int getRewardExp() { return rewardExp; }
    public void setRewardExp(int rewardExp) { this.rewardExp = rewardExp; }
    public int getRewardMinutes() { return rewardMinutes; }
    public void setRewardMinutes(int rewardMinutes) { this.rewardMinutes = rewardMinutes; }
    public int getAppSwitchCount() { return appSwitchCount; }
    public void setAppSwitchCount(int appSwitchCount) { this.appSwitchCount = appSwitchCount; }
    public int getAppCount() { return appCount; }
    public void setAppCount(int appCount) { this.appCount = appCount; }
    public String getTopAppName() { return topAppName; }
    public void setTopAppName(String topAppName) { this.topAppName = topAppName; }
    public long getTopAppDurationMillis() { return topAppDurationMillis; }
    public void setTopAppDurationMillis(long topAppDurationMillis) { this.topAppDurationMillis = topAppDurationMillis; }
}
