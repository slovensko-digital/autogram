# Commission Implementing Regulation (EU) 2025/1569 — qualified electronic attestations of attributes

**Register:** [Autogram legal-act register](README.md) · **Assessment date:** 8 October 2026 · **Repository baseline:** `5ca91d5c`. **Amended by:** [2026/1735](eu-reg_impl-2026-1735.md).

## Executive summary

**What this act is.** Implementing rules for Regulation (EU) No 910/2014 on **qualified electronic attestations of attributes** and electronic attestations of attributes issued by or on behalf of a public sector body responsible for an authentic source. It fixes reference standards (Annex I), issuance technical specifications (Annex II), notification/publication duties for Member States and the Commission (Articles 5–8, Annex III), revocation duties on providers (Article 4) and attribute-verification mechanisms (Article 9).

**Relevance to Autogram: None (context).** Autogram is a desktop/API/CLI application that **signs and validates documents** (XAdES/CAdES/PAdES/ASiC) in accordance with eIDAS; it does **not issue, revoke, catalogue or verify electronic attestations of attributes**, operate a wallet or act as a qualified trust-service provider (**A**, **S**, **K**). Every operative duty in this act falls on attestation providers, Member States or the Commission. No Autogram code path implements any of them. Interpretative recitals and definitions are marked **Indirect / Ignored**; only recital (15) is **Indirect / Unknown** as a pointer to the [GDPR](eu-reg-2016-679.md).

**Established evidence.** Repository inspection confirms zero occurrences of "attestation", "wallet" or "EUDI" in `src/main/java` and `src/test/java`; the signing path produces document signatures only and the local HTTP API exposes signing/validation endpoints, not attestation issuance. This is an engineering reading aid, **not legal advice, certification or a released-binary audit**.

**Major unknowns / dates.** Adopted **29 July 2025**, published **30 July 2025**, in force **19 August 2025**; **Articles 6–9 apply from 19 August 2026**. Amending Implementing Regulation (EU) **2026/1735** updates the applicable standards and amends Article 3(2), Article 4 and Article 9 ([register entry](eu-reg_impl-2026-1735.md)); the text assessed below is the **original 2025 version**, not a consolidation. The applicable ETSI and 2024/2979 standards were **not** inspected. Assessment/source access: **8 October 2026**.

## Source and version

