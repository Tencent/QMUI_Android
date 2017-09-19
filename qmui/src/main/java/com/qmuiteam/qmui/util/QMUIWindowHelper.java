package com.qmuiteam.qmui.util;

import android.os.Build;
import android.view.WindowManager;

/**
 *
 * @author cginechen
 * @date 2016-08-05
 */
public class QMUIWindowHelper {
    /**
     * 设置WindowManager.LayoutParams的type
     *
     * 1.使用 type 值为 TYPE_PHONE 和TYPE_SYSTEM_ALERT 需要申请 SYSTEM_ALERT_WINDOW 权限
     * 2.type 值为 TYPE_TOAST 显示的 System overlay view 不需要权限，即可在任何平台显示。
     * 3.type 值为 TYPE_TOAST在API level 19 以下因无法接收无法接收触摸（点击)和按键事件
     * 4.Android 6.0 悬浮窗被默认被禁用，即使申请了 SYSTEM_ALERT_WINDOW 权限，应用也会crash,需要用户自己去开启
     * （开启路径：通用 -- 应用管理 -- 更多 -- 配置应用 --- 在其他应用的上层显示 --- 选择你的APP -- 运行在其他应用的上层显示）
     * 5. 不直接返回type而是传layoutParams是不想调用者增加 @SuppressWarnings({"ResourceType"}) 跳过编译器的检查
     */

    public static void setWindowType(WindowManager.LayoutParams layoutParams){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
    }
}
