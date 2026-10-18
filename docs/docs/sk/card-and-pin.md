---
description: "Čo nasleduje po kliknutí na Podpísať: výber karty, kód k úložisku kľúčov, výber certifikátu a zadanie podpisového PIN-u."
---

# Výber karty a zadanie PIN-u

Po kliknutí na **Podpísať** Autogram zistí, kde máte podpisový certifikát, a vypýta si PIN. Podľa toho, akú kartu máte, prejdete niektorými z týchto krokov:

1. [výber karty](#vyber-karty), ak je z čoho vyberať,
2. [kód k úložisku kľúčov](#kod-k-ulozisku-klucov), ak ho vaša karta má,
3. [výber certifikátu](#vyber-certifikatu),
4. [podpisový PIN](#podpisovy-pin).

Pri ďalších dokumentoch sa väčšina krokov preskočí, pozrite si [Ďalšie podpisy](#dalsie-podpisy).

## Výber karty {#vyber-karty}

Autogram vyhľadá karty vo všetkých pripojených čítačkách. Ak nájde iba jednu kartu, použije ju bez pýtania a tento krok sa nezobrazí.

### Viac kariet vo viacerých čítačkách

K počítaču môžete mať pripojených viac čítačiek naraz, napríklad zabudovanú čítačku s občianskym preukazom a USB čítačku s kartou od zamestnávateľa. Autogram nájde karty vo všetkých a opýta sa **Kde je uložený podpisový certifikát?** Vyberte kartu a kliknite na **Pokračovať**.

![Výber z dvoch kariet](/assets/images/screenshots/sk/token-picker.png)

Karty sú v zozname pomenované podľa typu, napríklad **Občiansky preukaz** alebo **I\.CA SecureStore**. Karta, ktorú ste použili naposledy, je vopred vybraná.

Ak sú vložené dve karty rovnakého typu, napríklad občianske preukazy dvoch ľudí, pod každou je uvedená čítačka, v ktorej je vložená.

![Dva občianske preukazy v dvoch čítačkách](/assets/images/screenshots/sk/token-picker-readers.png)

::: callout info "Ktorá čítačka je ktorá?"
Ak podľa názvu nespoznáte, ktorá čítačka je ktorá, vytiahnite kartu, ktorú nechcete použiť, a kliknite na **Vyhľadať karty znova**. V zozname ostane iba tá druhá.
:::

### Viac certifikátov na jednej karte {#viac-certifikatov-na-jednej-karte}

Niektoré karty majú certifikáty uložené vo viacerých častiach, ktoré sa volajú sloty. Takáto karta je v zozname viackrát a sloty sú očíslované.

![Karta Gemalto IDPrime 940 s dvoma slotmi](/assets/images/screenshots/sk/token-picker-slots.png)

Na karte Gemalto IDPrime 940 býva kvalifikovaný certifikát v inom slote ako ostatné certifikáty. Ak po výbere slotu neuvidíte svoj kvalifikovaný certifikát alebo podpis zlyhá, kliknite na **Podpísať iným certifikátom** a vyberte druhý slot.

Občiansky preukaz má okrem kvalifikovaného certifikátu aj druhý, ktorý sa na kvalifikované podpisovanie nepoužíva. Autogram ho predvolene neponúka. Ak v [Nastaveniach](settings.md#ine) zapnete **Zobraziť EP certifikát občianskeho preukazu**, preukaz bude v zozname dvakrát. Na bežné podpisovanie vyberte **kvalifikovaný**.

![Občiansky preukaz s kvalifikovaným a nekvalifikovaným certifikátom](/assets/images/screenshots/sk/token-picker-eid.png)

### Nevidíte svoju kartu? {#nevidite-svoju-kartu}

Ak Autogram kartu nenájde, zobrazí **Nenašla sa žiadna karta**. Skontrolujte, či:

- je karta vložená v čítačke,
- je čítačka pripojená k počítaču,
- máte nainštalovaný obslužný softvér ku karte, napríklad eID klient pre občiansky preukaz.

Potom kliknite na **Vyhľadať karty znova**. Ak sa zobrazí **Čítačka kariet neodpovedá**, odpojte čítačku od počítača, znova ju pripojte a vyhľadajte karty znova. Ak to nepomôže, reštartujte počítač.

Ktorý softvér potrebuje vaša karta, zistíte na stránke [Karty a certifikáty](cards.md).

## Kód k úložisku kľúčov {#kod-k-ulozisku-klucov}

Pri niektorých kartách sa Autogram po výbere karty opýta **Aký je kód k úložisku kľúčov?** Je to kód ku karte, ktorým sa sprístupnia certifikáty na karte. Niektoré karty ho majú, niektoré nie.

![Aký je kód k úložisku kľúčov?](/assets/images/screenshots/sk/card-code.png)

- Ak vaša karta kód má, zadajte ho a kliknite na **Pokračovať**. Dostali ste ho od vydavateľa karty spolu s ostatnými kódmi.
- Ak vaša karta kód nemá, nechajte pole prázdne a kliknite na **Pokračovať**.

Pri občianskom preukaze sa toto okno nezobrazí. Kód k nemu (BOK) si vypýta eID klient, pozrite si [Občiansky preukaz](#obciansky-preukaz).

## Výber certifikátu {#vyber-certifikatu}

Autogram zobrazí certifikáty z vybranej karty. Vyberte ten, ktorým chcete podpisovať, a kliknite na **Podpísať**.

![Výber certifikátu](/assets/images/screenshots/sk/certificate-picker.png)

## Podpisový PIN {#podpisovy-pin}

Nakoniec zadáte podpisový PIN, pri niektorých kartách sa volá QPIN. Pri všetkých kartách okrem občianskeho preukazu ho zadávate v okne Autogramu.

![Zadanie podpisového PIN-u](/assets/images/screenshots/sk/pin-dialog.png)

::: callout warning "Pozor na počet pokusov"
Karta sa po niekoľkých nesprávne zadaných PIN-och zablokuje. Ak si PIN nie ste istí, podpisovanie radšej zrušte.
:::

## Občiansky preukaz {#obciansky-preukaz}

Pri občianskom preukaze zadávate kódy v okne eID klienta, nie v Autograme.

Keď sa Autogram pripája ku karte, eID klient si vypýta BOK (bezpečnostný osobný kód).

![Okno eID klienta: Zadajte BOK](/assets/images/screenshots/sk/eid-bok.png)

Pri podpise si vypýta podpisový PIN.

![Okno eID klienta: Zadajte Podpisový PIN](/assets/images/screenshots/sk/eid-pin.png)

Výnimkou je podpisovanie [viacerých dokumentov naraz](batch.md). Podpisový PIN vtedy zadáte v okne Autogramu, aby ho stačilo zadať raz pre všetky dokumenty.

## Ďalšie podpisy {#dalsie-podpisy}

Vybraný certifikát si Autogram pamätá. Ďalšie dokumenty podpíšete tlačidlom **Podpísať ako …** s vaším menom a kartu ani certifikát už nevyberáte.

Po 5 minútach nečinnosti sa spojenie s kartou ukončí a pri ďalšom podpise sa PIN zadáva znova. Čas nastavíte v [Nastaveniach](settings.md#podpisovanie).

Ak chcete podpísať iným certifikátom alebo inou kartou, kliknite na **Podpísať iným certifikátom**.
