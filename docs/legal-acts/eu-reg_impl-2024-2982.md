# Commission Implementing Regulation (EU) 2024/2982 — wallet protocols and interfaces

## Executive summary

**Actual title:** Commission Implementing Regulation (EU) 2024/2982 of 28 November 2024 laying down rules for the application of Regulation (EU) No 910/2014 of the European Parliament and of the Council as regards protocols and interfaces to be supported by the European Digital Identity Framework.

This is a wallet-protocol act, not a general PDF-signature validation standard. Under the confirmed [register scope and shared facts](README.md), Autogram desktop, local API and CLI have **no current wallet-provider or wallet-relying-party role**. Its wallet duties are therefore **None / Ignored** for this assessment, not outstanding Autogram implementation tasks. Validating a signed PDF is not authenticating a wallet, a wallet user, a wallet-relying party, or a wallet presentation request. No wallet conformity is claimed from existing signing or validation functionality.

The original act entered into force on **24 December 2024**. Regulation **2026/1731**, effective **11 August 2026**, materially changes its authentication rules, removes wallet-to-wallet requirements, and replaces its two-item annex with detailed issuance and presentation annexes. Article 3(4), as amended, applies from **11 August 2028**. These are dates for the regulated wallet functions, not independent Autogram release deadlines. A future wallet deployment would require a fresh role assessment, a documented protocol/security architecture and interoperability tests; none was supplied for that hypothetical deployment.

## Source and version

