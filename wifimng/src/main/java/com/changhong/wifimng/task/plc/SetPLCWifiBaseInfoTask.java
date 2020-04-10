package com.changhong.wifimng.task.plc;

import com.changhong.wifimng.been.BaseResponseBeen;
import com.changhong.wifimng.been.PLCRequestBeen;
import com.changhong.wifimng.been.plc.WifiAdvanceInfo;
import com.changhong.wifimng.been.plc.WlanInfo;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

public class SetPLCWifiBaseInfoTask extends GenericTask {
    public final SetPLCWifiBaseInfoTask execute(String gateway, WlanInfo info, TaskListener listener) {
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
        WlanInfo info = params[0].get("info");
        try {
            String response;
            final String url = String.format("http://%s:8081/rpc", gateway);
            if (info.getEnable() != null) {//step1 设置双频开关状态
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"dual_band_optimization_setting\",\"param\":{\"src_type\":1,\"enable\":" + info.getEnable() + "}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }
            if (info.getBus_switch_2() != null) {//step2 设置2.4Gwifi 开关状态
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wlan_switch_2_setting\",\"param\":{\"src_type\":1,\"bus_switch_2\":" + info.getBus_switch_2() + "}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }
            if (info.getBus_switch_5() != null) {//step3 设置5Gwifi 开关状态
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wlan_switch_5_setting\",\"param\":{\"src_type\":1,\"bus_switch_5\":" + info.getBus_switch_5() + "}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }
            if (info.getWifiBase2G() != null) {//step4 设置2Gwifi 基本状态
                String body = new PLCRequestBeen("wlan_basic_2_setting", info.getWifiBase2G()).toJsonString();
                response = DefaultHttpUtils.httpPostWithText(url, body);
                BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }
            if (info.getWifiBase5G() != null) {//step5 设置5Gwifi 基本状态
                String body = new PLCRequestBeen("wlan_basic_5_setting", info.getWifiBase5G()).toJsonString();
                response = DefaultHttpUtils.httpPostWithText(url, body);
                BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }
            if (info.getWifiAdvanceInfo2G() != null) {//step6 设置2Gwifi 高级信息
                String body = new PLCRequestBeen("wlan_advanced_2_setting", info.getWifiAdvanceInfo2G()).toJsonString();
                response = DefaultHttpUtils.httpPostWithText(url, body);
                BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }
            if (info.getWifiAdvanceInfo5G() != null) {//step7 设置5Gwifi 高级信息
                String body = new PLCRequestBeen("wlan_advanced_5_setting", info.getWifiAdvanceInfo5G()).toJsonString();
                response = DefaultHttpUtils.httpPostWithText(url, body);
                WifiAdvanceInfo been = new Gson().fromJson(response, WifiAdvanceInfo.class);
                if (been.getErr_code() != 0) {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }
// end
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }
}
