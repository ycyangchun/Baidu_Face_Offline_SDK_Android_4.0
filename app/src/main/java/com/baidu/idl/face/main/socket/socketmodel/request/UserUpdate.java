package com.baidu.idl.face.main.socket.socketmodel.request;

public class UserUpdate {
    // 设备指纹
    private String deviceId;
    // 用户id（由数字、字母、下划线组成），长度限制128B
    private String userId;
    // 用户组id，标识一组用户（由数字、字母、下划线组成），长度限制48B
    private String groupId;
    // 图片信息，数据大小应小于10M，每次仅支持单张图片
    private String image;
    //  图片类型，必选择以下三种形式之一
    // BASE64：图片的base64值；
    // FACE_FILE：图片的本地文件路径地址；
    // FACE_TOKEN：face_token 人脸标识；
    private String imageType;
    // 用户资料，长度限制256B
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

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }
}
