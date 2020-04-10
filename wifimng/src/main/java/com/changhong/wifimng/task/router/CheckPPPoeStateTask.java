package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.wan.ResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

import java.util.Map;

/**
 * 获取pppoe状态设置
 */
public class CheckPPPoeStateTask extends BaseRouterTask {

    public final CheckPPPoeStateTask execute(String gateway, String cookies, TaskListener<EnumPPPoeState> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        try {
            String url = "http://" + gateway + ":80/rpc";
            String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"" + HttpRequestMethod.METHOD_CHECK_PPPOE_STATE + "\",\"params\":{\"src_type\":1}}";

            while (true) {
                String response = DefaultHttpUtils.httpPostWithJson(url, body, cookies);
                ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
                if (been.getErr_code() == 0) {
                    if (been.getPppoe_state() == null) {
                        publishProgress(EnumPPPoeState.UNKNOWN_ERROR);
                        return TaskResult.FAILED;
                    } else if (been.getPppoe_state() == EnumPPPoeState.CONNECTING.getValue()) {
                        publishProgress(EnumPPPoeState.CONNECTING);
                        Thread.sleep(500);
                        continue;
                    } else if (been.getPppoe_state() == EnumPPPoeState.ACCOUNT_OR_PASSWORD_ERROR.getValue()) {
                        publishProgress(EnumPPPoeState.ACCOUNT_OR_PASSWORD_ERROR);
                        break;
                    } else if (been.getPppoe_state() == EnumPPPoeState.DISCONNECT.getValue()) {
                        publishProgress(EnumPPPoeState.DISCONNECT);
                        break;
                    } else if (been.getPppoe_state() == EnumPPPoeState.CONNECTED.getValue()) {
                        publishProgress(EnumPPPoeState.CONNECTED);
                        break;
                    } else {
                        publishProgress(EnumPPPoeState.UNKNOWN_ERROR);
                        break;
                    }
                } else {
                    setException(new Exception(been.getMessage()));
                    return TaskResult.FAILED;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            if (e instanceof AuthenticatorException)
                return dealAuthenticatorException(gateway, params);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }

    /**
     * 0 -- 未连接；1 -- 连接中；2 -- 连接成功；3 -- 用户名或密码错误 ； 4 -- 未知错误。
     */
    public enum EnumPPPoeState {
        DISCONNECT(0, R.string.pppoe_state_disconnect),
        CONNECTING(1, R.string.pppoe_state_connecting),
        CONNECTED(2, R.string.pppoe_state_connected),
        ACCOUNT_OR_PASSWORD_ERROR(3, R.string.pppoe_state_account_or_passord_error),
        UNKNOWN_ERROR(4, R.string.pppoe_state_unknown_error);

        private int value;

        private int resId;

        EnumPPPoeState(int value, int resId) {
            this.value = value;
            this.resId = resId;
        }

        public int getValue() {
            return value;
        }

        public int getResId() {
            return resId;
        }


    }
}
