package com.changhong.wifimng.http.task;

import android.util.Log;

import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.http.been.DeviceManagerBeen;
import com.changhong.wifimng.http.been.FamilyMemberListBeen;
import com.changhong.wifimng.http.been.RequestBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;

/**
 * GetSharedListTask
 * http://10.9.52.122/showdoc/web/#/10?page_id=346
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2020年2月14日10点05分
 */
public class GetFamilyMemberListTask extends BaseTask {
    /**
     * @param currentPage
     * @param pageSize
     * @param userInfo
     * @param listener
     * @return
     */
    public final GetFamilyMemberListTask execute( int currentPage, int pageSize, Whole2LocalBeen userInfo, TaskListener<FamilyMemberListBeen> listener) {
        TaskParams params = new TaskParams();
        params.put("userInfo", userInfo);
        params.put("currentPage", currentPage);
        params.put("pageSize", pageSize);
        setListener(listener);
        executeOnExecutor(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        String url = getUrl(METHOD_FAMILY_MEMBER);
        Whole2LocalBeen userInfo = params[0].get("userInfo");
        int pageSize = params[0].get("pageSize");
        int currentPage = params[0].get("currentPage");
        try {
            DeviceManagerBeen data = new DeviceManagerBeen();
            data.setUserId(userInfo.getUserUuid());
            data.setToken(userInfo.getToken());
            data.setPageSize(pageSize);
            data.setCurrentPage(currentPage);
            RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);

            String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
            FamilyMemberListBeen object = getConnectResponseBeen(response, FamilyMemberListBeen.class);
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
