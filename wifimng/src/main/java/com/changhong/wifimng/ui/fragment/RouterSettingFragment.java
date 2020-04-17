package com.changhong.wifimng.ui.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.BaseBeen3;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.been.sys.SettingResponseAllBeen;
import com.changhong.wifimng.http.been.UpgradeBeen;
import com.changhong.wifimng.http.task.CheckUpdateTask;
import com.changhong.wifimng.http.task.UnbindingTask;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.GetDeviceTypeTask;
import com.changhong.wifimng.task.GetRouterInfoTask;
import com.changhong.wifimng.task.LoginTask;
import com.changhong.wifimng.task.RebootTask;
import com.changhong.wifimng.task.RecoveryTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.GetPLCDeviceBaseInfoTask;
import com.changhong.wifimng.task.CheckWizardGuidTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.activity.WifiHomeActivity;
import com.changhong.wifimng.ui.activity.WizardSettingActivity;
import com.changhong.wifimng.uttils.CommUtil;
import com.changhong.wifimng.uttils.WifiMacUtils;

import java.util.Observable;
import java.util.Observer;

/**
 * A simple {@link Fragment} subclass.
 */
public class RouterSettingFragment extends BaseFragment implements View.OnClickListener, ExpandableListView.OnChildClickListener {

    private ExpandableListView mListView;
    /**
     * 当前已连接路由器类型
     */
    private String mCurrentDeviceType;
    /**
     * 当前设备的软件版本
     */
    private String mCurrentDeviceSoftwareVersion;
    /**
     * 当前路由mac地址
     */
    private String mCurrentRouterMac;
    /**
     * 配置状态代码
     */
    private int mGuideState;
    /**
     * 下一页的页码
     */
    private EnumPage mNextPage;
    /**
     * 下一件事情
     */
    private Observer mNextObserver;

    private boolean isLogin;

