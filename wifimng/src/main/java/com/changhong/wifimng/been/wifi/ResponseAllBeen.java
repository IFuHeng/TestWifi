package com.changhong.wifimng.been.wifi;

import com.changhong.wifimng.been.BaseResponseBeen;
import com.changhong.wifimng.been.Group;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class ResponseAllBeen extends BaseResponseBeen {

    /**
     * 0:disabled;1:enabled
     */
    private Integer enabled;
    /**
     * required when enabled is 1
     * 0:2.4G和5G同时设置(prefer_5g 为1的时候该值默认为0)
     * 1：只配置2.4G
     * 2：只配置5G
     */
    private Integer flag;
    /**
     * 0:关闭；1：开启
     */
    private Integer prefer_5g;
    /**
     * 不超过32字符
     */
    private String ssid;
    /**
     * none/wpa2-psk/wpa2_mixed_psk
     */
    private String encryption;
    /**
     * 不超过64字符
     */
    private String key;
    /**
     * 0:穿墙；1:标准；2：睡眠
     */
    private Integer txpower_mode;

    /**
     * 0-13有效范围。0：为自动
     */
    private Integer channel_2;
    /**
     * when band_2 is not 0 、1 、 2	0:20Mhz;1:40Mhz;3: 20/40Mhz
     */
    private Integer channel_width_2;
    /**
     * when band_2 is not 0 、1 、 2	0:长间隔；1：短间隔
     */
    private Integer shortgi_2;
    /**
     * 0:b;1:g;2:bg;7:n;9:gn;10:bgn
     */
    private Integer band_2;
    /**
     * 3: a;7:n; 11:an; 63: ac; 71: n+ac;75: a+n+ac
     */
    private Integer band_5;
    /**
     * 0:关闭；1：开启
     */
    private Integer hidden_2;
    /**
     * 0:关闭；1：开启
     */
    private Integer wmm_2;
    /**
     * 0：为自动，其余值为：
     * 36、40、44、48、52、56、60、149、153、157、161（仅中国区域）
     */
    private Integer channel_5;
    /**
     * when band_2 is not 3 	0:20Mhz;1: 40Mhz;2:80Mhz;
     */
    private Integer channel_width_5;
    /**
     * when band_2 is not 3	0:长间隔；1：短间隔
     */
    private Integer shortgi_5;
    /**
     * 0:关闭；1：开启
     */
    private Integer hidden_5;
    /**
     * 0:关闭；1：开启
     */
    private Integer wmm_5;
    /**
     * 0：延时30分钟。其他值不延时
     */
    private Integer delay;
    /**
     * 0:不限时间，1-24表示上网小时数.该字段只有在delay不为0才有效
     */
    private Integer long_time;

    /**
     * 1:2.4G，0:5G
     */
    private Integer wlan_index;

    private List<Level2Been> ap_infos;
    private List<Level2Been> list;

    private Integer mac_num;
    private List<Group> mac_list;
    /**
     * Integer	required	返回还剩余的秒数
     */
    private Integer disable_time;

    public Integer getEnabled() {
        return enabled;
    }

    public Integer getFlag() {
        return flag;
    }

    public Integer getPrefer_5g() {
        return prefer_5g;
    }

    public String getSsid() {
        return ssid;
    }

    public String getEncryption() {
        return encryption;
    }

    public String getKey() {
        return key;
    }

    public Integer getTxpower_mode() {
        return txpower_mode;
    }

    public Integer getChannel_2() {
        return channel_2;
    }

    public Integer getChannel_width_2() {
        return channel_width_2;
    }

    public Integer getShortgi_2() {
        return shortgi_2;
    }

    public Integer getHidden_2() {
        return hidden_2;
    }

    public Integer getWmm_2() {
        return wmm_2;
    }

    public Integer getChannel_5() {
        return channel_5;
    }

    public Integer getChannel_width_5() {
        return channel_width_5;
    }

    public Integer getShortgi_5() {
        return shortgi_5;
    }

    public Integer getHidden_5() {
        return hidden_5;
    }

    public Integer getWmm_5() {
        return wmm_5;
    }

    public Integer getDelay() {
        return delay;
    }

    public Integer getLong_time() {
        return long_time;
    }

    public Integer getWlan_index() {
        return wlan_index;
    }

    public List<Level2Been> getAp_infos() {
        return ap_infos;
    }

    public Integer getBand_2() {
        return band_2;
    }

    public Integer getBand_5() {
        return band_5;
    }

    public Integer getDisable_time() {
        return disable_time;
    }

    public List<Level2Been> getList() {
        return list;
    }

    public Integer getMac_num() {
        return mac_num;
    }

    public List<Group> getMac_list() {
        return mac_list;
    }

    @Override
    public String toString() {
        return "ResponseAllBeen{" +
                "enabled=" + enabled +
                ", flag=" + flag +
                ", prefer_5g=" + prefer_5g +
                ", ssid='" + ssid + '\'' +
                ", encryption='" + encryption + '\'' +
                ", key='" + key + '\'' +
                ", txpower_mode=" + txpower_mode +
                ", channel_2=" + channel_2 +
                ", channel_width_2=" + channel_width_2 +
                ", shortgi_2=" + shortgi_2 +
                ", band_2=" + band_2 +
                ", band_5=" + band_5 +
                ", hidden_2=" + hidden_2 +
                ", wmm_2=" + wmm_2 +
                ", channel_5=" + channel_5 +
                ", channel_width_5=" + channel_width_5 +
                ", shortgi_5=" + shortgi_5 +
                ", hidden_5=" + hidden_5 +
                ", wmm_5=" + wmm_5 +
                ", delay=" + delay +
                ", long_time=" + long_time +
                ", wlan_index=" + wlan_index +
                ", ap_infos=" + ap_infos +
                ", disable_time=" + disable_time +
                ", err_code=" + err_code +
                ", message='" + message + '\'' +
                ", waite_time=" + waite_time +
                ", list=" + list +
                ", mac_num=" + mac_num +
                ", mac_list=" + mac_list +
                '}';
    }


    public RequireAllBeen clone() {
        RequireAllBeen newOne = new RequireAllBeen();
        for (Field field : getClass().getDeclaredFields()) {
            try {
                field.set(newOne, field.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return newOne;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResponseAllBeen)) return false;
        ResponseAllBeen that = (ResponseAllBeen) o;
        return Objects.equals(enabled, that.enabled) &&
                Objects.equals(flag, that.flag) &&
                Objects.equals(prefer_5g, that.prefer_5g) &&
                Objects.equals(ssid, that.ssid) &&
                Objects.equals(encryption, that.encryption) &&
                Objects.equals(key, that.key) &&
                Objects.equals(txpower_mode, that.txpower_mode) &&
                Objects.equals(channel_2, that.channel_2) &&
                Objects.equals(channel_width_2, that.channel_width_2) &&
                Objects.equals(shortgi_2, that.shortgi_2) &&
                Objects.equals(hidden_2, that.hidden_2) &&
                Objects.equals(wmm_2, that.wmm_2) &&
                Objects.equals(channel_5, that.channel_5) &&
                Objects.equals(channel_width_5, that.channel_width_5) &&
                Objects.equals(shortgi_5, that.shortgi_5) &&
                Objects.equals(hidden_5, that.hidden_5) &&
                Objects.equals(wmm_5, that.wmm_5) &&
                Objects.equals(delay, that.delay) &&
                Objects.equals(long_time, that.long_time) &&
                Objects.equals(wlan_index, that.wlan_index) &&
                Objects.equals(ap_infos, that.ap_infos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, flag, prefer_5g, ssid, encryption, key, txpower_mode, channel_2, channel_width_2, shortgi_2, hidden_2, wmm_2, channel_5, channel_width_5, shortgi_5, hidden_5, wmm_5, delay, long_time, wlan_index, ap_infos);
    }
}
