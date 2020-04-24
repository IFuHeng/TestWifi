package com.changhong.testwifi;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.changhong.testwifi.ui.BaseActivtiy;
import com.changhong.testwifi.ui.fragment.BeepFragment;
import com.changhong.testwifi.ui.fragment.HttpConnectFragment;
import com.changhong.testwifi.ui.fragment.OnFragmentLifeListener;
import com.changhong.testwifi.ui.fragment.RecycleConnectFragment;
import com.changhong.testwifi.ui.fragment.TestListViewFragment;
import com.changhong.testwifi.ui.fragment.TestPingFragment;
import com.changhong.testwifi.ui.fragment.TestTimePickerFragment;
import com.changhong.testwifi.ui.fragment.TestWifiFragment;
import com.changhong.testwifi.ui.view.UpdateWarnDialog;
import com.changhong.testwifi.update.DownloadUtils;
import com.changhong.testwifi.update.UpdateCheckTask;
import com.changhong.testwifi.utils.CommUtil;
import com.changhong.testwifi.utils.FileSizeUtils;
import com.changhong.wifimng.ui.activity.WifiHomeActivity;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.view.DefaultDialog;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivtiy implements AdapterView.OnItemClickListener, UpdateCheckTask.OnUpdateListener {

    public static final String FORMAT_UPDATE = "检测到新版本:\n%s\n\n版本号: %d -> %s\n版本名: %s -> %s\n更新文件:%s";
    private static final int REQUEST_CODE_WIFI = 2;

    private BaseFragment mCurFragment;
    private ViewSwitcher mViewSwitcher;
    private UpdateCheckTask mTask;
    private UpdateWarnDialog mUpdateInfoDialog;

    private UpdateCheckTask.UpdateResponse mUpdateInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.list_function);
        listView.setOnItemClickListener(this);
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        int[] location = new int[2];
                        v.getLocationOnScreen(location);
                        Log.d(getClass().getSimpleName(), "onTouchEvent " + event.getX() + "," + event.getY()
                                + " ,  lcdWidth = " + getResources().getDisplayMetrics().widthPixels
                                + " ,  lcdHeight = " + getResources().getDisplayMetrics().heightPixels
                                + ",  view x = " + location[0]
                                + ",  view y = " + location[1]);
                        break;
                }
                return false;
            }
        });

        TextView tvVersion = (TextView) findViewById(R.id.textView3);
        tvVersion.setText("V" + App.getInstance().sVersionName);
        tvVersion.getPaint().setUnderlineText(true);
        tvVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUpdateInfo != null)
                    showQRcode(v);
            }
        });

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher01);

        Log.d(getClass().getSimpleName(), "====~ Build.VERSION.SDK_INT = " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission_wifi_state = checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE);
            int permission_network_state = checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE);
            int permission_coarse_location = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            Log.d(getClass().getSimpleName(), "====~ permission_wifi_state = " + permission_network_state + " , permission_network_state =" + permission_network_state);
            if (permission_wifi_state != PackageManager.PERMISSION_GRANTED
                    || permission_network_state != PackageManager.PERMISSION_GRANTED
                    || permission_coarse_location != PackageManager.PERMISSION_GRANTED)
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_COARSE_LOCATION}
                        , REQUEST_CODE_WIFI);
        }

        checkUpdate();
    }

    private void showQRcode(View v) {
//        Bitmap bitmap = CommUtil.createQRCode(mUpdateInfo.getUpdate_url(), BitmapFactory.decodeResource(getResources(), R.drawable.ic_router_black_24dp));
        Bitmap bitmap = CommUtil.createQRCode(mUpdateInfo.getUpdate_url(), getResources().getDrawable(R.drawable.ic_router_black_24dp));
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        PopupWindow popupWindow = new PopupWindow(imageView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);

        popupWindow.showAsDropDown(v);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(getClass().getSimpleName(), "====~ onRequestPermissionsResult : requestCode = " + requestCode
                + " , permissions = " + Arrays.toString(permissions)
                + " , grantResult = " + Arrays.toString(grantResults));
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mCurFragment != null) {
            if (mCurFragment.onKeyDown(keyCode, event))
                return true;
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                exitFragment();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mCurFragment != null && mCurFragment.onKeyUp(keyCode, event))
            return true;

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                new DefaultDialog(this, "警告", 0, "厨房水浸检测到漏水，请排查", "我知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startFragment(RecycleConnectFragment.class);
                    }
                }, null, null).show();
                break;
            case 1:
                new DefaultDialog(this, "警告", 0, "厨房水浸检测到漏水，请排查", "我知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startFragment(HttpConnectFragment.class);
                    }
                }, "我不知道了", null).show();
                break;
            case 2:
                startFragment(TestWifiFragment.class);
                break;
            case 3:
                startFragment(TestPingFragment.class);
                break;
            case 4:
                startFragment(BeepFragment.class);
                break;
            case 5:
