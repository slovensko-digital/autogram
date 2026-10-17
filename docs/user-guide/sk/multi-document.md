# Viac dokumentov v jednom kontajneri (ASiC-E)

Kontajner ASiC-E (prípona `.asice`) môže obsahovať niekoľko dokumentov, ktoré sú podpísané spoločne. Na Slovensku sa to nazýva aj *spoločná autorizácia dokumentov*. Autogram takýto kontajner zobrazí, overí v ňom podpisy a pridá k nemu váš podpis.

## Otvorenie kontajnera

1. Spustite Autogram.
2. Kliknite na **Vyberte súbor** alebo súbor `.asice` presuňte do okna aplikácie.

Kontajner môže Autogramu poslať aj webová stránka alebo iná aplikácia (napríklad Autogram Portal), vtedy sa podpisové okno otvorí samo.

## Dokumenty v záložkách

Ak kontajner obsahuje viac dokumentov, nad náhľadom sa zobrazí jedna záložka pre každý dokument. Autogram vždy otvorí prvý dokument. Ostatné si pozriete kliknutím na ich záložku alebo klávesnicou (Tab a šípky).

![Kontajner s dvoma dokumentmi v záložkách](../screenshots/sk/multi-document-tabs.png)

- Dlhý názov súboru je v záložke skrátený, celý názov uvidíte po podržaní myši nad záložkou.
- Dokument bez názvu sa zobrazí ako „Dokument 1“, „Dokument 2“ atď.
- Každý dokument sa zobrazí vo svojom formáte: PDF, obrázok, text alebo XML formulár. Formulár slovenskej verejnej správy (XML Data Container) sa zobrazí v čitateľnej podobe.

![Záložka s XML formulárom](../screenshots/sk/multi-document-xml-form.png)

Pred podpisom si prezrite všetky záložky. Podpisom potvrdzujete obsah všetkých dokumentov v kontajneri, nie iba toho, ktorý práve vidíte.

## Podpisy v kontajneri

Ak kontajner už obsahuje podpisy, Autogram ich overí a pod náhľadom zobrazí podpisy pre dokument vo vybranej záložke.

Ak niektorý podpis nepokrýva všetky dokumenty v kontajneri, zobrazí sa upozornenie **Podpisy nie sú rovnaké na všetkých dokumentoch**. Znamená to, že niektorý podpisujúci podpísal iba časť dokumentov. Pozrite si podpisy v jednotlivých záložkách a zvážte, či chcete pokračovať.

![Upozornenie na podpisy, ktoré nepokrývajú všetky dokumenty](../screenshots/sk/multi-document-incomplete-coverage.png)

Podrobnosti o podpisoch otvoríte tlačidlom **Zobraziť detail podpisov**.

## Podpísanie

Autogram vždy podpíše všetky dokumenty v kontajneri jedným podpisom. Podpísať iba niektoré dokumenty nie je možné.

- Ak ste otvorili existujúci kontajner, tlačidlo **Podpísať** pridá váš podpis ku kontajneru.
- Ak webová stránka poslala na podpis niekoľko samostatných dokumentov, tlačidlo sa volá **Podpísať všetko** (po výbere certifikátu **Podpísať všetko ako …**). Autogram z nich vytvorí jeden nový kontajner ASiC-E.

![Tlačidlo Podpísať všetko](../screenshots/sk/multi-document-sign-all.png)

Po podpísaní Autogram uloží podpísaný kontajner a ukáže, kde ho nájdete.

## Pre integrátorov

Nový kontajner z viacerých dokumentov vytvoríte cez HTTP API `POST /api/v1/sign` s poľom `documents` (najviac 5 dokumentov, formát `XAdES` alebo `CAdES`, kontajner `ASiC_E`). Dokument typu ASiC-E nemôže byť súčasťou takejto požiadavky. Podrobnosti a príklady sú v [dokumentácii API](https://generator3.swagger.io/index.html?url=https://raw.githubusercontent.com/slovensko-digital/autogram/main/src/main/resources/digital/slovensko/autogram/server/server.yml).

```json
{
  "parameters": { "form": "XAdES", "container": "ASiC_E" },
  "documents": [
    { "filename": "zmluva.pdf", "mimeType": "application/pdf;base64", "content": "JVBERi0xLjUK..." },
    { "filename": "priloha.txt", "mimeType": "text/plain", "content": "Príloha k zmluve" }
  ]
}
```
