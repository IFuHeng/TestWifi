package com.changhong.wifimng.ui.fragment;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.Group;
import com.changhong.wifimng.been.wan.Level2Been;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.router.GetGroupLimitTask;
import com.changhong.wifimng.task.router.SetGroupLimitTask;
import com.changhong.wifimng.uttils.WifiMacUtils;

import java.util.ArrayList;
import java.util.List;

public class ClientDetail2Fragment extends ClientDetailFragment {

    private List<Group> mArrayGroup;
    private List<Level2Been> mArrayTimeLimit;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View btnGroup = view.findViewById(R.id.panel_group);
        btnGroup.setVisibility(View.VISIBLE);
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        int index = viewGroup.indexOfChild(btnGroup);
        viewGroup.getChildAt(index + 1).setVisibility(View.VISIBLE);
    }

    protected void showGroup(View v) {
        if (mArrayGroup == null) {// 没有分组信息
            showToast(R.string.no_other_group);
            return;
        }

        final String[] choices = getGroupChoices();
        showPopWinChoose(v, choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String newGroup = choices[i];
                String oldGroup = getCurrentGroupName();
                if ((oldGroup == null && i == 0)
                        || (i != 0 && newGroup.equalsIgnoreCase(oldGroup))) {//未修改
                    return;
                }

                final ArrayList<Group> newArrayGroup = new ArrayList<>();
                final ArrayList<Level2Been> arrayLimitRule = new ArrayList<>();

                if (oldGroup == null) {//新增
                    for (Group group : mArrayGroup) {
                        newArrayGroup.add(group.cloneOne());
                    }
                    newArrayGroup.add(new Group(newGroup, mStaInfo.getMac()));
                    Level2Been rule = getLimitFromStaByGroup(newGroup);
                    rule = rule.cloneOneForInternetTimeLimit();
                    rule.setState(true);
                    arrayLimitRule.add(rule);
                    doSetGroupAndLimitInfo(newArrayGroup, arrayLimitRule);
                    return;
                }


                if (i == 0) {//选择取消分组
                    for (Group group : mArrayGroup) {
                        if (!WifiMacUtils.compareMac(group.getMac(), mStaInfo.getMac()))
                            newArrayGroup.add(group.cloneOne());
                    }
                    Level2Been rule = getLimitFromStaByMac(mStaInfo.getMac());
                    rule.setMac(mStaInfo.getMac());

                    arrayLimitRule.add(rule);
                    doSetGroupAndLimitInfo(newArrayGroup, arrayLimitRule);
                }
                else {//变更
                    //统计分组数量
                    int oldGroupCount = 0;
                    for (Group group : mArrayGroup) {
                        newArrayGroup.add(group);
                        if (group.getGroup_name().equalsIgnoreCase(oldGroup))
                            ++oldGroupCount;
                    }
                    //修改分组
                    for (Group group : mArrayGroup) {
                        if (WifiMacUtils.compareMac(group.getMac(), mStaInfo.getMac())) {
                            mArrayGroup.remove(group);
                            group = group.cloneOne();
                            group.setGroup_name(newGroup);
                            newArrayGroup.add(group);
                            oldGroupCount--;
                            break;
                        }
                    }
                    if (oldGroupCount == 0) {
                        SpannableString ss = new SpannableString(_getString(R.string.notice_other_group_empty) + '\n' + '[' + oldGroup + ']');
                        ss.setSpan(new ForegroundColorSpan(getRandomColor(180, 360)), ss.length() - oldGroup.length() - 1, ss.length() - 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        showAlert(ss, _getString(R.string._continue), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                doSetGroupAndLimitInfo(newArrayGroup, arrayLimitRule);
                            }
                        }, _getString(R.string.cancel), null, false);
                    } else
                        doSetGroupAndLimitInfo(newArrayGroup, arrayLimitRule);
                }
            }
        });
    }

    /**
     * 获取分组信息
     */
    private void doGetGroupAndLimitInfo() {
        addTask(
                new GetGroupLimitTask().execute(getGateway(), getCookie(), new TaskListener<BaseBeen<List<Group>, List<Level2Been>>>() {
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
                    public void onProgressUpdate(GenericTask task, BaseBeen<List<Group>, List<Level2Been>> param) {
                        mArrayGroup = param.getT1();
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
     * 修改分组信息
     */
    private void doSetGroupAndLimitInfo(@Nullable final List<Group> groupList, final List<Level2Been> level2Beens) {
        // If edit the rule of my phone , keep the editing at last.
        if (level2Beens != null && level2Beens.size() > 1) {
            // the mac of my phone to protected network.
            String mac = WifiMacUtils.getMac(mActivity);
            for (int i = 0; i < level2Beens.size() - 1; i++) {
                // When same mac ,exchange with the last.
                if (level2Beens.get(i).getMac().equalsIgnoreCase(mac)) {
                    Level2Been temp = level2Beens.remove(i);
                    level2Beens.add(temp);
                    break;
                }
            }
        }

        addTask(
                new SetGroupLimitTask().execute(getGateway(), groupList, level2Beens, getCookie(), new TaskListener<Boolean>() {
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
     * 根据mac从下挂设备的限制上网列表中获取信息
     *
     * @param mac
     * @return
     */
    private Level2Been getLimitFromStaByMac(@NonNull String mac) {
        for (Level2Been level2Been : mArrayTimeLimit) {
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
    private Level2Been getLimitFromStaByGroup(@NonNull String groupName) {
        for (Group group : mArrayGroup) {
            if (group.getGroup_name().equals(groupName)) {
                Level2Been been = getLimitFromStaByMac(group.getMac());
                if (been != null)
                    return been;
            }
        }
        return null;
    }

    private String getCurrentGroupName() {
        if (mArrayGroup == null)
            return null;

        String curGroup = null;
        for (Group group : mArrayGroup) {
            if (WifiMacUtils.compareMac(group.getMac(), mStaInfo.getMac())) {
                curGroup = group.getGroup_name();
                break;
            }
        }

        return curGroup;
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
                ArrayList<Level2Been> newArrayStaLimit = new ArrayList<>();//所有限制
                for (Group group : mArrayGroup)
                    if (!group.getGroup_name().equalsIgnoreCase(groupName))
                        newArrayGroup.add(group);
                    else
                        for (Level2Been been : mArrayTimeLimit)
                            if (WifiMacUtils.compareMac(been.getMac(), group.getMac())) {
                                Level2Been item = been.getCopy();
                                item.setState(null);
                                newArrayStaLimit.add(item);
                            }

                doSetGroupAndLimitInfo(newArrayGroup, newArrayStaLimit);
            }
        }, _getString(R.string.cancel), null, true);
    }

    private int getRandomColor(@IntRange(from = 0, to = 360) int hStart, @IntRange(from = 0, to = 360) int hEnd) {
        return Color.HSVToColor(new float[]{Math.round(Math.random() * (hEnd - hStart) + hStart), 1, 1});
    }

    private String[] getGroupChoices() {
        ArrayList<String> list = new ArrayList();
        list.add(_getString(R.string.default_group));
        for (Group group : mArrayGroup) {
            if (list.contains(group.getGroup_name()))
                continue;
            list.add(group.getGroup_name());
        }
        final String[] choices = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            choices[i] = list.get(i);
        }
        return choices;
    }
}
