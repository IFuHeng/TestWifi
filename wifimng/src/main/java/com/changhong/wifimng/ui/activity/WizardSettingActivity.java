package com.changhong.wifimng.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.guide.RequireAllBeen;
import com.changhong.wifimng.been.guide.ResponseAllBeen;
import com.changhong.wifimng.been.sys.SettingResponseAllBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.GetRouterInfoTask;
import com.changhong.wifimng.task.GetWizardWifiTask;
import com.changhong.wifimng.task.GetWizardWlanTask;
import com.changhong.wifimng.task.LoginTask;
import com.changhong.wifimng.task.SetRouterPasswordTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.CheckWizardGuidTask;
import com.changhong.wifimng.task.router.WizardSettingCompleteTask;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.fragment.EnumPage;
import com.changhong.wifimng.ui.fragment.InputDialog;
import com.changhong.wifimng.ui.fragment.OnFragmentLifeListener;
import com.changhong.wifimng.ui.fragment.guide.NoticeConnectCHWifiFragment;
import com.changhong.wifimng.ui.fragment.guide.RouterPasswordFragment;
import com.changhong.wifimng.ui.fragment.guide.WizardCompleteFragment;
import com.changhong.wifimng.ui.fragment.guide.WizardNetworkFragment;
import com.changhong.wifimng.ui.fragment.guide.WizardWifiFragment;
import com.changhong.wifimng.ui.fragment.guide.CustomGroupAndNameFragment;
import com.changhong.wifimng.ui.fragment.setting.DeviceShareFragment;

import java.util.List;

public class WizardSettingActivity extends BaseWifiActivtiy {


    private RequireAllBeen mBeenLan;
    private RequireAllBeen mBeenWifi;

    private boolean isLogin = false;

    /**
     * 当前设备类型
     */
    private String mCurrentDeviceType;
    /**
     * 配置状态代码
     * 进入此页面的状态： 0、未配置 ； 1、已完成配置 ； 2、仅完成密码配置 ；其它、初始化绑定
     */
    private int mGuideState;

