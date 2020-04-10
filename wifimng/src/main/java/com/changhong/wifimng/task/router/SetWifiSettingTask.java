package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

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

import java.util.HashMap;

/**
 * 获取WAN口设置
 */
public class SetWifiSettingTask extends BaseRouterTask {

    public final SetWifiSettingTask execute(String gateway, HashMap<String, RequireAllBeen> info, String cookies, TaskListener listener) {
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
        HashMap<String, RequireAllBeen> info = params[0].get("info");
        String url = "http://" + gateway + ":80/rpc";

        try {

            if (info.get(KeyConfig.KEY_24G).getPrefer_5g() == 1)
                info.get(KeyConfig.KEY_24G).setFlag(0);

            if (info.containsKey(KeyConfig.KEY_WIFI_ADVANCE))
                commit(url, HttpRequestMethod.METHOD_WLAN_ADVANCED_SETTING, cookies, info.get(KeyConfig.KEY_WIFI_ADVANCE));

            if (info.containsKey(KeyConfig.KEY_5G)) {
                commit(url, HttpRequestMethod.METHOD_WLAN_QUICK_SETTING, cookies, info.get(KeyConfig.KEY_24G));
                commit(url, HttpRequestMethod.METHOD_WLAN_QUICK_SETTING, cookies, info.get(KeyConfig.KEY_5G));
            } else {
                commit(url, HttpRequestMethod.METHOD_WLAN_QUICK_SETTING, cookies, info.get(KeyConfig.KEY_24G));
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

    private <T> void commit(String url, String method, String cookies, T t) throws Exception {
        RequestBeen requestBeen = new RequestBeen(method, t);
        String response = DefaultHttpUtils.httpPostWithJson(url, requestBeen.toJsonString(), cookies);
        ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
        if (been.getErr_code() != 0) {
            throw new Exception(been.getMessage());
        }
    }


}
