package com.changhong.wifimng;

public class Test {
    public static final void main(String[] args){
        String mac = "AA:BB:CC:DD:EE:FF";
        String name = "HH";
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_access_control_add_ex\",\"param\":{\"src_type\":1,\"enabled\":" + 1 +
                ",\"mac\":\"" + mac + "\",\"name\":\"" + name + "\"}}";

        System.out.println(body);
    }
}
