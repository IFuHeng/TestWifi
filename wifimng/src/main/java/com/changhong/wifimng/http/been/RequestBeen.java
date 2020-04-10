package com.changhong.wifimng.http.been;

import android.text.TextUtils;
import android.util.Base64;

import com.changhong.wifimng.BuildConfig;
import com.changhong.wifimng.http.Config;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class RequestBeen<T> implements Config {

    private String client = "ANDROID";
    private String version = "1.0.1";
    private final String key;
    /**
     * user	是	string	用户登录后的UUID
     */
    private final String user;
    private final T data;

    public RequestBeen(String user, T data) {
        this.user = user;
        this.data = data;
        if (BuildConfig.IS_RELEASE) {
            String millis = getTimeKey();
            key = getRandomString(2) + millis + getRandomString(2);
        } else {
            key = "";
        }
    }

    private String getTimeKey() {
        String millis = System.currentTimeMillis() + "";
        int length = millis.length();
        return millis.substring(3, length);
    }

    private String getRandomString(int length) {
        String base = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    private String encrypt(String data, String key) throws Exception {
        if (data == null) return null;
        if (TextUtils.isEmpty(key)) return null;
        try {
            DESKeySpec dks = new DESKeySpec(key.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_MODE);
            // key的长度不能够小于8位字节
            Key secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            IvParameterSpec iv = new IvParameterSpec(ivString.getBytes());
            AlgorithmParameterSpec paramSpec = iv;
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
            byte[] bytes = cipher.doFinal(data.getBytes(codeType));
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "{" +
                "\"client\",\"" + client + '\"' +
                ", \"version\",\"" + version + '\"' +
                ", \"key\",\"" + key + '\"' +
                ", \"user\",\"" + user + '\"' +
                ", \"data\",\"" + data + "\"" +
                '}';
    }

    public String toJsonString() {
        Gson gson = new Gson();
        if (BuildConfig.IS_RELEASE) {
            JSONObject jsonObject = new JSONObject();

            try {
                String millis = getTimeKey();
                String temp = gson.toJson(data);
                temp = encrypt(temp, millis.substring(2, 10));
                jsonObject.put("client", client);
                jsonObject.put("version", version);
                jsonObject.put("key", key);
                jsonObject.put("user", user);
                jsonObject.put("data", temp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonObject.toString();
        } else {
            return gson.toJson(this);
        }
    }
}
