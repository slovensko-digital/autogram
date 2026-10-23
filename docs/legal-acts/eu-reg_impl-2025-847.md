# Commission Implementing Regulation (EU) 2025/847 — reactions to security breaches of European Digital Identity Wallets

## Executive summary

This act lays down Member-State rules for reacting to security breaches or compromises of European Digital Identity Wallets ('wallets'), of the validation mechanisms referred to in Article 5a(8) of Regulation (EU) No 910/2014, and of the electronic identification scheme under which wallets are provided. It governs assessment (criteria in the Annex), suspension, information duties, re-establishment, withdrawal and a CIRAS-based information system. Its addressees are Member States and their single points of contact, wallet providers and 'concerned entities', and the Commission — not ordinary document-signing software.

Under the confirmed scope (E1–E3), Autogram is a desktop application that signs and validates documents using a user's certificate/token through a local API and CLI. It is not a wallet solution, wallet provider, wallet unit, wallet-relying party, PID/EAA issuer or Member State, and it operates no CIRAS or breach-reporting function (E4–E6). Every operative requirement below is therefore scoped out rather than reported as completed or breached, and a scan of `src/main` found no wallet/EUDI/CIRAS components (see § Coverage). A future wallet-related deployment would require a new role assessment; the local API and use of an identity card do not establish those roles.

The act was adopted on **6 May 2025**, published on **7 May 2025**, and entered into force on **27 May 2025** (twentieth day after publication). Article 10 (information system via CIRAS/ENISA) applies from **7 May 2026**. No consolidation or amending act was found; the original OJ text is authoritative. These are not Autogram release deadlines. This is an engineering/legal reading aid, not legal advice, certification or a released-binary audit.

## Source and version

