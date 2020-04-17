package com.changhong.wifimng.ui.fragment.guide;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.Whole2LocalBeen;
import com.changhong.wifimng.http.task.BindingTask;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.uttils.WifiMacUtils;

import org.json.JSONException;

public class BindPageFragment extends BaseFragment implements View.OnClickListener {

    private DeviceType deviceType;

    private Whole2LocalBeen mInfoFromApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInfoFromApp = getArguments().getParcelable(KeyConfig.KEY_INFO_FROM_APP);

        deviceType = DeviceType.getDeviceTypeFromName(mInfoFromApp.getDeviceType());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bind_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_bind).setOnClickListener(this);
        view.findViewById(R.id.btn_back).setOnClickListener(this);

        ImageView icon = view.findViewById(R.id.icon);
        {
            Drawable drawable = getResources().getDrawable(deviceType.getIconResId());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable = DrawableCompat.wrap(drawable);
                drawable.setTint(0xff2B5A97);
            }
            icon.setImageDrawable(drawable);
        }

        TextView tv_info = view.findViewById(R.id.text);
        tv_info.setText(R.string.found_device);
        if (deviceType == DeviceType.R2s) {
            tv_info.append(":" + getString(R.string.R2s));
        } else if (deviceType == DeviceType.BWR) {
            tv_info.append(":" + getString(R.string.bwr510));
        } else if (deviceType == DeviceType.PLC) {
            tv_info.append(":" + getString(R.string.plc));
        }
        tv_info.append(deviceType.getName());
        tv_info.append("\nMAC:" + WifiMacUtils.macNoColon(mInfoFromApp.getMac()));

        TextView tvtitle = view.findViewById(R.id.tv_title);
        tvtitle.setText(R.string.plc_setting);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_back) {
//            onFragmentLifeListener.onChanged(null);
            mActivity.finish();
            return;
        }

        //TODO 绑定
        if (v.getId() == R.id.btn_bind) {
            doBinding();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mActivity.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void doBinding() {
        addTask(
                new BindingTask().execute(mInfoFromApp.getMac(), mInfoFromApp, new TaskListener<String>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.binding), true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                task.cancel(true);
                            }
                        });
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, final String param) {
                        showToast(R.string.binding_success);
                        showAlert(_getString(R.string.bing_complete) + param, _getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onFragmentLifeListener.onChanged(param);
                                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(KeyConfig.ACTION_BIND_DEVICE));
                            }
                        });
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                        showToast(R.string.cancel_bind);
                    }
                })
        );
    }
}
