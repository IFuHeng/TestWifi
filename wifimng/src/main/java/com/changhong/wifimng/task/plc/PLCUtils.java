package com.changhong.wifimng.task.plc;

public class PLCUtils {

    /**
     * @param value 0:open 2:wpa-psk  3:wpa2-psk 4:wpa/wp2-psk
     * @return
     */
    public static final String getModeString(int value) {
        switch (value) {
            case 0:
                return "none";
            case 2:
                return "wpa2-psk";
            case 4:
                return "wpa2_mixed_psk";
            default:
                return getModeString(2);
        }
    }

    public static final int getModeIndex(int value) {
        switch (value) {
            case 0:
                return 0;
            case 2:
                return 1;
            case 4:
                return 2;
            default:
                return 2;
        }
    }
}
