---
description: "What each version of Autogram brings: new features, improvements, fixes and security updates."
---

# What's new

Changes in each version of Autogram, newest first. To find out which version you have, click **About Autogram** in the main window. How to update Autogram is described in [Updates](installation.md#updates).

::: changelog

== Coming next

## Coming next {#coming-next}

<p class="release-meta">Changes in the next version</p>

**Improved**

- **Signing certificates only.** When you choose a certificate, Autogram offers only certificates meant for signing. Certificates of web servers, timestamps and certificate status services (OCSP) are no longer in the list. See [Choosing the certificate](card-and-pin.md#choosing-the-certificate).

**Fixed**

- Autogram no longer freezes when you open the signature details while signatures are being validated. It used to wait until the validation finished, which took up to several minutes when the trusted lists downloaded slowly.

== 2.8.3

## 2.8.3 {#v2-8-3}

<p class="release-meta">26 October 2026 <strong class="idsk-tag idsk-tag--red">Security update</strong></p>

**New**

- **Autogram in the Microsoft Store.** On Windows, install Autogram from the Microsoft Store or with `winget`, and new versions are then installed automatically. If you have an older version from the `.msi` file, the new one helps you uninstall it. See [Installation](installation.md) and [Moving to the Microsoft Store version](installation.md#moving-to-microsoft-store).
- **Containers with several documents.** Autogram opens an ASiC-E container with several documents, shows them in tabs, validates their signatures and adds your signature to them. See [Several documents in one container](multi-document.md).
- **Signing one by one or all at once.** When you select several files, Autogram asks whether you want to look at each document before signing it or sign them all at once. See [Several documents at once](batch.md).
- **Choosing from several cards.** If several cards are inserted, for example a Slovak ID card and a card from your employer, Autogram finds all of them and asks which one to use. See [Choosing the card](card-and-pin.md#choosing-the-card).
- **Proxy server.** In the settings and on the command line, you can enter an HTTP proxy that Autogram uses to reach the internet. See [Settings](settings.md#other).
- **Opening files from the file manager.** Open PDF files and ASiC-E containers in Autogram straight from the file manager, for example with **Open with**. If Autogram is already running, it opens them in the running window.

**Improved**

- Autogram remembers the folder you last opened files from.
- You can copy the support email address in error messages.
- The country selection in the settings and the signature details buttons can be used with the keyboard.
- Autogram's windows have their own icon on Windows.

**Fixed**

- Forms and XML documents in the windows-1250 encoding are shown correctly.
- Documents with `?` or `#` in their name are signed without an error.
- A wrong keystore password shows the right message.
- An empty path to a keystore or to a card driver in the settings no longer causes an error.

**For developers**

- `POST /api/v1/sign` signs several documents into one ASiC-E container. See [Several documents in one container](api.md#several-documents-in-one-container).
- `autoLoadEform` is on by default for XDC forms and ASiC-E containers.
- When the user cancels signing in a warning before signing, Autogram answers the request instead of leaving it open.
- Fixed canonicalization settings in requests to `/api/v1/sign`.

== 2.7.5

## 2.7.5 {#v2-7-5}

<p class="release-meta">31 July 2026</p>

**New**

- **Native version for Macs with Apple silicon.** On Macs with an M1 chip or newer, Autogram runs without translation through Rosetta.

**Improved**

- The default timestamp sources are free qualified providers from Belgium and Spain. See [Signature with a timestamp](timestamps.md).
- When you change the trusted list countries in the settings, Autogram loads the lists right away, without a restart.
- The keystore password window explains that some keystores do not require a password.
- More reliable display of Slovak public administration forms.

**Fixed**

- A signed PDF file gets the `.pdf` extension even when ASiC-E is the default format.
- Forms with empty values in their manifest open without an error.

== 2.7.4

## 2.7.4 {#v2-7-4}

<p class="release-meta">21 April 2026 <strong class="idsk-tag idsk-tag--red">Security update</strong></p>

- Security fix for macOS.

== 2.7.3

## 2.7.3 {#v2-7-3}

<p class="release-meta">21 March 2026 <strong class="idsk-tag idsk-tag--red">Security update</strong></p>

- Safer processing of XML documents.

== 2.7.2

## 2.7.2 {#v2-7-2}

<p class="release-meta">3 March 2026 <strong class="idsk-tag idsk-tag--red">Security update</strong></p>

- Safer processing of XML documents.

== 2.7.1

## 2.7.1 {#v2-7-1}

<p class="release-meta">29 November 2025 <strong class="idsk-tag idsk-tag--red">Security update</strong></p>

- Stricter checks of Slovak public administration forms (XDC) before they are shown.

:::

Older versions and the technical list of changes are in the [releases on GitHub](https://github.com/slovensko-digital/autogram/releases).
