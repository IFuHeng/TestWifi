package com.changhong.wifimng.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

public class TestInternetTimeLimitFragment extends BaseFragment implements View.OnClickListener {
    private ListView mListView;
    private List<Level2Been> mListData;
    private MyAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListData = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_detail, null, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText("Test INTERNET time limit list");

        view.findViewById(R.id.btn_back).setOnClickListener(this);
        TextView tvDelete = view.findViewById(R.id.btn_save);
        tvDelete.setText(R.string.delete);
        tvDelete.setOnClickListener(this);

        mListView = view.findViewById(R.id.list_device);
        mAdapter = new MyAdapter(mActivity, mListData);
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        doGetGroupAndLimitInfo();
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
                        mListData.clear();
                        if (param != null)
                            mListData.addAll(param.getT2());
                        mAdapter.notifyDataSetChanged();
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
                            refreshRuleList(level2Beens);
                            mAdapter.notifyDataSetChanged();
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
     * 更新规则列表
     *
     * @param level2Beens 有变动的规则
     */
    private void refreshRuleList(List<Level2Been> level2Beens) {
        if (level2Beens == null || level2Beens.isEmpty())
            return;

        for (Level2Been level2Been : level2Beens) {
            if (level2Been.getState() == null) {//删除
                for (Level2Been been : mListData) {
                    if (WifiMacUtils.compareMac(level2Been.getMac(), been.getMac())) {
                        mListData.remove(been);
                        break;
                    }
                }
            } else if (level2Been.getState() != null && level2Been.getState()) {//新增
                mListData.add(level2Been);
            } else {
                boolean isEdit = false;
                for (Level2Been been : mListData) {// 修改
                    if (WifiMacUtils.compareMac(level2Been.getMac(), been.getMac())) {
                        isEdit = true;
                        mListData.remove(been);
                        if (level2Been.getState() != null) {//修改
                            mListData.add(level2Been);
                        }
                        break;
                    }
                }
                if (!isEdit) {//未发现，新增
                    mListData.add(level2Been);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_back) {
            onFragmentLifeListener.onChanged(null);
        } else if (view.getId() == R.id.btn_save) {
            long[] checkIds = mListView.getCheckItemIds();
            if (checkIds == null || checkIds.length == 0)
                onFragmentLifeListener.onChanged(null);
            else {
                final ArrayList<Level2Been> macs = new ArrayList<>();
                for (long checkId : checkIds) {
                    Level2Been item = mListData.get((int) checkId);
                    item.setState(null);
                    macs.add(item);
                }
                showAlert(_getString(R.string.notice_ask_execute_immediately), _getString(R.string.save_and_execute), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doSetGroupAndLimitInfo(null, macs);
                    }
                }, _getString(R.string.save_only), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doSetGroupAndLimitInfo(null, macs);
                    }
                }, true);
            }
        }
    }

    private class MyAdapter extends BaseAdapter {
        private String WEEK_DAYS;
        private String WEEK_ENDS;
        private String TO;
        private String EVERY_DAY;
        private String[] WEEK_DAY;
        private Context mContext;
        private List<Level2Been> mData;

        public MyAdapter(Context mContext, List<Level2Been> mData) {
            this.mContext = mContext;
            this.mData = mData;

            WEEK_DAY = mContext.getResources().getStringArray(R.array.week_days);
            WEEK_DAYS = mContext.getString(R.string.weekdays);
            WEEK_ENDS = mContext.getString(R.string.weekends);
            TO = mContext.getString(R.string._to);
            EVERY_DAY = mContext.getString(R.string.everyday);
        }

        @Override
        public int getCount() {
            if (this.mData == null)
                return 0;
            return this.mData.size();
        }

        @Override
        public Object getItem(int i) {
            if (this.mData == null)
                return null;
            return this.mData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.simple_list_item_multiple_choice, null, false);
            }

            TextView textView = (TextView) view;
            Level2Been item = mData.get(i);
            textView.setText(item.getMac() + '\n' + item.getHealthString(WEEK_DAY, TO, WEEK_ENDS, WEEK_DAYS, EVERY_DAY));

            return view;
        }
    }
}
