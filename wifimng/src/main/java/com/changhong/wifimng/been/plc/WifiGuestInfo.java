package com.changhong.wifimng.been.plc;

import com.changhong.wifimng.been.BaseResponseBeen;

public class WifiGuestInfo extends BaseResponseBeen {
    Integer enable;//使能访客网络，0-禁用，1-启动
    String name;//访客网络名称
    String passwd;//访客网络密码
    Integer valid_time;//访客网络预置可用时间，单位：分钟
    Integer time_left;//访客网络剩余可用时间，单位：分钟

    public Integer getEnable() {
        return enable;
    }

    public void setEnable(Integer enable) {
        this.enable = enable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public Integer getValid_time() {
        return valid_time;
    }

    public void setValid_time(Integer valid_time) {
        this.valid_time = valid_time;
    }

    public Integer getTime_left() {
        return time_left;
    }

    public void setTime_left(Integer time_left) {
        this.time_left = time_left;
    }
}
