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

import java.util.List;

/**
 * 删除限速
 */
public class DelSpeedLimitsLoadTask extends BaseRouterTask {
    /**
     * @param gateway
     * @param macs
     * @param isEffectImmediately 是否立即执行
     * @param cookies
     * @param listener
     * @return
     */
    public final DelSpeedLimitsLoadTask execute(String gateway, List<String> macs, boolean isEffectImmediately, String cookies, TaskListener listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        for (int i = 0; i < macs.size(); i++) {
            String mac = macs.get(i);
            if (mac.indexOf(':') != -1)
                mac = mac.replace(":", "");
            macs.set(i, mac);
        }
        params.put("macs", macs);
        params.put("isEffectImmediately", isEffectImmediately);
        setListener(listener);
        execute(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        List<String> macs = params[0].get("macs");
        boolean isEffectImmediately = params[0].get("isEffectImmediately");

        String url = "http://" + gateway + ":80/rpc";

        try {
            for (int i = 0; i < macs.size(); i++) {
                String mac = macs.get(i);
                RequireAllBeen require = new RequireAllBeen();
                require.setMac(mac);
                require.setMax_up_bandwidth(0);
                require.setMax_down_bandwidth(0);
                require.setMode(0);

                if (isEffectImmediately) {
                    require.setAction_flag(i == macs.size() - 1 ? 1 : 0);
                } else {
                    require.setAction_flag(0);
                }

                String response = DefaultHttpUtils.httpPostWithJson(url,
                        new RequestBeen<>(HttpRequestMethod.METHOD_MAC_RATE_LIMIT_SETTING, require).toJsonString(), cookies);
                ResponseAllBeen been
                        = new Gson().fromJson(response, ResponseAllBeen.class);

                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
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
