package com.changhong.wifimng.http.task;

import android.util.Log;

import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.http.been.DeviceManagerBeen;
import com.changhong.wifimng.http.been.RequestBeen;
import com.changhong.wifimng.http.been.UpgradeBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.uttils.WifiMacUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 批量设备升级检测（新）
 * http://10.9.52.122/showdoc/web/#/10?page_id=615
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2020年2月13日15点9分
 */
public class CheckUpdateTask extends BaseTask {
    /**
     * @param userInfo
     * @param listener
     * @return
     */
    public final CheckUpdateTask execute(Whole2LocalBeen userInfo, TaskListener<UpgradeBeen> listener) {
        TaskParams params = new TaskParams();
        params.put("userInfo", userInfo);
        setListener(listener);
        executeOnExecutor(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        String url = getUrl(METHOD_UPGRADE_CHECK);
        Whole2LocalBeen userInfo = params[0].get("userInfo");
        String mac = WifiMacUtils.macNoColon(userInfo.getMac());
        try {
            DeviceManagerBeen data = new DeviceManagerBeen();
            data.setUserId(userInfo.getUserUuid());
            data.setToken(userInfo.getToken());
            data.setMacs(mac);
            RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);

            String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
            Log.d(getClass().getSimpleName(), "====~response = " + response);
            String string = getConnectResponseBeen(response);
            JSONObject jsonObject = new JSONObject(string);
            if (jsonObject.has(mac)) {
                jsonObject = jsonObject.getJSONObject(mac);
                UpgradeBeen been = new Gson().fromJson(jsonObject.toString(), UpgradeBeen.class);
                publishProgress(been);
            } else
                publishProgress(new UpgradeBeen());
            return TaskResult.OK;
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.FAILED;
        }
    }
}
