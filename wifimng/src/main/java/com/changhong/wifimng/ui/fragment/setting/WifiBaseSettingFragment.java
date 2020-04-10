package com.changhong.wifimng.ui.fragment.setting;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.plc.EnumWifiAutoMode;
import com.changhong.wifimng.been.plc.WifiBaseInfo;
import com.changhong.wifimng.been.plc.WlanInfo;
import com.changhong.wifimng.been.wifi.RequireAllBeen;
import com.changhong.wifimng.been.wifi.ResponseAllBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.router.GetWifiSettingTask;
import com.changhong.wifimng.task.router.SetWifiSettingTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.fragment.EnumPage;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class WifiBaseSettingFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Switch mSwitchDoubleFrequencey;
    private View mPanel5G;
    private View mTvNotice2Frequency;

    private TextView mTvTitle;
    private EditText mEtWifiSSiD;
    private EditText mEtWifiPassword;
    private CheckBox mCheckboxHide;
    private TextView mTvEncrypt;

    private EditText mEtWifiSSiD5G;
    private EditText mEtWifiPassword5G;
    private CheckBox mCheckboxHide5G;
    private TextView mTvEncrypt5G;
    private TextView mTvTxPower;
    private TextView mTvTxPower5G;

    private String[] mArrEncryption;
    private String[] mArrTxPower;

    /**
     * 加载的wifi信息
     */
    private HashMap<String, ResponseAllBeen> mWifiInfo;

    private String mCurrentDeviceType;
    private DeviceType mDeviceType;

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

        mArrEncryption = getResources().getStringArray(R.array.encryption);
        mArrTxPower = getResources().getStringArray(R.array.tx_power);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wifi_base_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.btn_save).setOnClickListener(this);

        view.findViewById(R.id.panel_double_frequency).setOnClickListener(this);
        mSwitchDoubleFrequencey = view.findViewById(R.id.switch_double_frequency);
        mSwitchDoubleFrequencey.setOnCheckedChangeListener(this);

        mPanel5G = view.findViewById(R.id.panel_5g);
        mTvNotice2Frequency = view.findViewById(R.id.tv_notice_double_frequency);

        // double frequncy on 1 & 2.4G

        mTvTitle = view.findViewById(R.id.textView_title1);
        mEtWifiSSiD = view.findViewById(R.id.et_wifi_ssid);
        mEtWifiPassword = view.findViewById(R.id.et_wifi_password);
        mCheckboxHide = view.findViewById(R.id.checkbox_hide_psw);
        mCheckboxHide.setOnCheckedChangeListener(this);
        view.findViewById(R.id.panel_encrypt).setOnClickListener(this);
        mTvEncrypt = view.findViewById(R.id.textView_encrypt);
        view.findViewById(R.id.panel_tx_power).setOnClickListener(this);
        mTvTxPower = view.findViewById(R.id.textView_tx_power);
        view.findViewById(R.id.textView_advance_setting).setOnClickListener(this);

        //5G
        mEtWifiSSiD5G = view.findViewById(R.id.et_wifi_ssid_5G);
        mEtWifiPassword5G = view.findViewById(R.id.et_wifi_password_5G);
        mCheckboxHide5G = view.findViewById(R.id.checkbox_hide_psw_5G);
        mCheckboxHide5G.setOnCheckedChangeListener(this);
        view.findViewById(R.id.panel_encrypt_5G).setOnClickListener(this);
        mTvEncrypt5G = view.findViewById(R.id.textView_encrypt_5G);
        view.findViewById(R.id.panel_tx_power_5G).setOnClickListener(this);
        mTvTxPower5G = view.findViewById(R.id.textView_tx_power_5G);
        view.findViewById(R.id.textView_advance_setting_5G).setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);

        doLoadInfo();
    }

    private void doLoadInfo() {
        doGetWifiSetting();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(null);
        } else if (v.getId() == R.id.btn_save) {
            if (checkCommitInfo())
                askEffectImmediately();
        }

        //main
        else if (v.getId() == R.id.panel_double_frequency) {
            mSwitchDoubleFrequencey.toggle();
        }

        //double frequncy on 1  & 2.4G
        else if (v.getId() == R.id.textView_advance_setting) {
            gotoAdvanceSetting();
        } else if (v.getId() == R.id.panel_encrypt) {
            showDialogChoose(mArrEncryption, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTvEncrypt.setText(mArrEncryption[which]);
                    mTvEncrypt.setTag(which);
                    setPasswordShow(which != 0, false);

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
        }

        //5G
        else if (v.getId() == R.id.textView_advance_setting_5G) {
            gotoAdvanceSetting();
        } else if (v.getId() == R.id.panel_encrypt_5G) {
            showDialogChoose(mArrEncryption, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTvEncrypt5G.setText(mArrEncryption[which]);
                    mTvEncrypt5G.setTag(which);
                    setPasswordShow(which != 0, true);
                }
            });
        } else if (v.getId() == R.id.panel_tx_power_5G) {
            showDialogChoose(mArrTxPower, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTvTxPower5G.setText(mArrTxPower[which]);
                    mTvTxPower5G.setTag(which);
                }
            });
        }
    }

    private void setPasswordShow(boolean isShown, boolean is5G) {
        View view;
        if (is5G)
            view = ((ViewGroup) mEtWifiPassword5G.getParent());
        else
            view = ((ViewGroup) mEtWifiPassword.getParent());
        boolean isShowing = view.getVisibility() == View.VISIBLE;
        if (isShowing == isShown)
            return;

        if (!isShown)
            view.setVisibility(View.GONE);
        else if (isShown)
            view.setVisibility(View.VISIBLE);
    }

    private void refreshView(HashMap<String, ResponseAllBeen> param) {
        boolean isDoubleFrequncey = !param.containsKey(KeyConfig.KEY_5G);
        mSwitchDoubleFrequencey.setChecked(isDoubleFrequncey);
        {
            ResponseAllBeen been = param.get(KeyConfig.KEY_24G);
            mEtWifiSSiD.setText(been.getSsid());
            mEtWifiPassword.setText(been.getKey());
            mTvEncrypt.setText(been.getEncryption());
            setPasswordShow(Arrays.binarySearch(mArrEncryption, been.getEncryption()) != 0, false);
            mTvTxPower.setText(mArrTxPower[been.getTxpower_mode()]);
            mTvTxPower.setTag(been.getTxpower_mode());
        }
        {
            ResponseAllBeen been = param.get(isDoubleFrequncey ? KeyConfig.KEY_24G : KeyConfig.KEY_5G);
            mEtWifiSSiD5G.setText(been.getSsid());
            mEtWifiPassword5G.setText(been.getKey());
            mTvEncrypt5G.setText(been.getEncryption());
            setPasswordShow(Arrays.binarySearch(mArrEncryption, been.getEncryption()) != 0, true);
            mTvTxPower5G.setText(mArrTxPower[been.getTxpower_mode()]);
            mTvTxPower5G.setTag(been.getTxpower_mode());
        }
    }

    private void refreshView(WlanInfo param) {
        mSwitchDoubleFrequencey.setChecked(param.getEnable() == 1);
        {//2.4G
            WifiBaseInfo been = param.getWifiBase2G();
            mEtWifiSSiD.setText(been.getName());
            mEtWifiPassword.setText(been.getKey());
            EnumWifiAutoMode mode = EnumWifiAutoMode.getModeByCode(been.getAuth_mode());
            mTvEncrypt.setText(mode.getName());
            int index = Arrays.binarySearch(EnumWifiAutoMode.values(), mode);
            mTvEncrypt.setTag(index);
            setPasswordShow(index != 0, false);
        }
        {//5G
            WifiBaseInfo been = param.getWifiBase5G();
            mEtWifiSSiD5G.setText(been.getName());
            mEtWifiPassword5G.setText(been.getKey());
            EnumWifiAutoMode mode = EnumWifiAutoMode.getModeByCode(been.getAuth_mode());
            mTvEncrypt5G.setText(mode.getName());
            int index = Arrays.binarySearch(EnumWifiAutoMode.values(), mode);
            mTvEncrypt5G.setTag(index);
            setPasswordShow(index != 0, true);
        }
    }

    private boolean checkCommitInfo() {
        boolean isDoubleFrequency = mSwitchDoubleFrequencey.isChecked();
        if (mEtWifiSSiD.getText().length() == 0) {
            mEtWifiSSiD.requestFocus();
            showToast(R.string.notice_not_be_empty);
            return false;
        }

        if (!mTvEncrypt.getText().equals("none") && mEtWifiPassword.getText().length() < 8) {
            mEtWifiPassword.requestFocus();
            showToast(R.string.password_wifi_rule);
            return false;
        }

        if (!isDoubleFrequency) {
            if (mEtWifiSSiD5G.getText().length() == 0) {
                mEtWifiSSiD5G.requestFocus();
                showToast(R.string.notice_not_be_empty);
                return false;
            }

            if (!mTvEncrypt5G.getText().equals("none") && mEtWifiPassword5G.getText().length() < 8) {
                mEtWifiPassword5G.requestFocus();
                showToast(R.string.password_wifi_rule);
                return false;
            }
        }

        return true;
    }

    /**
     * 询问是否立即执行
     */
    private void askEffectImmediately() {
        showAlert(_getString(R.string.notice_ask_execute_immediately), _getString(R.string.save_and_execute), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doSetWifiSetting(true);
            }
        }, _getString(R.string.save_only), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doSetWifiSetting(false);
            }
        }, true);
    }

    private void gotoAdvanceSetting() {
        if (onFragmentLifeListener != null)
            onFragmentLifeListener.onChanged(EnumPage.WIFI_SETTING_ADVANCE);
    }

    /**
     * @param isAction 是否立即执行
     * @return
     */
    private HashMap<String, RequireAllBeen> createRequireAllBeen(boolean isAction) {
        HashMap<String, RequireAllBeen> info = new HashMap<>();
        boolean isDoubleFrequency = mSwitchDoubleFrequencey.isChecked();
        if (isDoubleFrequency) {
            RequireAllBeen been = new RequireAllBeen();
            been.setEnabled(1);
            been.setFlag(0);
            been.setPrefer_5g(1);
            been.setSsid(mEtWifiSSiD.getText().toString());
            been.setEncryption(mTvEncrypt.getText().toString());
            been.setKey(mEtWifiPassword.getText().toString());
            been.setTxpower_mode((Integer) mTvTxPower.getTag());
            been.setSave_action(isAction ? 1 : 0);
            info.put(KeyConfig.KEY_24G, been);
        } else {
            RequireAllBeen been = new RequireAllBeen();
            been.setEnabled(1);
            been.setFlag(1);
            been.setPrefer_5g(0);
            been.setSsid(mEtWifiSSiD.getText().toString());
            been.setEncryption(mTvEncrypt.getText().toString());
            been.setKey(mEtWifiPassword.getText().toString());
            been.setTxpower_mode((Integer) mTvTxPower.getTag());
            been.setSave_action(0);
            info.put(KeyConfig.KEY_24G, been);

            RequireAllBeen been5G = new RequireAllBeen();
            been5G.setEnabled(1);
            been5G.setFlag(2);
            been5G.setPrefer_5g(0);
            been5G.setSsid(mEtWifiSSiD5G.getText().toString());
            been5G.setEncryption(mTvEncrypt5G.getText().toString());
            been5G.setKey(mEtWifiPassword5G.getText().toString());
            been5G.setTxpower_mode((Integer) mTvTxPower5G.getTag());
            been5G.setSave_action(isAction ? 1 : 0);
            info.put(KeyConfig.KEY_5G, been5G);
        }

        return info;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.switch_double_frequency) {
            mTvTitle.setText(isChecked ? _getString(R.string.base_setting) : "2.4GHz WiFi");
            mTvNotice2Frequency.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            mPanel5G.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        }

        // double frequncy on 1  & 2.4G
        else if (buttonView.getId() == R.id.checkbox_hide_psw) {
            isShowPassword(mEtWifiPassword, !isChecked);
        }

        // 5G
        else if (buttonView.getId() == R.id.checkbox_hide_psw_5G) {
            isShowPassword(mEtWifiPassword5G, !isChecked);
        }
    }

    /**
     * 读取wifi设置信息
     */
    private void doGetWifiSetting() {
        addTask(
                new GetWifiSettingTask().execute(getGateway(), getCookie(), new TaskListener<HashMap<String, ResponseAllBeen>>() {
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
                    public void onProgressUpdate(GenericTask task, HashMap<String, ResponseAllBeen> param) {
                        if (param != null && !param.isEmpty()) {
                            mWifiInfo = param;
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

    /**
     * @param isAction 是否立即执行
     */
    private void doSetWifiSetting(boolean isAction) {
        addTask(
                new SetWifiSettingTask().execute(getGateway(), createRequireAllBeen(isAction), getCookie(), new TaskListener() {
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
                            showTaskError(task, R.string.commit_failed);
                        } else
                            onFragmentLifeListener.onChanged(null);
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

}