    private int[][] mListResIdArr;
    private SettingAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_router_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_back).setOnClickListener(this);

        mListView = view.findViewById(R.id.listview);
        mListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                mListView.setOnGroupCollapseListener(null);
                mListView.expandGroup(i);
                mListView.setOnGroupCollapseListener(this);
            }
        });
        String[] groupTitle = new String[]{getString(R.string.function_setting), getString(R.string.common_setting)};
        String[][] childArr = getChoices();
        mListResIdArr = new int[childArr.length][];
        for (int i = 0; i < mListResIdArr.length; i++) {
            mListResIdArr[i] = new int[childArr[i].length];
        }
        mAdapter = new SettingAdapter(mActivity, groupTitle, childArr, mListResIdArr);
        mListView.setAdapter(mAdapter);
        mListView.setOnChildClickListener(this);

        for (int i = 0; i < mListView.getExpandableListAdapter().getGroupCount(); i++) {
            mListView.expandGroup(i);
        }

        super.onViewCreated(view, savedInstanceState);

        doChechUpgrade();
    }

    protected String[][] getChoices() {
        return new String[][]{getResources().getStringArray(R.array.setting_function), getResources().getStringArray(R.array.setting_common)};
    }

    protected void showVersionHasNew() {
        mListResIdArr[1][2] = R.drawable.ic_verson_new;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null) {
                onFragmentLifeListener.onChanged(null);
            }
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (onFragmentLifeListener == null)
            return false;

        switch (groupPosition) {
            case 0:
                switch (childPosition) {
                    case 0://wifi setting
                        gotoNextPage(EnumPage.NET_MENU);
                        break;
                    case 1:
                        // onFragmentLifeListener.onChanged(new BaseBeen3(EnumPage.GROUP_SETTING, null, null));
                        gotoNextPage(EnumPage.GROUP_SETTING);
                        break;
                    case 2:
                        gotoNextPage(EnumPage.GUEST_NETWORK);
                        break;
                    case 3:
                        gotoNextPage(EnumPage.DDNS_SHOW);
                        break;
                    case 4:
                        gotoNextPage(EnumPage.WLAN_ACCESS);
                        break;
                    case 5:
                        gotoNextPage(EnumPage.LAN_SETTING);
                        break;
                    default:
                        showToast(R.string.wait_for_develop);
                }
                break;

            case 1:
                switch (childPosition) {
                    case 0:
                        onFragmentLifeListener.onChanged(new BaseBeen3(EnumPage.DEVICE_NAME_AND_ROOM, null, null));
                        break;
                    case 1:
                        gotoNextPage(EnumPage.ADMIN_PASSWORD);
                        break;
                    case 2:
                        gotoNextPage(EnumPage.DEVICE_UPDATE);
                        break;
                    case 3://重启
                        doNextFunction(new Observer() {
                            @Override
                            public void update(Observable observable, Object o) {
                                askReboot();
                            }
                        });
                        break;
                    case 4:// reset
                        doNextFunction(new Observer() {
                            @Override
                            public void update(Observable observable, Object o) {
                                askRecovery();
                            }
                        });
                        break;
                    case 5:
                        askDeleteDevice();
                        break;
                    default:
                        showToast(R.string.wait_for_develop);
                }
                break;
        }

        return false;
    }

    private void doCheckDeviceMac() {
        if (DeviceType.PLC.getName().equalsIgnoreCase(mCurrentDeviceType))
            doGetPLCRouterInfo();
        else
            doGetRouterInfo();
    }

    /**
     * 前往下一页
     *
     * @param page
     */
    protected void gotoNextPage(EnumPage page) {
        mNextPage = page;
        if (mCurrentDeviceType == null) {
            doGetDeviceType();
        } else if (mGuideState != 1) {
            doCheckGuideState();
        } else if (!isLogin) {
            checkIsLogin(false);
        } else if (mCurrentRouterMac == null) {
            doCheckDeviceMac();
        } else {
            onFragmentLifeListener.onChanged(new BaseBeen3(page, mCurrentRouterMac, mCurrentDeviceSoftwareVersion));
            mNextPage = null;
        }
    }

    protected void doNextFunction(Observer observer) {
        mNextObserver = observer;
        if (mCurrentDeviceType == null) {
            doGetDeviceType();
        } else if (mGuideState != 1) {
            doCheckGuideState();
        } else if (!isLogin) {
            checkIsLogin(false);
        } else if (mCurrentRouterMac == null) {
            doCheckDeviceMac();
        } else {
            observer.update(null, null);
            mNextObserver = null;
        }
    }

    protected void askRecovery() {
        String message;
        if (DeviceType.PLC.getName().equalsIgnoreCase(mCurrentDeviceType))
            message = _getString(R.string.notice_make_sure_recovery_plc);
        else
            message = _getString(R.string.notice_make_sure_recovery);
        showAlert(message, _getString(R.string.recovery), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doRecovery();
                dialog.dismiss();
            }
        }, _getString(R.string.cancel), null, true);
    }

    /**
     * 执行恢复出厂设置
     */
    private void doRecovery() {
        addTask(new RecoveryTask().execute(getGateway(), mCurrentDeviceType, getCookie(), new TaskListener<String>() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public void onPreExecute(GenericTask task) {
                showProgressDialog(_getString(R.string.recovering), false, null);
            }

            @Override
            public void onPostExecute(GenericTask task, TaskResult result) {
                hideProgressDialog();
                if (result != TaskResult.OK) {
                    showTaskError(task, R.string.interaction_failed);
                } else
                    alertCompleteAndExit(_getString(R.string.notice_reset_completed));
            }

            @Override
            public void onProgressUpdate(GenericTask task, String param) {

            }

            @Override
            public void onCancelled(GenericTask task) {
                hideProgressDialog();
            }
        }));
    }

    protected void askReboot() {
        String message;
        if (DeviceType.PLC.getName().equalsIgnoreCase(mCurrentDeviceType))
            message = _getString(R.string.notice_make_sure_reboot_plc);
        else
            message = _getString(R.string.notice_make_sure_reboot);
        showAlert(message, _getString(R.string.reboot), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doReboot();
                dialog.dismiss();
            }
        }, _getString(R.string.cancel), null, true);
    }

    /**
     * 执行重启
     */
    protected void doReboot() {
        addTask(new RebootTask().execute(getGateway(), mCurrentDeviceType, getCookie(), new TaskListener<String>() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public void onPreExecute(GenericTask task) {
                showProgressDialog(_getString(R.string.rebooting), false, null);
            }

            @Override
            public void onPostExecute(GenericTask task, TaskResult result) {
                hideProgressDialog();
                if (result != TaskResult.OK) {
                    showTaskError(task, R.string.interaction_failed);
                } else
                    alertCompleteAndExit(_getString(R.string.notice_reboot_completed));
            }

            @Override
            public void onProgressUpdate(GenericTask task, String param) {

            }

            @Override
            public void onCancelled(GenericTask task) {
                hideProgressDialog();
            }
        }));
    }

    private void alertCompleteAndExit(CharSequence charSequence) {
//        onFragmentLifeListener.onChanged(null);
        showAlert(charSequence, _getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(BaseWifiActivtiy.ACTION_EXIT_APP));
            }
        });
    }

    /**
     * 列表的adapter
     */
    class SettingAdapter extends BaseExpandableListAdapter {

        private Context context;
        private String[] groupTitle;
        private String[][] childArr;
        private int[][] resIdArr;

        public SettingAdapter(Context context, String[] groupTitle, String[][] childArr, int[][] resIdArr) {
            this.groupTitle = groupTitle;
            this.childArr = childArr;
            this.context = context;
            this.resIdArr = resIdArr;
        }

        @Override
        public int getGroupCount() {
            if (groupTitle == null)
                return 0;
            else
                return groupTitle.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (childArr == null || childArr[groupPosition] == null)
                return 0;
            else
                return childArr[groupPosition].length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            if (groupTitle == null)
                return null;
            return groupTitle[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            if (childArr == null || childArr[groupPosition] == null)
                return null;
            else
                return childArr[groupPosition][childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return (groupPosition << 16) | childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
//                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null, false);
                convertView = new TextView(context);
                convertView.setMinimumHeight(0);
                TextView textView = (TextView) convertView;
                textView.setBackgroundColor(context.getResources().getColor(R.color.setting_group_bg));
                int paddingLR = CommUtil.dip2px(mActivity, 10);
                int paddingTB = CommUtil.dip2px(mActivity, 4);
                textView.setPadding(paddingLR, paddingTB, paddingLR, paddingTB);
                textView.setTextColor(Color.GRAY);
                textView.setTextSize(14);
            }
            TextView textView = (TextView) convertView;
            textView.setText(groupTitle[groupPosition]);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(context).inflate(R.layout.item_group, null, false);

            TextView tv_name = convertView.findViewById(R.id.tv_name);
            tv_name.setText(childArr[groupPosition][childPosition]);
            tv_name.setTextColor(Color.BLACK);

            ImageView icon1 = convertView.findViewById(R.id.btn_delete);
            icon1.setImageResource(R.drawable.ic_navigate_next_black_24dp);

            ImageView icon2 = convertView.findViewById(R.id.btn_edit);
            if (resIdArr == null || resIdArr[groupPosition] == null || resIdArr[groupPosition][childPosition] == 0)
                icon2.setVisibility(View.INVISIBLE);
            else {
                icon2.setVisibility(View.VISIBLE);
                icon2.setImageResource(resIdArr[groupPosition][childPosition]);
            }
//            ((TextView) convertView).setText(childArr[groupPosition][childPosition]);
//            ((TextView) convertView).setTextColor(context.getResources().getColor(R.color.textColorSecondary));
//            ((TextView) convertView).setTextColor(Color.BLACK);

//            ((TextView) convertView).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_navigate_next_black_24dp, 0);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
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
                                showAlert(e.getMessage(), _getString(R.string._exit), new DialogInterface.OnClickListener() {
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
                        DeviceType typeRouter = DeviceType.getDeviceTypeFromName(deviceType);
                        DeviceType typeAim = DeviceType.getDeviceTypeFromName(mInfoFromApp.getDeviceType());

                        if (typeRouter != typeAim) {//类型不匹配
                            Preferences.getInstance(mActivity).remove(KeyConfig.KEY_DEVICE_TYPE);
                            showAlertTypeNotSame(typeAim, deviceType);
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
                            mGuideState = 0;
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, Integer s) {
                        if (s != null)
                            if (s != 1) {
                                Intent intent = new Intent(mActivity, WizardSettingActivity.class);
                                if (mInfoFromApp != null)
                                    intent.putExtra(KeyConfig.KEY_INFO_FROM_APP, mInfoFromApp);
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
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, SettingResponseAllBeen param) {
                        if (param != null) {
//                            if (!param.getWan_mac().equalsIgnoreCase(mInfoFromApp.getMac())) {
                            if (!WifiMacUtils.compareMac(param.getWan_mac(), mInfoFromApp.getMac())) {
                                showAlertMacNotSame(WifiMacUtils.macNoColon(mInfoFromApp.getMac()), param.getWan_mac());
                            } else {
                                mCurrentRouterMac = param.getWan_mac();
                                mCurrentDeviceSoftwareVersion = param.getSoft_ver();
                                //TODO 前往下一页
                                if (mNextPage != null) {
                                    gotoNextPage(mNextPage);
                                } else if (mNextObserver != null) {
                                    doNextFunction(mNextObserver);
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

    private void doGetPLCRouterInfo() {
        addTask(
                new GetPLCDeviceBaseInfoTask().execute(getGateway(), new TaskListener<PLCInfo>() {
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
                            if (!WifiMacUtils.compareMac(param.getDev_mac(), mInfoFromApp.getMac())) {
                                showAlertMacNotSame(WifiMacUtils.macNoColon(mInfoFromApp.getMac()), param.getDev_mac());
                            } else {
                                mCurrentRouterMac = param.getDev_mac();
                                mCurrentDeviceSoftwareVersion = param.getSw_ver();
                                //TODO 前往下一页
                                if (mNextPage != null) {
                                    gotoNextPage(mNextPage);
                                } else if (mNextObserver != null) {
                                    doNextFunction(mNextObserver);
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
                            doCheckDeviceMac();
                        } else {
                            isLogin = false;
                            showTaskError(task, R.string.login_failed);
                            checkIsLogin(true);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, BaseBeen<String, String> param) {
                        if (param != null) {
                            Preferences.getInstance(mActivity).saveString(KeyConfig.KEY_ROUTER_PASSWORD, param.getT1());
                            Preferences.getInstance(mActivity).saveString(KeyConfig.KEY_COOKIE_SSID, param.getT2());
                            isLogin = true;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WifiHomeActivity.REQUEST_CODE_WIZARD) {
            if (resultCode == Activity.RESULT_OK)
                doCheckGuideState();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    protected void askDeleteDevice() {
        showAlert(_getString(R.string.notice_make_sure_unbind), _getString(R.string.unbind), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doDeleteDevice(true);
                dialog.dismiss();
            }
        }, _getString(R.string.cancel), null, true);
    }

    private void doDeleteDevice(boolean isClean) {
        addTask(
                new UnbindingTask().execute(isClean, mInfoFromApp, new TaskListener<String>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(_getString(R.string.commit), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result == TaskResult.OK) {
                            showAlert(_getString(R.string.unbind_completed), _getString(R.string._exit), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    onFragmentLifeListener.onChanged(null);
                                    LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(KeyConfig.ACTION_UNBIND_DEVICE));
                                }
                            });
                        } else {
                            showTaskError(task, R.string.interaction_failed);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, String param) {

                    }

                    @Override
                    public void onCancelled(GenericTask task) {

                    }
                })
        );
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
//                            showTaskError(task, R.string.load_update_info_failed);
                        } else {
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, UpgradeBeen param) {
                        if (param == null || param.getIsUpgrade() == 0) {
                            return;
                        }

                        if (param.getIsUpgrade() == 1) {
                            if (param.getIsForce() == 1) {
                                showAlert(_getString(R.string.major_update) + '\n' + param.getComments(), _getString(R.string.update), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        gotoNextPage(EnumPage.DEVICE_UPDATE);
                                    }
                                }, _getString(R.string.cancel), null, false);
                            } else
                                showVersionHasNew();
                        }


                    }

                    @Override
                    public void onCancelled(GenericTask task) {

                    }
                })
        );
    }
}
