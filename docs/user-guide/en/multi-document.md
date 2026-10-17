# Several documents in one container (ASiC-E)

An ASiC-E container (`.asice` extension) can hold several documents that are signed together. In Slovakia this is also called *shared authorization of documents* (spoločná autorizácia). Autogram displays such a container, validates its signatures and adds your signature to it.

## Opening a container

1. Start Autogram.
2. Click **Select file** or drag the `.asice` file into the application window.

A website or another application (for example Autogram Portal) can also send a container to Autogram; the signing window then opens by itself.

## Documents in tabs

When the container holds more than one document, one tab per document appears above the preview. Autogram always opens the first document. To see the others, click their tab or use the keyboard (Tab and the arrow keys).

![Container with two documents in tabs](../screenshots/en/multi-document-tabs.png)

- A long file name is shortened in the tab; hover over the tab to see the full name.
- A document without a name is shown as "Document 1", "Document 2" and so on.
- Each document is shown in its own format: PDF, image, text or XML form. Slovak government forms (XML Data Container) are shown in a human-readable form.

![Tab with an XML form](../screenshots/en/multi-document-xml-form.png)

Look through all tabs before signing. Your signature confirms the content of every document in the container, not only the one you are looking at.

## Signatures in the container

When the container already has signatures, Autogram validates them and shows the signatures of the document in the selected tab below the preview.

If a signature does not cover all documents in the container, the warning **Signatures are not the same across all documents.** appears. It means that a signer signed only some of the documents. Check the signatures in each tab and consider whether you want to continue.

![Warning about signatures that do not cover all documents](../screenshots/en/multi-document-incomplete-coverage.png)

Open the signature details with **Show signature details**.

## Signing

Autogram always signs all documents in the container with one signature. Signing only some of the documents is not possible.

- When you opened an existing container, the **Sign** button adds your signature to the container.
- When a website sent several separate documents for signing, the button reads **Sign all** (after choosing a certificate, **Sign all as …**). Autogram creates one new ASiC-E container from them.

![Sign all button](../screenshots/en/multi-document-sign-all.png)

After signing, Autogram saves the signed container and shows where to find it.

## For integrators

Create a new container from several documents through the HTTP API `POST /api/v1/sign` with a `documents` array (at most 5 documents, format `XAdES` or `CAdES`, container `ASiC_E`). An ASiC-E document cannot be part of such a request. See the [API documentation](https://generator3.swagger.io/index.html?url=https://raw.githubusercontent.com/slovensko-digital/autogram/main/src/main/resources/digital/slovensko/autogram/server/server.yml) for details and examples.

```json
{
  "parameters": { "form": "XAdES", "container": "ASiC_E" },
  "documents": [
    { "filename": "contract.pdf", "mimeType": "application/pdf;base64", "content": "JVBERi0xLjUK..." },
    { "filename": "annex.txt", "mimeType": "text/plain", "content": "Annex to the contract" }
  ]
}
```
