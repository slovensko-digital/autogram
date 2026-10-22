---
description: "Ako nainštalovať Autogram na Windows z Microsoft Store, na macOS a Linux spolu so softvérom ku karte a rozšírením do prehliadača, ako ho aktualizovať a ako prejsť z inštalátora .msi na Microsoft Store."
---

# Inštalácia

Autogram je zdarma pre Windows, macOS a Linux.

::: button "Stiahnuť Autogram" url:"https://sluzby.slovensko.digital/autogram/#download"

## Windows

Na Windows inštalujte Autogram z Microsoft Store. Nové verzie potom Microsoft Store inštaluje automaticky.

::: button "Autogram v Microsoft Store" url:"https://apps.microsoft.com/detail/9N4XBDWNF438"

1. Otvorte Autogram v Microsoft Store a kliknite na **Získať**.
2. Po inštalácii kliknite na **Otvoriť**. Autogram potom nájdete v ponuke **Štart**.

Ak Microsoft Store v počítači nemáte, napríklad ho vo firme zakázal správca, na [stránke na stiahnutie](https://sluzby.slovensko.digital/autogram/#download) je aj inštalátor `.msi`. Stiahnite ho, spustite a prejdite sprievodcom inštaláciou. Takto nainštalovaný Autogram sa automaticky neaktualizuje.

Ak už máte Autogram nainštalovaný zo súboru `.msi`, pozrite si [Prechod na verziu z Microsoft Store](#prechod-na-microsoft-store).

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

Autogram z Microsoft Store aktualizuje Microsoft Store automaticky, nemusíte nič robiť.

Na macOS, na Linuxe a pri inštalácii zo súboru `.msi` Autogram pri spustení skontroluje, či je dostupná nová verzia. Ak áno, zobrazí okno **Dostupná aktualizácia**:

- **Stiahnuť novú verziu** otvorí v prehliadači stránku na stiahnutie,
- **Teraz nie** okno zavrie a Autogram môžete používať ďalej.

<!-- SCREENSHOT sk/update-available.png "Dostupná aktualizácia" -->

Novú verziu nainštalujete rovnako ako prvú. Nastavenia ostanú zachované.

Na Windows ponúkne okno od verzie 2.8 namiesto sťahovania tlačidlo **Inštalovať z Microsoft Store**. Odkaz **Zistiť viac informácií** otvorí stránku na stiahnutie. Ďalší postup je v časti [Prechod na verziu z Microsoft Store](#prechod-na-microsoft-store).

<!-- SCREENSHOT sk/update-store.png "Dostupná aktualizácia s inštaláciou z Microsoft Store" -->

## Prechod na verziu z Microsoft Store {#prechod-na-microsoft-store}

Do verzie 2.7 sa Autogram pre Windows inštaloval zo súboru `.msi` a každú novú verziu ste si museli stiahnuť a nainštalovať sami. Od verzie 2.8 je Autogram v Microsoft Store, ktorý ho aktualizuje automaticky. Ak máte Autogram zo súboru `.msi`, odporúčame prejsť na verziu z Microsoft Store. Prechod trvá niekoľko minút.

1. Staršia verzia Autogramu pri spustení zobrazí okno **Dostupná aktualizácia**. Kliknite na **Stiahnuť novú verziu** a na stránke, ktorá sa otvorí v prehliadači, pokračujte do Microsoft Store. Autogram v Microsoft Store otvoríte aj priamo [týmto odkazom](https://apps.microsoft.com/detail/9N4XBDWNF438).
2. V Microsoft Store kliknite na **Získať** a po inštalácii na **Otvoriť**. Staršiu verziu nemusíte zatvárať, nová ju pri spustení ukončí sama.
3. Nová verzia zistí, že v počítači je aj staršia, a zobrazí okno **Staršia verzia Autogramu**. Kliknite na **Odinštalovať staršiu verziu**.

   <!-- SCREENSHOT sk/legacy-installation.png "Staršia verzia Autogramu" -->

4. Windows sa opýta, či povolíte Inštalátoru systému Windows vykonať zmeny v zariadení. Kliknite na **Áno**. Ak nie ste správcom počítača, Windows si vypýta heslo správcu.
5. Keď sa staršia verzia odinštaluje, okno sa zavrie. Odteraz používate verziu z Microsoft Store.

### Čo sa zmení {#co-sa-zmeni}

- **Nastavenia ostanú.** Nová verzia prevezme nastavenia staršej, napríklad jazyk, typ podpisu alebo server časových pečiatok.
- **Podpisovanie na webových stránkach funguje ďalej.** Webové stránky aj rozšírenie do prehliadača budú spúšťať novú verziu, nič nemusíte nastavovať.
- **Aktualizácie sú automatické.** Okno **Dostupná aktualizácia** sa už nezobrazuje, nové verzie inštaluje Microsoft Store.
- **Pripnutie sa presunie.** Ak ste mali Autogram pripnutý na paneli úloh alebo v ponuke **Štart**, po inštalácii tam bude nová verzia.
- **Ikona na ploche zmizne.** Staršia verzia ju pri odinštalovaní odstráni a verzia z Microsoft Store ju nevytvára. Ak ju chcete mať, pretiahnite Autogram z ponuky **Štart** na plochu.
- **Príkaz `autogram-cli`** funguje v každom termináli, pozrite si [Príkazový riadok (CLI)](cli.md).

### Ak staršiu verziu hneď neodinštalujete

- Po kliknutí na **Teraz nie** sa okno **Staršia verzia Autogramu** zobrazí pri každom spustení, kým staršiu verziu neodinštalujete. Dovtedy uvidíte Autogram v ponuke **Štart** dvakrát.
- Staršiu verziu môžete odinštalovať aj sami: v nastaveniach Windows v časti **Aplikácie** vyberte Autogram s verziou 2.7 alebo nižšou a zvoľte **Odinštalovať**.
- Ak novú verziu po inštalácii neotvoríte, webové stránky budú používať staršiu, kým ju nezatvoríte alebo nereštartujete počítač. Potom už spustia novú verziu a tá vám ponúkne odinštalovanie staršej.
