package com.changhong.wifimng.been;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 设备信息
 */
public class DeviceBeen implements Parcelable {

    private DeviceType deviceType;
    private String name;
    private String ip;
    private String mac;
    private String location;
    private int count_under;
    private boolean isMain;

    public DeviceBeen() {
    }


    protected DeviceBeen(Parcel in) {
        name = in.readString();
        ip = in.readString();
        mac = in.readString();
        location = in.readString();
        count_under = in.readInt();
        isMain = in.readByte() != 0;
    }

    public static final Creator<DeviceBeen> CREATOR = new Creator<DeviceBeen>() {
        @Override
        public DeviceBeen createFromParcel(Parcel in) {
            return new DeviceBeen(in);
        }

        @Override
        public DeviceBeen[] newArray(int size) {
            return new DeviceBeen[size];
        }
    };

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCount_under() {
        return count_under;
    }

    public void setCount_under(int count_under) {
        this.count_under = count_under;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(ip);
        dest.writeString(mac);
        dest.writeString(location);
        dest.writeInt(count_under);
        dest.writeByte((byte) (isMain ? 1 : 0));
    }
}
