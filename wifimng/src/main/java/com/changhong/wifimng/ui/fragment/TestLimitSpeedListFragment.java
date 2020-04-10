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
import com.changhong.wifimng.been.wan.Level2Been;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.router.DelSpeedLimitsLoadTask;
import com.changhong.wifimng.task.router.GetSpeedLimitLoadTask;

import java.util.ArrayList;
import java.util.List;

public class TestLimitSpeedListFragment extends BaseFragment implements View.OnClickListener {
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
        tvTitle.setText("Test speed limit list");

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
        doLoadSpeedLimit();
    }

    private void doLoadSpeedLimit() {
        addTask(
                new GetSpeedLimitLoadTask().execute(getGateway(), getCookie(), new TaskListener<List<Level2Been>>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result == TaskResult.OK) {

                        } else {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, List<Level2Been> param) {
                        mListData.clear();
                        if (param != null) {
                            mListData.addAll(param);
                        }
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doDelSpeedLimit(List<String> macs, boolean isEffectImmediately) {
        addTask(
                new DelSpeedLimitsLoadTask().execute(getGateway(), macs, isEffectImmediately, getCookie(), new TaskListener() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.commiting), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result == TaskResult.OK) {
                            showToast(R.string.commit_completed);
                            onFragmentLifeListener.onChanged(null);
                        } else {
                            showTaskError(task, R.string.interaction_failed);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, Object param) {
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
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
                final ArrayList<String> macs = new ArrayList<>();
                for (long checkId : checkIds) {
                    macs.add(mListData.get((int) checkId).getMac());
                }
                showAlert(_getString(R.string.notice_ask_execute_immediately), _getString(R.string.save_and_execute), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doDelSpeedLimit(macs, true);
                    }
                }, _getString(R.string.save_only), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doDelSpeedLimit(macs, false);
                    }
                }, true);
            }
        }
    }

    private class MyAdapter extends BaseAdapter {
        private Context mContext;
        private List<Level2Been> mData;

        public MyAdapter(Context mContext, List<Level2Been> mData) {
            this.mContext = mContext;
            this.mData = mData;
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
            textView.setText(item.getMac() + "\n up: " + item.getMax_upload() + "\t  down:" + item.getMax_download());

            return view;
        }
    }
}
