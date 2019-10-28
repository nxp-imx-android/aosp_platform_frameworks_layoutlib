/*
 * Copyright (C) 2018 The Android Open Source Project
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

package android.graphics.fonts;

import com.android.tools.layoutlib.annotations.LayoutlibDelegate;

import android.annotation.NonNull;
import android.text.FontConfig;
import android.util.ArrayMap;

import java.util.ArrayList;

/**
 * Delegate implementing the native methods of android.graphics.fonts.SystemFonts
 * <p>
 * Through the layoutlib_create tool, the original native methods of SystemFonts have been
 * replaced by calls to methods of the same name in this delegate class.
 * <p>
 * This class behaves like the original native implementation, but in Java, keeping previously
 * native data into its own objects and mapping them to int that are sent back and forth between it
 * and the original SystemFonts class.
 *
 */
public class SystemFonts_Delegate {

    private static String sFontLocation;
    public static boolean sIsTypefaceInitialized = false;

    public static void setFontLocation(String fontLocation) {
        sFontLocation = fontLocation;
    }

    @LayoutlibDelegate
    /*package*/ static FontConfig.Alias[] buildSystemFallback(@NonNull String xmlPath,
            @NonNull String fontDir,
            @NonNull FontCustomizationParser.Result oemCustomization,
            @NonNull ArrayMap<String, FontFamily[]> fallbackMap,
            @NonNull ArrayList<Font> availableFonts) {
        sIsTypefaceInitialized = true;
        return SystemFonts.buildSystemFallback_Original(sFontLocation + "native/fonts.xml",
                sFontLocation, oemCustomization, fallbackMap, availableFonts);
    }
}