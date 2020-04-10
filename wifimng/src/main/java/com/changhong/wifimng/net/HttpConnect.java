package com.changhong.wifimng.net;

import android.content.Context;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.changhong.wifimng.uttils.FileUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/***
 * @author fuheng
 * @require implementation 'com.android.volley:dc-volley:1.1.0'
 */
public class HttpConnect {

    private final RequestQueue queue;
    private Context mContext;

    public HttpConnect(Context context) {
        mContext = context;

        Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024); // 1MB cap

        Network network = new BasicNetwork(new HurlStack());

        queue = new RequestQueue(cache, network);

        queue.start();
    }

    public void simpleRequest(String url, String tag, final OnNetworkCallback<String> observer) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        observer.onCallback(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        observer.onFailed(error.networkResponse);
                    }
                });
        if (tag != null && tag.length() > 0)
            stringRequest.setTag(tag);

        queue.add(stringRequest);

    }

    void cancel(String tag) {
        queue.cancelAll(tag);
    }

    void stop() {
        queue.stop();
    }

    public void httpPostStringRequest(String url, File cacheFile, String tag, final OnNetworkCallback<String> observer) {
        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        observer.onCallback(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        observer.onFailed(error.networkResponse);
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void httpPostJSONObjectRequest(String url, File cacheFile, String requestBody, String tag, final OnNetworkCallback<JSONObject> observer) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        jsonRequest.setTag(tag);
        queue.add(jsonRequest);
    }

    public void httpPostJSONArrayRequest(String url, File cacheFile, String requestBody, String tag, final OnNetworkCallback<JSONArray> observer) {
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.POST, url, requestBody, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                observer.onCallback(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                observer.onFailed(error.networkResponse);
            }
        });
        jsonRequest.setTag(tag);
        queue.add(jsonRequest);
    }

    public <T> void gsonRequest(String url, File cacheFile, Class<T> clazz, Map<String, String> headers, String requestBody, String tag, final OnNetworkCallback<T> observer) {
        GsonRequest jsonRequest = new GsonRequest(Request.Method.POST, url, clazz, headers, requestBody, new Response.Listener<T>() {
            @Override
            public void onResponse(T response) {
                observer.onCallback(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(getClass().getSimpleName(), "====~ error = " + error);
                observer.onFailed(error.networkResponse);
            }
        });
        jsonRequest.setTag(tag);
        queue.add(jsonRequest);
    }

    public interface OnNetworkCallback<T> {
        void onCallback(T t);

        void onFailed(NetworkResponse response);
    }

    public void httpPost(String url, Map<String, String> headers, String requestBody) {
        Log.d(getClass().getSimpleName(), "====~ 1 ");
        Log.d(getClass().getSimpleName(), "====~ url = " + url);
        Log.d(getClass().getSimpleName(), "====~ headers = " + headers);
        Log.d(getClass().getSimpleName(), "====~ requestBody = " + requestBody);
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();

            if (headers != null && !headers.isEmpty()) {
                Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> item = iterator.next();
                    httpURLConnection.setRequestProperty(item.getKey(), item.getValue());
                }
            }
            Log.d(getClass().getSimpleName(), "====~ 2 ");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
            Log.d(getClass().getSimpleName(), "====~ 3 ");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.connect();
            Log.d(getClass().getSimpleName(), "====~ 4 ");

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream()));
            bw.write(requestBody);
            bw.flush();
            bw.close();
            Log.d(getClass().getSimpleName(), "====~ 5 ");

            Thread.sleep(1000);
            byte[] result = FileUtils.readFromIputStream(httpURLConnection.getInputStream());
            Log.d(getClass().getSimpleName(), "====~ httpPost result = " + Arrays.toString(result));
            Log.d(getClass().getSimpleName(), "====~ httpPost result = " + new String(result));

            httpURLConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
