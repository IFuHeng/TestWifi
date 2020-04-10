package com.changhong.wifimng.task.plc;

import com.changhong.wifimng.been.BaseResponseBeen;
import com.changhong.wifimng.been.PLCRequestBeen;
import com.changhong.wifimng.been.plc.PLCGuestInfo;
import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

public class SetPLCLinkInfoTask extends GenericTask {
    public final SetPLCLinkInfoTask execute(String gateway, PLCGuestInfo info, TaskListener listener) {
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
        PLCGuestInfo info = params[0].get("info");
        try {
            String response;
            final String url = String.format("http://%s:8081/rpc", gateway);
            info.setTime_left(null);
            String body = new PLCRequestBeen("wlan_guest_setting", info).toJsonString();
            response = DefaultHttpUtils.httpPostWithText(url, body);
            BaseResponseBeen been5 = new Gson().fromJson(response, BaseResponseBeen.class);
            if (been5.getErr_code() != 0) {
                setException(new Exception(been5.getMessage()));
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