- **Official English act (B):** [ELI / OJ text](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj), CELEX **32025R0847**, ELI `http://data.europa.eu/eli/reg_impl/2025/847/oj`; adopted **6 May 2025**, published in OJ L, **7 May 2025** (act 2025/847). Article 11 makes entry into force the twentieth day following publication: **27 May 2025**. Article 11 also provides that the act is binding in its entirety and directly applicable in all Member States, **except Article 10, which applies from 7 May 2026**.
- No consolidation (`02...`) and no amending or repealing act for 2025/847 was found. Verification is limited to these official records, not a guarantee against unindexed later publications.
- Complete English XHTML was retrieved from the **official Publications Office** Cellar service using language/content negotiation: [32025R0847 (eng)](https://publications.europa.eu/resource/celex/32025R0847). No missing annex was inferred from a secondary summary.
- **Anchor scheme (publisher convention):** `#art_<n>` for articles, `#rct_<n>` for recitals and `#anx_1` for the annex, all on `https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj`. The annex heading is unnumbered ("ANNEX"), but Article 3(1) refers to it as "**Annex I** to this Regulation", which fixes the `#anx_1` identifier. The EUR-Lex live HTML page was behind an anti-bot challenge at retrieval time, so anchors could not be machine-verified against rendered HTML; the Cellar XHTML carries no HTML anchors.
- **Access/assessment date:** 8 October 2026. **Repository baseline:** `5ca91d5c`. Scope and shared facts: [register README](README.md).

### Evidence and table conventions

**E1:** [`README.md`, lines 4–19](../../README.md#L4-L19), inspected: desktop file signing/verification, localhost HTTP API and CLI. **E2:** [`SigningKey.sign`, lines 15–29](../../src/main/java/digital/slovensko/autogram/core/SigningKey.java#L15-L29), inspected: signs through the selected token; it does not issue wallet units, wallet unit attestations or credentials. **E3:** confirmed organisational facts in [register README, lines 3–14](README.md#L3-L14): no hosted document-processing service, no QTSP role. **E4:** [`AutogramServer.start`, lines 35–72](../../src/main/java/digital/slovensko/autogram/server/AutogramServer.java#L35-L72), inspected: HTTP contexts are `/info`, `/certificates`, `/docs`, `/sign`, `/batch` and `/assets`; there is no wallet, breach, suspension or CIRAS service. **E5:** [`SignatureValidator`, lines 76–81 and 117–130](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java#L76-L81), inspected: it validates signed documents against trusted lists and CRL/OCSP; these are signature-validation inputs, not the Article 5a(8) eIDAS wallet validation mechanisms addressed by this act. **E6:** [`server.yml`, lines 17–20](../../src/main/resources/digital/slovensko/autogram/server/server.yml#L17-L20), inspected: default server URL `http://localhost:37200`.

Rows cite **B** (the authoritative act) with the exact provision identifier. **S** means the requirement addresses a Member State, wallet provider, wallet solution/unit, wallet-relying party, concerned entity or the Commission — none of which is Autogram under E1–E6. **F** names the evidence that would be needed *if* a future deployment introduced such a role: national scheme mandate, wallet-provider contract, wallet certification, CIRAS integration and breach-response tests. Neither S nor F means a present compliance gap. Recitals, definitions and commencement are interpretative context rather than software tasks; they are marked **Ignored** with a reason, never **Done**.

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or outside the confirmed scope; never "knowingly disregarded"). Every provision row links to the official text at that unit (act anchors `#art_N`, `#anx_1`, recitals `#rct_N`); structural/preamble labels link to the [act page](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj).

## Provision-by-provision assessment

### Recitals (B)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Recital 1](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_1) | Secure, interoperable digital identity ecosystem with wallets as cornerstone; data protection and privacy. | Indirect | Ignored | B; interpretative purpose, E1 boundary. | 2025 preamble. |
| [Recital 2](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_2) | GDPR 2016/679, 2018/1725 and, where relevant, ePrivacy apply; breach-notification duties unaffected. | Indirect | Ignored | B; not a GDPR exemption; separate data-flow review needed if a service existed. | Context, not a new legal basis. |
| [Recital 3](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_3) | Technical specifications rely on the Toolbox/Architecture and Reference Framework; Commission reviews and updates. | Indirect | Ignored | B; Commission policy, no local-app task. | Regulation 2024/1183 recital 75. |
| [Recital 4](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_4) | Fast, coordinated reactions; without prejudice to NIS2, the Cybersecurity Act and the CRA; timely suspension or withdrawal. | Indirect | Ignored | B; the cross-referenced NIS2/CRA acts are separate works (CRA commercial-supply classification Unknown in E3). | Member-State duty. |
| [Recital 5](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_5) | Member States assess reliability against uniform criteria; criteria do not automatically trigger suspension/withdrawal. | Indirect | Ignored | B; assessment policy, S. | Criteria in the Annex. |
| [Recital 6](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_6) | Evaluate whether revoking wallet unit attestations or other measures is necessary. | None | Ignored | B; S: not a wallet provider. | Member-State choice. |
| [Recital 7](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_7) | Keep wallet users and wallet-relying parties informed about breaches. | None | Ignored | B; S: no wallet users or wallet-relying parties served. | Operative duties in Arts. 5 and 9. |
| [Recital 8](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_8) | Information about breaches and consequences at least as required; assess risk of exploitation by attackers. | None | Ignored | B; S. | See Art. 5(2), Art. 9(2). |
| [Recital 9](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_9) | Re-establish wallets after remedy and inform users, relying parties, SPOCs and Commission. | None | Ignored | B; S. | Operative duties in Arts. 6 and 7. |
| [Recital 10](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_10) | Withdraw when not remedied within three months or by severity; revoke attestations; inform stakeholders. | None | Ignored | B; S. | Operative duties in Arts. 8 and 9. |
| [Recital 11](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_11) | Three-month remedy limit; Member States may set a shorter limit and prepare for withdrawal. | None | Ignored | B; interpretative of Art. 8(1); S. | Three-month period. |
| [Recital 12](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_12) | Use existing notification tools such as CIRAS/ENISA; clear, comprehensive, accessible channels. | None | Ignored | B; S: no CIRAS or notification channel (E4). | See Art. 10. |
| [Recital 13](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_13) | EDPS consulted; delivered opinion. | Indirect | Ignored | B; institutional procedural record. | Opinion 31 January 2025. |
| [Recital 14](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#rct_14) | Measures in accordance with the Article 48 eIDAS committee opinion. | Indirect | Ignored | B; institutional procedural record. | No software task. |

### Article 1 — subject matter (B)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 1, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_1) | Rules for reactions to security breaches of wallets, of the Art. 5a(8) validation mechanisms and of the eID scheme providing them. | Indirect | Ignored | B; E1–E6: ordinary local document signing/validation is outside this activity. | In force 27 May 2025. |

### Article 2 — definitions (B)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 2, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_2) | Definitions apply for the purpose of this Regulation. | Indirect | Ignored | B; interpretative rule. | No implementation measure. |
| [Art. 2(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_2) | 'Wallet solution' combines software, hardware, services, settings and configurations, including instances and WSCAs/WSCDs. | Indirect | Ignored | B; E1/E2 do not establish such a combination. | Definition. |
| [Art. 2(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_2) | 'Wallet user' controls the wallet unit. | Indirect | Ignored | B; a local signatory is not thereby a wallet user. | Definition. |
| [Art. 2(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_2) | 'Wallet-relying party' intends to rely on wallet units for digital services. | Indirect | Ignored | B; validating a signed file is not wallet reliance (E5). | Definition. |
| [Art. 2(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_2) | 'Wallet instance' is the application part of a wallet unit used for interaction. | Indirect | Ignored | B; E1 alone does not establish wallet-unit membership. | Definition. |
| [Art. 2(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_2) | 'Wallet secure cryptographic application' manages critical assets using the WSCD's functions. | Indirect | Ignored | B; E2 token signing does not confer this role. | Definition. |
| [Art. 2(6)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_2) | 'Wallet secure cryptographic device' is a tamper-resistant protected environment. | Indirect | Ignored | B; no claim that Autogram or a card reader is such a device. | Definition. |
| [Art. 2(7)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_2) | 'Wallet provider' is a natural or legal person providing wallet solutions. | Indirect | Ignored | B; E1–E3: no established wallet supply. | Definition. |
| [Art. 2(8)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_2) | 'Wallet unit' is a unique provider-supplied configuration for an individual user. | Indirect | Ignored | B; not established under E1–E4. | Definition. |
| [Art. 2(9)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_2) | 'Critical assets' are wallet-unit assets whose compromise seriously undermines reliance. | Indirect | Ignored | B; wallet-specific risk concept, not blanket app certification. | Definition. |
| [Art. 2(10)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_2) | 'Wallet unit attestation' describes or authenticates/validates wallet-unit components. | Indirect | Ignored | B; not a signed-document validation report (E5). | Definition. |

### Article 3 — establishing a security breach or compromise (B)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 3(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_3) | Member States duly consider the Annex I criteria to assess whether a breach/compromise affects reliability, without prejudice to NIS2, 2019/881 and 2024/2847. | Indirect | Ignored | B; S: a Member-State assessment duty; E3 CRA classification remains Unknown and is assessed elsewhere. | Criteria in the Annex. |
| [Art. 3(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_3) | A suspending Member State takes Articles 4–5 measures; a withdrawing Member State takes Articles 8–9 measures. | None | Ignored | S; B; future F: national scheme decision and response records. | Trigger structure. |
| [Art. 3(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_3) | A Member State aware of a possible breach in another Member State communicates without undue delay to the Commission and affected SPOCs, including Art. 5(2) information. | None | Ignored | S; B; future F: CIRAS/notification integration (absent, E4). | Art. 46c(1) SPOCs. |
| [Art. 3(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_3) | The receiving Member State takes the paragraph 1 and 2 measures without undue delay. | None | Ignored | S; B. | Cross-border cooperation. |

### Article 4 — suspension of the provision and the use of wallets and other remedies (B)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 4(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_4) | Member States ensure no wallet units are provided, used or activated under a suspended wallet solution. | None | Ignored | S; B; future F: wallet provisioning/activation controls. | At suspension. |
| [Art. 4(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_4) | Member States evaluate whether revoking affected wallet unit attestations or another remedy is necessary. | None | Ignored | S; B; future F: attestation-revocation process. | Member-State evaluation. |
| [Art. 4(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_4) | Measures under paragraphs 1 and 2 are taken without undue delay and no later than 24 hours after suspension. | None | Ignored | S; B; future F: incident timeline and records. | 24 hours. |
| [Art. 4(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_4) | Measures must not hinder the eIDAS Art. 5a(4)(g) data-portability right, subject to security of critical assets. | None | Ignored | S; B; E5 shows document-signature validation is unrelated to wallet data portability. | Both sentences assessed. |

### Article 5 — information about suspensions and remedies (B)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 5(1), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Clear, comprehensive and easily accessible suspension information provided without delay and no later than 24 hours after suspension to the following. | None | Ignored | S; B; future F: notification channel and templates. | 24 hours; parent framing. |
| [Art. 5(1)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Notify the single points of contact designated under Art. 46c(1) eIDAS. | None | Ignored | S; B. | Addressee list. |
| [Art. 5(1)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Notify the Commission. | None | Ignored | S; B. | Addressee list. |
| [Art. 5(1)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Notify the wallet users affected. | None | Ignored | S; B; no wallet users under E1–E6. | Addressee list. |
| [Art. 5(1)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Notify the wallet-relying parties registered under Art. 5b eIDAS. | None | Ignored | S; B. | Addressee list. |
| [Art. 5(2), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | The information under paragraph 1 includes at least the following. | None | Ignored | S; B. | Content list; parent framing. |
| [Art. 5(2)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Name of the provider of the suspended wallet solution. | None | Ignored | S; B. | Content item. |
| [Art. 5(2)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Name and reference identifier of the solution (Art. 5d certified-wallet list), and concerned versions where applicable. | None | Ignored | S; B. | Content item. |
| [Art. 5(2)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Date and time the breach/compromise was detected. | None | Ignored | S; B. | Content item. |
| [Art. 5(2)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | If known, date and time the breach became effective, based on logs or other sources. | None | Ignored | S; B. | Content item. |
| [Art. 5(2)(e)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Date and time of the suspension. | None | Ignored | S; B. | Content item. |
| [Art. 5(2)(f)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Contact details (email and telephone) for the notifying Member State and, where different, the wallet provider. | None | Ignored | S; B. | Content item. |
| [Art. 5(2)(g)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Description of the breach/compromise. | None | Ignored | S; B. | Content item. |
| [Art. 5(2)(h)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Description of the data compromised, including, where applicable, GDPR Art. 9(1)/10 categories. | None | Ignored | S; B. | Content item. |
| [Art. 5(2)(i)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Where possible, estimate of affected wallet users and other natural persons. | None | Ignored | S; B. | Content item. |
| [Art. 5(2)(j)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Description of potential impacts on relying parties or users, and possible user mitigation measures. | None | Ignored | S; B. | Content item. |
| [Art. 5(2)(k)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Description of measures taken or planned to remedy, with planning and deadline. | None | Ignored | S; B. | Content item. |
| [Art. 5(2)(l)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_5) | Where applicable and appropriate, measures to transition affected users to alternative solutions or services. | None | Ignored | S; B. | Content item. |

### Article 6 — re-establishment of the provision and the use of wallets (B)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 6, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_6) | Where necessary to re-establish the provision, activation and use of a wallet solution, Member States act without undue delay. | None | Ignored | S; B; future F: Member-State recovery process. | Parent framing. |
| [Art. 6(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_6) | Re-establish by issuing a wallet unit under a new version of the wallet solution to all affected users. | None | Ignored | S; B; future F: wallet re-issuance tests. | Numbered point. |
| [Art. 6(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_6) | Issue new wallet unit attestations to new or previously issued units, provided they meet post-remedy security requirements. | None | Ignored | S; B; future F: attestation re-issuance tests. | Numbered point. |
| [Art. 6(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_6) | Repeal any Article 4 measure hindering new wallet units, where linked solely to the now-remedied breach. | None | Ignored | S; B. | Numbered point. |

### Article 7 — information about re-establishment (B)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 7, opening text](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_7) | Where a Member State re-establishes a wallet solution, that Member State ensures the following. | None | Ignored | S; B. | Parent framing. |
| [Art. 7(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_7) | Provide information about re-establishment without undue delay to all parties that received the Art. 5(1) suspension information. | None | Ignored | S; B. | Numbered point. |
| [Art. 7(2), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_7) | Information under point (1) includes at least the Art. 5(2)(a), (b) and (f)–(h) elements plus the following. | None | Ignored | S; B. | Parent framing. |
| [Art. 7(2)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_7) | Date and time the breach/compromise was remedied. | None | Ignored | S; B. | Content item. |
| [Art. 7(2)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_7) | Date and time of re-establishment of the solution and, where appropriate, the affected wallet units. | None | Ignored | S; B. | Content item. |
| [Art. 7(2)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_7) | Description of the measures taken to remedy the breach/compromise. | None | Ignored | S; B. | Content item. |
| [Art. 7(2)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_7) | Description of potential residual impacts on relying parties or users, and possible mitigation measures. | None | Ignored | S; B. | Content item. |

### Article 8 — withdrawal of wallets (B)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 8(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_8) | If not remedied within three months after suspension, the providing Member State ensures the wallet solution is withdrawn and its validity revoked, without undue delay and within 72 hours after the period expires. | None | Ignored | S; B; future F: withdrawal/revocation records and timeline. | Three months + 72 hours. |
| [Art. 8(2), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_8) | When a Member State withdraws a wallet solution, it ensures the following. | None | Ignored | S; B. | Parent framing. |
| [Art. 8(2)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_8) | Wallet unit attestations of the affected solution's wallet unit are revoked. | None | Ignored | S; B. | Letter. |
| [Art. 8(2)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_8) | Wallet unit attestations cannot revert to a valid state. | None | Ignored | S; B; future F: immutable status-transition tests. | Letter. |
| [Art. 8(2)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_8) | No new wallet unit attestation can be issued to existing affected wallet units. | None | Ignored | S; B. | Letter. |
| [Art. 8(2)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_8) | No new wallet unit can be provided under the affected solution. | None | Ignored | S; B. | Letter. |
| [Art. 8(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_8) | Measures under paragraphs 1 and 2 must not hinder the eIDAS Art. 5a(4)(g) data-portability right, subject to security of critical assets. | None | Ignored | S; B. | Both sentences assessed. |

### Article 9 — information about withdrawal (B)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 9(1), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Clear, comprehensive and easily accessible withdrawal information provided without delay and no later than 24 hours after withdrawal to the following. | None | Ignored | S; B; future F: notification channel and templates. | 24 hours; parent framing. |
| [Art. 9(1)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Notify the single points of contact designated under Art. 46c(1) eIDAS. | None | Ignored | S; B. | Addressee list. |
| [Art. 9(1)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Notify the Commission. | None | Ignored | S; B. | Addressee list. |
| [Art. 9(1)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Notify the wallet users affected. | None | Ignored | S; B; no wallet users under E1–E6. | Addressee list. |
| [Art. 9(1)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Notify the wallet-relying parties registered under Art. 5b eIDAS. | None | Ignored | S; B. | Addressee list. |
| [Art. 9(2), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Information under paragraph 1 includes at least the following. | None | Ignored | S; B. | Content list; parent framing. |
| [Art. 9(2)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Name of the provider of the withdrawn wallet solution. | None | Ignored | S; B. | Content item. |
| [Art. 9(2)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Name and reference identifier of the solution (Art. 5d certified-wallet list), and concerned versions where applicable. | None | Ignored | S; B. | Content item. |
| [Art. 9(2)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Date and time of detection of the breach that led to withdrawal (severity or non-remedy within three months). | None | Ignored | S; B. | Content item. |
| [Art. 9(2)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | If known, date and time the breach became effective, based on logs or other sources. | None | Ignored | S; B. | Content item. |
| [Art. 9(2)(e)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Date and time of withdrawal and of effective revocation of the wallet unit attestations. | None | Ignored | S; B. | Content item. |
| [Art. 9(2)(f)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Whether withdrawal results from severity or from non-remedy. | None | Ignored | S; B. | Content item. |
| [Art. 9(2)(g)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Contact details (email and telephone) for the notifying Member State and, where different, the wallet provider. | None | Ignored | S; B. | Content item. |
| [Art. 9(2)(h)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Description of the breach/compromise. | None | Ignored | S; B. | Content item. |
| [Art. 9(2)(i)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Description of the data compromised, including GDPR Art. 9(1)/10 categories. | None | Ignored | S; B. | Content item. |
| [Art. 9(2)(j)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Where possible, estimate of affected wallet users and other natural persons. | None | Ignored | S; B. | Content item. |
| [Art. 9(2)(k)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Description of potential impacts on relying parties or users, and possible user mitigation measures. | None | Ignored | S; B. | Content item. |
| [Art. 9(2)(l)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_9) | Description of measures for transitioning affected users to alternative wallet solutions or, where applicable, alternative services. | None | Ignored | S; B. | Content item. |

### Articles 10–11 — information system and entry into force (B)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 10, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_10) | Member States send the information in Articles 3, 5, 7 and 9 to the Commission and Member-State SPOCs via the ENISA-operated CIRAS or an equivalent agreed system. | None | Ignored | S; B; E4: no CIRAS or notification service. | Applies from **7 May 2026**. |
| [Art. 11, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_11) | Entry into force on the twentieth day following OJ publication. | Indirect | Ignored | B; legal commencement, not a software control. | **27 May 2025**. |
| [Art. 11, unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#art_11) | Binding in its entirety and directly applicable in all Member States, except Article 10, which applies from 7 May 2026. | Indirect | Ignored | B; territorial/legal effect does not remove the activity/addressee limits. | **7 May 2026** for Article 10. |

### Annex — criteria for the assessment of a security breach or compromise (B)

The annex is unnumbered in its heading but is "Annex I" per Article 3(1); it contains five numbered points. Each criteria letter, each dash indent and the procedural points have their own row.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex, point 1, introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Member States base their assessment of a breach/compromise on the following criteria. | None | Ignored | S; B; future F: national assessment methodology. | Parent framing. |
| [Annex, point 1(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Breach caused or capable of causing death or considerable damage to health of a natural person. | None | Ignored | S; B. | Criterion. |
| [Annex, point 1(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Successful or possible malicious/unauthorised access to critical network and information systems of concerned entities, capable of causing severe operational disruption. | None | Ignored | S; B; 'concerned entities' defined here. | Criterion. |
| [Annex, point 1(c), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Availability-related criterion for a wallet solution, Art. 5a(8) validation mechanism, eID scheme or part of them. | None | Ignored | S; B. | Parent framing for two indents. |
| [Annex, point 1(c), first indent](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Complete or projected complete unavailability to users/relying parties for more than 12 consecutive hours. | None | Ignored | S; B. | 12 consecutive hours. |
| [Annex, point 1(c), second indent](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Unavailability or projected unavailability for more than 16 hours calculated on a calendar-week basis. | None | Ignored | S; B. | 16 hours per week. |
| [Annex, point 1(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Suspected or projected impact of limited availability on more than 1 % of wallet users or wallet-relying parties. | None | Ignored | S; B. | 1 % threshold. |
| [Annex, point 1(e)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Capability of or actual compromise of physical access restricted to trusted personnel, or of its protection, at network/system locations. | None | Ignored | S; B. | Criterion. |
| [Annex, point 1(f), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Compromise or capability of compromise of privacy, integrity, confidentiality or authenticity of data, in one or more of the following ways. | None | Ignored | S; B. | Parent framing for seven indents. |
| [Annex, point 1(f), first indent](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Impact on more than 1 % of affected users or more than 100 000 users, whichever is smaller. | None | Ignored | S; B. | 1 % / 100 000. |
| [Annex, point 1(f), second indent](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Result of successful suspectedly malicious activity. | None | Ignored | S; B. | Sub-criterion. |
| [Annex, point 1(f), third indent](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Result or likely result of one or more known vulnerabilities, including those handled under Implementing Regulation (EU) 2024/2981. | None | Ignored | S; B; 2024/2981 is a separate act assessed elsewhere. | External reference. |
| [Annex, point 1(f), fourth indent](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Likely risk to rights and freedoms, in particular a personal-data breach under GDPR Art. 9(1) and 10. | None | Ignored | S; B. | Sub-criterion. |
| [Annex, point 1(f), fifth indent](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Likely impact on personal electronic communications. | None | Ignored | S; B. | Sub-criterion. |
| [Annex, point 1(f), sixth indent](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Likely high risk to the rights and freedoms of natural persons. | None | Ignored | S; B. | Sub-criterion. |
| [Annex, point 1(f), seventh indent](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Likely impact on vulnerable natural persons. | None | Ignored | S; B. | Sub-criterion. |
| [Annex, point 1(g)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Certification of the wallet solution has been or is projected to be cancelled. | None | Ignored | S; B. | Criterion. |
| [Annex, point 1(h)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Direct financial loss to a concerned entity exceeding EUR 500 000 or 5 % of prior-year annual turnover, whichever is lower. | None | Ignored | S; B. | EUR 500 000 / 5 %. |
| [Annex, point 2, introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Member States shall not consider planned consequences of a maintenance operation, provided it meets both following conditions. | None | Ignored | S; B. | Parent framing. |
| [Annex, point 2(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Maintenance notified in advance to potentially affected wallet users, wallet-relying parties and relevant supervisory bodies. | None | Ignored | S; B. | Condition. |
| [Annex, point 2(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Maintenance does not meet any point 1 criterion. | None | Ignored | S; B. | Condition. |
| [Annex, point 3](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Measurement of the duration of an availability incident (from disruption or detection/log, whichever earlier) and of complete unavailability. | None | Ignored | S; B. | Applies to point 1(c). |
| [Annex, point 4](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Limited availability occurs when service is considerably slower than average response time or not all functions are available; objective criteria where possible. | None | Ignored | S; B. | Applies to point 1(d). |
| [Annex, point 5](https://eur-lex.europa.eu/eli/reg_impl/2025/847/oj#anx_1) | Determination of direct financial losses: qualifying and excluded costs; calculation from available data and estimation where amounts cannot be determined. | None | Ignored | S; B. | Applies to point 1(h). |

## Coverage and limitations

- **Texts obtained in full:** the authoritative original English act (B) including preamble and the whole annex, retrieved from the official Publications Office Cellar service. No consolidation, amendment or repeal for 2025/847 was found. The annex was present in full and was not reconstructed from a secondary summary.
- **Enumerated units:** **14 recitals**; **11 articles** (1–11); **1** unnumbered Article 1 paragraph; Article 2's introductory sentence plus **10 definitions**; **15 numbered operative paragraphs** under Articles 3, 4, 5, 8 and 9 (4 + 4 + 2 + 3 + 2); Article 5's and Article 9's **16 letters each** (4 addressee + 12 content), Article 7(2)'s **4 letters**, Article 8(2)'s **4 letters** (40 letters in total); Article 6's introductory text plus **3 numbered points**; Article 7's opening plus **2 numbered points**; Articles 6 and 7's numbering are points under a colon, not paragraphs. Article 10 is one unnumbered paragraph and Article 11 has **2 unnumbered paragraphs** (commencement and binding/applicability). The annex has **5 numbered points**: point 1's **8 letters (a)–(h)** with **2 dash indents** under (c) and **7 dash indents** under (f), point 2's **2 letters**, and unnumbered procedural text in points 3–5. The table rows total **115**, one per enumerated unit; parent framings and the article headings are structural, not invented additional duties.
- **Checking method:** parsed the complete official Cellar XHTML to enumerate recitals, articles, numbered paragraphs, letters, dash indents and annex points; cross-checked dates against the publication and the twentieth-day clause in Article 11 and the Article 10 application date. The EUR-Lex rendered HTML and the ELI OJ page were behind an anti-bot challenge, so the `#art_`/`#rct_`/`#anx_1` anchor scheme is stated from the publisher convention and the Act's own "Annex I" reference rather than re-verified against live HTML.
- **Unavailable/omitted portions:** none of the act's substantive text was unavailable or deliberately omitted. Footnote bibliographic citations, the signature block (Brussels, 6 May 2025; Ursula von der Leyen) and publication identifiers were read as sources, not counted as separate obligations.
- **External works not inspected:** the referenced ISO/IEC/RFC/ETSI standards and the Architecture and Reference Framework, plus Regulation (EU) No 910/2014 (the parent act; own register entry), Directive (EU) 2022/2555, Regulations (EU) 2019/881 and (EU) 2024/2847, Regulations (EU) 2016/679 and (EU) 2018/1725, Directive 2002/58/EC, Recommendation (EU) 2021/946, Regulation (EU) 2024/1183 and Implementing Regulation (EU) 2024/2981. Their complete requirements are separate works and are not silently imported here.
- **Outstanding evidence:** a national wallet scheme/role mandate, wallet-provider contract, wallet certification, CIRAS integration, and breach-detection/suspension/withdrawal runbooks and tests. None is an established present missing statutory measure for Autogram. Repository evidence is narrow (E1–E6) and no released binary, complete codebase role audit or organisational records were examined. The broader uncertainties recorded in the register (eIDAS Art. 15 end-user product, CRA commercial supply, all-country signature correctness) are not resolved or exempted by this wallet-specific exclusion.
