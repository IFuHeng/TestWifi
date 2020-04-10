package com.changhong.wifimng.been;

import com.google.gson.Gson;

public class RequestBeen<T> {
    String jsonrpc = "2.0";
    int id = 1;
    String method;

    T params;

    public RequestBeen(T params) {
        this.params = params;
    }

    public RequestBeen(String method, T params) {
        this.method = method;
        this.params = params;
    }

    @Override
    public String toString() {
        return "RequestBeen{" +
                "jsonrpc='" + jsonrpc + '\'' +
                ", id=" + id +
                ", method='" + method + '\'' +
                ", params=" + params +
                '}';
    }

    public String toJsonString() {
        return new Gson().toJson(this);
    }
}
