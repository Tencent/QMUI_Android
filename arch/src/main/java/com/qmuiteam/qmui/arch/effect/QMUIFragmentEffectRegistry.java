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


package com.qmuiteam.qmui.arch.effect;

import android.util.ArraySet;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;

import com.qmuiteam.qmui.QMUIConfig;
import com.qmuiteam.qmui.QMUILog;
import com.qmuiteam.qmui.arch.QMUIFragment;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class QMUIFragmentEffectRegistry extends ViewModel {

    class PendingRegister<T extends Effect> implements QMUIFragmentEffectRegistration{
        final LifecycleOwner lifecycleOwner;
        final QMUIFragmentEffectHandler<T> effectHandler;
        private QMUIFragmentEffectRegistration registration;


        public PendingRegister(LifecycleOwner lifecycleOwner, QMUIFragmentEffectHandler<T> effectHandler){
            this.lifecycleOwner = lifecycleOwner;
            this.effectHandler = effectHandler;
        }

        public void doRegister(){
            if(lifecycleOwner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.DESTROYED)){
                return;
            }
            registration = register(lifecycleOwner, effectHandler);
        }

        @Override
        public void unregister() {
            if(registration != null){
                registration.unregister();
            }
        }
    }

    private static final String TAG = "FragmentEffectRegistry";

    private final AtomicInteger mNextRc = new AtomicInteger(0);

    private final transient Map<Integer, EffectHandlerWrapper<?>> mKeyToHandler = new HashMap<>();
    private transient int mNotifyEffectRunning = 0;
    private final transient Set<Integer> mPendingRemoveKeys = new HashSet<>();
    private final transient List<PendingRegister<?>> mPendingRegister = new ArrayList<>();


    /**
     * Register a new handler with this registry.
     *
     * This is normally called by a higher level convenience methods like
     * {@link QMUIFragment#registerEffect}.
     *
     * @param lifecycleOwner a {@link LifecycleOwner} that makes this call.
     * @param effectHandler the handler to handle effect
     *
     * @return a FragmentEffectRegistration that can be used to unregister an FragmentEffectHandler.
     */
    public <T extends Effect> QMUIFragmentEffectRegistration register(
            @NonNull final LifecycleOwner lifecycleOwner,
            @NonNull final QMUIFragmentEffectHandler<T> effectHandler) {
        if(mNotifyEffectRunning > 0){
            PendingRegister<T> pendingRegister = new PendingRegister<>(lifecycleOwner, effectHandler);
            mPendingRegister.add(pendingRegister);
            return pendingRegister;
        }

        final int rc = mNextRc.getAndIncrement();
        Lifecycle lifecycle = lifecycleOwner.getLifecycle();
        mKeyToHandler.put(rc, new EffectHandlerWrapper<T>(effectHandler, lifecycle));
        lifecycle.addObserver((LifecycleEventObserver) (lifecycleOwner1, event) -> {
            if (Lifecycle.Event.ON_DESTROY.equals(event)) {
                unregister(rc);
            }
        });

        return () -> QMUIFragmentEffectRegistry.this.unregister(rc);
    }

    /**
     * Unregister a handler previously registered with {@link #register}. This shouldn't be
     * called directly, but instead through {@link QMUIFragmentEffectRegistration#unregister()}.
     *
     * @param key the unique key used when registering a callback.
     */
    @MainThread
    final void unregister(int key) {
        if(mNotifyEffectRunning > 0){
            mPendingRemoveKeys.add(key);
            return;
        }
        safeUnregister(key);
    }

    private void safeUnregister(int key){
        EffectHandlerWrapper<?> effectHandlerWrapper = mKeyToHandler.remove(key);
        if (effectHandlerWrapper != null) {
            effectHandlerWrapper.cancel();
        }
    }

    /**
     * notify the effect to handlers registered with {@link #register}.
     *
     * This is normally called by a higher level convenience methods like
     * {@link QMUIFragment#notifyEffect}
     * @param effect
     */
    public <T extends Effect> void notifyEffect(T effect) {
        mNotifyEffectRunning++;
        for (Integer key : mKeyToHandler.keySet()) {
            EffectHandlerWrapper<?> wrapper = mKeyToHandler.get(key);
            if (wrapper != null && wrapper.shouldHandleEffect(effect)) {
                wrapper.pushOrHandleEffect(effect);
            }
        }
        mNotifyEffectRunning--;
        if(mNotifyEffectRunning == 0){
            if(!mPendingRemoveKeys.isEmpty()){
                for(Integer key: mPendingRemoveKeys){
                    safeUnregister(key);
                }
                mPendingRemoveKeys.clear();
            }
            if(!mPendingRegister.isEmpty()){
                for(PendingRegister<?> register: mPendingRegister){
                    register.doRegister();
                }
                mPendingRegister.clear();
            }
        }
    }

    private static class EffectHandlerWrapper<T extends Effect> implements LifecycleEventObserver {
        final QMUIFragmentEffectHandler<T> mHandler;
        final Lifecycle mLifecycle;
        ArrayList<T> mEffects = null;
        final Class<? extends Effect> mEffectType;

        EffectHandlerWrapper(QMUIFragmentEffectHandler<T> handler, Lifecycle lifecycle) {
            mHandler = handler;
            mLifecycle = lifecycle;
            lifecycle.addObserver(this);
            mEffectType = getHandlerEffectType(handler);
        }

        @SuppressWarnings("unchecked")
        private Class<? extends Effect> getHandlerEffectType(QMUIFragmentEffectHandler handler) {
            Class<? extends Effect> effectClz = null;
            try {
                Class<?> handlerCls = handler.getClass();
                while (handlerCls != null && handlerCls.getSuperclass() != QMUIFragmentEffectHandler.class) {
                    handlerCls = handlerCls.getSuperclass();
                }
                if (handlerCls != null) {
                    Type type = handlerCls.getGenericSuperclass();
                    if (type instanceof ParameterizedType) {
                        Type[] params = ((ParameterizedType) type).getActualTypeArguments();
                        if (params.length > 0) {
                            effectClz = (Class<? extends Effect>) params[0];
                        }
                    }
                }
            } catch (Throwable ignore) {

            }

            if (effectClz == null) {
                if (QMUIConfig.DEBUG) {
                    throw new RuntimeException("Error to get FragmentEffectHandler's generic parameter type");
                } else {
                    QMUILog.d(TAG, "Error to get FragmentEffectHandler's generic parameter type");
                }
            }

            return effectClz;
        }

        @SuppressWarnings("unchecked")
        boolean shouldHandleEffect(Effect effect) {
            return mEffectType != null && mEffectType.isAssignableFrom(effect.getClass()) && mHandler.shouldHandleEffect((T) effect);
        }

        @MainThread
        @SuppressWarnings("unchecked")
        void pushOrHandleEffect(Effect effect) {
            QMUIFragmentEffectHandler.HandlePolicy policy = mHandler.provideHandlePolicy();
            if (policy == QMUIFragmentEffectHandler.HandlePolicy.Immediately ||
                    (policy == QMUIFragmentEffectHandler.HandlePolicy.ImmediatelyIfStarted &&
                            mLifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED))) {
                mHandler.handleEffect((T) effect);
                return;
            }

            if (mEffects == null) {
                mEffects = new ArrayList<>();
            }
            mEffects.add((T) effect);
        }

        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            if (event == Lifecycle.Event.ON_START) {
                if (mEffects != null && !mEffects.isEmpty()) {
                    List<T> effects = mEffects;
                    mEffects = null;
                    if (effects.size() == 1) {
                        mHandler.handleEffect(effects.get(0));
                    } else {
                        mHandler.handleEffect(effects);
                    }
                }
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                cancel();
            }
        }

        void cancel() {
            mLifecycle.removeObserver(this);
            mEffects = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        for (Integer key : mKeyToHandler.keySet()) {
            EffectHandlerWrapper effectHandlerWrapper = mKeyToHandler.get(key);
            if (effectHandlerWrapper != null) {
                effectHandlerWrapper.cancel();
            }
        }
        mKeyToHandler.clear();
    }
}
