# Headless CLI signing

Autogram CLI can be used by scripts and desktop integrations without an interactive certificate or PIN prompt. The same options also support batch signing from a directory.

## Select a certificate

List certificates exposed by the selected driver:

```sh
printf '%s\n' "$PIN" | autogram --cli \
  --driver secure_store \
  --list-keys \
  --pin-stdin
```

The machine-readable records have the form `AUTOGRAM_KEY<TAB>serial<TAB>common-name`. Use either the certificate serial number or common name with `--key`:

```sh
printf '%s\n' "$PIN" | autogram --cli \
  --driver secure_store \
  --key "123456789" \
  --pin-stdin \
  --source "$HOME/Documents/input.pdf" \
  --target "$HOME/Documents/input_signed.pdf"
```

The PIN is read from standard input only when `--pin-stdin` is present. Do not place a real PIN in shell history, source code, or a committed configuration file. Prefer an operating-system secret store or a protected input pipe.

## PAdES Baseline T

For a timestamped PDF signature, use `PAdES_BASELINE_T` and provide one or more TSA endpoints:

```sh
autogram --cli \
  --driver secure_store \
  --pdf-level PAdES_BASELINE_T \
  --tsa-server "http://timestamp.sectigo.com/qualified" \
  --source "$HOME/Documents/input.pdf" \
  --target "$HOME/Documents/input_signed.pdf"
```

The signature level must be routed to the PAdES builder for PDF input. Whether a TSA service is legally qualified depends on the service and its status in the applicable trusted list. Verify the service before relying on the signature for a regulated workflow.

## Exit status

The CLI exits with status `0` after successful processing and a non-zero status when argument parsing, driver discovery, certificate selection, PIN access, or signing fails. This allows Finder actions and other wrappers to report failures without relying on terminal text.

## Security notes

- Keep PIN input outside command-line arguments because process arguments can be observable.
- Do not commit token details, private keys, personal certificate names, or client documents.
- Treat the signed output as a new file. The source document is not overwritten unless `--force` is explicitly used.
