---
description: "Pre vývojárov: ako prepojiť webovú stránku alebo aplikáciu s Autogramom cez Autogram SDK alebo HTTP API."
---

# Integrácia cez HTTP API

Táto stránka je pre vývojárov webových stránok a aplikácií, ktoré chcú podpisovať cez Autogram. Používateľ musí mať Autogram nainštalovaný vo svojom počítači.

## Autogram SDK

Najjednoduchšie je použiť knižnicu [Autogram SDK](https://github.com/slovensko-digital/autogram-extension/tree/master/autogram-sdk) (npm balík `autogram-sdk`). Postará sa o spustenie Autogramu, počká, kým začne odpovedať, a zobrazí na stránke hotový dialóg, v ktorom si používateľ vyberie podpisovanie v počítači alebo v mobile ([Autogram v mobile](https://sluzby.slovensko.digital/autogram-v-mobile/)). Rovnaký dialóg používa aj [rozšírenie do prehliadača](websites.md#vyber-medzi-pocitacom-a-mobilom), takže ho používatelia poznajú.

```ts
import { createAutogramClient } from "autogram-sdk/with-ui";

const client = await createAutogramClient();
const { content } = await client.sign(
  { content: "hello world", mimeType: "text/plain", filename: "hello.txt" },
  { form: "XAdES", container: "ASiC_E" }
);
```

Dokumentácia knižnice je na [GitHub Pages](https://slovensko-digital.github.io/autogram-extension/autogram-sdk/).

## Ako to funguje

Autogram spúšťa v počítači používateľa lokálny server na adrese `http://localhost:37200`. Vaša stránka mu cez HTTP pošle dokument, Autogram ho zobrazí, používateľ ho skontroluje a podpíše a Autogram vráti podpísaný dokument v odpovedi.

- Dokumentácia API (Swagger) je na [GitHube](https://generator3.swagger.io/index.html?url=https://raw.githubusercontent.com/slovensko-digital/autogram/main/src/main/resources/digital/slovensko/autogram/server/server.yml) a po spustení Autogramu aj na <http://localhost:37200/docs>.
- Ak Autogram nebeží, spustíte ho otvorením adresy s protokolom `autogram://`, napríklad `autogram://go`. Potom počkajte, kým začne odpovedať `GET /api/v1/info`, a až potom pošlite dokument. Používateľ zatiaľ môže vidieť iba hlavné okno Autogramu, preto mu na stránke ukážte, že má počkať a dokument v Autograme neotvárať sám.
- Podpisovanie jedného dokumentu: `POST /api/v1/sign` s poľom `document`.
- Viac dokumentov v jednom kontajneri ASiC-E: `POST /api/v1/sign` s poľom `documents`, pozrite si [Viac dokumentov v jednom kontajneri](#viac-dokumentov-v-jednom-kontajneri).
- Formulár Finančnej správy: identifikátor formulára v `xdcParameters.fsFormIdentifier`, pozrite si [Formuláre Finančnej správy](#formulare-financnej-spravy).
- Viac samostatných dokumentov: batch API `/api/v1/batch`, používateľ si vyberie, či ich podpíše po jednom alebo hromadne.
- Časovú pečiatku si vyžiadate úrovňou `BASELINE_T` (v `/api/v1/sign` parametrom `profile`). Autogram ju získa zo [zdrojov časových pečiatok](timestamps.md) v nastaveniach používateľa, predvolene od bezplatných kvalifikovaných poskytovateľov.
- Zoznam certifikátov: `GET /api/v1/certificates`. Autogram si pred odoslaním certifikátov vypýta [súhlas používateľa](websites.md#suhlas-s-precitanim-certifikatov).

## Viac dokumentov v jednom kontajneri {#viac-dokumentov-v-jednom-kontajneri}

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

## Formuláre Finančnej správy {#formulare-financnej-spravy}

XML formuláre Finančnej správy, napríklad daňové priznania, väčšinou nemajú v koreňovom elemente menný priestor, podľa ktorého by Autogram zistil, o aký formulár ide. Identifikátor formulára mu preto pošlite v parametri `fsFormIdentifier` v `xdcParameters` dokumentu. Autogram potom nájde XSD a XSLT formulára vo verejnom zozname formulárov Slovensko.Digital, XML overí podľa XSD, vytvorí z neho XML Datacontainer, používateľovi ho zobrazí v čitateľnej podobe a podpíše ho podpisom XAdES.

```json
{
  "parameters": { "form": "XAdES" },
  "documents": [
    {
      "filename": "danove-priznanie.xml",
      "mimeType": "application/xml;base64",
      "content": "PGRva3VtZW50Pj...",
      "xdcParameters": { "autoLoadEform": true, "fsFormIdentifier": "792_772" }
    }
  ]
}
```

- Identifikátor má tvar `číslo_číslo`, napríklad `792_772` je daňové priznanie k dani z príjmov fyzickej osoby typ B za rok 2023. Identifikátory s názvami formulárov nájdete v súbore [forms.xml](https://forms-slovensko-digital.s3.eu-central-1.amazonaws.com/fs/forms.xml) v atribúte `sdIdentifier`.
- Ak formulár posielate už v XML Datacontaineri alebo v kontajneri ASiC-E, identifikátor netreba. Autogram formulár rozpozná podľa jeho identifikátora v kontajneri.
- Identifikátor v nesprávnom tvare alebo XML, ktoré nezodpovedá XSD formulára, vráti chybu 422 (`UNPROCESSABLE_INPUT`). Neznámy identifikátor vráti chybu 400 (`UNKNOWN_EFORM`).
- Na nájdenie formulára potrebuje Autogram pripojenie na internet.
- Staršie API `POST /sign` má na to parameter `parameters.fsFormId`.

## Čo musí mať používateľ

- Nainštalovaný Autogram s nastavením **Zapnúť Autogram API** (predvolene zapnuté).
- Kartu s certifikátom a jej obslužný softvér, pozrite si [Karty a certifikáty](cards.md).

## Obmedzený režim

Ak je port 37200 obsadený, napríklad inou aplikáciou, Autogram sa spustí v obmedzenom režime bez API. Podpisovanie súborov v aplikácii funguje, podpisovanie z webu nie. Pozrite si [Riešenie problémov](troubleshooting.md#obmedzeny-rezim).
