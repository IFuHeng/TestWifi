package com.changhong.wifimng.net;

import android.accounts.AuthenticatorException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultHttpUtils {

//    private static DefaultHttpClient httpClient;

    //    private static DefaultHttpClient getHttpClient() {
//        if (httpClient == null)
//            httpClient = new DefaultHttpClient();
//        return httpClient;
//    }
    public static String httpPostWithJson(String url, String json) throws IOException, AuthenticatorException, NoSuchAlgorithmException, KeyManagementException {
        return OkHttpUtils.httpPostWithJson(url, json, null);
    }

    public static String httpPostWithJson(String url, String json, String ssid)
            throws IOException, AuthenticatorException, NoSuchAlgorithmException, KeyManagementException {

        if (ssid != null && ssid.length() > 0) {
            ssid = "ssid=" + ssid;
        }
        return OkHttpUtils.httpPostWithJson(url, json, ssid);
    }

    public static String httpPostWithJson(String url, String json, Map<String, String> cookies)
            throws IOException, AuthenticatorException, NoSuchAlgorithmException, KeyManagementException {

        String ssid = null;
        if (cookies != null && !cookies.isEmpty() && cookies.containsKey("ssid")) {
            ssid = "ssid=" + cookies.get("ssid");
        }
        return OkHttpUtils.httpPostWithJson(url, json, ssid);
        // 将JSON进行UTF-8编码,以便传输中文
//        String encoderJson = URLEncoder.encode(json, HTTP.UTF_8);
//
//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        Log.d("DefaultHttpUtils", "====~ url = " + url + ", body = " + json);
//        HttpPost httpPost = new HttpPost(url);
//        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8");
//
//        StringEntity se = new StringEntity(json, "utf-8");//解决中文乱码问题
//        se.setContentType("application/json; charset=utf-8");
//        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));
//        se.setContentEncoding(new BasicHeader("Accept-Encoding", "gzip"));
//
//        if (cookies != null && !cookies.isEmpty()) {
//            Iterator<Map.Entry<String, String>> iterator = cookies.entrySet().iterator();
//            while (iterator.hasNext()) {
//                Map.Entry<String, String> map = iterator.next();
//                BasicClientCookie2 cookie2 = new BasicClientCookie2(map.getKey(), map.getValue());
//                cookie2.setPath("/");
//                cookie2.setCommentURL(getHost(url));
//                cookie2.setDomain(getHost(url));
//                httpClient.getCookieStore().addCookie(cookie2);
//            }
//        }
//        httpPost.setEntity(se);
//
//        {
//            List<Cookie> cookieS = httpClient.getCookieStore().getCookies();
//            for (Cookie cookie : cookieS) {
//                Log.d("httpPostWithJson", "====~cookie: " + cookie.toString());
//            }
//        }
//
//        HttpResponse response = httpClient.execute(httpPost);
//        if (response.getStatusLine().getStatusCode() == 200) {
//            /** 读取服务器返回过来的json字符串数据 **/
//            String strResult = EntityUtils.toString(response.getEntity());
//            Log.d("zph", "====~strResult=" + strResult);
//            if (strResult != null && strResult.contains("\"result\""))
//                try {
//                    JSONObject jsobj = new JSONObject(strResult);
//                    strResult = jsobj.getString("result");
//                    return strResult;
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            return strResult;
//        } else if (response.getStatusLine().getStatusCode() == 401) {
//            throw new AuthenticatorException("unauthorized");
//        } else {
//            Log.d("zph", "====~ response.getStatusLine().getStatusCode = " + response.getStatusLine().getStatusCode());
//            Log.d("zph", "====~ response.getStatusLine().getReasonPhrase = " + response.getStatusLine().getReasonPhrase());
//            Log.d("zph", "====~ response.getEntity().getContentLength = " + response.getEntity().getContentLength());
//            Log.d("zph", "====~ response.getEntity() = " + response.getEntity());
//        }
//        return null;
    }

    public static String httpPostWithText(String url, String text)
            throws IOException, AuthenticatorException, NoSuchAlgorithmException, KeyManagementException {

        return OkHttpUtils.httpPostWithJson(url, text, null);

        // 将JSON进行UTF-8编码,以便传输中文

//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        Log.d("DefaultHttpUtils", "====~ url = " + url + ", body = " + text);
//        HttpPost httpPost = new HttpPost(url);
//        httpPost.addHeader(HTTP.CONTENT_TYPE, "text/plain; charset=utf-8");
//        httpPost.addHeader("cache-control", "no-cache");
//
//        StringEntity se = new StringEntity(text, "utf-8");//解决中文乱码问题
//        se.setContentType("text/plain; charset=utf-8");
//        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "text/plain; charset=utf-8"));
//
//        httpPost.setEntity(se);
//
//        {
//            List<Cookie> cookieS = httpClient.getCookieStore().getCookies();
//            for (Cookie cookie : cookieS) {
//                Log.d("httpPostWithJson", "====~cookie: " + cookie.toString());
//            }
//        }
//
//        HttpResponse response = httpClient.execute(httpPost);
//        if (response.getStatusLine().getStatusCode() == 200) {
//            /** 读取服务器返回过来的json字符串数据 **/
//            String strResult = EntityUtils.toString(response.getEntity());
//            Log.d("zph", "====~strResult=" + strResult);
//            if (strResult != null && strResult.contains("\"result\""))
//                try {
//                    JSONObject jsobj = new JSONObject(strResult);
//                    strResult = jsobj.getString("result");
//                    return strResult;
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            return strResult;
//        } else if (response.getStatusLine().getStatusCode() == 401) {
//            throw new AuthenticatorException("unauthorized");
//        } else {
//            Log.d("zph", "====~ response.getStatusLine().getStatusCode = " + response.getStatusLine().getStatusCode());
//            Log.d("zph", "====~ response.getStatusLine().getReasonPhrase = " + response.getStatusLine().getReasonPhrase());
//            Log.d("zph", "====~ response.getEntity().getContentLength = " + response.getEntity().getContentLength());
//            Log.d("zph", "====~ response.getEntity() = " + response.getEntity());
//        }
//        return null;
    }

    public static String httpsPost(String url, String json) throws IOException, AuthenticatorException, KeyManagementException, NoSuchAlgorithmException {
        return JavaHttpUtils.httpPost(url, json, null);
    }

    public static String httpPost(String url, String json) throws IOException, AuthenticatorException, KeyManagementException, NoSuchAlgorithmException {
        return OkHttpUtils.httpPost(url, json, null);
//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        Log.d("DefaultHttpUtils", "====~ url = " + url + ", body = " + json);
//        HttpPost httpPost = new HttpPost(url);
//        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8");
//
//        StringEntity se = new StringEntity(json, "utf-8");//解决中文乱码问题
//        se.setContentType("application/json; charset=utf-8");
//        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));
//        se.setContentEncoding(new BasicHeader("Accept-Encoding", "gzip"));
//
//        httpPost.setEntity(se);
//        HttpResponse response = httpClient.execute(httpPost);
//        if (response.getStatusLine().getStatusCode() == 200) {
//            /** 读取服务器返回过来的json字符串数据 **/
//            String strResult = EntityUtils.toString(response.getEntity());
//            Log.d("httpPost", "====~strResult = " + strResult);
//            return strResult;
//        } else if (response.getStatusLine().getStatusCode() == 401) {
//            throw new AuthenticatorException("unauthorized");
//        } else {
//            Log.d("zph", "====~ response.getStatusLine().getStatusCode = " + response.getStatusLine().getStatusCode());
//            throw new IOException("error code is " + response.getStatusLine().getStatusCode());
//        }
//        return null;
    }

    public static String login(String url, String requestBody) throws Exception {
        return JavaHttpUtils.login(url, requestBody);
        // 将JSON进行UTF-8编码,以便传输中文
//        String encoderJson = URLEncoder.encode(requestBody, HTTP.UTF_8);
//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        HttpPost httpPost = new HttpPost(url);
//        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
//        Log.d("DefaultHttpUtils", "====~ url = " + url + ", body = " + requestBody);
//
//        StringEntity se = new StringEntity(requestBody, "utf-8");//解决中文乱码问题
//        se.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
//        se.setContentType("User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
//        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
//                "gzip"));
//        httpPost.setEntity(se);
//        HttpResponse response = httpClient.execute(httpPost);
//        if (response.getStatusLine().getStatusCode() == 200) {
//            /** 读取服务器返回过来的json字符串数据 **/
//            String strResult = EntityUtils.toString(response.getEntity());
//
//            List<Cookie> cookies = httpClient.getCookieStore().getCookies();
//            for (Cookie cookie : cookies) {
//                Log.d("login", "====~cookie: " + cookie.toString());
//                if (cookie.getName().equals("ssid")) {
//                    Log.d("ssid", "====~ssid=" + cookie.getValue());
//                    return cookie.getValue();
//                }
//            }
//        } else
//            Log.d("login", "====~response code =" + response.getStatusLine().getStatusCode());
//
//
//        return null;
    }

    public static String getHost(String url) {
        if (url == null || url.trim().equals("")) {
            return "";
        }
        String host = "";
        Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
        Matcher matcher = p.matcher(url);
        if (matcher.find()) {
            host = matcher.group();
        }

        if (host.indexOf(':') != -1) {
            host = host.substring(0, host.indexOf(':'));
        }

        return host;
    }
}
