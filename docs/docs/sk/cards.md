---
description: "Ktoré karty Autogram podporuje (občiansky preukaz, Gemalto IDPrime 940, MONET+ ProID+Q, I.CA SecureStore, česká eObčanka) a aký softvér k nim potrebujete."
---

# Karty a certifikáty

Na podpisovanie potrebujete podpisový certifikát a k nemu súkromný kľúč. Väčšinou sú uložené na čipovej karte, ktorú vložíte do čítačky, alebo na USB kľúči. Aby Autogram vedel s kartou pracovať, musí byť v počítači nainštalovaný jej obslužný softvér (ovládač).

Táto stránka opisuje, čo potrebujete k jednotlivým kartám. Ako sa karta vyberá pri podpise a kde zadávate PIN, je na stránke [Výber karty a zadanie PIN-u](card-and-pin.md).

| Karta | Obslužný softvér | Na stiahnutie |
| --- | --- | --- |
| [Občiansky preukaz](#obciansky-preukaz-eid) | eID klient | [slovensko.sk](https://www.slovensko.sk/sk/na-stiahnutie) |
| [Gemalto IDPrime 940](#gemalto-idprime-940) | SafeNet Authentication Client | [snca.gov.sk](https://snca.gov.sk/podpora-gemalto-idprime-940) |
| [MONET+ ProID+Q](#monet-proid-q) | ProID+ | [snca.gov.sk](https://snca.gov.sk/cipove-karty/monet-proidq/podpora-monet-proidq) |
| [I\.CA SecureStore](#i-ca-securestore) | I\.CA SecureStore | [ica.cz](https://www.ica.cz/secure-store) |
| [Česká eObčanka](#ceska-eobcanka) | eObčanka | [info.identita.gov.cz](https://info.identita.gov.cz/download/Default.aspx#SoftwareKeStazeni) |

Pripojených môžete mať aj viac kariet naraz, každú vo vlastnej čítačke. Autogram ich nájde všetky a pri podpise sa opýta, ktorú použiť.

## Občiansky preukaz (eID) {#obciansky-preukaz-eid}

Slovenský občiansky preukaz s čipom môže obsahovať certifikát na kvalifikovaný elektronický podpis.

1. Nainštalujte si **eID klient** zo stránky [slovensko.sk](https://www.slovensko.sk/sk/na-stiahnutie).
2. Ak ste certifikáty na podpisovanie ešte nemali, vydajte si ich v eID kliente. Pri novom občianskom preukaze to často treba urobiť ako prvé.
3. Vložte preukaz do čítačky a v Autograme kliknite na **Podpísať**.
4. V okne eID klienta zadajte najprv BOK (bezpečnostný osobný kód) a po výbere certifikátu podpisový PIN. Pozrite si [Výber karty a zadanie PIN-u](card-and-pin.md#obciansky-preukaz).

Okrem kvalifikovaného certifikátu je na preukaze aj druhý, ktorý sa na kvalifikované podpisovanie nepoužíva. Autogram ho predvolene neponúka, pozrite si [Viac certifikátov na jednej karte](card-and-pin.md#viac-certifikatov-na-jednej-karte).

## Ďalšie karty na kvalifikovaný podpis

Na Slovensku sa okrem občianskeho preukazu používajú najmä tieto karty od poskytovateľov kvalifikovaných certifikátov. Ku každej potrebujete obslužný softvér od jej vydavateľa. Kódy zadávate v okne Autogramu: najprv [kód k úložisku kľúčov](card-and-pin.md#kod-k-ulozisku-klucov), ak ho karta má, a pri podpise [podpisový PIN](card-and-pin.md#podpisovy-pin).

### Gemalto IDPrime 940 {#gemalto-idprime-940}

- Dodáva sa ako čipová karta do čítačky alebo ako **USB kľúč**, ktorý zasuniete priamo do počítača bez čítačky.
- Obslužný softvér je **SafeNet Authentication Client**. Stiahnete ho zo stránky [Podpora Gemalto IDPrime 940](https://snca.gov.sk/podpora-gemalto-idprime-940) Slovenskej národnej certifikačnej autority, kde je aj návod na inštaláciu.
- Kvalifikovaný certifikát býva na karte v inom slote ako ostatné, pozrite si [Viac certifikátov na jednej karte](card-and-pin.md#viac-certifikatov-na-jednej-karte).

### MONET+ ProID+Q {#monet-proid-q}

- Čipová karta do čítačky.
- Obslužný softvér je **ProID+** od spoločnosti MONET+. Stiahnete ho zo stránky [Podpora MONET+ ProID+Q](https://snca.gov.sk/cipove-karty/monet-proidq/podpora-monet-proidq) Slovenskej národnej certifikačnej autority, kde je aj návod na inštaláciu.

### I\.CA SecureStore {#i-ca-securestore}

- Čipová karta do čítačky.
- Obslužný softvér je **I\.CA SecureStore**. Stiahnete ho zo stránky [I\.CA SecureStore](https://www.ica.cz/secure-store) spoločnosti I\.CA.

## Česká eObčanka {#ceska-eobcanka}

Český občiansky preukaz s čipom funguje s nainštalovaným obslužným softvérom **eObčanka**, ktorý stiahnete zo stránky [info.identita.gov.cz](https://info.identita.gov.cz/download/Default.aspx#SoftwareKeStazeni). Autogram ho pri hľadaní kariet neprehľadáva, preto ho ponúkne vždy, keď je softvér nainštalovaný, a nikdy ho nepoužije bez pýtania.
