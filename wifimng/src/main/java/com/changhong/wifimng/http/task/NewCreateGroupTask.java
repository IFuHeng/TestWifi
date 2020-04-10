package com.changhong.wifimng.http.task;

import android.util.Log;

import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.http.been.DeviceManagerBeen;
import com.changhong.wifimng.http.been.Group;
import com.changhong.wifimng.http.been.RequestBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;

/**
 * 新建分组
 * http://10.9.52.122/showdoc/web/#/10?page_id=275
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2020年1月9日15点3分
 */
public class NewCreateGroupTask extends BaseTask {
    /**
     * @param userInfo
     * @param listener
     * @return
     */
    public final NewCreateGroupTask execute(String groupName, Whole2LocalBeen userInfo, TaskListener<Group> listener) {
        TaskParams params = new TaskParams();
        params.put("userInfo", userInfo);
        params.put("groupName", groupName);
        setListener(listener);
        executeOnExecutor(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        String url = getUrl(METHOD_CREATE_GROUP);
        Whole2LocalBeen userInfo = params[0].get("userInfo");
        String groupName = params[0].get("groupName");
        try {
            DeviceManagerBeen data = new DeviceManagerBeen();
            data.setUserId(userInfo.getUserUuid());
            data.setToken(userInfo.getToken());
            data.setGroupName(groupName);
            RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);

            String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
            Log.d(getClass().getSimpleName(), "====~response = " + response);
            String uuid = getConnectResponseBeen(response);
            if (uuid != null) {
                Group newGroup = new Group();
                newGroup.setUuid(uuid);
                newGroup.setUserId(userInfo.getUserUuid());
                newGroup.setGroupName(groupName);
                publishProgress(newGroup);
                return TaskResult.OK;
            }
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
        }
        return TaskResult.FAILED;
    }
}
