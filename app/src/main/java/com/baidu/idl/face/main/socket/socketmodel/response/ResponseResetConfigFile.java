package com.baidu.idl.face.main.socket.socketmodel.response;

/**
 * author : baidu
 * date : 2019/8/8 5:05 PM
 * description :
 */
public class ResponseResetConfigFile {
    // 设备指纹
    private String deviceId;

    // 是否修改成功
    private boolean isOK;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isOK() {
        return isOK;
    }

    public void setOK(boolean modifyOK) {
        isOK = modifyOK;
    }
}
