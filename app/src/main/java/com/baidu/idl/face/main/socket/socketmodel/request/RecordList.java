package com.baidu.idl.face.main.socket.socketmodel.request;

import com.baidu.idl.face.main.socket.socketmodel.response.ResponseGetRecords;

import java.util.List;

/**
 * author : baidu
 * date : 2019/9/4 8:41 PM
 * description :
 */
public class RecordList {
    private String deviceId;
    private List<ResponseGetRecords> list;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<ResponseGetRecords> getList() {
        return list;
    }

    public void setList(List<ResponseGetRecords> list) {
        this.list = list;
    }
}