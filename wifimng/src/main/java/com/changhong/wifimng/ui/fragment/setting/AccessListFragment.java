package com.changhong.wifimng.ui.fragment.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.wifi.Level2Been;
import com.changhong.wifimng.been.wifi.ResponseAllBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.router.DeleteWlanAccessTask;
import com.changhong.wifimng.task.router.GetWlanAccessTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.uttils.WifiMacUtils;

import java.util.List;

/**
 * 访问控制的黑白名单
 * 传入Intent.EXTRA_TEXT 的信息 （boolean）：true为白名单
 */
public class AccessListFragment extends BaseFragment implements View.OnClickListener {

    private TableLayout mTableBlackList;

    private boolean isWhiteList;

    /**
     * 当前已连接路由器类型
     */
    private String mCurrentDeviceType;
    private TextView mTvMessage;

    private Integer mCurrentEnabled;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            mCurrentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
        else
            mCurrentDeviceType = Preferences.getInstance(mActivity).getString(KeyConfig.KEY_DEVICE_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visit_white_black_list, null, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_back).setOnClickListener(this);

        mTvMessage = view.findViewById(R.id.tv_info1);

        mTableBlackList = view.findViewById(R.id.table_black_list);

        if (getArguments() != null && getArguments().containsKey(Intent.EXTRA_TEXT) && getArguments().getBoolean(Intent.EXTRA_TEXT)) {
            isWhiteList = true;
            TextView textView = view.findViewById(R.id.btn_list);
            textView.setText(R.string.device_white_list);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isWhiteList) {
            doGetWlanAccess();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (onFragmentLifeListener != null)
                onFragmentLifeListener.onChanged(null);
        } else if (v.getId() == R.id.icon && v.getTag() != null) {
            if (v.getTag() instanceof Level2Been) {
                final Level2Been been = (Level2Been) v.getTag();
                if (mCurrentEnabled == 1 && been.getMac().equalsIgnoreCase(WifiMacUtils.getMac(mActivity))) {
                    showAlert(_getString(R.string.notice_del_my_device_from_whitelist), _getString(R.string._continue), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            askDelEffectImmediately(been.getName(), been.getMac());
                        }
                    }, _getString(R.string.cancel), null, true);
                } else
                    askDelEffectImmediately(been.getName(), been.getMac());
            }
        }
    }


    /**
     * 获取黑名单
     */
    private void doGetWlanAccess() {
        addTask(
                new GetWlanAccessTask().execute(getGateway(), mCurrentDeviceType, getCookie(), new TaskListener<ResponseAllBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.downloading), true, new DialogInterface.OnCancelListener() {
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
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, ResponseAllBeen param) {
                        if (param != null) {
                            refreshView(param);
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
     * 询问删除是否立即执行
     */
    private void askDelEffectImmediately(final String name, final String mac) {
        if (mCurrentDeviceType.equalsIgnoreCase(DeviceType.PLC.getName())) {
            doDelAccess(name, mac, true);
        } else
            showAlert(_getString(R.string.notice_ask_execute_immediately), getString(R.string.save_and_execute),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doDelAccess(name, mac, true);
                        }
                    }, getString(R.string.save_only), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doDelAccess(name, mac, false);
                        }
                    }, true);
    }

    /**
     * 删除黑名单
     *
     * @param name
     * @param mac
     * @param isEffectImmediately
     */
    private void doDelAccess(String name, String mac, boolean isEffectImmediately) {
        addTask(new DeleteWlanAccessTask().execute(getGateway(), mCurrentDeviceType, mac, isEffectImmediately, getCookie(), new TaskListener<Boolean>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(getString(R.string.commiting), true, new DialogInterface.OnCancelListener() {
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
                        } else {
                            showToast(R.string.commit_completed);
                            doGetWlanAccess();
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, Boolean param) {

                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    /**
     * 更新开关和黑名单
     *
     * @param param
     */
    private void refreshView(ResponseAllBeen param) {
        mCurrentEnabled = param.getEnabled();
        switch (param.getEnabled()) {
            case 1:
                mTvMessage.setText(R.string.device_white_list);
                break;
            case 2:
                mTvMessage.setText(R.string.device_black_list);
                break;
            default:
                mTvMessage.setText(null);
        }
        while (mTableBlackList.getChildCount() > 1)
            mTableBlackList.removeViewAt(1);
        //清理除标题外的所有行
        List<Level2Been> list = param.getList();
        if (list != null && !list.isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            for (int i = 0; i < param.getList().size(); i++) {
                Level2Been level2Been = param.getList().get(i);
                View view = inflater.inflate(R.layout.item_device_table_row, null, false);
                TextView tvIndex = view.findViewById(R.id.text1);
                tvIndex.setText(String.valueOf(i));
                TextView tvName = view.findViewById(R.id.text2);
                tvName.setText(level2Been.getName());
                TextView tvMac = view.findViewById(R.id.text3);
                tvMac.setText(level2Been.getMac());
                ImageView icon = view.findViewById(R.id.icon);
                icon.setImageResource(R.drawable.ic_cell_del);
                icon.setTag(level2Been);
                icon.setOnClickListener(this);
                mTableBlackList.addView(view, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            }
        }
    }

}
