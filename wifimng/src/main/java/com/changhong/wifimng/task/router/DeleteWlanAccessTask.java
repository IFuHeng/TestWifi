package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.wifi.ReqireWlanAccessDelBeen;
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
 * 获取访问控制信息
 */
public class DeleteWlanAccessTask extends BaseRouterTask {
    public final DeleteWlanAccessTask execute(String gateway, String deviceType, String mac, boolean isEffectImmediately,  String cookies, TaskListener<Boolean> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put("deviceType", deviceType);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        params.put("mac", mac);
        params.put("isEffectImmediately", isEffectImmediately);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String deviceType = params[0].getString("deviceType");
        String mac = params[0].get("mac");
        boolean isEffectImmediately = params[0].get("isEffectImmediately");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);

        try {
            Gson gson = new Gson();
            String response;
            if (DeviceType.PLC.getName().equals(deviceType)) {// 电力猫的情况
                final String url = String.format("http://%s:8081/rpc", gateway);
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_access_control_del\",\"param\":{\"src_type\":1,\"mac\":\"" + mac + "\"}}";
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
                mac = mac.replaceAll(":", "");
                ArrayList<String> macList = new ArrayList<>();
                macList.add(mac);
                ReqireWlanAccessDelBeen requireBeen = new ReqireWlanAccessDelBeen();
                requireBeen.setList(macList);
                requireBeen.setAction_flag(isEffectImmediately ? 1 : 0);
                String url = "http://" + gateway + ":80/rpc";
                response = DefaultHttpUtils.httpPostWithJson(url,
                        new RequestBeen<>(HttpRequestMethod.METHOD_ACCESS_DEL, requireBeen).toJsonString(), cookies);
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
