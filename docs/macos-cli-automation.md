# macOS CLI and Finder Quick Action

Autogram can be run in CLI mode on macOS and used from Finder through an Automator Quick Action. The Quick Action is a Finder integration, not a macOS Shortcuts workflow. It uses macOS dialogs for certificate store selection, certificate selection, and PIN entry, then runs the signing operation through the Autogram CLI without opening the Autogram GUI or a Terminal window.

## Requirements

- macOS with Finder and Automator.
- An Autogram application with the headless CLI options described in [Headless CLI signing](cli-headless-signing.md).
- A compatible signing token or card, its PKCS#11 driver, and the PIN.
- Network access to the configured qualified timestamp service when PAdES Baseline T is required.
- Rosetta and an Intel Autogram build on Apple Silicon when the installed PKCS#11 driver is Intel-only.

The scripts do not install Autogram, a token driver, Rosetta, or a timestamp service.

## Install the scripts

From the repository root:

```bash
chmod +x scripts/macos-automation/*.sh
```

The scripts look for the application in these locations:

- `/Applications/Autogram.app/Contents/MacOS/AutogramApp`
- `$HOME/Applications/Autogram.app/Contents/MacOS/AutogramApp`
- `target/app-image/Autogram.app/Contents/MacOS/AutogramApp` for a local build

Set `AUTOGRAM_BIN` when the application is installed elsewhere. For an Intel-only PKCS#11 driver on Apple Silicon, set `AUTOGRAM_INTEL_BIN` to an Intel Autogram executable or install an Intel Autogram application in one of the documented locations.

## Create the Finder Quick Action

1. Open **Automator** and create a new **Quick Action**. Do not create a macOS Shortcuts workflow for this integration.
2. Set **Workflow receives current** to `files or folders` and **In** to `Finder`.
3. Add the **Run Shell Script** action.
4. Set **Shell** to `/bin/bash` and **Pass input** to `as arguments`.
5. Use this script body, replacing the repository path with the local clone path:

```bash
REPO_DIR="/path/to/autogram"
"$REPO_DIR/scripts/macos-automation/autogram-quick-action.sh" "$@"
```

6. Save the Quick Action as `Sign PDFs Autogram`.

Select one or more PDF files in Finder, right-click, open **Quick Actions**, and choose `Sign PDFs Autogram`. Only PDF files are processed. The original files are not overwritten. Signed files are saved beside the originals as `<name>_signed.pdf`, with numbered variants when that name already exists.

The Quick Action asks for the certificate store, PIN, and certificate when more than one certificate is available. It signs each selected PDF with PAdES Baseline T and requests a timestamp from the configured qualified TSA.

## Configuration

The following environment variables are supported:

- `AUTOGRAM_BIN`: path to the native Autogram executable.
- `AUTOGRAM_INTEL_BIN`: path to an Intel Autogram executable for Rosetta execution.
- `AUTOGRAM_SECURE_STORE_PKCS11_PATH`: override the default I.CA SecureStore PKCS#11 library path.
- `AUTOGRAM_TSA_SERVER`: override the default qualified TSA URL.

Automator has a restricted environment. If an override is needed, define it in the Quick Action shell script before invoking `autogram-quick-action.sh`, for example:

```bash
export AUTOGRAM_BIN="/path/to/Autogram.app/Contents/MacOS/AutogramApp"
export AUTOGRAM_TSA_SERVER="https://timestamp.example.invalid/qualified"
REPO_DIR="/path/to/autogram"
"$REPO_DIR/scripts/macos-automation/autogram-quick-action.sh" "$@"
```

Replace the example TSA URL with a real qualified service and verify its status in the applicable EU Trusted List before relying on it.

## Manual CLI use

```bash
scripts/macos-automation/autogram-cli-sign.sh \
  --driver secure_store \
  --pdf-level PAdES_BASELINE_T \
  --tsa-server "http://timestamp.sectigo.com/qualified" \
  "/path/to/file.pdf"
```

For non-interactive certificate and PIN handling, use the headless options:

```bash
printf '%s\n' "$PIN_VALUE" | \
  scripts/macos-automation/autogram-cli-sign.sh \
    --driver secure_store \
    --list-keys \
    --pin-stdin
```

Do not put a real PIN, private key, client document, or certificate identity in shell history, source files, or the repository.

## Finder selection helper

`autogram-finder-selection.sh` reads the current Finder selection through AppleScript and forwards it to the same Quick Action implementation. It can be used from a launcher such as Alfred or Raycast:

```bash
REPO_DIR="/path/to/autogram"
"$REPO_DIR/scripts/macos-automation/autogram-finder-selection.sh"
```

## Troubleshooting

- If no Autogram executable is found, set `AUTOGRAM_BIN` explicitly.
- If an Apple Silicon Mac reports an incompatible `x86_64` PKCS#11 library, install Rosetta and use an Intel Autogram build through `AUTOGRAM_INTEL_BIN`.
- If certificates cannot be loaded, check the token, PKCS#11 driver, PIN, and the selected driver name.
- If a timestamped PDF is expected, make sure `PAdES_BASELINE_T` is routed to the PDF signing path and that the TSA is reachable.
- The Quick Action validates the output by checking the PDF header and end marker. This avoids treating an ASiC ZIP with a `.pdf` extension as a successfully signed PDF.
- Use absolute paths for system tools in Automator scripts. Developer tools such as `rg` may not be available in Automator's restricted `PATH`.
