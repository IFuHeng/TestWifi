package com.changhong.wifimng.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class WifiReceiver extends BroadcastReceiver {

    private static final String TAG = "WifiReceiver";
    private WifiReceiverListener mListener;
    private boolean isRegisted;
    private static final boolean DEBUG = false;
    private NetworkInfo.DetailedState mLastNetworkState;
    private String mLastSsid;

    private static final long DEFAULT_DELAY_DURATION = 1000;
//    private NetworkInfo mLastDisconnectNetworkInfo;

    public WifiReceiver(WifiReceiverListener listener) {
        mListener = listener;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj == null)
                Log.d(getClass().getSimpleName(), "====~ mLastDisconnectNetworkInfo is null");
            else
                Log.d(getClass().getSimpleName(), "====~ mLastDisconnectNetworkInfo is " + msg.obj);

//            mListener.onConnectNetInfoChanged(mLastDisconnectNetworkInfo);
            mListener.onConnectNetInfoChanged((NetworkInfo) msg.obj);
            super.handleMessage(msg);
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        log("onReceive action > " + action);

        if (mListener == null) {
            log(" WifiReceiverListener is null, return. ");
            return;
        }
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int status = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            switch (status) {
                case WifiManager.WIFI_STATE_ENABLED:
                    log(" wifi state change:  enabled");
                    mListener.onWifiStateChange(EnumWifiStatus.WIFI_STATE_ENABLED);
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    log(" wifi state change:  disabled");
                    mListener.onWifiStateChange(EnumWifiStatus.WIFI_STATE_DISABLED);
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    log(" wifi state change:  关闭中");
                    mListener.onWifiStateChange(EnumWifiStatus.WIFI_STATE_DISABLING);
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    log(" wifi state change:  启动中");
                    mListener.onWifiStateChange(EnumWifiStatus.WIFI_STATE_ENABLING);
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    log(" wifi state change:  未知状态");
                    mListener.onWifiStateChange(EnumWifiStatus.WIFI_STATE_UNKNOWN);
                    break;
            }
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) { // 网络状态变更
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            log(" network state change:  networkInfo = " + networkInfo);
            if (networkInfo.getExtraInfo() != null
                    && (networkInfo.getExtraInfo().charAt(0) == '\"' && networkInfo.getExtraInfo().charAt(networkInfo.getExtraInfo().length() - 1) == '\"')) {
                if (mLastSsid != null && !mLastSsid.equals(networkInfo.getExtraInfo())) {//当ssid变更时，只更新上次状态。
                    cancelWifiDisconnectedDelay();
                    mHandler.sendEmptyMessage(0);
                    mLastNetworkState = networkInfo.getDetailedState();
                    mLastSsid = networkInfo.getExtraInfo();
                    return;
                }
                mLastSsid = networkInfo.getExtraInfo();
            }
            if (mLastNetworkState != networkInfo.getDetailedState()) {
                if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED)
                    addWifiDisconnected2Delay(context, networkInfo);
                else {
                    cancelWifiDisconnectedDelay();
                    mListener.onConnectNetInfoChanged(networkInfo);
                }
            }
            mLastNetworkState = networkInfo.getDetailedState();
        } else if (action.equals(WifiManager.NETWORK_IDS_CHANGED_ACTION)) {

        } else if (action.equals(WifiManager.ACTION_PICK_WIFI_NETWORK)) {
            mListener.onPickWifiNetwork();
        } else if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            boolean success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false);
            mListener.onScanResults(success);
        } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
            mListener.onRssiChange();
        }
    }

    public void registReceiver(Context context) {
        if (isRegisted)
            return;

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.ACTION_PICK_WIFI_NETWORK);

        context.registerReceiver(this, filter);
        isRegisted = true;
        log("registReceiver");
    }

    public void unregistReceiver(Context context) {
        try {
            if (isRegisted) {
                log("unregistReceiver");
                context.unregisterReceiver(this);
                isRegisted = false;
            }
            cancelWifiDisconnectedDelay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum EnumWifiStatus {
        WIFI_STATE_ENABLED, WIFI_STATE_DISABLED, WIFI_STATE_DISABLING, WIFI_STATE_ENABLING, WIFI_STATE_UNKNOWN;
    }

    private void log(String string) {
        if (DEBUG)
            Log.d(TAG, "=====~> " + string);
    }

    public interface WifiReceiverListener {
        void onPickWifiNetwork();

        void onScanResults(boolean isSuccess);

        void onRssiChange();

        void onConnectNetInfoChanged(NetworkInfo networkInfo);

        void onWifiStateChange(EnumWifiStatus status);
    }

    private void addWifiDisconnected2Delay(Context context, NetworkInfo delayInfo) {
        cancelWifiDisconnectedDelay();
        log("addWifiDisconnected2Delay");
//        mLastDisconnectNetworkInfo = delayInfo;
//        mHandler.sendEmptyMessageDelayed(0, DEFAULT_DELAY_DURATION);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(0, 0, 0, delayInfo), DEFAULT_DELAY_DURATION);
    }

    private void cancelWifiDisconnectedDelay() {
        log("cancelWifiDisconnectedDelay");
        mHandler.removeMessages(0);
    }

}
