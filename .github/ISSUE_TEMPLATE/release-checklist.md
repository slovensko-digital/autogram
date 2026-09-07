---
name: Release checklist
about: Použi tento checklist na testovanie artefaktov pred public releaseom
title: "Release "
labels: release
---

## Windows

- [ ] funguje inštalácia na Windows cez stiahnutý .msi
- [ ] funguje spustenie v GUI móde
- [ ] funguje URL handler [autogram://go](autogram://go)
- [ ] funguje GUI otvoriť jeden súbor, ten sa zobrazí, viem ho podpísať, vytvorí sa podpísaný súbor
- [ ] podpísaný súbor otvorím v autograme a pod náhľadom dokumentu je zobrazený môj podpis
- [ ] kliknem na "Zobraziť detail podpisov" a otvorí sa detail podpisov
- [ ] kliknem na "Zobraziť technické detaily" a otvorí sa report
- [ ] funguje CLI `autogram-cli --help`
- [ ] v nastaveniach funguje zapnutie/vypnutie "Hromadné podpisovanie"
- [ ] pri zapnutom "Hromadnom podpisovaní" sa výber režimu nezobrazí a dokumenty sa podpíšu naraz bez zobrazenia
- [ ] pri vypnutom "Hromadnom podpisovaní" sa zobrazí otázka "Ako chcete podpísať dokumenty?"
- [ ] funguje "Podpisovať po jednom" - zobrazí sa náhľad prvého dokumentu a PIN sa uchováva medzi dokumentmi
- [ ] pri podpisovaní po jednom funguje "Preskočiť" aj "Ukončiť podpisovanie", vrátane posledného dokumentu
- [ ] pri podpise jedného dokumentu sa zobrazí iba "Zrušiť" a PIN použije natívny driver

## Linux
- [ ] funguje inštalácia na Linux (Debian-based) cez stiahnutý .deb
- [ ] funguje inštalácia na Linux cez stiahnutý .rpm
- [ ] funguje spustenie v GUI móde
- [ ] funguje URL handler [autogram://go](autogram://go)
- [ ] funguje GUI otvoriť jeden súbor, ten sa zobrazí, viem ho podpísať, vytvorí sa podpísaný súbor
- [ ] podpísaný súbor otvorím v autograme a pod náhľadom dokumentu je zobrazený môj podpis
- [ ] kliknem na "Zobraziť detail podpisov" a otvorí sa detail podpisov
- [ ] kliknem na "Zobraziť technické detaily" a otvorí sa report
- [ ] funguje CLI `autogram --help`
- [ ] v nastaveniach funguje zapnutie/vypnutie "Hromadné podpisovanie"
- [ ] pri zapnutom "Hromadnom podpisovaní" sa výber režimu nezobrazí a dokumenty sa podpíšu naraz bez zobrazenia
- [ ] pri vypnutom "Hromadnom podpisovaní" sa zobrazí otázka "Ako chcete podpísať dokumenty?"
- [ ] funguje "Podpisovať po jednom" - zobrazí sa náhľad prvého dokumentu a PIN sa uchováva medzi dokumentmi
- [ ] pri podpisovaní po jednom funguje "Preskočiť" aj "Ukončiť podpisovanie", vrátane posledného dokumentu
- [ ] pri podpise jedného dokumentu sa zobrazí iba "Zrušiť" a PIN použije natívny driver

## MacOS
- [ ] funguje inštalácia na MacOS cez stiahnutý .pkg
- [ ] funguje spustenie v GUI móde
- [ ] funguje URL handler [autogram://go](autogram://go)
- [ ] funguje GUI otvoriť jeden súbor, ten sa zobrazí, viem ho podpísať, vytvorí sa podpísaný súbor
- [ ] podpísaný súbor otvorím v autograme a pod náhľadom dokumentu je zobrazený môj podpis
- [ ] kliknem na "Zobraziť detail podpisov" a otvorí sa detail podpisov
- [ ] kliknem na "Zobraziť technické detaily" a otvorí sa report
- [ ] funguje CLI `/Applications/Autogram.app/Contents/MacOS/AutogramApp --help`
- [ ] v nastaveniach funguje zapnutie/vypnutie "Hromadné podpisovanie"
- [ ] pri zapnutom "Hromadnom podpisovaní" sa výber režimu nezobrazí a dokumenty sa podpíšu naraz bez zobrazenia
- [ ] pri vypnutom "Hromadnom podpisovaní" sa zobrazí otázka "Ako chcete podpísať dokumenty?"
- [ ] funguje "Podpisovať po jednom" - zobrazí sa náhľad prvého dokumentu a PIN sa uchováva medzi dokumentmi
- [ ] pri podpisovaní po jednom funguje "Preskočiť" aj "Ukončiť podpisovanie", vrátane posledného dokumentu
- [ ] pri podpise jedného dokumentu sa zobrazí iba "Zrušiť" a PIN použije natívny driver


## Na aspoň jednom systéme

- [ ] fungujú všetky smoke testy `./mvnw test -Psmoke`
- [ ] funguje spustenie v GUI serverovom móde `autogram --url=autogram://listen?protocol=http&port=37201` na inom porte
- [ ] funguje CLI `autogram --cli --source source.pdf`
- [ ] funguje CLI `autogram --cli --source source.pdf --target target.pdf`
- [ ] funguje CLI `autogram --cli --source source-dir --target target-dir`
- [ ] funguje API info request
- [ ] funguje API docs request
- [ ] funguje API sign request
- [ ] funguje s [extension](https://github.com/slovensko-digital/autogram-extension)
