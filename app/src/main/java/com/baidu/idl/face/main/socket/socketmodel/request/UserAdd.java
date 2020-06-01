package com.baidu.idl.face.main.socket.socketmodel.request;

public class UserAdd {
    // 设备指纹
    private String deviceId;
    // 用户id（由数字、字母、下划线组成），长度限制128B
    private String userId;
    // 用户组id，标识一组用户（由数字、字母、下划线组成），长度限制128B。用户组和user_id之间，仅为映射关系。
    // 如传入的groupid并未事先创建完毕，则注册用户的同时，直接完成group的创建。
    private String groupId;
    // 图片信息，数据大小应小于10M，每次仅支持单张图片
    private String image;
    // 用户资料，长度限制256B。可为空
    private String userInfo;
    // 用户名
    private String userName;
    // 图片名
    private String imageName;
    // 人脸token
    private String faceToken;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getFaceToken() {
        return faceToken;
    }

    public void setFaceToken(String faceToken) {
        this.faceToken = faceToken;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

}
