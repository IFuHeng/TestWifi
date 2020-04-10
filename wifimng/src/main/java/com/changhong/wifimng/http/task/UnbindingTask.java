package com.changhong.wifimng.http.task;

import android.util.Log;

import com.changhong.wifimng.BuildConfig;
import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.http.Config;
import com.changhong.wifimng.http.been.DeviceManagerBeen;
import com.changhong.wifimng.http.been.RequestBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 设备解除绑定
 * http://10.9.52.122/showdoc/web/#/10?page_id=275
 *
 * @author fuheng
 * @see android.os.AsyncTask
 * @since 2019年12月30日
 */
public class UnbindingTask extends BaseTask {
    /**
     * @param isClean  是否清除
     * @param userInfo 用户信息对象
     * @param listener 回调参数内容是 被绑定设备的uuid
     * @return 返回task对象
     */
    public final UnbindingTask execute(boolean isClean, Whole2LocalBeen userInfo, TaskListener<String> listener) {
        TaskParams params = new TaskParams();
        params.put("userInfo", userInfo);
        params.put("isClean", isClean);
        setListener(listener);
        executeOnExecutor(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        String url = getUrl(METHOD_UNBINDING);
        Whole2LocalBeen userInfo = params[0].get("userInfo");
        boolean isClean = params[0].get("isClean");

        ArrayList<String> list = new ArrayList<>();
        list.add(userInfo.getDevcieUuid());
        try {
            DeviceManagerBeen data = new DeviceManagerBeen();
            data.setDevList(list);
            data.setIsClean(isClean ? 1 : 0);
            data.setUserId(userInfo.getUserUuid());
            data.setToken(userInfo.getToken());
            RequestBeen requestBeen = new RequestBeen(userInfo.getUserUuid(), data);

            String response = DefaultHttpUtils.httpPost(url, requestBeen.toJsonString());
            Log.d(getClass().getSimpleName(), "====~response = " + response);
            JSONObject jsobj = new JSONObject(response);
            int code = jsobj.getInt("code");
            if (code == Config.CodeSuccess) {
                return TaskResult.OK;
            }
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.FAILED;
    }

    protected String getConnectResponseBeen(String response) throws Exception {
        JSONObject jsobj = new JSONObject(response);
        int code = jsobj.getInt("code");
        if (code == Config.CodeSuccess) {//|| code == Config.NoData) {

            String key = jsobj.getString("key");
            if (!jsobj.has("data"))
                return null;
            String data = jsobj.getString("data");

            if (BuildConfig.IS_RELEASE) {
                data = decrypt(data, key);
            }
            return data;
        } else {
            throw new Exception(jsobj.getString("msg"));
        }
    }
}
