# Custom launcher for macOS - launches the app with the custom protocol URL

# Check Autogram-post-image.sh to see how it is applied

# Check main.scpt in Script Editor for the logic

# Reason it was used - Desktop.setOpenURIHandler was not called on the first launch

# See https://bugs.openjdk.java.net/browse/JDK-8198549

`mac-launcher/MacOS/Autogram` is `applet` from when you "Export" app from applescript main.scpt

Because Apple Application Bundle can have only one executable "responsible" for both app and handling urls (defined in Info.plist) we have created AppleScript wrapper with content in `main.scpt` and executable in `MacOS/Autogram` (this is only AppleScript interpreter, afaik). The wrapper checks if it was run as normal application or URL handler and fills in `--url` parameter if it's so. If someone wants to call it from comannd line with parameters use `Autogram.app/Content/MacOS/AutogramApp` executable directly.

In `main.scpt` is something like this:

```
# Launches the app with the custom protocol URL
# Desktop.setOpenURIHandler was not called on the first launch
# See https://bugs.openjdk.java.net/browse/JDK-8198549

on run
	set appPath to (POSIX path of (path to me)) & "/Contents/MacOS/AutogramApp"
	#	display dialog "running from run"
	do shell script appPath & " > /dev/null 2>&1 &"
end run

on open location this_URL
	set appPath to (POSIX path of (path to me)) & "Contents/MacOS/AutogramApp"
	set appScript to (appPath & " --url=" & this_URL)
	#	display dialog "running from open location " & appScript
	do shell script appScript & " > /dev/null 2>&1 &"
end open location
```
