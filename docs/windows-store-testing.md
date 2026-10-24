# Testovanie verzie z Microsoft Store (MSIX)

Ako nainštalovať vývojový build MSIX balíka na ľubovoľný počítač tak, aby sa správal ako verzia z Microsoft Store, ešte
pred jej zverejnením.

Windows nainštaluje len podpísaný MSIX a podpis musí patriť vydavateľovi uvedenému v manifeste balíka
(`win.msix.publisher` v `build.properties`). Do Store sa nahráva nepodpísaný balík, podpíše ho až Microsoft. Na
testovanie ho preto podpíšeme vlastným testovacím certifikátom s rovnakým vydavateľom a testovací počítač tomuto
certifikátu dôveruje. Aplikácia má potom rovnakú identitu balíka ako zo Store a beží v rovnakom prostredí (rovnaký
kontajner, Windows nehľadá DLL cez PATH a pod.), len sa sama neaktualizuje.

## 1. MSIX balík

- **GitHub Actions:** workflow *Windows Store Packaging*. Pri tagu `vX.Y.Z` beží automaticky v rámci *Package All*,
  ručne sa spúšťa cez *Run workflow* s prostredím `dev-build` (GitHub to dovolí, až keď je workflow vo vetve `main`).
  Artefakt `autogram-<verzia>-windows-store.msix` sa stiahne ako zip, treba ho rozbaliť.
- **Lokálny build** (Windows s Git Bash):

  ```sh
  WINDOWS_PACKAGE_TYPES=msix ./mvnw package -DskipTests -Psystem-jdk
  ```

  Vznikne `target/Autogram-<verzia>.0.msix`. Bez nainštalovaného Windows SDK nastavte `WINDOWS_SDK_BIN` na priečinok
  `bin/<verzia>/x64` z balíka v kroku 2.

Vývojové buildy majú všetky verziu `1.0.0.0`.

## 2. Nástroj signtool

Je vo Windows SDK (`C:\Program Files (x86)\Windows Kits\10\bin\<verzia>\x64\signtool.exe`). Bez inštalácie ho
stiahnete z NuGet balíka (PowerShell):

```powershell
$v = (Invoke-RestMethod https://api.nuget.org/v3-flatcontainer/microsoft.windows.sdk.buildtools/index.json).versions |
    Where-Object { $_ -notmatch '-' } | Select-Object -Last 1
Invoke-WebRequest "https://api.nuget.org/v3-flatcontainer/microsoft.windows.sdk.buildtools/$v/microsoft.windows.sdk.buildtools.$v.nupkg" -OutFile buildtools.zip
Expand-Archive buildtools.zip buildtools
$signtool = (Get-ChildItem buildtools\bin\*\x64\signtool.exe | Select-Object -Last 1).FullName
```

## 3. Testovací certifikát

Stačí raz. Subjekt certifikátu musí byť presne `win.msix.publisher` z `build.properties`:

```powershell
$publisher = "CN=A3AB6846-572E-48AA-8261-AD2E48FD46E2"
$cert = New-SelfSignedCertificate -Type Custom -Subject $publisher -FriendlyName "Autogram MSIX test" `
    -KeyUsage DigitalSignature -CertStoreLocation Cert:\CurrentUser\My -NotAfter (Get-Date).AddYears(1) `
    -TextExtension @("2.5.29.37={text}1.3.6.1.5.5.7.3.3", "2.5.29.19={text}")
Export-Certificate -Cert $cert -FilePath autogram-msix-test.cer
```

`autogram-msix-test.cer` je verejná časť pre testovacie počítače. Ak bude balíky podpisovať aj niekto iný, exportujte
certifikát s privátnym kľúčom (`Export-PfxCertificate`) a zdieľajte ho len v tíme. Kto ho má, vie podpísať balík, ktorému
testovacie počítače dôverujú. Do repozitára ho nedávajte.

## 4. Podpísanie

```powershell
& $signtool sign /fd SHA256 /sha1 $cert.Thumbprint autogram-1.0.0-windows-store.msix
```

Ak certifikát nie je v úložisku používateľa, použite `/f autogram-msix-test.pfx /p <heslo>` namiesto `/sha1`. Signtool
podpíše súbor na mieste.

## 5. Inštalácia na testovacom počítači

Certifikátu raz dôverujte (PowerShell ako správca):

```powershell
Import-Certificate -FilePath autogram-msix-test.cer -CertStoreLocation Cert:\LocalMachine\TrustedPeople
```

Potom balík nainštalujte dvojklikom na `.msix` alebo:

```powershell
Add-AppxPackage -Path autogram-1.0.0-windows-store.msix
```

- Ďalší build s rovnakou verziou: pridajte `-ForceUpdateFromAnyVersion`, alebo predchádzajúci najprv odinštalujte.
- Kým Autogram beží, inštalácia zlyhá s chybou `0x80073D02`. Zatvorte ho, alebo pridajte `-ForceApplicationShutdown`.

