#!/usr/bin/env python3
"""Check register coverage and Markdown links; this is not a legal-content audit."""

import argparse
import re
from pathlib import Path
from urllib.parse import unquote


ROOT = Path(__file__).resolve().parents[1]
REGISTER = ROOT / "docs/legal-acts"
REPORT = ROOT / "docs/eidas-2-compliance-report.md"
EU = re.compile(r"https://eur-lex\.europa\.eu/eli/(reg_impl|dec_impl|reg|dec|dir)/(\d{4})/(\d+)")
SK = re.compile(r"https://www\.slov-lex\.sk/ezbierky/pravne-predpisy/SK/ZZ/(\d{4})/(\d+)")
STATUSES = {"Done", "Not done", "Unknown", "Ignored"}
RELEVANCE = {"Direct", "Conditional", "Indirect", "None"}


def inventory():
    text = REPORT.read_text()
    result = {f"eu-{kind}-{year}-{number}.md" for kind, year, number in EU.findall(text)}
    result.update(f"sk-{year}-{number}.md" for year, number in SK.findall(text))
    return sorted(result)


def plain(cell):
    return re.sub(r"[*`]", "", cell).strip()


def check_file(path, expected_names=()):
    text = path.read_text()
    errors = []
    pending = []
    if not re.search(r"executive summary", text, re.I):
        errors.append("missing Executive summary")
    if not re.search(r"coverage", text, re.I):
        errors.append("missing coverage statement")
    if not re.search(r"https://(?:eur-lex\.europa\.eu|www\.slov-lex\.sk|static\.slov-lex\.sk|eur-lex\.europa\.eu|op\.europa\.eu|publications\.europa\.eu|www\.etsi\.org|www\.iso\.org|www\.itu\.int)", text):
        errors.append("missing official legal-text link")

    for link in re.findall(r"\]\(([^\s)]+)(?:\s+\"[^\"]*\")?\)", text):
        target = unquote(link.split("#", 1)[0])
        if not target or re.match(r"(?:[a-zA-Z][a-zA-Z0-9+.-]*:|//)", target):
            continue
        if not (path.parent / target).exists():
            if Path(target).name in expected_names:
                pending.append(target)
            else:
                errors.append(f"broken local link: {target}")

    columns = None
    rows = 0
    for number, line in enumerate(text.splitlines(), 1):
        if not line.startswith("|"):
            columns = None
            continue
        cells = [plain(cell) for cell in re.split(r"(?<!\\)\|", line.strip())[1:-1]]
        if "Relevance" in cells and "Status" in cells:
            columns = (len(cells), cells.index("Relevance"), cells.index("Status"))
            continue
        if columns is None or all(re.fullmatch(r":?-+:?", cell.replace(" ", "")) for cell in cells):
            continue
        expected, relevance, status = columns
        if len(cells) != expected:
            errors.append(f"line {number}: table has {len(cells)} cells, expected {expected}")
            continue
        rows += 1
        if cells[relevance] not in RELEVANCE:
            errors.append(f"line {number}: invalid relevance {cells[relevance]!r}")
        if cells[status] not in STATUSES:
            errors.append(f"line {number}: invalid status {cells[status]!r}")
        if cells[relevance] == "None" and cells[status] in {"Done", "Not done"}:
            errors.append(f"line {number}: no relevance but completion/breach status")
    if rows == 0:
        errors.append("no assessment rows (source may be blocked; manually review)")
    return rows, errors, pending


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--inventory", action="store_true", help="print expected filenames without reading agent output")
    parser.add_argument("files", nargs="*", help="check only finished documents, otherwise check the complete register")
    args = parser.parse_args()
    expected = inventory()
    if args.inventory:
        print("\n".join(expected))
        return 0
    files = [Path(name) for name in args.files] if args.files else [REGISTER / name for name in expected]
    failures = 0
    for path in files:
        if not path.exists():
            print(f"MISSING: {path.relative_to(ROOT)}")
            failures += 1
            continue
        rows, errors, pending = check_file(path, set(expected))
        print(f"{path.name}: {rows} assessment rows; {len(errors)} structural issues; {len(pending)} pending links to not-yet-created acts")
        for error in errors:
            print(f"  {error}")
        failures += len(errors)
    print(f"Inventory: {len(expected)} acts. Structural issues: {failures}.")
    print("This check cannot prove source completeness, correct applicability, current law or factual compliance.")
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
