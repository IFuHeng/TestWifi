package com.changhong.wifimng.task.plc;

import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

public class GetPLCLinkInfoTask extends GenericTask {
    public final GetPLCLinkInfoTask execute(String gateway, TaskListener<PLCInfo> listener) {
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
            PLCInfo been;
            {//step 1
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_link_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                been = new Gson().fromJson(response, PLCInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }

            {//step 2
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wan_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                PLCInfo been5 = new Gson().fromJson(response, PLCInfo.class);
                if (been5.getErr_code() != 0) {
                    setException(new Exception(been5.getMessage()));
                    return TaskResult.FAILED;
                }
                been.setAddr_type(been5.getAddr_type());
                been.setIp_addr(been5.getIp_addr());
            }
            {//step 3
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wlan_2_status_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                PLCInfo been5 = new Gson().fromJson(response, PLCInfo.class);
                if (been5.getErr_code() != 0) {
                    setException(new Exception(been5.getMessage()));
                    return TaskResult.FAILED;
                }
                been.setPkt_stat(been5.getPkt_stat());
                been.setConn_status_2(been5.getConn_status_2());
                been.setChannel_2(been5.getChannel_2());
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
