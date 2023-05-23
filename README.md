# Autogram

Autogram je multi-platformová (Windows, MacOS, Linux) desktopová JavaFX aplikácia, ktorá slúži na podpisovanie dokumentov v súlade s eIDAS reguláciou. Používateľ ňou môže podpisovať súbory priamo alebo je možné aplikáciu jednoducho zaintegrovať do vlastného (webového) informačného systému pomocou HTTP API.

**Inštalačné balíky pre Windows, MacOS a Linux sú dostupné v časti [Releases](https://github.com/slovensko-digital/autogram/releases).** Na použitie na existujúcich štátnych weboch bude potrebné doinštalovať aj [rozšírenie do prehliadača](https://github.com/slovensko-digital/autogram-extension#readme).

![Screenshot](assets/autogram-screenshot.png?raw=true)

## CLI mód

Aplikácia Autogram poskytuje aj možnosť spustenia v príkazovom riadku (CLI móde), čo umožňuje automatizované podpisovanie súborov.

Dostupné argumenty sú:

- help alebo -h: Zobrazí nápovedu a informácie o dostupných argumentoch.
- usage alebo -u: Zobrazí príklady použitia aplikácie v CLI móde.
- sourceDirectory <adresár> alebo -sd <adresár>: Určuje zdrojový adresár, kde sa nachádzajú súbory, ktoré chcete podpísať.
- targetDirectory <adresár> alebo -td <adresár>: Určuje cieľový adresár, do ktorého sa uložia podpísané súbory.
- sourceFile <súbor> alebo -sf <súbor>: Určuje konkrétny súbor, ktorý chcete podpísať.
- rewriteFile alebo -rf: Parameter typu boolean, indikuje, či sa má prepísať existujúci podpísaný súbor v prípade, že už existuje v cieľovom adresári.
- cli alebo -c: Parameter typu boolean, indikuje, že aplikácia sa spúšťa v CLI móde.
- driver <názov> alebo -d <názov>: Určuje konkrétny ovládač, ktorý sa má použiť pri podpisovaní.

## Integrácia

Swagger dokumentácia pre HTTP API je [dostupná na githube](https://generator3.swagger.io/index.html?url=https://raw.githubusercontent.com/slovensko-digital/autogram/main/src/main/resources/digital/slovensko/autogram/server/server.yml) alebo po spustení aplikácie je tiež dostupná na http://localhost:37200/docs.

Vyvolať spustenie programu je možné priamo z webového prehliadača otvorením adresy so špeciálnym protokolom `autogram://`. Napríklad cez [autogram://go](autogram://go).

### Štýlovanie

Aplikácia momentálne podporuje len jeden štýl - štátny IDSK dizajn. Ďalšie štýly sú plánované. Štýlovanie sa však už teraz deje výhradne cez kaskádové štýly, viď [idsk.css](https://github.com/slovensko-digital/autogram/blob/main/src/main/resources/digital/slovensko/autogram/ui/gui/idsk.css)

### Texty a preklady

Momentálne sú texty v kóde "natvrdo", je plánovaná možnosť ich meniť cez properties súbory. Toto bude slúžiť aj ako zdroj pre preklady.

## Vývoj

### Predpoklady

- JDK 17 s JavaFX (viď nižšie)
- Maven
- Voliteľné: Visual Studio Code ako IDE alebo Intellij IDEA (stačí komunitná verzia).

Odporúčame používať Liberica JDK, ktoré má v sebe JavaFX, všetko je potom jednoduchšie. Po zavolaní `mnvw initialize` by sa malo stiahnuť do `target/jdkCache`.


### Build cez `mvn package`

Pripraví všetko do `./target`:

- `dependency-jars/`
- `preparedJDK/` - JLink JDK (JRE) pripravené pre bundling s aplikáciou.
- `autogram-*.jar` - JAR s aplikáciou

Následne pomocou `jpackager` vytvorí všetky spustiteľné balíčky (.msi/.exe, .dmg/.pkg, and .rpm/.deb).

## Autori a sponzori

Jakub Ďuraš, Slovensko.Digital, CRYSTAL CONSULTING, s.r.o, Solver IT s.r.o. a ďalší spoluautori.

## Licencia

Tento softvér je licencovaný pod licenciou EUPL v1.2, pôvodne vychádza z Octosign White Label projektu od Jakuba Ďuraša, ktorý je licencovaný pod MIT licenciou, a so súhlasom autora je táto verzia distribuovaná pod licenciou EUPL v1.2.

V skratke to znamená, že tento softvér môžete voľne používať komerčne aj nekomerčne, môžete vytvárať vlastné verzie a to všetko za predpokladu, že prípadné vlastné zmeny a rozšírenia tiež zverejníte pod rovnakou licenciou a zachováte originálny copyright pôvodných autorov. Softvér sa poskytuje "ber ako je", bez záväzkov.

Tento projekt je postavený výhradne na open-source softvéri, ktorý umožnuje jeho používanie tiež komerčne, aj nekomerčne.

Konkrétne využívame najmä [GPLv2+Classpath Exception license](https://openjdk.java.net/legal/gplv2+ce.html) a EU Digital Signature Service pod licenciou [LGPL-2.1](https://github.com/esig/dss/blob/master/LICENSE).
