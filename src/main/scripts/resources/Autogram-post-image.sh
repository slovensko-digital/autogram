TARGET="`cd ../images/*/*/Contents;pwd`"
SOURCE="`cd ../../mac-launcher;pwd`"

cp -r "$SOURCE/Resources" "$TARGET"
