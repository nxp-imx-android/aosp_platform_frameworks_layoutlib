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

/**
 * Constant values for shadow related configuration
 */
class ShadowConstants {

    public static final int SPOT_SHADOW_RAYS = 40;
    public static final int SPOT_SHADOW_LAYERS = 13;
    public static final int SPOT_SHADOW_LIGHT_RADIUS = 50;
    public static final int SPOT_SHADOW_LIGHT_SOURCE_POINTS = 4;
    public static final int SPOT_SHADOW_LIGHT_Z_HEIGHT_DP = 50;
    public static final int SPOT_SHADOW_LIGHT_Z_EPSILON = 10;
    public static final float SPOT_SHADOW_STRENGTH = 0.3f;

    public static final float AMBIENT_SHADOW_EDGE_SCALE = 60f;
    public static final float AMBIENT_SHADOW_SHADOW_BOUND = 0.02f;
    public static final int AMBIENT_SHADOW_RAYS = 120;
    public static final int AMBIENT_SHADOW_LAYERS = 10;
    public static final float AMBIENT_SHADOW_STRENGTH = 1.0f;

    public static final int COORDINATE_SIZE = 3;
    public static final int RECT_VERTICES_SIZE = 4;
}
