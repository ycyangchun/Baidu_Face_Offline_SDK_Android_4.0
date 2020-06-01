package com.baidu.idl.face.main.socket.socketmodel.response;

public class ResponseDeleteRecords {
    // 设备指纹
    private String deviceId;
    // 删除的记录总条数
    private String num;
    // "result：1"，删除成功，0 为失败
    private String result;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
