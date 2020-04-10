package com.changhong.testwifi.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.changhong.testwifi.R;
import com.changhong.wifimng.ui.fragment.BaseFragment;

import static android.os.Looper.getMainLooper;

public class P2PFragment extends BaseFragment implements WifiP2pManager.ChannelListener, CompoundButton.OnCheckedChangeListener {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;

    private TextView mTvState;
    private Switch mSwitch;
    private ListView mListViewDevices;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = (WifiP2pManager) mActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(mActivity, getMainLooper(), this);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mActivity.registerReceiver(receiver, intentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_p2p, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mTvState = (TextView) view.findViewById(R.id.tv_p2p_state);
        mSwitch = (Switch) view.findViewById(R.id.switch1);
        mListViewDevices = (ListView) view.findViewById(R.id.lv_devices);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        mActivity.unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onChannelDisconnected() {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    private void changeStateShow(boolean is) {
        mSwitch.setChecked(is);
        mSwitch.setOnCheckedChangeListener(null);
        mTvState.setText(is ? mSwitch.getTextOn() : mSwitch.getTextOff());
        mSwitch.setOnCheckedChangeListener(this);
    }

    private void requestPeers() {

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(getClass().getSimpleName(), "====~discoverPeers  onSuccess ");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(getClass().getSimpleName(), "====~discoverPeers  onFailure : reason = " + reason);
            }
        });

        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                Log.d(getClass().getSimpleName(), "====~WifiP2pGroup : " + group);
            }
        });
        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                Log.d(getClass().getSimpleName(), "====~WifiP2pInfo : " + info);
            }
        });
    }

    /**
     * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
     */
    class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;

        public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel) {
            super();
            this.mManager = manager;
            this.mChannel = channel;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, WifiP2pManager.WIFI_P2P_STATE_DISABLED);
                Log.d(getClass().getSimpleName(), "====~EXTRA_WIFI_STATE = " + state);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    changeStateShow(true);
                } else {
                    changeStateShow(false);
                }
                requestPeers();
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
            }
        }
    }
}
