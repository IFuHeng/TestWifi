package com.changhong.wifimng.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen3;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.receiver.WifiReceiver;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.fragment.EnumPage;
import com.changhong.wifimng.ui.fragment.MeshSettingFragment;
import com.changhong.wifimng.ui.fragment.OnFragmentLifeListener;
import com.changhong.wifimng.ui.fragment.PLCSettingFragment;
import com.changhong.wifimng.ui.fragment.RouterSettingFragment;
import com.changhong.wifimng.ui.fragment.TestInternetTimeLimitFragment;
import com.changhong.wifimng.ui.fragment.TestLimitSpeedListFragment;
import com.changhong.wifimng.ui.fragment.setting.AccessListFragment;
import com.changhong.wifimng.ui.fragment.setting.AdminPasswordFragment;
import com.changhong.wifimng.ui.fragment.setting.DDNSFragment;
import com.changhong.wifimng.ui.fragment.setting.DeviceNameAndRoomFragment;
import com.changhong.wifimng.ui.fragment.setting.DeviceShareFragment;
import com.changhong.wifimng.ui.fragment.setting.GroupSettingFragment;
import com.changhong.wifimng.ui.fragment.setting.GuestNetworkFragment;
import com.changhong.wifimng.ui.fragment.setting.LanSettingFragment;
import com.changhong.wifimng.ui.fragment.setting.NetworkFragment;
import com.changhong.wifimng.ui.fragment.setting.NetworkMenuFragment;
import com.changhong.wifimng.ui.fragment.setting.PLCWifiSettingFragment;
import com.changhong.wifimng.ui.fragment.setting.PlcGroupSettingFragment;
import com.changhong.wifimng.ui.fragment.setting.StaGroupSettingFragment;
import com.changhong.wifimng.ui.fragment.setting.WifiAdvanceSettingFragment;
import com.changhong.wifimng.ui.fragment.setting.WifiBaseSettingFragment;
import com.changhong.wifimng.ui.fragment.setting.WlanAccessFragment;

