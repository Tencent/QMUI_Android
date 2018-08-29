package com.qmuiteam.qmui.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import java.lang.reflect.Method;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class QMUINotchHelper {

    private static final String TAG = "QMUINotchHelper";

    private static final int NOTCH_IN_SCREEN_VOIO = 0x00000020;
    private static final String MIUI_NOTCH = "ro.miui.notch";
    private static Boolean sHasNotch = null;
    private static Rect sRotation0SafeInset = null;
    private static Rect sRotation90SafeInset = null;
    private static Rect sRotation180SafeInset = null;
    private static Rect sRotation270SafeInset = null;
    private static int[] sNotchSizeInHawei = null;
    private static Boolean sHuaweiIsNotchSetToShow = null;

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

    @SuppressLint("PrivateApi")
    public static boolean hasNotchInXiaomi(Context context) {
        try {
            Class spClass = Class.forName("android.os.SystemProperties");
            Method getMethod = spClass.getDeclaredMethod("getInt", String.class, int.class);
            getMethod.setAccessible(true);
            int hasNotch = (int) getMethod.invoke(null, MIUI_NOTCH, 0);
            return hasNotch == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasNotch(View view){
        if (sHasNotch == null) {
            if(isNotchOfficialSupport()){
                if(!attachHasOfficialNotch(view)){
                    return false;
                }
            }else {
                sHasNotch = has3rdNotch(view.getContext());
            }
        }
        return sHasNotch;
    }


    public static boolean hasNotch(Activity activity) {
        if (sHasNotch == null) {
            if(isNotchOfficialSupport()){
                Window window = activity.getWindow();
                if(window == null){
                    return false;
                }
                View decorView = window.getDecorView();
                if(decorView == null){
                    return false;
                }
                if(!attachHasOfficialNotch(decorView)){
                    return false;
                }
            }else {
                sHasNotch = has3rdNotch(activity);
            }
        }
        return sHasNotch;
    }

    /**
     *
     * @param view
     * @return false indicates the failure to get the result
     */
    @TargetApi(28)
    private static boolean attachHasOfficialNotch(View view){
        WindowInsets windowInsets = view.getRootWindowInsets();
        if(windowInsets != null){
            DisplayCutout displayCutout = windowInsets.getDisplayCutout();
            sHasNotch = displayCutout != null;
            return true;
        }else{
            // view not attached, do nothing
            return false;
        }
    }

    public static boolean has3rdNotch(Context context){
        if (QMUIDeviceHelper.isHuawei()) {
            return hasNotchInHuawei(context);
        } else if (QMUIDeviceHelper.isVivo()) {
            return hasNotchInVivo(context);
        } else if (QMUIDeviceHelper.isOppo()) {
            return hasNotchInOppo(context);
        } else if (QMUIDeviceHelper.isXiaomi()) {
            return hasNotchInXiaomi(context);
        }
        return false;
    }

    public static int getSafeInsetTop(Activity activity) {
        if (!hasNotch(activity)) {
            return 0;
        }
        return getSafeInsetRect(activity).top;
    }

    public static int getSafeInsetBottom(Activity activity) {
        if (!hasNotch(activity)) {
            return 0;
        }
        return getSafeInsetRect(activity).bottom;
    }

    public static int getSafeInsetLeft(Activity activity) {
        if (!hasNotch(activity)) {
            return 0;
        }
        return getSafeInsetRect(activity).left;
    }

    public static int getSafeInsetRight(Activity activity) {
        if (!hasNotch(activity)) {
            return 0;
        }
        return getSafeInsetRect(activity).right;
    }


    public static int getSafeInsetTop(View view) {
        if (!hasNotch(view)) {
            return 0;
        }
        return getSafeInsetRect(view).top;
    }

    public static int getSafeInsetBottom(View view) {
        if (!hasNotch(view)) {
            return 0;
        }
        return getSafeInsetRect(view).bottom;
    }

    public static int getSafeInsetLeft(View view) {
        if (!hasNotch(view)) {
            return 0;
        }
        return getSafeInsetRect(view).left;
    }

    public static int getSafeInsetRight(View view) {
        if (!hasNotch(view)) {
            return 0;
        }
        return getSafeInsetRect(view).right;
    }


    private static void clearAllRectInfo() {
        sRotation0SafeInset = null;
        sRotation90SafeInset = null;
        sRotation180SafeInset = null;
        sRotation270SafeInset = null;
    }

    private static void clearPortraitRectInfo() {
        sRotation0SafeInset = null;
        sRotation180SafeInset = null;
    }

    private static void clearLandscapeRectInfo() {
        sRotation90SafeInset = null;
        sRotation270SafeInset = null;
    }

    private static Rect getSafeInsetRect(Activity activity) {
        if(isNotchOfficialSupport()){
            Rect rect = new Rect();
            View decorView = activity.getWindow().getDecorView();
            getOfficialSafeInsetRect(decorView, rect);
            return rect;
        }
        return get3rdSafeInsetRect(activity);
    }

    private static Rect getSafeInsetRect(View view) {
        if(isNotchOfficialSupport()){
            Rect rect = new Rect();
            getOfficialSafeInsetRect(view, rect);
            return rect;
        }
        return get3rdSafeInsetRect(view.getContext());
    }

    @TargetApi(28)
    private static void getOfficialSafeInsetRect(View view, Rect out) {
        if(view == null){
            return;
        }
        WindowInsets rootWindowInsets = view.getRootWindowInsets();
        if(rootWindowInsets == null){
            return;
        }
        DisplayCutout displayCutout = rootWindowInsets.getDisplayCutout();
        if(displayCutout != null){
            out.set(displayCutout.getSafeInsetLeft(), displayCutout.getSafeInsetTop(),
                    displayCutout.getSafeInsetRight(), displayCutout.getSafeInsetBottom());
        }
    }

    private static Rect get3rdSafeInsetRect(Context context){
        // 全面屏设置项更改
        if (QMUIDeviceHelper.isHuawei()) {
            boolean isHuaweiNotchSetToShow = QMUIDisplayHelper.huaweiIsNotchSetToShowInSetting(context);
            if (sHuaweiIsNotchSetToShow != null && sHuaweiIsNotchSetToShow != isHuaweiNotchSetToShow) {
                clearLandscapeRectInfo();
            }
            sHuaweiIsNotchSetToShow = isHuaweiNotchSetToShow;
        }
        int screenRotation = getScreenRotation(context);
        if (screenRotation == Surface.ROTATION_90) {
            if (sRotation90SafeInset == null) {
                sRotation90SafeInset = getRectInfoRotation90(context);
            }
            return sRotation90SafeInset;
        } else if (screenRotation == Surface.ROTATION_180) {
            if (sRotation180SafeInset == null) {
                sRotation180SafeInset = getRectInfoRotation180(context);
            }
            return sRotation180SafeInset;
        } else if (screenRotation == Surface.ROTATION_270) {
            if (sRotation270SafeInset == null) {
                sRotation270SafeInset = getRectInfoRotation270(context);
            }
            return sRotation270SafeInset;
        } else {
            if (sRotation0SafeInset == null) {
                sRotation0SafeInset = getRectInfoRotation0(context);
            }
            return sRotation0SafeInset;
        }
    }

    private static Rect getRectInfoRotation0(Context context) {
        Rect rect = new Rect();
        if (QMUIDeviceHelper.isVivo()) {
            // TODO vivo 显示与亮度-第三方应用显示比例
            rect.top = getNotchHeightInVivo(context);
            rect.bottom = 0;
        } else if (QMUIDeviceHelper.isOppo()) {
            // TODO OPPO 设置-显示-应用全屏显示-凹形区域显示控制
            rect.top = QMUIStatusBarHelper.getStatusbarHeight(context);
            rect.bottom = 0;
        } else if (QMUIDeviceHelper.isHuawei()) {
            int[] notchSize = getNotchSizeInHuawei(context);
            rect.top = notchSize[1];
            rect.bottom = 0;
        } else if (QMUIDeviceHelper.isXiaomi()) {
            rect.top = getNotchHeightInXiaomi(context);
            rect.bottom = 0;
        }
        return rect;
    }

    private static Rect getRectInfoRotation90(Context context) {
        Rect rect = new Rect();
        if (QMUIDeviceHelper.isVivo()) {
            rect.left = getNotchHeightInVivo(context);
            rect.right = 0;
        } else if (QMUIDeviceHelper.isOppo()) {
            rect.left = QMUIStatusBarHelper.getStatusbarHeight(context);
            rect.right = 0;
        } else if (QMUIDeviceHelper.isHuawei()) {
            if (sHuaweiIsNotchSetToShow) {
                rect.left = getNotchSizeInHuawei(context)[1];
            } else {
                rect.left = 0;
            }
            rect.right = 0;
        } else if (QMUIDeviceHelper.isXiaomi()) {
            rect.left = getNotchHeightInXiaomi(context);
            rect.right = 0;
        }
        return rect;
    }

    private static Rect getRectInfoRotation180(Context context) {
        Rect rect = new Rect();
        if (QMUIDeviceHelper.isVivo()) {
            rect.top = 0;
            rect.bottom = getNotchHeightInVivo(context);
        } else if (QMUIDeviceHelper.isOppo()) {
            rect.top = 0;
            rect.bottom = QMUIStatusBarHelper.getStatusbarHeight(context);
        } else if (QMUIDeviceHelper.isHuawei()) {
            int[] notchSize = getNotchSizeInHuawei(context);
            rect.top = 0;
            rect.bottom = notchSize[1];
        } else if (QMUIDeviceHelper.isXiaomi()) {
            rect.top = 0;
            rect.bottom = getNotchHeightInXiaomi(context);
        }
        return rect;
    }

    private static Rect getRectInfoRotation270(Context context) {
        Rect rect = new Rect();
        if (QMUIDeviceHelper.isVivo()) {
            rect.right = getNotchHeightInVivo(context);
            rect.left = 0;
        } else if (QMUIDeviceHelper.isOppo()) {
            rect.right = QMUIStatusBarHelper.getStatusbarHeight(context);
            rect.left = 0;
        } else if (QMUIDeviceHelper.isHuawei()) {
            if (sHuaweiIsNotchSetToShow) {
                rect.right = getNotchSizeInHuawei(context)[1];
            } else {
                rect.right = 0;
            }
            rect.left = 0;
        } else if (QMUIDeviceHelper.isXiaomi()) {
            rect.right = getNotchHeightInXiaomi(context);
            rect.left = 0;
        }
        return rect;
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
        return QMUIDisplayHelper.getStatusBarHeight(context);
    }

    public static int getNotchWidthInVivo(Context context){
        return QMUIDisplayHelper.dp2px(context, 100);
    }

    public static int getNotchHeightInVivo(Context context){
        return QMUIDisplayHelper.dp2px(context, 27);
    }

    /**
     * this method is private, because we do not need to handle tablet
     *
     * @param context
     * @return
     */
    private static int getScreenRotation(Context context) {
        WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (w == null) {
            return Surface.ROTATION_0;
        }
        Display display = w.getDefaultDisplay();
        if (display == null) {
            return Surface.ROTATION_0;
        }

        return display.getRotation();
    }

    public static boolean isNotchOfficialSupport(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    /**
     * fitSystemWindows 对小米挖孔屏横屏挖孔区域无效
     * @param view
     * @return
     */
    public static boolean needFixLandscapeNotchAreaFitSystemWindow(View view){
       return QMUIDeviceHelper.isXiaomi() && QMUINotchHelper.hasNotch(view);
    }

}
