package com.changhong.wifimng.ui.fragment;


import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen3;
import com.changhong.wifimng.been.plc.EnumBandWidth;

import java.util.Observable;
import java.util.Observer;

/**
 * A simple {@link Fragment} subclass.
 */
public class PLCSettingFragment extends RouterSettingFragment {

    protected String[][] getChoices() {
        String[][] childarr = {getResources().getStringArray(R.array.setting_function_plc), getResources().getStringArray(R.array.setting_common)};
        return childarr;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.tv_title);
        title.setText(R.string.plc_setting);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (onFragmentLifeListener == null)
            return false;

        switch (groupPosition) {
            case 0:
                switch (childPosition) {
                    case 0://wifi setting
                        gotoNextPage(EnumPage.NET_MENU);
                        break;
                    case 1:
//                        onFragmentLifeListener.onChanged(new BaseBeen3(EnumPage.GROUP_SETTING, null, null));
                        gotoNextPage(EnumPage.GROUP_SETTING);
                        break;
                    case 2:
                        gotoNextPage(EnumPage.GUEST_NETWORK);
                        break;
                    case 3:
                        gotoNextPage(EnumPage.WLAN_ACCESS);
                        break;
                    case 4:
                        gotoNextPage(EnumPage.LAN_SETTING);
                        break;
                    default:
                        showToast(R.string.wait_for_develop);
                }
                break;

            case 1:
                switch (childPosition) {
                    case 0:
                        gotoNextPage(EnumPage.DEVICE_NAME_AND_ROOM);
                        break;
                    case 1:
                        gotoNextPage(EnumPage.ADMIN_PASSWORD);
                        break;
//                    case 2:
//                        onFragmentLifeListener.onChanged(new BaseBeen3(EnumPage.DEVICE_SHARE, null, null));
//                        break;
                    case 2://重启
                        doNextFunction(new Observer() {
                            @Override
                            public void update(Observable observable, Object o) {
                                askReboot();
                            }
                        });
                        break;
                    case 3:// reset
                        doNextFunction(new Observer() {
                            @Override
                            public void update(Observable observable, Object o) {
                                askRecovery();
                            }
                        });
                        break;
                    case 4:// reset
                        askDeleteDevice();
                        break;
                    default:
                        showToast(R.string.wait_for_develop);
                }
                break;
        }

        return false;
    }
}
