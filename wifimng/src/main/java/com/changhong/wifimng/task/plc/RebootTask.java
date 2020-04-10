package com.changhong.wifimng.task.plc;

import com.changhong.wifimng.been.BaseResponseBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

/**
 * 电力猫重启
 */
public class RebootTask extends GenericTask {

    public final RebootTask execute(String gateway, TaskListener<String> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        setListener(listener);
        execute(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");

        try {
            String response;
            final String url = String.format("http://%s:8081/rpc", gateway);
            String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"reboot\",\"param\":{\"src_type\":1}}";
            response = DefaultHttpUtils.httpPostWithText(url, body);
            BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
            if (been.getErr_code() != 0) {
                setException(new Exception(been.getMessage()));
                return TaskResult.FAILED;
            }
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }

        return TaskResult.OK;
    }


}
