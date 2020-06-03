package com.yc.patrol;


public class MyConstants {
    public static final int ZhengShi = 0;
    public static final int CheShi = 1;

    public static String BaseUrl = "";
    public static boolean isDubug = false;
    public static void setType(int type) {
        switch (type) {
            case ZhengShi:
                BaseUrl = "";
                isDubug = false;
                break;
            case CheShi:
                BaseUrl = "";
                isDubug = true;
                break;

        }
    }
    //登录之后返回的sessionid，其他接口的header需要此参数
    public static String sessionid = "";
    //登录时的用户名
    public static String username = "";
    //登录时的密码
    public static String password = "";
    //登录成功之后返回的用户id
    public static String id = "";
    //个人信息那绑定指纹id时发送消息更新界面的标识
    //手机DeviceId
    public static String deviceId = "";

    /**
     *  兑奖 or  活动 （二维码框不同）
     **/
    public static boolean activeORedeem = false;//
    public static boolean isAlphaStatusBar = true;
    public static int width = 0;
    public static int height = 0;
    public static String packageName = "patrol";
}
