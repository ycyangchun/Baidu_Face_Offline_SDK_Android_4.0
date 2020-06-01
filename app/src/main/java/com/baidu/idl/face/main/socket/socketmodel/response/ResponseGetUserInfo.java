package com.baidu.idl.face.main.socket.socketmodel.response;

public class ResponseGetUserInfo {
    // 设备指纹
    private String deviceId;
    // 用户组标识
    private String groupId;
    // 用户备注信息
    private String userInfo;
    // 人脸信息
    private String face;
    // 人脸特征的唯一标识
    private String faceToken;
    // 人脸首次注册时间
    private String createTime;
    // 识别结果列表
    private String[] result;

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

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public String getFaceToken() {
        return faceToken;
    }

    public void setFaceToken(String faceToken) {
        this.faceToken = faceToken;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String[] getResult() {
        return result;
    }

    public void setResult(String[] result) {
        this.result = result;
    }
}
