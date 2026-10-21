#!/usr/bin/env python3
"""Export the legal-act register to an Excel workbook with one sheet per act.

Each register row becomes one workbook row: one provision-level unit, the
register's own assessment, and - where it can be resolved deterministically -
the verbatim text of that provision taken from the official publication
(EUR-Lex via the Publications Office Cellar API, or the static Slov-Lex
version pages).

The workbook is a working copy for online review. It is generated from the
Markdown register; edits made in the spreadsheet are not written back.

Usage:
    python3 scripts/export-legal-register-xlsx.py                 # build the workbook
    python3 scripts/export-legal-register-xlsx.py --offline       # use the cache only
    python3 scripts/export-legal-register-xlsx.py --only "EU 2014/910"
"""

import argparse
import re
import sys
import time
from html import unescape
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

try:
    import openpyxl
    from openpyxl.styles import Alignment, Font, PatternFill
    from openpyxl.utils import get_column_letter
except ImportError:  # pragma: no cover - dependency guard
    sys.exit("This script needs openpyxl: python3 -m pip install openpyxl")

try:
    from bs4 import BeautifulSoup, NavigableString
except ImportError:  # pragma: no cover - dependency guard
    sys.exit("This script needs beautifulsoup4: python3 -m pip install beautifulsoup4")

ROOT = Path(__file__).resolve().parents[1]
REGISTER = ROOT / "docs" / "legal-acts"
CACHE = ROOT / ".cache" / "legal-texts"
ASSESSMENT_DATE = "2026-10-08"
USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126 Safari/537.36"
MAX_CELL = 30000

COLUMNS = [
    ("Act", 16),
    ("Act type", 22),
    ("Act name", 44),
    ("Section", 40),
    ("Provision", 26),
    ("Row type", 16),
    ("Parent", 20),
    ("Official provision text", 90),
    ("Text source", 46),
    ("Requirement / assessment", 70),
    ("Relevance", 13),
    ("Assessment", 13),
    ("Ignored?", 10),
    ("Reason / evidence needed", 60),
    ("Dates / notes", 34),
    ("Official URL", 60),
    ("Register document", 30),
    ("Owner", 14),
    ("Review state", 14),
    ("Priority", 10),
    ("Evidence URL", 40),
    ("Notes", 40),
    ("Last reviewed", 14),
]

# --------------------------------------------------------------------------------------
# Markdown register parsing
# --------------------------------------------------------------------------------------

LINK_RE = re.compile(r"\[((?:[^\[\]]|\[[^\[\]]*\])*)\]\(([^)]*)\)")


def strip_md(text):
    """Reduce a Markdown table cell to plain text."""
    text = LINK_RE.sub(r"\1", text)
    text = text.replace("\\|", "|")
    text = re.sub(r"[*`]{1,3}", "", text)
    text = text.replace("▼", "").replace("▲", "")
    return re.sub(r"\s+", " ", text).strip()


def first_link(cell):
    match = LINK_RE.search(cell)
    return match.group(2).strip() if match else None


def split_row(line):
    return [c.strip() for c in re.split(r"(?<!\\)\|", line.strip())[1:-1]]


ACT_TYPES = {
    "reg": "Regulation",
    "reg_impl": "Implementing regulation",
    "dec": "Decision",
    "dec_impl": "Implementing decision",
    "dir": "Directive",
    "sk": "Slovak act",
}

ROW_TYPES = [
    (r"^recital", "Recital"),
    (r"^(art|article)\b", "Article"),
    (r"^§", "Section"),
    (r"^(annex|anx)\b", "Annex point"),
    (r"^[IVX]{1,4}\b", "Annex point"),
    (r"^preamble", "Preamble"),
    (r"^(ch|chapter)\b", "Chapter"),
    (r"^(closing|signature|final)", "Signature block"),
]


def row_type(label):
    low = label.lower()
    for pattern, name in ROW_TYPES:
        if re.search(pattern, low):
            return name
    return "Other"


