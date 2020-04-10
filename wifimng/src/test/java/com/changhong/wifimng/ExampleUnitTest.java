package com.changhong.wifimng;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.changhong.wifimng.been.plc.DevControlRuleBeen;
import com.changhong.wifimng.been.wan.Level2Been;
import com.changhong.wifimng.uttils.WifiMacUtils;

import org.junit.Test;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);

        String mac = "AA:BB:CC:DD:EE:FF";
        String name = "HH";
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"device_access_control_add_ex\",\"param\":{\"src_type\":1,\"enabled\":" + 1 +
                ",\"mac\":\"" + mac + "\",\"name\":\"" + name + "\"}}";

        System.out.println(mac.getClass() == name.getClass());

        DevControlRuleBeen been = new DevControlRuleBeen();

//        been.setWeekStartAndEnd(new boolean[]{false, false, false, true, false, true, true});
//        System.out.println(been.toString());
//        System.out.println("Integer.toBinaryString(been.getWeekDays()) = " + Integer.toBinaryString(been.getWeekDays()));
//        System.out.println(been.getHealthString(WEEK_DAYS,"to" ,"周末","工作日","每天"));
//        been.setRepeat_day("123456");
//        System.out.println(been.getHealthString(WEEK_DAYS,"to" ,"周末","工作日","每天"));
//        been.setRepeat_day("23456");
//        System.out.println(been.getHealthString(WEEK_DAYS,"to" ,"周末","工作日","每天"));
//        been.setRepeat_day("123457");
//        System.out.println(been.getHealthString(WEEK_DAYS,"to" ,"周末","工作日","每天"));
//        been.setRepeat_day("36");
//        System.out.println(been.getHealthString(WEEK_DAYS,"to" ,"周末","工作日","每天"));
        been.setRepeat_day("23467");
        System.out.println(been.getHealthString(WEEK_DAYS,"to" ,"周末","工作日","每天"));
//        int weekDayOn = Integer.parseInt("1111100", 2);
//        System.out.println(showWeekDaysOn(weekDayOn));
//        System.out.println(showWeekDaysOn("1,4", "1,7"));

//        show99table();

