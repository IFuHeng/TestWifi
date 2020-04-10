package com.changhong.wifimng.been;

import com.changhong.wifimng.R;

public enum ConnectType {
    //    bit0: 有线;bit1:2.4G; bit2:5G；bit3:访客网络
//    如1表示有线； 2表示2.4G；4表示5G；8表示访客网络

    NO_CONNECT(0, R.string.connecttype_no),
    LINE(1, R.string.connecttype_wire),
    WIFI24(2, R.string.connecttype_24G),
    WIFI5(4, R.string.connecttype_5G),
    WIFI2or5(6, R.string.connecttype_2_5G),
    GUEST(8, R.string.connecttype_guest),
    PLC(16, R.string._plc);


    private int value;
    private int resId;

    ConnectType(int value, int resId) {
        this.value = value;
        this.resId = resId;
    }

    public int getValue() {
        return value;
    }

    public int getResId() {
        return resId;
    }
}
