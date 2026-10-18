---
description: "Ako podpisovať Autogramom z príkazového riadka a v skriptoch, prehľad prepínačov a kam sa podpísané súbory uložia."
---

# Príkazový riadok (CLI)

Autogram vie podpisovať aj bez grafického rozhrania, čo sa hodí na podpisovanie veľkého počtu súborov naraz alebo v skriptoch. Na Windows použite `autogram-cli`, na macOS a Linuxe `autogram`.

```sh
autogram --cli dokument.pdf
autogram --cli -s dokument.pdf -t podpisane/dokument.pdf --parents
autogram --cli -s priecinok -t priecinok_podpisane -f
autogram --cli -s dokument.pdf -d eid --tsa-server http://timestamp.sectigo.com/qualified
```

Úplný zoznam prepínačov vypíše `autogram --help`, príklady `autogram --usage`.

## Prepínače

| Prepínač | Význam |
| --- | --- |
| `-c`, `--cli` | Spustí Autogram v príkazovom riadku. Bez neho sa spustí grafické rozhranie. |
| `-s`, `--source` | Súbor alebo priečinok na podpis. Dá sa zadať aj ako jediný argument bez prepínača. |
| `-t`, `--target` | Kam uložiť podpísaný súbor alebo priečinok. Musí byť rovnakého druhu ako zdroj (súbor alebo priečinok). |
| `-f`, `--force` | Prepíše existujúce súbory. |
| `--parents` | Vytvorí chýbajúce priečinky cieľa. |
| `-d`, `--driver` | Karta alebo úložisko: `eid`, `cz_eid`, `secure_store`, `monet`, `gemalto`, `keystore`, `custom_pkcs11`. Bez neho Autogram vyhľadá všetky karty a ak ich nájde viac, opýta sa, ktorú použiť. |
| `--slot-id` | Poradie slotu v zozname slotov, ktoré hlási zvolený ovládač. Bez neho Autogram ponúkne všetky nájdené sloty. |
| `--keystore` | Cesta k súboru s certifikátom (JKS) pre `-d keystore`. |
| `--pkcs11-driver-path` | Cesta k ovládaču karty pre `-d custom_pkcs11`. |
| `--pdf-level` | Typ podpisu pre PDF: `PAdES_BASELINE_B` (predvolený), `XAdES_BASELINE_B` alebo `CAdES_BASELINE_B`. |
| `--tsa-server` | Adresa servera časových pečiatok. Ak je zadaná, k podpisom sa pridá časová pečiatka. Viac adries oddeľte čiarkou. |
| `--pdfa` | Pred podpisom skontroluje súlad PDF s formátom PDF/A. |
| `--plain-xml` | Povolí podpisovanie XML súborov, ktoré nie sú formulárom verejnej správy. |
| `--en319132` | Podpisuje podľa noriem ETSI EN 319 132 a EN 319 122. |

PIN k vybranej karte zadávate v príkazovom riadku, pri občianskom preukaze v okne eID klienta.

## Kam sa podpísané súbory uložia

- Bez `-t` sa podpísaný súbor uloží vedľa pôvodného s príponou `_signed`, napríklad `dokument_signed.pdf`. Ak taký súbor existuje, pridá sa číslo: `dokument_signed (1).pdf`.
- Pri priečinku bez `-t` vznikne vedľa neho priečinok s príponou `_signed`.
- S `-t` sa existujúci cieľ prepíše iba s `-f`, inak podpisovanie skončí chybou.

## Kontajnery s viacerými dokumentmi

Každý vstupný súbor sa podpisuje samostatne. Existujúci kontajner ASiC-E s viacerými dokumentmi sa podpíše ako celok, rovnako ako v grafickom rozhraní: podpis pokryje všetky dokumenty v kontajneri.
