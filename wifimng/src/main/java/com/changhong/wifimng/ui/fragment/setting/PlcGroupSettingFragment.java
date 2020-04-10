package com.changhong.wifimng.ui.fragment.setting;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewFlipper;

import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.StaInfo;
import com.changhong.wifimng.been.plc.DevControlRuleBeen;
import com.changhong.wifimng.been.plc.PLCInfo;
import com.changhong.wifimng.been.Group;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.*;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.adapter.Client2Adapter;
import com.changhong.wifimng.ui.adapter.GroupAdapter;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.fragment.InputDialog;
import com.changhong.wifimng.ui.fragment.OnFragmentLifeListener;
import com.changhong.wifimng.uttils.ClientIconUtils;
import com.changhong.wifimng.uttils.CommUtil;
import com.changhong.wifimng.uttils.WifiMacUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 电力猫分组控制
 */
public class PlcGroupSettingFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener {

    private DeviceType mDeviceType;

    private String DEFAULT_GROUP;

    private TextView mTvTitle;
    private ViewFlipper mViewFlipper;
    private TextView mBtnAction;

    private List<StaInfo> mCurrentLinkDevice;

    /**
     * 状态： 0、分组列表  1、分组详情 2、添加分组Step1  3、添加分组step2  4、修改分组里的下挂设备  5、修改分组里的时间限制  6、修改组名
     */
    private byte mStatus;

    // Page1 group list
    private GroupAdapter mAdapterGroup;
    private List<Group> mArrayGroup;
    private List<BaseBeen<String, Integer>> mArrayGroupName;
    private List<DevControlRuleBeen> mArrayTimeLimit;
    // page2 edit group
    private ListView mListviewClient;
    private TextView mTvGroupName;
    private TextView mTvHealthTime;
    /**
     * 当前组名
     */
    private String mCurrentGroupName;
    private ArrayList<StaInGroupItem> mChoicesOfGroupSta;//用于修改分组时的临时数据
    private View mBtnEditGroupName;
    private View mBtnEditNetWorkTime;
    private View mBtnEditClient;

    // page3 edit group
    private EditText mEtGroupName;
    private ListView mListViewSta;