def parse_doc(path):
    """Parse one register document into its metadata and assessment rows."""
    text = path.read_text()
    lines = text.splitlines()
    title = ""
    for line in lines:
        if line.startswith("# "):
            title = line[2:].strip()
            break
    rows = []
    heading = ""
    index = 0
    while index < len(lines):
        line = lines[index]
        if line.startswith("#"):
            heading = strip_md(line.lstrip("#").strip())
            index += 1
            continue
        if line.startswith("|"):
            cells = split_row(line)
            if "Relevance" in cells and "Status" in cells:
                header = cells
                index += 1
                while index < len(lines) and lines[index].startswith("|"):
                    data = split_row(lines[index])
                    if len(data) == len(header) and not all(
                        re.fullmatch(r":?-+:?", c.replace(" ", "")) for c in data
                    ):
                        rows.append(
                            {
                                "section": heading,
                                "provision": strip_md(data[0]),
                                "url": first_link(data[0]),
                                "requirement": strip_md(data[1]),
                                "relevance": strip_md(data[2]),
                                "assessment": strip_md(data[3]),
                                "evidence": strip_md(data[4]),
                                "notes": strip_md(data[5]),
                            }
                        )
                    index += 1
                continue
            index += 1
            continue
        bullet = re.match(r"^-\s+\*\*\[\((\d+)\)\]\((https?://[^)]+)\)\*\*\s+(.*)$", line)
        if bullet:
            body = strip_md(bullet.group(3))
            relevance = ""
            status = ""
            rel_match = re.search(r"Relevance:\s*([A-Za-z]+)", body)
            if rel_match:
                relevance = rel_match.group(1)
            status_match = re.search(r"\*(Done|Not done|Unknown|Ignored)\*", bullet.group(3))
            if status_match:
                status = status_match.group(1)
            rows.append(
                {
                    "section": heading,
                    "provision": f"Recital {bullet.group(1)}",
                    "url": bullet.group(2),
                    "requirement": body,
                    "relevance": relevance,
                    "assessment": status,
                    "evidence": "",
                    "notes": "",
                }
            )
        index += 1
    return {"title": title, "rows": rows}


def act_identity(filename):
    """Derive the act label and type from the register file name."""
    stem = filename[:-3]
    if stem == "etsi-ts-119101":
        return "ETSI TS 119 101", "Technical standard"
    match = re.match(r"^sk-(\d{4})-(\d+)$", stem)
    if match:
        return f"SK {match.group(1)}/{match.group(2)}", ACT_TYPES["sk"]
    match = re.match(r"^eu-([a-z_]+)-(\d{4})-(\d+)$", stem)
    if match:
        kind, year, number = match.groups()
        return f"EU {year}/{number}", ACT_TYPES.get(kind, kind)
    return stem, "Other"


# --------------------------------------------------------------------------------------
# Official texts
# --------------------------------------------------------------------------------------

ELI_RE = re.compile(
    r"/eli/(?P<kind>reg_impl|dec_impl|reg|dec|dir)/(?P<year>\d{4})/(?P<number>\d+)"
    r"(?:/(?P<date>\d{4}-\d{2}-\d{2}))?"
)
SK_RE = re.compile(r"SK/ZZ/(?P<year>\d{4})/(?P<number>\d+)")


def celex_for(url):
    """Map an ELI URL to the CELEX identifier of the corresponding official text."""
    match = ELI_RE.search(url or "")
    if not match:
        return None
    kind = match.group("kind")
    letter = "R" if kind.startswith("reg") else ("D" if kind.startswith("dec") else "L")
    year = match.group("year")
    number = match.group("number").zfill(4)
    date = match.group("date")
    if date:
        return f"0{year}{letter}{number}-{date.replace('-', '')}"
    return f"3{year}{letter}{number}"


def fetch(url, cache_path, offline=False, headers=None):
    """Fetch a URL, caching the response on disk."""
    if cache_path.exists():
        return cache_path.read_text(encoding="utf-8", errors="ignore")
    if offline:
        return None
    request = Request(url, headers=headers or {"User-Agent": USER_AGENT})
    for attempt in range(3):
        try:
            with urlopen(request, timeout=180) as response:
                data = response.read().decode("utf-8", errors="ignore")
            cache_path.parent.mkdir(parents=True, exist_ok=True)
            cache_path.write_text(data, encoding="utf-8")
            time.sleep(0.4)
            return data
        except (HTTPError, URLError, TimeoutError) as error:
            if attempt == 2:
                print(f"  ! fetch failed {url}: {error}", file=sys.stderr)
                return None
            time.sleep(2 + attempt * 3)
    return None


