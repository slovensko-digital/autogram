TARGET="`cd ../images/*/*/Contents;pwd`"
SOURCE="`cd ../../mac-launcher;pwd`"


mv "$TARGET/MacOS/Autogram" "$TARGET/MacOS/AutogramApp"
mv "$TARGET/app/Autogram.cfg" "$TARGET/app/AutogramApp.cfg"

cp -r "$SOURCE/Resources" "$TARGET"
cp -r "$SOURCE/MacOS" "$TARGET"

chmod +x "$TARGET/MacOS/Autogram"