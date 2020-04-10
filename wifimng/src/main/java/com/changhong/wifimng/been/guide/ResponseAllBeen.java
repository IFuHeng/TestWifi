package com.changhong.wifimng.been.guide;

import com.changhong.wifimng.been.BaseResponseBeen;
import com.google.gson.annotations.SerializedName;

public class ResponseAllBeen extends BaseResponseBeen {

    /**
     * 1为WEB,2为APP，3为其他
     */
    private Integer src_type = 1;

    /**
     * static，dhcp，pppoe
     *
     * PLC:1、static ；2、dhcp ；3、pppoe
     */
    private String type;

    /**
     * when type is static	需校验输入值是否为有效IP
     */
    private String ipaddr;
    /**
     * 校验netmask
     */
    private String netmask;
    /**
     * 需校验输入值是否为有效IP
     */
    private String gw;
    /**
     * 需校验输入值是否为有效IP
     */
    private String dns1;
    /**
     * 需校验输入值是否为有效IP
     */
    private String dns2;
    /**
     * required when type is pppoe	校验长度不超过128位
     */
    private String pppoe_username;
    /**
     * 校验长度不超过128位
     */
    private String pppoe_password;

    /**
     * 不超过32个字符
     */
    private String ssid;
    private String ssid_2G;
    private String ssid_5G;
    /**
     * 不超过64个字符
     */
    private String key;
    /**
     * string	required	none/wpa2-psk/wpa2_mixed_psk
     */
    private String encryption;
    /**
     * 0:不同步到登录密码；1：同步
     */
    private Integer key_sync;

    /**
     * 1：5g优先；0：2.4g优先
     */
    @SerializedName("5G_priority")
    private Integer _5G_priority;

    /**
     * 1 为设置完成。0 为设置失败或取消掉了。2为只设置完登陆密码，而后的设置向导并非完成。
     */
    private Integer guid_flag;

    public Integer getSrc_type() {
        return src_type;
    }

    public String getType() {
        return type;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public String getNetmask() {
        return netmask;
    }

    public String getGw() {
        return gw;
    }

    public String getDns1() {
        return dns1;
    }

    public String getDns2() {
        return dns2;
    }

    public String getPppoe_username() {
        return pppoe_username;
    }

    public String getPppoe_password() {
        return pppoe_password;
    }

    public String getSsid() {
        return ssid;
    }

    public String getKey() {
        return key;
    }

    public String getEncryption() {
        return encryption;
    }

    public Integer getKey_sync() {
        return key_sync;
    }

    public Integer get_5G_priority() {
        return _5G_priority;
    }

    public Integer getGuid_flag() {
        return guid_flag;
    }

    public String getSsid_2G() {
        return ssid_2G;
    }

    public String getSsid_5G() {
        return ssid_5G;
    }

    @Override
    public String toString() {
        return "ResponseAllBeen{" +
                "src_type=" + src_type +
                ", type='" + type + '\'' +
                ", ipaddr='" + ipaddr + '\'' +
                ", netmask='" + netmask + '\'' +
                ", gw='" + gw + '\'' +
                ", dns1='" + dns1 + '\'' +
                ", dns2='" + dns2 + '\'' +
                ", pppoe_username='" + pppoe_username + '\'' +
                ", pppoe_password='" + pppoe_password + '\'' +
                ", ssid='" + ssid + '\'' +
                ", ssid_2G='" + ssid_2G + '\'' +
                ", ssid_5G='" + ssid_5G + '\'' +
                ", key='" + key + '\'' +
                ", encryption='" + encryption + '\'' +
                ", key_sync=" + key_sync +
                ", _5G_priority=" + _5G_priority +
                ", guid_flag=" + guid_flag +
                ", err_code=" + err_code +
                ", message='" + message + '\'' +
                ", waite_time=" + waite_time +
                '}';
    }
}
