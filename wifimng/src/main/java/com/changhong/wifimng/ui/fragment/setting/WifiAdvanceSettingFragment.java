package com.changhong.wifimng.ui.fragment.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.EnumMeshBandWidth;
import com.changhong.wifimng.been.EnumMeshBandWidth5G;
import com.changhong.wifimng.been.plc.EnumBandWidth;
import com.changhong.wifimng.been.plc.EnumBandWidth5G;
import com.changhong.wifimng.been.plc.WifiAdvanceInfo;
import com.changhong.wifimng.been.plc.WlanInfo;
import com.changhong.wifimng.been.wifi.RequireAllBeen;
import com.changhong.wifimng.been.wifi.ResponseAllBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.GetPLCWifiAdvanceInfoTask;
import com.changhong.wifimng.task.plc.SetPLCWifiAdvanceInfoTask;
import com.changhong.wifimng.task.router.GetWifiAdvanceSettingTask;
import com.changhong.wifimng.task.router.SetWifiAdvanceSettingTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.BaseFragment;

import java.util.Arrays;

public class WifiAdvanceSettingFragment extends BaseFragment implements View.OnClickListener {

    private Switch mSwitch_auto;
    private Switch mSwitch_auto5g;
    private TextView mTvChannel;
    private TextView mTvChannel5G;
    private TextView mTvBandwidth;
    private TextView mTvBandwidth5G;

    private String[] choices_channel;
    private String[] choices_channel_5G;

    private String[] choices_bandwidth;
    private String[] choices_bandwidth_5G;

    private ResponseAllBeen mResponseAllBeen;
    private WlanInfo mWlanInfo;

    private Switch mSwitchHideSSID;
    private Switch mSwitchHideSSID5G;

