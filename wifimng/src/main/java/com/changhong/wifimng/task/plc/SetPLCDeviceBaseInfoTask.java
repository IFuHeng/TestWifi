package com.changhong.wifimng.task.plc;

import com.changhong.wifimng.been.BaseResponseBeen;
import com.changhong.wifimng.been.PLCRequestBeen;
import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

public class SetPLCDeviceBaseInfoTask extends GenericTask {
    public final SetPLCDeviceBaseInfoTask execute(String gateway, PLCInfo info, TaskListener listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put("info", info);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }


    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        PLCInfo info = params[0].get("info");
        try {
            String response;
            String body;
            final String url = String.format("http://%s:8081/rpc", gateway);
            {//                step1
//                body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wan_show\",\"param\":{\"src_type\":1}}";
                body = new PLCRequestBeen("wan_setting", info).toJsonString();
                response = DefaultHttpUtils.httpPostWithText(url, body);
                BaseResponseBeen result = new Gson().fromJson(response, BaseResponseBeen.class);
                if (result.getErr_code() != 0) {
                    setException(new Exception(result.getMessage()));
                    return TaskResult.FAILED;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }
}
