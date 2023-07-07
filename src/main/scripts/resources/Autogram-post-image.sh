#!/bin/bash -e
TARGET="$(cd ../images/*/*/Contents;pwd)"
SOURCE="$(cd ../../mac-launcher;pwd)"

mv "$TARGET/MacOS/Autogram" "$TARGET/MacOS/AutogramApp"
mv "$TARGET/app/Autogram.cfg" "$TARGET/app/AutogramApp.cfg"

cp -r "$SOURCE/Resources" "$TARGET"
cp -r "$SOURCE/MacOS" "$TARGET"

chmod +x "$TARGET/MacOS/Autogram"

# codesign changed executables
ENTITLEMENTS=../../Autogram.entitlements
if [[ "$JPACKAGE_MAC_SIGN" == "1" ]]; then
    codesign -s "$APPLE_DEVELOPER_IDENTITY" --keychain $APPLE_KEYCHAIN_PATH --entitlements "$ENTITLEMENTS" --options=runtime --deep --timestamp --force "$TARGET/MacOS/Autogram"
    codesign -s "$APPLE_DEVELOPER_IDENTITY" --keychain $APPLE_KEYCHAIN_PATH --entitlements "$ENTITLEMENTS" --options=runtime --deep --timestamp --force "$TARGET/MacOS/AutogramApp"
fi