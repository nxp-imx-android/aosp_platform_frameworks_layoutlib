/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.layoutlib.bridge.android;

import android.content.ClipData;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.MergedConfiguration;
import android.view.DisplayCutout;
import android.view.IWindow;
import android.view.IWindowId;
import android.view.IWindowSession;
import android.view.InputChannel;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceView;
import android.view.WindowManager.LayoutParams;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Implementation of {@link IWindowSession} so that mSession is not null in
 * the {@link SurfaceView}.
 */
public final class BridgeWindowSession {
    public static IWindowSession create() {
        return (IWindowSession) Proxy.newProxyInstance(BridgeWindowSession.class.getClassLoader(),
                new Class[]{IWindowSession.class}, new InvocationHandler() {
            public Object invoke(Object proxy, Method m, Object[] args) {
                final Class<?> returnType = m.getReturnType();
                if (returnType == boolean.class) {
                    return false;
                } else if (returnType == int.class) {
                    return 0;
                } else if (returnType == long.class) {
                    return 0L;
                } else if (returnType == short.class) {
                    return 0;
                } else if (returnType == char.class) {
                    return 0;
                } else if (returnType == byte.class) {
                    return 0;
                } else if (returnType == float.class) {
                    return 0f;
                } else if (returnType == double.class) {
                    return 0.0;
                } else {
                    return null;
                }
            }
        });
    }
}
