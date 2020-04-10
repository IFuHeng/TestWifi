package com.changhong.wifimng.ui.fragment;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen3;
import com.changhong.wifimng.been.ConnectType;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.StaInfo;
import com.changhong.wifimng.been.mesh.ListInfo;
import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.been.wifi.Level2Been;
import com.changhong.wifimng.been.wifi.ResponseAllBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.GetPLCLinkInfoTask;
import com.changhong.wifimng.task.router.AddWlanAccessTask;
import com.changhong.wifimng.task.router.DeleteWlanAccessTask;
import com.changhong.wifimng.task.router.GetMeshNetworkTask;
import com.changhong.wifimng.task.router.GetStaInfoTask;
import com.changhong.wifimng.task.router.GetWlanAccessTask;
import com.changhong.wifimng.task.router.SetWlanAccessTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.uttils.ClientIconUtils;
import com.changhong.wifimng.uttils.WifiMacUtils;

import java.util.List;

public class ClientDetailFragment extends BaseFragment<BaseBeen3<EnumPage, String, String>> implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private ImageView mIcon;
    private Switch mSwitchNetworkForbid;
    private Switch mSwitchOnlineNotice;
    private TextView mTvStengthForce;
    private TextView mTvname;
    private TextView mTv245g;
    private TextView mTvtime;
    private TextView mTvip;
    private TextView mTvmac;
    private TextView mTvup_speed;
    private TextView mTvdown_speed;

    protected StaInfo mStaInfo;
    //    private CountDownTimer mCountTimer;
    private DeviceType mDevictType;
    /**
     * 当前已连接路由器类型
     */
    private String mCurrentDeviceType;

    private String FORMAT_LINK_TIME_HH_MM;
    private String FORMAT_LINK_TIME_MM;

