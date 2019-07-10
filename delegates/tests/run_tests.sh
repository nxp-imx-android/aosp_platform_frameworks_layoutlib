#!/bin/bash

set -e

readonly SCRIPT_DIR="$(dirname "$0")"

STUDIO_JDK=${SCRIPT_DIR}"/../../../../prebuilts/studio/jdk/linux"
JAVA_LIBRARIES=${SCRIPT_DIR}"/../../../../out/host/linux-x86/framework/"
NATIVE_LIBRARIES=${SCRIPT_DIR}"/../../../../out/host/linux-x86/lib64/"

# add this argument to enable debugging
# -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 \

make -j layoutlib-common-delegates-tests liblayoutlib-common-delegates-tests-native
${STUDIO_JDK}/bin/java -ea \
    -Djava.library.path=${NATIVE_LIBRARIES} \
    -cp ${JAVA_LIBRARIES}/layoutlib-common-delegates-tests.jar \
    org.junit.runner.JUnitCore \
    dalvik.system.VMRuntimeTest
