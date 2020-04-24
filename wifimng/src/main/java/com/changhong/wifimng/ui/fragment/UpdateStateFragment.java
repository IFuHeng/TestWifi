package com.changhong.wifimng.ui.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.http.been.DeviceDetailBeen;
import com.changhong.wifimng.http.been.MeshStateBeen;
import com.changhong.wifimng.http.been.PLCModemStateBeen;
import com.changhong.wifimng.http.been.UpgradeBeen;
import com.changhong.wifimng.http.task.CheckUpdateTask;
import com.changhong.wifimng.http.task.DeviceDetailTask;
import com.changhong.wifimng.http.task.PushUpdateTask;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.uttils.WifiMacUtils;
import com.google.gson.Gson;

public class UpdateStateFragment extends BaseFragment<Boolean> implements View.OnClickListener {

    public static final int STATE_CHECKING = 0;
    public static final int STATE_CHECK_COMPLETED = 1;
    public static final int STATE_UPDATING = 2;
    public static final int STATE_UPDATE_CHOICE = 3;
    public static final int STATE_UPDATE_COMPLETED = 4;

    private ImageView mIcon;
    private TextView mTvTitle;//标题
    private TextView mTvTitle2;
    private TextView mTvState;//状态
    private TextView mTvUpdate;//升级中文字
    private TextView mTvUpdateWarmTips;//温馨提示
    private View mViewOldVersion;
    private View mViewNewVersion;
    private TextView mTvUpdateInfo;
    private Button mBtnNext;

