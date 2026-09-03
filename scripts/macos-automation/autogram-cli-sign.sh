#!/usr/bin/env bash
set -euo pipefail

# Optional overrides:
#   AUTOGRAM_BIN=/path/to/AutogramApp ./autogram-cli-sign.sh file1.pdf
#   AUTOGRAM_INTEL_BIN=/path/to/Intel/AutogramApp ./autogram-cli-sign.sh --driver secure_store file1.pdf
resolve_autogram_bin() {
  local candidates=(
    "${AUTOGRAM_BIN:-}"
    "/Applications/Autogram.app/Contents/MacOS/AutogramApp"
    "$HOME/Applications/Autogram.app/Contents/MacOS/AutogramApp"
    "$(cd "$(dirname "$0")/../.." && pwd)/target/app-image/Autogram.app/Contents/MacOS/AutogramApp"
  )

  local candidate
  for candidate in "${candidates[@]}"; do
    if [[ -n "$candidate" && -x "$candidate" ]]; then
      printf '%s\n' "$candidate"
      return 0
    fi
  done

  return 1
}

resolve_intel_autogram_bin() {
  local candidates=(
    "${AUTOGRAM_INTEL_BIN:-}"
    "${AUTOGRAM_BIN:-}"
    "$HOME/Applications/Autogram Intel GUI.app/Contents/MacOS/AutogramApp"
    "$HOME/Applications/Autogram Intel.app/Contents/MacOS/AutogramApp"
    "/Applications/Autogram Intel.app/Contents/MacOS/AutogramApp"
    "$HOME/Applications/Autogram-intel.app/Contents/MacOS/AutogramApp"
    "/Applications/Autogram-intel.app/Contents/MacOS/AutogramApp"
  )

  local candidate
  for candidate in "${candidates[@]}"; do
    if [[ -n "$candidate" && -x "$candidate" ]] && /usr/bin/lipo -verify_arch x86_64 "$candidate" >/dev/null 2>&1; then
      printf '%s\n' "$candidate"
      return 0
    fi
  done

  return 1
}

secure_store_driver_path() {
  printf '%s\n' "${AUTOGRAM_SECURE_STORE_PKCS11_PATH:-/usr/local/lib/pkcs11/libICASecureStorePkcs11.dylib}"
}

secure_store_requires_intel_autogram() {
  [[ "$(uname -m)" == "arm64" ]] || return 1

  local driver_path
  driver_path="$(secure_store_driver_path)"
  [[ -f "$driver_path" ]] || return 1

  ! /usr/bin/lipo -verify_arch arm64 "$driver_path" >/dev/null 2>&1
}

should_use_intel_autogram() {
  local requested_driver="${1:-}"

  [[ "$(uname -m)" == "arm64" ]] || return 1

  if [[ "$requested_driver" == "secure_store" ]]; then
    secure_store_requires_intel_autogram
    return $?
  fi

  [[ -z "$requested_driver" ]] && secure_store_requires_intel_autogram
}

run_autogram() {
  local app_bin="$1"
  shift

  if [[ "$(uname -m)" == "arm64" ]] && /usr/bin/lipo -verify_arch x86_64 "$app_bin" >/dev/null 2>&1 \
    && ! /usr/bin/lipo -verify_arch arm64 "$app_bin" >/dev/null 2>&1; then
    echo "Using Intel Autogram through Rosetta: $app_bin" >&2
    /usr/bin/arch -x86_64 "$app_bin" "$@"
    return
  fi

  "$app_bin" "$@"
}

usage() {
  cat <<'EOF'
Usage:
  autogram-cli-sign.sh [options] <file-or-dir> [...]

Examples:
  autogram-cli-sign.sh "/path/to/sample.pdf"
  autogram-cli-sign.sh --driver eid --pdfa "/path/to/invoice.pdf"

Notes:
  - Signed outputs are created by Autogram CLI defaults (e.g. *_signed).
  - Use --key and --pin-stdin for non-interactive signing.
  - On Apple Silicon, Intel-only I.CA SecureStore needs an Intel Autogram build through Rosetta.
  - Common forwarded options include:
      --driver, --key, --target, --pdf-level, --slot-id, --keystore, --tsa-server, --pkcs11-driver-path
      --pin-stdin, --list-keys, --pdfa, --plain-xml, --en319132, --force, --parents
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

declare -a common_opts=()
declare -a sources=()
requested_driver=""
list_keys_requested=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --driver|--key|--target|--pdf-level|--slot-id|--keystore|--tsa-server|--pkcs11-driver-path)
      if [[ $# -lt 2 ]]; then
        echo "Missing value for option: $1"
        exit 1
      fi
      common_opts+=("$1" "${2:-}")
      if [[ "$1" == "--driver" ]]; then
        requested_driver="${2:-}"
      fi
      shift 2
      ;;
    --pin-stdin|--list-keys|--pdfa|--plain-xml|--en319132|--force|--parents)
      common_opts+=("$1")
      [[ "$1" == "--list-keys" ]] && list_keys_requested=1
      shift
      ;;
    -*)
      echo "Unsupported option: $1"
      usage
      exit 1
      ;;
    *)
      sources+=("$1")
      shift
      ;;
  esac
done

if [[ ${#sources[@]} -eq 0 && "$list_keys_requested" -eq 0 ]]; then
  echo "No source file or directory provided."
  usage
  exit 1
fi

AUTOGRAM_APP_BIN="$(resolve_autogram_bin || true)"
if should_use_intel_autogram "$requested_driver"; then
  INTEL_AUTOGRAM_APP_BIN="$(resolve_intel_autogram_bin || true)"
  if [[ -z "$INTEL_AUTOGRAM_APP_BIN" ]]; then
    echo "Intel Autogram is required for the installed I.CA SecureStore driver."
    echo "The driver is x86_64-only, while this Mac runs on Apple Silicon."
    echo "Install the official Intel Autogram package or set AUTOGRAM_INTEL_BIN explicitly."
    echo "Expected Intel app: \$HOME/Applications/Autogram Intel GUI.app/Contents/MacOS/AutogramApp"
    exit 1
  fi
  AUTOGRAM_APP_BIN="$INTEL_AUTOGRAM_APP_BIN"
fi

if [[ -z "$AUTOGRAM_APP_BIN" ]]; then
  echo "Autogram executable not found."
  echo "Expected one of:"
  echo "  /Applications/Autogram.app/Contents/MacOS/AutogramApp"
  echo "  \$HOME/Applications/Autogram.app/Contents/MacOS/AutogramApp"
  echo "  <repo>/target/app-image/Autogram.app/Contents/MacOS/AutogramApp"
  echo "Or set AUTOGRAM_BIN explicitly."
  exit 1
fi

if [[ "$list_keys_requested" -eq 1 ]]; then
  run_autogram "$AUTOGRAM_APP_BIN" --cli "${common_opts[@]}"
else
  for src in "${sources[@]}"; do
    echo "Signing: $src"
    if [[ ${#common_opts[@]} -gt 0 ]]; then
      run_autogram "$AUTOGRAM_APP_BIN" --cli -s "$src" "${common_opts[@]}"
    else
      run_autogram "$AUTOGRAM_APP_BIN" --cli -s "$src"
    fi
  done
fi

echo "Done."
