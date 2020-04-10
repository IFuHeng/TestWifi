package com.changhong.wifimng.ui.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.DeviceItem;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.StaInfo;
import com.changhong.wifimng.been.WanType;
import com.changhong.wifimng.been.mesh.ListInfo;
import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.been.sys.SettingResponseAllBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.GetRouterInfoTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.GetPLCLinkInfoTask;
import com.changhong.wifimng.task.router.GetMeshNetworkTask;
import com.changhong.wifimng.task.router.GetStaInfoTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.adapter.ClientAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceDetailOptionFragment extends BaseFragment<BaseBeen<StaInfo, DeviceType>> implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ListView mListDevice;

    private TextView mTvSsid;
    private TextView mTvIp;
    private TextView tv_mac;
    private TextView tv_networktype;
    private TextView tv_device_type;
    private TextView tv_state;

    private ClientAdapter mAdapter;
    private List<StaInfo> mListClient;

    private DeviceItem mDeviceInfo;

    private boolean isFirst = true;

    private Drawable mDrawableGreen;
    private Drawable mDrawableRed;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDeviceInfo = getArguments().getParcelable(KeyConfig.KEY_DEVICE_INFO);

        mListClient = new ArrayList<>();

        mDrawableGreen = getResources().getDrawable(R.drawable.shape_bg_number_round_green);
        mDrawableRed = getResources().getDrawable(R.drawable.shape_bg_number_round_red);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_detail_option, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        view.findViewById(R.id.btn_back).setOnClickListener(this);

        //设置图标
        ImageView icon = view.findViewById(R.id.icon);
        {
            Drawable drawable = getResources().getDrawable(mDeviceInfo.getType().getIconResId());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable = DrawableCompat.wrap(drawable);
                drawable.setTint(0xff2B5A97);
            }
            icon.setImageDrawable(drawable);
        }

        mTvSsid = view.findViewById(R.id.tv_ssid);
        mTvIp = view.findViewById(R.id.tv_ip);
        tv_mac = view.findViewById(R.id.tv_mac);
        tv_networktype = view.findViewById(R.id.tv_networktype);
        tv_device_type = view.findViewById(R.id.tv_device_type);
        tv_state = view.findViewById(R.id.tv_state);
        refreshState();

        mListDevice = view.findViewById(R.id.list_device);
        mAdapter = new ClientAdapter(mActivity, mListClient);
        mListDevice.setAdapter(mAdapter);
        mListDevice.setOnItemClickListener(this);
        refreshList();

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            reloadDeviceInfo();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            reloadDeviceInfo();
        }
    }

    private void reloadDeviceInfo() {
        if (mDeviceInfo.getType() == DeviceType.BWR) {
            if (mDeviceInfo.isChild()) {
                doGetMeshNetState();
                doGetRouterInfo();
            } else
                doGetStaInfo();
        } else if (mDeviceInfo.getType() == DeviceType.R2s) {
            doGetStaInfo();
        } else if (mDeviceInfo.getType() == DeviceType.PLC) {
            doGetPlcInfo();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(null);
        }
    }

    private void refreshState() {

        //设置名字
        String ssid = mDeviceInfo.getDeviceName();
//        if (mDeviceInfo.getType() == DeviceType.BWR || mDeviceInfo.getType() == DeviceType.PLC) {
//            if (!mDeviceInfo.isChild()) {
//                ssid += "\n" + getString(R.string.mainNode);
//            } else {
//                ssid += "\n" + getString(R.string.childNode);
//            }
//            mTvSsid.setText(ssid);
//        } else
        mTvSsid.setText(ssid);
        if (mDeviceInfo.getLocation() != null) {
            SpannableString ss = new SpannableString('\n' + mDeviceInfo.getLocation());
            ss.setSpan(new RelativeSizeSpan(0.8f), 0, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            mTvSsid.append(ss);
        }

        //设置上级节点 和 子节点的状态
        if (mDeviceInfo.isChild()) {
            tv_device_type.setText(R.string.childNode);
            setState(true);
        } else if (mDeviceInfo.getType() == DeviceType.PLC) {
            tv_device_type.setText(R.string.mainNode);
            setState(mDeviceInfo.getUpConnected() != null && mDeviceInfo.getUpConnected());
        } else {
            tv_device_type.setText(R.string.mainNode);
            //设置路由或主节点的状态
            setState(mDeviceInfo.getUpConnected() != null && mDeviceInfo.getUpConnected() && mDeviceInfo.getWan_type() != null);
        }

        //设置IP地址、mac地址
        mTvIp.setText(mDeviceInfo.getIp());
        tv_mac.setText(mDeviceInfo.getMac());
        //设置wan口联网方式
        if (mDeviceInfo.getWan_type() != null)
            tv_networktype.setText(mDeviceInfo.getWan_type().getName());
    }

    private void setState(boolean isEnable) {
        if (tv_state.getTag() != null && (boolean) tv_state.getTag() == isEnable) {
            return;
        }
        Drawable drawable = tv_state.getCompoundDrawables()[0];
        Drawable newDrawable = (isEnable ? mDrawableGreen : mDrawableRed);
        newDrawable.setBounds(drawable.getBounds());
        //设置路由或主节点的状态
        tv_state.setText(isEnable ? R.string.regular : R.string.abnormal);
        tv_state.setCompoundDrawables(newDrawable, null, null, null);
    }

    private void refreshList() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onFragmentLifeListener.onChanged(new BaseBeen(mListClient.get(position), mDeviceInfo.getType()));
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
                        if (isFirst) {
                            isFirst = false;
                            showProgressDialog(_getString(R.string.note_mesh_info_request), false, null);
                        }
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
                            if (!param.isEmpty() ||
                                    (param.isEmpty() && mDeviceInfo.isChild())) {
                                for (ListInfo listInfo : param) {
                                    if (listInfo.getMac().equals(mDeviceInfo.getMac())) {
                                        mListClient.clear();
                                        for (StaInfo staInfo : listInfo.getSta_info()) {
                                            if (staInfo.getMac() != null)
                                                mListClient.add(staInfo);
                                        }
                                        refreshList();
                                        break;
                                    }
                                }
                                return;
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

    private void doGetStaInfo() {
        addTask(
                new GetStaInfoTask().execute(getGateway(), getCookie(), new TaskListener<List<StaInfo>>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        if (isFirst) {
                            isFirst = false;
                            showProgressDialog(_getString(R.string.downloading), true, new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    task.cancel(true);
                                    onFragmentLifeListener.onChanged(null);
                                }
                            });
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
                    public void onProgressUpdate(GenericTask task, List<StaInfo> param) {
                        if (param != null) {
                            mListClient.clear();
                            for (StaInfo staInfo : param) {
                                if (staInfo.getMac() != null)
                                    mListClient.add(staInfo);
                            }
                            refreshList();
                            if (!mDeviceInfo.isChild())
                                doGetRouterInfo();
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
     * 获取本地路由器设备的信息，对比mac地址
     */
    private void doGetRouterInfo() {
        addTask(
                new GetRouterInfoTask().execute(getGateway(), mDeviceInfo.getType().getName(), getCookie(), new TaskListener<SettingResponseAllBeen>() {
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
                    public void onProgressUpdate(GenericTask task, SettingResponseAllBeen param) {
                        if (param != null) {
//                            if (!mDeviceInfo.isChild()) {
//                                if (!param.isLinkOn())
//                                    tv_pre_node.setText(param.getWan_ip());
//                                tv_networktype.setText(param.getWan_type());
                            mDeviceInfo.setUpConnected(param.isLinkOn());
                            mDeviceInfo.setWan_type(WanType.getDeviceTypeFromName(param.getWan_type()));
                            refreshState();
//                            }
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
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
                        if (isFirst) {
                            isFirst = false;
                            showProgressDialog(_getString(R.string.downloading), false, null);
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
                    public void onProgressUpdate(GenericTask task, PLCInfo param) {
                        if (param != null) {
                            mListClient.clear();
                            for (PLCInfo.Dev_Info dev_info : param.getDev_info()) {
                                //判断
                                if (dev_info.getStatus() != 0 && mDeviceInfo.getMac().equalsIgnoreCase(dev_info.getPlc_node()))
                                    mListClient.add(dev_info.turn2StaInfo());
                            }
                            refreshList();

                            //设置路由或主节点的状态
                            setState(mDeviceInfo.getUpConnected() != null && mDeviceInfo.getUpConnected() && param.getAddr_type() != null);
                            tv_networktype.setText(WanType.getDeviceTypeFromTypeCode(param.getAddr_type()).getName());
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }
}
