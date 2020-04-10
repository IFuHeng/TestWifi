package com.changhong.wifimng.http.task;

import android.util.Log;

import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.http.been.DeviceManagerBeen;
import com.changhong.wifimng.http.been.RequestBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;

import java.util.List;

/**
 * 处理设备分享
 * http://10.9.52.122/showdoc/web/#/10?page_id=296
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2020年2月14日10点05分
 */
public class DealSharingTask extends BaseTask {
    /**
     * @param shareUserType shareUserType
     * @param sharedUserId
     * @param sharingUserId
     * @param operation     分享的操作，1：接受，2：拒绝,3:取消（shareUserType = 1时有效），4：删除
     * @param userInfo
     * @param listener
     * @return
     */
    public final DealSharingTask execute(int shareUserType, String sharedUserId, String sharingUserId, int operation, Whole2LocalBeen userInfo, TaskListener listener) {
        TaskParams params = new TaskParams();
        params.put("userInfo", userInfo);
        params.put("shareUserType", shareUserType);
        params.put("sharedUserId", sharedUserId);
        params.put("sharingUserId", sharingUserId);
        params.put("operation", operation);
        setListener(listener);
        executeOnExecutor(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        String url = getUrl(METHOD_DEAL_SHARED);
        Whole2LocalBeen userInfo = params[0].get("userInfo");
        int shareUserType = params[0].get("shareUserType");
        String sharedUserId = params[0].get("sharedUserId");
        String sharingUserId = params[0].get("sharingUserId");
        int operation = params[0].get("operation");
        try {
            DeviceManagerBeen data = new DeviceManagerBeen();
            data.setUserId(userInfo.getUserUuid());
            data.setToken(userInfo.getToken());

            data.setSharedUserId(sharedUserId);
            data.setSharingUserId(sharingUserId);
            data.setDeviceId(userInfo.getDevcieUuid());
            data.setOperation(operation);
            data.setShareUserType(shareUserType);

            RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);

            String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
            Log.d(getClass().getSimpleName(), "====~response = " + response);
            checkConnectResponse(response);
            return TaskResult.OK;

        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
    }
}
