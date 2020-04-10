package com.changhong.wifimng.been.plc;

import android.os.Parcel;
import android.os.Parcelable;

import com.changhong.wifimng.been.BaseResponseBeen;

public class PLCDDNSBeen extends BaseResponseBeen implements Parcelable {
    private Integer enable;//1:使能；0:关闭
    private String domain_name;//域名
    private String user_name;//用户名
    private String user_password;//用户密码
    private Integer type;//0:DynDNS; 1:TZO

    public PLCDDNSBeen() {
    }


    protected PLCDDNSBeen(Parcel in) {
        if (in.readByte() == 0) {
            enable = null;
        } else {
            enable = in.readInt();
        }
        domain_name = in.readString();
        user_name = in.readString();
        user_password = in.readString();
        if (in.readByte() == 0) {
            type = null;
        } else {
            type = in.readInt();
        }
    }

    public static final Creator<PLCDDNSBeen> CREATOR = new Creator<PLCDDNSBeen>() {
        @Override
        public PLCDDNSBeen createFromParcel(Parcel in) {
            return new PLCDDNSBeen(in);
        }

        @Override
        public PLCDDNSBeen[] newArray(int size) {
            return new PLCDDNSBeen[size];
        }
    };

    public String getDomain_name() {
        return domain_name;
    }

    public void setDomain_name(String domain_name) {
        this.domain_name = domain_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_password() {
        return user_password;
    }

    public void setUser_password(String user_password) {
        this.user_password = user_password;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Integer getEnable() {
        return enable;
    }

    public void setEnable(Integer enable) {
        this.enable = enable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (enable == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(enable);
        }
        parcel.writeString(domain_name);
        parcel.writeString(user_name);
        parcel.writeString(user_password);
        if (type == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(type);
        }
    }
}

