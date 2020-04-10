package com.changhong.wifimng.task;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.guide.ResponseAllBeen;
import com.changhong.wifimng.been.sys.SettingRequireAllBeen;
import com.changhong.wifimng.been.sys.SettingResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.router.BaseRouterTask;
import com.google.gson.Gson;

import java.util.Map;

public class SetRouterPasswordTask extends BaseRouterTask {
    public final SetRouterPasswordTask execute(String password, String newPassword, String gateway, String deviceType, boolean isGuide, String errorPassord,  String cookies, TaskListener<String> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put("newPassword", newPassword);
        params.put("password", password);
        params.put("isGuide", isGuide);
        params.put("deviceType", deviceType);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        params.put("errorPassord", errorPassord);
        setListener(listener);
        execute(params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        String password = params[0].getString("password");
        String newPassword = params[0].getString("newPassword");
        String deviceType = params[0].getString("deviceType");
        String errorPassord = params[0].getString("errorPassord");
        boolean isGuide = params[0].get("isGuide");
        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);

        try {
            if (DeviceType.PLC.getName().equalsIgnoreCase(deviceType)) {
                if (!doPlc(gateway, password, newPassword, isGuide, errorPassord))
                    return TaskResult.FAILED;
            } else {
                if (!doRouterOrMesh(gateway, password, newPassword, isGuide, cookies, errorPassord))
                    return TaskResult.FAILED;
            }


            publishProgress(newPassword);
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            if (e instanceof AuthenticatorException)
                return dealAuthenticatorException(gateway, params);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }

    private boolean doRouterOrMesh(String gateway, String password, String newPassword, boolean isGuide, String cookies, String errorPassord) throws Exception {
        Gson gson = new Gson();
        String url;
        if (isGuide)
            url = "http://" + gateway + ":80/rpcsupper";
        else
            url = "http://" + gateway + ":80/rpc";
        SettingRequireAllBeen been = new SettingRequireAllBeen();
        been.setUser_password(password);
        been.setNew_user_password(newPassword);
        if (isGuide) {
            been.setWizard_flag(1);
        }

        String body = new RequestBeen<>(HttpRequestMethod.METHOD_USER_PSW_SETTING, been).toJsonString();
        String response = DefaultHttpUtils.httpPostWithJson(url, body, isGuide ? null : cookies);
        SettingResponseAllBeen responseAllBeen = gson.fromJson(response, SettingResponseAllBeen.class);

        if (responseAllBeen.getErr_code() != 0) {
            String message = responseAllBeen.getMessage();
            if (message != null && message.contains("password") && message.contains("error"))
                message = errorPassord;
            setException(new Exception(message));
            return false;
        }

        if (isGuide) {//设置引导状态为2（密码已设置）
            com.changhong.wifimng.been.guide.RequireAllBeen gbeen = new com.changhong.wifimng.been.guide.RequireAllBeen();
            gbeen.setGuid_flag(2);
            response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen<>(HttpRequestMethod.METHOD_WIZARD_SET_GUID, gbeen).toJsonString(), cookies);
            ResponseAllBeen responseAllBeen1 = gson.fromJson(response, ResponseAllBeen.class);
            if (responseAllBeen1.getErr_code() != 0) {
                setException(new Exception(responseAllBeen1.getMessage()));
                return false;
            }
        }
        return true;
    }

    private boolean doPlc(String gateway, String password, String newPassword, boolean isGuide, String errorPassord) throws Exception {
        String url = "http://" + gateway + ":8081/rpc";
        String body;
        if (isGuide)
            body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wizard_password_setting\",\"param\":{\"src_type\":1,\"password\":\"" + password + "\",\"save_action\":" + 1 + "}}";
        else {
//            body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"admin_passwd_setting\",\"param\":{\"src_type\":1,\"old_passwd\":\"" + password + "\",\"new_passwd\":\"" + newPassword + "\"}}";
            body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"user_passwd_setting\",\"param\":{\"src_type\":1,\"confirm_passwd\":\"" + newPassword + "\",\"new_passwd\":\"" + newPassword + "\"}}";
        }
        Gson gson = new Gson();
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        SettingResponseAllBeen responseAllBeen = gson.fromJson(response, SettingResponseAllBeen.class);
        if (responseAllBeen.getErr_code() != 0) {
            String message = responseAllBeen.getMessage();
            if (message != null && message.contains("password") && message.contains("error"))
                message = errorPassord;
            setException(new Exception(message));
            return false;
        }
        return true;
    }

}
