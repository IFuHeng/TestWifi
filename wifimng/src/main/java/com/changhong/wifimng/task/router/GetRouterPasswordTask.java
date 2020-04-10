package com.changhong.wifimng.task.router;

import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.sys.SettingRequireAllBeen;
import com.changhong.wifimng.been.sys.SettingResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

public class GetRouterPasswordTask extends GenericTask {
    public final GetRouterPasswordTask execute(String gateway, TaskListener<BaseBeen<String, String>> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        try {
            Gson gson = new Gson();

            String url = "http://" + gateway + ":80/rpcsupper";
            String response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen<>(HttpRequestMethod.METHOD_USER_PSW_SHOW, new SettingRequireAllBeen()).toJsonString());
            SettingResponseAllBeen responseAllBeen = gson.fromJson(response, SettingResponseAllBeen.class);

            if (responseAllBeen.getErr_code() != 0) {
                setException(new Exception(responseAllBeen.getMessage()));
                return TaskResult.FAILED;
            }

            publishProgress(new BaseBeen(responseAllBeen.getUser_name(), responseAllBeen.getUser_password()));
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }

}
