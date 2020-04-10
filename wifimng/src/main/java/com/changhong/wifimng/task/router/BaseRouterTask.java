package com.changhong.wifimng.task.router;

import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;

public abstract class BaseRouterTask extends GenericTask {

    /**
     * @param gateway 网关
     * @return 重新登录
     * @throws Exception
     */
    protected String reLogin(String gateway) throws Exception {

        String password = Preferences.getInstance(null).getString(KeyConfig.KEY_ROUTER_PASSWORD);
        if (password == null)
            return null;

        final String url = String.format("http://%s:80/login.html", gateway);
        String param = "password=" + password;
        String response = DefaultHttpUtils.login(url, param);
        Preferences.getInstance(null).saveString(KeyConfig.KEY_COOKIE_SSID, response);
        return response;
    }

    private boolean hasReLogin = false;

    protected TaskResult dealAuthenticatorException(String gateway, TaskParams... params) {

        if (hasReLogin) {
            return TaskResult.AUTH_ERROR;
        }

        hasReLogin = true;
        try {
            String ssid = reLogin(gateway);
            if (params != null && params.length > 0 && params[0] != null)
                params[0].put(KeyConfig.KEY_COOKIE_SSID, ssid);
            return _doInBackground(params);
        } catch (Exception e) {
            e.printStackTrace();
            return TaskResult.AUTH_ERROR;
        }
    }
}
