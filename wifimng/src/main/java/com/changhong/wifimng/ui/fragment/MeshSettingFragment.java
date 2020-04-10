package com.changhong.wifimng.ui.fragment;


import android.view.View;
import android.widget.ExpandableListView;

import androidx.fragment.app.Fragment;

import com.changhong.wifimng.R;
import com.changhong.wifimng.been.BaseBeen3;

import java.util.Observable;
import java.util.Observer;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeshSettingFragment extends RouterSettingFragment {

    protected String[][] getChoices() {
        return new String[][]{getResources().getStringArray(R.array.setting_function_mesh), getResources().getStringArray(R.array.setting_common)};
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
                        gotoNextPage(EnumPage.DDNS_SHOW);
                        break;
                    case 3:
                        gotoNextPage(EnumPage.WLAN_ACCESS);
                        break;
                    case 4:
                        gotoNextPage(EnumPage.LAN_SETTING);
                        break;
                    case 5:
                        gotoNextPage(EnumPage.TEST_SPEED_LIMIT_LIST);
                        break;
                    case 6:
                        gotoNextPage(EnumPage.TEST_INTERNET_TIME_LIMIT_LIST);
                        break;
                    default:
                        showToast(R.string.wait_for_develop);
                }
                break;

            case 1:
                switch (childPosition) {
                    case 0:
                        onFragmentLifeListener.onChanged(new BaseBeen3(EnumPage.DEVICE_NAME_AND_ROOM, null, null));
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
