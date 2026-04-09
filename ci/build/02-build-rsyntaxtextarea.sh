#!/usr/bin/env bash
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

run_in_dir "submodules/PokeScript/PokeScriptIDE/submodules/RSyntaxTextArea" chmod +x ./gradlew
run_in_dir "submodules/PokeScript/PokeScriptIDE/submodules/RSyntaxTextArea" ./gradlew jar publishToMavenLocal --no-daemon
