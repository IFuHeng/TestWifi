package com.changhong.wifimng.http.been;

import com.changhong.wifimng.been.DeviceItem;
import com.changhong.wifimng.been.DeviceType;

import java.util.List;

/**
 * 电力猫工况
 */
public class PLCModemStateBeen {

    private String mac;
    private String userId;
    private String heartbeatFreq;
    private Long reportTime;

    private StateValue stateValue;

    public String getMac() {
        return mac;
    }

    public String getUserId() {
        return userId;
    }

    public String getHeartbeatFreq() {
        return heartbeatFreq;
    }

    public Long getReportTime() {
        return reportTime;
    }

    public StateValue getStateValue() {
        return stateValue;
    }

    @Override
    public String toString() {
        return "PLCModemStateBeen{" +
                "mac='" + mac + '\'' +
                ", userId='" + userId + '\'' +
                ", heartbeatFreq='" + heartbeatFreq + '\'' +
                ", reportTime=" + reportTime +
                ", stateValue=" + stateValue +
                '}';
    }

    public static class StateValue {
        /**
         * 设备MAC地址
         */
        private String dev_mac;
        /**
         * 设备WAN口IPV4地址
         */
        private String ipv4_addr;
        /**
         * 设备WAN口IPV6地址
         */
        private String ipv6_addr;
        /**
         * 设备固件版本
         */
        private String sw_ver;
        /**
         * 当前设备的wifi ssid，暂只考虑1个SSID，一般都会用双频优选功能
         */
        private String ssid;
        /**
         * 设备运行时间，以秒为单位，重新上电后会自动重新从零计时
         */
        private Integer running_time;
        /**
         * 设备WAN口下载速率,单位为Kbps
         */
        private Integer down_speed;
        /**
         * 设备WAN口上传速率,单位为Kbps
         */
        private Integer up_speed;
        /**
         * 组网系统中已接入的CPE数量
         */
        private Integer attached_dev_num;
        /**
         * 组网子设备结点列表
         */
        private List<Slave> slave_list;
        /**
         * 接入设备结点列表
         */
        private List<Cpe> cpe_list;

        public String getDev_mac() {
            return dev_mac;
        }

        public String getIpv4_addr() {
            return ipv4_addr;
        }

        public String getIpv6_addr() {
            return ipv6_addr;
        }

        public String getSw_ver() {
            return sw_ver;
        }

        public String getSsid() {
            return ssid;
        }

        public Integer getRunning_time() {
            return running_time;
        }

        public Integer getDown_speed() {
            return down_speed;
        }

        public Integer getUp_speed() {
            return up_speed;
        }

        public Integer getAttached_dev_num() {
            return attached_dev_num;
        }

        public List<Slave> getSlave_list() {
            return slave_list;
        }

        public List<Cpe> getCpe_list() {
            return cpe_list;
        }

        @Override
        public String toString() {
            return "StateValue{" +
                    "dev_mac='" + dev_mac + '\'' +
                    ", ipv4_addr='" + ipv4_addr + '\'' +
                    ", ipv6_addr='" + ipv6_addr + '\'' +
                    ", sw_ver='" + sw_ver + '\'' +
                    ", ssid='" + ssid + '\'' +
                    ", running_time=" + running_time +
                    ", down_speed=" + down_speed +
                    ", up_speed=" + up_speed +
                    ", attached_dev_num=" + attached_dev_num +
                    ", slave_list=" + slave_list +
                    ", cpe_list=" + cpe_list +
                    '}';
        }
    }

    public static class Slave {
        /*"slave_name": "pn200_0915",
                "slave_mac": "98:2f:3c:83:09:15",
                "slave_ip_addr": "192.168.10.12",
                "slave_location": "bedroom",
                "slave_status": 1*/
        /**
         * 子设备名称, 若用户未配置则使用默认名称
         */
        private String slave_name;
        /**
         * 子设备MAC
         */
        private String slave_mac;
        /**
         * 子设备IP地址
         */
        private String slave_ip_addr;
        /**
         * 子设备位置信息，由用户配置，默认为客厅
         */
        private String slave_location;
        /**
         * 0-离线(Offline), 1-在线(Online)
         */
        private Integer slave_status;

        public String getSlave_name() {
            return slave_name;
        }

        public String getSlave_mac() {
            return slave_mac;
        }

        public String getSlave_ip_addr() {
            return slave_ip_addr;
        }

        public String getSlave_location() {
            return slave_location;
        }

        public Integer getSlave_status() {
            return slave_status;
        }

        @Override
        public String toString() {
            return "Slave{" +
                    "slave_name='" + slave_name + '\'' +
                    ", slave_mac='" + slave_mac + '\'' +
                    ", slave_ip_addr='" + slave_ip_addr + '\'' +
                    ", slave_location='" + slave_location + '\'' +
                    ", slave_status=" + slave_status +
                    '}';
        }

        public DeviceItem getDeviceItem() {
            DeviceItem item = new DeviceItem();
            item.setType(DeviceType.PLC);
            item.setChild(true);
            item.setDeviceName(slave_name);
            item.setIp(slave_ip_addr);
            item.setMac(slave_mac);
            item.setLocation(slave_location);
            item.setUpConnected(slave_status == 1);
            return item;
        }
    }

    public static class Cpe {
        /**
         * CPE名称, 未获取到就传空值
         */
        private String cpe_name;
        /**
         * CPE MAC
         */
        private String cpe_mac;
        /**
         * CPE IP地址
         */
        private String cpe_ip_addr;
        /**
         * 对应的接入组网设备MAC地址
         */
        private String access_node;
        /**
         * CPE已连接时间，以秒为单位
         */
        private Integer attached_time;
        /**
         * 0-未知(Unknown), 1-有线(LAN), 2-2.4G, 3-5G
         */
        private Integer attached_method;


        /* "cpe_name": "Bob’s phone",
            "cpe_mac": "98:2f:3c:83:09:25",
            "cpe_ip_addr": "192.168.10.55",
            "access_node": "98:2f:3c:83:09:12",
            "attached_time": 120,
            "attached_method": 1*/

        public String getCpe_name() {
            return cpe_name;
        }

        public String getCpe_mac() {
            return cpe_mac;
        }

        public String getCpe_ip_addr() {
            return cpe_ip_addr;
        }

        public String getAccess_node() {
            return access_node;
        }

        public Integer getAttached_time() {
            return attached_time;
        }

        public Integer getAttached_method() {
            return attached_method;
        }

        @Override
        public String toString() {
            return "Cpe{" +
                    "cpe_name='" + cpe_name + '\'' +
                    ", cpe_mac='" + cpe_mac + '\'' +
                    ", cpe_ip_addr='" + cpe_ip_addr + '\'' +
                    ", access_node='" + access_node + '\'' +
                    ", attached_time=" + attached_time +
                    ", attached_method=" + attached_method +
                    '}';
        }
    }

}
