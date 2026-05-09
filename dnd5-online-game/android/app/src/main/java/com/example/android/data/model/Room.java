package com.example.android.data.model;

public class Room {
    public String code;
    public String masterName;
    public int playersCount;
    public int maxPlayers;
    public boolean isPublic;

    public Room(String code, String masterName, int playersCount, int maxPlayers, boolean isPublic) {
        this.code = code;
        this.masterName = masterName;
        this.playersCount = playersCount;
        this.maxPlayers = maxPlayers;
        this.isPublic = isPublic;
    }
}
