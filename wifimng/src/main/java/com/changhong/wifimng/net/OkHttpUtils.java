package com.changhong.wifimng.net;

import android.accounts.AuthenticatorException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtils {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int CONNECT_TIMEOUT = 5;
    private static final int READ_TIMEOUT = 5;
    private static final int WRITE_TIMEOUT = 5;
    // private static final String TAG = "ConnectTask";
    private static OkHttpClient mHttpClient = new OkHttpClient.Builder().readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS).connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS).build();

    public static void post(String url, String requestBody) {
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), requestBody);
        Request request = new Request.Builder().url(url).addHeader("content-type", "application/json;charset:utf-8").post(body).build();
        Log.i("OkHttpUtils", "=====~request: " + requestBody);
        try {
            Response response = mHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                // Log.i(TAG, "=====~ getCheckStatus----- fail");
                return;
            }
            String result = response.body().string();
            Log.i("OkHttpUtils", "=====~response: " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String connect(String url, JSONObject jsonObject) {
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder().url(url).addHeader("content-type", "application/json;charset:utf-8").post(body).build();
        // Log.i(TAG, "=====~request: " + jsonStr);
        try {
            Response response = mHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                // Log.i(TAG, "=====~ getCheckStatus----- fail");
                return null;
            }
            String result = response.body().string();
            // Log.i(TAG, "=====~response: " + result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static boolean send(String url, String jsonStr) {
        RequestBody body = RequestBody.create(JSON, jsonStr);
        Request request = new Request.Builder().url(url).addHeader("content-type", "application/json;charset:utf-8").post(body).build();
        try {
            Response response = mHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                return false;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String httpPostWithJson(String registerUrl, String json, String ssid)
            throws IOException, AuthenticatorException {
        String strResult = httpPost(registerUrl, json, ssid);

        if (strResult != null && strResult.contains("\"result\""))
            try {
                JSONObject jsobj = new JSONObject(strResult);
                strResult = jsobj.getString("result");
                return strResult;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return strResult;
    }

    public static String httpPost(String registerUrl, String json, String ssid)
            throws IOException, AuthenticatorException {
        Log.d("OkHttpUtils", "====~ url = " + registerUrl + ", body = " + json);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        RequestBody body = RequestBody.create(JSON, json);
        Request.Builder builder = new Request.Builder()
                .url(registerUrl)
                .method("POST", body)
                .addHeader("Content-Type", "application/json");
        if (ssid != null && ssid.length() > 0)
            builder.addHeader("Cookie", ssid);
        Request request = builder.build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {//成功
            String line = response.body().string();
            Log.d("OkHttpUtils", "====~ response = " + line);
            return line;
        } else if (response.code() == 401) {
            throw new AuthenticatorException("unauthorized");
        } else {
            Log.d("OkHttpUtils", "====~ postConnection.getResponseCode() = " + response.code());
            Log.d("OkHttpUtils", "====~ postConnection.getResponseMessage() = " + response.message());
            throw new IOException(response.message());
        }
    }

    public static String httpPost(String registerUrl, Map<String, String> map) throws IOException {
        Log.d("JavaHttpUtils", "====~ url = " + registerUrl + ", map = " + map);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            builder.addFormDataPart(next.getKey(), next.getValue());
        }
        MultipartBody body = builder.build();

        Request request = new Request.Builder()
                .url(registerUrl)
                .method("POST", body)
                .addHeader("Content-Type", "text/plain")
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException(response.message());
        }
    }
}
