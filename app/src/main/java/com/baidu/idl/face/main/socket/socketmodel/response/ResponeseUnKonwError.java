package com.baidu.idl.face.main.socket.socketmodel.response;

/**
 * author : baidu
 * date : 2019/9/5 7:43 PM
 * description :
 */
public class ResponeseUnKonwError {
    private String deviceId;
    private String errorMsg;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
