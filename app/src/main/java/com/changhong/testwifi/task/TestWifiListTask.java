package com.changhong.testwifi.task;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

public class TestWifiListTask extends AsyncTask<String, String, String> {

    private Context mContext;
    private WifiManager mWifiManager;
    private boolean isRunning;

    public TestWifiListTask(Context mContext, WifiManager wifimanager) {
        this.mContext = mContext;
        mWifiManager = wifimanager;
    }

    public TestWifiListTask execute(String[] SSID, boolean[] funList, OnCallback<String, String> onCallback) {


        return this;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();


    }

    @Override
    protected String doInBackground(String... strings) {


        return null;
    }

    private boolean connect2aimWifi(WifiConfiguration wifiConfig) {
        boolean result = mWifiManager.enableNetwork(wifiConfig.networkId, true);
        result = result && mWifiManager.reconnect();
        return result;
    }
}
