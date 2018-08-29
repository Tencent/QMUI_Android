package com.qmuiteam.qmuidemo.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;

import com.qmuiteam.qmui.span.QMUIBlockSpaceSpan;
import com.qmuiteam.qmui.span.QMUITouchableSpan;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIPackageHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmuidemo.R;

public class UpgradeTipTask implements UpgradeTask {
    private final int mOldVersion;
    private final int mNewVersion;

    public UpgradeTipTask(int oldVersion, int newVersion) {
        mOldVersion = oldVersion;
        mNewVersion = newVersion;
    }

    @Override
    public void upgrade() {
        throw new RuntimeException("please call upgrade(Activity activity)");
    }

    public void upgrade(Activity activity) {
        String title = String.format(activity.getString(R.string.app_upgrade_tip_title), QMUIPackageHelper.getAppVersion(activity));
        CharSequence message = getUpgradeWord(activity);
        new QMUIDialog.MessageDialogBuilder(activity)
                .setTitle(title)
                .setMessage(message)
                .create(R.style.ReleaseDialogTheme)
                .show();
    }

    private void appendBlockSpace(Context context, SpannableStringBuilder builder) {
        int start = builder.length();
        builder.append("[space]");
        QMUIBlockSpaceSpan blockSpaceSpan = new QMUIBlockSpaceSpan(QMUIDisplayHelper.dp2px(context, 6));
        builder.setSpan(blockSpaceSpan, start, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    public CharSequence getUpgradeWord(final Activity activity) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        if(mNewVersion == QDUpgradeManager.VERSION_1_1_7){
            text.append("1. Improved QMUINotchHelper to support Xiaomi. \n");
            text.append("2. Improved drawing effect of QMUIQQFaceView. \n");
            text.append("3. Fixed a bug where UI would become unresponsive " +
                    "if popBackStack was invoked during fragment transitions.");
        }else if(mNewVersion == QDUpgradeManager.VERSION_1_1_6){
            text.append("1. Feature: QMUINotchHelper, a new helper class for notch compatibility. \n");
            appendBlockSpace(activity, text);
            text.append("2. Added \"more\" click event to QMUIQQFaceView.\n");
            appendBlockSpace(activity, text);
            text.append("3. Added text color setter for QMUITouchableSpan.\n");
            appendBlockSpace(activity, text);
            text.append("4. The method startFragmentAndDestroyCurrent in QMUIFragment supports transfer of target fragment.\n");
            appendBlockSpace(activity, text);
            text.append("5. Fixed issues: ");
            final String[] issues = new String[]{
                    "334", "352"
            };
            handleIssues(activity, text, issues);
        }else if (mNewVersion == QDUpgradeManager.VERSION_1_1_5) {
            text.append("1. Code optimization for QMUIDialog.\n");
            appendBlockSpace(activity, text);
            text.append("2. Added a return value to KeyboardVisibilityEventListener, which " +
                    "determines whether OnGlobalLayoutListener is deleted.\n");
            appendBlockSpace(activity, text);
            text.append("3. Bug fix: getSignCount() in QMUITabSegment should return 0 " +
                    "if view is not visible.\n");
            appendBlockSpace(activity, text);
            text.append("4. Bug fix: fixed incorrect layout of translucent status bar may " +
                    "appear in Android 4.4.\n");
            appendBlockSpace(activity, text);
            text.append("5. Fixed issues: ");
            final String[] issues = new String[]{
                    "304", "308"
            };
            handleIssues(activity, text, issues);
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_1_4) {
            text.append("1. Added a new widget: QMUIPriorityLinearLayout.\n");
            appendBlockSpace(activity, text);
            text.append("2. Bug fix: marginRight does not make sense for controlling " +
                    "the position of signCount, it should use marginLeft.\n");
            appendBlockSpace(activity, text);
            text.append("3. Fixed issues: ");
            final String[] issues = new String[]{
                    "165", "247"
            };
            handleIssues(activity, text, issues);
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_1_3) {
            text.append("1. Feature: delay validation of QMUIFragment.canDragBack() until a pop " +
                    "gesture occurs. This feature allows you to control pop gesture on the fly.\n");
            appendBlockSpace(activity, text);
            text.append("2. Replace QMUIMaterialProgressDrawable with CircularProgressDrawable, " +
                    "an official implementation.\n");
            appendBlockSpace(activity, text);
            text.append("3. Fixed issues: ");
            final String[] issues = new String[]{
                    "254", "258", "284", "285", "293", "294"
            };
            handleIssues(activity, text, issues);
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_1_2) {
            text.append("1. Updated arch library to 0.0.4 to fix issue #235.\n");
            appendBlockSpace(activity, text);
            text.append("2. Added API to get line count in QMUIFloatLayout");
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_1_1) {
            text.append("1. Bug fixes: can not read /system/build.prop begin from android 8.0.\n");
            appendBlockSpace(activity, text);
            text.append("2. Allow custom layout in QMUIPopup.");
        } else if (mNewVersion <= QDUpgradeManager.VERSION_1_1_0) {
            text.append("1. Added QMUILayout, making it easy to implement shadows, radii, and separators.\n");
            appendBlockSpace(activity, text);
            text.append("2. Refactored the theme usage of QMUITopbar.\n");
            appendBlockSpace(activity, text);
            text.append("3. Refactored QMUIDialog for more flexible configuration.\n");
            appendBlockSpace(activity, text);
            text.append("4. Updated arch library to 0.0.3 to provide methods runAfterAnimation and startFragmentForResult.\n");
            appendBlockSpace(activity, text);
            text.append("5. Bug fixes: ");
            final String[] issues = new String[]{
                    "125", "127", "132", "141", "177", "184", "198", "200", "209", "213"
            };
            handleIssues(activity, text, issues);
        } else {
            text.append("welcome to QMUI!");
        }
        return text;
    }

    private void handleIssues(final Activity activity, SpannableStringBuilder text, String[] issues) {
        final String issueBaseUrl = "https://github.com/QMUI/QMUI_Android/issues/";
        int start, end;
        for (int i = 0; i < issues.length; i++) {
            if (i == issues.length - 1) {
                text.append("and ");
            }
            final String issue = issues[i];
            start = text.length();
            text.append("#");
            text.append(issue);
            end = text.length();
            int normalColor = ContextCompat.getColor(activity, R.color.app_color_blue);
            int pressedColor = ContextCompat.getColor(activity, R.color.app_color_blue_pressed);
            text.setSpan(new QMUITouchableSpan(normalColor, pressedColor, 0, 0) {
                @Override
                public void onSpanClick(View widget) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(issueBaseUrl + issue));
                    activity.startActivity(intent);
                }
            }, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            if (i < issues.length - 1) {
                text.append(", ");
            } else {
                text.append(".");
            }
        }
    }
}
