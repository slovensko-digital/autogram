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

# Launch Services hands a document to the running process of a bundle and will
# not start a second copy of an application it considers running. With the Java
# application in the outer bundle it was that application, so opening a file
# while Autogram ran only activated the window - the wrapper below never ran and
# the event never reached Java either. Moving it into its own nested bundle
# keeps the outer Autogram.app "not running", so the wrapper is started for
# every open and forwards the paths to the running instance via
# SingleInstanceManager, exactly like on Windows and Linux.
echo "[Autogram-post-image] moving the application into a nested helper bundle"
HELPER_APP="$TARGET/Library/AutogramHelper.app"
HELPER="$HELPER_APP/Contents"
mkdir -p "$HELPER/MacOS" "$HELPER/Resources"

# Keeps jpackage's own launcher name, so its Autogram.cfg keeps matching.
mv "$TARGET/MacOS/Autogram" "$HELPER/MacOS/Autogram"
mv "$TARGET/app" "$HELPER/app"
mv "$TARGET/runtime" "$HELPER/runtime"
cp "$TARGET/Resources/Autogram.icns" "$HELPER/Resources/Autogram.icns"
printf 'APPL????' > "$HELPER/PkgInfo"

helper_version="$(/usr/libexec/PlistBuddy -c "Print :CFBundleShortVersionString" "$TARGET/Info.plist")"
helper_bundle_version="$(/usr/libexec/PlistBuddy -c "Print :CFBundleVersion" "$TARGET/Info.plist")"
helper_identifier="$(/usr/libexec/PlistBuddy -c "Print :CFBundleIdentifier" "$TARGET/Info.plist").helper"

# The bundle can't be called Autogram.app - a second bundle of that name makes
# name-based launches (open -a, Spotlight) resolve to the running helper, which
# brings back the "activate only" bug. The Dock and Finder still show
# "Autogram", because Finder applies a localized display name whenever the
# plist's own name matches the file name - so the plist says AutogramHelper
# and the localization says Autogram.
rm -f "$HELPER/Info.plist"
/usr/libexec/PlistBuddy \
    -c "Add :CFBundleExecutable string Autogram" \
    -c "Add :CFBundleIdentifier string ${helper_identifier}" \
    -c "Add :CFBundleName string AutogramHelper" \
    -c "Add :CFBundleDisplayName string AutogramHelper" \
    -c "Add :LSHasLocalizedDisplayName bool true" \
    -c "Add :CFBundlePackageType string APPL" \
    -c "Add :CFBundleIconFile string Autogram.icns" \
    -c "Add :CFBundleShortVersionString string ${helper_version}" \
    -c "Add :CFBundleVersion string ${helper_bundle_version}" \
    -c "Add :NSHighResolutionCapable bool true" \
    "$HELPER/Info.plist" > /dev/null

mkdir -p "$HELPER/Resources/en.lproj"
printf 'CFBundleName = "Autogram";\nCFBundleDisplayName = "Autogram";\n' > "$HELPER/Resources/en.lproj/InfoPlist.strings"

# jpackage exports its own UTI for each file association. Finder resolves a .pdf
# to the system type com.adobe.pdf, so a document type claiming our exported UTI
# never matches a real PDF and Autogram is missing from "Open With". Claim the
# system type instead and drop the competing export. .asice keeps its exported
# UTI - no system type owns that extension.
echo "[Autogram-post-image] claiming system PDF UTI for file association"
PLIST="$TARGET/Info.plist"

# Not named "path": jpackage runs this with zsh, where $path is tied to $PATH.
for plist_entry in ":CFBundleDocumentTypes:0:LSItemContentTypes:0" ":UTExportedTypeDeclarations:0:UTTypeIdentifier"; do
    value="$(/usr/libexec/PlistBuddy -c "Print ${plist_entry}" "${PLIST}")"
    if [[ "${value}" != "digital.slovensko.autogram.pdf" ]]; then
        echo "[Autogram-post-image] ERROR: expected the PDF file association at ${plist_entry}, found '${value}'" >&2
        exit 1
    fi
done

/usr/libexec/PlistBuddy -c "Set :CFBundleDocumentTypes:0:LSItemContentTypes:0 com.adobe.pdf" "${PLIST}"
/usr/libexec/PlistBuddy -c "Delete :UTExportedTypeDeclarations:0" "${PLIST}"

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
    # Nested bundle first - signing an outer bundle seals what is inside it.
    codesign -s "$APPLE_DEVELOPER_IDENTITY" --keychain "$APPLE_KEYCHAIN_PATH" --entitlements "$ENTITLEMENTS" --options=runtime --deep --timestamp --force "$HELPER_APP"
    codesign -s "$APPLE_DEVELOPER_IDENTITY" --keychain "$APPLE_KEYCHAIN_PATH" --entitlements "$ENTITLEMENTS" --options=runtime --deep --timestamp --force "$TARGET/MacOS/Autogram"
fi