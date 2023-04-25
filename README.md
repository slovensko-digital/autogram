# Autogram

Autogram je multi-platformová (Windows, macOS, Linux) desktopová JavaFX aplikácia, ktorá slúži na podpisovanie dokumentov v súlade s eIDAS reguláciou. Používateľ s ňou môže podpisovať súbory priamo alebo je možné aplikáciu jednoducho zaintegrovať do vlastného (webového) informačného systému pomocou HTTP API. 

Na použitie na existujúcich štátnych weboch je potrebné ešte doinštalovať [rozšírenie do prehliadača](https://github.com/slovensko-digital/signer-switcher-extension).

![Screenshot](assets/autogram-screenshot.png?raw=true)

## Integrácia

Swagger dokumentácia pre HTTP API je [dostupná na githube](https://generator3.swagger.io/index.html?url=https://raw.githubusercontent.com/slovensko-digital/autogram/main/src/main/resources/digital/slovensko/autogram/server/server.yml) alebo po spustení aplikácie je tiež dostupná na http://localhost:37200/docs. 

Program je taktiež možné spustil priamo z webového prehliadača pomocou custom schema `autogram://`. 

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

Tento softvér je licencovaný pod licenciou EUPL v1.2, pôvodne vychádza z Octosign White Label projektu od Jakuba Ďuraša, ktorý je licencovaný pod MIT licenciou a so súhlasom autora je táto verzia distribuovaná pod licenciou EUPL v1.2.

V skratke to znamená, že môžete tento softvér voľne používať v komerčne aj nekomerčne, môžete vytvárať vlastné verzie a to všetko za predpokladu, že prípadné vlastné zmeny a rozšírenia tiež zverejníte pod rovnakou licenciou a zachováte originálny copyright pôvodných autorov. Softvér sa poskytuje "ber ako je", bez záväzkov.

Tento projekt je postavený výhradne na open-source softvéri, ktorý umožnuje jeho používanie  tiež komerčne, aj nekomerčne.

Konkrétne využívame najmä [GPLv2+Classpath Exception license](https://openjdk.java.net/legal/gplv2+ce.html), a EU Digital Signature Service pod licenciou [LGPL-2.1](https://github.com/esig/dss/blob/4b82afb014f0836eb282e1e3498ab4bb843ef321/LICENSE).
