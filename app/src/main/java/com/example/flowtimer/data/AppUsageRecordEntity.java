package com.example.flowtimer.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_usage_records", indices = {@Index("sessionId"), @Index("userId")})
public class AppUsageRecordEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long sessionId;
    private String userId;
    private String packageName;
    private String appName;
    private long durationMillis;
    private int launchCount;
    private String category;

    public AppUsageRecordEntity(long sessionId,
                                String userId,
                                String packageName,
                                String appName,
                                long durationMillis,
                                int launchCount,
                                String category) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.packageName = packageName;
        this.appName = appName;
        this.durationMillis = durationMillis;
        this.launchCount = launchCount;
        this.category = category;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    public long getDurationMillis() { return durationMillis; }
    public void setDurationMillis(long durationMillis) { this.durationMillis = durationMillis; }
    public int getLaunchCount() { return launchCount; }
    public void setLaunchCount(int launchCount) { this.launchCount = launchCount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
