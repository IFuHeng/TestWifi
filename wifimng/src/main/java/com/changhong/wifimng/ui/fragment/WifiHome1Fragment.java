package com.changhong.wifimng.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
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
import com.changhong.wifimng.been.StaInfo;
import com.changhong.wifimng.been.WanType;
import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.been.mesh.ListInfo;
import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.been.status.RequireAndResponseBeen;
import com.changhong.wifimng.been.sys.SettingResponseAllBeen;
import com.changhong.wifimng.been.wan.ResponseAllBeen;
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
import com.changhong.wifimng.task.router.GetLanInfoTask;
import com.changhong.wifimng.task.router.GetMeshNetworkTask;
import com.changhong.wifimng.task.router.GetRouterStatusTask;
import com.changhong.wifimng.task.router.GetStaInfoTask;
import com.changhong.wifimng.task.router.GetWlanTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.activity.WifiHomeActivity;
import com.changhong.wifimng.ui.activity.WizardSettingActivity;
import com.changhong.wifimng.ui.view.CustomHorizontalScrollView;
import com.changhong.wifimng.ui.view.VerticalSubordinateEffectView;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class WifiHome1Fragment extends BaseFragment<BaseBeen<EnumPage, Object>> implements View.OnClickListener {

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

    private LinearLayout mPanelChilds;
    private CustomHorizontalScrollView mHorizontalScrollView;

    private DeviceItem mNextDeviceItem;

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
        mVMainDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentLifeListener.onChanged(new BaseBeen(EnumPage.ROUTER_DEVICE_INFO, mMainDeviceInfo));
            }
        });

        mPanelChilds = view.findViewById(R.id.panel_child);
        view.findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentLifeListener.onChanged(new BaseBeen(EnumPage.ADD_MESH, mMainDeviceInfo));
            }
        });


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
                if (onFragmentLifeListener != null) {
                    Whole2LocalBeen been = new Whole2LocalBeen();
                    been.setMac(mMainDeviceInfo.getMac());
                    been.setDeviceType(mCurrentDeviceType);
                    onFragmentLifeListener.onChanged(new BaseBeen(EnumPage.ROUTER_SETTING, been));
                }
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
            if (getWifiManager().isWifiEnabled() && getWifiManager().getConnectionInfo().getNetworkId() != -1) {
                if (!Preferences.getInstance(mActivity).contains(KeyConfig.KEY_DEVICE_TYPE)) {
                    doGetDeviceType();
                } else if (!Preferences.getInstance(mActivity).contains(KeyConfig.KEY_COOKIE_SSID))
                    checkIsLogin(false);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
//            mTvType.setText(R.string.known_device);
        } else {
            if (getWifiManager().isWifiEnabled() && getWifiManager().getConnectionInfo().getNetworkId() != -1) {
                if (!Preferences.getInstance(mActivity).contains(KeyConfig.KEY_DEVICE_TYPE)) {
                    doGetDeviceType();
                } else if (!Preferences.getInstance(mActivity).contains(KeyConfig.KEY_COOKIE_SSID))
                    checkIsLogin(false);
            }
        }
    }

    /**
     * 检查本地是否缓存密码，没缓存就输入，缓存了就直接进入密码检测接口
     *
     * @param isForceInput 是否强制输入密码 或者退出
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
                    } else {
                        onFragmentLifeListener.onChanged(null);
                    }
                }

            });
            dialog.show(getFragmentManager(), "password_input");
        } else {
            doLoginRouter(password);
        }
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
                            mTvInternet.setVisibility(View.VISIBLE);
                            mVVerticalLine.setVisibility(View.VISIBLE);
                            mBtnSetting.setVisibility(View.VISIBLE);
                            goNext(Preferences.getInstance(mActivity).getString(KeyConfig.KEY_DEVICE_TYPE));
                        } else {
                            showTaskError(task, R.string.login_failed);
                            checkIsLogin(true);
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

    /**
     * 前往下一页（设备详情页面）
     */
    private void goNext(String deviceTypeName) {
//        doGetStatusInfo();
        //组网设备类型获取组网信息
        if (DeviceType.BWR.getName().equals(deviceTypeName)) {
            doGetRouterInfo(true);
        } else if (DeviceType.R2s.getName().equals(deviceTypeName)) {
            doGetRouterInfo(false);
        } else if (DeviceType.PLC.getName().equals(deviceTypeName)) {
            doGetPlcInfo();
        }
    }

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
                        while (mPanelChilds.getChildCount() > 1)
                            mPanelChilds.removeViewAt(0);
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
                            mVMainDevice.setVisibility(View.VISIBLE);
                            mMainDeviceInfo = param.turn2DeviceItem();

                            if (param.getPlc_node() != null && !param.getPlc_node().isEmpty()) {
                                for (PLCInfo.PlcNode dev_info : param.getPlc_node()) {
                                    if (dev_info.getMac().equalsIgnoreCase(param.getDev_mac()))
                                        continue;
                                    DeviceItem item = dev_info.turn2DeviceItem();
                                    item.setType(DeviceType.PLC);
                                    item.setStaNum(param.getLinkNum(dev_info.getMac()));
                                    View view = LayoutInflater.from(mActivity).inflate(R.layout.item_device1, null, false);
                                    refreshDevice(view, item);
                                    view.setTag(item);
                                    view.setOnClickListener(WifiHome1Fragment.this);
                                    mPanelChilds.addView(view, mPanelChilds.getChildCount() - 1);
                                }
                                mVSubordinateEffect.setVisibility(View.VISIBLE);
                                mVSubordinateEffect.setMain(mVMainDevice);
                                mHorizontalScrollView.setVisibility(View.VISIBLE);
                            }
                            refreshDevice(mVMainDevice, mMainDeviceInfo);
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doGetStaInfo() {

        addTask(new GetStaInfoTask().execute(getGateway(), getCookie(), new TaskListener<List<StaInfo>>() {
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
                    public void onProgressUpdate(GenericTask task, List<StaInfo> param) {
                        if (param != null) {
                            if (mMainDeviceInfo != null) {
                                mMainDeviceInfo.setStaNum(param.size());
                                refreshDevice(mVMainDevice, mMainDeviceInfo);
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

    private void doGetRouterInfo(final boolean isMesh) {
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
                            onFragmentLifeListener.onChanged(null);
                        } else {
                            doGetStaInfo();
                            mVMainDevice.setVisibility(View.VISIBLE);
                            if (isMesh)
                                doGetMeshNetState();
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, SettingResponseAllBeen param) {
                        if (param != null) {
                            DeviceItem item = new DeviceItem();
                            item.setDeviceName(param.getEquipment());
                            item.setIp(param.getLan_ip());
                            item.setMac(param.getWan_mac());
                            item.setUpConnected(param.isLinkOn());
                            item.setWan_type(WanType.getDeviceTypeFromName(param.getWan_type()));

                            for (DeviceType value : DeviceType.values()) {
                                if (value.getName().equals(Preferences.getInstance(mActivity).getString(KeyConfig.KEY_DEVICE_TYPE))) {
                                    item.setType(value);
                                    break;
                                }
                            }
                            mMainDeviceInfo = item;
                            refreshDevice(mVMainDevice, item);
                            if (param.isLinkOn()) {
                                mVVerticalLine.setBackgroundColor(Color.GREEN);
                            } else
                                mVVerticalLine.setBackgroundColor(Color.RED);

                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void refreshDevice(View view, DeviceItem item) {
        //TODO reset main device
        if (item == null) {
            view.setVisibility(View.INVISIBLE);
            return;
        }

        ImageView icon = view.findViewById(R.id.icon);
        if (item.getType() != null) {
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
        if (item.getStaNum() == 0)
            tv_num.setVisibility(View.INVISIBLE);
        else {
            tv_num.setVisibility(View.VISIBLE);
            tv_num.setText(String.valueOf(item.getStaNum()));
        }
        name.setText(item.getDeviceName());
        if (item.getIp() != null)
            ip.setText("IP\t" + item.getIp());
        else if (item.getMac() != null) {
            SpannableString ss = new SpannableString("MAC\n" + item.getMac());
            ss.setSpan(new RelativeSizeSpan(0.8f), 4, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(Color.BLUE), 4, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            ip.setText(ss);
            ip.setGravity(Gravity.CENTER);
        }
    }

    private void doGetLanInfo() {
        addTask(
                new GetLanInfoTask().execute(getGateway(), getCookie(), new TaskListener<ResponseAllBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.downloading), false, new DialogInterface.OnCancelListener() {
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
                    public void onProgressUpdate(GenericTask task, ResponseAllBeen param) {
                        if (param != null) {

                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doGetStatusInfo() {
        addTask(
                new GetRouterStatusTask().execute(getGateway(), getCookie(), new TaskListener<RequireAndResponseBeen>() {
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
                        if (result != TaskResult.OK)
                            showTaskError(task, R.string.interaction_failed);
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, RequireAndResponseBeen param) {

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
                        while (mPanelChilds.getChildCount() > 1)
                            mPanelChilds.removeViewAt(0);

                        // TEST FOR SHOW LIST EFFECT
//                        for (int i = 0; i < 5; i++) {
//
//                            DeviceItem item = new DeviceItem();
//                            View view = LayoutInflater.from(mActivity).inflate(R.layout.item_device1, null, false);
//                            refreshDevice(view, item);
//                            mPanelChilds.addView(view, mPanelChilds.getChildCount() - 1);
//                            view.setTag(item);
//                            view.setOnClickListener(WifiHome1Fragment.this);
//                        }
                        mHorizontalScrollView.setVisibility(View.VISIBLE);
                        mVSubordinateEffect.setVisibility(View.VISIBLE);
                        mVSubordinateEffect.postInvalidate();
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
                            for (ListInfo listInfo : param) {
                                DeviceItem item = listInfo.getDeviceItem(mMainDeviceInfo.getDeviceName());
                                item.setType(DeviceType.getDeviceTypeFromName(mCurrentDeviceType));
                                View view = LayoutInflater.from(mActivity).inflate(R.layout.item_device1, null, false);
                                refreshDevice(view, item);
                                view.setTag(item);
                                view.setOnClickListener(WifiHome1Fragment.this);
                                mPanelChilds.addView(view, mPanelChilds.getChildCount() - 1);
                            }
                            mVSubordinateEffect.setVisibility(View.VISIBLE);
                            mVSubordinateEffect.setMain(mVMainDevice);
                            mVSubordinateEffect.postInvalidate();
                            //TODO
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
     * 获取wan口配置信息
     */
    private void doGetWanState() {
        addTask(
                new GetWlanTask().execute(getGateway(), getCookie(), new TaskListener<ResponseAllBeen>() {
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
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, ResponseAllBeen param) {
                        if (param != null) {
                            mMainDeviceInfo.setWan_type(WanType.getDeviceTypeFromName(param.getType()));
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
                            public void onCancel(DialogInterface dialogInterface) {
                                task.cancel(true);
                                onFragmentLifeListener.onChanged(null);
                                dialogInterface.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result == TaskResult.OK) {

                        } else {
                            mCurrentDeviceType = null;
                            //退出
                            try {
                                alertExitWithNotInteractionWithRouter(getTaskError(task, R.string.no_device_type_receive));
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
                        if (deviceType != null) {
//                            mTvType.setText(deviceType);

                            for (DeviceType value : DeviceType.values()) {
                                if (value.getName().equalsIgnoreCase(deviceType)) {
                                    mCurrentDeviceType = deviceType;
                                    Preferences.getInstance(mActivity).saveString(KeyConfig.KEY_DEVICE_TYPE, deviceType);
                                    //启动检测设备配置状态
                                    doCheckGuideState();
                                    return;
                                }
                            }
                        }
                        Preferences.getInstance(mActivity).remove(KeyConfig.KEY_DEVICE_TYPE);
                        alertExitWithNotCorrectDevice(deviceType);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {

                    }
                })
        );
    }

    /***
     * 通知非正确长虹路由设备，并退出
     * @param deviceType
     */
    private void alertExitWithNotCorrectDevice(String deviceType) {
        SpannableString spannableString = new SpannableString(deviceType + _getString(R.string.not_ch_router));
        spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, deviceType.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        showAlert(spannableString, _getString(R.string._exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onFragmentLifeListener.onChanged(null);
            }
        });
//        checkIsLogin(false);
    }

    /***
     * 通知无法与路由交互，并退出
     */
    private void alertExitWithNotInteractionWithRouter(String string) {
        showAlert(string, _getString(R.string._exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onFragmentLifeListener != null)
                    onFragmentLifeListener.onChanged(null);
                else
                    mActivity.finish();
            }
        });
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
                            } else
                                checkIsLogin(false);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {

                    }
                })
        );
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null && v.getTag() instanceof DeviceItem) {

            DeviceItem item = (DeviceItem) v.getTag();
            if (Preferences.getInstance(mActivity).contains(KeyConfig.KEY_DEVICE_TYPE))
                onFragmentLifeListener.onChanged(new BaseBeen(EnumPage.ROUTER_DEVICE_INFO, item));
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

}
