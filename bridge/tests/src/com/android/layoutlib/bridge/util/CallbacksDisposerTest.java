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

import com.android.layoutlib.bridge.util.CallbacksDisposer.SessionKey;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CallbacksDisposerTest {
    @Test
    public void singleSession() {
        Set<Object> callbacks = new HashSet<>();
        CallbacksDisposer disposer = new CallbacksDisposer(callbacks::remove);

        SessionKey session1 = new SessionKey(1);

        Object action1 = new Object();
        Object action2 = new Object();

        callbacks.add(action2);

        disposer.onCallbackAdded(session1, action1);
        disposer.onCallbackAdded(session1, action2);

        assertEquals(1, disposer.getSessionsWithCallbacks().size());
        assertTrue(disposer.getSessionsWithCallbacks().contains(session1));
        assertTrue(callbacks.contains(action2));
        assertEquals(1, callbacks.size());

        disposer.onCallbackRemoved(session1, action1);

        assertEquals(1, disposer.getSessionsWithCallbacks().size());
        assertTrue(disposer.getSessionsWithCallbacks().contains(session1));
        assertTrue(callbacks.contains(action2));
        assertEquals(1, callbacks.size());

        disposer.onDispose(session1);

        assertTrue(disposer.getSessionsWithCallbacks().isEmpty());
        assertTrue(callbacks.isEmpty());
    }

    @Test
    public void twoIndependentSessions() {
        Set<Object> callbacks = new HashSet<>();
        CallbacksDisposer disposer = new CallbacksDisposer(callbacks::remove);

        SessionKey session1 = new SessionKey(1);
        SessionKey session2 = new SessionKey(2);

        Object action1 = new Object();
        Object action2 = new Object();

        callbacks.add(action2);
        callbacks.add(action1);

        disposer.onCallbackAdded(session1, action1);
        disposer.onCallbackAdded(session2, action2);

        assertEquals(2, disposer.getSessionsWithCallbacks().size());
        assertTrue(disposer.getSessionsWithCallbacks().contains(session1));
        assertTrue(disposer.getSessionsWithCallbacks().contains(session2));

        disposer.onDispose(session1);

        assertEquals(1, disposer.getSessionsWithCallbacks().size());
        assertTrue(disposer.getSessionsWithCallbacks().contains(session2));
        assertTrue(callbacks.contains(action2));
        assertEquals(1, callbacks.size());

        disposer.onDispose(session2);

        assertTrue(disposer.getSessionsWithCallbacks().isEmpty());
        assertTrue(callbacks.isEmpty());
    }
}
