package com.changhong.testwifi.utils;

public class NumberUtils {

    /**
     * 正整数正则表达式
     */
    public static final String REGEX_POSITIVE_INTEGER = "^[1-9]\\d*|0$";
    /**
     * 负整数正则表达式
     */
    public static final String REGEX_NEGATIVE_INTEGER = "^-[1-9]\\d*$";
    /**
     * 负整数正则表达式
     */
    public static final String REGEX_INTEGER = "^-?[1-9]\\d*|0$";
    /**
     * 匹配非负整数（正整数 + 0）正则表达式
     */
    public static final String REGEX_NOT_NEGATIVE_INTEGER = "^[1-9]\\d*|0$";
    /**
     * 匹配非正整数（正整数 + 0）正则表达式
     */
    public static final String REGEX_NOT_POSITIVE_INTEGER = "^-[1-9]\\d*|0$";
    /**
     * 正浮点数 正则表达式
     */
    public static final String REGEX_NEGATIVE_FLOAT = "^-[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*$";
    /**
     * 负浮点数 正则表达式
     */
    public static final String REGEX_POSITIVE_FLOAT = "^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*$";


    public static final String REGEX_FLOAT = "^-?([1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|0?\\.0+|0)|0$";

    //^[1-9]\d*$　 　 //匹配正整数
    //^-[1-9]\d*$ 　 //匹配负整数
    //^-?[1-9]\d*$　　 //匹配整数
    //^[1-9]\d*|0$　 //匹配非负整数（正整数 + 0）
    //^-[1-9]\d*|0$　　 //匹配非正整数（负整数 + 0）
    //^[1-9]\d*\.\d*|0\.\d*[1-9]\d*$　　 //匹配正浮点数
    //^-([1-9]\d*\.\d*|0\.\d*[1-9]\d*)$　 //匹配负浮点数
    //^-?([1-9]\d*\.\d*|0\.\d*[1-9]\d*|0?\.0+|0)$　 //匹配浮点数
    //^[1-9]\d*\.\d*|0\.\d*[1-9]\d*|0?\.0+|0$　　 //匹配非负浮点数（正浮点数 + 0）
    //^(-([1-9]\d*\.\d*|0\.\d*[1-9]\d*))|0?\.0+|0$　　//匹配非正浮点数（负浮点数 + 0）

}
