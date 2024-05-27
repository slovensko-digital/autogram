# Autogram

Autogram je multi-platformová (Windows, MacOS, Linux) desktopová JavaFX aplikácia, ktorá slúži na podpisovanie a overovanie dokumentov v súlade s európskym nariadením eIDAS. Používateľ ňou môže podpisovať súbory priamo alebo je možné aplikáciu jednoducho zaintegrovať do vlastného (webového) informačného systému pomocou HTTP API. Podpisovanie je možné spúšať aj z príkazového riadku, čo je vhodné pre hromadné podpisovanie veľkého množstva súborov naraz.

**Inštalačné balíky pre Windows, MacOS a Linux sú dostupné v časti [Releases](https://github.com/slovensko-digital/autogram/releases).** Na použitie na existujúcich štátnych weboch bude potrebné doinštalovať aj [rozšírenie do prehliadača](https://github.com/slovensko-digital/autogram-extension#readme).

![Screenshot](assets/autogram-screenshot.png?raw=true)

## Integrácia

Swagger dokumentácia pre HTTP API je [dostupná na githube](https://generator3.swagger.io/index.html?url=https://raw.githubusercontent.com/slovensko-digital/autogram/main/src/main/resources/digital/slovensko/autogram/server/server.yml) alebo po spustení aplikácie je tiež dostupná na http://localhost:37200/docs.

Vyvolať spustenie programu je možné priamo z webového prehliadača otvorením adresy so špeciálnym protokolom `autogram://`. Napríklad cez `autogram://go`.

## Konzolový mód

Autogram je možné spúšťať aj z príkazového riadku (CLI mód). Detailné informácie o prepínačoch sú popísané v nápovede po spustení `autogram --help`, resp. `autogram-cli --help` na Windows.

### Štýlovanie

Aplikácia momentálne podporuje len jeden štýl - štátny IDSK dizajn. Ďalšie štýly sú plánované. Štýlovanie sa však už teraz deje výhradne cez kaskádové štýly, viď [idsk.css](https://github.com/slovensko-digital/autogram/blob/main/src/main/resources/digital/slovensko/autogram/ui/gui/idsk.css)

### Texty a preklady

Momentálne sú texty v kóde "natvrdo", je plánovaná možnosť ich meniť cez properties súbory. Toto bude slúžiť aj ako zdroj pre preklady.

## Podporované karty

Momentálne podporujeme na Slovensku bežne používané karty a ich ovládače:
- občiansky preukaz (eID klient)
- I.CA SecureStore
- MONET+ ProID+Q
- Gemalto IDPrime 940

Doplniť ďalšie je pomerne ľahké pokiaľ používajú PKCS#11.

## Štátne elektronické formuláre

### slovensko.sk

Autogram dokáže v stand-alone režime otvárať a podpisovať všetky formuláre zverejnenené v [statickom úložisku](https://www.slovensko.sk/static/eForm/dataset/) na slovensko.sk. Pri integrácii cez API je možné nastaviť v body `parameters.autoLoadEform: true`. Vtedy sa potrebné XSD, XSLT a ďalšie metadáta stiahnu automaticky podľa typu podpisovaného formulára.

### Obchodný register SR

Navonok rovnako ako formuláre zo slovensko.sk fungujú aj ORSR formuláre. Autogram deteguje typ formulára automaticky a pri API je potrebné nastaviť spomínaný parameter. Technicky sa potom ORSR formuláre odlišujú v tom, že používajú embedované schémy v datacontainer-i oproti referencovaným schémam v iných formulároch.

Ak je pri podpise cez API zapnutý parameter `autoLoadEform` a formulár je z ORSR, automaticky sa nastaví vytváranie podpisu s embedovanou schémou. Pri poskytnutí XSD a XSLT v parametroch bez `autoLoadEform` je potrebné ešte nastaviť v body `parameters.embedUsedSchemas: true`, aby boli schémy embedované.

### Finančná správa SR

Podpísané formuláre v `.asice` kontajneroch dokáže Autogram rovanko automaticky detegovať v stand-alone režime a cez API pri použití `autoLoadEform`.

Avšak, pri podpisovaní je potrebné Autogramu explicitne určiť typ formuláru. V stand-alone režime je potrebné, aby názov súbor obsahoval: `_fs<identifikator>` a mal príponu: `.xml`. Napríklad:
```
moj-dokument_fs792_772.xml
dalsi-dokument_fs792_772_test.xml
nazov-firmy_fs2682_712_nieco-dalsie.xml
```

Pri podpisovaní cez API je potrebné nastaviť v body `parameters.fsFormId: "<identifikator>"`.  Identifikátory formulárov finančnej správy je možné získať z [nášho zoznamu](https://forms-slovensko-digital.s3.eu-central-1.amazonaws.com/fs/forms.xml) ako atribút `sdIdentifier`.

## Vývoj

### Predpoklady

- JDK 17 s JavaFX (viď nižšie)
- Maven
- Voliteľné: Visual Studio Code ako IDE alebo Intellij IDEA (stačí komunitná verzia).

Odporúčame používať Liberica JDK, ktoré má v sebe JavaFX, všetko je potom jednoduchšie. Po zavolaní `./mvnw initialize` by sa malo stiahnuť do `target/jdkCache`.

### Build

Spustenie `./mvnw package` pripraví všetko do `./target`:

- `dependency-jars/`
- `preparedJDK/` - JLink JDK (JRE) pripravené pre bundling s aplikáciou.
- `autogram-*.jar` - JAR s aplikáciou

Následne pomocou `jpackage` vytvorí všetky spustiteľné balíčky (.msi/.exe, .dmg/.pkg, a .rpm/.deb).

```sh
./mvnw versions:set -DnewVersion=$(git describe --tags --abbrev=0 | sed -r 's/^v//g')
./mvnw package
```

#### Debian/Ubuntu

```sh
sudo apt install openjdk-17-jdk maven binutils rpm fakeroot
```

#### Fedora

```sh
sudo dnf install java-17-openjdk maven rpm-build
```

## Autori a sponzori

Jakub Ďuraš, Slovensko.Digital, CRYSTAL CONSULTING, s.r.o, Solver IT s.r.o. a ďalší spoluautori.

## Licencia

Tento softvér je licencovaný pod licenciou EUPL v1.2, pôvodne vychádza z Octosign White Label projektu od Jakuba Ďuraša, ktorý je licencovaný pod MIT licenciou, a so súhlasom autora je táto verzia distribuovaná pod licenciou EUPL v1.2.

V skratke to znamená, že tento softvér môžete voľne používať komerčne aj nekomerčne, môžete vytvárať vlastné verzie a to všetko za predpokladu, že prípadné vlastné zmeny a rozšírenia tiež zverejníte pod rovnakou licenciou a zachováte originálny copyright pôvodných autorov. Softvér sa poskytuje "ber ako je", bez záväzkov.

Tento projekt je postavený výhradne na open-source softvéri, ktorý umožnuje jeho používanie tiež komerčne, aj nekomerčne.

Konkrétne využívame najmä [GPLv2+Classpath Exception license](https://openjdk.java.net/legal/gplv2+ce.html) a EU Digital Signature Service pod licenciou [LGPL-2.1](https://github.com/esig/dss/blob/master/LICENSE).
