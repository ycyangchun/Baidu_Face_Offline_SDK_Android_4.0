package com.baidu.idl.face.main.socket.socketmodel.request;

public class GetGroupList {
    // 设备指纹
    private String deviceId;
    // 默认值0，起始序号
    private String start;
    // 返回数量，默认值100，最大值1000
    private String length;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }
}
