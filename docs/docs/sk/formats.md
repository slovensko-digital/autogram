---
description: "Aké súbory Autogram zobrazí a podpíše, aké typy podpisov vytvára (PAdES, XAdES, CAdES) a ako pracuje s formulármi verejnej správy a PDF/A."
---

# Formáty a typy podpisov

Táto stránka vysvetľuje, aké súbory Autogram vie podpísať a zobraziť a aký podpis z nich vznikne. Pri bežnom podpisovaní ju nepotrebujete, Autogram vyberie vhodný formát sám.

## Čo Autogram zobrazí {#co-autogram-zobrazi}

Podpísať viete akýkoľvek súbor. Zobraziť pred podpisom vie Autogram tieto:

- PDF,
- obrázky PNG a JPEG,
- textové súbory,
- HTML,
- elektronické formuláre slovenskej verejnej správy (XML), ktoré zobrazí v čitateľnej podobe.

Ostatné súbory, napríklad dokumenty Word, podpíšete bez náhľadu a Autogram vás na to [upozorní](warnings.md#dokument-nie-je-mozne-zobrazit).

PDF chránené heslom podpísať nemožno, najprv z neho heslo odstráňte.

## Typy podpisov

Autogram vytvára podpisy troch typov:

| Typ | Kde je podpis | Kedy sa použije |
| --- | --- | --- |
| **PAdES** | priamo vo vnútri PDF, výsledok je opäť PDF | predvolene pre PDF |
| **XAdES** | v kontajneri ASiC-E spolu s dokumentom | pre všetky ostatné súbory, pre PDF podľa nastavenia |
| **CAdES** | v kontajneri ASiC-E spolu s dokumentom | pre PDF podľa nastavenia |

PAdES je všeobecne prijímaný a najjednoduchší: podpísané PDF otvoríte v každom prehliadači PDF. Niektoré úrady však vyžadujú XAdES alebo CAdES. Typ podpisu pre PDF zmeníte v [Nastaveniach](settings.md#podpisovanie) voľbou **Typ podpisu pre PDF**.

Ak už dokument obsahuje podpis, Autogram pridá podpis rovnakého typu a nastavenie ignoruje.

## Kontajner ASiC-E

Kontajner ASiC-E (prípona `.asice`) je súbor, ktorý obsahuje podpísaný dokument a jeho podpisy. Môže obsahovať aj viac dokumentov podpísaných spoločne, pozrite si [Viac dokumentov v jednom kontajneri](multi-document.md).

Kontajner otvoríte a overíte v Autograme. Dokument z neho dostanete aj bez Autogramu: kontajner je ZIP archív, takže ho po premenovaní na `.zip` rozbalíte.

## Elektronické formuláre verejnej správy

Formuláre slovenskej verejnej správy (napríklad zo slovensko.sk) sú XML súbory, často v obálke XML Data Container. Autogram ich zobrazí v čitateľnej podobe podľa vzoru formulára a podpíše ich podpisom XAdES v kontajneri ASiC-E, ako to úrady vyžadujú.

Formuláre Finančnej správy, napríklad daňové priznania, podpíšete najčastejšie priamo na portáli Finančnej správy s [rozšírením do prehliadača](websites.md#statne-weby-s-rozsirenim-do-prehliadaca) alebo v aplikácii, ktorá ich Autogramu posiela. Ako to robia vývojári, je na stránke [Integrácia cez HTTP API](api.md#formulare-financnej-spravy).

XML súbor, ktorý nie je takýmto formulárom, Autogram predvolene nepodpíše. Ak ho podpísať potrebujete, zapnite v [Nastaveniach](settings.md#podpisovanie) voľbu **Podpisovanie neznámych XML**.

## Časové pečiatky

Časová pečiatka od dôveryhodného poskytovateľa potvrdzuje, že podpis existoval v určitom čase. Podpis s ňou ostane overiteľný aj po skončení platnosti certifikátu. Pri kvalifikovanom elektronickom podpise s kvalifikovanou časovou pečiatkou sa na Slovensku nevyžaduje osvedčenie pravosti podpisu.

Podpis s časovou pečiatkou má úroveň BASELINE_T (napríklad PAdES-BASELINE-T), podpis bez nej BASELINE_B. Autogram časové pečiatky predvolene nepridáva. Ako ich zapnúť a odkiaľ ich Autogram berie, je na stránke [Podpis s časovou pečiatkou](timestamps.md).

## PDF/A

PDF/A je verzia PDF na dlhodobé uchovávanie, ktorú niektoré úrady vyžadujú. Autogram PDF do PDF/A neprevádza, iba vás [upozorní](warnings.md#pdfa), ak ho podpisovaný dokument nespĺňa. PDF/A vytvoríte napríklad pri exporte do PDF v textovom editore.
