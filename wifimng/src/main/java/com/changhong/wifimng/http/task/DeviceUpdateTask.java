package com.changhong.wifimng.http.task;

import android.util.Log;

import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.net.OkHttpUtils;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;

import java.util.HashMap;

/**
 * 设备信息更新
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2020年2月6日15点3分
 */
public class DeviceUpdateTask extends BaseTask {
    /**
     * @param userInfo
     * @param listener
     * @return
     */
    public final DeviceUpdateTask execute(String deviceName, Whole2LocalBeen userInfo, TaskListener listener) {
        TaskParams params = new TaskParams();
        params.put("userInfo", userInfo);
        params.put("deviceName", deviceName);
        setListener(listener);
        executeOnExecutor(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        String url = getUrl(METHOD_DEVICE_UPDATE);
        Whole2LocalBeen userInfo = params[0].get("userInfo");
        String deviceName = params[0].get("deviceName");
        try {
            // step 2 更新信息
            updateDevice(userInfo, deviceName);
            return TaskResult.OK;
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.FAILED;
        }
    }

    private void updateDevice(Whole2LocalBeen userInfo, String deviceName) throws Exception {
        String url = getUrl(METHOD_DEVICE_UPDATE);
        HashMap<String, String> map = new HashMap<>();
        map.put("client", "ANDROID");
        map.put("version", "1.0.1");
        map.put("iconId", "");
        map.put("name", deviceName);
        map.put("uuid", userInfo.getDevcieUuid());
        map.put("token", userInfo.getToken());
        map.put("file", "");
        map.put("key", "");
        map.put("user", userInfo.getUserUuid());
        String response = OkHttpUtils.httpPost(url, map);
        checkConnectResponse(response);
        Log.d(getClass().getSimpleName(), "====~response = " + response);
    }
}
