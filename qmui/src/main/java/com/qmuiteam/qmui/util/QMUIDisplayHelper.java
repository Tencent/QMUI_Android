package com.qmuiteam.qmui.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**
 *
 * @author cginechen
 * @date 2016-03-17
 */
public class QMUIDisplayHelper {

    private static final String TAG = "Devices";

    /**
     * DisplayMetrics
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    /**
     * 屏幕密度,系统源码注释不推荐使用
     */
    public static final float DENSITY = Resources.getSystem()
            .getDisplayMetrics().density;

    /**
     * 把以 dp 为单位的值，转化为以 px 为单位的值
     *
     * @param dpValue
     *            以 dp 为单位的值
     * @return px value
     */
    public static int dpToPx(int dpValue) {
        return (int) (dpValue * DENSITY + 0.5f);
    }

    /**
     * 把以 px 为单位的值，转化为以 dp 为单位的值
     *
     * @param pxValue
     *            以 px 为单位的值
     * @return dp值
     */
    public static int pxToDp(float pxValue) {
        return (int) (pxValue / DENSITY + 0.5f);
    }

    /**
     * 屏幕密度
     */
    public static float sDensity = 0f;
    public static float getDensity(Context context){
        if(sDensity == 0f){
            sDensity = getDisplayMetrics(context).density;
        }
        return sDensity;
    }

    /**
     * 屏幕宽度
     * @return
     */
    public static int getScreenWidth(Context context){
        return getDisplayMetrics(context).widthPixels;
    }

    /**
     * 屏幕高度
     * @return
     */
    public static int getScreenHeight(Context context){
        return getDisplayMetrics(context).heightPixels;
    }

    /**
     * 获取屏幕的真实宽高
     * @param context
     * @return
     */
    public static int[] getRealScreenSize(Context context){
        int[] size = new int[2];
        int widthPixels =0,heightPixels = 0;
        WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        // since SDK_INT = 1;
        widthPixels = metrics.widthPixels;
        heightPixels = metrics.heightPixels;
        try {
            // used when 17 > SDK_INT >= 14; includes window decorations (statusbar bar/menu bar)
            widthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
            heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
        } catch (Exception ignored) {
        }
        try {
            // used when SDK_INT >= 17; includes window decorations (statusbar bar/menu bar)
            Point realSize = new Point();
            Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
            widthPixels = realSize.x;
            heightPixels = realSize.y;
        } catch (Exception ignored) {
        }

        size[0] = widthPixels;
        size[1] = heightPixels;
        return size;

    }


    /**
     * 单位转换: dp -> px
     * @param dp
     * @return
     */
    public static int dp2px(Context context, int dp){
        return (int) (getDensity(context) * dp + 0.5);
    }

    /**
     * 单位转换:px -> dp
     * @param px
     * @return
     */
    public static int px2dp(Context context,int px){
        return (int) (px/getDensity(context) + 0.5);
    }

    /**
     * 是否有状态栏
     * @param context
     * @return
     */
    public static boolean hasStatusBar(Context context) {
        if(context instanceof Activity){
            Activity activity = (Activity) context;
            WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            return (attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        return true;
    }

    /**
     * 获取ActionBar高度
     * @param context
     * @return
     */
    public static int getActionBarHeight(Context context) {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize,tv, true)){
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                    context.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }


    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c;
        Object obj;
        Field field;
        int x;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            return context.getResources()
                    .getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取虚拟菜单的高度,若无则返回0
     * @param context
     * @return
     */
    public static int getNavMenuHeight(Context context){
        return getRealScreenSize(context)[1] - getScreenHeight(context);
    }

    /**
     * 是否有摄像头
     */
    private static Boolean sHasCamera = null;
    public static final boolean hasCamera(Context context) {
        if (sHasCamera == null) {
            PackageManager pckMgr =context.getPackageManager();
            boolean flag = pckMgr
                    .hasSystemFeature("android.hardware.camera.front");
            boolean flag1 = pckMgr.hasSystemFeature("android.hardware.camera");
            boolean flag2;
            if (flag || flag1)
                flag2 = true;
            else
                flag2 = false;
            sHasCamera = Boolean.valueOf(flag2);
        }
        return sHasCamera.booleanValue();
    }

    /**
     * 是否有硬件menu
     * @param context
     * @return
     */
    public static boolean hasHardwareMenuKey(Context context) {
        boolean flag = false;
        if (Build.VERSION.SDK_INT < 11)
            flag = true;
        else if ( Build.VERSION.SDK_INT >= 14) {
            flag = ViewConfiguration.get(context).hasPermanentMenuKey();
        } else
            flag = false;
        return flag;
    }

    /**
     * 是否有网络功能
     * @param context
     * @return
     */
    public static boolean hasInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    /**
     * 是否存在pckName包
     * @param pckName
     * @return
     */
    public static boolean isPackageExist(Context context, String pckName) {
        try {
            PackageInfo pckInfo = context.getPackageManager()
                    .getPackageInfo(pckName, 0);
            if (pckInfo != null)
                return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

    /**
     * SD Card是否ready
     * @return
     */
    public static boolean isSdcardReady() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    /**
     * 获取当前国家的语言
     * @param context
     * @return
     */
    public static String getCurCountryLan(Context context) {
        return context.getResources().getConfiguration().locale
                .getLanguage()
                + "-"
                + context.getResources().getConfiguration().locale
                .getCountry();
    }

    /**
     * 是否是中文环境
     * @param context
     * @return
     */
    public static boolean isZhCN(Context context) {
        String lang = context.getResources()
                .getConfiguration().locale.getCountry();
        return lang.equalsIgnoreCase("CN");
    }

    /**
     * 设置全屏
     * @param context
     */
    public static void setFullScreen(Context context) {
        if(context instanceof Activity){
            Activity activity = (Activity) context;
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            activity.getWindow().setAttributes(params);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

    }

    /**
     * 取消全屏
     * @param context
     */
    public static void cancelFullScreen(Context context) {
        if(context instanceof Activity){
            Activity activity = (Activity) context;
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().setAttributes(params);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    /**
     * 判断是否全屏
     * @param activity
     * @return
     */
    public static boolean isFullScreen(Activity activity){
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            return (params.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }


    public static boolean isElevationSupported() {
        return android.os.Build.VERSION.SDK_INT >= 21;
    }
}
