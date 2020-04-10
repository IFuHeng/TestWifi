package com.changhong.wifimng.http.task;

import android.text.TextUtils;
import android.util.Base64;

import com.changhong.wifimng.BuildConfig;
import com.changhong.wifimng.http.Config;
import com.changhong.wifimng.task.GenericTask;
import com.changhong.wifimng.task.TaskParams;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public abstract class BaseTask extends GenericTask implements Config {

    protected void executeOnExecutor(TaskParams params) {
        super.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
    }

    protected <T> T getConnectResponseBeen(String response, Class<T> classOfT) throws Exception {
        JSONObject jsobj = new JSONObject(response);
        int code = jsobj.getInt("code");
        if (code == Config.CodeSuccess) {//|| code == Config.NoData) {

            String key = jsobj.getString("key");
            if (!jsobj.has("data"))
                return null;
            String data = jsobj.getString("data");

            if (BuildConfig.IS_RELEASE) {
                data = decrypt(data, key);
            }
            return new Gson().fromJson(data, classOfT);
        } else {
            throw new Exception(jsobj.getString("msg"));
        }
    }

    protected void checkConnectResponse(String response) throws Exception {
        JSONObject jsobj = new JSONObject(response);
        int code = jsobj.getInt("code");
        if (code == Config.CodeSuccess) {//|| code == Config.NoData) {

        } else {
            throw new Exception(jsobj.getString("msg"));
        }
    }

    protected String getConnectResponseBeen(String response) throws Exception {
        JSONObject jsobj = new JSONObject(response);
        int code = jsobj.getInt("code");
        if (code == Config.CodeSuccess) {//|| code == Config.NoData) {

            String key = jsobj.getString("key");
            if (!jsobj.has("data"))
                return null;
            String data = jsobj.getString("data");

            if (BuildConfig.IS_RELEASE) {
                data = decrypt(data, key);
            }
            return data;
        } else {
            throw new Exception(jsobj.getString("msg"));
        }
    }

    protected String getUrl(String method) {
        if (BuildConfig.IS_RELEASE)
            return HOST + File.pathSeparatorChar + PORT + File.separatorChar + method;
        else
            return HOST_TEST + File.pathSeparatorChar + PORT + File.separatorChar + method;
    }

    /**
     * DES解密
     *
     * @return 解密出的字符串
     */
    public static String decrypt(String data, String key) {
        if (data == null) return null;
        if (TextUtils.isEmpty(key)) return null;
        if (key.length() < 10) return null;
        key = key.substring(key.length() - 10, key.length() - 2);
        try {
            DESKeySpec dks = new DESKeySpec(key.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_MODE);
            // key的长度不能够小于8位字节
            Key secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            IvParameterSpec iv = new IvParameterSpec(ivString.getBytes());
            AlgorithmParameterSpec paramSpec = iv;
            cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
            return new String(cipher.doFinal(Base64.decode(data, Base64.DEFAULT)), codeType);
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }
}
