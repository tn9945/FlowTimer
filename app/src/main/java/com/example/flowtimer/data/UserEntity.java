package com.example.flowtimer.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {@Index(value = {"userId"}, unique = true)}
)
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String userId;
    private String password;

    public UserEntity(String name, String userId, String password) {
        this.name = name;
        this.userId = userId;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
