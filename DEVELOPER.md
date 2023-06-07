# Info for developers

# How to use FakeTokenDriver

create empty file `fakeTokenDriver` in cwd - so in project root when developing, to enable "Fake token driver"

# More info about inner workings of builds for MacOS

To run signed mac build add follwing to `.vscode/settings.json` (or you can do unsigned build by setting `mac.sign=0` in `build.properties`)

```json
  "autogram.APPLE_DEVELOPER_IDENTITY": "Developer ID Application: Sluzby Slovensko.Digital, s.r.o. (44U4JSRX4Z)",
  "autogram.APPLE_KEYCHAIN_PATH": "..../autogram/secret/app-signing.keychain-db"
```

(Developer ID is visible in signature, so it's ok that its public)

run this before building in any terminal - set app-signing keychain as default and unlock it

**Setup**

```sh
export APPLE_KEYCHAIN_PATH=".../autogram/secret/app-signing.keychain-db"
export APPLE_KEYCHAIN_PASSWORD=""
security unlock-keychain -p $APPLE_KEYCHAIN_PASSWORD $APPLE_KEYCHAIN_PATH
security list-keychains -d user -s $APPLE_KEYCHAIN_PATH login.keychain
security default-keychain -s $APPLE_KEYCHAIN_PATH
export APPLE_DEVELOPER_IDENTITY="Developer ID Application: Sluzby Slovensko.Digital, s.r.o. (44U4JSRX4Z)"
```

run this after you finish - so app-signing keychain wont be used for your private data

**Cleanup**

```sh
security list-keychains -d user -s login.keychain
security default-keychain -s login.keychain
```

**Timeline**

- `jpackage` creates "app-image"
- `jpackage` signs binary/executable in app-image
- unpacked "app-image" is edited by `Autogram-post-image.sh` by adding new executable (from `src/main/scripts/resources/mac-launcher/MacOS/Autogram`), and some other changes
- `Autogram-post-image.sh` signs using `codesign` both of these executables since they are both changed
- `jpackage` finishes creating `.app` file that gets packaged into installer `.pkg`
- `xcrun notarytool` notarizes `pkg` with Apple
- `xcrun stapler staple` adds (staples) notarization ticket to pkg file so it can be installed without internet
- 🎉 you have signed and notarized package

Notes:

- there are two types of `pkg` [1](https://stackoverflow.com/questions/74422992/what-is-the-difference-between-pkgbuild-vs-productbuild)
  - "component package" - use `pkgbuild`
  - "product archive" - use `productbuild`, also known as "distribution packages" or "installer packages" can contain multiple "component packages"

Following is just overview of useful commands and

## Creating certificates and keychain

- create keychain using `security create-keychain -p "$APPLE_KEYCHAIN_PASSWORD" $APPLE_KEYCHAIN_PATH`
- create two CSR in that keychain (or not, but you will have to copy-paste it from login keychain) (one for ...Application and one for ...Installer cert)
- create certificates "Developer ID Application" and "Developer ID Installer" on Apple website
- add certificates to keychain - when you generate CSR you create private key, when you add generated `.cer`
- copy "Developer ID Certification Authority" cert from login keychain if it's missing - or you will get errors about missing chain

## Creating .app

Using [jpackage](https://docs.oracle.com/en/java/javase/17/docs/specs/man/jpackage.html)

- `Autogram.entitlements` - entitlements default is [sanbox.plist](https://github.com/openjdk/jdk/blob/master/src/jdk.jpackage/macosx/classes/jdk/jpackage/internal/resources/sandbox.plist) and we are removing audio recording permission
- `Autogram-post-image.sh` - this script gets run during `jpackage` execution, between when "app-image" is prepared, but before packaging to pkg
- `Autogram-background.png`, `Autogram-background-darkAqua.png` - images for installer background, aligned to bottom left, margins have to be in image

## MacOS packaging and signing

Unpacking pkg

```
pkgutil --expand-full Autogram-1.0.0.pkg Autogram-1.0.0
```

### Signing code

```sh
codesign -s "$APPLE_DEVELOPER_IDENTITY" --keychain $APPLE_KEYCHAIN_PATH --options=runtime  --deep --timestamp Autogram-1.0.0.pkg
```

- `-s <identity>` - which identity to use for signing
- `--options=runtime` - signs with hardened runtime [1](https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution/resolving_common_notarization_issues#3087724)
- `--deep` - sign insides of package
- `--timestamp` - use secure timestamp [1](https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution/resolving_common_notarization_issues#3087733)
- `Autogram-1.0.0.pkg` - what to sign

### Signing installer

```
productsign...
```

### Verifying before notarization

Check if pkg is code signed

```sh
codesign -vvv --deep --strict Autogram-1.0.0.pkg
```

Check pkg is product signed

```sh
pkgutil --check-signature Autogram-1.0.0.pkg
```

Check product will run with current policy

```sh
spctl -vvv --assess --type exec Autogram-1.0.0.pkg
```

Check if installer will run with current policy

```sh
spctl --assess --ignore-cache --verbose --type install Autogram-1.0.0.pkg
```

### Notarization

Store credentials for notarization

```sh
xcrun notarytool store-credentials --keychain $APPLE_KEYCHAIN_PATH
```

Unlock keychain

```sh
security unlock-keychain -p $APPLE_KEYCHAIN_PASSWORD $APPLE_KEYCHAIN_PATH
```

Set keychain as default

```sh
security list-keychains -d user -s $APPLE_KEYCHAIN_PATH
security default-keychain -s $APPLE_KEYCHAIN_PATH
```

Clean up default keychain

```sh
security list-keychains -d user -s login.keychain
security default-keychain -s login.keychain
```

Submit for notarization

```sh
xcrun notarytool submit --keychain-profile "autogram" --keychain $APPLE_KEYCHAIN_PATH --progress --wait Autogram-1.0.0.pkg
```

Check what went wrong

```sh
# get summary/status
xcrun notarytool info  $submission_id --keychain-profile "autogram" --keychain $APPLE_KEYCHAIN_PATH
# get detailed log and individual issues
xcrun notarytool log  $submission_id --keychain-profile "autogram" --keychain $APPLE_KEYCHAIN_PATH
```

Staple package (so it can be installed offline)

```sh
xcrun stapler staple Autogram-1.0.0.pkg
```

### Debugging pkg

extracting

```sh
pkgutil --expand-full Autogram.pkg autogram-pkg-extracted
```

### More Resources

- https://github.com/openjdk/jdk/tree/master/src/jdk.jpackage/macosx/classes/jdk/jpackage/internal
- https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution/resolving_common_notarization_issues
- https://developer.apple.com/library/archive/technotes/tn2206/_index.html#//apple_ref/doc/uid/DTS40007919-CH1-TNTAG205
- https://developer.apple.com/library/archive/documentation/MacOSX/Conceptual/BPFrameworks/Concepts/FrameworkAnatomy.html
- https://stackoverflow.com/questions/52905940/how-to-codesign-and-enable-the-hardened-runtime-for-a-3rd-party-cli-on-xcode
- https://stackoverflow.com/questions/74422992/what-is-the-difference-between-pkgbuild-vs-productbuild
- https://apple.stackexchange.com/questions/377232/signed-pkg-using-productbuild-distribute-but-codesign-says-code-object-is-not/377236#377236
- https://matthew-brett.github.io/docosx/flat_packages.html
- https://bugs.openjdk.org/browse/JDK-8251892
- https://bugs.openjdk.org/browse/JDK-8237490
- https://blog.macadmin.me/posts/apple-notarytool/
- https://docs.oracle.com/en/java/javase/19/jpackage/packaging-tool-user-guide.pdf
