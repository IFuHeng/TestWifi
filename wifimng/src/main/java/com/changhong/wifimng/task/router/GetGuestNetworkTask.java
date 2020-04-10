package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.wifi.GuestRequireAndResponseBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

public class GetGuestNetworkTask extends BaseRouterTask {
    public final GetGuestNetworkTask execute(String gateway,  String cookies, TaskListener<GuestRequireAndResponseBeen> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR,params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        try {
            String url = "http://" + gateway + ":80/rpc";
            GuestRequireAndResponseBeen requireBody = new GuestRequireAndResponseBeen();

            String response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen(HttpRequestMethod.METHOD_GUEST_NETCORK_SHOW, requireBody).toJsonString(),
                    cookies
            );
            GuestRequireAndResponseBeen responseAllBeen = new Gson().fromJson(response, GuestRequireAndResponseBeen.class);
            if (responseAllBeen.getErr_code() != 0) {
                setException(new Exception(responseAllBeen.getMessage()));
            } else {
                publishProgress(responseAllBeen);
                return TaskResult.OK;
            }
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            if (e instanceof AuthenticatorException)
                return dealAuthenticatorException(gateway, params);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.FAILED;
    }

}
