package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.wan.RequireAllBeen;
import com.changhong.wifimng.been.wan.ResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

public class SetLanInfoTask extends BaseRouterTask {
    public final SetLanInfoTask execute(String gateway, RequireAllBeen requireBeen, String cookies, TaskListener listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        params.put("requireBeen", requireBeen);
        setListener(listener);
        execute(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        RequireAllBeen requireBeen = params[0].get("requireBeen");
        try {
            String url = "http://" + gateway + ":80/rpc";

            String response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen(HttpRequestMethod.METHOD_LAN_SETTING, requireBeen).toJsonString(),
                    cookies
            );
            ResponseAllBeen responseAllBeen = new Gson().fromJson(response, ResponseAllBeen.class);
            if (responseAllBeen.getErr_code() != 0) {
                setException(new Exception(responseAllBeen.getMessage()));
                return TaskResult.FAILED;
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
