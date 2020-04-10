package com.changhong.wifimng.http.been;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 设备列表item
 */
public class DeviceBeen implements Parcelable {
    private String uuid;//	设备Id
    private String name;//	设备名称
    private String sn;//	SN
    private String mac;//	MAC
    private Integer deviceCategroy;//	设备品类
    private Integer deviceModel;//	设备型号
    private Integer deviceType;//	设备类型
    private String hardwareVersion;//	硬件版本
    private String softwareVersion;//	软件版本
    private String iconUrl;//	图标URL
    private String iconId;//	图标id
    private String state;//	设备工况信息
    private String createTime;//	创建时间
    private String updateTime;//	更新时间
    private String sharingUserId;//分享用户的uuid
    private String sharedUserId;// 被分享用户的uuid
    private Integer shareStatus;//分享的状态，0：待接受，1：已接受

    public DeviceBeen() {
    }

    protected DeviceBeen(Parcel in) {
        uuid = in.readString();
        name = in.readString();
        sn = in.readString();
        mac = in.readString();
        if (in.readByte() == 0) {
            deviceCategroy = null;
        } else {
            deviceCategroy = in.readInt();
        }
        if (in.readByte() == 0) {
            deviceModel = null;
        } else {
            deviceModel = in.readInt();
        }
        if (in.readByte() == 0) {
            deviceType = null;
        } else {
            deviceType = in.readInt();
        }
        hardwareVersion = in.readString();
        softwareVersion = in.readString();
        iconUrl = in.readString();
        iconId = in.readString();
        state = in.readString();
        createTime = in.readString();
        updateTime = in.readString();
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Integer getDeviceCategroy() {
        return deviceCategroy;
    }

    public void setDeviceCategroy(Integer deviceCategroy) {
        this.deviceCategroy = deviceCategroy;
    }

    public Integer getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(Integer deviceModel) {
        this.deviceModel = deviceModel;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getIconId() {
        return iconId;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getSharingUserId() {
        return sharingUserId;
    }

    public void setSharingUserId(String sharingUserId) {
        this.sharingUserId = sharingUserId;
    }

    public String getSharedUserId() {
        return sharedUserId;
    }

    public void setSharedUserId(String sharedUserId) {
        this.sharedUserId = sharedUserId;
    }

    public Integer getShareStatus() {
        return shareStatus;
    }

    public void setShareStatus(Integer shareStatus) {
        this.shareStatus = shareStatus;
    }

    @Override
    public String toString() {
        return "DeviceBeen{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", sn='" + sn + '\'' +
                ", mac='" + mac + '\'' +
                ", deviceCategroy=" + deviceCategroy +
                ", deviceModel=" + deviceModel +
                ", deviceType=" + deviceType +
                ", hardwareVersion='" + hardwareVersion + '\'' +
                ", softwareVersion='" + softwareVersion + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", iconId='" + iconId + '\'' +
                ", state='" + state + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", sharingUserId='" + sharingUserId + '\'' +
                ", sharedUserId='" + sharedUserId + '\'' +
                ", shareStatus=" + shareStatus +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(name);
        dest.writeString(sn);
        dest.writeString(mac);
        if (deviceCategroy == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(deviceCategroy);
        }
        if (deviceModel == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(deviceModel);
        }
        if (deviceType == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(deviceType);
        }
        dest.writeString(hardwareVersion);
        dest.writeString(softwareVersion);
        dest.writeString(iconUrl);
        dest.writeString(iconId);
        dest.writeString(state);
        dest.writeString(createTime);
        dest.writeString(updateTime);
    }
}
