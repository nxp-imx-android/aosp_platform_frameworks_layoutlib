/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.tools.layoutlib.create;

public class NativeConfig {

    private NativeConfig() {}

    public final static String[] DELEGATE_CLASS_NATIVES_TO_NATIVES = new String [] {
            "android.graphics.ColorSpace$Rgb",
            "android.graphics.FontFamily",
            "android.graphics.ImageDecoder",
            "android.graphics.Matrix",
            "android.graphics.Path",
            "android.graphics.Typeface",
            "android.graphics.fonts.Font$Builder",
            "android.graphics.fonts.FontFamily$Builder",
            "android.graphics.text.LineBreaker",
    };

    public final static String[] DELEGATE_CLASS_NATIVES = new String[] {
            "android.os.SystemClock",
            "android.os.SystemProperties",
            "android.view.Display",
            "libcore.icu.ICU",
    };
}