public class RouterSettingActivity extends BaseWifiActivtiy implements WifiReceiver.WifiReceiverListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Preferences.getInstance(getApplicationContext()).remove(KeyConfig.KEY_COOKIE_SSID);

        setContentView(R.layout.activity_wizard_setting);

        gotoSettingMain();

        setWifiStateChangeListenerOnOrOff(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver.registReceiver(this);
    }

    @Override
    protected void onPause() {
        mReceiver.unregistReceiver(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        setWifiStateChangeListenerOnOrOff(false);
        super.onDestroy();
    }

    /**
     * @param page   前往的页面
     * @param params 参数数组 String[]
     */
    private void gotoPage(EnumPage page, String... params) {
        if (page == EnumPage.WIFI_SETTING) {
            if (DeviceType.PLC.getName().equals(mInfoFromApp.getDeviceType()))
                gotoDefaultPage(PLCWifiSettingFragment.class);
            else
                gotoWifiSetting();
        } else if (page == EnumPage.WIFI_SETTING_ADVANCE) {
            gotoDefaultPage(WifiAdvanceSettingFragment.class);
        } else if (page == EnumPage.WAN_SETTING) {
            gotoDefaultPage(NetworkFragment.class);
        } else if (page == EnumPage.LAN_SETTING) {
            gotoDefaultPage(LanSettingFragment.class);
        } else if (page == EnumPage.ADMIN_PASSWORD) {
            gotoAdminPassword(params[0]);
        } else if (page == EnumPage.GUEST_NETWORK) {
            gotoDefaultPage(GuestNetworkFragment.class);
        } else if (page == EnumPage.WLAN_ACCESS) {
            gotoDefaultPage(WlanAccessFragment.class);
        } else if (page == EnumPage.ACCESS_LIST) {
            gotoDefaultPage(AccessListFragment.class);
        } else if (page == EnumPage.NET_MENU) {
            gotoNetMenu();
        } else if (page == EnumPage.GROUP_SETTING) {
//            gotoDefaultPageWithoutWifiListener(GroupSettingFragment.class);
            if (DeviceType.PLC.getName().equals(mInfoFromApp.getDeviceType()))
                gotoDefaultPage(PlcGroupSettingFragment.class);
            else
                gotoDefaultPage(StaGroupSettingFragment.class);
        } else if (page == EnumPage.DEVICE_SHARE) {
            gotoDefaultPageWithoutWifiListener(DeviceShareFragment.class);
        } else if (page == EnumPage.DEVICE_NAME_AND_ROOM) {
            gotoDefaultPage(DeviceNameAndRoomFragment.class);
        } else if (page == EnumPage.DDNS_SHOW) {
            gotoDefaultPage(DDNSFragment.class);
        } else if (page == EnumPage.TEST_INTERNET_TIME_LIMIT_LIST) {
            gotoDefaultPage(TestInternetTimeLimitFragment.class);
        } else if (page == EnumPage.TEST_SPEED_LIMIT_LIST) {
            gotoDefaultPage(TestLimitSpeedListFragment.class);
        }
    }

    private void gotoDefaultPage(Class<? extends BaseFragment> fragmentClass) {
        try {
            BaseFragment fragment = fragmentClass.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(KeyConfig.KEY_DEVICE_TYPE, mInfoFromApp.getDeviceType());
            bundle.putParcelable(KeyConfig.KEY_INFO_FROM_APP, mInfoFromApp);
            fragment.setArguments(bundle);
            fragment.setOnFragmentLifeListener(new OnFragmentLifeListener() {
                @Override
                public void onChanged(Object been) {
                    if (been == null)
                        backFragment();
                    else if (been instanceof EnumPage) {
                        gotoPage((EnumPage) been);
                    }
                }

            });
            startFragment(fragment);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void gotoDefaultPageWithoutWifiListener(Class<? extends BaseFragment> fragmentClass) {
        setWifiStateChangeListenerOnOrOff(false);
        try {
            BaseFragment fragment = fragmentClass.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(KeyConfig.KEY_DEVICE_TYPE, mInfoFromApp.getDeviceType());
            bundle.putParcelable(KeyConfig.KEY_INFO_FROM_APP, mInfoFromApp);
            fragment.setArguments(bundle);
            fragment.setOnFragmentLifeListener(new OnFragmentLifeListener() {
                @Override
                public void onChanged(Object been) {
                    if (been == null)
                        backFragment();
                    else if (been instanceof EnumPage) {
                        gotoPage((EnumPage) been);
                    }
                    setWifiStateChangeListenerOnOrOff(true);
                }

            });
            startFragment(fragment);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param deviceType 设备类型，主要区分电力猫 和 组网/千兆
     */
    private void gotoAdminPassword(String deviceType) {
        BaseFragment fragment = new AdminPasswordFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KeyConfig.KEY_DEVICE_TYPE, deviceType);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener() {

            @Override
            public void onChanged(Object been) {
                if (been == null)
                    backFragment();
                else if (been instanceof EnumPage) {
                    gotoPage((EnumPage) been);
                }
            }
        });
        startFragment(fragment);
    }

    private void gotoSettingMain() {
        BaseFragment fragment;
        if (DeviceType.BWR.getName().equalsIgnoreCase(mInfoFromApp.getDeviceType())) {
            fragment = new MeshSettingFragment();
        } else if (DeviceType.PLC.getName().equalsIgnoreCase(mInfoFromApp.getDeviceType())) {
            fragment = new PLCSettingFragment();
        } else
            fragment = new RouterSettingFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KeyConfig.KEY_INFO_FROM_APP, mInfoFromApp);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener<BaseBeen3<EnumPage, String, String>>() {
            @Override
            public void onChanged(BaseBeen3<EnumPage, String, String> been) {
                if (been == null)
                    backFragment();
                else
                    gotoPage(been.getT1(), been.getT2(), been.getT3());
            }

        });
        startFragment(fragment);
    }

    private void gotoNetMenu() {
        BaseFragment fragment = new NetworkMenuFragment();
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener() {


            @Override
            public void onChanged(Object been) {
                if (been == null)
                    backFragment();
                else if (been instanceof EnumPage) {
                    gotoPage((EnumPage) been);
                }
            }


        });
        startFragment(fragment);
    }

    private void gotoWifiSetting() {
        BaseFragment fragment = new WifiBaseSettingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KeyConfig.KEY_DEVICE_TYPE, mInfoFromApp.getDeviceType());
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener() {
            @Override
            public void onChanged(Object been) {
                if (been == null)
                    backFragment();
                else if (been instanceof EnumPage) {
                    EnumPage value = (EnumPage) been;
                    if (value == EnumPage.WIFI_SETTING_ADVANCE) {
                        gotoDefaultPage(WifiAdvanceSettingFragment.class);
                    }
                }
            }


        });
        startFragment(fragment);
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
        if (!networkInfo.isConnected()) {
            // TODO 断开链接就退出
            if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                Preferences.getInstance(getApplicationContext()).remove(KeyConfig.KEY_DEVICE_TYPE);
                showAlert(getString(R.string.notice_wifi_disconnect_and_exit), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        LocalBroadcastManager.getInstance(RouterSettingActivity.this).sendBroadcast(new Intent(ACTION_EXIT_APP));
                    }
                }, false);
//                new AlertDialog.Builder(getApplicationContext()).setTitle(R.string._exit).setMessage("已断开wifi连接，退出设置").setPositiveButton(R.string.confirm, null).create().show();

            }
        }
    }

    @Override
    public void onWifiStateChange(WifiReceiver.EnumWifiStatus status) {

    }

}
