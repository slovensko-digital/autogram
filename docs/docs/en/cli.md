---
description: "How to sign with Autogram from the command line and in scripts, the list of options and where the signed files are saved."
---

# Command line (CLI)

Autogram can sign without its graphical interface, which is useful for signing many files at once or in scripts. Use `autogram-cli` on Windows and `autogram` on macOS and Linux. In the Microsoft Store version, `autogram-cli` works in any terminal. When installed from an `.msi` file, you find it in the `C:\Program Files\Autogram` folder.

```sh
autogram --cli document.pdf
autogram --cli -s document.pdf -t signed/document.pdf --parents
autogram --cli -s folder -t folder_signed -f
autogram --cli -s document.pdf -d eid --tsa-server http://timestamp.sectigo.com/qualified
```

`autogram --help` lists all options and `autogram --usage` prints examples.

## Options

| Option | Meaning |
| --- | --- |
| `-c`, `--cli` | Runs Autogram on the command line. Without it, the graphical interface starts. |
| `-s`, `--source` | File or folder to sign. Can also be given as the only argument, without the option. |
| `-t`, `--target` | Where to save the signed file or folder. Must be of the same kind as the source (file or folder). |
| `-f`, `--force` | Overwrites existing files. |
| `--parents` | Creates missing target folders. |
| `-d`, `--driver` | Card or store: `eid`, `cz_eid`, `secure_store`, `monet`, `gemalto`, `keystore`, `custom_pkcs11`. Without it, Autogram searches all cards and asks which one to use if it finds more. |
| `--slot-id` | Position of the slot in the list of slots the chosen driver reports. Without it, Autogram offers all slots it finds. |
| `--keystore` | Path to a certificate file (JKS) for `-d keystore`. |
| `--pkcs11-driver-path` | Path to the card driver for `-d custom_pkcs11`. |
| `--pdf-level` | Signature type for PDFs: `PAdES_BASELINE_B` (default), `XAdES_BASELINE_B` or `CAdES_BASELINE_B`. |
| `--tsa-server` | Address of a timestamp server. When given, signatures get a timestamp. Separate several addresses with commas. |
| `--proxy` | HTTP proxy in the form `http://host:port` used for all internet communication (trusted lists, CRL, OCSP, timestamps, form resources). Proxy authentication is not supported. The proxy set in the graphical interface is not used in the command line. |
| `--pdfa` | Checks PDF/A compliance before signing. |
| `--plain-xml` | Allows signing XML files that are not government forms. |
| `--en319132` | Signs according to ETSI EN 319 132 and EN 319 122. |

You enter the PIN for the chosen card on the command line; for a Slovak ID card, in the eID client's window.

## Where the signed files are saved

- Without `-t`, the signed file is saved next to the original with the `_signed` suffix, for example `document_signed.pdf`. If that file exists, a number is added: `document_signed (1).pdf`.
- For a folder without `-t`, a folder with the `_signed` suffix is created next to it.
- With `-t`, an existing target is overwritten only with `-f`; otherwise signing fails.

## Containers with several documents

Each input file is signed separately. An existing ASiC-E container with several documents is signed as a whole, the same as in the graphical interface: the signature covers all documents in the container.
