---
description: "Ako Autogramom overiť podpisy na dokumente, čo znamenajú výsledky overenia a kde nájdete detail podpisov."
---

# Overenie podpisov

Autogram overí podpisy na každom dokumente, ktorý otvoríte. Na overenie nepotrebujete kartu ani certifikát, stačí pripojenie na internet.

## Overenie dokumentu

1. Spustite Autogram a kliknite na **Vyberte súbor**, alebo podpísaný súbor presuňte do okna aplikácie.
2. Pod náhľadom dokumentu sa zobrazí časť **Podpisy na dokumente**. Kým overovanie prebieha, pri podpisoch je **Prebieha overovanie…**.
3. Po overení uvidíte pri každom podpise meno podpisujúceho a výsledok.

![Podpísaný dokument s overenými podpismi](/assets/images/screenshots/sk/signed-document.png)

Overiť môžete podpísané PDF aj kontajnery ASiC-E (`.asice`). Pri kontajneri s viacerými dokumentmi sa zobrazia podpisy dokumentu vo vybranej záložke, pozrite si [Viac dokumentov v jednom kontajneri](multi-document.md#podpisy-v-kontajneri).

Ak dokument nemá žiadne podpisy, Autogram zobrazí **Dokument neobsahuje žiadne podpisy.** Ak chcete dokument iba overiť, nemusíte ho podpisovať, okno jednoducho zatvorte.

## Čo znamenajú výsledky {#vysledky}

| Výsledok | Význam |
| --- | --- |
| **Vlastnoručný podpis** | Kvalifikovaný elektronický podpis. Má rovnaké právne účinky ako vlastnoručný podpis na papieri. |
| **Osvedčený podpis** | Kvalifikovaný elektronický podpis s kvalifikovanou časovou pečiatkou. Podľa Občianskeho zákonníka sa pri ňom nevyžaduje osvedčenie pravosti podpisu, napríklad u notára. Pozrite si [Podpis s časovou pečiatkou](timestamps.md). |
| **Elektronická pečať** | Kvalifikovaná elektronická pečať. Nepodpisuje ňou človek, ale organizácia, napríklad úrad. |
| **Uznaný spôsob autorizácie** | Zdokonalený elektronický podpis založený na kvalifikovanom certifikáte. |
| **Iný zdokonalený elektronický podpis** | Zdokonalený podpis, ktorý nie je kvalifikovaný. Úrady ho nemusia prijať. |
| **Predbežne platný …** | Podpis zatiaľ nie je možné úplne overiť, napríklad preto, že nie je dostupná informácia o zneplatnení certifikátu. Overenie môžete zopakovať neskôr. |
| **Neznámy podpis** | Autogram nevie overiť, kto podpis vytvoril, napríklad certifikát nevydal dôveryhodný poskytovateľ z vybraných krajín. |
| **Neplatný podpis** | Podpis je neplatný, napríklad dokument bol po podpísaní zmenený. |

Ak dokument obsahuje neznámy alebo neplatný podpis a chcete ho podpísať, Autogram vás upozorní, pozrite si [Upozornenia pred podpisom](warnings.md#nedoveryhodne-podpisy).

## Detail podpisov

Kliknite na **Zobraziť detail podpisov**. Pri každom podpise uvidíte:

- **Výsledok overenia:** Platný, Predbežne platný, Neplatný alebo Nepodarilo sa overiť,
- **Certifikát:** údaje o podpisujúcom z certifikátu,
- **Vydavateľ:** kto certifikát vydal,
- **Negarantovaný čas podpisu:** čas podľa počítača podpisujúceho, ktorý nie je ničím potvrdený,
- **Typ podpisu:** napríklad kvalifikovaný elektronický podpis,
- **Časové pečiatky:** ak ich podpis má. Časová pečiatka dôveryhodne potvrdzuje, že podpis existoval v danom čase.

![Detailné informácie o podpisoch](/assets/images/screenshots/sk/signature-details.png)

Tlačidlom **Zobraziť technické detaily** otvoríte úplnú správu o overení. Hodí sa, keď riešite problém s podpisom s odborníkom alebo podporou.

## Dôveryhodné zoznamy {#doveryhodne-zoznamy}

Autogram overuje podpisy podľa dôveryhodných zoznamov Európskej únie, ktoré sťahuje z internetu. Predvolene overuje podpisy z certifikátov poskytovateľov zo Slovenska, Česka, Rakúska, Poľska, Maďarska, Belgicka, Holandska a Španielska. Ďalšie krajiny zapnete v [Nastaveniach](settings.md#overovanie-podpisov). Každá ďalšia krajina overovanie spomalí.

Ak sa dôveryhodné zoznamy nepodarí stiahnuť, Autogram zobrazí upozornenie, že podpisy nie je možné úplne overiť. Skontrolujte pripojenie na internet a dokument otvorte znova.

Niekedy sa nepodarí stiahnuť iba zoznam niektorej krajiny, napríklad keď jej server práve neodpovedá. Autogram potom overuje podľa ostatných zoznamov. Ak má dokument podpis, ktorý sa pre chýbajúci zoznam nedá overiť, Autogram nad podpismi upozorní, koľko zoznamov sa nepodarilo stiahnuť. Pri ďalšom spustení Autogramu sa ich pokúsi stiahnuť znova.

![Upozornenie, že sa nepodarilo stiahnuť niektoré dôveryhodné zoznamy](/assets/images/screenshots/sk/signed-document-trusted-lists.png)
