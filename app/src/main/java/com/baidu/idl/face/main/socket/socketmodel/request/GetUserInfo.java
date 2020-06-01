package com.baidu.idl.face.main.socket.socketmodel.request;

public class GetUserInfo {
    // 设备指纹
    private String deviceId;
    // 用户id（由数字、字母、下划线组成），长度限制128B
    private String userId;
    // 用户组id（由数字、字母、下划线组成） 长度限制48B
    private String groupId;
    // 用户名
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
