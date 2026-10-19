---
description: "Ako nainštalovať Autogram na Windows, macOS a Linux spolu so softvérom ku karte a rozšírením do prehliadača a ako ho aktualizovať."
---

# Inštalácia

Autogram je zdarma pre Windows, macOS a Linux.

::: button "Stiahnuť Autogram" url:"https://sluzby.slovensko.digital/autogram/#download"

## Windows

1. Stiahnite súbor `.msi` a spustite ho.
2. Prejdite sprievodcom inštaláciou.
3. Autogram potom nájdete v ponuke **Štart**.

## macOS

1. Zistite, aký procesor má váš Mac: v ponuke Apple  zvoľte **O tomto Macu**. Ak je pri položke **Čip** uvedené Apple M…, stiahnite `macos.pkg`, pri procesore Intel stiahnite `macos-intel.pkg`.
2. Otvorte stiahnutý súbor a prejdite inštaláciou. Inštalátor si môže vypýtať heslo správcu počítača.
3. Autogram sa nainštaluje do priečinka **Aplikácie**.

## Linux

Balík nainštalujte správcom balíkov vašej distribúcie, napríklad:

```sh
sudo apt install ./autogram-…-ubuntu.deb    # Ubuntu
sudo apt install ./autogram-…-debian.deb    # Debian
sudo dnf install ./autogram-…-fedora.rpm    # Fedora
```

## Softvér ku karte

Na podpisovanie potrebujete okrem Autogramu aj obslužný softvér ku karte, na ktorej máte certifikát. Pri občianskom preukaze je to eID klient. Prehľad kariet a ich softvéru je na stránke [Karty a certifikáty](cards.md).

## Rozšírenie do prehliadača

Ak chcete podpisovať na weboch slovenskej verejnej správy, napríklad na slovensko\.sk alebo na portáli Finančnej správy, nainštalujte si aj [rozšírenie Autogramu do prehliadača](https://sluzby.slovensko.digital/autogram-na-statnych-weboch#download). Podrobnosti sú na stránke [Podpisovanie na webových stránkach](websites.md#statne-weby-s-rozsirenim-do-prehliadaca).

## Prvé spustenie

Po spustení sa zobrazí hlavné okno Autogramu. Odtiaľ otvoríte súbor na podpísanie alebo overenie a dostanete sa k nastaveniam.

![Hlavné okno Autogramu](/assets/images/screenshots/sk/main-window.png)

Webové stránky môžu Autogram spustiť aj samy. Ak sa po spustení zo stránky zobrazí iba hlavné okno, nič v ňom neotvárajte a chvíľu počkajte, kým stránka pošle dokument. Pozrite si [Keď Autogram ešte nebeží](websites.md#ked-autogram-este-nebezi).

## Aktualizácie

Pri spustení Autogram skontroluje, či je dostupná nová verzia. Ak áno, zobrazí okno **Dostupná aktualizácia**:

- **Stiahnuť novú verziu** otvorí v prehliadači stránku na stiahnutie,
- **Teraz nie** okno zavrie a Autogram môžete používať ďalej.

Novú verziu nainštalujete rovnako ako prvú. Nastavenia ostanú zachované.
