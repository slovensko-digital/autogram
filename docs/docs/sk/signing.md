---
description: "Ako v Autograme podpísať súbor uložený v počítači: otvorenie, kontrola dokumentu, podpísanie a kam sa podpísaný súbor uloží."
---

# Podpísanie súboru z počítača

Takto podpíšete súbor, ktorý máte uložený v počítači. Ak podpisujete prvýkrát, prejdite si radšej [Prvý podpis krok za krokom](first-signature.md), kde je ku každému kroku obrázok. Ak podpisujete na webovej stránke, súbor v Autograme neotvárajte, stránka ho Autogramu pošle sama. Pozrite si [Podpisovanie na webových stránkach](websites.md).

## Otvorenie súboru

Spustite Autogram a kliknite na **Vyberte súbor**, alebo súbor presuňte do okna aplikácie.

![Hlavné okno Autogramu](/assets/images/screenshots/sk/main-window.png)

Ak vyberiete viac súborov alebo celý priečinok, Autogram ich podpíše postupne alebo hromadne, pozrite si [Viac dokumentov naraz](batch.md).

## Kontrola dokumentu

Autogram otvorí dokument v novom okne a zobrazí jeho obsah. Pred podpisom si ho celý prezrite, podpisujete presne to, čo vidíte.

![Dokument pripravený na podpis](/assets/images/screenshots/sk/sign-document.png)

Ak dokument už obsahuje podpisy, Autogram ich overí a zobrazí pod náhľadom. Čo znamenajú, je na stránke [Overenie podpisov](verification.md).

Niektoré súbory, napríklad dokumenty Word, Autogram zobraziť nevie. Vtedy vás na to upozorní, pozrite si [Upozornenia pred podpisom](warnings.md#dokument-nie-je-mozne-zobrazit).

## Podpísanie {#podpisanie}

::: steps
1. Kliknite na **Podpísať**.
2. Ak máte pripojených viac kariet, vyberte tú, na ktorej je váš certifikát. Jedinú nájdenú kartu Autogram použije sám.
3. Ak sa Autogram opýta na kód k úložisku kľúčov, zadajte kód ku karte. Ak ho vaša karta nemá, nechajte pole prázdne.
4. Vyberte certifikát a kliknite na **Podpísať**.

   ![Výber certifikátu](/assets/images/screenshots/sk/certificate-picker.png)

5. Zadajte podpisový PIN. Pri občianskom preukaze ho zadávate v okne eID klienta.
6. Autogram uloží podpísaný súbor a ukáže, kde ho nájdete. Kliknutím na cestu otvoríte priečinok.

   ![Dokument bol úspešne podpísaný](/assets/images/screenshots/sk/signing-success.png)
:::

Podrobnosti o výbere karty, kóde ku karte a PIN-e sú na stránke [Výber karty a zadanie PIN-u](card-and-pin.md).

Ak máte v Nastaveniach zapnuté časové pečiatky, Autogram k podpisu pridá aj kvalifikovanú časovú pečiatku. Pozrite si [Podpis s časovou pečiatkou](timestamps.md).

## Kam sa podpísaný súbor uloží {#kam-sa-podpisany-subor-ulozi}

Podpísaný súbor sa uloží do toho istého priečinka ako pôvodný dokument. K názvu sa pridá `_signed`, pôvodný súbor ostane nezmenený.

| Podpisujete | Vznikne |
| --- | --- |
| `zmluva.pdf` | `zmluva_signed.pdf` s podpisom vo vnútri PDF |
| `ziadost.xml`, `fotka.png`, `ponuka.docx` a iné súbory | `ziadost_signed.asice` a pod., kontajner s dokumentom aj podpisom |
| už podpísaný súbor | súbor rovnakého typu, ku ktorému pribudne váš podpis |

Ak súbor s takým názvom už existuje, Autogram pridá číslo, napríklad `zmluva_signed (1).pdf`. Niektoré úrady pri PDF vyžadujú iný typ podpisu, ten zmeníte v [Nastaveniach](settings.md#podpisovanie). Prečo niekedy vznikne PDF a inokedy kontajner ASiC-E, vysvetľuje stránka [Formáty a typy podpisov](formats.md).

## Ďalší dokument

Kým je spojenie s kartou aktívne, ďalší dokument podpíšete jedným kliknutím na **Podpísať ako …** s vaším menom.

![Tlačidlo Podpísať ako](/assets/images/screenshots/sk/sign-as.png)

Tlačidlom **Podpísať iným certifikátom** vyberiete iný certifikát alebo inú kartu.

## Zrušenie podpisu

Podpisovanie zrušíte zatvorením okna dokumentu. Kým nezadáte PIN, žiadny podpis nevznikne.
