package com.opensource.seebus.sendGpsInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SendGpsInfoService {
    @Headers("Content-Type: application/json")
    @POST("location")
    Call<SendGpsInfoResponseDto> requestSendGps(@Body SendGpsInfoRequestDto body); // 받을 응답 없어서 Call<Void>
}
