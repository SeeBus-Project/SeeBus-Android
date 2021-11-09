package com.opensource.seebus.sendDeviceInfo;

public class SendDeviceInfoRequestDto {
    String androidId;
    String firebaseToken;

    public SendDeviceInfoRequestDto(String androidId, String firebaseToken) {
        this.androidId=androidId;
        this.firebaseToken=firebaseToken;
    }
}
