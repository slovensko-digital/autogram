# Commission Implementing Regulation (EU) 2024/2980 — notifications to the Commission concerning the European Digital Identity Wallet ecosystem

**Register:** [Autogram legal-act register](README.md) · **Assessment date:** 8 October 2026 · **Repository baseline:** `5ca91d5c`.

## Executive summary

This act lays down **notification and publication rules for the European Digital Identity Wallet ("wallet") ecosystem**. It makes the Commission provide a secure electronic notification system (Article 3), requires **Member States** to notify specified information about registrars/registers of wallet-relying parties, wallet providers, providers of person identification data (PID), and providers of wallet-relying party access/registration certificates (Article 4), and requires the **Commission** to publish and maintain the compiled lists plus their technical specifications, URLs and verification certificates (Article 5). Its whole subject matter is a public-authority information exchange; its addressees are **Member States and the Commission**, with the notified entities (wallet providers, PID providers, registrars and certificate providers) being the *subject* of notifications rather than duty-bearers.

Under the confirmed scope, Autogram is **not a Member State, the Commission, a wallet provider, a provider of person identification data, an electronic register or registrar of wallet-relying parties, or a provider of wallet-relying party access/registration certificates**. It is a desktop and CLI application that signs and validates files ([README, line 4](../../README.md#L4)). It operates no notification system and consumes none of the lists this act creates; its only related activity is validating signatures against the eIDAS **trusted lists** ([`SignatureValidator.LOTL_URL`, line 55](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java#L55)), which are separate from the wallet notification lists. The individual duties below are therefore scoped out and marked **Ignored**, not reported as completed or breached. A local signing application or localhost API does not create a notification role.

The original act was adopted **28 November 2024**, published **4 December 2024** and entered into force **24 December 2024** (Article 6, twentieth day). It is amended by **Implementing Regulation (EU) 2026/1731** (adopted 15 July 2026, published 22 July 2026), whose Article 3 replaces Article 5(2), replaces four certificate-standard points in Annex II with **ETSI EN 319 412-2 V2.4.1 / EN 319 412-3 V1.3.1**, and inserts a new **Annex II section 5** on providers of wallet-relying party registration certificates. Those operations belong to the [amending-act register](eu-reg_impl-2026-1731.md); the fullest current text is the [11 August 2026 consolidation](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811) (CELEX 02024R2980, version 001.001). None is an Autogram release deadline. This is an engineering/legal reading aid, not legal advice or certification.

## Source and version

- **B — original authoritative English act:** [ELI / complete OJ text](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj), CELEX **32024R2980**; adopted **28 November 2024**, published **4 December 2024**, OJ L 2024/2980. Article 6 (twentieth day) → entry into force **24 December 2024**; no separate deferred application provision in B.
- **C — current English consolidation:** [11 August 2026 complete text](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811), CELEX **02024R2980**, version **001.001**. A non-authoritative documentation tool (its own header says it "has no legal effect"). Its amendment table lists only **M1** (2026/1731) and no corrigendum.
- **M — authoritative amending act:** [2026/1731 complete OJ text](https://eur-lex.europa.eu/eli/reg_impl/2026/1731/oj/eng), CELEX **32026R1731**; adopted **15 July 2026**, published **22 July 2026**. Its **Article 3** amends this act; see the separate [amending-act register](eu-reg_impl-2026-1731.md). M's other amendments are not provisions of 2024/2980.
- **Text retrieved:** complete English HTML from the **official Publications Office**, using `curl -sS -L -H "Accept: application/xhtml+xml" -H "Accept-Language: en"`: [B](https://publications.europa.eu/resource/celex/32024R2980?language=eng) (83,555 bytes) and, for the amendment check, [C](https://publications.europa.eu/resource/celex/02024R2980-20260811?language=eng) (55,093 bytes). No missing annex or paragraph was inferred from a secondary summary.
- **Anchor scheme:** verified against the retrieved Cellar XHTML. The original Official Journal act uses `#rct_1`–`#rct_8`, `#art_1`–`#art_6` and `#anx_I`, `#anx_II`; the recitals do not appear in the consolidation. Technical annex rows link to the annex anchor because the publisher does not number individual list items as separate anchors.
- **Access/assessment date:** 8 October 2026. **Repository baseline:** `5ca91d5c`. Scope and shared facts: [register README](README.md). No repeal of this act was found in the retrieved current consolidation; amendment verification is limited to these official records, not a guarantee against unindexed later publications.

### Evidence and table conventions

Reviewed boundary (not an exhaustive negative proof about every release or contract):

- **E1:** [README.md, line 4](../../README.md#L4), inspected: multi-platform desktop signing/verification, HTTP API and CLI batch signing. There is no wallet-ecosystem, notification or registrar function.
- **E2:** [`SignatureValidator.LOTL_URL`, line 55](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java#L55), inspected: validation is driven by the **EU LOTL** (`https://ec.europa.eu/tools/lotl/eu-lotl.xml`) and selected national trusted lists — eIDAS trust lists, not the wallet notification lists published under this act.
- **E3:** repository-wide search under [`src/main/java/`](../../src/main/java/): **no** match for `wallet`, `EUDI`, `notification` or `registrar`. This is a bounded search, not proof about a released binary.
- **E4:** [`SigningKey.sign`, lines 24–29](../../src/main/java/digital/slovensko/autogram/core/SigningKey.java#L24-L29), inspected: signatures are created through the selected token's private key; nothing is notified to any authority.
- **E5:** [`DssSigningParametersFactory.createSignatureParameters`, lines 14–26](../../src/main/java/digital/slovensko/autogram/core/DssSigningParametersFactory.java#L14-L26) and [`SigningJob.createSignatureService`, lines 131–150](../../src/main/java/digital/slovensko/autogram/core/SigningJob.java#L131-L150), inspected: dispatch to XAdES/CAdES/PAdES only; no wallet, registration or notification code path.
- **E6:** [`UserSettings` trusted list and `SettingsDialogController`, lines 273–289](../../src/main/java/digital/slovensko/autogram/ui/gui/SettingsDialogController.java#L273-L289), inspected: the user selects countries whose eIDAS trusted lists are used for validation; that selection is distinct from any wallet notification list.

Rows cite **B** (original) or **C** (consolidation) as defined above with the exact provision identifier. **S** means the requirement addresses a Member State, the Commission, a wallet, wallet provider, PID provider, registrar or certificate provider absent from the confirmed E1–E6 scope. **F** names the evidence that would be needed *if* a future deployment introduced that role: a national scheme/mandate, notification records, registrar designation, wallet certification, or wallet-list integration and format tests. Neither S nor F means a present compliance gap. Recitals and definitions are interpretative context rather than software tasks.

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or out of the confirmed scope; never "knowingly disregarded"). Every row in the original-act tables links to the authoritative original text at that unit — act anchors `#rct_N`, `#art_N` and `#anx_R` on [https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj). Rows in the "current consolidation" section link to the corresponding unit of the [11 August 2026 consolidation](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811) and cross-link the [amending-act register](eu-reg_impl-2026-1731.md), because inserted units do not exist on the original-act page.

## Provision-by-provision assessment (original act B)

### Recitals (B; not reproduced in C)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Recital 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#rct_1) | Secure, interoperable EU digital identity ecosystem; wallets are its cornerstone; access to cross-border services with data protection and privacy. | Indirect | Ignored | B; interpretative purpose; E1 boundary. | 2024 preamble. |
| [Recital 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#rct_2) | GDPR (2016/679), Regulation 2018/1725 and, where relevant, ePrivacy Directive 2002/58 apply to all processing under this act. | Indirect | Ignored | B; not a GDPR exemption; separate data review in the [GDPR register](eu-reg-2016-679.md). | Context, not a new legal basis. |
| [Recital 3](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#rct_3) | Article 5a(23) eIDAS mandate; four implementing regulations divide protocols/interfaces, integrity/core functions, PID/EAA, and notifications; this act sets Member-State notification requirements. | Indirect | Ignored | B; institutional division of labour, not a vendor duty. | Related [2024/2979](eu-reg_impl-2024-2979.md), [2024/2977](eu-reg_impl-2024-2977.md), [2024/2982](eu-reg_impl-2024-2982.md). |
| [Recital 4](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#rct_4) | Commission regularly reviews technologies/standards; specifications rely on Recommendation (EU) 2021/946 and the Architecture and Reference Framework; Recital 75 of 2024/1183 calls for review/update. | Indirect | Ignored | B; Commission policy, no local-app task. | M (2026/1731) later updates standards. |
| [Recital 5](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#rct_5) | Member States should notify required information to the Commission's electronic system, in English, following Implementing Decision (EU) 2015/1984. | None | Ignored | B; S: not a Member State. | Notification rationale. |
| [Recital 6](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#rct_6) | Commission should establish infrastructure making the information public in a secure, human-readable, clear, easily accessible, signed/sealed and machine-processable form, including an API. | None | Ignored | B; S: not the Commission. | See Article 5. |
| [Recital 7](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#rct_7) | EDPS consulted under Article 42(1) of Regulation 2018/1725; opinion delivered 30 September 2024. | Indirect | Ignored | B; institutional procedural record. | Opinion 30 September 2024. |
| [Recital 8](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#rct_8) | Measures are in accordance with the Article 48 eIDAS committee opinion. | Indirect | Ignored | B; institutional procedural record. | No software task. |

### Article 1 — Subject matter and scope

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 1, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_1) | Establishes notification obligations enabling validation of the listed items. **Addressee: Member States/Commission.** | Indirect | Ignored | B; E1–E6 establish that ordinary local signing is outside this activity. | Framing for points (1)–(6). |
| [Art. 1(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_1) | Validation of Member-State registers of wallet-relying parties (Art. 5b(5) eIDAS), their location, and identification of their registrars. | None | Ignored | S; B; future F: national register/registrar designation. | — |
| [Art. 1(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_1) | Validation of the identity of registered wallet-relying parties. | None | Ignored | S; B; E3 no wallet-relying-party role. | — |
| [Art. 1(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_1) | Validation of the authenticity and validity of wallet units. | None | Ignored | S; B; E3 no wallet unit. | — |
| [Art. 1(4)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_1) | Identification of wallet providers. | None | Ignored | S; B; E1–E5 no wallet provision. | — |
| [Art. 1(5)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_1) | Authenticity of person identification data. | None | Ignored | S; B; E4 signs documents, not PID. | — |
| [Art. 1(6)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_1) | Identification of providers of person identification data. | None | Ignored | S; B. | — |
| [Art. 1, closing sentence](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_1) | The notifications must be updated regularly to keep in line with technology, standards and the Architecture and Reference Framework. | Indirect | Ignored | B; E1–E6; Commission/Member-State maintenance duty. | Not a software control. |

### Article 2 — Definitions

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 2, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | Definitions apply for this Regulation. | Indirect | Ignored | B; interpretative rule. | No implementation measure. |
| [Art. 2(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Wallet provider' provides wallet solutions. | Indirect | Ignored | B; E1–E5 no established wallet supply. | Definition. |
| [Art. 2(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Provider of person identification data' issues/revokes PID and cryptographically binds it to a wallet unit. | Indirect | Ignored | B; E4 signs documents, not PID. | Definition. |
| [Art. 2(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Wallet-relying party' intends to rely on wallet units for public/private services. | Indirect | Ignored | B; validating a signed file does not establish wallet reliance. | Definition. |
| [Art. 2(4)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Register of wallet-relying parties' is a Member-State electronic register under Art. 5b(5) eIDAS. | Indirect | Ignored | B; E3 no register function. | Definition. |
| [Art. 2(5)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Registrar of wallet-relying parties' is the body designated by a Member State to maintain the list. | Indirect | Ignored | B; no designation supplied or claimed. | Definition. |
| [Art. 2(6)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Wallet unit' is a unique configuration including wallet instances, WSCAs and WSCDs. | Indirect | Ignored | B; E3 no such configuration. | Definition. |
| [Art. 2(7)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Wallet solution' combines software, hardware, services, settings and configurations. | Indirect | Ignored | B; not every cryptographic application is a wallet. | Definition. |
| [Art. 2(8)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Wallet instance' is the application on the user's device forming part of a wallet unit. | Indirect | Ignored | B; E1 alone does not establish wallet membership. | Definition. |
| [Art. 2(9)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Wallet secure cryptographic application' manages critical assets via the WSCD. | Indirect | Ignored | B; E4 token signing does not make a WSCA. | Definition. |
| [Art. 2(10)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Wallet secure cryptographic device' is a tamper-resistant protected environment. | Indirect | Ignored | B; no claim that Autogram is such a device. | Definition. |
| [Art. 2(11)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Critical assets' are wallet-unit assets whose compromise seriously debilitates reliance on the unit. | Indirect | Ignored | B; wallet-specific risk concept. | Definition. |
| [Art. 2(12)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Wallet user' is the user in control of the wallet unit. | Indirect | Ignored | B; a local signatory is not thereby a wallet user. | Definition. |
| [Art. 2(13)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Provider of wallet-relying party access certificates' is mandated by a Member State to issue access certificates. | Indirect | Ignored | B; no mandate supplied or claimed. | Definition. |
| [Art. 2(14)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_2) | 'Wallet-relying party access certificate' authenticates/validates a wallet-relying party. | Indirect | Ignored | B; not the user's ordinary signing certificate. | Definition. |

### Article 3 — Notification system

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 3(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_3) | The Commission shall make available a secure electronic notification system no later than twelve months after OJ publication, for the Art. 5a(18) eIDAS bodies/mechanisms. **Addressee: Commission.** | None | Ignored | S; B; not a signing-application task. | Due by 4 December 2025. |
| [Art. 3(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_3) | The notification system shall comply with the technical requirements in Annex I. **Addressee: Commission.** | None | Ignored | S; C; Annex I requirements. | See Annex I. |

### Article 4 — Notifications by Member States

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 4(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_4) | Member States shall submit, through the Art. 3(1) system, at least the information specified in Annex II. **Addressee: Member States.** | None | Ignored | S; B; E3 no notification function. | See Annex II. |
| [Art. 4(2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_4) | Notifications at least in English; no obligation to translate supporting documents where that would be an unreasonable burden. **Addressee: Member States.** | None | Ignored | S; B. | Language rule. |
| [Art. 4(3)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_4) | The Commission may request additional information or clarifications to verify completeness and consistency. **Addressee: Commission.** | None | Ignored | S; B. | Verification power. |

### Article 5 — Publications by the Commission

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 5(1)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_5) | The Commission shall establish, maintain and publish a list compiling notified information on registrars and registers referred to in Annex II section 1. **Addressee: Commission.** | None | Ignored | S; B; E3. | — |
| [Art. 5(2), original wording](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_5) | Commission list compiling notified information on wallet providers, PID providers and providers of wallet-relying party access certificates (Annex II sections 2, 3, 4). | None | Ignored | S; historical; E3. | **Replaced** by 2026/1731 Art. 3 on 11 August 2026 (adds registration-certificate providers and a "where applicable" limiter). |
| [Art. 5(3), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_5) | The lists in paragraphs 1 and 2 shall be accessible as follows. **Addressee: Commission.** | None | Ignored | S; B; parent framing for (a)–(c). | — |
| [Art. 5(3)(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_5) | Accessible in signed/sealed machine-processable form and through a human-readable website at least in English. | None | Ignored | S; B. | — |
| [Art. 5(3)(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_5) | Accessible without registration or authentication. | None | Ignored | S; B. | Public availability. |
| [Art. 5(3)(c)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_5) | Securely accessible using state-of-the-art transport-layer encryption. | None | Ignored | S; B. | Transport security. |
| [Art. 5(4), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_5) | In addition, the Commission shall publish the following. **Addressee: Commission.** | None | Ignored | S; B; parent framing for (a)–(d). | — |
| [Art. 5(4)(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_5) | The technical specifications used for the structure of the lists. | None | Ignored | S; B. | — |
| [Art. 5(4)(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_5) | The details of the URL where the lists are published. | None | Ignored | S; B. | — |
| [Art. 5(4)(c)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_5) | The certificates used to verify the signature or seal on the lists. | None | Ignored | S; B. | — |
| [Art. 5(4)(d)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_5) | The details of mechanisms used to validate changes to the URL in (b) or the certificates in (c). | None | Ignored | S; B. | — |

### Article 6 — Entry into force

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 6, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_6) | Entry into force on the twentieth day following OJ publication. | Indirect | Ignored | B/C; legal commencement, not a software control. | 24 December 2024. |
| [Art. 6, unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#art_6) | Binding in its entirety and directly applicable in all Member States. | Indirect | Ignored | B/C; territorial/legal effect does not remove activity/addressee limits. | No general certification obligation. |

### Annex I — requirements for the Commission's notifications system

Addressee: the Commission's notification system; an internal public-authority IT system, external to Autogram (E3).

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex I, item 1](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_I) | The interface of the secure electronic notifications system shall be at least in English. | None | Ignored | S; B. | — |
| [Annex I, item 2, introductory text](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_I) | The Commission's notification system shall be designed to provide the following. | None | Ignored | S; B; parent framing for (a)–(g). | — |
| [Annex I, item 2(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_I) | Allow Member States to submit the same information only once, re-using prior submissions. | None | Ignored | S; B. | — |
| [Annex I, item 2(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_I) | Enable submission via both machine-processable and human-usable interfaces. | None | Ignored | S; B. | — |
| [Annex I, item 2(c)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_I) | Support access controls and access-control management, delegating access granting to Member States. | None | Ignored | S; B. | — |
| [Annex I, item 2(d)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_I) | Support notifications of the information specified in Annex II. | None | Ignored | S; B. | — |
| [Annex I, item 2(e)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_I) | Allow Member States to view notified information. | None | Ignored | S; B. | — |
| [Annex I, item 2(f)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_I) | Acknowledge receipt of notifications by electronic means. | None | Ignored | S; B. | — |
| [Annex I, item 2(g)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_I) | Retain, and allow Member States to view, a historical record of changes to notified information. | None | Ignored | S; B. | — |

### Annex II — requirements for Member States' notifications

Addressee: Member States (data subjects: registrars, wallet providers, PID providers and certificate providers); external to Autogram (E3). Certificate-standard rows are marked **Indirect** only as format-convergence context for any future certificate inspection.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex II, section 1, point (1), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Member States shall provide the following information on their registrars and registers. | None | Ignored | S; B; parent framing for (a)–(i). | — |
| [Annex II, section 1, point (1)(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The name of the register. | None | Ignored | S; B. | — |
| [Annex II, section 1, point (1)(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | At least one URL where the register is available, using state-of-the-art transport-layer encryption. | None | Ignored | S; B. | — |
| [Annex II, section 1, point (1)(c)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The name of the registrar responsible for that register. | None | Ignored | S; B. | — |
| [Annex II, section 1, point (1)(d)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Where applicable, the registration number of the registrar. | None | Ignored | S; B. | — |
| [Annex II, section 1, point (1)(e)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The Member State in which the registrar is established. | None | Ignored | S; B. | — |
| [Annex II, section 1, point (1)(f)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Contact email and phone number of the registrar for register matters. | None | Ignored | S; B. | — |
| [Annex II, section 1, point (1)(g)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Where applicable, the URL of a webpage with additional information about the registrar and register. | None | Ignored | S; B. | — |
| [Annex II, section 1, point (1)(h)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The URL of the webpage where the applicable registration policy and related information are located. | None | Ignored | S; B. | — |
| [Annex II, section 1, point (1)(i), original wording](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Certificate(s) compliant with IETF RFC 3647 to verify the registrar's signature/seal on register data, with certified identity data. | Indirect | Ignored | S; historical. | **Replaced** by 2026/1731 Art. 3 (now ETSI EN 319 412-2 V2.4.1 / EN 319 412-3 V1.3.1). |
| [Annex II, section 1, point (2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The information in point (1) shall be provided per register and registrar. | None | Ignored | S; B. | — |
| [Annex II, section 2, point (1), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Member States shall provide the following information on wallet providers and wallet-unit validation. | None | Ignored | S; B; parent framing for (a)–(i). | — |
| [Annex II, section 2, point (1)(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The name of the wallet provider. | None | Ignored | S; B. | — |
| [Annex II, section 2, point (1)(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Where applicable, the registration number of the wallet provider. | None | Ignored | S; B. | — |
| [Annex II, section 2, point (1)(c)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Where applicable, the name of the body responsible for providing the wallet solution. | None | Ignored | S; B. | — |
| [Annex II, section 2, point (1)(d)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The Member State in which the wallet provider is established. | None | Ignored | S; B. | — |
| [Annex II, section 2, point (1)(e)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Contact email and phone number of the wallet provider. | None | Ignored | S; B. | — |
| [Annex II, section 2, point (1)(f)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Where applicable, the URL of a webpage with additional information about the wallet provider and solution. | None | Ignored | S; B. | — |
| [Annex II, section 2, point (1)(g)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The URL of the webpage with the policies, terms and conditions applying to the wallet solution. | None | Ignored | S; B. | — |
| [Annex II, section 2, point (1)(h), original wording](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Certificate(s) compliant with IETF RFC 3647 to authenticate/validate wallet unit components, with certified identity data. | Indirect | Ignored | S; historical. | **Replaced** by 2026/1731 Art. 3 (now ETSI EN 319 412-2 V2.4.1 / EN 319 412-3 V1.3.1, wallet unit attestations). |
| [Annex II, section 2, point (1)(i)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | For each wallet solution, its name and reference number (published in the OJ under Art. 5d eIDAS). | None | Ignored | S; B. | — |
| [Annex II, section 2, point (2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The information in point (1) shall be provided per provider. | None | Ignored | S; B. | — |
| [Annex II, section 3, point (1), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Member States shall provide the following information on providers of person identification data. | None | Ignored | S; B; parent framing for (a)–(h). | — |
| [Annex II, section 3, point (1)(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The name of the PID provider. | None | Ignored | S; B. | — |
| [Annex II, section 3, point (1)(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Where applicable, a registration number of the PID provider. | None | Ignored | S; B. | — |
| [Annex II, section 3, point (1)(c)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Where applicable, the name of the body ensuring the PID is associated with the wallet unit. | None | Ignored | S; B. | — |
| [Annex II, section 3, point (1)(d)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The Member State in which the PID provider is established. | None | Ignored | S; B. | — |
| [Annex II, section 3, point (1)(e)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Contact email and phone number of the PID provider. | None | Ignored | S; B. | — |
| [Annex II, section 3, point (1)(f)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Where applicable, the URL of a webpage with additional information about the PID provider. | None | Ignored | S; B. | — |
| [Annex II, section 3, point (1)(g)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The URL of the webpage with the policies, terms and conditions applying to the PID. | None | Ignored | S; B. | — |
| [Annex II, section 3, point (1)(h), original wording](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Certificate(s) compliant with IETF RFC 3647 to verify the PID provider's signature/seal on the PID, with certified identity data. | Indirect | Ignored | S; historical. | **Replaced** by 2026/1731 Art. 3 (now ETSI EN 319 412-2 V2.4.1 / EN 319 412-3 V1.3.1). |
| [Annex II, section 3, point (2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The information in point (1) shall be provided per provider. | None | Ignored | S; B. | — |
| [Annex II, section 4, point (1), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Member States shall provide the following information on providers of wallet-relying party access certificates. | None | Ignored | S; B; parent framing for (a)–(g). | — |
| [Annex II, section 4, point (1)(a)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The name of the access-certificate provider. | None | Ignored | S; B. | — |
| [Annex II, section 4, point (1)(b)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Where applicable, a registration number of the access-certificate provider. | None | Ignored | S; B. | — |
| [Annex II, section 4, point (1)(c)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The Member State in which the access-certificate provider is established. | None | Ignored | S; B. | — |
| [Annex II, section 4, point (1)(d)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Contact email and phone number of the access-certificate provider. | None | Ignored | S; B. | — |
| [Annex II, section 4, point (1)(e)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Where applicable, the URL of a webpage with additional information about the provider and its access certificates. | None | Ignored | S; B. | — |
| [Annex II, section 4, point (1)(f)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The URL of the webpage with the policies, terms and conditions applying to the access certificates. | None | Ignored | S; B. | — |
| [Annex II, section 4, point (1)(g), original wording](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | Certificate(s) compliant with IETF RFC 3647 to verify the provider's seal/signature on access certificates, with distinguishing information. | Indirect | Ignored | S; historical. | **Replaced** by 2026/1731 Art. 3 (now ETSI EN 319 412-2 V2.4.1 / EN 319 412-3 V1.3.1). |
| [Annex II, section 4, point (2)](https://eur-lex.europa.eu/eli/reg_impl/2024/2980/oj#anx_II) | The information in point (1) shall be provided per access-certificate provider. | None | Ignored | S; B. | — |

### Current consolidation — amendments by Implementing Regulation (EU) 2026/1731

These rows record how the current text differs from B. They belong in substance to the [amending-act register](eu-reg_impl-2026-1731.md); the changed/inserted units below link to the [11 August 2026 consolidation](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811). Relevance and status follow the same non-addressee reasoning; no amendment creates a duty on a non-wallet signing application.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 5(2) (replaced)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#art_5) | Where applicable, the Commission publishes a list on wallet providers, PID providers, access-certificate providers and registration-certificate providers (Annex II sections 2–5). **Addressee: Commission.** | None | Ignored | M Art. 3; S. | Effective 11 August 2026. |
| [Annex II, section 1, point (1)(i) (replaced)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | Registrar certificates now compliant with ETSI EN 319 412-2 V2.4.1 (2025-06) or EN 319 412-3 V1.3.1 (2023-09). | Indirect | Ignored | M Art. 3; external ETSI standards not inspected; format-convergence context only. | Replaced 11 August 2026. |
| [Annex II, section 2, point (1)(h) (replaced)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | Wallet-provider certificates now authenticate/validate **wallet unit attestations**, per ETSI EN 319 412-2/-3. | Indirect | Ignored | M Art. 3; external ETSI standards not inspected. | Replaced 11 August 2026. |
| [Annex II, section 3, point (1)(h) (replaced)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | PID-provider certificates now per ETSI EN 319 412-2/-3. | Indirect | Ignored | M Art. 3; external ETSI standards not inspected. | Replaced 11 August 2026. |
| [Annex II, section 4, point (1)(g) (replaced)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | Access-certificate-provider certificates now per ETSI EN 319 412-2/-3. | Indirect | Ignored | M Art. 3; external ETSI standards not inspected. | Replaced 11 August 2026. |
| [Annex II, section 5, point 1, introductory text (inserted)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | Member States shall provide information on providers of wallet-relying party **registration** certificates. | None | Ignored | M Art. 3; S; parent framing for (a)–(g). | Inserted 11 August 2026. |
| [Annex II, section 5, point 1(a) (inserted)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | The name of the registration-certificate provider. | None | Ignored | M Art. 3; S. | Inserted 11 August 2026. |
| [Annex II, section 5, point 1(b) (inserted)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | Where applicable, a registration number of the provider. | None | Ignored | M Art. 3; S. | Inserted 11 August 2026. |
| [Annex II, section 5, point 1(c) (inserted)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | The Member State in which the provider is established. | None | Ignored | M Art. 3; S. | Inserted 11 August 2026. |
| [Annex II, section 5, point 1(d) (inserted)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | Contact email and phone number of the provider. | None | Ignored | M Art. 3; S. | Inserted 11 August 2026. |
| [Annex II, section 5, point 1(e) (inserted)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | Where applicable, the URL of a webpage with additional information about the provider and its registration certificates. | None | Ignored | M Art. 3; S. | Inserted 11 August 2026. |
| [Annex II, section 5, point 1(f) (inserted)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | The URL of the webpage with the policies, terms and conditions applying to the registration certificates. | None | Ignored | M Art. 3; S. | Inserted 11 August 2026. |
| [Annex II, section 5, point 1(g) (inserted)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | Certificate(s) compliant with ETSI EN 319 412-2 V2.4.1 or EN 319 412-3 V1.3.1 to verify the provider's seal/signature on registration certificates, with distinguishing information. | Indirect | Ignored | M Art. 3; external ETSI standards not inspected. | Inserted 11 August 2026. |
| [Annex II, section 5, point 2 (inserted)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02024R2980-20260811#anx_II) | The information in point 1 shall be provided per registration-certificate provider. | None | Ignored | M Art. 3; S. | Inserted 11 August 2026. |

## Coverage and limitations

- **Texts obtained in full:** the original authoritative English **B** (CELEX 32024R2980, 83,555 bytes) and, for the amendment check, the current English consolidation **C** (CELEX 02024R2980, version 001.001, consolidation date 11 August 2026, 55,093 bytes). Both were read from the Publications Office/Cellar XHTML. The amending act **M** (2026/1731) was not re-fetched here; its Article 3 operations are read from C's `▼M1` markers and cross-linked to the [amending-act register](eu-reg_impl-2026-1731.md).
- **Enumerated units (original act B):** **8 recitals**; **6 articles**. Inside them: Art. 1 introductory text plus **6 numbered points** and a closing sentence; Art. 2 introductory text plus **14 definitions**; Art. 3 **2** paragraphs; Art. 4 **3**; Art. 5 **2** paragraphs plus a **3-letter** list in paragraph 3 and a **4-letter** list in paragraph 4 (with their introductory texts); Art. 6 **2** unnumbered paragraphs. Annexes: **Annex I** has item 1 and item 2 with **7 letters (a)–(g)**; **Annex II** has sections 1–4, each with a numbered point (1) whose introductory text is followed by letters — section 1 has **(a)–(i)**, section 2 **(a)–(i)**, section 3 **(a)–(h)**, section 4 **(a)–(g)** — and a closing point (2). Article and annex headings are structural, not invented additional duties; unnumbered introductory/parent paragraphs are separately identifiable as framing.
- **Current consolidation (2026/1731 Article 3):** all amendment operations on this act are recorded — Art. 5(2) replaced; Annex II section 1 point (1)(i), section 2 point (1)(h), section 3 point (1)(h) and section 4 point (1)(g) replaced; **Annex II section 5 inserted** (point 1 introductory text plus letters (a)–(g) and point 2). The consolidation date **11 August 2026** is taken from the retrieved consolidation header `02024R2980 — EN — 11.08.2026 — 001.001`.
- **Checking method:** parsed the complete official Publications Office XHTML to count recitals, articles, paragraphs, letters and annex list items, and compared B against C's `▼B`/`▼M1` markers. Dates were checked against the OJ publication line and Article 6's twentieth-day entry-into-force clause. The consolidation's amendment table lists only M1 (2026/1731), with no corrigendum. No substantive part of act B was unavailable or deliberately omitted; the signature/publication block and footnote citations were read as sources, not counted as separate obligations.
- **External works not inspected:** the referenced IETF RFC 3647 and the ETSI standards **EN 319 412-2 V2.4.1** and **EN 319 412-3 V1.3.1**, plus Recommendation (EU) 2021/946, the Architecture and Reference Framework, and Implementing Decision (EU) 2015/1984. They are separate works, not part of this act's complete text; no external-standard conformity claim follows. The cross-referenced [2024/2979](eu-reg_impl-2024-2979.md), [2024/2977](eu-reg_impl-2024-2977.md), [2024/2982](eu-reg_impl-2024-2982.md) and [2026/1731](eu-reg_impl-2026-1731.md) instruments need their own registers; their complete requirements are not imported here.
- **Outstanding evidence:** actual Member-State notification records, the Commission's notification system and published lists, registrar designations, national registration policies, and any wallet-provider/PID-provider/access- or registration-certificate-provider participation. None is a present missing statutory measure for Autogram, because the act addresses Member States and the Commission, not signing-application vendors. Repository evidence is narrow (E1–E6) and no released binary, complete codebase role audit or organisational records were examined. The known broader uncertainties (eIDAS Article 15 end-user product, CRA commercial supply, trusted-list/signature-validation correctness) are not resolved or exempted by this wallet-notification exclusion.
