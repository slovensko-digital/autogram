# Commission Implementing Regulation (EU) 2024/2977 — wallet identification data and attributes

## Executive summary

This act regulates issuance of person identification data (PID) and electronic attestations of attributes (EAA) **to European Digital Identity Wallet units**, not ordinary document signing or signature validation. Under the confirmed scope, Autogram is neither a wallet provider, PID/EAA issuer nor wallet-relying party. The individual issuer, wallet and Member State requirements below are therefore scoped out, not reported as completed or breached. Future wallet integration would require a new role assessment; a local API and use of an identity card do not establish those roles.

The original act entered into force on **24 December 2024**. The current text incorporates **2026/1731**, effective **11 August 2026**, including new Article 3a (portrait protection), changed cross-references and a completely replaced annex. The annex's portrait requirement has a specific **11 August 2028** application date. These are not Autogram release deadlines. No wallet-profile conformity, organisational issuance arrangement or portrait-processing deployment was audited. This is an engineering/legal reading aid, not legal advice or certification.

## Source and version

- **B — original authoritative English act:** [ELI / complete OJ text](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj/eng), CELEX **32024R2977**; adopted **28 November 2024**, published **4 December 2024**, OJ L 2024/2977. Article 6 makes entry into force the twentieth day following publication: **24 December 2024**; no separate general deferred application provision.
- **C — current English consolidation:** [11 August 2026 complete text](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2977-20260811), CELEX **02024R2977-20260811**, version **001.001**. A non-authoritative documentation tool, checked against B and M; its amendment table lists only M and no corrigendum. EUR-Lex's original-act page identifies this as the current consolidation, and the act as in force.
- **M — authoritative amending act:** [2026/1731 complete OJ text](https://eur-lex.europa.eu/eli/reg_impl/2026/1731/oj/eng), CELEX **32026R1731**; adopted **15 July 2026**, published **22 July 2026**, effective **11 August 2026** under Article 5. Article 1(1) inserts Article 3a, 1(2) replaces Article 4(1), 1(3) replaces Article 5(4)(b), and 1(4)/Annex I replaces this act's entire annex. See the separate [amending-act register](eu-reg_impl-2026-1731.md); its other amendments are not provisions of 2024/2977.
- Complete English HTML was retrieved from the **official Publications Office**, using language/content negotiation, after EUR-Lex HTTP fetches returned empty responses: [B](https://publications.europa.eu/resource/celex/32024R2977), [C](https://publications.europa.eu/resource/celex/02024R2977-20260811), [M](https://publications.europa.eu/resource/celex/32026R1731). No missing annex was inferred from a secondary summary.
- **Access/assessment date:** 8 October 2026. **Repository baseline:** `5ca91d5c`. Scope and shared facts: [register README](README.md). No repeal found in the retrieved current consolidation; amendment verification is limited to these official records, not a guarantee against unindexed later publications.

### Evidence and table conventions

**E1:** [`README.md`, lines 4–19](../../README.md#L4-L19), inspected: desktop file signing/verification, localhost API and CLI. **E2:** [`SigningKey.sign`, lines 15–29](../../src/main/java/digital/slovensko/autogram/core/SigningKey.java#L15-L29), inspected: signs through the selected token; it does not issue wallet PID or EAA. **E3:** confirmed organisational facts in [register README, lines 3–14](README.md#L3-L14): no hosted document-processing service, no QTSP role. These establish the reviewed boundary, not an exhaustive negative proof about every release or contract.

Rows cite **B**, **C** or **M** as defined above, with their exact provision identifier. **S** means the requirement addresses a state, wallet, wallet-relying party or PID/EAA issuer absent from the confirmed E1–E3 scope. **F** names the evidence needed *if* a future deployment introduces that role: role/service contracts, national scheme/mandate, wallet certification and issuer protocol/profile tests. Neither S nor F means a present compliance gap. Recitals and definitions are interpretative context rather than software tasks. Technical annex rows are None rather than blanket Direct: the app does not consume/issue these wallet credentials in the established scope.

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or out of the confirmed scope; never "knowingly disregarded"). Every provision row links to the official text at that unit (act anchors `#art_N`, `#anx_R`, recitals `#rct_N`); structural/preamble labels link to the [act page](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj).

## Provision-by-provision assessment

### Original recitals (B; not reproduced in C)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Recital 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_1) | Secure, interoperable EU wallet ecosystem and privacy objectives. | Indirect | Ignored | B; interpretative purpose, E1 boundary. | 2024 preamble. |
| [Recital 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_2) | GDPR and, where relevant, ePrivacy apply to processing under this act. | Indirect | Ignored | B; not a GDPR exemption for independent software/support processing; separate data-flow review needed. | Context, not a new legal basis. |
| [Recital 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_3) | Four implementing acts divide protocols, core functions, PID/EAA and notifications. | Indirect | Ignored | B; use actual addressees, not a combined vendor duty. | Related 2024/2979, 2980, 2982. |
| [Recital 4](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_4) | Commission reviews specifications against technology and the toolbox/ARF. | Indirect | Ignored | B; Commission policy, no local-app task. | M subsequently updates specifications. |
| [Recital 5](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_5) | Wallet privacy features prevent combining unrelated service data. | None | Ignored | B; S: not a wallet or attribute provider. | GDPR duties assessed separately. |
| [Recital 6](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_6) | Harmonised wallet data operations, formats and identity matching. | None | Ignored | B; S: file signing is not wallet PID presentation. | Wallet interoperability rationale. |
| [Recital 7](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_7) | States ensure authentication of wallet counterparties; common access certificates. | None | Ignored | B; S: no wallet-relying-party transaction. | Not ordinary signer-certificate validation. |
| [Recital 8](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_8) | Publish supported wallets and use high-assurance enrolment/identity proofing. | None | Ignored | B; S: states/PID issuers, not software publisher. | See Art. 3(6)–(9). |
| [Recital 9](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_9) | EAA formats need harmonisation. | None | Ignored | B; S: not EAA issuer/consumer. | See Art. 4(1). |
| [Recital 10](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_10) | Authenticate EAA issuers and validate wallets before issuance. | None | Ignored | B; S: no EAA issuance workflow. | Interpretative recital, not an additional operative paragraph. |
| [Recital 11](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_11) | Issuers publish revocation circumstances/procedures. | None | Ignored | B; S: not a PID/EAA issuer. | Do not conflate with certificate OCSP/CRL checks. |
| [Recital 12](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_12) | States use optional attributes to make PID unique. | None | Ignored | B; S: no national PID scheme. | Optional fields are not universally mandatory. |
| [Recital 13](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_13) | EDPS consultation. | Indirect | Ignored | B; institutional procedural record. | Opinion 30 September 2024. |
| [Recital 14](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#rct_14) | Committee opinion supports measures. | Indirect | Ignored | B; institutional procedural record. | No software task. |

### Articles 1–2 — scope and definitions (B = C)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 1, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_1) | Issuance of PID/EAA to wallet units; specifications updated with technology/ARF. | Indirect | Ignored | B/C; E1–E3 establish that ordinary local signing is outside this activity. | Effective 24 December 2024. |
| [Art. 2, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | Definitions apply for this regulation. | Indirect | Ignored | B/C; interpretative rule. | No implementation measure. |
| [Art. 2(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | Wallet user controls wallet unit. | Indirect | Ignored | B/C; a local signatory is not thereby a wallet user. | Definition. |
| [Art. 2(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | Wallet unit is provider-supplied unique configuration of instances and secure components. | Indirect | Ignored | B/C; E1/E2 do not establish such configuration. | Definition. |
| [Art. 2(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | Wallet solution combines software, hardware, services/settings and secure components. | Indirect | Ignored | B/C; not every cryptographic app is a wallet. | Definition. |
| [Art. 2(4)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | PID provider issues/revokes PID and ensures cryptographic wallet binding. | Indirect | Ignored | B/C; E2 signs documents, not PID. | Definition. |
| [Art. 2(5)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | Wallet unit attestation describes or authenticates/validates components. | Indirect | Ignored | B/C; not a signed-document validation report. | Definition. |
| [Art. 2(6)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | Wallet instance is configured app forming part of a wallet unit. | Indirect | Ignored | B/C; E1 alone does not establish wallet membership. | Definition. |
| [Art. 2(7)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | Wallet secure cryptographic application manages critical assets through secure device. | Indirect | Ignored | B/C; E2 token use alone does not confer this role. | Definition. |
| [Art. 2(8)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | Wallet secure cryptographic device is tamper-resistant protected environment. | Indirect | Ignored | B/C; no claim that Autogram is such a device. | Definition. |
| [Art. 2(9)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | Wallet provider provides wallet solutions. | Indirect | Ignored | B/C; E1–E3: no established wallet supply. | Definition. |
| [Art. 2(10)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | Critical assets: compromise seriously undermines reliance on wallet unit. | Indirect | Ignored | B/C; wallet-specific risk concept, not blanket app certification. | Definition. |
| [Art. 2(11)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | Wallet-relying party intends to rely on wallet units for digitally delivered services. | Indirect | Ignored | B/C; validating a signed file does not establish wallet reliance. | Definition. |
| [Art. 2(12)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | Access certificate authenticates/validates wallet-relying party. | Indirect | Ignored | B/C; not the user's ordinary signing certificate. | Definition. |
| [Art. 2(13)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_2) | State-mandated provider issues access certificates to registered parties. | Indirect | Ignored | B/C; no mandate supplied or claimed. | Definition. |

### Article 3 — issuance of PID (B = C)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 3(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_3) | PID issuer follows electronic identification scheme under which wallet provided. | None | Ignored | S; B/C; future F: scheme and issuer contract. | At PID issuance. |
| [Art. 3(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_3) | Issued PID contains authentication/validation information. | None | Ignored | S; B/C; future F: credential fixtures and issuer profile. | Not document-signature metadata. |
| [Art. 3(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_3) | Issuer complies with annex technical specifications. | None | Ignored | S; B/C; current and historical annex separately assessed below. | Annex replaced 11 August 2026. |
| [Art. 3(4)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_3) | State ensures PID unique for given user within state. | None | Ignored | S; B/C; future national uniqueness policy. | No vendor identity-number assignment duty. |
| [Art. 3(5)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_3) | Issuer cryptographically binds PID to receiving wallet unit. | None | Ignored | S; B/C; future F: wallet key-binding tests. | E2 document signing is different. |
| [Art. 3(6)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_3) | State publishes supported wallet-solution list. | None | Ignored | S; B/C; state publication, not Autogram release list. | Public availability. |
| [Art. 3(7)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_3) | State enrols at assurance high; issuer verifies user identity before issuance under 2015/1502. | None | Ignored | S; B/C; future enrolment policy, proofing records and scheme. | Both sentences assessed; no separate numbered subdivision. |
| [Art. 3(8)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_3) | Issuer identifies to wallet with access certificate or high-assurance notified-scheme mechanism. | None | Ignored | S; B/C; future F: authentication protocol tests. | At issuance. |
| [Art. 3(9)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_3) | Issuer validates wallet attestation and accepted wallet solution, or alternative high-assurance scheme authentication. | None | Ignored | S; B/C; future F: attestation/solution acceptance tests. | Before issuance. |

### Articles 3a–6 — current operative text

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 3a(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_3) | Wallet provider warns that requested portrait disclosure shares biometric data and requires confirmation. | None | Ignored | S; C/M Art. 1(1); future wallet UI warnings and user tests. | Added 11 August 2026; supplements GDPR information duties. |
| [Art. 3a(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_3) | Wallet solution requires explicit, specific confirmation of portrait presentation. | None | Ignored | S; C/M Art. 1(1); future consent-flow tests; technical confirmation is not itself a GDPR legal basis. | Added 11 August 2026. |
| [Art. 3a(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_3) | Wallet-relying party limits retention to lawful necessary identification/authentication or legal provision; third-country/international transfer only as data protection law permits. | None | Ignored | S; C/M Art. 1(1); future purpose/retention/transfer assessment. | Both sentences assessed; no present wallet portrait receipt. |
| [Art. 4(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_4) | EAA issued to wallets meets at least one standard in 2024/2979 Annex II. | None | Ignored | S; C/M Art. 1(2); future issuer profile/tests against actual cited annex. | Replacement effective 11 August 2026. |
| [Art. 4(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_4) | EAA issuer identifies to wallet using access certificate. | None | Ignored | S; B/C; future issuer/access-certificate mandate and tests. | Not ordinary certificate signing. |
| [Art. 4(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_4) | EAA contains authentication/validation information. | None | Ignored | S; B/C; future EAA fixtures/profile. | At issuance. |
| [Art. 5(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_5) | PID issuer has written public validity-management/revocation policy. | None | Ignored | S; B/C; future issuer validity policy. | Includes circumstances for revocation without delay. |
| [Art. 5(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_5) | Only originating PID/EAA issuer can revoke its issued data/attestations. | None | Ignored | S; B/C; future authorisation controls. | Deleting a local signed file is not issuer revocation. |
| [Art. 5(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_5) | PID issuer securely notifies affected wallet user of revocation/reasons within 24 hours, clearly and accessibly. | None | Ignored | S; B/C; future notification channel, wording and delivery records. | 24 hours from revocation, not incident awareness. |
| [Art. 5(4), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_5) | Issuer revokes PID in each listed circumstance. | None | Ignored | S; B/C; trigger structure for following letters. | Parent framing, not an extra trigger. |
| [Art. 5(4)(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_5) | Revocation upon explicit request of corresponding wallet user. | None | Ignored | S; B/C; future authenticated user-request process. | Source wording also mentions EAA. |
| [Art. 5(4)(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_5) | Revocation when attestation of wallet unit receiving PID is revoked. | None | Ignored | S; C/M Art. 1(3); future attestation-status propagation tests. | Clarified wording from 11 August 2026. |
| [Art. 5(4)(c)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_5) | Revocation in further situations specified by issuer policy. | None | Ignored | S; B/C; future policy's exact additional triggers. | Do not invent mandatory triggers. |
| [Art. 5(5)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_5) | PID revocation cannot be reversed. | None | Ignored | S; B/C; future immutable status-transition tests. | Not application file restore. |
| [Art. 5(6)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_5) | Revoked PID remains accessible for periods required by EU/national law. | None | Ignored | S; B/C; future applicable retention law and policy. | No universal period stated here. |
| [Art. 5(7)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_5) | Issuer publishes privacy-preserving validity status and locates it in PID. | None | Ignored | S; B/C; future status service/privacy tests. | Not certificate CRL/OCSP publication by app. |
| [Art. 5(8)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_5) | PID provider enables privacy-preserving unlinkability where EAA does not require user identification. | None | Ignored | S; B/C; future unlinkability design/test evidence. | Preserve act's PID-provider/EAA wording. |
| [Art. 6, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj#art_6) | Entry into force on twentieth day after OJ publication. | Indirect | Ignored | B/C; legal commencement, not a software control. | 24 December 2024. |
| [Final unnumbered operative paragraph 1, following Art. 6](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Binding in entirety and directly applicable in all Member States. | Indirect | Ignored | B/C; territorial/legal effect does not remove activity/addressee limits. | No general certification obligation. |

### Current annex — sections 1–3 (C; M Annex I)

The section and table titles are structural headings. Each data row and each following substantive indent has its own assessment. **N** in the evidence column means no wallet PID issue/consumption under E1–E3; future evidence would be the national PID schema, issuer payload fixtures and wallet disclosure tests. These are issuer/state requirements, not current local-app input requirements.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [§1 Table 1: family_name](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory current surname(s). | None | Ignored | N; C/M. | Selectively disclosable. |
| [§1 Table 1: given_name](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory first/middle name(s). | None | Ignored | N; C/M. | Selectively disclosable. |
| [§1 Table 1: birth_date](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory date of birth. | None | Ignored | N; C/M. | Day/month/year. |
| [§1 Table 1: birth_place](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory birthplace country code or territorial/local description. | None | Ignored | N; C/M. | ISO 3166-1 where country code used. |
| [§1 Table 1: nationality](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory one or more nationality country codes. | None | Ignored | N; C/M. | Exceptional values below. |
| [§1 Table 1: portrait](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Facial image; full frontal ISO/IEC 39794-5 or compatibility 19794-5 clauses 8.2–8.4; JPEG data without specified headers/blocks; opt-out where applicable. | None | Ignored | N; C/M; future image-quality/profile tests. | Requirement applies from **11 August 2028**. |
| [§1 following Table 1, indent 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | State may allow user to decline portrait insertion. | None | Ignored | N; C/M; future national opt-out decision. | Permission, not universally required opt-out. |
| [§1 following Table 1, indent 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | State ensures selective disclosure of each identifier, including portrait. | None | Ignored | N; C/M. | Credential-level disclosure. |
| [§1 following Table 1, indent 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Unknown birth date: appropriate values under §4.1/4.2 specifications. | None | Ignored | N; C/M. | No invented date. |
| [§1 following Table 1, indent 4](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Unknown nationality uses QU. | None | Ignored | N; C/M. | Distinct from statelessness. |
| [§1 following Table 1, indent 5](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | No nationality uses QS. | None | Ignored | N; C/M. | Statelessness case. |
| [§1 following Table 1, indent 6](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Portrait value empty when user opts out. | None | Ignored | N; C/M. | Where opt-out used. |
| [§1 Table 2: resident_address](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional full residence/contact address. | None | Ignored | N; C/M. | Selective disclosure. |
| [§1 Table 2: resident_country](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional residence country, ISO alpha-2. | None | Ignored | N; C/M. | Optional attribute. |
| [§1 Table 2: resident_state](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional state/province/district/local area. | None | Ignored | N; C/M. | Optional attribute. |
| [§1 Table 2: resident_city](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional municipality/city/town/village. | None | Ignored | N; C/M. | Optional attribute. |
| [§1 Table 2: resident_postal_code](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional residence postal code. | None | Ignored | N; C/M. | Optional attribute. |
| [§1 Table 2: resident_street](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional street including house number and affix/suffix. | None | Ignored | N; C/M. | Separate resident_house_number removed. |
| [§1 Table 2: personal_administrative_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional issuer-unique number; state opting in describes values/processing policy in scheme. | None | Ignored | N; C/M; future state policy. | Both field and policy assessed. |
| [§1 Table 2: family_name_birth](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional birth surname(s). | None | Ignored | N; C/M. | Not current surname. |
| [§1 Table 2: given_name_birth](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional birth first/middle name(s). | None | Ignored | N; C/M. | Not current first name. |
| [§1 Table 2: sex](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional enumerated sex value. | None | Ignored | N; C/M. | Values individually below. |
| [§1 Table 2: sex, value 0](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Not known. | None | Ignored | N; C/M. | ISO/IEC 5218 applies. |
| [§1 Table 2: sex, value 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Male. | None | Ignored | N; C/M. | ISO/IEC 5218 applies. |
| [§1 Table 2: sex, value 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Female. | None | Ignored | N; C/M. | ISO/IEC 5218 applies. |
| [§1 Table 2: sex, value 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Other. | None | Ignored | N; C/M. | Act's enumeration. |
| [§1 Table 2: sex, value 4](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Inter. | None | Ignored | N; C/M. | Act's enumeration. |
| [§1 Table 2: sex, value 5](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Diverse. | None | Ignored | N; C/M. | Act's enumeration. |
| [§1 Table 2: sex, value 6](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Open. | None | Ignored | N; C/M. | Act's enumeration. |
| [§1 Table 2: sex, value 9](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Not applicable. | None | Ignored | N; C/M. | ISO/IEC 5218 applies. |
| [§1 Table 2: email_address](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional email conforming to RFC 5322. | None | Ignored | N; C/M. | External standard not inspected. |
| [§1 Table 2: mobile_phone_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional phone: +, country code, then digits only. | None | Ignored | N; C/M. | Format constraint if supplied. |
| [§2 Table 3: current legal name](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory current legal person's name. | None | Ignored | N; C/M. | Legal-person PID. |
| [§2 Table 3: unique identifier](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | State-constructed cross-border identifier, as persistent as possible. | None | Ignored | N; C/M. | Not local account ID. |
| [§2 following Table 3, indent 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Unknown/unissuable identifier: state supplies situationally appropriate value. | None | Ignored | N; C/M. | Legal-person fallback retained. |
| [§2 Table 4: current address](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional legal-person address. | None | Ignored | N; C/M. | Optional. |
| [§2 Table 4: VAT registration number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional VAT identifier. | None | Ignored | N; C/M. | Optional. |
| [§2 Table 4: tax reference number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional tax reference. | None | Ignored | N; C/M. | Optional. |
| [§2 Table 4: European unique identifier](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional identifier under Directive 2017/1132. | None | Ignored | N; C/M. | Referenced instrument is separate. |
| [§2 Table 4: LEI](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional LEI under 2022/1860. | None | Ignored | N; C/M. | Referenced instrument is separate. |
| [§2 Table 4: EORI](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional EORI under 1352/2013. | None | Ignored | N; C/M. | Referenced instrument is separate. |
| [§2 Table 4: excise number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional number under 389/2012 Art. 2(12). | None | Ignored | N; C/M. | Referenced instrument is separate. |
| [§3 Table 5: issuing_authority](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory issuing authority name or state alpha-2 code if no separate authority. | None | Ignored | N; C/M. | Metadata. |
| [§3 Table 5: issuing_country](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory issuer's country/territory alpha-2 code. | None | Ignored | N; C/M. | ISO 3166-1. |
| [§3 Table 5: expiry_date](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional administrative-validity expiry date/time where possible. | None | Ignored | N; C/M. | No longer mandatory; distinct from technical exp. |
| [§3 Table 5: document_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional issuer-assigned PID number. | None | Ignored | N; C/M. | Not signed-document filename. |
| [§3 Table 5: issuing_jurisdiction](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional ISO 3166-2:2020 clause 8 subdivision; prefix matches issuing_country. | None | Ignored | N; C/M. | Both constraints assessed. |
| [§3 Table 5: issuance_date](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional administrative-validity start date/time where possible. | None | Ignored | N; C/M. | Newly included metadata. |

### Current annex — section 4 and 4.1, ISO/IEC-mdoc

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [§4, indent 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Natural-person PID uses 2024/2979 Annex II clauses 5 (SD-JWT VC) and 6 (mdoc); excludes 5.2.2, 5.2.4, 5.2.5, EAA-6.1-03, 6.2.2–6.2.5. | None | Ignored | N; C/M; future exact profile mapping. | Does not import excluded clauses. |
| [§4, indent 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Natural-person encoding complies with both specified §4.1/4.2 profiles. | None | Ignored | N; C/M. | Replaces old W3C VCDM route. |
| [§4.1, indent 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | mdoc attestation type and PID namespace both eu.europa.ec.eudi.pid.1. | None | Ignored | N; C/M. | Two identifiers. |
| [§4.1, indent 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Additional identifiers use domestic namespace with state/region code and optional dot/version. | None | Ignored | N; C/M. | eu.europa.ec.eudi.pid.[code]. |
| [§4.1, indent 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Publish domestic namespace schema/definitions/presence/encodings under 2025/1569 Art. 8. | None | Ignored | N; C/M; future public schema. | Only where domestic namespace used. |
| [§4.1, indent 4](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | §1/§3 data and metadata are mdoc PID data elements. | None | Ignored | N; C/M. | Natural-person profile. |
| [§4.1, indent 5](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | MobileSecurityObject deviceKeyInfo.deviceKey contains public key. | None | Ignored | N; C/M; future mdoc fixtures. | Binding structure. |
| [§4.1, indent 6](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Corresponding private key stored in user's wallet secure cryptographic device. | None | Ignored | N; C/M; future WSCD/key-attestation evidence. | E2 token signing does not prove this. |
| [§4.1, indent 7](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | CB-AdES protected header has RFC 9360 x5u and x5t. | None | Ignored | N; C/M. | Both required. |
| [§4.1, indent 8](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | x5t digest is SHA-256. | None | Ignored | N; C/M. | Not general document-signature mandate. |
| [§4.1, indent 9](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Encoding requirements specified by Table 6. | None | Ignored | N; C/M. | Table framing; rows below. |
| [§4.1 Table 6: family_name](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | family_name → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: given_name](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | given_name → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: birth_date](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | birth_date → full-date. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: birth_place](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | place_of_birth → place_of_birth. | None | Ignored | N; C/M. | Structured type. |
| [§4.1 Table 6: nationality](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | nationality → nationalities. | None | Ignored | N; C/M. | Array type. |
| [§4.1 Table 6: resident_address](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | resident_address → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: resident_country](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | resident_country → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: resident_state](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | resident_state → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: resident_city](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | resident_city → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: resident_postal_code](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | resident_postal_code → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: resident_street](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | resident_street → tstr. | None | Ignored | N; C/M. | Includes house number. |
| [§4.1 Table 6: personal_administrative_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | personal_administrative_number → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: portrait](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | portrait → bstr. | None | Ignored | N; C/M. | Image bytes. |
| [§4.1 Table 6: family_name_birth](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | family_name_birth → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: given_name_birth](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | given_name_birth → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: sex](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | sex → uint. | None | Ignored | N; C/M. | Enumeration above. |
| [§4.1 Table 6: email_address](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | email_address → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: mobile_phone_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | mobile_phone_number → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: expiry_date](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | expiry_date → tdate or full-date. | None | Ignored | N; C/M. | Alternative types. |
| [§4.1 Table 6: issuing_authority](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | issuing_authority → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: issuing_country](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | issuing_country → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: document_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | document_number → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: issuing_jurisdiction](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | issuing_jurisdiction → tstr. | None | Ignored | N; C/M. | Encoding. |
| [§4.1 Table 6: issuance_date](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | issuance_date → tdate or full-date. | None | Ignored | N; C/M. | Alternative types. |
| [§4.1, following Table 6, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Representation types follow RFC 8610 plus following constraints. | None | Ignored | N; C/M. | Lettered items individually assessed. |
| [§4.1(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | tstr encoded UTF-8. | None | Ignored | N; C/M. | Not an independent app-wide rule. |
| [§4.1(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | tstr supports full Unicode. | None | Ignored | N; C/M. | PID encoding. |
| [§4.1(c)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | tstr maximum 150 characters. | None | Ignored | N; C/M. | PID encoding. |
| [§4.1(d)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | date encoding follows RFC 8943. | None | Ignored | N; C/M. | External reference not inspected. |
| [§4.1(e)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | full-date is #6.1004(tstr), RFC 8943 tag. | None | Ignored | N; C/M. | Typed date. |
| [§4.1(f)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | tdate contains RFC 3339 date-time string. | None | Ignored | N; C/M. | Typed timestamp. |
| [§4.1(g)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | full-date contains RFC 3339 full-date under RFC 8943. | None | Ignored | N; C/M. | Typed date. |
| [§4.1(h), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Date representations follow listed rules unless indicated otherwise. | None | Ignored | N; C/M. | Parent condition. |
| [§4.1(h), indent 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | No fractional seconds. | None | Ignored | N; C/M. | Default date representation. |
| [§4.1(h), indent 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | No local UTC offset; time-offset Z. | None | Ignored | N; C/M. | Default date representation. |
| [§4.1(i)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Major-type 0/1 integer as small as possible under RFC 8949 §4.2. | None | Ignored | N; C/M. | CBOR minimal encoding. |
| [§4.1(j)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | place_of_birth has at least one country, region or locality pair. | None | Ignored | N; C/M. | No required combination invented. |
| [§4.1(k)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | bstr/tstr/array/map length encoded as short as possible under RFC 8949 §4.2. | None | Ignored | N; C/M. | CBOR minimal encoding. |
| [§4.1(l), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Nationality array of ISO 3166-1 alpha-2 codes; CDDL form as below when used. | None | Ignored | N; C/M. | Parent encoding rule. |
| [§4.1(l), indent 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | nationalities = [+ CountryCode]. | None | Ignored | N; C/M. | Non-empty array notation. |
| [§4.1(l), indent 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | CountryCode is tstr alpha-2 code. | None | Ignored | N; C/M. | CDDL value type. |
| [§4.1(l), indent 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Issuer attesting multiple nationalities may include all. | None | Ignored | N; C/M. | Permission, not obligation to attest all. |
| [§4.1(l), indent 4](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | place_of_birth encoded as specified type; CDDL map when used. | None | Ignored | N; C/M. | Source nests birthplace under (l). |
| [§4.1(l), birthplace map: country](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional country key, tstr single alpha-2 code. | None | Ignored | N; C/M. | At least one key under (j). |
| [§4.1(l), birthplace map: region](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional region key, tstr state/province/district/local area. | None | Ignored | N; C/M. | At least one key under (j). |
| [§4.1(l), birthplace map: locality](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional locality key, tstr municipality/city/town/village. | None | Ignored | N; C/M. | At least one key under (j). |

### Current annex — section 4.2, SD-JWT VC, and section 5

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [§4.2, pre-table indent 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | PID and metadata included as SD-JWT VC claims. | None | Ignored | N; C/M; future SD-JWT fixtures. | Format-specific. |
| [§4.2, pre-table indent 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Each referenced claim individually selectively disclosable except format-defined non-selective claims. | None | Ignored | N; C/M; future disclosure tests. | Exception retained. |
| [§4.2, pre-table indent 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Table 7 defines public claim names. | None | Ignored | N; C/M. | Table framing. |
| [§4.2, pre-table indent 4](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Table 8 defines PID-specific claim names. | None | Ignored | N; C/M. | Table framing. |
| [§4.2, pre-table indent 5](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | JSON strings UTF-8/full Unicode unless Table 8 or references explicitly otherwise. | None | Ignored | N; C/M. | Exception retained. |
| [§4.2, pre-table indent 6](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | JWT nbf/exp under RFC 7519 express technical validity. | None | Ignored | N; C/M. | Different from administrative dates. |
| [§4.2, pre-table indent 7](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | RFC 7800 cnf public key from private key held in user's WSCD. | None | Ignored | N; C/M; future holder-binding tests. | Both claim and secure key storage. |
| [§4.2, pre-table indent 8](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Signature protected header has RFC 7515 x5u and x5t#S256. | None | Ignored | N; C/M. | Both headers required. |
| [§4.2 Table 7: family_name](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | family_name → string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: given_name](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | given_name → string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: birth_date](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | birthdate → ISO 8601-1 YYYY-MM-DD string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: birth_place](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | place_of_birth → JSON structure. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: nationality](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | nationalities → array of strings. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: resident_address](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | address.formatted → string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: resident_country](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | address.country → string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: resident_state](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | address.region → string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: resident_city](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | address.locality → string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: resident_postal_code](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | address.postal_code → string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: resident_street](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | address.street_address → string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: family_name_birth](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | birth_family_name → string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: given_name_birth](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | birth_given_name → string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: email_address](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | email → string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: mobile_phone_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | phone_number → string. | None | Ignored | N; C/M. | Public name. |
| [§4.2 Table 7: portrait](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | picture → data-URL string with base64 JPEG portrait. | None | Ignored | N; C/M. | Not arbitrary remote image URL. |
| [§4.2 Table 8: expiry_date](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | date_of_expiry → ISO 8601-1 YYYY-MM-DD string. | None | Ignored | N; C/M. | PID-specific name. |
| [§4.2 Table 8: issuance_date](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | date_of_issuance → ISO 8601-1 YYYY-MM-DD string. | None | Ignored | N; C/M. | PID-specific name. |
| [§4.2 Table 8: personal_administrative_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | personal_administrative_number → string. | None | Ignored | N; C/M. | PID-specific name. |
| [§4.2 Table 8: sex](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | sex → number. | None | Ignored | N; C/M. | Enumerated values above. |
| [§4.2 Table 8: issuing_authority](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | issuing_authority → string. | None | Ignored | N; C/M. | PID-specific name. |
| [§4.2 Table 8: issuing_country](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | issuing_country → string. | None | Ignored | N; C/M. | PID-specific name. |
| [§4.2 Table 8: document_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | document_number → string. | None | Ignored | N; C/M. | PID-specific name. |
| [§4.2 Table 8: issuing_jurisdiction](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | issuing_jurisdiction → string. | None | Ignored | N; C/M. | PID-specific name. |
| [§4.2, post-table indent 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Base type urn:eudi:pid:1 in vct; all PID types in urn:eudi:pid: namespace. | None | Ignored | N; C/M. | Both type constraints. |
| [§4.2, post-table indent 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Extra attributes defined in domestic type. | None | Ignored | N; C/M. | When extra attributes used. |
| [§4.2, post-table indent 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Domestic type schema/definitions/presence/encodings published under 2025/1569 Art. 8. | None | Ignored | N; C/M; future public schema. | When domestic type used. |
| [§5, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Commission PID-provider list under 2024/2980 enables PID authentication. | None | Ignored | S; C/M; future wallet trust-chain/list tests. | Not EU LOTL signature-provider list. |

### Historical original annex (B) — replaced in full on 11 August 2026

These rows assess **all original annex requirements**, not only changed fields. Historical status is Ignored for both supersession and absent wallet role. They must not be used as the current profile. Original Articles 4(1) and 5(4)(b), also replaced, are assessed after the annex.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [B §1 Table 1: family_name](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory current surname(s). | None | Ignored | B; historical PID issuer, S. | 24 December 2024–10 August 2026. |
| [B §1 Table 1: given_name](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory first/middle name(s). | None | Ignored | B; historical PID issuer, S. | Same historical interval. |
| [B §1 Table 1: birth_date](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory day/month/year of birth. | None | Ignored | B; historical PID issuer, S. | Same historical interval. |
| [B §1 Table 1: birth_place](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory birthplace country code or territorial/local description. | None | Ignored | B; historical PID issuer, S. | Same historical interval. |
| [B §1 Table 1: nationality](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory one or more alpha-2 nationality codes. | None | Ignored | B; historical PID issuer, S. | Same historical interval. |
| [B §1, following Table 1, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | State supplies appropriate value for unknown/unissuable attribute. | None | Ignored | B; historical state duty, S. | Replaced by specific current fallback rules. |
| [B §1 Table 2: resident_address](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional full residence/contact address. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §1 Table 2: resident_country](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional residence alpha-2 country code. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §1 Table 2: resident_state](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional state/province/district/local area. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §1 Table 2: resident_city](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional municipality/city/town/village. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §1 Table 2: resident_postal_code](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional residence postal code. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §1 Table 2: resident_street](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional residence street name. | None | Ignored | B; historical PID issuer, S. | Current definition includes house number. |
| [B §1 Table 2: resident_house_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional house number including affix/suffix. | None | Ignored | B; historical PID issuer, S. | Removed as separate field. |
| [B §1 Table 2: personal_administrative_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional issuer-unique number; opting-in state describes value/processing policy in scheme. | None | Ignored | B; historical issuer/state, S. | Field and policy assessed. |
| [B §1 Table 2: portrait](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional image compliant with ISO 19794-5 or ISO 39794. | None | Ignored | B; historical PID issuer, S. | Not current mandatory-from-2028 rule. |
| [B §1 Table 2: family_name_birth](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional birth surname(s). | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §1 Table 2: given_name_birth](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional birth first/middle name(s). | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §1 Table 2: sex](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional enumerated value. | None | Ignored | B; historical PID issuer, S. | Enumerated values individually below. |
| [B §1 Table 2: sex, value 0](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Not known. | None | Ignored | B; historical PID issuer, S. | ISO/IEC 5218. |
| [B §1 Table 2: sex, value 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Male. | None | Ignored | B; historical PID issuer, S. | ISO/IEC 5218. |
| [B §1 Table 2: sex, value 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Female. | None | Ignored | B; historical PID issuer, S. | ISO/IEC 5218. |
| [B §1 Table 2: sex, value 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Other. | None | Ignored | B; historical PID issuer, S. | Act's enumeration. |
| [B §1 Table 2: sex, value 4](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Inter. | None | Ignored | B; historical PID issuer, S. | Act's enumeration. |
| [B §1 Table 2: sex, value 5](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Diverse. | None | Ignored | B; historical PID issuer, S. | Act's enumeration. |
| [B §1 Table 2: sex, value 6](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Open. | None | Ignored | B; historical PID issuer, S. | Act's enumeration. |
| [B §1 Table 2: sex, value 9](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Not applicable. | None | Ignored | B; historical PID issuer, S. | ISO/IEC 5218. |
| [B §1 Table 2: email_address](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional RFC 5322 email. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §1 Table 2: mobile_phone_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional +, country code, then digits only. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §2 Table 3: current legal name](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory legal-person name. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §2 Table 3: unique identifier](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory state-constructed cross-border identifier as persistent as possible. | None | Ignored | B; historical state/issuer, S. | Historical. |
| [B §2, following Table 3, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | State supplies appropriate value for unknown/unissuable data element. | None | Ignored | B; historical state duty, S. | Historical. |
| [B §2 Table 4: current address](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional legal-person address. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §2 Table 4: VAT registration number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional VAT ID. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §2 Table 4: tax reference number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional tax reference. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §2 Table 4: European unique identifier](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional identifier under 2017/1132. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §2 Table 4: LEI](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional identifier under 2022/1860. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §2 Table 4: EORI](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional identifier under 1352/2013. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §2 Table 4: excise number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional identifier under 389/2012 Art. 2(12). | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §3 Table 5: expiry_date](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory PID expiry date/time where possible. | None | Ignored | B; historical PID issuer, S. | Now optional administrative expiry. |
| [B §3 Table 5: issuing_authority](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory authority name or state alpha-2 code if no separate authority. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §3 Table 5: issuing_country](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Mandatory issuer country/territory alpha-2. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §3 Table 5: document_number](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional issuer-assigned PID number. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §3 Table 5: issuing_jurisdiction](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional ISO 3166-2:2020 clause 8 subdivision; country prefix matches issuing_country. | None | Ignored | B; historical PID issuer, S. | Historical. |
| [B §3 Table 5: location_status](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Optional validity-status location where issuer revokes PID. | None | Ignored | B; historical PID issuer, S. | Removed from current table; Art. 5(7) retained. |
| [B §4, unnumbered introductory paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | PID issued in two formats. | None | Ignored | B; historical PID issuer, S. | Parent format requirement. |
| [B §4(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | ISO/IEC 18013-5:2021 format. | None | Ignored | B; historical PID issuer, S. | Replaced current encoding profile. |
| [B §4(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | W3C Verifiable Credentials Data Model 1.1, recommendation 3 March 2022. | None | Ignored | B; historical PID issuer, S. | Not current SD-JWT VC rule. |
| [B §5, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Commission PID-provider list under 2024/2980 enables PID authentication. | None | Ignored | B; historical Commission/list function, S. | Not ordinary EU LOTL validation. |
| [B Art. 4(1), original wording](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | EAA complies with at least one standard listed in 2024/2979 Annex I. | None | Ignored | B; historical EAA issuer, S. | Replaced by Annex II reference on 11 August 2026. |
| [B Art. 5(4)(b), original wording](https://eur-lex.europa.eu/eli/reg_impl/2024/2977/oj) | Revoke PID where wallet unit attestation to which PID issued revoked. | None | Ignored | B; historical PID issuer, S. | Clarified wallet-unit wording on 11 August 2026. |

## Coverage and limitations

- **Texts obtained in full:** original English B, current English C (including its entire replacement annex and source amendment table), and English M. B's preamble supplies **14 individually assessed recitals**. C contains **7 article identifiers** (1, 2, 3, 3a, 4, 5, 6), including **13 numbered definitions**, **23 numbered operative paragraphs** (9 + 3 + 3 + 8), **3 Art. 5(4) letters**, Art. 1/2 introductory/Art. 6 unnumbered text and final binding/applicability paragraph. Article headings are structural, not invented additional duties. The Art. 5(4) introduction is separately identifiable as trigger framing.
- **Current annex:** **5 sections**, **2 encoding subsections**, **8 tables with 81 data rows** (6 + 12 + 2 + 7 + 6 + 24 + 16 + 8). All 81 are assessed individually, with eight sex values additionally identifiable. Outside the tables: six §1 indents, one §2 fallback, two §4 indents, nine §4.1 pre-table indents, one post-table introduction, **12 letters (a)–(l)**, two nested (h) indents, four nested (l) indents and three birthplace-map keys, eleven §4.2 indents (8 before / 3 after tables), and §5's unnumbered paragraph. Parent framing is labelled without treating it as an additional independent duty.
- **Historical annex:** all **5 sections**, **5 tables / 34 data rows** (5 + 14 + 2 + 7 + 6), eight sex values, two fallback paragraphs, §4's unnumbered format introduction and two numbered format points, and §5's paragraph. Original replaced Art. 4(1) and 5(4)(b) have their own historical rows. Unchanged original articles are assessed in the B/C rows rather than duplicated. M's insertion/replacement blocks affecting this act are assessed at each resulting current provision; the other acts' amendments and M's own preamble/commencement belong to its separate document.
- **Checking method:** parsed complete official Publications Office HTML to enumerate articles, table entries, indents and lettered subdivisions; compared B with C's B/M1 markers and M Article 1/Annex I. Dates checked against OJ publication and the twentieth-day clauses. EUR-Lex browser metadata confirmed the current consolidation date. No substantive part of this act or its historical annex was unavailable or deliberately omitted; footnote bibliographic citations and signature/publication identifiers were read as sources, not counted as separate obligations.
- **External works not inspected:** the referenced ISO/IEC/ISO, RFC, W3C and ETSI standards themselves; no full external-standard conformity claim follows from these rows. The cross-referenced 2024/2979, 2024/2980, 2015/1502 and 2025/1569 instruments need their separate registers; their complete technical requirements are not silently imported here.
- **Outstanding evidence:** actual wallet integration/issuer contracts or state mandate if any, national scheme/enrolment and uniqueness policies, wallet certification, profile fixtures, key-binding/status/disclosure tests, portrait processing/retention/transfer legal assessment and national portrait opt-out decision. None is an established present missing statutory measure for Autogram. Repository evidence is narrow and no released binary, complete codebase role audit or organisational records were examined. The known broader uncertainties (eIDAS Art. 15 end-user product, CRA commercial supply, all-country signature correctness) are not resolved or exempted by this wallet-specific exclusion.
