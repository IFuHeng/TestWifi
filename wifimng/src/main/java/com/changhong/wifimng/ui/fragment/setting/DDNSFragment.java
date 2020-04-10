package com.changhong.wifimng.ui.fragment.setting;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.DDNSBeen;
import com.changhong.wifimng.been.DeviceType;
import com.changhong.wifimng.been.plc.PLCDDNSBeen;
import com.changhong.wifimng.been.sys.ServiceRequireAllBeen;
import com.changhong.wifimng.been.sys.ServiceResponseAllBeen;
import com.changhong.wifimng.preference.KeyConfig;
import com.changhong.wifimng.preference.Preferences;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.router.GetDDNSTask;
import com.changhong.wifimng.task.router.SetDDNSTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskResult;
import com.changhong.wifimng.task.plc.GetPLCDdnsTask;
import com.changhong.wifimng.task.plc.SetPLCDdnsTask;
import com.changhong.wifimng.ui.activity.BaseWifiActivtiy;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.view.HideOrShowWatcher;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class DDNSFragment extends BaseFragment<Boolean> implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private ViewSwitcher mViewSwitcher;

    private View mBtnAdd;
    private TableLayout mTableServiceList;

    private View mBtnSave;
    private EditText mEtDomainName;
    private EditText mEtPassword;
    private EditText mEtAccount;
    private CheckBox mCbHidePsw;
    private View mBtnClear;
    private View mBtnClearDomain;
    private Spinner mSpinner;

    private String mCurrentDomainName;
    private static final String DEFAULT_DOMAIN_NAME = "host.dyndns.org";

    private String mCurrentDeviceType;
    private DeviceType mDeviceType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mCurrentDeviceType = getArguments().getString(KeyConfig.KEY_DEVICE_TYPE);
        }
        if (mCurrentDeviceType == null)
            mCurrentDeviceType = Preferences.getInstance(mActivity).getString(KeyConfig.KEY_DEVICE_TYPE);
        mDeviceType = DeviceType.getDeviceTypeFromName(mCurrentDeviceType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ddns, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        view.findViewById(R.id.btn_back).setOnClickListener(this);

        mViewSwitcher = view.findViewById(R.id.viewSwitcher01);
        /**page 1*/
        mBtnAdd = view.findViewById(R.id.btn_add);
        mBtnAdd.setOnClickListener(this);

        mTableServiceList = view.findViewById(R.id.panel_online_device_table);

        /**page 2*/
        mBtnSave = view.findViewById(R.id.btn_save);
        mBtnSave.setOnClickListener(this);

        mSpinner = view.findViewById(R.id.spinner_ddns);
        mEtDomainName = view.findViewById(R.id.et_domain_name);
        mEtPassword = view.findViewById(R.id.et_password);
        mEtAccount = view.findViewById(R.id.et_account);

        mCbHidePsw = view.findViewById(R.id.checkbox_hide_psw);
        mBtnClear = view.findViewById(R.id.btn_clear);
        mBtnClearDomain = view.findViewById(R.id.btn_clear_domain);

        mEtDomainName.addTextChangedListener(new HideOrShowWatcher(mBtnClearDomain));
        mEtPassword.addTextChangedListener(new HideOrShowWatcher(mCbHidePsw));
        mEtAccount.addTextChangedListener(new HideOrShowWatcher(mBtnClear));

        mCbHidePsw.setOnCheckedChangeListener(this);
        mBtnClear.setOnClickListener(this);
        mBtnClearDomain.setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden())
            doLoad();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) doLoad();
    }

    private void doLoad() {
        if (mDeviceType == DeviceType.PLC)
            doLoadPlcDDNS(true);
        else
            doLoadDDNS(true);
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
        if (v.getId() == R.id.btn_save) {
            if (!checkPage2()) {
                showToast(R.string.notice_input_not_correct);
                return;
            }

            if (DeviceType.PLC == mDeviceType) {
                PLCDDNSBeen info = new PLCDDNSBeen();
                info.setEnable(1);
                info.setDomain_name(mEtDomainName.getText().toString());
                info.setUser_name(mEtAccount.getText().toString());
                info.setUser_password(mEtPassword.getText().toString());
                info.setType(mSpinner.getSelectedItemPosition());
                doSetPlcDDNS(info);
            } else {
                ServiceRequireAllBeen requireAllBeen = new ServiceRequireAllBeen();
                requireAllBeen.setEnabled(1);
                requireAllBeen.setDomain_name(mEtDomainName.getText().toString());
                requireAllBeen.setUser_name(mEtAccount.getText().toString());
                requireAllBeen.setUser_password(mEtPassword.getText().toString());
                requireAllBeen.setType(mSpinner.getSelectedItemPosition());
                doSetDDNS(requireAllBeen, true);
            }

        }
        //添加
        else if (v.getId() == R.id.btn_add) {
            if (mTableServiceList.getChildCount() > 1) {
                showAlert(getString(R.string.notice_ddns_support_1_item_only), getString(R.string.confirm), null);
                return;
            }
            refreshViewPage2(null);
        }
        //修改
        else if (v.getId() == R.id.btn_edit) {
            refreshViewPage2((DDNSBeen) v.getTag());
        }
        //删除
        else if (v.getId() == R.id.btn_delete) {
            final DDNSBeen been = (DDNSBeen) v.getTag();
            showAlert(_getString(R.string.notice_delete_ddns_service), _getString(R.string.delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ServiceRequireAllBeen requireAllBeen = new ServiceRequireAllBeen();
                    requireAllBeen.setEnabled(1);
                    requireAllBeen.setDomain_name(mCurrentDomainName);
                    requireAllBeen.setUser_name("");
                    requireAllBeen.setUser_password("");
                    requireAllBeen.setType(been.getType());
                    doSetDDNS(requireAllBeen, false);
                }
            }, _getString(R.string.cancel), null, true);
        }
        // 清除文字
        else if (v.getId() == R.id.btn_clear) {
            mEtAccount.setText(null);
        }
        // 清除服务器名字
        else if (v.getId() == R.id.btn_clear_domain) {
            mEtDomainName.setText(null);
        }

        //返回
        else if (v.getId() == R.id.btn_back) {
            if (mViewSwitcher.getDisplayedChild() == 0)
                onFragmentLifeListener.onChanged(null);
            else
                mViewSwitcher.showNext();
        }

    }

    /**
     * @return 检查修改或新增输入是否正确
     */
    private boolean checkPage2() {
        if (mEtAccount.getText().length() == 0
                || mEtDomainName.getText().length() == 0
                || mEtPassword.getText().length() == 0
        )
            return false;
        return true;
    }

    private void refreshViewPage1(ArrayList<DDNSBeen> arrDDNSItem) {
        while (mTableServiceList.getChildCount() > 1) {
            mTableServiceList.removeViewAt(1);
        }

        if (arrDDNSItem == null || arrDDNSItem.isEmpty())
            return;

        LayoutInflater inflater = LayoutInflater.from(mActivity);
        for (DDNSBeen ddnsBeen : arrDDNSItem) {
            View view = inflater.inflate(R.layout.item_ddns, null, false);
            View btnEdit = view.findViewById(R.id.btn_edit);
            View btnDelete = view.findViewById(R.id.btn_delete);
            Switch scEnable = view.findViewById(R.id.switch_enable);
            TextView textView = view.findViewById(R.id.text1);
            textView.setText(ddnsBeen.getDomain_name());
            btnDelete.setOnClickListener(this);
            btnDelete.setTag(ddnsBeen);
            btnEdit.setOnClickListener(this);
            btnEdit.setTag(ddnsBeen);
            scEnable.setOnCheckedChangeListener(this);
            scEnable.setChecked(ddnsBeen.isEnable());
            scEnable.setTag(ddnsBeen);
            mTableServiceList.addView(view, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    private void refreshViewPage2(DDNSBeen been) {
        if (been == null) {
            mSpinner.setSelection(0);
            mEtAccount.setText(null);
            mEtDomainName.setText(mCurrentDomainName);
            mEtPassword.setText(null);
        } else {
            mSpinner.setSelection(been.getType());
            mEtAccount.setText(been.getUser_name());
            mEtDomainName.setText(been.getDomain_name());
            mEtPassword.setText(been.getUser_password());
        }
        if (mViewSwitcher.getDisplayedChild() != 1)
            mViewSwitcher.showNext();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.switch_enable) {
            if (buttonView.getTag() != null && buttonView.getTag() instanceof DDNSBeen) {
                DDNSBeen been = (DDNSBeen) buttonView.getTag();
                ServiceRequireAllBeen requireAllBeen = new ServiceRequireAllBeen();
                requireAllBeen.setEnabled(isChecked ? 1 : 0);
                requireAllBeen.setDomain_name(been.getDomain_name());
                requireAllBeen.setUser_name(been.getUser_name());
                requireAllBeen.setUser_password(been.getUser_password());
                requireAllBeen.setType(been.getType());
                doSetDDNS(requireAllBeen, false);
            }
        }
        //显示或隐藏密码
        else if (buttonView.getId() == R.id.checkbox_hide_psw) {
            isShowPassword(mEtPassword, !isChecked);
        }
    }

    private void doLoadDDNS(final boolean isFirst) {
        addTask(
                new GetDDNSTask().execute(getGateway(), getCookie(), new TaskListener<ServiceResponseAllBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(getString(R.string.downloading), isFirst, new DialogInterface.OnCancelListener() {
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
                    public void onProgressUpdate(GenericTask task, ServiceResponseAllBeen param) {
                        if (param == null) {
                            refreshViewPage1(null);
                            return;
                        }
                        if (mCurrentDomainName == null)
                            mCurrentDomainName = param.getDomain_name();
                        //TODO 目前仅支持一条
                        if (isEmptyOrNull(param.getUser_name())
                                || isEmptyOrNull(param.getDomain_name())
                                || isEmptyOrNull(param.getUser_password())) {
                            refreshViewPage1(null);
                            return;
                        }
                        ArrayList<DDNSBeen> arrDDNSItem = new ArrayList<>();
                        DDNSBeen item = new DDNSBeen();
                        item.setEnable(param.getEnabled() == 1);
                        item.setDomain_name(param.getDomain_name());
                        item.setUser_name(param.getUser_name());
                        item.setUser_password(param.getUser_password());
                        item.setType(param.getType());
                        arrDDNSItem.add(item);

                        refreshViewPage1(arrDDNSItem);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doLoadPlcDDNS(final boolean isFirst) {
        addTask(
                new GetPLCDdnsTask().execute(getGateway(), new TaskListener<PLCDDNSBeen>() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public void onPreExecute(final GenericTask task) {
                        showProgressDialog(getString(R.string.downloading), isFirst, new DialogInterface.OnCancelListener() {
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
                    public void onProgressUpdate(GenericTask task, PLCDDNSBeen param) {
                        if (param == null) {
                            refreshViewPage1(null);
                            return;
                        }
                        if (mCurrentDomainName == null)
                            mCurrentDomainName = param.getDomain_name();
                        if (isEmptyOrNull(param.getUser_name())
                                || isEmptyOrNull(param.getDomain_name())
                                || isEmptyOrNull(param.getUser_password())) {
                            refreshViewPage1(null);
                            return;
                        }
                        ArrayList<DDNSBeen> arrDDNSItem = new ArrayList<>();
                        DDNSBeen item = new DDNSBeen();
                        item.setEnable(param.getEnable() == 1);
                        item.setDomain_name(param.getDomain_name());
                        item.setUser_name(param.getUser_name());
                        item.setUser_password(param.getUser_password());
                        item.setType(param.getType());
                        arrDDNSItem.add(item);

                        refreshViewPage1(arrDDNSItem);
                    }

                    @Override
                    public void onCancelled(GenericTask task) {
                        hideProgressDialog();
                    }
                })
        );
    }

    private void doSetDDNS(ServiceRequireAllBeen been, boolean isCreate) {
        addTask(
                new SetDDNSTask().execute(getGateway(), isCreate, been, getCookie(), new TaskListener() {
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
                            if (mViewSwitcher.getDisplayedChild() == 1)
                                mViewSwitcher.showNext();
                            doLoadDDNS(false);
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

    private void doSetPlcDDNS(PLCDDNSBeen been) {
        addTask(
                new SetPLCDdnsTask().execute(getGateway(), been, new TaskListener() {
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
                            if (mViewSwitcher.getDisplayedChild() == 1)
                                mViewSwitcher.showNext();
                            doLoadPlcDDNS(false);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mViewSwitcher.getDisplayedChild() == 0)
                onFragmentLifeListener.onChanged(null);
            else
                mViewSwitcher.showNext();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isEmptyOrNull(String string) {
        return string == null || string.isEmpty() || string.equals("null");
    }
}
