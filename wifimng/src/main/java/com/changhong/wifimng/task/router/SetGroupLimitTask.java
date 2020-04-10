package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.Group;
import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.wan.Level2Been;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 获取分组信息
 */
public class SetGroupLimitTask extends BaseRouterTask {
    /**
     * @param gateway
     * @param groupList all group list
     * @param staList   all sta limit info.
     * @param cookies
     * @param listener
     * @return
     */
    public final SetGroupLimitTask execute(String gateway, List<Group> groupList, List<Level2Been> staList, String cookies, TaskListener<Boolean> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put("groupList", groupList);
        params.put("staList", staList);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        List<Group> groupList = params[0].get("groupList");
        List<Level2Been> staList = params[0].get("staList");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);

        String url = "http://" + gateway + ":80/rpc";
        if (groupList != null)
            try {
                setStaGroupInfo(url, groupList, cookies);
            } catch (Exception e) {
                e.printStackTrace();
                setException(e);
                if (e instanceof AuthenticatorException)
                    return dealAuthenticatorException(gateway, params);
                return TaskResult.IO_ERROR;
            }

        if (staList != null) {
            for (int i = 0; i < staList.size(); i++) {
                try {
                    setTimeLimitShow(url, staList.get(i), cookies, i == staList.size() - 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    setException(e);
                }
            }
        }

        return TaskResult.OK;
    }

    private void setStaGroupInfo(String url, List<Group> groupList, String cookies) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        com.changhong.wifimng.been.wifi.RequireAllBeen requireBody = new com.changhong.wifimng.been.wifi.RequireAllBeen();
        requireBody.setMac_num(groupList.size());
        requireBody.setMac_list(groupList);

        String response = DefaultHttpUtils.httpPostWithJson(url,
                new RequestBeen(HttpRequestMethod.METHOD_STA_GROUP_SET, requireBody).toJsonString(),
                cookies
        );
        com.changhong.wifimng.been.wifi.ResponseAllBeen responseAllBeen = new Gson().fromJson(response, com.changhong.wifimng.been.wifi.ResponseAllBeen.class);
        if (responseAllBeen.getErr_code() != 0) {
            throw new IOException(responseAllBeen.getMessage());
        }
    }

    /**
     * @param url
     * @param been
     * @param cookies
     * @param isEffectImmediately 是否立即执行，一般最后一个立即执行
     * @throws AuthenticatorException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException
     */
    private void setTimeLimitShow(String url, Level2Been been, String cookies, boolean isEffectImmediately) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        com.changhong.wifimng.been.wan.RequireAllBeen requireBody = new com.changhong.wifimng.been.wan.RequireAllBeen();
        requireBody.setMac(been.getMac().replace(":", ""));
        requireBody.setStart_time_h(been.getStart_h());
        requireBody.setStart_time_m(been.getStart_m());
        requireBody.setStart_time_w(been.getStart_w());
        requireBody.setEnd_time_h(been.getEnd_h());
        requireBody.setEnd_time_m(been.getEnd_m());
        requireBody.setEnd_time_w(been.getEnd_w());
        requireBody.setMode(been.getState() == null ? 0 : 1);
        requireBody.setAction_flag(isEffectImmediately ? 1 : 0);
        String response = DefaultHttpUtils.httpPostWithJson(url,
                new RequestBeen(HttpRequestMethod.METHOD_INTERNET_TIME_LIMIT_SETTING, requireBody).toJsonString(),
                cookies
        );

        com.changhong.wifimng.been.wan.ResponseAllBeen responseAllBeen = new Gson().fromJson(response, com.changhong.wifimng.been.wan.ResponseAllBeen.class);
        if (responseAllBeen.getErr_code() != 0) {
            throw new IOException(responseAllBeen.getMessage());
        }
    }
}
