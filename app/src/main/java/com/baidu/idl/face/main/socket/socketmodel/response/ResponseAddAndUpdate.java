package com.baidu.idl.face.main.socket.socketmodel.response;

public class ResponseAddAndUpdate {
    // 设备指纹
    private String deviceId;
    // 人脸唯一标识
    private String faceToken;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFaceToken() {
        return faceToken;
    }

    public void setFaceToken(String faceToken) {
        this.faceToken = faceToken;
    }
}
