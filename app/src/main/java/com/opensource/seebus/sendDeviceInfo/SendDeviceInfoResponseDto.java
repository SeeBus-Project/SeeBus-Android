package com.opensource.seebus.sendDeviceInfo;

public class SendDeviceInfoResponseDto {
    public String androidId;
    public String firebaseToken;
    public String id;
    public Boolean isArrived; //사용자가 목적지에 도착했을때 푸시알림 발송여부

    public SendDeviceInfoResponseDto(String androidId, String firebaseToken,String id) {
        this.androidId=androidId;
        this.firebaseToken=firebaseToken;
        this.id=id;
        this.isArrived=isArrived;
    }
}
