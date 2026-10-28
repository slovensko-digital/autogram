---
description: "Čo znamenajú upozornenia, ktoré Autogram zobrazí pred podpisom, napríklad pri dokumente bez náhľadu alebo s nedôveryhodnými podpismi."
---

# Upozornenia pred podpisom

Autogram pred podpisom kontroluje dokument aj podpisy, ktoré už obsahuje. Keď niečo nie je v poriadku, upozorní vás a rozhodnutie nechá na vás. Upozornenie neznamená chybu, ale vec, ktorú si treba všimnúť.

## Dokument nie je možné zobraziť {#dokument-nie-je-mozne-zobrazit}

Niektoré typy súborov, napríklad dokumenty Word alebo tabuľky Excel, Autogram zobraziť nevie. Namiesto náhľadu uvidíte upozornenie.

![Dokument nie je možné zobraziť](/assets/images/screenshots/sk/warning-unsupported-document.png)

Takýto dokument podpíšte, iba ak poznáte jeho obsah a dôverujete jeho zdroju. Ak je to možné, uložte ho ako PDF a podpíšte PDF. Ktoré súbory Autogram zobrazí, je na stránke [Formáty a typy podpisov](formats.md#co-autogram-zobrazi).

## Dokument obsahuje nedôveryhodné podpisy {#nedoveryhodne-podpisy}

Ak dokument už obsahuje podpis, ktorý je neplatný alebo ho nie je možné overiť, Autogram vás po kliknutí na **Podpísať** upozorní.

![Dokument obsahuje nedôveryhodné podpisy](/assets/images/screenshots/sk/warning-invalid-signatures.png)

Úrady takýto dokument nemusia prijať. Detail podpisov si pozriete tlačidlom **Zobraziť detail podpisov**. Potom sa rozhodnite:

- **Zrušiť podpisovanie:** dokument nepodpíšete,
- **Podpísať:** dokument napriek tomu podpíšete.

Ak podpis vydal poskytovateľ z krajiny, ktorej dôveryhodný zoznam nemáte zapnutý, upozornenie to uvedie aj s odkazom **Otvoriť nastavenia**. Keď krajinu zapnete a nastavenia uložíte, Autogram podpisy overí znova. Potom kliknite na **Podpísať** ešte raz.

![Upozornenie s odkazom do nastavení](/assets/images/screenshots/sk/warning-invalid-signatures-country-off.png)

Čo znamenajú jednotlivé výsledky overenia, je na stránke [Overenie podpisov](verification.md#vysledky). Upozornenie môžete vypnúť v [Nastaveniach](settings.md#bezpecnost) voľbou **Kontrola platnosti podpisov**.

## Existujúce podpisy ešte neboli overené

Ak kliknete na **Podpísať** skôr, ako Autogram dokončí overovanie podpisov na dokumente, opýta sa, či chcete dokument podpísať aj tak. Ak si nie ste istí, kliknite na **Zrušiť podpisovanie**, počkajte na výsledok overenia a podpíšte znova.

## Dokument nie je vo formáte PDF/A {#pdfa}

PDF/A je verzia formátu PDF určená na dlhodobé uchovávanie. Niektoré úrady prijímajú iba dokumenty v tomto formáte. Ak PDF, ktoré podpisujete, nie je PDF/A, Autogram vás upozorní.

![Dokument nie je vo formáte PDF/A](/assets/images/screenshots/sk/warning-pdfa.png)

- **Pokračovať:** dokument podpíšete tak, ako je,
- **Zrušiť podpisovanie:** dokument môžete najprv uložiť ako PDF/A (väčšina textových editorov to vie pri exporte do PDF) a potom ho podpísať.

Ak PDF/A nepotrebujete, upozornenie vypnete v [Nastaveniach](settings.md#bezpecnost) voľbou **Kontrola súladu s PDF/A formátom**.

## Podpisy nie sú rovnaké na všetkých dokumentoch

Toto upozornenie sa zobrazí pri kontajneri ASiC-E s viacerými dokumentmi, ak niektorý podpis nepokrýva všetky dokumenty. Viac je na stránke [Viac dokumentov v jednom kontajneri](multi-document.md#podpisy-v-kontajneri).
