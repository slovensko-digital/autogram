#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
QUICK_ACTION_SCRIPT="$SCRIPT_DIR/autogram-quick-action.sh"

if [[ ! -x "$QUICK_ACTION_SCRIPT" ]]; then
  echo "Quick action launcher is missing or not executable: $QUICK_ACTION_SCRIPT"
  exit 1
fi

finder_selection_text="$(/usr/bin/osascript <<'OSA'
tell application "Finder"
  if (count of selection) is 0 then
    return ""
  end if

  set outText to ""
  repeat with it in selection
    set outText to outText & POSIX path of (it as alias) & linefeed
  end repeat
  return outText
end tell
OSA
)"

declare -a finder_selection=()
while IFS= read -r selected_path; do
  [[ -n "$selected_path" ]] && finder_selection+=("$selected_path")
done <<< "$finder_selection_text"

if [[ ${#finder_selection[@]} -eq 0 || -z "${finder_selection[0]:-}" ]]; then
  echo "No selected files/folders found in Finder."
  exit 0
fi

"$QUICK_ACTION_SCRIPT" "${finder_selection[@]}"
