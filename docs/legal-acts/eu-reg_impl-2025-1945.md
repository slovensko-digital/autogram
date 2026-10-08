# Commission Implementing Regulation (EU) 2025/1945 — signature and seal validation

## Executive summary

This act specifies the adapted standards supporting the eIDAS presumption of compliance for validation of qualified signatures/seals (Annex I) and advanced signatures/seals based on qualified certificates (Annex II). It is directly relevant to Autogram's validation function as a technical reference; reliance on the presumption is **Conditional**, not an established universal certification duty on every software publisher. The organisation is not a QTSP and operates no hosted document-processing service. Those facts do **not** remove the annexes' express requirements for **signature validation applications**, including ETSI TS 119 101 conformity, when using this route.

Both annexes require a maximum **24-hour revocation freshness for the signing certificate**, except where the end-entity certificate represents a trust anchor; they prohibit setting freshness values for other certificates, including timestamp-supporting certificates. Failed applicability checks must stop the process, produce an **indeterminate** qualification result and be recorded with intermediate results. Annex II additionally replaces the final qualification rule and voids two requirements.

Inspected source establishes DSS integration, CRL/OCSP sources, trusted-list completeness checks and cautious UI presentation. It does not establish the complete adapted policy, ETSI TS 119 101 conformity or conformant applicability/validation reports. Overall fulfilment remains **Unknown**; missing proof is not a demonstrated statutory violation. Assessment/source access: **8 October 2026**; repository baseline **5ca91d5c**. Adopted **29 September 2025**, published **30 September 2025**, effective **20 October 2025**.

## Source and version

- Official complete English act, including both annexes: [EUR-Lex ELI](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj/eng), [OJ HTML](https://eur-lex.europa.eu/legal-content/EN/TXT/HTML/?uri=OJ:L_202501945), [authentic OJ PDF](https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=OJ%3AL_202501945).
- **CELEX 32025R1945**, **C/2025/6492**, OJ L, 2025/1945, 30.9.2025; ELI `http://data.europa.eu/eli/reg_impl/2025/1945/oj`. English is an official language version; tables below are engineering/legal paraphrases, not replacement legal text.
- Original published version, not a consolidation. EUR-Lex displayed **In force**. Article 2 provides entry on the twentieth day following publication: **20 October 2025**; no separate deferred application date appears in this act.
- Implements Regulation (EU) No 910/2014, Articles 32(3), 32a(3), 40 and 40a, in the framework amended by Regulation (EU) 2024/1183. It does not itself amend or repeal those acts. No amendment/repeal/corrigendum was identified in the obtained original text; a complete subsequent amendment-history check remains outstanding.
- Retrieval qualification: direct HTTP text/PDF requests returned empty responses or a WAF challenge. The official English page and OJ HTML were readable in the browser. Browser text output was truncated at the annex boundary; the complete annex wording, including footnotes, was additionally obtained from the official EUR-Lex search-index result and checked against the browser-visible structure. No authentic PDF was downloaded or independently verified. A third-party transcription was consulted only as a cross-check, not authority or evidence of completeness.

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or out of the confirmed scope; never "knowingly disregarded"). Every provision row links to the official text at that unit — act anchors `#art_1`, `#art_2`, `#anx_I`, `#anx_II` and recital anchors `#rct_1`–`#rct_6` on the [official OJ page](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj).

## Evidence key

All evidence below was inspected on 8 October 2026. Source inspection is not a released-binary audit or proof that tests pass; no builds or tests were run.

