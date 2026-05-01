package com.example.android.net.api;

import com.example.android.net.dto.RoomDtos;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RoomsApi {

    class PublicRoomsResponse {
        public List<RoomDtos.PublicRoomDto> rooms;
    }

    class RoomEventsResponse {
        public List<RoomDtos.RoomEventDto> events;
    }

    @POST("rooms")
    Call<RoomDtos.CreateRoomResponse> create(@Body RoomDtos.CreateRoomRequest body);

    @GET("rooms/public")
    Call<PublicRoomsResponse> getPublic(@Query("limit") int limit, @Query("offset") int offset);

    @POST("rooms/{roomCode}/join")
    Call<RoomDtos.JoinRoomResponse> join(@Path("roomCode") String roomCode);

    @POST("rooms/{roomId}/leave")
    Call<Void> leave(@Path("roomId") String roomId);

    @POST("rooms/{roomId}/kick")
    Call<Void> kick(@Path("roomId") String roomId, @Body RoomDtos.KickParticipantRequest body);

    @POST("rooms/{roomId}/finish")
    Call<Void> finish(@Path("roomId") String roomId, @Body RoomDtos.FinishRoomRequest body);

    @GET("rooms/{roomId}/events")
    Call<RoomEventsResponse> getEvents(@Path("roomId") String roomId,
                                       @Query("limit") int limit,
                                       @Query("offset") int offset);

    @GET("rooms/{roomId}/state")
    Call<RoomDtos.RoomStateDto> getState(@Path("roomId") String roomId);
}
