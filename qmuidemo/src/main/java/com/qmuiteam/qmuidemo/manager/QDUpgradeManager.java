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
import com.qmuiteam.qmuidemo.R;

/**
 * Created by cgspine on 2018/1/14.
 */

public class QDUpgradeManager {
    public static final int INVALIDATE_VERSION_CODE = -1;

    private static final int VERSION_1_1_0 = 110;
    private static final int sCurrentVersion = VERSION_1_1_0;
    private static QDUpgradeManager sQDUpgradeManager = null;

    private Context mContext;

    private QDUpgradeManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static final QDUpgradeManager getInstance(Context context) {
        if (sQDUpgradeManager == null) {
            sQDUpgradeManager = new QDUpgradeManager(context);
        }
        return sQDUpgradeManager;
    }

    public void check() {
        int oldVersion = QDPreferenceManager.getInstance(mContext).getVersionCode();
        int currentVersion = sCurrentVersion;
        if (currentVersion > oldVersion) {
            if (oldVersion == INVALIDATE_VERSION_CODE) {
                onNewInstall(currentVersion);
            } else {
                onUpgrade(oldVersion, currentVersion);
            }
            QDPreferenceManager.getInstance(mContext).setAppVersionCode(currentVersion);
        }
    }

    private void onUpgrade(int oldVersion, int currentVersion) {
        QDPreferenceManager.getInstance(mContext).setNeedShowUpgradeTip(true);
    }

    private void onNewInstall(int currentVersion) {
        QDPreferenceManager.getInstance(mContext).setNeedShowUpgradeTip(true);
    }

    private void appendBlockSpace(Context context, SpannableStringBuilder builder) {
        int start = builder.length();
        builder.append("[space]");
        QMUIBlockSpaceSpan blockSpaceSpan = new QMUIBlockSpaceSpan(QMUIDisplayHelper.dp2px(context, 6));
        builder.setSpan(blockSpaceSpan, start, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    public CharSequence getUpgradeWord(final Activity activity) {
        switch (sCurrentVersion) {
            case VERSION_1_1_0:
                SpannableStringBuilder text = new SpannableStringBuilder();
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
                final String issueBaseUrl = "https://github.com/QMUI/QMUI_Android/issues/";
                int start, end;
                for (int i = 0; i < issues.length; i++) {
                    if(i == issues.length - 1){
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
                    }else{
                        text.append(".");
                    }
                }
                return text;
        }
        return "欢迎体验新版本！";
    }
}
