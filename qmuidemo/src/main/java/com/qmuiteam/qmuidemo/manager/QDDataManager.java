package com.qmuiteam.qmuidemo.manager;

import com.qmuiteam.qmuidemo.base.BaseFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDBottomSheetFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDButtonFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDCollapsingTopBarLayoutFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDDialogFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDEmptyViewFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDFloatLayoutFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDGroupListViewFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDLayoutFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDLinkTextViewFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDPopupFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDPriorityLinearLayoutFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDProgressBarFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDPullRefreshFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDRadiusImageViewFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDSpanTouchFixTextViewFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDTabSegmentFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDTipDialogFragment;
import com.qmuiteam.qmuidemo.fragment.components.QDVerticalTextViewFragment;
import com.qmuiteam.qmuidemo.fragment.components.qqface.QDQQFaceFragment;
import com.qmuiteam.qmuidemo.fragment.components.viewpager.QDViewPagerFragment;
import com.qmuiteam.qmuidemo.fragment.lab.QDAnimationListViewFragment;
import com.qmuiteam.qmuidemo.fragment.lab.QDArchTestFragment;
import com.qmuiteam.qmuidemo.fragment.lab.QDSnapHelperFragment;
import com.qmuiteam.qmuidemo.fragment.util.QDColorHelperFragment;
import com.qmuiteam.qmuidemo.fragment.util.QDDeviceHelperFragment;
import com.qmuiteam.qmuidemo.fragment.util.QDDrawableHelperFragment;
import com.qmuiteam.qmuidemo.fragment.util.QDNotchHelperFragment;
import com.qmuiteam.qmuidemo.fragment.util.QDSpanFragment;
import com.qmuiteam.qmuidemo.fragment.util.QDStatusBarHelperFragment;
import com.qmuiteam.qmuidemo.fragment.util.QDViewHelperFragment;
import com.qmuiteam.qmuidemo.model.QDItemDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cginechen
 * @date 2016-10-21
 */

public class QDDataManager {
    private static QDDataManager _sInstance;
    private QDWidgetContainer mWidgetContainer;

    private List<Class<? extends BaseFragment>> mComponentsNames;
    private List<Class<? extends BaseFragment>> mUtilNames;
    private List<Class<? extends BaseFragment>> mLabNames;

    public QDDataManager() {
        mWidgetContainer = QDWidgetContainer.getInstance();
        initComponentsDesc();
        initUtilDesc();
        initLabDesc();
    }

    public static QDDataManager getInstance() {
        if (_sInstance == null) {
            _sInstance = new QDDataManager();
        }
        return _sInstance;
    }


    /**
     * Components
     */
    private void initComponentsDesc() {
        mComponentsNames = new ArrayList<>();
        mComponentsNames.add(QDButtonFragment.class);
        mComponentsNames.add(QDDialogFragment.class);
        mComponentsNames.add(QDFloatLayoutFragment.class);
        mComponentsNames.add(QDEmptyViewFragment.class);
        mComponentsNames.add(QDTabSegmentFragment.class);
        mComponentsNames.add(QDProgressBarFragment.class);
        mComponentsNames.add(QDBottomSheetFragment.class);
        mComponentsNames.add(QDGroupListViewFragment.class);
        mComponentsNames.add(QDTipDialogFragment.class);
        mComponentsNames.add(QDRadiusImageViewFragment.class);
        mComponentsNames.add(QDVerticalTextViewFragment.class);
        mComponentsNames.add(QDPullRefreshFragment.class);
        mComponentsNames.add(QDPopupFragment.class);
        mComponentsNames.add(QDSpanTouchFixTextViewFragment.class);
        mComponentsNames.add(QDLinkTextViewFragment.class);
        mComponentsNames.add(QDQQFaceFragment.class);
        mComponentsNames.add(QDSpanFragment.class);
        mComponentsNames.add(QDCollapsingTopBarLayoutFragment.class);
        mComponentsNames.add(QDViewPagerFragment.class);
        mComponentsNames.add(QDLayoutFragment.class);
        mComponentsNames.add(QDPriorityLinearLayoutFragment.class);
    }

    /**
     * Helper
     */
    private void initUtilDesc() {
        mUtilNames = new ArrayList<>();
        mUtilNames.add(QDColorHelperFragment.class);
        mUtilNames.add(QDDeviceHelperFragment.class);
        mUtilNames.add(QDDrawableHelperFragment.class);
        mUtilNames.add(QDStatusBarHelperFragment.class);
        mUtilNames.add(QDViewHelperFragment.class);
        mUtilNames.add(QDNotchHelperFragment.class);
    }

    /**
     * Lab
     */
    private void initLabDesc() {
        mLabNames = new ArrayList<>();
        mLabNames.add(QDAnimationListViewFragment.class);
        mLabNames.add(QDSnapHelperFragment.class);
        mLabNames.add(QDArchTestFragment.class);
    }

    public QDItemDescription getDescription(Class<? extends BaseFragment> cls) {
        return mWidgetContainer.get(cls);
    }

    public String getName(Class<? extends BaseFragment> cls) {
        QDItemDescription itemDescription = getDescription(cls);
        if (itemDescription == null) {
            return null;
        }
        return itemDescription.getName();
    }

    public List<QDItemDescription> getComponentsDescriptions() {
        List<QDItemDescription> list = new ArrayList<>();
        for (int i = 0; i < mComponentsNames.size(); i++) {
            list.add(mWidgetContainer.get(mComponentsNames.get(i)));
        }
        return list;
    }

    public List<QDItemDescription> getUtilDescriptions() {
        List<QDItemDescription> list = new ArrayList<>();
        for (int i = 0; i < mUtilNames.size(); i++) {
            list.add(mWidgetContainer.get(mUtilNames.get(i)));
        }
        return list;
    }

    public List<QDItemDescription> getLabDescriptions() {
        List<QDItemDescription> list = new ArrayList<>();
        for (int i = 0; i < mLabNames.size(); i++) {
            list.add(mWidgetContainer.get(mLabNames.get(i)));
        }
        return list;
    }
}
