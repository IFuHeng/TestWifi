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
import java.util.Map;

/**
 * 获取WIFI设置信息和高级设置信息
 */
public class GetWifiSettingTask extends BaseRouterTask {

    public final GetWifiSettingTask execute(String gateway, String cookies, TaskListener<HashMap<String, ResponseAllBeen>> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        String url = "http://" + gateway + ":80/rpc";
        try {
            HashMap<String, ResponseAllBeen> result = loadWifiBase(url, cookies);


            {// 获取wifi高级设置信息
                String response = DefaultHttpUtils.httpPostWithJson(url,
                        new RequestBeen(HttpRequestMethod.METHOD_WLAN_ADVANCED_SHOW, new RequireAllBeen()).toJsonString(), cookies);
                ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
                if (been.getErr_code() != 0) {
                    throw new Exception(been.getMessage());
                }
                result.put(KeyConfig.KEY_WIFI_ADVANCE, been);
            }
            publishProgress(result);
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            if (e instanceof AuthenticatorException) {
                return dealAuthenticatorException(gateway, params);
            }
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }

    private HashMap<String, ResponseAllBeen> loadWifiBase(String url, String cookies) throws Exception {

        HashMap<String, ResponseAllBeen> result = new HashMap<>();

        RequireAllBeen requireBeen = new RequireAllBeen();
        RequestBeen requestBeen = new RequestBeen(HttpRequestMethod.METHOD_WLAN_QUICK_SHOW, requireBeen);

        String response;
//获取5G wifi信息
        requireBeen.setFlag(2);
        response = DefaultHttpUtils.httpPostWithJson(url,
                requestBeen.toJsonString(), cookies);
        ResponseAllBeen been5G = new Gson().fromJson(response, ResponseAllBeen.class);
        if (been5G.getErr_code() != 0) {
            throw new Exception(been5G.getMessage());
        } else if (been5G.getPrefer_5g() == 1) {
            result.put(KeyConfig.KEY_24G, been5G);
            return result;
        } else
            result.put(KeyConfig.KEY_5G, been5G);
//获取2.4G wifi信息
        requireBeen.setFlag(1);
        response = DefaultHttpUtils.httpPostWithJson(url, requestBeen.toJsonString(), cookies);
        ResponseAllBeen been24G = new Gson().fromJson(response, ResponseAllBeen.class);
        if (been24G.getErr_code() != 0) {
            throw new Exception(been24G.getMessage());
        }
        result.put(KeyConfig.KEY_24G, been24G);
        return result;
    }

}
