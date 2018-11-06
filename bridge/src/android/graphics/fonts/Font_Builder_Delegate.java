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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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
    private static DelegateManager<String> sAssetManager = new DelegateManager<>(String.class);
    private static long sAssetFinalizer = -1;

    @LayoutlibDelegate
    /*package*/ static long nGetNativeAsset(
            @NonNull AssetManager am, @NonNull String path, boolean isAsset, int cookie) {
        return sAssetManager.addNewDelegate(path);
    }

    @LayoutlibDelegate
    /*package*/ static ByteBuffer nGetAssetBuffer(long nativeAsset) {
        String fullPath = sAssetManager.getDelegate(nativeAsset);
        if (fullPath == null) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(fullPath)) {
            final FileChannel fc = fis.getChannel();
            return fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        } catch (IOException e) {
            Bridge.getLog().error(LayoutLog.TAG_MISSING_ASSET,
                    "Error reading font file " + fullPath, null, null, null);
            return null;
        }
    }

    @LayoutlibDelegate
    /*package*/ static long nGetReleaseNativeAssetFunc() {
        synchronized (Font_Builder_Delegate.class) {
            if (sAssetFinalizer == -1) {
                sAssetFinalizer = NativeAllocationRegistry_Delegate.createFinalizer(
                        sAssetManager::removeJavaReferenceFor);
            }
        }
        return sAssetFinalizer;
    }
}
