package com.changhong.wifimng.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.DeviceItem;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.mesh.ListInfo;
import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.been.sys.SettingResponseAllBeen;
import com.changhong.wifimng.http.been.DeviceDetailBeen;
import com.changhong.wifimng.http.been.Group;
import com.changhong.wifimng.http.been.MeshStateBeen;
import com.changhong.wifimng.http.been.PLCModemStateBeen;
import com.changhong.wifimng.http.task.DeviceDetailAndGroupTask;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.GetDeviceTypeTask;
import com.changhong.wifimng.task.GetRouterInfoTask;
import com.changhong.wifimng.task.LoginTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.GetPLCInfoTask;
import com.changhong.wifimng.task.CheckWizardGuidTask;
import com.changhong.wifimng.task.router.GetMeshNetworkTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.activity.WifiHomeActivity;
import com.changhong.wifimng.ui.activity.WizardSettingActivity;
import com.changhong.wifimng.ui.view.CustomHorizontalScrollView;
import com.changhong.wifimng.ui.view.VerticalSubordinateEffectView;
import com.google.gson.Gson;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class WifiHomeFragment extends BaseFragment<BaseBeen<EnumPage, Object>> implements View.OnClickListener {

    private View mBtnSetting;
    /**
     * 效果线条
     */
    private VerticalSubordinateEffectView mVSubordinateEffect;

    private View mVVerticalLine;
    private View mTvInternet;
    /**
     * 主设备信息
     */
    private View mVMainDevice;
    private DeviceItem mMainDeviceInfo;

    /**
     * 当前设备类型
     */
    private String mCurrentDeviceType;
    /**
     * 当前路由mac地址
     */
    private String mCurrentRouterMac;
    /**
     * 配置状态代码
     */
    private int mGuideState;

    private boolean isLogined;

    private LinearLayout mPanelChilds;
    private CustomHorizontalScrollView mHorizontalScrollView;

    private BaseBeen<EnumPage, Object> mNextPageItem;

    private boolean isFirst = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Preferences.getInstance(mActivity).remove(KeyConfig.KEY_COOKIE_SSID);
        Preferences.getInstance(mActivity).remove(KeyConfig.KEY_DEVICE_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wifi_home, null, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mVMainDevice = view.findViewById(R.id.view_item_main_device);

        mHorizontalScrollView = view.findViewById(R.id.horizontalScrollView01);
        mHorizontalScrollView.setOnScrollChangeListener(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                mVSubordinateEffect.postInvalidate();
            }
        });

        mTvInternet = view.findViewById(R.id.tv_internet);
        mVVerticalLine = view.findViewById(R.id.view_vertical_line);
        mVMainDevice.setOnClickListener(this);

        mPanelChilds = view.findViewById(R.id.panel_child);
        view.findViewById(R.id.btn_add).setOnClickListener(this);


        mVSubordinateEffect = view.findViewById(R.id.view_subordinate_effect);
        mVSubordinateEffect.setMain(mVMainDevice);
        mVSubordinateEffect.setArrChild(mPanelChilds);


        view.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onFragmentLifeListener != null)
                    onFragmentLifeListener.onChanged(null);
            }
        });
        mBtnSetting = view.findViewById(R.id.btn_setting);
        mBtnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onFragmentLifeListener != null)
                    onFragmentLifeListener.onChanged(new BaseBeen(EnumPage.ROUTER_SETTING, mCurrentDeviceType));
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            doGetDeviceDetailInfoFromCloud();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
        } else {
            doGetDeviceDetailInfoFromCloud();
        }
    }

    /**
     * 检查本地是否缓存密码，没缓存就输入，缓存了就直接进入密码检测接口
     *
     * @param isForceInput 是否强制输入密码
     */
    private void checkIsLogin(boolean isForceInput) {

        String password = Preferences.getInstance(mActivity.getApplicationContext()).getString(KeyConfig.KEY_ROUTER_PASSWORD);
        if (password == null || isForceInput) {
            InputDialog dialog = new InputDialog();
            Bundle bundle = new Bundle();
            bundle.putBoolean(InputDialog.EXTRA_PASSWORD, true);
            bundle.putBoolean(InputDialog.KEY_CANCEL_ABLE, false);
            bundle.putString(Intent.EXTRA_TEXT, _getString(DeviceType.PLC.getName().equalsIgnoreCase(mCurrentDeviceType) ? R.string.login_notice_plc : R.string.login_notice));
            dialog.setArguments(bundle);
            dialog.setFragmentListener(new OnFragmentLifeListener<String>() {

                @Override
                public void onChanged(String o) {
                    if (!TextUtils.isEmpty(o)) {
                        doLoginRouter(o);
                    }
                }

            });
            dialog.show(getFragmentManager(), "password_input");
        } else {
            doLoginRouter(password);
        }
    }

    private void refreshDevice(View view, DeviceItem item) {
        if (item == null) {
            view.setVisibility(View.INVISIBLE);
            return;
        }

        ImageView icon = view.findViewById(R.id.icon);
        if (icon.getDrawable() == null && item.getType() != null) {
            Drawable drawable = _getResources().getDrawable(item.getType().getIconResId());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable = DrawableCompat.wrap(drawable);
                drawable.setTint(_getResources().getColor(R.color.textColorClickable));
            }
            icon.setImageDrawable(drawable);
        }
        TextView tv_num = view.findViewById(R.id.tv_num);
        TextView name = view.findViewById(R.id.tv_name);
        TextView ip = view.findViewById(R.id.tv_ip);
        tv_num.setText(String.valueOf(item.getStaNum()));
        name.setText(item.getDeviceName());
        if (item.getLocation() != null) {
            SpannableString ss = new SpannableString(" " + item.getLocation());
            ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(_getResources().getColor(R.color.textColorClickable)), 0, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            name.append(ss);
        }
        ip.setText("IP:\t" + item.getIp());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_add) {
            EnumPage next = EnumPage.ADD_MESH;
            DeviceItem item = mMainDeviceInfo;
            mNextPageItem = new BaseBeen(next, item);
            if (mCurrentDeviceType == null) {
                doGetDeviceType();
            } else if (mGuideState != 1) {
                doCheckGuideState();
            } else if (!isLogined) {
                checkIsLogin(false);
            } else if (mCurrentRouterMac == null) {
                doGetRouterInfo();
            } else
                onFragmentLifeListener.onChanged(mNextPageItem);
        } else if (v.getTag() != null && v.getTag() instanceof DeviceItem) {

            DeviceItem item = (DeviceItem) v.getTag();
            if (!getWifiManager().isWifiEnabled() && getWifiManager().getConnectionInfo().getNetworkId() == -1) {
                showToast(_getString(R.string.notice_connect_wifi_first) + item.getDeviceName());
                return;
            }
            EnumPage next = EnumPage.ROUTER_DEVICE_INFO;

            mNextPageItem = new BaseBeen(next, item);
            if (mCurrentDeviceType == null) {
                doGetDeviceType();
            } else if (mGuideState != 1) {
                doCheckGuideState();
            } else if (!isLogined) {
                checkIsLogin(false);
            } else if (mCurrentRouterMac == null) {
                doGetRouterInfo();
            } else
                onFragmentLifeListener.onChanged(mNextPageItem);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WifiHomeActivity.REQUEST_CODE_WIZARD) {
            if (resultCode == Activity.RESULT_OK) {
                checkIsLogin(false);
            } else {
                mActivity.finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 登录路由器，校验密码
     *
     * @param password
     */
    private void doLoginRouter(String password) {
        addTask(
                new LoginTask().execute(password, getGateway(), mCurrentDeviceType, _getString(R.string.password_wrong), new TaskListener<BaseBeen<String, String>>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(_getString(R.string.logining), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result == TaskResult.OK) {
                            doGetRouterInfo();
                        } else {
                            showTaskError(task, R.string.login_failed);
                            isLogined = false;
                            checkIsLogin(true);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, BaseBeen<String, String> param) {
                        if (param != null) {
                            Preferences.getInstance(mActivity).saveString(KeyConfig.KEY_ROUTER_PASSWORD, param.getT1());
                            Preferences.getInstance(mActivity).saveString(KeyConfig.KEY_COOKIE_SSID, param.getT2());
                            isLogined = true;
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
     * 获取设备类型
     */
    private void doGetDeviceType() {
        addTask(
                new GetDeviceTypeTask().execute(getGateway(), _getString(R.string.illegal_device), new TaskListener<String>() {
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
                            mCurrentDeviceType = null;
                            try {
                                showAlert(getTaskError(task, R.string.no_device_type_receive), _getString(R.string.confirm), null);
                            } catch (Exception e) {
                                e.printStackTrace();
                                showAlert(e.getMessage(), getString(R.string._exit), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        onFragmentLifeListener.onChanged(null);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, String deviceType) {

                        DeviceType aimType = ((DeviceItem) mNextPageItem.getT2()).getType();
                        if (!aimType.getName().equalsIgnoreCase(deviceType)) {//类型不匹配
                            Preferences.getInstance(mActivity).remove(KeyConfig.KEY_DEVICE_TYPE);
                            showAlertTypeNotSame(aimType, deviceType);
                            return;
                        }

                        mCurrentDeviceType = deviceType;

                        doCheckGuideState();

                        Preferences.getInstance(mActivity).saveString(KeyConfig.KEY_DEVICE_TYPE, deviceType);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doCheckGuideState() {
        addTask(
                new CheckWizardGuidTask().execute(mActivity, getGateway(), mCurrentDeviceType, new TaskListener<Integer>() {
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
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, Integer s) {
                        if (s != null)
                            if (s != 1) {
                                Intent intent = new Intent(mActivity, WizardSettingActivity.class);
                                intent.putExtra(KeyConfig.KEY_GUIDE_STATE, s);
                                intent.putExtra(KeyConfig.KEY_DEVICE_BINDED, true);
                                intent.putExtra(KeyConfig.KEY_DEVICE_TYPE, mCurrentDeviceType);
                                startActivityForResult(intent, WifiHomeActivity.REQUEST_CODE_WIZARD);
                            } else {
                                checkIsLogin(false);
                                mGuideState = s;
                            }
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
                new DeviceDetailAndGroupTask().execute(mInfoFromApp, new TaskListener<BaseBeen<DeviceDetailBeen, Group>>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        if (isFirst) {
                            showProgressDialog(_getString(R.string.downloading), false, null);
                            isFirst = false;
                        }
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
                    public void onProgressUpdate(GenericTask task, BaseBeen<DeviceDetailBeen, Group> param) {
                        while (mPanelChilds.getChildCount() > 1)
                            mPanelChilds.removeViewAt(0);
                        if (param == null)
                            return;
                        DeviceDetailBeen deviceBeen = param.getT1();
                        if (param != null) {
                            if (TextUtils.isEmpty(deviceBeen.getState())) {
                                alertNoStateInfoAndExist();
                                return;
                            }
                            DeviceItem item = new DeviceItem();
                            item.setDeviceName(deviceBeen.getName());
                            item.setMac(deviceBeen.getMac());
                            item.setIconUrl(deviceBeen.getIconUrl());
                            item.setUpConnected(true);
                            item.setType(DeviceType.getDeviceTypeByCloudCode(deviceBeen.getDeviceType()));
                            if (param.getT2() != null)
                                item.setLocation(param.getT2().getGroupName());

                            if (!TextUtils.isEmpty(deviceBeen.getState())) {
                                if (item.getType() == DeviceType.BWR) {// 展示组网信息，包含子设备
                                    analysisMeshState(deviceBeen.getState(), item);
                                } else if (item.getType() == DeviceType.R2s) {// 展示组网信息，包含子设备
                                    analysisRouterState(deviceBeen.getState(), item);
                                }
                                // 展示电力猫子设备
                                else if (item.getType() == DeviceType.PLC) {
                                    analysisPlcState(deviceBeen.getState(), item);
                                }
                            }
                            mMainDeviceInfo = item;
                            mVMainDevice.setTag(item);
                            mVMainDevice.setVisibility(View.VISIBLE);
                            refreshDevice(mVMainDevice, item);
                            mTvInternet.setVisibility(View.VISIBLE);
                            mVVerticalLine.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void analysisMeshState(String state, DeviceItem item) {
        MeshStateBeen been = new Gson().fromJson(state, MeshStateBeen.class);
        if (item.getDeviceName() == null)
            item.setDeviceName(been.getStateValue().getSsid());
        item.setIp(been.getStateValue().getIp());
        item.setStaNum(been.getStateValue().getDevice_list() == null ? 0 : been.getStateValue().getDevice_list().size());
        for (MeshStateBeen.NetItem netItem : been.getStateValue().getNet_list()) {
            DeviceItem netItemDeviceItem = netItem.getDeviceItem();
            netItemDeviceItem.setChild(true);
            netItemDeviceItem.setType(DeviceType.BWR);
            netItemDeviceItem.setUpNodeName(been.getMac());
            View view = LayoutInflater.from(mActivity).inflate(R.layout.item_device1, null, false);
            refreshDevice(view, netItemDeviceItem);
            view.setTag(netItemDeviceItem);
            view.setOnClickListener(WifiHomeFragment.this);
            mPanelChilds.addView(view, mPanelChilds.getChildCount() - 1);
        }
        mVSubordinateEffect.setVisibility(View.VISIBLE);
        mVSubordinateEffect.setMain(mVMainDevice);
        mHorizontalScrollView.setVisibility(View.VISIBLE);
//                                    } else {
//                                        mHorizontalScrollView.setVisibility(View.INVISIBLE);
//                                        mVSubordinateEffect.setVisibility(View.INVISIBLE);
//                                    }


    }

    private void analysisRouterState(String state, DeviceItem item) {
        MeshStateBeen been = new Gson().fromJson(state, MeshStateBeen.class);
        if (item.getDeviceName() == null)
            item.setDeviceName(been.getStateValue().getSsid());
        item.setIp(been.getStateValue().getIp());
        item.setStaNum(been.getStateValue().getConnected_num());
    }

    private void analysisPlcState(String state, DeviceItem item) {
        PLCModemStateBeen been = new Gson().fromJson(state, PLCModemStateBeen.class);
//                                    if (!TextUtils.isEmpty(been.getStateValue().getIpv4_addr()))
//                                        item.setIp(been.getStateValue().getIpv4_addr() + '\n' + been.getStateValue().getIpv6_addr());
//                                    else
        item.setIp(been.getStateValue().getIpv4_addr());
        {//当前连接数量
            String mac = been.getMac();
            if (mac.length() == 12) {
                StringBuilder sb = new StringBuilder(mac);
                for (int i = 0; i < 5; ++i)
                    sb.insert(2 + 3 * i, ':');
                mac = sb.toString();
            }
            int num = 0;
            for (PLCModemStateBeen.Cpe cpe : been.getStateValue().getCpe_list()) {

                if (!TextUtils.isEmpty(cpe.getAccess_node()) && cpe.getAccess_node().equalsIgnoreCase(mac)) {
                    num++;
                }
            }
            item.setMac(mac);
            item.setStaNum(num);
//                                    item.setStaNum(been.getStateValue().getAttached_dev_num());
        }
        for (PLCModemStateBeen.Slave slave : been.getStateValue().getSlave_list()) {
            DeviceItem netItemDeviceItem = slave.getDeviceItem();
            netItemDeviceItem.setUpNodeName(slave.getSlave_mac());
            netItemDeviceItem.setIp(slave.getSlave_ip_addr());
            netItemDeviceItem.setDeviceName(slave.getSlave_name());
            netItemDeviceItem.setChild(true);
            netItemDeviceItem.setUpConnected(true);
            netItemDeviceItem.setType(DeviceType.PLC);
            netItemDeviceItem.setLocation(slave.getSlave_location());
            int num = 0;
            for (PLCModemStateBeen.Cpe cpe : been.getStateValue().getCpe_list()) {
                if (!TextUtils.isEmpty(cpe.getAccess_node()) && cpe.getAccess_node().equalsIgnoreCase(slave.getSlave_mac())) {
                    num++;
                }
            }
            netItemDeviceItem.setStaNum(num);
            View view = LayoutInflater.from(mActivity).inflate(R.layout.item_device1, null, false);
            refreshDevice(view, netItemDeviceItem);
            view.setTag(netItemDeviceItem);
            view.setOnClickListener(WifiHomeFragment.this);
            mPanelChilds.addView(view, mPanelChilds.getChildCount() - 1);
        }
        if (TextUtils.isEmpty(item.getDeviceName()))
            item.setDeviceName(been.getStateValue().getSsid());

        mVSubordinateEffect.setVisibility(View.VISIBLE);
        mVSubordinateEffect.setMain(mVMainDevice);
        mHorizontalScrollView.setVisibility(View.VISIBLE);

    }

    /**
     * 提示设备离线，并退出
     */
    private void alertNoStateInfoAndExist() {
        showAlert(_getString(R.string.notice_devcie_offline), _getString(R.string._exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onFragmentLifeListener.onChanged(null);
            }
        });
    }

    /**
     * 获取本地路由器设备的信息，对比mac地址
     */
    private void doGetRouterInfo() {
        addTask(
                new GetRouterInfoTask().execute(getGateway(), mCurrentDeviceType, getCookie(), new TaskListener<SettingResponseAllBeen>() {
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
                            mCurrentRouterMac = null;
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, SettingResponseAllBeen param) {
                        if (param != null) {
                            if (!param.getWan_mac().equalsIgnoreCase(mInfoFromApp.getMac())) {
                                DeviceItem item = ((DeviceItem) mNextPageItem.getT2());
                                if (item.getType() == DeviceType.R2s)
                                    showAlertMacNotSame(mInfoFromApp.getMac(), param.getWan_mac());
                                else if (item.getType() == DeviceType.BWR)
                                    doGetMeshNetState();
                                else if (item.getType() == DeviceType.PLC) {
                                    doGetPlcInfo();
                                }
                            } else {
                                mCurrentRouterMac = param.getWan_mac();
                                if (!mMainDeviceInfo.isLinkOn() && param.getWan_ip() != null)
                                    mMainDeviceInfo.setIp(param.getWan_ip());
                                onFragmentLifeListener.onChanged(mNextPageItem);//前往下一页
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
                        showProgressDialog(_getString(R.string.note_mesh_info_request), false, null);
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
                        if (param != null && !param.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (ListInfo listInfo : param) {
                                if (mInfoFromApp.getMac().equals(listInfo.getMac())) {
                                    onFragmentLifeListener.onChanged(mNextPageItem);//前往下一页
                                    return;
                                }
                                sb.append(listInfo.getMac()).append(" / ");
                            }
                            sb.deleteCharAt(sb.length() - 1);
                            //TODO
                            showAlertMacNotSame(mInfoFromApp.getMac(), sb.toString());
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
     * 获取PLC信息
     */
    private void doGetPlcInfo() {
        addTask(
                new GetPLCInfoTask().execute(getGateway(), new TaskListener<PLCInfo>() {
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
                            if (param.getPlc_node() != null && !param.getPlc_node().isEmpty()) {
                                for (PLCInfo.Dev_Info dev_info : param.getDev_info()) {
                                    DeviceItem item = dev_info.turn2DeviceItem();
                                    if (mInfoFromApp.getMac().equals(item.getMac())) {
                                        onFragmentLifeListener.onChanged(mNextPageItem);//前往下一页
                                        return;
                                    }
                                }
                            }
                        }
                        showAlertMacNotSame(mInfoFromApp.getMac(), "");
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }
}
