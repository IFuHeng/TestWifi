package com.changhong.wifimng.ui.fragment.setting;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.WanType;
import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.been.wan.RequireAllBeen;
import com.changhong.wifimng.been.wan.ResponseAllBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.router.CheckPPPoeStateTask;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.router.GetWlanTask;
import com.changhong.wifimng.task.router.SetWlanTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.GetPLCWanTask;
import com.changhong.wifimng.task.plc.SetPLCDeviceBaseInfoTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.fragment.EnumPage;
import com.changhong.wifimng.ui.view.DefaultIPV4Watcher;
import com.changhong.wifimng.uttils.CommUtil;
import com.changhong.wifimng.uttils.WifiUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class NetworkFragment extends BaseFragment<EnumPage> implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private TextView mSpinnerNetworyType;
    private View mPanelPPPoe;
    private View mPanelStatic;
    private EditText mEtIp;
    private EditText mEtMask;
    private EditText mEtGateway;
    private EditText mEtDns1;
    private EditText mEtDns2;
    private EditText mEtPppoeName;
    private EditText mEtPppoePassword;

    private CheckBox mCheckHide;

    private DefaultIPV4Watcher mIpv4Watcher;

    private RequireAllBeen mRequireBeen;
    private PLCInfo mPlcInfo;

    private DeviceType mDeviceType;

    /**
     * 当前已连接路由器类型
     */
    private String mCurrentDeviceType;

    private String[] mTypeChoices;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIpv4Watcher = new DefaultIPV4Watcher();

        //获取当前设备类型
        if (getArguments() != null) {
            mCurrentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
        }
        if (mCurrentDeviceType == null)
            mCurrentDeviceType = Preferences.getInstance(mActivity).getString(KeyConfig.KEY_DEVICE_TYPE);
        mDeviceType = DeviceType.getDeviceTypeFromName(mCurrentDeviceType);

        final String[] choices;
        if (mDeviceType == DeviceType.BWR)
            mTypeChoices = getResources().getStringArray(R.array.networkType_bwr);
        else
            mTypeChoices = getResources().getStringArray(R.array.networkType);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_network, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mSpinnerNetworyType = view.findViewById(R.id.tv_network_type);
        mSpinnerNetworyType.setOnClickListener(this);
        view.findViewById(R.id.panel_network_type).setOnClickListener(this);

        mPanelPPPoe = view.findViewById(R.id.panel_pppoe);
        mPanelStatic = view.findViewById(R.id.panel_static);

        /***static**/
        mEtIp = view.findViewById(R.id.et_ip);
        mEtMask = view.findViewById(R.id.et_mask);
        mEtGateway = view.findViewById(R.id.et_gateway);
        mEtDns1 = view.findViewById(R.id.et_dns1);
        mEtDns2 = view.findViewById(R.id.et_dns2);

        mEtIp.addTextChangedListener(mIpv4Watcher);
        mEtMask.addTextChangedListener(mIpv4Watcher);
        mEtGateway.addTextChangedListener(mIpv4Watcher);
        mEtDns1.addTextChangedListener(mIpv4Watcher);
        mEtDns2.addTextChangedListener(mIpv4Watcher);

        /***pppoe**/
        mEtPppoeName = view.findViewById(R.id.et_pppoe_name);
        mEtPppoePassword = view.findViewById(R.id.et_pppoe_password);
        mEtPppoePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mCheckHide.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });
        mCheckHide = view.findViewById(R.id.checkbox_hide_psw);
        mCheckHide.setOnCheckedChangeListener(this);

        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.btn_save).setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden())
            doLoadInfo();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden)
            doLoadInfo();
    }

    private void doLoadInfo() {
        if (mDeviceType == DeviceType.PLC)
            doGetPLCWanInfo();
        else
            doGetWanInfo();
    }

    private void setEditText(EditText et, String value) {
        if (value != null) {
            et.setText(value);
//            et.setHint(value);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save) {
            CommUtil.closeIME(mActivity);
            if (mDeviceType == DeviceType.PLC) {
                saveAndCheckPLC();
            } else {
                saveAndCheck();
            }
        }

        //返回
        else if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(null);
        } else if (v.getId() == R.id.tv_network_type || v.getId() == R.id.panel_network_type) {
            showPopWinChoose(mSpinnerNetworyType, mTypeChoices, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mSpinnerNetworyType.setText(mTypeChoices[which]);
                    changePage(which);
                }
            });
        }
    }

    private void saveAndCheckPLC() {
        String nettype = mSpinnerNetworyType.getText().toString();
        int index = Arrays.binarySearch(mTypeChoices, nettype);

        if (index == 2) {//static
            String ip = mEtIp.getText().toString();
            if (!Pattern.matches(WifiUtils.PATTERN_IPV4, ip)) {
                showToast(R.string.notice_error_format);
                mEtIp.requestFocus();
                return;
            } else if (WifiUtils.isBroadcastOrNetworkAddress(ip)) {
                showToast(R.string.illegal_address);
                mEtIp.requestFocus();
                return;
            }
            String mask = mEtMask.getText().toString();
            if (!Pattern.matches(WifiUtils.PATTERN_IPV4, mask)) {
                showToast(R.string.notice_error_format);
                mEtMask.requestFocus();
                return;
            }
            String gateway = mEtGateway.getText().toString();
            if (!Pattern.matches(WifiUtils.PATTERN_IPV4, gateway)) {
                showToast(R.string.notice_error_format);
                mEtGateway.requestFocus();
                return;
            } else if (WifiUtils.isBroadcastOrNetworkAddress(gateway)) {
                showToast(R.string.illegal_address);
                mEtGateway.requestFocus();
                return;
            }
            String dns1 = mEtDns1.getText().toString();
            if (!Pattern.matches(WifiUtils.PATTERN_IPV4, dns1)) {
                showToast(R.string.notice_error_format);
                mEtDns1.requestFocus();
                return;
            } else if (WifiUtils.isBroadcastOrNetworkAddress(dns1)) {
                showToast(R.string.illegal_address);
                mEtDns1.requestFocus();
                return;
            }
            String dns2 = mEtDns2.getText().toString();
            if (mEtDns2.getText().length() > 0) {
                if (!Pattern.matches(WifiUtils.PATTERN_IPV4, gateway)) {
                    showToast(R.string.notice_error_format);
                    mEtDns2.requestFocus();
                    return;
                } else if (WifiUtils.isBroadcastOrNetworkAddress(dns2)) {
                    showToast(R.string.illegal_address);
                    mEtDns2.requestFocus();
                    return;
                }
            }
            mPlcInfo.setDns1(dns1);
            mPlcInfo.setDns2(dns2);
            mPlcInfo.setIp_addr(ip);
            mPlcInfo.setGw(gateway);
            mPlcInfo.setNetmask(mask);
            mPlcInfo.setAddr_type(WanType.STATIC.getTypeCode());
        } else if (index == 1) {//pppoe
            if (mEtPppoeName.getText().length() == 0) {
                showToast(R.string.notice_name_empty);
                mEtPppoeName.requestFocus();
                return;
            }
            if (mEtPppoePassword.getText().length() == 0) {
                showToast(R.string.notice_pwd_empty);
                mEtPppoePassword.requestFocus();
                return;
            }

            mPlcInfo.setUsername(mEtPppoeName.getText().toString());
            mPlcInfo.setPasswd(mEtPppoePassword.getText().toString());
            mPlcInfo.setAddr_type(WanType.PPPOE.getTypeCode());
            mPlcInfo.setAuth_type(0);
            mPlcInfo.setDialing_mode(2);
            mPlcInfo.setMtu(1492);//pppoe时此值必须为1492
        } else {
            mPlcInfo.setAddr_type(WanType.DHCP.getTypeCode());
        }
        doSetWanInfo(mPlcInfo);
    }

    private void saveAndCheck() {
        String nettype = mSpinnerNetworyType.getText().toString();
        int index = Arrays.binarySearch(mTypeChoices, nettype);

        RequireAllBeen been;
        if (mRequireBeen == null) {
            been = new RequireAllBeen();
            been.setFlag_dns(0);
            been.setDail_type(0);
        } else
            been = mRequireBeen;

        if (index == 2) {//static
            String ip = mEtIp.getText().toString();
            if (!Pattern.matches(WifiUtils.PATTERN_IPV4, ip)) {
                showToast(R.string.notice_error_format);
                mEtIp.requestFocus();
                return;
            } else if (WifiUtils.isBroadcastOrNetworkAddress(ip)) {
                showToast(R.string.illegal_address);
                mEtIp.requestFocus();
                return;
            }
            String mask = mEtMask.getText().toString();
            if (!Pattern.matches(WifiUtils.PATTERN_IPV4, mask)) {
                showToast(R.string.notice_error_format);
                mEtMask.requestFocus();
                return;
            }

            String gateway = mEtGateway.getText().toString();
            if (!Pattern.matches(WifiUtils.PATTERN_IPV4, gateway)) {
                showToast(R.string.notice_error_format);
                mEtGateway.requestFocus();
                return;
            } else if (WifiUtils.isBroadcastOrNetworkAddress(gateway)) {
                showToast(R.string.illegal_address);
                mEtGateway.requestFocus();
                return;
            }

            String dns1 = mEtDns1.getText().toString();
            if (!Pattern.matches(WifiUtils.PATTERN_IPV4, dns1)) {
                showToast(R.string.notice_error_format);
                mEtDns1.requestFocus();
                return;
            } else if (WifiUtils.isBroadcastOrNetworkAddress(dns1)) {
                showToast(R.string.illegal_address);
                mEtDns1.requestFocus();
                return;
            }
            String dns2 = mEtDns2.getText().toString();
            if (mEtDns2.getText().length() > 0) {
                if (!Pattern.matches(WifiUtils.PATTERN_IPV4, gateway)) {
                    showToast(R.string.notice_error_format);
                    mEtDns2.requestFocus();
                    return;
                } else if (WifiUtils.isBroadcastOrNetworkAddress(dns2)) {
                    showToast(R.string.illegal_address);
                    mEtDns2.requestFocus();
                    return;
                }
            }
            been.setDns1(dns1);
            been.setDns2(dns2);
            been.setIpaddr(ip);
            been.setGw(gateway);
            been.setNetmask(mask);
            been.setType("static");
        } else if (index == 1) {//pppoe
            if (mEtPppoeName.getText().length() == 0) {
                showToast(R.string.notice_name_empty);
                mEtPppoeName.requestFocus();
                return;
            }
            if (mEtPppoePassword.getText().length() == 0) {
                showToast(R.string.notice_pwd_empty);
                mEtPppoePassword.requestFocus();
                return;
            }

            been.setPppoe_username(mEtPppoeName.getText().toString());
            been.setPppoe_password(mEtPppoePassword.getText().toString());
            been.setType("pppoe");

        } else {
            been.setType("dhcp");
        }

        doSetWanInfo(been);
    }

    private void changePage(int position) {
        switch (position) {
            case 0:
                mPanelPPPoe.setVisibility(View.GONE);
                mPanelStatic.setVisibility(View.GONE);
                break;
            case 2:
                mPanelPPPoe.setVisibility(View.GONE);
                mPanelStatic.setVisibility(View.VISIBLE);
                break;
            case 1:
                mPanelPPPoe.setVisibility(View.VISIBLE);
                mPanelStatic.setVisibility(View.GONE);
                break;

        }
    }

    private void refreshData(ResponseAllBeen been) {
        //input type
        setEditText(mEtIp, been.getIpaddr());
        setEditText(mEtMask, been.getNetmask());
        setEditText(mEtGateway, been.getGw());
        setEditText(mEtDns1, been.getDns1());
        setEditText(mEtDns2, been.getDns2());
        setEditText(mEtPppoeName, been.getPppoe_username());
        setEditText(mEtPppoePassword, been.getPppoe_password());

        {
            WanType wanType = WanType.getDeviceTypeFromName(been.getType());
            if (wanType == WanType.UNKONWN)
                been.setType(WanType.DHCP.getName());
        }
        mSpinnerNetworyType.setText(been.getType().toUpperCase());
        int index = 0;
        if ("static".equalsIgnoreCase(been.getType()) && mDeviceType != DeviceType.BWR) //组网不含static模式
            index = 2;
        else if ("pppoe".equalsIgnoreCase(been.getType()))
            index = 1;
        else
            index = 0;
        mSpinnerNetworyType.setTag(index);
        changePage(index);
    }

    private void doCheckPPPoeState() {

        addTask(
                new CheckPPPoeStateTask().execute(getGateway(), getCookie(), new TaskListener<CheckPPPoeStateTask.EnumPPPoeState>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(getString(R.string.checking_pppoe_state), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task,R.string.interaction_failed);
                        } else {

                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, CheckPPPoeStateTask.EnumPPPoeState param) {
                        if (param != null)
                            showProgressDialog(getString(param.getResId()), false, null);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {

                    }
                })
        );
    }

    private void doSetWanInfo(RequireAllBeen been) {
        addTask(
                new SetWlanTask().execute(getGateway(), been, getCookie(),
                        new TaskListener<Boolean>() {
                            @Override
                            public String getName() {
                                return null;
                            }

                            @Override
                            public void onPreExecute(final GenericTask task) {
                                showProgressDialog(getString(R.string.downloading), false, new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        task.cancel(true);
                                    }
                                });
                            }

                            @Override
                            public void onPostExecute(GenericTask task, TaskResult result) {
                                hideProgressDialog();
                                if (result != TaskResult.OK) {
                                    showTaskError(task,R.string.interaction_failed);
                                } else {
                                    showToast(R.string.commit_completed);
//                                    onFragmentLifeListener.onChanged(true);
                                }
                            }

                            @Override
                            public void onProgressUpdate(GenericTask task, Boolean param) {
                            }

                            @Override
                            public void onCancelled(GenericTask task) {
                                hideProgressDialog();
                            }
                        })
        );
    }

    private void doSetWanInfo(PLCInfo been) {
        addTask(
                new SetPLCDeviceBaseInfoTask().execute(getGateway(), been,
                        new TaskListener<Boolean>() {
                            @Override
                            public String getName() {
                                return null;
                            }

                            @Override
                            public void onPreExecute(final GenericTask task) {
                                showProgressDialog(getString(R.string.downloading), false, new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        task.cancel(true);
                                    }
                                });
                            }

                            @Override
                            public void onPostExecute(GenericTask task, TaskResult result) {
                                hideProgressDialog();
                                if (result != TaskResult.OK) {
                                    showTaskError(task,R.string.interaction_failed);
                                } else {
                                    showToast(R.string.commit_completed);
//                                    onFragmentLifeListener.onChanged(true);
                                }
                            }

                            @Override
                            public void onProgressUpdate(GenericTask task, Boolean param) {
                            }

                            @Override
                            public void onCancelled(GenericTask task) {
                                hideProgressDialog();
                            }
                        })
        );
    }

    private void doGetWanInfo() {
        addTask(
                new GetWlanTask().execute(getGateway(), getCookie(), new TaskListener<ResponseAllBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(getString(R.string.downloading), false, new DialogInterface.OnCancelListener() {
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
                            showTaskError(task,R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, ResponseAllBeen param) {
                        mRequireBeen = new RequireAllBeen();
                        mRequireBeen.setSrc_type(param.getSrc_type());
                        mRequireBeen.setType(param.getType());
                        mRequireBeen.setIpaddr(param.getIpaddr());
                        mRequireBeen.setNetmask(param.getNetmask());
                        mRequireBeen.setGw(param.getGw());
                        mRequireBeen.setPppoe_username(param.getPppoe_username());
                        mRequireBeen.setPppoe_password(param.getPppoe_password());
                        mRequireBeen.setService_name(param.getService_name());
                        mRequireBeen.setDail_type(param.getDail_type());
                        mRequireBeen.setMtu(param.getMtu());
                        mRequireBeen.setClone_mac(param.getClone_mac());
                        mRequireBeen.setFlag_dns(param.getFlag_dns());
                        mRequireBeen.setDns1(param.getDns1());
                        mRequireBeen.setDns2(param.getDns2());
                        refreshData(param);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doGetPLCWanInfo() {
        addTask(
                new GetPLCWanTask().execute(getGateway(), new TaskListener<PLCInfo>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(getString(R.string.downloading), false, new DialogInterface.OnCancelListener() {
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
                            showTaskError(task,R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, PLCInfo param) {
                        mPlcInfo = param;
                        ResponseAllBeen been = new ResponseAllBeen();
                        param.set2ResponseBeen(been);
                        refreshData(been);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.checkbox_hide_psw) {
            isShowPassword(mEtPppoePassword, !isChecked);
        }
    }
}
