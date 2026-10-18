---
description: "Ako podpisovať a overovať elektronické dokumenty bezplatnou aplikáciou Autogram pre Windows, macOS a Linux, na webových stránkach aj v počítači."
titleAppend: false
---

# Používateľská príručka Autogramu

Autogram je bezplatná aplikácia pre Windows, macOS a Linux na podpisovanie a overovanie elektronických dokumentov. Podpisy vytvára podľa európskeho nariadenia eIDAS a slovenských štandardov, takže ich prijímajú úrady aj súkromné firmy. Autogram vyvíja Slovensko.Digital a jeho zdrojové kódy sú verejné.

## Čo s Autogramom urobíte

- **Podpíšete dokument na webovej stránke.** Webové stránky a aplikácie, ktoré Autogram podporujú, mu dokument pošlú samy a po podpise si ho samy prevezmú. Nič nesťahujete ani nenahrávate. Takto Autogram používa väčšina ľudí. Pozrite si [Podpisovanie na webových stránkach](websites.md).
- **Podpíšete súbor z počítača.** PDF, formulár slovenskej verejnej správy, obrázok, text alebo iný súbor. Pozrite si [Podpísanie súboru z počítača](signing.md).
- **Podpíšete viac dokumentov s jedným PIN-om.** Vyberte niekoľko súborov alebo celý priečinok a podpíšte ich po jednom alebo hromadne. PIN zadáte iba raz, nech je dokumentov koľkokoľvek. Pozrite si [Viac dokumentov naraz](batch.md).
- **Pridáte k podpisu kvalifikovanú časovú pečiatku.** Pri takom podpise sa nevyžaduje osvedčenie pravosti podpisu, napríklad u notára. Autogram pečiatky získava zadarmo od verejne dostupných poskytovateľov z iných krajín EÚ. Pozrite si [Podpis s časovou pečiatkou](timestamps.md).
- **Overíte podpisy.** Po otvorení podpísaného dokumentu uvidíte, kto ho podpísal, kedy a či je podpis platný a kvalifikovaný. Pozrite si [Overenie podpisov](verification.md).

Pokročilí používatelia môžu podpisovať aj z [príkazového riadka](cli.md) a vývojári môžu Autogram prepojiť so svojou webovou aplikáciou cez [HTTP API](api.md).

## Čo potrebujete

1. **Autogram** nainštalovaný v počítači. Pozrite si [Inštalácia](installation.md).
2. **Podpisový certifikát** na čipovej karte alebo inom zariadení, napríklad:
   - slovenský občiansky preukaz s čipom, ktorý má vydané certifikáty na podpisovanie, a nainštalovaný eID klient,
   - kartu na kvalifikovaný podpis Gemalto IDPrime 940 (aj v podobe USB kľúča), MONET+ ProID+Q alebo I\.CA SecureStore a nainštalovaný obslužný softvér karty,
   - českú eObčanku a nainštalovaný softvér eObčanka.
3. **Čítačku kariet**, ak ju počítač nemá zabudovanú. Pri USB kľúči čítačku nepotrebujete.
4. **PIN** k certifikátu.

Podrobnosti o jednotlivých kartách sú na stránke [Karty a certifikáty](cards.md).

Na overenie podpisov certifikát ani karta potrebné nie sú, stačí pripojenie na internet.

::: callout info "Podpisovanie na štátnych weboch"
Na štátnych weboch, napríklad na slovensko\.sk, portáli Finančnej správy alebo v Obchodnom registri, potrebujete aj [rozšírenie do prehliadača](https://sluzby.slovensko.digital/autogram-na-statnych-weboch#download). Weby, ktoré Autogram podporujú priamo, ho nepotrebujú. Pozrite si [Dva druhy webov](websites.md#dva-druhy-webov).
:::

## Používate Autogram prvýkrát?

Prejdite si návod [Prvý podpis krok za krokom](first-signature.md). Ku každému kroku má obrázok a vysvetlí aj okná, ktoré sa zobrazia iba pri niektorých kartách. Nižšie je stručný postup pre tých, ktorí už Autogram poznajú.

## Ako podpísať dokument na webovej stránke

::: steps
1. Na stránke kliknite na tlačidlo na podpísanie. Ak sa stránka opýta, ako chcete dokument podpísať, zvoľte **Podpísať v tomto počítači**.
2. Ak Autogram ešte nebeží, stránka ho spustí a zobrazí sa jeho hlavné okno. Nič v ňom neotvárajte a chvíľu počkajte. Dokument vám stránka pošle sama.
3. Prezrite si dokument a kliknite na **Podpísať**.
4. Vyberte kartu a certifikát a zadajte PIN.
5. Podpísaný dokument sa vráti na webovú stránku.
:::

Podrobnosti sú na stránke [Podpisovanie na webových stránkach](websites.md).

## Ako podpísať súbor z počítača

::: steps
1. Spustite Autogram a kliknite na **Vyberte súbor**, alebo súbor presuňte do okna aplikácie.
2. Prezrite si dokument v náhľade. Ak už obsahuje podpisy, Autogram ich overí a zobrazí pod náhľadom.
3. Kliknite na **Podpísať**.
4. Vyberte kartu a certifikát a zadajte PIN. Pozrite si [Výber karty a zadanie PIN-u](card-and-pin.md).
5. Autogram uloží podpísaný súbor do priečinka, v ktorom je pôvodný dokument, a ukáže, kde ho nájdete.
:::

PDF ostane po podpísaní PDF. Ostatné súbory Autogram uloží spolu s podpisom do kontajnera s príponou `.asice`. Pozrite si [Podpísanie súboru z počítača](signing.md#kam-sa-podpisany-subor-ulozi).

Vybraný certifikát si Autogram zapamätá. Ďalší dokument už podpíšete tlačidlom **Podpísať ako …** s vaším menom.

::: callout warning "Podpisujte iba to, čo ste videli"
Kvalifikovaný elektronický podpis má rovnaké právne účinky ako vlastnoručný. Autogram preto pred podpisom vždy zobrazí obsah dokumentu. Ak dokument zobraziť nevie, upozorní vás na to. Takýto dokument podpíšte, iba ak dôverujete jeho zdroju.
:::

## Pomoc a kontakt

- Najčastejšie problémy a ich riešenie: [Riešenie problémov](troubleshooting.md).
- Otázky a problémy s Autogramom: [podpora@slovensko.digital](mailto:podpora@slovensko.digital).
- Chyby a návrhy na zlepšenie môžete nahlásiť aj na [GitHube](https://github.com/slovensko-digital/autogram/issues).
- Autogram je open-source pod licenciou EUPL 1.2 a môžete ho používať komerčne aj nekomerčne. Zdrojové kódy sú na [GitHube](https://github.com/slovensko-digital/autogram).
