#!/bin/bash
set -euo pipefail

trap 'echo "[Autogram-post-image] ERROR at line ${LINENO} (exit code: $?)" >&2' ERR
echo "[Autogram-post-image] invoked as: $0"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
echo "[Autogram-post-image] script dir: ${SCRIPT_DIR}"

if [[ -d "./Contents" ]]; then
    TARGET="$(cd "./Contents" && pwd)"
else
    for contents_dir in ../images/*/*/Contents; do
        if [[ -d "${contents_dir}" ]]; then
            TARGET="$(cd "${contents_dir}" && pwd)"
            break
        fi
    done

    if [[ -z "${TARGET:-}" ]]; then
        echo "[Autogram-post-image] ERROR: could not locate .app Contents directory (cwd=$(pwd))" >&2
        echo "[Autogram-post-image] cwd listing:" >&2
        ls -la . >&2
        exit 1
    fi
fi
echo "[Autogram-post-image] target Contents dir: ${TARGET}"

SOURCE="${SCRIPT_DIR}/../../mac-launcher"
if [[ ! -d "${SOURCE}" ]]; then
    echo "[Autogram-post-image] ERROR: mac-launcher source dir not found at ${SOURCE}" >&2
    exit 1
fi
SOURCE="$(cd "${SOURCE}" && pwd)"
echo "[Autogram-post-image] mac-launcher source dir: ${SOURCE}"

echo "[Autogram-post-image] renaming main executable/config to AutogramApp"
mv "$TARGET/MacOS/Autogram" "$TARGET/MacOS/AutogramApp"
mv "$TARGET/app/Autogram.cfg" "$TARGET/app/AutogramApp.cfg"

echo "[Autogram-post-image] installing mac-launcher into app bundle"
cp -r "$SOURCE/Resources" "$TARGET"
cp -r "$SOURCE/MacOS" "$TARGET"

chmod +x "$TARGET/MacOS/Autogram"

# codesign changed executables
ENTITLEMENTS="${SCRIPT_DIR}/../../Autogram.entitlements"
if [[ ! -f "${ENTITLEMENTS}" ]]; then
    echo "[Autogram-post-image] ERROR: entitlements file not found at ${ENTITLEMENTS}" >&2
    exit 1
fi

if [[ "${JPACKAGE_MAC_SIGN:-}" == "1" ]]; then
    echo "[Autogram-post-image] codesigning launcher executables (entitlements: ${ENTITLEMENTS})"
    codesign -s "$APPLE_DEVELOPER_IDENTITY" --keychain "$APPLE_KEYCHAIN_PATH" --entitlements "$ENTITLEMENTS" --options=runtime --deep --timestamp --force "$TARGET/MacOS/Autogram"
    codesign -s "$APPLE_DEVELOPER_IDENTITY" --keychain "$APPLE_KEYCHAIN_PATH" --entitlements "$ENTITLEMENTS" --options=runtime --deep --timestamp --force "$TARGET/MacOS/AutogramApp"
fi