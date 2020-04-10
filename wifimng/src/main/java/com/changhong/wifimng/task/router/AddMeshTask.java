package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.StaInfo;
import com.changhong.wifimng.been.mesh.ListInfo;
import com.changhong.wifimng.been.mesh.MeshRequireAllBeen;
import com.changhong.wifimng.been.mesh.MeshResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取访问控制信息
 */
public class AddMeshTask extends BaseRouterTask {
    public final AddMeshTask execute(String gateway, List<StaInfo> devList, String cookies, TaskListener<Boolean> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        params.put("devList", devList);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        List<StaInfo> devList = params[0].get("devList");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);

        String url = "http://" + gateway + ":80/rpc";
        MeshRequireAllBeen requireBeen = new MeshRequireAllBeen();
        requireBeen.setOp(1);

        List<ListInfo> list = new ArrayList<>();
        for (StaInfo staInfo : devList) {
            ListInfo item = new ListInfo();
            item.setIp(staInfo.getIp());
            item.setMac(staInfo.getMac().replace(":", ""));
            list.add(item);
        }
        requireBeen.setDev(list);

        try {
            Gson gson = new Gson();

            String response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen<>(HttpRequestMethod.METHOD_NETWORK_ADD, requireBeen).toJsonString(), cookies);
            MeshResponseAllBeen responseAllBeen = gson.fromJson(response, MeshResponseAllBeen.class);

            if (responseAllBeen.getErr_code() != 0) {
                setException(new Exception(responseAllBeen.getMessage()));
                return TaskResult.FAILED;
            }

            publishProgress(true);
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
