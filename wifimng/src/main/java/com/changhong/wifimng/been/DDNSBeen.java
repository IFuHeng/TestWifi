package com.changhong.wifimng.been;

import android.os.Parcel;
import android.os.Parcelable;

public class DDNSBeen implements Parcelable {
    boolean enable;
    String domain_name;
    String user_name;
    String user_password;
    int type;

    public DDNSBeen() {
    }

    protected DDNSBeen(Parcel in) {
        enable = in.readByte() != 0;
        domain_name = in.readString();
        user_name = in.readString();
        user_password = in.readString();
        type = in.readInt();
    }

    public static final Creator<DDNSBeen> CREATOR = new Creator<DDNSBeen>() {
        @Override
        public DDNSBeen createFromParcel(Parcel in) {
            return new DDNSBeen(in);
        }

        @Override
        public DDNSBeen[] newArray(int size) {
            return new DDNSBeen[size];
        }
    };

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (enable ? 1 : 0));
        dest.writeString(domain_name);
        dest.writeString(user_name);
        dest.writeString(user_password);
        dest.writeInt(type);
    }
}
