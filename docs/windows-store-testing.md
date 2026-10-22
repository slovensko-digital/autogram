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
  WINDOWS_PACKAGE_TYPES=msix ./mvnw package -DskipTests
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
- dialóg o novej verzii sa nezobrazuje, aktualizácie zabezpečuje Store.

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
