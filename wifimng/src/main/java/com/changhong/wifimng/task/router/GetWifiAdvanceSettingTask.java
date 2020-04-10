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
 * 获取WIFI设置信息和高级设置信息
 */
public class GetWifiAdvanceSettingTask extends BaseRouterTask {

    public final GetWifiAdvanceSettingTask execute(String gateway, String cookies, TaskListener<ResponseAllBeen> listener) {
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
            // 获取wifi高级设置信息
            String response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen(HttpRequestMethod.METHOD_WLAN_ADVANCED_SHOW, new RequireAllBeen()).toJsonString(), cookies);
            ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
            if (been.getErr_code() != 0)
                throw new Exception(been.getMessage());

            publishProgress(been);
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