# ---- generic HTML helpers ------------------------------------------------------------

TAG_RE = re.compile(r"<[^>]+>", re.S)
SPACE_RE = re.compile(r"[\s\u00a0\u202f]+")


def html_text(fragment):
    text = unescape(TAG_RE.sub(" ", fragment))
    return SPACE_RE.sub(" ", text).strip()


def div_ranges(text):
    """Map the start offset of every <div> to the offset just past its closing tag."""
    ranges = {}
    stack = []
    for match in re.finditer(r"<(/?)div\b[^>]*>", text, re.I):
        tag = match.group(0)
        if match.group(1):
            if stack:
                ranges[stack.pop()] = match.end()
        elif tag.endswith("/>"):
            continue
        else:
            stack.append(match.start())
    return ranges


def divs_with_class(text, ranges, class_pattern):
    """Return (identifier, start, end) for <div> elements with a matching class."""
    found = []
    for match in re.finditer(r"<div\b[^>]*>", text, re.I):
        tag = match.group(0)
        if not re.search(r'class="[^"]*' + class_pattern + r'[^"]*"', tag, re.I):
            continue
        id_match = re.search(r'\bid="([^"]+)"', tag)
        if not id_match:
            continue
        if match.start() in ranges:
            found.append((id_match.group(1), match.start(), ranges[match.start()]))
    return found


def elements_by_id(text, ranges, pattern):
    """Return (identifier, start, end) for elements whose id matches the pattern."""
    found = []
    for match in re.finditer(r"<div\b[^>]*>", text, re.I):
        tag = match.group(0)
        id_match = re.search(r'\bid="([^"]+)"', tag)
        if not id_match:
            continue
        identifier = id_match.group(1)
        if not re.fullmatch(pattern, identifier):
            continue
        if match.start() in ranges:
            found.append((identifier, match.start(), ranges[match.start()]))
    return found


MARKER_RES = [
    ("num", re.compile(r"^\d+[a-z]?\.$")),
    ("paren_digit", re.compile(r"^\(\d+[a-z]?\)$")),
    ("letter", re.compile(r"^\([a-z]\)$")),
    ("roman", re.compile(r"^\([ivxlcdm]{2,}\)$")),
    ("dash", re.compile(r"^([—–-])$")),
]
RANK = {"num": 0, "paren_digit": 1, "letter": 2, "roman": 3, "dash": 4}


def marker_type(text):
    value = SPACE_RE.sub("", text).strip()
    if not value:
        return None
    for name, pattern in MARKER_RES:
        if pattern.match(value):
            return name
    return None


def norm_token(token):
    return token.strip("()").rstrip(".").strip().lower()


class OfficialText:
    """Provision-level texts of one official document, indexed by token path."""

    def __init__(self, label, version, source_url):
        self.label = label
        self.version = version
        self.source_url = source_url
        self.index = {}
        self.containers = {}

    def units(self, container):
        return self.index.get(container, {})


