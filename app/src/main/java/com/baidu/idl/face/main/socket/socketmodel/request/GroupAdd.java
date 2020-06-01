package com.baidu.idl.face.main.socket.socketmodel.request;

public class GroupAdd {
    // 设备指纹
    private String deviceId;
    // group标识 （由数字、字母、下划线组成），长度限制48B
    private String groupId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
