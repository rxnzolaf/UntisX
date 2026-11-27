#!/usr/bin/env zsh

### Run this script manually from the project dir if you need to generate new screenshots.

SCREENSHOTS_BASE_PATH=/data/data/com.rxnzolaf.untis.debug/files/screenshots

# Build and install app and test APKs onto the connected device/emulator first
./gradlew installGmsDebug installGmsDebugAndroidTest

# Ensure screenshots directory exists on device and clean local screenshots dir
MSYS_NO_PATHCONV=1 adb shell run-as com.rxnzolaf.untis.debug mkdir -p $SCREENSHOTS_BASE_PATH 2>/dev/null || true
MSYS_NO_PATHCONV=1 adb shell run-as com.rxnzolaf.untis.debug rm -rf $SCREENSHOTS_BASE_PATH/* 2>/dev/null || true
rm -rf screenshots/* 2>/dev/null || true

# Run Android tests to generate screenshots
./gradlew -PtestInstrumentationRunnerArguments.package=com.rxnzolaf.untis.screenshots connectedGmsDebugAndroidTest

# Copy screenshots from device to local (only if files exist)
[ -d screenshots/. ] || mkdir screenshots
DEVICE_FILES=$(MSYS_NO_PATHCONV=1 adb shell run-as com.rxnzolaf.untis.debug ls $SCREENSHOTS_BASE_PATH 2>/dev/null)
if [ -n "$DEVICE_FILES" ]; then
  for f in $DEVICE_FILES; do
    if [ -n "$f" ]; then
      MSYS_NO_PATHCONV=1 adb exec-out run-as com.rxnzolaf.untis.debug cat "$SCREENSHOTS_BASE_PATH/$f" > "screenshots/$f"
    fi
  done
fi
