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

/**
 * 提交wan口设置
 */
public class SetWlanTask extends BaseRouterTask {

    public final SetWlanTask execute(String gateway, RequireAllBeen require, String cookies, TaskListener<Boolean> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        params.put("require", require);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        RequireAllBeen require = params[0].get("require");
        try {
            String url = "http://" + gateway + ":80/rpc";

            String response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen<>(HttpRequestMethod.METHOD_WAN_SETTING, require).toJsonString(), cookies);
            ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
            if (been.getErr_code() == 0)
                return TaskResult.OK;
            else
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