class EuDocument(OfficialText):
    """Official text of an EU act, read from the Publications Office XHTML."""

    TITLE_CLASSES = (
        "title-article-norm",
        "stitle-article-norm",
        "oj-doc-ti",
        "title-annex-1",
        "title-gr-seq-level-1",
        "title-num",
    )

    def __init__(self, label, version, source_url, xhtml):
        super().__init__(label, version, source_url)
        self.parse(xhtml)

    def parse(self, xhtml):
        soup = BeautifulSoup(xhtml, "html.parser")
        for tag in soup.find_all("div", id=re.compile(r"^(?:art|anx|cpt|sct|rct)_")):
            identifier = tag.get("id")
            units, full = self.render_structured(tag)
            if units:
                self.index[identifier] = units
            if full:
                self.containers[identifier] = full
        preamble = self.render_preamble(soup)
        if preamble:
            self.index["preamble"] = {(): preamble}
            self.containers["preamble"] = preamble
        # amending acts sometimes carry a generated id for their single article
        extra = [
            tag
            for tag in soup.find_all("div", class_="eli-subdivision")
            if not re.match(r"^(?:art|anx|cpt|sct|rct)_", tag.get("id") or "")
        ]
        if len(extra) == 1:
            units, full = self.render_structured(extra[0])
            if units:
                self.index["art_1"] = units
                self.containers["art_1"] = full

    @staticmethod
    def render_preamble(soup):
        """Text published before the first article or annex."""
        parts = []
        for element in soup.descendants:
            if isinstance(element, NavigableString):
                text = str(element).strip()
                if text:
                    parts.append(text)
                continue
            identifier = element.get("id") if getattr(element, "name", None) == "div" else None
            if identifier and re.match(r"^(?:art|anx|cpt|sct|rct)_", identifier):
                break
        return " ".join(parts)

    @staticmethod
    def own_text(element):
        """Text that sits directly in the element, without its children."""
        return SPACE_RE.sub(" ", " ".join(str(child) for child in element.children if isinstance(child, NavigableString))).strip()

    def render_structured(self, container):
        """Walk the provision structure; return {token path: text} and the full text."""
        events = []
        for element in container.find_all(True):
            classes = element.get("class") or []
            if element.name == "span" and "no-parag" in classes:
                events.append(("marker", element.get_text(" ", strip=True)))
                continue
            if element.name == "div" and "grid-list-column-1" in classes:
                span = element.find("span")
                if span is not None:
                    events.append(("marker", span.get_text(" ", strip=True)))
                continue
            if element.name not in ("p", "td", "div"):
                continue
            if any(name in classes for name in self.TITLE_CLASSES):
                continue
            text = self.own_text(element)
            if not text:
                continue
            kind = marker_type(text)
            events.append(("marker" if kind else "text", text))
        events = [(kind, value) for kind, value in events if value]

        units = {}
        stack = []
        unmarked = 0
        full_parts = []

        for kind, value in events:
            if kind == "marker":
                mtype = marker_type(value)
                if mtype is None:
                    continue
                if mtype == "letter" and stack and stack[-1][0] == "letter" and value.strip() in ("(ii)", "(iii)", "(iv)"):
                    mtype = "roman"
                while stack and RANK[stack[-1][0]] > RANK[mtype]:
                    stack.pop()
                if stack and RANK[stack[-1][0]] == RANK[mtype]:
                    stack.pop()
                token = norm_token(value)
                flat = tuple(item[1] for item in stack) + (token,)
                stack.append((mtype, token, flat))
                units.setdefault(flat, "")
                continue
            path = stack[-1][2] if stack else None
            if path is None:
                unmarked += 1
                path = (f"u{unmarked}",)
                units.setdefault(path, "")
            units[path] = (units[path] + " " + value).strip()
            full_parts.append(value)
        return units, " ".join(full_parts)


class SkDocument(OfficialText):
    """Official text of a Slovak act, read from the static Slov-Lex pages."""

    LEVELS = {"odsek": 0, "pismeno": 1, "bod": 2}

    def __init__(self, label, version, source_url, html):
        super().__init__(label, version, source_url)
        self.parse(html)

    def parse(self, html):
        soup = BeautifulSoup(html, "html.parser")
        for paragraph in soup.find_all("div", class_="paragraf"):
            identifier = paragraph.get("id")
            if not identifier:
                continue
            units, full = self.render_units(paragraph)
            if full:
                self.index[identifier] = units
                self.containers[identifier] = " ".join(full)
        for article in soup.find_all("div", class_="clanok"):
            identifier = article.get("id")
            if not identifier:
                continue
            marker = article.find(class_="clanokOznacenie")
            label = marker.get_text(" ", strip=True) if marker else ""
            match = re.search(r"([IVXivx]+)\s*$", label)
            key = f"clanok-{match.group(1).lower()}" if match else identifier
            units, full = self.render_units(article)
            if full:
                self.index[key] = units
                self.containers[key] = " ".join(full)

    def render_units(self, paragraph):
        """Collect the text of a Slovak unit and the path of every paragraph."""
        events = []
        for element in paragraph.find_all(True):
            classes = element.get("class") or []
            if "odsekOznacenie" in classes:
                events.append(("odsek", element.get_text(" ", strip=True)))
            elif "pismenoOznacenie" in classes:
                events.append(("pismeno", element.get_text(" ", strip=True)))
            elif "bodOznacenie" in classes:
                events.append(("bod", element.get_text(" ", strip=True)))
            elif element.name == "div" and "text" in classes:
                text = element.get_text(" ", strip=True)
                if text:
                    events.append(("text", text))
        levels = {}
        units = {}
        full = []
        unmarked = 0
        for kind, value in events:
            if kind == "text":
                key = tuple(levels[index] for index in sorted(levels)) or ("u1",)
                units[key] = (units.get(key, "") + " " + value).strip()
                full.append(value)
                continue
            level = self.LEVELS[kind]
            if not value:
                unmarked += 1
                token = f"u{unmarked}"
            else:
                token = norm_token(value)
            levels[level] = token
        return units, full

    @staticmethod
    def suffix_of(element_id, odsek_id):
        """Tokens of a lettered or numbered point below an odsek."""
        tokens = []
        rest = element_id[len(odsek_id):] if element_id.startswith(odsek_id) else element_id
        for part in rest.split("."):
            if part.startswith("pismeno-"):
                tokens.append(norm_token(part[8:]))
            elif part.startswith("bod-"):
                tokens.append(norm_token(part[4:]))
        return tuple(tokens)


