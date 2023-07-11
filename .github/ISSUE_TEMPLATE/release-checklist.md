---
name: Release checklist
about: Použi tento checklist na testovanie artefaktov pred public releaseom
title: "Release "
labels: release
assignees: @slovensko-digital/autogram-release-team
---

## Windows

- [ ] funguje inštalácia na Windows cez stiahnutý .msi
- [ ] funguje spustenie v GUI móde
- [ ] funguje URL handler [autogram://go](autogram://go)
- [ ] funguje GUI otvoriť jeden súbor, ten sa zobrazí, viem ho podpísať, vytvorí sa podpísaný súbor

## Linux
- [ ] funguje inštalácia na Linux (Debian-based) cez stiahnutý .deb
- [ ] funguje inštalácia na Linux cez stiahnutý .rpm
- [ ] funguje spustenie v GUI móde
- [ ] funguje URL handler [autogram://go](autogram://go)
- [ ] funguje GUI otvoriť jeden súbor, ten sa zobrazí, viem ho podpísať, vytvorí sa podpísaný súbor

## MacOS
- [ ] funguje inštalácia na MacOS cez stiahnutý .pkg
- [ ] funguje spustenie v GUI móde
- [ ] funguje URL handler [autogram://go](autogram://go)
- [ ] funguje GUI otvoriť jeden súbor, ten sa zobrazí, viem ho podpísať, vytvorí sa podpísaný súbor


## Na aspoň jednom systéme

- [ ] fungujú všetky smoke testy `./mvnw test -Psmoke`
- [ ] funguje spustenie v GUI serverovom móde `autogram --url=autogram://listen?protocol=http&host=localhost&port=37201&origin=*&language=sk` na inom porte
- [ ] funguje CLI `autogram --cli --source source.pdf`
- [ ] funguje CLI `autogram --cli --source source.pdf --target target.pdf`
- [ ] funguje CLI `autogram --cli --source source-dir --target target-dir`
- [ ] funguje API info request
- [ ] funguje API docs request
- [ ] funguje API sign request
- [ ] funguje s [extension](https://github.com/slovensko-digital/autogram-extension)