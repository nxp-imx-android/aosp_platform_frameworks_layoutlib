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

package android.view;

import com.android.tools.layoutlib.annotations.LayoutlibDelegate;

import android.view.Choreographer.CallbackRecord;

import java.util.Set;

import static android.view.Choreographer_Delegate.currentContext;

/**
 * Delegate used to provide new implementation of a select few methods of {@link CallbackRecord}
 *
 * Through the layoutlib_create tool, the original methods of {@link CallbackRecord} have been
 * replaced by calls to methods of the same name in this delegate class.
 *
 */
public class Choreographer_CallbackRecord_Delegate {
    @LayoutlibDelegate
    public static void run(CallbackRecord thiz, long frameTimeNanos) {
        Set<Object> callbacks = Choreographer_Delegate.sFrameCallbacks.get(currentContext());
        if (callbacks != null && thiz.action != null) {
            callbacks.remove(thiz.action);
        }
        thiz.run_Original(frameTimeNanos);
    }
}
