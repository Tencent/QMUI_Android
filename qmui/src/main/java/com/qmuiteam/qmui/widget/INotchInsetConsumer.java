package com.qmuiteam.qmui.widget;

public interface INotchInsetConsumer {
    /**
     *
     * @return if true stop dispatch to child view
     */
    boolean notifyInsetMaybeChanged();
}