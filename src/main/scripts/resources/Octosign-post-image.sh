TARGET="`cd ../images/*/*/Contents;pwd`"
SOURCE="`cd ../../mac-launcher;pwd`"

mv "$TARGET/MacOS/Octosign" "$TARGET/MacOS/OctosignApp"
mv "$TARGET/app/Octosign.cfg" "$TARGET/app/OctosignApp.cfg"
cp -r "$SOURCE/MacOS" "$TARGET"
cp -r "$SOURCE/Resources" "$TARGET"
