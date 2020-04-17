package com.changhong.wifimng.ui.fragment.guide;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
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
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.activity.WifiHomeActivity;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.uttils.WifiUtils;

public class WizardCompleteFragment extends BaseFragment<Boolean> implements View.OnClickListener {

    private CheckBox mCheckboxIcon;
    private Button mBtnConfirm;

    /**
     * 当前已连接路由器类型
     */
    private String mCurrentDeviceType;
    private String mSSid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
        mSSid = getArguments().getString(KeyConfig.KEY_SSID);
        if (TextUtils.isEmpty(mSSid))
            onFragmentLifeListener.onChanged(null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_completed, container, false);
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

        TextView tvSSid = view.findViewById(R.id.tv_ssid);
        tvSSid.setText(mSSid);
        TextView tvPassword = view.findViewById(R.id.tv_password);
        tvPassword.setText(getArguments().getString(KeyConfig.KEY_PASSWORD));

        mBtnConfirm = view.findViewById(R.id.btn_confirm);
        mBtnConfirm.setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        if (!isHidden()) {
            refreshUi(getWifiManager().isWifiEnabled()
                    && getWifiManager().getConnectionInfo() != null
                    && getWifiManager().getConnectionInfo().getNetworkId() != -1
                    && mSSid.equals(WifiUtils.clearSSID(getWifiManager().getConnectionInfo().getSSID())));
        }
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
        } else {
            refreshUi(getWifiManager().isWifiEnabled()
                    && getWifiManager().getConnectionInfo() != null
                    && getWifiManager().getConnectionInfo().getNetworkId() != -1
                    && mSSid.equals(WifiUtils.clearSSID(getWifiManager().getConnectionInfo().getSSID())));
        }
        super.onHiddenChanged(hidden);
    }

    public void refreshUi(boolean isConnect2CHRouter) {
        if (mBtnConfirm != null)
            if (isConnect2CHRouter) {
                mBtnConfirm.setText(R.string.complete);
                mBtnConfirm.setTag(true);
            } else {
                mBtnConfirm.setText(R.string.connect_2_wifi);
                mBtnConfirm.setTag(false);
            }
        if (mCheckboxIcon != null)
            mCheckboxIcon.setChecked(isConnect2CHRouter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            onFragmentLifeListener.onChanged(null);
        } else if (v.getId() == R.id.btn_confirm) {
            if (v.getTag() != null && (Boolean) v.getTag())
                onFragmentLifeListener.onChanged(true);
            else
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
    }

    @Override
    public boolean onNetworkChange(NetworkInfo networkInfo) {
        super.onNetworkChange(networkInfo);
        if (isDetached())
            return false;
        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            refreshUi(mSSid.equals(WifiUtils.clearSSID(networkInfo.getExtraInfo())));
        } else if (!networkInfo.isConnected()) {
            refreshUi(false);
        }
        return true;
    }

}
