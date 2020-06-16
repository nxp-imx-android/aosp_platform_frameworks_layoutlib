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
 * limitations under the License
 */
package android.view;

import com.android.ide.common.rendering.api.LayoutLog;
import com.android.layoutlib.bridge.Bridge;

import android.content.Context;
import android.graphics.Region;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display.Mode;

public class WindowManagerImpl implements WindowManager {

    private final DisplayMetrics mMetrics;
    private final Display mDisplay;

    public WindowManagerImpl(DisplayMetrics metrics) {
        mMetrics = metrics;

        DisplayInfo info = new DisplayInfo();
        info.logicalHeight = mMetrics.heightPixels;
        info.logicalWidth = mMetrics.widthPixels;
        info.supportedModes = new Mode[] {
                new Mode(0, mMetrics.widthPixels, mMetrics.heightPixels, 60f)
        };
        info.logicalDensityDpi = mMetrics.densityDpi;
        mDisplay = new Display(null, Display.DEFAULT_DISPLAY, info,
                DisplayAdjustments.DEFAULT_DISPLAY_ADJUSTMENTS);
    }

    public WindowManagerImpl createLocalWindowManager(Window parentWindow) {
        Bridge.getLog().fidelityWarning(LayoutLog.TAG_UNSUPPORTED,
                "The preview does not support multiple windows.",
                null, null, null);
        return this;
    }

    public WindowManagerImpl createPresentationWindowManager(Context displayContext) {
        Bridge.getLog().fidelityWarning(LayoutLog.TAG_UNSUPPORTED,
                "The preview does not support multiple windows.",
                null, null, null);
        return this;
    }

    /**
     * Sets the window token to assign when none is specified by the client or
     * available from the parent window.
     *
     * @param token The default token to assign.
     */
    public void setDefaultToken(IBinder token) {

    }

    @Override
    public Display getDefaultDisplay() {
        return mDisplay;
    }


    @Override
    public void addView(View arg0, android.view.ViewGroup.LayoutParams arg1) {
        // pass
    }

    @Override
    public void removeView(View arg0) {
        // pass
    }

    @Override
    public void updateViewLayout(View arg0, android.view.ViewGroup.LayoutParams arg1) {
        // pass
    }


    @Override
    public void removeViewImmediate(View arg0) {
        // pass
    }

    @Override
    public void requestAppKeyboardShortcuts(
            KeyboardShortcutsReceiver receiver, int deviceId) {
    }

    @Override
    public Region getCurrentImeTouchRegion() {
        return null;
    }

    @Override
    public void setShouldShowWithInsecureKeyguard(int displayId, boolean shouldShow) {
        // pass
    }

    @Override
    public void setShouldShowSystemDecors(int displayId, boolean shouldShow) {
        // pass
    }

    @Override
    public void setShouldShowIme(int displayId, boolean shouldShow) {
        // pass
    }
}
