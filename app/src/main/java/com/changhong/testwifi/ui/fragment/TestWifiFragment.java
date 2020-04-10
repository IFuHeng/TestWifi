package com.changhong.testwifi.ui.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
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
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.changhong.testwifi.R;
import com.changhong.testwifi.receiver.WifiReceiver;
import com.changhong.testwifi.task.BaseBeen;
import com.changhong.testwifi.task.OnCallback;
import com.changhong.testwifi.task.PingResult;
import com.changhong.testwifi.task.PingTask;
import com.changhong.testwifi.utils.CommUtil;
import com.changhong.testwifi.utils.WifiUtils;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestWifiFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, WifiReceiver.WifiReceiverListener {

    private static final int EVENT_NEXT_WIFI_SSID = 2;
    private static final int EVENT_SHOW_RESULT = 3;

    /***限制最大重连次数*/
    private static final int LIMIT_RECONNECT_TIMES = 10;

    private TextView mTvResult;
    private ListView mListViewSSIDs;
    private CheckBox mCbAll;
    private CheckBox mCbPing;
    private CheckBox mCbPingGateway;
    private CheckBox mCbTestSpeed;
    private EditText mEtUrl;
    private FloatingActionButton mBtnStart;
    private View mBtnChooseSSID;

    private List<WifiConfiguration> mListTestDevice;

    private SSIDListAdapter mAdpater;

    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver;


    private PingTask mPingTask;

    private Boolean[] mItemState;

    private NetworkInfo.DetailedState mCurrentNetworkState;
    private BaseBeen<String, NetworkInfo.DetailedState> mLastConnectInfo;

    private int mReconnectTimes = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiManager = (WifiManager) mActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiReceiver = new WifiReceiver(this);

        mListTestDevice = new ArrayList<>();
        mAdpater = new SSIDListAdapter(mActivity, mListTestDevice);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test_wifi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mListViewSSIDs = (ListView) view.findViewById(R.id.list_ssids);
        mListViewSSIDs.setEmptyView(view.findViewById(R.id.tv_no_content));
        mListViewSSIDs.setAdapter(mAdpater);

        mTvResult = (TextView) view.findViewById(R.id.tv_result);
        mEtUrl = (EditText) view.findViewById(R.id.et_url);
        mEtUrl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText et = (EditText) v;
                if (et.getText().toString().trim().length() == 0) {
                    et.setText("www.baidu.com");
                    return;
                }
            }
        });

        mCbAll = (CheckBox) view.findViewById(R.id.cb_select_all);
        mCbAll.setOnCheckedChangeListener(this);

        mCbPing = (CheckBox) view.findViewById(R.id.cb_ping);
        mCbPingGateway = (CheckBox) view.findViewById(R.id.cb_ping_gateway);
        mCbTestSpeed = (CheckBox) view.findViewById(R.id.cb_test_speed);

        mBtnStart = (FloatingActionButton) view.findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(this);

        mBtnChooseSSID = view.findViewById(R.id.btn_choose_ssid);
        mBtnChooseSSID.setOnClickListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_choose_ssid:
                showWifiSeletcted();
                break;
            case R.id.btn_start:

                onClickPlayOrPause(v);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_select_all:
                mCbPing.setOnCheckedChangeListener(null);
                mCbPing.setChecked(isChecked);
                mCbPing.setOnCheckedChangeListener(this);
                mCbPingGateway.setOnCheckedChangeListener(null);
                mCbPingGateway.setChecked(isChecked);
                mCbPingGateway.setOnCheckedChangeListener(this);
                break;
            case R.id.cb_ping_gateway:
            case R.id.cb_ping:
                resetCheckboxAll(mCbPing.isChecked(), mCbPingGateway.isChecked());
                break;
            case R.id.cb_test_speed:
                break;
        }
    }

    private void resetCheckboxAll(boolean... iss) {
        for (boolean is : iss)
            if (!is) {
                mCbAll.setOnCheckedChangeListener(null);
                mCbAll.setChecked(false);
                mCbAll.setOnCheckedChangeListener(this);
                return;
            }
        mCbAll.setOnCheckedChangeListener(null);
        mCbAll.setChecked(true);
        mCbAll.setOnCheckedChangeListener(this);
    }

    private void setEditEnable(boolean is) {
        mCbPingGateway.setEnabled(is);
        mBtnChooseSSID.setEnabled(is);
        mCbAll.setEnabled(is);
        mCbPing.setEnabled(is);

        mEtUrl.setEnabled(is);

        mBtnStart.setImageResource(is ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause);
    }

    private void onClickPlayOrPause(View view) {

        if (view.getTag() != null && (Boolean) view.getTag()) {
            doTestEnd();
        } else if (doTestPrepare()) {
            handler.sendEmptyMessageDelayed(EVENT_NEXT_WIFI_SSID, 1000);
        }

    }

    private void startConnectWifi() {
        int index = mAdpater.getRunningIndex();
        mAdpater.notifyDataSetChanged();
        boolean result = mWifiManager.enableNetwork(mListTestDevice.get(index).networkId, true);
        if (result) {
            result = result && mWifiManager.reconnect();
        }

        if (!result) {
            mAdpater.setItemState(mAdpater.getRunningIndex(), false);
            mAdpater.notifyDataSetChanged();
            handler.sendMessageDelayed(handler.obtainMessage(EVENT_NEXT_WIFI_SSID, mAdpater.getRunningIndex(), 0), 200);
        }
    }

    private void showWifiSeletcted() {
        final List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();


        if (list == null || list.isEmpty()) {// TODO if list is empty,goto system set
            showToast("No wifi configure is exist.Please operate in SystemSettings");
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            return;
        }

        CharSequence[] items = new CharSequence[list.size()];
        for (int i = 0; i < list.size(); i++) {
            WifiConfiguration conf = list.get(i);
            items[i] = WifiUtils.clearSSID(conf.SSID);
        }
        final boolean[] choices = new boolean[items.length];
        for (int i = 0; i < list.size(); i++) {
            WifiConfiguration cf = list.get(i);
            for (WifiConfiguration wifiConfiguration : mListTestDevice) {
                if (wifiConfiguration.SSID.equals(cf.SSID)) {
                    choices[i] = true;
                    break;
                }
            }
        }

        DialogInterface.OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                choices[which] = isChecked;
                Log.d(getClass().getSimpleName(), "====~ onClick :  choices = " + Arrays.toString(choices));
            }
        };

        AlertDialog alertDialog = new AlertDialog.Builder(mActivity)
                .setMultiChoiceItems(items, choices, listener)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(getClass().getSimpleName(), "====~ onClick : choices = " + Arrays.toString(choices));
                        mListTestDevice.clear();
                        for (int i = 0; i < choices.length; i++) {
                            boolean choice = choices[i];
                            if (choice)
                                mListTestDevice.add(list.get(i));
                        }
                        Log.d(getClass().getSimpleName(), "====~ onClick : mListTestDevice = " + mListTestDevice);
                        mItemState = new Boolean[mListTestDevice.size()];
                        mAdpater.notifyDataSetChanged();
                        CommUtil.expandListView(mAdpater, mListViewSSIDs);

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
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

        int index = mAdpater.getRunningIndex();
        if (index == -1) {
            return;
        }

        String ssid = WifiUtils.clearSSID(networkInfo.getExtraInfo());//WifiUtils.getWifiSSID(mActivity, mWifiManager);
        String aimSSid = WifiUtils.clearSSID(mListTestDevice.get(index).SSID);
        Log.i(getClass().getSimpleName(), "====~onConnectNetInfoChanged :" + ssid + " <-> " + aimSSid);
        mCurrentNetworkState = networkInfo.getDetailedState();

        BaseBeen tempConnectInfo = new BaseBeen(ssid, mCurrentNetworkState);
        if (tempConnectInfo.equals(mLastConnectInfo)) {
            mLastConnectInfo = tempConnectInfo;
            return;
        }
        mLastConnectInfo = tempConnectInfo;
