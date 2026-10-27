---
description: "Prehľad nastavení Autogramu: typ podpisu, časové pečiatky, overovanie podpisov, bezpečnosť a ďalšie možnosti."
---

# Nastavenia

Nastavenia otvoríte odkazom **Nastavenia** v spodnej časti hlavného okna. Sú rozdelené do štyroch záložiek. Zmeny uložíte tlačidlom **Uložiť nastavenia**. Tlačidlo **Resetovať nastavenia** vráti všetky nastavenia na pôvodné hodnoty.

Predvolené nastavenia vyhovujú väčšine používateľov. Meňte ich, iba ak to potrebujete, napríklad keď úrad vyžaduje iný typ podpisu.

## Podpisovanie {#podpisovanie}

![Nastavenia – Podpisovanie](/assets/images/screenshots/sk/settings-signing.png)

| Nastavenie | Čo robí | Predvolene |
| --- | --- | --- |
| **Typ podpisu pre PDF** | Ako sa podpíše PDF: PAdES (podpis vo vnútri PDF), XAdES alebo CAdES (kontajner ASiC-E). Pri už podpísanom dokumente sa použije rovnaký typ, ako má existujúci podpis. Pozrite si [Formáty a typy podpisov](formats.md). | PAdES |
| **Zdroje časových pečiatok** | Odkiaľ sa časové pečiatky získavajú. Predvolene sú to bezplatní kvalifikovaní poskytovatelia z Belgicka a Španielska. Môžete zadať aj vlastné adresy. | bezplatní poskytovatelia |
| **Vlastné adresy TSP serverov** | Adresy vlastných poskytovateľov časových pečiatok, viac adries oddeľte čiarkou bez medzier. | prázdne |
| **Vždy podpisovať hromadne** | Pri výbere viacerých dokumentov sa Autogram nepýta na spôsob podpisovania a dokumenty podpíše hromadne, bez zobrazenia. | Nie |
| **Automatické odpojenie pri nečinnosti** | Po koľkých minútach nečinnosti sa ukončí spojenie s kartou. Potom treba znova zadať PIN. | 5 |
| **Podpisovanie neznámych XML** | Umožní podpísať XML súbory, ktoré nie sú formulárom slovenskej verejnej správy. | Nie |
| **Použitie nového štandardu** | Podpisovanie podľa noriem ETSI EN 319 132 a ETSI EN 319 122. Systémy verejnej správy ho väčšinou nepodporujú. | Nie |

Pridanie časovej pečiatky vyberáte pri každom podpise v okne **Aký podpis potrebujete?**, nie v nastaveniach. Aby bola pečiatka pri overovaní uznaná, jej poskytovateľ musí byť kvalifikovaný a jeho krajina musí byť zapnutá v záložke **Overovanie podpisov**.

## Overovanie podpisov {#overovanie-podpisov}

![Nastavenia – Overovanie podpisov](/assets/images/screenshots/sk/settings-validation.png)

Zoznam krajín EÚ, podľa ktorých dôveryhodných zoznamov Autogram overuje podpisy. Predvolene sú zapnuté Slovensko, Česko, Rakúsko, Poľsko, Maďarsko, Belgicko, Holandsko a Španielsko. Ak dostávate dokumenty podpísané certifikátom z inej krajiny, zapnite ju. Viac krajín overovanie spomalí.

Podľa týchto zoznamov Autogram zisťuje aj to, či je kvalifikovaný certifikát, ktorým podpisujete. Ak máte certifikát z inej krajiny, zapnite aj ju, inak ho Autogram vo [výbere certifikátu](card-and-pin.md#vyber-certifikatu) označí ako nekvalifikovaný.

## Bezpečnosť {#bezpecnost}

![Nastavenia – Bezpečnosť](/assets/images/screenshots/sk/settings-security.png)

| Nastavenie | Čo robí | Predvolene |
| --- | --- | --- |
| **Kontrola platnosti podpisov** | Pred podpisom upozorní na neplatné podpisy, ktoré už dokument obsahuje. Pozrite si [Upozornenia pred podpisom](warnings.md#nedoveryhodne-podpisy). | Áno |
| **Kontrola súladu s PDF/A formátom** | Pred podpisom upozorní, ak PDF nie je vo formáte PDF/A. Pozrite si [Upozornenia pred podpisom](warnings.md#pdfa). | Áno |
| **Povoliť podpisovanie exspirovanými certifikátmi** | Vo výbere certifikátu sa zobrazia aj certifikáty s uplynutou platnosťou. Podpis takým certifikátom úrady nemusia prijať. | Nie |
| **Povoliť podpisovanie nekvalifikovanými certifikátmi** | Vo výbere certifikátu sa dá vybrať aj certifikát, ktorý podľa dôveryhodných zoznamov nie je kvalifikovaný. Podpis takým certifikátom nie je kvalifikovaný elektronický podpis a úrady ho nemusia prijať. Pozrite si [Výber certifikátu](card-and-pin.md#vyber-certifikatu). | Nie |
| **Zobrazovať detailné chyby** | Ak sa súbor nepodarí otvoriť, zobrazí sa podrobný popis chyby. | Áno |

## Iné {#ine}

![Nastavenia – Iné](/assets/images/screenshots/sk/settings-other.png)

| Nastavenie | Čo robí | Predvolene |
| --- | --- | --- |
| **Jazyk** | Jazyk Autogramu: slovenčina, angličtina alebo jazyk systému. | jazyk systému |
| **Zapnúť Autogram API** | Umožní webovým stránkam a iným aplikáciám posielať Autogramu dokumenty na podpis. Bez neho [podpisovanie na webových stránkach](websites.md) nefunguje. Zmena sa prejaví po reštarte Autogramu. | Áno |
| **Rozlíšenie náhľadu PDF** | Ostrosť náhľadu PDF. Vyššie rozlíšenie je ostrejšie, no veľké dokumenty sa zobrazujú pomalšie. | 100 dpi |
| **Zobraziť EP certifikát občianskeho preukazu** | Vo výbere karty ponúkne aj druhý, nekvalifikovaný certifikát občianskeho preukazu. Na podpis ním treba zapnúť aj **Povoliť podpisovanie nekvalifikovanými certifikátmi**. Pozrite si [Viac certifikátov na jednej karte](card-and-pin.md#viac-certifikatov-na-jednej-karte). | Nie |
| **Cesta k vlastnému úložisku kľúčov** | Súbor s certifikátom a kľúčom (JKS) pre voľbu **Zo súboru**. | prázdne |
| **Cesta k vlastnému ovládaču pre PKCS11 token** | Ovládač karty, ktorú Autogram nepozná. | prázdne |
| **HTTP proxy** | Proxy server vo formáte `http://host:port`, cez ktorý Autogram posiela všetku komunikáciu do internetu: sťahovanie dôveryhodných zoznamov, overovanie platnosti certifikátov (CRL, OCSP), časové pečiatky, formuláre a kontrolu aktualizácií. Autogram dôveruje aj certifikátom, ktorým dôveruje operačný systém, takže funguje aj s proxy, ktorá kontroluje HTTPS komunikáciu. Prihlasovanie do proxy nie je podporované. Zmena sa prejaví po reštarte Autogramu. | prázdne (bez proxy) |
