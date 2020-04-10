package com.changhong.wifimng.task;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.guide.ResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.router.BaseRouterTask;
import com.google.gson.Gson;

import java.util.Map;

public class GetWizardWifiTask extends BaseRouterTask {
    public final GetWizardWifiTask execute(String gateway, String deviceType, String cookies, TaskListener<ResponseAllBeen> listener) {
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
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wizard_wireless_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                com.changhong.wifimng.been.guide.ResponseAllBeen been = new Gson().fromJson(response, com.changhong.wifimng.been.guide.ResponseAllBeen.class);
                if (been.getErr_code() == 0) {
                    publishProgress(been);
                } else {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }

            // mesh or router
            else {
                String url = "http://" + gateway + ":80/rpc";
                com.changhong.wifimng.been.guide.RequireAllBeen requireBody = new com.changhong.wifimng.been.guide.RequireAllBeen();

                response = DefaultHttpUtils.httpPostWithJson(url,
                        new RequestBeen(HttpRequestMethod.METHOD_WIZARD_GET_WIRELESS, requireBody).toJsonString(),
                        cookies
                );

                com.changhong.wifimng.been.guide.ResponseAllBeen been
                        = new Gson().fromJson(response, com.changhong.wifimng.been.guide.ResponseAllBeen.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                publishProgress(been);
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
