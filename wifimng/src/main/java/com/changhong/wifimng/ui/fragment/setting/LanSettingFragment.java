package com.changhong.wifimng.ui.fragment.setting;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.plc.LanV4Info;
import com.changhong.wifimng.been.wan.RequireAllBeen;
import com.changhong.wifimng.been.wan.ResponseAllBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.GetPLCLanTask;
import com.changhong.wifimng.task.plc.SetPLCLanTask;
import com.changhong.wifimng.task.router.GetLanInfoTask;
import com.changhong.wifimng.task.router.SetLanInfoTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.view.DefaultIPV4Watcher;
import com.changhong.wifimng.ui.view.DefaultIntegerWatcher;
import com.changhong.wifimng.uttils.CommUtil;
import com.changhong.wifimng.uttils.WifiUtils;

import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class LanSettingFragment extends BaseFragment<Boolean> implements View.OnClickListener {

    private DefaultIntegerWatcher mWatcher;

    private EditText mEtMask;
    private EditText mEtGateway;
    private EditText mEtMax;
    private EditText mEtMin;
    private EditText mEtRentTime;

    /**
     * PLC的租期设置
     */
    private Spinner mSpinnerRemainTime;

    private Switch mSwitchDhcpService;
    private TextView mTvGatewayPrifix;

    private String mGateway;
    private int mGatewaySuffix;

    private ResponseAllBeen mResponseAllBeen;
    private LanV4Info mLanV4Info;

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

        mWatcher = new DefaultIntegerWatcher();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lan_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mEtMask = view.findViewById(R.id.et_mask);
        mEtMask.addTextChangedListener(new DefaultIPV4Watcher());

        mEtGateway = view.findViewById(R.id.et_gateway);
        mSwitchDhcpService = view.findViewById(R.id.switch_hdcp_service);
        if (mDeviceType == DeviceType.BWR || mDeviceType == DeviceType.PLC)// 电力猫和组网不能关闭DHCP服务
        {
//            ((View) mSwitchDhcpService.getParent()).setVisibility(View.GONE);
            mSwitchDhcpService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (!b) {
                        compoundButton.setChecked(!b);
                        showToast(mDeviceType.getName() + getString(R.string.cannot_close_dhcp_server));
                    }
                }
            });
            ((View) mSwitchDhcpService.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showToast(mDeviceType.getName() + getString(R.string.cannot_close_dhcp_server));
                }
            });
        } else
            ((View) mSwitchDhcpService.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSwitchDhcpService.toggle();
                }
            });
        mTvGatewayPrifix = view.findViewById(R.id.tv_gateway_prifix);
        mEtMin = view.findViewById(R.id.et_min);
        mEtMax = view.findViewById(R.id.et_max);
        mEtRentTime = view.findViewById(R.id.et_rent_time);

        mEtGateway.addTextChangedListener(new DefaultIPV4Watcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                if (Pattern.matches(WifiUtils.PATTERN_IPV4, s.toString())) {
                    mGateway = s.toString();
                    mGatewaySuffix = getIpLastValue(mGateway);
                    mTvGatewayPrifix.setText(mGateway.substring(0, mGateway.lastIndexOf('.') + 1));
                } else {
                    mGateway = null;
                }
            }
        });
        mEtRentTime.addTextChangedListener(mWatcher);
        mEtMin.addTextChangedListener(mWatcher);
        mEtMax.addTextChangedListener(mWatcher);

        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.btn_save).setOnClickListener(this);

        mSpinnerRemainTime = view.findViewById(R.id.spinner_rent_time);

        if (mDeviceType == DeviceType.PLC) {
            ((View) mEtRentTime.getParent()).setVisibility(View.GONE);
            doGetPLCLanInfo();
        } else {
            ((View) mSpinnerRemainTime.getParent()).setVisibility(View.GONE);
            doGetLanInfo();
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setEditText(EditText et, String value) {
        if (value != null) {
            et.setText(value);
            et.setHint(value);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save) {
            CommUtil.closeIME(mActivity);
            doCommit();
        }

        //返回
        else if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(null);
        }
    }

    private void doCommit() {
        if (mDeviceType == DeviceType.PLC)
            doCommitPlc();
        else
            doCommitRouterOrMesh();

    }

    private void doCommitPlc() {
        if (!checkCommitRule())
            return;

        mLanV4Info.setMask(mEtMask.getText().toString());
        mLanV4Info.setIp_addr(mEtGateway.getText().toString());
        mLanV4Info.setDhcps_enable(mSwitchDhcpService.isChecked() ? 1 : 0);
        {
            String temp = mLanV4Info.getIp_addr();
            int index = temp.lastIndexOf('.') + 1;
            temp = temp.substring(0, index);
            mLanV4Info.setDhcp_start(temp + mEtMin.getText().toString());
            mLanV4Info.setDhcp_end(temp + mEtMax.getText().toString());
        }
        switch (mSpinnerRemainTime.getSelectedItemPosition()) {
            case 0:
                mLanV4Info.setLease(60);
                break;
            case 1:
                mLanV4Info.setLease(3600);
                break;
            case 2:
                mLanV4Info.setLease(24 * 3600);
                break;
            case 3:
                mLanV4Info.setLease(7 * 24 * 3600);
                break;
            default:
                mLanV4Info.setLease(0);
                break;
        }

        addTask(
                new SetPLCLanTask().execute(getGateway(), mLanV4Info, new TaskListener() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(getString(R.string.commiting), true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                task.cancel(true);
                            }
                        });
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result == TaskResult.OK) {
                            showToast(R.string.commit_completed);
                            onFragmentLifeListener.onChanged(null);
                        } else {
                            showTaskError(task, R.string.interaction_failed);
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

    private void doCommitRouterOrMesh() {
        if (!checkCommitRule())
            return;

        RequireAllBeen requireBeen = new RequireAllBeen();
        requireBeen.setMask(mEtMask.getText().toString());
        requireBeen.setDomain_name(mResponseAllBeen.getDomain_name());
        requireBeen.setGw_addr(mResponseAllBeen.getIpaddr());
        requireBeen.setDhcpd_enabled(mSwitchDhcpService.isChecked() ? 2 : 0);
        {
            String temp = mEtGateway.getText().toString();
            requireBeen.setIpaddr(temp);
            int index = temp.lastIndexOf('.') + 1;
            temp = temp.substring(0, index);
            requireBeen.setDhcp_start(temp + mEtMin.getText().toString());
            requireBeen.setDhcp_end(temp + mEtMax.getText().toString());
        }
        requireBeen.setLease(Integer.parseInt(mEtRentTime.getText().toString()));

        addTask(
                new SetLanInfoTask().execute(getGateway(), requireBeen, getCookie(), new TaskListener() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(getString(R.string.commiting), true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                task.cancel(true);
                            }
                        });
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result == TaskResult.OK) {
                            showToast(R.string.commit_completed);
                            onFragmentLifeListener.onChanged(null);
                        } else {
                            showTaskError(task, R.string.interaction_failed);
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

    private boolean checkCommitRule() {
        String mask = mEtMask.getText().toString();
        if (!Pattern.matches(WifiUtils.PATTERN_IPV4, mask)) {
            mEtMask.requestFocus();
            showToast(R.string.notice_not_match_ipv4);
            return false;
        }

//网关规则判断
        String gateway = mEtGateway.getText().toString();
        if (!Pattern.matches(WifiUtils.PATTERN_IPV4, gateway)) {
            mEtGateway.requestFocus();
            showToast(R.string.notice_not_match_ipv4);
            return false;
        } else if (WifiUtils.isBroadcastOrNetworkAddress(gateway)) {
            showToast(R.string.illegal_address);
            mEtGateway.requestFocus();
            return false;
        }

        if (mEtMin.getText().length() == 0) {
            mEtMin.requestFocus();
            showToast(R.string.notice_not_be_empty);
            return false;
        }

        if (mEtMax.getText().length() == 0) {
            mEtMax.requestFocus();
            showToast(R.string.notice_not_be_empty);
            return false;
        }

        int max = Integer.parseInt(mEtMax.getText().toString());
        int min = Integer.parseInt(mEtMin.getText().toString());
        if (max > 254) {
            mEtMax.requestFocus();
            showToast(R.string.notice_max_gt_254);
            return false;
        }
        if (min < 2) {
            mEtMin.requestFocus();
            showToast(R.string.notice_min_lt_2);
            return false;
        }
        if (min >= max) {
            mEtMin.requestFocus();
            showToast(R.string.notice_cannot_more_than_max);
            return false;
        }

        if (mEtRentTime.getText().length() == 0)
            mEtRentTime.setText(String.valueOf(1));
        else if (mEtRentTime.getText().length() > 5)
            mEtRentTime.setText(String.valueOf(10080));
        else {
            int num = Integer.parseInt(mEtRentTime.getText().toString());
            if (num < 1)
                num = 1;
            else if (num > 10080)
                num = 10080;
            mEtRentTime.setText(String.valueOf(num));
        }

        return true;
    }

    private void doGetLanInfo() {
        addTask(
                new GetLanInfoTask().execute(getGateway(), getCookie(), new TaskListener<ResponseAllBeen>() {
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
                        if (result == TaskResult.OK) {

                        } else {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, ResponseAllBeen param) {

                        mResponseAllBeen = param;
                        mEtMask.setText(param.getMask());
                        mEtGateway.setText(param.getIpaddr());
                        mTvGatewayPrifix.setText(param.getIpaddr().substring(0, param.getIpaddr().lastIndexOf('.') + 1));
                        mSwitchDhcpService.setChecked(param.getDhcpd_enabled() != 0);
                        mEtRentTime.setText(param.getLease() == null ? "0" : param.getLease().toString());

                        int min = getIpLastValue(param.getDhcp_start());
                        mEtMin.setText(String.valueOf(min));
                        int max = getIpLastValue(param.getDhcp_end());
                        mEtMax.setText(String.valueOf(max));
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doGetPLCLanInfo() {
        addTask(
                new GetPLCLanTask().execute(getGateway(), new TaskListener<LanV4Info>() {
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
                        if (result == TaskResult.OK) {

                        } else {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, LanV4Info param) {

                        mLanV4Info = param;
                        mEtMask.setText(param.getMask());
                        mEtGateway.setText(param.getIp_addr());
                        mTvGatewayPrifix.setText(param.getIp_addr().substring(0, param.getIp_addr().lastIndexOf('.') + 1));
                        mSwitchDhcpService.setChecked(param.getDhcps_enable() == 1);
                        if (param.getLease() == null) {
                            mSpinnerRemainTime.setSelection(-1);
                        } else if (param.getLease() == 7 * 24 * 3600) {
                            mSpinnerRemainTime.setSelection(3, true);
                        } else if (param.getLease() == 24 * 3600) {
                            mSpinnerRemainTime.setSelection(2, true);
                        } else if (param.getLease() == 3600) {
                            mSpinnerRemainTime.setSelection(1, true);
                        } else if (param.getLease() == 60) {
                            mSpinnerRemainTime.setSelection(0, true);
                        } else {
                            mSpinnerRemainTime.setSelection(-1);
                        }

                        int min = getIpLastValue(param.getDhcp_start());
                        mEtMin.setText(String.valueOf(min));
                        int max = getIpLastValue(param.getDhcp_end());
                        mEtMax.setText(String.valueOf(max));
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private int getIpLastValue(String ip) {
        if (!TextUtils.isEmpty(ip) && Pattern.matches(WifiUtils.PATTERN_IPV4, ip)) {
            String value = ip.substring(ip.lastIndexOf('.') + 1);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private String getLeaseString(int lease) {
        String result;
        if (lease < 60) {
            result = lease + getString(lease > 1 ? R.string.secounds : R.string.secound);
        } else if (lease < 3600) {
            lease /= 60;
            result = lease + getString(lease > 1 ? R.string.minutes : R.string.minute);
        } else if (lease < 24 * 3600) {
            lease /= 3600;
            result = lease + getString(lease > 1 ? R.string.hours : R.string.hour);
        } else if (lease < 7 * 24 * 3600) {
            lease /= 3600;
            lease /= 24;
            result = lease + getString(lease > 1 ? R.string.days : R.string.day);
        } else {
            lease /= 3600;
            lease /= 24;
            lease /= 7;
            result = lease + getString(lease > 1 ? R.string.days : R.string.day);
        }
        return result;
    }

}
