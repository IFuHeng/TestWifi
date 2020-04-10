package com.changhong.wifimng.ui.fragment.setting;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.DeviceItem;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.plc.DeviceCustormInfoBeen;
import com.changhong.wifimng.http.been.DeviceDetailBeen;
import com.changhong.wifimng.http.been.Group;
import com.changhong.wifimng.http.been.GroupListBeen;
import com.changhong.wifimng.http.been.MeshStateBeen;
import com.changhong.wifimng.http.been.PLCModemStateBeen;
import com.changhong.wifimng.http.task.DeviceDetailAndGroupTask;
import com.changhong.wifimng.http.task.DeviceGroupTask;
import com.changhong.wifimng.http.task.DeviceUpdateTask;
import com.changhong.wifimng.http.task.GetGroupListTask;
import com.changhong.wifimng.http.task.NewCreateGroupTask;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.DevcieCustomerInfoShowTask;
import com.changhong.wifimng.task.plc.UpdateCustomerInfoTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.fragment.EnumPage;
import com.changhong.wifimng.ui.fragment.InputDialog;
import com.changhong.wifimng.ui.fragment.OnFragmentLifeListener;
import com.changhong.wifimng.ui.view.CustomHorizontalScrollView;
import com.changhong.wifimng.ui.view.VerticalSubordinateEffectView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * @author fuheng
 * @since 20200221
 */
public class DeviceNameAndRoomFragment2 extends BaseFragment<BaseBeen<EnumPage, Object>> implements View.OnClickListener {

    /**
     * 效果线条
     */
    private VerticalSubordinateEffectView mVSubordinateEffect;

    /**
     * 主设备信息View
     */
    private View mVMainDevice;

    /**
     * 子节点容器
     **/
    private LinearLayout mPanelChilds;

