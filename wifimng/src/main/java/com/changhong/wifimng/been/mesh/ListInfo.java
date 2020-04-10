package com.changhong.wifimng.been.mesh;

import android.os.Parcel;
import android.os.Parcelable;

import com.changhong.wifimng.been.DeviceItem;
import com.changhong.wifimng.been.StaInfo;

import java.util.List;

public class ListInfo implements Parcelable {

    private String mac;
    private String ip;
    /**
     * 1表示快连接口下挂的临时设备；0表示正式组网设备；
     */
    private Integer qlink;
    /**
     * 该设备下连接设备信息
     */
    private List<StaInfo> sta_info;

    public ListInfo() {
    }

    protected ListInfo(Parcel in) {
        mac = in.readString();
        ip = in.readString();
        if (in.readByte() == 0) {
            qlink = null;
        } else {
            qlink = in.readInt();
        }
        sta_info = in.createTypedArrayList(StaInfo.CREATOR);
    }

    public static final Creator<ListInfo> CREATOR = new Creator<ListInfo>() {
        @Override
        public ListInfo createFromParcel(Parcel in) {
            return new ListInfo(in);
        }

        @Override
        public ListInfo[] newArray(int size) {
            return new ListInfo[size];
        }
    };

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getQlink() {
        return qlink;
    }

    public void setQlink(Integer qlink) {
        this.qlink = qlink;
    }

    public List<StaInfo> getSta_info() {
        return sta_info;
    }

    public void setSta_info(List<StaInfo> sta_info) {
        this.sta_info = sta_info;
    }

    @Override
    public String toString() {
        return "ListInfo{" +
                "mac='" + mac + '\'' +
                ", ip='" + ip + '\'' +
                ", qlink='" + qlink + '\'' +
                ", sta_info=" + sta_info +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mac);
        dest.writeString(ip);
        if (qlink == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(qlink);
        }
        dest.writeTypedList(sta_info);
    }


    public DeviceItem getDeviceItem(String deviceName){
        DeviceItem item = new DeviceItem();
        item.setUpConnected(getQlink() == 0);
        item.setUpNodeName(deviceName);
        item.setMac(getMac());
        item.setIp(getIp());
        item.setStaNum(getSta_info() == null ? 0 : getSta_info().size());
        item.setChild(true);
        return item;
    }
}
