package com.changhong.wifimng.ui.fragment.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
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
import com.changhong.wifimng.task.router.GetMeshNetworkTask;
import com.changhong.wifimng.task.router.GetStaInfoTask;
import com.changhong.wifimng.task.router.GetWlanAccessTask;
import com.changhong.wifimng.task.router.SetWlanAccessTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.fragment.EnumPage;
import com.changhong.wifimng.uttils.CommUtil;
import com.changhong.wifimng.uttils.WifiMacUtils;

import java.util.ArrayList;
import java.util.List;

public class WlanAccessFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

    private Switch mSwitchMain;
    private TableLayout mTableOnlineDevice;
    private EditText mEtName;
    private EditText mEtMac;
    private Spinner mSpinnerWhiteOrBlack;
    private View mVHorizontalLine;
    private TextView mTvStatus;

    /**
     * 当前已连接路由器类型
     */
    protected DeviceType mDeviceType;

    /**
     * 名单
     */
    private List<Level2Been> mList;
    /**
     * 当前在线设备
     **/
    private List<StaInfo> mCurrentLinkDevice;
    private Integer mCurrentEnabled;
    private TextView mTvIntroduce;
    private View mBtnSave;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String currentDeviceType;
        if (getArguments() != null)
            currentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
        else
            currentDeviceType = Preferences.getInstance(mActivity).getString(KeyConfig.KEY_DEVICE_TYPE);

        mDeviceType = DeviceType.getDeviceTypeFromName(currentDeviceType);
        if (mDeviceType == null) {
            showToast(R.string.data_error);
            onFragmentLifeListener.onChanged(null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visit_control, null, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        marth_parent(view);//填充满屏幕

        view.findViewById(R.id.btn_back).setOnClickListener(this);
        mBtnSave = view.findViewById(R.id.btn_save);
        mBtnSave.setOnClickListener(this);

        mVHorizontalLine = view.findViewById(R.id.view_horizontal_line);

        mSwitchMain = view.findViewById(R.id.switch_01);
        mSwitchMain.setOnCheckedChangeListener(this);
        view.findViewById(R.id.panel_switch01).setOnClickListener(this);

        mTableOnlineDevice = view.findViewById(R.id.panel_online_device_table);
        mSpinnerWhiteOrBlack = view.findViewById(R.id.spinner_white_or_black);
        mSpinnerWhiteOrBlack.setOnItemSelectedListener(this);

        mEtName = view.findViewById(R.id.et_device_name);
        mEtMac = view.findViewById(R.id.et_device_mac_addr);
        mTvStatus = view.findViewById(R.id.tv_status);

        view.findViewById(R.id.btn_add).setOnClickListener(this);
        view.findViewById(R.id.btn_list).setOnClickListener(this);

        mTvIntroduce = view.findViewById(R.id.tv_introduce);
        if (mDeviceType == DeviceType.PLC) {
            mTvIntroduce.setText(R.string.notice_white_black_list_plc);
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        doGetWlanAccess();
        if (mDeviceType == DeviceType.BWR) {
            doGetStaInfo(true);
        } else if (mDeviceType == DeviceType.R2s) {
            doGetStaInfo(false);
        } else if (mDeviceType == DeviceType.PLC) {
            doGetPlcInfo();
        }
    }

    /**
     * @param enable 状态开关后刷新界面，显示或隐藏修改部分数据
     */
    private void setEditEnable(boolean enable) {
        mTvStatus.setText(enable ? R.string.open : R.string.close);

        ((View) mSpinnerWhiteOrBlack.getParent()).setVisibility(enable ? View.VISIBLE : View.GONE);
        mTvIntroduce.setVisibility(enable ? View.VISIBLE : View.GONE);

        ViewGroup viewGroup = (ViewGroup) mTvIntroduce.getParent();
        int index = viewGroup.indexOfChild(mTvIntroduce);
        for (int i = index + 1; i < viewGroup.getChildCount(); i++) {
            viewGroup.getChildAt(i).setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(null);
        } else if (v.getId() == R.id.panel_switch01) {
            mSwitchMain.toggle();
        } else if (v.getId() == R.id.btn_save) {
            askSetEffectImmediately(getEnabled());
        } else if (v.getId() == R.id.btn_list) {//查看列表
            onFragmentLifeListener.onChanged(EnumPage.ACCESS_LIST);
        } else if (v.getId() == R.id.btn_add) {
            final String name = mEtName.getText().toString();
            if (TextUtils.isEmpty(name)) {
                mEtName.requestFocus();
                showToast(R.string.notice_device_name_empty);
                return;
            }
            final String mac = mEtMac.getText().toString();
            if (TextUtils.isEmpty(mac)) {
                mEtMac.requestFocus();
                showToast(R.string.notice_mac_empty);
                return;
            }
            if (mCurrentEnabled == 2 && mac.equalsIgnoreCase(getMyDeviceMac())) {
                showAlert(_getString(R.string.notice_add_my_device_2_blacklist), _getString(R.string._continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        askAddEffectImmediately(name, mac);
                    }
                }, _getString(R.string.cancel), null, true);
            } else
                askAddEffectImmediately(name, mac);
        } else if (v.getId() == R.id.icon && v.getTag() != null) {
            if (v.getTag() instanceof StaInfo) {
                final StaInfo staInfo = (StaInfo) v.getTag();
                if (mCurrentEnabled == 2 && staInfo.getMac().equalsIgnoreCase(getMyDeviceMac())) {
                    showAlert(_getString(R.string.notice_add_my_device_2_blacklist), _getString(R.string._continue), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            askAddEffectImmediately(staInfo.getName(), staInfo.getMac());
                        }
                    }, _getString(R.string.cancel), null, true);
                } else
                    askAddEffectImmediately(staInfo.getName(), staInfo.getMac());
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.switch_01) {
            setEditEnable(isChecked);
            if (mCurrentEnabled != getEnabled()) {
                if (mBtnSave.getVisibility() != View.VISIBLE)
                    mBtnSave.setVisibility(View.VISIBLE);

                mBtnSave.setEnabled(true);
            } else
                mBtnSave.setEnabled(false);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (mCurrentEnabled != getEnabled()) {
            if (mBtnSave.getVisibility() != View.VISIBLE)
                mBtnSave.setVisibility(View.VISIBLE);
            mBtnSave.setEnabled(true);
        } else
            mBtnSave.setEnabled(false);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * 获取黑名单
     */
    protected void doGetWlanAccess() {
        addTask(
                new GetWlanAccessTask().execute(getGateway(), mDeviceType.getName(), getCookie(), new TaskListener<ResponseAllBeen>() {
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
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, ResponseAllBeen param) {
                        if (param != null) {
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
     * 询问添加是否立即执行
     */
    private void askAddEffectImmediately(final String name, final String mac) {

        if (isMacInAccessList(mac)) {
            showAlert(_getString(R.string.notice_device_in_list), _getString(R.string.confirm), null);
            return;
        }

        if (mDeviceType == DeviceType.PLC) {
            doAddAccess(name, mac, true);
        } else
            new AlertDialog.Builder(mActivity).setMessage(_getString(R.string.notice_ask_add_access_list_then_execute_immediately)).setPositiveButton(_getString(R.string.save_and_execute), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doAddAccess(name, mac, true);
                }
            }).setNegativeButton(_getString(R.string.save_only), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doAddAccess(name, mac, false);
                }
            }).create().show();
    }

    protected void doAddAccess(final String name, final String mac, boolean isEffectImmediately) {
        addTask(
                new AddWlanAccessTask().execute(getGateway(), mDeviceType.getName(), name, mac, isEffectImmediately, getCookie(), new TaskListener<Boolean>() {
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
                        } else {
//                            doGetWlanAccess();
                            if (mCurrentLinkDevice == null)
                                mCurrentLinkDevice = new ArrayList<>();
                            Level2Been stainfo = new Level2Been();
                            stainfo.setMac(mac);
                            stainfo.setName(name);
                            mList.add(stainfo);
                            switch (mCurrentEnabled) {
                                case 1:
                                    showToast(R.string.notice_complete_add_2_white_list);
                                    break;
                                case 2:
                                    showToast(R.string.notice_complete_add_2_black_list);
                                    break;
                                default:
                                    showToast(R.string.notice_complete_add_2_list);
                            }
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
     * @return 获取enabled的值 ，0 关 ，1、白名单 、2 黑名单
     */
    private int getEnabled() {
        if (!mSwitchMain.isChecked())
            return 0;
        return mSpinnerWhiteOrBlack.getSelectedItemPosition() + 1;
    }

    /**
     * @return 判断名单是否包含此设备
     */
    protected boolean isCurrentMacInAccessList() {
        String mac = getMyDeviceMac();
        if (mList != null && !mList.isEmpty()) {
            for (Level2Been level2Been : mList) {
                if (level2Been.getMac().equalsIgnoreCase(mac)) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * @return 判断名单是否包含此设备
     */
    protected boolean isMacInAccessList(String mac) {
        if (mList != null && !mList.isEmpty()) {
            for (Level2Been level2Been : mList) {
                if (level2Been.getMac().equalsIgnoreCase(mac)) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * 询问设置是否立即执行
     *
     * @param enabled
     */
    private void askSetEffectImmediately(final int enabled) {
        if (mCurrentEnabled == enabled) {//未修改的提示
            showToast(R.string.commit_completed);
            onFragmentLifeListener.onChanged(null);
            return;
        }
        if (mDeviceType == DeviceType.PLC) {
            if (enabled == 2)
                showAlert(_getString(R.string.notice_clear_blist_after_execute), _getString(R.string._continue),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doSetAccess(enabled, true);
                            }
                        }, _getString(R.string.cancel), null, true
                );
            else
                doSetAccess(enabled, true);
            return;
        }

// mesh router
        if (enabled == 2) {
            if (mCurrentEnabled == 1) {
                showAlert(_getString(R.string.notice_whitelist_2_blacklist), _getString(R.string.confirm), null);
                return;
            } else if (isCurrentMacInAccessList()) {
                showAlert(_getString(R.string.notice_open_blacklist), _getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onFragmentLifeListener.onChanged(EnumPage.ACCESS_LIST);
                    }
                });
                return;
            }
        } else if (enabled == 1) {
            if (mCurrentEnabled == 2) {
                showAlert(getString(R.string.notice_blacklist_2_whitelist), getString(R.string.confirm), null);
                return;
            } else if (!isCurrentMacInAccessList()) {//当要开启白名单的时候，需要先将当前设备加入到列表中
                showAlert(_getString(R.string.notice_open_whitelist), _getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                return;
            }
        }
        CommUtil.closeIME(mActivity);
        showAlert(_getString(R.string.notice_ask_execute_immediately), _getString(R.string.save_and_execute),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        doSetAccess(enabled, true);
                    }
                }, _getString(R.string.save_only), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        doSetAccess(enabled, false);
                    }
                }
                , true
        );
    }

    private void doSetAccess(int enable, boolean isEffectImmediately) {
        StaInfo staInfo = null;
        if (mDeviceType == DeviceType.PLC) {
            staInfo = getCurrentDevice(getMyDeviceMac());
            if (staInfo == null) {
                staInfo = new StaInfo();
                staInfo.setMac(getMyDeviceMac());
                staInfo.setName(Build.MODEL);
            }
        }

        addTask(
                new SetWlanAccessTask().execute(getGateway(), mDeviceType.getName(), enable, staInfo, isEffectImmediately, getCookie(), new TaskListener<Boolean>() {
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
                            onFragmentLifeListener.onChanged(null);
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

    private void doGetStaInfo(final boolean isMesh) {
        addTask(
                new GetStaInfoTask().execute(getGateway(), getCookie(), new TaskListener<List<StaInfo>>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(_getString(R.string.downloading), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        } else if (isMesh)
                            doGetMeshNetState();
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, List<StaInfo> param) {
                        if (param != null) {
                            mCurrentLinkDevice = param;
                            refreshTableViewOnline(param);
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
     * 更新开关和黑白名单
     *
     * @param param
     */
    private void refreshView(ResponseAllBeen param) {
        mCurrentEnabled = param.getEnabled();
        mList = param.getList();
        mSwitchMain.setOnCheckedChangeListener(null);
        mSwitchMain.setChecked(param.getEnabled() != 0);
        mSwitchMain.setOnCheckedChangeListener(this);
        Log.d(getClass().getSimpleName(), "====~ " + (isCurrentMacInAccessList() ? "mac in list" : "mac not in list"));
        mSpinnerWhiteOrBlack.setOnItemSelectedListener(null);
        if (param.getEnabled() == 1) {
            mSpinnerWhiteOrBlack.setSelection(0);
        } else if (param.getEnabled() == 2)
            mSpinnerWhiteOrBlack.setSelection(1);
        mSpinnerWhiteOrBlack.setOnItemSelectedListener(this);

        setEditEnable(mSwitchMain.isChecked());
    }

    /**
     * 更新在线列表
     *
     * @param param
     */
    private void refreshTableViewOnline(List<StaInfo> param) {
        while (mTableOnlineDevice.getChildCount() > 1)
            mTableOnlineDevice.removeViewAt(mTableOnlineDevice.getChildCount() - 1);

        if (param != null && !param.isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            for (int i = 0; i < param.size(); i++) {
                StaInfo staInfo = param.get(i);
                View view = inflater.inflate(R.layout.item_device_table_row, null, false);

                TextView tvIndex = view.findViewById(R.id.text1);
                TextView tvName = view.findViewById(R.id.text2);
                TextView tvMac = view.findViewById(R.id.text3);
                View btn = view.findViewById(R.id.icon);
                tvIndex.setText(String.valueOf(i + 1));
                tvName.setText(staInfo.getName());
                tvMac.setText(staInfo.getMac());
                btn.setTag(staInfo);
                btn.setOnClickListener(this);
                mTableOnlineDevice.addView(view, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            }
        }
        mTableOnlineDevice.postInvalidate();
    }

    private void doGetPlcInfo() {
        addTask(
                new GetPLCLinkInfoTask().execute(getGateway(), new TaskListener<PLCInfo>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(_getString(R.string.downloading), false, null);
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
                        if (param != null) {
                            List<StaInfo> list = new ArrayList<>();
                            for (PLCInfo.Dev_Info dev_info : param.getDev_info()) {
                                if (dev_info.getAccess_port() != 0)
                                    list.add(dev_info.turn2StaInfo());
                            }
                            mCurrentLinkDevice = list;
                            refreshTableViewOnline(list);
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
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, List<ListInfo> param) {
                        if (param != null) {
                            if (!param.isEmpty()) {
                                for (ListInfo listInfo : param) {
                                    for (StaInfo staInfo : listInfo.getSta_info()) {
                                        if (staInfo.getMac() != null
                                                && !isInCache(staInfo.getMac()))
                                            mCurrentLinkDevice.add(staInfo);
                                    }
                                    break;
                                }
                                refreshTableViewOnline(mCurrentLinkDevice);
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

    private boolean isInCache(String mac) {
        if (mac == null
                || mCurrentLinkDevice == null)
            return false;
        for (StaInfo staInfo : mCurrentLinkDevice) {
            if (mac.equalsIgnoreCase(staInfo.getMac())) {
                return true;
            }
        }
        return false;
    }

    private StaInfo getCurrentDevice(String mac) {
        for (StaInfo staInfo : mCurrentLinkDevice) {
            if (staInfo.getMac() != null && staInfo.getMac().equalsIgnoreCase(mac))
                return staInfo;
        }
        return null;
    }

    private String getMyDeviceMac() {
        return WifiMacUtils.getMac(mActivity);
    }
}
