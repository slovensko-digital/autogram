# Commission Implementing Regulation (EU) 2025/1942 — reference standards for qualified validation services

## Executive summary

This act fixes the reference standards and specifications that support the eIDAS presumption of compliance for **qualified validation services for qualified electronic signatures** and **qualified validation services for qualified electronic seals** under Article 33(2) and Article 40 of Regulation (EU) No 910/2014. It adapts two external standards — ETSI TS 119 441 V1.2.1 (2023-10), policy requirements for TSPs providing signature validation services, and ETSI TS 119 172-4 V1.1.1 (2021-05), the signature-applicability/validation policy for European qualified signatures and seals using trusted lists — by inserting, replacing or voiding individual clauses. The addressee is the **trust service provider offering a qualified validation service** (the SVSP, and the QSVSP where the service is qualified); supervisory bodies apply the presumption when granting or confirming qualified status.

The publisher (Autogram, published under its own name) is **not a qualified trust-service provider and operates no Autogram service** that receives users' documents or personal data. The local repository investigated here is the desktop application plus its local HTTP API and CLI. Under those confirmed facts most of this act's organisational clauses address **other actors** and are classified **None / Ignored**; the act does not by itself create a service-authorisation or practice-statement duty for the publisher. Recent code review establishes a real but limited validation capability: the desktop GUI performs local DSS validation with EU trusted lists and online CRL/OCSP sources, while the CLI and local API only **sign** and run fast structural checks.

The decisive distinction is that **a local DSS validation report is not a qualified validation service**. A desktop programme that a user runs locally, producing an unsigned DSS simple report, does not itself offer a qualified validation service to relying parties and does not attract the Article 33(2)/Article 40 presumption or the SVSP obligations attached to it. Where an application-level, validation-process or validation-report requirement would bite only if Autogram were used inside a qualified validation service or to claim the standards-based presumption, relevance is **Conditional**, not Direct; status is **Unknown** because no conformity evidence (policy mapping, TARC reports, schema-valid reports, application-level TS 119 101 assessment) was found. These are indirect technical gaps; they are not asserted as statutory violations by a non-QTSP.

Inspected evidence shows DSS 6.5 integration, trusted-list completeness checks, CRL/OCSP sources and cautious UI qualification presentation. It does **not** show an explicit effective validation policy argument, a 24-hour signing-certificate revocation-freshness setting, a TS 119 172-4 applicability/TARC process, a TS 119 102-2 conformant report, or compliance with ETSI TS 119 101 for the application. Overall fulfilment remains **Unknown**; missing proof is not a demonstrated violation. Assessment/source access: **8 October 2026**; repository baseline **5ca91d5c**. Adopted **29 September 2025**, published **30 September 2025**, effective **20 October 2025**.

## Source and version

- Official complete English act, including the Annex and its footnotes: [EUR-Lex ELI](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj/eng), [OJ HTML](https://eur-lex.europa.eu/legal-content/EN/TXT/HTML/?uri=OJ:L_202501942), [authentic OJ PDF](https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=OJ%3AL_202501942).
- **CELEX 32025R1942**, OJ L, 2025/1942, 30.9.2025; ELI `http://data.europa.eu/eli/reg_impl/2025/1942/oj`. English is an official language version; tables below are engineering/legal paraphrases, not replacement legal text.
- Title as published: “Commission Implementing Regulation (EU) 2025/1942 of 29 September 2025 laying down rules for the application of Regulation (EU) No 910/2014 of the European Parliament and of the Council as regards qualified validation services for qualified electronic signatures and qualified validation services for qualified electronic seals”.
- Original published version, not a consolidation. EUR-Lex displayed **In force**. Article 2 provides entry into force on the twentieth day following publication: **20 October 2025**; no separate deferred application date appears.
- Implements Regulation (EU) No 910/2014, Article 33(2) and Article 40, in the framework amended by Regulation (EU) 2024/1183. It does not itself amend or repeal those acts. No amendment/repeal/corrigendum was identified in the obtained original text; a complete subsequent amendment-history check remains outstanding.
- Retrieval: the official English text was retrieved as Cellar XHTML via `https://publications.europa.eu/resource/celex/32025R1942?language=eng` (HTTP 200, 102 860 bytes, `L_202501942EN.000101.fmx.xml`, generated 20250929-1657). The full body language, both adaptation points and all footnotes were present in that single source. Direct browser/HTML requests to the EUR-Lex ELI and legal-content pages returned a WAF challenge (HTTP 202, empty body), so **no separate rendered page or authentic PDF was downloaded**.
- **Anchor scheme.** Provision links use the EUR-Lex website's own anchors on `https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj`: recitals `#rct_1`–`#rct_7`, articles `#art_1`, `#art_2`, and the single Annex `#anx_1`. This is the scheme required by [README.md](README.md) and used by the sibling [EU 2025/1945](eu-reg_impl-2025-1945.md) document. It could **not** be re-verified against a rendered EUR-Lex page in this session (the site returned a WAF challenge); the retrieved Cellar XHTML carries internal ids (for example `d1e42-1-1`, `tit_1`), not these website anchors. Anchors are therefore functionally indicative of the unit, not byte-verified against the live page.
- Source-access date **8 October 2026**; repository baseline **5ca91d5c** (`5ca91d5ceba5b49f2c6d348a2a7b2503f1414526`, 3 October 2026).

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or out of the confirmed scope; never “knowingly disregarded”). Every provision row links to the official text at that unit — act anchors `#art_1`, `#art_2`, `#anx_1` and recital anchors `#rct_1`–`#rct_7` on the [official page](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj). Because this act has a single unnumbered Annex with two numbered adaptation points, all Annex rows link to `#anx_1`; each row's `Provision` cell names the exact nested point so it remains individually identifiable. Relevance and status are independent per [README.md](README.md).

