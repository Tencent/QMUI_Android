package com.qmuiteam.qmui.util;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;

import com.qmuiteam.qmui.QMUILog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cginechen
 * @date 2016-08-11
 */
public class QMUIDeviceHelper {
    private final static String TAG = "QMUIDeviceHelper";
    private final static String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private final static String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private final static String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private final static String KEY_MEIZU_VERSION_ID = "ro.build.display.id";
    private final static String FLYME = "flyme";
    private final static String ZTEC2016 = "zte c2016";
    private final static String ZUKZ1 = "zuk z1";
    private final static String MEIZUBOARD[] = {"m9", "M9", "mx", "MX"};
    private static String sMiuiVersion;
    private static String sMiuiVersionCode;
    private static String sMiuiInternalStorage;
    private static boolean sIsTabletChecked = false;
    private static boolean sIsTabletValue = false;
    private static String sMerizuVersion;

    static {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
            Properties mProperties = new Properties();
            mProperties.load(fileInputStream);
            sMiuiVersionCode = mProperties.getProperty(KEY_MIUI_VERSION_CODE, null);
            sMiuiVersion = mProperties.getProperty(KEY_MIUI_VERSION_NAME, null);
            sMiuiInternalStorage = mProperties.getProperty(KEY_MIUI_INTERNAL_STORAGE, null);
            sMerizuVersion = mProperties.getProperty(KEY_MEIZU_VERSION_ID, null);
        } catch (IOException e) {
            QMUILog.printErrStackTrace(TAG, e, "getProperty error");
        } finally {
            QMUILangHelper.close(fileInputStream);
        }
    }

    private static boolean _isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * 判断是否平板设备
     *
     * @param context
     * @return true:平板,false:手机
     */
    public static boolean isTablet(Context context) {
        if (sIsTabletChecked) {
            return sIsTabletValue;
        }
        sIsTabletValue = _isTablet(context);
        sIsTabletChecked = true;
        return sIsTabletValue;
    }

    /**
     * 是否是flyme系统
     *
     * @return
     */
    public static boolean isFlyme() {
//        try {
//            // Invoke Build.hasSmartBar()
//            final Method method = Build.class.getMethod("hasSmartBar");
//            return method != null;
//        } catch (final Exception e) {
//            return false;
//        }
        String androidDisplay = android.os.Build.DISPLAY.toLowerCase();
        return Pattern.compile(FLYME).matcher(androidDisplay).find();
    }

    /**
     * 是否是MIUI系统
     *
     * @return
     */
    public static boolean isMIUI() {
        return !QMUILangHelper.isNullOrEmpty(sMiuiVersionCode)
                || !QMUILangHelper.isNullOrEmpty(sMiuiVersion)
                || !QMUILangHelper.isNullOrEmpty(sMiuiInternalStorage);
    }

    public static boolean isMIUIVersionHigherV5() {
        return isMIUI() && sMiuiVersion != null && (sMiuiVersion.equalsIgnoreCase("V6")
                || sMiuiVersion.equalsIgnoreCase("V7")
                || sMiuiVersion.equalsIgnoreCase("V8"));
    }

    public static boolean isFlymeVersionHigher5_2_4() {
        //查不到默认高于5.2.4
        boolean isHigher = true;
        if(sMerizuVersion != null && !sMerizuVersion.equals("")){
            Pattern pattern = Pattern.compile("(\\d+\\.){2}\\d");
            Matcher matcher = pattern.matcher(sMerizuVersion);
            if (matcher.find()) {
                String versionString = matcher.group();
                if (versionString != null && !versionString.equals("")) {
                    String[] version = versionString.split("\\.");
                    if (version.length == 3) {
                        if (Integer.valueOf(version[0]) < 5) {
                            isHigher = false;
                        } else if (Integer.valueOf(version[0]) > 5) {
                            isHigher = true;
                        } else {
                            if (Integer.valueOf(version[1]) < 2) {
                                isHigher = false;
                            } else if (Integer.valueOf(version[1]) > 2) {
                                isHigher = true;
                            } else {
                                if (Integer.valueOf(version[2]) < 4) {
                                    isHigher = false;
                                } else if (Integer.valueOf(version[2]) >= 5) {
                                    isHigher = true;
                                }
                            }
                        }
                    }

                }
            }
        }
        return isMeizu() && isHigher;
    }

    /**
     * 是否是魅族
     *
     * @return
     */
    public static boolean isMeizu() {
        return isPhone(MEIZUBOARD) || isFlyme();
    }

    /**
     * ZUK Z1,ZTK C2016: android 6.0,但不支持状态栏icon颜色改变
     *
     * @return
     */
    public static boolean isZUKZ1() {
        final String board = android.os.Build.MODEL;
        if (board == null) {
            return false;
        }
        return board.toLowerCase().contains(ZUKZ1);
    }

    public static boolean isZTKC2016(){
        final String board = android.os.Build.MODEL;
        if (board == null) {
            return false;
        }
        return board.toLowerCase().contains(ZTEC2016);
    }


    private static boolean isPhone(String[] boards) {
        final String board = android.os.Build.BOARD;
        if (board == null) {
            return false;
        }
        final int size = boards.length;
        for (int i = 0; i < size; i++) {
            if (board.equals(boards[i])) {
                return true;
            }
        }
        return false;
    }


    public static boolean isMIUIV8() {
        return isMIUI() && sMiuiVersion != null && sMiuiVersion.equalsIgnoreCase("V8");
    }

    /**
     * 判断悬浮窗权限（目前主要用户魅族与小米的检测）
     * @param context
     * @return
     */
    public static boolean isFloatWindowOpAllowed(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(context, 24);  // 24 是AppOpsManager.OP_SYSTEM_ALERT_WINDOW 的值，该值无法直接访问
        } else {
            try {
                return (context.getApplicationInfo().flags & 1 << 27) == 1 << 27;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Method method = manager.getClass().getDeclaredMethod("checkOp", int.class, int.class, String.class);
                int property = (Integer) method.invoke(manager, op,
                        Binder.getCallingUid(), context.getPackageName());
                return AppOpsManager.MODE_ALLOWED == property;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
        return false;
    }
}
