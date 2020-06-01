package com.baidu.idl.face.main.socket;

public class RequestConfig {

    // 发送设备deviceid
    public static final int sendDeviceId = 100;
    // 配置文件修改
    public static final int resetConfigFile = 200;
    // 人脸注册
    public static final int userFaceAdd = 201;
    // 人脸删除
//    public static final int userFaceDelete = 202;
    // 用户信息更新
    public static final int userUpdate = 203;
    // 用户删除
    public static final int userDelete = 204;
    // 人脸复制
    public static final int userCopy = 205;
    // 用户组创建
    public static final int groupAdd = 206;
    // 用户组删除
    public static final int groupDelete = 207;
    // 用户信息查询
    public static final int getUserInfo = 208;
    // 用户组列表查询
    public static final int getUserList = 209;
    // 组列表查询
    public static final int getGroupList = 210;
    // 人脸对比
    public static final int match = 211;
    // 人脸识别
    public static final int identify = 212;
    // 清理日志
    public static final int cleanLog = 213;
    // 获取识别记录
    public static final int getRecords = 214;
    // 删除识别记录
    public static final int deleteRecords = 215;
    // 未知异常
    public static final int UnKonwError = 404;
}
