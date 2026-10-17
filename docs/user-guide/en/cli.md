# Command line (CLI)

Autogram can sign without its graphical interface, which is useful for signing many files at once. Use `autogram-cli` on Windows and `autogram` on macOS and Linux.

```sh
autogram --cli document.pdf
autogram --cli -s document.pdf -t signed/document.pdf --parents
autogram --cli -s folder -t folder_signed -f
```

- `-s`, `--source`: file or folder to sign (can also be given as the only argument).
- `-t`, `--target`: where to save the signed file or folder. Without it, the file is saved next to the original with the `_signed` suffix.
- `-f`, `--force`: overwrite existing files.
- `--parents`: create missing target folders.
- `-d`, `--driver`: certificate store (`eid`, `cz_eid`, `secure_store`, `monet`, `gemalto`, `keystore`, `custom_pkcs11`).

`autogram --help` lists all options and `autogram --usage` prints examples.

## Containers with several documents

Each input file is signed separately. An existing ASiC-E container with several documents is signed as a whole, the same as in the graphical interface: the signature covers all documents in the container.

The CLI does not combine a folder of files into one container; each file gets its own signature. To create a new container from several documents, use the HTTP API (see [Several documents in one container](multi-document.md#for-integrators)).