    private int mState;
    private ViewSwitcher mViewSwitcher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.btn_back2).setOnClickListener(this);

        mViewSwitcher = view.findViewById(R.id.viewSwitcher01);

        mIcon = view.findViewById(R.id.image);
        mTvTitle = view.findViewById(R.id.tv_title);
        mTvTitle2 = view.findViewById(R.id.tv_title2);
        mTvState = view.findViewById(R.id.tv_state);
        mTvUpdate = view.findViewById(R.id.tv_update_waiting);
        mTvUpdateWarmTips = view.findViewById(R.id.tv_update_notice);

        mViewOldVersion = view.findViewById(R.id.panel_old_version);
        mViewNewVersion = view.findViewById(R.id.panel_new_version);
        mTvUpdateInfo = view.findViewById(R.id.tv_update_update);
        mBtnNext = view.findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);

        ((TextView) mViewOldVersion.findViewById(R.id.tv_version)).setText(getArguments().getString(KeyConfig.KEY_FIRMWARE_VERSION));//版本号

        doGetDeviceDetailInfoFromCloud();
    }

    private void refreshTitleBarUI() {
        View parent = (View) mTvTitle.getParent();
        View parent2 = (View) mTvTitle2.getParent();
        switch (mState) {
            case STATE_UPDATE_CHOICE:
            case STATE_UPDATE_COMPLETED:
                if (parent2.getVisibility() != View.VISIBLE) {
                    parent2.setVisibility(View.VISIBLE);
                    parent.setVisibility(View.GONE);
                    mTvTitle2.setText(R.string.rom_update);
                }
                break;
            case STATE_CHECKING:
                if (parent.getVisibility() != View.VISIBLE) {
                    parent.setVisibility(View.VISIBLE);
                    parent2.setVisibility(View.GONE);
                }
                mTvTitle.setText(R.string.check_update);
                mIcon.setImageResource(R.drawable.ic_check_update);
                break;
            case STATE_CHECK_COMPLETED:
                if (parent.getVisibility() != View.VISIBLE) {
                    parent.setVisibility(View.VISIBLE);
                    parent2.setVisibility(View.GONE);
                }
                mTvTitle.setText(R.string.check_update);
                mIcon.setImageResource(R.drawable.ic_update_no_need);
                break;
            case STATE_UPDATING:
                if (parent.getVisibility() != View.VISIBLE) {
                    parent.setVisibility(View.VISIBLE);
                    parent2.setVisibility(View.GONE);
                }
                mTvTitle.setText(R.string.rom_update);
                mIcon.setImageResource(R.drawable.ic_updating);
                break;
            default:

        }
    }

    private void refreshBodyUI() {
        switch (mState) {
            case STATE_UPDATE_CHOICE:
                if (mViewSwitcher.getDisplayedChild() == 0)
                    mViewSwitcher.showNext();
                mTvUpdateInfo.setGravity(Gravity.NO_GRAVITY);
                mTvUpdateInfo.setText(mTvUpdateInfo.getTag().toString());
                mBtnNext.setText(R.string.one_key_update);
                break;
            case STATE_UPDATE_COMPLETED:
                if (mViewSwitcher.getDisplayedChild() == 0)
                    mViewSwitcher.showNext();
                mTvUpdateInfo.setGravity(Gravity.CENTER);
                mTvUpdateInfo.setText(R.string.update_completed);
                mBtnNext.setText(R.string.complete);
            case STATE_CHECKING:
                if (mViewSwitcher.getDisplayedChild() != 0)
                    mViewSwitcher.showPrevious();
                mTvState.setText(R.string.checking_update_now);
                mTvState.setVisibility(View.VISIBLE);
                mTvUpdateWarmTips.setVisibility(View.GONE);
                mTvUpdate.setVisibility(View.GONE);
                break;
            case STATE_CHECK_COMPLETED:
                if (mViewSwitcher.getDisplayedChild() != 0)
                    mViewSwitcher.showPrevious();
                mTvState.setVisibility(View.VISIBLE);
                mTvState.setText(R.string.is_last_version);
                mTvUpdateWarmTips.setVisibility(View.GONE);
                mTvUpdate.setVisibility(View.GONE);
                break;
            case STATE_UPDATING:
                if (mViewSwitcher.getDisplayedChild() != 0)
                    mViewSwitcher.showPrevious();
                mTvState.setVisibility(View.GONE);
                mTvUpdateWarmTips.setVisibility(View.VISIBLE);
                mTvUpdate.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_in);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setRepeatCount(Animation.INFINITE);
                mTvUpdate.startAnimation(animation);
                break;
            default:

        }
    }

    private void refreshUI() {
        refreshTitleBarUI();
        refreshBodyUI();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back
                || v.getId() == R.id.btn_back2) {
            if (onFragmentLifeListener != null) {
                onFragmentLifeListener.onChanged(null);
            }
        } else if (v.getId() == R.id.btn_next) {
            switch (mState) {
                case STATE_UPDATE_CHOICE:
                    doPushUpdateInfo();
                    break;
                case STATE_UPDATE_COMPLETED:
                    onFragmentLifeListener.onChanged(null);
                    break;
            }
        }
    }

    private void doChechUpgrade() {
        addTask(
                new CheckUpdateTask().execute(mInfoFromApp, new TaskListener<UpgradeBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        } else {
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, UpgradeBeen param) {
                        if (param == null || param.getIsUpgrade() == 0) {
                            mState = STATE_CHECK_COMPLETED;
                            refreshUI();
                            return;
                        }

                        mState = STATE_UPDATE_CHOICE;
                        TextView tv_version = mViewNewVersion.findViewById(R.id.tv_version);
                        tv_version.setText(param.getUpgradeVersion());
                        mTvUpdateInfo.setTag(param.getComments());
                        refreshUI();
                    }

                    @Override
                    public void onCancelled(GenericTask task) {

                    }
                })
        );
    }

    /**
     * 从云端获取设备详细信息
     */
    private void doGetDeviceDetailInfoFromCloud() {
        addTask(
                new DeviceDetailTask().execute(mInfoFromApp, new TaskListener<DeviceDetailBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        } else
                            doChechUpgrade();
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, DeviceDetailBeen param) {
                        if (param == null)
                            return;
                        if (param != null) {
                            if (TextUtils.isEmpty(param.getState())) {
                                alertNoStateInfoAndExist();
                                return;
                            }
                            DeviceType deviceType = DeviceType.getDeviceTypeByCloudCode(param.getDeviceType());
                            if (!TextUtils.isEmpty(param.getState())) {
                                if (deviceType == DeviceType.BWR) {// 展示组网信息，包含子设备
                                    analysisMeshState(param.getState());
                                } else if (deviceType == DeviceType.R2s) {// 展示组网信息，包含子设备
                                    analysisRouterState(param.getState());
                                }
                                // 展示电力猫子设备
                                else if (deviceType == DeviceType.PLC) {
                                    analysisPlcState(param.getState());
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

    private void doPushUpdateInfo() {
        addTask(
                new PushUpdateTask().execute(mInfoFromApp, new TaskListener() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), true, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        } else {
                            mState = STATE_UPDATING;
                            refreshUI();
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
     * 提示设备离线，并退出
     */
    private void alertNoStateInfoAndExist() {
        showAlert(_getString(R.string.notice_devcie_offline), _getString(R.string._exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(BaseWifiActivtiy.ACTION_EXIT_APP));
            }
        });
    }

    private void analysisMeshState(String state) {
        MeshStateBeen been = new Gson().fromJson(state, MeshStateBeen.class);
        setVersionUI(mViewOldVersion, been.getStateValue().getSsid(), been.getStateValue().getIp(), WifiMacUtils.macShownFormat(mInfoFromApp.getMac()), been.getStateValue().getVersion(), false);
        setVersionUI(mViewNewVersion, been.getStateValue().getSsid(), been.getStateValue().getIp(), WifiMacUtils.macShownFormat(mInfoFromApp.getMac()), been.getStateValue().getVersion(), true);
    }

    private void analysisRouterState(String state) {
        analysisMeshState(state);
    }

    private void analysisPlcState(String state) {
        PLCModemStateBeen been = new Gson().fromJson(state, PLCModemStateBeen.class);
        setVersionUI(mViewOldVersion, been.getStateValue().getSsid(), been.getStateValue().getIpv4_addr(), WifiMacUtils.macShownFormat(mInfoFromApp.getMac()), been.getStateValue().getSw_ver(), false);
        setVersionUI(mViewNewVersion, been.getStateValue().getSsid(), been.getStateValue().getIpv4_addr(), WifiMacUtils.macShownFormat(mInfoFromApp.getMac()), been.getStateValue().getSw_ver(), true);
    }

    private void setVersionUI(View container, String ssid, String ip, String mac, String firmwareVersion, boolean signNew) {
        ImageView icon = container.findViewById(R.id.icon);
        icon.setImageResource(DeviceType.getDeviceTypeFromName(mInfoFromApp.getDeviceType()).getThumbResId());
        TextView text_type = container.findViewById(R.id.tv_device_type);
        text_type.setText(ssid);
        TextView tv_ip = container.findViewById(R.id.tv_ip);
        tv_ip.setText(ip);
        TextView tv_mac = container.findViewById(R.id.tv_mac);
        tv_mac.setText(mac);
        TextView tv_version = container.findViewById(R.id.tv_version);
        tv_version.setText(firmwareVersion);
        if (signNew)
            tv_version.setTextColor(_getResources().getColor(R.color.colorAccent));
        else
            tv_version.setTextColor(_getResources().getColor(R.color.textColorSecondary));
    }
}
