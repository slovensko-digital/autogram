# Commission Implementing Regulation (EU) 2015/806 — EU trust mark for qualified trust services

## Executive summary

This act specifies the **official EU trust mark**, not an ordinary application logo or every graphic containing EU stars. Its operative design/use rules are **Conditional** here: they matter if the official mark is displayed in the assessed application/process. The organisation is **not a qualified trust-service provider (QTSP)**; this act does not confer qualified status or require Autogram to display a mark.

Inspection found a trusted-list browser `/trustmark` **link**, not evidence of displaying the official mark. The inspected Autogram icon and EU funding banner are visually different from the Annex designs. Whether the official mark is used anywhere in released desktop/API/CLI outputs or associated distribution material remains **Unknown**. No statutory failure or complete compliance is established.

Assessment/source access: **8 October 2026**; repository baseline: **5ca91d5c**. Adopted **22 May 2015**, published **23 May 2015**, entered into force **12 June 2015**. This is an engineering/legal reading aid, not legal advice or a released-binary audit; shared scope and vocabulary are in [README](README.md).

## Source and version

- Identifier: **CELEX 32015R0806**; ELI: `http://data.europa.eu/eli/reg_impl/2015/806/oj`.
- Official EU complete-text links: [EUR-Lex English text](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:32015R0806), [English OJ PDF](https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=CELEX:32015R0806), [ELI English original](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj/eng).
- **Text actually obtained:** the complete English **Official Journal L 128, 23 May 2015, pp. 13–15**, including the visual Annexes, from the [OJ PDF preserved by the UK National Archives](https://www.legislation.gov.uk/eur/2015/806/pdfs/eur_20150806_adopted_en.pdf). This is a copy of the published EU act, not the UK-revised text. Direct EUR-Lex retrieval returned empty content or a bot challenge; it was not treated as successful access.
- Version assessed: **original as adopted/published**, no consolidation used; consolidation date: not applicable to this reading. The complete legal text is linked, not reproduced.
- Entry into force is calculated from Article 5: twentieth day following publication, **12 June 2015**. The act contains **no separate application-date provision**. Eligibility and timing of use must also be read under the applicable version of Regulation (EU) No 910/2014, especially Article 23; this design act's commencement is not itself evidence of eligibility.
- Legal basis/relationship: implements **Article 23(3) of Regulation (EU) No 910/2014**; Article 4 preserves the Article 23(2) trusted-list link. It does not amend or repeal that base act. No amending/repealing act is identified in the obtained original; **current EU amendment/repeal/corrigendum history remains unverified** because current EUR-Lex metadata was inaccessible. The UK site's “revoked” label concerns the UK version and must not be read as EU repeal evidence.
- Language: English official published text. Source-access and assessment date: **8 October 2026**. Repository baseline: **5ca91d5c**; inspected working-tree files are evidence of source behaviour, not attestation of a released binary.

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or out of the confirmed scope; never "knowingly disregarded"). Every provision row links to the official text at that unit (act anchors `#art_N`, `#anx_R`, recitals `#rct_N`); structural/preamble labels link to the [act page](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj).

## Repository evidence and unresolved trigger

1. [`simple-report-bootstrap4.xslt`](../../src/main/resources/digital/slovensko/autogram/core/simple-report-bootstrap4.xslt), `dss:TrustAnchor` template, **lines 542–589**, constructs a provider URL using `/trustmark` (**550–551**) and renders provider-name text as a “View in TL Browser” hyperlink (**568–575**). This is a reference to an external trusted-list browser; the inspected template does not turn that link into an official-mark image. It is not proof that the provider's service is qualified or that every link resolves correctly.
2. [`about-dialog.fxml`](../../src/main/resources/digital/slovensko/autogram/ui/gui/about-dialog.fxml), About dialog image block, **lines 48–59**, references [`logo_OPII_ESIF.png`](../../src/main/resources/digital/slovensko/autogram/ui/gui/logo_OPII_ESIF.png). Visual inspection shows an EU flag and regional-development/funding text, **not** the Annex padlock/check-mark design.
3. Visual inspection of [`Autogram.png`](../../src/main/resources/digital/slovensko/autogram/ui/gui/Autogram.png) shows an “A” application icon, **not** the official mark. No inference from the name “logo”, EU colours, or stars establishes trust-mark use.
4. Searches of `src` for trust-mark/eIDAS wording and enumeration of image/FXML resources support only the inspected paths. **Overall official-mark use remains Unknown**: obtain released-package asset inventory, rendered validation reports/screenshots, desktop screenshots and distribution/marketing artwork inventory with an owner decision identifying each mark and the service it represents. If the official mark is used, also obtain the depicted provider/service's qualified-status evidence, trusted-list entry and actual link behaviour. Being a non-QTSP is not an entitlement to adopt this mark as Autogram branding.

