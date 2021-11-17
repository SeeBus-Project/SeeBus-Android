package com.opensource.seebus.sendRouteInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SendRouteInfoService {
    @Headers("Content-Type: application/json")
    @POST("user")
    Call<Void> requestSendRoute(@Body SendRouteInfoRequestDto body); // 받을 응답 없어서 Call<Void>
}
