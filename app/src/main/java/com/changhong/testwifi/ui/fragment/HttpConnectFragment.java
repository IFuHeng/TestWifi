package com.changhong.testwifi.ui.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.changhong.testwifi.R;
import com.changhong.testwifi.been.HttpInterfaceDate;
import com.changhong.wifimng.net.HttpConnect;
import com.changhong.testwifi.ui.view.DefaultIPV4Watcher;
import com.changhong.testwifi.ui.view.DefaultPortWatcher;
import com.changhong.testwifi.utils.WifiUtils;
import com.changhong.wifimng.net.DefaultHttpUtils;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpConnectFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, HttpConnect.OnNetworkCallback<String> {

    private EditText mEtIp;
    private EditText mEtPort;
    private EditText mEtPath;
    private EditText mEtRequest;
    private TextView mEtResponse;
    private ImageButton mBtnRefresh;
    private FloatingActionButton mBtnSend;
    private WifiManager mWifiManager;
    private Spinner mSpinner;

    private HttpConnect httpConnect;

    private List<HttpInterfaceDate.Item> mSpinnerData;
    private TextView mTvSSID;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    mTvSSID.setText(message.obj.toString());
                    mTvError.setText(null);
                    break;
                case 1:
                    if (message.obj == null)
                        mEtResponse.setText("no response");
                    else
                        mEtResponse.setText(message.obj.toString());
                    mTvError.setText(null);
                    break;
                case 2:
                    mEtResponse.setText(null);
                    if (message.obj == null)
                        mTvError.setText("known error");
                    else {
                        Exception error = (Exception) message.obj;
                        mTvError.setText(error.getClass() + ": " + error.getMessage());
                    }
                    break;
            }
            super.handleMessage(message);
        }
    };
    private DefaultIPV4Watcher mIpWatcher;
    private TextView mTvError;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiManager = (WifiManager) mActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mIpWatcher = new DefaultIPV4Watcher();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_http_connect, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mEtIp = (EditText) view.findViewById(R.id.et_ip);
        mEtPort = (EditText) view.findViewById(R.id.et_port);
        mEtRequest = (EditText) view.findViewById(R.id.et_request);
        mEtPath = (EditText) view.findViewById(R.id.et_path);
        mEtResponse = (TextView) view.findViewById(R.id.et_response);
        mBtnRefresh = (ImageButton) view.findViewById(R.id.btn_refresh);
        mBtnSend = (FloatingActionButton) view.findViewById(R.id.btn_send);
        mSpinner = (Spinner) view.findViewById(R.id.spinner_method);

        mTvSSID = (TextView) view.findViewById(R.id.tv_ssid);
        mTvError = (TextView) view.findViewById(R.id.tv_error);

        HttpInterfaceDate interfaceDate = new HttpInterfaceDate(mActivity);
        mSpinner.setAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_dropdown_item, interfaceDate.getNameList()));
        mSpinnerData = interfaceDate.getArray();
        mSpinner.setOnItemSelectedListener(this);
        mEtPort.setText(String.valueOf(80));

        mBtnRefresh.setOnClickListener(this);
        mBtnSend.setOnClickListener(this);

        mEtIp.addTextChangedListener(mIpWatcher);
        mEtPort.addTextChangedListener(new DefaultPortWatcher());

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWifiManager != null && mWifiManager.getDhcpInfo() != null)
            mEtIp.setText(getGateway());
        else
            mEtIp.setText(WifiUtils.turnInteger2Ip(0));


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                doSend();

                break;
        }
    }

    private void doSend() {
        if (mEtPath.getText().length() == 0) {
            Toast.makeText(mActivity, "Path cannot be empty.", Toast.LENGTH_SHORT).show();
            mEtPath.requestFocus();
            return;
        }

        if (mEtRequest.getText().length() == 0) {
            Toast.makeText(mActivity, "Request params cannot be empty.", Toast.LENGTH_SHORT).show();
            mEtRequest.requestFocus();
            return;
        }

        final String address = getSendAddress();
        final String requestBody = mEtRequest.getText().toString();
        new Thread() {
            public void run() {

                try {
                    if ("login.html".equals(mEtPath.getText().toString()))
                        handler.sendMessage(handler.obtainMessage(0, 0, 0, DefaultHttpUtils.login(address, requestBody)));
                    else {
//                        Map<String, String> cookies = new HashMap<>();
//                        cookies.put("ssid", mTvSSID.getText().toString());
                        if (mTvSSID.getText().length() > 0) {
                            Map<String, String> cookies = new HashMap<>();
                            cookies.put("ssid", mTvSSID.getText().toString());
                            handler.sendMessage(handler.obtainMessage(1, 0, 0, DefaultHttpUtils.httpPostWithJson(address, requestBody, cookies)));
                        } else
                            handler.sendMessage(handler.obtainMessage(1, 0, 0, DefaultHttpUtils.httpPostWithJson(address, requestBody)));
                    }
                } catch (Exception e) {
                    handler.sendMessage(handler.obtainMessage(2, 0, 0, e));
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void startRefreshRunning() {
        Drawable drawable = mBtnRefresh.getDrawable();

    }

    private void stopRefreshRunning() {

    }

    private HttpConnect getHttpConnect() {
        if (httpConnect == null)
            httpConnect = new HttpConnect(mActivity);
        return httpConnect;
    }

    private Map<String, String> createNormalHeaders(String ip, String method, int contentLength) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Connection", "keep-alive");
        map.put("Content-Length", String.valueOf(contentLength));
        map.put("Accept", "text/html, */*; q=0.01");
        map.put("Origin", "http://" + ip);
        map.put("X-Requested-With", "XMLHttpRequest");
//        map.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
//        map.put("Content-Type", "text/plain;charset=UTF-8");
        map.put("Content-Type", "application/json; charset=UTF-8");
        map.put("Accept", "*/*");
        map.put("Referer", "http://" + ip + '/' + method);
        map.put("Accept-Encoding", "gzip, deflate");
        map.put("Accept-Language", "zh-CN,zh;q=0.9");
//        map.put("Cookie", "lock_end_time=0; lock_count=5; menuLanguageIndex=0;");

        // TODO: 从本地获取到cookie，并把cookie添加到header中
//        map.put(COOKIE_KEY,GlobalConfig.Cookie);

        return map;
    }

    private String getSendAddress() {
        return "http://" + mEtIp.getText().toString() + ':' + mEtPort.getText().toString() + '/' + mEtPath.getText().toString();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mEtRequest.setText(mSpinnerData.get(position).getParams());
        mEtPath.setText(mSpinnerData.get(position).getPath());
        Log.d(getClass().getSimpleName(), "====~ onItemSelected position = " + position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mEtRequest.getText().clear();
    }

    @Override
    public void onCallback(String o) {

        Log.d(getClass().getSimpleName(), "====~ onCallback = " + o);
    }

    @Override
    public void onFailed(NetworkResponse response) {
    }
}
