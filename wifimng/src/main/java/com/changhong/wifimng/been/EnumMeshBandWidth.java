package com.changhong.wifimng.been;

public enum EnumMeshBandWidth {
    _20MHz("20MHz", 0), _40MHz("40MHz", 1), _20_40MHz("20/40MHz", 3);

    String name;
    int code;

    EnumMeshBandWidth(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public static EnumMeshBandWidth findBandWidthByPlcCode(int code) {
        for (EnumMeshBandWidth value : values()) {
            if (value.code == code)
                return value;
        }
        return null;
    }
}
