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

package android.view.shadow;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

public class HighQualityShadowPainter {

    private static final int SHADOW_RAYS = 40;
    private static final int SHADOW_LAYERS = 13;
    private static final float SHADOW_STRENGTH = 0.4f;

    private static final int LIGHT_RADIUS = 50;
    private static final int LIGHT_SOURCE_POINTS = 4;

    private static final int LIGHT_Z_HEIGHT_DP = 50;
    private static final int LIGHT_Z_EPSILON = 10;

    private static final int COORDINATE_SIZE = 3;
    private static final int RECT_VERTICES_SIZE = 4;

    private HighQualityShadowPainter() { }

    /**
     * Draws simple Rect shadow
     */
    public static void paintRectShadow(ViewGroup parent, Outline outline, float elevation,
            Canvas canvas, float alpha, float densityDpi) {

        // TODO: Use alpha later
        // TODO: potential optimization with memory usage of light coords + poly vertices
        //      - For Rect case, their size never changes
        //      - After single-light-source CL, the light source coord does not change

        float lightZHeightPx = LIGHT_Z_HEIGHT_DP * (densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        if (lightZHeightPx - elevation < LIGHT_Z_EPSILON) {
            // If the view is above or too close to the light source then return.
            // This is done to somewhat simulate android behaviour.
            return;
        }

        int width = parent.getWidth();
        int height = parent.getHeight();

        Rect rectBound = new Rect();
        if (!outline.getRect(rectBound)) {
            return;
        }

        float[] poly = getPoly(rectBound, elevation);

        float lightX = (rectBound.left + rectBound.right) / 2;
        float lightY = rectBound.top;

        ShadowConfig config = new ShadowConfig.Builder()
                .setSize(width, height)
                .setLayers(SHADOW_LAYERS)
                .setRays(SHADOW_RAYS)
                .setLightCoord(lightX, lightY, lightZHeightPx)
                .setLightRadius(LIGHT_RADIUS)
                .setLightSourcePoints(LIGHT_SOURCE_POINTS)
                .setShadowStrength(SHADOW_STRENGTH)
                .setPolygon(poly, RECT_VERTICES_SIZE)
                .build();

        SpotShadowBitmapGenerator generator = new SpotShadowBitmapGenerator(config);

        generator.populateShadow();

        if (!generator.validate()) {
            return;
        }

        int save = canvas.save();
        canvas.drawBitmap(generator.getBitmap(), -generator.getTranslateX(),
                -generator.getTranslateY(), null);
        canvas.restoreToCount(save);
    }

    private static float[] getPoly(Rect rect, float elevation) {
        float[] poly = new float[RECT_VERTICES_SIZE * COORDINATE_SIZE];

        poly[0] = poly[9] = rect.left;
        poly[1] = poly[4] = rect.top;
        poly[3] = poly[6] = rect.right;
        poly[7] = poly[10] = rect.bottom;
        poly[2] = poly[5] = poly[8] = poly[11] = elevation;

        return poly;
    }
}
