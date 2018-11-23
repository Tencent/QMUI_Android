package com.qmuiteam.qmui.widget.textview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

import com.qmuiteam.qmui.R;
import com.qmuiteam.qmui.link.QMUILinkTouchMovementMethod;
import com.qmuiteam.qmui.link.QMUILinkify;
import com.qmuiteam.qmui.span.QMUIOnSpanClickListener;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * 使 {@link android.widget.TextView} 能自动识别 URL、电话、邮箱地址。
 * 相比于 {@link android.widget.TextView} 使用 {@link android.text.util.Linkify},
 * {@link QMUILinkTextView} 有以下特点:
 * <ul>
 * <li>可以通过 {@link com.qmuiteam.qmui.R.styleable#QMUILinkTextView} 中的属性设置链接的样式</li>
 * <li>可以通过 {@link QMUILinkTextView#setOnLinkClickListener(QMUILinkTextView.OnLinkClickListener)} 设置链接的点击事件，
 * 而不是 {@link android.widget.TextView} 默认的 {@link android.content.Intent} 跳转</li>
 * </ul>
 *
 * @author cginechen
 * @date 2017-03-17
 */
public class QMUILinkTextView extends AppCompatTextView implements QMUIOnSpanClickListener, ISpanTouchFix {
    private static final String TAG = "LinkTextView";
    private static final int MSG_CHECK_DOUBLE_TAP_TIMEOUT = 1000;
    public static int AUTO_LINK_MASK_REQUIRED = QMUILinkify.PHONE_NUMBERS | QMUILinkify.EMAIL_ADDRESSES | QMUILinkify.WEB_URLS;
    private static Set<String> AUTO_LINK_SCHEME_INTERRUPTED = new HashSet<>();
    private CharSequence mOriginText = null;

    static {
        AUTO_LINK_SCHEME_INTERRUPTED.add("tel");
        AUTO_LINK_SCHEME_INTERRUPTED.add("mailto");
        AUTO_LINK_SCHEME_INTERRUPTED.add("http");
        AUTO_LINK_SCHEME_INTERRUPTED.add("https");
    }

    /**
     * 链接文字颜色
     */
    private ColorStateList mLinkTextColor;
    /**
     * 链接背景颜色
     */
    private ColorStateList mLinkBgColor;

    /**
     * 是否允许显示链接下划线
     */
    private boolean mAllowUnderLine = true;

    private int mAutoLinkMaskCompat;
    private OnLinkClickListener mOnLinkClickListener;
    private OnLinkLongClickListener mOnLinkLongClickListener;
    private boolean mNeedForceEventToParent = false;
    /**
     * 是否允许链接，号码等，设置最大字数限制，超过显示getMoreString()
     * 还需要设置 maxLength 属性显示最大字数才能一起生效
     */
    private boolean mEllipsizeEnable = false;
    /**
     * 记录当前 Touch 事件对应的点是不是点在了 span 上面
     */
    private boolean mTouchSpanHit;

    private long mDownMillis = 0;
    private static final long TAP_TIMEOUT = 200; // ViewConfiguration.getTapTimeout();
    private static final long DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();

    public QMUILinkTextView(Context context) {
        this(context, null);
        mLinkBgColor = null;
        mLinkTextColor = ContextCompat.getColorStateList(context, R.color.qmui_s_link_color);
    }

    public QMUILinkTextView(Context context, ColorStateList linkTextColor, ColorStateList linkBgColor) {
        this(context, null);
        mLinkBgColor = linkBgColor;
        mLinkTextColor = linkTextColor;
    }

    public QMUILinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAutoLinkMaskCompat = getAutoLinkMask() | AUTO_LINK_MASK_REQUIRED;
        setAutoLinkMask(0);
        setMovementMethod(QMUILinkTouchMovementMethod.getInstance());
        setHighlightColor(Color.TRANSPARENT);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.QMUILinkTextView);
        mLinkBgColor = array.getColorStateList(R.styleable.QMUILinkTextView_qmui_linkBackgroundColor);
        mLinkTextColor = array.getColorStateList(R.styleable.QMUILinkTextView_qmui_linkTextColor);
        mAllowUnderLine = array.getBoolean(R.styleable.QMUILinkTextView_qmui_allowUnderLine, mAllowUnderLine);
        mEllipsizeEnable = array.getBoolean(R.styleable.QMUILinkTextView_qmui_ellipsizeEnable, mEllipsizeEnable);
        array.recycle();
        if (mOriginText != null) {
            setText(mOriginText);
        }
    }

    public void setOnLinkClickListener(OnLinkClickListener onLinkClickListener) {
        mOnLinkClickListener = onLinkClickListener;
    }

    public void setOnLinkLongClickListener(OnLinkLongClickListener onLinkLongClickListener) {
        mOnLinkLongClickListener = onLinkLongClickListener;
    }

    public int getAutoLinkMaskCompat() {
        return mAutoLinkMaskCompat;
    }

    public void setAutoLinkMaskCompat(int mask) {
        mAutoLinkMaskCompat = mask;
    }

    public void addAutoLinkMaskCompat(int mask) {
        mAutoLinkMaskCompat |= mask;
    }

    public void removeAutoLinkMaskCompat(int mask) {
        mAutoLinkMaskCompat &= ~mask;
    }

    /**
     * 是否强制把TextView的事件强制传递给父元素。TextView在有ClickSpan的情况下默认会消耗掉事件
     *
     * @param needForceEventToParent true 为强制把TextView的事件强制传递给父元素，false 则不传递
     */
    public void setNeedForceEventToParent(boolean needForceEventToParent) {
        if (mNeedForceEventToParent != needForceEventToParent) {
            mNeedForceEventToParent = needForceEventToParent;
            if (mOriginText != null) {
                setText(mOriginText);
            }
        }
    }

    public void setLinkColor(ColorStateList linkTextColor) {
        mLinkTextColor = linkTextColor;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        mOriginText = text;
        int mMax = Integer.MAX_VALUE;
        if (!TextUtils.isEmpty(text)) {
            if (mEllipsizeEnable) {
                // 还必须设置maxLength
                InputFilter[] filters = getFilters();
                for (InputFilter filter : filters) {
                    if (filter instanceof InputFilter.LengthFilter) {
                        mMax = (int)getFieldValue(filter, "mMax");
                    }
                }

                int offset = getMoreString().length();
                if (text.length() > mMax) {
                    text = text.subSequence(0, mMax - offset) + getMoreString();
                }
            }

            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            QMUILinkify.addLinks(builder, mOriginText, mMax, mAutoLinkMaskCompat, mLinkTextColor, mLinkBgColor, mAllowUnderLine, this);
            text = builder;
        }

        super.setText(text, type);
        if (mNeedForceEventToParent && getLinksClickable()) {
            setFocusable(false);
            setClickable(false);
            setLongClickable(false);
        }
    }

    @Override
    public boolean onSpanClick(String text) {
        if (null == text) {
            Log.w(TAG, "onSpanClick interrupt null text");
            return true;
        }
        long clickUpTime = (SystemClock.uptimeMillis() - mDownMillis);
        Log.w(TAG, "onSpanClick clickUpTime: " + clickUpTime);
        if (mSingleTapConfirmedHandler.hasMessages(MSG_CHECK_DOUBLE_TAP_TIMEOUT)) {
            disallowOnSpanClickInterrupt();
            return true;
        }

        if (TAP_TIMEOUT < clickUpTime) {
            Log.w(TAG, "onSpanClick interrupted because of TAP_TIMEOUT: " + clickUpTime);
            return true;
        }

        String scheme = Uri.parse(text).getScheme();
        if (scheme != null) {
            scheme = scheme.toLowerCase();
        }

        if (AUTO_LINK_SCHEME_INTERRUPTED.contains(scheme)) {
            long waitTime = DOUBLE_TAP_TIMEOUT - clickUpTime;
            mSingleTapConfirmedHandler.removeMessages(MSG_CHECK_DOUBLE_TAP_TIMEOUT);
            Message msg = Message.obtain();
            msg.what = MSG_CHECK_DOUBLE_TAP_TIMEOUT;
            msg.obj = text;
            mSingleTapConfirmedHandler.sendMessageDelayed(msg, waitTime);
            return true;
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                boolean hasSingleTap = mSingleTapConfirmedHandler.hasMessages(MSG_CHECK_DOUBLE_TAP_TIMEOUT);
                Log.w(TAG, "onTouchEvent hasSingleTap: " + hasSingleTap);
                if (!hasSingleTap) {
                    mDownMillis = SystemClock.uptimeMillis();
                } else {
                    Log.w(TAG, "onTouchEvent disallow onSpanClick mSingleTapConfirmedHandler because of DOUBLE TAP");
                    disallowOnSpanClickInterrupt();
                }
                break;
        }
        boolean ret = super.onTouchEvent(event);
        if (mNeedForceEventToParent) {
            return mTouchSpanHit;
        }
        return ret;
    }

    private void disallowOnSpanClickInterrupt() {
        mSingleTapConfirmedHandler.removeMessages(MSG_CHECK_DOUBLE_TAP_TIMEOUT);
        mDownMillis = 0;
    }

    protected boolean performSpanLongClick(String text) {
        if (mOnLinkLongClickListener != null) {
            mOnLinkLongClickListener.onLongClick(text);
            return true;
        }
        return false;
    }

    @Override
    public boolean performClick() {
        if (!mTouchSpanHit && !mNeedForceEventToParent) {
            return super.performClick();
        }
        return false;
    }

    @Override
    public boolean performLongClick() {
        int end = getSelectionEnd();

        if (end > 0) {

            String selectStr = getText().subSequence(getSelectionStart(), end).toString();
            return performSpanLongClick(selectStr) || super.performLongClick();

        }
        return super.performLongClick();
    }

    private Handler mSingleTapConfirmedHandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(android.os.Message msg) {
            if (MSG_CHECK_DOUBLE_TAP_TIMEOUT != msg.what) {
                return;
            }

            Log.d(TAG, "handleMessage: " + msg.obj);
            if (msg.obj instanceof String) {
                String url = (String) msg.obj;
                if (null != mOnLinkClickListener && !TextUtils.isEmpty(url)) {
                    String schemeUrl = url.toLowerCase();
                    if (schemeUrl.startsWith("tel:")) {
                        String phoneNumber = Uri.parse(url).getSchemeSpecificPart();
                        mOnLinkClickListener.onTelLinkClick(phoneNumber);
                    } else if (schemeUrl.startsWith("mailto:")) {
                        String mailAddr = Uri.parse(url).getSchemeSpecificPart();
                        mOnLinkClickListener.onMailLinkClick(mailAddr);
                    } else if (schemeUrl.startsWith("http") || schemeUrl.startsWith("https")) {
                        mOnLinkClickListener.onWebUrlLinkClick(url);
                    }
                }
            }
        }

    };

    @Override
    public void setTouchSpanHit(boolean hit) {
        if (mTouchSpanHit != hit) {
            mTouchSpanHit = hit;
        }
    }

    public interface OnLinkClickListener {

        /**
         * 电话号码被点击
         */
        void onTelLinkClick(String phoneNumber);

        /**
         * 邮箱地址被点击
         */
        void onMailLinkClick(String mailAddress);

        /**
         * URL 被点击
         */
        void onWebUrlLinkClick(String url);

    }

    public interface OnLinkLongClickListener {
        void onLongClick(String text);
    }
    public void setEllipsizeEnable(boolean ellipsizeEnable) {
        this.mEllipsizeEnable = ellipsizeEnable;
    }

    @NonNull
    private String getMoreString() {
        return "...";
    }

    /**
     * 通过反射, 获取obj对象的 指定成员变量的值
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || TextUtils.isEmpty(fieldName)) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception e) {
                Log.e("tag","错误:" + e.getMessage());
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
