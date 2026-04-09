#!/usr/bin/env bash
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

for step in \
	01-build-xstandard.sh \
	02-build-rsyntaxtextarea.sh \
	03-build-pokescript.sh \
	04-build-rpm-authoring-tools.sh \
	05-install-ctrmap-ce.sh
do
	bash "$ROOT_DIR/ci/build/$step"
done
