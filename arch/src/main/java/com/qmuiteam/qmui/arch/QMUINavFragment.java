package com.qmuiteam.qmui.arch;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelStoreOwner;

import com.qmuiteam.qmui.QMUILog;

public class QMUINavFragment extends QMUIFragment implements QMUIFragmentContainerProvider {
    private static final String TAG = "QMUINavFragment";
    private static final String QMUI_ARGUMENT_DST_FRAGMENT = "qmui_argument_dst_fragment";
    private static final String QMUI_ARGUMENT_FRAGMENT_ARG = "qmui_argument_fragment_arg";
    private FragmentContainerView mContainerView;
    private boolean mIsFirstFragmentAdded = false;

    public static QMUINavFragment getDefaultInstance(Class<? extends QMUIFragment> firstFragmentCls,
                                                     @Nullable Bundle firstFragmentArgument){
        QMUINavFragment navFragment = new QMUINavFragment();
        Bundle arg = new Bundle();
        arg.putString(QMUI_ARGUMENT_DST_FRAGMENT, firstFragmentCls.getName());
        arg.putBundle(QMUI_ARGUMENT_FRAGMENT_ARG, firstFragmentArgument);
        navFragment.setArguments(initArguments(firstFragmentCls, firstFragmentArgument));
        return navFragment;
    }

    public static Bundle initArguments(Class<? extends QMUIFragment> firstFragmentCls,
                                           @Nullable Bundle firstFragmentArgument){
        Bundle arg = new Bundle();
        arg.putString(QMUI_ARGUMENT_DST_FRAGMENT, firstFragmentCls.getName());
        arg.putBundle(QMUI_ARGUMENT_FRAGMENT_ARG, firstFragmentArgument);
        return arg;
    }

    static Bundle initArguments(String firstFragmentClsName, @Nullable Bundle firstFragmentArgument){
        Bundle arg = new Bundle();
        arg.putString(QMUI_ARGUMENT_DST_FRAGMENT, firstFragmentClsName);
        arg.putBundle(QMUI_ARGUMENT_FRAGMENT_ARG, firstFragmentArgument);
        return arg;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        getChildFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                getParentFragmentManager().beginTransaction()
                        .setPrimaryNavigationFragment(getChildFragmentManager().getBackStackEntryCount() > 1 ? QMUINavFragment.this : null)
                        .commit();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            onCreateFirstFragment();
        }
    }

    public boolean isFirstFragmentAdded() {
        return mIsFirstFragmentAdded;
    }

    protected void setFirstFragmentAdded(boolean firstFragmentAdded) {
        mIsFirstFragmentAdded = firstFragmentAdded;
    }

    protected void onCreateFirstFragment(){
        Bundle arguments = getArguments();
        if (arguments != null) {
            String dstFragmentName = arguments.getString(QMUI_ARGUMENT_DST_FRAGMENT);
            QMUIFragment firstFragment = instantiationFirstFragment(dstFragmentName, arguments);
            if (firstFragment != null) {
                mIsFirstFragmentAdded = true;
                getChildFragmentManager()
                        .beginTransaction()
                        .add(getContextViewId(), firstFragment, firstFragment.getClass().getSimpleName())
                        .addToBackStack(firstFragment.getClass().getSimpleName())
                        .commit();
            }
        }
    }


    @SuppressWarnings("unchecked")
    private QMUIFragment instantiationFirstFragment(String clsName, Bundle arguments) {
        try {
            Class<? extends QMUIFragment> cls = (Class<? extends QMUIFragment>) Class.forName(clsName);
            QMUIFragment fragment = cls.newInstance();
            Bundle args = arguments.getBundle(QMUI_ARGUMENT_FRAGMENT_ARG);
            if (args != null) {
                fragment.setArguments(args);
            }
            return fragment;
        } catch (IllegalAccessException e) {
            QMUILog.d(TAG, "Can not access " + clsName + " for first fragment");
        } catch (java.lang.InstantiationException e) {
            QMUILog.d(TAG, "Can not instance " + clsName + " for first fragment");
        } catch (ClassNotFoundException e) {
            QMUILog.d(TAG, "Can not find " + clsName);
        }
        return null;
    }

    @Override
    protected View onCreateView() {
        setContainerView(new FragmentContainerView(getContext()));
        return mContainerView;
    }

    @Override
    protected void onViewCreated(@NonNull View rootView) {
        if(mContainerView == null){
            throw new RuntimeException("must call #setContainerView() in onCreateView()");
        }
    }

    protected void setContainerView(FragmentContainerView fragmentContainerView){
        mContainerView = fragmentContainerView;
        mContainerView.setId(getContextViewId());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContainerView = null;
    }

    @Override
    public int getContextViewId() {
        return R.id.qmui_activity_fragment_container_id;
    }

    @Override
    public FragmentManager getContainerFragmentManager() {
        return getChildFragmentManager();
    }

    @Override
    public ViewModelStoreOwner getContainerViewModelStoreOwner() {
        return this;
    }

    @Nullable
    @Override
    public FragmentContainerView getFragmentContainerView() {
        return mContainerView;
    }
}
