package com.changhong.wifimng.ui.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.been.wan.Level2Been;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.router.GetSpeedLimitLoadTask;
import com.changhong.wifimng.task.router.SetSpeedLimitLoadTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.GetPLCLinkInfoTask;
import com.changhong.wifimng.task.plc.SetPLCSpeedLimitTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SpeedLimitFragment extends BaseFragment implements View.OnClickListener, View.OnFocusChangeListener {

    private EditText mEtUploadLimit;
    private EditText mEtDownloadLimit;
    private String mDeviceMac;
    /**
     * 当前已连接路由器类型
     */
    private String mCurrentDeviceType;
    private DeviceType mDeviceType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDeviceMac = getArguments().getString(KeyConfig.KEY_DEVICE_MAC);
        mCurrentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
//        if (mDeviceMac.indexOf(':') != -1) {
//            mDeviceMac = mDeviceMac.replace(":", "");
//        }
        if (mDeviceMac == null) {
            onFragmentLifeListener.onChanged(null);
            showToast(R.string.notice_mac_empty);
        }

        mDeviceType = DeviceType.getDeviceTypeFromName(mCurrentDeviceType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speed_limit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.btn_save).setOnClickListener(this);

        mEtUploadLimit = view.findViewById(R.id.et_upload_limit);
        mEtDownloadLimit = view.findViewById(R.id.et_download_limit);
        mEtUploadLimit.setOnFocusChangeListener(this);
        mEtDownloadLimit.setOnFocusChangeListener(this);

// 设备限速海思底层又改回Kbps了，从Mbps改为Kbps
//        if (mDeviceType == DeviceType.PLC) {
//            TextView tabUpload = view.findViewById(R.id.tab_max_upload);
//            tabUpload.setText(R.string.tab_upload_limit_PLC);
//            TextView tabDownload = view.findViewById(R.id.tab_max_download);
//            tabDownload.setText(R.string.tab_download_limit_PLC);
//        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DeviceType.PLC.getName().equalsIgnoreCase(mCurrentDeviceType)) {
            doGetPlcLinkInfo();
        } else {
            doLoadSpeedLimit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(null);
        }
        //保存
        else if (v.getId() == R.id.btn_save) {
            if (DeviceType.PLC.getName().equalsIgnoreCase(mCurrentDeviceType))
                doCommitSpeedLimitPLC();
            else {
//                if (isChanged())
                    askAddEffectImmediately();
//                else
//                    showToast(R.string.not_changed);
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v instanceof EditText) {
                EditText et = (EditText) v;

                et.setSelection(et.getText().length());
            }
        }

        if (v.getId() == R.id.et_upload_limit) {
        } else if (v.getId() == R.id.et_download_limit) {
        }
    }

    /**
     * 询问添加是否立即执行
     */
    private void askAddEffectImmediately() {
        new AlertDialog.Builder(mActivity).setMessage(_getString(R.string.notice_ask_execute_immediately)).setPositiveButton(_getString(R.string.save_and_execute), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doCommitSpeedLimit(true);
            }
        }).setNegativeButton(_getString(R.string.save_only), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doCommitSpeedLimit(false);
            }
        }).create().show();
    }

    private void doCommitSpeedLimit(boolean isEffectImmediately) {
        int down_speed = 0;
        int up_speed = 0;
        if (mEtUploadLimit.getText().length() > 0)
            up_speed = Integer.parseInt(mEtUploadLimit.getText().toString());
        if (mEtDownloadLimit.getText().length() > 0)
            down_speed = Integer.parseInt(mEtDownloadLimit.getText().toString());

        boolean isAdd = down_speed != 0 || up_speed != 0;
        addTask(new SetSpeedLimitLoadTask().execute(getGateway(), mDeviceMac, up_speed, down_speed, isAdd, isEffectImmediately, getCookie(), new TaskListener<List<Level2Been>>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), true, new DialogInterface.OnCancelListener() {
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
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, List<Level2Been> param) {

                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doCommitSpeedLimitPLC() {
        int down_speed = 0;
        int up_speed = 0;
        if (mEtUploadLimit.getText().length() > 0)
            up_speed = Integer.parseInt(mEtUploadLimit.getText().toString());
        if (mEtDownloadLimit.getText().length() > 0)
            down_speed = Integer.parseInt(mEtDownloadLimit.getText().toString());
        addTask(new SetPLCSpeedLimitTask().execute(getGateway(), mDeviceMac, up_speed, down_speed, new TaskListener() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), true, new DialogInterface.OnCancelListener() {
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

    private void doLoadSpeedLimit() {
        addTask(
                new GetSpeedLimitLoadTask().execute(getGateway(), getCookie(), new TaskListener<List<Level2Been>>() {
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
                        if (result == TaskResult.OK) {

                        } else {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, List<Level2Been> param) {
                        if (param != null) {
                            for (Level2Been been : param) {
                                if (mDeviceMac.equalsIgnoreCase(been.getMac())) {
                                    if (been.getMax_download() != null && been.getMax_download() != 0) {
                                        mEtDownloadLimit.setText(
                                                String.valueOf(
                                                        been.getMax_download()
                                                )
                                        );
                                        mEtDownloadLimit.setTag(been.getMax_download());
                                    }
                                    if (been.getMax_upload() != null && been.getMax_upload() != 0) {
                                        mEtUploadLimit.setText(
                                                String.valueOf(
                                                        been.getMax_upload()
                                                )
                                        );
                                        mEtUploadLimit.setTag(been.getMax_upload());
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doGetPlcLinkInfo() {
        addTask(
                new GetPLCLinkInfoTask().execute(getGateway(), new TaskListener<PLCInfo>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
//                        showProgressDialog(getString(R.string.downloading), false, null);
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
                    public void onProgressUpdate(GenericTask task, PLCInfo param) {
                        if (param != null && !param.getDev_info().isEmpty()) {
                            for (PLCInfo.Dev_Info dev_info : param.getDev_info()) {
                                if (dev_info.getMac().equals(mDeviceMac)) {
                                    if (dev_info.getRx_speed_limited() != null && dev_info.getRx_speed_limited() != 0) {
                                        mEtDownloadLimit.setText(
                                                String.valueOf(
                                                        dev_info.getRx_speed_limited()
                                                )
                                        );
                                        mEtDownloadLimit.setTag(dev_info.getRx_speed_limited());
                                    }
                                    if (dev_info.getTx_speed_limited() != null && dev_info.getTx_speed_limited() != 0) {
                                        mEtUploadLimit.setText(
                                                String.valueOf(
                                                        dev_info.getTx_speed_limited()
                                                )
                                        );
                                        mEtUploadLimit.setTag(dev_info.getTx_speed_limited());
                                    }
                                    break;
                                }
                            }
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
     * @return 数据是否有修改
     */
    private boolean isChanged() {
        int down_speed = 0;
        int up_speed = 0;
        if (mEtUploadLimit.getText().length() > 0)
            up_speed = Integer.parseInt(mEtUploadLimit.getText().toString());
        if (mEtDownloadLimit.getText().length() > 0)
            down_speed = Integer.parseInt(mEtDownloadLimit.getText().toString());
        //检测是否修改
        int ous = mEtUploadLimit.getTag() != null ? (Integer) mEtUploadLimit.getTag() : 0;
        int ods = mEtDownloadLimit.getTag() != null ? (Integer) mEtDownloadLimit.getTag() : 0;
        if (ous == up_speed && ods == down_speed) {
            return false;
        }

        return true;
    }
}
