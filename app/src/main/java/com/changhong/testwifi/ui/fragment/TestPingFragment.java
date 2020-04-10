package com.changhong.testwifi.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.changhong.testwifi.R;
import com.changhong.testwifi.receiver.WifiReceiver;
import com.changhong.testwifi.task.BaseBeen;
import com.changhong.testwifi.task.OnCallback;
import com.changhong.testwifi.task.PingResult;
import com.changhong.testwifi.task.PingTask;
import com.changhong.testwifi.ui.view.DefaultIntegerWatcher;
import com.changhong.testwifi.utils.WifiUtils;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TestPingFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener, WifiReceiver.WifiReceiverListener, View.OnClickListener {

    private TextView mTvResult;
    private TextView mTvGateWay;
    private TextView mTvSSID;
    private EditText mEtUrl;
    private RadioButton mRbPing;
    private RadioButton mRbPingGateway;
    private RadioButton[] mArrayRadioButton;
    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver;
    private FloatingActionButton mBtnPlay;
    private View mBtnChooseWifi;
    private EditText mEtPingTimes;
    private PingTask mPingTask;
    private ScrollView mScrollView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWifiManager = (WifiManager) mActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiReceiver = new WifiReceiver(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ping1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mTvResult = (TextView) view.findViewById(R.id.tv_result);
        mTvGateWay = (TextView) view.findViewById(R.id.tv_gateway);
        mTvSSID = (TextView) view.findViewById(R.id.tv_ssid);
        mEtUrl = (EditText) view.findViewById(R.id.et_url);
        mEtUrl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                EditText et = (EditText) v;
                if (hasFocus) {
                    if (et.getText().length() > 0)
                        et.setSelection(0, et.getText().length());
                    return;
                }
                if (et.getText().toString().trim().length() == 0) {
                    et.setText("www.baidu.com");
                    return;
                }
            }
        });

        mRbPing = (RadioButton) view.findViewById(R.id.cb_ping);
        mRbPing.setOnCheckedChangeListener(this);
        mRbPingGateway = (RadioButton) view.findViewById(R.id.cb_ping_gateway);

        mBtnChooseWifi = view.findViewById(R.id.btn_choose_wifi);
        mBtnChooseWifi.setOnClickListener(this);

        mBtnPlay = view.findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(this);

        mEtPingTimes = (EditText) view.findViewById(R.id.et_ping_times);
        mEtPingTimes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText et = (EditText) v;
                if (hasFocus) {
                    if (et.getText().length() > 0)
                        et.setSelection(0, et.getText().length());
                    return;
                }
                if (et.getText().toString().trim().length() == 0) {
                    et.setText("100");
                    return;
                }
                try {
                    int value = Integer.parseInt(et.getText().toString());
                    if (value < 1)
                        value = 1;
                    et.setText(String.valueOf(value));
                } catch (Exception e) {
                    et.setText("100");
                    return;
                }
            }
        });
        mEtPingTimes.addTextChangedListener(new DefaultIntegerWatcher());

        mScrollView = view.findViewById(R.id.scrollView01);

        mArrayRadioButton = new RadioButton[]{mRbPingGateway, mRbPing};

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        mTvSSID.setText(WifiUtils.getWifiSSID(mActivity, mWifiManager));
        if (mWifiManager.getDhcpInfo().gateway == 0)
            mTvGateWay.setText(null);
        else
            mTvGateWay.setText(getGateway());
        mWifiReceiver.registReceiver(mActivity);

    }

    @Override
    public void onPause() {
        mWifiReceiver.unregistReceiver(mActivity);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mPingTask != null)
            mPingTask.cancel(true);

        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        for (RadioButton radioButton : mArrayRadioButton) {
            if (radioButton == buttonView)
                radioButton.setOnCheckedChangeListener(null);
            else {
                radioButton.setChecked(false);
                radioButton.setOnCheckedChangeListener(this);
            }
        }
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
        if (networkInfo.isConnectedOrConnecting()) {
            if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED || mWifiManager.getDhcpInfo().gateway != 0) {
                mTvGateWay.setText(getGateway());
            } else
                mTvGateWay.setText(null);

        } else
            mTvGateWay.setText(null);

        mTvSSID.setText(WifiUtils.clearSSID(networkInfo.getExtraInfo()));
    }

    @Override
    public void onWifiStateChange(WifiReceiver.EnumWifiStatus status) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                onClickPlayOrPause(v);
                break;
            case R.id.btn_choose_wifi:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;
        }
    }

    private void setEditEnable(boolean is) {
        mRbPingGateway.setEnabled(is);
        mRbPing.setEnabled(is);

        mEtUrl.setEnabled(is);
        mEtPingTimes.setEnabled(is);

        mBtnPlay.setImageResource(is ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause);

        mBtnPlay.setTag(is ? null : true);

        for (RadioButton radioButton : mArrayRadioButton) {
            if (radioButton.getTag() != null) {
                radioButton.setTextColor((ColorStateList) radioButton.getTag());
                radioButton.setTag(null);
            }
            if (!is && radioButton.isChecked()) {
                ColorStateList colors = radioButton.getTextColors();
                radioButton.setTextColor(Color.CYAN);
                radioButton.setTag(colors);
            }
        }


    }

    private void onClickPlayOrPause(View v) {

        if (v.getTag() != null && (Boolean) v.getTag()) {
            if (mPingTask != null)
                mPingTask.cancel(true);
            mTvResult.append("\n" + getCurrentTime() + " ---->  Ping task canceled\n");

            mScrollView.pageScroll(View.FOCUS_DOWN);

            setEditEnable(true);
        } else {
            TextView view;
            String url;
            int times = Integer.parseInt(mEtPingTimes.getText().toString());
            if (times < 1) {
                times = 1;
                mEtPingTimes.setText(String.valueOf(times));
            }
            if (mTvGateWay.getText().length() == 0) {
                showToast("Wifi is not connected.");
                return;
            }
            if (mRbPing.isChecked()) {
                url = mEtUrl.getText().toString();
                view = mRbPing;
            } else {
                view = mRbPingGateway;
                url = mTvGateWay.getText().toString();
            }
            startPingTask(url, times, view);

            setEditEnable(false);
        }

    }

    private void startPingTask(final String url, int times, final TextView textView) {
        mTvResult.setText(getCurrentTime() + " ---->  Ping " + url + " start");
        mPingTask = new PingTask().execute(url, times, new OnCallback<PingResult, BaseBeen<String, PingResult>>() {
            @Override
            public void onStep(BaseBeen<String, PingResult> result) {
                SpannableString ss = new SpannableString("\n" + result.getT1());
                ss.setSpan(new ForegroundColorSpan(Color.BLUE), 1, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                ss.setSpan(new RelativeSizeSpan(0.6f), 1, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                mTvResult.append(ss);

//                mScrollView.arrowScroll(View.FOCUS_DOWN);
                mScrollView.fullScroll(View.FOCUS_DOWN);
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
                SpannableString ss = new SpannableString(sb.toString());
                ss.setSpan(new ForegroundColorSpan(Color.GREEN), 1, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                ss.setSpan(new RelativeSizeSpan(0.6f), 1, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                mTvResult.append(ss);

                mTvResult.append("\n" + getCurrentTime() + " ---->  Ping " + url + " end\n");
                mScrollView.fullScroll(View.FOCUS_DOWN);

                //TODO TASK END
                setEditEnable(true);
            }
        });
    }
}
