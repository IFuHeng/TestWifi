package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.StaInfo;
import com.changhong.wifimng.been.wifi.ReqireWlanAccessDelBeen;
import com.changhong.wifimng.been.wifi.RequireAllBeen;
import com.changhong.wifimng.been.wifi.ResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

/**
 * 提交访问控制权限
 */
public class SetWlanAccessTask extends BaseRouterTask {
    public final SetWlanAccessTask execute(String gateway, String deviceType, int enabled, StaInfo staInfo, boolean isEffectImmediately, String cookies, TaskListener<Boolean> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        params.put("enabled", enabled);
        params.put("staInfo", staInfo);
        params.put("deviceType", deviceType);
        params.put("isEffectImmediately", isEffectImmediately);
        setListener(listener);
        execute(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String deviceType = params[0].getString("deviceType");
        StaInfo staInfo = params[0].get("staInfo");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        int enabled = params[0].get("enabled");
        boolean isEffectImmediately = params[0].get("isEffectImmediately");

        try {
            Gson gson = new Gson();

            String response;
            if (DeviceType.PLC.getName().equals(deviceType)) {// 电力猫的情况
                final String url = String.format("http://%s:8081/rpc", gateway);
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_access_control_setting\",\"param\":{\"src_type\":1,\"enabled\":" + enabled + "}}";
//                if (enabled == 1 && staInfo != null) {//白名单模式下要先提交后添加当前设备
//                    body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_access_control_add_ex\",\"param\":{\"src_type\":1,\"enabled\":" + enabled +
//                            ",\"mac\":\"" + staInfo.getMac() + "\",\"name\":\"" + staInfo.getName() + "\"}}";
//                }
                response = DefaultHttpUtils.httpPostWithText(url, body);
                if (response == null || response.length() == 0)
                    throw new Exception("wait for develop.");
                ResponseAllBeen been = gson.fromJson(response, ResponseAllBeen.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
//                if (enabled == 1 && staInfo != null) {//STEP1 白名单模式下要先提交后添加当前设备
//                    addDevice2ListPLC(url, staInfo.getMac());
//                }

            }
//
            else {//组网和千兆，白名单要先添加本设备，后修改模式；黑名单要先提出本设备，再修改模式
                //并且黑白名单切换，需要先关闭访问限制

                String url = "http://" + gateway + ":80/rpc";


//                Integer curEnabled = getAccess(url, cookies).getEnabled();
//                if ((curEnabled == 1 && enabled == 2) || (curEnabled == 2 && enabled == 1)) {
//                    setAccess(url, 0, true, cookies);
//                }
//
//                if (staInfo != null) {
//                    if (enabled == 1)
//                        addDevice2List(url, staInfo.getName(), staInfo.getMac(), cookies);
//                    else if (enabled == 2)
//                        delDeviceFromList(url, staInfo.getMac(), cookies);
//                }
                setAccess(url, enabled, true, cookies);

            }
            publishProgress(isEffectImmediately);
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            if (e instanceof AuthenticatorException)
                return dealAuthenticatorException(gateway, params);
            return TaskResult.IO_ERROR;
        }

        return TaskResult.OK;
    }


    private ResponseAllBeen getAccess(String url, Map<String, String> cookies) throws Exception {
        String response = DefaultHttpUtils.httpPostWithJson(url,
                new RequestBeen<>(HttpRequestMethod.METHOD_ACCESS_SHOW, new RequireAllBeen()).toJsonString(), cookies);
        ResponseAllBeen responseAllBeen = new Gson().fromJson(response, ResponseAllBeen.class);
        if (responseAllBeen.getErr_code() != 0) {
            throw new Exception(responseAllBeen.getMessage());
        }
        return responseAllBeen;
    }

    private void setAccess(String url, int enabled, boolean isEffectImmediately, String cookies) throws Exception {
        RequireAllBeen been = new RequireAllBeen();
        been.setEnabled(enabled);
        been.setAction_flag(isEffectImmediately ? 1 : 0);
        String response = DefaultHttpUtils.httpPostWithJson(url,
                new RequestBeen<>(HttpRequestMethod.METHOD_ACCESS_SETTING, been).toJsonString(), cookies);
        ResponseAllBeen responseAllBeen = new Gson().fromJson(response, ResponseAllBeen.class);

        if (responseAllBeen.getErr_code() != 0) {
            throw new Exception(responseAllBeen.getMessage());
        }
    }

    private void delDeviceFromList(String url, String mac, String cookies) throws Exception {
        mac = mac.replaceAll(":", "");
        ArrayList<String> macList = new ArrayList<>();
        macList.add(mac);
        ReqireWlanAccessDelBeen requireBeen = new ReqireWlanAccessDelBeen();
        requireBeen.setList(macList);
        requireBeen.setAction_flag(0);
        String response = DefaultHttpUtils.httpPostWithJson(url,
                new RequestBeen<>(HttpRequestMethod.METHOD_ACCESS_DEL, requireBeen).toJsonString(), cookies);
        ResponseAllBeen responseAllBeen = new Gson().fromJson(response, ResponseAllBeen.class);

        if (responseAllBeen.getErr_code() != 0) {
            throw new Exception(responseAllBeen.getMessage());
        }
    }

    private void addDevice2List(String url, String name, String mac, String cookies) throws Exception {
        mac = mac.replaceAll(":", "");
        ArrayList<String> macList = new ArrayList<>();
        macList.add(mac);
        RequireAllBeen requireBeen = new RequireAllBeen();
        requireBeen.setName(name);
        requireBeen.setMac(mac);
        String response = DefaultHttpUtils.httpPostWithJson(url,
                new RequestBeen<>(HttpRequestMethod.METHOD_ACCESS_ADD, requireBeen).toJsonString(), cookies);
        ResponseAllBeen responseAllBeen = new Gson().fromJson(response, ResponseAllBeen.class);

        if (responseAllBeen.getErr_code() != 0) {
            throw new Exception(responseAllBeen.getMessage());
        }
    }

    private void addDevice2ListPLC(String url, String mac) throws Exception {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_access_control_del\",\"param\":{\"src_type\":1,\"mac\":\"" + mac + "\"}}";
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        if (response == null || response.length() == 0)
            throw new Exception("wait for develop.");
        ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
        if (been.getErr_code() != 0) {
            throw new Exception(been.getMessage());
        }
    }
}
