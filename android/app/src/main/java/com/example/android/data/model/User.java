package com.example.android.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String username;

    @NonNull
    public String password;

    public int gamesPlayed;
    public int gamesWon;
    public int gamesLost;
    public int timesMaster;
    public long registrationDate;

    public User(@NonNull String username, @NonNull String password) {
        this.username = username;
        this.password = password;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.timesMaster = 0;
        this.registrationDate = System.currentTimeMillis();
    }
}
