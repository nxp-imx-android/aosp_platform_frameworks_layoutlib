#!/bin/bash

SCRIPT_DIR="$(dirname $0)"
DIST_DIR="$1"

STUDIO_JDK=${SCRIPT_DIR}"/../../../../prebuilts/studio/jdk/linux"
MISC_COMMON=${SCRIPT_DIR}"/../../../../prebuilts/misc/common"
M2_REPO=${SCRIPT_DIR}"/../../../../prebuilts/tools/common/m2/repository"
JAVA_LIBRARIES=${SCRIPT_DIR}"/../../../../out/host/common/obj/JAVA_LIBRARIES"

${STUDIO_JDK}/bin/java -ea \
    -cp ${M2_REPO}/junit/junit/4.12/junit-4.12.jar:${M2_REPO}/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:${JAVA_LIBRARIES}/layoutlib-create-tests_intermediates/javalib.jar:${JAVA_LIBRARIES}/layoutlib_create_intermediates/javalib.jar:${SCRIPT_DIR}/res \
    org.junit.runner.JUnitCore \
    com.android.tools.layoutlib.create.AllTests

