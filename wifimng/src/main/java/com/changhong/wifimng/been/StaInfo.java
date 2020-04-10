package com.changhong.wifimng.been;

import android.os.Parcel;
import android.os.Parcelable;

public class StaInfo implements Parcelable {

    private String name;
    private String ip;
    private String mac;
    /**
     * bit0: 有线;bit1:2.4G; bit2:5G；bit3:访客网络 如1表示有线； 2表示2.4G；4表示5G；8表示访客网络 , 16PLC
     */
    private int connect_type;
    /**
     * 在线时长 单位秒.(有线没有时长)
     */
    private int link_time;

    private long upload;
    private long download;

    private Integer rssi;

    public StaInfo() {
    }


    protected StaInfo(Parcel in) {
        name = in.readString();
        ip = in.readString();
        mac = in.readString();
        connect_type = in.readInt();
        link_time = in.readInt();
        upload = in.readLong();
        download = in.readLong();
        if (in.readByte() == 0) {
            rssi = null;
        } else {
            rssi = in.readInt();
        }
    }

    public static final Creator<StaInfo> CREATOR = new Creator<StaInfo>() {
        @Override
        public StaInfo createFromParcel(Parcel in) {
            return new StaInfo(in);
        }

        @Override
        public StaInfo[] newArray(int size) {
            return new StaInfo[size];
        }
    };

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

    public Integer getConnect_type() {
        return connect_type;
    }

    public ConnectType getConnectType() {

        for (ConnectType value : ConnectType.values()) {
            if (connect_type == value.getValue())
                return value;
        }
        return ConnectType.NO_CONNECT;
    }

    /**
     * @param connect_type bit0: 有线;bit1:2.4G; bit2:5G；bit3:访客网络 如1表示有线； 2表示2.4G；4表示5G；8表示访客网络
     */
    public void setConnect_type(Integer connect_type) {
        this.connect_type = connect_type;
    }

    public Integer getLink_time() {
        return link_time;
    }

    public void setLink_time(Integer link_time) {
        this.link_time = link_time;
    }

    public Long getUpload() {
        return upload;
    }

    public void setUpload(Integer upload) {
        this.upload = upload;
    }

    public Long getDownload() {
        return download;
    }

    public void setDownload(Integer download) {
        this.download = download;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public Integer getRssi() {
        return rssi;
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
        dest.writeInt(connect_type);
        dest.writeInt(link_time);
        dest.writeLong(upload);
        dest.writeLong(download);
        if (rssi == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(rssi);
        }
    }
}
