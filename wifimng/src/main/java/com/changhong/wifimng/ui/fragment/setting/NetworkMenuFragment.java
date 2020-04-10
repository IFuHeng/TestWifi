package com.changhong.wifimng.ui.fragment.setting;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.changhong.wifimng.R;
import com.changhong.wifimng.ui.fragment.BaseFragment;
import com.changhong.wifimng.ui.fragment.EnumPage;

/**
 * A simple {@link Fragment} subclass.
 */
public class NetworkMenuFragment extends BaseFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_net_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_back).setOnClickListener(this);

        view.findViewById(R.id.btn_wifi_setting).setOnClickListener(this);
        view.findViewById(R.id.btn_wlan_setting).setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onClick(View v) {
        if (onFragmentLifeListener == null)
            return;

        if (v.getId() == R.id.btn_back) {
            onFragmentLifeListener.onChanged(null);
        } else if (v.getId() == R.id.btn_wifi_setting) {
            onFragmentLifeListener.onChanged(EnumPage.WIFI_SETTING);
        } else if (v.getId() == R.id.btn_wlan_setting) {
            onFragmentLifeListener.onChanged(EnumPage.WAN_SETTING);
        }


    }

}
