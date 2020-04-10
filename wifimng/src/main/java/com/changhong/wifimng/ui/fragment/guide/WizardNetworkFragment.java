package com.changhong.wifimng.ui.fragment.guide;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.guide.RequireAllBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.view.DefaultIPV4Watcher;
import com.changhong.wifimng.uttils.CommUtil;
import com.changhong.wifimng.uttils.WifiUtils;

import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class WizardNetworkFragment extends BaseFragment<RequireAllBeen> implements View.OnClickListener, AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    private Spinner mSpinnerNetworyType;
    private View mPanelPPPoe;
    private View mPanelStatic;
    private EditText mEtIp;
    private EditText mEtMask;
    private EditText mEtGateway;
    private EditText mEtDns1;
    private EditText mEtDns2;
    private EditText mEtPppoeName;
    private EditText mEtPppoePassword;

    private DefaultIPV4Watcher mIpv4Watcher;

    private DeviceType mDeviceType;
    /**
     * 当前设备类型
     */
    private String mCurrentDeviceType;

    private CheckBox mCheckHide;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIpv4Watcher = new DefaultIPV4Watcher();

        //获取当前设备类型
        mCurrentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
        if (!TextUtils.isEmpty(mCurrentDeviceType))
            for (DeviceType value : DeviceType.values()) {
                if (mCurrentDeviceType.equals(value.getName())) {
                    mDeviceType = value;
                    break;
                }
            }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wizard_network, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mSpinnerNetworyType = view.findViewById(R.id.spinner_network_type);
        mSpinnerNetworyType.setOnItemSelectedListener(this);
        if (mDeviceType == DeviceType.BWR) {
            ArrayAdapter adapter = ArrayAdapter.createFromResource(mActivity, R.array.networkType_bwr, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinnerNetworyType.setAdapter(adapter);
        }

        mPanelPPPoe = view.findViewById(R.id.panel_pppoe);
        mPanelStatic = view.findViewById(R.id.panel_static);

        view.findViewById(R.id.btn_next).setOnClickListener(this);


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

        //input type
        if (getArguments() != null) {
            RequireAllBeen been = getArguments().getParcelable(Intent.EXTRA_TEXT);
            if (mDeviceType != DeviceType.BWR) {
                setEditText(mEtIp, been.getIpaddr());
                setEditText(mEtMask, been.getNetmask());
                setEditText(mEtGateway, been.getGw());
                setEditText(mEtDns1, been.getDns1());
                setEditText(mEtDns2, been.getDns2());
                setEditText(mEtPppoeName, been.getPppoe_username());
                setEditText(mEtPppoePassword, been.getPppoe_password());
            }

            if (been.getType() != null) {
                if ("static".equalsIgnoreCase(been.getType())) {
                    if (mDeviceType != DeviceType.BWR)//组网不含static模式
                        mSpinnerNetworyType.setSelection(2);
                } else if ("pppoe".equalsIgnoreCase(been.getType()))
                    mSpinnerNetworyType.setSelection(1);
            }
        }

        super.onViewCreated(view, savedInstanceState);
    }

    private void setEditText(EditText et, String value) {
        if (value != null) {
            et.setText(value);
            et.setHint(value);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_next) {
            int index = mSpinnerNetworyType.getSelectedItemPosition();
            RequireAllBeen been = new RequireAllBeen();
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

            //TODO GO NEXT
            if (onFragmentLifeListener != null) {
                CommUtil.closeIME(mActivity);
                been.setSave_action(0);
                onFragmentLifeListener.onChanged(been);
            }
        }

        //返回
        else if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(null);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0://DHCP
                mPanelPPPoe.setVisibility(View.GONE);
                mPanelStatic.setVisibility(View.GONE);
                break;
            case 2://STATIC
                mPanelPPPoe.setVisibility(View.GONE);
                mPanelStatic.setVisibility(View.VISIBLE);
                break;
            case 1://PPPOE
                mPanelPPPoe.setVisibility(View.VISIBLE);
                mPanelStatic.setVisibility(View.GONE);
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.checkbox_hide_psw) {
            isShowPassword(mEtPppoePassword, !isChecked);
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (getFragmentManager().getFragments()!=null &&getFragmentManager().getFragments().size()>1){
//            getFragmentManager()
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
}
