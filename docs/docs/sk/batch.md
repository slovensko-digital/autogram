---
description: "Ako podpísať viac dokumentov naraz, po jednom alebo hromadne. PIN zadáte iba raz, nech je dokumentov koľkokoľvek."
---

# Viac dokumentov naraz

Ak potrebujete podpísať viac dokumentov, nemusíte ich otvárať po jednom. V hlavnom okne kliknite na **Vyberte súbor** a označte viac súborov, alebo do okna presuňte celý priečinok.

::: callout info "Jeden PIN pre všetky dokumenty"
PIN zadáte iba raz, pri prvom dokumente. Autogram potom podpíše všetky vybrané dokumenty bez ďalšieho zadávania PIN-u, či ich je desať alebo tisíc. Platí to pri podpisovaní po jednom aj hromadne a pri všetkých kartách vrátane občianskeho preukazu.
:::

Každý dokument dostane vlastný podpis a uloží sa ako samostatný súbor.

## Výber spôsobu podpisovania

Autogram sa opýta, ako chcete dokumenty podpísať.

![Ako chcete podpísať dokumenty?](/assets/images/screenshots/sk/batch-mode-picker.png)

- **Podpisovať po jednom:** každý dokument sa pred podpisom zobrazí a vy rozhodnete, či ho podpíšete. Odporúčame tento spôsob, ak dokumenty nepoznáte.
- **Podpísať hromadne:** dokumenty sa podpíšu naraz, bez zobrazenia.

Vyberte spôsob a kliknite na **Pokračovať**.

## Podpisovanie po jednom

Dokumenty sa otvárajú postupne. Pri každom máte tieto možnosti:

- **Podpísať** (alebo **Podpísať ako …**) dokument podpíše a otvorí ďalší,
- **Preskočiť dokument** tento dokument vynechá,
- **Preskočiť zostávajúce dokumenty** ukončí podpisovanie, už podpísané dokumenty ostanú podpísané,
- **Podpísať iným certifikátom** zmení certifikát.

![Podpisovanie po jednom](/assets/images/screenshots/sk/batch-one-by-one.png)

## Hromadné podpisovanie

Autogram zobrazí, koľko dokumentov sa chystáte podpísať. Samotné dokumenty pri hromadnom podpisovaní neuvidíte.

![Hromadné podpisovanie](/assets/images/screenshots/sk/batch-sign-all.png)

::: callout warning "Podpisujete dokumenty, ktoré nevidíte"
Hromadne podpisujte iba dokumenty, ktorých obsah poznáte a ktorých zdroju dôverujete. Podpisovanie dokumentov, ktoré ste nevideli, môže mať vážne právne následky.
:::

Kliknite na **Podpísať** a zadajte PIN. Po skončení Autogram ukáže, koľko dokumentov podpísal a kam ich uložil.

![Hromadné podpisovanie úspešne ukončené](/assets/images/screenshots/sk/batch-success.png)

Ak chcete hromadne podpisovať vždy, bez otázky na spôsob podpisovania, zapnite v [Nastaveniach](settings.md#podpisovanie) voľbu **Vždy podpisovať hromadne**.

## Kam sa podpísané súbory uložia

| Vybrali ste | Podpísané súbory nájdete |
| --- | --- |
| viac súborov | v priečinku `signed` vedľa vybraných súborov |
| priečinok, napríklad `Faktúry` | v novom priečinku `Faktúry_signed` vedľa neho |

Pôvodné súbory ostanú nezmenené. Podpísať naraz viac priečinkov alebo kombináciu súborov a priečinkov nie je možné.

## Keď sa niektorý dokument nepodarí podpísať

Ak sa niektorý dokument podpísať nedá, napríklad je chránený heslom, ostatné sa podpíšu. Na konci Autogram zobrazí, ktoré dokumenty ostali nepodpísané. Dôvod uvidíte po kliknutí na **Zobraziť podrobnosti chyby**.

![Hromadné podpisovanie skončilo s chybami](/assets/images/screenshots/sk/batch-failure.png)