`Conditional` below means: the clause becomes relevant only if the publisher's software is deployed within a service that claims the Article 33(2)/Article 40 presumption, or is otherwise treated as a qualified validation service — an unresolved role. It never means an obligation is currently established. `None / Ignored` marks an obligation on a trust-service provider/supervisory body, a definition, a voided clause, or a purely contextual recital. Recitals are interpretative and are not software tasks.

## Evidence key

All evidence below was inspected on 8 October 2026. Source inspection is not a released-binary audit or proof that tests pass; no builds or tests were run.

- **V — validation integration:** [SignatureValidator.java](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java), `validate`, lines 76–81, delegates to DSS `validateDocument()` **without an explicit policy argument**; `initialize`, lines 91–133, configures the EU LOTL source, a country-code predicate, trusted certificates from the lists and online `OnlineCRLSource`/`OnlineOCSPSource`; the six-hour cache at lines 107–110 is a **trusted-list** cache, not a signing-certificate revocation-freshness setting. [pom.xml](../../pom.xml), line 18 and lines 44–46 and 91–92, selects DSS **6.5** (`dss-bom`, `dss-validation`). Dependency selection and online fetching do not prove any effective freshness, qualification or applicability policy.
- **T — trusted lists:** [SignatureValidator.java](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java), `TrustedListStatus.isComplete` and `getTrustedListStatus`, lines 277–302, requires a valid LOTL and all selected countries. SignatureValidatorTrustedListTest.java, lines 19–58, mocks list status only (`noLoadedListsAreNotConsideredValid`, `processedButInvalidListIsNotConsideredValid`, `allSelectedListsMustBeValidated`, `listWithoutValidatedLotlIsNotTrusted`, `allSelectedListsAndLotlValidated`); it is not an ETSI trusted-list parsing or historical-status conformance test.
- **U — presentation:** [GUIValidationUtils.java](../../src/main/java/digital/slovensko/autogram/ui/gui/GUIValidationUtils.java), `createSignatureBox`, lines 99–144, `createSignatureQualificationBadge`, lines 154–169, and `validityToString`, lines 171–194; revocation detection at line 114 uses an error-message substring, not a 24-hour freshness calculation. SignatureValidationPresentationTest.java, lines 17–60, checks narrow UI behaviour with synthetic/mocked inputs (`indeterminateQualificationIsNotPresentedAsValidationInProgress`, `successfulCryptographicCheckIsNotEnoughWhenTrustedListIsMissing`, `indeterminateTimestampDoesNotProduceValidResult`, `qualifiedBadgeIsNotShownWithoutAllSelectedTrustedLists`).
- **R — reports:** [ValidationReports.java](../../src/main/java/digital/slovensko/autogram/core/ValidationReports.java), `DocumentReport`, lines 14–26, and `getDocumentReports` / `getSignatures`, lines 78–119. [SignatureValidator.java](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java), `getSignatureValidationReport`, lines 145–163, and `getSignatureValidationReportBodyHTML`, lines 169–185, collect DSS reports and render **simple-report** XML through an XSLT template. No inspected fixture proves a TS 119 102-2 report schema, an applicability-rules checking report or intermediate-result recording.
- **A — application/service surface:** [Autogram.java](../../src/main/java/digital/slovensko/autogram/core/Autogram.java), `checkAndValidateSignatures`, lines 47–60, and `initializeSignatureValidator`, lines 275–285; only [GUI.java](../../src/main/java/digital/slovensko/autogram/ui/gui/GUI.java), line 367, invokes full validation. [CliUI.java](../../src/main/java/digital/slovensko/autogram/ui/cli/CliUI.java), lines 222–230, has empty `onSignatureValidationCompleted` / `onSignatureCheckCompleted`; [CliApp.java](../../src/main/java/digital/slovensko/autogram/ui/cli/CliApp.java), lines 20–64, only signs. [AutogramServer.java](../../src/main/java/digital/slovensko/autogram/server/AutogramServer.java), lines 35–72, exposes info, certificates, sign and batch — **no validation endpoint**. [ServerSigningParameters.java](../../src/main/java/digital/slovensko/autogram/server/dto/ServerSigningParameters.java), `resolveSignatureLevel`, lines 293–311, calls `getSignedDocumentSimpleReport`, which sets an empty `CommonCertificateVerifier` and performs only a fast structural check ([SignatureValidator.java](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java), lines 198–231). These facts support the finding that no operated qualified validation service exists.