## Provision-by-provision assessment

### Preamble and recitals — interpretative context, not software tasks

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Preamble, unnumbered legal-basis paragraphs 1–2](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj) | Commission cites the TFEU and Regulation 910/2014, Article 23(3), as authority. | Indirect | Ignored | Published OJ p. 13; authority/context, not an implementation measure. | Adopted 22 May 2015. |
| [Recital (1)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#rct_1) | Explains optional QTSP use and differentiation of qualified from other trust services to foster transparency/confidence. | Indirect | Ignored | Non-QTSP publisher fact; no qualification follows from software signing or branding. | Interpretative, not a duty to display a mark. |
| [Recital (2)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#rct_2) | Records art/design competition, public consultation and selection of the official design. | None | Ignored | Historical Commission design-selection process, not a publisher task. | Consultation 14 October–14 November 2014. |
| [Recital (3)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#rct_3) | Explains collective-mark registration in the UK and intended Union/international registrations. | Indirect | Ignored | Historical rationale only; current registration/licensing position would require official register extracts, not assumptions from this recital. | Statements as published in 2015. |
| [Recital (4)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#rct_4) | Records conformity with the opinion of the Article 48 committee. | None | Ignored | Commission adoption procedure; not application development. | 2015 adoption context. |

### Articles — form, permitted variants, size and identification

For Conditional rows, the unresolved trigger is **actual use of the official mark**; these statuses do not allege a missing duty to introduce it. Additional graphical/textual elements are optional, not mandatory features.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 1, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#art_1) | Official mark must have the Annex I/II form, subject to Article 2; addresses mark use. | Conditional | Unknown | Evidence items 1–4; inventory and visual comparison of any actual official mark to Annex forms needed. | Not a requirement to redesign the Autogram icon. |
| [Article 2(1)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#art_2) | Reference colours: Pantone **654 and 116**; four-colour blue **100% cyan, 78% magenta, 25% yellow, 9% black**, yellow **19% magenta, 95% yellow**; RGB blue **43, 67, 117**, yellow **243, 202, 18**. | Conditional | Unknown | If official mark used, inspect original artwork colour profile/values and rendered outputs. Ordinary icon/funding-banner colours do not trigger this rule. | Alternative colour systems within one numbered paragraph; no lettered points. |
| [Article 2(2)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#art_2) | Black-and-white Annex II use permitted only where colour is not practical. | Conditional | Unknown | Need actual monochrome-use inventory and documented practical reason, if used. | Permission constrained by condition, not a monochrome-output mandate. |
| [Article 2(3)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#art_2) | On a dark background, negative format using the same background colour is permitted as shown in Annexes I/II. | Conditional | Unknown | Need screenshots/artwork of actual mark on dark backgrounds and comparison to negative variants. | Optional variant, not duty to support dark mode. |
| [Article 2(4)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#art_2) | Where a coloured background makes the colour mark difficult to see, an outer delimiting line may improve contrast. | Conditional | Unknown | Need actual background/contrast examples if this optional variant is used. | Optional outline; no independent accessibility-conformity finding. |
| [Article 3, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#art_3) | Minimum size must preserve visual attributes/key forms and be no smaller than **64 × 85 pixels 150 dpi**. | Conditional | Unknown | Need measured actual-mark artwork and rendering/print settings in each relevant output. No measurement of an unrelated logo establishes this requirement. | Preserve the source's pixel/dpi wording; no invented scaling formula. |
| [Article 4, unnumbered paragraph 1, sentence 1](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#art_4) | Use must clearly identify the qualified services to which the mark pertains. | Conditional | Unknown | Need rendered mark/service context and the identified service's current qualified-status/trusted-list evidence. The external provider-name link alone is insufficient. | Service-specific, not a general quality badge for Autogram. |
| [Article 4, unnumbered paragraph 1, sentence 2](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#art_4) | Graphical/textual association may identify the qualified services only if it neither changes the mark's nature nor alters the Article 23(2) link to applicable trusted lists. | Conditional | Unknown | Need accompanying artwork/text and tested target/link to the relevant trusted-list service; template evidence shows only construction of a provider link, not fulfilment by a displayed mark. | One paragraph, two individually identifiable specifications; base-act use conditions remain separate. |
| [Article 5, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#art_5) | Entry into force on the twentieth day after OJ publication. | Indirect | Ignored | OJ publication date and commencement calculation; not a software task. | Published 23 May 2015 → **12 June 2015**. No separate application date in this act. |
| [Final binding clause, unnumbered paragraph 1 (after Article 5)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj) | Regulation binding in its entirety and directly applicable in all Member States. | Indirect | Ignored | Legal effect of regulation, not proof all provisions apply to this non-QTSP publisher. | No implementation deadline created by this clause. |
| [Signature/date block](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj) | Commission adoption at Brussels and President Jean-Claude Juncker's authentication. | None | Ignored | OJ p. 14; non-operative publication/authentication block. | **22 May 2015**. |

### Annex I — colour form

The Annex has a heading and **two unnumbered graphical specimens**, not numbered points or letters. Identifiers below are assessment labels for the two shown forms; the published artwork remains the reference, not these descriptive summaries.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex I, unnumbered graphical specimen 1 (left, positive)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#anx_I) | Colour padlock form with surrounding stars and a check mark; preserve the published form under Articles 1–3. | Conditional | Unknown | OJ p. 15 visually inspected; compare any actual official-mark asset/rendering, not the unrelated inspected icon/banner. | Blue padlock, white stars and yellow check. Colours governed by Article 2(1). |
| [Annex I, unnumbered graphical specimen 2 (right, negative)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#anx_I) | Negative colour form shown against a dark field, subject to Article 2(3). | Conditional | Unknown | OJ p. 15 visually inspected; need actual dark-background artwork if this variant is used. | White padlock, dark stars and yellow check; optional background-dependent variant. |

### Annex II — black-and-white form

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex II, unnumbered graphical specimen 1 (left, positive)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#anx_II) | Black-and-white padlock/stars/check form, allowed subject to Article 2(2). | Conditional | Unknown | OJ p. 15 visually inspected; need actual monochrome asset/rendering and impracticality-of-colour evidence. | Black padlock with white stars/check. |
| [Annex II, unnumbered graphical specimen 2 (right, negative)](https://eur-lex.europa.eu/eli/reg_impl/2015/806/oj#anx_II) | Negative black-and-white form on dark field, subject to Articles 2(2) and 2(3). | Conditional | Unknown | OJ p. 15 visually inspected; need actual negative monochrome artwork and both triggering conditions. | White padlock with black stars/check on dark field. |

## Coverage and limitations

- **Complete original published text obtained:** all three English OJ pages, including preamble, **recitals (1)–(4)**, **Articles 1–5**, **Article 2(1)–(4)**, all unnumbered operative paragraphs, the final binding clause, adoption/signature block and **both Annexes with all four graphical specimens**. Structure was checked against the PDF's page text and the rendered Annex page, not inferred from an older report or search snippets. Article 4's two sentences are separately assessed without inventing numbered paragraphs. No lettered subpoints, definitions, chapters, amendment blocks or transitional provisions appear in this original act.
- No original-act provisions or Annex images were omitted/unavailable in the recovered OJ copy. **Current EU version history, amendments, corrigenda and repeal status were not independently established**; the blocked direct EUR-Lex source is a remaining verification limit, not a reason to fabricate a consolidated version. UK-revised content was not substituted for EU law.
- No ETSI standard is incorporated or cited by this act; **no ETSI standards were inspected for this document**. Pantone values are specified by the act; no external Pantone manual, logo user manual, competition specifications or trademark register was inspected. Those separate works are not silently imported as statutory requirements.
- Repository inspection was targeted, not exhaustive visual/runtime or released-binary testing. No live report, external trusted-list browser page, public website or marketing/distribution campaign was audited. The `/trustmark` URL is not evidence that Autogram displays an official mark. The two inspected raster images do not prove absence everywhere else.
- Outstanding evidence: released desktop/API/CLI output and package inventories; screenshots/reports and distribution artwork; explicit asset/branding-owner decision on official-mark use; if used, eligible provider/service identification, authoritative qualified-status/trusted-list record, link tests, source artwork colours/dimensions/contrast and monochrome-use reasons. No row is marked **Done** or **Not done** on the basis of the unrelated application/funding logos.
