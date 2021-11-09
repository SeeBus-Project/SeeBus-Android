package com.opensource.seebus.sendDeviceInfo;

public class SendDeviceInfoResponseDto {
    public String androidId;
    public String firebaseToken;
    public String id;

    public SendDeviceInfoResponseDto(String androidId, String firebaseToken,String id) {
        this.androidId=androidId;
        this.firebaseToken=firebaseToken;
        this.id=id;
    }
}
