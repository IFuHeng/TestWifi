package com.changhong.wifimng.function;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.guide.ResponseAllBeen;
import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.been.sys.SettingResponseAllBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.GetRouterInfoTask;
import com.changhong.wifimng.task.LoginTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.GetPLCDeviceBaseInfoTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.InputDialog;
import com.changhong.wifimng.ui.fragment.OnFragmentLifeListener;
import com.changhong.wifimng.uttils.WifiUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

public class AddChildDeviceEntrance {
    FragmentActivity activity;
    DeviceType deviceType;
    String mac;
    String name;
    Observer observer;
    private WifiManager mWifiManager;
    private ProgressDialog mProgressDialog;

    public AddChildDeviceEntrance(FragmentActivity activity, DeviceType deviceType, String mac, String name, Observer observer) {
        this.activity = activity;
        this.deviceType = deviceType;
        this.mac = mac;
        if (mac != null && mac.length() == 12) {
            StringBuilder sb = new StringBuilder(mac);
            for (int i = 0; i < 5; i++) {
                sb.insert(3 * i + 2, File.pathSeparatorChar);
            }
            mac = sb.toString();
        }
        this.mac = mac;
        this.name = name;
        this.observer = observer;
    }

    public void start() {
        if (!getWifiManager().isWifiEnabled() || getWifiManager().getConnectionInfo().getNetworkId() == -1) {
            activity.startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), BaseWifiActivtiy.REQUEST_CODE_WIFI_SETTING);
            return;
        }
        new CheckAndLoginTask().execute();
    }

    private WifiManager getWifiManager() {
        if (mWifiManager == null)
            mWifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return mWifiManager;
    }

    private class CheckAndLoginTask extends AsyncTask<Object, String, Boolean> {

        @Override
        protected Boolean doInBackground(Object... taskParams) {
            String gateWay = WifiUtils.turnInteger2Ip(getWifiManager().getDhcpInfo().gateway);

            //第一步，检测设备类型是否匹配
            try {
                if (!checkDeviceType(gateWay, deviceType)) {
                    publishProgress(activity.getString(
                            deviceType == DeviceType.PLC ? R.string.notice_con_ch_plc : R.string.notice_con_ch_router
                    ));
                    return false;
                }
            } catch (Exception e) {
                String message = e.getMessage();
                if (TextUtils.isEmpty(message))
                    message = activity.getString(R.string.interaction_failed);
                publishProgress(message);
                return false;
            }

            //第二步，检测配置状态是否正确
            try {
                if (!isConfiguationCompleted(gateWay, deviceType)) {
                    publishProgress(activity.getString(R.string.configuation_not_completed));
                    return false;
                }
            } catch (Exception e) {
                String message = e.getMessage();
                if (TextUtils.isEmpty(message))
                    message = activity.getString(R.string.interaction_failed);
                publishProgress(message);
                return false;
            }

            //第三步，登录

            return true;
        }

        /**
         * 检测设备类型
         *
         * @param gateway
         * @param type
         * @return
         */
        private boolean checkDeviceType(String gateway, DeviceType type) throws Exception {
            String url;
            String body;
            String response;
            if (type == DeviceType.PLC) {
                url = "http://" + gateway + ":8081/rpc";
                body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_type_get\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithJson(url, body);
            } else {
                url = "http://" + gateway + ":80/rpcsupper";
                body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_type_get\",\"params\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
            }

            if (response == null)
                throw new IOException(activity.getString(R.string.connected_wrong_device));
            SettingResponseAllBeen been = new Gson().fromJson(response, SettingResponseAllBeen.class);
            if (been.getErr_code() == 0 && type.getName().equalsIgnoreCase(been.getDev_type())) {
                return true;
            }
            return false;
        }

        /**
         * 检测设备配置是否已完成
         *
         * @param gateway
         * @param type
         * @return
         */
        private boolean isConfiguationCompleted(String gateway, DeviceType type) throws Exception {
            String response;
            if (DeviceType.PLC == type) {// PLC
                String url = "http://" + gateway + ":8081/rpc";
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wizard_guid_show\",\"param\":{\"src_type\":1}}";
                response = DefaultHttpUtils.httpPostWithText(url, body);
                if (response == null)
                    throw new Exception(activity.getString(R.string.connected_wrong_device));
            }

            //
            else {
                String url = "http://" + gateway + ":80/rpcsupper";
                String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"wizard_get_guid\",\"params\":{\"src_type\":1}}";

                response = DefaultHttpUtils.httpPostWithJson(url, body);
            }

            ResponseAllBeen been = new Gson().fromJson(response, ResponseAllBeen.class);
            return been.getErr_code() == 0 && been.getGuid_flag() == 1;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values != null && values.length > 0) {
                Toast.makeText(activity, values[0], Toast.LENGTH_SHORT).show();
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(activity.getString(R.string.checking_wizard_setting), false, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    CheckAndLoginTask.this.cancel(true);
                    hideProgressDialog();
                }
            });
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            hideProgressDialog();
            if (aBoolean != null && aBoolean)
                checkIsLogin(false);
        }
    }

    /**
     * 检查本地是否缓存密码，没缓存就输入，缓存了就直接进入密码检测接口
     *
     * @param isForceInput 是否强制输入密码
     */
    private void checkIsLogin(boolean isForceInput) {

        String password = Preferences.getInstance(activity.getApplicationContext()).getString(KeyConfig.KEY_ROUTER_PASSWORD);
        if (password == null || isForceInput) {
            InputDialog dialog = new InputDialog();
            Bundle bundle = new Bundle();
            bundle.putBoolean(InputDialog.EXTRA_PASSWORD, true);
            bundle.putBoolean(InputDialog.KEY_CANCEL_ABLE, false);
            bundle.putString(Intent.EXTRA_TEXT, activity.getString(DeviceType.PLC == deviceType ? R.string.login_notice_plc : R.string.login_notice));
            dialog.setArguments(bundle);
            dialog.setFragmentListener(new OnFragmentLifeListener<String>() {

                @Override
                public void onChanged(String o) {
                    if (!TextUtils.isEmpty(o)) {
                        doLoginRouter(o);
                    }
                }

            });
            dialog.show(activity.getSupportFragmentManager(), "password_input");
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
        String gateWay = WifiUtils.turnInteger2Ip(getWifiManager().getDhcpInfo().gateway);
        new LoginTask().execute(password, gateWay, deviceType.getName(),activity.getString(R.string.password_wrong), new TaskListener<BaseBeen<String, String>>() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public void onPreExecute(final GenericTask task) {
                showProgressDialog(activity.getString(R.string.logining), false, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        task.cancel(true);
                    }
                });
            }

            @Override
            public void onPostExecute(GenericTask task, TaskResult result) {
                hideProgressDialog();
                if (result == TaskResult.OK) {
                    doCheckDeviceMac();
                } else {
                    if (task.getException() != null && !TextUtils.isEmpty(task.getException().getMessage()))
                        Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(activity, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    checkIsLogin(true);
                }
            }

            @Override
            public void onProgressUpdate(GenericTask task, BaseBeen<String, String> param) {
                if (param != null) {
                    Preferences.getInstance(activity).saveString(KeyConfig.KEY_ROUTER_PASSWORD, param.getT1());
                    Preferences.getInstance(activity).saveString(KeyConfig.KEY_COOKIE_SSID, param.getT2());
                }
            }

            @Override
            public void onCancelled(GenericTask task) {
                hideProgressDialog();
            }
        });
    }

    private void doCheckDeviceMac() {
        if (DeviceType.PLC == deviceType)
            doGetPLCRouterInfo();
        else
            doGetRouterInfo();
    }

    private void doGetPLCRouterInfo() {
        String gateWay = WifiUtils.turnInteger2Ip(getWifiManager().getDhcpInfo().gateway);
        new GetPLCDeviceBaseInfoTask().execute(gateWay, new TaskListener<PLCInfo>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(activity.getString(R.string.downloading), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result == TaskResult.AUTH_ERROR)
                            LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(BaseWifiActivtiy.ACTION_UNAUTHORIZED));
                        else if (result != TaskResult.OK) {
                            if (task.getException() != null && !TextUtils.isEmpty(task.getException().getMessage())) {
                                Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(activity, R.string.interaction_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, PLCInfo param) {
                        if (param != null) {
                            if (!param.getDev_mac().equalsIgnoreCase(mac)) {
                                showMacNotSame(mac, param.getDev_mac());
                            } else {
                                gotoNext(param);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                }
        );
    }

    /**
     * 获取本地路由器设备的信息，对比mac地址
     */
    private void doGetRouterInfo() {
        String gateWay = WifiUtils.turnInteger2Ip(getWifiManager().getDhcpInfo().gateway);
        new GetRouterInfoTask().execute(gateWay, deviceType.getName(), getCookie(), new TaskListener<SettingResponseAllBeen>() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public void onPreExecute(final GenericTask task) {
                showProgressDialog(activity.getString(R.string.downloading), false, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        task.cancel(true);
                    }
                });
            }

            @Override
            public void onPostExecute(GenericTask task, TaskResult result) {
                hideProgressDialog();
                if (result == TaskResult.AUTH_ERROR)
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(BaseWifiActivtiy.ACTION_UNAUTHORIZED));
                else if (result != TaskResult.OK) {
                    if (task.getException() != null && !TextUtils.isEmpty(task.getException().getMessage())) {
                        Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, R.string.interaction_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onProgressUpdate(GenericTask task, SettingResponseAllBeen param) {
                if (param != null) {
                    if (!param.getWan_mac().equalsIgnoreCase(mac)) {
                        showMacNotSame(mac, param.getWan_mac());
                    } else {
                        gotoNext(param);
                    }
                }
            }

            @Override
            public void onCancelled(GenericTask task) {
                hideProgressDialog();
            }
        });
    }

    private <T> void gotoNext(T param) {
        observer.update(null, param);
    }

    protected void showProgressDialog(CharSequence cs, boolean cancelable, DialogInterface.OnCancelListener listener) {
        if (mProgressDialog == null) {
//            mProgressDialog = ProgressDialog.show(this, null, cs, true, cancelable, listener);
            mProgressDialog = new ProgressDialog(activity);
        }
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        mProgressDialog.setMessage(cs);
        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.setCanceledOnTouchOutside(cancelable);
        mProgressDialog.setOnCancelListener(listener);
        mProgressDialog.show();

    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    /**
     * 提示获取到的设备MAC和本地mac不匹配
     *
     * @param aimMac
     * @param localMac
     */
    protected void showMacNotSame(String aimMac, String localMac) {
        int start = 11;
        int end = 11 + localMac.length();
        SpannableString spannableString = new SpannableString(String.format(activity.getString(R.string.notice_mac_not_same), localMac, aimMac));
        spannableString.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = end + 6;
        end = start + localMac.length();
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Toast.makeText(activity, spannableString, Toast.LENGTH_LONG).show();
    }

    protected String getCookie() {
//        Map<String, String> cookies = new HashMap<>();
//        cookies.put("ssid", Preferences.getInstance(activity.getApplicationContext()).getString(KeyConfig.KEY_COOKIE_SSID));
//        return cookies;
        return Preferences.getInstance(activity.getApplicationContext()).getString(KeyConfig.KEY_COOKIE_SSID);
    }
}
