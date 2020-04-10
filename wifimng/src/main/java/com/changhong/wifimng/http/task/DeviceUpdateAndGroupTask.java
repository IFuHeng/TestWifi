package com.changhong.wifimng.http.task;

import android.util.Log;

import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.http.been.DeviceManagerBeen;
import com.changhong.wifimng.http.been.RequestBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.OkHttpUtils;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;

import java.util.HashMap;

/**
 * 设备信息更新和分组
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2020年2月6日15点3分
 */
public class DeviceUpdateAndGroupTask extends BaseTask {
    /**
     * @param userInfo
     * @param listener
     * @return
     */
    public final DeviceUpdateAndGroupTask execute(String deviceName, String groupId, Whole2LocalBeen userInfo, TaskListener listener) {
        TaskParams params = new TaskParams();
        params.put("userInfo", userInfo);
        params.put("deviceName", deviceName);
        params.put("groupId", groupId);
        setListener(listener);
        executeOnExecutor(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        Whole2LocalBeen userInfo = params[0].get("userInfo");
        String deviceName = params[0].get("deviceName");
        String groupId = params[0].get("groupId");
        try {
            if (userInfo.getDevcieUuid() == null) {            // step 1 绑定
                String uuid = bindDevice(userInfo);
                if (uuid == null) {
                    return TaskResult.FAILED;
                }
                userInfo.setDevcieUuid(uuid);
            }
            // step 2 更新信息
            updateDevice(userInfo, deviceName);
            // step 3 设备分组
            saveGroup(userInfo, groupId);
            return TaskResult.OK;
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.FAILED;
        }
    }

    private void updateDevice(Whole2LocalBeen userInfo, String deviceName) throws Exception {
        String url = getUrl(METHOD_DEVICE_UPDATE);
//        DeviceManagerBeen data = new DeviceManagerBeen();
//        data.setUserId(userInfo.getUserUuid());
//        data.setToken(userInfo.getToken());
//        data.setName(deviceName);
//        data.setDeviceId(deviceId);
//        RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);
//
//        String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
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

    private void saveGroup(Whole2LocalBeen userInfo, String groupId) throws Exception {
        String url = getUrl(METHOD_DEVICE_GROUP);
        DeviceManagerBeen data = new DeviceManagerBeen();
        data.setUserId(userInfo.getUserUuid());
        data.setToken(userInfo.getToken());
        data.setGroupId(groupId);
        data.setDeviceId(userInfo.getDevcieUuid());
        RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);

        String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
        checkConnectResponse(response);
        Log.d(getClass().getSimpleName(), "====~response = " + response);
    }

    private String bindDevice(Whole2LocalBeen userInfo) throws Exception {
        String url = getUrl(METHOD_BINDING);
        String mac = userInfo.getMac();
        if (mac.indexOf(':') != -1)
            mac = mac.replace(":", "");
        DeviceManagerBeen data = new DeviceManagerBeen();
        data.setMac(mac);
        data.setUserId(userInfo.getUserUuid());
        data.setToken(userInfo.getToken());
        RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);

        String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
        Log.d(getClass().getSimpleName(), "====~response = " + response);
        String uuid = getConnectResponseBeen(response);
        return uuid;
    }
}
