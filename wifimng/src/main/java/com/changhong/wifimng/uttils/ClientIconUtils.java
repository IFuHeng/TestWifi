package com.changhong.wifimng.uttils;

import com.changhong.wifimng.R;

public class ClientIconUtils {

    public static int getClientDevcieByString(String str) {

        if (str != null) {
            for (ClientType value : ClientType.values()) {
                if (value.isGuessBe(str))
                    return value.resIconId;
            }
        }

        return R.drawable.ic_client_other;
    }

    enum ClientType {
        MI(R.drawable.ic_client_xiaomi, "mi"),
        HUAWEI(R.drawable.ic_client_huawei, "huawei", "honor"),
        APPLE(R.drawable.ic_client_apple, "apple","mac","iphone"),
        ANDROID(R.drawable.ic_client_android, "android"),
        AMOI(R.drawable.ic_client_amoi, "amoi"),
        BBK(R.drawable.ic_client_bbk, "bbk"),
        COOLPAD(R.drawable.ic_client_coolpad, "coolpad"),
        HAIER(R.drawable.ic_client_haier, "haier"),
        HTC(R.drawable.ic_client_htc, "htc"),
        GIONEE(R.drawable.ic_client_jinli, "gionee"),
        KTOUCH(R.drawable.ic_client_ktouch, "k-touch", "ktouch"),
        LENOVO(R.drawable.ic_client_lenovo, "lianxiang", "lenovo"),
        LETV(R.drawable.ic_client_lenovo, "letv"),
        LG(R.drawable.ic_client_lg, "lg"),
        MEIZU(R.drawable.ic_client_meizu, "meizu", "lanmei"),
        MOTOROLA(R.drawable.ic_client_motorola, "motorola"),
        NOKIA(R.drawable.ic_client_nokia, "nokia"),
        OPPOE(R.drawable.ic_client_oppo, "oppoe"),
        SAMSUNG(R.drawable.ic_client_samsung, "samsung"),
        SONY(R.drawable.ic_client_sony, "sony", "vaio"),
        VIVO(R.drawable.ic_client_vivo, "vivo"),
        ZTE(R.drawable.ic_client_zte, "zte"),
        NEC(R.drawable.ic_client_nec, "nec"),
        PC(R.drawable.ic_client_pc, "pc"),
        SMARTISAN(R.drawable.ic_client_smartisan, "smartisan");

        String[] strMarks;
        int resIconId;

        ClientType(int resIconId, String... marks) {
            strMarks = marks;
            this.resIconId = resIconId;
        }

        public boolean isGuessBe(String str) {
            for (String strMark : strMarks) {
                if (str.toLowerCase().contains(strMark))
                    return true;
            }
            return false;
        }

        public int getResIconId() {
            return resIconId;
        }
    }

}