    private CustomHorizontalScrollView mHorizontalScrollView;
    /**
     * 主设备信息
     */
    private DeviceItem mMainDeviceInfo;
    private DeviceType mDeviceType;
    /**
     * 分组信息
     */
    private List<Group> mArrayGroup;
    /**
     * 在电力猫中自定义的信息列表
     */
    private List<DeviceCustormInfoBeen.CuntomerInfo> mCustomerInfo;
    /**
     * 新增分组
     */
    private CharSequence mAddStr;
    private Animation mAnimClicked;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mInfoFromApp == null && onFragmentLifeListener != null)
            onFragmentLifeListener.onChanged(null);
        mArrayGroup = new ArrayList<>();
        mDeviceType = DeviceType.getDeviceTypeFromName(mInfoFromApp.getDeviceType());

        mAnimClicked = AnimationUtils.loadAnimation(mActivity, R.anim.button_click);
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_name_and_room, null, false);
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

        mPanelChilds = view.findViewById(R.id.panel_child);

        mVSubordinateEffect = view.findViewById(R.id.view_subordinate_effect);
        mVSubordinateEffect.setMain(mVMainDevice);
        mVSubordinateEffect.setArrChild(mPanelChilds);


        view.findViewById(R.id.btn_back).setOnClickListener(this);

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
//        if (!hidden) {
//            doGetDeviceDetailInfoFromCloud();
//        }
    }

    private void refreshDevice(View view, DeviceItem item) {
        if (item == null) {
            view.setVisibility(View.INVISIBLE);
            return;
        }

        ImageView icon = view.findViewById(R.id.icon);
        if (item.getType() != null) {
            icon.setImageResource(item.getType().getThumbResId());
        }
        TextView name = view.findViewById(R.id.tv_name);
        name.setText(item.getDeviceName());
        name.setOnClickListener(this);
        TextView location = view.findViewById(R.id.tv_location);
        location.setText(item.getLocation());
        location.setOnClickListener(this);
    }

    private void refreshDeviceEditDisabled(View view, DeviceItem item) {
        if (item == null) {
            view.setVisibility(View.INVISIBLE);
            return;
        }

        ImageView icon = view.findViewById(R.id.icon);
        if (item.getType() != null) {
            icon.setImageResource(item.getType().getThumbResId());
        }
        TextView name = view.findViewById(R.id.tv_name);
        name.setText(item.getDeviceName());
        name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        TextView location = view.findViewById(R.id.tv_location);
        location.setText(item.getLocation());
        location.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(null);
        }

        //更新名字
        else if (v.getId() == R.id.tv_name) {
            v.startAnimation(mAnimClicked);
            DeviceItem deviceItem = (DeviceItem) ((View) v.getParent()).getTag();
            if (deviceItem == mMainDeviceInfo) {//主节点更新名称
                showInputDialog(true, false, deviceItem, false);
            } else {//子节点更新名称
                showInputDialog(false, false, deviceItem, false);
            }
        }

        // 更新地址
        else if (v.getId() == R.id.tv_location) {
            v.startAnimation(mAnimClicked);
            DeviceItem deviceItem = (DeviceItem) ((View) v.getParent()).getTag();
            if (deviceItem == mMainDeviceInfo) {//主节点更新名称
                showGroupDilaog(deviceItem);
            } else {//子节点更新名称
                showGroupDilaog(deviceItem);
//                showInputDialog(false, true, deviceItem, false);//mDeviceType == DeviceType.PLC);
            }
        }

    }

    private void showCreateGroupDialog(final DeviceItem deviceItem) {
        InputDialog dialog = new InputDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(InputDialog.EXTRA_PASSWORD, false);
        bundle.putBoolean(InputDialog.KEY_CANCEL_ABLE, false);
        bundle.putString(Intent.EXTRA_TEXT, _getString(R.string.notice_new_group_name));
        dialog.setArguments(bundle);
        dialog.setFragmentListener(new OnFragmentLifeListener<String>() {

            @Override
            public void onChanged(String o) {
                if (o == null)
                    return;
                if (o.trim().length() == 0) {
                    showToast(R.string.notice_not_be_empty);
                    return;
                }
                doCreateGroup(o, deviceItem);
            }

        });
        if (getFragmentManager() != null) {
            dialog.show(getFragmentManager(), "group_create");
        }
    }

    /**
     * @param isMain      是否主设备
     * @param isLocation  是否位置
     * @param deviceItem  设备信息
     * @param englishOnly 只能输入英文
     */
    private void showInputDialog(final boolean isMain, final boolean isLocation, final DeviceItem deviceItem, boolean englishOnly) {
        InputDialog dialog = new InputDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(InputDialog.EXTRA_PASSWORD, false);
        bundle.putBoolean(InputDialog.KEY_CANCEL_ABLE, true);
        if (englishOnly)
            bundle.putBoolean(InputDialog.KEY_SUPPORT_EN_ONLY, true);
        bundle.putString(InputDialog.KEY_HINT, isLocation ? deviceItem.getLocation() : deviceItem.getDeviceName());
        bundle.putString(Intent.EXTRA_TEXT, _getString(isLocation ? R.string.notice_enter_new_group : R.string.notice_enter_new_name));
        dialog.setArguments(bundle);
        dialog.setFragmentListener(new OnFragmentLifeListener<String>() {

            @Override
            public void onChanged(String o) {
                if (o == null)
                    return;

                if (o.trim().length() == 0) {
                    showToast(R.string.notice_not_be_empty);
                    return;
                }

                if (isMain) {
                    if (!isLocation) {
                        doUpdateDeviceName(o);
                    }
                } else {
                    if (isLocation) {
                        doUpdatePlcDeviceCustomerInfo(deviceItem.getMac(), deviceItem.getDeviceName(), o);
                    } else {
                        doUpdatePlcDeviceCustomerInfo(deviceItem.getMac(), o, deviceItem.getLocation());
                    }

                }
            }

        });
        if (getFragmentManager() != null) {
            dialog.show(getFragmentManager(), "enter_new_name");
        }
    }

    private void showGroupDilaog(final DeviceItem deviceItem) {
        CharSequence[] mChoices = new CharSequence[mArrayGroup == null ? 0 : mArrayGroup.size() + 1];
        if (mArrayGroup != null)
            for (int i = 0; i < mArrayGroup.size(); i++) {
                Group item = mArrayGroup.get(i);
                mChoices[i] = item.getGroupName();
            }
        mChoices[mChoices.length - 1] = getAddGroupCharSequence();
        super.showDialogChoose(mChoices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mArrayGroup != null && i < mArrayGroup.size()) {
                    showNoticeOfChangeGroup(i, deviceItem);
                } else {
                    showCreateGroupDialog(deviceItem);
                }
            }
        });
    }

    private void showNoticeOfChangeGroup(int index, final DeviceItem deviceItem) {
        final Group group = mArrayGroup.get(index);
        if (deviceItem == mMainDeviceInfo)
            showAlert(
                    String.format(_getString(R.string.notice_device_join_group), group.getGroupName()),
                    _getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            doAdd2Group(group);
                        }
                    },
                    _getString(R.string.cancel), null, true);
        else
            doUpdatePlcDeviceCustomerInfo(deviceItem.getMac(), deviceItem.getDeviceName(), group.getGroupName());
    }

    private CharSequence getAddGroupCharSequence() {

        if (mAddStr == null) {
            SpannableString spannableString = new SpannableString(_getString(R.string.new_group));
            spannableString.setSpan(new ForegroundColorSpan(_getResources().getColor(R.color.colorAccent)), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            mAddStr = spannableString;
        }
        return mAddStr;
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
                        showProgressDialog(_getString(R.string.downloading), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        } else {
                            if (mDeviceType == DeviceType.PLC)
                                doGetPlcDeviceCustomerInfo();
                            else
                                doLoadGroup(true, 1, 10);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, BaseBeen<DeviceDetailBeen, Group> param) {
                        mPanelChilds.removeAllViews();
                        if (param == null)
                            return;
                        DeviceDetailBeen deviceBeen = param.getT1();
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
                            if (item.getType() == DeviceType.BWR) {
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
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    /**
     * 解析云端组网工况
     *
     * @param state 工况字符串
     * @param item  主设备信息
     */
    private void analysisMeshState(String state, DeviceItem item) {
        // 展示组网信息，包含子设备
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
            @SuppressLint("InflateParams") View view = LayoutInflater.from(mActivity).inflate(R.layout.item_device, null, false);
            view.setTag(netItemDeviceItem);
            refreshDeviceEditDisabled(view, netItemDeviceItem);
            mPanelChilds.addView(view, mPanelChilds.getChildCount() - 1);
        }
        mVSubordinateEffect.setVisibility(View.VISIBLE);
        mVSubordinateEffect.setMain(mVMainDevice);
        mHorizontalScrollView.setVisibility(View.VISIBLE);
    }

    /**
     * 解析云端千兆工况
     *
     * @param state 工况字符串
     * @param item  主设备信息
     */
    private void analysisRouterState(String state, DeviceItem item) {
        MeshStateBeen been = new Gson().fromJson(state, MeshStateBeen.class);
        if (item.getDeviceName() == null)
            item.setDeviceName(been.getStateValue().getSsid());
        item.setIp(been.getStateValue().getIp());
        item.setStaNum(been.getStateValue().getConnected_num());
    }

    /**
     * 解析云端电力猫工况
     *
     * @param state 工况字符串
     * @param item  主设备信息
     */
    private void analysisPlcState(String state, DeviceItem item) {
        PLCModemStateBeen been = new Gson().fromJson(state, PLCModemStateBeen.class);
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
            @SuppressLint("InflateParams") View view = LayoutInflater.from(mActivity).inflate(R.layout.item_device, null, false);
            view.setTag(netItemDeviceItem);
            refreshDevice(view, netItemDeviceItem);
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
     * 获取PLC子节点名称信息
     */
    private void doGetPlcDeviceCustomerInfo() {
        addTask(
                new DevcieCustomerInfoShowTask().execute(getGateway(), new TaskListener<DeviceCustormInfoBeen>() {
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
//                            onFragmentLifeListener.onChanged(null);
                        } else if (mArrayGroup == null || mArrayGroup.isEmpty())
                            doLoadGroup(true, 1, 10);
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, DeviceCustormInfoBeen param) {
                        if (param != null && param.getCustomer_info() != null && !param.getCustomer_info().isEmpty()) {
                            mCustomerInfo = param.getCustomer_info();
                            for (int i = 0; i < mPanelChilds.getChildCount(); i++) {
                                DeviceItem item = (DeviceItem) mPanelChilds.getChildAt(i).getTag();
                                String mac = item.getMac();
                                for (DeviceCustormInfoBeen.CuntomerInfo cuntomerInfo : param.getCustomer_info()) {
                                    if (mac.equalsIgnoreCase(cuntomerInfo.getMac())) {
                                        item.setDeviceName(cuntomerInfo.getDev_name());
                                        item.setLocation(cuntomerInfo.getDev_location());
                                        refreshDevice(mPanelChilds.getChildAt(i), item);
                                        break;
                                    }
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
     * 更新电力猫子节点信息
     */
    private void doUpdatePlcDeviceCustomerInfo(String mac, String deviceName, String deviceLocation) {
        Integer index = null;
        if (mCustomerInfo != null)
            for (DeviceCustormInfoBeen.CuntomerInfo cuntomerInfo : mCustomerInfo) {
                if (cuntomerInfo.getMac().equalsIgnoreCase(mac)) {
                    index = cuntomerInfo.getIndex();
                    break;
                }
            }

        addTask(
                new UpdateCustomerInfoTask().execute(index, mac, deviceName, deviceLocation, getGateway(), new TaskListener<DeviceCustormInfoBeen>() {
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
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        } else
                            doGetPlcDeviceCustomerInfo();
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, DeviceCustormInfoBeen param) {
                        if (param != null && param.getCustomer_info() != null && !param.getCustomer_info().isEmpty()) {
                            mCustomerInfo = param.getCustomer_info();
                            for (int i = 0; i < mPanelChilds.getChildCount(); i++) {
                                DeviceItem item = (DeviceItem) mPanelChilds.getChildAt(i).getTag();
                                String mac = item.getMac();
                                for (DeviceCustormInfoBeen.CuntomerInfo cuntomerInfo : param.getCustomer_info()) {
                                    if (mac.equalsIgnoreCase(cuntomerInfo.getMac())) {
                                        item.setDeviceName(cuntomerInfo.getDev_name());
                                        item.setLocation(cuntomerInfo.getDev_location());
                                        refreshDevice(mPanelChilds.getChildAt(i), item);
                                        break;
                                    }
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
     * 更新主节点名称
     */
    private void doUpdateDeviceName(final String deviceName) {
        addTask(
                new DeviceUpdateTask().execute(deviceName, mInfoFromApp, new TaskListener() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        } else {
                            mMainDeviceInfo.setDeviceName(deviceName);
                            ((TextView) mVMainDevice.findViewById(R.id.tv_name)).setText(deviceName);
                            showTaskError(task, R.string.commit_completed);
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

    private void doLoadGroup(final boolean isFirst, int currentPage, int pageSize) {
        if (isFirst) {
            mArrayGroup.clear();
        }
        addTask(
                new GetGroupListTask().execute(currentPage, pageSize, mInfoFromApp, new TaskListener<GroupListBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        if (isFirst)
                            showProgressDialog(_getString(R.string.downloading_group), isFirst, new DialogInterface.OnCancelListener() {
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
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, GroupListBeen param) {
                        if (param != null) {
                            mArrayGroup.addAll(param.getList());
                            if (param.getCurrentPage() < param.getTotalPage())
                                doLoadGroup(false, param.getCurrentPage() + 1, param.getPageSize());
                            else
                                hideProgressDialog();
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doCreateGroup(String groupName, final DeviceItem deviceItem) {
        addTask(
                new NewCreateGroupTask().execute(groupName, mInfoFromApp, new TaskListener<Group>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        } else {
                            showToast(R.string.binding_success);

                            if (deviceItem == mMainDeviceInfo)//完成添加后进行绑定操作
                                doAdd2Group(mArrayGroup.get(0));
                            else //子节点手动添加
                                doUpdatePlcDeviceCustomerInfo(deviceItem.getMac(), deviceItem.getDeviceName(), mArrayGroup.get(0).getGroupName());
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, Group param) {
                        mArrayGroup.add(0, param);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doAdd2Group(final Group group) {
        addTask(
                new DeviceGroupTask().execute(group.getUuid(), mInfoFromApp.getDevcieUuid(), mInfoFromApp, new TaskListener() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        } else {
                            showToast(R.string.commit_completed);
                            mMainDeviceInfo.setLocation(group.getGroupName());
                            ((TextView) mVMainDevice.findViewById(R.id.tv_location)).setText(group.getGroupName());
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
}
