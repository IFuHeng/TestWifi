package com.changhong.testwifi.task;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.guide.RequireAllBeen;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.uttils.PskType;
import com.changhong.wifimng.uttils.WifiUtils;

import java.util.List;
import java.util.concurrent.TimeoutException;


/**
 * 提交线程
 */
public class ConnectToOtherSSid extends GenericTask {

    private boolean isRunning;
    private WifiManager mWifiManager;
    private Context mContext;

    public final ConnectToOtherSSid execute(Context context, String ssid, TaskListener<CharSequence> listener) {
        mContext = context;
        TaskParams params = new TaskParams();
        params.put("ssid", ssid);
        setListener(listener);
        execute(params);

        this.mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {
        isRunning = true;

        String ssid = params[0].getString("ssid");
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
            // 等待重新连接上
            sleep(3000);
        }
        try {
            connectAimWifi(ssid);
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
     */
    private void connectAimWifi(String aimSsid) throws TimeoutException {
        connect2aimWifi(aimSsid);
        sleep(1000);
        long startTime = System.currentTimeMillis();
        while (isRunning) {
            String temp = getCurrentSSID();
            int ip = mWifiManager.getDhcpInfo().ipAddress;
            Log.d(getClass().getSimpleName(), "====~ ssid = " + aimSsid + " , temp = " + temp + ", ip =" + ip);
            if (temp == null
                    || (temp.indexOf('<') == 0 && temp.lastIndexOf('>') == temp.length() - 1))
                sleep(500);
            else if (!aimSsid.equals(temp)) {
                // 重新连接指定ssid
//                publishProgress(been);
//                mWifiManager.disableNetwork(getExsitsWifiConfiguration(temp).networkId);
                mWifiManager.disconnect();
                connect2aimWifi(aimSsid);
                sleep(5000);
            } else if (mWifiManager.getDhcpInfo().ipAddress == 0) {
                sleep(1000);
            } else {
                Log.d(getClass().getSimpleName(), "====~ reconnect success");
                break;
            }

            if (System.currentTimeMillis() - startTime > 30000) {
                Log.d(getClass().getSimpleName(), "====~ reconnect time out");
                throw new TimeoutException(String.format(mContext.getString(R.string.reconnect_aim_wifi_timeout), aimSsid));
            }
        }
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

    //检测该SSID是否已存在
    private WifiConfiguration getExsitsWifiConfiguration(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                Log.d(getClass().getSimpleName(), "====~ wificonfig exist {{{{{{   " + existingConfig.SSID);
                return existingConfig;
            }
        }
        return null;
    }

    private boolean connect2aimWifi(String ssid) {
        Log.d(getClass().getSimpleName(), "====~ connect2aimWifi ===>" + ssid);
        WifiConfiguration wifiConfig = getExsitsWifiConfiguration(ssid);
        if (wifiConfig != null) {
            boolean result = mWifiManager.enableNetwork(wifiConfig.networkId, true);
            Log.d(getClass().getSimpleName(), "====~ mWifiManager.enableNetwork() ===>" + result);
            if (!result) {
                result = mWifiManager.reconnect();
                Log.d(getClass().getSimpleName(), "====~ mWifiManager.reconnect() ===>" + result);
            }
            return true;
        }

        return false;
    }

}
