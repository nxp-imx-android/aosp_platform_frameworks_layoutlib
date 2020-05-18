/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.view;

import com.android.ide.common.rendering.api.LayoutLog;
import com.android.layoutlib.bridge.Bridge;
import com.android.layoutlib.bridge.android.BridgeContext;
import com.android.layoutlib.bridge.impl.RenderAction;
import com.android.tools.layoutlib.annotations.LayoutlibDelegate;

import android.view.Choreographer.FrameCallback;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Delegate used to provide new implementation of a select few methods of {@link Choreographer}
 *
 * Through the layoutlib_create tool, the original  methods of Choreographer have been
 * replaced by calls to methods of the same name in this delegate class.
 *
 */
public class Choreographer_Delegate {
    static final WeakHashMap<BridgeContext, Set<Object>> sFrameCallbacks = new WeakHashMap<>();

    @LayoutlibDelegate
    public static float getRefreshRate() {
        return 60.f;
    }

    @LayoutlibDelegate
    public static void postCallbackDelayedInternal(
            Choreographer thiz, int callbackType, Object action, Object token, long delayMillis) {
        if (callbackType == Choreographer.CALLBACK_ANIMATION) {
            sFrameCallbacks.computeIfAbsent(currentContext(), c -> new HashSet<>()).add(action);
        }
        thiz.postCallbackDelayedInternal_Original(callbackType, action, token, delayMillis);
    }

    @LayoutlibDelegate
    public static void removeCallbacksInternal(
            Choreographer thiz, int callbackType, Object action, Object token) {
        if (callbackType == Choreographer.CALLBACK_ANIMATION) {
            Set<Object> actionSet = sFrameCallbacks.get(currentContext());
            if (actionSet != null) {
                actionSet.remove(action);
            }
        }
        thiz.removeCallbacksInternal_Original(callbackType, action, token);
    }

    public static void dispose(BridgeContext bridgeContext) {
        Set<Object> actionSet = sFrameCallbacks.get(bridgeContext);
        if (actionSet != null) {
            // In theory we could modify actionSet during the following removeFrameCallback call.
            Set<Object> actionSetCopy = new HashSet<>(actionSet);
            for (Object action: actionSetCopy) {
                if (action instanceof FrameCallback) {
                    FrameCallback callback = (FrameCallback)action;
                    Choreographer.getInstance().removeFrameCallback(callback);
                } else if (action instanceof Runnable) {
                    Runnable runnable = (Runnable)action;
                    Choreographer.getInstance().removeCallbacksInternal_Original(
                            Choreographer.CALLBACK_ANIMATION, runnable, null);
                } else {
                    Bridge.getLog().error(LayoutLog.TAG_BROKEN, "Unexpected action as " +
                            "ANIMATION_CALLBACK", (Object)null, null);
                }
            }
            sFrameCallbacks.remove(bridgeContext);
        }
    }

    static BridgeContext currentContext() {
        return RenderAction.getCurrentContext();
    }
}
