# Autogram user guide

Autogram is a free application for Windows, macOS and Linux for signing and verifying electronic documents. Its signatures follow the European eIDAS regulation and Slovak standards, so they are accepted by public authorities and private companies alike. Autogram is developed by Slovensko.Digital and its source code is public.

## What you can do with Autogram

- **Sign a document on a website.** Websites and applications that support Autogram send it the document and take the signed document back by themselves. You download or upload nothing. This is how most people use Autogram. See [Signing on websites](websites.md).
- **Sign a file from your computer.** A PDF, a Slovak government form, an image, a text file or any other file. See [Signing a file from your computer](signing.md).
- **Sign several documents with one PIN.** Select several files or a whole folder and sign them one by one or in bulk. You enter the PIN only once, however many documents there are. See [Several documents at once](batch.md).
- **Add a qualified timestamp to your signature.** In Slovakia, such a signature does not have to be certified, for example by a notary. Autogram gets the timestamps for free from publicly available providers in other EU countries. See [Signature with a timestamp](timestamps.md).
- **Verify signatures.** Open a signed document to see who signed it, when, and whether the signature is valid and qualified. See [Verifying signatures](verification.md).

Advanced users can also sign from the [command line](cli.md), and developers can connect Autogram to their web application through the [HTTP API](api.md).

## What you need

1. **Autogram** installed on your computer. See [Installation](installation.md).
2. **A signing certificate** on a smart card or another device, for example:
   - a Slovak ID card with a chip that has signing certificates issued, and the eID client installed,
   - a qualified signature card Gemalto IDPrime 940 (also as a USB key), MONET+ ProID+Q or I\.CA SecureStore, and the card's software installed,
   - a Czech eObčanka and the eObčanka software installed.
3. **A card reader**, unless your computer has one built in. A USB key needs no reader.
4. **The PIN** for the certificate.

The individual cards are described in [Cards and certificates](cards.md).

To verify signatures you need no certificate or card, only an internet connection.

::: callout info "Signing on government websites"
On Slovak government websites, such as slovensko\.sk, the Financial Administration portal or the Business Register, you also need the [browser extension](https://sluzby.slovensko.digital/autogram-na-statnych-weboch#download). Websites that support Autogram directly do not need it. See [Two kinds of websites](websites.md#two-kinds-of-websites).
:::

## Using Autogram for the first time?

Follow [Your first signature step by step](first-signature.md). It has a picture for every step and also explains the windows that appear only with some cards. Below is a short procedure for those who already know Autogram.

## How to sign a document on a website

::: steps
1. On the website, click the button to sign. If the website asks how you want to sign the document, choose **Podpísať v tomto počítači** (sign on this computer).
2. If Autogram is not running yet, the website starts it and Autogram's main window appears. Do not open anything in it and wait a moment. The website sends you the document by itself.
3. Look through the document and click **Sign**.
4. Choose the card and the certificate and enter the PIN.
5. The signed document goes back to the website.
:::

See [Signing on websites](websites.md) for details.

## How to sign a file from your computer

::: steps
1. Start Autogram and click **Select file**, or drag the file into the application window.
2. Look through the document in the preview. If it already has signatures, Autogram validates them and shows them below the preview.
3. Click **Sign**.
4. Choose the card and the certificate and enter the PIN. See [Choosing a card and entering the PIN](card-and-pin.md).
5. Autogram saves the signed file in the folder of the original document and shows where to find it.
:::

A PDF stays a PDF after signing. Other files are saved together with the signature in a container with the `.asice` extension. See [Signing a file from your computer](signing.md#where-the-signed-file-is-saved).

Autogram remembers the chosen certificate. You sign the next document with the **Sign as …** button that shows your name.

::: callout warning "Sign only what you have seen"
A qualified electronic signature has the same legal effect as a handwritten one. That is why Autogram always shows the document's content before you sign. If it cannot display the document, it warns you. Sign such a document only if you trust its source.
:::

## Help and contact

- The most common problems and their solutions: [Troubleshooting](troubleshooting.md).
- Questions and problems with Autogram: [podpora@slovensko.digital](mailto:podpora@slovensko.digital).
- You can also report bugs and suggest improvements on [GitHub](https://github.com/slovensko-digital/autogram/issues).
- Autogram is open source under the EUPL 1.2 licence and you can use it commercially and non-commercially. The source code is on [GitHub](https://github.com/slovensko-digital/autogram).
