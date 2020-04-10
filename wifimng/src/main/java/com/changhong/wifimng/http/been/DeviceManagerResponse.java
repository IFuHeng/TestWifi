package com.changhong.wifimng.http.been;

import java.util.ArrayList;

public class DeviceManagerResponse {

    /**
     * 被绑定设备的uuid
     */
    private String uuid;
    /**
     * 设备名称
     **/
    private String name;
    /**
     * SN
     **/
    private String sn;
    /**
     * mac
     **/
    private String mac;
    /**
     * 硬件版本
     **/
    private String hardwareVersion;
    /**
     * 软件版本，设备检查更新时更新版本号
     **/
    private String softwareVersion;
    /**
     * 设备品类
     **/
    private String deviceCategory;
    /**
     * 设备类型
     **/
    private String deviceType;
    /**
     * 设备型号
     **/
    private String deviceModel;
    /**
     * 设备的图标url
     **/
    private String iconUrl;
    /**
     * 设备工况
     **/
    private String state;

    /**
     * 当前访问的页数
     */
    private Integer currentPage;
    /**
     * 每页的SIZE
     */
    private Integer pageSize;
    private Integer totalPage;
    private Integer totalSize;

    private ArrayList<GroupObject> list;

    class GroupObject {
        String userId;
        String groupName;
        String uuid;
    }

    public String getName() {
        return name;
    }

    public String getSn() {
        return sn;
    }

    public String getMac() {
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

    public String getIconUrl() {
        return iconUrl;
    }

    public String getState() {
        return state;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public ArrayList<GroupObject> getList() {
        return list;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "DeviceManagerResponse{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", sn='" + sn + '\'' +
                ", mac='" + mac + '\'' +
                ", hardwareVersion='" + hardwareVersion + '\'' +
                ", softwareVersion='" + softwareVersion + '\'' +
                ", deviceCategory='" + deviceCategory + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", state='" + state + '\'' +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", totalPage=" + totalPage +
                ", totalSize=" + totalSize +
                ", list=" + list +
                '}';
    }
}
