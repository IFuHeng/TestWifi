package com.changhong.wifimng.ui.fragment.setting;


import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.plc.PLCGuestInfo;
import com.changhong.wifimng.been.wifi.GuestRequireAndResponseBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.GetPLCGuestTask;
import com.changhong.wifimng.task.plc.SetPLCLinkInfoTask;
import com.changhong.wifimng.task.router.GetGuestNetworkTask;
import com.changhong.wifimng.task.router.SetGuestNetworkTask;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.view.DefaultIntegerWatcher;

/**
 * A simple {@link Fragment} subclass.
 */
public class GuestNetworkFragment extends BaseFragment<Boolean> implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private EditText mEtPassword;
    private EditText mEtSSid;
    private EditText mEtNumber;
    private CheckBox mCbUsePassword;
    private View mPanelUsePassword;
    private CheckBox mCbUseTimeLimit;
    private CheckBox mCbHidePsw;

    private Switch mSwitchUse;
    private View mPanelGuestNetwork;
    private TextView mTvState;
    private TextView mTvTimeRemain;

    private View mBtnReduce;
    private View mBtnIncrease;
    private Spinner mSpinnerTimeLimit;

    private final int MAX_GUEST_COUNT = 100;

    private CountDownTimer mCountDownTimer;

    private GuestRequireAndResponseBeen mInfo;
    private PLCGuestInfo mPLCInfo;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guest_wifi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        view.findViewById(R.id.btn_back).setOnClickListener(this);
        mPanelGuestNetwork = view.findViewById(R.id.panel_set);

        mSwitchUse = view.findViewById(R.id.switch_guest_wifi);
        mSwitchUse.setOnCheckedChangeListener(this);
        ((View) mSwitchUse.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitchUse.toggle();
            }
        });
        mTvState = view.findViewById(R.id.tv_status);

        mEtPassword = view.findViewById(R.id.et_guest_password);
        mEtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mCbHidePsw.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
            }
        });
        ((View) mEtPassword.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtPassword.requestFocus();
            }
        });
        mEtSSid = view.findViewById(R.id.et_guest_ssid);
        ((View) mEtSSid.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtSSid.requestFocus();
            }
        });
        mEtNumber = view.findViewById(R.id.et_guest_max_count);
        mEtNumber.addTextChangedListener(new DefaultIntegerWatcher());
        mEtNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && v instanceof EditText) {
                    EditText edit = (EditText) v;
                    if (edit.getText().length() > 0)
                        edit.setSelection(0, edit.getText().length());
                }
            }
        });

        mCbUsePassword = view.findViewById(R.id.checkbox_use_password);
        mCbUsePassword.setOnCheckedChangeListener(this);
        mCbUseTimeLimit = view.findViewById(R.id.checkbox_use_time_limit);
        mCbUseTimeLimit.setOnCheckedChangeListener(this);
        mCbHidePsw = view.findViewById(R.id.checkbox_hide_psw);
        mCbHidePsw.setOnCheckedChangeListener(this);

        mPanelUsePassword = view.findViewById(R.id.panel_guest_password);
        mSpinnerTimeLimit = view.findViewById(R.id.spinner_limit_time);

        mBtnIncrease = view.findViewById(R.id.btn_increase);
        mBtnIncrease.setOnClickListener(this);
        mBtnReduce = view.findViewById(R.id.btn_reduce);
        mBtnReduce.setOnClickListener(this);

        mTvTimeRemain = view.findViewById(R.id.tv_time_remain);


