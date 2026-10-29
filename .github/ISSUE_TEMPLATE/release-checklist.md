---
name: Release checklist
about: Použi tento checklist na testovanie artefaktov pred public releaseom
title: "Release "
labels: release
---

Postup vydania je v [RELEASE.md](https://github.com/slovensko-digital/autogram/blob/main/RELEASE.md).

## Release

- [ ] release má všetkých 7 inštalátorov: `-windows.msi`, `-windows-store.msix`, `-macos.pkg` (Apple silicon), `-macos-intel.pkg`, `-debian.deb`, `-ubuntu.deb`, `-fedora.rpm`
- [ ] v okne **O Autograme** je verzia releasu
- [ ] popis releasu zodpovedá zmenám, pri bezpečnostnej oprave obsahuje upozornenie

## Windows – Microsoft Store (.msix)

Balík podpísať testovacím certifikátom a nainštalovať, ideálne vo Windows Sandbox bez JDK v PATH, pozri [docs/windows-store-testing.md](https://github.com/slovensko-digital/autogram/blob/main/docs/windows-store-testing.md).

- [ ] funguje spustenie z ponuky Štart
- [ ] funguje podpis kartou s každým ovládačom, ktorý je k dispozícii
- [ ] funguje podpis z webu cez [autogram://go](autogram://go) a cez [extension](https://github.com/slovensko-digital/autogram-extension)
- [ ] *Otvoriť pomocou* otvorí `.pdf` aj `.asice` v Autograme, aj keď už beží
- [ ] funguje CLI `autogram-cli --help` v termináli
- [ ] keď je nainštalovaná aj verzia z `.msi`, Autogram ponúkne jej odinštalovanie a odinštaluje ju
- [ ] nezobrazí sa okno o novej verzii, aktualizácie zabezpečuje Store

## Windows – inštalátor .msi

- [ ] funguje inštalácia cez stiahnutý `.msi`, inštalátor je podpísaný (Vlastnosti → Digitálne podpisy)
- [ ] funguje spustenie v GUI móde
- [ ] funguje URL handler [autogram://go](autogram://go)
- [ ] funguje GUI otvoriť jeden súbor, ten sa zobrazí, viem ho podpísať, vytvorí sa podpísaný súbor
- [ ] podpísaný súbor otvorím v Autograme a pod náhľadom dokumentu je zobrazený môj podpis
- [ ] kliknem na "Zobraziť detail podpisov" a otvorí sa detail podpisov
- [ ] kliknem na "Zobraziť technické detaily" a otvorí sa report
- [ ] funguje CLI `autogram-cli --help` v priečinku `C:\Program Files\Autogram`

## macOS – Apple silicon (`-macos.pkg`) aj Intel (`-macos-intel.pkg`)

- [ ] funguje inštalácia cez stiahnutý `.pkg` bez varovania, že vývojára nemožno overiť (balík je podpísaný a notarizovaný)
- [ ] funguje spustenie v GUI móde
- [ ] funguje URL handler [autogram://go](autogram://go)
- [ ] funguje GUI otvoriť jeden súbor, ten sa zobrazí, viem ho podpísať, vytvorí sa podpísaný súbor
- [ ] podpísaný súbor otvorím v Autograme a pod náhľadom dokumentu je zobrazený môj podpis
- [ ] kliknem na "Zobraziť detail podpisov" a otvorí sa detail podpisov
- [ ] kliknem na "Zobraziť technické detaily" a otvorí sa report
- [ ] funguje CLI `/Applications/Autogram.app/Contents/Library/AutogramHelper.app/Contents/MacOS/Autogram --help`

## Linux – Debian a Ubuntu (`.deb`), Fedora (`.rpm`)

- [ ] funguje inštalácia každého balíka na jeho systéme
- [ ] funguje spustenie v GUI móde
- [ ] funguje URL handler [autogram://go](autogram://go)
- [ ] funguje GUI otvoriť jeden súbor, ten sa zobrazí, viem ho podpísať, vytvorí sa podpísaný súbor
- [ ] podpísaný súbor otvorím v Autograme a pod náhľadom dokumentu je zobrazený môj podpis
- [ ] kliknem na "Zobraziť detail podpisov" a otvorí sa detail podpisov
- [ ] kliknem na "Zobraziť technické detaily" a otvorí sa report
- [ ] funguje CLI `autogram --help`

## Podpisovanie a overovanie (na aspoň jednom systéme)

- [ ] pri dvoch vložených kartách sa Autogram opýta, ktorú použiť
- [ ] viac súborov naraz sa dá podpísať po jednom aj hromadne a PIN sa zadáva iba raz
- [ ] kontajner ASiC-E s viacerými dokumentmi ukáže dokumenty v záložkách, overí ich podpisy a pridá podpis
- [ ] funguje podpis s časovou pečiatkou
- [ ] detail podpisov sa otvorí aj vtedy, keď overovanie podpisov ešte beží

## Server, API a CLI (na aspoň jednom systéme)

- [ ] fungujú všetky smoke testy `./mvnw test -Psmoke`
- [ ] funguje spustenie v GUI serverovom móde `autogram --url=autogram://listen?protocol=http&port=37201` na inom porte
- [ ] funguje API info request (`GET /info`)
- [ ] funguje API docs request (`GET /docs`)
- [ ] funguje API certificates request (`GET /certificates`) po súhlase s prečítaním certifikátov
- [ ] funguje API sign request (`POST /api/v1/sign`) s jedným dokumentom aj s viacerými dokumentmi do jedného kontajnera
- [ ] funguje starší API sign request (`POST /sign`) a batch (`/batch`)
- [ ] funguje s [extension](https://github.com/slovensko-digital/autogram-extension)
- [ ] funguje CLI `autogram --cli --source source.pdf`
- [ ] funguje CLI `autogram --cli --source source.pdf --target target.pdf`
- [ ] funguje CLI `autogram --cli --source source-dir --target target-dir`

## Po vydaní

- [ ] `.msix` je odoslaný do Microsoft Store a prešiel certifikáciou
- [ ] release je označený ako latest
- [ ] [dokumentácia](https://docs.autogram.slovensko.digital) ukazuje novú verziu a jej zmeny v časti Čo je nové
- [ ] [stránka na stiahnutie](https://sluzby.slovensko.digital/autogram/#download) ponúka novú verziu