    /**
     * 是否已绑定
     */
    private boolean isBinded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard_setting);

        mGuideState = getIntent().getIntExtra(KeyConfig.KEY_GUIDE_STATE, -1);
        mCurrentDeviceType = getIntent().getStringExtra(KeyConfig.KEY_DEVICE_TYPE);
        if (mCurrentDeviceType == null && mInfoFromApp != null)
            mCurrentDeviceType = mInfoFromApp.getDeviceType();
        isBinded = getIntent().getBooleanExtra(KeyConfig.KEY_DEVICE_BINDED, false);
        doSwitchPageByStateWizard(mGuideState);
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

    private void gotoWanFragment(RequireAllBeen param) {
        WizardNetworkFragment fragment = new WizardNetworkFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Intent.EXTRA_TEXT, param);
        bundle.putString(KeyConfig.KEY_DEVICE_TYPE, mCurrentDeviceType);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener<RequireAllBeen>() {


            @Override
            public void onChanged(RequireAllBeen been) {
                if (been == null)
                    finish();
                else {
                    mBeenLan = been;
                    doGetWifiInfo();
                }
            }

        });
        startFragment(fragment);
    }

    private void gotoWifiFragment(RequireAllBeen param) {
        WizardWifiFragment fragment = new WizardWifiFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KeyConfig.KEY_DEVICE_TYPE, mCurrentDeviceType);
        if (param != null) {
            bundle.putParcelable(Intent.EXTRA_TEXT, param);
        }
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener<RequireAllBeen>() {


            @Override
            public void onChanged(RequireAllBeen been) {
                if (been == null)
                    backFragment();
                else {
                    mBeenWifi = been;
                    doCommit();
                }
            }
        });
        startFragment(fragment);
    }


    private void gotoNoticeConnectFragment() {
        if (mCurFragment != null && mCurFragment instanceof NoticeConnectCHWifiFragment)
            return;
        setWifiStateChangeListenerOnOrOff(true);
        NoticeConnectCHWifiFragment fragment = new NoticeConnectCHWifiFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KeyConfig.KEY_DEVICE_TYPE, mCurrentDeviceType);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener<BaseBeen<String, String>>() {

            @Override
            public void onChanged(BaseBeen<String, String> been) {
                if (been == null) {
//                    backFragment();
                    finish();
                } else {
                    mCurrentDeviceType = been.getT2();
                    doCheckWizardGuideTask();
                }
            }

        });
        startFragment(fragment);
    }

    private void gotoPasswordFragment() {
        setWifiStateChangeListenerOnOrOff(true);
        RouterPasswordFragment fragment = new RouterPasswordFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KeyConfig.KEY_DEVICE_TYPE, mCurrentDeviceType);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener<String>() {

            @Override
            public void onChanged(String string) {
                if (string == null) {
                    backFragment();
                } else {
                    doCommitRouterPassowrd(string);
                }
            }

        });
        startFragment(fragment);
    }

    private void gotoBindPageFragment(String mac) {
        //此页面可以关闭wifi监听了。
        backToConnectNoticePage();
        setWifiStateChangeListenerOnOrOff(true);

        mInfoFromApp.setMac(mac);
        BaseFragment fragment = new CustomGroupAndNameFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KeyConfig.KEY_INFO_FROM_APP, mInfoFromApp);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener<BaseBeen<EnumPage, String>>() {

            @Override
            public void onChanged(BaseBeen<EnumPage, String> been) {
                if (been == null) {
                    finish();
                } else if (been.getT1() == EnumPage.WIZARD_FIRST) {
                    backFragment();
                } else if (been.getT1() == EnumPage.DEVICE_SHARE) {
                    gotoSharePage(been.getT2());
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            }

        });
        startFragment(fragment);
    }

    private void gotoWizardCompleteFragment(String ssid, String password) {
        backToConnectNoticePage(true);
        //此页面可以关闭wifi监听了。
        BaseFragment fragment = new WizardCompleteFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KeyConfig.KEY_INFO_FROM_APP, mInfoFromApp);
        bundle.putString(KeyConfig.KEY_DEVICE_TYPE, mCurrentDeviceType);
        bundle.putString(KeyConfig.KEY_SSID, ssid);
        bundle.putString(KeyConfig.KEY_PASSWORD, password);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener<Boolean>() {

            @Override
            public void onChanged(Boolean been) {
                if (been == null || !been) {
                    finish();
                } else {
                    if (!isBinded) {
                        doGetRouterInfo();
                    } else {
                        setResult(RESULT_OK);
                        finish();
                    }
                }
            }

        });
        startFragment(fragment);

        setWifiStateChangeListenerOnOrOff(true);
    }

    private void gotoSharePage(String deviceName) {
        setWifiStateChangeListenerOnOrOff(false);
        BaseFragment fragment = new DeviceShareFragment();
        Bundle bundle = new Bundle();
        mInfoFromApp.setDeviceName(deviceName);
        bundle.putString(KeyConfig.KEY_DEVICE_TYPE, mInfoFromApp.getDeviceType());
        bundle.putParcelable(KeyConfig.KEY_INFO_FROM_APP, mInfoFromApp);
        fragment.setArguments(bundle);
        fragment.setOnFragmentLifeListener(new OnFragmentLifeListener() {
            @Override
            public void onChanged(Object been) {
                setResult(RESULT_OK);
                finish();
            }
        });
        startFragment(fragment);
    }

    /**
     * 完成引导
     */
    private void doCommit() {

        addTask(new WizardSettingCompleteTask().execute(this, getGateway(), mCurrentDeviceType, mBeenLan, mBeenWifi, getCookie(),
                new TaskListener<CharSequence>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        //先停止监听网络变化
                        setWifiStateChangeListenerOnOrOff(false);

                        showProgressDialog(getString(R.string.notice_commit_succeed), false, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialog.dismiss();
                                task.cancel(true);
                            }
                        });
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();

                        if (result != TaskResult.OK) {
                            //恢复监听网络
                            setWifiStateChangeListenerOnOrOff(true);
                            showTaskError(task, R.string.commit_failed);
                        } else {
                            mGuideState = 1;
                            if (task.getException() != null && task.getException().getMessage() != null) {
                                showAlert(task.getException().getMessage(), WizardSettingActivity.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (!isBinded)
                                            doGetRouterInfo();
                                        else {
                                            setResult(RESULT_OK);
                                            finish();
                                        }
                                    }
                                }, false);
                            } else {
                                if (!isBinded) {
//                                    doGetRouterInfo();
                                    String ssid = mBeenWifi.getSsid();
                                    if (mBeenWifi.get_5G_priority() != null && mBeenWifi.get_5G_priority() == 0) {
                                        ssid = mBeenWifi.getSsid_2G();
                                    }
                                    gotoWizardCompleteFragment(ssid, mBeenWifi.getKey());
                                } else {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            }
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, CharSequence param) {
//                        showAlert(param, getString(R.string.confirm), null, true);
                        showToast(param);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        //恢复监听网络
                        setWifiStateChangeListenerOnOrOff(true);

                        hideProgressDialog();
                        finish();
                    }
                })
        );
    }

    @Override
    public void onConnectNetInfoChanged(NetworkInfo networkInfo) {
        if (networkInfo == null)
            return;

        Log.d(getClass().getSimpleName(), "====~ onConnectNetInfoChanged:" + networkInfo);
        if (mCurFragment != null && mCurFragment.onNetworkChange(networkInfo)) {
            return;
        }


        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {

        } else if (!networkInfo.isConnected()) {
            // TODO 清空回退并进入提示
            if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                mGuideState = -1;
                if (mCurFragment == null || !(mCurFragment instanceof NoticeConnectCHWifiFragment))
                    backToConnectNoticePage();
                Preferences.getInstance(getApplicationContext()).remove(KeyConfig.KEY_DEVICE_TYPE);

                mCurrentDeviceType = null;
            }
        }
    }

    /**
     * 退出到提示连接对话框
     */
    private void backToConnectNoticePage(boolean... clearAll) {
        do {
            List<Fragment> list = getSupportFragmentManager().getFragments();
            if (list == null || list.isEmpty()) {
                mCurFragment = null;
                break;
            }
            mCurFragment = (BaseFragment) list.get(list.size() - 1);
            if (clearAll == null || clearAll.length == 0 || clearAll[0] == false)
                if (mCurFragment instanceof NoticeConnectCHWifiFragment) {
                    if (mCurFragment.isHidden())
                        getSupportFragmentManager().beginTransaction().show(mCurFragment).commit();
                    return;
                }
        } while (getSupportFragmentManager().popBackStackImmediate());
        gotoNoticeConnectFragment();
    }

    private void showBackFragment() {
        int num = getSupportFragmentManager().getBackStackEntryCount();
        Log.d(getClass().getSimpleName(), "====~ ++++++++++++++++++++++++++++Fragment回退栈数量：" + num);
        for (int i = 0; i < num; i++) {
            FragmentManager.BackStackEntry backstatck = getSupportFragmentManager().getBackStackEntryAt(i);
            Log.d(getClass().getSimpleName(), "====~   backstack name is >>>  " + backstatck.getName());
        }
    }

    private void doCheckWizardGuideTask() {
        if (mCurrentDeviceType == null) {
            Log.d(getClass().getSimpleName(), "====~ doCheckWizardGuideTask when current device type is NULL!");
            return;
        }
        addTask(
                new CheckWizardGuidTask().execute(this, getGateway(), mCurrentDeviceType, new TaskListener<Integer>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(getString(R.string.checking_wizard_setting), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, Integer param) {
                        if (param != null) {
                            mGuideState = param;
                            doSwitchPageByStateWizard(mGuideState);
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
     * 根据路由配置状态进入不同页面的判断
     *
     * @param stateWizard
     */
    private void doSwitchPageByStateWizard(int stateWizard) {
        switch (stateWizard) {
            case 0://如果当前设备类型是长虹的设备
                gotoPasswordFragment();
                break;
            case 1:
                if (!isLogin)
                    doCheckIsLogin(false);
                else
                    doGetRouterInfo();
                break;
            case 2://已完成管理密码设置
                gotoNoticeConnectFragment();
                if (!isLogin)
                    doCheckIsLogin(false);
                break;
            default:
                gotoNoticeConnectFragment();
        }
    }

    /**
     * 检查本地是否缓存密码，没缓存就输入，缓存了就直接进入密码检测接口
     *
     * @param isForceInput 是否强制输入密码
     */
    private void doCheckIsLogin(boolean isForceInput) {

        String password = Preferences.getInstance(this).getString(KeyConfig.KEY_ROUTER_PASSWORD);
        if (password == null || isForceInput) {
            InputDialog dialog = new InputDialog();
            Bundle bundle = new Bundle();
            bundle.putBoolean(InputDialog.EXTRA_PASSWORD, true);
            bundle.putBoolean(InputDialog.KEY_CANCEL_ABLE, false);
            bundle.putString(Intent.EXTRA_TEXT, getString(DeviceType.PLC.getName().equalsIgnoreCase(mCurrentDeviceType) ? R.string.login_notice_plc : R.string.login_notice));
            dialog.setArguments(bundle);
            dialog.setFragmentListener(new OnFragmentLifeListener<String>() {

                @Override
                public void onChanged(String o) {
                    if (!TextUtils.isEmpty(o)) {
                        doLoginRouter(o);
                    }
                }

            });
            dialog.show(getSupportFragmentManager(), "password_input");
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
                new LoginTask().execute(password, getGateway(), mCurrentDeviceType, getString(R.string.password_wrong), new TaskListener<BaseBeen<String, String>>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(getString(R.string.logining), false, null);
                        isLogin = false;
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result == TaskResult.OK) {
                            //登录完成后有两种情况，要么进入wlan界面，要么进入绑定页面
                            if (mGuideState == 1) {
                                doGetRouterInfo();
                            } else
                                doGetWlanInfo();
                        } else {
                            showTaskError(task, R.string.login_failed);
                            doCheckIsLogin(true);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, BaseBeen<String, String> param) {
                        Preferences.getInstance(getApplicationContext()).saveString(KeyConfig.KEY_ROUTER_PASSWORD, param.getT1());
                        Preferences.getInstance(getApplicationContext()).saveString(KeyConfig.KEY_COOKIE_SSID, param.getT2());
                        isLogin = true;
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doGetWlanInfo() {
        addTask(
                new GetWizardWlanTask().execute(getGateway(), mCurrentDeviceType, getCookie(),
                        new TaskListener<com.changhong.wifimng.been.guide.ResponseAllBeen>() {

                            @Override
                            public String getName() {
                                return null;
                            }

                            @Override
                            public void onPreExecute(GenericTask task) {
                                showProgressDialog(getString(R.string.downloading), false, null);
                            }

                            @Override
                            public void onPostExecute(GenericTask task, TaskResult result) {
                                hideProgressDialog();
                                if (result != TaskResult.OK)
                                    showTaskError(task, R.string.interaction_failed);
                            }

                            @Override
                            public void onProgressUpdate(GenericTask task, ResponseAllBeen param) {
                                RequireAllBeen require = new RequireAllBeen();
                                require.setType(param.getType());
                                require.setIpaddr(param.getIpaddr());
                                require.setNetmask(param.getNetmask());
                                require.setGw(param.getGw());
                                require.setDns1(param.getDns1());
                                require.setDns2(param.getDns2());
                                require.setPppoe_username(param.getPppoe_username());
                                require.setPppoe_password(param.getPppoe_password());
                                gotoWanFragment(require);
                            }

                            @Override
                            public void onCancelled(GenericTask task) {

                                hideProgressDialog();
                            }
                        })
        );
    }

    /**
     * 获取wifi信息
     */
    private void doGetWifiInfo() {
        addTask(
                new GetWizardWifiTask().execute(getGateway(), mCurrentDeviceType, getCookie(),
                        new TaskListener<com.changhong.wifimng.been.guide.ResponseAllBeen>() {
                            @Override
                            public String getName() {
                                return null;
                            }

                            @Override
                            public void onPreExecute(GenericTask task) {
                                showProgressDialog(getString(R.string.downloading), false, null);
                            }

                            @Override
                            public void onPostExecute(GenericTask task, TaskResult result) {
                                hideProgressDialog();
                                if (result != TaskResult.OK)
                                    showTaskError(task, R.string.interaction_failed);
                            }

                            @Override
                            public void onProgressUpdate(GenericTask task, com.changhong.wifimng.been.guide.ResponseAllBeen param) {
                                if (param == null)
                                    return;
                                RequireAllBeen been = new RequireAllBeen();
                                been.setSsid(param.getSsid());
                                been.setKey(param.getKey());
                                been.setEncryption(param.getEncryption());
                                been.set_5G_priority(param.get_5G_priority());
                                been.setKey_sync(param.getKey_sync());
                                been.setSsid_2G(param.getSsid_2G());
                                been.setSsid_5G(param.getSsid_5G());
                                gotoWifiFragment(been);
                            }

                            @Override
                            public void onCancelled(GenericTask task) {
                                hideProgressDialog();
                            }
                        })
        );
    }

    private void doCommitRouterPassowrd(String password) {
        addTask(
                new SetRouterPasswordTask().execute(password, password,
                        getGateway(), mCurrentDeviceType,
                        true, getString(R.string.old_password_wrong), getCookie(),
                        new TaskListener<String>() {
                            @Override
                            public String getName() {
                                return null;
                            }

                            @Override
                            public void onPreExecute(GenericTask task) {
                                showProgressDialog(getString(R.string.commiting), false, null);
                            }

                            @Override
                            public void onPostExecute(GenericTask task, TaskResult result) {
                                hideProgressDialog();
                                if (TaskResult.OK == result) {

                                } else {
                                    showTaskError(task, R.string.commit_failed);
                                }
                            }

                            @Override
                            public void onProgressUpdate(GenericTask task, String param) {
                                Preferences.getInstance(getApplicationContext()).saveString(KeyConfig.KEY_ROUTER_PASSWORD, param);
                                doLoginRouter(param);
                            }

                            @Override
                            public void onCancelled(GenericTask task) {
                                hideProgressDialog();
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
                        showProgressDialog(getString(R.string.downloading), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            finish();
                        } else {

                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, SettingResponseAllBeen param) {
                        if (param != null) {
                            gotoBindPageFragment(param.getWan_mac());//将LAN口 mac传入界面
                        } else
                            finish();
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
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

}
