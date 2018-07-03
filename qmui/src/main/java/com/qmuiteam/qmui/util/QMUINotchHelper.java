package com.qmuiteam.qmui.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.Log;

import java.lang.reflect.Method;

public class QMUINotchHelper {

    private static final String TAG = "QMUINotchHelper";

    private static final int NOTCH_IN_SCREEN_VOIO = 0x00000020;
    private static final String MIUI_NOTCH = "ro.miui.notch";
    private static Boolean sHasNotch = null;
    private static Rect sLandscapeSafeInset = null;
    private static Rect sPortraitSafeInset = null;
    private static int[] sNotchSizeInHawei = null;
    private static Boolean sHuaweiIsNotchSetToShow = null;
    private static Boolean sXiaomiIsNotchSetToShow = null;

    public static boolean hasNotchInVivo(Context context) {
        boolean ret = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class ftFeature = cl.loadClass("android.util.FtFeature");
            Method[] methods = ftFeature.getDeclaredMethods();
            if (methods != null) {
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    if (method.getName().equalsIgnoreCase("isFeatureSupport")) {
                        ret = (boolean) method.invoke(ftFeature, NOTCH_IN_SCREEN_VOIO);
                        break;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "hasNotchInVivo ClassNotFoundException");
        } catch (Exception e) {
            Log.e(TAG, "hasNotchInVivo Exception");
        }
        return ret;
    }


