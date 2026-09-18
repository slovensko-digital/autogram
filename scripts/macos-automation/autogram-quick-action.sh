#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
CLI_SCRIPT="$SCRIPT_DIR/autogram-cli-sign.sh"
QUALIFIED_TSA_URL="${AUTOGRAM_TSA_SERVER:-http://timestamp.sectigo.com/qualified}"

show_alert() {
  local title="$1"
  local message="$2"

  /usr/bin/osascript - "$title" "$message" <<'OSA'
on run argv
  display alert (item 1 of argv) message (item 2 of argv) as warning
end run
OSA
}

choose_driver() {
  /usr/bin/osascript <<'OSA'
set driverChoices to {"I.CA SecureStore", "Občiansky preukaz (eID klient)"}
set selectedDriver to choose from list driverChoices ¬
  with title "Autogram: CLI podpis PDF" ¬
  with prompt "Vyberte úložisko podpisového certifikátu." ¬
  default items {"I.CA SecureStore"} ¬
  OK button name "Pokračovať" ¬
  cancel button name "Zrušiť" ¬
  without multiple selections allowed

if selectedDriver is false then
  return "CANCELLED"
end if

return item 1 of selectedDriver
OSA
}

choose_key_label() {
  /usr/bin/osascript - "$@" <<'OSA'
on run argv
  set selectedKey to choose from list argv ¬
    with title "Autogram: podpisový certifikát" ¬
    with prompt "Vyberte certifikát, ktorým sa má PDF podpísať." ¬
    OK button name "Vybrať" ¬
    cancel button name "Zrušiť" ¬
    without multiple selections allowed

  if selectedKey is false then
    return "CANCELLED"
  end if

  return item 1 of selectedKey
end run
OSA
}

ask_pin() {
  /usr/bin/osascript <<'OSA'
try
  set pinDialog to display dialog "Zadajte podpisový PIN alebo heslo k certifikátu." ¬
    with title "Autogram: podpisový PIN" ¬
    default answer "" ¬
    with hidden answer ¬
    buttons {"Zrušiť", "Podpísať"} ¬
    default button "Podpísať" ¬
    cancel button "Zrušiť"
  return text returned of pinDialog
on error number -128
  return "CANCELLED"
end try
OSA
}

collect_pdfs() {
  local item

  for item in "$@"; do
    if [[ -f "$item" ]]; then
      case "${item##*.}" in
        [pP][dD][fF]) printf '%s\0' "$item" ;;
      esac
    elif [[ -d "$item" ]]; then
        /usr/bin/find "$item" -type f -iname '*.pdf' -print0
    fi
  done
}

next_signed_path() {
  local source="$1"
  local stem="${source%.*}"
  local candidate="${stem}_signed.pdf"
  local suffix=1

  while [[ -e "$candidate" ]]; do
    candidate="${stem}_signed ($suffix).pdf"
    suffix=$((suffix + 1))
  done

  printf '%s\n' "$candidate"
}

is_valid_pdf() {
  local path="$1"
  [[ -s "$path" ]] || return 1
  [[ "$(/usr/bin/head -c 5 "$path")" == "%PDF-" ]] || return 1
  /usr/bin/tail -c 1024 "$path" | /usr/bin/grep -q '%%EOF'
}

cli_error_details() {
  local log_file="$1"
  local details

  details="$(/usr/bin/tail -n 12 "$log_file" 2>/dev/null | /usr/bin/tr '\n' ' ' | /usr/bin/cut -c1-800)"
  if [[ -z "$details" ]]; then
    details="CLI neposkytlo ďalšie podrobnosti."
  fi

  printf '%s\n' "$details"
}

driver_shortname() {
  case "$1" in
    "I.CA SecureStore") printf '%s\n' "secure_store" ;;
    "Občiansky preukaz (eID klient)") printf '%s\n' "eid" ;;
    *) return 1 ;;
  esac
}

