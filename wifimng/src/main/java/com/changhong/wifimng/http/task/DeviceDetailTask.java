package com.changhong.wifimng.http.task;

import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.http.been.DeviceDetailBeen;
import com.changhong.wifimng.http.been.DeviceDetailBeenParent;
import com.changhong.wifimng.http.been.DeviceManagerBeen;
import com.changhong.wifimng.http.been.RequestBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;

/**
 * 设备详情
 * http://10.9.52.122/showdoc/web/#/10?page_id=422
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2019年12月2日16点59分
 */
public class DeviceDetailTask extends BaseTask {
    /**
     * @param info
     * @param listener 回调参数内容是 设备详细信息
     * @return
     */
    public final DeviceDetailTask execute(Whole2LocalBeen info, TaskListener<DeviceDetailBeen> listener) {
        TaskParams params = new TaskParams();
        params.put("info", info);
        setListener(listener);
        executeOnExecutor(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        Whole2LocalBeen info = params[0].get("info");
        try {
            DeviceDetailBeen been = getDeviceDetail(info);
            if (been == null) {
                return TaskResult.FAILED;
            }

            publishProgress(been);
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }

    private DeviceDetailBeen getDeviceDetail(Whole2LocalBeen info) throws Exception {
        String url = getUrl(METHOD_DEVICE_DETAIL);
        DeviceManagerBeen data = new DeviceManagerBeen();
        data.setUuid(info.getDevcieUuid());
        data.setToken(info.getToken());
        RequestBeen requestBeen = new RequestBeen(info.getUserUuid(), data);

        String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
        DeviceDetailBeenParent object = getConnectResponseBeen(response, DeviceDetailBeenParent.class);
        return object.getObject();
    }

}