    public static boolean hasNotchInHuawei(Context context) {
        boolean hasNotch = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            hasNotch = (boolean) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "hasNotchInHuawei ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "hasNotchInHuawei NoSuchMethodException");
        } catch (Exception e) {
            Log.e(TAG, "hasNotchInHuawei Exception");
        }
        return hasNotch;
    }

    public static boolean hasNotchInOppo(Context context) {
        return context.getPackageManager()
                .hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    public static boolean hasNotchInXiaomi(Context context) {
        try {
            @SuppressLint("PrivateApi") Class spClass = Class.forName("android.os.SystemProperties");
            Method getMethod = spClass.getDeclaredMethod("getInt", String.class, Integer.class);
            int hasNotch = (int) getMethod.invoke(null, MIUI_NOTCH, 0);
            return hasNotch == 1;
        } catch (Exception ignore) {
        }
        return false;
    }


    public static boolean hasNotch(Context context) {
        if (sHasNotch == null) {
            if (QMUIDeviceHelper.isHuawei()) {
                sHasNotch = hasNotchInHuawei(context);
            } else if (QMUIDeviceHelper.isVivo()) {
                sHasNotch = hasNotchInVivo(context);
            } else if (QMUIDeviceHelper.isOppo()) {
                sHasNotch = hasNotchInOppo(context);
            } else if (QMUIDeviceHelper.isXiaomi()) {
                sHasNotch = hasNotchInXiaomi(context);
            } else {
                sHasNotch = false;
            }
        }
        return sHasNotch;
    }

    public static int getSafeInsetTop(Context context) {
        if (!hasNotch(context)) {
            return 0;
        }
        return getSafeInsetRect(context).top;
    }

    public static int getSafeInsetBottom(Context context) {
        if (!hasNotch(context)) {
            return 0;
        }
        return getSafeInsetRect(context).bottom;
    }

    public static int getSafeInsetLeft(Context context) {
        if (!hasNotch(context)) {
            return 0;
        }
        return getSafeInsetRect(context).left;
    }

    public static int getSafeInsetRight(Context context) {
        if (!hasNotch(context)) {
            return 0;
        }
        return getSafeInsetRect(context).right;
    }

    private static Rect getSafeInsetRect(Context context) {
        // 全面屏设置项更改
        if (QMUIDeviceHelper.isHuawei()) {
            boolean isHuaweiNotchSetToShow = QMUIDisplayHelper.huaweiIsNotchSetToShowInSetting(context);
            if (sHuaweiIsNotchSetToShow != null && sHuaweiIsNotchSetToShow != isHuaweiNotchSetToShow) {
                sLandscapeSafeInset = null;
            }
            sHuaweiIsNotchSetToShow = isHuaweiNotchSetToShow;
        } else if (QMUIDeviceHelper.isXiaomi()) {
            boolean isXiaomiNotchSetToShow = QMUIDisplayHelper.xiaomiIsNotchSetToShowInSetting(context);
            if (sXiaomiIsNotchSetToShow != null && sXiaomiIsNotchSetToShow != isXiaomiNotchSetToShow) {
                sLandscapeSafeInset = null;
                sPortraitSafeInset = null;
            }
            sXiaomiIsNotchSetToShow = isXiaomiNotchSetToShow;
        }

        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (sPortraitSafeInset == null) {
                sPortraitSafeInset = new Rect();
                if (!hasNotch(context)) {
                    return sPortraitSafeInset;
                }
                if (QMUIDeviceHelper.isVivo()) {
                    sPortraitSafeInset.top = QMUIDisplayHelper.dp2px(context, 32);
                    // TODO vivo 系统导航-导航手势样式-显示手势操作区域
                    sPortraitSafeInset.bottom = 0;
                } else if (QMUIDeviceHelper.isOppo()) {
                    sPortraitSafeInset.top = QMUIStatusBarHelper.getStatusbarHeight(context);
                    sPortraitSafeInset.bottom = 0;
                } else if (QMUIDeviceHelper.isHuawei()) {
                    int[] notchSize = getNotchSizeInHuawei(context);
                    sPortraitSafeInset.top = notchSize[1];
                    sPortraitSafeInset.bottom = 0;
                } else if (QMUIDeviceHelper.isXiaomi()) {
                    sPortraitSafeInset.top = getNotchHeightInXiaomi(context);
                    sPortraitSafeInset.bottom = 0;
                }
            }
            return sPortraitSafeInset;
        } else {
            if (sLandscapeSafeInset == null) {
                sLandscapeSafeInset = new Rect();
                if (!hasNotch(context)) {
                    return sLandscapeSafeInset;
                }
                int safeInsetHor = 0;
                if (QMUIDeviceHelper.isVivo()) {
                    safeInsetHor = QMUIDisplayHelper.dp2px(context, 32);
                } else if (QMUIDeviceHelper.isOppo()) {
                    safeInsetHor = QMUIStatusBarHelper.getStatusbarHeight(context);
                } else if (QMUIDeviceHelper.isHuawei()) {
                    if (sHuaweiIsNotchSetToShow) {
                        safeInsetHor = getNotchSizeInHuawei(context)[1];
                    } else {
                        safeInsetHor = 0;
                    }
                } else if (QMUIDeviceHelper.isXiaomi()) {
                    if (sXiaomiIsNotchSetToShow) {
                        safeInsetHor = getNotchHeightInXiaomi(context);
                    } else {
                        safeInsetHor = 0;
                    }
                }
                sLandscapeSafeInset.left = safeInsetHor;
                sLandscapeSafeInset.right = safeInsetHor;
            }
            return sLandscapeSafeInset;
        }
    }


    public static int[] getNotchSizeInHuawei(Context context) {
        if (sNotchSizeInHawei == null) {
            sNotchSizeInHawei = new int[]{0, 0};
            try {
                ClassLoader cl = context.getClassLoader();
                Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
                Method get = HwNotchSizeUtil.getMethod("getNotchSize");
                sNotchSizeInHawei = (int[]) get.invoke(HwNotchSizeUtil);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "getNotchSizeInHuawei ClassNotFoundException");
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "getNotchSizeInHuawei NoSuchMethodException");
            } catch (Exception e) {
                Log.e(TAG, "getNotchSizeInHuawei Exception");
            }

        }
        return sNotchSizeInHawei;
    }

    public static int getNotchWidthInXiaomi(Context context) {
        int resourceId = context.getResources().getIdentifier("notch_width", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return -1;
    }

    public static int getNotchHeightInXiaomi(Context context) {
        int resourceId = context.getResources().getIdentifier("notch_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return -1;
    }

}
