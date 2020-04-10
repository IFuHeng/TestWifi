package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.sys.ServiceRequireAllBeen;
import com.changhong.wifimng.been.sys.ServiceResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

/**
 * 修改ddns
 */
public class SetDDNSTask extends BaseRouterTask {

    public final SetDDNSTask execute(String gateway, boolean isCreate, ServiceRequireAllBeen been, String cookies, TaskListener listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        params.put("isCreate", isCreate);
        params.put("been", been);
        setListener(listener);
        execute(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        ServiceRequireAllBeen been = params[0].get("been");
        boolean isCreate = params[0].get("isCreate");
        String url = "http://" + gateway + ":80/rpc";

        //创建
        try {
            editDDNS(url, been, cookies);
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            if (e instanceof AuthenticatorException)
                return dealAuthenticatorException(gateway, params);
            return TaskResult.IO_ERROR;
        }

        //关闭
        if (isCreate)
            try {
                been.setEnabled(0);
                editDDNS(url, been, cookies);
            } catch (Exception e) {
                e.printStackTrace();
                setException(e);
            }

        return TaskResult.OK;
    }

    private void editDDNS(String url, ServiceRequireAllBeen been, String cookies) throws Exception {
        String response = DefaultHttpUtils.httpPostWithJson(url,
                new RequestBeen(HttpRequestMethod.METHOD_DDNS_SETTING, been).toJsonString(),
                cookies
        );
        ServiceResponseAllBeen responseAllBeen = new Gson().fromJson(response, ServiceResponseAllBeen.class);
        if (responseAllBeen.getErr_code() != 0) {
            throw new Exception(responseAllBeen.getMessage());
        }
    }

}
