package com.baidu.idl.face.main.socket.socketmodel.response;

public class ResponseGetUserList {
    // 设备指纹
    private String deviceId;
    // userId列表数组
    private String[] userIdList;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String[] getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(String[] userIdList) {
        this.userIdList = userIdList;
    }
}
