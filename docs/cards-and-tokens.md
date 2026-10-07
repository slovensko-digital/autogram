# Karty, ovládače a tokeny

Ako Autogram hľadá karty, vyberá certifikát a podpisuje cez PKCS#11, čo robia jednotlivé ovládače a na čo si pri zmenách
dať pozor. Väčšina poznatkov o ovládačoch je overená na macOS s reálnymi kartami (eID, MONET+ ProID+Q, I.CA), správanie
na Windows a Linuxe sa môže líšiť.

Pojmy:

- **ovládač** (`TokenDriver`) – PKCS#11 knižnica (napr. eID klient), súbor s certifikátom alebo testovací ovládač,
- **slot** – miesto, kde ovládač vidí kartu, väčšinou zodpovedá čítačke,
- **token** (`TokenSlot`) – karta v slote; eID karta má dva tokeny,
- **spojenie** (`NativePkcs11SignatureToken`) – DSS token nad SunPKCS11 providerom pre jeden slot,
- **kľúč** (`SigningKey`) – vybraný certifikát a spojenie, ktorým sa podpisuje.

## Priebeh výberu certifikátu

1. `Autogram.pickSigningKeyAndThen` (podpis) alebo `getCertificates` (web chce certifikáty) zavolá `pickTokenAndThen`.
2. `findTokenOptions` zistí, z čoho sa dá vybrať:
   - `CardReaders.check` cez PC/SC zistí, či je v niektorej čítačke karta (najviac 3 s). Robí sa, len ak je nainštalovaný
     aspoň jeden vyhľadávaný ovládač kariet.
   - Ak PC/SC neodpovedá (`NOT_RESPONDING`, videné na macOS), ovládače kariet sa nenačítajú vôbec – zasekli by sa tiež a
     niektoré by zablokovali aj ukončenie aplikácie. Ponúknu sa len ovládače bez karty (súbor s certifikátom, vlastný
     PKCS#11).
   - Inak sa ovládače rozdelia:
     - nevyhľadávané (`DRIVERS_SKIPPED_IN_TOKEN_SEARCH`, teraz len eObčanka) → `otherDrivers`,
     - ovládače kariet, keď v žiadnej čítačke nie je karta (`NO_CARD`) → `unavailableDrivers`, ani sa nenačítajú,
     - ostatné sa vyhľadajú paralelne (`TokenDriver.getSlotsWithToken`), spolu najviac 10 s.
   - Výsledok vyhľadávania ovládača:
     - `Optional.empty()` – ovládač karty nevypisuje (súbor s certifikátom, nastavený slot index) → ponúkne sa ovládač,
     - zoznam slotov – každý token je voľba; `Sig_EP` eID karty je skrytý, ak nie je zapnutý v nastaveniach; bez
       viditeľných tokenov → `unavailableDrivers`,
     - výnimka (napr. knižnica sa nedá načítať) → `otherDrivers`,
     - nestihol do 10 s → `unavailableDrivers` a `notResponding`.
3. `TokenOptions.getAutomaticOption` rozhodne, či sa dá pokračovať bez pýtania (pravidlá nižšie). Ak sa automaticky
   použitá karta nedotiahla do konca (používateľ zavrel výber certifikátu), nabudúce sa dialóg zobrazí.
4. Inak sa zobrazí dialóg výberu (`PickTokenDialogController`, `pick-token-dialog.fxml`).
5. `connectToToken` vytvorí spojenie a načíta kľúče (prihlásenie, PIN). Ak ovládač zlyhá spôsobom, z ktorého sa dá
   zotaviť, pripojí sa raz znova (`TokenDriver.recoverToken`, zatiaľ len eID).
6. Používateľ vyberie certifikát, vznikne `SigningKey`. GUI ho drží ako aktívny kľúč, kým ho po 5 minútach nečinnosti
   nezahodí časovač, používateľ nezvolí iný certifikát alebo nenastane chyba, po ktorej sa spojenie nedá použiť.

### `TokenOptions`

| Pole | Obsah |
|---|---|
| `found` | nájdené tokeny (karty) a ovládače, ktoré karty nevypisujú (súbor s certifikátom) |
| `otherDrivers` | ovládače, ktoré nevedia povedať, či majú kartu: nevyhľadávané (eObčanka) alebo im vyhľadávanie zlyhalo – ponúkajú sa vždy, nikdy sa nepoužijú automaticky |
| `unavailableDrivers` | ovládače, ktoré sa teraz nedajú použiť: nenašli kartu alebo neodpovedali |
| `notResponding` | čítačky alebo niektorý ovládač neodpovedali |

Automatické použitie (`getAutomaticOption`):

- ak sú `otherDrivers`, nikdy – používateľ môže chcieť kartu ovládača, ktorý ju nevie nájsť (napr. eObčanku popri eID),
- jediná nájdená karta sa použije, aj keď niektorý ovládač neodpovedal,
- ovládač, ktorý kartu nenašiel, sa nepoužije nikdy – skončilo by to chybou „Nepodarilo sa prečítať kartu“; a nepoužije
  sa namiesto neho ani nič iné (radšej sa používateľ dozvie, že sa karta nenašla),
- jediná voľba bez kariet (napr. len súbor s certifikátom) sa použije.

### Dialóg výberu

| Stav | Kedy | Čo sa zobrazí |
|---|---|---|
| je z čoho vybrať | neplatí `noTokenFound()` | otázka „Kde je uložený podpisový certifikát?“, jeden zoznam volieb (karty, ovládače bez vyhľadávania, súbor s certifikátom vždy posledný), zbalené „Nevidíte svoju kartu?“, „Pokračovať“ a sekundárne „Vyhľadať karty znova“ |
| žiadna karta, nič na výber | `noTokenFound()` a nie je čo vybrať | nadpis „Nenašla sa žiadna karta“, čo skontrolovať (zoznam), hlavné tlačidlo „Vyhľadať karty znova“ |
| žiadna karta, iná voľba existuje | `noTokenFound()`, ale je napr. súbor s certifikátom | otázka, pod ňou podnadpis „Nenašla sa žiadna karta“ s tým istým zoznamom, voľby |
| čítačky neodpovedajú | `notResponding` a karta sa nenašla | „Čítačka kariet neodpovedá“ a čo robiť (odpojiť a pripojiť čítačku, reštart) |

- `noTokenFound()` platí, len ak ovládače hľadali a nič nenašli. Ak je v ponuke ovládač, ktorý to nevie povedať
  (eObčanka), hláška o nenájdenej karte sa nezobrazí – jeho karta tam môže byť.
- „Vyhľadať karty znova“ vyhľadá v tom istom okne (`searchAgain`), počas hľadania je formulár vypnutý a tlačidlo hovorí
  „Vyhľadávam karty…“. Nájdenú kartu predvyberie, ale automaticky nepokračuje.
- UX podľa GOV.UK: žiadny červený chybový box (chybové hlásenie je len pre chyby vo formulári), problém pomenuje nadpis
  a obyčajný text, pomoc pre menšinu je v zbaliteľnom bloku (details), na stránke je jedno hlavné tlačidlo.

## Ovládače

### eID klient (občiansky preukaz)

- Sloty existujú, len keď je karta vložená (na macOS `C_GetSlotList(false)` bez karty vráti prázdny zoznam).
- Karta má dva tokeny: `Sig_ZEP` (kvalifikovaný certifikát) a `Sig_EP` (skrytý, kým nie je zapnutý v nastaveniach). Keď
  sú v ponuke oba, volajú sa „kvalifikovaný“ a „nekvalifikovaný“ (`TokenOptionDescription.qualified`), sloty iných kariet
  sa číslujú.
- Popis slotu je meno čítačky z PC/SC a token, napr. `Generic EMV Smartcard Reader 01; Sig_ZEP`. Duálna čítačka má v
  PC/SC dve mená (kontaktná a bezkontaktná časť, napr. `X` a `X 01`), takže tá istá karta môže mať po vložení iné meno
  čítačky.
- Má protected authentication path: PIN nepýta Autogram, ale eID klient vo vlastnom okne (iný proces) – BOK pri
  prihlásení (`C_Login`) a podpisový PIN pri podpise. Po zatvorení jeho okna je aktívna iná aplikácia, preto sa okná
  Autogramu, ktoré prídu potom, zobrazujú cez `GUIUtils.bringToForeground` (pozri macOS nižšie).
- Keď sa po prihlásení na eID inicializuje iný ovládač, ktorý prechádza všetky karty (overené s MONET), každé ďalšie
  prihlásenie zlyhá s `CKR_FUNCTION_FAILED` ešte pred oknom na PIN. Pomôže len nová inicializácia modulu – robí to `PKCS11TokenDriver.recoverToken` (eID klient si PIN potom
  vypýta sám, žiadny PIN sa neposiela dvakrát). Nová inicializácia ukončí všetky relácie modulu; odhlásenie zo staršej
  relácie by eID klienta zhodilo, preto ho `NativePkcs11SignatureToken.close` vtedy vynechá (generácia modulu).
- **eID klient zhodí celý proces** (`std::system_error: unique_lock::lock: already locked`, abort), keď iné vlákno volá
  `C_GetSlotInfo`, kým `C_Login` čaká na BOK. Stalo sa to so SunPKCS11 pollerom starého spojenia (pozri nižšie, je
  vypnutý). Do eID knižnice preto nevolať súbežne počas prihlásenia. Neoverené: či padne pri akomkoľvek súbežnom
  volaní, napr. ak web vypýta certifikáty, kým používateľ zadáva BOK v inom okne.
- `CKR_FUNCTION_FAILED` sa mapuje na „Nesprávny PIN“ (`PINIncorrectException`) – pri eID to nemusí byť pravda.
- Hromadné podpisovanie (`bulkEnabled`): Autogram si podpisový PIN vypýta sám, zapamätá si ho a pred každým podpisom sa
  prihlási `CKU_CONTEXT_SPECIFIC`. Nastavenie sa uplatní hneď po kliknutí (aj bez uloženia) a už otvorené spojenie si ho
  číta pri každom podpise. Raz sa po jeho vypnutí s aktívnym kľúčom objavila chyba „Kartu sa nepodarilo rozpoznať“, ďalej
  sa ju nepodarilo zopakovať. Kľúč sa pri zmene nastavenia zámerne nezahadzuje.

### MONET+ ProID+Q

- Vyhľadáva sa. Na macOS `/usr/local/lib/ProIDPlus/libproidqcm11.dylib` (v priečinku sú aj iné knižnice MONET).
- Hlási slot pre každú čítačku, popis slotu je presne meno z PC/SC.
- **Jeho inicializácia (`C_Initialize`) po prihlásení na eID pokazí eID klienta** (pozri eID vyššie). Neskoršie
  vyhľadávania už len čítajú sloty a eID nepokazia. V Autograme sa MONET inicializuje pri prvom hľadaní s vloženou kartou,
  teda pred prvým prihlásením na eID; ak by sa to predsa stalo, zotaví sa pripojenie k eID karte.
- PIN pýta Autogram. Ak by dostal `C_Login` bez PIN-u, otvorí vlastné natívne okno na PIN – preto
  `PasswordManager.getPassword` pri zrušení hodí `PasswordNotProvidedException` a prihlásenie sa vôbec nezavolá.

### eObčanka (český občiansky preukaz)

- Nevyhľadáva sa: videli sme ju zaseknúť sa v `C_Initialize`. `PKCS11.getInstance` je synchronizovaný, takže zasekla by
  všetky PKCS#11 ovládače až do reštartu, aplikácia by sa nedala ukončiť a bez finalizácie pri ukončení padá JVM.
- Ponúka sa vždy, keď je nainštalovaná (okrem nereagujúcich čítačiek), a nikdy sa nepoužije automaticky. Kým je
  nainštalovaná, výber sa zobrazí pri každom podpise, aj pri jedinej nájdenej karte a aj keď je jediným ovládačom.
- CLI s `--driver cz_eid` ju použije priamo.

### I.CA SecureStore

- Issue #703: na macOS sa Autogram 2.7.5 po podpise zasekne pri ukončení (natívny deštruktor čaká na heartbeat vlákno
  knižnice); I.CA uvádza, že chýba `C_Finalize`. Táto vetva volá `C_Finalize` pri ukončení pre každý použitý modul
  (`Pkcs11TokenSlots`) a ak sa zasekne, proces skončí bez natívnych deštruktorov (`NativeExit`). Mimo GUI sa zaseknutie
  nepodarilo vyvolať (SecureStore 8.3, prihlásenie aj podpis priamo cez PKCS#11 aj cez DSS so SunPKCS11), takže oprava
  nie je overená.
- Karta Starcos 3.7 podpisuje len `CKM_RSA_PKCS` (haš sa počíta mimo karty).
- Knižnica má vlastné vlákna (sledovanie čítačiek, pool), ktoré bežia, kým je modul inicializovaný.

### Ostatné

- **Gemalto IDPrime 940** – kvalifikovaný certifikát býva v inom slote karty (pomocný text pri chybe).
- **Windows, eID klient bez Visual C++ 2015 runtime** – knižnica sa nedá načítať (`IOException` pri
  `PKCS11.getInstance`). `PKCS11TokenDriver.getSlotsWithToken` vtedy hodí výnimku, ovládač je v `otherDrivers` a po jeho
  výbere sa zobrazí dialóg `PkcsEidWindowsDllException`. Ostatné chyby pri čítaní slotov znamenajú „žiadna karta“ (napr.
  Windows bez čítačky, keď nebeží služba Smart Card).
- **Súbor s certifikátom** – heslo sa pýta pred vytvorením spojenia, ponúka sa vždy.
- **Vlastný PKCS#11** – `needsInsertedCard()` je `false`, môže mať softvérové tokeny (SoftHSM).
- **Testovací ovládač** – zapne ho súbor `fakeTokenDriver` v pracovnom adresári (pozri `DEVELOPER.md`).

## PKCS#11 a SunPKCS11

- `PKCS11.getInstance` drží moduly podľa cesty ku knižnici: `C_Initialize` prebehne raz za proces a modul ostane
  inicializovaný až do ukončenia (alebo novej inicializácie, len eID). Metóda je synchronizovaná – zaseknutá
  inicializácia jedného ovládača zablokuje všetky.
- `Pkcs11TokenSlots` číta sloty tou istou inštanciou modulu ako SunPKCS11, preto je bezpečné volať ho pred vytvorením
  spojenia. Pri ukončení finalizuje moduly paralelne (najviac 5 s), zaseknutý modul ukončí proces cez `_exit` (vyžaduje
  `--enable-native-access`).
- Každé spojenie má vlastný SunPKCS11 provider (`SunPKCS11-SmartCard<UUID>`). Keď SunPKCS11 zistí, že karta zmizla,
  spustí poller, ktorý vo vlákne kontroluje slot (`C_GetSlotInfo`), a ten prežije aj zatvorenie spojenia. Autogram sa po
  vytiahnutí karty vždy pripája nanovo, preto `NativePkcs11SignatureToken` poller vypína nastavením
  `insertionCheckInterval = Integer.MAX_VALUE` (interval používa len poller, minimum je 100 ms).
- Vytiahnutá karta: SunPKCS11 hodí `ProviderException("Token has been removed")`, Autogram z toho spraví
  `TokenRemovedException` a GUI zahodí aktívny kľúč. Staré spojenie už nefunguje ani po vrátení karty, ďalší podpis
  vyhľadá karty znova.
- Prázdny PIN z callbacku znamená `C_Login(NULL)`, na čo niektoré ovládače reagujú vlastným oknom (MONET).
- Prístup k `sun.security.pkcs11.wrapper` vyžaduje `--add-exports` a `--add-opens` (sú v `.vscode/launch.json` a
  `src/main/scripts/package.sh`). Testy ich nemajú, preto v testoch reflexia na wrapper zlyhá skôr, než sa knižnica načíta.

## Chyby (`AutogramException.createFromDSSException`)

- `AutogramException` medzi príčinami sa vráti tak, ako je (napr. zrušené zadanie PIN-u obalené SunPKCS11).
- `Token has been removed` → `TokenRemovedException` (od 11/2023 to pre `ProviderException` nefungovalo).
- Hlášky o slot indexe v `ProviderException` → `InitializationFailedException` / `SlotIndexOutOfRangeException`.
- `TokenNotRecognizedException` nesie pôvodnú chybu, je vidno v detaile chyby.
- GUI po chybe podpisu zahodí kľúč pri `TokenRemovedException` a `TokenNotRecognizedException`; po chybe pri načítaní
  kľúčov vždy.
- Server pri vytiahnutej karte v dávke vráti `SIGNING_FAILED` (predtým `UNRECOGNIZED_DSS_ERROR`), HTTP 502.

## macOS: fokus okien

JavaFX na macOS nevie aktivovať aplikáciu, keď je vpredu iná (okno eID klienta, prehliadač). `show()`, `showOnTop`
(`alwaysOnTop` + `toFront`) aj `toFront()` + `requestFocus()` okno nanajvýš ukážu, fokus ostane v inej aplikácii.
Aktivovať ju vie len AWT `Desktop.requestForeground(true)` – volá ho `GUIUtils.bringToForeground` (len na macOS, inde sa
AWT nenačíta). Používa sa pre výber karty a certifikátu, súhlas s čítaním certifikátov, výsledky podpisu, chyby, PDF/A a
okná cez `showOnTop` (podpis z prehliadača). Je to prvé použitie AWT v Autograme – modul `java.desktop` je v runtime, ale
treba sledovať vedľajšie účinky v zabalenej aplikácii (Dock, menu).

## Testovanie s kartami na macOS

- Vlastná inštancia popri bežiacej: single-instance zámok je v `~/Library/Application Support/Autogram`, preto spustiť
  s `-Duser.home=<iný adresár>` a s iným portom v `--url=autogram://listen?...&port=37201...`. JVM argumenty sú v
  `.vscode/launch.json`, classpath z `./mvnw -Psystem-jdk dependency:build-classpath`, debug logy
  `-Dorg.slf4j.simpleLogger.log.digital.slovensko.autogram=debug`. Testy: `./mvnw -Psystem-jdk test`.
- Java Preferences na macOS (nastavenia, naposledy použitá karta) sú spoločné aj pri inom `user.home`.
- Podpisové okno otvorí `POST /sign` (JSON s dokumentom), certifikáty `GET /certificates`.
- Okná sa dajú ovládať cez `osascript` a System Events (prístupnosť): tlačidlá podľa `description`, voľby v dialógu
  výberu sú `radio button` v `scroll area 1`. `screencapture` potrebuje povolenie Nahrávanie obrazovky.
- Bez zásahu do kariet: PC/SC zoznam čítačiek a ATR (zdieľané pripojenie, `disconnect(false)`), PKCS#11 zoznam slotov
  cez wrapper (`--add-exports`), bez prihlásenia.
- PIN zadáva vždy človek v okne ovládača alebo Autogramu. eID: BOK pri prihlásení, podpisový PIN pri podpise. Zrušenie
  okna na PIN dokazuje, že sa prihlásenie k PIN-u dostalo.
- Pád JVM: `~/Library/Logs/DiagnosticReports/java-*.ips` ukáže vlákno, ktoré padlo, aj natívne knižnice v ňom.
- Stav čítačiek (`NO_CARD`, `NOT_RESPONDING`) sa dá vynútiť dočasnou úpravou `Supplier<CardReaders.State>` v konštruktore
  `Autogram`; kartu programovo vybrať nejde.

### Overené pokusy (macOS, eID klient, MONET+ ProID+Q)

| Poradie | Druhé prihlásenie na eID |
|---|---|
| prihlásenie, odhlásenie, prvá inicializácia MONET, prihlásenie | `CKR_FUNCTION_FAILED` po 36 ms, bez okna na PIN |
| prihlásenie, odhlásenie, prihlásenie (bez MONET) | funguje |
| inicializácia MONET, prihlásenie, odhlásenie, vyhľadanie MONET, prihlásenie | funguje |
| po novej inicializácii eID modulu | funguje (okno na PIN) |

V Autograme s kartami eID, I.CA a MONET (MONET vyhľadávaný) prešli bez obnovy: podpis, požiadavka webu na certifikáty
(zrušená aj s výberom eID), ďalší podpis, „Podpísať iným certifikátom“.

## Zvážené a zamietnuté

- **Opakovanie podpisu po obnove spojenia** (`SigningKey`) – odstránené, v žiadnom reálnom postupe sa nespustilo.
- **Hľadať MONET len pri karte, ktorú iný ovládač nepozná** (párovanie čítačiek podľa mien) – netreba, MONET kazí eID
  len inicializáciou po prihlásení.
- **Zahodiť kľúč pri zmene hromadného podpisovania** – zámerne nie.
- **Odkaz „Iné úložisko certifikátu“ a oddeľovač „alebo“ v zozname** – odstránené, ovládače bez vyhľadávania sú bežné
  voľby.
