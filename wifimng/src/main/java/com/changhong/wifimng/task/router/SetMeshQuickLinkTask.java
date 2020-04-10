package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.mesh.MeshRequireAllBeen;
import com.changhong.wifimng.been.mesh.MeshResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

/**
 * 设置组网快速连接，开启或关闭
 */
public class SetMeshQuickLinkTask extends BaseRouterTask {

    public final SetMeshQuickLinkTask execute(String gateway, boolean enable, String cookies, TaskListener<Integer> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        params.put("enable", enable);
        setListener(listener);
        execute(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        boolean enable = params[0].get("enable");
        String url = "http://" + gateway + ":80/rpc";
        try {
            MeshRequireAllBeen reqire = new MeshRequireAllBeen();
            //先获取状态，如果是相同的，就退出
            String response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen(HttpRequestMethod.METHOD_MESH_QUICK_SHOW, reqire).toJsonString(), cookies);
            MeshResponseAllBeen been = new Gson().fromJson(response, MeshResponseAllBeen.class);
            if (been.getErr_code() != 0) {
                throw new Exception(been.getMessage());
            }
            if ((been.getQuick_status() == 1) == enable) {
                publishProgress(30);
                return TaskResult.OK;
            }

            // 当前状态与目标状态不同，切换请求
            reqire.setEnable(enable ? 1 : 0);
            response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen(HttpRequestMethod.METHOD_MESH_QUICK_LINK, reqire).toJsonString(), cookies);
            been = new Gson().fromJson(response, MeshResponseAllBeen.class);
            if (been.getErr_code() != 0) {
                throw new Exception(been.getMessage());
            }
            publishProgress(been.getWaite_time());
        } catch (Exception e) {
            if (e instanceof AuthenticatorException)
                return dealAuthenticatorException(gateway, params);
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }


}
