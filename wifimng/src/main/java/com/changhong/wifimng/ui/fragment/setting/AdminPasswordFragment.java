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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.LoginTask;
import com.changhong.wifimng.task.SetRouterPasswordTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.uttils.CommUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminPasswordFragment extends BaseFragment<Boolean> implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private EditText mEtPassword;
    private EditText mEtPasswordNew;
    private EditText mEtPasswordNew2;
    private CheckBox mBtnHide;
    private CheckBox mBtnHide1;
    private CheckBox mBtnHide2;
    private String mCurrentDeviceType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            mCurrentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manager_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mEtPassword = view.findViewById(R.id.et_password);
        mEtPasswordNew = view.findViewById(R.id.et_password_new);
        mEtPasswordNew2 = view.findViewById(R.id.et_password_new_2);

        mBtnHide = view.findViewById(R.id.checkbox_hide_psw);
        mBtnHide1 = view.findViewById(R.id.checkbox_hide_psw1);
        mBtnHide2 = view.findViewById(R.id.checkbox_hide_psw2);

        mEtPassword.addTextChangedListener(new MyTextWatcher(mBtnHide));
        mEtPasswordNew.addTextChangedListener(new MyTextWatcher(mBtnHide1));
        mEtPasswordNew2.addTextChangedListener(new MyTextWatcher(mBtnHide2));

        mBtnHide.setOnCheckedChangeListener(this);
        mBtnHide1.setOnCheckedChangeListener(this);
        mBtnHide2.setOnCheckedChangeListener(this);

        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.btn_confirm).setOnClickListener(this);

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
        if (v.getId() == R.id.btn_confirm) {
            if (mEtPassword.length() < 8) {
                showToast(R.string.password_rule);
                mEtPassword.setFocusable(true);
                return;
            }

            if (mEtPasswordNew.length() < 8) {
                showToast(R.string.password_rule);
                mEtPasswordNew.setFocusable(true);
                return;
            }
            if (mEtPasswordNew2.length() < 8) {
                showToast(R.string.password_rule);
                mEtPasswordNew2.setFocusable(true);
                return;
            }

            String password = mEtPassword.getText().toString();
            String passwordnew = mEtPasswordNew.getText().toString();
            String passwordnew2 = mEtPasswordNew2.getText().toString();
            if (!passwordnew.equals(passwordnew2)) {
                showToast(R.string.password_not_same);
                mEtPasswordNew2.setFocusable(true);
                return;
            }

            if (DeviceType.PLC.getName().equalsIgnoreCase(mCurrentDeviceType)
                    && !Preferences.getInstance(null).getString(KeyConfig.KEY_ROUTER_PASSWORD).equals(password)) {
                showToast(R.string.notice_admin_password_error);
                return;
            }
            CommUtil.closeIME(mActivity);
            doCommitPassword(password, passwordnew);
        }

        //返回
        else if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(null);
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.checkbox_hide_psw) {
            togglePasswordState(mEtPassword, isChecked);
        } else if (buttonView.getId() == R.id.checkbox_hide_psw1) {
            togglePasswordState(mEtPasswordNew, isChecked);
        } else if (buttonView.getId() == R.id.checkbox_hide_psw2) {
            togglePasswordState(mEtPasswordNew2, isChecked);
        }
    }

    private void doCommitPassword(String oldpassword, String newPassword) {
        addTask(
                new SetRouterPasswordTask().execute(oldpassword, newPassword, getGateway(), mCurrentDeviceType, false, getString(R.string.old_password_wrong), getCookie(),
                        new TaskListener<String>() {
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
                                if (result != TaskResult.OK) {
                                    showTaskError(task, R.string.commit_failed);
                                } else
                                    showToast(R.string.commit_completed);
                            }

                            @Override
                            public void onProgressUpdate(GenericTask task, String param) {
                                Preferences.getInstance(mActivity).saveString(KeyConfig.KEY_ROUTER_PASSWORD, param);
                                doLoginRouter(param);
                            }

                            @Override
                            public void onCancelled(GenericTask task) {
                                hideProgressDialog();
                            }
                        })
        );
    }

    /**
     * 登录路由器，校验密码
     *
     * @param password
     */
    private void doLoginRouter(String password) {

        addTask(
                new LoginTask().execute(password, getGateway(), mCurrentDeviceType, getString(R.string.password_wrong), new TaskListener<BaseBeen<String, String>>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(getString(R.string.logining), false, new DialogInterface.OnCancelListener() {
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
//                            showToast(R.string.login_succeed);
                            showToast(R.string.commit_completed);
                            onFragmentLifeListener.onChanged(null);
                        } else {
                            showTaskError(task, R.string.interaction_failed);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, BaseBeen<String, String> param) {
                        if (param != null) {
                            Preferences.getInstance(mActivity).saveString(KeyConfig.KEY_ROUTER_PASSWORD, param.getT1());
                            Preferences.getInstance(mActivity).saveString(KeyConfig.KEY_COOKIE_SSID, param.getT2());
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private class MyTextWatcher implements TextWatcher {

        private View btn;

        public MyTextWatcher(View btn) {
            this.btn = btn;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0)
                btn.setVisibility(View.INVISIBLE);
            else
                btn.setVisibility(View.VISIBLE);
        }
    }


}
