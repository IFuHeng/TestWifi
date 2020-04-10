package com.changhong.wifimng.task;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.BaseResponseBeen;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.sys.SettingResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.router.BaseRouterTask;
import com.google.gson.Gson;

import java.util.Map;

/**
 * 恢复出厂设置
 */
public class RecoveryTask extends BaseRouterTask {

    public final RecoveryTask execute(String gateway, String deviceType,  String cookies, TaskListener<String> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put("deviceType", deviceType);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        setListener(listener);
        execute(params);
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
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"factory_default\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }

            // mesh or router
            else {
                String url = "http://" + gateway + ":80/rpc";
                response = DefaultHttpUtils.httpPostWithJson(url,
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"" + HttpRequestMethod.METHOD_RESET + "\",\"params\":{\"src_type\":1}}",
                        cookies
                );
                SettingResponseAllBeen been
                        = new Gson().fromJson(response, SettingResponseAllBeen.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }
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
