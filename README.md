# Autogram
[🇸🇰 Slovenská verzia](README-SK.md)

Autogram is a multi-platform (Windows, MacOS, Linux) desktop JavaFX application for signing and verifying documents in accordance with the European eIDAS regulation. Users can sign files directly or easily integrate the application into their own (web) information system using HTTP API. Signing can also be initiated from the command line, which is suitable for batch signing large numbers of files at once.

**Installation packages for Windows, MacOS, and Linux are available in the [Releases](https://github.com/slovensko-digital/autogram/releases) section.** If you want to use Autogram on existing Slovak government websites, you will need to install a [browser extension](https://github.com/slovensko-digital/autogram-extension#readme) as well.

![Screenshot](assets/autogram-screenshot-en.png?raw=true)


## Integration

Swagger documentation for the HTTP API is [available on GitHub](https://generator3.swagger.io/index.html?url=https://raw.githubusercontent.com/slovensko-digital/autogram/main/src/main/resources/digital/slovensko/autogram/server/server.yml) or after starting the application at http://localhost:37200/docs.

You can trigger the application to run directly from a web browser by opening an address with the special protocol `autogram://`. For example, via `autogram://go`.

## Console Mode

Autogram can also be run from the command line (CLI mode). Detailed information about the switches is described in the help after running `autogram --help`, or `autogram-cli --help` on Windows.

### Styling

The application currently supports only one style - the state IDSK design. Additional styles are planned. However, styling already happens exclusively through cascading style sheets, see [idsk.css](https://github.com/slovensko-digital/autogram/blob/main/src/main/resources/digital/slovensko/autogram/ui/gui/idsk.css)

## Supported Cards

Currently, we support commonly used cards and their drivers:
- Any PKCS#11 compatible card (by setting a path to driver)
- Native support: Slovak ID card (eID client), Czech ID card (eObčanka) I.CA SecureStore, MONET+ ProID+Q, Gemalto IDPrime 940

Adding more cards is relatively easy as long as they use PKCS#11.

## Development

### Prerequisites

- JDK 24 with JavaFX (see below)
- Maven
- Optional: Visual Studio Code as IDE or Intellij IDEA (community version is sufficient).

We recommend using Liberica JDK, which includes JavaFX, making everything simpler. After calling `./mvnw initialize`, it should download to `target/jdkCache`.

### Build

Running `./mvnw package` prepares everything in `./target`:

- `dependency-jars/`
- `preparedJDK/` - JLink JDK (JRE) prepared for bundling with the application.
- `autogram-*.jar` - JAR with the application

Then using `jpackage`, it creates all executable packages (.msi/.exe, .dmg/.pkg, and .rpm/.deb).
The packaging script automatically detects if `jpackage` supports the newer `--arch` option
and falls back to `--target-arch` on older JDK versions.
On macOS, jpackage signs the installer by default. To build an unsigned macOS package, set
`mac.sign=0` in `src/main/resources/digital/slovensko/autogram/build.properties` before running
`./mvnw package`.

```sh
./mvnw versions:set -DnewVersion=$(git describe --tags --abbrev=0 | sed -r 's/^v//g')
./mvnw package
```

#### Debian/Ubuntu

```sh
sudo apt install openjdk-24-jdk maven binutils rpm fakeroot
```

#### Fedora

```sh
sudo dnf install java-24-openjdk maven rpm-build
```

#### Linux Docker compose

There is a `docker-compose.yml` with 3 services to package 3 Linux distributables - `Ubuntu 22.04`, `Debian 11` and `Fedora 41`. Run:

```
docker compose up --build
```

And the resulting packages will appear in `packaging/output/`.


## Authors and Sponsors

Jakub Ďuraš, Slovensko.Digital, CRYSTAL CONSULTING, s.r.o, Solver IT s.r.o. and other co-authors.

## License

This software is licensed under EUPL v1.2, originally derived from the Octosign White Label project by Jakub Ďuraš, which is licensed under MIT license, and with the author's permission, this version is distributed under EUPL v1.2 license.

In short, this means that you can freely use this software commercially and non-commercially, you can create your own versions, all provided that you also publish any of your changes and extensions under the same license and preserve the original copyright of the original authors. The software is provided "as is", without warranties.

This project is built exclusively on open-source software, which also allows its use both commercially and non-commercially.

Specifically, we mainly use [GPLv2+Classpath Exception license](https://openjdk.java.net/legal/gplv2+ce.html) and EU Digital Signature Service under the [LGPL-2.1](https://github.com/esig/dss/blob/master/LICENSE) license.
