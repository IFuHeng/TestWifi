package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.DeviceType;
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
 * 获取访问控制信息
 */
public class GetWlanAccessTask extends BaseRouterTask {
    public final GetWlanAccessTask execute(String gateway, String deviceType, String cookies, TaskListener<ResponseAllBeen> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put("deviceType", deviceType);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
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
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_access_control_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                if (response == null || response.length() == 0)
                    throw new Exception("wait for develop.");
                ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                publishProgress(been);
            } else {
                Gson gson = new Gson();

                String url = "http://" + gateway + ":80/rpc";
                response = DefaultHttpUtils.httpPostWithJson(url,
                        new RequestBeen<>(HttpRequestMethod.METHOD_ACCESS_SHOW, new RequireAllBeen()).toJsonString(), cookies);
                ResponseAllBeen responseAllBeen = gson.fromJson(response, ResponseAllBeen.class);

                if (responseAllBeen.getErr_code() != 0) {
                    setException(new Exception(responseAllBeen.getMessage()));
                    return TaskResult.FAILED;
                }

                publishProgress(responseAllBeen);
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