//        System.out.println(isEmail("liuweirui@t-n-l.ru"));
//        System.out.println(isEmail("heng1.fu@changhong.com"));
//        System.out.println(isEmail("alucado_0@163.com"));
//        System.out.println(isEmail("alucado0@gmail.com"));
//        System.out.println(isEmail("alucado0@live.cn"));
//
//        setWeekStartAndEnd(new boolean[]{false, true, true, false, false, true, false, false});
//
//        fg();
//
//        System.out.println(Integer.toHexString("alucado0@live.cn".hashCode()));
//        System.out.println(Integer.toHexString("alucado2@live.cn".hashCode()));
//        System.out.println(Integer.toHexString(new String(new byte[]{1, 2, 3, 4, 5}).hashCode()));
//        System.out.println(Integer.toHexString("alucado1@live.cn".hashCode()));
//
//
//        System.out.println(WifiMacUtils.macNoColon("AA:BB:CC:DD:EE:FF"));
//
//        Level2Been level2Been = new Level2Been();
//        level2Been.setWeekStartAndEnd(new boolean[]{true, false, true, true, false, true, true});
//        System.out.println(level2Been);
    }

    private boolean isEmail(String email) {
        String[] regexs = {"^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$"
                , "[a-zA-Z_]{0,}[0-9]{0,}@(([a-zA-z0-9]-*){1,}\\.){1,3}[a-zA-z\\-]{1,}"};
        for (int i = 0; i < regexs.length; i++) {
            String regex = regexs[i];
            System.out.println(i);
            if (Pattern.matches(regex, email))
                return true;
        }
        return false;
    }

    private void show99table() {
        for (int i = 1; i < 10; i++) {
            for (int j = 0; j < i; j++) {
                try {
                    System.out.print(" " + (j + 1) + "*" + i + "=" + ((j + 1) * i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println();
        }
    }

    String[] WEEK_DAYS = {"一", "二", "三", "四", "五", "六", "七", "八"};

    private String showWeekDaysOn(int weekDayOn) {
        StringBuilder ss = new StringBuilder();
        int start = -1;

        for (int i = 0; i < 7; i++) {
            int tempValue = (weekDayOn >> i);
            System.out.println(Integer.toBinaryString(tempValue));
            boolean dayOn = (tempValue & 1) == 1;
            if (dayOn) {
                if (start < 0) {
                    start = i;
                }
            } else {
                if (start >= 0) {
                    if (start == i - 1) {
                        ss.append(WEEK_DAYS[start]);
                    } else {
                        ss.append(WEEK_DAYS[start]).append('到').append(WEEK_DAYS[i - 1]);
                    }
                    ss.append(' ');
                    start = -1;
                }
            }
        }
        if (start >= 0) {
            if (start == 6) {
                ss.append(WEEK_DAYS[start]);
            } else {
                ss.append(WEEK_DAYS[start]).append('到').append(WEEK_DAYS[6]);
            }
            ss.append(' ');
        }
        return ss.toString();
    }

    private String showWeekDaysOn(String startStr, String endStr) {
        String to = " to ";

        if (startStr.indexOf(',') == -1) {//连续日期
            Integer start = Integer.parseInt(startStr);
            Integer end = Integer.parseInt(endStr);
            if (start == end) {
                return WEEK_DAYS[start - 1];
            } else {
                return WEEK_DAYS[start - 1] + to + WEEK_DAYS[end - 1];
            }
        } else {//断续日期
            StringBuilder ss = new StringBuilder();
            String[] starts = startStr.split(",");
            String[] ends = endStr.split(",");
            for (int j = 0; j < starts.length; j++) {
                Integer start = Integer.parseInt(starts[j]);
                Integer end = Integer.parseInt(ends[j]);
                if (start == end) {
                    ss.append(WEEK_DAYS[start - 1]);
                } else {
                    ss.append(WEEK_DAYS[start - 1]).append(to).append(WEEK_DAYS[end - 1]);
                }
                ss.append(' ');
            }
            return ss.deleteCharAt(ss.length() - 1).toString();
        }
    }

    public void setWeekStartAndEnd(@NonNull @Size(value = 7) boolean[] weekChoice) {
        StringBuilder sbStart = new StringBuilder();
        StringBuilder sbEnd = new StringBuilder();
        for (int i = 0, start = -1; i < weekChoice.length; i++) {
            if (weekChoice[i]) {
                if (start == -1) {
                    start = i;
                    if (sbStart.length() > 0)
                        sbStart.append(',');
                    sbStart.append(i + 1);
                }
            } else {
                if (start != -1) {
                    if (sbEnd.length() > 0)
                        sbEnd.append(',');
                    sbEnd.append(i);
                    start = -1;
                }
            }
        }
        System.out.println("start = " + sbStart.toString());
        System.out.println("end = " + sbEnd.toString());
    }

    public void fg() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\r\n    \"client\": \"ANDROID/IOS\",\r\n    \"version\": \"1.0.1\",\r\n    \"data\": {\r\n    \t\"currentPage\": 1,\r\n        \"pageSize\": 20,\r\n        \"name\": \"\",\r\n    \t\"userId\": \"20191202_1239_2021080807642087\",\r\n        \"parentId\":\"\",\r\n        \"deviceModel\": \"\",\r\n    \t\"deviceType\": \"\",\r\n    \t\"deviceCategory\": \"\",\r\n        \"token\":\"9116563cb2c0730dc8dffc34728a26a7\"\r\n    },\r\n    \"key\": \"\",\r\n    \"user\": \"20191202_1239_2021080807642087\"\r\n}\r\n");
        Request request = new Request.Builder()
                .url("http://home1.chwliot.com:12281/api/smart_home_app/appDevice/list")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                System.out.println(response.body().string());
            } else {
                System.out.println(response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}