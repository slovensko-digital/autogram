# eIDAS Article 15 — accessibility obligations for Autogram

**Source:** [Regulation (EU) No 910/2014, Article 15, consolidated 18.10.2024](https://eur-lex.europa.eu/eli/reg/2014/910/2024-10-18/eng#art_15), as amended by [Regulation (EU) 2024/1183](eu-reg-2024-1183.md). **Reference act for the technical requirements:** [Directive (EU) 2019/882 (European Accessibility Act)](eu-dir-2019-882.md). **Assessment date:** 8 October 2026 · **Status:** **applicable, Not done — findings tracked**. Accessibility issues were raised at a hackathon; they are reported and will be fixed gradually.

> **Article 15.** The provision of electronic identification means, trust services and **end-user products that are used in the provision of those services** shall be made available in **plain and intelligible language**, in accordance with the **United Nations Convention on the Rights of Persons with Disabilities** and with the **accessibility requirements of Directive (EU) 2019/882**, thus also benefiting persons who experience functional limitations, such as elderly people, and persons with limited access to digital technologies.

**Why it applies to Autogram.** The user confirms that Autogram is used as an end-user product in the provision of an eID/trust service, so Article 15 is **applicable**. The obligations below are therefore not aspirational.

Article 15 decomposes into **three cumulative obligations**:

1. **Plain and intelligible language** — all user-facing text.
2. **UNCRPD accessibility** — accessibility on an equal basis with others.
3. **Directive (EU) 2019/882 (EAA) accessibility requirements** — the concrete technical criteria.

---

## 1. Plain and intelligible language

| # | Requirement | Where it applies in Autogram | Status |
| --- | --- | --- | --- |
| 1.1 | All user-facing text is clear, plain and understandable to a lay person. | GUI labels/messages, validation result texts, error dialogs, `l10n.properties` / `l10n_en.properties`, CLI help, API error messages. | Not done |
| 1.2 | Technical terms are explained or avoided (e.g. "qualified", "indeterminate", "trusted list"). | Qualification badges, validation detail screen. | Not done |
| 1.3 | Plain language available in the languages offered (SK and EN). | Both bundles. | Not done |

## 2. UNCRPD accessibility

| # | Requirement | Status |
| --- | --- | --- |
| 2.1 | Accessibility on an equal basis with other users; no disproportionate barriers. | Not done |
| 2.2 | Reasonable accommodation across the whole user journey (open file → review → sign → validate). | Not done |

## 3. Directive (EU) 2019/882 — Annex I Section I general product requirements

Software is an EAA "product". The following **binding general accessibility requirements** apply (Annex I, Section I). Each is a concrete item to fulfil and test. Applicability notes in brackets.

| # | Requirement (Annex I Section I) | Autogram implication | Status |
| --- | --- | --- | --- |
| 3.1 | Information about the product (labels, instructions, warnings) is available via **more than one sensory channel**. | On-screen text + accessible description. | Not done |
| 3.2 | That information is **understandable and perceivable**. | Plain language, legible presentation. | Not done |
| 3.3 | **Suitable font/size, contrast, adjustable spacing** for information. | GUI theming, no fixed tiny fonts. | Not done |
| 3.4 | **Instructions for use** available via more than one sensory channel, understandable, perceivable, suitable font/contrast/spacing. | User guide (README-SK, help). | Not done |
| 3.5 | Instructions provide **text enabling alternative assistive formats**. | Machine-readable docs. | Not done |
| 3.6 | **Alternatives to non-text content** in instructions. | Alt text/images. | Not done |
| 3.7 | Instructions **describe the interface and each feature**. | Complete help. | Not done |
| 3.8 | Instructions **describe disability-oriented functionality**. | Accessibility feature documentation. | Not done |
| 3.9 | Instructions **describe assistive interfaces and tested devices**. | Tested-device matrix. | Not done |
| 3.10 | Interface/features allow **access, perception, operation, understanding and control**. | Governing design rule. | Not done |
| 3.11 | **Multi-sensory alternatives** to visual/auditory-only controls. | Keyboard + screen-reader paths. | Not done |
| 3.12 | **Alternative to speech/vocal input** where used. | Only if such input exists. | N/A check |
| 3.13 | **Magnification, brightness/contrast and assistive navigation**. | Zoom/high-contrast support. | Not done |
| 3.14 | **Alternative to colour-coded meaning/action**. | Badges must not rely on colour alone. | Not done |
| 3.15 | **Alternative to audible signals** where used. | Only if such signals exist. | N/A check |
| 3.16 | **Flexible improvement of visual clarity**. | Text scaling. | Not done |
| 3.17 | **Audio volume/speed/clarity control** where audio used. | Only if audio used. | N/A check |
| 3.18 | **Sequential/manual alternatives, no simultaneous controls, tactile discernibility** where manual operation used. | Keyboard-only operation. | Not done |
| 3.19 | **Avoid extensive reach/great strength**. | Pointer/keyboard ergonomics. | Not done |
| 3.20 | **Avoid photosensitive seizure triggers**. | No flashing content. | Not done |
| 3.21 | **Privacy when using accessibility features**. | Accessibility data handling. | Not done |
| 3.22 | **Alternative to biometric identification/control** where used. | Card/PIN alternatives. | N/A check |
| 3.23 | **Consistent functionality and flexible interaction time** (timeouts, error recovery). | No forced timeouts; recoverable errors. | Not done |
| 3.24 | **Software/hardware interfaces to assistive technology**. | Screen-reader/AT interoperability. | Not done |

If Autogram is treated as part of a **service**, Annex I Section II (service requirements) and Section III(a) (product used in a service) add corresponding requirements on information, service terms, and support; those should be assessed if the deployment is confirmed as a covered service.

---

## Evidence

- **User-confirmed (8 October 2026):** accessibility issues were raised at a hackathon. They are **reported** (as issues) and will be **fixed gradually**.
- **Status:** Article 15 is **applicable**; the requirement is **not yet fully met** while the tracked accessibility issues remain open.
- **To attach for the file:** the list of hackathon accessibility issues and their resolution progress.
- The checklist below is the criteria set; open items should be linked to the corresponding reported issues.

## What is covered today

- Accessibility findings were produced at a hackathon and filed as issues (user-confirmed).
- The remaining task is to fix them gradually and close each against the criteria below.

## Proposed way to close

1. Adopt an **accessibility statement** and a **target standard** (e.g. EN 301 549 / WCAG 2.2 AA) under Article 15's EAA reference.
2. Run a **keyboard-only and screen-reader pass** over the full flow (open → review → sign → validate).
3. Fix the language, contrast, font-size, focus-order and non-colour-cueing findings.
4. Record a **tested-device / assistive-technology matrix** and publish it with the instructions.
5. Re-run and close the checklist above.

See also: [EAA document](eu-dir-2019-882.md) · [VERIFICATION tracker](VERIFICATION.md).
