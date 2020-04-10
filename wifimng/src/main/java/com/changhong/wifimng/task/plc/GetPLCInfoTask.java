package com.changhong.wifimng.task.plc;

import android.text.TextUtils;

import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

public class GetPLCInfoTask extends GenericTask {
    public final GetPLCInfoTask execute(String gateway, TaskListener<PLCInfo> listener) {
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
            {//                step2
                body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"plc_list_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                PLCInfo been = new Gson().fromJson(response, PLCInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                result.setPlc_node(been.getPlc_node());
            }
            {// step3
                body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_running_status_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                PLCInfo been = new Gson().fromJson(response, PLCInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }

                result.setUptime(been.getUptime());
                result.setWan_ip(been.getWan_ip());
                result.setNetwork_time(been.getNetwork_time());
                result.setSta_num(been.getSta_num());
            }
            {// step4
                body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_basic_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                PLCInfo been = new Gson().fromJson(response, PLCInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                result.setDev_model(been.getDev_model());
                result.setManufacturer(been.getManufacturer());
                result.setDev_identity(been.getDev_identity());
                result.setHw_ver(been.getHw_ver());
                result.setDev_desc(been.getDev_desc());
            }

            {// step5
                body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_link_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                PLCInfo been5 = new Gson().fromJson(response, PLCInfo.class);
                if (been5.getErr_code() != 0) {
                    setException(new Exception(been5.getMessage()));
                    return TaskResult.FAILED;
                }
                result.setDev_info(been5.getDev_info());
            }
            {// step6
                body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wan_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                PLCInfo been = new Gson().fromJson(response, PLCInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                been.writeWanInfo2Obj(result);
            }
// end
            publishProgress(result);
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }
}
