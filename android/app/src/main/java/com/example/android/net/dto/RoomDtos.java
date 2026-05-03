package com.example.android.net.dto;

import java.util.List;

/** DTO для эндпоинтов /rooms/* (Rooms модуль бекенда). */
public class RoomDtos {

    private RoomDtos() {}

    // ----- enums (передаются как строки) -----
    public static final class AccessMode {
        public static final String PUBLIC = "PUBLIC";
        public static final String PRIVATE = "PRIVATE";
        private AccessMode() {}
    }

    public static final class RoomStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String PAUSED = "PAUSED";
        public static final String FINISHED = "FINISHED";
        private RoomStatus() {}
    }

    public static final class ParticipantRole {
        public static final String MASTER = "MASTER";
        public static final String PLAYER = "PLAYER";
        private ParticipantRole() {}
    }

    // По ТЗ (3.1, 4.3.4) мобильное приложение — только для игрока.
    // CreateRoomRequest/Response, KickParticipantRequest, FinishRoomRequest
    // относятся к мастерскому функционалу веб-интерфейса и сюда не входят.

    public static class PublicRoomDto {
        public String roomId;
        public String roomCode;
        public String name;
        public String masterId;
        public String masterUsername;
        public int playersCount;
        public String createdAt;
    }

    public static class JoinRoomResponse {
        public String roomId;
        public String roomCode;
        public String name;
        public String accessMode;
        public String participantId;
        public String role;
        public RoomStateDto currentState;
    }

    public static class RoomStateDto {
        public String roomId;
        public String name;
        public String status;
        public String masterId;
        public String masterUsername;
        public List<RoomParticipantDto> participants;
    }

    public static class RoomParticipantDto {
        public String participantId;
        public String userId;
        public String username;
        public String role;
        public String joinedAt;
    }

    public static class RoomEventDto {
        public String eventId;
        public String type;
        public String actorUserId;
        public String payload;
        public String createdAt;
    }
}
