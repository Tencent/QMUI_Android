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

package com.qmuiteam.qmui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.layout.QMUIRelativeLayout;
import com.qmuiteam.qmui.qqface.QMUIQQFaceView;
import com.qmuiteam.qmui.skin.IQMUISkinHandlerView;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.skin.defaultAttr.IQMUISkinDefaultAttrProvider;
import com.qmuiteam.qmui.skin.defaultAttr.QMUISkinSimpleDefaultAttrProvider;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;

import org.jetbrains.annotations.NotNull;

/**
 * A standard toolbar for use within application content.
 * <p>
 * <ul>
 * <li>add icon/text/custom-view in left or right.</li>
 * <li>set title and subtitle with gravity support.</li>
 * </ul>
 */
public class QMUITopBar extends QMUIRelativeLayout implements IQMUISkinHandlerView, IQMUISkinDefaultAttrProvider {

    private static final int DEFAULT_VIEW_ID = -1;
    private int mLeftLastViewId; // 左侧最右 view 的 id
    private int mRightLastViewId; // 右侧最左 view 的 id

    private View mCenterView; // 中间的 View
    private LinearLayout mTitleContainerView; // 包裹 title 和 subTitle 的容器
    private QMUIQQFaceView mTitleView; // 显示 title 文字的 TextView
    private QMUIQQFaceView mSubTitleView; // 显示 subTitle 文字的 TextView

    private List<View> mLeftViewList;
    private List<View> mRightViewList;
    private int mTitleGravity;
    private int mLeftBackDrawableRes;
    private int mTitleTextSize;
    private int mTitleTextSizeWithSubTitle;
    private int mSubTitleTextSize;
    private int mTitleTextColor;
    private int mSubTitleTextColor;
    private int mTitleMarginHorWhenNoBtnAside;
    private int mTitleContainerPaddingHor;
    private int mTopBarImageBtnWidth;
    private int mTopBarImageBtnHeight;
    private int mTopBarTextBtnPaddingHor;
    private ColorStateList mTopBarTextBtnTextColor;
    private int mTopBarTextBtnTextSize;
    private int mTopBarHeight = -1;
    private Rect mTitleContainerRect;
    private boolean mIsBackgroundSetterDisabled = false;

    private static SimpleArrayMap<String, Integer> sDefaultSkinAttrs;

