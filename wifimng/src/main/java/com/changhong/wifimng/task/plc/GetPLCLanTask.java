package com.changhong.wifimng.task.plc;

import com.changhong.wifimng.been.plc.LanV4Info;
import com.changhong.wifimng.been.plc.PLCGuestInfo;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

public class GetPLCLanTask extends GenericTask {
    public final GetPLCLanTask execute(String gateway, TaskListener<LanV4Info> listener) {
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
            final String url = String.format("http://%s:8081/rpc", gateway);
            String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ipv4_lan_show\",\"param\":{\"src_type\":1}}";
            response = DefaultHttpUtils.httpPostWithText(url, body);
            LanV4Info been = new Gson().fromJson(response, LanV4Info.class);
            if (been.getErr_code() != 0) {
                setException(new Exception(been.getMessage()));
                return TaskResult.FAILED;
            }
// end
            publishProgress(been);
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }
}