    // page4 health time edit
    private CheckBox[] mWeekCheckBoxs;
    private TextView mTvStartTime;
    private TextView mTvEndTime;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat mSDFormatHHmm = new SimpleDateFormat("HH:mm");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mInfoFromApp != null)
            mDeviceType = DeviceType.getDeviceTypeFromName(mInfoFromApp.getDeviceType());

        mCurrentLinkDevice = new ArrayList<>();
        mArrayGroup = new ArrayList<>();
        mArrayGroupName = new ArrayList<>();

        DEFAULT_GROUP = getString(R.string.default_group);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_setting2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_back).setOnClickListener(this);
        mTvTitle = view.findViewById(R.id.tv_title);

        mViewFlipper = view.findViewById(R.id.viewFliper01);
        mBtnAction = view.findViewById(R.id.btn_action);
        mBtnAction.setOnClickListener(this);

        // page1
        ListView listviewGroup = view.findViewById(R.id.listView_group);
        mAdapterGroup = new GroupAdapter(mActivity, mArrayGroupName, this);
        listviewGroup.setAdapter(mAdapterGroup);
        listviewGroup.setOnItemClickListener(this);

        int padding = CommUtil.dip2px(mActivity, 20);
        { // add footer
            TextView textView = new TextView(mActivity);
            textView.setPadding(padding, padding, padding, padding);
            textView.setText(R.string.group_introduce);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            listviewGroup.addFooterView(textView, null, false);
        }

        // page2
        mBtnEditGroupName = view.findViewById(R.id.btn_edit_group_name);
        mBtnEditGroupName.setOnClickListener(this);
        mBtnEditNetWorkTime = view.findViewById(R.id.btn_edit_network_time);
        mBtnEditNetWorkTime.setOnClickListener(this);
        mBtnEditClient = view.findViewById(R.id.btn_edit_client);
        mBtnEditClient.setOnClickListener(this);
        mListviewClient = view.findViewById(R.id.listview_client);
        mTvGroupName = view.findViewById(R.id.tv_group_name);
        mTvHealthTime = view.findViewById(R.id.tv_health_time);

        // page3
        mEtGroupName = view.findViewById(R.id.et_new_group_name);
        mListViewSta = view.findViewById(R.id.listview_client_choose);

        // page4
        mWeekCheckBoxs = new CheckBox[7];
        mWeekCheckBoxs[0] = view.findViewById(R.id.cb_monday);
        mWeekCheckBoxs[1] = view.findViewById(R.id.cb_tuesday);
        mWeekCheckBoxs[2] = view.findViewById(R.id.cb_wednesday);
        mWeekCheckBoxs[3] = view.findViewById(R.id.cb_thursday);
        mWeekCheckBoxs[4] = view.findViewById(R.id.cb_friday);
        mWeekCheckBoxs[5] = view.findViewById(R.id.cb_saturday);
        mWeekCheckBoxs[6] = view.findViewById(R.id.cb_sunday);

        mTvStartTime = view.findViewById(R.id.tv_startTime);
        mTvStartTime.setOnClickListener(this);
        mTvEndTime = view.findViewById(R.id.tv_endTime);
        mTvEndTime.setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        doGetPlcInfo();
        doGetGroupAndLimitInfo();
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.btn_back) {
            onKeyBack();
        }

        // page1 delete group
        else if (v.getId() == R.id.btn_delete) {
            showAskDeleteGroup(mArrayGroupName.get((Integer) v.getTag()).getT1());
        }

        // page2 edit group name
        else if (v.getId() == R.id.btn_edit_group_name) {
            mStatus = 6;
            InputDialog dialog = new InputDialog();
            Bundle bundle = new Bundle();
            bundle.putBoolean(InputDialog.KEY_CANCEL_ABLE, true);
            bundle.putString(Intent.EXTRA_TEXT, _getString(R.string.notice_new_group_name));
            bundle.putString(InputDialog.KEY_HINT, mCurrentGroupName);
            dialog.setArguments(bundle);
            dialog.setFragmentListener(new OnFragmentLifeListener<String>() {

                @Override
                public void onChanged(String o) {
                    if (!TextUtils.isEmpty(o)) {
                        ArrayList<Group> groups = new ArrayList<>();
                        for (Group group : mArrayGroup) {
                            if (group.getGroup_name().equalsIgnoreCase(mCurrentGroupName)) {
                                group = group.cloneOne();
                                group.setGroup_name(o);
                                group.setState(false);
                                groups.add(group);
                            }
                        }
                        doSetGroupAndLimitInfo(groups, null);
                    }
                }

            });
            assert getFragmentManager() != null;
            dialog.show(Objects.requireNonNull(getFragmentManager()), "groupName_input");
        }

        // page2 edit group name
        else if (v.getId() == R.id.btn_edit_network_time) {
            gotoEditClientsTimeRule();
        }

        // page2 edit client
        else if (v.getId() == R.id.btn_edit_client) {
            gotoEditClientsUnderGroup();
        } else if (v.getId() == R.id.btn_action) {
            onClickAction(v);
        }

        // page4
        else if (v.getId() == R.id.tv_startTime
                || v.getId() == R.id.tv_endTime) {
            setTimeTextView((TextView) v);
        }
    }

    /**
     * 修改分组的时间限制
     */
    @SuppressLint("DefaultLocale")
    private void gotoEditClientsTimeRule() {
        mStatus = 5;
        mTvTitle.setText(mCurrentGroupName);
        gotoViewFlipperPage(3);
        setBtnAction(true, null, R.drawable.ic_check_white_24dp);
        DevControlRuleBeen rule = getLimitFromStaByGroup(mCurrentGroupName);
        if (rule == null) {
            showToast(R.string.data_error);
            return;
        }
        mTvStartTime.setText(rule.getStart_time());
        mTvEndTime.setText(rule.getEnd_time());

        byte weekValue = rule.getWeekDays();
        for (int i = 0; i < mWeekCheckBoxs.length; i++) {
            mWeekCheckBoxs[i].setOnCheckedChangeListener(null);
            mWeekCheckBoxs[i].setChecked(((weekValue >> i) & 1) == 1);
            mWeekCheckBoxs[i].setOnCheckedChangeListener(this);
        }
    }

    /**
     * 创建分组1
     */
    private void gotoCreateGroup1() {
        mStatus = 2;
        gotoViewFlipperPage(2);
        mTvTitle.setText(R.string.add_group);
        setBtnAction(true, _getString(R.string.next), 0);
        // CREATE STA NAME
        mChoicesOfGroupSta = getChoicesOfGroupSta(null);
        mListViewSta.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListViewSta.setAdapter(new EditGroupStaAdapter(mActivity, mChoicesOfGroupSta, R.layout.simple_list_item_multiple_choice, null));
        mEtGroupName.setText(null);
    }

    /**
     * 创建分组2
     */
    @SuppressLint("SetTextI18n")
    private void gotoCreateGroup2() {
        mStatus = 3;
        gotoViewFlipperPage(3);
        setBtnAction(true, null, R.drawable.ic_check_white_24dp);
        for (int i = 0; i < mWeekCheckBoxs.length; i++) {
            mWeekCheckBoxs[i].setOnCheckedChangeListener(null);
            mWeekCheckBoxs[i].setChecked(i > 4);
            mWeekCheckBoxs[i].setOnCheckedChangeListener(this);
        }
        mTvStartTime.setText(R.string.default_time_start);
        mTvEndTime.setText(R.string.default_time_end);
    }

    /**
     * 启动下挂app选择
     */
    private void gotoEditClientsUnderGroup() {

        mChoicesOfGroupSta = getChoicesOfGroupSta(mCurrentGroupName);

        mListviewClient.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListviewClient.setAdapter(new EditGroupStaAdapter(mActivity, mChoicesOfGroupSta, R.layout.simple_list_item_multiple_choice, mCurrentGroupName));
        for (Group group : mArrayGroup) {
            if (mCurrentGroupName.equalsIgnoreCase(group.getGroup_name())) {//同组
                String mac = group.getMac();
                for (int i = 0; i < mChoicesOfGroupSta.size(); i++) {
                    if (WifiMacUtils.compareMac(mac, mChoicesOfGroupSta.get(i).mac)) {//同mac
                        mListviewClient.setItemChecked(i, true);
                        break;
                    }
                }
            }
        }
        mStatus = 4;
        setBtnAction(true, null, R.drawable.ic_check_white_24dp);
        setViewStateWhenOnModifyGroupStaList(true);
    }

    private class EditGroupStaAdapter extends BaseAdapter {
        private int viewResId;
        private ArrayList<StaInGroupItem> data;
        private Context context;
        private HashMap<String, Integer> hashGroupColor = new HashMap<>();
        private String currentGroup;

        private EditGroupStaAdapter(Context context, ArrayList<StaInGroupItem> data, @LayoutRes int viewResId, String currentGroup) {
            this.data = data;
            this.context = context;
            this.viewResId = viewResId;
            this.currentGroup = currentGroup;
        }

        @Override
        public int getCount() {
            if (data != null) {
                return data.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int i) {
            if (data != null) {
                return data.get(i);
            }
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(context).inflate(viewResId, null, false);
            }

            StaInGroupItem item = mChoicesOfGroupSta.get(i);
            int color;
            if (item.group == null)
                color = _getResources().getColor(R.color.colorCellEdge);
            else if (hashGroupColor.containsKey(item.group)) {
                color = hashGroupColor.get(item.group);
            } else if (currentGroup != null && currentGroup.equalsIgnoreCase(item.group)) {
                color = _getResources().getColor(R.color.colorPrimaryDark);
                hashGroupColor.put(item.group, color);
            } else {
                color = getRandomColor(180, 360);
                hashGroupColor.put(item.group, color);
            }
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setText(item.getShowCharsequence(color));
                Drawable drawable = context.getResources().getDrawable(ClientIconUtils.getClientDevcieByString(item.name));
                int size = CommUtil.dip2px(context, 50);
                drawable.setBounds(0, 0, size, size);
                textView.setCompoundDrawables(drawable, null, null, null);
                textView.setCompoundDrawablePadding(CommUtil.dip2px(context, 10));
            }

            return view;
        }
    }

    /**
     * 提示删除分组
     *
     * @param groupName 分组名称
     */
    private void showAskDeleteGroup(final String groupName) {
        String content = String.format(_getString(R.string.ask_delete_group), groupName);
        SpannableString ss = new SpannableString(content);
        int start = content.indexOf(groupName);
        ss.setSpan(new ForegroundColorSpan(_getResources().getColor(R.color.colorAccent)), start, start + groupName.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        showAlert(ss, _getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ArrayList<Group> newArrayGroup = new ArrayList<>();//所有组信息的副本
                ArrayList<DevControlRuleBeen> newArrayStaLimit = new ArrayList<>();//所有限制
                for (Group group : mArrayGroup)
                    if (group.getGroup_name().equalsIgnoreCase(groupName)) {
                        Group newGroup = group.cloneOne();
                        newGroup.setState(null);
                        newArrayGroup.add(newGroup);

                        for (DevControlRuleBeen been : mArrayTimeLimit)
                            if (WifiMacUtils.compareMac(been.getMac(), group.getMac())) {
                                DevControlRuleBeen item = been.getCopy();
                                item.setState(null);
                                newArrayStaLimit.add(item);
                            }
                    }
                doSetGroupAndLimitInfo(newArrayGroup, newArrayStaLimit);
            }
        }, _getString(R.string.cancel), null, true);
    }


    @Override
    public void onItemClick(@NonNull AdapterView<?> parent, View view, int position, long id) {
        // 分组列表点击操作
        if (parent.getId() == R.id.listView_group) {
            if (position == 0) return;//默认列表
            gotoGroupDetailPage(mArrayGroupName.get(position - 1).getT1());
        }
    }

    /**
     * 前往分组详情页面
     *
     * @param groupName 组名
     */
    private void gotoGroupDetailPage(@NonNull String groupName) {
        mStatus = 1;
        mTvTitle.setText(R.string.group_list);
        gotoViewFlipperPage(1);
        setBtnAction(false, null, 0);
        mCurrentGroupName = groupName;
        mTvGroupName.setText(groupName);

        DevControlRuleBeen been = getLimitFromStaByGroup(groupName);

        if (been != null)
            mTvHealthTime.setText(been.getHealthString(_getResources().getStringArray(R.array.week_days), _getString(R.string._to)
                    , _getString(R.string.weekends), _getString(R.string.weekdays), _getString(R.string.everyday)));
        else
            mTvHealthTime.setText(null);
        if (been == null) {
            showAlert(_getString(R.string.data_error), _getString(R.string._exit), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    onFragmentLifeListener.onChanged(null);
                }
            });
