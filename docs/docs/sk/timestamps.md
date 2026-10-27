---
description: "Ako pridať k podpisu bezplatnú kvalifikovanú časovú pečiatku. Pri kvalifikovanom podpise s ňou sa nevyžaduje osvedčenie pravosti podpisu."
---

# Podpis s časovou pečiatkou

Autogram môže k vášmu podpisu pridať kvalifikovanú časovú pečiatku. Pečiatka od dôveryhodného poskytovateľa potvrdzuje, kedy ste dokument podpísali. Podpis s ňou ostane overiteľný aj po skončení platnosti vášho certifikátu.

::: callout info "Osvedčený podpis zadarmo"
Ak dokument podpíšete kvalifikovaným elektronickým podpisom s kvalifikovanou časovou pečiatkou, podľa [§ 40 ods. 5 Občianskeho zákonníka](https://www.slov-lex.sk/ezbierky/pravne-predpisy/SK/ZZ/1964/40/#paragraf-40.odsek-5) sa nevyžaduje osvedčenie pravosti vášho podpisu, napríklad u notára alebo na matrike. Platí to iba pre podpis s časovou pečiatkou, pozrite si [Čo hovorí zákon](#co-hovori-zakon). Autogram na to používa voľne dostupné kvalifikované časové pečiatky od poskytovateľov z Belgicka a Španielska. Nič za ne neplatíte a nemusíte si nič objednávať.
:::

## Zapnutie časových pečiatok

::: steps
1. V hlavnom okne Autogramu kliknite na **Nastavenia**.
2. V záložke **Podpisovanie** zvoľte pri **Pridávanie časových pečiatok** možnosť **Áno**.
3. V poli **Zdroje časových pečiatok** nechajte predvolených poskytovateľov.
4. Kliknite na **Uložiť nastavenia**.
:::

![Zapnuté časové pečiatky v nastaveniach](/assets/images/screenshots/sk/settings-timestamps.png)

Odteraz Autogram pridá časovú pečiatku ku každému podpisu súboru z počítača, aj pri podpisovaní [viacerých dokumentov naraz](batch.md). Na pridanie pečiatky treba pripojenie na internet.

Pre jeden práve podpisovaný dokument môžete voľbu zmeniť aj priamo vo [výbere certifikátu](card-and-pin.md#peciatka-pre-tento-podpis), bez zmeny uložených nastavení. Po overení kvalifikácie zdroja uvidíte rovnaký názov predpokladaného podpisu ako pri overovaní hotového podpisu: **Vlastnoručný podpis** bez pečiatky, **Osvedčený podpis** s kvalifikovanou pečiatkou. Podpis vytvorený kvalifikovaným certifikátom bez QSCD sa ani po pridaní pečiatky nemení na kvalifikovaný elektronický podpis.

## Podpisovanie na webových stránkach

Pri podpisovaní na webovej stránke o časovej pečiatke rozhoduje stránka. Ak ju stránka vyžaduje, Autogram ju pridá z rovnakých zdrojov, aké máte v nastaveniach, aj keď máte pridávanie časových pečiatok vypnuté.

## Ako spoznáte podpis s časovou pečiatkou

Po otvorení podpísaného dokumentu Autogram pri podpise s kvalifikovanou časovou pečiatkou zobrazí **Osvedčený podpis**. Na obrázku má časovú pečiatku podpis Jany Vzorovej, podpis Petra Ukážku je bez nej.

![Podpísaný dokument s osvedčeným a vlastnoručným podpisom](/assets/images/screenshots/sk/signed-document.png)

Časové pečiatky uvidíte aj v detaile podpisov. Pozrite si [Overenie podpisov](verification.md#vysledky).

## Čo hovorí zákon {#co-hovori-zakon}

Účinky elektronického podpisu upravuje § 40 [Občianskeho zákonníka](https://www.slov-lex.sk/ezbierky/pravne-predpisy/SK/ZZ/1964/40/#paragraf-40):

- **Podpis bez časovej pečiatky.** Dokument podpísaný kvalifikovaným elektronickým podpisom má podľa [odseku 4](https://www.slov-lex.sk/ezbierky/pravne-predpisy/SK/ZZ/1964/40/#paragraf-40.odsek-4) písomnú formu, rovnako ako dokument podpísaný rukou na papieri. Autogram taký podpis zobrazí ako **Vlastnoručný podpis**.
- **Podpis s časovou pečiatkou.** Ak je k podpisu pripojená časová pečiatka, podľa [odseku 5](https://www.slov-lex.sk/ezbierky/pravne-predpisy/SK/ZZ/1964/40/#paragraf-40.odsek-5) sa osvedčenie pravosti podpisu nevyžaduje. Autogram taký podpis zobrazí ako **Osvedčený podpis**.

Občiansky zákonník používa staršie pojmy „zaručený elektronický podpis“ a „časová pečiatka“. Podľa [§ 17 ods. 2 zákona č. 272/2016 Z. z. o dôveryhodných službách](https://www.slov-lex.sk/ezbierky/pravne-predpisy/SK/ZZ/2016/272/#paragraf-17.odsek-2) sa nimi rozumie kvalifikovaný elektronický podpis a kvalifikovaná elektronická časová pečiatka.

## Vlastný poskytovateľ časových pečiatok {#vlastny-poskytovatel-casovych-peciatok}

Ak máte zmluvu s iným poskytovateľom časových pečiatok, v poli **Zdroje časových pečiatok** zvoľte **Použiť vlastnú adresu TSA servera** a do poľa **Vlastné adresy TSP serverov** zadajte adresu jeho servera. Viac adries oddeľte čiarkou bez medzier.

Aby bola pečiatka pri overovaní uznaná za kvalifikovanú, poskytovateľ musí byť kvalifikovaný a jeho krajina musí byť zapnutá v záložke **Overovanie podpisov**. Pozrite si [Nastavenia](settings.md#overovanie-podpisov).