    static {
        sDefaultSkinAttrs = new SimpleArrayMap<>(4);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.BOTTOM_SEPARATOR, R.attr.qmui_skin_support_topbar_separator_color);
        sDefaultSkinAttrs.put(QMUISkinValueBuilder.BACKGROUND, R.attr.qmui_skin_support_topbar_bg);
    }

    public QMUITopBar(Context context) {
        this(context, null);
    }

    public QMUITopBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.QMUITopBarStyle);
    }

    public QMUITopBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVar();
        init(context, attrs, defStyleAttr);
    }

    private void initVar() {
        mLeftLastViewId = DEFAULT_VIEW_ID;
        mRightLastViewId = DEFAULT_VIEW_ID;
        mLeftViewList = new ArrayList<>();
        mRightViewList = new ArrayList<>();
    }

    void init(Context context, AttributeSet attrs) {
        init(context, attrs, R.attr.QMUITopBarStyle);
    }

    void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.QMUITopBar, defStyleAttr, 0);
        mLeftBackDrawableRes = array.getResourceId(R.styleable.QMUITopBar_qmui_topbar_left_back_drawable_id, R.drawable.qmui_icon_topbar_back);
        mTitleGravity = array.getInt(R.styleable.QMUITopBar_qmui_topbar_title_gravity, Gravity.CENTER);
        mTitleTextSize = array.getDimensionPixelSize(R.styleable.QMUITopBar_qmui_topbar_title_text_size, QMUIDisplayHelper.sp2px(context, 17));
        mTitleTextSizeWithSubTitle = array.getDimensionPixelSize(R.styleable.QMUITopBar_qmui_topbar_title_text_size, QMUIDisplayHelper.sp2px(context, 16));
        mSubTitleTextSize = array.getDimensionPixelSize(R.styleable.QMUITopBar_qmui_topbar_subtitle_text_size, QMUIDisplayHelper.sp2px(context, 11));
        mTitleTextColor = array.getColor(R.styleable.QMUITopBar_qmui_topbar_title_color, QMUIResHelper.getAttrColor(context, R.attr.qmui_config_color_gray_1));
        mSubTitleTextColor = array.getColor(R.styleable.QMUITopBar_qmui_topbar_subtitle_color, QMUIResHelper.getAttrColor(context, R.attr.qmui_config_color_gray_4));
        mTitleMarginHorWhenNoBtnAside = array.getDimensionPixelSize(R.styleable.QMUITopBar_qmui_topbar_title_margin_horizontal_when_no_btn_aside, 0);
        mTitleContainerPaddingHor = array.getDimensionPixelSize(R.styleable.QMUITopBar_qmui_topbar_title_container_padding_horizontal, 0);
        mTopBarImageBtnWidth = array.getDimensionPixelSize(R.styleable.QMUITopBar_qmui_topbar_image_btn_width, QMUIDisplayHelper.dp2px(context, 48));
        mTopBarImageBtnHeight = array.getDimensionPixelSize(R.styleable.QMUITopBar_qmui_topbar_image_btn_height, QMUIDisplayHelper.dp2px(context, 48));
        mTopBarTextBtnPaddingHor = array.getDimensionPixelSize(R.styleable.QMUITopBar_qmui_topbar_text_btn_padding_horizontal, QMUIDisplayHelper.dp2px(context, 12));
        mTopBarTextBtnTextColor = array.getColorStateList(R.styleable.QMUITopBar_qmui_topbar_text_btn_color_state_list);
        mTopBarTextBtnTextSize = array.getDimensionPixelSize(R.styleable.QMUITopBar_qmui_topbar_text_btn_text_size, QMUIDisplayHelper.sp2px(context, 16));
        array.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        while (parent instanceof View) {
            if (parent instanceof QMUICollapsingTopBarLayout) {
                makeSureTitleContainerView();
                return;
            }
            parent = parent.getParent();
        }
    }

    /**
     * 在 TopBar 的中间添加 View，如果此前已经有 View 通过该方法添加到 TopBar，则旧的View会被 remove
     *
     * @param view 要添加到TopBar中间的View
     */
    public void setCenterView(View view) {
        if (mCenterView == view) {
            return;
        }
        if (mCenterView != null) {
            removeView(mCenterView);
        }
        mCenterView = view;
        LayoutParams params = (LayoutParams) mCenterView.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(view, params);
    }

    /**
     * 添加 TopBar 的标题
     *
     * @param resId TopBar 的标题 resId
     */
    public QMUIQQFaceView setTitle(int resId) {
        return setTitle(getContext().getString(resId));
    }

    /**
     * 添加 TopBar 的标题
     *
     * @param title TopBar 的标题
     */
    public QMUIQQFaceView setTitle(String title) {
        QMUIQQFaceView titleView = getTitleView();
        titleView.setText(title);
        if (QMUILangHelper.isNullOrEmpty(title)) {
            titleView.setVisibility(GONE);
        } else {
            titleView.setVisibility(VISIBLE);
        }
        return titleView;
    }

    public CharSequence getTitle() {
        if (mTitleView == null) {
            return null;
        }
        return mTitleView.getText();
    }

    public void showTitleView(boolean toShow) {
        if (mTitleView != null) {
            mTitleView.setVisibility(toShow ? VISIBLE : GONE);
        }
    }

    private QMUIQQFaceView getTitleView() {
        if (mTitleView == null) {
            mTitleView = new QMUIQQFaceView(getContext());
            mTitleView.setGravity(Gravity.CENTER);
            mTitleView.setSingleLine(true);
            mTitleView.setEllipsize(TruncateAt.MIDDLE);
            mTitleView.setTextColor(mTitleTextColor);
            QMUISkinSimpleDefaultAttrProvider provider = new QMUISkinSimpleDefaultAttrProvider();
            provider.setDefaultSkinAttr(QMUISkinValueBuilder.TEXT_COLOR, R.attr.qmui_skin_support_topbar_title_color);
            mTitleView.setTag(R.id.qmui_skin_default_attr_provider, provider);
            updateTitleViewStyle();
            LinearLayout.LayoutParams titleLp = generateTitleViewAndSubTitleViewLp();
            makeSureTitleContainerView().addView(mTitleView, titleLp);
        }

        return mTitleView;
    }

    /**
     * 更新 titleView 的样式（因为有没有 subTitle 会影响 titleView 的样式）
     */
    private void updateTitleViewStyle() {
        if (mTitleView != null) {
            if (mSubTitleView == null || QMUILangHelper.isNullOrEmpty(mSubTitleView.getText())) {
                mTitleView.setTextSize(mTitleTextSize);
            } else {
                mTitleView.setTextSize(mTitleTextSizeWithSubTitle);
            }
        }
    }

    /**
     * 添加 TopBar 的副标题
     *
     * @param subTitle TopBar 的副标题
     */
    public QMUIQQFaceView setSubTitle(String subTitle) {
        QMUIQQFaceView subTitleView = getSubTitleView();
        subTitleView.setText(subTitle);
        if (QMUILangHelper.isNullOrEmpty(subTitle)) {
            subTitleView.setVisibility(GONE);
        } else {
            subTitleView.setVisibility(VISIBLE);
        }
        // 更新 titleView 的样式（因为有没有 subTitle 会影响 titleView 的样式）
        updateTitleViewStyle();
        return subTitleView;
    }

    /**
     * 添加 TopBar 的副标题
     *
     * @param resId TopBar 的副标题 resId
     */
    public QMUIQQFaceView setSubTitle(int resId) {
        return setSubTitle(getResources().getString(resId));
    }

    private QMUIQQFaceView getSubTitleView() {
        if (mSubTitleView == null) {
            mSubTitleView = new QMUIQQFaceView(getContext());
            mSubTitleView.setGravity(Gravity.CENTER);
            mSubTitleView.setSingleLine(true);
            mSubTitleView.setEllipsize(TruncateAt.MIDDLE);
            mSubTitleView.setTextSize(mSubTitleTextSize);
            mSubTitleView.setTextColor(mSubTitleTextColor);
            QMUISkinSimpleDefaultAttrProvider provider = new QMUISkinSimpleDefaultAttrProvider();
            provider.setDefaultSkinAttr(QMUISkinValueBuilder.TEXT_COLOR, R.attr.qmui_skin_support_topbar_subtitle_color);
            mSubTitleView.setTag(R.id.qmui_skin_default_attr_provider, provider);
            LinearLayout.LayoutParams titleLp = generateTitleViewAndSubTitleViewLp();
            titleLp.topMargin = QMUIDisplayHelper.dp2px(getContext(), 1);
            makeSureTitleContainerView().addView(mSubTitleView, titleLp);
        }

        return mSubTitleView;
    }

    /**
     * 设置 TopBar 的 gravity，用于控制 title 和 subtitle 的对齐方式
     *
     * @param gravity 参考 {@link android.view.Gravity}
     */
    public void setTitleGravity(int gravity) {
        mTitleGravity = gravity;
        if (mTitleView != null) {
            ((LinearLayout.LayoutParams) mTitleView.getLayoutParams()).gravity = gravity;
            if (gravity == Gravity.CENTER || gravity == Gravity.CENTER_HORIZONTAL) {
                mTitleView.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingLeft(), getPaddingBottom());
            }
        }
        if (mSubTitleView != null) {
            ((LinearLayout.LayoutParams) mSubTitleView.getLayoutParams()).gravity = gravity;
        }
        requestLayout();
    }

    public Rect getTitleContainerRect() {
        if (mTitleContainerRect == null) {
            mTitleContainerRect = new Rect();
        }
        if (mTitleContainerView == null) {
            mTitleContainerRect.set(0, 0, 0, 0);
        } else {
            QMUIViewHelper.getDescendantRect(this, mTitleContainerView, mTitleContainerRect);
        }
        return mTitleContainerRect;
    }

    public LinearLayout getTitleContainerView() {
        return mTitleContainerView;
    }

    void disableBackgroundSetter(){
        mIsBackgroundSetterDisabled = true;
        super.setBackgroundDrawable(null);
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        if(!mIsBackgroundSetterDisabled){
            super.setBackgroundDrawable(background);
        }
    }

    // ========================= leftView、rightView 相关的方法

    private LinearLayout makeSureTitleContainerView() {
        if (mTitleContainerView == null) {
            mTitleContainerView = new LinearLayout(getContext());
            // 垂直，后面要支持水平的话可以加个接口来设置
            mTitleContainerView.setOrientation(LinearLayout.VERTICAL);
            mTitleContainerView.setGravity(Gravity.CENTER);
            mTitleContainerView.setPadding(mTitleContainerPaddingHor, 0, mTitleContainerPaddingHor, 0);
            addView(mTitleContainerView, generateTitleContainerViewLp());
        }
        return mTitleContainerView;
    }

    /**
     * 生成 TitleContainerView 的 LayoutParams。
     * 左右有按钮时，该 View 在左右按钮之间；
     * 没有左右按钮时，该 View 距离 TopBar 左右边缘有固定的距离
     */
    private LayoutParams generateTitleContainerViewLp() {
        return new LayoutParams(LayoutParams.MATCH_PARENT,
                QMUIResHelper.getAttrDimen(getContext(), R.attr.qmui_topbar_height));
    }

    /**
     * 生成 titleView 或 subTitleView 的 LayoutParams
     */
    private LinearLayout.LayoutParams generateTitleViewAndSubTitleViewLp() {
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // 垂直居中
        titleLp.gravity = mTitleGravity;
        return titleLp;
    }

    /**
     * 在TopBar的左侧添加View，如果此前已经有View通过该方法添加到TopBar，则新添加进去的View会出现在已有View的右侧
     *
     * @param view   要添加到 TopBar 左边的 View
     * @param viewId 该按钮的id，可在ids.xml中找到合适的或新增。手工指定viewId是为了适应自动化测试。
     */
    public void addLeftView(View view, int viewId) {
        ViewGroup.LayoutParams viewLayoutParams = view.getLayoutParams();
        LayoutParams layoutParams;
        if (viewLayoutParams != null && viewLayoutParams instanceof LayoutParams) {
            layoutParams = (LayoutParams) viewLayoutParams;
        } else {
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        this.addLeftView(view, viewId, layoutParams);
    }

    /**
     * 在TopBar的左侧添加View，如果此前已经有View通过该方法添加到TopBar，则新添加进去的View会出现在已有View的右侧。
     *
     * @param view         要添加到 TopBar 左边的 View。
     * @param viewId       该按钮的 id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @param layoutParams 传入一个 LayoutParams，当把 Button addView 到 TopBar 时，使用这个 LayouyParams。
     */
    public void addLeftView(View view, int viewId, LayoutParams layoutParams) {
        if (mLeftLastViewId == DEFAULT_VIEW_ID) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else {
            layoutParams.addRule(RelativeLayout.RIGHT_OF, mLeftLastViewId);
        }
        layoutParams.alignWithParent = true; // alignParentIfMissing
        mLeftLastViewId = viewId;
        view.setId(viewId);
        mLeftViewList.add(view);
        addView(view, layoutParams);
    }

    /**
     * 在 TopBar 的右侧添加 View，如果此前已经有 iew 通过该方法添加到 TopBar，则新添加进去的View会出现在已有View的左侧
     *
     * @param view   要添加到 TopBar 右边的View
     * @param viewId 该按钮的id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     */
    public void addRightView(View view, int viewId) {
        ViewGroup.LayoutParams viewLayoutParams = view.getLayoutParams();
        LayoutParams layoutParams;
        if (viewLayoutParams != null && viewLayoutParams instanceof LayoutParams) {
            layoutParams = (LayoutParams) viewLayoutParams;
        } else {
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        this.addRightView(view, viewId, layoutParams);
    }

    /**
     * 在 TopBar 的右侧添加 View，如果此前已经有 View 通过该方法添加到 TopBar，则新添加进去的 View 会出现在已有View的左侧。
     *
     * @param view         要添加到 TopBar 右边的 View。
     * @param viewId       该按钮的 id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @param layoutParams 生成一个 LayoutParams，当把 Button addView 到 TopBar 时，使用这个 LayouyParams。
     */
    public void addRightView(View view, int viewId, LayoutParams layoutParams) {
        if (mRightLastViewId == DEFAULT_VIEW_ID) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            layoutParams.addRule(RelativeLayout.LEFT_OF, mRightLastViewId);
        }
        layoutParams.alignWithParent = true; // alignParentIfMissing
        mRightLastViewId = viewId;
        view.setId(viewId);
        mRightViewList.add(view);
        addView(view, layoutParams);
    }

    /**
     * 生成一个 LayoutParams，当把 Button addView 到 TopBar 时，使用这个 LayouyParams
     */
    public LayoutParams generateTopBarImageButtonLayoutParams() {
        LayoutParams lp = new LayoutParams(mTopBarImageBtnWidth, mTopBarImageBtnHeight);
        lp.topMargin = Math.max(0, (getTopBarHeight() - mTopBarImageBtnHeight) / 2);
        return lp;
    }


    public QMUIAlphaImageButton addRightImageButton(int drawableResId, int viewId) {
        return addRightImageButton(drawableResId, true, viewId);
    }

    /**
     * 根据 resourceId 生成一个 TopBar 的按钮，并 add 到 TopBar 的右侧
     *
     * @param drawableResId   按钮图片的 resourceId
     * @param viewId          该按钮的 id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @param followTintColor 换肤时使用 tintColor 更改它的颜色
     * @return 返回生成的按钮
     */
    public QMUIAlphaImageButton addRightImageButton(int drawableResId, boolean followTintColor, int viewId) {
        QMUIAlphaImageButton rightButton = generateTopBarImageButton(drawableResId, followTintColor);
        this.addRightView(rightButton, viewId, generateTopBarImageButtonLayoutParams());
        return rightButton;
    }

    public QMUIAlphaImageButton addLeftImageButton(int drawableResId, int viewId) {
        return addLeftImageButton(drawableResId, true, viewId);
    }

    /**
     * 根据 resourceId 生成一个 TopBar 的按钮，并 add 到 TopBar 的左边
     *
     * @param drawableResId   按钮图片的 resourceId
     * @param viewId          该按钮的 id，可在ids.xml中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @param followTintColor 换肤时使用 tintColor 更改它的颜色
     * @return 返回生成的按钮
     */
    public QMUIAlphaImageButton addLeftImageButton(int drawableResId, boolean followTintColor, int viewId) {
        QMUIAlphaImageButton leftButton = generateTopBarImageButton(drawableResId, followTintColor);
        this.addLeftView(leftButton, viewId, generateTopBarImageButtonLayoutParams());
        return leftButton;
    }

    /**
     * 生成一个LayoutParams，当把 Button addView 到 TopBar 时，使用这个 LayouyParams
     */
    public LayoutParams generateTopBarTextButtonLayoutParams() {
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, mTopBarImageBtnHeight);
        lp.topMargin = Math.max(0, (getTopBarHeight() - mTopBarImageBtnHeight) / 2);
        return lp;
    }

    /**
     * 在 TopBar 左边添加一个 Button，并设置文字
     *
     * @param stringResId 按钮的文字的 resourceId
     * @param viewId      该按钮的id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    public Button addLeftTextButton(int stringResId, int viewId) {
        return addLeftTextButton(getResources().getString(stringResId), viewId);
    }

    /**
     * 在 TopBar 左边添加一个 Button，并设置文字
     *
     * @param buttonText 按钮的文字
     * @param viewId     该按钮的 id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    public Button addLeftTextButton(String buttonText, int viewId) {
        Button button = generateTopBarTextButton(buttonText);
        this.addLeftView(button, viewId, generateTopBarTextButtonLayoutParams());
        return button;
    }

    /**
     * 在 TopBar 右边添加一个 Button，并设置文字
     *
     * @param stringResId 按钮的文字的 resourceId
     * @param viewId      该按钮的id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    public Button addRightTextButton(int stringResId, int viewId) {
        return addRightTextButton(getResources().getString(stringResId), viewId);
    }

    /**
     * 在 TopBar 右边添加一个 Button，并设置文字
     *
     * @param buttonText 按钮的文字
     * @param viewId     该按钮的 id，可在 ids.xml 中找到合适的或新增。手工指定 viewId 是为了适应自动化测试。
     * @return 返回生成的按钮
     */
    public Button addRightTextButton(String buttonText, int viewId) {
        Button button = generateTopBarTextButton(buttonText);
        this.addRightView(button, viewId, generateTopBarTextButtonLayoutParams());
        return button;
    }


    private IQMUISkinDefaultAttrProvider mTopBarTextDefaultAttrProvider;

    /**
     * 生成一个文本按钮，并设置文字
     *
     * @param text 按钮的文字
     * @return 返回生成的按钮
     */
    private Button generateTopBarTextButton(String text) {
        Button button = new Button(getContext());
        if (mTopBarTextDefaultAttrProvider == null) {
            QMUISkinSimpleDefaultAttrProvider provider = new QMUISkinSimpleDefaultAttrProvider();
            provider.setDefaultSkinAttr(
                    QMUISkinValueBuilder.TEXT_COLOR, R.attr.qmui_skin_support_topbar_text_btn_color_state_list);
            mTopBarTextDefaultAttrProvider = provider;

        }
        button.setTag(R.id.qmui_skin_default_attr_provider, mTopBarTextDefaultAttrProvider);
        button.setBackgroundResource(0);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setMinimumWidth(0);
        button.setMinimumHeight(0);
        button.setPadding(mTopBarTextBtnPaddingHor, 0, mTopBarTextBtnPaddingHor, 0);
        button.setTextColor(mTopBarTextBtnTextColor);
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTopBarTextBtnTextSize);
        button.setGravity(Gravity.CENTER);
        button.setText(text);
        return button;
    }


    private IQMUISkinDefaultAttrProvider mTopBarImageColorTintColorProvider;

    /**
     * 生成一个图片按钮，配合 {{@link #generateTopBarImageButtonLayoutParams()} 使用
     *
     * @param imageResourceId 图片的 resId
     */
    private QMUIAlphaImageButton generateTopBarImageButton(int imageResourceId, boolean followTintColor) {
        QMUIAlphaImageButton imageButton = new QMUIAlphaImageButton(getContext());
        if (followTintColor) {
            if (mTopBarImageColorTintColorProvider == null) {
                QMUISkinSimpleDefaultAttrProvider provider = new QMUISkinSimpleDefaultAttrProvider();
                provider.setDefaultSkinAttr(
                        QMUISkinValueBuilder.TINT_COLOR, R.attr.qmui_skin_support_topbar_image_tint_color);
                mTopBarImageColorTintColorProvider = provider;
            }
            imageButton.setTag(R.id.qmui_skin_default_attr_provider, mTopBarImageColorTintColorProvider);
        }
        imageButton.setBackgroundColor(Color.TRANSPARENT);
        imageButton.setImageResource(imageResourceId);
        return imageButton;
    }

    /**
     * 便捷方法，在 TopBar 左边添加一个返回图标按钮
     *
     * @return 返回按钮
     */
    public QMUIAlphaImageButton addLeftBackImageButton() {
        return addLeftImageButton(mLeftBackDrawableRes, R.id.qmui_topbar_item_left_back);
    }

    /**
     * 移除 TopBar 左边所有的 View
     */
    public void removeAllLeftViews() {
        for (View leftView : mLeftViewList) {
            removeView(leftView);
        }
        mLeftLastViewId = DEFAULT_VIEW_ID;
        mLeftViewList.clear();
    }

    /**
     * 移除 TopBar 右边所有的 View
     */
    public void removeAllRightViews() {
        for (View rightView : mRightViewList) {
            removeView(rightView);
        }
        mRightLastViewId = DEFAULT_VIEW_ID;
        mRightViewList.clear();
    }

    /**
     * 移除 TopBar 的 centerView 和 titleView
     */
    public void removeCenterViewAndTitleView() {
        if (mCenterView != null) {
            if (mCenterView.getParent() == this) {
                removeView(mCenterView);
            }
            mCenterView = null;
        }

        if (mTitleView != null) {
            if (mTitleView.getParent() == this) {
                removeView(mTitleView);
            }
            mTitleView = null;
        }
    }

    int getTopBarHeight() {
        if (mTopBarHeight == -1) {
            mTopBarHeight = QMUIResHelper.getAttrDimen(getContext(), R.attr.qmui_topbar_height);
        }
        return mTopBarHeight;
    }

    /**
     * 设置 TopBar 背景的透明度
     *
     * @param alpha 取值范围：[0, 255]，255表示不透明
     */
    public void setBackgroundAlpha(int alpha) {
        this.getBackground().mutate().setAlpha(alpha);
    }

    /**
     * 根据当前 offset、透明度变化的初始 offset 和目标 offset，计算并设置 Topbar 的透明度
     *
     * @param currentOffset     当前 offset
     * @param alphaBeginOffset  透明度开始变化的offset，即当 currentOffset == alphaBeginOffset 时，透明度为0
     * @param alphaTargetOffset 透明度变化的目标offset，即当 currentOffset == alphaTargetOffset 时，透明度为1
     */
    public int computeAndSetBackgroundAlpha(int currentOffset, int alphaBeginOffset, int alphaTargetOffset) {
        double alpha = (float) (currentOffset - alphaBeginOffset) / (alphaTargetOffset - alphaBeginOffset);
        alpha = Math.max(0, Math.min(alpha, 1)); // from 0 to 1
        int alphaInt = (int) (alpha * 255);
        setBackgroundAlpha(alphaInt);
        return alphaInt;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTitleContainerView != null) {
            // 计算左侧 View 的总宽度
            int leftViewWidth = getPaddingLeft();
            for (int leftViewIndex = 0; leftViewIndex < mLeftViewList.size(); leftViewIndex++) {
                View view = mLeftViewList.get(leftViewIndex);
                if (view.getVisibility() != GONE) {
                    leftViewWidth += view.getMeasuredWidth();
                }
            }
            // 计算右侧 View 的总宽度
            int rightViewWidth = getPaddingRight();
            for (int rightViewIndex = 0; rightViewIndex < mRightViewList.size(); rightViewIndex++) {
                View view = mRightViewList.get(rightViewIndex);
                if (view.getVisibility() != GONE) {
                    rightViewWidth += view.getMeasuredWidth();
                }
            }

            leftViewWidth = Math.max(mTitleMarginHorWhenNoBtnAside, leftViewWidth);
            rightViewWidth = Math.max(mTitleMarginHorWhenNoBtnAside, rightViewWidth);

            // 计算 titleContainer 的最大宽度
            int titleContainerWidth;
            if ((mTitleGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {


                // 标题水平居中，左右两侧的占位要保持一致
                titleContainerWidth = MeasureSpec.getSize(widthMeasureSpec) -
                        Math.max(leftViewWidth, rightViewWidth) * 2;
            } else {
                // 标题非水平居中，左右两侧的占位按实际计算即可
                titleContainerWidth = MeasureSpec.getSize(widthMeasureSpec) - leftViewWidth - rightViewWidth;
            }
            int titleContainerWidthMeasureSpec = MeasureSpec.makeMeasureSpec(titleContainerWidth, MeasureSpec.EXACTLY);
            mTitleContainerView.measure(titleContainerWidthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mTitleContainerView != null) {
            int titleContainerViewWidth = mTitleContainerView.getMeasuredWidth();
            int titleContainerViewHeight = mTitleContainerView.getMeasuredHeight();
            int titleContainerViewTop = (b - t - mTitleContainerView.getMeasuredHeight()) / 2;
            int titleContainerViewLeft = getPaddingLeft();
            if ((mTitleGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
                // 标题水平居中
                titleContainerViewLeft = (r - l - mTitleContainerView.getMeasuredWidth()) / 2;
            } else {
                // 标题非水平居中
                // 计算左侧 View 的总宽度
                for (int leftViewIndex = 0; leftViewIndex < mLeftViewList.size(); leftViewIndex++) {
                    View view = mLeftViewList.get(leftViewIndex);
                    if (view.getVisibility() != GONE) {
                        titleContainerViewLeft += view.getMeasuredWidth();
                    }
                }

                titleContainerViewLeft = Math.max(titleContainerViewLeft, mTitleMarginHorWhenNoBtnAside);
            }
            mTitleContainerView.layout(titleContainerViewLeft, titleContainerViewTop,
                    titleContainerViewLeft + titleContainerViewWidth,
                    titleContainerViewTop + titleContainerViewHeight);
        }
    }

    @Override
    public void handle(@NotNull QMUISkinManager manager, int skinIndex, @NotNull Resources.Theme theme, @Nullable SimpleArrayMap<String, Integer> attrs) {
        if (attrs != null) {
            for (int i = 0; i < attrs.size(); i++) {
                String key = attrs.keyAt(i);
                Integer attr = attrs.valueAt(i);
                if (attr == null) {
                    continue;
                }
                if (getParent() instanceof QMUITopBarLayout &&
                        (QMUISkinValueBuilder.BACKGROUND.equals(key) ||
                                QMUISkinValueBuilder.BOTTOM_SEPARATOR.equals(key))) {
                    // handled by parent
                    continue;
                }
                manager.defaultHandleSkinAttr(this, theme, key, attr);
            }
        }
    }


    @Override
    public SimpleArrayMap<String, Integer> getDefaultSkinAttrs() {
        return sDefaultSkinAttrs;
    }
}