- Official complete English act, including footnotes and all three annexes: [Publications Office / Cellar XHTML](https://publications.europa.eu/resource/celex/32025R1569?language=eng), [EUR-Lex ELI/OJ page](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj).
- **CELEX 32025R1569**, OJ L, 2025/1569, **30.7.2025**; ELI `http://data.europa.eu/eli/reg_impl/2025/1569/oj`. The **English** version is an official language version; the tables below are engineering/legal paraphrases, not replacement legal text.
- **Text obtained:** the official English XHTML of the original published act (HTTP 200, ~98 KB, fully legible) from the Publications Office/Cellar on 8 October 2026. All 18 recitals, Articles 1–11, the closing formula, Annexes I–III and the legislative footnotes were readable. The `eur-lex.europa.eu/eli/.../oj` page returned an empty/HTTP 202 response to a direct request on the same date; EUR-Lex in-force status and the consolidation were therefore not independently re-verified from that page.
- **Version assessed:** original published version, not a consolidation. Entry into force on the twentieth day following publication: **19 August 2025**. **Articles 6 to 9 apply from 19 August 2026** (recital (16): 12 months after entry into force). No separate deferred application date for other articles appears.
- **Amendments / relationships:** implements Regulation (EU) No 910/2014, Articles 45d(5), 45e(2), 45f(6) and 45f(7), in the framework amended by Regulation (EU) 2024/1183. **Amending act:** Commission Implementing Regulation (EU) **2026/1735** of 15 July 2026 (CELEX 32026R1735), which updates the applicable standards and amends Article 3(2), Article 4 and Article 9 — see [eu-reg_impl-2026-1735.md](eu-reg_impl-2026-1735.md). No corrigendum was identified in the obtained text; a complete subsequent amendment-history check remains outstanding.
- **Anchor scheme (verified in the retrieved XHTML):** recitals `#rct_1`–`#rct_18`, articles `#art_1`–`#art_11`, annexes `#anx_I`, `#anx_II`, `#anx_III`. Provision links below use these anchors on the [official OJ page](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj).

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored. `None` here means the provision addresses other actors/activities under the confirmed scope (attestation providers, Member States, the Commission). `Ignored` means deliberately scoped out as not applicable, with the reason given — never "knowingly disregarded". Relevance and status are independent.

Every provision label links to the authoritative text at that unit: `#art_1`–`#art_11`, `#anx_I`–`#anx_III` and `#rct_1`–`#rct_18` on the [official OJ page](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj).

## Evidence key

All evidence was inspected on 8 October 2026. Source inspection is not a released-binary audit or proof that tests pass; **no builds or tests were run**.

- **A — no attribute-attestation activity (scope fact):** `grep -ri "attestation|wallet|eudi" src/main/java src/test/java` returns **no matches**. [README.md](../../README.md), line 3, describes Autogram as a document **signing and verification** application. No issuance, revocation, catalogue or verification-point feature exists.
- **S — signing path:** [SigningJob.java](../../src/main/java/digital/slovensko/autogram/core/SigningJob.java), `signWithKeyAndRespond`, lines 86–108, and `createSignatureService`, lines 131–146, create XAdES/CAdES/PAdES/ASiC signatures over **input documents**. [Autogram.java](../../src/main/java/digital/slovensko/autogram/core/Autogram.java), `sign`, lines 42–45, `signCommonAndThen`, lines 107–128, `sign(job, key)`, lines 130–144.
- **K — signing keys/tokens:** [TokenDriver.java](../../src/main/java/digital/slovensko/autogram/drivers/TokenDriver.java); [PKCS11TokenDriver.java](../../src/main/java/digital/slovensko/autogram/drivers/PKCS11TokenDriver.java); [PKCS12KeystoreTokenDriver.java](../../src/main/java/digital/slovensko/autogram/drivers/PKCS12KeystoreTokenDriver.java); [FakeTokenDriver.java](../../src/main/java/digital/slovensko/autogram/drivers/FakeTokenDriver.java); [Autogram.java](../../src/main/java/digital/slovensko/autogram/core/Autogram.java), `fetchKeysAndThen`, lines 230–241. A signing key/certificate is not an attestation of attributes.
- **V — validation:** [SignatureValidator.java](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java), `validate`, lines 76–81, `initialize`, lines 91–133, and SignatureValidatorTrustedListTest.java (as recorded in the [2025/1945 register](eu-reg_impl-2025-1945.md)). Validation covers document signatures, not attribute attestations.
- **R — reports:** [ValidationReports.java](../../src/main/java/digital/slovensko/autogram/core/ValidationReports.java).
- **P — local HTTP API:** [SignEndpoint.java](../../src/main/java/digital/slovensko/autogram/server/SignEndpoint.java), `handle`, lines 21–51 (signs documents); [server.yml](../../src/main/resources/digital/slovensko/autogram/server/server.yml). No attestation endpoint.
- **D — DSS dependency:** [pom.xml](../../pom.xml), line 18 (`dss.version` 6.5), lines 44–100 (DSS signature/validation modules). Dependency selection is not conformity evidence.
- **N — external standards not inspected:** ETSI EN 319 401 v3.1.1 (2024-06) and Annex II of Implementing Regulation (EU) 2024/2979 are separate works, referenced by this act but not inspected here.

## Provision-by-provision assessment

All operative duties below address attestation providers, Member States or the Commission; none is an obligation on a document-signing application. Rows are therefore **None / Ignored** except interpretative recitals/definitions (**Indirect / Ignored**) and recital (15) (**Indirect / Unknown**). Dates marked "Applies" refer to Articles 6–9 from **19 August 2026**.

### Preamble and recitals

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Preamble — institutional opening](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj) | Commission is the adopting institution. | None | Ignored | Official heading; no software duty. | Adopted 29 July 2025. |
| [Preamble — having-regard paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj) | TFEU authority. | None | Ignored | Official preamble; legal basis, not an implementation task. | Original version. |
| [Preamble — having-regard paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj) | Regulation (EU) No 910/2014, Articles 45d(5), 45e(2), 45f(6), 45f(7) provide implementing authority. | None | Ignored | Official preamble; distinguishes attribute-attestation rules from document-signature rules. | Original version; framework amended by 2024/1183. |
| [Recital (1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_1) | Regulation 910/2014 framework; attestations of attributes enable wallet users to share information with relying parties. | Indirect | Ignored | **A**; interpretative purpose, necessarily about the wallet ecosystem Autogram does not operate. | No independent deadline. |
| [Recital (2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_2) | Wallet interfaces under Article 45g underline the importance of attestations. | Indirect | Ignored | **A**; interpretative scope. | Original version. |
| [Recital (3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_3) | Commission should review/update the act, relying on Recommendation (EU) 2021/946 and its Architecture and Reference Framework. | None | Ignored | Commission task, not a software task. | Refers to Regulation 2024/1183 recital 75. |
| [Recital (4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_4) | Scheme-compliance policies and procedures should be part of the conformity assessment under Regulation 910/2014. | None | Ignored | **A**; duty on attestation providers and conformity-assessment bodies. | Context for Article 3(2). |
| [Recital (5)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_5) | Attestations should be revocable, or risk-compensating measures used; revocation policies should be privacy-preserving. | Indirect | Ignored | **A**; interprets Article 4, which binds providers. | Context for revocation duties. |
| [Recital (6)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_6) | Simplified administrative communication; Member States notify relevant attributes, at least in English. | None | Ignored | Member State duty; not a software measure. | Context for Articles 5 and 7. |
| [Recital (7)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_7) | Member States notify public sector bodies; Commission maintains and publishes the list. | None | Ignored | Member State / Commission duty. | Context for Article 6. |
| [Recital (8)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_8) | Commission establishes a catalogue of attributes; registration mandatory for Annex VI attributes, otherwise optional. | None | Ignored | Commission duty. | Context for Article 7. |
| [Recital (9)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_9) | Commission establishes a catalogue of schemes; registration optional; requests by scheme owners. | None | Ignored | Commission / scheme-owner activity. | Context for Article 8. |
| [Recital (10)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_10) | Catalogues should carry minimum semantic/identifier/data-type information and versioning. | None | Ignored | Commission duty. | Context for Articles 7–8. |
| [Recital (11)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_11) | Member States should set up attribute-verification mechanisms for qualified trust service providers. | None | Ignored | Member State duty. | Within the Article 45e(1) time limit. |
| [Recital (12)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_12) | Member States should make verification points available per Annex VI attribute; controls on misuse are permitted. | None | Ignored | Member State duty. | Context for Article 9. |
| [Recital (13)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_13) | Commission should reuse common services under Regulation (EU) 2018/1724 where appropriate. | None | Ignored | Commission duty. | Context for Article 10. |
| [Recital (14)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_14) | Non-qualified trust service providers and other issuers may follow the principles and requirements of this act. | None | Ignored | Voluntary, not an obligation; **A** (Autogram issues no attestations). | No independent task. |
| [Recital (15)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_15) | Regulation (EU) 2016/679 and, where relevant, Directive 2002/58/EC apply to personal-data processing under this act. | Indirect | Unknown | **A**; publisher receives no user documents. Need actor-specific data-flow inventory, legal basis and retention. See [GDPR register](eu-reg-2016-679.md). | Context only; substantive duties arise under the referenced acts. |
| [Recital (16)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_16) | Catalogue and verification-point requirements should become applicable 12 months after entry into force. | Indirect | Ignored | **A**; commencement context for Articles 6–9. | Applies **19 August 2026**. |
| [Recital (17)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_17) | EDPS consultation and opinion. | None | Ignored | Official recital records the institutional consultation. | Opinion 31 January 2025. |
| [Recital (18)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#rct_18) | Measures accord with the committee opinion under Article 48 of Regulation 910/2014. | None | Ignored | Institutional procedure, not an application requirement. | Original version. |

### Article 1 — Subject matter and scope

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 1 — unnumbered opening paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_1) | Lays down reference standards, specifications and procedures for five attribute-attestation areas. | None | Ignored | **A**; scope covers attestation issuance/catalogues/verification, not document signing. | Applies from entry into force unless deferred. |
| [Article 1(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_1) | Qualified electronic attestations of attributes. | None | Ignored | **A**; addresses providers of qualified attestations. | — |
| [Article 1(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_1) | Attestations issued by or on behalf of a public sector body responsible for an authentic source. | None | Ignored | **A**; addresses public-sector issuers. | — |
| [Article 1(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_1) | List of providers of public-sector attestations. | None | Ignored | **A**; Commission list duty. | — |
| [Article 1(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_1) | Catalogue of attributes and catalogue of schemes. | None | Ignored | **A**; Commission catalogues. | — |
| [Article 1(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_1) | Verification of attributes against authentic sources or designated intermediaries. | None | Ignored | **A**; Member State verification mechanisms. | — |

### Article 2 — Definitions

Structural heading: the chapeau introduces thirteen definitions. Each definition is individually identified below; none is a software task.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 2 — chapeau](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | "For the purpose of this Regulation, the following definitions apply". | Indirect | Ignored | **A**; definitions are interpretative context. | Definitions (1)–(13). |
| [Article 2(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'wallet unit' means a unique wallet-solution configuration for an individual user. | Indirect | Ignored | **A**; Autogram is not a wallet and has no wallet code. | Definition. |
| [Article 2(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'wallet user' means a user in control of the wallet unit. | Indirect | Ignored | **A**; no wallet-unit code. | Definition. |
| [Article 2(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'catalogue of attributes' means the Commission-maintained online repository. | Indirect | Ignored | **A**; no catalogue feature. | Definition. |
| [Article 2(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'scheme for the attestation of attributes' means rules for one or more attestation types. | Indirect | Ignored | **A**; no scheme handling. | Definition. |
| [Article 2(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'type of electronic attestation of attributes' means a named, semantically described group. | Indirect | Ignored | **A**; no attestation type handling. | Definition. |
| [Article 2(6)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'catalogue of schemes for the attestation of attributes' means the Commission-maintained repository of schemes. | Indirect | Ignored | **A**; no catalogue feature. | Definition. |
| [Article 2(7)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'wallet solution' means combined software, hardware, services and configurations. | Indirect | Ignored | **A**; not a wallet solution. | Definition. |
| [Article 2(8)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'wallet instance' means the application installed on a user's device. | Indirect | Ignored | **A**; no wallet instance. | Definition. |
| [Article 2(9)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'wallet secure cryptographic application' manages critical assets. | Indirect | Ignored | **A**; Autogram uses PKCS#11/PKCS#12 signing tokens (**K**), not wallet cryptographic applications. | Definition. |
| [Article 2(10)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'wallet secure cryptographic device' means a tamper-resistant device. | Indirect | Ignored | **A**; **K** covers signing tokens, not wallet secure cryptographic devices. | Definition. |
| [Article 2(11)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'wallet provider' means a person providing wallet solutions. | Indirect | Ignored | **A**; Autogram is not a wallet provider. | Definition. |
| [Article 2(12)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'critical assets' means assets whose compromise would debilitate reliance on the wallet unit. | Indirect | Ignored | **A**; no wallet unit. | Definition. |
| [Article 2(13)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_2) | 'owner of a scheme for the attestation of attributes' means the responsible entity. | Indirect | Ignored | **A**; no scheme ownership. | Definition. |

### Article 3 — Issuance of qualified electronic attestations of attributes and public-sector attestations

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 3(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_3) | Attestation providers shall comply with the Annex I reference standards and issue attestations compliant with the Annex II technical specifications. | None | Ignored | **A**; duty on attestation providers. Autogram signs documents (**S**) and issues no attestations. | Amended by 2026/1735 as regards applicable standards. |
| [Article 3(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_3) | Where attestations fall within a registered scheme, providers shall comply with that scheme; their policies/procedures form part of the Regulation 910/2014 conformity assessment. | None | Ignored | **A**; duty on attestation providers and assessment bodies. | Expressly amended by [2026/1735](eu-reg_impl-2026-1735.md). |

### Article 4 — Revocation of attestations

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 4(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_4) | Providers shall keep written, publicly accessible validity/revocation-status policies, including revocation conditions and availability measures. | None | Ignored | **A**; duty on attestation providers. | Amended by 2026/1735. |
| [Article 4(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_4) | Providers shall be the only entities able to revoke the attestations they issue. | None | Ignored | **A**; duty on attestation providers. | — |
| [Article 4(3) — chapeau](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_4) | Attestations valid for more than 24 hours shall be revoked in at least the listed circumstances. | None | Ignored | **A**; duty on attestation providers. This is attestation-validity revocation, not document-signature revocation. | Do not confuse with signature-certificate revocation. |
| [Article 4(3)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_4) | Revoke upon the explicit request of the person to whom the attestation was issued or its subject. | None | Ignored | **A**; attestation-provider duty. | — |
| [Article 4(3)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_4) | Revoke where the provider knows of a compromise of security or trustworthiness. | None | Ignored | **A**; attestation-provider duty. | — |
| [Article 4(3)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_4) | Revoke in other situations required by Union/national law or by the provider's own policies. | None | Ignored | **A**; attestation-provider duty. | Cross-refers to Article 4(1) policies. |
| [Article 4(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_4) | Providers shall set up privacy-preserving revocation techniques hindering linkability/traceability. | None | Ignored | **A**; attestation-provider duty. | — |
| [Article 4(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_4) | Providers shall publish validity/revocation status with ensured integrity and authenticity. | None | Ignored | **A**; attestation-provider duty. | — |

### Article 5 — Notification of public sector bodies

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 5(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_5) | Member States shall submit at least the Annex III information via the Commission's secure notification system. | None | Ignored | **A**; Member State duty. | — |
| [Article 5(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_5) | Member States shall notify any changes to the notified information. | None | Ignored | **A**; Member State duty. | — |
| [Article 5(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_5) | Member States shall notify at least in English; translation is not required where unreasonable. | None | Ignored | **A**; Member State duty. | — |
| [Article 5(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_5) | The Commission may ask Member States for additional information. | None | Ignored | Commission power, not a software duty. | — |

### Article 6 — Publication of the list of public sector bodies

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 6(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_6) | The Commission shall establish, maintain and publish the provider list from Article 5 notifications. | None | Ignored | Commission duty. | Applies from **19 August 2026**. |
| [Article 6(2) — chapeau](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_6) | The Commission shall ensure access under the listed conditions. | None | Ignored | Commission duty. | Applies from 19 August 2026. |
| [Article 6(2)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_6) | Access in signed/sealed machine-readable form and via a human-readable website. | None | Ignored | Commission duty. | — |
| [Article 6(2)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_6) | Access without registration or authentication. | None | Ignored | Commission duty. | — |
| [Article 6(2)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_6) | Access only via state-of-the-art transport-layer security. | None | Ignored | Commission duty. | — |
| [Article 6(3) — chapeau](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_6) | The Commission shall publish, through a secure channel and without undue delay, the listed items. | None | Ignored | Commission duty. | Applies from 19 August 2026. |
| [Article 6(3)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_6) | Publish the technical specifications of the list. | None | Ignored | Commission duty. | — |
| [Article 6(3)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_6) | Publish the URL of the list. | None | Ignored | Commission duty. | — |
| [Article 6(3)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_6) | Publish the certificates used to verify the electronic signature/seal on the list. | None | Ignored | Commission duty. | — |
| [Article 6(3)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_6) | Publish details of mechanisms validating future changes to location/certificates. | None | Ignored | Commission duty. | — |

### Article 7 — Creation and maintenance of the catalogue of attributes

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 7(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | The Commission shall establish/publish the catalogue and a secure request system. | None | Ignored | Commission duty. | Applies from **19 August 2026**. |
| [Article 7(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | The Commission shall assess inclusion/modification requests, taking Cooperation Group advice and interoperability criteria into account. | None | Ignored | Commission duty. | — |
| [Article 7(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | Member States shall request inclusion of Annex VI attributes relying on authentic sources. | None | Ignored | Member State duty. | — |
| [Article 7(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | Member States may request inclusion of other public-sector attributes; private authentic sources may request their own. | None | Ignored | Member State / private-entity activity. | — |
| [Article 7(5) — chapeau](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | A request to include/modify an attribute shall contain at least the following information. | None | Ignored | Requester duty. | Letters (a)–(h). |
| [Article 7(5)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | Identification of the requesting entity. | None | Ignored | Requester duty. | — |
| [Article 7(5)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | Where applicable, reference to the law/practice making the requester a primary or authentic source. | None | Ignored | Requester duty. | — |
| [Article 7(5)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | Whether the request concerns an existing or a new attribute. | None | Ignored | Requester duty. | — |
| [Article 7(5)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | A namespace for the attribute identifier, unique within the catalogue. | None | Ignored | Requester duty. | — |
| [Article 7(5)(e)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | An attribute identifier unique within the namespace, and its version. | None | Ignored | Requester duty. | — |
| [Article 7(5)(f)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | Semantic description of the attribute. | None | Ignored | Requester duty. | — |
| [Article 7(5)(g)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | Data type of the attribute. | None | Ignored | Requester duty. | — |
| [Article 7(5)(h)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | National verification point or a link to a description of how to initiate verification. | None | Ignored | Requester duty. | — |
| [Article 7(6)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | Requests shall be signed or sealed with QES/QSeal or AdES/AdESeal based on a qualified certificate. | None | Ignored | Requester duty; applied by the requesting entity, not Autogram. | Does not oblige Autogram to sign request objects. |
| [Article 7(7)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | The Commission may include the attribute after assessment and completeness verification. | None | Ignored | Commission duty. | — |
| [Article 7(8)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | The sealed catalogue shall be publicly accessible free of charge, in machine- and human-readable forms, with a search function. | None | Ignored | Commission duty. | — |
| [Article 7(9)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | The Commission shall publish the catalogue's technical specifications. | None | Ignored | Commission duty. | — |
| [Article 7(10)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_7) | The Commission shall issue a unique identifier to each registered attribute. | None | Ignored | Commission duty. | — |

### Article 8 — Creation and maintenance of the catalogue of schemes

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 8(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | The Commission shall establish/publish the scheme catalogue and a secure request system. | None | Ignored | Commission duty. | Applies from **19 August 2026**. |
| [Article 8(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | The Commission shall assess requests, taking Cooperation Group advice and interoperability criteria into account. | None | Ignored | Commission duty. | — |
| [Article 8(3) — chapeau](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | A request to include/modify a scheme shall contain at least the following information. | None | Ignored | Scheme-owner duty. | Letters (a)–(i). |
| [Article 8(3)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | The scheme name, unique within the catalogue. | None | Ignored | Scheme-owner duty. | — |
| [Article 8(3)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | Name and contact information of the scheme owner. | None | Ignored | Scheme-owner duty. | — |
| [Article 8(3)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | Status and version of the scheme. | None | Ignored | Scheme-owner duty. | — |
| [Article 8(3)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | Reference to laws, standards or guidelines governing issuance/validation/use within the scheme. | None | Ignored | Scheme-owner duty. | — |
| [Article 8(3)(e)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | Format(s) of the attestations within the scheme. | None | Ignored | Scheme-owner duty. | — |
| [Article 8(3)(f)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | Namespaces, attribute identifiers, semantic descriptions and data types for each attribute in scope. | None | Ignored | Scheme-owner duty. | — |
| [Article 8(3)(g)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | Description of the trust model and governance mechanisms, including revocation. | None | Ignored | Scheme-owner duty. | — |
| [Article 8(3)(h)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | Requirements on providers or information sources, including authentic sources. | None | Ignored | Scheme-owner duty. | — |
| [Article 8(3)(i)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | Whether attestations in scope are qualified, public-sector based, or both. | None | Ignored | Scheme-owner duty. | — |
| [Article 8(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | Schemes shall contain only uniquely identifiable attributes; the request shall be signed/sealed with QES/QSeal or qualified-certificate-based AdES/AdESeal. | None | Ignored | Scheme-owner duty. | Signing duty on requesters, not Autogram. |
| [Article 8(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | The Commission may include the scheme after assessment and completeness verification. | None | Ignored | Commission duty. | — |
| [Article 8(6)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | The sealed scheme catalogue shall be freely accessible, machine- and human-readable, searchable and integrity/authenticity-protected. | None | Ignored | Commission duty. | — |
| [Article 8(7)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | The Commission shall publish the scheme catalogue's technical specifications. | None | Ignored | Commission duty. | — |
| [Article 8(8)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_8) | The Commission shall issue a unique identifier to each registered scheme. | None | Ignored | Commission duty. | — |

### Article 9 — Verification of attributes against authentic sources or designated intermediaries

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 9(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_9) | Member States shall establish attribute-verification mechanisms for qualified trust service providers and publish the request/receipt procedures. | None | Ignored | Member State duty. | Applies from **19 August 2026**. |
| [Article 9(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_9) | The mechanism shall provide an access point; results are returned by the public sector body or intermediary. | None | Ignored | Member State duty. | — |
| [Article 9(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_9) | The verification request shall set out the attributes and the subject's identification data. | None | Ignored | Qualified trust service provider duty. | — |
| [Article 9(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_9) | The result shall state only whether the attribute was verified and name the responsible authentic source. | None | Ignored | Member State / verification-point duty. | — |
| [Article 9(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_9) | Member States may impose access/control mechanisms and shall publish their extent. | None | Ignored | Member State duty. | — |

### Article 10 — Interoperability and reuse

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 10(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_10) | Member States may refer to and reuse the common services under Article 14 of Regulation (EU) 2018/1724. | None | Ignored | Member State activity. | — |
| [Article 10(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_10) | The Commission shall reuse those common services where appropriate. | None | Ignored | Commission duty. | — |

### Article 11 and closing formula

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 11 — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_11) | Entry into force on the twentieth day following OJ publication. | Indirect | Ignored | Official publication date and Article 11; commencement, not a software measure. | **19 August 2025**. |
| [Article 11 — unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_11) | Articles 6 to 9 shall apply from 19 August 2026. | Indirect | Ignored | Commencement context for Articles 6–9 (catalogues, list, verification). | **19 August 2026**. |
| [Article 11 — unnumbered paragraph 3](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_11) | Binding in its entirety and directly applicable in all Member States. | Indirect | Ignored | Official concluding text; no transposition and no software task. | Effective; no national implementing measures. |
| [Closing adoption/signature formula](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#art_11) | Brussels adoption and Commission President attribution. | None | Ignored | Official text; documentary metadata. | 29 July 2025; Ursula von der Leyen. |

### Annex I — List of reference standards and specifications (Article 3)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex I — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_I) | Providers of qualified/public-sector attestations shall issue their attestations according to **ETSI EN 319 401 v3.1.1 (2024-06)** for trust service providers. | None | Ignored | **A**; standard binds attestation providers, not document signers. **N**: the ETSI standard is a separate work and was not inspected. | Fixed edition; Amended by [2026/1735](eu-reg_impl-2026-1735.md). |

### Annex II — Technical specifications for issuance (Article 3)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex II(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_II) | Providers shall issue attestations in a format according to one of the standards listed in Annex II of Implementing Regulation (EU) 2024/2979. | None | Ignored | **A**; provider duty. **N**: the 2024/2979 list is a separate work and was not inspected. | Format requirement, not document-signature format. |
| [Annex II(2) — chapeau](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_II) | For issuance to natural or legal persons, providers shall perform the listed verifications and minimisation. | None | Ignored | **A**; provider duty. | — |
| [Annex II(2)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_II) | Where applicable, verify the requester's right to act on behalf of the attestation subject. | None | Ignored | **A**; provider duty. Distinct from verifying a signer's identity at signing time (**K**). | — |
| [Annex II(2)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_II) | Where applicable, verify the identity of the authentic source of the attributes. | None | Ignored | **A**; provider duty. | — |
| [Annex II(2)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_II) | Process only the minimum set of attributes necessary for issuance and management. | None | Ignored | **A**; provider data-minimisation duty. Not a general data-minimisation rule for document signing. | Do not import as an Autogram processing rule. |
| [Annex II(3) — chapeau](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_II) | Where an attestation is issued to a European Digital Identity Wallet, the provider shall do the following. | None | Ignored | **A**; provider duty. Autogram issues no attestations and operates no wallet. | — |
| [Annex II(3)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_II) | Authenticate to the wallet unit. | None | Ignored | **A**; provider/wallet duty. | — |
| [Annex II(3)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_II) | Verify that the wallet unit is not revoked or suspended. | None | Ignored | **A**; provider/wallet duty. | — |
| [Annex II — footnote (1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_II) | Citation of Implementing Regulation (EU) 2024/2979 (wallet integrity and core functionalities). | None | Ignored | **N**; the cited regulation is a separate act assessed in the shared register, not text of this act. | OJ L, 2024/2979, 4.12.2024. |

### Annex III — Notifications referred to in Article 5

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex III(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_III) | Member States shall notify the public sector body's name/registration number, establishment Member State and the law under which it is responsible/designated. | None | Ignored | Member State duty. | — |
| [Annex III(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_III) | Notify the contact email and phone number. | None | Ignored | Member State duty. | — |
| [Annex III(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_III) | Notify the URL of the webpage with additional information. | None | Ignored | Member State duty. | — |
| [Annex III(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1569/oj#anx_III) | Notify the conformity assessment report specified by Article 45f(3) of Regulation 910/2014. | None | Ignored | Member State duty. | References Regulation 910/2014, a separate act. |

## Coverage and limitations

- **Text obtained.** The official English XHTML of the original act from the Publications Office/Cellar was retrieved in full and enumerated: **18 recitals**, **Articles 1–11** (Article 1 opening + 5 points; Article 2 chapeau + 13 definitions; Article 3: 2 paragraphs; Article 4: 5 paragraphs with 3 letters; Article 5: 4 paragraphs; Article 6: 3 paragraphs with 3 + 4 letters; Article 7: 10 paragraphs with 8 letters; Article 8: 8 paragraphs with 9 letters; Article 9: 5 paragraphs; Article 10: 2 paragraphs; Article 11: 3 unnumbered paragraphs), the **3 preamble units**, the **closing formula**, **Annex I** (1 paragraph), **Annex II** (3 numbered points with 3 + 2 letters, plus footnote (1)) and **Annex III** (4 points). The legislative footnotes (1)–(8) were read as citations.
- **Enumerated counts.** These produce **125 assessment rows**: **21 preamble/recital rows** (3 preamble + 18 recitals), **90 article/concluding rows** (Article 1: 6; Article 2: 14; Article 3: 2; Article 4: 8; Article 5: 4; Article 6: 10; Article 7: 18; Article 8: 17; Article 9: 5; Article 10: 2; Article 11: 3; closing formula: 1) and **14 annex rows** (Annex I: 1; Annex II: 9; Annex III: 4). Every numbered paragraph and every lettered subpoint is individually identifiable; no article's paragraphs are collapsed into a blanket row.
- **Status rationale.** Because Autogram issues no electronic attestations of attributes, operates no wallet and is not a qualified trust-service provider, every operative provision is **None / Ignored** with the reason stated; interpretative recitals and all definitions are **Indirect / Ignored**; recital (15) is **Indirect / Unknown** as a pointer to the [GDPR](eu-reg-2016-679.md). No row is **Done** or **Not done**, because no applicable Autogram obligation is established. This is not a claim that the organisation is exempt from the act's general context, and it does not license ignoring duties that would attach if Autogram began issuing or verifying attribute attestations.
- **Access limitation.** The `eur-lex.europa.eu/eli/reg_impl/2025/1569/oj` HTML page returned an empty/HTTP 202 response on 8 October 2026, so in-force status and consolidation were not independently re-verified from that page; the complete authoritative text was obtained from the Publications Office/Cellar resource. The act has since been amended by [2026/1735](eu-reg_impl-2026-1735.md); the **consolidated text was not assessed** and the amendment is documented separately.
- **External works not inspected.** **ETSI EN 319 401 v3.1.1 (2024-06)** (Annex I) and **Annex II of Implementing Regulation (EU) 2024/2979** (Annex II) are separate works referenced by this act; their full text was **not** obtained and their unmodified clauses are not imported here. The cross-referenced acts (Regulation (EU) No 910/2014, 2024/1183, 2024/2979, 2018/1724, 2016/679, 2018/1725, Directive 2002/58/EC) are not assessed by this document.
- **Code grounding.** The **A/S/K/V/R/P/D** evidence records a document-signing and signature-validation application with no attribute-attestation, wallet or verification-point feature. Inspected source proves specific behaviours only; it does not prove organisational compliance. No builds, tests, implementation changes or commits accompany this reading; missing evidence is not proof that any the-act requirement applies or is breached.
