package com.qmuiteam.qmuidemo.richNest;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class QMUINestedBottomRecyclerView extends RecyclerView implements IQMUINestedBottomView {
    public QMUINestedBottomRecyclerView(@NonNull Context context) {
        super(context);
    }

    public QMUINestedBottomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public QMUINestedBottomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public int consumeScroll(int yUnconsumed) {
        scrollBy(0, yUnconsumed);
        return 0;
    }
}
