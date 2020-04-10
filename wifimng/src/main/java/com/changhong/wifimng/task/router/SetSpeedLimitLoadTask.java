package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.wan.Level2Been;
import com.changhong.wifimng.been.wan.RequireAllBeen;
import com.changhong.wifimng.been.wan.ResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

import java.util.List;

/**
 * 提交限速
 */
public class SetSpeedLimitLoadTask extends BaseRouterTask {
    /**
     * @param gateway
     * @param mac
     * @param up_speed
     * @param down_speed
     * @param isAadd              true 添加；false 删除
     * @param isEffectImmediately 是否立即执行
     * @param cookies
     * @param listener
     * @return
     */
    public final SetSpeedLimitLoadTask execute(String gateway, String mac, int up_speed, int down_speed, boolean isAadd, boolean isEffectImmediately, String cookies, TaskListener<List<Level2Been>> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        if (mac.indexOf(':') != -1)
            mac = mac.replace(":", "");
        params.put("mac", mac);
        params.put("up_speed", up_speed);
        params.put("down_speed", down_speed);
        params.put("isAadd", isAadd);
        params.put("isEffectImmediately", isEffectImmediately);
        setListener(listener);
        execute(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        String mac = params[0].getString("mac");
        int up_speed = params[0].get("up_speed");
        int down_speed = params[0].get("down_speed");
        boolean isAadd = params[0].get("isAadd");
        boolean isEffectImmediately = params[0].get("isEffectImmediately");

        String url = "http://" + gateway + ":80/rpc";

        try {
            RequireAllBeen require = new RequireAllBeen();
            require.setMac(mac);
            require.setMax_up_bandwidth(up_speed);
            require.setMax_down_bandwidth(down_speed);
            require.setMode(isAadd ? 1 : 0);
            require.setAction_flag(isEffectImmediately ? 1 : 0);

            String response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen<>(HttpRequestMethod.METHOD_MAC_RATE_LIMIT_SETTING, require).toJsonString(), cookies);
            ResponseAllBeen been
                    = new Gson().fromJson(response, ResponseAllBeen.class);

            if (been.getErr_code() != 0) {
                setException(new Exception(been.getMessage()));
                return TaskResult.FAILED;
            }

//            publishProgress(been.getInfo_list());
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
