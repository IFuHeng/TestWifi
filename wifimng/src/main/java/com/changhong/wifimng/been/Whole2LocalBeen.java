package com.changhong.wifimng.been;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * 从app传输到wifimng的必须参数
 */
public class Whole2LocalBeen implements Parcelable {
    private String user;
    private String token;
    private String userUuid;
    private String devcieUuid;
    private String mac;
    private String deviceType;
    private String deviceName;

    public Whole2LocalBeen() {

    }

    protected Whole2LocalBeen(Parcel in) {
        user = in.readString();
        token = in.readString();
        userUuid = in.readString();
        devcieUuid = in.readString();
        mac = in.readString();
        deviceType = in.readString();
        deviceName = in.readString();
    }

    public static final Creator<Whole2LocalBeen> CREATOR = new Creator<Whole2LocalBeen>() {
        @Override
        public Whole2LocalBeen createFromParcel(Parcel in) {
            return new Whole2LocalBeen(in);
        }

        @Override
        public Whole2LocalBeen[] newArray(int size) {
            return new Whole2LocalBeen[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user);
        dest.writeString(token);
        dest.writeString(userUuid);
        dest.writeString(devcieUuid);
        dest.writeString(mac);
        dest.writeString(deviceType);
        dest.writeString(deviceName);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public void setDevcieUuid(String devcieUuid) {
        this.devcieUuid = devcieUuid;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public String getDevcieUuid() {
        return devcieUuid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMac() {
//        if (mac != null && mac.length() == 12) {
//            StringBuilder sb = new StringBuilder(mac);
//            for (int i = 0; i < 5; i++) {
//                sb.insert(3 * i + 2, File.pathSeparatorChar);
//            }
//            return sb.toString();
//        }
        return mac;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public static Creator<Whole2LocalBeen> getCREATOR() {
        return CREATOR;
    }

    @Override
    public String toString() {
        return "Whole2LocalBeen{" +
                "user='" + user + '\'' +
                ", token='" + token + '\'' +
                ", userUuid='" + userUuid + '\'' +
                ", devcieUuid='" + devcieUuid + '\'' +
                ", mac='" + mac + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }
}
