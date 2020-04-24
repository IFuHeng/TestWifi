package com.changhong.wifimng.task.plc;

import com.changhong.wifimng.been.BaseResponseBeen;
import com.changhong.wifimng.been.PLCRequestBeen;
import com.changhong.wifimng.been.plc.DeviceCustormInfoBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.uttils.WifiMacUtils;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;

/**
 * 更新用户自定义个性化数据
 * 包含：
 * 1、删除用户自定义的个性化数据
 * 2、描述：设置设备基本信息，主要用于为设备重新命名，添加位置信息。
 */
public class UpdateCustomerInfoTask extends GenericTask {
    public final UpdateCustomerInfoTask execute(Integer index, String mac, String dev_name, String dev_location, String gateway, TaskListener listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put("mac", mac);
        params.put("dev_name", dev_name);
        params.put("dev_location", dev_location);
        params.put("index", index);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }


    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String mac = params[0].getString("mac");
        mac = WifiMacUtils.macShownFormat(mac);
        String dev_name = params[0].getString("dev_name");
//        if (dev_name != null)
//            try {
//                dev_name = new String(dev_name.getBytes(), "utf-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }

        String dev_location = params[0].getString("dev_location");
//        if (dev_location != null)
//            try {
//                dev_location = new String(dev_location.getBytes(), "utf-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }

        Integer index = params[0].get("index");

        final String url = String.format("http://%s:8081/rpc", gateway);
        try {
            if (index != null)
                delete(url, index);

            add(url, mac, dev_name, dev_location);

        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }

    private void delete(String url, int index) throws Exception {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_customer_info_del\",\"param\":{\"src_type\":1,\"index\":" + index + "}}";
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
        if (been.getErr_code() != 0) {
            throw new Exception(been.getMessage());
        }
    }

    private void add(String url, String mac, String dev_name, String dev_location) throws Exception {
//        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_customer_info_add\",\"param\":{\"src_type\":1" +
//                ",\"mac\":\"" + mac + "\"" +
//                ",\"dev_name\":" + dev_name +
//                ",\"dev_location\":" + dev_location
//                + "}}";
        String body = new PLCRequestBeen("device_customer_info_add",
                new DeviceCustormInfoBeen.CuntomerInfo(mac, dev_name, dev_location))
                .toJsonString();
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
        if (been.getErr_code() != 0) {
            throw new Exception(been.getMessage());
        }
    }
}
