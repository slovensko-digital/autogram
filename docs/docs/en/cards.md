---
description: "Which cards Autogram supports (Slovak ID card, Gemalto IDPrime 940, MONET+ ProID+Q, I.CA SecureStore, Czech eObčanka) and which software they need."
---

# Cards and certificates

To sign, you need a signing certificate and its private key. They are usually stored on a smart card that you insert into a reader, or on a USB key. For Autogram to work with the card, the card's software (driver) must be installed on your computer.

This page describes what you need for the individual cards. How the card is chosen when signing and where you enter the PIN is described in [Choosing a card and entering the PIN](card-and-pin.md).

| Card | Software | Download |
| --- | --- | --- |
| [Slovak ID card](#slovak-id-card-eid) | eID client | [slovensko.sk](https://www.slovensko.sk/sk/na-stiahnutie) |
| [Gemalto IDPrime 940](#gemalto-idprime-940) | SafeNet Authentication Client | [snca.gov.sk](https://snca.gov.sk/podpora-gemalto-idprime-940) |
| [MONET+ ProID+Q](#monet-proid-q) | ProID+ | [snca.gov.sk](https://snca.gov.sk/cipove-karty/monet-proidq/podpora-monet-proidq) |
| [I\.CA SecureStore](#i-ca-securestore) | I\.CA SecureStore | [ica.cz](https://www.ica.cz/secure-store) |
| [Czech eObčanka](#czech-id-card-eobcanka) | eObčanka | [info.identita.gov.cz](https://info.identita.gov.cz/download/Default.aspx#SoftwareKeStazeni) |

You can also have several cards connected at once, each in its own reader. Autogram finds all of them and asks which one to use when you sign.

## Slovak ID card (eID) {#slovak-id-card-eid}

A Slovak ID card with a chip can hold a certificate for a qualified electronic signature.

1. Install the **eID client** from [slovensko.sk](https://www.slovensko.sk/sk/na-stiahnutie).
2. If you have not had signing certificates before, issue them in the eID client. With a new ID card, this is often the first thing to do.
3. Insert the card into the reader and click **Sign** in Autogram.
4. In the eID client's window, enter the BOK (security personal code) first and, after choosing the certificate, the signing PIN. See [Choosing a card and entering the PIN](card-and-pin.md#slovak-id-card).

Besides the qualified certificate, the card also holds a second one that is not used for qualified signing. Autogram does not offer it by default, see [Several certificates on one card](card-and-pin.md#several-certificates-on-one-card).

## Other qualified signature cards

Besides the ID card, these cards from qualified certificate providers are the most common in Slovakia. Each needs the software from its issuer. You enter the codes in Autogram's window: first the [keystore password](card-and-pin.md#keystore-password) if the card has one, and the [signing PIN](card-and-pin.md#signing-pin) when signing.

### Gemalto IDPrime 940 {#gemalto-idprime-940}

- Comes as a smart card for a reader or as a **USB key** that you plug directly into the computer, without a reader.
- Its software is **SafeNet Authentication Client**. Download it from the [Gemalto IDPrime 940 support page](https://snca.gov.sk/podpora-gemalto-idprime-940) (in Slovak) of the Slovak National Certification Authority, which also has an installation guide.
- The qualified certificate is often in a different slot on the card than the others, see [Several certificates on one card](card-and-pin.md#several-certificates-on-one-card).

### MONET+ ProID+Q {#monet-proid-q}

- A smart card for a reader.
- Its software is **ProID+** from MONET+. Download it from the [MONET+ ProID+Q support page](https://snca.gov.sk/cipove-karty/monet-proidq/podpora-monet-proidq) (in Slovak) of the Slovak National Certification Authority, which also has an installation guide.

### I\.CA SecureStore {#i-ca-securestore}

- A smart card for a reader.
- Its software is **I\.CA SecureStore**. Download it from the [I\.CA SecureStore page](https://www.ica.cz/secure-store) (in Czech) of I\.CA.

## Czech ID card (eObčanka) {#czech-id-card-eobcanka}

The Czech ID card with a chip works with the **eObčanka** software installed, which you can download from [info.identita.gov.cz](https://info.identita.gov.cz/download/Default.aspx#SoftwareKeStazeni) (in Czech). Autogram does not search it for cards, so it always offers it when its software is installed and never uses it without asking.
