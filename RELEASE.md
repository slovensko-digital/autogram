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
- [ ] funguje inštalácia na Arch Linux cez `makepkg -si` z `packaging/arch` s archívom `autogram-…-arch.tar.gz` z releasu
- [ ] po zmene pre-releasu na release prebehol workflow „Publish to AUR“ a v AUR je nová verzia [autogram-bin](https://aur.archlinux.org/packages/autogram-bin)
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

## AUR (Arch Linux)

Workflow „Publish to AUR“ (`.github/workflows/publish_aur.yaml`) po zmene pre-releasu na release aktualizuje balík [autogram-bin](https://aur.archlinux.org/packages/autogram-bin). Jednorazové nastavenie:

1. AUR účet správcu balíka s verejným SSH kľúčom (*My Account* → *SSH Public Key*).
2. V repozitári *Settings* → *Environments* prostredie `aur` s pravidlom nasadenia pre tagy `v*` a so secretom `AUR_SSH_PRIVATE_KEY`, ktorý obsahuje súkromný kľúč.

Bez secretu workflow balík v AUR neaktualizuje a skončí s upozornením. Release do AUR pošlete znova spustením workflowu na jeho tagu.

## Pripraviť popis releasu

vid. existujúce [Releases](https://github.com/slovensko-digital/autogram/releases)