//        if (mDeviceType == DeviceType.PLC) {// TODO 电力猫 时间这部分暂时有问题。暂时屏蔽
//            ((ViewGroup) mCbUseTimeLimit.getParent()).setVisibility(View.INVISIBLE);
//            ((ViewGroup) mEtNumber.getParent()).setVisibility(View.INVISIBLE);
//            view.findViewById(R.id.tv_title_advance_settings).setVisibility(View.INVISIBLE);
//        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDeviceType == DeviceType.PLC)
            doPLCLoadTask();
        else
            doLoadTask();
    }

    @Override
    public void onDestroy() {
        hideCountDownTimer();

        super.onDestroy();
    }

    private void refreshView(PLCGuestInfo been) {
        mSwitchUse.setChecked(been.getEnable() == 1);
        if (been.getTime_left() != null && been.getTime_left() > 0) {
            ((View) mTvTimeRemain.getParent()).setVisibility(View.VISIBLE);
            startCountDownTimer(been.getTime_left());
        } else {
            hideCountDownTimer();
            ((View) mTvTimeRemain.getParent()).setVisibility(View.INVISIBLE);
        }

        mEtSSid.setText(been.getName());
        mCbUsePassword.setChecked(been.getPasswd() != null && been.getPasswd().length() > 0);
        mEtPassword.setText(been.getPasswd());
        int hour = been.getValid_time() == null ? 0 : been.getValid_time() / 3600;
        if (hour > 0) {
            mCbUseTimeLimit.setChecked(true);
            mSpinnerTimeLimit.setSelection(hour - 1);
        } else
            mCbUseTimeLimit.setChecked(false);
    }

    private void refreshView(GuestRequireAndResponseBeen been) {
        mSwitchUse.setChecked(been.getEnabled() == 1);
        if (been.getDisable_time() != null && been.getDisable_time() > 0) {
            ((View) mTvTimeRemain.getParent()).setVisibility(View.VISIBLE);
            startCountDownTimer(been.getDisable_time());
        } else {
            hideCountDownTimer();
            ((View) mTvTimeRemain.getParent()).setVisibility(View.INVISIBLE);
        }

        mEtSSid.setText(been.getSsid());
        mCbUsePassword.setChecked(been.getEncryption() == 1);
        mEtPassword.setText(been.getKey());
        if (been.getLong_time() != null && been.getLong_time() > 0) {
            mCbUseTimeLimit.setChecked(true);
            mSpinnerTimeLimit.setSelection(been.getLong_time() - 1);
        } else
            mCbUseTimeLimit.setChecked(false);
    }

    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.btn_save) {
//            if (checkCommitData())
//                doSaveTask();
//        }else

        //reduce
        if (v.getId() == R.id.btn_reduce) {
            if (mEtNumber.getText().length() == 0) {
                mEtNumber.setText(String.valueOf(1));
            } else {
                int number = Integer.parseInt(mEtNumber.getText().toString());
                if (--number < 1)
                    number = 1;
                mEtNumber.setText(String.valueOf(number));
                mEtNumber.setSelection(mEtNumber.getText().length());
            }
        }

        //increase
        else if (v.getId() == R.id.btn_increase) {
            if (mEtNumber.getText().length() == 0) {
                mEtNumber.setText(String.valueOf(1));
            } else {
                int number = Integer.parseInt(mEtNumber.getText().toString());
                if (++number < 1)
                    number = 1;
                else if (number > MAX_GUEST_COUNT)
                    number = MAX_GUEST_COUNT;
                mEtNumber.setText(String.valueOf(number));
                mEtNumber.setSelection(mEtNumber.getText().length());
            }
        }
        //返回
        else if (v.getId() == R.id.btn_back) {
            doBack();
        }
    }

    private void doBack() {
        if (isChanged()) {
            showAlert(_getString(R.string.notice_data_changed_and_save), _getString(R.string.save), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (checkCommitData())
                        if (mDeviceType == DeviceType.PLC)
                            doSavePLCTask();
                        else
                            doSaveTask();
                }
            }, _getString(R.string.no_save), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (onFragmentLifeListener != null)
                        onFragmentLifeListener.onChanged(null);
                }
            }, true);
        } else {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(null);
        }
    }

    /**
     * @return 判断是否修改过数据
     */
    private boolean isChanged() {
        if (mInfo != null) {
            if ((mInfo.getEnabled() == 1) != mSwitchUse.isChecked()
                    || (mInfo.getEncryption() == 1) != mCbUsePassword.isChecked()
                    || !mInfo.getSsid().equals(mEtSSid.getText().toString()))
                return true;

            if ((mInfo.getEncryption() == 1) != mCbUsePassword.isChecked())
                return true;
            else {
                if (mCbUsePassword.isChecked())
                    if (mEtPassword.getText().length() >= 8
                            && !mInfo.getKey().equals(mEtPassword.getText().toString()))
                        return true;
            }

            int longtime = mCbUseTimeLimit.isChecked() ? (mSpinnerTimeLimit.getSelectedItemPosition() + 1) : 0;
            if (mInfo.getLong_time() != longtime)
                return true;
        }
        //PLC
        if (mPLCInfo != null) {
            if ((mPLCInfo.getEnable() == 1) != mSwitchUse.isChecked()
                    || !mPLCInfo.getName().equals(mEtSSid.getText().toString()))
                return true;

            if (mEtPassword.getText().length() >= 8
                    && !mPLCInfo.getPasswd().equals(mEtPassword.getText().toString()))
                return true;

            if (mSwitchUse.isChecked()) {
                int longtime = mCbUseTimeLimit.isChecked() ? (mSpinnerTimeLimit.getSelectedItemPosition() + 1) : 0;
                longtime *= 3600;
                return mPLCInfo.getValid_time() != longtime;
            }
            return false;
        }

        return false;
    }

    /**
     * @return 校验填写的数据
     */
    private boolean checkCommitData() {

        if (mEtSSid.getText().length() == 0) {
            mEtSSid.requestFocus();
            showToast(R.string.notice_not_be_empty);
            return false;
        }
        if (mSwitchUse.isChecked()) {
            if (mCbUsePassword.isChecked() && mEtPassword.getText().length() < 8) {
                showToast(R.string.password_wifi_rule);
                mCbUsePassword.requestFocus();
                return false;
            }
        }
        return true;
    }

    /**
     * @return 创建提交数据对象
     */
    private GuestRequireAndResponseBeen createCommitBeen() {
        GuestRequireAndResponseBeen been = new GuestRequireAndResponseBeen();
        been.setEnabled(mSwitchUse.isChecked() ? 1 : 0);
//        if (mSwitchUse.isChecked()) {
        been.setDelay(1);
        been.setSsid(mEtSSid.getText().toString());
        been.setEncryption(mCbUsePassword.isChecked() ? 1 : 0);
        been.setKey(mEtPassword.getText().toString());
        if (mCbUseTimeLimit.isChecked())
            been.setLong_time(mSpinnerTimeLimit.getSelectedItemPosition() + 1);
        else
            been.setLong_time(0);
//        }
        return been;
    }

    /**
     * 创建提交数据对象
     */
    private void writeInfo2PlcInfo() {
        mPLCInfo.setEnable(mSwitchUse.isChecked() ? 1 : 0);
        mPLCInfo.setName(mEtSSid.getText().toString());
        mPLCInfo.setPasswd(mEtPassword.getText().toString());
        if (mCbUseTimeLimit.isChecked())
            mPLCInfo.setValid_time((mSpinnerTimeLimit.getSelectedItemPosition() + 1) * 60);
        else
            mPLCInfo.setValid_time(0);
    }

    private void doSaveTask() {
        addTask(
                new SetGuestNetworkTask().execute(getGateway(), createCommitBeen(), getCookie(), new TaskListener<GuestRequireAndResponseBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), false, new DialogInterface.OnCancelListener() {
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
                    public void onProgressUpdate(GenericTask task, GuestRequireAndResponseBeen param) {

                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doSavePLCTask() {
        PLCGuestInfo plcInfo = new PLCGuestInfo(mPLCInfo);
        plcInfo.setEnable(mSwitchUse.isChecked() ? 1 : 0);
        plcInfo.setName(mEtSSid.getText().toString());
        plcInfo.setPasswd(mEtPassword.getText().toString());
        int longtime = mCbUseTimeLimit.isChecked() ? (mSpinnerTimeLimit.getSelectedItemPosition() + 1) : 0;
        longtime *= 3600;
        plcInfo.setValid_time(longtime);
        addTask(
                new SetPLCLinkInfoTask().execute(getGateway(), plcInfo, new TaskListener() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), false, new DialogInterface.OnCancelListener() {
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

    /**
     * 加载
     */
    private void doLoadTask() {
        addTask(
                new GetGuestNetworkTask().execute(getGateway(), getCookie(), new TaskListener<GuestRequireAndResponseBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.downloading), true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                onFragmentLifeListener.onChanged(null);
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
                        }

                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, GuestRequireAndResponseBeen param) {
                        if (param != null) {
                            mInfo = param;
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
     * 加载
     */
    private void doPLCLoadTask() {
        addTask(
                new GetPLCGuestTask().execute(getGateway(), new TaskListener<PLCGuestInfo>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.downloading), true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                onFragmentLifeListener.onChanged(null);
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
                        }

                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, PLCGuestInfo param) {
                        if (param != null) {
                            mPLCInfo = param;
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.checkbox_use_password) {
            if (!isChecked && mDeviceType == DeviceType.PLC) {
                buttonView.setOnCheckedChangeListener(null);
                showToast(R.string.notice_plc_guest_cannot_open);
                buttonView.setChecked(true);
                buttonView.setOnCheckedChangeListener(this);
            } else
                mPanelUsePassword.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
        } else if (buttonView.getId() == R.id.checkbox_use_time_limit) {
            mSpinnerTimeLimit.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
        } else if (buttonView.getId() == R.id.switch_guest_wifi) {
            mPanelGuestNetwork.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            mTvState.setText(isChecked ? R.string.open : R.string.close);
        } else if (buttonView.getId() == R.id.checkbox_hide_psw) {
            togglePasswordState(mEtPassword, isChecked);
        }
    }

    private void startCountDownTimer(int duration) {
        hideCountDownTimer();
        if (mDeviceType == DeviceType.PLC)
            mCountDownTimer = new CountDownTimer(duration * 1000L, 60000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    ((View) mTvTimeRemain.getParent()).setVisibility(View.VISIBLE);
                    mTvTimeRemain.setText(turnTime2HHmm((int) (millisUntilFinished / 1000)));
                    Log.d(getClass().getSimpleName(), "===~" + millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    ((View) mTvTimeRemain.getParent()).setVisibility(View.INVISIBLE);
                }
            };
        else
            mCountDownTimer = new CountDownTimer(duration * 1000L, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    ((View) mTvTimeRemain.getParent()).setVisibility(View.VISIBLE);
                    mTvTimeRemain.setText(turnTime2HHmmss((int) (millisUntilFinished / 1000)));
                    Log.d(getClass().getSimpleName(), "===~" + millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    ((View) mTvTimeRemain.getParent()).setVisibility(View.INVISIBLE);
                }
            };
        mCountDownTimer.start();
    }

    private void hideCountDownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    private CharSequence turnTime2HHmmss(int secounds) {
        int hour = secounds / 3600;
        int minute = secounds / 60 % 60;
        int secound = secounds % 60;
//        if (hour == 0) {
//            if (minute == 0)
//                return String.format("%02d", secound);
//            else
//                return String.format("%02d分02%02d秒", minute, secound);
//        }
        return String.format("%d:%02d:%02d", hour, minute, secound);
    }

    private CharSequence turnTime2HHmm(int secounds) {
        int hour = secounds / 3600;
        int minute = secounds / 60 % 60;
//        if (hour == 0) {
//            return String.format("%02d分", minute);
//        }


        return new SpannableString(String.format("%d:%02d", hour, minute));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            doBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
