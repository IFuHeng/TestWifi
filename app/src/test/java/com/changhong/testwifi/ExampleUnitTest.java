package com.changhong.testwifi;

import android.text.Editable;

import org.junit.Test;

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

        String str = "0.00324234.4.02345";

        StringBuilder sb = new StringBuilder(str);

        for (int start = 0, end = 1; start < sb.length() ; ++end) {
//            System.out.println("start: char(" + start + ")==" + sb.charAt(start));
//            System.out.println("end: char(" + end + ")==" + sb.charAt(end));
            if (sb.charAt(end) == '.') {

                if (end - start == 1 && sb.charAt(start) == '.' && sb.charAt(end) == '.') {
//                    System.out.println("only 1 char:" + sb.charAt(start) + ", " + sb.charAt(end));
                    start = end + 1;
                    continue;
                }

                System.out.println("ll = " + sb.subSequence(start, end));
                while (end - start > 1 && sb.charAt(start) == '0') {
                    sb.delete(start, start + 1);
                    --end;
//                    System.out.println("stringbuilder = " + sb.toString());
                }
                while (end - start > 2 && Integer.parseInt(sb.substring(start, end)) > 255) {
                    sb.delete(end - 1, end);
                    --end;
//                    System.out.println("stringbuilder = " + sb.toString());
                }

                start = end + 1;
                end = start;
            } else if (end == sb.length() - 1) {
//                System.out.println("ll34 = " + sb.subSequence(start, end + 1));
                while (end - start > 0 && sb.charAt(start) == '0') {
                    sb.delete(start, start + 1);
                    --end;
//                    System.out.println("stringbuilder = " + sb.toString());
                }
                while (end - start > 1 && Integer.parseInt(sb.substring(start, end + 1)) > 255) {
                    sb.delete(end, end + 1);
                    --end;
//                    System.out.println("stringbuilder = " + sb.toString());
                }
                break;
            }
        }
    }

    static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'};

    /**
     * 判断是否是输入中的ipv4
     *
     * @param s
     * @return
     */
    private boolean isIpV4Input(Editable s) {
        // 判断超出的字符
        for (int i = 0; i < s.length(); ++i) {

            boolean isIn = false;
            for (int j = 0; j < HEX_CHARS.length && !isIn; j++) {
                if (s.charAt(i) == HEX_CHARS[j]) {
                    isIn = true;
                }
            }

            if (!isIn) {
                return false;
            }
        }

        String str = s.toString();

        // 判断'.'的数量
        int endsymbolNum = getCharCount(str, '.');
        if (endsymbolNum == 0) {
            if (s.length() > 1) {
                while (s.length() > 1 && s.charAt(0) == '0')
                    s.delete(0, 1);

                if (Integer.parseInt(str) > 255)
                    return false;
            }
        } else if (endsymbolNum > 3) {
            return false;
        } else {
            if (endsymbolNum > 1 && str.contains("..")) {//不允许".."出现
                return false;
            }

            for (int start = 0, end = 0; start < s.length(); ++end) {
                if (s.charAt(end) == '.') {

                    if (end - start == 1)
                        return false;

                    CharSequence charSequence = s.subSequence(start, end);
                    while (charSequence.length() > 1 && s.charAt(start) == '0') {
                        s.delete(start, 1);
                        --end;
                    }

                    start = end + 1;
                }
            }
        }
        return true;
    }

    private int getCharCount(String str, char c) {
        int result = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c)
                result++;
        }
        return result;
    }
}