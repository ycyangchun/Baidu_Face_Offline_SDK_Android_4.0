package com.baidu.idl.face.main.socket.socketmodel.request;

public class GetRecords {
    // 设备指纹
    private String deviceId;
    // 指定具体用户，若不指定，则会拉取选定时间段内所有用户的所有记录
    private String userId;
    // 按照具体时间形式传入，如 2019-07-15 12:05:00
    private String startTime;
    // 按照具体时间形式传入，如 2019-10-20 22:20:00
    private String endTime;
    // 默认返回所有识别结果。如传入参数为success或fail，则返回当前阈值设定下的识别成功或失败结果。
    // 识别结果覆盖1：N或者1：1业务的所有记录。
    private String recordsType;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRecordsType() {
        return recordsType;
    }

    public void setRecordsType(String recordsType) {
        this.recordsType = recordsType;
    }
}
