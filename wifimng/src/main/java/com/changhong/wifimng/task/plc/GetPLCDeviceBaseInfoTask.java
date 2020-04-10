package com.changhong.wifimng.task.plc;

import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

public class GetPLCDeviceBaseInfoTask extends GenericTask {
    public final GetPLCDeviceBaseInfoTask execute(String gateway, TaskListener<PLCInfo> listener) {
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
            String response;
            String body;
            PLCInfo result;
            final String url = String.format("http://%s:8081/rpc", gateway);
            {//                step1
                body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"plc_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                result = new Gson().fromJson(response, PLCInfo.class);
                if (result.getErr_code() != 0) {
                    setException(new Exception(result.getMessage()));
                    return TaskResult.FAILED;
                }
            }
            publishProgress(result);
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }
}
