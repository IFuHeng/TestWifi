package com.changhong.wifimng.http.been;

import com.changhong.wifimng.been.DeviceType;

import java.util.List;

/**
 * 组网工况
 */
public class MeshStateBeen {
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

    public static class StateValue {
        private Integer connected_num;
        private List<NetItem> net_list;
        private String ip;
        private String ssid;
        private Integer link_time;
        private String version;
        private Rate rate;
        private List<DeviceItem> device_list;

        public Integer getConnected_num() {
            return connected_num;
        }

        public List<NetItem> getNet_list() {
            return net_list;
        }

        public String getIp() {
            return ip;
        }

        public String getSsid() {
            return ssid;
        }

        public Integer getLink_time() {
            return link_time;
        }

        public String getVersion() {
            return version;
        }

        public Rate getRate() {
            return rate;
        }

        public List<DeviceItem> getDevice_list() {
            return device_list;
        }
    }

    public static class Rate {

        /**
         * 上行速度 单位Kbps
         */
        private Integer upload;
        /**
         * 下行速度 单位Kbps
         */
        private Integer download;

        public Integer getUpload() {
            return upload;
        }

        public Integer getDownload() {
            return download;
        }

        @Override
        public String toString() {
            return "Rate{" +
                    "upload=" + upload +
                    ", download=" + download +
                    '}';
        }

    }

    public static class NetItem {

        /**
         * 组网设备mac
         */
        private String mac;
        /**
         * 组网设备IP
         */
        private String ip;
        /**
         * not nessary 组网设备名称
         */
        private String name;
        /**
         * 组网设备上级节点
         */
        private String up_node;
        /**
         * 组网设备状态, connected:已连接,..
         */
        private String state;

        private List<DeviceItem> device_list;

        public String getMac() {
            return mac;
        }

        public String getIp() {
            return ip;
        }

        public String getName() {
            return name;
        }

        public String getUp_node() {
            return up_node;
        }

        public String getState() {
            return state;
        }

        public List<DeviceItem> getDevice_list() {
            return device_list;
        }

        public com.changhong.wifimng.been.DeviceItem getDeviceItem() {
            com.changhong.wifimng.been.DeviceItem item = new com.changhong.wifimng.been.DeviceItem();
            item.setIp(ip);
            item.setMac(mac);
            item.setUpNodeName(up_node);
            item.setDeviceName(name);
            item.setUpConnected("connected".equalsIgnoreCase(state));
            item.setStaNum(device_list != null ? device_list.size() : 0);
            return item;
        }

        @Override
        public String toString() {
            return "NetItem{" +
                    "mac='" + mac + '\'' +
                    ", ip='" + ip + '\'' +
                    ", name='" + name + '\'' +
                    ", up_node='" + up_node + '\'' +
                    ", state='" + state + '\'' +
                    ", device_list=" + device_list +
                    '}';
        }
    }

    class DeviceItem {
        String mac;
        String ip;
        String name;
        String access_point;
        /**
         * 在线时间 秒
         */
        Integer link_time;
        /**
         * 1 有线 2、2.4G 4、5G, 8、访客网络
         */
        Integer link_mode;
        Long upload;
        Long download;

        public String getMac() {
            return mac;
        }

        public String getIp() {
            return ip;
        }

        public String getName() {
            return name;
        }

        public String getAccess_point() {
            return access_point;
        }

        public Integer getLink_time() {
            return link_time;
        }

        public Integer getLink_mode() {
            return link_mode;
        }

        public Long getUpload() {
            return upload;
        }

        public Long getDownload() {
            return download;
        }

        @Override
        public String toString() {
            return "DeviceItem{" +
                    "mac='" + mac + '\'' +
                    ", ip='" + ip + '\'' +
                    ", name='" + name + '\'' +
                    ", access_point='" + access_point + '\'' +
                    ", link_time=" + link_time +
                    ", link_mode=" + link_mode +
                    ", upload=" + upload +
                    ", download=" + download +
                    '}';
        }
    }

}
