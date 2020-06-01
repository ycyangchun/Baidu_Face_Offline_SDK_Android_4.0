package com.baidu.idl.face.main.socket.socketmodel.request;

public class CleanLog {
    // 设备指纹
    private String deviceId;
    // 数字，表示隔多长时间清理一次log，参数可选择「none」，或者0到86400的数字，仅支持整数。如选择「none」，则不清理日志。
    private String hour;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }
}
