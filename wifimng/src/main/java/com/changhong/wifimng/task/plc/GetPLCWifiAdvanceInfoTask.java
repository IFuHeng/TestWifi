package com.changhong.wifimng.task.plc;

import com.changhong.wifimng.been.plc.WifiAdvanceInfo;
import com.changhong.wifimng.been.plc.WifiBaseInfo;
import com.changhong.wifimng.been.plc.WlanInfo;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

public class GetPLCWifiAdvanceInfoTask extends GenericTask {
    public final GetPLCWifiAdvanceInfoTask execute(String gateway, TaskListener<WlanInfo> listener) {
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
            WlanInfo result = new WlanInfo();
            String response;
            final String url = String.format("http://%s:8081/rpc", gateway);
            {//step1 获取2Gwifi 高级信息
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wlan_advanced_2_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                WifiAdvanceInfo been = new Gson().fromJson(response, WifiAdvanceInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                result.setWifiAdvanceInfo2G(been);
            }
            {//step2 获取5Gwifi 高级信息
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wlan_advanced_5_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                WifiAdvanceInfo been = new Gson().fromJson(response, WifiAdvanceInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                result.setWifiAdvanceInfo5G(been);
            }
            {//step3 获取2Gwifi 基本状态
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wlan_basic_2_show\",\"param\":{\"src_type\":1,\"ssid_idx\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                WifiBaseInfo been = new Gson().fromJson(response, WifiBaseInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                been.setSsid_idx(1);
                result.setWifiBase2G(been);
            }
            {//step4 获取5Gwifi 基本状态
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wlan_basic_5_show\",\"param\":{\"src_type\":1,\"ssid_idx\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                WifiBaseInfo been = new Gson().fromJson(response, WifiBaseInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                been.setSsid_idx(1);
                result.setWifiBase5G(been);
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