//    private boolean isFirst = true;
    /**
     * 为null 第一次查询  true 来自子设备，false 来自主设备
     */
    private Boolean isFromChild;

    /**
     * 判断是否是本设备
     */
    private boolean isCurrentDevice;

    private long mLastUpload, mLastDownload;
    private long mUploadSpeed, mDownloadSpeed;

    private final int FREQUENCY_REFRESH = 5000;
    private long lastRefreshTime = System.currentTimeMillis();

    /**
     * 0:未限制，1：白名单 2：黑名单
     */
    private int mCurrentEnabled;

    private boolean isFirst = true;

    private final String[] ARR_SIGNAL_INTENSITY = new String[3];

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!isResumed())
                return;
            if (msg.what == 0) {
                if (mDevictType == null) {
                    return;
                }

                if (getWifiManager().getDhcpInfo().gateway == 0) {//防止ip为0时循环操作。
                    sendEmptyMessageDelayed(0, FREQUENCY_REFRESH);
                    return;
                }

                if (mDevictType == DeviceType.PLC) {
                    doGetPlcLinkInfo();
                } else {
                    if (isFromChild == null || !isFromChild)
                        doGetStaInfo();
                    else
                        doGetMeshNetState();
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStaInfo = getArguments().getParcelable(KeyConfig.KEY_DEVICE_INFO);
        }
        if (getArguments() != null)
            mCurrentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
        else
            mCurrentDeviceType = Preferences.getInstance(mActivity).getString(KeyConfig.KEY_DEVICE_TYPE);

        mDevictType = DeviceType.getDeviceTypeFromName(mCurrentDeviceType);

        FORMAT_LINK_TIME_HH_MM = getString(R.string.format_link_time_hhmm);
        FORMAT_LINK_TIME_MM = getString(R.string.format_link_time_mm);

        isCurrentDevice = WifiMacUtils.getMac(mActivity).equalsIgnoreCase(mStaInfo.getMac());

        //设置下载速度
        if (mDevictType == DeviceType.PLC) {
            mUploadSpeed = mStaInfo.getUpload() * 1024;
            mDownloadSpeed = mStaInfo.getDownload() * 1024;
        } else {
            mUploadSpeed = mStaInfo.getDownload() - mLastUpload;
            mLastUpload = mStaInfo.getDownload();

            mDownloadSpeed = mStaInfo.getUpload() - mLastDownload;
            mLastDownload = mStaInfo.getUpload();
        }


        {//信号强度的文字
            String signal_intensity = getString(R.string.signal_intensity);
            ARR_SIGNAL_INTENSITY[0] = signal_intensity + getString(R.string.strong);
            ARR_SIGNAL_INTENSITY[1] = signal_intensity + getString(R.string.medium);
            ARR_SIGNAL_INTENSITY[2] = signal_intensity + getString(R.string.weak);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_back).setOnClickListener(this);

        mIcon = view.findViewById(R.id.icon);
        mTvStengthForce = view.findViewById(R.id.tv_strength_force);
        mTvname = view.findViewById(R.id.tv_name);
        mTv245g = view.findViewById(R.id.tv_245g);
        mTvtime = view.findViewById(R.id.tv_time);
        mTvip = view.findViewById(R.id.tv_ip);
        mTvmac = view.findViewById(R.id.tv_mac);
        mTvup_speed = view.findViewById(R.id.tv_up_speed);
        mTvdown_speed = view.findViewById(R.id.tv_down_speed);

        refreshUI();

        view.findViewById(R.id.btn_speed_limite).setOnClickListener(this);
        view.findViewById(R.id.panel_group).setOnClickListener(this);
        view.findViewById(R.id.panel_online_notice).setOnClickListener(this);
        view.findViewById(R.id.pannel_network_forbid).setOnClickListener(this);
        mSwitchOnlineNotice = view.findViewById(R.id.switch_online_notice);
        mSwitchNetworkForbid = view.findViewById(R.id.switch_network_forbid);
        mSwitchOnlineNotice.setOnCheckedChangeListener(this);
        mSwitchNetworkForbid.setOnCheckedChangeListener(this);

        if (mStaInfo.getRssi() != null) {
            refreshRssi(mStaInfo.getRssi());
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void refreshUI() {
//        if (!isResumed()) {
//            return;
//        }
        if (mStaInfo != null) {

            mIcon.setImageResource(ClientIconUtils.getClientDevcieByString(mStaInfo.getName()));

            mTv245g.setText(mStaInfo.getConnectType().getResId());
            if (TextUtils.isEmpty(mStaInfo.getName())) {
                mTvname.setText(R.string.unknown);
            } else
                mTvname.setText(mStaInfo.getName());
            {
                String linktime;
                int hour = mStaInfo.getLink_time() / 3600;
                int minute = mStaInfo.getLink_time() % 3600 / 60;
                if (hour > 0) {
                    linktime = String.format(FORMAT_LINK_TIME_HH_MM, hour, minute);
                } else {
                    linktime = String.format(FORMAT_LINK_TIME_MM, minute);
                }
                mTvtime.setText(linktime);
            }
            if (TextUtils.isEmpty(mStaInfo.getIp()))
                mTvip.setText("IP: 0.0.0.0");
            else
                mTvip.setText("IP: " + mStaInfo.getIp());
            mTvmac.setText("MAC: " + mStaInfo.getMac());

            if (mUploadSpeed > 1024 * 1024) {
                mTvup_speed.setText(String.format("%.1fMbps", mUploadSpeed / 1024f / 1024));
            } else if (mUploadSpeed > 1024) {
                mTvup_speed.setText(String.format("%.1fKbps", mUploadSpeed / 1024f));
            } else
                mTvup_speed.setText(mUploadSpeed + "bps");

            if (mDownloadSpeed > 1024 * 1024) {
                mTvdown_speed.setText(String.format("%.1fMbps", mDownloadSpeed / 1024f / 1024));
            } else if (mDownloadSpeed > 1024) {
                mTvdown_speed.setText(String.format("%.1fKbps", mDownloadSpeed / 1024f));
            } else
                mTvdown_speed.setText(mDownloadSpeed + "bps");

            // 有线设备不显示信号强度
            if (mStaInfo.getConnectType() == ConnectType.LINE)
                mTvStengthForce.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            startCountTimer();
            doGetWlanAccess();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            stopCountTimer();
        } else {
            startCountTimer();
            doGetWlanAccess();
        }
    }

    @Override
    public void onPause() {
        stopCountTimer();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        stopCountTimer();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null) {
                onFragmentLifeListener.onChanged(null);
            }
        } else if (v.getId() == R.id.pannel_network_forbid) {
            mSwitchNetworkForbid.toggle();
        } else if (v.getId() == R.id.panel_online_notice) {
            mSwitchOnlineNotice.toggle();
        } else if (v.getId() == R.id.btn_speed_limite) {
            onFragmentLifeListener.onChanged(new BaseBeen3(EnumPage.SPEED_LIMIT, mStaInfo.getMac(), mCurrentDeviceType));
        } else if (v.getId() == R.id.panel_group) {
            showToast(R.string.wait_for_develop);
        }
    }

    /**
     * 修改按钮状态，并且不响应监听
     *
     * @param button  按钮view
     * @param isCheck 是否选中
     */
    private void setCompoundButtonWithOutListener(CompoundButton button, boolean isCheck) {
        button.setOnCheckedChangeListener(null);
        button.setChecked(isCheck);
        button.setOnCheckedChangeListener(ClientDetailFragment.this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.switch_online_notice) {
            setCompoundButtonWithOutListener(buttonView, false);
            showToast(R.string.wait_for_develop);
        } else if (buttonView.getId() == R.id.switch_network_forbid) {
            if (isChecked) {
                if (mDevictType == DeviceType.PLC) {
                    if (mCurrentEnabled == 0) {
                        doSetAccess(2, false);
                    } else if (mCurrentEnabled == 1) {
                        //TODO 移除白名单，并提示从访问控制中恢复
                        doDelAccess(mStaInfo.getMac(), false);
                    } else if (mCurrentEnabled == 2) {
                        //TODO 添加到黑名单，并提示从访问控制中恢复
                        doAddAccess(mStaInfo.getName(), mStaInfo.getMac(), false);
                    }
                } else {
                    if (mCurrentEnabled == 0) {
                        //TODO 提示用户未开启黑白名单
                        setCompoundButtonWithOutListener(buttonView, false);
                        showAlert(_getString(R.string.notice_need_open_access), _getString(R.string.visit_control), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onFragmentLifeListener.onChanged(new BaseBeen3(EnumPage.WLAN_ACCESS, mStaInfo.getMac(), mCurrentDeviceType));
                            }
                        }, _getString(R.string.cancel), null, true);
                    } else if (mCurrentEnabled == 1) {
                        //TODO 移除白名单，并提示从访问控制中恢复
                        doDelAccess(mStaInfo.getMac(), false);
                    } else if (mCurrentEnabled == 2) {
                        //TODO 添加到黑名单，并提示从访问控制中恢复
                        doAddAccess(mStaInfo.getName(), mStaInfo.getMac(), false);
                    }
                }
            } else {
                if (mCurrentEnabled == 1) {
                    doAddAccess(mStaInfo.getName(), mStaInfo.getMac(), false);
                } else if (mCurrentEnabled == 2) {
                    doDelAccess(mStaInfo.getMac(), false);
                } else if (mCurrentEnabled == 0) {
                    // 不存在未开启情况
                }
            }


