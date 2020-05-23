/*
 * Copyright (C) 2014 The Android Open Source Project
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

package android.graphics;

import com.android.ide.common.rendering.api.AssetRepository;
import com.android.ide.common.rendering.api.LayoutLog;
import com.android.layoutlib.bridge.Bridge;
import com.android.tools.layoutlib.annotations.LayoutlibDelegate;

import android.content.res.AssetManager;
import android.content.res.BridgeAssetManager;
import android.graphics.fonts.FontVariationAxis;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FontFamily_Delegate {

    @LayoutlibDelegate
    /*package*/ static boolean addFontFromAssetManager(FontFamily fontFamily, AssetManager mgr,
            String path, int cookie, boolean isAsset, int ttcIndex, int weight, int isItalic,
            FontVariationAxis[] axes) {
        return nativeAddFontFromAssetManager(fontFamily, mgr, path, cookie, isAsset,
                ttcIndex, weight, isItalic, axes);
    }

    private static boolean nativeAddFontFromAssetManager(FontFamily fontFamily, AssetManager mgr,
            String path, int cookie, boolean isAsset, int ttcIndex, int weight, int isItalic,
            FontVariationAxis[] axes) {
        if (fontFamily == null) {
            return false;
        }
        if (mgr == null) {
            return false;
        }
        if (mgr instanceof BridgeAssetManager) {
            InputStream fontStream = null;
            try {
                AssetRepository assetRepository = ((BridgeAssetManager) mgr).getAssetRepository();
                if (assetRepository == null) {
                    Bridge.getLog().error(LayoutLog.TAG_MISSING_ASSET, "Asset not found: " + path,
                            null);
                    return false;
                }
                if (!assetRepository.isSupported()) {
                    // Don't log any warnings on unsupported IDEs.
                    return false;
                }
                fontStream = isAsset ?
                        assetRepository.openAsset(path, AssetManager.ACCESS_STREAMING) :
                        assetRepository.openNonAsset(cookie, path, AssetManager.ACCESS_STREAMING);
                if (fontStream == null) {
                    Bridge.getLog().error(LayoutLog.TAG_MISSING_ASSET, "Asset not found: " + path,
                            path);
                    return false;
                }

                List<Byte> byteList = new ArrayList<>();
                int b = fontStream.read();
                while (b != -1) {
                    byteList.add((byte) b);
                    b = fontStream.read();
                }
                ByteBuffer bb = ByteBuffer.allocateDirect(byteList.size());
                for (int i = 0; i < byteList.size(); i++) {
                    bb.put(byteList.get(i));
                }
                bb.rewind();
                return fontFamily.addFontFromBuffer(bb, ttcIndex, axes, weight, isItalic);
            } catch (IOException e) {
                Bridge.getLog().error(LayoutLog.TAG_MISSING_ASSET, "Unable to load font " + path, e,
                        path);
            } finally {
                if (fontStream != null) {
                    try {
                        fontStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            return false;
        }
        // This should never happen. AssetManager is a final class (from user's perspective), and
        // we've replaced every creation of AssetManager with our implementation. We create an
        // exception and log it, but continue with rest of the rendering, without loading this font.
        Bridge.getLog().error(LayoutLog.TAG_BROKEN,
                "You have found a bug in the rendering library. Please file a bug at b.android.com.",
                new RuntimeException("Asset Manager is not an instance of BridgeAssetManager"),
                null);
        return false;
    }
}