## 6. Čo otestovať

- spustenie z ponuky Štart,
- podpis kartou s každým ovládačom, ktorý máte k dispozícii – v balíku Windows nehľadá DLL ovládačov cez PATH
  (pozri `Pkcs11Library`),
- podpis z webu cez `autogram://` a cez rozšírenie prehliadača,
- *Otvoriť pomocou* pre `.pdf` a `.asice`,
- `autogram-cli` v termináli,
- ak je nainštalovaná aj MSI verzia, Autogram ponúkne jej odinštalovanie; ak MSI verzia práve beží, Autogram ju pri
  spustení ukončí, a pripnutie MSI verzie na paneli úloh a v ponuke Štart sa pri inštalácii presunie na novú,
- dialóg o novej verzii sa nezobrazuje, aktualizácie zabezpečuje Store,
- druhé spustenie (napr. *Otvoriť pomocou*, keď už Autogram beží) sa odovzdá bežiacemu Autogramu.

Testujte na počítači, ktorý sa čo najviac podobá bežnému používateľovi, ideálne vo Windows Sandbox:

- **Bez JDK v PATH.** JavaFX hľadá svoje DLL najprv v PATH, takže s nainštalovaným JDK (napr. Liberica) sa načítajú
  z neho a nie z balíka.
- **Bez stôp po MSI verzii.** Ak existuje `%LOCALAPPDATA%\Autogram` z MSI verzie, niektoré chyby balíka sa
  neprejavia.

V balíku sa často nedá pripojiť na AF_UNIX socket: v AppData, ktorý sa presmeruje do
`%LOCALAPPDATA%\Packages\<balík>\LocalCache` (na niektorých systémoch aj `TEMP`), ani cez 8.3 skrátenú cestu, ktorú
Windows dáva do `TEMP` používateľom s menom dlhším ako 8 znakov (`C:\Users\ADMINI~1\...`). Preto v MSIX:

- NIO selektory (server pre weby, `HttpClient`) namiesto AF_UNIX socketu v `TEMP` používajú TCP loopback
  (`MsixPackage.useTcpForNioPipes`). Inak Autogram pri spustení zobrazí *Neočakávaná chyba* s
  `error.serverNotCreated`, ako pri certifikácii v Store.
- Socket na odovzdanie spustenia bežiacemu Autogramu je v `%USERPROFILE%\.autogram` (`MsixPackage.socketDirectory`).

Overíte to spustením balíka s logom (pozri nižšie) s `TEMP` a `TMP` nastavenými na 8.3 cestu existujúceho priečinka
(`dir /x`) alebo na priečinok, ktorý v `%LOCALAPPDATA%` vytvoril samotný balík.

## 7. Automatický test v čistom Windows

Skript `packaging/msix-test/oem/test-msix.ps1` urobí v čerstvo nainštalovanom Windows kroky 2 – 5 a overí spustenie:
najnovší `.msix` zo zdieľaného priečinka podpíše testovacím certifikátom s vydavateľom z manifestu (podpísanému balíku
len dôveruje), nainštaluje ho, spustí Autogram z ponuky Štart ako bežný používateľ a počká, kým server pre weby odpovie
na `http://localhost:37200/info`. Výsledok je v `shared/results/latest.txt` (prvý riadok `PASS`, `FAIL: <dôvod>` alebo
`SKIPPED: <dôvod>`). V `shared/results/<čas>/` je log testu, snímka obrazovky, varovania a chyby zo *Zobrazovača
udalostí* a pri chybe aj podrobný log Autogramu (`autogram-debug.log`).

### Linux server s KVM (Docker)

