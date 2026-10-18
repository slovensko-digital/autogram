---
description: "Návod pre začiatočníkov: prvý podpis dokumentu v Autograme krok za krokom, s obrázkom ku každému kroku."
---

# Prvý podpis krok za krokom

Tento návod vás prevedie prvým podpisom v Autograme. Nepotrebujete žiadne skúsenosti, stačí postupovať po krokoch. Niektoré kroky sa zobrazia iba pri niektorých kartách. Ak sa vám krok nezobrazí, pokračujte ďalším.

## Skôr než začnete

Pripravte si:

- **Autogram** nainštalovaný v počítači. Ak ho ešte nemáte, pozrite si [Inštalácia](installation.md).
- **Kartu s podpisovým certifikátom**, napríklad občiansky preukaz s čipom, a jej **obslužný softvér**. Pri občianskom preukaze je to eID klient. Pozrite si [Karty a certifikáty](cards.md).
- **Čítačku kariet**, ak ju počítač nemá zabudovanú.
- **PIN-y ku karte.** Pri občianskom preukaze sú to BOK a podpisový PIN.

::: callout info "Máte nový občiansky preukaz?"
Najprv si v eID kliente vydajte certifikáty na podpisovanie. Bez nich sa s preukazom podpisovať nedá.
:::

## 1. Vložte kartu do čítačky

Pripojte čítačku k počítaču a vložte do nej kartu čipom napred. Ak máte kartu v podobe USB kľúča, zasuňte ho priamo do počítača.

## 2. Otvorte dokument v Autograme

Dokument sa do Autogramu dostane jedným z dvoch spôsobov. Vyberte ten, ktorý platí pre vás.

### Podpisujete na webovej stránke

Takto Autogram používa väčšina ľudí, napríklad na slovensko\.sk alebo v aplikácii vášho úradu či firmy.

1. Na stránke kliknite na tlačidlo na podpísanie.
2. Ak sa stránka opýta **Ako si prajete dokument podpísať?**, kliknite na **Podpísať v tomto počítači**.
3. Ak sa prehliadač opýta, či chcete otvoriť aplikáciu Autogram, povoľte to.
4. Počkajte. Autogram sa otvorí a dokument zobrazí sám.

::: callout warning "Vidíte iba hlavné okno Autogramu?"
Ak Autogram ešte nebol spustený, najprv sa zobrazí iba jeho hlavné okno s tlačidlom **Vyberte súbor**. Nič v ňom neotvárajte. Počkajte niekoľko sekúnd, stránka pošle dokument Autogramu sama.
:::

Na štátnych weboch, ako slovensko\.sk, potrebujete aj rozšírenie do prehliadača. Pozrite si [Podpisovanie na webových stránkach](websites.md#dva-druhy-webov).

### Podpisujete súbor z počítača

1. Spustite Autogram. Na Windows ho nájdete v ponuke **Štart**, na Macu v priečinku **Aplikácie**.
2. Kliknite na **Vyberte súbor** a vyberte dokument. Alebo ho do okna presuňte myšou.

![Hlavné okno Autogramu](/assets/images/screenshots/sk/main-window.png)

## 3. Prezrite si dokument

Autogram otvorí dokument v novom okne. Celý si ho prečítajte, podpisujete presne to, čo vidíte. Dlhší dokument posúvajte kolieskom myši.

![Dokument pripravený na podpis](/assets/images/screenshots/sk/sign-document.png)

## 4. Kliknite na Podpísať

Tlačidlo **Podpísať** je pod dokumentom. Kým nezadáte PIN, žiadny podpis nevznikne. Ak si to rozmyslíte, okno jednoducho zatvorte.

## 5. Vyberte kartu

Ak máte pripojenú iba jednu kartu, Autogram ju použije sám a tento krok sa nezobrazí.

Ak sa zobrazí otázka **Kde je uložený podpisový certifikát?**, kliknite na svoju kartu a potom na **Pokračovať**.

![Výber karty](/assets/images/screenshots/sk/token-picker.png)

## 6. Zadajte kód ku karte

Pri niektorých kartách sa Autogram opýta **Aký je kód k úložisku kľúčov?**

- Ak vaša karta takýto kód má, zadajte ho a kliknite na **Pokračovať**.
- Ak ho nemá, nechajte pole prázdne a kliknite na **Pokračovať**.

![Aký je kód k úložisku kľúčov?](/assets/images/screenshots/sk/card-code.png)

Pri občianskom preukaze sa namiesto toho otvorí okno eID klienta. Zadajte v ňom BOK.

![Okno eID klienta: Zadajte BOK](/assets/images/screenshots/sk/eid-bok.png)

## 7. Vyberte certifikát

Kliknite na certifikát so svojím menom a potom na **Podpísať**.

![Výber certifikátu](/assets/images/screenshots/sk/certificate-picker.png)

## 8. Zadajte podpisový PIN

Zadajte podpisový PIN, pri niektorých kartách sa volá QPIN, a kliknite na **Podpísať**.

![Zadanie podpisového PIN-u](/assets/images/screenshots/sk/pin-dialog.png)

Pri občianskom preukaze zadáte podpisový PIN v okne eID klienta.

![Okno eID klienta: Zadajte Podpisový PIN](/assets/images/screenshots/sk/eid-pin.png)

::: callout warning "Pozor na počet pokusov"
Karta sa po niekoľkých nesprávne zadaných PIN-och zablokuje. Ak si PIN nie ste istí, podpisovanie radšej zrušte.
:::

## 9. Hotovo

**Na webovej stránke:** Autogram vráti podpísaný dokument stránke a jeho okno sa zavrie. Pokračujte na stránke, napríklad odoslaním podania.

**Súbor z počítača:** Autogram uloží podpísaný súbor do toho istého priečinka ako pôvodný dokument, s `_signed` na konci názvu. Kliknutím na cestu otvoríte priečinok.

![Dokument bol úspešne podpísaný](/assets/images/screenshots/sk/signing-success.png)

## Čo ďalej

- Ďalší dokument podpíšete rýchlejšie. Autogram si kartu a certifikát pamätá, stačí kliknúť na **Podpísať ako …** s vaším menom.
- Potrebujete podpísať viac dokumentov? PIN zadáte iba raz pre všetky. Pozrite si [Viac dokumentov naraz](batch.md).
- Chcete podpis s časovou pečiatkou, ktorý si nemusíte dať osvedčiť u notára? Pozrite si [Podpis s časovou pečiatkou](timestamps.md).
- Niečo nefunguje? Pozrite si [Riešenie problémov](troubleshooting.md).
