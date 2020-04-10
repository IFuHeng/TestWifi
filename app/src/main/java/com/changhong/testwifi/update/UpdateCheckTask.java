package com.changhong.testwifi.update;

import android.os.AsyncTask;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.changhong.wifimng.net.OkHttpConnector;

import java.io.IOException;
import java.util.List;

public class UpdateCheckTask extends AsyncTask<Void, String, UpdateCheckTask.UpdateResponse> {


    private final String UPDATE_URL = "http://api.fir.im/apps/latest/5da132f123389f49329e9caf?api_token=447a57032c7e5d967138b407d6b53b5c";
    private final OkHttpConnector mHttpConnector;
    private OnUpdateListener onUpdateListener;

    public UpdateCheckTask(@NonNull OnUpdateListener onUpdateListener) {
        mHttpConnector = new OkHttpConnector();

        this.onUpdateListener = onUpdateListener;
    }

    @Override
    protected UpdateResponse doInBackground(Void... voids) {
        UpdateResponse info = null;
        try {
            info = getUpdateInfo();
        } catch (Exception e) {
            e.printStackTrace();
            publishProgress(e.getLocalizedMessage());
        }

        return info;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (onUpdateListener != null) {
            onUpdateListener.onError(values[0]);
        }
    }

    @Override
    protected void onPostExecute(UpdateResponse updateResponse) {
        if (onUpdateListener == null) {
            return;
        }

        if (updateResponse == null) {
            onUpdateListener.onError("Cannot get update info,plesase try again.");
            return;
        }

        if (updateResponse.code != 0) {
            if (updateResponse.errors.exception != null && !updateResponse.errors.exception.isEmpty())
                onUpdateListener.onError(updateResponse.errors.exception.get(0));
            else
                onUpdateListener.onError("unknown error.");

            return;
        }

        onUpdateListener.onReceiveUpdateInfo(updateResponse);

    }

    private synchronized UpdateResponse getUpdateInfo() throws IOException {

        String response = mHttpConnector.get(UPDATE_URL, null);
        if (!TextUtils.isEmpty(response))
            return new Gson().fromJson(response, UpdateResponse.class);

        return null;
    }


    public static class UpdateResponse {
        private String name;//	String	应用名称
        private String version;//	String	版本
        private String changelog;//	String	更新日志
        private String versionShort;//	String	版本编号(兼容旧版字段)
        private String build;//	String	编译号
        private String installUrl;//	String	安装地址（兼容旧版字段）
        private String install_url;//	String	安装地址(新增字段)
        private String update_url;//	String	更新地址(新增字段)
        private Binary binary;//	Object	更新文件的对象，仅有大小字段fsize
        private long updated_at;


        private int code;
        private Errors errors;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getChangelog() {
            return changelog;
        }

        public void setChangelog(String changelog) {
            this.changelog = changelog;
        }

        public String getVersionShort() {
            return versionShort;
        }

        public void setVersionShort(String versionShort) {
            this.versionShort = versionShort;
        }

        public String getBuild() {
            return build;
        }

        public void setBuild(String build) {
            this.build = build;
        }

        public String getInstallUrl() {
            return installUrl;
        }

        public void setInstallUrl(String installUrl) {
            this.installUrl = installUrl;
        }

        public String getInstall_url() {
            return install_url;
        }

        public void setInstall_url(String install_url) {
            this.install_url = install_url;
        }

        public String getUpdate_url() {
            return update_url;
        }

        public void setUpdate_url(String update_url) {
            this.update_url = update_url;
        }

        public Binary getBinary() {
            return binary;
        }

        public void setBinary(Binary binary) {
            this.binary = binary;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public Errors getErrors() {
            return errors;
        }

        public void setErrors(Errors errors) {
            this.errors = errors;
        }


        @Override
        public String toString() {
            return "UpdateResponse{" +
                    "name='" + name + '\'' +
                    ", version='" + version + '\'' +
                    ", changelog='" + changelog + '\'' +
                    ", versionShort='" + versionShort + '\'' +
                    ", build='" + build + '\'' +
                    ", installUrl='" + installUrl + '\'' +
                    ", install_url='" + install_url + '\'' +
                    ", update_url='" + update_url + '\'' +
                    ", binary=" + binary +
                    ", updated_at=" + updated_at +
                    ", code=" + code +
                    ", errors=" + errors +
                    '}';
        }

        public long getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(long updated_at) {
            this.updated_at = updated_at;
        }
    }

    public static class Errors {
        private List<String> exception;

        public List<String> getException() {
            return exception;
        }

        public void setException(List<String> exception) {
            this.exception = exception;
        }

        @Override
        public String toString() {
            return "Errors{" +
                    "exception=" + exception +
                    '}';
        }
    }

    public static class Binary {
        public long fsize;

        public long getFsize() {
            return fsize;
        }

        public void setFsize(long fsize) {
            this.fsize = fsize;
        }

        @Override
        public String toString() {
            return "Binary{" +
                    "fsize=" + fsize +
                    '}';
        }
    }

    public interface OnUpdateListener {
        void onReceiveUpdateInfo(UpdateResponse response);

        void onError(String error);
    }
}
