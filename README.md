# White Label Signer

Customizable, simple, cross-platform (Windows, macOS, Linux) desktop app that can be used to create signatures compliant with the eIDAS Regulation and be integrated with your (web) application.

![Screenshot](https://github.com/octosign/branding/blob/main/screenshots/White%20Label.png?raw=true)

## Integration

### Client

You can communicate with the signer via HTTP, either directly following the API Specification or using a client library, see [octosign/white-label-client](https://github.com/octosign/white-label-client).

Although it was made with the web in mind, it can be used from other desktop applications, from CLI, and over the local network if your use case requires it.
Your mileage may vary though, if you have a use case we do not yet cover, please open an issue.

### Styling

This application is made with easy custom styling in mind.

Any custom styles should be ideally placed in [src/main/resources/com/octosign/whitelabel/ui/overrides.css](https://github.com/octosign/white-label/blob/main/src/main/resources/com/octosign/whitelabel/ui/overrides.css). They can override any main window or dialog styles without causing conflicts with upstream (this repository).

### Strings and Defaults

You can also override various messages and defaults by editing [src/main/resources/com/octosign/whitelabel/ui/main.properties](https://github.com/octosign/white-label/blob/main/src/main/resources/com/octosign/whitelabel/ui/main.properties). For the time being, this can also serve as a poor man's solution to localization.

## Development

### Requirements

- JDK 17 with JavaFX (see below)
- Maven
- Optional: Visual Studio Code as IDE as you can utilize the versioned settings (launch config).

Development can be done only using JDK with bundled JavaFX. It is highly recommended to use the JDK that is downloaded and bundled with the application. You can run `mvn initialize` to download this JDK to `target/jdkCache/`. This JDK can then be used to configure the IDE. Using IDEs that support relative paths, you can use path `./target/jdkCache/REPLACE_WITH_SDK_DIR_NAME`. For example, on VSCode, it can be set using setting `java.configuration.runtimes` and the following example entry:

```
{
    "name": "JavaSE-17",
    "path": "/home/jakub/octosign/white-label/target/jdkCache/LIBERICA_jdk17.0.7+7_linux_amd64-full"
}
```

Having the same JDK (bundled with JavaFX) during the development and distribution lowers the probability of problems with compatibility and simplifies the building process.

### Goals

#### `mvn package`

Prepares all essential application artifacts in `./target`:

- `dependency-jars/`
- `preparedJDK/` - JLink-ed JDK (JRE) prepared for bundling with the application.
- `whitelabel-*.jar` - JAR with the application itself.

Assembles a directory and calls `jpackager` to create distributable packages (.msi/.exe, .dmg/.pkg, and .rpm/.deb).

If you want to create installers, you additionally need:

- Window: [WiX Toolset](https://wixtoolset.org/).
- macOS: Xcode.
- Linux: Distribution-specific tools for building rpm or deb packages, e.g., `rpm-build` on Fedora. This script expects to create both `rpm` and `deb` on distributions derived from Debian, which should be possible if you install `rpm`.

## License

This software is licensed under the MIT License.
In short, you are free to do whatever you want with this code, including forking and bundling as a commercial proprietary application, as long as you include the original copyright, license notice, and accept that you cannot hold any of the authors liable.

This project uses exclusively Open Source Software (OSS) that is safe to bundle with permissive and proprietary software provided you comply with the license requirements.
To comply with the requirements, make sure not to remove any copyright notices and info about used OSS.

Specifically, this project uses OpenJDK and OpenJFX that are licensed under [GPLv2+Classpath Exception license](https://openjdk.java.net/legal/gplv2+ce.html), and EU Digital Signature Service under [LGPL-2.1](https://github.com/esig/dss/blob/4b82afb014f0836eb282e1e3498ab4bb843ef321/LICENSE).

See [Octosign White Label Legal](https://whitelabel.octosign.com/legal) for more information.
