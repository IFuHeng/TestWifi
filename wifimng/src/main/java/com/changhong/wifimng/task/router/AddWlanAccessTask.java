package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.wifi.RequireAllBeen;
import com.changhong.wifimng.been.wifi.ResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

import java.util.Map;

/**
 * 获取访问控制信息
 */
public class AddWlanAccessTask extends BaseRouterTask {
    public final AddWlanAccessTask execute(String gateway, String deviceType, String name, String mac, boolean isEffectImmediately,  String cookies, TaskListener<Boolean> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        params.put("deviceType", deviceType);
        params.put("name", name);
        params.put("isEffectImmediately", isEffectImmediately);
        params.put("mac", mac);
        setListener(listener);
        execute(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String deviceType = params[0].getString("deviceType");
        String mac = params[0].getString("mac");
        String name = params[0].getString("name");
        boolean isEffectImmediately = params[0].get("isEffectImmediately");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);

        try {
            Gson gson = new Gson();
            String response;
            if (DeviceType.PLC.getName().equals(deviceType)) {// 电力猫的情况
                final String url = String.format("http://%s:8081/rpc", gateway);
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_access_control_add\",\"param\":{\"src_type\":1,\"name\":\"" + name + "\",\"mac\":\"" + mac + "\"}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                if (response == null || response.length() == 0)
                    throw new Exception("wait for develop.");
                ResponseAllBeen been = gson.fromJson(response, ResponseAllBeen.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }
//
            else {
                if (mac.indexOf(':') != -1)
                    mac = mac.replaceAll(":", "");
                String url = "http://" + gateway + ":80/rpc";
                RequireAllBeen requireBeen = new RequireAllBeen();
                requireBeen.setName(name);
                requireBeen.setMac(mac);
                requireBeen.setAction_flag(isEffectImmediately ? 1 : 0);
                response = DefaultHttpUtils.httpPostWithJson(url,
                        new RequestBeen<>(HttpRequestMethod.METHOD_ACCESS_ADD, requireBeen).toJsonString(), cookies);
                ResponseAllBeen responseAllBeen = gson.fromJson(response, ResponseAllBeen.class);

                if (responseAllBeen.getErr_code() != 0) {
                    setException(new Exception(responseAllBeen.getMessage()));
                    return TaskResult.FAILED;
                }
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

}
