package com.example.flowtimer.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert
    long insert(UserEntity user);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    UserEntity findByUserId(String userId);

    @Query("SELECT * FROM users WHERE userId = :userId AND password = :password LIMIT 1")
    UserEntity login(String userId, String password);

    @Query("DELETE FROM users WHERE userId = :userId AND password = :password")
    int deleteByUserIdAndPassword(String userId, String password);
}
