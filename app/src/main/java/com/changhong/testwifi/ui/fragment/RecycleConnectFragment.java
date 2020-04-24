package com.changhong.testwifi.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.changhong.testwifi.R;
import com.changhong.testwifi.receiver.WifiReceiver;
import com.changhong.testwifi.task.ConnectToOtherSSid;
import com.changhong.testwifi.utils.WifiUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.ui.fragment.BaseFragment;

import java.io.IOException;
import java.util.List;

public class RecycleConnectFragment extends BaseFragment implements WifiReceiver.WifiReceiverListener, View.OnClickListener {
    private String mAimSsid;

    private WifiReceiver mWifiReceiver;

    private WifiManager mWifiManager;


    private TextView mTvTimes;
    private TextView mTvWifiInfo;
    private TextView mTvSSID;
    private View mBtnChooseWifi;
    private List<ScanResult> mWifiList;
    private View mBtnPlay;

    private TouchScreemTask mTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiReceiver = new WifiReceiver(this);

        mWifiManager = (WifiManager) mActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        mWifiReceiver.registReceiver(mActivity.getApplicationContext());

        Log.d(getClass().getSimpleName(), "====~ scanresult = " + mWifiManager.getScanResults());
        mWifiManager.startScan();
    }

    @Override
    public void onDestroy() {
        handler.removeMessages(0);
        if (mTask != null) {
            mTask.interrupt();
        }
//        mWifiReceiver.unregistReceiver(mActivity.getApplicationContext());
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycle_connect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mTvTimes = (TextView) view.findViewById(R.id.tv_times);
        mTvSSID = (TextView) view.findViewById(R.id.tv_ssid);
        mTvWifiInfo = (TextView) view.findViewById(R.id.tv_wifi_info);

        mBtnChooseWifi = view.findViewById(R.id.btn_choose_wifi);
        mBtnChooseWifi.setOnClickListener(this);

        mBtnPlay = view.findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mWifiManager.getConnectionInfo() != null && mWifiManager.getConnectionInfo().getNetworkId() != -1) {
            mTvSSID.setText(WifiUtils.getWifiSSID(mActivity, mWifiManager));
            mAimSsid = mTvSSID.getText().toString();
        }

        setWifiInfo();
    }

    @Override
    public void onPickWifiNetwork() {

    }

    @Override
    public void onScanResults(boolean isSuccess) {
        mWifiList = mWifiManager.getScanResults();
        Log.d(getClass().getSimpleName(), "====~  >>>> mWifiList : " + mWifiManager.getScanResults());
        if (mWifiList != null) {
            for (ScanResult scanResult : mWifiList) {
                Log.d(getClass().getSimpleName(), "====~  >>>> wifi : " + scanResult);
            }
        }
        if (mBtnChooseWifi.getTag() != null && System.currentTimeMillis() - (Long) mBtnChooseWifi.getTag() < 2000) {

        }
    }

    @Override
    public void onRssiChange() {
        setWifiInfo();
    }

    private NetworkInfo.DetailedState mCurrentNetworkState;

    @Override
    public void onConnectNetInfoChanged(NetworkInfo networkInfo) {
        Log.i(getClass().getSimpleName(), "====~onConnectNetInfoChanged " + networkInfo);

        if (networkInfo.getDetailedState() == mCurrentNetworkState)
            return;
        mCurrentNetworkState = networkInfo.getDetailedState();
        String ssid = WifiUtils.getWifiSSID(mActivity, mWifiManager);

        if (mCurrentNetworkState == NetworkInfo.DetailedState.CONNECTED) {
            setTextWithSubText2(mTvSSID, mAimSsid, "已连接", Color.BLACK);
            if (mTvTimes.getTag() == null) {
                mTvTimes.setText("1次");
                mTvTimes.setTag(1);
            } else {
                int times = (Integer) mTvTimes.getTag();
                mTvTimes.setTag(++times);
                mTvTimes.setText(times + "次");
            }

            if (isPlayBtnRuningState()) {
                handler.sendEmptyMessageDelayed(0, 2000);
            }

        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.CONNECTING) {
            setTextWithSubText2(mTvSSID, mAimSsid, "连接中……", Color.DKGRAY);
            if (mAimSsid == null) {
                mAimSsid = ssid;
            } else if (!ssid.equals(mAimSsid)) {
                handler.sendEmptyMessageDelayed(1, 1000);
            }

        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.AUTHENTICATING) {
            setTextWithSubText2(mTvSSID, mAimSsid, "验证中……", Color.DKGRAY);
        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
            setTextWithSubText2(mTvSSID, mAimSsid, "获取IP中……", Color.DKGRAY);
        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
            setTextWithSubText2(mTvSSID, mAimSsid, "检查网络是否是受控门户", Color.DKGRAY);
        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.SCANNING) {
            setTextWithSubText2(mTvSSID, mAimSsid, "扫描中……", Color.BLUE);
        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.SUSPENDED) {
            setTextWithSubText2(mTvSSID, mAimSsid, "IP流量暂停", Color.RED);
        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.DISCONNECTING) {
            setTextWithSubText2(mTvSSID, mAimSsid, "断开中……", Color.RED);
            if (mTask != null)
                mTask.setWait(true);
        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.DISCONNECTED) {
            setTextWithSubText2(mTvSSID, mAimSsid, "已断开……", Color.RED);
            if (mTask != null)
                mTask.setWait(true);
        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.BLOCKED) {
            setTextWithSubText2(mTvSSID, mAimSsid, "此网络的访问被阻塞。", Color.RED);
        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.FAILED) {
            setTextWithSubText2(mTvSSID, mAimSsid, "尝试连接失败。", Color.RED);
        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.IDLE) {
            setTextWithSubText2(mTvSSID, mAimSsid, "准备开始数据连接设置。", Color.RED);
        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
            setTextWithSubText2(mTvSSID, mAimSsid, "链路连通性差。", Color.RED);
        }


        setWifiInfo();
    }


    @Override
    public void onWifiStateChange(WifiReceiver.EnumWifiStatus status) {

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
//        WifiConfiguration wifiConfig = getExsitsWifiConfiguration(mAimSsid);
//        if (wifiConfig != null) {
////            return new WifiAdmin(this).addNetwork(wifiConfig);
//            boolean result = mWifiManager.enableNetwork(wifiConfig.networkId, true);
//            result = result && mWifiManager.reconnect();
//            return true;
//        }
addTask(
        new ConnectToOtherSSid().execute(mActivity, mAimSsid, new TaskListener<CharSequence>() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public void onPreExecute(GenericTask task) {

            }

            @Override
            public void onPostExecute(GenericTask task, TaskResult result) {

            }

            @Override
            public void onProgressUpdate(GenericTask task, CharSequence param) {

            }

            @Override
            public void onCancelled(GenericTask task) {

            }
        })
);
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                onClickPlayOrPause(view);
                break;
            case R.id.btn_choose_wifi:
                view.setTag(System.currentTimeMillis());
                if (mWifiManager.getConfiguredNetworks().isEmpty())
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                else {
                    List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
                    final String[] items = new String[mWifiManager.getConfiguredNetworks().size()];
                    for (int i = 0; i < items.length; i++) {
                        items[i] = WifiUtils.clearSSID(list.get(i).SSID);
                    }
                    new AlertDialog.Builder(mActivity).setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAimSsid = items[which];
                            mTvSSID.setText(mAimSsid);
                            if (mWifiManager.getConnectionInfo() == null
                                    || mWifiManager.getConnectionInfo().getNetworkId() == -1
                                    || !mAimSsid.equals(WifiUtils.getWifiSSID(mActivity, mWifiManager))) {
                                connect2aimWifi();
                            }
                        }
                    }).create().show();
                }
                break;
        }
    }

    private void onClickPlayOrPause(View view) {
        if (mAimSsid == null) {
            showToast("Please choose wifi first!");
            return;
        }
        if (view.getTag() != null && (Boolean) view.getTag()) {
            view.setTag(null);
            ((ImageButton) view).setImageResource(android.R.drawable.ic_media_play);

            if (mTask != null) {
                mTask.interrupt();
            }
        } else {
            ((ImageButton) view).setImageResource(android.R.drawable.ic_media_pause);
            view.setTag(true);

            mTask = new TouchScreemTask(getResources().getDisplayMetrics().widthPixels - 100
                    , getResources().getDisplayMetrics().heightPixels - 100
                    , 1000);
            mTask.start();

            mWifiManager.disconnect();
            mTask.setWait(false);

            connect2aimWifi();
        }
    }

    private void setWifiInfo() {
        WifiInfo info = mWifiManager.getConnectionInfo();
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        StringBuilder sb = new StringBuilder();

        sb.append("connect status").append('\t').append(':').append('\t');
        switch (mWifiManager.getWifiState()) {
            case WifiManager.WIFI_STATE_DISABLING:
                sb.append("WIFI_STATE_DISABLING");
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                sb.append("WIFI_STATE_DISABLED");
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                sb.append("WIFI_STATE_ENABLED");
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                sb.append("WIFI_STATE_ENABLING");
                break;

        }
        sb.append('\n');
        sb.append('\n');

        if (info == null || info.getLinkSpeed() == -1) {
            sb.append("ip").append('\t').append(':').append('\n');
            sb.append("BSSID").append('\t').append(':').append('\n');
            sb.append("HiddenSSID").append('\t').append(':').append('\n');
            sb.append("linkSpeed").append('\t').append(':').append('\n');
            sb.append("Rssi").append('\t').append(':').append('\n');
        } else {
            sb.append("ip").append('\t').append(':').append('\t').append(WifiUtils.turnInteger2Ip(info.getIpAddress())).append('\n');
            sb.append("BSSID").append('\t').append(':').append('\t').append(info.getBSSID()).append('\n');
            sb.append("HiddenSSID").append('\t').append(':').append('\t').append(info.getHiddenSSID() ? "YES" : "NO").append('\n');
            sb.append("linkSpeed").append('\t').append(':').append('\t').append(info.getLinkSpeed()).append("Mbps").append('\n');
            sb.append("Rssi").append('\t').append(':').append('\t').append(info.getRssi()).append("dBm").append('\n');
        }
        sb.append('\n');
        if (dhcpInfo == null || dhcpInfo.gateway == 0) {
            sb.append("IP").append('\t').append(':').append('\n');
            sb.append("gateway").append('\t').append(':').append('\n');
            sb.append("mask").append('\t').append(':').append('\n');
            sb.append("nds1").append('\t').append(':').append('\n');
            sb.append("nds2").append('\t').append(':').append('\n');
            sb.append("serverAddress").append('\t').append(':').append('\n');
            sb.append("leaseDuration").append('\t').append(':').append('\n');
        } else {
            sb.append("IP").append('\t').append(':').append('\t').append(WifiUtils.turnInteger2Ip(dhcpInfo.ipAddress)).append('\n');
            sb.append("gateway").append('\t').append(':').append('\t').append(WifiUtils.turnInteger2Ip(dhcpInfo.gateway)).append('\n');
            sb.append("mask").append('\t').append(':').append('\t').append(WifiUtils.turnInteger2Ip(dhcpInfo.netmask)).append('\n');
            sb.append("nds1").append('\t').append(':').append('\t').append(WifiUtils.turnInteger2Ip(dhcpInfo.dns1)).append('\n');
            sb.append("nds2").append('\t').append(':').append('\t').append(WifiUtils.turnInteger2Ip(dhcpInfo.dns2)).append('\n');
            sb.append("serverAddress").append('\t').append(':').append('\t').append(WifiUtils.turnInteger2Ip(dhcpInfo.serverAddress)).append('\n');
            sb.append("leaseDuration").append('\t').append(':').append('\t').append(dhcpInfo.leaseDuration).append('\n');
        }

        mTvWifiInfo.setText(sb.toString());

    }

    private void setTextWithSubText2(TextView textView, CharSequence str1, CharSequence str2, int color2) {
        SpannableString ss = new SpannableString(str2);
        ss.setSpan(new ForegroundColorSpan(color2), 0, str2.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ss.setSpan(new RelativeSizeSpan(0.6f), 0, str2.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        textView.setText(str1);
        textView.append("\t");
        textView.append(ss);
    }

    /**
     * @return 是否播放状态
     */
    private boolean isPlayBtnRuningState() {
        return mBtnPlay.getTag() != null && (Boolean) mBtnPlay.getTag();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mWifiManager.disconnect();
                    mTask.setWait(false);
                    sendEmptyMessageDelayed(1, 300);
                    break;
                case 1:
                    connect2aimWifi();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onPause() {
        Log.d(getClass().getSimpleName(), "====~ onPause");
        super.onPause();
    }

    private class TouchScreemTask extends Thread {

        private boolean isRunning;
        private boolean isWait;

        private int x, y;
        private long interval = 1000;
        private String mCmd;

        public TouchScreemTask(int x, int y, long interval) {
            this.interval = interval;
            this.x = x;
            this.y = y;
            mCmd = "input tap " + x + " " + y;//new String[]{"input", "tap", String.valueOf(x), String.valueOf(y)};
        }

        @Override
        public void run() {
            isRunning = true;
            while (isRunning) {

                if (!isWait)
                    try {
                        Log.d(getClass().getSimpleName(), mCmd);
//                        ProcessBuilder process = new ProcessBuilder(mCmd);
//                        process.start();
                        Runtime.getRuntime().exec(mCmd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void setWait(boolean is) {
//            isWait = is;
        }

        @Override
        public void interrupt() {
            isRunning = false;
            super.interrupt();
        }
    }
}
