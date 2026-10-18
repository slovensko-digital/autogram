# Formats and signature types

This page explains which files Autogram can sign and display, and which signature it creates. You do not need it for everyday signing: Autogram picks a suitable format by itself.

## What Autogram displays {#what-autogram-displays}

You can sign any file. Before signing, Autogram can display:

- PDF,
- PNG and JPEG images,
- text files,
- HTML,
- Slovak government electronic forms (XML), shown in a human-readable form.

Other files, such as Word documents, are signed without a preview and Autogram [warns you](warnings.md#the-document-cannot-be-displayed) about it.

A password-protected PDF cannot be signed; remove the password first.

## Signature types

Autogram creates signatures of three types:

| Type | Where the signature is | When it is used |
| --- | --- | --- |
| **PAdES** | inside the PDF; the result is a PDF again | by default for PDFs |
| **XAdES** | in an ASiC-E container together with the document | for all other files, and for PDFs depending on the settings |
| **CAdES** | in an ASiC-E container together with the document | for PDFs depending on the settings |

PAdES is generally accepted and the easiest: you can open a signed PDF in any PDF viewer. Some authorities, however, require XAdES or CAdES. Change the signature type for PDFs in [Settings](settings.md#signing) with **PDF signature type**.

If a document already has a signature, Autogram adds a signature of the same type and ignores the setting.

## ASiC-E container

An ASiC-E container (`.asice` extension) is a file that holds the signed document and its signatures. It can also hold several documents signed together, see [Several documents in one container](multi-document.md).

You open and verify a container in Autogram. You can get the document out of it even without Autogram: the container is a ZIP archive, so after renaming it to `.zip` you can extract it.

## Government electronic forms

Slovak government forms (for example from slovensko.sk) are XML files, often wrapped in an XML Data Container. Autogram displays them in a human-readable form according to the form's template and signs them with a XAdES signature in an ASiC-E container, as authorities require.

Financial Administration forms, such as tax returns, are most often signed directly on the Financial Administration portal with the [browser extension](websites.md#government-websites-with-the-browser-extension) or in an application that sends them to Autogram. How developers do this is described in [Integration through the HTTP API](api.md#financial-administration-forms).

By default, Autogram does not sign an XML file that is not such a form. If you need to sign it, turn on **Signing unknown XML** in [Settings](settings.md#signing).

## Timestamps

A timestamp from a trusted provider confirms that a signature existed at a certain time. With it, the signature stays verifiable even after the certificate expires. In Slovakia, a qualified electronic signature with a qualified timestamp does not have to be certified.

A signature with a timestamp has the BASELINE_T level (for example PAdES-BASELINE-T), a signature without one BASELINE_B. Autogram does not add timestamps by default. How to turn them on and where Autogram gets them is described in [Signature with a timestamp](timestamps.md).

## PDF/A

PDF/A is a version of PDF for long-term archiving that some authorities require. Autogram does not convert PDFs to PDF/A; it only [warns you](warnings.md#pdfa) if the signed document does not comply. You can create PDF/A, for example, when exporting to PDF from a word processor.
