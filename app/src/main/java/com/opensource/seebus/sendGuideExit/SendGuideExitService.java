package com.opensource.seebus.sendGuideExit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SendGuideExitService {
    @Headers("Content-Type: application/json")
    @POST("exit")
    Call<Void> requestSendGps(@Body SendGuideExitRequestDto body); // 받을 응답 없어서 Call<Void>
}
