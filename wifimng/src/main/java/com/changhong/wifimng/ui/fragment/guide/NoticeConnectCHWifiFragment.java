package com.changhong.wifimng.ui.fragment.guide;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.receiver.WifiReceiver;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.GetDeviceTypeTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.ui.fragment.BaseFragment;

public class NoticeConnectCHWifiFragment extends BaseFragment<BaseBeen<String, String>> implements View.OnClickListener, WifiReceiver.WifiReceiverListener {

    private WifiReceiver mReceiver;

    private CheckBox mCheckboxIcon;
    private Button mBtnConfirm;
    private View mBtnBack;

    /**
     * 当前已连接路由器类型
     */
    private String mCurrentDeviceType;
    private boolean isRunning;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new WifiReceiver(this);
        mCurrentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_con2ch_router, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mCheckboxIcon = view.findViewById(R.id.cb_icon);
        if (DeviceType.BWR.getName().equalsIgnoreCase(mCurrentDeviceType))
            mCheckboxIcon.setButtonDrawable(R.drawable.shape_mesh_cycle);
        else if (DeviceType.R2s.getName().equalsIgnoreCase(mCurrentDeviceType))
            mCheckboxIcon.setButtonDrawable(R.drawable.shape_router_cycle);
        else if (DeviceType.PLC.getName().equalsIgnoreCase(mCurrentDeviceType))
            mCheckboxIcon.setButtonDrawable(R.drawable.shape_plc_cycle);

        view.findViewById(R.id.btn_back).setOnClickListener(this);
        TextView btnChangeSSid = view.findViewById(R.id.btn_change_ssid);
        btnChangeSSid.getPaint().setUnderlineText(true);
        btnChangeSSid.setOnClickListener(this);

        mBtnConfirm = view.findViewById(R.id.btn_confirm);
        mBtnConfirm.setOnClickListener(this);

        mBtnBack = view.findViewById(R.id.btn_confirm);
        mBtnBack.setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);
        mReceiver.registReceiver(mActivity);

        if (DeviceType.PLC.getName().equalsIgnoreCase(mCurrentDeviceType)) {
            TextView tv_info1 = view.findViewById(R.id.tv_info1);
            tv_info1.setText(R.string.notice_con_ch_plc);
        }
    }

    @Override
    public void onResume() {
        if (!isHidden()) {
            if (getWifiManager().isWifiEnabled() && getWifiManager().getConnectionInfo().getNetworkId() != -1)
                doGetDeviceType();
        }
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            mReceiver.unregistReceiver(mActivity);
        } else {
            mReceiver.registReceiver(mActivity);
            if (getWifiManager().isWifiEnabled() && getWifiManager().getConnectionInfo().getNetworkId() != -1)
                doGetDeviceType();
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        mReceiver.unregistReceiver(mActivity);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(null);
        } else if (v.getId() == R.id.btn_change_ssid) {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        } else if (v.getId() == R.id.btn_confirm) {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(new BaseBeen(getGateway(), mCurrentDeviceType));
        }
    }

    private void refreshViewByState(boolean isConnect2CHRouter) {
        mCheckboxIcon.setChecked(isConnect2CHRouter);
        mBtnConfirm.setEnabled(isConnect2CHRouter);
        mBtnConfirm.setVisibility(isConnect2CHRouter ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPickWifiNetwork() {

    }

    @Override
    public void onScanResults(boolean isSuccess) {

    }

    @Override
    public void onRssiChange() {

    }

    @Override
    public void onConnectNetInfoChanged(NetworkInfo networkInfo) {
        Log.d(getClass().getSimpleName(), "====~ networkinfo = " + networkInfo.getDetailedState());
        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            doGetDeviceType();
        } else if (!networkInfo.isConnected()) {
            refreshViewByState(false);
        }
    }

    @Override
    public void onWifiStateChange(WifiReceiver.EnumWifiStatus status) {

    }

    /**
     * 获取设备类型
     */
    private void doGetDeviceType() {
        if (isRunning)
            return;
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addTask(new GetDeviceTypeTask().execute(getGateway(), _getString(R.string.illegal_device),
                new TaskListener<String>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        isRunning = true;
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        isRunning = false;
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.no_device_type_receive);
                            refreshViewByState(false);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, String param) {
//                        if (param == null)
//                            Preferences.getInstance(mActivity).remove(KeyConfig.KEY_DEVICE_TYPE);
//                        else
//                            Preferences.getInstance(mActivity).saveString(KeyConfig.KEY_DEVICE_TYPE, param);
//                        mCurrentDeviceType = param;

                        //判断当前设备和目标设备是否一致
                        if (param == null || !param.equalsIgnoreCase(mCurrentDeviceType))
                            refreshViewByState(false);
                        else {
                            refreshViewByState(true);
                        }

                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        isRunning = false;
                    }
                })
        );
    }

}
