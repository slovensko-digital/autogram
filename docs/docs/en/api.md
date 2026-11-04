---
description: "For developers: how to connect a website or application to Autogram through the Autogram SDK or the HTTP API."
---

# Integration through the HTTP API

This page is for developers of websites and applications that want to sign through Autogram. The user must have Autogram installed on their computer.

## Autogram SDK

The easiest way is the [Autogram SDK](https://github.com/slovensko-digital/autogram-extension/tree/master/autogram-sdk) library (npm package `autogram-sdk`). It starts Autogram, waits until it responds and shows a ready-made dialog on your page in which the user chooses signing on the computer or on mobile ([Autogram v mobile](https://sluzby.slovensko.digital/autogram-v-mobile/)). The [browser extension](websites.md#choosing-between-computer-and-mobile) uses the same dialog, so users already know it.

```ts
import { createAutogramClient } from "autogram-sdk/with-ui";

const client = await createAutogramClient();
const { content } = await client.sign(
  { content: "hello world", mimeType: "text/plain", filename: "hello.txt" },
  { form: "XAdES", container: "ASiC_E" }
);
```

The library documentation is on [GitHub Pages](https://slovensko-digital.github.io/autogram-extension/autogram-sdk/).

## How it works

Autogram runs a local server on the user's computer at `http://localhost:37200`. Your website sends it a document over HTTP, Autogram shows it, the user checks and signs it, and Autogram returns the signed document in the response.

- The API documentation (Swagger) is on [GitHub](https://generator3.swagger.io/index.html?url=https://raw.githubusercontent.com/slovensko-digital/autogram/main/src/main/resources/digital/slovensko/autogram/server/server.yml) and, while Autogram is running, at <http://localhost:37200/docs>.
- If Autogram is not running, start it by opening an address with the `autogram://` protocol, for example `autogram://go`. Then wait until `GET /api/v1/info` responds and only then send the document. Meanwhile the user may see only Autogram's main window, so tell them on your page to wait and not to open the document in Autogram themselves.
- Signing one document: `POST /api/v1/sign` with a `document` field.
- Several documents in one ASiC-E container: `POST /api/v1/sign` with a `documents` array, see [Several documents in one container](#several-documents-in-one-container).
- A Financial Administration form: the form identifier in `xdcParameters.fsFormIdentifier`, see [Financial Administration forms](#financial-administration-forms).
- Several separate documents: the batch API `/api/v1/batch`; the user chooses whether to sign them one by one or in bulk.
- Request a timestamp with the `BASELINE_T` level (in `/api/v1/sign` with the `profile` parameter). Autogram gets it from the [timestamp sources](timestamps.md) in the user's settings, by default from free qualified providers.
- Only a certain type of certificate, for example a qualified electronic signature or a mandate certificate: the `signingCertificateTypes` parameter, see [Signing certificate type](#signing-certificate-type).
- List of certificates: `GET /api/v1/certificates`. Autogram asks for the [user's consent](websites.md#consent-to-reading-certificates) before sending the certificates.

## Several documents in one container {#several-documents-in-one-container}

Create a new container from several documents through the HTTP API `POST /api/v1/sign` with a `documents` array (format `XAdES` or `CAdES`, container `ASiC_E`). An ASiC-E document cannot be part of such a request. See the [API documentation](https://generator3.swagger.io/index.html?url=https://raw.githubusercontent.com/slovensko-digital/autogram/main/src/main/resources/digital/slovensko/autogram/server/server.yml) for details and examples.

```json
{
  "parameters": { "form": "XAdES", "container": "ASiC_E" },
  "documents": [
    { "filename": "contract.pdf", "mimeType": "application/pdf;base64", "content": "JVBERi0xLjUK..." },
    { "filename": "annex.txt", "mimeType": "text/plain", "content": "Annex to the contract" }
  ]
}
```

## Signing certificate type {#signing-certificate-type}

With the `signingCertificateTypes` parameter in the `parameters` of `POST /api/v1/sign` you set which certificates the document may be signed with. The types are named as in the central electronic registry (CEP) of slovensko.sk:

| Type | Certificate |
| --- | --- |
| `QESig` | qualified electronic signature, except a mandate certificate |
| `QESig-MQC` | mandate certificate (mandátny certifikát) |
| `QESeal` | qualified electronic seal |
| `AdESig-QC` | recognised method of authorisation (uznaný spôsob autorizácie), for example a Slovak ID card issued before 21 June 2021 |
| `AdESeal-QC` | seal with a qualified certificate that is not on a qualified device |
| `AdESig`, `AdESeal` | non-qualified signature or seal |

```json
{
  "parameters": { "form": "XAdES", "container": "ASiC_E", "signingCertificateTypes": ["QESig", "QESig-MQC"] },
  "document": { "filename": "submission.txt", "mimeType": "text/plain", "content": "Submission" }
}
```

- Certificates of other types are disabled when [choosing the certificate](card-and-pin.md#choosing-the-certificate), with the label **Document requires another type**. The user's setting **Allow signing with non-qualified certificates** does not apply then.
- Autogram determines the type from the certificate and the trusted lists. A certificate whose type it cannot determine, for example when the trusted lists did not load, cannot be used.
- Autogram recognises a mandate certificate by the certificate policy `1.3.158.36061701.1.1.<mandate number>`, by a subject field starting with `MANDANT ` or by `OPRÁVNENIE <number>` in the certificate's name.
- If a document in batch signing requires another type than the batch's certificate has, Autogram does not sign it and returns error 422 (`SIGNING_CERTIFICATE_NOT_ALLOWED`).
- Autogram knows the parameter from version 2.9.0, older versions ignore it. Get the version from `GET /api/v1/info`.
- The registry validates the signature anyway. The parameter only prevents the user from signing with a certificate the registry would not accept.

## Financial Administration forms {#financial-administration-forms}

XML forms of the Slovak Financial Administration (Finančná správa), such as tax returns, mostly have no namespace in their root element from which Autogram could tell which form it is. So send the form identifier in the `fsFormIdentifier` parameter in the document's `xdcParameters`. Autogram then finds the form's XSD and XSLT in the public Slovensko.Digital form list, validates the XML against the XSD, creates an XML Datacontainer from it, shows it to the user in a readable form and signs it with a XAdES signature.

```json
{
  "parameters": { "form": "XAdES" },
  "documents": [
    {
      "filename": "tax-return.xml",
      "mimeType": "application/xml;base64",
      "content": "PGRva3VtZW50Pj...",
      "xdcParameters": { "autoLoadEform": true, "fsFormIdentifier": "792_772" }
    }
  ]
}
```

- The identifier has the form `number_number`, for example `792_772` is the personal income tax return type B for 2023. You can find the identifiers with the form names in [forms.xml](https://forms-slovensko-digital.s3.eu-central-1.amazonaws.com/fs/forms.xml), in the `sdIdentifier` attribute.
- If you send the form already in an XML Datacontainer or an ASiC-E container, no identifier is needed. Autogram recognises the form by its identifier in the container.
- An identifier in a wrong format, or XML that does not match the form's XSD, returns error 422 (`UNPROCESSABLE_INPUT`). An unknown identifier returns error 400 (`UNKNOWN_EFORM`).
- Autogram needs an internet connection to find the form.
- The legacy `POST /sign` API uses the `parameters.fsFormId` parameter for this.

## What the user needs

- Autogram installed, with **Enable Autogram API** turned on (the default).
- A card with a certificate and its software, see [Cards and certificates](cards.md).

## Restricted mode

If port 37200 is taken, for example by another application, Autogram starts in restricted mode without the API. Signing files in the application works, signing from websites does not. See [Troubleshooting](troubleshooting.md#restricted-mode).