    private String mCurrentDeviceType;
    private DeviceType mDeviceType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mCurrentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
        }
        if (mCurrentDeviceType == null)
            mCurrentDeviceType = Preferences.getInstance(mActivity).getString(KeyConfig.KEY_DEVICE_TYPE);
        mDeviceType = DeviceType.getDeviceTypeFromName(mCurrentDeviceType);

        choices_channel_5G = new String[]{
                getString(R.string.auto),
                "36", "40", "44", "48", "52", "56", "60", "149", "153", "157", "161"
        };

        choices_channel = new String[14];
        choices_channel[0] = getString(R.string.auto);
        for (int i = 1; i < choices_channel.length; i++) {
            choices_channel[i] = String.valueOf(i);
        }

        //init BandWidth Choices
        if (DeviceType.PLC == mDeviceType) {
            choices_bandwidth = new String[EnumBandWidth.values().length];
            for (int i = 0; i < choices_bandwidth.length; i++) {
                choices_bandwidth[i] = EnumBandWidth.values()[i].getName();
            }
            choices_bandwidth_5G = new String[EnumBandWidth5G.values().length];
            for (int i = 0; i < choices_bandwidth_5G.length; i++) {
                choices_bandwidth_5G[i] = EnumBandWidth5G.values()[i].getName();
            }
        } else {
            choices_bandwidth = new String[EnumMeshBandWidth.values().length];
            for (int i = 0; i < choices_bandwidth.length; i++) {
                choices_bandwidth[i] = EnumMeshBandWidth.values()[i].getName();
            }
            choices_bandwidth_5G = new String[EnumMeshBandWidth5G.values().length];
            for (int i = 0; i < choices_bandwidth_5G.length; i++) {
                choices_bandwidth_5G[i] = EnumMeshBandWidth5G.values()[i].getName();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wifi_advance_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.btn_save).setOnClickListener(this);

        mSwitch_auto = view.findViewById(R.id.switch_auto);
        mSwitch_auto5g = view.findViewById(R.id.switch_auto_5g);

        view.findViewById(R.id.panel_auto).setOnClickListener(this);
        view.findViewById(R.id.panel_auto_5g).setOnClickListener(this);
        view.findViewById(R.id.panel_channel).setOnClickListener(this);
        view.findViewById(R.id.panel_channel_5g).setOnClickListener(this);
        view.findViewById(R.id.panel_bandwidth).setOnClickListener(this);
        view.findViewById(R.id.panel_bandwidth_5g).setOnClickListener(this);

        view.findViewById(R.id.panel_hide_ssid).setOnClickListener(this);
        mSwitchHideSSID = view.findViewById(R.id.switch_hide_ssid);
        view.findViewById(R.id.panel_hide_ssid_5G).setOnClickListener(this);
        mSwitchHideSSID5G = view.findViewById(R.id.switch_hide_ssid_5G);

        mTvChannel = view.findViewById(R.id.tv_channel);
        mTvChannel5G = view.findViewById(R.id.tv_channel_5g);

        mTvBandwidth = view.findViewById(R.id.tv_bandwidth);
        mTvBandwidth5G = view.findViewById(R.id.tv_bandwidth_5g);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDeviceType == DeviceType.PLC) {
            doGetPLCWifiAdvanceInfo();
        } else
            doGetAdvanceInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null) {
                onFragmentLifeListener.onChanged(null);
            }
        } else if (v.getId() == R.id.btn_save) {
            if (mDeviceType == DeviceType.PLC) {
                doSavePLCWifiAdvanceInfo();
            } else
                doSetAdvanceInfo();
        } else if (v.getId() == R.id.panel_auto) {
            mSwitch_auto.toggle();
        } else if (v.getId() == R.id.panel_auto_5g) {
            mSwitch_auto5g.toggle();
        } else if (v.getId() == R.id.panel_channel) {
            showDialogChannelChoice24g();
        } else if (v.getId() == R.id.panel_channel_5g) {
            showDialogChannelChoice5g();
        } else if (v.getId() == R.id.panel_bandwidth) {
            showDialogBandWidth();
        } else if (v.getId() == R.id.panel_bandwidth_5g) {
            showDialogBandWidth5G();
        } else if (v.getId() == R.id.panel_hide_ssid) {
            mSwitchHideSSID.toggle();
        } else if (v.getId() == R.id.panel_hide_ssid_5G) {
            mSwitchHideSSID5G.toggle();
        }

    }

    private void refreshView(WlanInfo param) {
        {
            WifiAdvanceInfo been = param.getWifiAdvanceInfo2G();

            if (been.getChannel_2() == 0)
                mTvChannel.setText(choices_channel[0]);
            else
                mTvChannel.setText(String.valueOf(been.getChannel_2()));
            mTvChannel.setTag(been.getChannel_2());
            EnumBandWidth bandwidth = EnumBandWidth.findBandWidthByPlcCode(been.getBandwidth_2());
            mTvBandwidth.setText(bandwidth.getName());
            mTvBandwidth.setTag(Arrays.binarySearch(EnumBandWidth.values(), bandwidth));
            mSwitchHideSSID.setChecked(param.getWifiBase2G().getSsid_hide() == 1);
        }
        {
            WifiAdvanceInfo been = param.getWifiAdvanceInfo5G();

            if (been.getChannel_2() == 0)
                mTvChannel5G.setText(choices_channel_5G[0]);
            else
                mTvChannel5G.setText(String.valueOf(been.getChannel_5()));
            mTvChannel5G.setTag(been.getChannel_5());
            EnumBandWidth5G bandwidth5 = EnumBandWidth5G.findBandWidthByPlcCode(been.getBandwidth_5());
            mTvBandwidth5G.setText(bandwidth5.getName());
            mTvBandwidth5G.setTag(Arrays.binarySearch(EnumBandWidth5G.values(), bandwidth5));
            mSwitchHideSSID5G.setChecked(param.getWifiBase5G().getSsid_hide() == 1);
        }
    }

    private void refreshView(ResponseAllBeen param) {
        mTvChannel.setTag(param.getChannel_2());
        if (param.getChannel_2() == 0)
            mTvChannel.setText(choices_channel[0]);
        else
            mTvChannel.setText(String.valueOf(param.getChannel_2()));
        EnumMeshBandWidth bandwidth = EnumMeshBandWidth.findBandWidthByPlcCode(param.getChannel_width_2());
        mTvBandwidth.setText(bandwidth.getName());
        mTvBandwidth.setTag(Arrays.binarySearch(EnumMeshBandWidth.values(), bandwidth));
        mSwitchHideSSID.setChecked(param.getHidden_2() == 1);

        mTvChannel5G.setTag(param.getChannel_5());
        if (param.getChannel_5() == 0)
            mTvChannel5G.setText(choices_channel_5G[0]);
        else
            mTvChannel5G.setText(String.valueOf(param.getChannel_5()));
        EnumMeshBandWidth5G bandwidth5 = EnumMeshBandWidth5G.findBandWidthByPlcCode(param.getChannel_width_5());
        mTvBandwidth5G.setText(bandwidth5.getName());
        mTvBandwidth5G.setTag(Arrays.binarySearch(EnumMeshBandWidth5G.values(), bandwidth5));
        mSwitchHideSSID5G.setChecked(param.getHidden_5() == 1);
    }

    private void showDialogBandWidth5G() {
        showDialogChoose(choices_bandwidth_5G, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTvBandwidth5G.setText(choices_bandwidth_5G[which]);
                mTvBandwidth5G.setTag(which);
            }
        });
    }

    private void showDialogBandWidth() {
        showDialogChoose(choices_bandwidth, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTvBandwidth.setText(choices_bandwidth[which]);
                mTvBandwidth.setTag(which);
            }
        });
    }

    private void showDialogChannelChoice5g() {

        showDialogChoose(choices_channel_5G, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTvChannel5G.setText(choices_channel_5G[which]);
                if (which == 0)
                    mTvChannel5G.setTag(which);
                else
                    mTvChannel5G.setTag(Integer.parseInt(choices_channel_5G[which]));
            }
        });
    }

    /**
     * show 2.4G CHANNEL choose dialig
     */
    private void showDialogChannelChoice24g() {

        showDialogChoose(choices_channel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTvChannel.setText(choices_channel[which]);
                mTvChannel.setTag(which);
            }
        });
    }

    private void doGetAdvanceInfo() {
        addTask(new GetWifiAdvanceSettingTask().execute(getGateway(), getCookie(), new TaskListener<ResponseAllBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.downloading), true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                task.cancel(true);
                                onFragmentLifeListener.onChanged(null);
                            }
                        });
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, ResponseAllBeen param) {
                        mResponseAllBeen = param;
                        refreshView(param);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doGetPLCWifiAdvanceInfo() {
        addTask(new GetPLCWifiAdvanceInfoTask().execute(getGateway(), new TaskListener<WlanInfo>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.downloading), true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                task.cancel(true);
                                onFragmentLifeListener.onChanged(null);
                            }
                        });
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, WlanInfo param) {
                        mWlanInfo = param;
                        refreshView(param);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doSavePLCWifiAdvanceInfo() {
        insetEdit2PlcInfo();
        addTask(new SetPLCWifiAdvanceInfoTask().execute(getGateway(), mWlanInfo, new TaskListener() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if (task != null)
                                    task.cancel(true);
                            }
                        });
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        } else {
                            showToast(R.string.commit_completed);
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, Object param) {

                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doSetAdvanceInfo() {
        addTask(new SetWifiAdvanceSettingTask().execute(getGateway(), createAdvanceInfo(), getCookie(), new TaskListener() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if (task != null)
                                    task.cancel(true);
                            }
                        });
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        } else {
                            showToast(R.string.commit_completed);
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, Object param) {

                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void insetEdit2PlcInfo() {
        WifiAdvanceInfo beenAd = mWlanInfo.getWifiAdvanceInfo2G();
        //2.4G
        beenAd.setChannel_2((Integer) mTvChannel.getTag());
        beenAd.setBandwidth_2(EnumBandWidth.values()[(int) mTvBandwidth.getTag()].getCode());
        mWlanInfo.getWifiBase2G().setSsid_hide(mSwitchHideSSID.isChecked() ? 1 : 0);
        //5G
        WifiAdvanceInfo beenAd5G = mWlanInfo.getWifiAdvanceInfo5G();
        beenAd5G.setChannel_5((Integer) mTvChannel5G.getTag());
        beenAd5G.setBandwidth_5(EnumBandWidth5G.values()[(int) mTvBandwidth5G.getTag()].getCode());
        mWlanInfo.getWifiBase5G().setSsid_hide(mSwitchHideSSID5G.isChecked() ? 1 : 0);
    }

    private RequireAllBeen createAdvanceInfo() {
        RequireAllBeen beenAd = new RequireAllBeen();
        //2.4G
        beenAd.setChannel_2((Integer) mTvChannel.getTag());
        beenAd.setChannel_width_2(EnumMeshBandWidth.values()[(int) mTvBandwidth.getTag()].getCode());
        beenAd.setShortgi_2(mResponseAllBeen.getShortgi_2());
        beenAd.setBand_2(mResponseAllBeen.getBand_2());
        beenAd.setHidden_2(mSwitchHideSSID.isChecked() ? 1 : 0);
        beenAd.setWmm_2(mResponseAllBeen.getWmm_2());
        //5G
        beenAd.setChannel_5((Integer) mTvChannel5G.getTag());
        beenAd.setChannel_width_5(EnumMeshBandWidth5G.values()[(int) mTvBandwidth5G.getTag()].getCode());
        beenAd.setShortgi_5(mResponseAllBeen.getShortgi_5());
        beenAd.setBand_5(mResponseAllBeen.getBand_5());
        beenAd.setHidden_5(mSwitchHideSSID5G.isChecked() ? 1 : 0);
        beenAd.setWmm_5(mResponseAllBeen.getWmm_5());
        return beenAd;

    }
}
