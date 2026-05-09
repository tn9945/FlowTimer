package com.example.flowtimer.focus;

import android.content.Context;

import com.example.flowtimer.data.AppDatabase;
import com.example.flowtimer.data.AppUsageRecordDao;
import com.example.flowtimer.data.AppUsageRecordEntity;
import com.example.flowtimer.data.FocusSessionDao;
import com.example.flowtimer.data.FocusSessionEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FocusBackupManager {

    private static final String FILE_NAME = "focus_backup.json";

    private final FocusSessionDao focusSessionDao;
    private final AppUsageRecordDao appUsageRecordDao;
    private final File backupFile;

    public FocusBackupManager(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        focusSessionDao = database.focusSessionDao();
        appUsageRecordDao = database.appUsageRecordDao();
        backupFile = new File(context.getFilesDir(), FILE_NAME);
    }

    public boolean exportUserData(String userId) {
        try {
            JSONObject root = new JSONObject();
            root.put("userId", userId);
            root.put("createdAt", System.currentTimeMillis());

            JSONArray sessionArray = new JSONArray();
            List<FocusSessionEntity> sessions = focusSessionDao.findAllByUserId(userId);
            for (FocusSessionEntity session : sessions) {
                JSONObject item = new JSONObject();
                item.put("id", session.getId());
                item.put("userId", session.getUserId());
                item.put("userName", session.getUserName());
                item.put("startTimeMillis", session.getStartTimeMillis());
                item.put("endTimeMillis", session.getEndTimeMillis());
                item.put("totalDurationMillis", session.getTotalDurationMillis());
                item.put("activeDurationMillis", session.getActiveDurationMillis());
                item.put("breakDurationMillis", session.getBreakDurationMillis());
                item.put("studyDurationMillis", session.getStudyDurationMillis());
                item.put("distractionDurationMillis", session.getDistractionDurationMillis());
                item.put("neutralDurationMillis", session.getNeutralDurationMillis());
                item.put("effectiveFocusDurationMillis", session.getEffectiveFocusDurationMillis());
                item.put("focusScore", session.getFocusScore());
                item.put("rewardCoin", session.getRewardCoin());
                item.put("rewardExp", session.getRewardExp());
                item.put("rewardMinutes", session.getRewardMinutes());
                item.put("appSwitchCount", session.getAppSwitchCount());
                item.put("appCount", session.getAppCount());
                item.put("topAppName", session.getTopAppName());
                item.put("topAppDurationMillis", session.getTopAppDurationMillis());
                sessionArray.put(item);
            }
            root.put("sessions", sessionArray);

            JSONArray recordArray = new JSONArray();
            List<AppUsageRecordEntity> records = appUsageRecordDao.findAllByUserId(userId);
            for (AppUsageRecordEntity record : records) {
                JSONObject item = new JSONObject();
                item.put("sessionId", record.getSessionId());
                item.put("userId", record.getUserId());
                item.put("packageName", record.getPackageName());
                item.put("appName", record.getAppName());
                item.put("durationMillis", record.getDurationMillis());
                item.put("launchCount", record.getLaunchCount());
                item.put("category", record.getCategory());
                recordArray.put(item);
            }
            root.put("records", recordArray);

            FileOutputStream outputStream = new FileOutputStream(backupFile, false);
            outputStream.write(root.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean importUserData(String userId) {
        if (!backupFile.exists()) {
            return false;
        }
        try {
            byte[] bytes = new byte[(int) backupFile.length()];
            FileInputStream inputStream = new FileInputStream(backupFile);
            int read = inputStream.read(bytes);
            inputStream.close();
            if (read <= 0) {
                return false;
            }
            String content = new String(bytes, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(content);
            JSONArray sessions = root.optJSONArray("sessions");
            JSONArray records = root.optJSONArray("records");

            appUsageRecordDao.deleteByUserId(userId);
            focusSessionDao.deleteByUserId(userId);

            Map<Long, Long> sessionIdMap = new HashMap<>();
            if (sessions != null) {
                for (int i = 0; i < sessions.length(); i++) {
                    JSONObject item = sessions.optJSONObject(i);
                    if (item == null) {
                        continue;
                    }
                    FocusSessionEntity session = new FocusSessionEntity(
                            userId,
                            item.optString("userName", ""),
                            item.optLong("startTimeMillis", 0L),
                            item.optLong("endTimeMillis", 0L),
                            item.optLong("totalDurationMillis", 0L),
                            item.optLong("activeDurationMillis", 0L),
                            item.optLong("breakDurationMillis", 0L),
                            item.optLong("studyDurationMillis", 0L),
                            item.optLong("distractionDurationMillis", 0L),
                            item.optLong("neutralDurationMillis", 0L),
                            item.optLong("effectiveFocusDurationMillis", 0L),
                            item.optInt("focusScore", 0),
                            item.optInt("rewardCoin", 0),
                            item.optInt("rewardExp", 0),
                            item.optInt("rewardMinutes", 0),
                            item.optInt("appSwitchCount", 0),
                            item.optInt("appCount", 0),
                            item.optString("topAppName", "기록 없음"),
                            item.optLong("topAppDurationMillis", 0L)
                    );
                    long newId = focusSessionDao.insert(session);
                    sessionIdMap.put(item.optLong("id", -1L), newId);
                }
            }
            if (records != null) {
                for (int i = 0; i < records.length(); i++) {
                    JSONObject item = records.optJSONObject(i);
                    if (item == null) {
                        continue;
                    }
                    long oldSessionId = item.optLong("sessionId", -1L);
                    long newSessionId = sessionIdMap.getOrDefault(oldSessionId, -1L);
                    if (newSessionId <= 0L) {
                        continue;
                    }
                    AppUsageRecordEntity record = new AppUsageRecordEntity(
                            newSessionId,
                            userId,
                            item.optString("packageName", ""),
                            item.optString("appName", ""),
                            item.optLong("durationMillis", 0L),
                            item.optInt("launchCount", 0),
                            item.optString("category", FocusCategory.NEUTRAL)
                    );
                    appUsageRecordDao.insert(record);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasBackup() {
        return backupFile.exists() && backupFile.length() > 0L;
    }

    public long getLastBackupTimeMillis() {
        if (!backupFile.exists()) {
            return 0L;
        }
        return backupFile.lastModified();
    }
}
