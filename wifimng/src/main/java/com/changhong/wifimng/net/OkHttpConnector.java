package com.changhong.wifimng.net;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpConnector {

    private final OkHttpClient mHttpClient;

    public OkHttpConnector() {
        mHttpClient = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS).writeTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS).build();
    }

    public String get(final String url, HashMap<String, String> pairs) throws IOException {
        final String newUrl = buildUrlParams(url, pairs);
        Log.d(getClass().getSimpleName(), "==== get: newUrl = " + url);
        Response response = mHttpClient.newCall(new Request.Builder().url(newUrl).build()).execute();
        return showResponseContent(response);
    }

    public String post(String url, HashMap<String, String> pairs) throws IOException {

        Log.d(getClass().getSimpleName(), "==== post: url = " + url + ", params = " + pairs);
        FormBody.Builder builder = new FormBody.Builder();

        Iterator<Map.Entry<String, String>> iterator = pairs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            if (!TextUtils.isEmpty(next.getValue()))
                builder.add(next.getKey(), next.getValue());
        }

        Response response = mHttpClient.newCall(new Request.Builder()
                .url(url).addHeader("content-type", "application/x-www-form-urlencoded")
//                    .url(url).addHeader("content-type", "application/json;charset:utf-8")
                .post(builder.build()).build()).execute();
        return showResponseContent(response);
    }

    public void download(String url, OutputStream os, String contentLength, Observer observer) throws IOException {
        Request request = new Request.Builder()
                //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                .addHeader("RANGE", "bytes=" + 0 + "-" + contentLength)
                .url(url)
                .build();
        Call call = mHttpClient.newCall(request);
        Response response = call.execute();

        InputStream is = null;
        try {
            is = response.body().byteStream();
            int downloadLength = 0;
            byte[] buffer = new byte[2048];//缓冲数组2kB
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
                downloadLength += len;
                observer.update(null, downloadLength);
            }
            os.flush();
        } finally {
            //关闭IO流
            os.close();
        }

    }

    public String buildUrlParams(String url, HashMap<String, String> pairs) {
        if (pairs != null && !pairs.isEmpty()) {
            StringBuilder sb = new StringBuilder(url);
            sb.append('?');

            Iterator<Map.Entry<String, String>> iterator = pairs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                if (!TextUtils.isEmpty(next.getValue()))
                    sb.append(next.getKey()).append('=').append(next.getValue()).append('&');
            }


            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        } else
            return url;
    }

    private String showResponseContent(Response response) throws IOException {
        if (response != null && response.isSuccessful() && response.body() != null) {
            String res = response.body().string();
            Log.d(getClass().getSimpleName(), "====>" + new Throwable().getStackTrace()[1].getMethodName() + " result:" + res);
            return res;
        }
        Log.d(getClass().getSimpleName(), "====> showResponseContent: response = " + response);
        return null;
    }
}
