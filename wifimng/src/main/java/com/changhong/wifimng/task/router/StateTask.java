package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.status.RequireAndResponseBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

/**
 * 获取路由状态
 */
public class StateTask extends BaseRouterTask {

    public final StateTask execute(String gateway, String cookies, TaskListener<RequireAndResponseBeen> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        setListener(listener);
        execute(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        try {
            String url = "http://" + gateway + ":80/rpc";

            String response = DefaultHttpUtils.httpPostWithJson(url,
                    "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"status_show\",\"params\":{\"src_type\":1}}", cookies);
            RequireAndResponseBeen been = new Gson().fromJson(response, RequireAndResponseBeen.class);
            if (been.getErr_code() == 0) {
                publishProgress(been);
                return TaskResult.OK;
            } else
                setException(new Exception(been.getMessage()));
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
