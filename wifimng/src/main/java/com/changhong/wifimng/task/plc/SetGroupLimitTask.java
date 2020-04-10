package com.changhong.wifimng.task.plc;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.BaseResponseBeen;
import com.changhong.wifimng.been.Group;
import com.changhong.wifimng.been.plc.DevControlRuleBeen;
import com.changhong.wifimng.been.plc.ParentControlInfoBeen;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 获取分组信息
 */
public class SetGroupLimitTask extends GenericTask {
    /**
     * @param gateway
     * @param groupList all group list
     * @param staList   all sta limit info.
     * @param listener
     * @return
     */
    public final SetGroupLimitTask execute(String gateway, List<Group> groupList, List<DevControlRuleBeen> staList, TaskListener<List<Group>> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
        params.put("groupList", groupList);
        params.put("staList", staList);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
        List<Group> groupList = params[0].get("groupList");
        List<DevControlRuleBeen> staList = params[0].get("staList");

        String url = "http://" + gateway + ":8081/rpc";

        // commit group info first
        if (groupList != null) {
            try {
                boolean has_new_add = false;
                for (int i = 0; i < groupList.size(); i++) {
                    if (groupList.get(i).getState() == null) {
                        groupDel(url, groupList.get(i));
                    } else if (groupList.get(i).getState()) {
                        groupAdd(url, groupList.get(i));
                        has_new_add = true;
                    } else {
                        groupUpdate(url, groupList.get(i));
                    }
                }

                if (has_new_add) {// if has new group, refresh for get group index;
                    List<Group> list = getStaGroupInfo(url);
                    publishProgress(list);
                }

            } catch (Exception e) {
                e.printStackTrace();
                setException(e);
                return TaskResult.IO_ERROR;
            }
        }

        // then commit control rule of sta
        if (staList != null) {
            for (int i = 0; i < staList.size(); i++) {
                try {
                    if (staList.get(i).getState() == null)
                        ruleDel(url, staList.get(i));
                    else if (staList.get(i).getState()) {
                        ruleAdd(url, staList.get(i));
                    } else {
                        ruleUpdate(url, staList.get(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setException(e);
                }
            }
        }

        return TaskResult.OK;
    }

    private void ruleAdd(String url, DevControlRuleBeen rule) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"parent_control_device_add\",\"param\":{\"src_type\":1" +
                ",\"mac\":\"" + rule.getMac() + "\"" +
                ",\"start_time\":\"" + rule.getStart_time() + "\"" +
                ",\"end_time\":\"" + rule.getEnd_time() + "\"" +
                ",\"repeat_day\":\"" + rule.getRepeat_day() +
                "\"}}";
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
        if (been.getErr_code() != 0) {
            throw new IOException(been.getMessage());
        }
    }

    private void ruleDel(String url, DevControlRuleBeen rule) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"parent_control_device_del\",\"param\":{\"src_type\":1" +
                ",\"mac\":\"" + rule.getMac() +
                "\"}}";
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
        if (been.getErr_code() != 0) {
            throw new IOException(been.getMessage());
        }
    }

    private void ruleUpdate(String url, DevControlRuleBeen rule) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"parent_control_device_time_update\",\"param\":{\"src_type\":1" +
                ",\"mac\":\"" + rule.getMac() + "\"" +
                ",\"start_time\":\"" + rule.getStart_time() + "\"" +
                ",\"end_time\":\"" + rule.getEnd_time() + "\"" +
                ",\"repeat_day\":\"" + rule.getRepeat_day() +
                "\"}}";
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
        if (been.getErr_code() != 0) {
            throw new IOException(been.getMessage());
        }
    }

    private void groupAdd(String url, Group rule) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"parent_control_list_name_add\",\"param\":{\"src_type\":1" +
                ",\"mac\":\"" + rule.getMac() + "\"" +
                ",\"list_name\":\"" + rule.getGroup_name() + "\"" +
                "}}";
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
        if (been.getErr_code() != 0) {
            throw new IOException(been.getMessage());
        }
    }

    private void groupDel(String url, Group rule) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"parent_control_list_name_del\",\"param\":{\"src_type\":1" +
                ",\"index\":" + rule.getIndex() +
                "}}";
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
        if (been.getErr_code() != 0) {
            throw new IOException(been.getMessage());
        }
    }

    private void groupUpdate(String url, Group rule) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"parent_control_list_name_update\",\"param\":{\"src_type\":1" +
                ",\"mac\":\"" + rule.getMac() + "\"" +
                ",\"list_name\":\"" + rule.getGroup_name() +
                "\"}}";
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        BaseResponseBeen been = new Gson().fromJson(response, BaseResponseBeen.class);
        if (been.getErr_code() != 0) {
            throw new IOException(been.getMessage());
        }
    }

    private List<Group> getStaGroupInfo(String url) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"parent_control_list_name_show\",\"param\":{\"src_type\":1}}";
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        ParentControlInfoBeen been = new Gson().fromJson(response, ParentControlInfoBeen.class);
        if (been.getErr_code() != 0) {
            throw new IOException(been.getMessage());
        } else {
            List<Group> list = been.getDev_info();
            return list;
        }
    }
}
