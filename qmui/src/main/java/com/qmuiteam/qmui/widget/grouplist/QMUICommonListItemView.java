package com.qmuiteam.qmui.widget.grouplist;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUILangHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 作为通用列表 {@link QMUIGroupListView} 里的 item 使用，也可以单独使用。
 * 支持以下样式:
 * <ul>
 *     <li>通过 {@link #setText(SpannableString)} 设置一行文字</li>
 *     <li>通过 {@link #setDetailText(String)} 设置一行说明文字, 并通过 {@link #setOrientation(int)} 设置说明文字的位置,
 *     也可以在 xml 中使用 {@link com.qmuiteam.qmui.R.styleable#QMUICommonListItemView_qmui_orientation} 设置。</li>
 *     <li>通过 {@link #setAccessoryType(int)} 设置右侧 View 的类型, 可选的类型见 {@link QMUICommonListItemAccessoryType},
 *     也可以在 xml 中使用 {@link com.qmuiteam.qmui.R.styleable#QMUICommonListItemView_qmui_accessory_type} 设置。</li>
 * </ul>
 *
 * @author chantchen
 * @date 2015-1-8
 */
public class QMUICommonListItemView extends RelativeLayout {

    /**
     * 右侧不显示任何东西
     */
    public final static int ACCESSORY_TYPE_NONE = 0;
    /**
     * 右侧显示一个箭头
     */
    public final static int ACCESSORY_TYPE_CHEVRON = 1;
    /**
     * 右侧显示一个开关
     */
    public final static int ACCESSORY_TYPE_SWITCH = 2;
    /**
     * 自定义右侧显示的 View
     */
    public final static int ACCESSORY_TYPE_CUSTOM = 3;

    @IntDef({ACCESSORY_TYPE_NONE, ACCESSORY_TYPE_CHEVRON, ACCESSORY_TYPE_SWITCH, ACCESSORY_TYPE_CUSTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface QMUICommonListItemAccessoryType {}

    /**
     * Item 右侧的 View 的类型
     */
    private @QMUICommonListItemAccessoryType int mAccessoryType;
    private ViewGroup mAccessoryView;

    /**
     * detailText 在 title 文字的下方
     */
    public final static int VERTICAL = 0;
    /**
     * detailText 在 item 的右方
     */
    public final static int HORIZONTAL = 1;

    @IntDef({VERTICAL, HORIZONTAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface QMUICommonListItemOrientation {}

    /**
     * 控制 detailText 是在 title 文字的下方还是 item 的右方
     */
    private int mOrientation = HORIZONTAL;
    protected TextView mDetailTextView;

    /**
     * 红点在左边
     */
    public final static int REDDOT_POSITION_LEFT = 0;
    /**
     * 红点在右边
     */
    public final static int REDDOT_POSITION_RIGHT = 1;

    @IntDef({REDDOT_POSITION_LEFT, REDDOT_POSITION_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface QMUICommonListItemRedDotPosition {}

    private @QMUICommonListItemRedDotPosition int mRedDotPosition = REDDOT_POSITION_LEFT;
    private ImageView mRedDot;

    protected ImageView mImageView;
    protected TextView mTextView;
    protected CheckBox mSwitch;
    protected LinearLayout mTextContainer;
    private ViewStub mNewTipViewStub;
    private View mNewTip;

    public QMUICommonListItemView(Context context) {
        this(context, null);
    }

    public QMUICommonListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.QMUICommonListItemViewStyle);
    }

    public QMUICommonListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.qmui_common_list_item, this, true);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.QMUICommonListItemView, defStyleAttr, 0);
        @QMUICommonListItemOrientation int orientation = array.getInt(R.styleable.QMUICommonListItemView_qmui_orientation, HORIZONTAL);
        @QMUICommonListItemAccessoryType int accessoryType = array.getInt(R.styleable.QMUICommonListItemView_qmui_accessory_type, ACCESSORY_TYPE_NONE);
        final int initTitleColor = array.getColor(R.styleable.QMUICommonListItemView_qmui_commonList_titleColor, QMUIResHelper.getAttrColor(getContext(), R.attr.qmui_config_color_gray_1));
        final int initDetailColor = array.getColor(R.styleable.QMUICommonListItemView_qmui_commonList_detailColor, QMUIResHelper.getAttrColor(getContext(), R.attr.qmui_config_color_gray_5));
        array.recycle();

        mImageView = (ImageView) findViewById(R.id.group_list_item_imageView);
        mTextContainer = (LinearLayout) findViewById(R.id.group_list_item_textContainer);
        mTextView = (TextView) findViewById(R.id.group_list_item_textView);
        mTextView.setTextColor(initTitleColor);
        mRedDot = (ImageView) findViewById(R.id.group_list_item_tips_dot);
        mNewTipViewStub = (ViewStub) findViewById(R.id.group_list_item_tips_new);
        mDetailTextView = (TextView) findViewById(R.id.group_list_item_detailTextView);
        mDetailTextView.setTextColor(initDetailColor);
        LinearLayout.LayoutParams detailTextViewLP = (LinearLayout.LayoutParams) mDetailTextView.getLayoutParams();
        if (QMUIViewHelper.getIsLastLineSpacingExtraError()) {
            detailTextViewLP.bottomMargin = -getResources().getDimensionPixelOffset(R.dimen.qmui_list_item_detail_lineSpacingExtra);
        }
        if (orientation == VERTICAL) {
            detailTextViewLP.topMargin = QMUIDisplayHelper.dp2px(getContext(), 6);
        } else {
            detailTextViewLP.topMargin = 0;
        }
        mAccessoryView = (ViewGroup) findViewById(R.id.group_list_item_accessoryView);
        setOrientation(orientation);
        setAccessoryType(accessoryType);
    }

    public void setImageDrawable(Drawable drawable) {
        if (drawable == null) {
            mImageView.setVisibility(View.GONE);
        } else {
            mImageView.setImageDrawable(drawable);
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    public void setRedDotPosition(@QMUICommonListItemRedDotPosition int redDotPosition) {
        mRedDotPosition = redDotPosition;
        requestLayout();
    }


    public void setText(SpannableString text) {
        mTextView.setText(text);
        if (QMUILangHelper.isNullOrEmpty(text)) {
            mTextView.setVisibility(View.GONE);
        } else {
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    public CharSequence getText() {
        return mTextView.getText();
    }

    public void setText(CharSequence text) {
        mTextView.setText(text);
        if (QMUILangHelper.isNullOrEmpty(text)) {
            mTextView.setVisibility(View.GONE);
        } else {
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 切换是否显示小红点
     *
     * @param isShow 是否显示小红点
     */
    public void showRedDot(boolean isShow) {
        showRedDot(isShow, true);
    }

    public void showRedDot(boolean isShow, boolean configToShow) {
        mRedDot.setVisibility((isShow && configToShow) ? VISIBLE : GONE);
    }

    /**
     * 切换是否显示更新提示
     *
     * @param isShow 是否显示更新提示
     */
    public void showNewTip(boolean isShow) {
        if (isShow) {
            if (mNewTip == null) {
                mNewTip = mNewTipViewStub.inflate();
            }
            mNewTip.setVisibility(View.VISIBLE);
            mRedDot.setVisibility(GONE);
//            // 要调requestLayout强制layout一次，否则位置不对！
//            this.invalidate();
//            this.requestLayout();
        } else {
            if (mNewTip != null && mNewTip.getVisibility() == View.VISIBLE) {
                mNewTip.setVisibility(View.GONE);
            }
        }
    }

    public Object getDetailText() {
        return mDetailTextView.getText();
    }

    public void setDetailText(StringBuilder text) {
        mDetailTextView.setText(text);
        if (QMUILangHelper.isNullOrEmpty(text)) {
            mDetailTextView.setVisibility(View.GONE);
        } else {
            mDetailTextView.setVisibility(View.VISIBLE);
        }
    }

    public void setDetailText(String text) {
        mDetailTextView.setText(text);
        if (QMUILangHelper.isNullOrEmpty(text)) {
            mDetailTextView.setVisibility(View.GONE);
        } else {
            mDetailTextView.setVisibility(View.VISIBLE);
        }
    }

    public
    @QMUICommonListItemOrientation
    int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(@QMUICommonListItemOrientation int orientation) {
        mOrientation = orientation;

        LinearLayout.LayoutParams titleLp = (LinearLayout.LayoutParams) mTextView.getLayoutParams();
        // 默认文字是水平布局的
        if (mOrientation == VERTICAL) {
            mTextContainer.setOrientation(LinearLayout.VERTICAL);
            mTextContainer.setGravity(Gravity.LEFT);
            titleLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            titleLp.weight = 0;
            titleLp.bottomMargin = QMUIDisplayHelper.dp2px(getContext(), 4);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.list_item_textSize_title_style_vertical));
            mDetailTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.list_item_textSize_detail_style_vertical));
        } else {
            mTextContainer.setOrientation(LinearLayout.HORIZONTAL);
            mTextContainer.setGravity(Gravity.CENTER_VERTICAL);
            titleLp.width = 0;
            titleLp.weight = 1;
            titleLp.bottomMargin = QMUIDisplayHelper.dp2px(getContext(), 0);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.list_item_textSize_title));
            mDetailTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.list_item_textSize_detail));
        }
    }

    public int getAccessoryType() {
        return mAccessoryType;
    }

    /**
     * 设置右侧 View 的类型。
     * <p>
     * 注意如果 type 为 {@link #ACCESSORY_TYPE_SWITCH}, 那么 switch 的切换事件应该 {@link #getSwitch()} 后用 {@link CheckBox#setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener)} 来监听
     * </p>
     *
     * @param type 见 {@link QMUICommonListItemAccessoryType}
     */
    public void setAccessoryType(@QMUICommonListItemAccessoryType int type) {
        mAccessoryView.removeAllViews();
        mAccessoryType = type;

        switch (type) {
            // 向右的箭头
            case ACCESSORY_TYPE_CHEVRON: {
                ImageView tempImageView = getAccessoryImageView();
                tempImageView.setImageDrawable(QMUIResHelper.getAttrDrawable(getContext(), R.attr.qmui_common_list_item_chevron));
                mAccessoryView.addView(tempImageView);
                mAccessoryView.setVisibility(VISIBLE);
            }
            break;
            // switch开关
            case ACCESSORY_TYPE_SWITCH: {
                if (mSwitch == null) {
                    mSwitch = new CheckBox(getContext());
                    mSwitch.setButtonDrawable(R.drawable.qmui_s_icon_switch);
                    mSwitch.setLayoutParams(getAccessoryLayoutParams());
                    // disable掉且不可点击，然后通过整个item的点击事件来toggle开关的状态
                    mSwitch.setClickable(false);
                    mSwitch.setEnabled(false);
                }
                mAccessoryView.addView(mSwitch);
                mAccessoryView.setVisibility(VISIBLE);
            }
            break;
            // 自定义View
            case ACCESSORY_TYPE_CUSTOM:
                mAccessoryView.setVisibility(VISIBLE);
                break;
            // 清空所有accessoryView
            case ACCESSORY_TYPE_NONE:
                mAccessoryView.setVisibility(GONE);
                break;
        }
    }

    private ViewGroup.LayoutParams getAccessoryLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private ImageView getAccessoryImageView() {
        ImageView resultImageView = new ImageView(getContext());
        resultImageView.setLayoutParams(getAccessoryLayoutParams());
        resultImageView.setScaleType(ImageView.ScaleType.CENTER);
        return resultImageView;
    }

    public TextView getTextView() {
        return mTextView;
    }

    public TextView getDetailTextView() {
        return mDetailTextView;
    }

    public CheckBox getSwitch() {
        return mSwitch;
    }

    public ViewGroup getAccessoryContainerView() {
        return mAccessoryView;
    }

    /**
     * 添加自定义的 Accessory View
     *
     * @param view 自定义的 Accessory View
     */
    public void addAccessoryCustomView(View view) {
        if (mAccessoryType == ACCESSORY_TYPE_CUSTOM) {
            mAccessoryView.addView(view);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        // 红点的位置
        if (mRedDot != null && mRedDot.getVisibility() == View.VISIBLE) {

            int top = getHeight() / 2 - mRedDot.getMeasuredHeight() / 2;
            int textLeft = mTextContainer.getLeft();
            int left;

            if (mRedDotPosition == REDDOT_POSITION_LEFT) {
                //红点在左
                float textWidth = mTextView.getPaint().measureText(mTextView.getText().toString()); // 文字宽度
                left = (int) (textLeft + textWidth + QMUIDisplayHelper.dp2px(getContext(), 4)); // 在原来红点位置的基础上右移

            } else if (mRedDotPosition == REDDOT_POSITION_RIGHT) {
                //红点在右
                left = textLeft + mTextContainer.getWidth() - mRedDot.getMeasuredWidth();

            } else {
                return;
            }

            mRedDot.layout(left,
                    top,
                    left + mRedDot.getMeasuredWidth(),
                    top + mRedDot.getMeasuredHeight());

        }

        // New的位置
        if (mNewTip != null && mNewTip.getVisibility() == View.VISIBLE) {
            int textLeft = mTextContainer.getLeft();
            float textWidth = mTextView.getPaint().measureText(mTextView.getText().toString()); // 文字宽度
            int left = (int) (textLeft + textWidth + QMUIDisplayHelper.dp2px(getContext(), 4)); // 在原来红点位置的基础上右移
            int top = getHeight() / 2 - mNewTip.getMeasuredHeight() / 2;
            mNewTip.layout(left,
                    top,
                    left + mNewTip.getMeasuredWidth(),
                    top + mNewTip.getMeasuredHeight());
        }
    }

}
