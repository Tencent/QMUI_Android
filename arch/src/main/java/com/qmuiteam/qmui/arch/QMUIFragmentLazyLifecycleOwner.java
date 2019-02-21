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

package com.qmuiteam.qmui.arch;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.OnLifecycleEvent;

public class QMUIFragmentLazyLifecycleOwner implements LifecycleOwner, LifecycleObserver {

    private LifecycleRegistry mLifecycleRegistry = null;
    private boolean mIsViewVisible = true;
    private Lifecycle.State mViewState = Lifecycle.State.INITIALIZED;
    private Callback mCallback;

    public QMUIFragmentLazyLifecycleOwner(@NonNull Callback callback){
        mCallback = callback;
    }

    /**
     * Initializes the underlying Lifecycle if it hasn't already been created.
     */
    void initialize() {
        if (mLifecycleRegistry == null) {
            mLifecycleRegistry = new LifecycleRegistry(this);
        }
    }

    void setViewVisible(boolean viewVisible) {
        if(mViewState.compareTo(Lifecycle.State.CREATED) < 0 || !isInitialized()){
            // not trust it before onCreate
            return;
        }
        mIsViewVisible = viewVisible;
        if (viewVisible) {
            mLifecycleRegistry.markState(mViewState);
        } else {
            if (mViewState.compareTo(Lifecycle.State.CREATED) > 0) {
                mLifecycleRegistry.markState(Lifecycle.State.CREATED);
            } else {
                mLifecycleRegistry.markState(mViewState);
            }
        }
    }

    /**
     * @return True if the Lifecycle has been initialized.
     */
    boolean isInitialized() {
        return mLifecycleRegistry != null;
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        initialize();
        return mLifecycleRegistry;
    }

    private void handleLifecycleEvent(@NonNull Lifecycle.Event event) {
        initialize();
        mLifecycleRegistry.handleLifecycleEvent(event);
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void onCreate(LifecycleOwner owner) {
        mIsViewVisible = mCallback.isVisibleToUser();
        mViewState = Lifecycle.State.CREATED;
        handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart(LifecycleOwner owner) {
        mViewState = Lifecycle.State.STARTED;
        if (mIsViewVisible) {
            handleLifecycleEvent(Lifecycle.Event.ON_START);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume(LifecycleOwner owner) {
        mViewState = Lifecycle.State.RESUMED;
        if (mIsViewVisible && mLifecycleRegistry.getCurrentState() == Lifecycle.State.STARTED) {
            handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause(LifecycleOwner owner) {
        mViewState = Lifecycle.State.STARTED;
        if (mLifecycleRegistry.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onStop(LifecycleOwner owner) {
        mViewState = Lifecycle.State.CREATED;
        if (mLifecycleRegistry.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            handleLifecycleEvent(Lifecycle.Event.ON_STOP);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy(LifecycleOwner owner) {
        mViewState = Lifecycle.State.DESTROYED;
        handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }

    interface Callback {
        boolean isVisibleToUser();
    }
}