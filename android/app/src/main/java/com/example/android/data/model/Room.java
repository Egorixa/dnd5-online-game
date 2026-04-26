package com.example.android.data.model;

/**
 * Модель игровой комнаты (используется как mock-объект для UI).
 */
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
