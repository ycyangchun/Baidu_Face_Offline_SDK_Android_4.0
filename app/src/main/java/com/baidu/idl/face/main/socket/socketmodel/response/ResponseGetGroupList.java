package com.baidu.idl.face.main.socket.socketmodel.response;

public class ResponseGetGroupList {
    // 设备指纹
    private String deviceId;
    // groupId列表数组
    private String[] groupIdList;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String[] getGroupIdList() {
        return groupIdList;
    }

    public void setGroupIdList(String[] groupIdList) {
        this.groupIdList = groupIdList;
    }
}
