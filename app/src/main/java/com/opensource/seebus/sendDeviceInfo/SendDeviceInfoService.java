package com.opensource.seebus.sendDeviceInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SendDeviceInfoService {
    @Headers("Content-Type: application/json")
    @POST("device")
    Call<SendDeviceInfoResponseDto> requestSendDevice(@Body SendDeviceInfoRequestDto body);
}