package com.opensource.seebus.sendGpsInfo;

public class SendGpsInfoResponseDto {
    String androidId;   // 안드로이드 ID
    boolean isArrived;    // 목적지 도착여부
    String nextStationName;   // 다음 정거장
    int remainingStationCount; // 남은 정거장 개수

    public SendGpsInfoResponseDto(String androidId,
                                  boolean isArrived,
                                  String nextStationName,
                                  int remainingStationCount) {
        this.androidId = androidId;
        this.isArrived = isArrived;
        this.nextStationName = nextStationName;
        this.remainingStationCount=remainingStationCount;
    }
}
