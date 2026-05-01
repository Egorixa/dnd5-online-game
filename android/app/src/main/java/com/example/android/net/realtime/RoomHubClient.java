package com.example.android.net.realtime;

import android.util.Log;

import com.example.android.net.ApiConfig;
import com.google.gson.JsonObject;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

/**
 * Тонкая обёртка над SignalR HubConnection для подключения к /hubs/session
 * (см. backend/RealTime/Hubs/RoomHub.cs).
 *
 * Жизненный цикл:
 *   - new RoomHubClient(token, listener)
 *   - start(roomId) — connect + JoinRoom
 *   - stop()        — LeaveRoom + disconnect
 *
 * События приходят в Listener.* колбэках; payload — сырой JSON-объект.
 * Парсинг конкретных полей делает потребитель.
 */
public class RoomHubClient {

    public interface Listener {
        void onConnectionStateChanged(boolean connected, String message);
        void onRoomUpdated(JsonObject payload);
        void onCharacterUpdated(JsonObject payload);
        void onDiceRolled(JsonObject payload);
        void onParticipantJoined(JsonObject payload);
        void onParticipantLeft(JsonObject payload);
    }

    private static final String TAG = "RoomHubClient";

    private final String token;
    private final Listener listener;
    private HubConnection connection;
    private String currentRoomId;

    public RoomHubClient(String token, Listener listener) {
        this.token = token;
        this.listener = listener;
    }

    public void start(String roomId) {
        this.currentRoomId = roomId;
        try {
            connection = HubConnectionBuilder.create(ApiConfig.HUB_URL)
                    .withAccessTokenProvider(io.reactivex.rxjava3.core.Single.defer(() ->
                            io.reactivex.rxjava3.core.Single.just(token == null ? "" : token)))
                    .build();

            connection.on("room.updated", payload -> safe(() -> listener.onRoomUpdated(payload)),
                    JsonObject.class);
            connection.on("character.updated", payload -> safe(() -> listener.onCharacterUpdated(payload)),
                    JsonObject.class);
            connection.on("dice.rolled", payload -> safe(() -> listener.onDiceRolled(payload)),
                    JsonObject.class);
            connection.on("participant.joined", payload -> safe(() -> listener.onParticipantJoined(payload)),
                    JsonObject.class);
            connection.on("participant.left", payload -> safe(() -> listener.onParticipantLeft(payload)),
                    JsonObject.class);

            connection.onClosed(ex -> {
                String msg = ex != null ? ex.getMessage() : "соединение закрыто";
                safe(() -> listener.onConnectionStateChanged(false, msg));
            });

            connection.start()
                    .doOnComplete(() -> {
                        safe(() -> listener.onConnectionStateChanged(true, "connected"));
                        if (currentRoomId != null && !currentRoomId.isEmpty()) {
                            connection.invoke("JoinRoom", java.util.UUID.fromString(currentRoomId))
                                    .subscribe(() -> {}, err ->
                                            Log.e(TAG, "JoinRoom failed", err));
                        }
                    })
                    .subscribe(() -> {}, err -> {
                        Log.e(TAG, "Hub start failed", err);
                        safe(() -> listener.onConnectionStateChanged(false,
                                err.getMessage() != null ? err.getMessage() : "ошибка подключения"));
                    });
        } catch (Throwable t) {
            Log.e(TAG, "init failed", t);
            safe(() -> listener.onConnectionStateChanged(false, t.getMessage()));
        }
    }

    public void stop() {
        if (connection == null) return;
        try {
            if (connection.getConnectionState() == HubConnectionState.CONNECTED && currentRoomId != null) {
                connection.invoke("LeaveRoom", java.util.UUID.fromString(currentRoomId))
                        .subscribe(() -> {}, err -> Log.w(TAG, "LeaveRoom failed", err));
            }
            connection.stop().subscribe(() -> {}, err -> Log.w(TAG, "stop failed", err));
        } catch (Throwable t) {
            Log.w(TAG, "stop error", t);
        }
        connection = null;
    }

    public boolean isConnected() {
        return connection != null && connection.getConnectionState() == HubConnectionState.CONNECTED;
    }

    private void safe(Runnable r) {
        try { r.run(); } catch (Throwable t) { Log.w(TAG, "listener error", t); }
    }
}
