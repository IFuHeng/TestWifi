package com.changhong.wifimng.http.been;

public class ResponseBeen {
    private int code;
    private DeviceManagerResponse data;
    private String key;
    private String msg;
    private String user;

    public int getCode() {
        return code;
    }

    public DeviceManagerResponse getData() {
        return data;
    }

    public String getKey() {
        return key;
    }

    public String getMsg() {
        return msg;
    }

    public String getUser() {
        return user;
    }

}
