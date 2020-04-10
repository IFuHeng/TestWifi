package com.changhong.wifimng.task;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.BaseResponseBeen;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.WanType;
import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.been.sys.SettingRequireAllBeen;
import com.changhong.wifimng.been.sys.SettingResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.router.BaseRouterTask;
import com.google.gson.Gson;

/**
 * 获取状态信息
 */
public class GetRouterInfoTask extends BaseRouterTask {
    public final GetRouterInfoTask execute(String gateway, String deviceType, String cookies, TaskListener<SettingResponseAllBeen> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        params.put("deviceType", deviceType);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String deviceType = params[0].getString("deviceType");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        try {

            String response;
            if (DeviceType.PLC.getName().equals(deviceType)) {// 电力猫的情况
                final String url = String.format("http://%s:8081/rpc", gateway);
                SettingResponseAllBeen responseAllBeen;
                {//                step1

                    String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wizard_wireless_show\",\"param\":{\"src_type\":1}}";
                    response = DefaultHttpUtils.httpPostWithText(url, body);
                    PLCDeviceInfo been = new Gson().fromJson(response, PLCDeviceInfo.class);
                    if (been.getErr_code() != 0) {
                        setException(new Exception(been.getMessage()));
                        return TaskResult.FAILED;
                    }
                    responseAllBeen = been.toRouterResponseBeen();
                }

                {//                step2
                    String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_running_status_show\",\"param\":{\"src_type\":1}}";
                    response = DefaultHttpUtils.httpPostWithText(url, body);
                    PLCDeviceInfo been = new Gson().fromJson(response, PLCDeviceInfo.class);
                    if (been.getErr_code() != 0) {
                        setException(new Exception(been.getMessage()));
                        return TaskResult.FAILED;
                    }
                    been.set(responseAllBeen);
                }
                {//                step3
                    String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ipv4_status_show\",\"param\":{\"src_type\":1}}";
                    response = DefaultHttpUtils.httpPostWithText(url, body);
                    PLCDeviceInfo been = new Gson().fromJson(response, PLCDeviceInfo.class);
                    if (been.getErr_code() != 0) {
                        setException(new Exception(been.getMessage()));
                        return TaskResult.FAILED;
                    }
                    been.setIpv4Info(responseAllBeen);
                }
                if (responseAllBeen.getWan_mac() == null) {// step4
                    String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_basic_show\",\"param\":{\"src_type\":1}}";
                    response = DefaultHttpUtils.httpPostWithText(url, body);
                    PLCInfo been = new Gson().fromJson(response, PLCInfo.class);
                    if (been.getErr_code() != 0) {
                        setException(new Exception(been.getMessage()));
                        return TaskResult.FAILED;
                    }
                    responseAllBeen.setWan_mac(been.getDev_mac());
                }
                publishProgress(responseAllBeen);
                return TaskResult.OK;
            }

            // mesh or router
            else {
                String url = "http://" + gateway + ":80/rpc";

                response = DefaultHttpUtils.httpPostWithJson(url,
                        new RequestBeen(HttpRequestMethod.METHOD_ROUTER_INFO_SHOW, new SettingRequireAllBeen()).toJsonString(),
                        cookies
                );
                SettingResponseAllBeen responseAllBeen = new Gson().fromJson(response, SettingResponseAllBeen.class);
                if (responseAllBeen.getErr_code() != 0) {
                    setException(new Exception(responseAllBeen.getMessage()));
                } else {
                    publishProgress(responseAllBeen);
                    return TaskResult.OK;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            if (e instanceof AuthenticatorException)
                return dealAuthenticatorException(gateway, params);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.FAILED;
    }

    class PLCDeviceInfo extends BaseResponseBeen {
        //step 1
        String dev_model; // String  required  设备型号
        String manufacturer;// String  required  生产厂家
        String dev_type; // String  required  设备类型
        String dev_identity; // String  required  设备标识(SN)
        String hw_ver; //String  required  硬件版本
        String sw_ver;// String  required  软件版本
        String dev_desc; //String  required  设备描述
        String dev_mac; //String  required  设备 mac 地址
        //step 2
        Integer uptime; //  required  设备上电后时间，单位为秒
        String wan_ip; //  required  IP 地址: AA:BB:CC:DD
        Integer upstream_rate; //   required  上行速率，单位：Kbps
        Integer downstream_rate; //   required  下行速率，单位：Kbps
        Integer lan_interface; //   required  LAN 接口，bit0: 有线; bit1:2.4G; bit2: 5G 如 7 表示 LAN 具有有线、2.4G和5G
        Integer sta_num;  // required  下挂终端总数（接入设备总量）
        Integer network_time; // ; required  上网时长，单位为秒
        //step3
        String conn_name;//	String	required	连接网络接口名称
        Integer enable_state;//	Integer	required	使能状态 0: disable    1: enable
        Integer conn_status;//	Integer	required	连接状态0: unconfigured  1: connecting 2: authenticating  3: connected 4: pending-disconnect  5: disconnecting 6: disconnected
        Integer addr_type;//	required	地址获取方式，1: static  2: dhcp  3: pppoe
        String ip_addr;//	required	IP地址
        String mask;//	required	子网掩码
        String default_gw;//	required	默认网关
        String pri_dns;//	required	首选DNS
        String sec_dns;//		required	备用DNS


        private SettingResponseAllBeen toRouterResponseBeen() {
            SettingResponseAllBeen responseAllBeen = new SettingResponseAllBeen();
            responseAllBeen.setDev_type(dev_type);
            responseAllBeen.setSN(dev_identity);
            responseAllBeen.setSoft_ver(sw_ver);
            responseAllBeen.setWan_mac(dev_mac);
            responseAllBeen.setEquipment(dev_desc);
            return responseAllBeen;
        }

        private SettingResponseAllBeen set(SettingResponseAllBeen been) {
            been.setUptime(uptime);
            been.setWan_ip(wan_ip);
            return been;
        }

        private SettingResponseAllBeen setIpv4Info(SettingResponseAllBeen been) {
            if (conn_status == null || conn_status != 3)
                been.setWan_ip("none_link");
            else
                been.setWan_ip(ip_addr);
            been.setWan_type(WanType.getDeviceTypeFromTypeCode(addr_type).getName());
            return been;
        }
    }

}