# --------------------------------------------------------------------------------------
# Label parsing and matching
# --------------------------------------------------------------------------------------

PATH_RE = re.compile(r"(\d+[a-z]?|\([a-z0-9]+\)|[IVX]{1,4})")
ROMAN_ANNEX = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"}


def label_tokens(label):
    """Extract the path tokens of a provision label, e.g. '32.1(a)' -> ['32','1','a']."""
    unnumbered = re.search(r"unnumbered (?:paragraph|para\.?|chapeau)\s+(\d+)", label, re.I)
    if unnumbered:
        return [f"u{unnumbered.group(1)}"]
    if re.search(r"chapeau", label, re.I):
        return ["u1"]
    # a range such as '12.3(a)-(c)' is matched on its first item
    label = re.split(r"\s*[–—]\s*|\s+-\s+|/", label)[0]
    cleaned = re.sub(
        r"^(art\.?|article|§|annex|anx|ch\.?|chapter|recital|part)\s*", "", label, flags=re.I
    )
    cleaned = cleaned.split(",")[0]
    tokens = []
    for match in PATH_RE.finditer(cleaned):
        token = match.group(1)
        if token in ROMAN_ANNEX and not tokens:
            continue
        tokens.append(norm_token(token))
    return tokens


def token_variants(tokens):
    """Alternative readings of a label, e.g. '20.3c' as ['20','3c'] and ['20','3','c']."""
    variants = [tuple(tokens)]
    split = []
    for token in tokens:
        match = re.fullmatch(r"(\d+)([a-z])", token)
        if match:
            split.extend([match.group(1), match.group(2)])
        else:
            split.append(token)
    if split != list(tokens):
        variants.append(tuple(split))
    return variants


def parent_of(tokens):
    return ".".join(tokens[:-1]) if len(tokens) > 1 else ""


def guess_container(document, label, section=""):
    """Find the unit a label refers to when the register row carries no anchor."""
    if not document:
        return None
    for source in (label, section):
        low = source.lower()
        match = re.search(r"annex\s+([ivx]+|\d+)", low)
        if match:
            token = match.group(1)
            candidate = "anx_" + (token.upper() if token.isalpha() else token)
            if candidate in document.index:
                return candidate
        match = re.search(r"(?:art(?:icle)?\.?|§)\s*(\d+[a-z]?)", low)
        if match:
            candidate = "art_" + match.group(1)
            if candidate in document.index:
                return candidate
    low = label.lower()
    if re.match(r"^[a-z]\s*[—–-]", low) and "art_1" in document.index:
        return "art_1"
    match = re.search(r"article\s+([ivx]+)\b", low)
    if match:
        candidate = "clanok-" + match.group(1)
        if candidate in document.index:
            return candidate
    if "preamble" in low and "preamble" in document.index:
        return "preamble"
    # a single annex in the act: use it for rows that point into it
    annexes = sorted(key for key in document.index if key.startswith("anx_"))
    if len(annexes) == 1 and re.search(r"annex|gr\s|part\s", low):
        return annexes[0]
    return None


def family_container(document, anchor):
    """Nearest unit of the same family, e.g. 'anx_2' when the text only has 'anx_1'."""
    if not document or anchor in document.index:
        return None
    family = anchor.split("_", 1)[0] + "_"
    candidates = sorted(key for key in document.index if key.startswith(family))
    return candidates[0] if candidates else None


