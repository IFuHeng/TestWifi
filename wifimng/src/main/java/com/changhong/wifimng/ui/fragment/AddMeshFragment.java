package com.changhong.wifimng.ui.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceItem;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.StaInfo;
import com.changhong.wifimng.been.mesh.ListInfo;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.StartPLC_DeviceTask;
import com.changhong.wifimng.task.router.AddMeshTask;
import com.changhong.wifimng.task.router.GetMeshNetworkTask;
import com.changhong.wifimng.task.router.SetMeshQuickLinkTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.adapter.MeshScanAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddMeshFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, AbsListView.MultiChoiceModeListener {

    private TextView mTvNotice1;
    private TextView mTvNotice2;
    private ViewFlipper mViewFlipper;

    private int step;
    private ListView mListDevice;
    private BaseAdapter mAdapter;
    private ArrayList<DeviceItem> mMeshDeviceList;

    private DeviceItem mCurRouter;

    private CountDownTimer mCountDownTimer;

    /**
     * 搜索时长
     */
    private static final int MAX_DURATION_SEARCH = 90000;
    private static final int DURATION_DOWN_INTERVAL = 5000;

    private String gateway;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurRouter = getArguments().getParcelable(KeyConfig.KEY_DEVICE_INFO);
        mMeshDeviceList = new ArrayList<>();
//        mMeshDeviceList.add(mCurRouter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_mesh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        view.findViewById(R.id.btn_back).setOnClickListener(this);

        mViewFlipper = view.findViewById(R.id.viewFliper01);

        mTvNotice1 = view.findViewById(R.id.tv_notice1);

        view.findViewById(R.id.btn_next).setOnClickListener(this);

        mTvNotice2 = view.findViewById(R.id.tv_notice);

        //page 3
        mListDevice = view.findViewById(R.id.list_device);
        mAdapter = new MeshScanAdapter(mActivity, mMeshDeviceList);
        mListDevice.setAdapter(mAdapter);
//        mListDevice.setMultiChoiceModeListener(this);
        mListDevice.setOnItemSelectedListener(this);
        view.findViewById(R.id.btn_complete).setOnClickListener(this);

        ImageView icon = view.findViewById(R.id.icon);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        if (mCurRouter.getType() == DeviceType.PLC) {
            icon.setImageResource(R.drawable.ic_plc);
            tvTitle.setText(R.string.add_new_plc);
            mTvNotice1.setText(R.string.notice_add_plc_1);
            mTvNotice2.setText(R.string.notice_search_plc);

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = icon.getDrawable();
            drawable = DrawableCompat.wrap(drawable);
            drawable.setTint(Color.DKGRAY);
        }

        //page 4
        view.findViewById(R.id.btn_retry).setOnClickListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        stopWait5s();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            if (step == 2)
                doSetQuickLinkEnable(false);
            else
                onFragmentLifeListener.onChanged(null);
        } else if (v.getId() == R.id.btn_complete) {
            List<StaInfo> list = new ArrayList<>();
            long[] checkids = mListDevice.getCheckItemIds();
//            long[] checkedItemIds = mListDevice.getCheckedItemIds();
            for (int i = 0; i < checkids.length; i++) {
                int index = (int) checkids[i];
                DeviceItem deviceItem = mMeshDeviceList.get(index);
                StaInfo item = new StaInfo();
                item.setIp(deviceItem.getIp());
                item.setMac(deviceItem.getMac());
                item.setName(deviceItem.getDeviceName());
                list.add(item);
            }
            if (list.isEmpty()) {
                showToast(R.string.none_selected);
                return;
            }
            doQuickLinkAdd(list);
        } else if (v.getId() == R.id.btn_next) {
            if (step == 0) {
                step = 1;
                if (mCurRouter.getType() == DeviceType.PLC)
                    mTvNotice1.setText(R.string.notice_add_plc_2);
                else
                    mTvNotice1.setText(R.string.notice_add_mesh_2);
            } else {
                if (mCurRouter.getType() == DeviceType.PLC)
                    doSetPlcQuickLinkEnable();
                else
                    doSetQuickLinkEnable(true);
            }
        } else if (v.getId() == R.id.btn_retry) {
            doSetQuickLinkEnable(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (step == 1) {
                step = 0;
                if (mCurRouter.getType() == DeviceType.PLC)
                    mTvNotice1.setText(R.string.notice_add_plc_1);
                else
                    mTvNotice1.setText(R.string.notice_add_mesh_1);
            } else if (step == 0 || step == 4)
                onFragmentLifeListener.onChanged(null);
            else if (step == 2) {
                stopWait5s();
                stopAllTask();
                doSetQuickLinkEnable(false);
            } else
                doSetQuickLinkEnable(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startWait5s(int waitDuration) {
        mCountDownTimer = new CountDownTimer(waitDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (mCurRouter.getType() == DeviceType.PLC) {
                    mTvNotice2.setText(_getString(R.string.notice_search_plc) + '\n' + (millisUntilFinished / 1000) + 's');
                } else
                    mTvNotice2.setText(_getString(R.string.notice_search_mesh) + '\n' + (millisUntilFinished / 1000) + 's');
                long interval = DURATION_DOWN_INTERVAL / 1000L;
                if (millisUntilFinished / 1000 % interval == 0) {
                    doGetMeshNetState();
                }
            }

            @Override
            public void onFinish() {
                step = 4;
                doSetQuickLinkEnable(null);
                if (mCurRouter.getType() == DeviceType.PLC) {
                    mTvNotice2.setText(R.string.notice_search_plc);
                } else
                    mTvNotice2.setText(R.string.notice_search_mesh);
                mViewFlipper.setDisplayedChild(3);
            }
        };
        mCountDownTimer.start();
    }

    private void stopWait5s() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
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
//                        showProgressDialog(getString(R.string.note_mesh_info_request), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task,R.string.interaction_failed);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, List<ListInfo> param) {
                        if (param != null && !param.isEmpty()) {
                            for (ListInfo listInfo : param) {
                                if (listInfo.getQlink() != 1) {
                                    continue;
                                }
                                if (listInfo.getMac().equalsIgnoreCase(mCurRouter.getMac()))
                                    continue;

                                DeviceItem item = new DeviceItem();
                                item.setUpNodeName(mCurRouter.getDeviceName());
                                item.setMac(listInfo.getMac());
                                item.setIp(listInfo.getIp());
                                item.setStaNum(listInfo.getSta_info() == null ? 0 : listInfo.getSta_info().size());
                                item.setChild(true);
                                item.setQlink(listInfo.getQlink());
                                mMeshDeviceList.add(item);
                            }
                        }

                        if (mMeshDeviceList.size() > 0) {//读取成功后，停止轮询，并显示列表。
                            stopWait5s();
                            mViewFlipper.showNext();
                            step = 3;
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doQuickLinkAdd(List<StaInfo> list) {
        addTask(
                new AddMeshTask().execute(getGateway(), list, getCookie(), new TaskListener<Boolean>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), true, new DialogInterface.OnCancelListener() {
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
                            showTaskError(task,R.string.interaction_failed);
                        } else {
                            doSetQuickLinkEnable(false);
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
     * 设置快速组网开关，当关掉的时候，退出页面
     *
     * @param enable 开启或关闭：null表示不退出关闭，true为开启，false为关闭并退出
     */
    private void doSetQuickLinkEnable(final Boolean enable) {
        addTask(
                new SetMeshQuickLinkTask().execute(getGateway(), enable == null ? false : enable, getCookie(), new TaskListener<Integer>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        boolean is = enable == null ? false : enable;
                        int resId = is ? R.string.opening_mesh_quick_link : R.string.closing_mesh_quick_link;
                        showProgressDialog(_getString(resId), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task,R.string.interaction_failed);
                        } else {

                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, Integer param) {
                        if (enable == null)//不关闭
                            return;
                        if (enable) {//为true时，启动轮询
//                            startWait5s(param == null ? MAX_DURATION_SEARCH : (param * 1000));
                            startWait5s(MAX_DURATION_SEARCH);
                            step = 2;
                            mViewFlipper.setDisplayedChild(1);
                        } else//为false时，退出
                            onFragmentLifeListener.onChanged(null);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    /**
     * 打开电力猫主设备组网请求
     */
    private void doSetPlcQuickLinkEnable() {
        addTask(
                new StartPLC_DeviceTask().execute(getGateway(), new TaskListener<Boolean>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(
                                _getString(R.string.open_plc_add_mode), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task,R.string.interaction_failed);
                        } else {
                            showAlert(_getString(R.string.open_add_new_plc_complete), _getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onFragmentLifeListener.onChanged(null);
                                }
                            });
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

    /* **AbsListView.MultiChoiceModeListener start ********/
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        Log.d(getClass().getSimpleName(), "====~ mode = " + mode);
        Log.d(getClass().getSimpleName(), "====~ selected = " + Arrays.toString(mListDevice.getCheckedItemIds()));
        if (position == 0 && checked == true) {
            mListDevice.setItemChecked(position, !checked);
            showToast(R.string.notice_main_note);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        Log.d(getClass().getSimpleName(), "====~ onCreateActionMode: mode = " + mode + ", menu = " + menu);
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Log.d(getClass().getSimpleName(), "====~ onPrepareActionMode: mode = " + mode + ", menu = " + menu);
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Log.d(getClass().getSimpleName(), "====~ onActionItemClicked: mode = " + mode + ", item = " + item);
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Log.d(getClass().getSimpleName(), "====~ onDestroyActionMode: mode = " + mode);
    }

    /* **AbsListView.MultiChoiceModeListener end ********/

    @Override
    protected String getGateway() {
        if (gateway == null)
            gateway = super.getGateway();
        return gateway;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        Log.d(getClass().getSimpleName(), "====~onItemSelected: position= " + position + " ,id= " + id);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(getClass().getSimpleName(), "====~onNothingSelected");

    }
}