- **V — validation integration:** [SignatureValidator.java](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java), `validate`, lines 76–81, delegates to DSS `validateDocument()` without an explicit policy argument; `initialize`, lines 91–133, configures LOTL/country filtering, trusted certificates and online CRL/OCSP sources. [pom.xml](../../pom.xml), lines 18 and 43–46, selects DSS **6.5**. Neither dependency selection nor online fetching proves the effective freshness/qualification policy. The six-hour cache at lines 107–110 is a **trusted-list** cache, not proof of a signing-certificate revocation freshness setting.
- **T — trusted lists:** [SignatureValidator.java](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java), `TrustedListStatus.isComplete` / `getTrustedListStatus`, lines 273–301, requires valid LOTL and all selected countries. SignatureValidatorTrustedListTest.java, lines 19–58: `noLoadedListsAreNotConsideredValid`, `processedButInvalidListIsNotConsideredValid`, `allSelectedListsMustBeValidated`, `listWithoutValidatedLotlIsNotTrusted`, `allSelectedListsAndLotlValidated`. These are mocked status tests, not ETSI trusted-list parsing/history conformance tests. Eight countries remain selected by default; the shared 3 October 2026 SK TLv6/LOTL live result does not establish EU-wide or historical correctness.
- **U — presentation:** [GUIValidationUtils.java](../../src/main/java/digital/slovensko/autogram/ui/gui/GUIValidationUtils.java), `createSignatureBox`, lines 99–139, `createSignatureQualificationBadge`, lines 154–168, and `validityToString`, lines 171–193. Missing-list, revocation, failed and indeterminate states affect presentation; revocation detection at line 114 uses an error-message substring, not a 24-hour freshness calculation. SignatureValidationPresentationTest.java, lines 17–60: `indeterminateQualificationIsNotPresentedAsValidationInProgress`, `successfulCryptographicCheckIsNotEnoughWhenTrustedListIsMissing`, `indeterminateTimestampDoesNotProduceValidResult`, `qualifiedBadgeIsNotShownWithoutAllSelectedTrustedLists`. These tests check UI behaviour using synthetic/mocked inputs, not the TARC process.
- **R — reports:** [ValidationReports.java](../../src/main/java/digital/slovensko/autogram/core/ValidationReports.java), `DocumentReport`, lines 14–26, and `getDocumentReports` / `getSignatures`, lines 78–118, retain DSS reports per document/signature. [SignatureValidator.java](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java), `getSignatureValidationReport`, lines 145–163, and `getSignatureValidationReportBodyHTML`, lines 169–185, collect reports and render **simple-report** XML. No inspected fixture proves all applicability intermediate results or ETSI TS 119 102-2 report-schema conformity. The fast structural checks at lines 198–230 deliberately lack the trusted verifier and must not be treated as qualified validation.

## Provision-by-provision assessment

`Conditional` below means **when Autogram's validation process is used to claim the standards-based eIDAS presumption**. Technical relevance exists independently of whether the publisher operates a trust service. Dates marked “Effective” refer to 20 October 2025, not a separately imposed vendor implementation deadline. Parent amendment/clause labels are structural headings; their individually identifiable children carry the assessment.

