package com.opensource.seebus.sendGpsInfo;

public class SendGpsInfoRequestDto {
    String androidId;   // 안드로이드 ID
    double latitude;    // gps 위도
    double longitude;   // gps 경도

    public SendGpsInfoRequestDto(String androidId, double latitude, double longitude) {
        this.androidId = androidId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
