package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.mesh.ListInfo;
import com.changhong.wifimng.been.mesh.MeshRequireAllBeen;
import com.changhong.wifimng.been.mesh.MeshResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class GetMeshNetworkTask extends BaseRouterTask {
    public final GetMeshNetworkTask execute(String gateway, String cookies, TaskListener<List<ListInfo>> listener) {
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
            MeshRequireAllBeen requireBody = new MeshRequireAllBeen();

            String response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen(HttpRequestMethod.METHOD_NETWORK_SHOW, requireBody).toJsonString(),
                    cookies
            );
            MeshResponseAllBeen responseAllBeen = new Gson().fromJson(response, MeshResponseAllBeen.class);
            if (responseAllBeen.getErr_code() != 0) {
                setException(new Exception(responseAllBeen.getMessage()));
            } else {
                publishProgress(responseAllBeen.getMesh_clients());
                return TaskResult.OK;
            }
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            if (e instanceof AuthenticatorException)
                return dealAuthenticatorException(gateway,params);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.FAILED;
    }

}
