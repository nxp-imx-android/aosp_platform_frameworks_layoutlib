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

import android.view.BridgeInflater;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Tracks Choreographer callbacks with corresponding RenderSessions (represented by BridgeContext)
 * and calls actionDisposer against the action that can be disposed.
 */
public class CallbacksDisposer {
    /**
     * An abstraction not to keep heavy session-related object like BridgeContext. Also,
     */
    public static class SessionKey {
        private final int bridgeContextHash;
        private final int classLoaderHash;

        public SessionKey(@NotNull BridgeContext bc) {
            bridgeContextHash = System.identityHashCode(bc);
            BridgeInflater currentInflater =
                    (BridgeInflater) bc.getSystemService(LAYOUT_INFLATER_SERVICE);
            classLoaderHash = System.identityHashCode(currentInflater.getComposeClassLoader());
        }

        @VisibleForTesting
        public SessionKey(int bridgeContextHash, int classLoaderHash) {
            this.bridgeContextHash = bridgeContextHash;
            this.classLoaderHash = classLoaderHash;
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
    // Tracking disposed sessions
    @NotNull private final Set<SessionKey> mDisposedSessions = new HashSet<>();

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
     */
    public void onCallbackRemoved(@NotNull SessionKey sessionKey, @NotNull Object action) {
        // The frame callback that is might have been added by another session. Thus, we do not
        // know which session it belongs to and have to try to remove from every session.
        for (WeakHashMap<Object, Object> actions : mFrameCallbacks.values()) {
            actions.remove(action);
        }
    }

    /**
     * Inform the disposer that the session is being disposed
     * @param sessionKey representing the session
     * @return true if the session can be fully disposed
     */
    public boolean onDispose(@NotNull SessionKey sessionKey) {
        mDisposedSessions.add(sessionKey);

        // Get all the session records for the current classloader
        Set<SessionKey> currentClassLoaderSessions =
                mFrameCallbacks
                        .keySet()
                        .stream()
                        .filter(r -> r.classLoaderHash == sessionKey.classLoaderHash)
                        .collect(Collectors.toSet());
        // If all the sessions for the current classloader are disposed, we can dispose all the
        // frame callbacks for the current class loader
        boolean disposeSession = false;
        if (mDisposedSessions.containsAll(currentClassLoaderSessions)) {
            for (SessionKey r: currentClassLoaderSessions) {
                WeakHashMap<Object, Object> actionSet = mFrameCallbacks.get(r);
                List<Object> actionSetCopy =
                        actionSet.keySet().stream().filter(Objects::nonNull).collect(Collectors.toList());
                for (Object action : actionSetCopy) {
                    mActionDisposer.accept(action);
                }
            }

            currentClassLoaderSessions.forEach(mFrameCallbacks::remove);

            disposeSession = true;
        }

        // If there are no frame callbacks for some disposed session because it is
        // 1) not compose
        // 2) compose but just not animated/interactive
        // 3) they have just been removed (current session was the last for the class loader)
        // We just remove the session record from the set as there is no need to keep it.
        mDisposedSessions.removeIf(s -> !mFrameCallbacks.containsKey(s));

        return disposeSession;
    }

    @VisibleForTesting
    public Set<SessionKey> getSessionsWithCallbacks() {
        return mFrameCallbacks.keySet();
    }

    @VisibleForTesting
    public Set<SessionKey> getDisposedSessions() {
        return mDisposedSessions;
    }
}
