package com.changhong.wifimng.been;

import com.google.gson.Gson;

public class PLCRequestBeen<T> {
    private String jsonrpc = "2.0";
    private int id = 1;
    private String method;

    private T param;

    public PLCRequestBeen(T param) {
        this.param = param;
    }

    public PLCRequestBeen(String method, T param) {
        this.method = method;
        this.param = param;
    }

    @Override
    public String toString() {
        return "RequestBeen{" +
                "jsonrpc='" + jsonrpc + '\'' +
                ", id=" + id +
                ", method='" + method + '\'' +
                ", param=" + param +
                '}';
    }

    public String toJsonString() {
        return new Gson().toJson(this);
    }
}
