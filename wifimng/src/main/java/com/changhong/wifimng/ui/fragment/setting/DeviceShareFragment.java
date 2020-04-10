package com.changhong.wifimng.ui.fragment.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.http.been.FamilyMemberListBeen;
import com.changhong.wifimng.http.been.UserInfo;
import com.changhong.wifimng.http.task.DealSharingTask;
import com.changhong.wifimng.http.task.GetFamilyMemberListTask;
import com.changhong.wifimng.http.task.GetSharedUserListTask;
import com.changhong.wifimng.http.task.ShareDeviceTask;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.fragment.InputDialog;
import com.changhong.wifimng.ui.fragment.OnFragmentLifeListener;
import com.changhong.wifimng.uttils.CommUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 分享控制
 */
public class DeviceShareFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private ListView mListViewMember;
    private ListView mListViewHistory;
    private TextView mTvHistory;

    private ArrayList<UserInfo> mArrayMember;
    private ArrayList<UserInfo> mArrayShareUser;

    private BaseAdapter mAdapterMember;
    private BaseAdapter mAdapterHistory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mArrayMember = new ArrayList<>();
        mArrayShareUser = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_share, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_back).setOnClickListener(this);

        TextView tvName = view.findViewById(R.id.tv_name);
        tvName.setText(mInfoFromApp.getDeviceName());
        {
            Drawable icondraw = getResources().getDrawable(DeviceType.getDeviceTypeFromName(mInfoFromApp.getDeviceType()).getThumbResId());
            int size = CommUtil.dip2px(mActivity, 48);
            icondraw.setBounds(0, 0, size, size);
            tvName.setCompoundDrawables(icondraw, null, null, null);
        }

        mTvHistory = view.findViewById(R.id.tv_history);

        mListViewMember = view.findViewById(R.id.listview_member);
        SpannableString spannableString = new SpannableString(getString(R.string.other));
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mAdapterMember = new MemberAdapter(mActivity, mArrayMember, spannableString);
        mListViewMember.setAdapter(mAdapterMember);
        mListViewMember.setOnItemClickListener(this);

        mListViewHistory = view.findViewById(R.id.listview_record);
        mAdapterHistory = new HistoryAdapter(mActivity, mArrayShareUser, this);
        mListViewHistory.setAdapter(mAdapterHistory);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        doLoadMember(true, 1, 6);
        doGetSharedUser(true, 1, 6, false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_back) {
            onFragmentLifeListener.onChanged(null);
        } else if (view.getId() == R.id.btn_delete) {
            final UserInfo userInfo = (UserInfo) view.getTag();
            showAlert(getString(R.string.notice_delete_share_ask), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    doDealHistory(userInfo.getUuid(), 4);
                }
            }, getString(R.string.cancel), null, true);
        } else if (view.getId() == R.id.btn_cancel) {
            final UserInfo userInfo = (UserInfo) view.getTag();
            showAlert(getString(R.string.notice_withdraw_share_ask), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    doDealHistory(userInfo.getUuid(), 3);
                }
            }, getString(R.string.cancel), null, true);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.listview_member) {
            if (i < mArrayMember.size()) {
                String user = mArrayMember.get(i).getPhoneNumber();
                if (TextUtils.isEmpty(user))
                    user = mArrayShareUser.get(i).getEmail();
                doShare2User(user);
            } else {
                showShare2OtherDialog();
            }
        }
    }

    private void showShare2OtherDialog() {
        InputDialog dialog = new InputDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(InputDialog.EXTRA_PASSWORD, false);
        bundle.putBoolean(InputDialog.KEY_CANCEL_ABLE, false);
        bundle.putString(Intent.EXTRA_TEXT, getString(R.string.notice_enter_the_account_of_user_share));
        dialog.setArguments(bundle);
        dialog.setFragmentListener(new OnFragmentLifeListener<String>() {

            @Override
            public void onChanged(String o) {
                if (!TextUtils.isEmpty(o)) {
                    doShare2User(o);
                } else {

                }
            }

        });
        dialog.show(getFragmentManager(), "share_2_someone");
    }

    private class MemberAdapter extends BaseAdapter {
        protected Context context;
        protected List<UserInfo> mData;
        private CharSequence mDefaultText;

        public MemberAdapter(Context context, List<UserInfo> data, CharSequence defaultText) {
            this.context = context;
            this.mData = data;
            this.mDefaultText = defaultText;
        }

        @Override
        public int getCount() {
            int count = 1;

            if (mData != null)
                count += mData.size();
            return count;
        }

        @Override
        public Object getItem(int i) {

            UserInfo userInfo = getMenber(i);
            if (userInfo != null)
                return userInfo.getUsername();

            return mDefaultText;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null, false);
                ((TextView) view).setCompoundDrawablePadding(CommUtil.dip2px(context, 10));
            }

            TextView textView = (TextView) view;
            UserInfo userInfo = getMenber(i);
            if (userInfo != null) {
                int resId = R.drawable.account;
                textView.setCompoundDrawablesWithIntrinsicBounds(resId, 0, R.drawable.ic_navigate_next_black_24dp, 0);
                textView.setText(userInfo.getName() + "\n" + userInfo.getUsername());
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                textView.setText(mDefaultText);
            }

            return view;
        }

        private UserInfo getMenber(int i) {
            if (mData != null && i < mData.size()) {
                return mData.get(i);
            }
            return null;
        }
    }

    private class HistoryAdapter extends BaseAdapter {
        private View.OnClickListener mListener;
        protected Context context;
        protected List<UserInfo> mData;

        public HistoryAdapter(Context context, List<UserInfo> mData, View.OnClickListener listener) {
            this.context = context;
            this.mData = mData;
            this.mListener = listener;
        }

        @Override
        public int getCount() {
            if (mData == null)
                return 0;
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            if (mData == null)
                return null;
            else
                return mData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_share_history, null, false);
                view.findViewById(R.id.btn_cancel).setOnClickListener(mListener);
//                view.findViewById(R.id.btn_delete).setOnClickListener(mListener);
                view.findViewById(R.id.btn_delete).setVisibility(View.GONE);
            }

            TextView textView = view.findViewById(R.id.tv_name);

            String content = context.getString(R.string._I) + " -> " + mData.get(i).getName();

            String state;
            int color;
            if (true == mData.get(i).getAccept()) {
                state = context.getString(R.string.accepted);
                color = context.getResources().getColor(R.color.colorPrimary);
            } else {
                state = context.getString(R.string.to_be_accept);
                color = context.getResources().getColor(R.color.colorAccent);
            }
            SpannableString ss = new SpannableString(content + '\n' + state);
            ss.setSpan(new RelativeSizeSpan(0.8f), content.length() + 1, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(color), content.length() + 1, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            textView.setText(ss);

            view.findViewById(R.id.btn_cancel).setTag(mData.get(i));
            view.findViewById(R.id.btn_delete).setTag(mData.get(i));

            return view;
        }
    }

    private void doLoadMember(final boolean isFirst, int currentPage, int pageSize) {
        if (isFirst) {
            mArrayMember.clear();
        }
        addTask(
                new GetFamilyMemberListTask().execute(currentPage, pageSize, mInfoFromApp, new TaskListener<FamilyMemberListBeen>() {
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
                            if (isFirst)
                                onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, FamilyMemberListBeen param) {
                        mArrayMember.addAll(param.getList());
                        mAdapterMember.notifyDataSetChanged();
                        if (param.getCurrentPage() < param.getTotalPage())
                            doLoadMember(false, param.getCurrentPage() + 1, param.getPageSize());
                    }

                    @Override
                    public void onCancelled(GenericTask task) {

                    }
                })
        );
    }

    private void doGetSharedUser(final boolean isFirst, int currentPage, final int pageSize, final boolean isAccepted) {
        if (isFirst) {
            mArrayShareUser.clear();
        }
        addTask(
                new GetSharedUserListTask().execute(currentPage, pageSize, isAccepted ? 1 : 0, mInfoFromApp, new TaskListener<FamilyMemberListBeen>() {
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
                            if (isFirst)
                                onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, FamilyMemberListBeen param) {
                        for (UserInfo familyMemberBeen : param.getList()) {
                            familyMemberBeen.setAccept(isAccepted);
                            mArrayShareUser.add(familyMemberBeen);
                        }
//                        if (param.getList().size() > 0)
                        mAdapterHistory.notifyDataSetChanged();

                        if (mArrayShareUser.size() > 0)
                            mTvHistory.setText(getString(R.string.share_history) + '(' + mArrayShareUser.size() + ')');

                        if (param.getCurrentPage() < param.getTotalPage())
                            doGetSharedUser(false, param.getCurrentPage() + 1, param.getPageSize(), isAccepted);
                        else if (!isAccepted) {
                            doGetSharedUser(false, 1, 6, true);
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {

                    }
                })
        );
    }

    private void doShare2User(String userAccount) {
        ArrayList<String> deviceList = new ArrayList<>();
        deviceList.add(mInfoFromApp.getDevcieUuid());
        addTask(
                new ShareDeviceTask().execute(deviceList, userAccount, mInfoFromApp, new TaskListener() {
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
                        } else
                            doGetSharedUser(true, 1, 6, false);
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

//    public void doGetHistory(final boolean isFirst, int currentPage, final int pageSize) {
//        if (isFirst) {
//            mArrayHistory.clear();
//            mTvHistory.setText(R.string.share_history);
//        }
//        addTask(
//                new GetSharedListTask().execute(null, currentPage, pageSize, mInfoFromApp, new TaskListener<DeviceListBeen>() {
//                    @Override
//                    public String getName() {
//                        return null;
//                    }
//
//                    @Override
//                    public void onPreExecute(GenericTask task) {
//
//                    }
//
//                    @Override
//                    public void onPostExecute(GenericTask task, TaskResult result) {
//                        if (result != TaskResult.OK) {
//                            showTaskError(task, R.string.interaction_failed);
//                        }
//                    }
//
//                    @Override
//                    public void onProgressUpdate(GenericTask task, DeviceListBeen param) {
//                        for (DeviceBeen deviceBeen : param.getList()) {
//                            //只显示我分享当前设备给别人的记录
//                            if (deviceBeen.getUuid() == null
//                                    || !deviceBeen.getUuid().equalsIgnoreCase(mInfoFromApp.getDevcieUuid())
//                                    || deviceBeen.getSharingUserId() == null
//                                    || !deviceBeen.getSharingUserId().equalsIgnoreCase(mInfoFromApp.getUserUuid())) {
//                                continue;
//                            }
//                            mArrayHistory.add(deviceBeen);
//                        }
//                        mAdapterHistory.notifyDataSetChanged();
//
//                        if (mArrayHistory.size() > 0)
//                            mTvHistory.setText(getString(R.string.share_history) + '(' + mArrayHistory.size() + ')');
//
//                        if (param.getCurrentPage() < param.getTotalPage())
//                            doGetHistory(false, param.getCurrentPage() + 1, param.getPageSize());
//                    }
//
//                    @Override
//                    public void onCancelled(GenericTask task) {
//
//                    }
//                })
//        );
//
//    }

    /**
     * @param sharedUserId
     * @param option       分享的操作，1：接受，2：拒绝,3:取消（shareUserType = 1时有效），4：删除
     */
    public void doDealHistory(String sharedUserId, int option) {
        addTask(
                new DealSharingTask().execute(1, sharedUserId, mInfoFromApp.getUserUuid(), option, mInfoFromApp, new TaskListener() {
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
                        } else {
                            doGetSharedUser(true, 1, 6, false);
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


}
