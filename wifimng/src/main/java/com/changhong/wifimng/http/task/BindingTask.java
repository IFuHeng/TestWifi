package com.changhong.wifimng.http.task;

import android.util.Log;

import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.http.been.DeviceBeen;
import com.changhong.wifimng.http.been.DeviceManagerBeen;
import com.changhong.wifimng.http.been.RequestBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;

/**
 * 设备绑定
 * http://10.9.52.122/showdoc/web/#/10?page_id=275
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2019年12月2日16点59分
 */
public class BindingTask extends BaseTask {
    /**
     * @param mac
     * @param userInfo
     * @param listener 回调参数内容是 被绑定设备的uuid
     * @return
     */
    public final BindingTask execute(String mac, Whole2LocalBeen userInfo, TaskListener<String> listener) {
        TaskParams params = new TaskParams();
        params.put("userInfo", userInfo);
        params.put("mac", mac);
        setListener(listener);
        executeOnExecutor(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        String url = getUrl(METHOD_BINDING);
        Whole2LocalBeen userInfo = params[0].get("userInfo");
        String mac = params[0].getString("mac");
        if (mac.indexOf(':') != -1)
            mac = mac.replace(":", "");
        try {
            DeviceManagerBeen data = new DeviceManagerBeen();
            data.setMac(mac);
            data.setUserId(userInfo.getUserUuid());
            data.setToken(userInfo.getToken());
            RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);

            String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
            Log.d(getClass().getSimpleName(), "====~response = " + response);
            String uuid = getConnectResponseBeen(response);
            if (uuid != null) {
                publishProgress(uuid);
                return TaskResult.OK;
            }
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.FAILED;
    }
}
