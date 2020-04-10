package com.changhong.wifimng.task.plc;

import android.accounts.AuthenticatorException;

import com.changhong.wifimng.been.BaseBeen;
import com.changhong.wifimng.been.Group;
import com.changhong.wifimng.been.RequestBeen;
import com.changhong.wifimng.been.StaInfo;
import com.changhong.wifimng.been.mesh.ListInfo;
import com.changhong.wifimng.been.mesh.MeshRequireAllBeen;
import com.changhong.wifimng.been.mesh.MeshResponseAllBeen;
import com.changhong.wifimng.been.plc.DevControlInfoResponse;
import com.changhong.wifimng.been.plc.DevControlRuleBeen;
import com.changhong.wifimng.been.plc.ParentControlInfoBeen;
import com.changhong.wifimng.been.wan.Level2Been;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.net.HttpRequestMethod;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskListener;
import com.changhong.wifimng.task.TaskParams;
import com.changhong.wifimng.task.TaskResult;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 获取分组信息
 */
public class GetGroupLimitTask extends GenericTask {
    public final GetGroupLimitTask execute(String gateway, TaskListener<BaseBeen<List<Group>, List<DevControlRuleBeen>>> listener) {
        TaskParams params = new TaskParams();
        params.put("gateway", gateway);
//        params.put("isMesh", isMesh);
        setListener(listener);
        executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String gateway = params[0].getString("gateway");
//        Boolean isMesh = params[0].get("isMesh");
        String url = "http://" + gateway + ":8081/rpc";
        try {
            List<Group> groupInfo = getStaGroupInfo(url);
            List<DevControlRuleBeen> devRuleLimitInfo = getTimeLimitShow(url);
            publishProgress(new BaseBeen(groupInfo, devRuleLimitInfo));
        } catch (Exception e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        }
        return TaskResult.OK;
    }

    private List<Group> getStaGroupInfo(String url) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"parent_control_list_name_show\",\"param\":{\"src_type\":1}}";
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        ParentControlInfoBeen been = new Gson().fromJson(response, ParentControlInfoBeen.class);
        if (been.getErr_code() != 0) {
            throw new IOException(been.getMessage());
        } else {
            List<Group> list = been.getDev_info();
            // 排个序
//            Collections.sort(list, new Comparator<Group>() {
//                @Override
//                public int compare(Group t1, Group t2) {
//                    int result = t1.getGroup_name().compareToIgnoreCase(t2.getGroup_name());
//                    if (result != 0)
//                        return result;
//
//                    return t1.getMac().compareToIgnoreCase(t2.getMac());
//                }
//            });
            return list;
        }
    }

    private List<DevControlRuleBeen> getTimeLimitShow(String url) throws AuthenticatorException, NoSuchAlgorithmException, KeyManagementException, IOException {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"parent_control_dev_list_show\",\"param\":{\"src_type\":1}}";
        String response = DefaultHttpUtils.httpPostWithText(url, body);
        DevControlInfoResponse responseAllBeen = new Gson().fromJson(response, DevControlInfoResponse.class);
        if (responseAllBeen.getErr_code() != 0) {
            throw new IOException(responseAllBeen.getMessage());
        } else {
            List<DevControlRuleBeen> list = responseAllBeen.getDev_info();
            // 排个序
//            Collections.sort(list, new Comparator<Level2Been>() {
//                @Override
//                public int compare(Level2Been t1, Level2Been t2) {
//                    return t1.getMac().compareToIgnoreCase(t2.getMac());
//                }
//            });
            return list;
        }
    }

}