if [[ $# -eq 0 ]]; then
  show_alert "Autogram" "Neboli odovzdané žiadne súbory."
  exit 0
fi

declare -a pdfs=()
while IFS= read -r -d '' pdf; do
  pdfs+=("$pdf")
done < <(collect_pdfs "$@")

if [[ ${#pdfs[@]} -eq 0 ]]; then
  show_alert "Autogram" "Vo výbere sa nenašli žiadne PDF súbory."
  exit 0
fi

driver_name="$(choose_driver)"
if [[ "$driver_name" == "CANCELLED" || -z "$driver_name" ]]; then
  exit 0
fi

driver_name_arg="$(driver_shortname "$driver_name" || true)"
if [[ -z "$driver_name_arg" ]]; then
  show_alert "Autogram" "Neznáme úložisko certifikátu: $driver_name"
  exit 1
fi

tmp_dir="$(/usr/bin/mktemp -d -t autogram-quick-action)"
trap '/bin/rm -rf "$tmp_dir"' EXIT

pin="$(ask_pin)"
if [[ "$pin" == "CANCELLED" ]]; then
  exit 0
fi

key_output="$tmp_dir/keys.tsv"
key_error="$tmp_dir/keys.error"
set +e
printf '%s\n' "$pin" | "$CLI_SCRIPT" \
    --driver "$driver_name_arg" \
    --list-keys \
    --pin-stdin > "$key_output" 2> "$key_error"
key_status=$?
set -e

if [[ "$key_status" -ne 0 ]] && ! /usr/bin/grep -q $'^AUTOGRAM_KEY\t' "$key_output"; then
  key_details="$(cli_error_details "$key_error")"
  key_message="$(printf '%s\n\n%s' \
    'Nepodarilo sa načítať podpisové certifikáty. Skontrolujte pripojené úložisko a PIN.' \
    "$key_details")"
  show_alert "Autogram" "$key_message"
  exit 0
fi

declare -a key_selectors=()
declare -a key_labels=()
while IFS=$'\t' read -r record_type selector label; do
  [[ "$record_type" == "AUTOGRAM_KEY" ]] || continue
  [[ -n "$selector" ]] || continue
  key_selectors+=("$selector")
  key_labels+=("$label [$selector]")
done < "$key_output"

if [[ ${#key_selectors[@]} -eq 0 ]]; then
  show_alert "Autogram" "V zvolenom úložisku sa nenašiel žiadny podpisový certifikát."
  exit 1
fi

if [[ ${#key_selectors[@]} -eq 1 ]]; then
  key_selector="${key_selectors[0]}"
else
  selected_label="$(choose_key_label "${key_labels[@]}")"
  if [[ "$selected_label" == "CANCELLED" || -z "$selected_label" ]]; then
    exit 0
  fi

  key_selector=""
  for index in "${!key_labels[@]}"; do
    if [[ "${key_labels[$index]}" == "$selected_label" ]]; then
      key_selector="${key_selectors[$index]}"
      break
    fi
  done

  if [[ -z "$key_selector" ]]; then
    show_alert "Autogram" "Nepodarilo sa vybrať podpisový certifikát."
    exit 1
  fi
fi

for index in "${!pdfs[@]}"; do
  log_file="$tmp_dir/sign-$index.log"
  target_path="$(next_signed_path "${pdfs[$index]}")"
  set +e
  printf '%s\n' "$pin" | "$CLI_SCRIPT" \
      --driver "$driver_name_arg" \
      --key "$key_selector" \
      --pin-stdin \
      --pdf-level PAdES_BASELINE_T \
      --tsa-server "$QUALIFIED_TSA_URL" \
      --target "$target_path" \
      "${pdfs[$index]}" > "$log_file" 2>&1
  set -e

  if ! is_valid_pdf "$target_path"; then
    sign_details="$(cli_error_details "$log_file")"
    sign_message="$(printf 'Podpisovanie zlyhalo pri súbore: %s\n\n%s' \
      "${pdfs[$index]}" "$sign_details")"
    show_alert "Autogram" "$sign_message"
    exit 0
  fi
done

success_message="$(printf 'Podpísaných PDF: %s\nPoužitý podpis: PAdES Baseline T\nKvalifikovaná časová pečiatka: Sectigo qualified TSA' "${#pdfs[@]}")"
show_alert "Autogram" "$success_message"
