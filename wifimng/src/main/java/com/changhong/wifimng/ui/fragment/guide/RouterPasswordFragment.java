package com.changhong.wifimng.ui.fragment.guide;

import android.os.Bundle;
import android.text.Editable;
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

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.uttils.CommUtil;

/**
 * 向导密码
 */
public class RouterPasswordFragment extends BaseFragment<String> implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private EditText mEtPassword;
    private CheckBox mCheckHide;
    private View mBtnConfirm;
    private View mBtnBack;
    /**
     * 当前设备类型
     */
    private String mCurrentDeviceType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_router_password, null, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mEtPassword = view.findViewById(R.id.et_router_password);

        mCheckHide = view.findViewById(R.id.checkbox_hide_psw);
        mCheckHide.setOnCheckedChangeListener(this);

        if (DeviceType.PLC.getName().equalsIgnoreCase(mCurrentDeviceType)) {//电力猫情况下，替换标题和hint
            mEtPassword.setHint(R.string.hint_input_plc_user_pwd);

            TextView titleView = view.findViewById(R.id.tv_title);
            titleView.setText(R.string.title_input_plc_admin_pwd);
        }

        mEtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBtnConfirm.setEnabled(s.length() >= 8);
                if (s.length() > 0) {
                    mCheckHide.setVisibility(View.VISIBLE);
                } else {
                    mCheckHide.setVisibility(View.INVISIBLE);
                }
            }
        });

        mBtnConfirm = view.findViewById(R.id.btn_confirm);
        mBtnConfirm.setOnClickListener(this);

        mBtnBack = view.findViewById(R.id.btn_back);
        mBtnBack.setOnClickListener(this);

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        togglePasswordState(mEtPassword, isChecked);
    }

    @Override
    public void onClick(View v) {
        if (onFragmentLifeListener != null) {
            if (v == mBtnBack) {
                onFragmentLifeListener.onChanged(null);
            } else if (v == mBtnConfirm) {
                CommUtil.closeIME(mActivity);
                onFragmentLifeListener.onChanged(mEtPassword.getText().toString());
            }
        }
    }
}
