# Commission Implementing Regulation (EU) 2025/849 — submission of information on certified European Digital Identity Wallets

## Executive summary

This act lays down the **formats and procedures for Member States to submit information to the Commission and the European Digital Identity Cooperation Group for the list of certified European Digital Identity Wallets** under Article 5d of Regulation (EU) No 910/2014. Its only addressees are Member States (as submitters) and the Commission/Cooperation Group (as recipients). It does not regulate document signing, signature creation or signature validation, and it imposes no duty on software publishers.

Under the confirmed scope, Autogram is neither a Member State, a wallet provider, a wallet unit, a provider of person identification data, nor the Commission. It is a desktop file signing/verification application with a local API and CLI ([README.md, lines 4–19](../../README.md#L4-L19)). The submission obligations in Articles 3 and the Annex are therefore scoped out rather than reported as completed or breached. The definitions in Article 2 describe wallet actors Autogram is not, and the act creates no recourse or interoperability input the application consumes. A future wallet-related deployment would require a fresh role assessment; neither local signing nor reading a user's signing certificate establishes a wallet role.

The original act was adopted on **6 May 2025**, published in the Official Journal on **7 May 2025** and entered into force on **27 May 2025** (twentieth day after publication). **No consolidated version and no amending act were found** in the official records consulted. There is no deferred application date and no Autogram release deadline. No Member State submission channel, wallet-list format or organisational role was audited. This is an engineering/legal reading aid, not legal advice or certification.

## Source and version

- **A — original and only authoritative English act:** [ELI / complete OJ text](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj/eng), CELEX **32025R0849**, ELI `http://data.europa.eu/eli/reg_impl/2025/849/oj`; Commission Implementing Regulation (EU) 2025/849 of **6 May 2025**; published **7 May 2025**, OJ L, 2025/849. Article 4 makes entry into force the twentieth day following publication: **27 May 2025**. No separate application or transitional provision.
- **No current consolidation found.** The Publications Office returned `Resource ... id '02025R0849' not found`, i.e. there is no `02025R0849-<date>` consolidated English text at the access date. No amending act for 2025/849 was identified in the retrieved records; this is not a guarantee against unindexed later publications.
- **Anchor scheme (verified against the retrieved source):** the official HTML exposes `#art_1`–`#art_4`, `#rct_1`–`#rct_6` and `#anx_1` for the single unnumbered Annex. This document links every article paragraph and letter, every recital and every Annex point to `https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#<anchor>`. Because the Annex is a single unnumbered annex, its publisher id is `#anx_1` (not a Roman numeral); all Annex rows use it.
- Complete English HTML was retrieved from the **official Publications Office** using language/content negotiation: [A](https://publications.europa.eu/resource/celex/32025R0849). EUR-Lex direct page fetches returned empty responses in this environment; the Cellar record supplied above is authoritative and carries the publisher's ELI. The full title, recitals, articles, Annex and signature block were all present; nothing had to be inferred from a secondary summary.
- **Access/assessment date:** 8 October 2026. **Repository baseline:** `5ca91d5c`. Scope and shared facts: [register README](README.md#L3-L14).

### Evidence and table conventions

**E1:** [`README.md`, lines 4–19](../../README.md#L4-L19), inspected: Autogram is a desktop application for signing and verifying documents under eIDAS, driven directly, via an HTTP API or from the CLI. **E2:** [`SigningKey.sign`, lines 24–29](../../src/main/java/digital/slovensko/autogram/core/SigningKey.java#L24-L29), inspected: signs data with the user's selected key-store/token; it creates no wallet, PID or wallet-submission artefact. **E3:** confirmed organisational facts in [register README, lines 3–14](README.md#L3-L14): no hosted document-processing service, no qualified trust-service provider role, no wallet role. **E4:** [`SignatureValidator`, lines 54–133](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java#L54-L133), inspected: the application validates signatures against the EU List of Trusted Lists and national trusted lists via DSS — the eIDAS signature-validation trust framework, which is distinct from the Article 5d list of certified wallets this act governs. **E5:** [`CertificatesEndpoint.handle`, lines 17–29](../../src/main/java/digital/slovensko/autogram/server/CertificatesEndpoint.java#L17-L29), inspected: the application's "certificates" endpoint lists the **user's own signing-token certificates** after a consent prompt; it exposes no wallet-list or Member State submission data.

Rows cite **A** with their exact provision identifier. **S** means the requirement addresses a Member State, the Commission, a wallet provider or a provider of person identification data, none of which Autogram is under E1–E5. **F** names the evidence that *would* be needed if a future deployment introduced such a role: the national submission mandate/channel, the wallet solution and eID scheme descriptions, liability and supervisory arrangements, and the Article 5c certificate and certification assessment report. Neither S nor F is a present compliance gap. Recitals and Article 2 definitions are interpretative context rather than software tasks; they are classified by relevance and given a reason rather than marked Done.

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or out of the confirmed scope; never "knowingly disregarded"). Every provision row links to the official text at that unit (act anchors `#art_N`, recitals `#rct_N`, Annex `#anx_1`); structural/preamble labels link to the [act page](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj).

## Provision-by-provision assessment

### Recitals (A)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Recital 1](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#rct_1) | European Digital Identity Framework builds a secure, interoperable identity ecosystem with wallets as its cornerstone. | Indirect | Ignored | A; interpretative purpose, E1 boundary. | 2025 preamble. |
| [Recital 2](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#rct_2) | GDPR (2016/679) and, where relevant, ePrivacy Directive 2002/58 apply to processing under this act. | Indirect | Ignored | A; not a GDPR exemption for independent software/support processing; separate data-flow review needed. | Context, not a new legal basis. |
| [Recital 3](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#rct_3) | Commission establishes formats/procedures for Member States to submit and update wallet information; submissions at least in English. | Indirect | Ignored | A; use actual addressees (Commission/MS), not a vendor duty. | Transparency/accessibility rationale. |
| [Recital 4](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#rct_4) | Submitted information on certified wallets should cover solution, eID scheme, responsible authorities, supervisory/liability regimes, suspension/revocation and certification; secure channel avoids duplicate submissions. | None | Ignored | A; S: not a Member State submitter or the Commission. | Rationale for Annex content; not an independent duty. |
| [Recital 5](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#rct_5) | EDPS consulted under Regulation 2018/1725 Art. 42(1); opinion delivered 31 January 2025. | Indirect | Ignored | A; institutional procedural record. | Opinion 31 January 2025. |
| [Recital 6](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#rct_6) | Measures accord with the opinion of the eIDAS Article 48 committee. | Indirect | Ignored | A; institutional procedural record. | No software task. |

### Articles 1–2 — subject matter and definitions (A)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 1, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_1) | Lays down formats/procedures for Member State submissions to the Commission and Cooperation Group for the certified-wallet list under Art. 5d of 910/2014, updated against technology and the ARF. | Indirect | Ignored | A; E1–E3 establish that ordinary local signing/validation is outside this activity. | Effective 27 May 2025. |
| [Art. 2, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_2) | The following definitions apply for this regulation. | Indirect | Ignored | A; interpretative rule. | No implementation measure. |
| [Art. 2(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_2) | 'Wallet solution' is software, hardware, services, settings and configurations, including wallet instances and secure cryptographic applications/devices. | Indirect | Ignored | A; E2/E5 do not establish such a combination. | Definition. |
| [Art. 2(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_2) | 'Wallet instance' is the configured app on a user device that is part of a wallet unit. | Indirect | Ignored | A; a signing application is not thereby part of a wallet unit. | Definition. |
| [Art. 2(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_2) | 'Wallet unit' is a provider-supplied unique configuration of wallet instances and secure components for one user. | Indirect | Ignored | A; E1–E3 do not establish wallet supply. | Definition. |
| [Art. 2(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_2) | 'Wallet provider' is a natural or legal person providing wallet solutions. | Indirect | Ignored | A; no wallet solution is supplied. | Definition. |
| [Art. 2(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_2) | 'Wallet user' is a user in control of the wallet unit. | Indirect | Ignored | A; a local signatory is not thereby a wallet user. | Definition. |
| [Art. 2(6)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_2) | 'Wallet secure cryptographic application' manages critical assets through the secure cryptographic device. | Indirect | Ignored | A; E2 token use alone does not confer this role. | Definition. |
| [Art. 2(7)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_2) | 'Wallet secure cryptographic device' is a tamper-resistant environment protecting critical assets. | Indirect | Ignored | A; no claim that Autogram or a smart-card reader is such a device. | Definition. |
| [Art. 2(8)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_2) | 'Critical assets' are wallet-unit assets whose compromise seriously undermines reliance on the wallet unit. | Indirect | Ignored | A; wallet-specific risk concept, not general app certification. | Definition. |
| [Art. 2(9)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_2) | 'Provider of person identification data' issues/revokes PID and cryptographically binds it to a wallet unit. | Indirect | Ignored | A; E2 signs documents, not PID. | Definition. |

### Article 3 — submission format and procedure (A)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 3(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_3) | Member States shall submit the Annex information to the Commission and Cooperation Group via the Commission's secure electronic channel. | None | Ignored | S; A; future F: national submission mandate and channel credentials if a Member State role ever arose. | Member State duty, not a vendor duty. |
| [Art. 3(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_3) | Member States shall submit the required information at least in English. | None | Ignored | S; A; future F: submission language/policy records. | Member State duty. |
| [Art. 3(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_3) | On any change, including certification-status changes, Member States shall submit updated information through the same channel. | None | Ignored | S; A; future F: change-notification process and records. | Update duty on Member States. |

### Article 4 and final clause (A)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 4, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_4) | Entry into force on the twentieth day following OJ publication. | Indirect | Ignored | A; legal commencement, not a software control. | 27 May 2025. |
| [Final unnumbered operative paragraph, following Art. 4](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#art_4) | Binding in entirety and directly applicable in all Member States. | Indirect | Ignored | A; territorial/legal effect does not create a wallet-vendor obligation. | No general certification obligation. |

### Annex — purpose and wallet information, point 1 (A, `#anx_1`)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex, chapeau](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | "Information to be submitted by Member States". | None | Ignored | S; A; parent framing of the Member State submission, not a separate duty. | Structural heading. |
| [Annex point 1, introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | State the purpose of the submission, one of the following options. | None | Ignored | S; A; trigger framing for point 1(a)–(c). | Not an extra submission item. |
| [Annex point 1(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Purpose: a provided and certified wallet. | None | Ignored | S; A. | Submission purpose option. |
| [Annex point 1(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Purpose: a change to previously submitted wallet information. | None | Ignored | S; A. | Submission purpose option. |
| [Annex point 1(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Purpose: a request to remove a wallet from the list. | None | Ignored | S; A. | Submission purpose option. |

### Annex point 2(a) — description of the wallet solution (A, `#anx_1`)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex point 2, introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Information to be submitted about a provided and certified wallet. | None | Ignored | S; A; parent framing. | Structural heading. |
| [Annex point 2(a), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | A description of the wallet solution, including the following. | None | Ignored | S; A; parent framing. | Summary of point 2(a) indents. |
| [Annex point 2(a), unnumbered indent 1](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | The name of the wallet solution. | None | Ignored | S; A; future F: wallet solution identity record. | Descriptive field. |
| [Annex point 2(a), unnumbered indent 2](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | A unique reference identifier of the wallet solution. | None | Ignored | S; A; future F: identifier scheme and value. | Descriptive field. |
| [Annex point 2(a), unnumbered indent 3](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Name, trade name where applicable, address and, where relevant, additional information on the wallet provider. | None | Ignored | S; A; future F: provider identity details. | Descriptive field. |
| [Annex point 2(a), unnumbered indent 4, introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | A description of the management of the wallet solution, including the following. | None | Ignored | S; A; parent framing for sub-indents 1–6. | Management description. |
| [Annex point 2(a), indent 4, sub-indent 1](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Characteristics and design. | None | Ignored | S; A; future F: design documentation. | Management sub-item. |
| [Annex point 2(a), indent 4, sub-indent 2](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Issuance, delivery and activation. | None | Ignored | S; A; future F: lifecycle procedures. | Management sub-item. |
| [Annex point 2(a), indent 4, sub-indent 3](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Suspension, revocation and reactivation. | None | Ignored | S; A; future F: status-management procedures. | Not certificate CRL/OCSP. |
| [Annex point 2(a), indent 4, sub-indent 4](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Recovery and backup, where applicable. | None | Ignored | S; A; future F: recovery/backup arrangements. | Conditional on applicability. |
| [Annex point 2(a), indent 4, sub-indent 5](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Renewal and replacement. | None | Ignored | S; A; future F: renewal procedures. | Management sub-item. |
| [Annex point 2(a), indent 4, sub-indent 6](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Management practices for wallet transaction logs. | None | Ignored | S; A; future F: log-management policy. | Wallet-specific, not app logs. |

### Annex point 2(b) — description of the electronic identification scheme (A, `#anx_1`)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex point 2(b), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | A description of the eID scheme under which the wallet solution is provided and certified, including the following. | None | Ignored | S; A; parent framing. | Structural heading. |
| [Annex point 2(b), unnumbered indent 1](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | The title of the electronic identification scheme. | None | Ignored | S; A. | Descriptive field. |
| [Annex point 2(b), unnumbered indent 2](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | A unique identifier for the electronic identification scheme. | None | Ignored | S; A. | Descriptive field. |
| [Annex point 2(b), unnumbered indent 3](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | The name of the authority or names of the authorities responsible for the eID scheme. | None | Ignored | S; A; future F: authority identification. | Governmental actors. |
| [Annex point 2(b), unnumbered indent 4](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | A description of the management and organisation of the eID scheme. | None | Ignored | S; A. | Descriptive field. |
| [Annex point 2(b), unnumbered indent 5](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Name, trade name where applicable, address and, where relevant, additional information on the provider(s) of person identification data. | None | Ignored | S; A; future F: PID provider identity details. | Descriptive field. |
| [Annex point 2(b), unnumbered indent 6, introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | For each provider of person identification data, the following. | None | Ignored | S; A; parent framing for sub-indents 1–2. | Per-provider block. |
| [Annex point 2(b), indent 6, sub-indent 1](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | The set or sets of person identification data provided to natural and legal persons under the eID scheme. | None | Ignored | S; A; future F: PID set definitions. | Descriptive field. |
| [Annex point 2(b), indent 6, sub-indent 2](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | A description of the set or sets of PID for which the Member State ensures uniqueness. | None | Ignored | S; A; future F: national uniqueness policy. | Member State duty. |
| [Annex point 2(b), unnumbered indent 7, introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | A description of the supervisory regime under Art. 46a of 910/2014, as regards the following. | None | Ignored | S; A; parent framing for sub-indents 1–2. | Supervisory description. |
| [Annex point 2(b), indent 7, sub-indent 1](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | The providers of person identification data, including identification of the supervisory body. | None | Ignored | S; A; future F: supervisory-body identification. | Supervisory sub-item. |
| [Annex point 2(b), indent 7, sub-indent 2](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | The wallet provider, including identification of the supervisory body. | None | Ignored | S; A; future F: supervisory-body identification. | Supervisory sub-item. |
| [Annex point 2(b), unnumbered indent 8, introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | A description of the liability regime under Art. 5a(19) of 910/2014, as regards the following. | None | Ignored | S; A; parent framing for sub-indents 1–2. | Liability description. |
| [Annex point 2(b), indent 8, sub-indent 1](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | The provider or providers of person identification data. | None | Ignored | S; A; future F: liability arrangements. | Liability sub-item. |
| [Annex point 2(b), indent 8, sub-indent 2](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | The wallet provider. | None | Ignored | S; A; future F: liability arrangements. | Liability sub-item. |
| [Annex point 2(b), unnumbered indent 9, introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | A description of the enrolment process, including descriptions of the following. | None | Ignored | S; A; parent framing for sub-indents 1–4. | Enrolment description. |
| [Annex point 2(b), indent 9, sub-indent 1](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Applying for wallet units and person identification data. | None | Ignored | S; A; future F: enrolment procedures. | Enrolment sub-item. |
| [Annex point 2(b), indent 9, sub-indent 2](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | The registration of wallet users. | None | Ignored | S; A; future F: registration procedures. | Enrolment sub-item. |
| [Annex point 2(b), indent 9, sub-indent 3](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Where applicable, the identity proofing and verification of natural persons. | None | Ignored | S; A; future F: identity-proofing procedures. | Conditional on applicability; cf. 2015/1502. |
| [Annex point 2(b), indent 9, sub-indent 4](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Where applicable, the identity proofing and verification of legal persons. | None | Ignored | S; A; future F: identity-proofing procedures. | Conditional on applicability. |
| [Annex point 2(b), unnumbered indent 10, introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Any policies applying to the authorities responsible for the eID scheme, including arrangements for suspension or revocation of the following. | None | Ignored | S; A; parent framing for sub-indents 1–3. | Policy description. |
| [Annex point 2(b), indent 10, sub-indent 1](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | The electronic identification schemes. | None | Ignored | S; A; future F: suspension/revocation arrangements. | Policy sub-item. |
| [Annex point 2(b), indent 10, sub-indent 2](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Wallet unit attestations issued by the wallet provider. | None | Ignored | S; A; future F: attestation-revocation arrangements. | Policy sub-item. |
| [Annex point 2(b), indent 10, sub-indent 3](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | Any other compromised parts of the wallets. | None | Ignored | S; A; future F: compromise-management arrangements. | Catch-all sub-item. |

### Annex point 2(c) — certification evidence (A, `#anx_1`)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex point 2(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/849/oj#anx_1) | The certificate and certification assessment report in compliance with Art. 5c of Regulation (EU) No 910/2014. | None | Ignored | S; A; future F: Article 5c certificate and assessment report. | Distinct from the application's ordinary signature validation (E4). |

## Coverage and limitations

- **Text obtained in full:** the original and only English act A, including its complete preamble, four articles, unnumbered Annex and signature block. No consolidated version exists to compare (Cellar returned "not found" for `02025R0849`), and no amending act was found in the records consulted; the act has one textual version since entry into force on 27 May 2025.
- **Enumerated units assessed:** **6 recitals**; **4 articles** comprising **16 assessed units** — Article 1's single unnumbered paragraph; Article 2's unnumbered introductory paragraph plus **9 definitions**; Article 3's **3 numbered paragraphs**; Article 4's entry-into-force paragraph plus the final binding/applicability paragraph. Structural article headings are not counted as additional duties.
- **Annex:** a single unnumbered Annex with **42 assessed units** — chapeau; point 1 introductory text and **3 letters (a)–(c)**; point 2 introductory text; point 2(a)'s introductory text, **3 named indents**, a management introductory text and **6 sub-indents**; point 2(b)'s introductory text, **5 named indents**, then four further lettered blocks each with its own introductory text and, respectively, **2 + 2 + 2 + 4 + 3 sub-indents**; and point 2(c). Every numbered point, letter and `—` indent is individually identifiable; parent framing is labelled without being treated as a separate duty.
- **Total assessed provision rows:** **64** (6 recitals + 16 article units + 42 Annex units).
- **Checking method:** the official Publications Office XHTML was retrieved and parsed to enumerate recitals, article paragraphs, definitions, Annex points and indents; the anchor ids `#art_1`–`#art_4`, `#rct_1`–`#rct_6` and `#anx_1` were read directly from that source; adoption (6 May 2025), publication (7 May 2025) and the twentieth-day entry into force (27 May 2025) were taken from the retrieved text. No substantive part of the act was unavailable or deliberately omitted. Footnote bibliographic citations and signature/publication identifiers were read as sources, not counted as separate obligations.
- **External works not inspected:** the cross-referenced Regulation (EU) No 910/2014 (especially Arts. 5a, 5c, 5d, 46a and 48), Regulation (EU) 2016/679, Directive 2002/58/EC, Regulation (EU) 2018/1725 and Recommendation (EU) 2021/946, and the Architecture and Reference Framework itself. These are separate works with their own registers; their technical or substantive requirements are not silently imported here. No standards were reproduced.
- **Outstanding evidence:** if a wallet-related role ever arises, the evidence needed would be the national submission mandate and channel, the wallet solution and eID scheme descriptions, supervisory/liability/enrolment arrangements, and the Article 5c certificate and certification assessment report (the F items above). None is a present missing statutory measure for Autogram. Repository evidence is narrow (E1–E5) and no released binary, complete codebase role audit or organisational records were examined; the application's trusted-list signature validation (E4) must not be conflated with the Article 5d certified-wallet list. The known broader uncertainties (eIDAS Art. 15 end-user product, CRA commercial supply, EU-wide signature correctness) are not resolved or exempted by this wallet-submission-specific exclusion.