Windows beží ako virtuálny počítač v kontajneri [dockurr/windows](https://github.com/dockur/windows)
(`packaging/msix-test/compose.yaml`). Server musí mať KVM (`sudo kvm-ok`; ak je sám virtuálny, potrebuje vnorenú
virtualizáciu), aspoň 8 GB voľnej RAM a 40 GB na disku.

```sh
cd packaging/msix-test
mkdir -p shared && cp ~/autogram-1.0.0-windows-store.msix shared/
docker compose up -d
```

Prvé spustenie stiahne Windows 11 od Microsoftu a nainštaluje ho (20 – 40 minút), test sa spustí po prvom prihlásení.
Obrazovku Windows uvidíte v prehliadači na porte 8006, ten je dostupný len zo servera: `ssh -L 8006:localhost:8006
<server>` a `http://localhost:8006`. Používateľ je `AutogramTester`, heslo `autogram`. Meno je dlhšie ako 8 znakov, aby
`TEMP` obsahoval 8.3 skrátenú cestu.

Ďalší build: nakopírujte nový `.msix` do `shared/` (testuje sa najnovší podľa času zmeny) a spustite
`docker compose restart`. Test beží pri každom prihlásení a predchádzajúcu testovaciu inštaláciu najprv odinštaluje.
Súbory v `shared/results` môžu patriť používateľovi root. Nainštalovaný Windows aj stiahnuté ISO zostávajú v
`storage/`, ďalšie spustenie preto trvá asi minútu.

Podpis bez karty vyskúšate s testovacím úložiskom kľúčov:

```sh
keytool -genkeypair -storetype PKCS12 -keystore shared/autogram-test.p12 -storepass autogram -alias autogram-test \
    -keyalg RSA -keysize 2048 -validity 365 -dname "CN=Autogram MSIX Test" -ext KeyUsage=digitalSignature,nonRepudiation
```

V Autograme ho nastavte v *Nastavenia* → *Iné* → *Cesta k vlastnému úložisku kľúčov* na
`\\host.lan\Data\autogram-test.p12` a pri podpise zadajte heslo `autogram`. Pri overení sa podpis zobrazí ako neznámy,
certifikát je vlastnoručne podpísaný. Nastavenie sa pri ďalšom teste stratí, test balík odinštaluje.

Ak má každý test začínať z úplne čistého Windows, odložte si disk hneď po inštalácii. Prvýkrát spustite s prázdnym
`shared/` (test skončí `SKIPPED`) a potom:

```sh
docker compose down
sudo cp -a --sparse=always storage storage.clean
# pred každým testom
docker compose down && sudo rm -rf storage && sudo cp -a --sparse=always storage.clean storage && docker compose up -d
```

- Zmeny v `oem/` sa prejavia až pri novej inštalácii Windows (zmažte `storage/`), skript sa kopíruje do `C:\OEM`.
  Upravený `test-msix.ps1` môžete do `C:\OEM` v bežiacom Windows skopírovať aj ručne cez `\\host.lan\Data`. Bez
  reštartu Windows nevidí súbory, ktoré na serveri pribudli alebo sa zmenili v `shared/` (Samba o nich klienta
  neupovedomí), pomôže `docker exec autogram-msix-test smbcontrol smbd close-share Data`.
- Iná verzia Windows: `WINDOWS_VERSION=10 docker compose up -d` pri prvej inštalácii. Konkrétnu zostavu, napr. Insider
  Preview 29691, pripojte ako ISO cez `/custom.iso` v `compose.yaml` (ISO z *Windows Insider Preview Downloads* alebo
  vytvorené cez [UUP dump](https://uupdump.net)).

### Windows Sandbox na vlastnom počítači

Windows Sandbox je čistá kópia Windows nainštalovaného na počítači, ktorá sa po zatvorení zahodí. Potrebuje Windows
10/11 Pro alebo Enterprise a raz zapnúť (PowerShell ako správca, potom reštart):

```powershell
Enable-WindowsOptionalFeature -Online -FeatureName Containers-DisposableClientVM -All
```

Test spustíte z koreňa repozitára, výsledok bude v `packaging\msix-test\shared\results`:

```powershell
.\packaging\msix-test\sandbox.ps1 -Msix target\Autogram-1.0.0.0.msix
```

V inom čistom virtuálnom počítači spustite ako správca
`powershell -ExecutionPolicy Bypass -File test-msix.ps1 -SharedDir <priečinok s .msix>`.

## Logy a pády

Autogram nepíše logy do súboru. Takto ho spustíte v kontexte balíka s podrobným logom (predtým zatvorte bežiaci
Autogram, inak by sa spustenie odovzdalo jemu):

```powershell
$pkg = Get-AppxPackage SlubySlovensko.Digitals.r.Autogram
$log = "$env:TEMP\autogram.log"
$opts = "-Dorg.slf4j.simpleLogger.log.digital.slovensko.autogram=debug -Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.showThreadName=true"
Invoke-CommandInDesktopPackage -PackageFamilyName $pkg.PackageFamilyName -AppId Autogram -Command cmd.exe `
    -Args "/c `"set `"JAVA_TOOL_OPTIONS=$opts`" && `"$($pkg.InstallLocation)\Autogram.exe`" > `"$log`" 2>&1`""
```

Natívne pády (napr. v ovládači karty) sa do logu nedostanú. Nájdete ich v *Zobrazovači udalostí* → *Denníky systému
Windows* → *Aplikácia* ako *Application Error* pre `Autogram.exe`, so zoznamom načítaných DLL v
`C:\ProgramData\Microsoft\Windows\WER\ReportArchive`.

## Odstránenie

```powershell
Get-AppxPackage SlubySlovensko.Digitals.r.Autogram | Remove-AppxPackage
# ako správca
Get-ChildItem Cert:\LocalMachine\TrustedPeople | Where-Object Subject -eq "CN=A3AB6846-572E-48AA-8261-AD2E48FD46E2" | Remove-Item
```

Pred inštaláciou zo Store testovaciu verziu odinštalujte, má rovnakú identitu balíka.
