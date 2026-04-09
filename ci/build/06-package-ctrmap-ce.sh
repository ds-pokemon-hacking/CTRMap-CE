#!/usr/bin/env bash
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

BUILD_DATE="${BUILD_DATE:-dev}"
BUILD_COMMIT="${BUILD_COMMIT:-local}"
BUILD_TYPE="${BUILD_TYPE:-nightly}"

run_in_dir "." mvn package assembly:single -B \
  -Dbuild.date="$BUILD_DATE" \
  -Dbuild.commit="$BUILD_COMMIT" \
  -Dbuild.type="$BUILD_TYPE"
