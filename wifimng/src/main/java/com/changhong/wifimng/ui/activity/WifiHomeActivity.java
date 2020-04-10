package com.changhong.wifimng.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.BaseBeen3;
import com.changhong.wifimng.been.DeviceItem;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.StaInfo;
import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.ui.fragment.AddMeshFragment;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.fragment.ClientDetailFragment;
import com.changhong.wifimng.ui.fragment.DeviceDetailOptionFragment;
import com.changhong.wifimng.ui.fragment.EnumPage;
import com.changhong.wifimng.ui.fragment.OnFragmentLifeListener;
import com.changhong.wifimng.ui.fragment.SpeedLimitFragment;
import com.changhong.wifimng.ui.fragment.WifiHome1Fragment;
import com.changhong.wifimng.ui.fragment.WifiHomeFragment;
import com.changhong.wifimng.ui.fragment.setting.AccessListFragment;
import com.changhong.wifimng.ui.fragment.setting.WlanAccessFragment;

public class WifiHomeActivity extends BaseWifiActivtiy {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_wizard_setting);

        if (getIntent().getBooleanExtra(KeyConfig.KEY_IS_ADD_CHILD, false)) {
            DeviceItem been = new DeviceItem();
            been.setDeviceName(mInfoFromApp.getDevcieUuid());
            been.setType(DeviceType.getDeviceTypeFromName(mInfoFromApp.getDeviceType()));
            been.setMac(mInfoFromApp.getMac());
            gotoAddMesh(been);
        } else {
            Preferences.getInstance(getApplicationContext()).remove(KeyConfig.KEY_COOKIE_SSID);
            gotoWifiHome();
        }

        setWifiStateChangeListenerOnOrOff(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!getWifiManager().isWifiEnabled() || getWifiManager().getConnectionInfo().getNetworkId() == -1)
            startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), REQUEST_CODE_WIFI_SETTING);
    }

    @Override
    protected void onDestroy() {
        setWifiStateChangeListenerOnOrOff(false);
        super.onDestroy();
    }

    private void gotoWifiHome() {
        BaseFragment fragment;
        if (mInfoFromApp != null) {//当从云端来的数据的时候
            fragment = new WifiHomeFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(KeyConfig.KEY_INFO_FROM_APP, mInfoFromApp);
            fragment.setArguments(bundle);
        } else {//当本地的时候
            fragment = new WifiHome1Fragment();
        }
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener<BaseBeen<EnumPage, Object>>() {
            @Override
            public void onChanged(BaseBeen<EnumPage, Object> been) {
                if (been == null)
                    finish();
                else if (been.getT1() == EnumPage.ROUTER_DEVICE_INFO) {
                    gotoDeviceDetail((DeviceItem) been.getT2());
                } else if (been.getT1() == EnumPage.ROUTER_SETTING) {
                    if (mInfoFromApp == null)
                        gotoSetting((Whole2LocalBeen) been.getT2());
                    else
                        gotoSetting(mInfoFromApp);
                } else if (been.getT1() == EnumPage.ADD_MESH) {
                    gotoAddMesh((DeviceItem) been.getT2());
                }
            }


        });
        startFragment(fragment);
    }

    private void gotoAddMesh(DeviceItem been) {
        BaseFragment fragment = new AddMeshFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KeyConfig.KEY_DEVICE_INFO, been);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener() {

            @Override
            public void onChanged(Object been) {
                if (been == null)
                    backFragment();
                else if (been instanceof StaInfo) {
                }
            }


        });
        startFragment(fragment);
    }

    private void gotoDeviceDetail(DeviceItem been) {
        DeviceDetailOptionFragment fragment = new DeviceDetailOptionFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KeyConfig.KEY_DEVICE_INFO, been);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener<BaseBeen<StaInfo, DeviceType>>() {

            @Override
            public void onChanged(BaseBeen<StaInfo, DeviceType> been) {
                if (been == null)
                    backFragment();
                else {
                    gotoClientDetail(been);
                }
            }


        });
        startFragment(fragment);
    }

    private void gotoClientDetail(BaseBeen<StaInfo, DeviceType> been) {
        ClientDetailFragment fragment = new ClientDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KeyConfig.KEY_DEVICE_INFO, been.getT1());
        bundle.putString(KeyConfig.KEY_DEVICE_TYPE, been.getT2().getName());

        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener<BaseBeen3<EnumPage, String, String>>() {

            @Override
            public void onChanged(BaseBeen3<EnumPage, String, String> been) {
                if (been == null)
                    backFragment();
                else if (been.getT1() == EnumPage.SPEED_LIMIT) {
                    gotoSpeedLimit(been.getT2(), been.getT3());
                } else if (been.getT1() == EnumPage.WLAN_ACCESS) {
                    gotoWlanAccess(been.getT3());
                }
            }
        });
        startFragment(fragment);
    }

    /**
     * 宽带限速
     *
     * @param mac
     * @param deviceType
     */
    private void gotoSpeedLimit(String mac, String deviceType) {
        BaseFragment fragment = new SpeedLimitFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KeyConfig.KEY_DEVICE_MAC, mac);
        bundle.putString(KeyConfig.KEY_DEVICE_TYPE, deviceType);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener() {

            @Override
            public void onChanged(Object been) {
                if (been == null)
                    backFragment();

            }
        });
        startFragment(fragment);
    }

    /**
     * 访问控制
     *
     * @param deviceType
     */
    private void gotoWlanAccess(final String deviceType) {
        BaseFragment fragment = new WlanAccessFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KeyConfig.KEY_DEVICE_TYPE, deviceType);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener() {

            @Override
            public void onChanged(Object been) {
                if (been == null)
                    backFragment();
                else if (been instanceof EnumPage) {
                    gotoAccessList(deviceType);
                }
            }
        });
        startFragment(fragment);
    }

    /**
     * 控制列表
     *
     * @param deviceType
     */
    private void gotoAccessList(String deviceType) {
        BaseFragment fragment = new AccessListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KeyConfig.KEY_DEVICE_TYPE, deviceType);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener() {

            @Override
            public void onChanged(Object been) {
                if (been == null)
                    backFragment();
            }
        });
        startFragment(fragment);
    }

    private void gotoSetting(Whole2LocalBeen t2) {
        Intent intent = new Intent(this, RouterSettingActivity.class);
        intent.putExtra(KeyConfig.KEY_INFO_FROM_APP, t2);
        startActivity(intent);
    }

    @Override
    protected void dealActionUnauthorized() {
        showAlert(getString(R.string.token_expired), getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        }, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_WIFI_SETTING) {
            if (!getWifiManager().isWifiEnabled() || getWifiManager().getConnectionInfo().getNetworkId() == -1) {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnectNetInfoChanged(NetworkInfo networkInfo) {
        if (!networkInfo.isConnected()) {
            // TODO 断开链接就退出
            if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                Preferences.getInstance(getApplicationContext()).remove(KeyConfig.KEY_DEVICE_TYPE);
                showAlert(getString(R.string.notice_wifi_disconnect_and_exit), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        LocalBroadcastManager.getInstance(WifiHomeActivity.this).sendBroadcast(new Intent(ACTION_EXIT_APP));
                    }
                }, false);
            }
        }
    }
}
