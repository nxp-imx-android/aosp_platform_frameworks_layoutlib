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

import com.android.ide.common.rendering.api.LayoutLog;
import com.android.layoutlib.bridge.Bridge;
import com.android.layoutlib.bridge.impl.DelegateManager;
import com.android.tools.layoutlib.annotations.LayoutlibDelegate;

import android.annotation.NonNull;
import android.content.res.AssetManager;

import java.nio.ByteBuffer;

import libcore.util.NativeAllocationRegistry_Delegate;

/**
 * Delegate implementing the native methods of android.graphics.fonts.Font$Builder
 * <p>
 * Through the layoutlib_create tool, the original native methods of Font$Builder have been
 * replaced by calls to methods of the same name in this delegate class.
 * <p>
 * This class behaves like the original native implementation, but in Java, keeping previously
 * native data into its own objects and mapping them to int that are sent back and forth between it
 * and the original Font$Builder class.
 *
 * @see DelegateManager
 */
public class Font_Builder_Delegate {
    protected static final DelegateManager<Font_Builder_Delegate> sBuilderManager =
            new DelegateManager<>(Font_Builder_Delegate.class);
    private static long sFontFinalizer = -1;

    protected ByteBuffer mBuffer;
    protected int mWeight;
    protected boolean mItalic;
    protected int mTtcIndex;

    @LayoutlibDelegate
    /*package*/ static long nInitBuilder() {
        return sBuilderManager.addNewDelegate(new Font_Builder_Delegate());
    }

    @LayoutlibDelegate
    /*package*/ static long nGetNativeAsset(
            @NonNull AssetManager am, @NonNull String path, boolean isAsset, int cookie) {
        // Not used in layoutlib so far
        return 0;
    }

    @LayoutlibDelegate
    /*package*/ static ByteBuffer nGetAssetBuffer(long nativeAsset) {
        // Not used in layoutlib so far
        return null;
    }

    @LayoutlibDelegate
    /*package*/ static long nGetReleaseNativeAssetFunc() {
        return 0;
    }

    @LayoutlibDelegate
    /*package*/ static void nAddAxis(long builderPtr, int tag, float value) {
        Bridge.getLog().fidelityWarning(LayoutLog.TAG_UNSUPPORTED,
                "Font$Builder.nAddAxis is not supported.", null, null, null);
    }

    @LayoutlibDelegate
    /*package*/ static long nBuild(
            long builderPtr, ByteBuffer buffer, int weight, boolean italic, int ttcIndex) {
        Font_Builder_Delegate font = sBuilderManager.getDelegate(builderPtr);
        if (font != null) {
            font.mBuffer = buffer;
            font.mWeight = weight;
            font.mItalic = italic;
            font.mTtcIndex = ttcIndex;
        }
        return builderPtr;
    }

    @LayoutlibDelegate
    /*package*/ static long nGetReleaseNativeFont() {
        synchronized (Font_Builder_Delegate.class) {
            if (sFontFinalizer == -1) {
                sFontFinalizer = NativeAllocationRegistry_Delegate.createFinalizer(
                        sBuilderManager::removeJavaReferenceFor);
            }
        }
        return sFontFinalizer;
    }
}
