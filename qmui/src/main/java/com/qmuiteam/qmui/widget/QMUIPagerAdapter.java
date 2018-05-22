package com.qmuiteam.qmui.widget;

import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

/**
 * @author cginechen
 * @date 2017-09-13
 */

public abstract class QMUIPagerAdapter extends PagerAdapter {
    private SparseArray<Object> mScrapItems = new SparseArray<>();

    public QMUIPagerAdapter() {
    }


    /**
     * Hydrating an object is taking an object that exists in memory,
     * that doesn't yet contain any domain data ("real" data),
     * and then populating it with domain data.
     */
    protected abstract Object hydrate(ViewGroup container, int position);

    protected abstract void populate(ViewGroup container, Object item, int position);

    protected abstract void destroy(ViewGroup container, int position, Object object);

    @Override
    public final Object instantiateItem(ViewGroup container, int position) {
        Object item = mScrapItems.get(position);
        if (item == null) {
            item = hydrate(container, position);
        } else {
            mScrapItems.remove(position);
        }
        populate(container, item, position);
        return item;
    }

    @Override
    public final void destroyItem(ViewGroup container, int position, Object object) {
        destroy(container, position, object);
        mScrapItems.put(position, object);
    }
}
