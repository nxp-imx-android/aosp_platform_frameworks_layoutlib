/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <stdio.h>

static jboolean NativeWriteIntArray(JNIEnv* /* env */, jclass /*clazz*/, jlong memoryAddress) {
    uint32_t* out_values = reinterpret_cast<uint32_t*>(memoryAddress);

    out_values[0] = 1;
    out_values[1] = 2;
    out_values[2] = 3;
    return JNI_TRUE;
}

static const JNINativeMethod gNativeArrayMethods[] = {
    {"nativeWriteIntArray", "(J)Z", (void*)NativeWriteIntArray},
};


int register_dalvik_system_NativeArray(JNIEnv* env) {
    jclass clazz = env->FindClass("dalvik/system/NativeArray");
    return env->RegisterNatives(clazz, gNativeArrayMethods, 1);
}