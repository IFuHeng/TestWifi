package com.changhong.wifimng.uttils;

import android.util.Base64;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * User: Jack Wang
 * Date: 19-4-29
 * Time: 下午1:56
 */
public class CHAndroidDesUtils {

    private static final String ALGORITHM_MODE = "DES";
    private static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";
    private static final String ivString = "12345678";
    private static final String codeType = "UTF-8";

//    public static void main(String[] args) throws Exception {
//        JSONObject o = new JSONObject();
//        o.put("p", "12345678");
//        o.put("u", "pf_admin");
//
//        String data = encrypt(o.toString(), "12345678");
//        System.out.println(data);
//        System.out.println(decrypt(data, "12345678"));
//    }

    /*
        比如你上传请求数据的时候
        String millis = CHAndroidDesUtils.getTimeKey();
        CHAndroidDesUtils.getRandomString(2) + millis + CHAndroidDesUtils.getRandomString(2);上传的key值
        key值你自己在上传的时候生成
        jStringer.key(KeyParams).value(CHAndroidDesUtils.encrypt(paramsJson.toString(), millis.substring(2, 10)));
        然后再把data这个字段的内容加密
        恩
        UUID 和 token 我给你
        client 传 ANDROID
        version 我给你
        然后拿到返回的Json 后 ， 你要用他的key 再对data这个字段的内容进行解密
    */

    public static String encrypt(String data, String key) throws Exception {
        if (data == null) return null;
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

    /**
     * DES解密
     */
    public static String decrypt(String data, String key) throws Exception {
        if (data == null) return null;
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

    public static String getTimeKey() {
        String millis = System.currentTimeMillis() + "";
        int length = millis.length();
        return millis.substring(3, length);
    }

    public static String getRandomString(int length) {
        String base = "0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}
