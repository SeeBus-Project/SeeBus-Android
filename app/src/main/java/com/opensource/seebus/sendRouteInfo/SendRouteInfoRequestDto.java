package com.opensource.seebus.sendRouteInfo;

public class SendRouteInfoRequestDto {
    String androidId;           // 안드로이드 ID
    String destinationArsId;    // 도착 정류소 ID
    String destinationName;     // 도착 정류소 이름
    String rtNm;                // 버스 이름
    String startArsId;          // 출발 정류소 ID

    public SendRouteInfoRequestDto(String androidId, String destinationArsId, String destinationName, String rtNm, String startArsId) {
        this.androidId = androidId;
        this.destinationArsId = destinationArsId;
        this.destinationName = destinationName;
        this.rtNm = rtNm;
        this.startArsId = startArsId;
    }
}
