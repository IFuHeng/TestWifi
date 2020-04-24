package com.changhong.wifimng.ui.fragment;


import androidx.fragment.app.Fragment;

import com.changhong.wifimng.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeshSettingFragment extends RouterSettingFragment {

    protected String[][] getChoices() {
        return new String[][]{getResources().getStringArray(R.array.setting_function_mesh), getResources().getStringArray(R.array.setting_common)};
    }

    @Override
    protected void onClickFunctionSetting(int childPosition) {
        switch (childPosition) {
            case 0://wifi setting
                gotoNextPage(EnumPage.NET_MENU);
                break;
//                    case 1:
////                        onFragmentLifeListener.onChanged(new BaseBeen3(EnumPage.GROUP_SETTING, null, null));
//                        gotoNextPage(EnumPage.GROUP_SETTING);
//                        break;
            case 1:
                gotoNextPage(EnumPage.DDNS_SHOW);
                break;
            case 2:
                gotoNextPage(EnumPage.WLAN_ACCESS);
                break;
            case 3:
                gotoNextPage(EnumPage.LAN_SETTING);
                break;
            default:
                showToast(R.string.wait_for_develop);
        }
    }

}
