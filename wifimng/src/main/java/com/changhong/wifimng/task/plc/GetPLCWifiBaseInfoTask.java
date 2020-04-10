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

public class GetPLCWifiBaseInfoTask extends GenericTask {
    public final GetPLCWifiBaseInfoTask execute(String gateway, TaskListener<WlanInfo> listener) {
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
            WlanInfo result = new WlanInfo();
            {//step1 获取双频开关状态
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"dual_band_optimization_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                WlanInfo been = new Gson().fromJson(response, WlanInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                result.setEnable(been.getEnable());
            }
            {//step2 获取2.4Gwifi 开关状态
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wlan_switch_2_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                WlanInfo been = new Gson().fromJson(response, WlanInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                result.setBus_switch_2(been.getBus_switch_2());
            }
            {//step3 获取5Gwifi 开关状态
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wlan_switch_5_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                WlanInfo been = new Gson().fromJson(response, WlanInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                result.setBus_switch_5(been.getBus_switch_5());
            }
            {//step4 获取2Gwifi 基本状态
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
            {//step5 获取5Gwifi 基本状态
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
            {//step6 获取2Gwifi 高级信息
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wlan_advanced_2_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                WifiAdvanceInfo been = new Gson().fromJson(response, WifiAdvanceInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                result.setWifiAdvanceInfo2G(been);
            }
            {//step7 获取5Gwifi 高级信息
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wlan_advanced_5_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                WifiAdvanceInfo been = new Gson().fromJson(response, WifiAdvanceInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
                result.setWifiAdvanceInfo5G(been);
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
