package com.changhong.wifimng.ui.fragment.setting;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ViewFlipper;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.http.been.Group;
import com.changhong.wifimng.http.been.GroupListBeen;
import com.changhong.wifimng.http.task.DeviceUpdateAndGroupTask;
import com.changhong.wifimng.http.task.GetGroupListTask;
import com.changhong.wifimng.http.task.NewCreateGroupTask;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.fragment.EnumPage;
import com.changhong.wifimng.ui.fragment.InputDialog;
import com.changhong.wifimng.ui.fragment.OnFragmentLifeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 定制化分组和名称
 * A simple {@link Fragment} subclass.
 */
public class CustomGroupAndNameFragment extends BaseFragment<BaseBeen<EnumPage, String>> implements View.OnClickListener, AdapterView.OnItemSelectedListener {


    private ViewFlipper mViewFlipper;

    private String mCurrentDeviceType;
    private DeviceType mDeviceType;

    //PAGE 1
    private Spinner mSpinner;
    private EditText mEtDeviceName;
    private List<CharSequence> mChoiceGroup;
    private List<Group> mArrayGroup;
    private BaseAdapter mSpinnerAdapter;

    @Override
    public void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mInfoFromApp == null)
            mCurrentDeviceType = Preferences.getInstance(mActivity).getString(KeyConfig.KEY_DEVICE_TYPE);
        else
            mCurrentDeviceType = mInfoFromApp.getDeviceType();

        mDeviceType = DeviceType.getDeviceTypeFromName(mCurrentDeviceType);

        mArrayGroup = new ArrayList<>();
        mChoiceGroup = new ArrayList<>();
        {
            SpannableString spannableString = new SpannableString(getString(R.string.notice_choose_group));
            spannableString.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            mChoiceGroup.add(spannableString);
        }
        {
            SpannableString spannableString = new SpannableString(getString(R.string.new_group));
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            mChoiceGroup.add(spannableString);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_group_and_name, container, false);
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {

        view.findViewById(R.id.btn_back).setOnClickListener(this);

        mViewFlipper = view.findViewById(R.id.viewFliper01);

        // page 1
        mEtDeviceName = view.findViewById(R.id.et_device_name);

        mEtDeviceName.setHint(getString(R.string.factory_name) + getString(DeviceType.getDeviceTypeFromName(mInfoFromApp.getDeviceType()).getNameResId()));

        mSpinner = view.findViewById(R.id.spinner_location);

        mSpinnerAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1, mChoiceGroup);
        mSpinner.setAdapter(mSpinnerAdapter);

        mSpinner.setOnItemSelectedListener(this);
        view.findViewById(R.id.btn_next).setOnClickListener(this);

        // page 2
        view.findViewById(R.id.btn_sharing).setOnClickListener(this);
        view.findViewById(R.id.btn_start_use).setOnClickListener(this);

        // page 3
        view.findViewById(R.id.btn_retry).setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);

        doLoadGroup(true, 1, 10);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setEditText(EditText et, String value) {
        if (value != null) {
            et.setText(value);
            et.setHint(value);
        }
    }

    @Override
    public void onClick(View v) {
        //保存
        if (v.getId() == R.id.btn_next) {
            String name = mEtDeviceName.getText().toString().trim();
            if (name.length() == 0) {
                if (mEtDeviceName.length() > 0) {
                    showToast(R.string.notice_input_not_correct);
                    mEtDeviceName.requestFocus();
                    return;
                }
                name = mEtDeviceName.getHint().toString();
            }
            int index = mSpinner.getSelectedItemPosition();
            if (index == 0 || index == mChoiceGroup.size() - 1) {
                showToast(R.string.notice_choose_group);
                return;
            }
            Group group = mArrayGroup.get(index - 1);
            doSaveGroupAndName(name, group.getUuid());
        }

        //返回
        else if (v.getId() == R.id.btn_back) {
            if (mViewFlipper.getDisplayedChild() == 2)
                mViewFlipper.setDisplayedChild(0);
            else
                onFragmentLifeListener.onChanged(null);
        } else if (v.getId() == R.id.btn_retry) {
            mViewFlipper.setDisplayedChild(0);
            String name = mEtDeviceName.getText().toString().trim();
            if (name.length() == 0) {
                name = mEtDeviceName.getHint().toString();
            }
            Group group = mArrayGroup.get(mSpinner.getSelectedItemPosition() - 1);
            doSaveGroupAndName(name, group.getUuid());
        } else if (v.getId() == R.id.btn_sharing) {
            onFragmentLifeListener.onChanged(new BaseBeen(EnumPage.DEVICE_SHARE,getDeviceName()));
        } else if (v.getId() == R.id.btn_start_use) {
            onFragmentLifeListener.onChanged(new BaseBeen(EnumPage.WIFI_SETTING, getDeviceName()));
        }

    }

    private String getDeviceName() {
        String name = mEtDeviceName.getText().toString().trim();
        if (name.length() == 0) {
            name = mEtDeviceName.getHint().toString();
        }
        return name;
    }

    private void doLoadGroup(final boolean isFirst, int currentPage, int pageSize) {
        if (isFirst) {
            mArrayGroup.clear();
            while (mChoiceGroup.size() > 2) {
                mChoiceGroup.remove(1);
            }
        }
        addTask(
                new GetGroupListTask().execute(currentPage, pageSize, mInfoFromApp, new TaskListener<GroupListBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        if (isFirst)
                            showProgressDialog(getString(R.string.downloading_group), isFirst, new DialogInterface.OnCancelListener() {
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
                            onFragmentLifeListener.onChanged(null);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, GroupListBeen param) {
                        if (param != null) {
                            mArrayGroup.addAll(param.getList());

                            //判断是否读取完，未读取完继续下一页读取
                            if (param.getCurrentPage() < param.getTotalPage()) {
                                doLoadGroup(false, param.getCurrentPage() + 1, param.getPageSize());
                            } else {
                                for (Group group : mArrayGroup) {
                                    mChoiceGroup.add(mChoiceGroup.size() - 1, group.getGroupName());
                                }
                                mSpinnerAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
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
                        showProgressDialog(getString(R.string.commiting), false, null);
                    }

                    @Override
                    public void onPostExecute(GenericTask task, TaskResult result) {
                        hideProgressDialog();
                        if (result != TaskResult.OK) {
                            showTaskError(task, R.string.interaction_failed);

                            mSpinner.setSelection(-1);
                        } else {
                            showToast(R.string.binding_success);
                        }
                    }

                    @Override
                    public void onProgressUpdate(GenericTask task, Group param) {
                        //TODO 重新加载最后一个group信息
                        mArrayGroup.add(param);
                        mChoiceGroup.add(mChoiceGroup.size() - 1, param.getGroupName());
                        mSpinnerAdapter.notifyDataSetChanged();
                        mSpinner.setSelection(mChoiceGroup.size() - 2);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doSaveGroupAndName(String name, String groupId) {
        addTask(
                new DeviceUpdateAndGroupTask().execute(name, groupId, mInfoFromApp, new TaskListener() {
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
                            mViewFlipper.setDisplayedChild(2);
                        } else {
                            showToast(R.string.commit_completed);
                            mViewFlipper.setDisplayedChild(1);
                            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(KeyConfig.ACTION_BIND_DEVICE));
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

    private boolean isEmptyOrNull(String string) {
        return string == null || string.isEmpty() || string.equals("null");
    }


    private void showCreateGroupDialog() {
        InputDialog dialog = new InputDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean(InputDialog.EXTRA_PASSWORD, false);
        bundle.putBoolean(InputDialog.KEY_CANCEL_ABLE, false);
        bundle.putString(Intent.EXTRA_TEXT, getString(R.string.notice_new_group_name));
        dialog.setArguments(bundle);
        dialog.setFragmentListener(new OnFragmentLifeListener<String>() {

            @Override
            public void onChanged(String o) {
                if (!TextUtils.isEmpty(o)) {
                    doCreateGroup(o);
                } else {
                    mSpinner.setSelection(0);//取消时，将spinner设置为未选择状态
                }
            }

        });
        dialog.show(getFragmentManager(), "group_create");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == mChoiceGroup.size() - 1) {//添加分组
            showCreateGroupDialog();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mViewFlipper.getDisplayedChild() == 2)
                mViewFlipper.setDisplayedChild(0);
            else
                onFragmentLifeListener.onChanged(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
