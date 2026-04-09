#!/usr/bin/env bash
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

run_in_dir "submodules/XStandard/lib" bash -c 'chmod +x _InstallLibs.sh && ./_InstallLibs.sh'
mvn_install_module "submodules/XStandard"
