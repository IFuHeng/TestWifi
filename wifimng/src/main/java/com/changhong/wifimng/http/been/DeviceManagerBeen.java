package com.changhong.wifimng.http.been;

import java.util.ArrayList;
import java.util.List;

public class DeviceManagerBeen {

    /**
     * APP用户id
     */
    private String userId;
    /**
     * 设备mac
     */
    private String mac;
    /**
     * 设备唯一标识
     */
    private String uuid;
    /**
     * 令牌
     */
    private String token;

    /**
     * 设备id集
     */
    private ArrayList<String> devList;
    /**
     * 设备数据是否清除 ,0:不需要,1:需要，若需要将发送mqtt消息给设备清除数据 ，具体格式参考客户端到终端的消息格式定义
     */
    private Integer isClean;


    /**
     * PC, ANDROID, IOS, DEVICE
     */
    private String client;
    /**
     * app内置设备图标id
     */
    private String iconId;
    /**
     * 设备名称
     */
    private String name;
    /**
     * 图片文件
     */
    private String file;
    /**
     * 分组Id
     */
    private String groupId;
    /**
     * 设备id
     */
    private String deviceId;
    /**
     * 分组名
     */
    private String groupName;
    /**
     * 当前访问的页数
     */
    private Integer currentPage;
    /**
     * 每页的SIZE
     */
    private Integer pageSize;

    /**
     * 被分享用户的账号
     */
    private String account;

    /**
     * 设备Id 的一个列表
     */
    private List<String> deviceList;

    /**
     * 分享状态，空：全部，0：待接受的分享，1：已被接受的分享
     */
    private Integer shareStatus;
    /**
     * 分享者类型，1：分享者，2：被分享者
     */
    private Integer shareUserType ;
    /**
     * 被分享者ID
     */
    private  String sharedUserId ;
    /**
     * 分享者ID
     */
    private  String sharingUserId ;
    /**
     * 分享的操作，1：接受，2：拒绝,3:取消（shareUserType = 1时有效），4：删除
     */
    private Integer operation;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setDevList(ArrayList<String> devList) {
        this.devList = devList;
    }

    public void setIsClean(Integer isClean) {
        this.isClean = isClean;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setDeviceList(List<String> deviceList) {
        this.deviceList = deviceList;
    }

    public void setShareStatus(Integer shareStatus) {
        this.shareStatus = shareStatus;
    }

    public void setShareUserType(Integer shareUserType) {
        this.shareUserType = shareUserType;
    }

    public void setSharedUserId(String sharedUserId) {
        this.sharedUserId = sharedUserId;
    }

    public void setSharingUserId(String sharingUserId) {
        this.sharingUserId = sharingUserId;
    }

    public void setOperation(Integer operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "DeviceManagerBeen{" +
                "userId='" + userId + '\'' +
                ", mac='" + mac + '\'' +
                ", uuid='" + uuid + '\'' +
                ", token='" + token + '\'' +
                ", devList=" + devList +
                ", isClean=" + isClean +
                ", client='" + client + '\'' +
                ", iconId='" + iconId + '\'' +
                ", name='" + name + '\'' +
                ", file='" + file + '\'' +
                ", groupId='" + groupId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", account='" + account + '\'' +
                ", deviceList=" + deviceList +
                ", shareStatus=" + shareStatus +
                ", shareUserType=" + shareUserType +
                ", sharedUserId='" + sharedUserId + '\'' +
                ", sharingUserId='" + sharingUserId + '\'' +
                ", operation=" + operation +
                '}';
    }
}
