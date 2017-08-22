package com.qmuiteam.qmui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * 可以监听滚动事件的 {@link ScrollView}，并能在滚动回调中获取每次滚动前后的偏移量。
 * <p>
 * 由于 {@link ScrollView} 没有类似于 addOnScrollChangedListener 的方法可以监听滚动事件，所以需要通过重写 {@link android.view.View#onScrollChanged}，来触发滚动监听
 *
 * @author chantchen
 * @date 2015-08-25
 */
public class QMUIObservableScrollView extends ScrollView {

    private List<OnScrollChangedListener> mOnScrollChangedListeners;

    public QMUIObservableScrollView(Context context) {
        super(context);
    }

    public QMUIObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QMUIObservableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
        if (mOnScrollChangedListeners == null) {
            mOnScrollChangedListeners = new ArrayList<>();
        }
        if (mOnScrollChangedListeners.contains(onScrollChangedListener)) {
            return;
        }
        mOnScrollChangedListeners.add(onScrollChangedListener);
    }

    public void removeOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
        if (mOnScrollChangedListeners == null) {
            return;
        }
        mOnScrollChangedListeners.remove(onScrollChangedListener);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListeners != null && !mOnScrollChangedListeners.isEmpty()) {
            for (OnScrollChangedListener listener : mOnScrollChangedListeners) {
                listener.onScrollChanged(this, l, t, oldl, oldt);
            }
        }
    }

    public interface OnScrollChangedListener {
        void onScrollChanged(QMUIObservableScrollView scrollView, int l, int t, int oldl, int oldt);
    }

}
