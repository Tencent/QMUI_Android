/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmuidemo.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.span.QMUIBlockSpaceSpan;
import com.qmuiteam.qmui.span.QMUITouchableSpan;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIPackageHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmuidemo.QDMainActivity;
import com.qmuiteam.qmuidemo.R;
import com.qmuiteam.qmuidemo.fragment.components.section.QDSectionLayoutFragment;
import com.qmuiteam.qmuidemo.fragment.lab.QDContinuousNestedScrollFragment;

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
                .setSkinManager(QMUISkinManager.defaultInstance(activity))
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
        if(mNewVersion == QDUpgradeManager.VERSION_2_0_0_alpha11){
            text.append("1. Feature: Added a new widget: QMUINavFragment.\n");
            text.append("2. Remove LazyLifecycle, use maxLifecycle for replacement.\n");
            text.append("3. Some bug fixes.\n");
        }else if(mNewVersion == QDUpgradeManager.VERSION_2_0_0_alpha10){
            text.append("1. Feature: Added a new widget: QMUISchemeHandler.\n");
            text.append("2. Feature: Supported to remove section title if only one section in QMUIStickSectionAdapter.\n");
            text.append("3. Feature: Supported to add a QMUISkinApplyListener to View.\n");
            text.append("4. Feature: Add a boolean return value for QMUITabSegment#OnTabClickListener to decide to interrupt the event or not.\n");
            text.append("5. Some bug fixes.");
        }else if(mNewVersion == QDUpgradeManager.VERSION_2_0_0_alpha9){
            text.append("1. Some bug fixes.");
        }else if(mNewVersion == QDUpgradeManager.VERSION_2_0_0_alpha8){
            text.append("1. Feature: Add new widget QMUISeekBar.\n");
            text.append("2. Feature: Provide QMUIFragment#registerEffect to replace startFragmentForResult.\n");
            text.append("3. Feature: Provide QMUINavFragment to support child fragment navigation\n");
            text.append("4. Feature: Refactor swipe back to support muti direction.\n");
            text.append("5. Some bug fixes.");
        }else if(mNewVersion == QDUpgradeManager.VERSION_2_0_0_alpha7){
            text.append("1. Add OnProgressChangeListener for QMUIProgressBar.\n");
            text.append("2. Add skin support for CompoundButton.\n");
            text.append("3. Some bug fixes.");
        }else if(mNewVersion == QDUpgradeManager.VERSION_2_0_0_alpha6){
            text.append("1. Features: Add new widget QMUITabSegment2 to support ViewPager2.\n");
            text.append("2. Remove the skin's default usage.\n");
            text.append("3. QMUILayout support radius which is half of the view height or width.\n");
            text.append("4. Some bug fixes.");
        }else if(mNewVersion == QDUpgradeManager.VERSION_2_0_0_alpha4){
            text.append("1. Features: Add new widget: QMUIPullLayout.\n");
            text.append("2. Features: Add new widget: QMUIRVItemSwipeAction.\n");
            text.append("3. Support muti instance for QMUISkinManager.\n");
            text.append("4. some bug fixes.");
        }else if(mNewVersion == QDUpgradeManager.VERSION_2_0_0_alpha2){
            text.append("1. Bugfix: Crash Happened on Android 7 and lower.\n");
            text.append("2. Bugfix: QMUIBottomSheet overlapped the navigation bar.");
        }else if(mNewVersion == QDUpgradeManager.VERSION_2_0_0_alpha1){
            text.append("1. Migrated the library to Androidx.\n");
            text.append("2. Provided dark mode(skin) support. Almost all widgets are covered.\n");
            text.append("3. Refactor some widget such as QMUIPopup, QMUITabSegment. Provided more function.\n");
            text.append("4. Provided some simple kotlin methods.");
        }else if(mNewVersion == QDUpgradeManager.VERSION_1_4_0){
            text.append("1. Updated arch library to 0.6.0. Provide annotation MaybeFirstIn and DefaultFirstFragment.\n");
            text.append("2. Updated lint library to 1.1.0 to Support Android Studio 3.4+.\n");
            text.append("3. Replaced parent theme of QMUI.Compat with Theme.AppCompat.DayNight.\n");
            text.append("4. Fixed issues: ");
            final String[] issues = new String[]{
                    "636", "642"
            };
            handleIssues(activity, text, issues);
        }else if(mNewVersion == QDUpgradeManager.VERSION_1_3_1){
            text.append("1. ");
            addNewWidget(activity, text, "QMUIContinuousNestedScrollLayout",
                    QDDataManager.getInstance().getDocUrl(QDContinuousNestedScrollFragment.class));
            text.append("\n");
            text.append("2. ");
            addNewWidget(activity, text, "QMUIRadiusImageView2",
                    QDDataManager.getInstance().getDocUrl(QDContinuousNestedScrollFragment.class));
            text.append("Implemented with QMUILayout.\n");
            text.append("3. Updated arch library to 0.5.0. Fixed issues on new androidx version.\n");
            text.append("4. Features: QMUIQQFaceView supports paragraph space when ellipsize at the end.\n");
            text.append("5. Features: QMUITabSegment supports space weight.\n");
            text.append("6. Features: QMUIPullRefreshLayout added method setToRefreshDirectly().\n");
            text.append("7. Fixed issues: ");
            final String[] issues = new String[]{
                    "562", "563", "563"
            };
            handleIssues(activity, text, issues);
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_2_0) {
            text.append("1. ");
            addNewWidget(activity, text, "QMUIStickySectionLayout",
                    QDDataManager.getInstance().getDocUrl(QDSectionLayoutFragment.class));
            text.append("\n");
            text.append("2. Supported startFragmentForResult in child fragment. #499");
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_1_12) {
            text.append("1. Fixed drag issues when refreshing.\n");
            text.append("2. Fixed the crash in QMUIPopup under Android 4.4 because of webp.");
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_1_11) {
            text.append("1. Updated arch library to 0.3.0. Now developer must update support library to 28 or use androidx.\n");
            text.append("2. Feature: Added custom typeface support in QMUITabSegment.\n");
            text.append("3. Fixed a bug that QMUICollapsingTopBarLayout will lose title if swipe back.\n");
            text.append("4. Fixed a bug that span click event is not triggered in QMUIQQFaceView. #473\n");
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_1_10) {
            text.append("1. Simplified the use of QMUIWebContainer.\n");
            text.append("2. Refactored QMUITabSegment to handle operations such as reducing item.\n");
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_1_9) {
            text.append("1. Fixed an error that fitSystemWindows does not work in QMUIWebContainer.\n");
            text.append("2. Fixed an error that swiping back would blink.\n");
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_1_8) {
            text.append("1. Implemented QMUIWebView (beta), where supports for env(safe-area-inset-*) in css were added.\n");
            text.append("2. Feature: QMUIQQFaceView supports gravity(left/right/center-horizontal) attribute.\n");
            text.append("3. Feature: allows setting shadow color on Android ROM version 9 and higher.\n");
            text.append("4. Feature: allows control of the size of left icon in QMUIGroupListView.Section by calling the method setLeftIconSize.\n");
            text.append("5. Feature: supports custom web url matcher in QMUILinkify.\n");
            text.append("6. Fixed some bugs and increased code robustness.");
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_1_7) {
            text.append("1. Improved QMUINotchHelper to support Xiaomi. \n");
            text.append("2. Improved drawing effect of QMUIQQFaceView. \n");
            text.append("3. Fixed a bug where UI would become unresponsive " +
                    "if popBackStack was invoked during fragment transitions.");
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_1_6) {
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
        } else if (mNewVersion == QDUpgradeManager.VERSION_1_1_5) {
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

    private void addNewWidget(final Activity activity, SpannableStringBuilder text, final String widgetName, final String docUrl) {
        text.append("Added a new widget: ");
        if (docUrl == null || docUrl.length() == 0) {
            text.append(widgetName);
        } else {
            int start = text.length();
            text.append(widgetName);
            int end = text.length();
            text.setSpan(new QMUITouchableSpan(QMUIViewHelper.getActivityRoot(activity),
                    R.attr.app_skin_span_normal_text_color,
                    R.attr.app_skin_span_pressed_text_color, 0, 0) {
                @Override
                public void onSpanClick(View widget) {
                    Intent intent = QDMainActivity.createWebExplorerIntent(activity, docUrl, widgetName);
                    activity.startActivity(intent);
                }
            }, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        text.append(".");
    }

    private void handleIssues(final Activity activity, SpannableStringBuilder text, String[] issues) {
        final String issueBaseUrl = "https://github.com/Tencent/QMUI_Android/issues/";
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
                    Intent intent = QDMainActivity.createWebExplorerIntent(activity, issueBaseUrl + issue, null);
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
