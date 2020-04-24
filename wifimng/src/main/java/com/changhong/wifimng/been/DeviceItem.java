package com.changhong.wifimng.been;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceItem implements Parcelable {
    private Boolean isUpConnected;
    private String deviceName;
    private String Ip;
    private String mac;
    private DeviceType type;
    private int staNum;
    private String location;
    private boolean isChild;
    private String upNodeName;
    private WanType wan_type;
    private String iconUrl;
    private Integer qlink;

    public DeviceItem() {
    }

    protected DeviceItem(Parcel in) {
        byte tmpIsUpConnected = in.readByte();
        isUpConnected = tmpIsUpConnected == 0 ? null : tmpIsUpConnected == 1;
        deviceName = in.readString();
        Ip = in.readString();
        mac = in.readString();
        staNum = in.readInt();
        location = in.readString();
        isChild = in.readByte() != 0;
        upNodeName = in.readString();
        wan_type = WanType.getDeviceTypeFromTypeCode(in.readInt());
        type = DeviceType.getDeviceTypeFromName(in.readString());
        iconUrl = in.readString();
        if (in.readByte() == 0) {
            qlink = null;
        } else {
            qlink = in.readInt();
        }
    }

    public static final Creator<DeviceItem> CREATOR = new Creator<DeviceItem>() {
        @Override
        public DeviceItem createFromParcel(Parcel in) {
            return new DeviceItem(in);
        }

        @Override
        public DeviceItem[] newArray(int size) {
            return new DeviceItem[size];
        }
    };

    public WanType getWan_type() {
        return wan_type;
    }

    public void setWan_type(WanType wan_type) {
        this.wan_type = wan_type;
    }

    public String getUpNodeName() {
        return upNodeName;
    }

    public void setUpNodeName(String upNodeName) {
        this.upNodeName = upNodeName;
    }


    public Boolean getUpConnected() {
        return isUpConnected;
    }

    public void setUpConnected(Boolean upConnected) {
        isUpConnected = upConnected;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceNameOrMac() {
        if (deviceName != null && deviceName.trim().length() > 0)
            return deviceName;
        else
            return mac;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getIp() {
        if ("none_link".equals(Ip))
            return "0.0.0.0";
        return Ip;
    }

    public void setIp(String ip) {
        Ip = ip;
    }

    public boolean isLinkOn() {
        return Ip != null && !"none_link".equals(Ip);
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public int getStaNum() {
        return staNum;
    }

    public void setStaNum(int staNum) {
        this.staNum = staNum;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean child) {
        isChild = child;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Integer getQlink() {
        return qlink;
    }

    public void setQlink(Integer qlink) {
        this.qlink = qlink;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isUpConnected == null ? 0 : isUpConnected ? 1 : 2));
        dest.writeString(deviceName);
        dest.writeString(Ip);
        dest.writeString(mac);
        dest.writeInt(staNum);
        dest.writeString(location);
        dest.writeByte((byte) (isChild ? 1 : 0));
        dest.writeString(upNodeName);
        dest.writeInt(wan_type == null ? -1 : wan_type.getTypeCode());
        dest.writeString(type == null ? null : type.getName());
        dest.writeString(iconUrl);
        if (qlink == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(qlink);
        }
    }

    @Override
    public String toString() {
        return "DeviceItem{" +
                "isUpConnected=" + isUpConnected +
                ", deviceName='" + deviceName + '\'' +
                ", Ip='" + Ip + '\'' +
                ", mac='" + mac + '\'' +
                ", type=" + type +
                ", staNum=" + staNum +
                ", location='" + location + '\'' +
                ", isChild=" + isChild +
                ", upNodeName='" + upNodeName + '\'' +
                ", wan_type=" + wan_type +
                ", iconUrl='" + iconUrl + '\'' +
                ", qlink=" + qlink +
                '}';
    }
}
