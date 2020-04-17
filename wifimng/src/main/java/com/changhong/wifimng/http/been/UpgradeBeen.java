package com.changhong.wifimng.http.been;

public class UpgradeBeen {
    private Long upgradePackageId;
    private Integer isUpgrade;
    private String mac;
    private Integer isForce;
    private String upgradeVersion;
    private String upgradeURL;
    private String packageSize;
    private String md5;
    private String comments;

    public Long getUpgradePackageId() {
        return upgradePackageId;
    }

    public void setUpgradePackageId(Long upgradePackageId) {
        this.upgradePackageId = upgradePackageId;
    }

    public Integer getIsUpgrade() {
        return isUpgrade;
    }

    public void setIsUpgrade(Integer isUpgrade) {
        this.isUpgrade = isUpgrade;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Integer getIsForce() {
        return isForce;
    }

    public void setIsForce(Integer isForce) {
        this.isForce = isForce;
    }

    public String getUpgradeVersion() {
        return upgradeVersion;
    }

    public void setUpgradeVersion(String upgradeVersion) {
        this.upgradeVersion = upgradeVersion;
    }

    public String getUpgradeURL() {
        return upgradeURL;
    }

    public void setUpgradeURL(String upgradeURL) {
        this.upgradeURL = upgradeURL;
    }

    public String getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(String packageSize) {
        this.packageSize = packageSize;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
