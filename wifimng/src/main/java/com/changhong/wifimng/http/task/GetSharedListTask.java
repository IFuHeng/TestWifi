package com.changhong.wifimng.http.task;

import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.http.been.DeviceListBeen;
import com.changhong.wifimng.http.been.DeviceManagerBeen;
import com.changhong.wifimng.http.been.RequestBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;

/**
 * 获取被分享的设备列表
 * http://10.9.52.122/showdoc/web/#/10?page_id=291
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2020年2月14日10点05分
 */
public class GetSharedListTask extends BaseTask {
    /**
     * @param shareStatus 分享状态，空：全部，0：待接受的分享，1：已被接受的分享
     * @param currentPage
     * @param pageSize
     * @param userInfo
     * @param listener
     * @return
     */
    public final GetSharedListTask execute(Integer shareStatus, int currentPage, int pageSize, Whole2LocalBeen userInfo, TaskListener<DeviceListBeen> listener) {
        TaskParams params = new TaskParams();
        params.put("userInfo", userInfo);
        params.put("currentPage", currentPage);
        params.put("pageSize", pageSize);
        params.put("shareStatus", shareStatus);
        setListener(listener);
        executeOnExecutor(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        String url = getUrl(METHOD_SHARED_LIST);
        Whole2LocalBeen userInfo = params[0].get("userInfo");
        int pageSize = params[0].get("pageSize");
        int currentPage = params[0].get("currentPage");
        Integer shareStatus = params[0].get("shareStatus");
        try {
            DeviceManagerBeen data = new DeviceManagerBeen();
            data.setUserId(userInfo.getUserUuid());
            data.setToken(userInfo.getToken());
            data.setPageSize(pageSize);
            data.setCurrentPage(currentPage);
            data.setShareStatus(shareStatus);
            RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);

            String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
            DeviceListBeen object = getConnectResponseBeen(response, DeviceListBeen.class);
            if (object != null) {
                publishProgress(object);
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
