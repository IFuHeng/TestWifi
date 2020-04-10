package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.StaInfo;
import com.changhong.wifimng.been.mesh.ListInfo;
import com.changhong.wifimng.been.mesh.MeshRequireAllBeen;
import com.changhong.wifimng.been.mesh.MeshResponseAllBeen;
import com.changhong.wifimng.been.wan.Level2Been;
import com.changhong.wifimng.been.Group;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 获取分组信息
 */
public class GetGroupLimitTask extends BaseRouterTask {
    public final GetGroupLimitTask execute(String gateway, String cookies, TaskListener<BaseBeen<List<Group>, List<Level2Been>>> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
//        params.put("isMesh", isMesh);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
//        Boolean isMesh = params[0].get("isMesh");
        String url = "http://" + gateway + ":80/rpc";
        try {
            List<Group> groupInfo = getStaGroupInfo(url, cookies);
            List<Level2Been> staLimitInfo = getTimeLimitShow(url, cookies);
            publishProgress(new BaseBeen(groupInfo, staLimitInfo));
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            if (e instanceof AuthenticatorException)
                return dealAuthenticatorException(gateway, params);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }

    private List<Group> getStaGroupInfo(String url, String cookies) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        com.changhong.wifimng.been.wifi.RequireAllBeen requireBody = new com.changhong.wifimng.been.wifi.RequireAllBeen();

        String response = DefaultHttpUtils.httpPostWithJson(url,
                new RequestBeen(HttpRequestMethod.METHOD_STA_GROUP_SHOW, requireBody).toJsonString(),
                cookies
        );
        com.changhong.wifimng.been.wifi.ResponseAllBeen responseAllBeen = new Gson().fromJson(response, com.changhong.wifimng.been.wifi.ResponseAllBeen.class);
        if (responseAllBeen.getErr_code() != 0) {
            throw new IOException(responseAllBeen.getMessage());
        } else {
            List<Group> list = responseAllBeen.getMac_list();
            // 排个序
            Collections.sort(list, new Comparator<Group>() {
                @Override
                public int compare(Group t1, Group t2) {
                    int result = t1.getGroup_name().compareToIgnoreCase(t2.getGroup_name());
                    if (result != 0)
                        return result;

                    return t1.getMac().compareToIgnoreCase(t2.getMac());
                }
            });
            return list;
        }
    }

    private List<Level2Been> getTimeLimitShow(String url, String cookies) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        com.changhong.wifimng.been.wan.RequireAllBeen requireBody = new com.changhong.wifimng.been.wan.RequireAllBeen();

        String response = DefaultHttpUtils.httpPostWithJson(url,
                new RequestBeen(HttpRequestMethod.METHOD_INTERNET_TIME_LIMIT_SHOW, requireBody).toJsonString(),
                cookies
        );
        com.changhong.wifimng.been.wan.ResponseAllBeen responseAllBeen = new Gson().fromJson(response, com.changhong.wifimng.been.wan.ResponseAllBeen.class);
        if (responseAllBeen.getErr_code() != 0) {
            throw new IOException(responseAllBeen.getMessage());
        } else {
            List<Level2Been> list = responseAllBeen.getInfo_list();
            // 排个序
            Collections.sort(list, new Comparator<Level2Been>() {
                @Override
                public int compare(Level2Been t1, Level2Been t2) {
                    return t1.getMac().compareToIgnoreCase(t2.getMac());
                }
            });
            return list;
        }
    }

    private List<StaInfo> getOnlineClient(String url, String cookies) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        com.changhong.wifimng.been.wan.RequireAllBeen requireBody = new com.changhong.wifimng.been.wan.RequireAllBeen();

        String response = DefaultHttpUtils.httpPostWithJson(url,
                new RequestBeen(HttpRequestMethod.METHOD_STA_INFO_SHOW, requireBody).toJsonString(),
                cookies
        );
        com.changhong.wifimng.been.wan.ResponseAllBeen responseAllBeen = new Gson().fromJson(response, com.changhong.wifimng.been.wan.ResponseAllBeen.class);
        if (responseAllBeen.getErr_code() != 0)
            throw new IOException(responseAllBeen.getMessage());
        return responseAllBeen.getSta_info();
    }

    private List<ListInfo> getMeshResponseAllBeen(String url, String cookies) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        String response = DefaultHttpUtils.httpPostWithJson(url,
                new RequestBeen(HttpRequestMethod.METHOD_NETWORK_SHOW, new MeshRequireAllBeen()).toJsonString(),
                cookies
        );
        MeshResponseAllBeen responseAllBeen = new Gson().fromJson(response, MeshResponseAllBeen.class);
        if (responseAllBeen.getErr_code() != 0)
            throw new IOException(responseAllBeen.getMessage());
        return responseAllBeen.getMesh_clients();
    }

}
