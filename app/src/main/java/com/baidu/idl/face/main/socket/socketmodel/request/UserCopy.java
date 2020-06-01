package com.baidu.idl.face.main.socket.socketmodel.request;

public class UserCopy {
    // 设备指纹
    private String deviceId;
    // 用户id
    private String userId;
    // 从指定group里复制，长度限制48B
    private String srcGroupId;
    // 需要添加用户的组id，长度限制48B
    private String dstGroupId;
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

    public String getSrcGroupId() {
        return srcGroupId;
    }

    public void setSrcGroupId(String srcGroupId) {
        this.srcGroupId = srcGroupId;
    }

    public String getDstGroupId() {
        return dstGroupId;
    }

    public void setDstGroupId(String dstGroupId) {
        this.dstGroupId = dstGroupId;
    }
}
