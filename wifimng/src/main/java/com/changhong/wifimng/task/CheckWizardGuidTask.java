package com.changhong.wifimng.task;

import android.content.Context;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.guide.ResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

/**
 * 校验引导类型状态
 */
public class CheckWizardGuidTask extends GenericTask {

    public final CheckWizardGuidTask execute(Context context, String gateway, String deviceType, TaskListener<Integer> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put("context", context);
        params.put("deviceType", deviceType);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String deviceType = params[0].getString("deviceType");
        Context context = params[0].get("context");

        try {
            String response;
            if (DeviceType.PLC.getName().equalsIgnoreCase(deviceType)) {// PLC
                String url = "http://" + gateway + ":8081/rpc";
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wizard_guid_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                if (response == null)
                    throw new Exception(context.getString(R.string.connected_wrong_device));
            }

            //
            else {
                String url = "http://" + gateway + ":80/rpcsupper";
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wizard_get_guid\",\"params\":{\"src_type\":1}}";

                response = DefaultHttpUtils.httpPostWithJson(url, body);
            }

            ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
            if (been.getErr_code() == 0) {
                publishProgress(been.getGuid_flag());
                return TaskResult.OK;
            } else
                setException(new Exception(been.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.FAILED;
    }


}
