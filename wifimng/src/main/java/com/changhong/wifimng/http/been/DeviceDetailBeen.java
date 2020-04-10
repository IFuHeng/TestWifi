package com.changhong.wifimng.http.been;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class DeviceDetailBeen implements Parcelable {

    /**
     * 设备名称
     */
    private String name;
    /**
     * SN
     */
    private String sn;
    /**
     * MAC
     */
    private String mac;
    /**
     * 硬件版本
     */
    private String hardwareVersion;
    /**
     * 软件版本，设备检查更新时更新版本号
     */
    private String softwareVersion;
    /**
     * 设备品类
     */
    private String deviceCategory;
    /**
     * 设备类型
     */
    private String deviceType;
    /**
     * 设备型号
     */
    private String deviceModel;
    /**
     * not nessary 		设备绑定的用户ID，解绑后才能被其他用户绑定
     */
    private String bindingUserId;
    /**
     * not nessary 		绑定用户的昵称
     */
    private String bindingUserName;
    /**
     * not nessary 		绑定用户的邮箱
     */
    private String bindingUserEmail;
    /**
     * not nessary 		绑定用户的电话号码
     */
    private String bingingUserPhoneNumber;
    /**
     * not nessary 		设备的图标url
     */
    private String iconUrl;
    /**
     * not nessary 		设备工况
     */
    private String state;

    private String groupId;

    public DeviceDetailBeen() {
    }

    protected DeviceDetailBeen(Parcel in) {
        name = in.readString();
        sn = in.readString();
        mac = in.readString();
        hardwareVersion = in.readString();
        softwareVersion = in.readString();
        deviceCategory = in.readString();
        deviceType = in.readString();
        deviceModel = in.readString();
        bindingUserId = in.readString();
        bindingUserName = in.readString();
        bindingUserEmail = in.readString();
        bingingUserPhoneNumber = in.readString();
        iconUrl = in.readString();
        state = in.readString();
        groupId = in.readString();
    }

    public static final Creator<DeviceDetailBeen> CREATOR = new Creator<DeviceDetailBeen>() {
        @Override
        public DeviceDetailBeen createFromParcel(Parcel in) {
            return new DeviceDetailBeen(in);
        }

        @Override
        public DeviceDetailBeen[] newArray(int size) {
            return new DeviceDetailBeen[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getSn() {
        return sn;
    }

    public String getMac() {
        if (mac != null && mac.length() == 12) {
            if (mac.indexOf(':') == -1) {
                StringBuilder sb = new StringBuilder(mac);
                for (int i = 0; i < 5; i++) {
                    sb.insert(i * 3 + 2, File.pathSeparatorChar);
                }
                return sb.toString();
            }
        }
        return mac;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public String getDeviceCategory() {
        return deviceCategory;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getBindingUserId() {
        return bindingUserId;
    }

    public String getBindingUserName() {
        return bindingUserName;
    }

    public String getBindingUserEmail() {
        return bindingUserEmail;
    }

    public String getBingingUserPhoneNumber() {
        return bingingUserPhoneNumber;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getState() {
        return state;
    }

    public String getGroupId() {
        return groupId;
    }

    @Override
    public String toString() {
        return "DeviceDetailBeen{" +
                "name='" + name + '\'' +
                ", sn='" + sn + '\'' +
                ", mac='" + mac + '\'' +
                ", hardwareVersion='" + hardwareVersion + '\'' +
                ", softwareVersion='" + softwareVersion + '\'' +
                ", deviceCategory='" + deviceCategory + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", bindingUserId='" + bindingUserId + '\'' +
                ", bindingUserName='" + bindingUserName + '\'' +
                ", bindingUserEmail='" + bindingUserEmail + '\'' +
                ", bingingUserPhoneNumber='" + bingingUserPhoneNumber + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", groupId='" + groupId + '\'' +
                ", state='" + state + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(sn);
        dest.writeString(mac);
        dest.writeString(hardwareVersion);
        dest.writeString(softwareVersion);
        dest.writeString(deviceCategory);
        dest.writeString(deviceType);
        dest.writeString(deviceModel);
        dest.writeString(bindingUserId);
        dest.writeString(bindingUserName);
        dest.writeString(bindingUserEmail);
        dest.writeString(bingingUserPhoneNumber);
        dest.writeString(iconUrl);
        dest.writeString(state);
        dest.writeString(groupId);
    }
}
