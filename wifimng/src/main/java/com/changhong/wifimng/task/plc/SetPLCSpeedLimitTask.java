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

public class SetPLCSpeedLimitTask extends GenericTask {
    public final SetPLCSpeedLimitTask execute(String gateway, String mac, int txSpeedLimit, int rxSpeedLimit, TaskListener listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put("txSpeedLimit", txSpeedLimit);
        params.put("rxSpeedLimit", rxSpeedLimit);
        params.put("mac", mac);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }


    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String mac = params[0].getString("mac");
        int txSpeedLimit = params[0].get("txSpeedLimit");
        int rxSpeedLimit = params[0].get("rxSpeedLimit");
        try {
            String response;
            String body;
            final String url = String.format("http://%s:8081/rpc", gateway);
            body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_speed_limit_setting\",\"param\":{\"src_type\":1,\"tx_speed_limit\":" + txSpeedLimit + ",\"rx_speed_limit\":" + rxSpeedLimit + ",\"mac\":\"" + mac + "\" }}";
            response = DefaultHttpUtils.httpPostWithText(url, body);
            BaseResponseBeen result = new Gson().fromJson(response, BaseResponseBeen.class);
            if (result.getErr_code() != 0) {
                setException(new Exception(result.getMessage()));
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
