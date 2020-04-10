package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.wifi.RequireAllBeen;
import com.changhong.wifimng.been.wifi.ResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

/**
 * 获取WAN口设置
 */
public class SetWifiAdvanceSettingTask extends BaseRouterTask {

    public final SetWifiAdvanceSettingTask execute(String gateway, RequireAllBeen info, String cookies, TaskListener listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        params.put("info", info);
        setListener(listener);
        execute(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        RequireAllBeen info = params[0].get("info");
        String url = "http://" + gateway + ":80/rpc";
        try {
            commit(url, HttpRequestMethod.METHOD_WLAN_ADVANCED_SETTING, cookies, info);
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            if (e instanceof AuthenticatorException)
                return dealAuthenticatorException(gateway, params);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }

    private <T> void commit(String url, String method, String cookies, T t) throws Exception {
        RequestBeen requestBeen = new RequestBeen(method, t);
        String response = DefaultHttpUtils.httpPostWithJson(url, requestBeen.toJsonString(), cookies);
        ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
        if (been.getErr_code() != 0) {
            throw new Exception(been.getMessage());
        }
    }


}
