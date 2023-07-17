#!/usr/bin/env bash

# extract pkg and check if it runs
echo "pkgutil"
pkgutil --check-signature /Applications/Autogram.app

for FILE in /Applications/Autogram.app \
    /Applications/Autogram.app/Contents/MacOS/Autogram \
    /Applications/Autogram.app/Contents/MacOS/AutogramApp; do

    echo "
checking $FILE:"

    echo "codesign"
    codesign -vvv --deep --strict $FILE

    echo "spctl"
    spctl -a -t exec -vv $FILE
done

echo "open"
open /Applications/Autogram.app
