#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

run_in_dir() {
  local rel_dir="$1"
  shift
  (
    cd "$ROOT_DIR/$rel_dir"
    "$@"
  )
}

mvn_install_module() {
  local rel_dir="$1"
  run_in_dir "$rel_dir" mvn install -B
}
