---
description: "Čo prinášajú jednotlivé verzie Autogramu: nové funkcie, vylepšenia, opravy a bezpečnostné aktualizácie."
---

# Čo je nové

Zmeny v jednotlivých verziách Autogramu, od najnovšej. Ktorú verziu máte, zistíte v hlavnom okne po kliknutí na **O Autograme**. Ako Autogram aktualizovať, sa dočítate v časti [Aktualizácie](installation.md#aktualizacie).

::: changelog

== Pripravuje sa

## Pripravuje sa {#pripravuje-sa}

<p class="release-meta">Zmeny, ktoré prinesie ďalšia verzia</p>

**Vylepšené**

- **Iba podpisové certifikáty.** Pri výbere certifikátu Autogram ponúka iba certifikáty určené na podpisovanie. Certifikáty webových serverov, časových pečiatok a služieb na overovanie platnosti certifikátov (OCSP) už v zozname nie sú. Pozrite si [Výber certifikátu](card-and-pin.md#vyber-certifikatu).

**Opravené**

- Autogram už nezamrzne, keď počas overovania podpisov otvoríte detail podpisov. Predtým čakal na koniec overovania, čo pri pomalom sťahovaní dôveryhodných zoznamov trvalo aj niekoľko minút.

== 2.8.3

## 2.8.3 {#v2-8-3}

<p class="release-meta">26. októbra 2026 <strong class="idsk-tag idsk-tag--red">Bezpečnostná aktualizácia</strong></p>

**Nové**

- **Autogram v Microsoft Store.** Na Windows Autogram nainštalujete z Microsoft Store alebo nástrojom `winget` a nové verzie sa potom inštalujú automaticky. Ak máte staršiu verziu zo súboru `.msi`, nová vám ju pomôže odinštalovať. Pozrite si [Inštalácia](installation.md) a [Prechod na verziu z Microsoft Store](installation.md#prechod-na-microsoft-store).
- **Kontajnery s viacerými dokumentmi.** Autogram otvorí kontajner ASiC-E s viacerými dokumentmi, ukáže ich v záložkách, overí ich podpisy a pridá k nim váš podpis. Pozrite si [Viac dokumentov v jednom kontajneri](multi-document.md).
- **Podpisovanie po jednom alebo hromadne.** Keď vyberiete viac súborov, Autogram sa opýta, či si chcete každý dokument pred podpisom pozrieť, alebo ich podpísať naraz. Pozrite si [Viac dokumentov naraz](batch.md).
- **Výber z viacerých kariet.** Ak máte vložených viac kariet, napríklad občiansky preukaz a kartu od zamestnávateľa, Autogram nájde všetky a opýta sa, ktorú chcete použiť. Pozrite si [Výber karty](card-and-pin.md#vyber-karty).
- **Proxy server.** V nastaveniach aj v príkazovom riadku môžete zadať HTTP proxy, cez ktorú Autogram komunikuje s internetom. Pozrite si [Nastavenia](settings.md#ine).
- **Otváranie súborov zo správcu súborov.** Súbory PDF a kontajnery ASiC-E otvoríte v Autograme priamo zo správcu súborov, napríklad cez **Otvoriť pomocou**. Ak Autogram už beží, otvorí ich v spustenom okne.

**Vylepšené**

- Autogram si pamätá priečinok, z ktorého ste naposledy otvárali súbory.
- E-mailovú adresu podpory v chybových hláseniach môžete skopírovať.
- Výber krajín v nastaveniach a tlačidlá detailu podpisov sa dajú ovládať klávesnicou.
- Okná Autogramu majú na Windows vlastnú ikonu.

**Opravené**

- Formuláre a dokumenty XML v kódovaní windows-1250 sa zobrazujú správne.
- Dokumenty, ktorých názov obsahuje znak `?` alebo `#`, sa podpíšu bez chyby.
- Pri nesprávnom kóde k úložisku kľúčov sa zobrazí správne hlásenie.
- Prázdna cesta k úložisku kľúčov alebo k ovládaču karty v nastaveniach už nespôsobí chybu.

**Pre vývojárov**

- `POST /api/v1/sign` podpíše viac dokumentov do jedného kontajnera ASiC-E. Pozrite si [Viac dokumentov v jednom kontajneri](api.md#viac-dokumentov-v-jednom-kontajneri).
- Pri formulároch XDC a kontajneroch ASiC-E je `autoLoadEform` predvolene zapnuté.
- Keď používateľ zruší podpisovanie v upozornení pred podpisom, Autogram odpovie na požiadavku a nenechá ju visieť.
- Opravené nastavenie kanonikalizácie v požiadavkách na `/api/v1/sign`.

== 2.7.5

## 2.7.5 {#v2-7-5}

<p class="release-meta">31. júla 2026</p>

**Nové**

- **Natívna verzia pre Mac s čipom Apple.** Na počítačoch Mac s čipom M1 a novším beží Autogram bez prekladu cez Rosettu.

**Vylepšené**

- Predvolenými zdrojmi časových pečiatok sú bezplatní kvalifikovaní poskytovatelia z Belgicka a Španielska. Pozrite si [Podpis s časovou pečiatkou](timestamps.md).
- Keď v nastaveniach zmeníte krajiny dôveryhodných zoznamov, Autogram zoznamy načíta hneď, bez reštartu.
- Okno na zadanie kódu k úložisku kľúčov vysvetľuje, že niektoré úložiská kód nevyžadujú.
- Spoľahlivejšie zobrazovanie formulárov verejnej správy.

**Opravené**

- Podpísaný súbor PDF dostane príponu `.pdf` aj vtedy, keď je predvoleným formátom ASiC-E.
- Formuláre s prázdnymi hodnotami v manifeste sa otvoria bez chyby.

== 2.7.4

## 2.7.4 {#v2-7-4}

<p class="release-meta">21. apríla 2026 <strong class="idsk-tag idsk-tag--red">Bezpečnostná aktualizácia</strong></p>

- Bezpečnostná oprava pre macOS.

== 2.7.3

## 2.7.3 {#v2-7-3}

<p class="release-meta">21. marca 2026 <strong class="idsk-tag idsk-tag--red">Bezpečnostná aktualizácia</strong></p>

- Bezpečnejšie spracovanie dokumentov XML.

== 2.7.2

## 2.7.2 {#v2-7-2}

<p class="release-meta">3. marca 2026 <strong class="idsk-tag idsk-tag--red">Bezpečnostná aktualizácia</strong></p>

- Bezpečnejšie spracovanie dokumentov XML.

== 2.7.1

## 2.7.1 {#v2-7-1}

<p class="release-meta">29. novembra 2025 <strong class="idsk-tag idsk-tag--red">Bezpečnostná aktualizácia</strong></p>

- Prísnejšia kontrola formulárov verejnej správy (XDC) pred ich zobrazením.

:::

Staršie verzie a technický zoznam zmien nájdete v [releasoch na GitHube](https://github.com/slovensko-digital/autogram/releases).
