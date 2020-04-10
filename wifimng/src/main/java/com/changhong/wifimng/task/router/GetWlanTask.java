package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.wan.ResponseAllBeen;
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
public class GetWlanTask extends BaseRouterTask {

    public final GetWlanTask execute(String gateway, String cookies, TaskListener<ResponseAllBeen> listener) {
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
        try {
            String url = "http://" + gateway + ":80/rpc";

            String response = DefaultHttpUtils.httpPostWithJson(url,
                    "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"" + HttpRequestMethod.METHOD_WAN_SHOW + "\",\"params\":{\"src_type\":1}}", cookies);
            ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
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
