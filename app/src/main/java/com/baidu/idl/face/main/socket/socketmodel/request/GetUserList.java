package com.baidu.idl.face.main.socket.socketmodel.request;

public class GetUserList {
    private String deviceId;
    // 用户组id
    private String groupId;
    // 默认值0，起始序号
    private String start;
    // 返回数量，默认值100，最大值1000
    private String length;

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

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }
}
