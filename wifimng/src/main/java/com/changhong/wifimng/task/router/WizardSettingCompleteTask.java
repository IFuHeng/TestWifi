package com.changhong.wifimng.task.router;

import android.accounts.AuthenticatorException;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.PLCRequestBeen;
import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.guide.RequireAllBeen;
import com.changhong.wifimng.been.guide.ResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.uttils.PskType;
import com.changhong.wifimng.uttils.WifiUtils;
import com.google.gson.Gson;

import java.util.concurrent.TimeoutException;


/**
 * 提交线程
 */
public class WizardSettingCompleteTask extends BaseRouterTask {

    private boolean isRunning;
    private WifiManager mWifiManager;
    private Context mContext;

    public final WizardSettingCompleteTask execute(Context context, String gateway, String deviceType, RequireAllBeen wanBeen, RequireAllBeen wifiBeen, String cookies, TaskListener<CharSequence> listener) {
        mContext = context;
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put("deviceType", deviceType);
        params.put("wanBeen", wanBeen);
        params.put("wifiBeen", wifiBeen);
        params.put(KeyConfig.KEY_COOKIE_SSID, cookies);
        setListener(listener);
        execute(params);

        this.mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        isRunning = true;

        String gateway = params[0].getString("gateway");
        String deviceType = params[0].getString("deviceType");

        RequireAllBeen wanBeen = params[0].get("wanBeen");
        RequireAllBeen wifiBeen = params[0].get("wifiBeen");
        wifiBeen.setKey_sync(0);

        String cookies = params[0].get(KeyConfig.KEY_COOKIE_SSID);
        try {
            if (DeviceType.PLC.getName().equals(deviceType)) {// 电力猫的情况
                if (!commitPLC(gateway, wanBeen, wifiBeen))
                    return TaskResult.FAILED;
            }

            // mesh or router
            else {
                if (!commitRouterOrMesh(gateway, wanBeen, wifiBeen, cookies)) {
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

        // 等待重新连接上
        sleep(3000);
        try {
            connectAimWifi(wifiBeen);
        } catch (TimeoutException e) {
            e.printStackTrace();
            publishProgress(e.getMessage());
        }

        return TaskResult.OK;
    }

    @Override
    protected void onCancelled() {
        isRunning = false;
        super.onCancelled();
    }

    /**
     * 等待已连接ssid匹配上目标ssid
     *
     * @param been
     */
    private void connectAimWifi(RequireAllBeen been) throws TimeoutException {
        String aimSsid = been.getSsid();
        if (been.get_5G_priority() != null && been.get_5G_priority() == 0)
            aimSsid = been.getSsid_2G();
        reconnect(been);
        long startTime = System.currentTimeMillis();
        while (isRunning) {
            String temp = getCurrentSSID();
            Log.d(getClass().getSimpleName(), "====~ ssid = " + aimSsid + " , temp = " + temp);
            if (temp == null)
                sleep(1000);
            else if (!aimSsid.equals(temp)) {
                // 重新连接指定ssid
//                publishProgress(been);
                reconnect(been);
                sleep(5000);
            } else if (mWifiManager.getDhcpInfo().ipAddress == 0) {
                sleep(1000);
            } else
                break;

            if (System.currentTimeMillis() - startTime > 30000) {
                Log.d(getClass().getSimpleName(), "====~ reconnect time out");
                throw new TimeoutException(String.format(mContext.getString(R.string.reconnect_aim_wifi_timeout), aimSsid));
            }
        }
    }

    private boolean commitRouterOrMesh(String gateway, RequireAllBeen wanBeen, RequireAllBeen wifiBeen, String cookies) throws Exception {
        String url = "http://" + gateway + ":80/rpc";

        Gson gson = new Gson();

        if (isRunning) { //wan
            String response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen<>(HttpRequestMethod.METHOD_WIZARD_SETTING, wanBeen).toJsonString(), cookies);
            ResponseAllBeen responseAllBeen = gson.fromJson(response, ResponseAllBeen.class);
            if (responseAllBeen.getErr_code() != 0) {
                setException(new Exception(responseAllBeen.getMessage()));
                return false;
            }
        }

        if (isRunning) { //wifi
            String response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen<>(HttpRequestMethod.METHOD_WIZARD_SET_WIRELESS, wifiBeen).toJsonString(), cookies);
            ResponseAllBeen responseAllBeen = new Gson().fromJson(response, ResponseAllBeen.class);
            if (responseAllBeen.getErr_code() != 0) {
                setException(new Exception(responseAllBeen.getMessage()));
                return false;
            }

        }

        if (isRunning) { //effect
            RequireAllBeen been = new RequireAllBeen();
            been.setGuid_flag(1);
            String response = DefaultHttpUtils.httpPostWithJson(url,
                    new RequestBeen<>(HttpRequestMethod.METHOD_WIZARD_SET_GUID, been).toJsonString(), cookies);
            ResponseAllBeen responseAllBeen = new Gson().fromJson(response, ResponseAllBeen.class);
            if (responseAllBeen.getErr_code() != 0) {
                setException(new Exception(responseAllBeen.getMessage()));
                return false;
            }
        }

        if (isRunning) { //wifi 生效
            wifiBeen.setSave_action(1);
            try {
                String response = DefaultHttpUtils.httpPostWithJson(url,
                        new RequestBeen<>(HttpRequestMethod.METHOD_WIZARD_SET_WIRELESS, wifiBeen).toJsonString(), cookies);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private boolean commitPLC(String gateway, RequireAllBeen wanBeen, RequireAllBeen wifiBeen) throws Exception {
        String response;

        final String url = String.format("http://%s:8081/rpc", gateway);
        if (isRunning) { //wan
            String body = new PLCRequestBeen("wizard_wan_setting", wanBeen).toJsonString();
            response = DefaultHttpUtils.httpPostWithText(url, body);
            ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
            if (been.getErr_code() != 0) {
                setException(new Exception(been.getMessage()));
                return false;
            }
        }
        if (isRunning) { //wan
            wifiBeen.setKey_sync(0);//要求必须为0
            String body = new PLCRequestBeen("wizard_wireless_setting", wifiBeen).toJsonString();
            response = DefaultHttpUtils.httpPostWithText(url, body);
            ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
            if (been.getErr_code() != 0) {
                setException(new Exception(been.getMessage()));
                return false;
            }
        }
        if (isRunning) { //effect
            String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wizard_guid_setting\",\"param\":{\"src_type\":1,\"guid_flag\":1}}";
            response = DefaultHttpUtils.httpPostWithText(url, body);
            ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
            if (been.getErr_code() != 0) {
                setException(new Exception(been.getMessage()));
                return false;
            }
        }
        return true;
    }

    /**
     * 等待重新连接wifi
     *
     * @param wifiBeen
     */
    private void reconnect(RequireAllBeen wifiBeen) {
        String ssid = wifiBeen.getSsid();
        if (wifiBeen.get_5G_priority() != null && wifiBeen.get_5G_priority() == 0) {
            ssid = wifiBeen.getSsid_2G();
        }

        String password = wifiBeen.getKey();
        PskType type = null;// none/wpa2-psk/wpa2_mixed_psk
        if ("none".equals(wifiBeen.getEncryption()))
            type = PskType.UNKNOWN;
        else if ("wpa2_mixed_psk".equals(wifiBeen.getEncryption()))
            type = PskType.WPA_WPA2;
        else if ("wpa2-psk".equals(wifiBeen.getEncryption()))
            type = PskType.WPA2;
        WifiUtils.connectNetwork(mWifiManager, ssid, password, type);
    }

    public String getCurrentSSID() {
        // notice not run in ui thread
        // 用来获取当前网络是否连接上了目标ssid
        if (mWifiManager.isWifiEnabled() && mWifiManager.getConnectionInfo().getNetworkId() != -1) {
            return WifiUtils.getWifiSSID(mContext, mWifiManager);
        }
        return null;
    }

}
