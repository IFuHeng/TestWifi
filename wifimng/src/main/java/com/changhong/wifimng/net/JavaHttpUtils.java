package com.changhong.wifimng.net;

import android.accounts.AuthenticatorException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JavaHttpUtils {

    public static String login(String registerUrl, String json)
            throws IOException, AuthenticatorException {
        Log.d("JavaHttpUtils", "====~ url = " + registerUrl + ", body = " + json);

        java.net.CookieManager manager = new java.net.CookieManager();
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);

        // 将JSON进行UTF-8编码,以便传输中文
        URL url = new URL(registerUrl);
        HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
        postConnection.setRequestMethod("POST");//post 请求
        postConnection.setConnectTimeout(1000 * 5);
        postConnection.setReadTimeout(1000 * 5);
        postConnection.setDoInput(true);//允许从服务端读取数据
        postConnection.setDoOutput(true);//允许写入
        postConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        postConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
        postConnection.setRequestProperty("Content-Type", "gzip");
        OutputStream outputStream = postConnection.getOutputStream();
        outputStream.write(json.getBytes());//把参数发送过去.
        outputStream.flush();
        int code = postConnection.getResponseCode();
        if (code == 200) {//成功
            postConnection.getHeaderFields();
            CookieStore store = manager.getCookieStore();
            Iterator<HttpCookie> iterator = store.getCookies().iterator();
            while (iterator.hasNext()) {
                HttpCookie cookie = iterator.next();
                Log.d(JavaHttpUtils.class.getSimpleName(), "====~cookie = " + cookie.getName() + "  -  " + cookie.getValue());
                if ("ssid".equalsIgnoreCase(cookie.getName()))
                    return cookie.getValue();
            }
            throw new java.net.ProtocolException("Unexpected status line");
        } else if (code == 401) {
            String string = postConnection.getResponseMessage();
            if (string == null || string.length() == 0)
                string = "unauthorized";
            throw new AuthenticatorException(string);
        } else {
            Log.d("JavaHttpUtils", "====~ postConnection.getResponseCode() = " + code);
            Log.d("JavaHttpUtils", "====~ postConnection.getResponseMessage() = " + postConnection.getResponseMessage());
            throw new IOException(postConnection.getResponseMessage());
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
        Log.d("JavaHttpUtils", "====~ url = " + registerUrl + ", body = " + json);

        // 将JSON进行UTF-8编码,以便传输中文
//        URL url = new URL(registerUrl);
        HttpURLConnection postConnection = getHttpsUrlConnection(registerUrl); //(HttpURLConnection) url.openConnection();
        postConnection.setRequestMethod("POST");//post 请求
        postConnection.setConnectTimeout(1000 * 5);
        postConnection.setReadTimeout(1000 * 5);
        postConnection.setDoInput(true);//允许从服务端读取数据
        postConnection.setDoOutput(true);//允许写入
        postConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        postConnection.setRequestProperty("Accept-Encoding", "gzip");
        if (ssid != null && ssid.length() > 0) {
            postConnection.setRequestProperty("Cookie", ssid);
//            java.net.CookieManager manager = new java.net.CookieManager();
//            manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
//            CookieHandler.setDefault(manager);
//            manager.getCookieStore().getCookies().clear();
//            manager.getCookieStore().getCookies().add(new HttpCookie("ssid", ssid));
        }
//        postConnection.getHeaderFields().put("set-cookie", "ssid=" + cookie);
        postConnection.connect();// ;连接服务器
        OutputStream outputStream = postConnection.getOutputStream();
        outputStream.write(json.getBytes());//把参数发送过去.
        outputStream.flush();
        int code = postConnection.getResponseCode();
        if (code == 200) {//成功
            InputStream inputStream = postConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder buffer = new StringBuilder();
            String line;//一行一行的读取
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);//把一行数据拼接到buffer里
            }
            line = buffer.toString();
            Log.d("JavaHttpUtils", "====~ response = " + line);
            return line;
        } else if (code == 401) {
            throw new AuthenticatorException("unauthorized");
        } else {
            Log.d("JavaHttpUtils", "====~ postConnection.getResponseCode() = " + code);
            Log.d("JavaHttpUtils", "====~ postConnection.getResponseMessage() = " + postConnection.getResponseMessage());
            throw new IOException(postConnection.getResponseMessage());
        }
    }

    private static HttpURLConnection getHttpsUrlConnection(String strUrl) throws IOException {
        URL url = new URL(strUrl);
        if (strUrl.contains("https")) {
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            //2.SSLContext 初始化
            SSLContext tls = null;
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };
            try {
                tls = SSLContext.getInstance("TLS");
                tls.init(null, trustAllCerts, new SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }

            //3.ssl工厂
            SSLSocketFactory factory = tls.getSocketFactory();
            urlConnection.setSSLSocketFactory(factory);
            //4.添加一个主机名称校验器
            urlConnection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    if (hostname.equals("home1.chwliot.com")) {
                        return true;
                    } else {
                        return true;
                    }

                }
            });

            return urlConnection;
        } else
            return (HttpURLConnection) url.openConnection();
    }

}
