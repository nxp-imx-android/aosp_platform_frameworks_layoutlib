/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.layoutlib.bridge.util;

import com.android.layoutlib.bridge.android.BridgeContext;
import com.android.tools.layoutlib.annotations.NotNull;
import com.android.tools.layoutlib.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * Tracks Choreographer callbacks with corresponding RenderSessions (represented by BridgeContext)
 * and calls actionDisposer against the action that can be disposed.
 */
public class CallbacksDisposer {
    /**
     * An abstraction not to keep heavy session-related object like BridgeContext.
     */
    public static class SessionKey {
        private final int bridgeContextHash;

        public SessionKey(@NotNull BridgeContext bc) {
            bridgeContextHash = System.identityHashCode(bc);
        }

        @VisibleForTesting
        public SessionKey(int bridgeContextHash) {
            this.bridgeContextHash = bridgeContextHash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SessionKey)) {
                return false;
            }

            return this.bridgeContextHash == ((SessionKey) obj).bridgeContextHash;
        }

        @Override
        public int hashCode() {
            return bridgeContextHash;
        }
    }

    @NotNull private final Consumer<Object> mActionDisposer;
    // Tracking actions that should be removed from Choreographer to prevent memory leaks. Using
    // WeakHashMap as WeakHashSet. If a key (action) gets garbage collected we do not need to do
    // anything since it is not referenced by the Choreographer.
    @NotNull private final Map<SessionKey, WeakHashMap<Object, Object>> mFrameCallbacks =
            new HashMap<>();

    /**
     * Constructs disposer
     * @param actionDisposer a function to be called against the action that should be disposed
     */
    public CallbacksDisposer(@NotNull Consumer<Object> actionDisposer) {
        mActionDisposer = actionDisposer;
    }

    /**
     * Inform disposer that an action was added during the session
     * @param sessionKey representing the session
     * @param action callback that was added
     */
    public void onCallbackAdded(@NotNull SessionKey sessionKey, @NotNull Object action) {
        mFrameCallbacks.computeIfAbsent(sessionKey, r -> new WeakHashMap<>()).put(action, null);
    }

    /**
     * Inform disposer that an action was removed during the session
     * @param sessionKey representing the session
     * @param action callback that was removed
     * @return true if the callback for the session was stored and removed, false otherwise
     */
    public boolean onCallbackRemoved(@NotNull SessionKey sessionKey, @NotNull Object action) {
        WeakHashMap<Object, Object> sessionCallbacks = mFrameCallbacks.get(sessionKey);
        if (sessionCallbacks == null) {
            return false;
        }
        return sessionCallbacks.remove(action, null);
    }

    /**
     * Inform the disposer that the session is being disposed
     * @param sessionKey representing the session
     */
    public void onDispose(@NotNull SessionKey sessionKey) {
        WeakHashMap<Object, Object> actionSet = mFrameCallbacks.remove(sessionKey);
        if (actionSet != null) {
            for (Object action : actionSet.keySet()) {
                mActionDisposer.accept(action);
            }
        }
    }

    @VisibleForTesting
    public Set<SessionKey> getSessionsWithCallbacks() {
        return mFrameCallbacks.keySet();
    }
}
