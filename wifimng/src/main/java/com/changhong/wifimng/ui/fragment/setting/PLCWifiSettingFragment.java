package com.changhong.wifimng.ui.fragment.setting;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.EnumMeshBandWidth;
import com.changhong.wifimng.been.EnumMeshBandWidth5G;
import com.changhong.wifimng.been.plc.EnumAutoMode;
import com.changhong.wifimng.been.plc.EnumBandWidth;
import com.changhong.wifimng.been.plc.EnumBandWidth5G;
import com.changhong.wifimng.been.plc.EnumPowerTrans;
import com.changhong.wifimng.been.plc.EnumWifiAutoMode;
import com.changhong.wifimng.been.plc.WifiAdvanceInfo;
import com.changhong.wifimng.been.plc.WifiBaseInfo;
import com.changhong.wifimng.been.plc.WlanInfo;
import com.changhong.wifimng.been.wifi.ResponseAllBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.GetPLCWifiBaseInfoTask;
import com.changhong.wifimng.task.plc.SetPLCWifiBaseInfoTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.uttils.CommUtil;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class PLCWifiSettingFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private TextView mTvTitleBarTitle;
    private Switch mSwitchDoubleFrequencey;
    private View mTvNotice2Frequency;
    private TextView mBtnSwitch2G_5G;

    private TextView mTvTitle;
    private TextView mTvTitleAdvance;
    private EditText mEtWifiSSiD;
    private EditText mEtWifiPassword;
    private CheckBox mCheckboxHide;
    private TextView mTvAutoMode;
    private TextView mTvEncrypt;
    private Switch mSwitchHideSSID;

    private TextView mBtnSwitch2G_5G_advance;
    private Switch mSwitch_auto;
    private TextView mTvTxPower;
    private TextView mTvChannel;
    private TextView mTvBandwidth;

    private String[] mArrEncryption;
    private String[] mArrAutoMode;
    private String[] mArrTxPower;
    private String[] choices_channel;
    private String[] choices_channel_5G;

    private String[] choices_bandwidth;
    private String[] choices_bandwidth_5G;

    /**
     * 加载的wifi信息
     */
    private WlanInfo mPlcWlanInfo;

    private String mCurrentDeviceType;
    private DeviceType mDeviceType;
    private ViewSwitcher mViewSwitcher;

    private boolean isCurrent5G = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //获取当前设备类型
        if (getArguments() != null) {
            mCurrentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
        }
        if (mCurrentDeviceType == null)
            mCurrentDeviceType = Preferences.getInstance(mActivity).getString(KeyConfig.KEY_DEVICE_TYPE);
        mDeviceType = DeviceType.getDeviceTypeFromName(mCurrentDeviceType);

        if (mDeviceType == DeviceType.PLC) {
            mArrEncryption = new String[EnumWifiAutoMode.values().length];
            for (int i = 0; i < mArrEncryption.length; i++) {
                mArrEncryption[i] = EnumWifiAutoMode.values()[i].getName();
            }
            mArrTxPower = new String[EnumPowerTrans.values().length];
            for (int i = 0; i < mArrTxPower.length; i++) {
                mArrTxPower[i] = EnumPowerTrans.values()[i].getName();
            }

            mArrAutoMode = new String[EnumAutoMode.values().length];
            for (int i = 0; i < mArrAutoMode.length; i++) {
                mArrAutoMode[i] = EnumAutoMode.values()[i].getName();
            }
        } else {
            mArrEncryption = getResources().getStringArray(R.array.encryption);
            mArrTxPower = getResources().getStringArray(R.array.tx_power);
        }

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
        return inflater.inflate(R.layout.fragment_plc_wlan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mTvTitleBarTitle = view.findViewById(R.id.tv_title);
        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.btn_save).setOnClickListener(this);

        mViewSwitcher = view.findViewById(R.id.viewSwitcher01);

        // base settings
        view.findViewById(R.id.panel_double_frequency).setOnClickListener(this);
        mSwitchDoubleFrequencey = view.findViewById(R.id.switch_double_frequency);
        mSwitchDoubleFrequencey.setOnCheckedChangeListener(this);
        mBtnSwitch2G_5G = view.findViewById(R.id.btn_switch_2G_5G);
        mBtnSwitch2G_5G.setOnClickListener(this);
        mBtnSwitch2G_5G_advance = view.findViewById(R.id.btn_switch_2G_5G_advance);
        mBtnSwitch2G_5G_advance.setOnClickListener(this);


        mTvNotice2Frequency = view.findViewById(R.id.tv_notice_double_frequency);
        mTvTitle = view.findViewById(R.id.textView_title1);
        mEtWifiSSiD = view.findViewById(R.id.et_wifi_ssid);
        mEtWifiPassword = view.findViewById(R.id.et_wifi_password);
        mCheckboxHide = view.findViewById(R.id.checkbox_hide_psw);
        mCheckboxHide.setOnCheckedChangeListener(this);
        view.findViewById(R.id.panel_encrypt).setOnClickListener(this);
        mTvAutoMode = view.findViewById(R.id.textView_authentication_mode);
        view.findViewById(R.id.panel_authentication_mode).setOnClickListener(this);
        mTvEncrypt = view.findViewById(R.id.textView_encrypt);
        view.findViewById(R.id.panel_hide_ssid).setOnClickListener(this);
        mSwitchHideSSID = view.findViewById(R.id.switch_hide_ssid);
        view.findViewById(R.id.textView_advance_setting).setOnClickListener(this);

        //advane settings
        mTvTitleAdvance = view.findViewById(R.id.tv_advance_title);
        mTvTxPower = view.findViewById(R.id.textView_tx_power);
        mSwitch_auto = view.findViewById(R.id.switch_auto);
        mTvChannel = view.findViewById(R.id.tv_channel);
        mTvBandwidth = view.findViewById(R.id.tv_bandwidth);

        view.findViewById(R.id.panel_tx_power).setOnClickListener(this);
        view.findViewById(R.id.panel_auto).setOnClickListener(this);
        view.findViewById(R.id.panel_channel).setOnClickListener(this);
        view.findViewById(R.id.panel_bandwidth).setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);

        doGetPLCWifiInfo();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (mViewSwitcher.getDisplayedChild() == 0) {
                if (onFragmentLifeListener != null)
                    onFragmentLifeListener.onChanged(null);
            } else {
                saveAdvanceEditInfo();
                mViewSwitcher.showPrevious();
                mTvTitleBarTitle.setText(R.string.wifi_setting);
                if (mSwitchDoubleFrequencey.isChecked()) {
                    isCurrent5G = false;
                }
                doSwitch2Gor5G(isCurrent5G);
            }
        } else if (v.getId() == R.id.btn_save) {
            if (mViewSwitcher.getDisplayedChild() == 0) {//base
                if (mEtWifiSSiD.getText().length() == 0) {
                    mEtWifiSSiD.requestFocus();
                    showToast(R.string.notice_not_be_empty);
                    return;
                }

                if (((Integer) mTvEncrypt.getTag()) != 0 && mEtWifiPassword.getText().length() < 8) {
                    mEtWifiPassword.requestFocus();
                    showToast(R.string.password_wifi_rule);
                    return;
                }
                CommUtil.closeIME(mActivity);
                saveBaseEditInfo();
                WlanInfo info = new WlanInfo();
                if (isCurrent5G)
                    info.setWifiBase5G(mPlcWlanInfo.getWifiBase5G());
                else
                    info.setWifiBase2G(mPlcWlanInfo.getWifiBase2G());
                doSetPLCWifiSetting(info);
            }
            //advance
            else {
                saveAdvanceEditInfo();
                WlanInfo info = new WlanInfo();
                if (isCurrent5G)
                    info.setWifiAdvanceInfo5G(mPlcWlanInfo.getWifiAdvanceInfo5G());
                else
                    info.setWifiAdvanceInfo2G(mPlcWlanInfo.getWifiAdvanceInfo2G());
                doSetPLCWifiSetting(info);
            }
        }

        //main
        else if (v.getId() == R.id.panel_double_frequency) {
            mSwitchDoubleFrequencey.toggle();
        }

        //double frequncy on 1  & 2.4G
        else if (v.getId() == R.id.textView_advance_setting) {
            gotoAdvanceSetting();
        } else if (v.getId() == R.id.panel_hide_ssid) {
            mSwitchHideSSID.toggle();
        } else if (v.getId() == R.id.panel_authentication_mode) {
            //配置认证模式
            showDialogChoose(mArrAutoMode, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Integer oldTag = (Integer) mTvAutoMode.getTag();
                    Integer oldEncryptTag = (Integer) mTvEncrypt.getTag();

                    mTvAutoMode.setText(mArrAutoMode[which]);
                    mTvAutoMode.setTag(which);

                    if (which == 0) {//认证模式为open的时候，加密方式为none
                        mTvEncrypt.setText(mArrEncryption[which]);
                        mTvEncrypt.setTag(which);
                        ((View) mEtWifiPassword.getParent()).setVisibility(View.GONE);
                    } else if (oldTag == 0 && oldEncryptTag == 0) {//认证模式从open变为其他的时候，加密方式推荐最高加密方式
                        mTvEncrypt.setText(mArrEncryption[mArrEncryption.length - 1]);
                        mTvEncrypt.setTag(mArrEncryption.length - 1);
                        ((View) mEtWifiPassword.getParent()).setVisibility(View.VISIBLE);
                    }
                }
            });
        } else if (v.getId() == R.id.panel_encrypt) {
            showDialogChoose(mArrEncryption, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Integer oldTag = (Integer) mTvEncrypt.getTag();
                    Integer oldAutoModeTag = (Integer) mTvAutoMode.getTag();
                    mTvEncrypt.setText(mArrEncryption[which]);
                    mTvEncrypt.setTag(which);
                    if (which == 0) {//加密方式为none的时候，认证方式为open
                        mTvAutoMode.setText(mArrAutoMode[which]);
                        mTvAutoMode.setTag(which);
                        ((View) mEtWifiPassword.getParent()).setVisibility(View.GONE);
                    } else if (oldTag == 0 && oldAutoModeTag == 0) {//加密方式从none变为其他的时候，认证模式推荐最高模式
                        mTvAutoMode.setText(mArrAutoMode[mArrAutoMode.length - 1]);
                        mTvAutoMode.setTag(mArrAutoMode.length - 1);
                        ((View) mEtWifiPassword.getParent()).setVisibility(View.VISIBLE);
                    }
                }
            });
        } else if (v.getId() == R.id.panel_tx_power) {
            showDialogChoose(mArrTxPower, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTvTxPower.setText(mArrTxPower[which]);
                    mTvTxPower.setTag(which);
                }
            });
        } else if (v.getId() == R.id.btn_switch_2G_5G) {
            saveBaseEditInfo();
            doSwitch2Gor5G(isCurrent5G = !isCurrent5G);
        } else if (v.getId() == R.id.btn_switch_2G_5G_advance) {
            saveAdvanceEditInfo();
            doSwitch2Gor5GAdvance(isCurrent5G = !isCurrent5G);
        }
        //advance
        else if (v.getId() == R.id.panel_auto) {
            mSwitch_auto.toggle();
        } else if (v.getId() == R.id.panel_channel) {
            showDialogChannelChoice();
        } else if (v.getId() == R.id.panel_bandwidth) {
            showDialogBandWidth();
        }

    }

    private void showDialogBandWidth() {
        if (isCurrent5G)
            showDialogChoose(choices_bandwidth_5G, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTvBandwidth.setText(choices_bandwidth_5G[which]);
                    mTvBandwidth.setTag(which);
                }
            });
        else
            showDialogChoose(choices_bandwidth, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTvBandwidth.setText(choices_bandwidth[which]);
                    mTvBandwidth.setTag(which);
                }
            });
    }

    /**
     * show CHANNEL choose dialig
     */
    private void showDialogChannelChoice() {
        if (isCurrent5G)
            showDialogChoose(choices_channel_5G, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTvChannel.setText(choices_channel_5G[which]);
                    mTvChannel.setTag(which);
                }
            });
        else
            showDialogChoose(choices_channel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTvChannel.setText(choices_channel[which]);
                    mTvChannel.setTag(which);
                }
            });
    }

    /**
     * 切换2G 5G显示
     *
     * @param is5g 5G
     */
    private void doSwitch2Gor5G(boolean is5g) {
        mBtnSwitch2G_5G.setText(is5g ? R.string.switchTo2g : R.string.switchTo5g);
//        mTvTitle.setText(is5g ? R.string.title_5G : R.string.title_2_4G);
        mTvTitle.setText(mSwitchDoubleFrequencey.isChecked() ? R.string.base_setting : (is5g ? R.string.title_5G : R.string.title_2_4G));
        {//BASE
            WifiBaseInfo been = is5g ? mPlcWlanInfo.getWifiBase5G() : mPlcWlanInfo.getWifiBase2G();
            mEtWifiSSiD.setText(been.getName());
            mEtWifiPassword.setText(been.getKey());
            // 设置认证方式
            EnumWifiAutoMode mode = EnumWifiAutoMode.getModeByCode(been.getAuth_mode());
            mTvEncrypt.setText(mode.getName());
            int index = Arrays.binarySearch(EnumWifiAutoMode.values(), mode);
            mTvEncrypt.setTag(index);
            ((View) mEtWifiPassword.getParent()).setVisibility(index != 0 ? View.VISIBLE : View.GONE);
            // 设置加密方式
            EnumAutoMode autoMode = EnumAutoMode.getModeByCode(been.getAuth_mode());
            mTvAutoMode.setText(autoMode.getName());
            mTvAutoMode.setTag(Arrays.binarySearch(EnumAutoMode.values(), autoMode));
            mSwitchHideSSID.setChecked(been.getSsid_hide() == 1);
        }

        {//ADVANCE
            WifiAdvanceInfo been = is5g ? mPlcWlanInfo.getWifiAdvanceInfo5G() : mPlcWlanInfo.getWifiAdvanceInfo2G();
            EnumPowerTrans mode = EnumPowerTrans.findBandWidthByPlcCode(is5g ? been.getTrans_power_5() : been.getTrans_power_2());
            mTvTxPower.setText(mode.getName());
            mTvTxPower.setTag(Arrays.binarySearch(EnumPowerTrans.values(), mode));

            if (is5g) {
                if (been.getChannel_5() == 0)
                    mTvChannel.setText(choices_channel[0]);
                else
                    mTvChannel.setText(String.valueOf(been.getChannel_5()));
                mTvChannel.setTag(been.getChannel_5());
                EnumBandWidth5G bandwidth = EnumBandWidth5G.findBandWidthByPlcCode(been.getBandwidth_5());
                mTvBandwidth.setText(bandwidth.getName());
                mTvBandwidth.setTag(Arrays.binarySearch(EnumBandWidth5G.values(), bandwidth));

            } else {
                if (been.getChannel_2() == 0)
                    mTvChannel.setText(choices_channel[0]);
                else
                    mTvChannel.setText(String.valueOf(been.getChannel_2()));
                mTvChannel.setTag(been.getChannel_2());
                EnumBandWidth bandwidth = EnumBandWidth.findBandWidthByPlcCode(been.getBandwidth_2());
                mTvBandwidth.setText(bandwidth.getName());
                mTvBandwidth.setTag(Arrays.binarySearch(EnumBandWidth.values(), bandwidth));
            }

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mViewSwitcher.getDisplayedChild() == 1) {
                saveAdvanceEditInfo();
                mViewSwitcher.showPrevious();
                mTvTitleBarTitle.setText(R.string.wifi_setting);
                if (mSwitchDoubleFrequencey.isChecked()) {
                    isCurrent5G = false;
                }
                doSwitch2Gor5G(isCurrent5G);

                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void refreshView(WlanInfo param) {
        mSwitchDoubleFrequencey.setOnCheckedChangeListener(null);
        boolean isChecked = param.getEnable() == 1;
        mSwitchDoubleFrequencey.setChecked(isChecked);
        if (isChecked)
            isCurrent5G = false;

        mTvTitle.setText(isChecked ? R.string.base_setting : (isCurrent5G ? R.string.title_5G : R.string.title_2_4G));
        mTvNotice2Frequency.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        mBtnSwitch2G_5G.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        mSwitchDoubleFrequencey.setOnCheckedChangeListener(this);
        doSwitch2Gor5G(isCurrent5G);
        doSwitch2Gor5GAdvance(isCurrent5G);
    }

    private void doSwitch2Gor5GAdvance(boolean isCurrent5G) {
        if (isCurrent5G) {
            mTvTitleAdvance.setText(getString(R.string.title_5G) + getString(R.string.advance_setting));
            mBtnSwitch2G_5G_advance.setText(R.string.switchTo2g);

            WifiAdvanceInfo been = mPlcWlanInfo.getWifiAdvanceInfo5G();
            if (been.getChannel_5() == 0) {
                mTvChannel.setText(choices_channel_5G[0]);
                mTvChannel.setTag(been.getChannel_5());
            } else {
                String value = String.valueOf(been.getChannel_5());
                mTvChannel.setText(value);
                for (int i = 1; i < choices_channel_5G.length; i++) {
                    if (choices_channel_5G[i].equals(value)) {
                        mTvChannel.setTag(i);
                        break;
                    }
                }
            }
            EnumBandWidth5G bandwidth5 = EnumBandWidth5G.findBandWidthByPlcCode(been.getBandwidth_5());
            mTvBandwidth.setText(bandwidth5.getName());
            mTvBandwidth.setTag(Arrays.binarySearch(EnumBandWidth5G.values(), bandwidth5));
        } else {
            mTvTitleAdvance.setText(getString(R.string.title_2_4G) + getString(R.string.advance_setting));
            mBtnSwitch2G_5G_advance.setText(R.string.switchTo5g);

            WifiAdvanceInfo been = mPlcWlanInfo.getWifiAdvanceInfo2G();
            if (been.getChannel_2() == 0)
                mTvChannel.setText(choices_channel[0]);
            else
                mTvChannel.setText(String.valueOf(been.getChannel_2()));
            mTvChannel.setTag(been.getChannel_2());
            EnumBandWidth bandwidth = EnumBandWidth.findBandWidthByPlcCode(been.getBandwidth_2());
            mTvBandwidth.setText(bandwidth.getName());
            mTvBandwidth.setTag(Arrays.binarySearch(EnumBandWidth.values(), bandwidth));
        }
    }

    private void gotoAdvanceSetting() {

        doSwitch2Gor5GAdvance(isCurrent5G);
        mTvTitleBarTitle.setText(R.string.wifi_advance_setting);
        mViewSwitcher.showNext();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.switch_double_frequency) {
            if (isChecked)
                isCurrent5G = false;

            mPlcWlanInfo.setEnable(isChecked ? 1 : 0);
            doSwitch2Gor5G(!isChecked && isCurrent5G);
            mTvNotice2Frequency.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            mBtnSwitch2G_5G.setVisibility(isChecked ? View.GONE : View.VISIBLE);

            WlanInfo info = new WlanInfo();
            info.setEnable(isChecked ? 1 : 0);
            doSetPLCWifiSetting(info);
        }

        // double frequncy on 1  & 2.4G
        else if (buttonView.getId() == R.id.checkbox_hide_psw) {
            isShowPassword(mEtWifiPassword, !isChecked);
        }
    }

    private void doGetPLCWifiInfo() {
        addTask(
                new GetPLCWifiBaseInfoTask().execute(getGateway(), new TaskListener<WlanInfo>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(getString(R.string.downloading), true, new DialogInterface.OnCancelListener() {
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
                        if (param != null) {
                            mPlcWlanInfo = param;
                            refreshView(param);
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }


    private void doSetPLCWifiSetting(WlanInfo info) {
        addTask(
                new SetPLCWifiBaseInfoTask().execute(getGateway(), info, new TaskListener() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(getString(R.string.commiting), true, new DialogInterface.OnCancelListener() {
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
                        } else {
                            showToast(R.string.commit_completed);
                            doGetPLCWifiInfo();
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

    /**
     * 将数据缓存到内存中
     */
    private void saveBaseEditInfo() {
        if (isCurrent5G) {
            mPlcWlanInfo.getWifiBase5G().setName(mEtWifiSSiD.getText().toString());
            mPlcWlanInfo.getWifiBase5G().setKey(mEtWifiPassword.getText().toString());
            mPlcWlanInfo.getWifiBase5G().setEncryption(EnumWifiAutoMode.values()[(Integer) mTvEncrypt.getTag()].getCode());
            mPlcWlanInfo.getWifiBase5G().setAuth_mode(EnumAutoMode.values()[(Integer) mTvAutoMode.getTag()].getCode());
            mPlcWlanInfo.getWifiBase5G().setSsid_hide(mSwitchHideSSID.isChecked() ? 1 : 0);
        } else {
            mPlcWlanInfo.getWifiBase2G().setName(mEtWifiSSiD.getText().toString());
            mPlcWlanInfo.getWifiBase2G().setKey(mEtWifiPassword.getText().toString());
            mPlcWlanInfo.getWifiBase2G().setEncryption(EnumWifiAutoMode.values()[(Integer) mTvEncrypt.getTag()].getCode());
            mPlcWlanInfo.getWifiBase2G().setAuth_mode(EnumAutoMode.values()[(Integer) mTvAutoMode.getTag()].getCode());
            mPlcWlanInfo.getWifiBase2G().setSsid_hide(mSwitchHideSSID.isChecked() ? 1 : 0);
        }
    }

    private void saveAdvanceEditInfo() {
        if (isCurrent5G) {
            //5G
            WifiAdvanceInfo beenAd5G = mPlcWlanInfo.getWifiAdvanceInfo5G();
//            beenAd5G.setChannel_5((Integer) mTvChannel.getTag());
            int index = (int) mTvChannel.getTag();
            if (index == 0)
                beenAd5G.setChannel_5(index);
            else
                beenAd5G.setChannel_5(Integer.parseInt(choices_channel_5G[index]));

            beenAd5G.setBandwidth_5(EnumBandWidth5G.values()[(int) mTvBandwidth.getTag()].getCode());
            mPlcWlanInfo.getWifiBase5G().setSsid_hide(mSwitchHideSSID.isChecked() ? 1 : 0);
            beenAd5G.setTrans_power_5(EnumPowerTrans.values()[(int) mTvTxPower.getTag()].getCode());
        } else {
            //2.4G
            WifiAdvanceInfo beenAd = mPlcWlanInfo.getWifiAdvanceInfo2G();
            beenAd.setChannel_2((Integer) mTvChannel.getTag());
            beenAd.setBandwidth_2(EnumBandWidth.values()[(int) mTvBandwidth.getTag()].getCode());
            mPlcWlanInfo.getWifiBase2G().setSsid_hide(mSwitchHideSSID.isChecked() ? 1 : 0);
            beenAd.setTrans_power_2(EnumPowerTrans.values()[(int) mTvTxPower.getTag()].getCode());
        }
    }
}