def find_unit(document, container, tokens, label, section=""):
    """Resolve the official text of a row: exact unit, parent unit, or whole container."""
    if not document:
        return "", "not resolved - needs source review", "unresolved"
    if not container:
        guess = guess_container(document, label, section)
        if guess:
            return (
                document.containers.get(guess, ""),
                f"{document.source_url} - {guess} (unit derived from the label)",
                "container",
            )
        return "", "not resolved - needs source review", "unresolved"
    units = document.units(container)
    if not units:
        near = family_container(document, container)
        if near:
            return (
                document.containers.get(near, ""),
                f"{document.source_url} - {near} (anchor {container} not in the retrieved text)",
                "container",
            )
        return "", f"{document.source_url} - container {container} not found - needs source review", "unresolved"
    if () in units and units[()].strip():
        return units[()].strip(), f"{document.source_url} - {container}", "exact"
    if not tokens:
        return (
            document.containers.get(container, ""),
            f"{document.source_url} - {container} (whole unit)",
            "exact",
        )
    for variant in token_variants(tokens):
        for size in range(len(variant), 0, -1):
            candidate = variant[-size:]
            if candidate in units and units[candidate].strip():
                quality = "exact" if size >= len(variant) - 1 else "parent"
                note = "" if quality == "exact" else f" (parent of {label})"
                return (
                    units[candidate].strip(),
                    f"{document.source_url} - {container}{note}",
                    quality,
                )
    if len(tokens) == 1 and container.split("_", 1)[-1].lower() == tokens[0].lower():
        return (
            document.containers.get(container, ""),
            f"{document.source_url} - {container} (whole unit)",
            "exact",
        )
    # last resort: a single unit of this container ending with the same token
    tail = tokens[-1]
    endings = [key for key in units if key and key[-1] == tail and units[key].strip()]
    if len(endings) == 1:
        return (
            units[endings[0]].strip(),
            f"{document.source_url} - {container} (unit ending in {tail})",
            "parent",
        )
    return (
        document.containers.get(container, ""),
        f"{document.source_url} - {container} (whole unit)",
        "container",
    )


# --------------------------------------------------------------------------------------
# Document loading
# --------------------------------------------------------------------------------------


def load_eu_documents(rows_by_act, offline):
    """Fetch and parse the official text for every EU act referenced by the register."""
    wanted = set()
    for rows in rows_by_act.values():
        for row in rows:
            celex = celex_for(row["url"])
            if celex:
                wanted.add(celex)
    documents = {}
    for index, celex in enumerate(sorted(wanted), 1):
        url = f"https://publications.europa.eu/resource/celex/{celex}"
        cache_path = CACHE / f"{celex}.xhtml"
        data = fetch(
            url,
            cache_path,
            offline,
            headers={
                "User-Agent": USER_AGENT,
                "Accept": "application/xhtml+xml",
                "Accept-Language": "eng",
            },
        )
        if not data:
            continue
        documents[celex] = EuDocument(celex, celex, url, data)
        print(f"  [{index}/{len(wanted)}] EU {celex}")
    return documents


def sk_version(year, number, offline):
    """Resolve the Slov-Lex version in force on the assessment date."""
    base = f"https://static.slov-lex.sk/static/SK/ZZ/{year}/{number}/"
    history = fetch(base, CACHE / f"sk-{year}-{number}-history.html", offline)
    if not history:
        return None, None
    entries = re.findall(
        r'<tr class="effectivenessHistoryItem"[^>]*data-iri="([^"]+)"[^>]*data-ucinnostod="([^"]*)"'
        r'[^>]*data-ucinnostdo="([^"]*)"',
        history,
    )
    chosen = None
    for iri, start, end in entries:
        if start and start <= ASSESSMENT_DATE and (not end or end > ASSESSMENT_DATE):
            chosen = iri
            break
    if not chosen:
        dated = sorted(iri for iri, start, _ in entries if start)
        chosen = dated[-1] if dated else None
    if not chosen:
        return None, None
    version = chosen.rsplit("/", 1)[-1]
    url = f"https://static.slov-lex.sk/static/SK/ZZ/{year}/{number}/{version}.html"
    return url, version


