Release robí slovensko.digital, ale checklist je verejný aby mohol každý contributor samostatne
pretestovať zmeny pred tým než urobí PR.

# Release Checklist

## Kontrola nového kódu

Je potrebné skontrolovať či nepribudol škodlivý kód, exfiltrácia secrets cez GH actions, či je nová core funkcionalita pokrytá testami.

## Testing

- [ ] zelené všetky automatizované testy
- [ ] funguje inštalácia na Windows cez stiahnutý .msi
- [ ] funguje inštalácia na MacOS cez stiahnutý .pkg
- [ ] funguje inštalácia na Linux (Debian-based) cez stiahnutý .deb
- [ ] funguje inštalácia na Linux cez stiahnutý .rpm
- [ ] funguje spustenie v GUI móde
- [ ] funguje spustenie v GUI serverovom móde `autogram --url=autogram://listen?protocol=http&port=37200`
- [ ] funguje URL handler na Windows [autogram://go](autogram://go)
- [ ] funguje URL handler na MacOS [autogram://go](autogram://go)
- [ ] funguje URL handler na Linux [autogram://go](autogram://go)
- [ ] funguje GUI otvoriť jeden súbor, ten sa zobrazí, viem ho podpísať, vytvorí sa podpísaný súbor
- [ ] funguje CLI `autogram --cli --source source.pdf`
- [ ] funguje CLI `autogram --cli --source source.pdf --target target.pdf`
- [ ] funguje CLI `autogram --cli --source source-dir --target target-dir`
- [ ] funguje API info request
- [ ] funguje API docs request
- [ ] funguje API sign request
- [ ] funguje s [extension](https://github.com/slovensko-digital/autogram-extension)

## Pripraviť popis releasu

vid. existujúce [Releases](https://github.com/slovensko-digital/autogram/releases)
