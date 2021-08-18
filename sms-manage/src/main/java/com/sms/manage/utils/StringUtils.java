package com.sms.manage.utils;

public class StringUtils {
    /**
     * 不够位数的在前面补0, 保留num的长度位数字
     *
     * @param code
     * @return
     */
    public static String autoGenericCode(Integer code, int num) {
        String result = "";
        result = String.format("%0" + num + "d", code);
        return result;
    }
}