//            if (isChecked) {
//                askEffectImmediately(true, mStaInfo.getName(), mStaInfo.getMac());
//            } else
//                askEffectImmediately(false, mStaInfo.getName(), mStaInfo.getMac());
        }
    }

    /**
     * 刷新信号强度
     */
    private void refreshRssi(int rssi) {
        if (rssi > -50) {
            mTvStengthForce.setText(ARR_SIGNAL_INTENSITY[0]);
            mTvStengthForce.setTextColor(0xff00ff00);
        } else if (rssi > -70) {
            mTvStengthForce.setTextColor(0xFFFF8C00);
            mTvStengthForce.setText(ARR_SIGNAL_INTENSITY[1]);
        } else {
            mTvStengthForce.setText(ARR_SIGNAL_INTENSITY[2]);
            mTvStengthForce.setTextColor(0xffff0000);
        }
    }

    private void startCountTimer() {

        stopCountTimer();
        lastRefreshTime = System.currentTimeMillis();
        mHandler.sendEmptyMessage(0);
//        mCountTimer = new CountDownTimer(Integer.MAX_VALUE, FREQUNCY_REFRESH) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                if (mDevictType == null) {
//                    return;
//                }
//                if (mDevictType == DeviceType.PLC) {
//                    doGetPlcLinkInfo();
//                } else {
//                    if (isFromChild == null || !isFromChild)
//                        doGetStaInfo();
//                    else
//                        doGetMeshNetState();
//                }
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        }.start();
    }

    private void stopCountTimer() {
        stopTask(GetMeshNetworkTask.class);
        stopTask(GetStaInfoTask.class);
        stopTask(GetPLCLinkInfoTask.class);
        mHandler.removeMessages(0);
//        if (mCountTimer != null) {
//            mCountTimer.cancel();
//            mCountTimer = null;
//        }
    }

    /**
     * 获取组网设备列表
     */
    private void doGetMeshNetState() {
        addTask(
                new GetMeshNetworkTask().execute(getGateway(), getCookie(), new TaskListener<List<ListInfo>>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
//                        showProgressDialog(_getString(R.string.note_mesh_info_request), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result == TaskResult.AUTH_ERROR) {
                            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(BaseWifiActivtiy.ACTION_UNAUTHORIZED));
                            return;
                        } else if (task.getException() instanceof java.net.ConnectException) {
                            showAlert(getTaskError(task, R.string.interaction_failed), _getString(R.string._exit), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    onFragmentLifeListener.onChanged(null);
                                }
                            });
                        } else {
                            if (result != TaskResult.OK) {
                                showTaskError(task, R.string.interaction_failed);
                                if (isFirst)
                                    onFragmentLifeListener.onChanged(null);
                            }
                            mHandler.removeMessages(0);
                            mHandler.sendEmptyMessageDelayed(0, FREQUENCY_REFRESH);
                        }
                        isFirst = false;
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, List<ListInfo> param) {
                        if (param != null && !param.isEmpty()) {
                            for (ListInfo listInfo : param) {
                                for (StaInfo staInfo : listInfo.getSta_info()) {
                                    if (staInfo.getMac() == null)
                                        continue;

                                    if (staInfo.getMac().equals(mStaInfo.getMac())) {
                                        mStaInfo = staInfo;
                                        //计算速度
                                        calculationSpeed();
                                        isFromChild = true;
                                        refreshUI();
                                        if (isCurrentDevice)
                                            refreshRssi(getWifiManager().getConnectionInfo().getRssi());
                                        return;
                                    }
                                }
                            }
                            alertDeviceOffline();
                        } else
                            //当组网的信息没获取到信息时，执行下面的
                            doGetStaInfo();
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doGetStaInfo() {
        addTask(
                new GetStaInfoTask().execute(getGateway(), getCookie(), new TaskListener<List<StaInfo>>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
//                        showProgressDialog(_getString(R.string.downloading), true, new DialogInterface.OnCancelListener() {
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                task.cancel(true);
//                                onFragmentLifeListener.onChanged(null);
//                            }
//                        });
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result == TaskResult.AUTH_ERROR) {
                            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(BaseWifiActivtiy.ACTION_UNAUTHORIZED));
                            return;
                        } else if (task.getException() instanceof java.net.ConnectException) {
                            showAlert(getTaskError(task, R.string.interaction_failed), _getString(R.string._exit), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    onFragmentLifeListener.onChanged(null);
                                }
                            });
                        } else {
                            if (result != TaskResult.OK) {
                                showTaskError(task, R.string.interaction_failed);
                                if (isFirst)
                                    onFragmentLifeListener.onChanged(null);
                            }
                            mHandler.removeMessages(0);
                            mHandler.sendEmptyMessageDelayed(0, FREQUENCY_REFRESH);
                        }
                        isFirst = false;
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, List<StaInfo> param) {
                        if (param != null && !param.isEmpty()) {
                            for (StaInfo staInfo : param) {
                                if (staInfo.getMac() == null)
                                    continue;

                                if (staInfo.getMac().equals(mStaInfo.getMac())) {
                                    mStaInfo = staInfo;

                                    calculationSpeed();
//                                        showToast(String.format("%d - %d \n %d - %d", mLastUpload, mLastDownload, mUploadSpeed, mDownloadSpeed));

                                    isFromChild = false;
                                    refreshUI();
                                    if (isCurrentDevice)
                                        refreshRssi(getWifiManager().getConnectionInfo().getRssi());
                                    return;
                                }
                            }
                            if (isFromChild == null)
                                doGetMeshNetState();
                            else
                                alertDeviceOffline();
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
     * 当sta信息中不再含有此设备时，中断刷新，并提示退出
     */
    private void alertDeviceOffline() {
        stopCountTimer();
        SpannableString spannableString = new SpannableString("设备" + mStaInfo.getName() + "已离线！");
        spannableString.setSpan(new RelativeSizeSpan(1.1f), 2, 2 + mStaInfo.getName().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.RED), 2, 2 + mStaInfo.getName().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        showAlert(spannableString, "退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onFragmentLifeListener.onChanged(null);
            }
        });
    }

    /**
     * 获取黑名单
     */
    private void doGetWlanAccess() {
        addTask(
                new GetWlanAccessTask().execute(getGateway(), mCurrentDeviceType, getCookie(), new TaskListener<ResponseAllBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
//                        if (isFirst) {
//                            isFirst = false;
//                            showProgressDialog(_getString(R.string.downloading), true, new DialogInterface.OnCancelListener() {
//                                @Override
//                                public void onCancel(DialogInterface dialog) {
//                                    task.cancel(true);
//                                    onFragmentLifeListener.onChanged(null);
//                                }
//                            });
//                        }
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, ResponseAllBeen param) {
                        if (param != null) {
                            mCurrentEnabled = param.getEnabled();
                            refreshForbidenState(param);
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
     * 刷新【禁止上网】状态
     *
     * @param param
     */
    private void refreshForbidenState(ResponseAllBeen param) {
        boolean isForbiden = false;
        List<Level2Been> list = param.getList();
        if (param.getEnabled() == 1 && !isMacInAccessList(list, mStaInfo.getMac())) {
            //白名单情况下，如果列表中没有此设备，即被禁止
            isForbiden = true;
        } else if (param.getEnabled() == 2 && isMacInAccessList(list, mStaInfo.getMac())) {
            //黑名单情况下，如果列表中有此设备，即被禁止
            isForbiden = true;
        }

        setCompoundButtonWithOutListener(mSwitchNetworkForbid, isForbiden);
    }

    /**
     * 询问删除是否立即执行
     */
    private void askEffectImmediately(final boolean isAdd, final String name, final String mac) {
        showAlert(_getString(R.string.notice_ask_execute_immediately), _getString(R.string.save_and_execute), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isAdd)
                    doAddAccess(name, mac, true);
                else
                    doDelAccess(mac, true);
            }
        }, _getString(R.string.save_only), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isAdd)
                    doAddAccess(name, mac, false);
                else
                    doDelAccess(mac, false);
            }
        }, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setCompoundButtonWithOutListener(mSwitchNetworkForbid, !isAdd);
            }
        });

    }

    /**
     * 添加黑名单
     *
     * @param name                设备名称
     * @param mac                 设备mac
     * @param isEffectImmediately 是否立即生效
     */
    private void doAddAccess(String name, String mac, boolean isEffectImmediately) {
        addTask(
                new AddWlanAccessTask().execute(getGateway(), mCurrentDeviceType, name, mac, isEffectImmediately, getCookie(), new TaskListener<Boolean>() {
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
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            setCompoundButtonWithOutListener(mSwitchNetworkForbid, false);
                        } else {
                            showToast(R.string.commit_completed);
                            doGetWlanAccess();
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

    /**
     * 删除黑名单
     *
     * @param mac                 设备mac
     * @param isEffectImmediately 是否立即生效
     */
    private void doDelAccess(String mac, boolean isEffectImmediately) {
        addTask(new DeleteWlanAccessTask().execute(getGateway(), mCurrentDeviceType, mac, isEffectImmediately, getCookie(), new TaskListener<Boolean>() {
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
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            setCompoundButtonWithOutListener(mSwitchNetworkForbid, true);
                        } else {
                            showToast(R.string.commit_completed);
                            doGetWlanAccess();
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

    private void doGetPlcLinkInfo() {
        addTask(
                new GetPLCLinkInfoTask().execute(getGateway(), new TaskListener<PLCInfo>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
//                        showProgressDialog(_getString(R.string.downloading), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            if (task.getException() instanceof java.net.ConnectException)
                                showAlert(getTaskError(task, R.string.interaction_failed), _getString(R.string._exit), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        onFragmentLifeListener.onChanged(null);
                                    }
                                });
                            else {
                                showTaskError(task, R.string.interaction_failed);
                                if (isFirst)
                                    onFragmentLifeListener.onChanged(null);
                            }
                        }
                        mHandler.removeMessages(0);
                        mHandler.sendEmptyMessageDelayed(0, FREQUENCY_REFRESH);

                        isFirst = false;
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, PLCInfo param) {
                        if (param != null && !param.getDev_info().isEmpty()) {
                            for (PLCInfo.Dev_Info dev_info : param.getDev_info()) {
                                if (dev_info.getMac() == null)
                                    continue;

                                if (dev_info.getMac().equals(mStaInfo.getMac())) {
                                    mUploadSpeed = dev_info.getTx_speed() * 1024;
                                    mDownloadSpeed = dev_info.getRx_speed() * 1024;
                                    refreshUI();
                                    if (dev_info.getPower_level() != null) {
                                        mTvStengthForce.setVisibility(View.VISIBLE);
                                        refreshRssi(dev_info.getPower_level());
                                    }
                                    return;
                                }
                            }
                            alertDeviceOffline();
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
     * @return 判断名单是否包含此设备
     */
    protected boolean isMacInAccessList(List<Level2Been> list, String mac) {
        if (list != null && !list.isEmpty()) {
            for (Level2Been level2Been : list) {
                if (level2Been.getMac().equalsIgnoreCase(mac)) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * @param enable              限制状态：0、未开启； 1、白名单 2、黑名单
     * @param isEffectImmediately 是否立即生效
     */
    private void doSetAccess(final int enable, boolean isEffectImmediately) {
        addTask(
                new SetWlanAccessTask().execute(getGateway(), mCurrentDeviceType, enable, mStaInfo, isEffectImmediately, getCookie(), new TaskListener<Boolean>() {
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
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        } else {//保存完成退出
                            showToast(R.string.commit_completed);
                            if (enable == 2 && mDevictType == DeviceType.PLC) {
                                doAddAccess(mStaInfo.getName(), mStaInfo.getMac(), true);
                            } else
                                doGetWlanAccess();
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

    /**
     * 计算速度
     */
    private void calculationSpeed() {
        long costTime = System.currentTimeMillis() - lastRefreshTime;
        lastRefreshTime = System.currentTimeMillis();
        mUploadSpeed = mStaInfo.getDownload() - mLastUpload;
        mUploadSpeed = Math.max(0, mUploadSpeed);
        if (mUploadSpeed < 0)
            mUploadSpeed = 0;
        mLastUpload = mStaInfo.getDownload();
        mUploadSpeed *= 1000;
        mUploadSpeed /= costTime;

        mDownloadSpeed = mStaInfo.getUpload() - mLastDownload;
        mDownloadSpeed = Math.max(0, mDownloadSpeed);
        if (mDownloadSpeed < 0)
            mDownloadSpeed = 0;
        mLastDownload = mStaInfo.getUpload();
        mDownloadSpeed *= 1000;
        mDownloadSpeed /= costTime;
    }
}
