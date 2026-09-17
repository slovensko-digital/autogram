# Custom launcher for macOS

# Check Autogram-post-image.sh to see how it is applied

# Check main.scpt in Script Editor for the logic

# Reason it was used - Desktop.setOpenURIHandler was not called on the first launch

# See https://bugs.openjdk.java.net/browse/JDK-8198549

`mac-launcher/MacOS/Autogram` is `applet` from when you "Export" app from applescript main.scpt

Because an Apple Application Bundle can have only one executable "responsible" for the app, for
handling urls and for opening documents (all defined in Info.plist), we have created an AppleScript
wrapper with content in `main.scpt` and executable in `MacOS/Autogram` (this is only AppleScript
interpreter, afaik). The wrapper translates what it was launched with into command line arguments
for the real application.

## Why the app lives in a nested bundle

The Java application is **not** `Contents/MacOS/` of the outer bundle - it sits in its own nested
bundle at `Contents/Library/AutogramHelper.app`, with its own identifier
(`digital.slovensko.autogram.helper`). `Autogram-post-image.sh` moves it there.

This is what makes opening documents work while Autogram is already running. Launch Services picks
the process to hand a document to by bundle, and it will not start a second copy of an application
it considers running. When the Java process lived in the outer bundle it *was* that application, so
opening a file only activated the window: the wrapper was never run, and the event never reached
Java either (neither `java.awt.Desktop.setOpenFileHandler` nor the internal Glass event handler ever
sees it on macOS 26 / JavaFX 25).

With the Java process registered as the helper bundle instead, Launch Services no longer considers
the outer `Autogram.app` running, so it runs this wrapper for **every** open. The wrapper starts the
helper with the file paths as arguments, and `SingleInstanceManager` forwards them over its unix
socket to the instance that is already running - the same mechanism used on Windows and Linux.

To call the application from the command line with parameters, use the helper executable directly:

```
Autogram.app/Contents/Library/AutogramHelper.app/Contents/MacOS/Autogram --help
```

In `main.scpt` is this:

```
# Launches the app with the custom protocol URL or the documents to open.
# The app itself lives in a nested helper bundle, so that Launch Services keeps
# treating this wrapper as the application and runs it for every open - the
# helper forwards its arguments to an already running instance.
# Desktop.setOpenURIHandler was not called on the first launch
# See https://bugs.openjdk.java.net/browse/JDK-8198549

on appPath()
	return quoted form of ((POSIX path of (path to me)) & "Contents/Library/AutogramHelper.app/Contents/MacOS/Autogram")
end appPath

on launch(arguments)
	do shell script appPath() & arguments & " > /dev/null 2>&1 &"
end launch

on run argv
	launch("")
end run

on open location this_URL
	launch(" --url=" & quoted form of this_URL)
end open location

on open theseItems
	set arguments to ""
	repeat with anItem in theseItems
		set arguments to arguments & " " & quoted form of (POSIX path of anItem)
	end repeat
	launch(arguments)
end open
```

`on open theseItems` handles files opened from Finder (double-click, "Open With", or dropping them
on the app icon). Without it macOS sends the "open documents" Apple Event and nothing happens.

## Installing a local development build

An unsigned `.pkg` (built without `APPLE_DEVELOPER_IDENTITY`) is refused by the macOS Installer,
so extract the app bundle from its payload and copy it into `/Applications` instead:

```
./mvnw package -Psystem-jdk -DskipTests
rm -rf /tmp/autogram-pkg
pkgutil --expand-full target/Autogram-1.0.0.pkg /tmp/autogram-pkg
osascript -e 'tell application id "digital.slovensko.autogram.helper" to quit'
rm -rf /Applications/Autogram.app
ditto /tmp/autogram-pkg/Autogram-app.pkg/Payload/Autogram.app /Applications/Autogram.app
chmod +x /Applications/Autogram.app/Contents/MacOS/Autogram \
         /Applications/Autogram.app/Contents/Library/AutogramHelper.app/Contents/MacOS/Autogram
/System/Library/Frameworks/CoreServices.framework/Frameworks/LaunchServices.framework/Support/lsregister -f /Applications/Autogram.app
```

`lsregister -f` refreshes the file associations and URL handler in Launch Services; without it Finder
may keep using a previously registered copy (e.g. one in the Trash).
