package com.example.android.net.api;

import com.example.android.net.dto.CharacterDtos;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CharactersApi {

    class CharactersListResponse {
        public List<CharacterDtos.CharacterResponse> characters;
    }

    // ---- Templates (account-scoped) ----

    @GET("characters")
    Call<CharactersListResponse> listTemplates();

    @POST("characters")
    Call<CharacterDtos.CharacterResponse> createTemplate(@Body CharacterDtos.CharacterUpsertRequest body);

    @GET("characters/{characterId}")
    Call<CharacterDtos.CharacterResponse> getTemplate(@Path("characterId") String characterId);

    @HTTP(method = "PATCH", path = "characters/{characterId}", hasBody = true)
    Call<CharacterDtos.CharacterResponse> updateTemplate(@Path("characterId") String characterId,
                                                        @Body CharacterDtos.CharacterUpsertRequest body);

    @DELETE("characters/{characterId}")
    Call<Void> deleteTemplate(@Path("characterId") String characterId);

    // ---- Room-scoped characters ----

    @GET("rooms/{roomId}/characters")
    Call<CharactersListResponse> listInRoom(@Path("roomId") String roomId);

    @POST("rooms/{roomId}/characters")
    Call<CharacterDtos.CharacterResponse> createInRoom(@Path("roomId") String roomId,
                                                      @Body CharacterDtos.CharacterUpsertRequest body);

    @GET("rooms/{roomId}/characters/{characterId}")
    Call<CharacterDtos.CharacterResponse> getInRoom(@Path("roomId") String roomId,
                                                   @Path("characterId") String characterId);

    @HTTP(method = "PATCH", path = "rooms/{roomId}/characters/{characterId}", hasBody = true)
    Call<CharacterDtos.CharacterResponse> updateInRoom(@Path("roomId") String roomId,
                                                      @Path("characterId") String characterId,
                                                      @Body CharacterDtos.CharacterUpsertRequest body);

    @DELETE("rooms/{roomId}/characters/{characterId}")
    Call<Void> deleteInRoom(@Path("roomId") String roomId,
                            @Path("characterId") String characterId);
}
