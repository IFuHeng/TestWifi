package com.changhong.wifimng.http.task;

import android.util.Log;

import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.http.been.DeviceManagerBeen;
import com.changhong.wifimng.http.been.GroupListBeen;
import com.changhong.wifimng.http.been.RequestBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;

/**
 * 分组列表
 * http://10.9.52.122/showdoc/web/#/10?page_id=275
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2020年1月9日14点59分
 */
public class GetGroupListTask extends BaseTask {
    /**
     * @param userInfo
     * @param listener
     * @return
     */
    public final GetGroupListTask execute(int currentPage, int pageSize, Whole2LocalBeen userInfo, TaskListener<GroupListBeen> listener) {
        TaskParams params = new TaskParams();
        params.put("userInfo", userInfo);
        params.put("pageSize", pageSize);
        params.put("currentPage", currentPage);
        setListener(listener);
        executeOnExecutor(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        String url = getUrl(METHOD_GROUP_LIST);
        Whole2LocalBeen userInfo = params[0].get("userInfo");
        int currentPage = params[0].get("currentPage");
        int pageSize = params[0].get("pageSize");
        try {
            DeviceManagerBeen data = new DeviceManagerBeen();
            data.setUserId(userInfo.getUserUuid());
            data.setToken(userInfo.getToken());
            data.setCurrentPage(currentPage);
            data.setPageSize(pageSize);
            RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);

            String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
            GroupListBeen groupList = getConnectResponseBeen(response, GroupListBeen.class);
            if (groupList != null) {
                publishProgress(groupList);
                return TaskResult.OK;
            }

            Log.d(getClass().getSimpleName(), "====~response = " + response);
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.FAILED;
    }
}