### Preamble and recitals

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Preamble — institutional opening](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj) | Commission is the adopting institution. | None | Ignored | Official heading; no software duty. | Adopted 29 September 2025. |
| [Preamble — having-regard paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj) | TFEU authority. | Indirect | Ignored | Official preamble; legal basis, not an implementation task. | Original version. |
| [Preamble — having-regard paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj) | eIDAS Articles 32(3), 32a(3), 40 and 40a provide implementing authority. | Indirect | Ignored | Official preamble; distinguishes validation standards from qualified-service-provider authorisation. | Original version. |
| [Recital (1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#rct_1) | Validated signatures/seals support integrity, authenticity and certainty of identity for relying parties. | Indirect | Ignored | Interpretative purpose, not a separate product requirement. | No independent deadline. |
| [Recital (2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#rct_2) | Adapted recognised technical standards support the compliance presumption and additional validity/qualification controls. | Indirect | Ignored | Interpretative scope; V/T/U/R provide only partial evidence for the operative standards route. | Do not turn the presumption into an unconditional publisher duty. |
| [Recital (3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#rct_3) | Commission should review/update standards for technological developments. | None | Ignored | Commission task, not a software task. | Refers to Regulation 2024/1183 recital 75. |
| [Recital (4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#rct_4) | GDPR and, where relevant, ePrivacy rules apply to personal-data processing under this act. | Conditional | Unknown | Local certificate/revocation processing exists (V); publisher receives no user documents. Need actor-specific data-flow inventory, legal basis, retention and any support-data processing records. | Context only; substantive duties arise under the referenced acts. |
| [Recital (5)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#rct_5) | EDPS consultation and opinion. | None | Ignored | Official recital records the institutional consultation. | Opinion 6 June 2025. |
| [Recital (6)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#rct_6) | Measures accord with the eIDAS Article 48 committee opinion. | None | Ignored | Institutional procedure, not an application requirement. | Original version. |

### Articles and concluding formula

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 1(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#art_1) | Annex I identifies reference standards/specifications for qualified signature/seal validation under eIDAS Articles 32(3)/40. | Conditional | Unknown | V/T/U/R; need adopted validation-policy mapping and conformity evidence for Annex I. | Effective; qualified validation route. |
| [Article 1(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#art_1) | Annex II identifies reference standards/specifications for advanced signature/seal validation based on qualified certificates under Articles 32a(3)/40a. | Conditional | Unknown | V/T/U/R; need a separate Annex II qualification/policy mapping, not merely qualified-signature tests. | Effective; AdES/QC route. |
| [Article 2 — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#art_2) | Entry into force on twentieth day following OJ publication. | Indirect | Ignored | Official publication date and Article 2; commencement, not a software measure. | 20 October 2025. |
| [Closing binding formula — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#art_2) | Binding in entirety and directly applicable in all Member States. | Indirect | Ignored | Official concluding text; does not establish which presumption route the publisher claims. | Effective; no transposition required. |
| [Closing adoption/signature formula](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#art_2) | Brussels adoption and Commission President attribution. | None | Ignored | Official text; documentary metadata, not a separate duty. | 29 September 2025; Ursula von der Leyen. |

### Annex I — qualified electronic signatures and seals

#### Opening paragraph and reference specifications

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex I — unnumbered opening paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | Apply the two listed standards with this annex's adaptations for qualified signature/seal validation. | Conditional | Unknown | Need a combined standard/adaptation conformity matrix; using DSS alone is insufficient (V). | Effective; both standards apply, not alternatives. |
| [Annex I — reference specification / footnote (1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | ETSI TS 119 172-4 **V1.1.1 (2021-05)**: signature applicability rules/validation policy for European qualified signatures/seals using trusted lists. | Conditional | Unknown | V/T; need the exact external standard and qualification/TARC implementation assessment. | Separate work; fixed edition. |
| [Annex I — reference specification / footnote (2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | ETSI TS 119 102-2 **V1.4.1 (2023-06)**: signature validation report. | Conditional | Unknown | R; need schema-valid report fixtures and clause-by-clause report mapping. HTML simple reports alone do not establish conformity. | Separate work; no specific adaptation to this standard is enumerated here. |

#### Point 1 — ETSI TS 119 172-4; (1) clause 2.1 normative-reference adaptations

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(1) — reference [1]](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | Use ETSI EN 319 102-1 **V1.4.1 (2024-06)**, AdES creation/validation procedures, Part 1. | Conditional | Unknown | V; need validation-algorithm/edition mapping and conformance vectors. | Effective; reference update. |
| [I.1(1) — unnumbered substitution instruction](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | Interpret every reference to ETSI TS 119 102-1 [1] as ETSI EN 319 102-1 [1]. | Conditional | Unknown | Need policy and documentation reference audit; dependency version is not that audit. | Applies throughout the referenced policy. |
| [I.1(1) — reference [2]](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | Use ETSI TS 119 612 **V2.3.1 (2024-11)**, Trusted Lists. | Conditional | Unknown | T; selected-country status tests and shared SK live evidence do not establish complete edition conformity. Need parsing, historical-status and EU-wide vectors. | Effective; fixed edition. |
| [I.1(1) — reference [13]](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | Use ETSI TS 119 101 **V1.1.1 (2016-03)**, policy/security requirements for signature creation and validation applications. | Conditional | Unknown | Need application-level policy/security conformity matrix, not only library assurance. | Separate standard; expressly included. |

#### Point 1(2) — clause 4.2, REQ-4.2-03, X.509 constraints point c)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(2)(i)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | If the end-entity certificate represents a trust anchor, do not use RevocationCheckingConstraints. | Conditional | Unknown | V; need effective policy and a trust-anchor end-entity regression fixture demonstrating omission. | Effective; certificate-specific exception. |
| [I.1(2)(ii)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | Otherwise set RevocationCheckingConstraints to `eitherCheck`, as defined in ETSI TS 119 172-1 [3], A.4.2.1, table A.2 (m)2.1. | Conditional | Unknown | V shows online OCSP/CRL sources, not this constraint. Need policy dump and successful/failed OCSP and CRL vectors. | External definition is not reproduced in the act. |
| [I.1(2)(iii)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | If the end-entity certificate represents a trust anchor, do not use RevocationFreshnessConstraints defined in TS 119 172-1 [3], A.4.2.1, table A.2 (m)2.2. | Conditional | Unknown | Need anchor-specific freshness policy and fixture; V alone does not prove omission. | Effective; distinct from checking exception. |
| [I.1(2)(iv) — sentence 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | For a non-anchor end-entity certificate, use the defined freshness constraints with a maximum **24 hours for the signing certificate**. | Conditional | Unknown | V/U do not establish age enforcement. Need just-under/at/over-24-hour OCSP/CRL fixtures and effective default/custom policy evidence. | Not a trusted-list refresh interval or blanket daily fetch requirement. |
| [I.1(2)(iv) — sentence 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | Set no freshness-constraint value for certificates other than the signing certificate, including timestamp-supporting certificates. | Conditional | Unknown | Need chain/timestamp certificate policy inspection and tests proving no such value is set. | Do not extend the signing-certificate maximum to every chain certificate. |

#### Point 1(3) — clause 4.3, validation and applicability checking practices

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(3), REQ-4.3-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | Signature validation applications must comply with ETSI TS 119 101 [13]. | Conditional | Unknown | Need application-level security/policy assessment, operating assumptions and conformity evidence for desktop/API/CLI; DSS delegation and no-QTSP status do not discharge this requirement. | Express validation-application requirement within the presumption route. |

#### Point 1(4) — clause 4.4, technical applicability rules checking

Adapted **REQ-4.4.2-03**: when **any** check specified in REQ-4.4.2-01 fails, all three consequences below apply. The checks themselves are in the external standard, not restated by this act.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(4), REQ-4.4.2-03(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | Stop the applicability checking process. | Conditional | Unknown | V delegates execution; need instrumented negative TARC vectors showing the required stop, not merely a UI failure label. | Effective; failed applicability check, not every software error. |
| [I.1(4), REQ-4.4.2-03(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | Technically determine the signature as **indeterminate**, neither an EU qualified electronic signature nor an EU qualified electronic seal. | Conditional | Unknown | U proves presentation safeguards only. Need every failed-REQ-4.4.2-01 fixture's qualification result, for both signatures and seals. | Indeterminate is not a successful qualification or a blanket assertion of cryptographic invalidity. |
| [I.1(4), REQ-4.4.2-03(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_I) | Record that result and results of all intermediate processes in the signature applicability rules checking report. | Conditional | Unknown | R; need negative-check reports showing all intermediate results, traceability and report validation. | Simple HTML output is insufficient evidence. |

### Annex II — advanced electronic signatures and seals based on qualified certificates

This annex has its own assessments even where its wording duplicates Annex I. Its final suitability rule is for **AdES/QC**, not QES and not a requirement to prove QSCD use.

#### Opening paragraph and reference specifications

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex II — unnumbered opening paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | Apply both listed standards with Annex II adaptations for advanced signatures/seals based on qualified certificates. | Conditional | Unknown | Need a combined Annex II conformity matrix separate from qualified-validation claims. | Effective; both standards apply. |
| [Annex II — reference specification / footnote (1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | ETSI TS 119 172-4 **V1.1.1 (2021-05)**, applicability rules/validation policy using trusted lists, adapted here for AdES/QC. | Conditional | Unknown | V/T; need exact external policy and Annex II TARC mapping. | Separate work; do not infer QES from QC alone. |
| [Annex II — reference specification / footnote (2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | ETSI TS 119 102-2 **V1.4.1 (2023-06)**, signature validation report. | Conditional | Unknown | R; need schema-valid AdES/QC reports and complete clause mapping. | Separate work; no specific report-standard adaptation enumerated. |

#### Point 1 — ETSI TS 119 172-4; (1) clause 2.1 normative-reference adaptations

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [II.1(1) — reference [1]](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | ETSI EN 319 102-1 **V1.4.1 (2024-06)**, AdES creation/validation procedures, Part 1. | Conditional | Unknown | V; need edition-specific procedure mapping and AdES/QC validation vectors. | Effective. |
| [II.1(1) — unnumbered substitution instruction](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | Interpret all ETSI TS 119 102-1 [1] references as ETSI EN 319 102-1 [1]. | Conditional | Unknown | Need Annex II policy/documentation reference audit. | Applies throughout the referenced policy. |
| [II.1(1) — reference [2]](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | ETSI TS 119 612 **V2.3.1 (2024-11)**, Trusted Lists. | Conditional | Unknown | T; need complete parsing, status-history and territory coverage assessment, not selected-list completeness alone. | Effective; fixed edition. |
| [II.1(1) — reference [13]](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | ETSI TS 119 101 **V1.1.1 (2016-03)**, application policy/security requirements. | Conditional | Unknown | Need application conformity matrix; external standard not inspected in this assessment. | Validation applications expressly within scope. |

#### Point 1(2) — clause 4.2, REQ-4.2-03, X.509 constraints point (c)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [II.1(2)(i)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | For a trust-anchor end-entity certificate, do not use RevocationCheckingConstraints. | Conditional | Unknown | Need Annex II effective policy and anchor-specific check-omission fixture; V insufficient. | Effective. |
| [II.1(2)(ii)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | Otherwise use `eitherCheck`, defined by TS 119 172-1 [3], A.4.2.1, table A.2 (m)2.1. | Conditional | Unknown | V establishes CRL/OCSP configuration only; need constraint mapping and AdES/QC revocation vectors. | External definition, not reproduced here. |
| [II.1(2)(iii)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | For a trust-anchor end-entity certificate, do not use the specified RevocationFreshnessConstraints. | Conditional | Unknown | Need anchor-specific policy/fixtures under Annex II. | Same TS 119 172-1 table A.2 (m)2.2 definition. |
| [II.1(2)(iv) — sentence 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | For a non-anchor end-entity certificate, apply a maximum **24-hour signing-certificate freshness**. | Conditional | Unknown | Need effective policy and boundary-age OCSP/CRL fixtures for AdES/QC; V/U do not prove enforcement. | Effective; signing certificate only. |
| [II.1(2)(iv) — sentence 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | Set no freshness value for other certificates, including those supporting timestamps. | Conditional | Unknown | Need chain/timestamp policy inspection and regression fixtures. | Do not apply the 24-hour limit indiscriminately. |

#### Point 1(3) — clause 4.3, validation and applicability checking practices

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [II.1(3), REQ-4.3-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | Signature validation applications must comply with ETSI TS 119 101 [13]. | Conditional | Unknown | Need application-level policy/security conformity evidence covering the Annex II validation route and desktop/API/CLI use; no-QTSP status is not an exemption. | Effective; separate from creation-only analysis. |

#### Point 1(4) — clause 4.4, technical applicability rules checking

Adapted **REQ-4.4.2-03** is triggered by failure of **any** REQ-4.4.2-01 check. Adapted **REQ-4.4.2-06** applies at the specified point of TARC: its two conditions are cumulative.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [II.1(4), REQ-4.4.2-03(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | Stop the applicability process on a failed check. | Conditional | Unknown | V; need instrumented Annex II failed-check execution vectors. | Effective; no proof from UI tests. |
| [II.1(4), REQ-4.4.2-03(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | Determine **indeterminate**, neither an advanced signature based on an EU qualified certificate nor an advanced seal based on an EU qualified certificate. | Conditional | Unknown | U supplies only presentation evidence; need Annex II signature/seal negative qualification fixtures. | Distinct result category from Annex I. |
| [II.1(4), REQ-4.4.2-03(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | Report that result and every intermediate process result in the applicability rules checking report. | Conditional | Unknown | R; need complete Annex II negative-result applicability reports and validation evidence. | Effective. |
| [II.1(4), REQ-4.4.2-04](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | Void this referenced-standard requirement for Annex II. | Indirect | Ignored | Binding adaptation removes the requirement; do not import it unchanged as an AdES/QC duty. | No independent task; policy mapping still needed under the opening row. |
| [II.1(4), REQ-4.4.2-05](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | Void this referenced-standard requirement for Annex II. | Indirect | Ignored | Same removal, individually identified; no separate fulfilment claim. | Effective. |
| [II.1(4), REQ-4.4.2-06(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | Determine the signing certificate, at **best signature time**, to be an EU qualified certificate for signatures/respectively seals, as in REQ-4.4.2-02(a). | Conditional | Unknown | V/T; need historical trusted-service status, certificate qualification and best-signature-time fixtures for both certificate types. | Current TL loading alone does not prove historical qualification. |
| [II.1(4), REQ-4.4.2-06(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | The clause 4.2 process result must be **TOTAL-PASSED**. | Conditional | Unknown | V; need Annex II policy and passing/failing/indeterminate clause-4.2 fixtures. | Cumulative with condition (a). |
| [II.1(4), REQ-4.4.2-06 — unnumbered outcome paragraph](https://eur-lex.europa.eu/eli/reg_impl/2025/1945/oj#anx_II) | If both conditions hold, determine technical suitability for EU AdES/QC signature/respectively seal; otherwise do not determine suitability for either category. | Conditional | Unknown | Need full condition truth-table tests and report evidence; U's qualified-badge safeguards are not this decision rule. | Does not confer QES/QSeal status or substitute for legal assessment. |

## Coverage and limitations

- Enumerated independently from the official act, not copied from the existing compliance report: **6 recitals; 2 articles (Article 1's 2 numbered paragraphs and Article 2's 1 unnumbered paragraph); 1 binding closing paragraph; 1 adoption/signature formula; 3 preamble units; both complete annexes**. These produce **51 assessment rows**: 9 preamble/recital rows, 5 article/concluding rows, **16 Annex I rows** and **21 Annex II rows**.
- Each annex's sole numbered top-level point **1** and its four numbered adaptations **(1)–(4)** are structural headings, not duplicate duties. Every adapted normative entry **[1], [2], [13]**, the unnumbered reference-substitution instruction, all four roman-numbered revocation points (with both sentences of (iv) separated), REQ-4.3-02, and all three REQ-4.4.2-03 lettered consequences have individual rows. Annex II additionally enumerates the two void requirements and both lettered conditions plus the outcome paragraph of REQ-4.4.2-06. Trigger text is retained above the respective tables.
- Reference coverage: **4 primary specification entries across the annexes** (two in each opening/footnote pair), **6 adapted normative-reference entries** (three in each annex), and **2 substitution instructions**. TS 119 172-1 [3] is individually identified in the revocation rows where invoked, not invented as an additional normative-reference insertion. The annex footnotes supply the full titles/versions of the two primary specifications and are represented in their respective reference rows. The five preamble legislative footnotes are contextual citations represented with the relevant preamble/recital, not five new obligations.
- Official body text, both annexes and their footnotes were obtained through the combined browser/index access described above; no act provision was deliberately omitted. **The browser snapshot alone did not deliver the whole text**, and no downloadable complete official source file/PDF was obtained. The indexed annex text explicitly ends after point 1(4) in each annex: neither annex has a point 2 adapting TS 119 102-2. This access limitation must remain visible rather than claiming independent PDF verification.
- **None of the full external ETSI standards was inspected**. Their cited titles/versions and the act's adaptations were inspected; the full TS 119 172-4, TS 119 102-2, EN 319 102-1, TS 119 612, TS 119 101 and the invoked TS 119 172-1 definition remain separate works requiring a dedicated conformity assessment. Their unmodified clauses are not silently imported as text of this act.
- Outstanding: documented decision whether/where the presumption is claimed; exact effective DSS policy and edition mapping; trust-anchor and 24-hour boundary tests; historical qualification/best-signature-time and signature/seal TARC vectors; schema-valid complete validation/applicability reports; application-level TS 119 101 evidence; relevant actor/data-processing records; subsequent amendment-history verification. Inspected named tests establish narrow presentation/status intentions only. No builds, test execution, implementation changes, commits or organisational compliance assertions accompany this reading register.
