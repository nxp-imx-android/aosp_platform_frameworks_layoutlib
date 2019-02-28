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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import static android.view.shadow.ShadowConstants.SCALE_DOWN;

public class HighQualityShadowPainter {

    private HighQualityShadowPainter() { }

    /**
     * Draws simple Rect shadow
     */
    public static void paintRectShadow(ViewGroup parent, Outline outline, float elevation,
            Canvas canvas, float alpha, float densityDpi) {

        if (!validate(elevation, densityDpi)) {
            return;
        }

        int width = parent.getWidth() / SCALE_DOWN;
        int height = parent.getHeight() / SCALE_DOWN;

        Rect rectBound = new Rect();
        if (!outline.getRect(rectBound)) {
            return;
        }

        rectBound.left /= SCALE_DOWN;
        rectBound.right /= SCALE_DOWN;
        rectBound.top /= SCALE_DOWN;
        rectBound.bottom /= SCALE_DOWN;

        float[] poly = getPoly(rectBound, elevation / SCALE_DOWN);

        paintAmbientShadow(poly, canvas, width, height);
        paintSpotShadow(poly, rectBound, elevation / SCALE_DOWN,
                canvas, alpha, densityDpi, width, height);
    }

    /**
     * High quality shadow does not work well with object that is too high in elevation. Check if
     * the object elevation is reasonable and returns true if shadow will work well. False other
     * wise.
     */
    private static boolean validate(float elevation, float densityDpi) {
        float scaledElevationPx = elevation / SCALE_DOWN;
        float scaledSpotLightHeightPx = ShadowConstants.SPOT_SHADOW_LIGHT_Z_HEIGHT_DP *
                (densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        if (scaledElevationPx > scaledSpotLightHeightPx) {
            return false;
        }

        return true;
    }

    private static void paintAmbientShadow(float[] polygon, Canvas canvas, int width, int height) {
        // TODO: Consider re-using the triangle buffer here since the world stays consistent.
        // TODO: Reduce the buffer size based on shadow bounds.

        AmbientShadowConfig config = new AmbientShadowConfig.Builder()
                .setSize(width, height)
                .setPolygon(polygon)
                .setEdgeScale(ShadowConstants.AMBIENT_SHADOW_EDGE_SCALE)
                .setShadowBoundRatio(ShadowConstants.AMBIENT_SHADOW_SHADOW_BOUND)
                .setShadowStrength(ShadowConstants.AMBIENT_SHADOW_STRENGTH)
                .setRays(ShadowConstants.AMBIENT_SHADOW_RAYS)
                .setLayers(ShadowConstants.AMBIENT_SHADOW_LAYERS)
                .build();

        AmbientShadowBitmapGenerator generator = new AmbientShadowBitmapGenerator(config);
        generator.populateShadow();

        if (!generator.isValid()) {
            return;
        }

        drawScaled(canvas, generator.getBitmap(), (int) generator.getTranslateX(),
                (int) generator.getTranslateY(), width, height);
    }

    private static void paintSpotShadow(float[] poly, Rect rectBound, float elevation, Canvas canvas, float alpha,
            float densityDpi, int width, int height) {

        // TODO: Use alpha later
        float lightZHeightPx = ShadowConstants.SPOT_SHADOW_LIGHT_Z_HEIGHT_DP * (densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        if (lightZHeightPx - elevation < ShadowConstants.SPOT_SHADOW_LIGHT_Z_EPSILON) {
            // If the view is above or too close to the light source then return.
            // This is done to somewhat simulate android behaviour.
            return;
        }

        float lightX = (rectBound.left + rectBound.right) / 2;
        float lightY = rectBound.top;
        // Light shouldn't be bigger than the object by too much.
        int dynamicLightRadius = Math.min(rectBound.width(), rectBound.height());

        SpotShadowConfig config = new SpotShadowConfig.Builder()
                .setSize(width, height)
                .setLayers(ShadowConstants.SPOT_SHADOW_LAYERS)
                .setRays(ShadowConstants.SPOT_SHADOW_RAYS)
                .setLightCoord(lightX, lightY, lightZHeightPx)
                .setLightRadius(dynamicLightRadius)
                .setLightSourcePoints(ShadowConstants.SPOT_SHADOW_LIGHT_SOURCE_POINTS)
                .setShadowStrength(ShadowConstants.SPOT_SHADOW_STRENGTH)
                .setPolygon(poly, ShadowConstants.RECT_VERTICES_SIZE)
                .build();

        SpotShadowBitmapGenerator generator = new SpotShadowBitmapGenerator(config);
        generator.populateShadow();

        if (!generator.validate()) {
            return;
        }

        drawScaled(canvas, generator.getBitmap(), (int) generator.getTranslateX(),
                (int) generator.getTranslateY(), width, height);
    }

    /**
     * Draw the bitmap scaled up.
     * @param translateX - offset in x axis by which the bitmap is shifted.
     * @param translateY - offset in y axis by which the bitmap is shifted.
     */
    private static void drawScaled(Canvas canvas, Bitmap bitmap, int translateX, int translateY,
            int width, int height) {
        Rect dest = new Rect();
        dest.left = -translateX * SCALE_DOWN;
        dest.top = -translateY * SCALE_DOWN;
        dest.right = (width * SCALE_DOWN) + dest.left;
        dest.bottom = (height * SCALE_DOWN) + dest.top;

        int save = canvas.save();
        canvas.drawBitmap(bitmap, null, dest, null);
        canvas.restoreToCount(save);
    }

    private static float[] getPoly(Rect rect, float elevation) {
        float[] poly = new float[ShadowConstants.RECT_VERTICES_SIZE *
                ShadowConstants.COORDINATE_SIZE];

        poly[0] = poly[9] = rect.left;
        poly[1] = poly[4] = rect.top;
        poly[3] = poly[6] = rect.right;
        poly[7] = poly[10] = rect.bottom;
        poly[2] = poly[5] = poly[8] = poly[11] = elevation;

        return poly;
    }
}
