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

package dalvik.system;

import com.android.internal.util.Preconditions;
import com.android.layoutlib.common.util.ReflectionUtils;
import com.android.tools.layoutlib.annotations.LayoutlibDelegate;
import com.android.tools.layoutlib.annotations.VisibleForTesting;

import android.annotation.NonNull;
import android.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Delegate used to provide implementation of a select few native methods of {@link VMRuntime}
 * <p/>
 * Through the layoutlib_create tool, the original native methods of VMRuntime have been replaced
 * by calls to methods of the same name in this delegate class.
 */
public class VMRuntime_Delegate {

    /**
     * A map of allocated non movable arrays to the (Direct)ByteBuffer backing it
     *
     * The JVM does not directly support newNonMovableArray. So as a workaround, this class will
     * allocate a (direct) ByteBuffer for use in native code, and provide methods to copy from native to
     * java array.
     */
    private static Map<Object, ByteBuffer> sNonMovableArrays = new WeakHashMap<>();

    // Copied from libcore/libdvm/src/main/java/dalvik/system/VMRuntime
    @LayoutlibDelegate
    /*package*/ static Object newUnpaddedArray(VMRuntime runtime, Class<?> componentType,
            int minLength) {
        // Dalvik has 32bit pointers, the array header is 16bytes plus 4bytes for dlmalloc,
        // allocations are 8byte aligned so having 4bytes of array data avoids padding.
        if (!componentType.isPrimitive()) {
            int size = ((minLength & 1) == 0) ? minLength + 1 : minLength;
            return java.lang.reflect.Array.newInstance(componentType, size);
        } else if (componentType == char.class) {
            int bytes = 20 + (2 * minLength);
            int alignedUpBytes = (bytes + 7) & -8;
            int dataBytes = alignedUpBytes - 20;
            int size = dataBytes / 2;
            return new char[size];
        } else if (componentType == int.class) {
            int size = ((minLength & 1) == 0) ? minLength + 1 : minLength;
            return new int[size];
        } else if (componentType == byte.class) {
            int bytes = 20 + minLength;
            int alignedUpBytes = (bytes + 7) & -8;
            int dataBytes = alignedUpBytes - 20;
            int size = dataBytes;
            return new byte[size];
        } else if (componentType == boolean.class) {
            int bytes = 20 + minLength;
            int alignedUpBytes = (bytes + 7) & -8;
            int dataBytes = alignedUpBytes - 20;
            int size = dataBytes;
            return new boolean[size];
        } else if (componentType == short.class) {
            int bytes = 20 + (2 * minLength);
            int alignedUpBytes = (bytes + 7) & -8;
            int dataBytes = alignedUpBytes - 20;
            int size = dataBytes / 2;
            return new short[size];
        } else if (componentType == float.class) {
            int size = ((minLength & 1) == 0) ? minLength + 1 : minLength;
            return new float[size];
        } else if (componentType == long.class) {
            return new long[minLength];
        } else if (componentType == double.class) {
            return new double[minLength];
        } else {
            assert componentType == void.class;
            throw new IllegalArgumentException("Can't allocate an array of void");
        }
    }

    @LayoutlibDelegate
    /*package*/ static Object newNonMovableArray(VMRuntime runtime, Class<?> componentType,
            int length) {
        // currently only support int types - since that seems to be the only usage in Android
        Preconditions.checkArgument(componentType == int.class,
                "unsupported type " + componentType.getName());
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * length);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] javaArray = new int[length];
        synchronized (sNonMovableArrays) {
            sNonMovableArrays.put(javaArray, byteBuffer);
        }
        return javaArray;
    }

    @LayoutlibDelegate
    static long addressOf(VMRuntime runtime, Object array) {
        ByteBuffer byteBuffer;
        synchronized (sNonMovableArrays) {
            byteBuffer = sNonMovableArrays.get(array);
        }
        Preconditions.checkNotNull(byteBuffer, "Trying to get address of unknown object");
        // TODO: implement this in JNI and use GetDirectBufferAddress
        try {
            Method addressMethod = ReflectionUtils.getAccessibleMethod(Class.forName("java.nio.DirectByteBuffer"),
                    "address");
            return (long)ReflectionUtils.invoke(addressMethod, byteBuffer);

        } catch (ClassNotFoundException | ReflectionUtils.ReflectionException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    static ByteBuffer getBackingBuffer(int[] javaArray) {
        synchronized (sNonMovableArrays) {
            return sNonMovableArrays.get(javaArray);
        }
    }

    /**
     * Copies the data from the native memory allocated with newNonMovableArray back to a java
     * array.
     */
    public static void copyNonMovableArray(int[] javaArray) {
        ByteBuffer byteBuffer = getBackingBuffer(javaArray);
        Preconditions.checkNotNull(byteBuffer, "javaArray is not a known non-movable array");
        byteBuffer.order(ByteOrder.nativeOrder()).asIntBuffer().get(javaArray);
    }

    private static long getNativeArrayAddress(long memoryAddress, int index) {
        return memoryAddress + index * 4;
    }

    @LayoutlibDelegate
    /*package*/ static int getNotifyNativeInterval() {
        // This cannot return 0, otherwise it is responsible for triggering an exception
        // whenever trying to use a NativeAllocationRegistry with size 0
        return 128; // see art/runtime/gc/heap.h -> kNotifyNativeInterval
    }

    static boolean is64Bit(VMRuntime vmRuntime) {
        return false;
    }

    static void registerSensitiveThread() {
        // ignore
    }

    static  void setProcessPackageName(String packageName) {
        // ignore
    }

    static  void setProcessDataDirectory(String dataDir) {
        // ignore
    }

    static void setDedupeHiddenApiWarnings(boolean ignored) {
        // ignore
    }

    static void clampGrowthLimit(VMRuntime runtime) {
        // ignore
    }

    static String vmLibrary(VMRuntime runtime) {
        return "";
    }

    static void updateProcessState(VMRuntime runtime, int var1) {}

    static  void notifyStartupCompleted(VMRuntime runtime) {}

    static  void preloadDexCaches(VMRuntime runtime) {}

    static void registerNativeAllocation(VMRuntime original, long ptr) {
        // ignore for now
    }

    static void registerNativeFree(VMRuntime original, long ptr) {
        // ignore for now
    }

}
