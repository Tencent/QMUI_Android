package com.qmuiteam.qmui.widget.textview;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.qmuiteam.qmui.span.QMUITouchableSpan;
import com.qmuiteam.qmui.link.QMUILinkTouchMovementMethod;

/**
 * <p>
 * 修复了 {@link TextView} 与 {@link android.text.style.ClickableSpan} 一起使用时，
 * 点击 {@link android.text.style.ClickableSpan} 也会触发 {@link TextView} 的事件的问题。
 * </p>
 * <p>
 * 同时通过 {@link #setNeedForceEventToParent(boolean)} 控制该 TextView 的点击事件能否传递给其 Parent，
 * 修复了 {@link TextView} 默认情况下如果添加了 {@link android.text.style.ClickableSpan} 之后就无法把点击事件传递给 {@link TextView} 的 Parent 的问题。
 * </p>
 * <p>
 * 注意: 使用该 {@link TextView} 时, 用 {@link QMUITouchableSpan} 代替 {@link android.text.style.ClickableSpan},
 * 且同时可以使用 {@link QMUITouchableSpan} 达到修改 span 的文字颜色和背景色的目的。
 * </p>
 * <p>
 * 注意: 使用该 {@link TextView} 时, 需调用 {@link #setMovementMethodDefault()} 方法设置默认的 {@link QMUILinkTouchMovementMethod},
 * TextView 会在 {@link #onTouchEvent(MotionEvent)} 时将事件传递给 {@link QMUILinkTouchMovementMethod},
 * 然后传递给 {@link QMUITouchableSpan}, 实现点击态的变化和点击事件的响应。
 * </p>
 *
 * @author cginechen
 * @date 2017-03-20
 * @see QMUITouchableSpan
 * @see QMUILinkTouchMovementMethod
 */
public class QMUISpanTouchFixTextView extends TextView implements ISpanTouchFix {
    /**
     * 记录当前 Touch 事件对应的点是不是点在了 span 上面
     */
    private boolean mTouchSpanHit;

    /**
     * 记录每次真正传入的press，每次更改mTouchSpanHint，需要再调用一次setPressed，确保press状态正确
     */
    private boolean mIsPressedRecord = false;
    /**
     * TextView是否应该消耗事件
     */
    private boolean mNeedForceEventToParent = false;

    public QMUISpanTouchFixTextView(Context context) {
        this(context, null);
    }

    public QMUISpanTouchFixTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QMUISpanTouchFixTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setHighlightColor(Color.TRANSPARENT);
    }

    public void setNeedForceEventToParent(boolean needForceEventToParent) {
        mNeedForceEventToParent = needForceEventToParent;
        setFocusable(!needForceEventToParent);
        setClickable(!needForceEventToParent);
        setLongClickable(!needForceEventToParent);
    }

    /**
     * 使用者主动调用
     */
    public void setMovementMethodDefault(){
        setMovementMethodCompat(QMUILinkTouchMovementMethod.getInstance());
    }

    public void setMovementMethodCompat(MovementMethod movement){
        setMovementMethod(movement);
        if(mNeedForceEventToParent){
            setNeedForceEventToParent(true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!(getText() instanceof Spannable)) {
            return super.onTouchEvent(event);
        }
        mTouchSpanHit = true;
        // 调用super.onTouchEvent,会走到QMUILinkTouchMovementMethod
        // 会走到QMUILinkTouchMovementMethod#onTouchEvent会修改mTouchSpanHint
        boolean ret = super.onTouchEvent(event);
        if(mNeedForceEventToParent){
            return mTouchSpanHit;
        }
        return ret;
    }

    @Override
    public void setTouchSpanHit(boolean hit) {
        if (mTouchSpanHit != hit) {
            mTouchSpanHit = hit;
            setPressed(mIsPressedRecord);
        }
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean performClick() {
        if (!mTouchSpanHit && !mNeedForceEventToParent) {
            return super.performClick();
        }
        return false;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean performLongClick() {
        if (!mTouchSpanHit && !mNeedForceEventToParent) {
            return super.performLongClick();
        }
        return false;
    }

    @Override
    public final void setPressed(boolean pressed) {
        mIsPressedRecord = pressed;
        if (!mTouchSpanHit) {
            onSetPressed(pressed);
        }
    }

    protected void onSetPressed(boolean pressed) {
        super.setPressed(pressed);
    }
}
