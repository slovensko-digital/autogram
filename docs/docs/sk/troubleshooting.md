# Riešenie problémov

## Podpisovanie na webových stránkach

### Po spustení zo stránky vidím iba hlavné okno

Je to v poriadku. Webová stránka Autogram spustila a o chvíľu mu pošle dokument, ktorý sa zobrazí v novom okne. V hlavnom okne nič neotvárajte. Ak sa dokument ani po niekoľkých sekundách nezobrazí, na stránke kliknite na podpísanie znova. Pozrite si [Keď Autogram ešte nebeží](websites.md#ked-autogram-este-nebezi).

### Stránka hlási, že Autogram nie je nainštalovaný

Stránka Autogram nenašla. Ak Autogram nainštalovaný nemáte, nainštalujte ho podľa stránky [Inštalácia](installation.md). Ak ho nainštalovaný máte, spustite ho sami, počkajte na hlavné okno a na stránke skúste podpísať znova.

![Autogram nie je nainštalovaný](/assets/images/screenshots/sk/web-signing-not-installed.png)

### Obmedzený režim {#obmedzeny-rezim}

Ak sa pri spustení zobrazí chyba **Nepodarilo sa spustiť server** s tlačidlom **Pokračovať v obmedzenom režime**, port 37200, na ktorom Autogram komunikuje s webovými stránkami, už používa iný program. Najčastejšie je to iná, už spustená kópia Autogramu. V titulku okna potom uvidíte **Autogram (obmedzený režim)**.

- Ukončite všetky spustené Autogramy a spustite ho znova.
- Ak to nepomôže, reštartujte počítač.

V obmedzenom režime môžete podpisovať súbory z počítača, podpisovanie z webových stránok však nefunguje.

## Karty a PIN

### Autogram nerozpoznal kartu {#autogram-nerozpoznal-kartu}

Ak Autogram zobrazí **Nenašla sa žiadna karta** alebo vašu kartu neponúkne, skontrolujte, či:

- je karta vložená v čítačke a čítačka pripojená k počítaču,
- máte nainštalovaný obslužný softvér (ovládač) ku karte.

Autogram rozpozná iba kartu, ku ktorej je v počítači nainštalovaný ovládač. Stiahnete ho tu:

| Karta | Obslužný softvér | Na stiahnutie |
| --- | --- | --- |
| [Občiansky preukaz](cards.md#obciansky-preukaz-eid) | eID klient | [slovensko.sk](https://www.slovensko.sk/sk/na-stiahnutie) |
| [Gemalto IDPrime 940](cards.md#gemalto-idprime-940) | SafeNet Authentication Client | [snca.gov.sk](https://snca.gov.sk/podpora-gemalto-idprime-940) |
| [MONET+ ProID+Q](cards.md#monet-proid-q) | ProID+ | [snca.gov.sk](https://snca.gov.sk/cipove-karty/monet-proidq/podpora-monet-proidq) |
| [I\.CA SecureStore](cards.md#i-ca-securestore) | I\.CA SecureStore | [ica.cz](https://www.ica.cz/secure-store) |
| [Česká eObčanka](cards.md#ceska-eobcanka) | eObčanka | [info.identita.gov.cz](https://info.identita.gov.cz/download/Default.aspx#SoftwareKeStazeni) |

Po inštalácii ovládača kliknite na **Vyhľadať karty znova**. Ak sa karta ani potom nezobrazí, reštartujte počítač. Ďalšie rady sú na stránke [Výber karty a zadanie PIN-u](card-and-pin.md#nevidite-svoju-kartu).

Pri novom občianskom preukaze si najprv overte v eID kliente, že máte vydané certifikáty na podpisovanie.

### Autogram sa pýta na kód k úložisku kľúčov

Niektoré karty majú okrem podpisového PIN-u aj kód ku karte. Ak ho vaša karta nemá, nechajte pole prázdne a kliknite na **Pokračovať**. Pozrite si [Kód k úložisku kľúčov](card-and-pin.md#kod-k-ulozisku-klucov).

### Nesprávny PIN {#nespravny-pin}

Ak Autogram hlási nesprávny PIN, skúste ho zadať znova a pozorne. Po niekoľkých nesprávnych pokusoch sa karta zablokuje. Ako ju odblokovať, zistíte u vydavateľa karty, pri občianskom preukaze v eID kliente pomocou PUK.

### Podpisovanie kartou Gemalto zlyhá

Kvalifikovaný certifikát na karte Gemalto IDPrime 940 býva v inom slote karty. Kliknite na **Podpísať iným certifikátom** a vo výbere zvoľte iný slot karty. Pozrite si [Viac certifikátov na jednej karte](card-and-pin.md#viac-certifikatov-na-jednej-karte).

### Chyba komunikácie s kartou na Windows {#chyba-komunikacie-s-kartou-na-windows}

Ak sa pri podpisovaní občianskym preukazom na Windows zobrazí **Ovládač nie je možné použiť**, v systéme pravdepodobne chýba balík Microsoft Visual C++ Redistributable. Stiahnite ho zo [stránky Microsoftu](https://learn.microsoft.com/cpp/windows/latest-supported-vc-redist), na väčšine počítačov s Windows je to súbor [vc_redist.x64.exe](https://aka.ms/vc14/vc_redist.x64.exe). Nainštalujte ho a skúste podpísať znova.

## Dokumenty a podpisy

### Dokument je chránený heslom {#dokument-je-chraneny-heslom}

PDF chránené heslom Autogram nepodpíše. Odstráňte z dokumentu ochranu heslom, napríklad v programe, v ktorom ste ho vytvorili, a podpíšte ho znova.

![Dokument je chránený heslom](/assets/images/screenshots/sk/error-locked-pdf.png)

### XML súbor sa nedá podpísať {#xml-subor-sa-neda-podpisat}

Autogram predvolene podpisuje iba XML formuláre slovenskej verejnej správy. Iný XML súbor podpíšete po zapnutí voľby **Podpisovanie neznámych XML** v [Nastaveniach](settings.md#podpisovanie).

### Podpisy sa nedajú overiť

Ak Autogram hlási chybu pri sťahovaní dôveryhodných zoznamov, nemá prístup na internet alebo je server s dôveryhodnými zoznamami nedostupný. Skontrolujte pripojenie a dokument otvorte znova. Viac je na stránke [Overenie podpisov](verification.md#doveryhodne-zoznamy).

## Stále to nefunguje {#stale-to-nefunguje}

Napíšte nám na [podpora@slovensko.digital](mailto:podpora@slovensko.digital). Pomôže, ak priložíte snímku obrazovky s chybou a text, ktorý sa zobrazí po kliknutí na **Zobraziť detail chyby**.
