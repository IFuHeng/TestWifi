package com.changhong.wifimng.ui.fragment.guide;

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

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.guide.RequireAllBeen;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.uttils.CommUtil;

public class WizardWifiFragment extends BaseFragment<RequireAllBeen> implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, TextWatcher {

    private TextView mTvAccount;
    private TextView mTvPassword;
    private EditText mEtAccount;
    private EditText mEtPassword;
    private View mBtnClear;
    private CheckBox mBtnHide;

    private EditText mEtAccount5G;
    private View mBtnClear5G;

    private View mBtnNext;
    private View mBtnBack;
    private RequireAllBeen been;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wizard_wifi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        //2.4G
        mTvAccount = view.findViewById(R.id.tab_ssid);
        mTvPassword = view.findViewById(R.id.tv_password);
        mEtAccount = view.findViewById(R.id.et_wifi_ssid);
        mEtAccount.addTextChangedListener(this);
        mEtPassword = view.findViewById(R.id.et_wifi_password);
        mEtPassword.addTextChangedListener(this);
        mBtnHide = view.findViewById(R.id.checkbox_hide_psw);
        mBtnHide.setOnCheckedChangeListener(this);
        mBtnClear = view.findViewById(R.id.btn_clear);
        mBtnClear.setOnClickListener(this);

        //5G
        mEtAccount5G = view.findViewById(R.id.et_wifi_ssid_5G);
        mEtAccount5G.addTextChangedListener(this);
        mBtnClear5G = view.findViewById(R.id.btn_clear_5G);
        mBtnClear5G.setOnClickListener(this);


        mBtnNext = view.findViewById(R.id.btn_next);
        mBtnBack = view.findViewById(R.id.btn_back);
        mBtnNext.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            been = getArguments().getParcelable(Intent.EXTRA_TEXT);
            refreshView(been);
        }
    }

    @Override
    public void onClick(View v) {
        if (onFragmentLifeListener != null) {
            if (v == mBtnNext) {
                dealCommit();
            } else if (v == mBtnClear) {
                mEtAccount.setText(null);
                mEtAccount.requestFocus();
            } else if (v == mBtnClear5G) {
                mEtAccount5G.setText(null);
                mEtAccount5G.requestFocus();
            } else if (v == mBtnBack) {
                onFragmentLifeListener.onChanged(null);
            }
        }
    }

    private void dealCommit() {
        if (mEtAccount.getText().length() == 0) {
            mEtAccount.requestFocus();
            showToast(R.string.notice_ssid_empty);
            return;
        }
        if (mEtPassword.getText().length() > 0 && mEtPassword.getText().length() < 8) {
            mEtPassword.requestFocus();
            showToast(R.string.wizard_password_wifi_rule);
            return;
        }

        if (been != null && been.get_5G_priority() != null && been.get_5G_priority() == 0) {
            if (mEtAccount5G.getText().length() == 0) {
                mEtAccount5G.requestFocus();
                showToast(R.string.notice_ssid_empty);
                return;
            }
        }

        if (been == null) {
            been = new RequireAllBeen();
            been.set_5G_priority(1);
            been.setKey_sync(0);
            been.setEncryption("none");
        }

        if (been.get_5G_priority() != null && been.get_5G_priority() == 0) {
            been.setSsid_2G(mEtAccount.getText().toString());
            been.setSsid_5G(mEtAccount5G.getText().toString());
        } else {
            been.setSsid(mEtAccount.getText().toString());
        }
        String password = mEtPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            been.setEncryption("none");
            been.setKey("");
        } else {
            been.setEncryption("wpa2_mixed_psk");
            been.setKey(password);
        }

        been.setSave_action(0);

        CommUtil.closeIME(mActivity);
        onFragmentLifeListener.onChanged(been);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        togglePasswordState(mEtPassword, isChecked);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {


    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        refreshButton();
    }

    private void refreshButton() {
        if (been.get_5G_priority() != null && been.get_5G_priority() == 0) {
            boolean enable = mEtPassword.getText().length() == 0 || mEtPassword.getText().length() > 7;
            enable = mEtAccount.getText().length() != 0 && enable;
            enable = mEtAccount5G.getText().length() != 0 && enable;

            mBtnNext.setEnabled(enable);

            if (mEtPassword.getText().length() == 0)
                mBtnHide.setVisibility(View.GONE);
            else
                mBtnHide.setVisibility(View.VISIBLE);

            if (mEtAccount.getText().length() == 0) {
                mBtnClear.setVisibility(View.GONE);
            } else
                mBtnClear.setVisibility(View.VISIBLE);

            if (mEtAccount5G.getText().length() == 0) {
                mBtnClear5G.setVisibility(View.GONE);
            } else
                mBtnClear5G.setVisibility(View.VISIBLE);
        } else {

            boolean enable = mEtPassword.getText().length() == 0 || mEtPassword.getText().length() > 7;
            enable = mEtAccount.getText().length() != 0 && enable;
            mBtnNext.setEnabled(enable);

            if (mEtPassword.getText().length() == 0)
                mBtnHide.setVisibility(View.GONE);
            else
                mBtnHide.setVisibility(View.VISIBLE);

            if (mEtAccount.getText().length() == 0) {
                mBtnClear.setVisibility(View.GONE);
            } else
                mBtnClear.setVisibility(View.VISIBLE);
        }

    }

    private void refreshView(RequireAllBeen been) {
        String password = been.getKey();
        if (password != null) {
            mEtPassword.setText(password);
//                mEtPassword.setHint(password);
        }
        if (been.get_5G_priority() == null || been.get_5G_priority() == 1) {
            ((View) mEtAccount5G.getParent()).setVisibility(View.GONE);
            mTvAccount.setText(R.string.wifi_name);

            String ssid = been.getSsid();
            if (ssid != null) {
                mEtAccount.setText(ssid);
                mEtAccount.setHint(ssid);
            }
        } else if (been.get_5G_priority() == 0) {
            ((View) mEtAccount5G.getParent()).setVisibility(View.VISIBLE);
            mTvAccount.setText(R.string.wifi_name_2G);

            mEtAccount.setText(been.getSsid_2G());
            mEtAccount.setHint(been.getSsid_2G());
            mEtAccount5G.setText(been.getSsid_5G());
            mEtAccount5G.setHint(been.getSsid_5G());
        }
    }
}