//            return;
        }
        mListviewClient.setChoiceMode(ListView.CHOICE_MODE_NONE);
        mListviewClient.setAdapter(getCurrentGroupStaAdapter());
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!b) {
            // 保持星期里最少选择了一天
            boolean isAllUnchecked = true;
            for (CheckBox cb : mWeekCheckBoxs) {
                if (cb.isChecked()) {
                    isAllUnchecked = false;
                    break;
                }
            }
            if (isAllUnchecked) {
                compoundButton.setChecked(true);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onKeyBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获取电力猫下挂设备
     */
    private void doGetPlcInfo() {
        addTask(
                new GetPLCLinkInfoTask().execute(getGateway(), new TaskListener<PLCInfo>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(_getString(R.string.downloading), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            if (onFragmentLifeListener != null)
                                onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, PLCInfo param) {
                        if (param != null) {
                            List<StaInfo> list = new ArrayList<>();
                            for (PLCInfo.Dev_Info dev_info : param.getDev_info()) {
                                if (dev_info.getAccess_port() != 0)
                                    list.add(dev_info.turn2StaInfo());
                            }
                            mCurrentLinkDevice = list;
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
     * 获取分组信息
     */
    private void doGetGroupAndLimitInfo() {
        addTask(
                new GetGroupLimitTask().execute(getGateway(), new TaskListener<BaseBeen<List<Group>, List<DevControlRuleBeen>>>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(_getString(R.string.downloading), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, BaseBeen<List<Group>, List<DevControlRuleBeen>> param) {
                        mArrayGroup = param.getT1();
                        refreshPage1ListView(param.getT1());
                        mArrayTimeLimit = param.getT2();
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    /**
     * 刷新第一页分组列表的显示
     *
     * @param groupList
     */
    private void refreshPage1ListView(List<Group> groupList) {
        mArrayGroupName.clear();
        if (groupList.size() > 0) {
            for (Group group : groupList) {
                boolean contain = false;
                for (BaseBeen<String, Integer> stringIntegerBaseBeen : mArrayGroupName) {
                    if (stringIntegerBaseBeen.getT1().equals(group.getGroup_name())) {
                        stringIntegerBaseBeen.setT2(stringIntegerBaseBeen.getT2() + 1);
                        contain = true;
                        break;
                    }
                }
                if (!contain)
                    mArrayGroupName.add(new BaseBeen(group.getGroup_name(), 1));
            }
        }
        mAdapterGroup.notifyDataSetChanged();
    }

    /**
     * 修改分组信息
     */
    private void doSetGroupAndLimitInfo(@Nullable final List<Group> groupList, final List<DevControlRuleBeen> level2Beens) {
        // If edit the rule of my phone , keep the editing at last.
        if (level2Beens != null && level2Beens.size() > 1) {
            // the mac of my phone to protected network.
            String mac = WifiMacUtils.getMac(mActivity);
            for (int i = 0; i < level2Beens.size() - 1; i++) {
                // When same mac ,exchange with the last.
                if (level2Beens.get(i).getMac().equalsIgnoreCase(mac)) {
                    DevControlRuleBeen temp = level2Beens.remove(i);
                    level2Beens.add(temp);
                    break;
                }
            }
        }

        addTask(
                new SetGroupLimitTask().execute(getGateway(), groupList, level2Beens, new TaskListener<List<Group>>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        } else {
                            refreshPageAfterCommitSuccessed(groupList, level2Beens);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, List<Group> param) {

                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    /**
     * 提交成功后的刷新页面
     *
     * @param groupList   新的组信息
     * @param level2Beens 修改了的限制信息列表
     */
    private void refreshPageAfterCommitSuccessed(@Nullable List<Group> groupList, @Nullable List<DevControlRuleBeen> level2Beens) {
        if (mStatus == 0) {//删除分组:更新列表，返回分组列表页面
            refreshGroupList(groupList);
            mAdapterGroup.notifyDataSetChanged();
        } else if (mStatus == 3) {
            refreshRuleList(level2Beens);
            refreshGroupList(groupList);
            refreshPage1ListView(mArrayGroup);
            gotoGroupListPage();
        } else if (mStatus == 6) {
            // 修改当前页面
            mStatus = 1;
            String newName = null;
            for (Group group : mArrayGroup) {
                if (group.getGroup_name().equalsIgnoreCase(mCurrentGroupName)) {
                    for (Group group1 : groupList) {
                        if (group1.getMac().equalsIgnoreCase(group.getMac())) {
                            newName = group1.getGroup_name();
                            break;
                        }
                    }
                    break;
                }
            }
            if (newName == null) {
                showToast(R.string.data_error);
                return;
            }
            mCurrentGroupName = newName;
            mTvGroupName.setText(newName);
            mArrayGroup = groupList;
            refreshPage1ListView(groupList);
        } else if (mStatus == 4) {
            refreshRuleList(level2Beens);
            refreshGroupList(groupList);
            refreshPage1ListView(mArrayGroup);

            mListviewClient.setChoiceMode(ListView.CHOICE_MODE_NONE);
            // CREATE STA NAME
            mListviewClient.setAdapter(getCurrentGroupStaAdapter());
            mStatus = 1;
            setBtnAction(false, null, R.drawable.logo_add_timer);
            setViewStateWhenOnModifyGroupStaList(false);
        } else if (mStatus == 5) {//修改健康时间
            mStatus = 1;
            refreshRuleList(level2Beens);
            mTvTitle.setText(R.string.group_list);
            setBtnAction(false, null, 0);
            DevControlRuleBeen been = getLimitFromStaByGroup(mCurrentGroupName);
            if (been != null)
                mTvHealthTime.setText(been.getHealthString(_getResources().getStringArray(R.array.week_days), _getString(R.string._to)
                        , _getString(R.string.weekends), _getString(R.string.weekdays), _getString(R.string.everyday)));
            gotoViewFlipperPage(1);
        }
    }

    /**
     * 更新规则列表
     *
     * @param level2Beens 有变动的规则
     */
    private void refreshRuleList(List<DevControlRuleBeen> level2Beens) {
        if (level2Beens == null || level2Beens.isEmpty())
            return;

        for (DevControlRuleBeen level2Been : level2Beens) {
            if (level2Been.getState() == null) {//删除
                for (DevControlRuleBeen been : mArrayTimeLimit) {
                    if (WifiMacUtils.compareMac(level2Been.getMac(), been.getMac())) {
                        mArrayTimeLimit.remove(been);
                        break;
                    }
                }
            } else if (level2Been.getState() != null && level2Been.getState()) {//新增
                mArrayTimeLimit.add(level2Been);
            } else {
                boolean isEdit = false;
                for (DevControlRuleBeen been : mArrayTimeLimit) {// 修改
                    if (WifiMacUtils.compareMac(level2Been.getMac(), been.getMac())) {
                        isEdit = true;
                        mArrayTimeLimit.remove(been);
                        if (level2Been.getState() != null) {//修改
                            mArrayTimeLimit.add(level2Been);
                        }
                        break;
                    }
                }
                if (!isEdit) {//未发现，新增
                    mArrayTimeLimit.add(level2Been);
                }
            }
        }
    }

    /**
     * 更新规则列表
     *
     * @param groupList 有变动的规则
     */
    private void refreshGroupList(List<Group> groupList) {
        if (groupList == null || groupList.isEmpty())
            return;

        for (Group group : groupList) {
            if (group.getState() == null) {//删除
                for (Group been : mArrayGroup) {
                    if (been.getMac().equalsIgnoreCase(group.getMac())) {
                        mArrayGroup.remove(been);
                        break;
                    }
                }
            } else if (group.getState()) {//新增
                mArrayGroup.add(group);
            } else {// 修改
                for (Group been : mArrayGroup) {
                    if (been.getMac().equalsIgnoreCase(group.getMac())) {//删除旧的
                        mArrayTimeLimit.remove(been);
                        break;
                    }
                }
                mArrayGroup.add(group);
            }
        }
    }

    /**
     * 防止重复添加，判断mac是否已存在于缓存中
     *
     * @param mac mac地址
     * @return 是否已添加到缓存
     */
    private boolean isInCache(@NonNull String mac) {
        if (mac == null
                || mCurrentLinkDevice == null)
            return false;
        for (StaInfo staInfo : mCurrentLinkDevice) {
            if (mac.equalsIgnoreCase(staInfo.getMac())) {
                return true;
            }
        }
        return false;
    }

    private void onKeyBack() {

        if (mViewFlipper.getDisplayedChild() == 1) {//分组详情页面 返回分组列表页面
            if (mStatus == 4) {
                mListviewClient.setChoiceMode(ListView.CHOICE_MODE_NONE);
                // CREATE STA NAME
                mListviewClient.setAdapter(getCurrentGroupStaAdapter());
                mStatus = 1;
                setBtnAction(false, null, R.drawable.logo_add_timer);
                setViewStateWhenOnModifyGroupStaList(false);
            } else {
                gotoGroupListPage();
            }
        } else if (mViewFlipper.getDisplayedChild() == 0) { //分组列表页面，退出页面
            onFragmentLifeListener.onChanged(null);
        } else if (mViewFlipper.getDisplayedChild() == 2) { // create group 1
            mStatus = 0;
            gotoViewFlipperPage(0);
            mTvTitle.setText(R.string.group_list);
            setBtnAction(true, null, R.drawable.logo_add_timer);
        } else if (mViewFlipper.getDisplayedChild() == 3) { // create group 2
            if (mStatus == 3) {
                mStatus = 2;
                gotoViewFlipperPage(2);
                setBtnAction(true, _getString(R.string.next), 0);
            } else if (mStatus == 5) {
                mStatus = 1;
                gotoViewFlipperPage(1);
                mTvTitle.setText(R.string.group_list);
                setBtnAction(false, null, 0);
            }
        }
    }

    /**
     * 前往第一页  列表分组
     */
    private void gotoGroupListPage() {
        mStatus = 0;
        gotoViewFlipperPage(0);
        setBtnAction(true, null, R.drawable.logo_add_timer);
        mTvTitle.setText(R.string.group_list);
        mCurrentGroupName = null;
    }

    private void gotoViewFlipperPage(int page) {
        int curPage = mViewFlipper.getDisplayedChild();
        if (page == curPage)
            return;

        if (page > curPage) {
            mViewFlipper.setInAnimation(mActivity, R.anim.slide_in_right);
            mViewFlipper.setOutAnimation(mActivity, R.anim.slide_out_left);
        } else {
            mViewFlipper.setInAnimation(mActivity, android.R.anim.slide_in_left);
            mViewFlipper.setOutAnimation(mActivity, android.R.anim.slide_out_right);
        }

        mViewFlipper.setDisplayedChild(page);
    }

    /**
     * @return 获取当前组的下挂设备名称
     */
    private ArrayList<String> getCurrentGroupSta() {
        ArrayList<String> result = new ArrayList<>();
        for (Group group : mArrayGroup) {
            if (mCurrentGroupName.equalsIgnoreCase(group.getGroup_name())) {//同组
                String staMac = group.getMac();
                String name = getStaNameByMac(staMac);
                result.add(TextUtils.isEmpty(name) ? staMac : name);
            }
        }
        return result;
    }

    /**
     * @return 返回全部列表和被选中状态
     */
    private ArrayList<StaInGroupItem> getChoicesOfGroupSta(final String groupName) {
        HashMap<String, StaInGroupItem> hashMap = new HashMap<>();//mac item[mac name group]

        for (Group group : mArrayGroup) {
            StaInGroupItem item = new StaInGroupItem();
            item.mac = group.getMac();
            item.group = group.getGroup_name();
            item.name = getStaNameByMac(item.mac);
            hashMap.put(item.mac, item);
        }

        // 默认组
        for (StaInfo staInfo : mCurrentLinkDevice) {
            if (hashMap.containsKey(staInfo.getMac()))//判断是否存在于分组中
                continue;
            StaInGroupItem item = new StaInGroupItem();
            item.mac = staInfo.getMac();
            item.group = null;
            item.name = staInfo.getName();
            hashMap.put(item.mac, item);
        }
        ArrayList<StaInGroupItem> result = new ArrayList<>();
        result.addAll(hashMap.values());
        Collections.sort(result, new Comparator<StaInGroupItem>() {
            @Override
            public int compare(StaInGroupItem t1, StaInGroupItem t2) {

                if (groupName == null) {
                    if (t1.group == null) {
                        if (t2.group == null)
                            return t1.mac.compareTo(t2.mac);
                        else
                            return -1;
                    } else if (t2.group == null)
                        return 1;

                    return t1.group.compareTo(t2.group);
                }

                if (t1.group == null) {
                    if (t2.group == t1.group)
                        return t1.mac.compareTo(t2.mac);
                    else
                        return 1;
                }

                if (t2.group == null)
                    return -1;

                if (t1.group.equalsIgnoreCase(t2.group))
                    return t1.mac.compareTo(t2.mac);

                if (groupName.equalsIgnoreCase(t2.group))
                    return 1;
                else if (groupName.equalsIgnoreCase(t1.group))
                    return -1;

                return t1.group.compareTo(t2.group);
            }
        });

        return result;
    }

    private class StaInGroupItem {
        String name;
        String mac;
        String group;

        CharSequence getShowCharsequence(int... colors) {
            String tempMac = WifiMacUtils.macShownFormat(mac);
            String prefix = TextUtils.isEmpty(name) ? "[" + tempMac + "]" : name;

            String suffix = TextUtils.isEmpty(group) ? DEFAULT_GROUP : group;
            SpannableString ss = new SpannableString(prefix + "\n" + suffix + '\n' + tempMac);
            if (TextUtils.isEmpty(name))
                ss.setSpan(new ForegroundColorSpan(Color.GRAY), 0, prefix.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

            int end = suffix.length() + prefix.length() + 1;
//            if (!TextUtils.isEmpty(group) && colors != null && colors.length > 0)
            ss.setSpan(new ForegroundColorSpan(colors[0]), prefix.length(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            else
//                ss.setSpan(new ForegroundColorSpan(Color.LTGRAY), prefix.length(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            ss.setSpan(new RelativeSizeSpan(0.8f), prefix.length(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new RelativeSizeSpan(0.9f), end, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return ss;
        }
    }

    /**
     * 根据mac地址获取在线下挂设备列表中的设备名称
     *
     * @param mac
     * @return 获取设备mac
     */
    private String getStaNameByMac(@NonNull String mac) {
        StaInfo staInfo = getStaInfoByMac(mac);
        if (staInfo != null)
            return staInfo.getName();
        return null;
    }

    /**
     * 根据mac地址获取在线下挂设备列表中的设备信息
     *
     * @param mac
     * @return 获取设备mac
     */
    private StaInfo getStaInfoByMac(@NonNull String mac) {
        if (mCurrentLinkDevice != null)
            for (StaInfo staInfo : mCurrentLinkDevice) {
                if (WifiMacUtils.compareMac(mac, staInfo.getMac()))
                    return staInfo;
            }
        return null;
    }

    /**
     * 根据mac从下挂设备的限制上网列表中获取信息
     *
     * @param mac
     * @return
     */
    private DevControlRuleBeen getLimitFromStaByMac(@NonNull String mac) {
        for (DevControlRuleBeen level2Been : mArrayTimeLimit) {
            if (WifiMacUtils.compareMac(mac, level2Been.getMac()))
                return level2Been;
        }
        return null;
    }

    /**
     * 根据分组名称从下挂设备的限制上网列表中获取信息
     *
     * @param groupName
     * @return
     */
    private DevControlRuleBeen getLimitFromStaByGroup(@NonNull String groupName) {
        for (Group group : mArrayGroup) {
            if (group.getGroup_name().equals(groupName)) {
                DevControlRuleBeen been = getLimitFromStaByMac(group.getMac());
                if (been != null)
                    return been;
            }
        }
        return null;
    }

    private void setBtnAction(boolean isShown, String txt, int imgResId) {
        mBtnAction.setVisibility(isShown ? View.VISIBLE : View.INVISIBLE);
        if (isShown) {
            if (imgResId != 0)
                mBtnAction.setCompoundDrawablesRelativeWithIntrinsicBounds(imgResId, 0, 0, 0);
            else
                mBtnAction.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            mBtnAction.setText(txt);
        }
    }

    private void onClickAction(View v) {
        if (mStatus == 4) {//修改分组
            dealEditGroupStaList(mCurrentGroupName);
        }

        // 点击创建分组
        else if (mStatus == 0) {
            gotoCreateGroup1();
        }
        //点击next前往下一步
        else if (mStatus == 2) {
            String name = mEtGroupName.getText().toString();
            // 名称不可为空
            if (TextUtils.isEmpty(name)) {
                showToast(R.string.notice_group_name_empty);
                mEtGroupName.requestFocus();
                return;
            }
            // 名称不可重复
            else
                for (Group group : mArrayGroup)
                    if (name.equalsIgnoreCase(group.getGroup_name())) {
                        showToast(R.string.notice_group_name_used);
                        mEtGroupName.requestFocus();
                        return;
                    }
            long[] checkids = mListViewSta.getCheckItemIds();
            if (checkids.length == 0) {
                showToast(R.string.notice_sta_empty);
                return;
            }
            gotoCreateGroup2();
        }

        //完成创建
        else if (mStatus == 3) {
            dealCreateAndSave();
        } else if (mStatus == 5) {
            dealEditHealthTime();
        }
    }

    private void dealEditHealthTime() {
        //获取时间规则
        boolean[] weekchoice = new boolean[7];
        for (int i = 0; i < mWeekCheckBoxs.length; i++) {
            weekchoice[i] = mWeekCheckBoxs[i].isChecked();
        }
        DevControlRuleBeen ruleNew = new DevControlRuleBeen();//新规则
        ruleNew.setWeekStartAndEnd(weekchoice);
        ruleNew.setStart_time(mTvStartTime.getText().toString());
        ruleNew.setEnd_time(mTvEndTime.getText().toString());

        //修改规则
        final ArrayList<DevControlRuleBeen> staRules = new ArrayList<>();//修改的规则集合
        for (Group group : mArrayGroup) {
            if (group.getGroup_name().equals(mCurrentGroupName)) {
                DevControlRuleBeen rule = ruleNew.getCopy();
                rule.setMac(group.getMac());
                rule.setState(false);
                staRules.add(rule);
            }
        }
        doSetGroupAndLimitInfo(null, staRules);
    }

    /**
     * 修改组下挂设备数量
     */
    private void dealEditGroupStaList(String currentGroupName) {
        long[] checkids = mListviewClient.getCheckItemIds();
        if (checkids.length == 0) {// 未选择下挂设备，即将删除
            showAskDeleteGroup(currentGroupName);
            return;
        }

        final ArrayList<DevControlRuleBeen> arrayModify = new ArrayList<>();//需要提交规则的下挂设备副本
        final ArrayList<Group> newArrayGroup = new ArrayList<>();//所有组信息的副本
        HashMap<String, Integer> hashGroupCount = new HashMap<>();//统计其它各组下挂设备数量

        DevControlRuleBeen rule = getLimitFromStaByGroup(currentGroupName);//获取当前组规则
        for (int i = 0; i < mChoicesOfGroupSta.size(); i++) {
            StaInGroupItem sta = mChoicesOfGroupSta.get(i);
            boolean isChoosed = false;
            boolean isCurGroup = mCurrentGroupName.equalsIgnoreCase(sta.group);
            for (long checkid : checkids) {
                if (i == checkid) {
                    isChoosed = true;
                    break;
                }
            }
            if (isChoosed) {
                if (!isCurGroup) {//新增的  or 变更组的
                    DevControlRuleBeen e = rule.cloneOneForInternetTimeLimit();
                    e.setMac(sta.mac);
                    if (sta.group == null) {//新增的
                        e.setState(true);
                        Group group = new Group(mCurrentGroupName, sta.mac);
                        group.setState(true);
                        newArrayGroup.add(group);
                    } else {//变更组的
                        e.setState(false);
                        //修改
                        for (Group group : mArrayGroup) {
                            if (WifiMacUtils.compareMac(group.getMac(), sta.mac)) {
                                Group ngroup = new Group(mCurrentGroupName, sta.mac);
                                ngroup.setState(false);
                                newArrayGroup.add(ngroup);
                                break;
                            }
                        }
                    }
                    arrayModify.add(e);
                }
            } else if (isCurGroup) {//删除的
                DevControlRuleBeen e = new DevControlRuleBeen();
                e.setMac(sta.mac);
                e.setState(null);
                arrayModify.add(e);
                //删除组信息中的当前mac的信息
                for (Group group : mArrayGroup)
                    if (WifiMacUtils.compareMac(group.getMac(), sta.mac)) {
                        Group ngroup = group.cloneOne();
                        ngroup.setState(null);
                        newArrayGroup.add(ngroup);
                        break;
                    }
            }
        }
        //是否有其他组被清空
        ArrayList<String> arrays = new ArrayList<>();
        if (!hashGroupCount.isEmpty()) {
            Iterator<Map.Entry<String, Integer>> iterator = hashGroupCount.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> next = iterator.next();
                if (next.getValue() == 0) {
                    arrays.add(next.getKey());
                }
            }
        }

        if (arrays.size() == 0) {
            doSetGroupAndLimitInfo(newArrayGroup, arrayModify);
            return;
        }
        String messsage = _getString(R.string.notice_other_group_empty);
        StringBuilder sb = new StringBuilder();
        sb.append(messsage);
        sb.append('\n').append('[');
        int index = messsage.length() + 2;
        for (int i = 0; i < arrays.size(); i++) {
            if (i != 0)
                sb.append(',').append(' ');
            sb.append(arrays.get(i));
        }
        sb.append(']');
        SpannableString ss = new SpannableString(sb.toString());
        for (String str : arrays) {
            ss.setSpan(new ForegroundColorSpan(getRandomColor(180, 360)), index, index + str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            index += str.length();
            index += 2;
        }
        showAlert(ss, _getString(R.string._continue), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doSetGroupAndLimitInfo(newArrayGroup, arrayModify);
            }
        }, _getString(R.string.cancel), null, false);
    }

    /**
     * 处理创建的保存
     */
    private void dealCreateAndSave() {
        boolean[] weekchoice = new boolean[7];
        for (int i = 0; i < mWeekCheckBoxs.length; i++) {
            weekchoice[i] = mWeekCheckBoxs[i].isChecked();
        }
        DevControlRuleBeen ruleNew = new DevControlRuleBeen();//新规则
        ruleNew.setWeekStartAndEnd(weekchoice);
        ruleNew.setStart_time(mTvStartTime.getText().toString());
        ruleNew.setEnd_time(mTvEndTime.getText().toString());

        long[] checkids = mListViewSta.getCheckItemIds();
        String newGroupName = mEtGroupName.getText().toString();//新建的组名
        //组信息列表（平铺）
        final List<Group> groups = new ArrayList<>();
        final ArrayList<DevControlRuleBeen> staRules = new ArrayList<>();//修改的规则集合
        HashMap<String, Integer> hashGroupCount = new HashMap<>();//统计其它各组下挂设备数量
        for (BaseBeen<String, Integer> stringIntegerBaseBeen : mArrayGroupName) {
            hashGroupCount.put(stringIntegerBaseBeen.getT1(), stringIntegerBaseBeen.getT2());
        }
        for (long checkid : checkids) {
            StaInGroupItem itemChoosed = mChoicesOfGroupSta.get((int) checkid);
            DevControlRuleBeen newRule = ruleNew.cloneOneForInternetTimeLimit();
            newRule.setMac(itemChoosed.mac);
            if (itemChoosed.group == null) {//从默认列表中获取到的
                newRule.setState(true);
            } else {//从其他组弄过来的
                for (Group group : mArrayGroup) {
                    if (WifiMacUtils.compareMac(group.getMac(), itemChoosed.mac)) {
                        groups.add(group.cloneOne().setState(null));
                        hashGroupCount.put(group.getGroup_name(), hashGroupCount.get(group.getGroup_name()) - 1);
                        break;
                    }
                }
                newRule.setState(false);
            }
            staRules.add(newRule);
            //新建的
            groups.add(new Group(newGroupName, itemChoosed.mac).setState(true));
        }

        //是否有其他组被清空
        ArrayList<String> arrays = new ArrayList<>();
        if (!hashGroupCount.isEmpty()) {
            Iterator<Map.Entry<String, Integer>> iterator = hashGroupCount.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> next = iterator.next();
                if (next.getValue() == 0) {
                    arrays.add(next.getKey());
                }
            }
        }

        if (arrays.size() == 0) {
            doSetGroupAndLimitInfo(groups, staRules);
            return;
        }
        String messsage = _getString(R.string.notice_other_group_empty);
        StringBuilder sb = new StringBuilder();
        sb.append(messsage);
        sb.append('\n').append('[');
        int index = messsage.length() + 2;
        for (int i = 0; i < arrays.size(); i++) {
            if (i != 0)
                sb.append(',').append(' ');
            sb.append(arrays.get(i));
        }
        sb.append(']');
        SpannableString ss = new SpannableString(sb.toString());
        for (String str : arrays) {
            ss.setSpan(new ForegroundColorSpan(getRandomColor(180, 360)), index, index + str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            index += str.length();
            index += 2;
        }
        showAlert(ss, _getString(R.string._continue), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doSetGroupAndLimitInfo(groups, staRules);
            }
        }, _getString(R.string.cancel), null, true);
    }

    /**
     * 当修改分组下的下挂mac列表的时候，切换状态
     *
     * @param isModifyState 是否修改状态
     */
    private void setViewStateWhenOnModifyGroupStaList(boolean isModifyState) {
        mBtnEditGroupName.setClickable(!isModifyState);
        mBtnEditClient.setClickable(!isModifyState);
        mBtnEditNetWorkTime.setClickable(!isModifyState);
        Animation anim = AnimationUtils.loadAnimation(mActivity, isModifyState ? R.anim.view_disable : R.anim.view_enable);
        anim.setFillAfter(true);
        ((View) mBtnEditGroupName.getParent()).startAnimation(anim);
        ((View) mBtnEditClient.getParent()).startAnimation(anim);
        ((View) mBtnEditNetWorkTime.getParent()).startAnimation(anim);
    }

    /**
     * 设置时间
     *
     * @param textView 预备修改的文字组件
     */
    private void setTimeTextView(@NonNull final TextView textView) {
        Date date;
        try {
            date = mSDFormatHHmm.parse(textView.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        TimePickerDialog timeDialog = new TimePickerDialog(mActivity, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                textView.setText(String.format("%02d:%02d", hourOfDay, minute));
            }
        }, 12, 0, true);
        timeDialog.updateTime(date.getHours(), date.getMinutes());
        timeDialog.show();
    }

    /**
     * @return 当前组的分组情况
     */
    private Client2Adapter getCurrentGroupStaAdapter() {
        List<StaInfo> data = new ArrayList<>();
        for (Group group : mArrayGroup) {
            if (group.getGroup_name().equals(mCurrentGroupName)) {
                StaInfo staInfo = getStaInfoByMac(group.getMac());
                if (staInfo == null) {
                    staInfo = new StaInfo();
                    staInfo.setMac(WifiMacUtils.macShownFormat(group.getMac()));
                }
                data.add(staInfo);
            }
        }
        return new Client2Adapter(mActivity, R.layout.simple_list_item_1, data);
    }

    private int getRandomColor(@IntRange(from = 0, to = 360) int hStart, @IntRange(from = 0, to = 360) int hEnd) {
        return Color.HSVToColor(new float[]{Math.round(Math.random() * (hEnd - hStart) + hStart), 1, 1});
    }
}
