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

import java.util.ArrayList;
import java.util.List;

/**
 * 获取被分享的用户列表
 * http://10.9.52.122/showdoc/web/#/10?page_id=375
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2020年2月14日10点05分
 */
public class GetSharedUserListTask extends BaseTask {
    /**
     * @param currentPage
     * @param pageSize
     * @param shareStatus 分享状态，空：全部，0：待接受，1：已接受
     * @param userInfo
     * @param listener
     * @return
     */
    public final GetSharedUserListTask execute(int currentPage, int pageSize, Integer shareStatus, Whole2LocalBeen userInfo, TaskListener<FamilyMemberListBeen> listener) {
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
        String url = getUrl(METHOD_GET_SHARED_LIST);
        Whole2LocalBeen userInfo = params[0].get("userInfo");
        int pageSize = params[0].get("pageSize");
        int currentPage = params[0].get("currentPage");
        Integer shareStatus = params[0].get("shareStatus");
        try {
            FamilyMemberListBeen object = getSharedList(url, userInfo, currentPage, pageSize, shareStatus);
            if (object == null) {
                return TaskResult.FAILED;
            }
            publishProgress(object);

        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }

    /**
     * @param url
     * @param userInfo
     * @param currentPage
     * @param pageSize
     * @param shareStatus shareStatus 分享状态，空：全部，0：待接受，1：已接受
     * @return
     * @throws Exception
     */
    private FamilyMemberListBeen getSharedList(String url, Whole2LocalBeen userInfo, int currentPage, int pageSize, Integer shareStatus) throws Exception {
        DeviceManagerBeen data = new DeviceManagerBeen();
        data.setUserId(userInfo.getUserUuid());
        data.setToken(userInfo.getToken());
        data.setDeviceId(userInfo.getDevcieUuid());
        data.setPageSize(pageSize);
        data.setShareStatus(shareStatus);
        data.setCurrentPage(currentPage);
        RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);

        String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
        FamilyMemberListBeen object = getConnectResponseBeen(response, FamilyMemberListBeen.class);
        return object;
    }
}