| Item | Verified information |
| --- | --- |
| Official complete original text | [EUR-Lex English OJ text](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj/eng); [official Publications Office English XHTML actually retrieved](https://publications.europa.eu/resource/cellar/0c2d7c7b-b1e1-11ef-acb1-01aa75ed71a1.0006.03/DOC_1). |
| Identifier / language | CELEX **32024R2982**; ELI `http://data.europa.eu/eli/reg_impl/2024/2982/oj`; English; Commission document C/2024/8496. |
| Adoption / publication | **28 November 2024** / **4 December 2024**, OJ L, 2024/2982. |
| Entry / application | Article 8: twentieth day after publication, **24 December 2024**; original act has no separate general application date. Amended Article 3(4) has the specific application date **11 August 2028**. |
| Amendment verified | [Regulation (EU) 2026/1731](https://eur-lex.europa.eu/eli/reg_impl/2026/1731/oj/eng), Article 4 and Annexes XI–XII; [complete official English XHTML actually retrieved](https://publications.europa.eu/resource/cellar/802ddc72-856b-11f1-bf5e-01aa75ed71a1.0006.03/DOC_1). Adopted **15 July 2026**, published **22 July 2026**, effective **11 August 2026** under its Article 5. See also the separate [amending-act assessment](eu-reg_impl-2026-1731.md). |
| Consolidation | EUR-Lex identifies **11 August 2026** as the current consolidation: [02024R2982-20260811](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2982-20260811). The assessment reconstructs the amended provisions from the complete published original and amending act; the consolidated full text was not independently retrieved. |
| Relationships | Implements Regulation 910/2014 Article 5a(23); coordinates with Regulations 2024/2977, 2024/2979 and 2024/2980. The retrieved sources identify amendment by 2026/1731, not repeal of 2024/2982. No exhaustive independent certification that no further corrigendum exists is claimed. |
| Access / baseline | **8 October 2026**; repository baseline **5ca91d5c**; assessment is an engineering/legal reading aid, not legal advice or a released-binary audit. |

Complete legal text is linked, not reproduced. The following English descriptions are assessment paraphrases, not substitutes for the authoritative text.

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or out of the confirmed scope; never "knowingly disregarded"). Every provision row links to the official text at that unit (act anchors `#art_N`, `#anx_R`, recitals `#rct_N`); structural/preamble labels link to the [act page](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj).

## Provision-by-provision assessment

### Reading conventions and evidence

- **O**: unchanged original text, in force from 24 December 2024.
- **A**: text introduced/replaced from 11 August 2026 by Regulation 2026/1731 Article 4.
- **H**: original historical provision, deleted/replaced on 11 August 2026.
- **W** in the evidence column: confirmed absence of the current wallet role under [README.md](README.md). If that role changes, obtain the product/deployment role decision, wallet-provider or relying-party contracts/registration, protocol architecture, certificate trust/validation policy, security risk analysis, and requirement-specific interoperability tests. W is **not** proof that hypothetical wallet functionality is implemented.
- **Context**: the official act supplies the definition, interpretation or legal date; there is no software completion measure. No unrelated source-code path is used to assert organisational compliance.

Tables assess the **amended state** unless expressly labelled historical. Parent introductory text is labelled structurally, without inventing an additional duty. Parent numbered paragraphs with their own substantive rule have their own row as well as individually identified subpoints.

### Preamble and recitals (original, retained interpretative context)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Preamble — legal-basis clauses](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj) | Commission acts under the TFEU and Regulation 910/2014 Article 5a(23). | Indirect | Ignored | Context: official preamble. | O; not a software duty. |
| [Recital 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#rct_1) | Secure interoperable identity ecosystem, cross-border access and privacy objectives. | Indirect | Ignored | Context. | O; interpretative. |
| [Recital 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#rct_2) | GDPR and, where relevant, ePrivacy rules apply to processing under this act. | Indirect | Ignored | Context; any separate Autogram processing assessment belongs under those instruments. | O; does not create a wallet role. |
| [Recital 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#rct_3) | Explains four coordinated implementing regulations and protocol remit. | Indirect | Ignored | Context; companion acts. | O. |
| [Recital 4](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#rct_4) | Commission review of technical developments, Toolbox and architecture/reference framework. | Indirect | Ignored | Context; Commission update process. | O; amendment verified below. |
| [Recital 5](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#rct_5) | Mutual authentication, trustworthy wallets, minimisation, unlinkability and common issuance interfaces. | Indirect | Ignored | Context; W if developing wallets. | O; original wallet-to-wallet context is not a surviving operative obligation. |
| [Recital 6](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#rct_6) | Common remote/proximity presentation specifications; additional use-case protocols possible. | Indirect | Ignored | Context. | O. |
| [Recital 7](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#rct_7) | Privacy by design, separation of service data, registered requested attributes and visible registration data. | Indirect | Ignored | Context; W if entering wallet ecosystem. | O; not a PDF-validation rule. |
| [Recital 8](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#rct_8) | Request validation before disclosure and user data-erasure requests. | Indirect | Ignored | Context. | O; read with amended Article 3. |
| [Recital 9](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#rct_9) | Easy reports to national data-protection authorities, with implementation flexibility. | Indirect | Ignored | Context; national reporting procedures if W changes. | O. |
| [Recital 10](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#rct_10) | EDPS consultation and opinion. | None | Ignored | Context; no publisher action. | Opinion **30 September 2024**. |
| [Recital 11](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#rct_11) | Committee opinion under eIDAS Article 48. | None | Ignored | Context; no publisher action. | O. |

### Article 1 — subject matter and scope

Structural introduction: rules concern wallet-solution protocols and interfaces for the following individually numbered activities.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 1(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_1) | Issuance of person identification data (PID) and electronic attestations of attributes (EAA) to wallet units. | None | Ignored | W; no wallet issuance product. | O. |
| [Article 1(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_1) | Presentation of PID/EAA attributes to wallet-relying parties. | None | Ignored | W; no wallet-relying-party interaction. | A; removes original reference to other wallet units. |
| [Article 1(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_1) | Communication of erasure requests to wallet-relying parties. | None | Ignored | W. | O. |
| [Article 1(4)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_1) | Reports concerning wallet-relying parties to GDPR Article 51 authorities. | None | Ignored | W. | O. |
| [Article 1 — unnumbered concluding paragraph](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_1) | Regular updating with technology, standards, Toolbox and architecture/reference framework. | Indirect | Ignored | Context; Commission updates, not developer release duty. | O. |

### Article 2 — definitions

Structural introduction: the following definitions govern this act. They do not impose independent implementation tasks.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 2(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Wallet-relying party: intends digital reliance on wallet units to provide public/private services. | Indirect | Ignored | Context; ordinary signed-PDF validation does not establish this role. | O. |
| [Article 2(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Wallet user: controls the wallet unit. | Indirect | Ignored | Context. | O. |
| [Article 2(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Wallet solution: combined software/hardware/services/settings/configuration, including secure components. | Indirect | Ignored | Context; no such product role assumed. | O. |
| [Article 2(4)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Wallet unit: unique configuration supplied to an individual user. | Indirect | Ignored | Context. | O. |
| [Article 2(5)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Wallet provider: person providing wallet solutions. | Indirect | Ignored | Context; confirmed current Autogram scope excludes this role. | O. |
| [Article 2(6)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Wallet instance: installed/configured application forming part of a unit. | Indirect | Ignored | Context. | O. |
| [Article 2(7)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Wallet secure cryptographic application: linked application managing critical assets. | Indirect | Ignored | Context; signing software is not automatically this component. | O. |
| [Article 2(8)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Wallet secure cryptographic device: tamper-resistant protected execution environment. | Indirect | Ignored | Context; no device conformity claim. | O. |
| [Article 2(9)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Critical assets: exceptionally important assets whose compromise seriously undermines wallet reliance. | Indirect | Ignored | Context. | O. |
| [Article 2(10)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Wallet-relying-party access certificate: signature/seal certificate authenticating and validating that party. | Indirect | Ignored | Context; not every PDF signing certificate is such a certificate. | O. |
| [Article 2(11)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Access-certificate provider: Member-State-mandated issuer to registered relying parties. | Indirect | Ignored | Context; mandate/registration documents would be needed for that role. | O. |
| [Article 2(12)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Wallet unit attestation: component-description/authentication/validation data object. | Indirect | Ignored | Context; definition remains despite deletion of Article 3(8). | O. |
| [Article 2(13)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Embedded disclosure policy: provider-embedded conditions for relying-party access to EAA. | Indirect | Ignored | Context. | O. |
| [Article 2(14)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Registration certificate: indicates attributes a relying party registered an intention to request. | Indirect | Ignored | Context; separate from access certificate. | O. |
| [Article 2(15)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | PID provider: issues/revokes PID and ensures cryptographic binding to a unit. | Indirect | Ignored | Context; not a PDF-signature validator role. | O. |
| [Article 2(16)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_2) | Cryptographic binding: links PID/EAA to wallet units cryptographically. | Indirect | Ignored | Context. | O. |

### Article 3 — general provisions

Structural introduction: wallet providers must ensure these unit behaviours for Articles 4 and 5 protocols/interfaces. Article 3(9) is a gate whose individual steps are assessed below.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 3(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_3) | Authenticate/validate access certificates without delegating execution to OS browser or intermediary application. | None | Ignored | W; would need in-wallet validation and negative-certificate tests. | A; stricter than original. |
| [Article 3(2) — deleted](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_3) | Original: authenticate/validate other units' attestations in wallet-to-wallet interactions. | None | Ignored | Historical text only; W. | H. |
| [Article 3(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_3) | Authenticate/validate requests using relying-party access certificates. | None | Ignored | W; certificate-bound request tests. | A; other-unit attestations removed. |
| [Article 3(4)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_3) | Authenticate/validate relying-party registration certificate. | None | Ignored | W; registration trust, revocation and validation evidence. | A; **applies 11 August 2028**; original qualifier removed. |
| [Article 3(5)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_3) | Display information contained in access certificates to users. | None | Ignored | W; wallet consent-screen evidence. | A; other-unit information removed. |
| [Article 3(6)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_3) | Display requested attributes where applicable. | None | Ignored | W; wallet attribute-request UX tests. | O. |
| [Article 3(7)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_3) | Display registration-certificate information where applicable. | None | Ignored | W; registration-certificate UX tests. | O. |
| [Article 3(8) — deleted](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_3) | Original: present own wallet unit attestations to requesting relying parties/units. | None | Ignored | Historical text only; Article 4(3)(b) remains separately assessed. | H. |
| [Article 3(9)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_3) | Do not present requested attributes to relying parties before completing the steps below. | None | Ignored | W; disclosure-gate tests. | A; not the original three-step gate. |
| [Article 3(9)(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_3) | Verify embedded policies processed under Regulation 2024/2979 Article 10. | None | Ignored | W; policy-engine and enforcement tests. | A; original referenced Article 11. |
| [Article 3(9)(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_3) | Verify user's partial/full approval of presentation. | None | Ignored | W; explicit user-approval evidence. | A. |
| [Article 3(10)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_3) | Privacy-preserving unlinkability across relying parties when EAA does not require user identification. | None | Ignored | W; threat model and cross-transaction linkability tests. | O. |

Historical Article 3(9) steps are retained individually for traceability:

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Original Article 3(9)(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj) | Verify secure cryptographic application authenticated wallet-user identity. | None | Ignored | W; historical step, not current Article 3(9)(a). | H. |
| [Original Article 3(9)(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj) | Verify embedded policies processed under Article 11 of 2024/2979, where applicable. | None | Ignored | W; superseded cross-reference. | H. |
| [Original Article 3(9)(c)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj) | Verify partial/full user approval. | None | Ignored | W; now Article 3(9)(b). | H. |

### Article 4 — issuance

Paragraph 3 introduces the individually assessed issuance requirements; it does not add a separate technical measure beyond its subpoints.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 4(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_4) | Wallet solutions support Annex I issuance protocols/interfaces. | None | Ignored | W; issuance profile and interop suite. | A; replaces generic support requirement. |
| [Article 4(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_4) | Request issuance only from parties with authentic, valid access certificate attesting one of the following provider roles. | None | Ignored | W; issuer trust/role checks and negative tests. | O. |
| [Article 4(2)(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_4) | Eligible certificate role: PID provider. | None | Ignored | W; role attestation. | O. |
| [Article 4(2)(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_4) | Eligible role: qualified EAA provider. | None | Ignored | W; qualified-provider role attestation. | O. |
| [Article 4(2)(c)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_4) | Eligible role: EAA issued by/on behalf of public body responsible for authentic source. | None | Ignored | W; public-source provider role attestation. | O. |
| [Article 4(2)(d)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_4) | Eligible role: non-qualified EAA provider. | None | Ignored | W; non-qualified-provider role attestation. | O. |
| [Article 4(3)(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_4) | For multi-format issuers request all formats referred to in 2024/2979 Article 8. | None | Ignored | W; format negotiation and issuer tests. | O. |
| [Article 4(3)(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_4) | Present unit attestations to PID/EAA providers on request for component authentication/validation. | None | Ignored | W; issuer-facing attestation exchange tests. | O; not deleted with Article 3(8). |
| [Article 4(3)(c)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_4) | Enable PID providers to verify issuance, delivery and activation to assurance level high under 2015/1502. | None | Ignored | W; level-high assurance/security evidence. | O; no inference from PDF signing. |
| [Article 4(3)(d)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_4) | Verify PID/EAA authenticity and validity. | None | Ignored | W; credential authenticity/status tests. | O; not ordinary PDF validation. |

### Article 5 — presentation

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 5(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_5) | Support remote and, where appropriate, proximity presentation under Annex II technical specifications. | None | Ignored | W; profile/proximity interoperability tests. | A. |
| [Article 5(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_5) | At user request respond to successfully authenticated/validated Article 3 requests under Annex II. | None | Ignored | W; request/consent/response tests. | A. |
| [Article 5(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_5) | Support proof of possession of private keys used for cryptographic bindings. | None | Ignored | W; binding-key proof tests. | O; signature creation alone proves no wallet measure. |
| [Article 5(4)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_5) | Support selective disclosure of PID/EAA attributes. | None | Ignored | W; selective-disclosure tests. | O. |
| [Article 5(5) — deleted](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_5) | Original: apply paragraphs 1–4 to two units interacting in proximity. | None | Ignored | Historical text; no current wallet-to-wallet duty under this paragraph. | H. |

### Articles 6–8 — rights interfaces and commencement

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 6(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_6) | Enable users to request GDPR Article 17 erasure from parties previously interacted with through their unit. | None | Ignored | W; erasure-request interface and transmission tests. | O; not unconditional right to erase all records. |
| [Article 6(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_6) | Let users select erasure-request recipients. | None | Ignored | W; recipient-selection UX tests. | O. |
| [Article 6(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_6) | Display previously submitted erasure requests. | None | Ignored | W; request-history UX tests. | O. |
| [Article 7(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_7) | Enable easy reports of relying parties to GDPR Article 51 supervisory authorities. | None | Ignored | W; authority routing/report UX tests. | O. |
| [Article 7(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_7) | Implement reporting protocols/interfaces in accordance with Member States' procedural laws. | None | Ignored | W; applicable national procedures and legal mapping. | O; no common invented filing procedure. |
| [Article 7(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_7) | Permit substantiation with identifying information and user claims in machine-readable form. | None | Ignored | W; attachment/export/schema tests. | O. |
| [Article 8 — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_8) | Entry on twentieth day after original OJ publication. | Indirect | Ignored | Context; original OJ date and amendment. | Original entry **24 December 2024**; replacement does not restart the original act. |
| [Article 8 — unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_8) | Article 3(4) applies from 11 August 2028. | None | Ignored | Context; W if entering wallet role. | A; targeted deferment, not a blanket annex deferment. |
| [Article 8 — unnumbered paragraph 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#art_8) | Binding in entirety and directly applicable in all Member States. | Indirect | Ignored | Context; does not extend subject matter beyond wallet functions. | A; original concluding binding clause retained substantively. |
| [Original Article 8 — unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj) | Original binding/direct-applicability clause. | Indirect | Ignored | Context; now replacement paragraph 3. | H; no original separate application-date paragraph. |
| [Closing adoption/signature block](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj) | Brussels adoption and Commission President signature. | None | Ignored | Context; authenticating publication, not publisher duty. | **28 November 2024**. |

### Original Annex — historical standards referred to in Article 5(1) and (2)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Original Annex — dash entry 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj) | ISO/IEC 18013-5:2021 reference. | None | Ignored | W; standard itself not inspected. | H; original annex deleted in full. |
| [Original Annex — dash entry 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj) | ISO/IEC TS 18013-7:2024 reference. | None | Ignored | W; standard itself not inspected. | H; do not use this edition as the amended reference. |

### Annex I — issuance protocols/interfaces (2026/1731 Annex XI insertion)

All entries below are **A**. The subsection labels refer to the external specification clauses being adapted, not new article numbering.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex I — unnumbered opening paragraph](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | Apply ETSI TS 119 472-3 V1.1.1 (2026-03) with listed adaptations. | None | Ignored | W; full referenced ETSI text and profile tests would be needed. | A; external standard not inspected. |
| [Annex I point 1 / GEN-REQ-4.1-05](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | Void this general requirement. | None | Ignored | Context; no duty to implement a void entry. | A. |
| [Annex I point 1 / NOTE](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | Void the associated note. | None | Ignored | Context. | A. |
| [Annex I point 2 / ISS-MDATA-REG_CERT-4.2.3-04](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | Include provider registration certificate in an `issuer_info` array element. | None | Ignored | W; issuer metadata/registration tests. | A; PID/EAA provider metadata. |
| [Annex I point 2 / ISS-MDATA-REG_CERT-4.2.3-07](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | Void this entry. | None | Ignored | Context. | A. |
| [Annex I point 2 / ISS-MDATA-REG_CERT-4.2.3-08](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | Void this entry. | None | Ignored | Context. | A. |
| [Annex I point 2 / ISS-MDATA-REG_CERT-4.2.3-09](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | Void this entry. | None | Ignored | Context. | A. |
| [Annex I point 2 / ISS-MDATA-REG_CERT-4.2.3-10](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | Void this entry. | None | Ignored | Context. | A. |
| [Annex I point 2 / ISS-MDATA-REG_CERT-4.2.3-11](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | Void this entry. | None | Ignored | Context. | A. |
| [Annex I point 2 / ISS-MDATA-REG_CERT-4.2.3-12](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | Void this entry. | None | Ignored | Context. | A. |
| [Annex I point 2 / ISS-MDATA-REG_CERT-4.2.3-13](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | Void this entry. | None | Ignored | Context. | A. |
| [Annex I point 3 / ISS-MDATA-EAA-REUSE-POL-4.2.4.2-09](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | For `arf_annex_ii` policy with `once_only`/`per-relying-party` details, include numeric `reissue_trigger_unused` in the same JSON object. | None | Ignored | W; policy-schema and reissuance tests. | A. |
| [Annex I point 4](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_1) | Do not apply external ETSI Annex A. | None | Ignored | Context; scope exclusion, not missing implementation. | A. |

### Annex II — presentation: introduction and points (1)–(6)

Each external clause heading is structural; each dash entry, including notes and void entries, is individually identified. All rows are **A**, with no claim that the incorporated external standards have been inspected.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex II — unnumbered opening paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Apply Annex C of ISO/IEC 18013-7:2025. | None | Ignored | W; external ISO text and API profile tests. | A; 2025 edition. |
| [Annex II — unnumbered opening paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Apply ETSI TS 119 472-2 V1.2.1 (2026-03) clauses 4.1, 4.2, 5, 6 with adaptations/new clause 4.3. | None | Ignored | W; external ETSI text and profile mapping. | A. |
| [Annex II(1) — scope dash entry / introduction](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Two request/presentation profiles, each supporting API and non-API transmission. | None | Ignored | W; profile/transmission design. | A; adapted external clause 1. |
| [Annex II(1)(a) — dash entry 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | ISO/IEC 18013-5 supports non-API transmission only in this profile. | None | Ignored | W; non-API profile tests. | A. |
| [Annex II(1)(a) — dash entry 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | ISO/IEC 18013-7 Annex C supports API transmission. | None | Ignored | W; API profile tests. | A. |
| [Annex II(1)(a) — unnumbered concluding paragraph](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | ISO/IEC-mdoc profile defined in external clause 5. | None | Ignored | Context; external profile not inspected. | A. |
| [Annex II(1)(b) — opening paragraph](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | OpenID4VC-HAIP profile supports both transmission mechanisms. | None | Ignored | W; HAIP profile mapping. | A. |
| [Annex II(1)(b) — dash entry 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Use HAIP sections 5, 5.1, 5.3, 7, 8 for redirects/non-API transmission. | None | Ignored | W; redirect profile tests. | A. |
| [Annex II(1)(b) — dash entry 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Use HAIP sections 5, 5.2, 5.3, 7, 8 for API transmission. | None | Ignored | W; API profile tests. | A. |
| [Annex II(1)(b) — unnumbered concluding paragraph](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | HAIP profile defined in external clause 6. | None | Ignored | Context. | A. |
| [Annex II(2) — reference [15]](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Normative reference to ISO 639 language code. | None | Ignored | Context; standard not inspected. | A. |
| [Annex II(2) — reference [16]](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Normative reference to ISO/IEC 18013-7:2025 mDL add-on functions. | None | Ignored | Context; standard not inspected. | A. |
| [Annex II(3) / EAAP-SD-JWT VC-04](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(4) / EAAP-ISO/IEC-mdoc-01](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(4) / Note2](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this note. | None | Ignored | Context. | A. |
| [Annex II(4) / EAAP-ISO/IEC-mdoc-02](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(5) / EAAP-API-GEN-01](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Wallet supports mediating API for both HAIP clause 5.2 and ISO Annex C protocols. | None | Ignored | W; OS/browser/API interoperability matrix. | A. |
| [Annex II(5) / NOTE](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Missing device protocol support causes underlying-OS/browser interoperability non-conformity. | None | Ignored | Context; would need platform-support evidence under W. | A; do not attribute automatically to wallet implementation. |
| [Annex II(5) / EAAP-API-GEN-02](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | API supports at least registered catalogue PID/EAA types and all 2024/2979 Annex II formats. | None | Ignored | W; catalogue/format coverage tests. | A; catalogue under 2025/1569. |
| [Annex II(5) / NOTE 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | External-layer restrictions of supported types/formats can cause non-conformity attributable to those layers. | None | Ignored | Context; OS/browser/API capability and restriction evidence. | A. |
| [Annex II(6) / WRP-VALIDATION-01](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Validate received registration certificate before showing requested PID/EAA for user approval. | None | Ignored | W; pre-approval validation gate tests. | A; distinct from Article 3(4) commencement. |
| [Annex II(6) / WRP-VALIDATION-02](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | On validation failure warn, do not show request as validated, require explicit approval; silence/pre-ticked boxes insufficient. | None | Ignored | W; expiry/revocation/trust/malformed/crypto failure and UX tests. | A. |
| [Annex II(6) / WRP-VALIDATION-03](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Provider risk analysis/security policy determines whether/when user may bypass specific failed checks. | None | Ignored | W; documented bypass decision and tests. | A; not unconditional permission. |
| [Annex II(6) / WRP-OVERASKING-01](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Compare requested attestations/attributes with registered ones. | None | Ignored | W; request/registration comparison tests. | A. |
| [Annex II(6) / WRP-OVERASKING-02](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Warn clearly before excess disclosure; identify overasking and require explicit approval, not silence/pre-ticked boxes. | None | Ignored | W; overasking UX and consent tests. | A. |
| [Annex II(6) / WRP-OVERASKING-03](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Provider determines continuation, registered-subset disclosure or rejection under risk/security policy and law. | None | Ignored | W; legal/risk decision and disclosure tests. | A. |

### Annex II — points (7)–(12): ISO/IEC-mdoc profile

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex II(7) — introduction dash entry](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Clause 5 covers non-API ISO 18013-5 and API Annex C encapsulated data structures. | None | Ignored | W; profile design. | A. |
| [Annex II(7) — organisation dash entry](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Introduces remaining clause-5 structure. | None | Ignored | Context; structural description. | A. |
| [Annex II(7) — clause 5.2 dash entry](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Support requirements for relying parties and wallets. | None | Ignored | Context; assessed in point (8). | A. |
| [Annex II(7) — clause 5.3 dash entry](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Non-API-specific requirements. | None | Ignored | Context; points (9)–(10). | A. |
| [Annex II(7) — clause 5.4 dash entry](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | API-specific requirements. | None | Ignored | Context; points (11)–(12). | A. |
| [Annex II(8) / ISO/IEC 18013-SUPPORT-01](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Units, PID/attestation/wallet providers and relying parties must not support ISO 18013-5 server retrieval for PID/attribute requests/presentation. | None | Ignored | W; prohibited-flow tests for each deployed role. | A; multiple explicit addressees. |
| [Annex II(8) / ISO/IEC 18013-SUPPORT-02](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Wallet meets clauses 5.3 and 5.4. | None | Ignored | W; profile test mapping. | A. |
| [Annex II(8) / ISO/IEC 18013-SUPPORT-04](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Relying party should implement clause 5.4 profile. | None | Ignored | W; relying-party profile decision. | A; recommendation wording preserved. |
| [Annex II(9) / ISO/IEC 18013-5-REQ-04](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Device requests include `requestInfo` with RequestInfo type. | None | Ignored | W; request encoding tests. | A. |
| [Annex II(9) / ISO/IEC 18013-5-REQ-05](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | RequestInfo CDDL defines `euWrprc` byte-string certificate member. | None | Ignored | W; CDDL schema tests; attached code definition included in this row. | A. |
| [Annex II(9) / ISO/IEC 18013-5-REQ-06](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Include `euWrprc` member in requestInfo. | None | Ignored | W; required-member tests. | A. |
| [Annex II(9) / ISO/IEC 18013-5-REQ-07](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | `euWrprc` value is CBOR-encoded registration certificate. | None | Ignored | W; CBOR/certificate tests. | A. |
| [Annex II(9) / ISO/IEC 18013-5-REQ-08](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(9) / NOTE 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this note. | None | Ignored | Context. | A. |
| [Annex II(9) / ISO/IEC 18013-5-REQ-09](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(9) / ISO/IEC 18013-5-REQ-10](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(9) / ISO/IEC 18013-5-REQ-11](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(10) — opening dash entry](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | DeviceResponse profile common to API and non-API mechanisms. | None | Ignored | W; response profile mapping. | A. |
| [Annex II(10) / NOTE 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Explains direct non-API response and API encapsulation. | None | Ignored | Context; published wording refers to DeviceRequest in API explanation. | A; not silently corrected to DeviceResponse. |
| [Annex II(10) / ISO/IEC 18013-5-RESP-02](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | PID/EAA issuers exclude KeyAuthorizations data elements except relying-party transactional data signed/sealed by unit binding key. | None | Ignored | W; issuer MSO and transaction-data tests. | A. |
| [Annex II(10) / NOTE 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Explains restriction on device-signed elements except relying-party supplied data. | None | Ignored | Context; secure-user-authentication example. | A; not general PDF signing. |
| [Annex II(10) / NOTE 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | ISO 18013-5:2021 lacks transactional-data inclusion method; addition of technical specifications required. | None | Ignored | W; applicable additional specification would be needed. | A; no invented encoding. |
| [Annex II(10) / ISO/IEC 18013-5-RESP-03](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | PID providers must not allow PID private key to sign relying-party transactional data elements. | None | Ignored | W; provider key-use policy and negative tests. | A; narrower PID prohibition. |
| [Annex II(11) / 5.4.1 — unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Defines API requirements related to ISO Annex C. | None | Ignored | Context; structural explanation. | A. |
| [Annex II(11) / ISO/IEC 18013-7-API-01](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | API profile complies with Annex C as further profiled in clauses 5.3/5.4. | None | Ignored | W; ISO profile conformity mapping. | A. |
| [Annex II(11) / ISO/IEC 18013-7-API-02](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | All Annex C mandatory requirements apply as profiled. | None | Ignored | W; full ISO Annex C and test suite. | A; external clauses not enumerated as legislative text. |
| [Annex II(11) / ISO/IEC 18013-7-API-03](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Optional Annex C requirements remain optional unless modified. | None | Ignored | W; optionality mapping. | A. |
| [Annex II(12) — opening dash entry](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Introduces additional API-mediated requirements. | None | Ignored | Context; structural. | A. |
| [Annex II(12) / ISO/IEC 18013-ADD-API-01](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Default disclosure of stored EAA types to API, not attributes or values. | None | Ignored | W; API data-minimisation tests. | A. |
| [Annex II(12) / NOTE 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | No attribute-value disclosure even to improve OS attestation selection. | None | Ignored | W; API payload tests if deployed. | A; interpretative boundary. |
| [Annex II(12) — considerations dash entry](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Introduces OS/browser matters outside implementer control. | None | Ignored | Context; following notes separate platform roles. | A. |
| [Annex II(12) / NOTE 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Browser/OS may process requests for EAA search, anti-fraud or troubleshooting. | None | Ignored | Context; platform processing evidence if deployed. | A. |
| [Annex II(12) / NOTE 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Browser/OS expected to process requests for user security. | None | Ignored | Context. | A. |
| [Annex II(12) / NOTE 4](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | No expected processing for market analysis/secondary analysis or internal browser/OS purposes. | None | Ignored | Context; platform purpose/contract evidence if deployed. | A. |
| [Annex II(12) / ISO/IEC 18013-ADD-API-02](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Notify API when user-requested deletion removes previously advertised PID/EAA. | None | Ignored | W; deletion/API synchronisation tests. | A. |
| [Annex II(12) / ISO/IEC 18013-ADD-API-03](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Notify API on uninstall that no previously advertised PID/EAA remains stored. | None | Ignored | W; uninstall lifecycle tests. | A. |
| [Annex II(12) / ISO/IEC 18013-ADD-API-04](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Global switch disables stored-EAA disclosure; disabled wallet does not advertise/respond to API presentation or issuance. | None | Ignored | W; settings and both API-flow tests. | A. |
| [Annex II(12) / ISO/IEC 18013-ADD-API-05](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Cross-device API flow verifies close proximity through secure direct user-mediated local channel. | None | Ignored | W; proximity/relay-resistance tests. | A. |
| [Annex II(12) / NOTE](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | CTAP 2.3 BLE can check proximity and transport data; underlying layers should prefer local check/transfer over Hybrid tunnel. | None | Ignored | Context; platform transport evidence if deployed. | A; preference is not rewritten as unconditional wallet duty. |

### Annex II — points (13)–(20): OpenID4VC-HAIP profile

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex II(13) / OIDFVP-HAIP-SUPPORT-02](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Wallet meets clause 6.5. | None | Ignored | W; HAIP API profile tests. | A. |
| [Annex II(13) / OIDFVP-HAIP-SUPPORT-03](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Wallet should not support redirect-based cross-device presentations under clause 6.4. | None | Ignored | W; flow-support/security decision. | A; recommendation. |
| [Annex II(13) / NOTE 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Redirect flow vulnerable to attacks such as session fixation; relying parties mitigate; implementation alone does not trigger non-compliance. | None | Ignored | Context; threat model if W changes. | A; preserve explicit qualification. |
| [Annex II(13) / OIDFVP-HAIP-SUPPORT-05](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Relying party meets clause 6.5 requirements. | None | Ignored | W; relying-party API profile tests. | A. |
| [Annex II(13) / NOTE 5](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this note. | None | Ignored | Context. | A. |
| [Annex II(14) / OIDFVP-HAIP-GEN-01](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Apply mandatory HAIP clauses 5, 5.3, 7, 8 requirements. | None | Ignored | W; referenced HAIP text and tests. | A. |
| [Annex II(14) / NOTE 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Section 5 means directly under heading, not automatically 5.1/5.2/5.3. | None | Ignored | Context; 5.3 separately included by preceding rule. | A. |
| [Annex II(14) / OIDFVP-HAIP-GEN-03](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Annex modifications prevail over external HAIP requirements. | None | Ignored | W; profile precedence mapping. | A. |
| [Annex II(14) / NOTE 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Modifications can make optional requirements mandatory or extend them. | None | Ignored | Context. | A. |
| [Annex II(14) / OIDFVP-HAIP-GEN-04](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | For ISO-format attestations wallets/relying parties comply with HAIP section 6 ISO mdocs profile. | None | Ignored | W; mdoc format/profile tests. | A. |
| [Annex II(14) / NOTE 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | ISO mdocs profile entails applicable reference [7] Annex B.2 requirements. | None | Ignored | W; external reference chain not inspected. | A. |
| [Annex II(14) / OIDFVP-HAIP-GEN-05](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | For reference [2] format comply with HAIP section 6 IETF SD-JWT VCs profile. | None | Ignored | W; SD-JWT profile tests and external texts. | A. |
| [Annex II(14) / NOTE 4](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | SD-JWT profile entails reference [7] Annex B.3 and HAIP section 6.1. | None | Ignored | W; external reference chain not inspected. | A. |
| [Annex II(15) / OIDFVP-HAIP-COMMON-REQ-01](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-02](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-03](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-04](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-05](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-06](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-07](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-08](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-09](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-10](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-11](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-12](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(16) / Note 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this note. | None | Ignored | Context. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-13](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Include registration certificate in a `verifier_info` element. | None | Ignored | W; request metadata tests. | A. |
| [Annex II(16) / OIDFVP-HAIP-COMMON-REQ-RO-23](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | OpenID4VP 5.9.3 `x509_hash` leaf certificate must be RP access certificate under ETSI TS 119 475 [14]. | None | Ignored | W; access certificate/client identifier tests and external standard. | A. |
| [Annex II(17) / OIDFVP-HAIP-COMMON-RESP-01](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(18) / OIDFVP-HAIP-REDIRECTS-04](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this requirement. | None | Ignored | Context. | A. |
| [Annex II(18) / NOTE](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Void this note. | None | Ignored | Context. | A. |
| [Annex II(19) / OIDFVP-HAIP-ADD-API-01](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Default disclosure of stored EAA types to HAIP API, not attributes/values. | None | Ignored | W; API payload/minimisation tests. | A. |
| [Annex II(19) / OIDFVP-HAIP-ADD-API-04](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Global setting disables stored-EAA disclosure; then should enable selection of individual attestations for disclosure. | None | Ignored | W; global/per-attestation UX and API tests. | A; differs from ISO profile switch wording. |
| [Annex II(19) / OIDFVP-HAIP-ADD-API-05](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | Verify close proximity in cross-device API flow via secure direct user-mediated local channel. | None | Ignored | W; proximity/relay tests. | A. |
| [Annex II(19) / NOTE 5](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | CTAP 2.3 BLE proximity/local transport capabilities; underlying layers should prefer local path over Hybrid tunnel. | None | Ignored | Context; platform transport evidence if deployed. | A. |
| [Annex II(20) / OIDFVP-HAIP-ISO/IEC_18013_5_REQ-02](https://eur-lex.europa.eu/eli/reg_impl/2024/2982/oj#anx_2) | All applicable DeviceRequest/DeviceResponse requirements also apply to API-transmitted ISO mdoc presentation under reference [16] C.1. | None | Ignored | W; cross-profile structure tests. | A. |

## Coverage and limitations

1. **Complete official text obtained:** the original English OJ XHTML `L_202402982EN.000101.fmx.xml` (via the Publications Office link above), including preamble, **11 recitals, Articles 1–8, closing block, 11 bibliographic footnotes and the complete two-entry original Annex**. Direct shell/web-fetch requests to EUR-Lex were challenged/empty; the browser verified the original title, publication and current-consolidation marker. The Publications Office returned the full English XHTML, not merely search snippets or a draft.
2. **Amendment checked against complete official text:** `L_202601731EN.000101.fmx.xml` was retrieved in full. Article 4 was checked for all **eight amendment points**, including Article 3's seven lettered changes and Article 5's two lettered changes. Annexes **XI and XII** were read completely to reconstruct current Annexes I and II. The effective date was checked against amending Article 5 and the 22 July 2026 OJ publication. Other amended acts and the amending act's own recitals are outside this base-act document and belong in their separate documents.
3. **Enumeration:** every original recital; Article 1 points (1)–(4) and concluding unnumbered text; all **16 definitions**; Article 3 points (1)–(10), including deleted (2)/(8), both current (9)(a)–(b) and historical (9)(a)–(c); Article 4 paragraphs 1–3 with every (2)(a)–(d) and (3)(a)–(d); Article 5 paragraphs 1–5 including deleted 5; Article 6 paragraphs 1–3; Article 7 paragraphs 1–3; original and replacement Article 8 unnumbered paragraphs; both historical annex references. Current Annex I covers its opening incorporation rule and **points 1–4**, every adaptation/void note. Current Annex II covers both opening incorporation paragraphs and **points (1)–(20)**, every embedded letter, dash entry, named requirement, note and normative-reference entry. The RequestInfo CDDL block is assessed with REQ-05 rather than treated as a separate duplicated duty. Numbered parent introductions and external-clause headings are structural where they merely introduce assessed subunits.
4. **Historical comparison:** replacement points are assessed at their surviving provision labels, with the original difference identified in the notes; substantive deleted units remain labelled historical. This is not a second full reproduction of superseded wording. The original recital wording is retained as interpretative context, not silently rewritten to match the amendment.
5. **No unavailable portion of the original act or relevant published amendment annexes is filled in from inference.** The consolidated complete text was not independently retrieved; reconstruction rests on the published original and amendment, not a claimed consolidation audit. No exhaustive subsequent-corrigendum search certification is given. Bibliographic footnotes and formal adoption text are source apparatus, not separately invented obligations; the closing block has a context row.
6. **External works not inspected:** ETSI TS 119 472-3 V1.1.1, ETSI TS 119 472-2 V1.2.1, ETSI TS 119 475 and their referenced chains; ISO/IEC 18013-5, ISO/IEC TS 18013-7:2024, ISO/IEC 18013-7:2025 including Annex C, ISO 639; HAIP, OpenID4VP and CTAP specifications. The incorporated standards are separate works. Only the adaptations actually printed in the legislative annexes are enumerated here; no full ETSI/ISO conformity or complete standard-clause coverage is claimed.
7. **Outstanding evidence:** no hypothetical wallet role/deployment decision, provider/relying-party contracts or registration, wallet certification, credential issuer documentation, external standard texts, wallet risk/security policy, platform interoperability matrix, consent/overasking UX demonstrations, national reporting-procedure mapping, or wallet issuance/presentation/erasure/reporting tests was inspected. Current absence of a wallet role is the confirmed scope supplied in the register, not the result of a new organisational or binary audit. Ordinary Autogram PDF validation is not credited as any wallet authentication or presentation measure.
