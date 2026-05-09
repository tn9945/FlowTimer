package com.example.flowtimer.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AppUsageRecordDao {

    @Insert
    void insertAll(List<AppUsageRecordEntity> records);

    @Insert
    long insert(AppUsageRecordEntity record);

    @Query("SELECT * FROM app_usage_records WHERE sessionId = :sessionId ORDER BY durationMillis DESC")
    List<AppUsageRecordEntity> findBySessionId(long sessionId);

    @Query("SELECT * FROM app_usage_records WHERE userId = :userId ORDER BY durationMillis DESC")
    List<AppUsageRecordEntity> findAllByUserId(String userId);

    @Query("DELETE FROM app_usage_records WHERE userId = :userId")
    void deleteByUserId(String userId);
}
