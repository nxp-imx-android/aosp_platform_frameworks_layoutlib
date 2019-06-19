#!/bin/bash

readonly OUT_DIR="$1"
readonly DIST_DIR="$2"
readonly BUILD_NUMBER="$3"

readonly SCRIPT_DIR="$(dirname "$0")"

readonly FAILURE_DIR=layoutlib-test-failures
readonly FAILURE_ZIP=layoutlib-test-failures.zip

STUDIO_JDK=${SCRIPT_DIR}"/../../../../prebuilts/jdk/jdk11/linux-x86"
MISC_COMMON=${SCRIPT_DIR}"/../../../../prebuilts/misc/common"
M2_REPO=${SCRIPT_DIR}"/../../../../prebuilts/tools/common/m2/repository"
JAVA_LIBRARIES=${SCRIPT_DIR}"/../../../../out/host/common/obj/JAVA_LIBRARIES"

# Run layoutlib tests
${STUDIO_JDK}/bin/java -ea \
    -Dtest_res.dir=${SCRIPT_DIR}/res \
    -Dtest_failure.dir=${OUT_DIR}/${FAILURE_DIR} \
    -cp ${M2_REPO}/junit/junit/4.12/junit-4.12.jar:${M2_REPO}/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:${M2_REPO}/net/sf/trove4j/trove4j/1.1/trove4j-1.1.jar:${MISC_COMMON}/tools-common/tools-common-prebuilt.jar:${MISC_COMMON}/sdk-common/sdk-common.jar:${MISC_COMMON}/layoutlib_api/layoutlib_api-prebuilt.jar:${MISC_COMMON}/kxml2/kxml2-2.3.0.jar:${M2_REPO}/com/google/guava/guava/22.0/guava-22.0.jar:${JAVA_LIBRARIES}/layoutlib-tests_intermediates/javalib.jar:${JAVA_LIBRARIES}/layoutlib_intermediates/javalib.jar:${JAVA_LIBRARIES}/mockito-host_intermediates/javalib.jar:${JAVA_LIBRARIES}/objenesis-host_intermediates/javalib.jar \
    org.junit.runner.JUnitCore \
    com.android.layoutlib.bridge.intensive.Main

test_exit_code=$?

# Create zip of all failure screenshots
if [[ -d "${OUT_DIR}/${FAILURE_DIR}" ]]; then
    zip -q -j -r ${OUT_DIR}/${FAILURE_ZIP} ${OUT_DIR}/${FAILURE_DIR}
fi

# Move failure zip to dist directory if specified
if [[ -d "${DIST_DIR}" ]] && [[ -e "${OUT_DIR}/${FAILURE_ZIP}" ]]; then
    mv ${OUT_DIR}/${FAILURE_ZIP} ${DIST_DIR}
fi

exit ${test_exit_code}
