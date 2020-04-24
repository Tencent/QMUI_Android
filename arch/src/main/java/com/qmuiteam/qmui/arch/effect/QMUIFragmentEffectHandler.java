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

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import java.util.List;

public abstract class QMUIFragmentEffectHandler<T extends Effect> {

    /**
     * handle the effect immediately without lifeCycle check. You can override this to true to
     * pre load the data from db or network. But you should be careful with the UI rendering.
     * @return true to handle the effect immediately.
     */
    public boolean handleImmediately() {
        return false;
    }

    /**
     * determine whether we need handle the effect or not.
     * @param effect the effect to check
     * @return true if we need handle the effect
     */
    public abstract boolean shouldHandleEffect(@NonNull T effect);

    /**
     * the time to handle effect depends on {@link #handleImmediately()}.
     * if {@link #handleImmediately()} return true，handle the effect immediately.
     * if {@link #handleImmediately()} return false, handle the effect after {@link Lifecycle.State#STARTED}
     * @param effect
     */
    public abstract void handleEffect(@NonNull T effect);

    /**
     * if {@link #handleImmediately()} return false, we may need handle more than one effects.
     * then you may want merge them or only handle the newest.
     * @param effects
     */
    public void handleEffect(@NonNull List<T> effects) {
        for (T effect : effects) {
            handleEffect(effect);
        }
    }
}
