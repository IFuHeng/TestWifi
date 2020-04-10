package com.changhong.wifimng.task;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.sys.SettingResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * 校验引导类型状态
 */
public class GetDeviceTypeTask extends GenericTask {

    public final GetDeviceTypeTask execute(String gateway, String errorUnableConnect, TaskListener<String> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put("errorUnableConnect", errorUnableConnect);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String errorUnableConnect = params[0].get("errorUnableConnect");

        String response = getDeviceTypeFromBwrR2s(gateway);
        if (response == null) {
            response = getDeviceTypeFromPLC(gateway);
            if (response == null) {
                setException(new Exception(errorUnableConnect != null ? errorUnableConnect : "Unable to connect to Changhong network device."));
                return TaskResult.FAILED;
            }
        }
        try {
            SettingResponseAllBeen been = new Gson().fromJson(response, SettingResponseAllBeen.class);
            if (been.getErr_code() == 0) {
                publishProgress(been.getDev_type());
                return TaskResult.OK;
            } else
                setException(new Exception(been.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            setException(new Exception(errorUnableConnect));
        }
        return TaskResult.FAILED;
    }

    private String getDeviceTypeFromBwrR2s(String gateway) {
        String url = "http://" + gateway + ":80/rpcsupper";
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_type_get\",\"params\":{\"src_type\":1}}";

        try {
            String response = DefaultHttpUtils.httpPostWithJson(url, body);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getDeviceTypeFromPLC(String gateway) {
        String url = "http://" + gateway + ":8081/rpc";
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_type_get\",\"param\":{\"src_type\":1}}";
        try {
            String response = DefaultHttpUtils.httpPostWithText(url, body);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }


}