//                startFragment(NoticeConnectCHWifiFragment.class);
                startActivity(new Intent(this, WifiHomeActivity.class));
                break;
            case 6:
                startFragment(TestListViewFragment.class);
                break;
            case 7:
                startFragment(TestTimePickerFragment.class);
                break;
            default:
                Toast.makeText(this, "wait for develop", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startFragment(Class<? extends BaseFragment> clazz) {
        if (mViewSwitcher.getDisplayedChild() == 0) {
            mViewSwitcher.setDisplayedChild(1);
        }
        try {
            mCurFragment = clazz.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.layout_container, mCurFragment).commit();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void exitFragment() {
        if (mCurFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mCurFragment).commit();
            mCurFragment = null;
            mViewSwitcher.setDisplayedChild(0);
        }
    }

    private void startFragment(BaseFragment fragment) {
        FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
        if (mCurFragment != null)
            beginTransaction.hide(mCurFragment);

        beginTransaction.add(R.id.layout_container, fragment).addToBackStack(null).commit();
        mCurFragment = fragment;
    }

    private void backFragment() {
        if (mCurFragment != null) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStack();
                List<Fragment> list = getSupportFragmentManager().getFragments();
                mCurFragment = (BaseFragment) list.get(list.size() - 1);
            } else
                finish();
        } else finish();
    }

    public void checkUpdate() {
        cancelTask();

        mTask = new UpdateCheckTask(this);
        mTask.execute();
        showToast("开始检查是否有新版本，请稍候……");
    }

    private void cancelTask() {
        if (mTask != null)
            mTask.cancel(true);
        mTask = null;
    }

    @Override
    public void onReceiveUpdateInfo(final UpdateCheckTask.UpdateResponse response) {
        Log.d(getClass().getSimpleName(), response.toString());
        if (mUpdateInfoDialog != null) {
            getFragmentManager().beginTransaction().remove(mUpdateInfoDialog).commit();
        }

        mUpdateInfo = response;

        if (App.getInstance().sVersionCode < Integer.parseInt(response.getVersion())) {

            mUpdateInfoDialog = new UpdateWarnDialog();
            Bundle bundle = new Bundle();
            bundle.putString(Intent.EXTRA_TEXT,
                    String.format(FORMAT_UPDATE,
                            response.getChangelog(), App.getInstance().sVersionCode, response.getVersion(),
                            App.getInstance().sVersionName, response.getVersionShort(),
                            FileSizeUtils.FormetFileSize(response.getBinary().getFsize())));
            mUpdateInfoDialog.setArguments(bundle);

            mUpdateInfoDialog.setFragmentListener(new OnFragmentLifeListener<Boolean>() {
                @Override
                public void onStarted(Boolean o) {

                }

                @Override
                public void onChanged(Boolean o) {
                    if (o)
                        doStartUpdate(response);
                }

                @Override
                public void onStopped(Boolean o) {

                }
            });

            mUpdateInfoDialog.show(getFragmentManager(), "updateinfo");
        } else {
            showToast("当前版本为最新版本，无需升级。");
        }


        mTask = null;
    }

    private void doStartUpdate(UpdateCheckTask.UpdateResponse response) {
        showToast("开始后台下载，请稍候……");
        new DownloadUtils(this).downloadAPK(response.getInstall_url(), "Download");

    }

    @Override
    public void onError(String error) {
        showToast(error);
        mTask = null;
    }

}
