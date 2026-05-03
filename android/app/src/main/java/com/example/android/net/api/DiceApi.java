package com.example.android.net.api;

import com.example.android.net.dto.DiceDtos;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DiceApi {

    @POST("rooms/{roomId}/dice/roll")
    Call<DiceDtos.DiceRollResponse> roll(@Path("roomId") String roomId,
                                         @Body DiceDtos.DiceRollRequest body);
}
