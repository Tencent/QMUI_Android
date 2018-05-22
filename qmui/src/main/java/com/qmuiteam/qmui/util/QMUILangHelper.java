package com.qmuiteam.qmui.util;

import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;

/**
 * @author cginechen
 * @date 2016-03-17
 */
public class QMUILangHelper {

    /**
     * 获取数值的位数，例如9返回1，99返回2，999返回3
     *
     * @param number 要计算位数的数值，必须>0
     * @return 数值的位数，若传的参数小于等于0，则返回0
     */
    public static int getNumberDigits(int number) {
        if (number <= 0) return 0;
        return (int) (Math.log10(number) + 1);
    }


    public static int getNumberDigits(long number) {
        if (number <= 0) return 0;
        return (int) (Math.log10(number) + 1);
    }

    /**
     * 规范化价格字符串显示的工具类
     *
     * @param price 价格
     * @return 保留两位小数的价格字符串
     */
    public static String regularizePrice(float price) {
        return String.format(Locale.CHINESE, "%.2f", price);
    }

    /**
     * 规范化价格字符串显示的工具类
     *
     * @param price 价格
     * @return 保留两位小数的价格字符串
     */
    public static String regularizePrice(double price) {
        return String.format(Locale.CHINESE, "%.2f", price);
    }


    public static boolean isNullOrEmpty(@Nullable CharSequence string) {
        return string == null || string.length() == 0;
    }

    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean objectEquals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    public static float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }
}
