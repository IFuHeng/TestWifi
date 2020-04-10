package com.changhong.wifimng.task;

import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.guide.ResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.google.gson.Gson;

public class LoginTask extends GenericTask {

    public final LoginTask execute(String password, String gateway, String deviceType, String errorPasswordWrong, TaskListener<BaseBeen<String, String>> listener) {
        TaskParams params = new TaskParams();
        params.put("password", password);
        params.put("gateway", gateway);
        params.put("deviceType", deviceType);
        params.put("errorPasswordWrong", errorPasswordWrong);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        String errorPasswordWrong = params[0].getString("errorPasswordWrong");
        try {
            String password = params[0].getString("password");

            String gateway = params[0].getString("gateway");
            String deviceType = params[0].getString("deviceType");

            String response;
            if (DeviceType.PLC.getName().equals(deviceType)) {// 电力猫的情况
                final String url = String.format("http://%s:8081/rpc", gateway);
//                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"login\",\"param\":{\"src_type\":1,\"username\":\"admin\",\"passwd\":\"" + password + "\"}}";
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"login\",\"param\":{\"src_type\":1,\"username\":\"user\",\"passwd\":\"" + password + "\"}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
                if (been.getErr_code() == 0) {
                    publishProgress(new BaseBeen(password, null));
                    return TaskResult.OK;
                } else {
                    String message = been.getMessage();
                    if (message != null && message.contains("error"))
                        setException(new Exception(message));
                }

            } else {
                final String url = String.format("http://%s:80/login.html", gateway);
                String param = "password=" + password;
                response = DefaultHttpUtils.login(url, param);
                if (response != null) {
                    publishProgress(new BaseBeen(password, response));
                    return TaskResult.OK;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof java.net.ProtocolException && e.getMessage().contains("Unexpected status line"))
                setException(new Exception(errorPasswordWrong));
            else
                setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.FAILED;
    }
}
