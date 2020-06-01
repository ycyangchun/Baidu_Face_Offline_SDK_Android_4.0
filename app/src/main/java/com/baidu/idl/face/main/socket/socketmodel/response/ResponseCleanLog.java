package com.baidu.idl.face.main.socket.socketmodel.response;

public class ResponseCleanLog {
    // 设备指纹
    private String deviceId;
    // 清理的日志条数
    private String logNum;
    // 放到log清理的时间范围，时间戳形式表示，时间范围为「上次清理执行的时间」到「这次清理执行的时间」
    private String logTimeArea;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getLogNum() {
        return logNum;
    }

    public void setLogNum(String logNum) {
        this.logNum = logNum;
    }

    public String getLogTimeArea() {
        return logTimeArea;
    }

    public void setLogTimeArea(String logTimeArea) {
        this.logTimeArea = logTimeArea;
    }
}
