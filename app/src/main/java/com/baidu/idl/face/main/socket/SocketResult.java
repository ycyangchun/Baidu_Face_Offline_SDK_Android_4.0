package com.baidu.idl.face.main.socket;

import com.google.gson.annotations.SerializedName;

/**
 * author : baidu
 * date : 2019/7/25 4:02 PM
 * description :
 */
public class SocketResult<T> {

    @SerializedName("code")
    public int code;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public T data;

    @SerializedName("queryId")
    public String queryId;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }
}
