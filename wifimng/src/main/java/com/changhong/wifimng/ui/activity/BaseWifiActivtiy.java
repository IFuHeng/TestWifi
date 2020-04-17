package com.changhong.wifimng.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.receiver.WifiReceiver;

/**
 * Created by Administrator on 2019/11/7.
 */
public class BaseWifiActivtiy extends BaseActivtiy implements WifiReceiver.WifiReceiverListener {
    public static final int REQUEST_CODE_WIZARD = (int) Math.round(Math.random());
    public static final int REQUEST_CODE_WIFI_SETTING = REQUEST_CODE_WIZARD + 1;

    public static final String ACTION_EXIT_APP = "com.changhong.wifimng.action.ACTION_APP_EXIT";
    public static final String ACTION_UNAUTHORIZED = "com.changhong.wifimng.action.ACTION_UNAUTHORIZED";
    public static final String ACTION_RECONNECTED_THE_WIFI = "com.changhong.wifimng.action.ACTION_RECONNECTED_THE_WIFI";
    public static final String ACTION_CONNECT_REFUSED = "com.changhong.wifimng.action.ACTION_CONNECT_REFUSED";
    protected WifiReceiver mReceiver;

    private ExitInfoReceiveBroadcast exitInfoReceiveBroadcast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_EXIT_APP);
        filter.addAction(ACTION_RECONNECTED_THE_WIFI);
        filter.addAction(ACTION_UNAUTHORIZED);
        filter.addAction(ACTION_CONNECT_REFUSED);
        exitInfoReceiveBroadcast = new ExitInfoReceiveBroadcast();
        LocalBroadcastManager.getInstance(this).registerReceiver(exitInfoReceiveBroadcast, filter);

        mReceiver = new WifiReceiver(this);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(exitInfoReceiveBroadcast);
        super.onDestroy();
    }

    /**
     * 开启或关闭网络变化开关
     *
     * @param isOn true 开 ；false 关
     */
    protected void setWifiStateChangeListenerOnOrOff(boolean isOn) {
        if (isOn) {
            mReceiver.registReceiver(this);
        } else
            mReceiver.unregistReceiver(this);
    }

    @Override
    public void onPickWifiNetwork() {

    }

    @Override
    public void onScanResults(boolean isSuccess) {

    }

    @Override
    public void onRssiChange() {

    }

    @Override
    public void onConnectNetInfoChanged(NetworkInfo networkInfo) {

    }

    @Override
    public void onWifiStateChange(WifiReceiver.EnumWifiStatus status) {

    }

    /**
     * 处理授权失效的情况
     */
    protected void dealActionUnauthorized() {
        showAlert(getString(R.string.authorization_failed), getString(R.string._exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                LocalBroadcastManager.getInstance(BaseWifiActivtiy.this).sendBroadcast(new Intent(ACTION_EXIT_APP));
            }
        }, false);
    }

    /**
     * 处理授权失效的情况
     */
    protected void dealConnectFailed(String message) {
        showAlert(message, getString(R.string._exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        }, false);
    }

    private class ExitInfoReceiveBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_EXIT_APP.equals(action)) {
                finish();
            } else if (ACTION_UNAUTHORIZED.equals(action)) {
                dealActionUnauthorized();
            } else if (ACTION_CONNECT_REFUSED.equals(action)) {
//                dealConnectFailed(intent.getStringExtra(Intent.EXTRA_TEXT));
            } else if (ACTION_RECONNECTED_THE_WIFI.equals(action)) {
                //TODO
            }
        }
    }
}