//        if (networkInfo.getDetailedState() == mCurrentNetworkState)
//            return;

        if (networkInfo.isConnectedOrConnecting()) {
            if (ssid.equals(aimSSid)) {
                if (mCurrentNetworkState == NetworkInfo.DetailedState.CONNECTED) {
                    appendTextWithSubText(mTvResult, ssid, "已连接", Color.BLACK);
                    mAdpater.setItemState(index, true);

                    if (isPlayBtnRuningState()) {
                        if (mCbPingGateway.isChecked()) {
                            startPingTask(getGateway(), mCbPingGateway);
                        } else if (mCbPing.isChecked()) {
                            startPingTask(mEtUrl.getText().toString(), mCbPing);
                        } else {
                            handler.sendEmptyMessage(EVENT_NEXT_WIFI_SSID);
                        }
                    }

                } else if (mCurrentNetworkState == NetworkInfo.DetailedState.CONNECTING) {
                    if (mReconnectTimes == 0) {
                        appendTextWithSubText(mTvResult, ssid, "连接中……", Color.DKGRAY);
                        ++mReconnectTimes;
                    } else if (mReconnectTimes < LIMIT_RECONNECT_TIMES) {
                        appendTextWithSubText(mTvResult, ssid, "连接中(第" + ++mReconnectTimes + "次)……", Color.DKGRAY);
                    } else {
                        appendTextWithSubText(mTvResult, ssid, "连续" + LIMIT_RECONNECT_TIMES + "次无法连上" + ssid + "，默认失败，连接下一个。", Color.DKGRAY);
                        mAdpater.setItemState(index, false);
                        handler.sendEmptyMessage(EVENT_NEXT_WIFI_SSID);
                    }

                } else if (mCurrentNetworkState == NetworkInfo.DetailedState.AUTHENTICATING) {
                    appendTextWithSubText(mTvResult, ssid, "验证中……", Color.DKGRAY);
                } else if (mCurrentNetworkState == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    appendTextWithSubText(mTvResult, ssid, "获取IP中……", Color.DKGRAY);
                } else if (mCurrentNetworkState == NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
                    appendTextWithSubText(mTvResult, ssid, "检查网络是否是受控门户", Color.DKGRAY);
                }
            } else startConnectWifi();
            return;
        }

        if (!aimSSid.equals(ssid)) {
            return;
        }

        if (mCurrentNetworkState == NetworkInfo.DetailedState.SCANNING) {
            appendTextWithSubText(mTvResult, ssid, "扫描中……", Color.BLUE);
        } else if (mCurrentNetworkState == NetworkInfo.DetailedState.DISCONNECTING) {
            appendTextWithSubText(mTvResult, ssid, "断开中……", Color.RED);
        } else {
            if (mCurrentNetworkState == NetworkInfo.DetailedState.SUSPENDED) {
                appendTextWithSubText(mTvResult, ssid, "IP流量暂停", Color.RED);
            } else if (mCurrentNetworkState == NetworkInfo.DetailedState.DISCONNECTED) {
                appendTextWithSubText(mTvResult, ssid, "已断开", Color.RED);
            } else if (mCurrentNetworkState == NetworkInfo.DetailedState.BLOCKED) {
                appendTextWithSubText(mTvResult, ssid, "此网络的访问被阻塞。", Color.RED);
            } else if (mCurrentNetworkState == NetworkInfo.DetailedState.FAILED) {
                appendTextWithSubText(mTvResult, ssid, "尝试连接失败。", Color.RED);
            } else if (mCurrentNetworkState == NetworkInfo.DetailedState.IDLE) {
                appendTextWithSubText(mTvResult, ssid, "准备开始数据连接设置。", Color.RED);
            } else if (mCurrentNetworkState == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                appendTextWithSubText(mTvResult, ssid, "链路连通性差。", Color.RED);
            }
            mAdpater.setItemState(index, false);
            handler.sendEmptyMessage(EVENT_NEXT_WIFI_SSID);
        }

    }

    @Override
    public void onWifiStateChange(WifiReceiver.EnumWifiStatus status) {

    }


    private class SSIDListAdapter extends BaseAdapter {
        private Context mContext;
        private List<WifiConfiguration> mData;

        private int mRunningIndex = -1;

        public SSIDListAdapter(Context context, List<WifiConfiguration> data) {
            this.mContext = context;
            this.mData = data;
        }

        @Override
        public int getCount() {
            if (mData == null)
                return 0;

            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            if (mData == null)
                return null;
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null)
                view = LayoutInflater.from(mContext).inflate(R.layout.item_ssid, null, false);

            ProgressBar progressBar = view.findViewById(android.R.id.progress);
            TextView text1 = view.findViewById(android.R.id.text1);
            ImageView icon = view.findViewById(android.R.id.icon);
            if (mItemState != null && position < mItemState.length) {
                if (mItemState[position] == null) {
                    icon.setVisibility(View.GONE);
                } else if (mItemState[position]) {
                    icon.setVisibility(View.VISIBLE);
                    icon.setImageResource(R.drawable.ic_check_white_24dp);
                } else {
                    icon.setVisibility(View.VISIBLE);
                    icon.setImageResource(R.drawable.ic_close_black_24dp);
                }
            } else {
                icon.setVisibility(View.GONE);
            }

            text1.setText(WifiUtils.clearSSID(mData.get(position).SSID));

            if (position == mRunningIndex) {
                if (progressBar.getVisibility() != View.VISIBLE)
                    progressBar.setVisibility(View.VISIBLE);
            } else {
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.INVISIBLE);
            }

            return view;
        }

        public void setRunningIndex(int index) {
            mRunningIndex = index;
        }

        public int getRunningIndex() {
            return mRunningIndex;
        }

        /**
         * clear all item state
         */
        public void clearState() {
            for (int i = 0; i < mItemState.length; i++) {
                mItemState[i] = null;
            }
        }

        /**
         * @param position 位置
         * @param state    状态
         */
        public void setItemState(int position, boolean state) {
            if (mItemState != null && mItemState.length > position)
                mItemState[position] = state;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_NEXT_WIFI_SSID:
                    mAdpater.setRunningIndex(mAdpater.getRunningIndex() + 1);
                    mReconnectTimes = 0;
                    if (mAdpater.getRunningIndex() < mAdpater.getCount()) {
                        appendTextWithSubText(mTvResult, getCurrentTime() + " ---->  connect to ", mListTestDevice.get(mAdpater.getRunningIndex()).SSID, 0xffff00ff);
                        startConnectWifi();
                    } else {
                        doTestEnd();
                    }
                    break;
                case EVENT_SHOW_RESULT:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void startPingTask(final String url, final TextView textView) {
        ColorStateList colors = textView.getTextColors();
        textView.setTextColor(Color.CYAN);
        textView.setTag(colors);
        mTvResult.append("\n" + getCurrentTime() + " ---->  Ping " + url + " start");
        mPingTask = new PingTask().execute(url, 100, new OnCallback<PingResult, BaseBeen<String, PingResult>>() {
            @Override
            public void onStep(BaseBeen<String, PingResult> result) {

            }

            @Override
            public void onCallback(PingResult result) {

                StringBuilder sb = new StringBuilder();
//                sb.append('\n').append("pingresult = ").append(result).append('\n');
                sb.append("\n").append(getCurrentTime()).append(" ---->  Ping ").append(url).append(" end").append('\n');
                if (result.getErrorCode() != 0) {
                    sb.append(getCurrentTime()).append(" ---->  Ping ").
                            append(result.getIp()).append(" is failed. Error message is ").
                            append(result.getErrorMsg()).append('\n').append('\n');
                } else {
                    sb.append(getCurrentTime()).append(" ---->  Ping ").append(url).append(" result is:").append('\n');
                    sb.append("    ").append("Host name :").append(result.getHostname()).append('\n');
                    sb.append("    ").append("IP :").append(result.getIp()).append('\n');
                    sb.append("    ").append("SEND NUM :").append(result.getSendNum()).append('\n');
                    sb.append("    ").append("RECEIVE NUM :").append(result.getReceiveNum()).append('\n');
                    sb.append("    ").append("LOST RATE :").append(result.getLostRate()).append('\n');
                    sb.append("    ").append("LOST NUM :").append(result.getLostCount()).append('\n');
                    sb.append("    ").append("MAX TIME :").append(result.getMaxTime()).append('\n');
                    sb.append("    ").append("MIN TIME :").append(result.getMinTime()).append('\n');
                    sb.append("    ").append("AVERAGE TIME :").append(result.getAverageTime()).append('\n').append('\n');
                }
                appendTextWithSubText(mTvResult, "", sb.toString(), 0xff4455ff);

                if (textView.getTag() != null) {
                    textView.setTextColor((ColorStateList) textView.getTag());
                    textView.setTag(null);
                }

//                mTvResult.append(sb.toString());
                //TODO END TO NEXT
                switch (textView.getId()) {
                    case R.id.cb_ping_gateway:
                        if (mCbPing.isChecked())
                            startPingTask(mEtUrl.getText().toString(), mCbPing);
                        else
                            handler.sendEmptyMessage(EVENT_NEXT_WIFI_SSID);
                        break;
                    case R.id.cb_ping:
                    default:
                        handler.sendEmptyMessage(EVENT_NEXT_WIFI_SSID);
                        break;
                }

            }
        });
    }

    private void appendTextWithSubText(TextView textView, CharSequence str1, CharSequence str2, int color2) {
        SpannableString ss = new SpannableString("\n" + str1 + '\t' + str2);
        int start = ss.length() - str2.length();
        ss.setSpan(new ForegroundColorSpan(color2), start, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ss.setSpan(new RelativeSizeSpan(0.6f), start, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        textView.append(ss);
    }

    /**
     * @return 是否播放状态
     */
    private boolean isPlayBtnRuningState() {
        return mBtnStart.getTag() != null && (Boolean) mBtnStart.getTag();
    }

    private boolean doTestPrepare() {
        if (mListTestDevice.isEmpty()) {
            showToast("Please choose wifi ssid first!");
            showWifiSeletcted();
            return false;
        }

        if (mCbPingGateway.isChecked() && mEtUrl.getText().toString().trim().length() == 0) {
            showToast("Url cannot be empty,Please retry.");
            mEtUrl.requestFocus();
            return false;
        }
        mReconnectTimes = 0;
        mTvResult.setText("");
        appendTextWithSubText(mTvResult, getCurrentTime(), " ---->  test start.", Color.BLUE);
        mBtnStart.setTag(true);
        mAdpater.clearState();
        mWifiManager.disconnect();
        mWifiReceiver.registReceiver(mActivity);
        setEditEnable(false);

        return true;
    }

    /**
     * when test end ,enable button and other,stop wifi receiver.
     */
    private void doTestEnd() {
        handler.removeMessages(EVENT_NEXT_WIFI_SSID);
        if (mPingTask != null) {
            mPingTask.cancel(true);
        }
        mReconnectTimes = 0;
        appendTextWithSubText(mTvResult, getCurrentTime(), " ---->  test complete connected.", Color.BLUE);
        setEditEnable(true);
        mWifiReceiver.unregistReceiver(mActivity);
        mBtnStart.setTag(null);
        mAdpater.setRunningIndex(-1);
        mAdpater.notifyDataSetChanged();
    }

}
