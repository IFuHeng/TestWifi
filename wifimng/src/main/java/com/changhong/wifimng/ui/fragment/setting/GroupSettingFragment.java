package com.changhong.wifimng.ui.fragment.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.http.been.DeviceDetailBeen;
import com.changhong.wifimng.http.been.Group;
import com.changhong.wifimng.http.been.GroupListBeen;
import com.changhong.wifimng.http.task.DeleteGroupTask;
import com.changhong.wifimng.http.task.DeviceDetailTask;
import com.changhong.wifimng.http.task.DeviceGroupTask;
import com.changhong.wifimng.http.task.GetGroupListTask;
import com.changhong.wifimng.http.task.NewCreateGroupTask;
import com.changhong.wifimng.http.task.UpdateGroupTask;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.fragment.InputDialog;
import com.changhong.wifimng.ui.fragment.OnFragmentLifeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 分组设置
 */
public class GroupSettingFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private MyAdapter mAdapter;
    private List<Group> mArrayGroup;
    private ListView mListView;

    private int mTotleNum;

    private String mGroupUUid;
    private SpannableString mDefaultAddString;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArrayGroup = new ArrayList<>();

        SpannableString spannableString = new SpannableString(getString(R.string.new_group));
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mDefaultAddString = spannableString;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_setting, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        doLoadGroup(true, 1, 10);
        doGetDeviceDetailInfoFromCloud();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        view.findViewById(R.id.btn_back).setOnClickListener(this);

        mListView = view.findViewById(R.id.listview);
        mAdapter = new MyAdapter(mActivity, mArrayGroup, this);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_back) {
            onFragmentLifeListener.onChanged(null);
        } else if (view.getId() == R.id.btn_edit) {
            showEditGroupDialog((Integer) view.getTag());
            Log.d(getClass().getSimpleName(), "====~ btn_edit click :" + view.getTag());
        } else if (view.getId() == R.id.btn_delete) {
            Log.d(getClass().getSimpleName(), "====~ btn_delete click :" + view.getTag());
            doDeleteGroup((Integer) view.getTag());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mArrayGroup.size() > i) {
            showNoticeOfChangeGroup(i);
        } else
            showCreateGroupDialog();
    }

    private void showNoticeOfChangeGroup(int index) {
        final Group group = mArrayGroup.get(index);
        showAlert(
                String.format(_getString(R.string.notice_device_join_group), group.getGroupName()),
                _getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doAdd2Group(group);
                    }
                },
                _getString(R.string.cancel), null, true);
    }

    private void showCreateGroupDialog() {
        InputDialog dialog = new InputDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(InputDialog.EXTRA_PASSWORD, false);
        bundle.putBoolean(InputDialog.KEY_CANCEL_ABLE, false);
        bundle.putString(Intent.EXTRA_TEXT, _getString(R.string.notice_new_group_name));
        dialog.setArguments(bundle);
        dialog.setFragmentListener(new OnFragmentLifeListener<String>() {

            @Override
            public void onChanged(String o) {
                if (!TextUtils.isEmpty(o)) {
                    doCreateGroup(o);
                } else {
                }
            }

        });
        dialog.show(getFragmentManager(), "group_create");
    }

    private void showEditGroupDialog(final int index) {
        InputDialog dialog = new InputDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(InputDialog.EXTRA_PASSWORD, false);
        bundle.putBoolean(InputDialog.KEY_CANCEL_ABLE, false);
        bundle.putString(Intent.EXTRA_TEXT, _getString(R.string.notice_new_group_name));
        dialog.setArguments(bundle);
        dialog.setFragmentListener(new OnFragmentLifeListener<String>() {

            @Override
            public void onChanged(String o) {
                if (!TextUtils.isEmpty(o)) {
                    doUpdateGroup(index, o);
                } else {
                }
            }

        });
        dialog.show(getFragmentManager(), "group_create");
    }

    private void doCreateGroup(String groupName) {
        addTask(
                new NewCreateGroupTask().execute(groupName, mInfoFromApp, new TaskListener<Group>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(_getString(R.string.downloading), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                        } else {
                            showToast(R.string.add_success);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, Group param) {
                        //TODO 重新加载最后一个group信息
                        mArrayGroup.add(0, param);
                        mListView.smoothScrollToPosition(0);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doLoadGroup(boolean isFirst, int currentPage, final int pageSize) {
        if (isFirst) {
            mArrayGroup.clear();
        } else if (mTotleNum != 0 && mTotleNum <= mArrayGroup.size()) {
            showToast(R.string.no_more);
            return;
        }
        addTask(
                new GetGroupListTask().execute(currentPage, pageSize, mInfoFromApp, new TaskListener<GroupListBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, GroupListBeen param) {
                        if (param != null) {
                            mTotleNum = param.getTotalSize();
                            mArrayGroup.addAll(param.getList());
                            mAdapter.notifyDataSetChanged();

                            if (param.getCurrentPage() < param.getTotalPage())
                                doLoadGroup(false, param.getCurrentPage() + 1, param.getPageSize());
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                    }
                })
        );
    }

    private void doDeleteGroup(final int index) {

        addTask(
                new DeleteGroupTask().execute(mArrayGroup.get(index).getUuid(), mInfoFromApp, new TaskListener<GroupListBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        } else {
                            mArrayGroup.remove(index);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, GroupListBeen param) {

                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                    }
                })
        );
    }

    private void doUpdateGroup(final int index, final String groupName) {

        addTask(
                new UpdateGroupTask().execute(mArrayGroup.get(index).getUuid(), groupName, mInfoFromApp, new TaskListener<GroupListBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        } else {
                            mArrayGroup.get(index).setGroupName(groupName);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, GroupListBeen param) {
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                    }
                })
        );
    }

    private class MyAdapter extends BaseAdapter {
        Context context;
        private List<Group> mData;
        private View.OnClickListener mOnclickListener;

        public MyAdapter(Context context, List<Group> mData, View.OnClickListener listener) {
            this.context = context;
            this.mData = mData;
            this.mOnclickListener = listener;
        }

        @Override
        public int getCount() {
            if (mData == null)
                return 1;
            return mData.size() + 1;
        }

        @Override
        public Object getItem(int i) {
            if (mData == null)
                return null;
            return mData.get(i);
        }

        @Override
        public long getItemId(int i) {
            if (mData == null)
                return 0;
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_group, null, false);
            }
            TextView textView = view.findViewById(R.id.tv_name);
            textView.setTypeface(Typeface.DEFAULT);
            if (i < mArrayGroup.size()) {
                if (mGroupUUid != null && mGroupUUid.equalsIgnoreCase(mData.get(i).getUuid()))
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                textView.setText(mData.get(i).getGroupName());
            } else
                textView.setText(mDefaultAddString);

            view.findViewById(R.id.btn_edit).setOnClickListener(mOnclickListener);
            view.findViewById(R.id.btn_edit).setTag(i);
            view.findViewById(R.id.btn_edit).setVisibility(i == getCount() - 1 ? View.GONE : View.VISIBLE);

            view.findViewById(R.id.btn_delete).setOnClickListener(mOnclickListener);
            view.findViewById(R.id.btn_delete).setTag(i);
            view.findViewById(R.id.btn_delete).setVisibility(i == getCount() - 1 ? View.GONE : View.VISIBLE);

            return view;
        }
    }

    public void doAdd2Group(final Group group) {
        addTask(
                new DeviceGroupTask().execute(group.getUuid(), mInfoFromApp.getDevcieUuid(), mInfoFromApp, new TaskListener() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {

                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);
                            onFragmentLifeListener.onChanged(null);
                        } else {
                            showToast(R.string.commit_completed);
                            mGroupUUid = group.getUuid();
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, Object param) {

                    }

                    @Override
                    public void onCancelled(GenericTask task) {

                    }
                })
        );
    }

    /**
     * 从云端获取设备详细信息
     */
    private void doGetDeviceDetailInfoFromCloud() {
        addTask(
                new DeviceDetailTask().execute(mInfoFromApp, new TaskListener<DeviceDetailBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(GenericTask task) {
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, DeviceDetailBeen param) {
                        mGroupUUid = param.getGroupId();
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }
}