def load_sk_documents(rows_by_act, offline):
    documents = {}
    for rows in rows_by_act.values():
        match = SK_RE.search(rows[0]["url"] or "") if rows else None
        if not match:
            continue
        year, number = match.group("year"), match.group("number")
        url, version = sk_version(year, number, offline)
        if not url:
            continue
        data = fetch(url, CACHE / f"sk-{year}-{number}-{version}.html", offline)
        if not data:
            continue
        documents[(year, number)] = SkDocument(f"SK {year}/{number}", version, url, data)
        print(f"  SK {year}/{number} ({version})")
    return documents


# --------------------------------------------------------------------------------------
# Workbook
# --------------------------------------------------------------------------------------


def instruction_rows():
    return [
        ["Item", "Value"],
        ["Workbook", "Autogram legal-act register - one sheet per act"],
        ["Generated", time.strftime("%Y-%m-%d %H:%M")],
        ["Register", "docs/legal-acts/ (56 acts) plus the ETSI TS 119 101 analysis"],
        ["Assessment date of the register", ASSESSMENT_DATE],
        [
            "Official text",
            "Verbatim provision text from the official publication: EUR-Lex (Publications Office Cellar) for EU acts, "
            "static Slov-Lex for Slovak acts. Reproduced for review; the published acts remain authoritative.",
        ],
        [
            "Working copy",
            "Generated from the Markdown register. Edits here are not written back to the register; close the item in "
            "the register document and re-export.",
        ],
        [
            "Row granularity",
            "One row per register provision unit (article, paragraph, letter, annex point, recital, requirement).",
        ],
        [
            "Text source column",
            "'exact' = the row's own unit was matched; 'parent' = a parent unit was matched; 'container' = only the "
            "whole article or annex was matched; 'unresolved' = nothing matched and the row needs source review.",
        ],
        [
            "Ignored? column",
            "Yes only where the register marks the provision Ignored with a stated reason. Ignored never means an "
            "applicable obligation was knowingly disregarded.",
        ],
        ["Editable columns", "Owner, Review state, Priority, Evidence URL, Notes, Last reviewed."],
        [
            "Text coverage",
            "exact = the row's own unit; parent = a parent unit; container = the whole article or annex; "
            "unresolved = no match, the row still needs a source review. Slovak annexes of Decree 78/2020 are "
            "published as separate attachments and are therefore not covered by the automatic extraction.",
        ],
        [
            "Reproducing the export",
            "python3 scripts/export-legal-register-xlsx.py (needs openpyxl and beautifulsoup4; official texts are "
            "cached in .cache/legal-texts)",
        ],
    ]


