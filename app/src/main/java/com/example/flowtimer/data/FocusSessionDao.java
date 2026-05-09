package com.example.flowtimer.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FocusSessionDao {

    @Insert
    long insert(FocusSessionEntity session);

    @Query("SELECT * FROM focus_sessions WHERE id = :sessionId LIMIT 1")
    FocusSessionEntity findById(long sessionId);

    @Query("SELECT * FROM focus_sessions WHERE userId = :userId ORDER BY startTimeMillis DESC")
    List<FocusSessionEntity> findAllByUserId(String userId);

    @Query("DELETE FROM focus_sessions WHERE userId = :userId")
    void deleteByUserId(String userId);
}
