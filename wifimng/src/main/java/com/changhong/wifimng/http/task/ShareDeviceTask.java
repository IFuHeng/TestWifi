package com.changhong.wifimng.http.task;

import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.http.been.DeviceManagerBeen;
import com.changhong.wifimng.http.been.RequestBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;

import java.util.List;

/**
 * 设备分享
 * http://10.9.52.122/showdoc/web/#/10?page_id=275
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2020年2月14日10点05分
 */
public class ShareDeviceTask extends BaseTask {
    /**
     * @param deviceList 被分享设备列表
     * @param account    被分享用户账号
     * @param userInfo
     * @param listener
     * @return
     */
    public final ShareDeviceTask execute(List<String> deviceList, String account, Whole2LocalBeen userInfo, TaskListener listener) {
        TaskParams params = new TaskParams();
        params.put("userInfo", userInfo);
        params.put("deviceList", deviceList);
        params.put("account", account);
        setListener(listener);
        executeOnExecutor(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        String url = getUrl(METHOD_DEVICE_SHARE);
        Whole2LocalBeen userInfo = params[0].get("userInfo");
        List<String> deviceList = params[0].get("deviceList");
        String account = params[0].get("account");
        try {
            DeviceManagerBeen data = new DeviceManagerBeen();
            data.setUserId(userInfo.getUserUuid());
            data.setToken(userInfo.getToken());
            data.setDeviceList(deviceList);
            data.setAccount(account);
            RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);

            String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
            checkConnectResponse(response);
            return TaskResult.OK;

        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
//        return TaskResult.FAILED;
    }
}
