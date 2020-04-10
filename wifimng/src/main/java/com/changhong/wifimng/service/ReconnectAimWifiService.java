package com.changhong.wifimng.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.receiver.WifiReceiver;
import com.changhong.wifimng.ui.activity.BaseActivtiy;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.uttils.WifiUtils;

import java.util.HashMap;
import java.util.List;

public class ReconnectAimWifiService extends Service implements WifiReceiver.WifiReceiverListener {

    private String mAimSsid;

    private WifiReceiver mWifiReceiver;

    private WifiManager mWifiManager;

    private boolean isFirstFound;

    private HashMap<String, NetworkInfo> mHashNetworkInfo = new HashMap<>();
    private String TAG = "=====>" + getClass().getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWifiReceiver = new WifiReceiver(this);
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        Log.d(TAG, "onCreate ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!intent.hasExtra(Intent.EXTRA_TEXT))
            stopSelf();

        mAimSsid = intent.getStringExtra(Intent.EXTRA_TEXT);
        mWifiReceiver.registReceiver(this);

        Log.d(TAG, "onStartCommand: mAimssid = " + mAimSsid);

        isFirstFound = false;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mWifiReceiver.unregistReceiver(this);
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    public void disconnectWiif(String ssid, NetworkInfo networkInfo) {


//        mHashNetworkInfo.put(ssid, networkInfo);
//        WifiConfiguration wifiCfg = getExsitsWifiConfiguration(ssid);
//        int netId = wifiCfg.networkId;
//        mWifiManager.removeNetwork(netId);
//        wifiCfg.
//        netId = mWifiManager.addNetwork(wifiCfg);
        mWifiManager.disconnect();

    }

    //检测该SSID是否已存在
    private WifiConfiguration getExsitsWifiConfiguration(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    private boolean connect2aimWifi() {
        WifiConfiguration wifiConfig = getExsitsWifiConfiguration(mAimSsid);
        if (wifiConfig != null) {
//            return new WifiAdmin(this).addNetwork(wifiConfig);
            boolean result = mWifiManager.enableNetwork(wifiConfig.networkId, true);
            result = result && mWifiManager.reconnect();
            return true;
        }

        return false;
    }

    @Override
    public void onPickWifiNetwork() {

    }

    @Override
    public void onScanResults(boolean isSuccess) {
        if (mWifiManager.getConnectionInfo().getNetworkId() != -1 && !isFirstFound) {
            for (ScanResult scanResult : mWifiManager.getScanResults()) {
                if (scanResult.SSID.equals(mAimSsid)) {
                    isFirstFound = true;
                    Log.d(TAG, "onScanResults: ssid = " + mAimSsid + ", connect to " + connect2aimWifi());

                }
            }
        }
    }


    @Override
    public void onRssiChange() {

    }

    @Override
    public void onConnectNetInfoChanged(NetworkInfo networkInfo) {
        if (networkInfo.isConnectedOrConnecting()) {
            String ssid = WifiUtils.clearSSID(networkInfo.getExtraInfo());

            if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {

                if (!ssid.equals(mAimSsid)) {
                    connect2aimWifi();
                } else {
                    stopSelf();
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BaseWifiActivtiy.ACTION_EXIT_APP));
                }
            }
        }
    }

    @Override
    public void onWifiStateChange(WifiReceiver.EnumWifiStatus status) {

    }

    public class LocalBinder extends Binder {

        ReconnectAimWifiService getService() {
            return ReconnectAimWifiService.this;
        }

    }
}