## Provision-by-provision assessment

### Preamble and recitals

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Preamble — institutional opening](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj) | Commission is the adopting institution. | None | Ignored | Official heading; no software duty. | Adopted 29 September 2025. |
| [Preamble — having-regard paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj) | TFEU authority. | Indirect | Ignored | Official preamble; legal basis, not an implementation task. | Original version. |
| [Preamble — having-regard paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj) | eIDAS Article 33(2) and Article 40 provide implementing authority. | Indirect | Ignored | Official preamble; frames the act as standards for qualified validation services, not for local desktop validation. | Original version. |
| [Recital (1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#rct_1) | Qualified validation services ensure integrity, authenticity and correctness of the validation process and results and ease the paper-to-digital transition. | Indirect | Ignored | Interpretative purpose; describes a trust service, not the desktop validator. | No independent deadline. |
| [Recital (2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#rct_2) | The Article 33(2)/40 presumption should apply only where qualified validation services comply with the standards set out in this Regulation, adapted to add security/trustworthiness controls and qualification/validity verification. | Indirect | Ignored | Interpretative scope; a local DSS report is not a qualified validation service (A), so the presumption is not engaged merely by validating locally. | Do not convert the presumption into an unconditional publisher duty. |
| [Recital (3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#rct_3) | Supervisory bodies should presume compliance from the Annex, but a QTSP may rely on other practices. | Indirect | Ignored | Interpretative; the presumption and its administration address supervisory bodies and QTSPs. | Refines the presumption route. |
| [Recital (4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#rct_4) | The Commission should review/update this Regulation for technological developments. | None | Ignored | Commission task, not a software task. | Refers to Regulation 2024/1183 recital 75. |
| [Recital (5)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#rct_5) | GDPR and, where relevant, the ePrivacy Directive apply to personal-data processing under this Regulation. | Conditional | Unknown | Local certificate/revocation processing exists (V); the publisher receives no user documents. Need an actor-specific data-flow inventory, legal basis, retention and any support-data processing records. | Context only; substantive duties arise under the referenced acts. |
| [Recital (6)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#rct_6) | EDPS consultation and opinion. | None | Ignored | Official recital records the institutional consultation. | Opinion 6 June 2025. |
| [Recital (7)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#rct_7) | Measures accord with the eIDAS Article 48 committee opinion. | None | Ignored | Institutional procedure, not an application requirement. | Original version. |

### [Articles](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#art_1) and concluding formula

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 1 — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#art_1) | The reference standards and specifications referred to in Article 33(2) and Article 40 of Regulation (EU) No 910/2014 are set out in the Annex. | Conditional | Unknown | V/T/U/R; need an adopted validation-policy mapping and conformity evidence against the Annex if the presumption route is claimed. Not engaged by a local DSS report alone. | Effective; qualified-validation-service route. |
| [Article 2 — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#art_2) | Entry into force on the twentieth day following OJ publication. | Indirect | Ignored | Official publication date and Article 2; commencement, not a software measure. | 20 October 2025. |
| [Article 2 — unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#art_2) | Binding in its entirety and directly applicable in all Member States. | Indirect | Ignored | Official concluding text; establishes direct applicability, not which presumption route the publisher claims. | Effective; no transposition required. |
| [Closing adoption/signature formula](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#art_2) | Brussels adoption and Commission President attribution. | None | Ignored | Official text; documentary metadata, not a separate duty. | 29 September 2025; Ursula von der Leyen. |

### Annex — ETSI TS 119 441 and ETSI TS 119 172-4 adaptations

#### Opening paragraph and reference specifications

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex — unnumbered opening paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI TS 119 441 V1.2.1 (2023-10) and ETSI TS 119 172-4 V1.1.1 (2021-05) apply with the following adaptations. | Conditional | Unknown | Need a combined standard/adaptation conformity matrix; using DSS alone is insufficient (V). Both standards apply together, not as alternatives. | Effective; both adaptations apply. |
| [Annex — reference specification / footnote (1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI TS 119 441 V1.2.1 (2023-10): policy requirements for TSPs providing signature validation services. | Conditional | Unknown | Need the exact external standard and an SVA/SVSP conformity assessment; the standard binds a service provisioning route the publisher does not operate. | Separate work; fixed edition. |
| [Annex — reference specification / footnote (2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI TS 119 172-4 V1.1.1 (2021-05): signature applicability rules (validation policy) for European qualified signatures/seals using trusted lists. | Conditional | Unknown | V/T; need the exact external policy and a TARC/qualification implementation assessment under this act's adaptation. | Separate work; fixed edition. |

#### Point 1 — adaptations to ETSI TS 119 441

#### 1(1) — clause 2.1 normative references

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(1) — reference [1]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI TS 119 101 V1.1.1 (2016-03), policy and security requirements for applications for signature creation and validation. | Indirect | Ignored | Bibliographic normative reference; its operative application duty appears at VPR-8.1-07 and 1(9)/OVR-7.7-03. | Fixed edition. |
| [1(1) — reference [2]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI EN 319 401 V3.1.1 (2024-06), general policy requirements for trust service providers. | None | Ignored | TSP-level standard; publisher is not a TSP. | External work, cited, not reproduced. |
| [1(1) — reference [3]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI EN 319 102-1 V1.4.1 (2024-06), AdES creation and validation procedures, Part 1. | Indirect | Ignored | Bibliographic; the validation-algorithm edition mapping is required by the invoked policies (V). | Fixed edition. |
| [1(1) — reference [4]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ISO/IEC 15408-1:2022, evaluation criteria for IT security. | Indirect | Ignored | Security-evaluation reference invoked by OVR-7.5-03. | Fixed edition. |
| [1(1) — reference [5]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Void. | Indirect | Ignored | Binding adaptation removes the reference; no duty remains in its place. | Removed by this act. |
| [1(1) — reference [6]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | FIPS PUB 140-3 (2019), security requirements for cryptographic modules. | Indirect | Ignored | Invoked by OVR-7.5-03(c) with a 31 December 2030 sunset. | Fixed edition. |
| [1(1) — reference [7]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Commission Implementing Regulation (EU) 2024/482 (EUCC). | Indirect | Ignored | Invoked by OVR-7.5-03(b) for device certification. | Fixed act. |
| [1(1) — reference [8]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI TS 119 172-4 V1.1.1 (2021-05), signature applicability rules/validation policy. | Conditional | Unknown | Cross-reference to the second standard adapted at Point 2; obligation carried by 1(13)/VPR-8.1-11 and 1(17)/VPR-B-02. | Fixed edition; not independently assessed here. |
| [1(1) — reference [9]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI TS 119 102-2 V1.4.1 (2023-06), signature validation report. | Conditional | Unknown | Report standard invoked by SVR-8.4-02 and SVR-8.4-17; R shows only simple-report XML. | Fixed edition; separate work. |
| [1(1) — reference [10]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ENISA European Cybersecurity Certification Group, “Agreed Cryptographic Mechanisms”. | Indirect | Ignored | Invoked by VPR-B-16 for a QSVSP. | External publication. |
| [1(1) — reference [11]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Commission Implementing Regulation (EU) 2024/3144 amending Implementing Regulation (EU) 2024/482. | Indirect | Ignored | Amending reference for the EUCC scheme. | Fixed act. |
| [1(1) — reference [12]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI EN 319 411-1, policy and security requirements for TSPs issuing certificates, Part 1. | Indirect | Ignored | Certificate-policy reference; publisher issues no certificates. | External work. |

#### 1(2) — clause 2.2 informative references

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(2) — reference [i.11]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Void. | Indirect | Ignored | Binding adaptation removes the informative reference. | Removed by this act. |

#### 1(3) — clause 3.3 abbreviations

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(3) — abbreviation EUCC](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | “EUCC” means the European Common Criteria-based cybersecurity certification scheme. | None | Ignored | Definition, not a requirement. | Context only. |

#### 1(4) — clause 4.3.3 Process note

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(4) — NOTE 10](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | See ETSI EN 319 102-1 [3] for guidance and ETSI TS 119 172-4 [8] for additional guidance for the EU qualified signature/seal case. | Indirect | Ignored | Guidance note; no separate software duty. | Interpretative. |

#### 1(5) — clause 6.1, SVS practice statement

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(5) — OVR-6.1-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The SVS practice statement shall be structured as per Annex A. | None | Ignored | Addressee is an SVS provider with a practice statement; publisher operates no service. | Service-provider duty. |
| [1(5) — OVR-6.1-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The SVS practice statement shall list or reference the supported SVS policies (for example via OIDs) and briefly describe them. | None | Ignored | No SVS practice statement or published policy set exists in scope. | Service-provider duty. |

#### 1(6) — clause 6.3, information security policy

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(6) — OVR-6.3-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The security policy shall document the security and privacy controls implemented to protect personal data. | None | Ignored | Addressed to an SVS provider's security policy; publisher operates no service. | Service-provider duty. |

#### 1(7) — clause 7.2, human resources

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(7) — OVR-7.2-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | SVSP personnel in trusted roles (and subcontractors) shall have expert knowledge, experience and qualifications through training/credentials or experience. | None | Ignored | Addressed to an SVS provider's personnel in trusted roles. | Service-provider duty. |
| [1(7) — OVR-7.2-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Compliance with OVR-7.2-02 includes at least 12-monthly updates on threats and current security practices. | None | Ignored | Personnel-training cadence for an SVS provider. | Service-provider duty. |

#### 1(8) — clause 7.5, cryptographic controls

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(8) — OVR-7.5-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] When validation reports are signed, the SVSP public signing certificate shall be issued under the NCP+ certificate policy of ETSI EN 319 411-1 [12]. | None | Ignored | Autogram produces unsigned DSS simple reports (R) and holds no SVSP report-signing certificate. | Conditional on signed reports and a service. |
| [1(8) — OVR-7.5-03 introductory sentence](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] When validation reports are signed, the SVSP private signing key shall be held in a secure cryptographic device certified under (a), (b) or (c). | None | Ignored | No SVSP private signing key exists in scope. | Conditional on signed reports and a service. |
| [1(8) — OVR-7.5-03(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Common Criteria (ISO/IEC 15408 [4] or CC:2022) certified to EAL 4 or higher. | None | Ignored | Sub-option of the preceding device-certification requirement. | Service-provider duty. |
| [1(8) — OVR-7.5-03(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | EUCC [7][11] certified to EAL 4 or higher. | None | Ignored | Sub-option of the preceding device-certification requirement. | Service-provider duty. |
| [1(8) — OVR-7.5-03(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Until 31.12.2030, FIPS PUB 140-3 [6] level 3. | None | Ignored | Time-limited sub-option of the preceding device-certification requirement. | Sunset 31 December 2030. |
| [1(8) — OVR-7.5-03, unnumbered certification-target paragraph](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Certification shall be to a security target/protection profile or module design and security documentation meeting the standard's requirements, based on risk analysis. | None | Ignored | Elaborates the device-certification requirement for an SVS provider. | Service-provider duty. |
| [1(8) — OVR-7.5-03, unnumbered EUCC-configuration paragraph](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | If the device benefits from EUCC certification, it shall be configured and used in accordance with that certification. | None | Ignored | Elaborates the device-certification requirement for an SVS provider. | Service-provider duty. |
| [1(8) — OVR-7.5-04](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Void. | Indirect | Ignored | Binding adaptation removes the requirement. | Removed by this act. |
| [1(8) — OVR-7.5-06](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | An SVSP private signing key shall be exported/imported into a different secure cryptographic device only securely and in accordance with that device's certification. | None | Ignored | No SVSP private signing key exists in scope. | Service-provider duty. |

#### 1(9) — clause 7.7, operation security

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(9) — OVR-7.7-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The SVA shall use an application environment maintained with up-to-date security fixes. | Conditional | Unknown | Application-level duty potentially touching the desktop/API/CLI build; no maintainer environment-patching evidence was found. | Conditional on use as an SVA. |
| [1(9) — OVR-7.7-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI TS 119 101 [1], clause 5.2, GSM 1.3, shall apply to the SVA. | Conditional | Unknown | Need an application-level TS 119 101 clause 5.2/GSM 1.3 conformity assessment; library assurance (V) is not that assessment. | Conditional on use as an SVA. |

#### 1(10) — clause 7.8, network security

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(10) — OVR-7.8-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | If remote access to systems storing/processing confidential data is allowed, a formal policy shall be adopted under OVR-6.3-02. | None | Ignored | Addressed to an operated service infrastructure; the local API binds loopback/opt-in host only, with no operated confidential-data store. | Service-provider duty. |
| [1(10) — OVR-7.8-04](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The vulnerability scan referenced in EN 319 401 REQ-7.8-13 shall be performed at least quarterly. | None | Ignored | Operated-service scanning cadence; publisher runs no service. | Service-provider duty. |
| [1(10) — OVR-7.8-05](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The penetration test referenced in EN 319 401 REQ-7.8-17X shall be performed at least annually. | None | Ignored | Operated-service testing cadence; publisher runs no service. | Service-provider duty. |
| [1(10) — OVR-7.8-06](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Firewalls shall be configured to prevent all protocols and accesses not required for the operation of the SVSP. | None | Ignored | Operated-service network control; publisher runs no service. | Service-provider duty. |

#### 1(11) — clause 7.12, service provisioning termination

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(11) — OVR-7.12-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The TSP's termination plan shall comply with implementing acts adopted under eIDAS Article 24(5). | None | Ignored | Addressed to a TSP operating a service; publisher runs no service. | Service-provider duty. |

#### 1(12) — clause 7.14, supply chain

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(12) — OVR-7.14-01](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | EN 319 401 [2], clause 7.14, requirements shall apply. | None | Ignored | Supply-chain requirements for a TSP; publisher is not a TSP. | Service-provider duty. |

#### 1(13) — clause 8.1, signature validation process

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(13) — VPR-8.1-07](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The validation application (SVA) shall comply with ETSI TS 119 101 [1], clause 7.4, SIA 1 to SIA 4. | Conditional | Unknown | Application-level duty for a validation application; DSS delegation (V) and no-QTSP status do not discharge it. Need SIA 1–4 conformity evidence. | Conditional on use as an SVA. |
| [1(13) — VPR-8.1-11](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] When the SVS validates qualified electronic signatures/seals under Article 32(1)/40, the validation process shall follow ETSI TS 119 172-4 [8]. | Conditional | Unknown | V validates via DSS with no explicit policy argument; need a TS 119 172-4 applicability/validation-policy implementation and TARC evidence. | Conditional on qualified-signature/seal validation. |

#### 1(14) — clause 8.2, signature validation protocol

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(14) — SVP-8.2-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The signature validation response shall bear the OID of the SVS policy. | None | Ignored | Addressed to a validation-service protocol/response; the local API returns signing responses, not SVS validation responses (A). | Service-protocol duty. |

#### 1(15) — clause 8.4, signature validation report

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(15) — SVR-8.4-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The validation report shall conform to ETSI TS 119 102-2 [9]. | Conditional | Unknown | R renders only a DSS simple report through XSLT; no schema-validated TS 119 102-2 report fixture was found. | Conditional on an SVS report obligation. |
| [1(15) — SVR-8.4-07](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] When a validation policy is not completely processed, the report shall report constraints ignored or overridden in addition to validated ones. | Conditional | Unknown | Need report content tracing ignored/overridden constraints; R shows none. | Conditional on incomplete policy processing. |
| [1(15) — SVR-8.4-15](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The report shall clearly indicate the origin of each PoE (from within the signature, from the client, from the server). | Conditional | Unknown | Need PoE-origin reporting evidence; not shown in R. | Conditional on an SVS report. |
| [1(15) — SVR-8.4-16](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The report shall bear a validation report signature, which shall be the SVSP's digital signature. | None | Ignored | Autogram produces unsigned local reports (R) and has no SVSP report-signing identity. | Service-provider duty. |
| [1(15) — SVR-8.4-17](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] When validation reports are signed, the format and target of the signature shall conform to ETSI TS 119 102-2 [9]. | None | Ignored | No signed validation reports are produced in scope. | Conditional on signed reports. |

#### 1(16) — clause 9, framework for validation service policies

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(16) — OVR-9-05](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] When building an SVS policy on a trust-service policy, a risk assessment shall determine security requirements for the community/applicability. | None | Ignored | Addressed to a service building a policy; publisher operates no service and publishes no SVS policy. | Service-provider duty. |
| [1(16) — OVR-9-06](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] The policy shall be approved and modified under a defined review process with maintenance responsibilities. | None | Ignored | Service policy governance; out of confirmed scope. | Service-provider duty. |
| [1(16) — OVR-9-07](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] A defined review process shall ensure the policy is supported by the practice statements. | None | Ignored | Service policy governance; out of confirmed scope. | Service-provider duty. |
| [1(16) — OVR-9-08](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] The TSP shall make available the policies it supports to its user community. | None | Ignored | No service user community or published policy set exists in scope. | Service-provider duty. |
| [1(16) — OVR-9-09](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] Revisions to supported policies shall be made available to subscribers. | None | Ignored | No service subscribers or published policy set exists in scope. | Service-provider duty. |

#### 1(17) — Annex B (normative), qualified validation service for QES

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(17) — VPR-B-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] If the SVSP is a QSVSP, the implementation shall comply with ETSI TS 119 172-4 [8]. | None | Ignored | Condition presumes a QSVSP; publisher is not a QTSP and operates no service. | Conditional on QSVSP status. |
| [1(17) — NOTE 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Void. | Indirect | Ignored | Binding adaptation removes the note. | Removed by this act. |
| [1(17) — OVR-B-04](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] If the SVSP is a QSVSP, the tests in OVR-B-03 shall check different positive and negative use-cases. | None | Ignored | QSVSP test obligation; out of confirmed scope. | Conditional on QSVSP status. |
| [1(17) — VPR-B-11](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] If the SVSP is a QSVSP, it shall control hash computation (on the server side or by controlling the client). | None | Ignored | QSVSP service control; Autogram computes hashes locally during signing but offers no validation service. | Conditional on QSVSP status. |
| [1(17) — NOTE 5](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Void. | Indirect | Ignored | Binding adaptation removes the note. | Removed by this act. |
| [1(17) — NOTE 6](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Void. | Indirect | Ignored | Binding adaptation removes the note. | Removed by this act. |
| [1(17) — VPR-B-15](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] If the SVSP is a QSVSP, the validation report shall comply with ETSI TS 119 102-2 [9] to satisfy VPR-B-13 to VPR-B-14. | None | Ignored | QSVSP report obligation; R shows only simple reports. | Conditional on QSVSP status. |
| [1(17) — VPR-B-16](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | [CONDITIONAL] If the SVSP is a QSVSP, the implementation shall comply with the Agreed Cryptographic Mechanisms endorsed by the ECG and published by ENISA [10]. | None | Ignored | QSVSP cryptographic-mechanism obligation; out of confirmed scope. | Conditional on QSVSP status. |

#### 1(18) — Annex C (informative), mapping of requirements

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(18) — Annex C informative paragraph](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | A correct, deterministic validation algorithm and a validation policy are needed to verify Articles 32(1)/40 conditions; ETSI TS 119 172-4 [8], based on EN 319 102-1 [3], was issued for this. | Indirect | Ignored | Explanatory mapping; source of the validation-algorithm requirement assessed at Point 2. | Informative only. |

#### Point 2 — adaptations to ETSI TS 119 172-4

#### 2(1) — clause 2.1 normative references

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(1) — reference [1]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI EN 319 102-1 V1.4.1 (2024-06), AdES creation and validation procedures, Part 1. | Indirect | Ignored | Bibliographic normative reference; the validation-algorithm edition matters for the invoked policy (V). | Fixed edition. |
| [2(1) — unnumbered substitution instruction](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | All references to “ETSI TS 119 102-1 [1]” shall be understood as references to “ETSI EN 319 102-1 [1]”. | Indirect | Ignored | Interpretative substitution applying throughout the referenced policy; a policy/documentation reference audit is still needed. | Applies throughout TS 119 172-4. |
| [2(1) — reference [2]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI TS 119 612 V2.3.1 (2024-11), Trusted Lists. | Indirect | Ignored | T checks only selected-list completeness; a full TS 119 612 parsing/status-history assessment is separate. | Fixed edition. |
| [2(1) — reference [13]](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | ETSI TS 119 101 V1.1.1 (2016-03), policy and security requirements for signature creation and validation applications. | Conditional | Unknown | Application standard also invoked by VPR-8.1-07 and OVR-7.7-03; need application-level conformity evidence. | Fixed edition. |

#### 2(2) — clause 4.2, REQ-4.2-03, X.509 validation constraints point (c)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(2)(i)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | If an end-entity certificate represents a trust anchor, RevocationCheckingConstraints shall not be used. | Conditional | Unknown | V configures CRL/OCSP sources but no effective policy dump; need an anchor-specific check-omission fixture. | Certificate-specific exception. |
| [2(2)(ii)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | Otherwise RevocationCheckingConstraints shall be set to 'eitherCheck' as defined in ETSI TS 119 172-1 [3], A.4.2.1, table A.2 rows (m)2.1. | Conditional | Unknown | V shows online OCSP/CRL sources, not this constraint; need policy dump and successful/failed OCSP and CRL vectors. | External definition, not reproduced in this act. |
| [2(2)(iii)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | If an end-entity certificate represents a trust anchor, the RevocationFreshnessConstraints of TS 119 172-1 [3], A.4.2.1, table A.2 rows (m)2.2 shall not be used. | Conditional | Unknown | Need anchor-specific freshness policy and fixture; V alone does not prove omission. | Distinct from the checking exception. |
| [2(2)(iv) — sentence 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | If the end-entity certificate is not a trust anchor, the freshness constraints of TS 119 172-1 [3], A.4.2.1, table A.2 rows (m)2.2 shall be used with a maximum value of 24 hours for the signing certificate. | Conditional | Unknown | V/U do not establish age enforcement; the six-hour list cache (V, lines 107–110) is not this value. Need just-under/at/over-24-hour OCSP/CRL fixtures. | Not a trusted-list refresh interval. |
| [2(2)(iv) — sentence 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | No value shall be set for the RevocationFreshnessConstraints for certificates other than the signing certificate, including certificates supporting time-stamps. | Conditional | Unknown | Need chain/timestamp certificate policy inspection and tests proving no value is set. | Do not extend the 24-hour maximum to every chain certificate. |

#### 2(3) — clause 4.4, technical applicability (rules) checking process, REQ-4.4.2-03

Adapted **REQ-4.4.2-03**: if any check specified in REQ-4.4.2-01 fails, all three consequences below apply. The checks themselves are in the external standard, not restated by this act.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(3) — REQ-4.4.2-03(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The process stops. | Conditional | Unknown | V delegates execution to DSS; need instrumented negative TARC vectors showing the required stop, not merely a UI failure label. | Failed applicability check, not every software error. |
| [2(3) — REQ-4.4.2-03(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The signature shall be technically determined as indeterminate — neither an EU qualified electronic signature nor an EU qualified electronic seal. | Conditional | Unknown | U proves presentation safeguards only; need each failed-REQ-4.4.2-01 fixture's qualification result, for both signatures and seals. | Indeterminate is not a successful qualification nor a blanket assertion of cryptographic invalidity. |
| [2(3) — REQ-4.4.2-03(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1942/oj#anx_1) | The above result and the results of all intermediate processes shall be reflected in the signature applicability rules checking report. | Conditional | Unknown | R shows only simple-report XML; need negative-check applicability reports recording all intermediate results and validating against the report standard. | Simple HTML output is insufficient evidence. |

## Coverage and limitations

- Enumerated independently from the official act, not copied from the existing compliance report: **7 recitals; 2 articles (Article 1's 1 unnumbered paragraph and Article 2's 2 unnumbered paragraphs); 1 binding directly-applicable paragraph; 1 adoption/signature formula; 3 preamble units; and the complete single Annex** with its opening paragraph and both numbered adaptation points. These produce **88 assessment rows**: 10 preamble/recital rows, 4 article/concluding rows, and **74 Annex rows**.
- Annex row breakdown: opening/reference-specification 3; Point 1 — (1) normative references 12, (2) informative reference 1, (3) abbreviation 1, (4) process note 1, (5) SVS practice statement 2, (6) information security policy 1, (7) human resources 2, (8) cryptographic controls 9, (9) operation security 2, (10) network security 4, (11) termination 1, (12) supply chain 1, (13) signature validation process 2, (14) validation protocol 1, (15) validation report 5, (16) policy framework 5, (17) Annex B QSVSP 8, (18) Annex C informative 1; Point 2 — (1) normative references 4, (2) revocation constraints 5, (3) TARC 3. Every inserted, replaced or voided clause, every lettered subpoint and the voided notes are individually identifiable; each `Provision` cell names its nested point while all rows link to `#anx_1`.
- **Text coverage:** the complete act body, both adaptation points, all footnotes and the full Annex were obtained in one Cellar XHTML retrieval (`L_202501942EN.000101.fmx.xml`). No act provision was deliberately omitted. The rendered EUR-Lex page, consolidations and the authentic PDF were **not** obtainable (WAF challenge, HTTP 202 empty on direct requests), so the website anchor scheme could not be independently byte-verified; anchors follow the required convention and the sibling 2025/1945 document. This limitation must remain visible rather than claiming independent PDF or live-anchor verification.
- **No qualified validation service is operated.** The publisher is not a QTSP; the desktop GUI performs local DSS validation, the CLI and the local HTTP API only sign (with a fast structural check at most), there is no validation endpoint, and the local DSS simple report is unsigned and self-provided. It is therefore **not** a qualified validation service and does not trigger the Article 33(2)/Article 40 presumption or the SVSP/QSVSP obligations. That is why organisational clauses are **None / Ignored** rather than **Not done**.
- **External standards not inspected:** none of ETSI TS 119 441, ETSI TS 119 172-4, ETSI TS 119 172-1, ETSI EN 319 102-1, ETSI EN 319 401, ETSI TS 119 101, ETSI TS 119 102-2 or ETSI TS 119 612 was inspected in full. Their cited titles/versions and the act's adaptations were inspected; their unmodified clauses remain separate works requiring dedicated conformity assessment and are not silently imported as text of this act. The TS 119 172-1 definition invoked in 2(2) is identified, not reproduced.
- **Conditional technical gaps (not violations):** no explicit effective DSS validation policy argument; no documented 24-hour signing-certificate revocation-freshness enforcement; no trust-anchor versus non-anchor constraint mapping; no covered TARC/applicability negative vectors; no TS 119 102-2 conformant or applicability-rules report; no application-level ETSI TS 119 101 evidence covering clause 5.2/GSM 1.3, clause 7.4 SIA 1–4. Named tests establish narrow list-status and presentation intentions only.
- **Outstanding evidence and review:** a documented decision whether/where the presumption is claimed; exact effective DSS policy and edition mapping; 24-hour boundary and anchor fixtures; complete applicability/TARC reports; application-level TS 119 101 conformity; actor-specific personal-data processing records for Recital (5); and a full subsequent amendment-history verification of this act. No builds, test execution, implementation changes, commits or organisational compliance assertions accompany this reading register. This is a traceable engineering/legal reading aid, not legal advice, certification or a released-binary audit.