def style_header(sheet, columns):
    for cell in sheet[1]:
        cell.font = Font(bold=True, color="FFFFFF")
        cell.fill = PatternFill("solid", fgColor="374151")
        cell.alignment = Alignment(vertical="center", wrap_text=True)
    for index, (_, width) in enumerate(columns, 1):
        sheet.column_dimensions[get_column_letter(index)].width = width


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out", default=str(ROOT / "docs" / "legal-acts" / "legal-register-export.xlsx"))
    parser.add_argument("--offline", action="store_true", help="use only cached official texts")
    parser.add_argument("--only", help="limit the export to a single act, e.g. 'EU 2014/910'")
    args = parser.parse_args()

    files = sorted(REGISTER.glob("*.md"))
    skip = {"README.md", "OVERVIEW.md", "VERIFICATION.md", "art15-accessibility.md"}
    files = [f for f in files if f.name not in skip]
    if args.only:
        files = [f for f in files if act_identity(f.name)[0] == args.only]

    parsed = {}
    eu_rows = {}
    sk_rows = {}
    for path in files:
        label, kind = act_identity(path.name)
        data = parse_doc(path)
        parsed[label] = (path, kind, data)
        for row in data["rows"]:
            if kind == "Technical standard":
                continue
            if label.startswith("SK"):
                sk_rows.setdefault(label, []).append(row)
            else:
                eu_rows.setdefault(label, []).append(row)

    total_rows = sum(len(data["rows"]) for _, _, data in parsed.values())
    print(f"parsed {len(parsed)} documents, {total_rows} rows")

    documents = {}
    if eu_rows:
        print("fetching EU official texts ...")
        documents.update(load_eu_documents(eu_rows, args.offline))
    if sk_rows:
        print("fetching Slovak official texts ...")
        documents.update(load_sk_documents(sk_rows, args.offline))

    workbook = openpyxl.Workbook()
    workbook.remove(workbook.active)

    sheet = workbook.create_sheet("00 How to use")
    sheet.append(["Item", "Value"])
    for row in instruction_rows()[1:]:
        sheet.append(row)
    style_header(sheet, [("Item", 32), ("Value", 120)])
    for row in sheet.iter_rows(min_row=2):
        for cell in row:
            cell.alignment = Alignment(vertical="top", wrap_text=True)

    summary = [["Act", "Act type", "Act name", "Rows", "Text resolved", "Sheet"]]
    stats = {"exact": 0, "parent": 0, "container": 0, "unresolved": 0, "excerpt": 0}
    summary_sheet = workbook.create_sheet("01 Summary")
    for row in summary:
        summary_sheet.append(row)

    for label, (path, kind, data) in parsed.items():
        act_name = strip_md(re.sub(r"^#\s*\S+\s*[—–-]\s*", "", data["title"]))
        rows = []
        for row in data["rows"]:
            url = row["url"]
            container = url.split("#", 1)[1] if url and "#" in url else ""
            tokens = label_tokens(row["provision"])
            if kind == "Technical standard":
                text = row["requirement"]
                source = "ETSI TS 119 101 V1.1.1 (separate work) - excerpt in the register, see the official PDF"
                quality = "excerpt"
            else:
                document = None
                if kind == "Slovak act":
                    match = SK_RE.search(url or "")
                    if match:
                        document = documents.get((match.group("year"), match.group("number")))
                    paragraph = re.search(r"§\s*(\d+)", row["provision"])
                    container = f"paragraf-{paragraph.group(1)}" if paragraph else container
                    if tokens and tokens[0].isdigit():
                        tokens = tokens[1:]
                else:
                    celex = celex_for(url)
                    document = documents.get(celex) if celex else None
                text, source, quality = find_unit(
                    document, container, tokens, row["provision"], row["section"]
                )
            stats[quality] = stats.get(quality, 0) + 1
            if len(text) > MAX_CELL:
                text = text[:MAX_CELL] + " ... [truncated for the cell limit]"
            rows.append(
                [
                    label,
                    kind,
                    act_name,
                    row["section"],
                    row["provision"],
                    row_type(row["provision"]),
                    parent_of(tokens),
                    text,
                    source,
                    row["requirement"],
                    row["relevance"],
                    row["assessment"],
                    "Yes" if row["assessment"] == "Ignored" else "No",
                    row["evidence"],
                    row["notes"],
                    url or "",
                    f"docs/legal-acts/{path.name}",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                ]
            )
        sheet_name = label.replace("/", "-")
        sheet = workbook.create_sheet(sheet_name[:31])
        sheet.append([column for column, _ in COLUMNS])
        for row in rows:
            sheet.append(row)
        style_header(sheet, COLUMNS)
        sheet.freeze_panes = "A2"
        sheet.auto_filter.ref = f"A1:{get_column_letter(len(COLUMNS))}{max(sheet.max_row, 1)}"
        for row in sheet.iter_rows(min_row=2):
            for cell in row:
                cell.alignment = Alignment(vertical="top", wrap_text=cell.column in (3, 4, 8, 10, 14, 15, 16, 22))
        resolved = sum(1 for r in rows if r[8] and "needs source review" not in r[8])
        summary.append([label, kind, act_name, len(rows), resolved, sheet_name])
        summary_sheet.append(summary[-1])

    style_header(summary_sheet, [("Act", 16), ("Act type", 22), ("Act name", 44), ("Rows", 10), ("Text resolved", 14), ("Sheet", 18)])
    for row in summary_sheet.iter_rows(min_row=2):
        for cell in row:
            cell.alignment = Alignment(vertical="top", wrap_text=True)
    summary_sheet.freeze_panes = "A2"

    out_path = Path(args.out)
    workbook.save(out_path)
    print(f"wrote {out_path} ({out_path.stat().st_size // 1024} kB)")
    print("text resolution:", stats)


if __name__ == "__main__":
    main()
