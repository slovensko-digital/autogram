# Commission Implementing Regulation (EU) 2025/848 — registration of wallet-relying parties (EU Digital Identity Wallet trust-mark framework)

**Register:** [Autogram legal-act register](README.md) · **Assessment date:** 8 October 2026 · **Repository baseline:** `5ca91d5c` · **Scope:** Autogram desktop application, locally hosted API and CLI. **Amended by:** [Regulation (EU) 2026/1730](eu-reg_impl-2026-1730.md).

## Executive summary

**What this act is.** Commission Implementing Regulation (EU) 2025/848 of 6 May 2025 lays down rules for the application of Regulation (EU) No 910/2014 as regards the **registration of wallet-relying parties** under Article 5b(11) eIDAS. It requires Member States to establish national registers with a single common API (Articles 3–6 and Annexes I–III), to publish registration policies (Article 4), to authorise **wallet-relying party access certificates** (Article 7, Annex IV) and optionally **wallet-relying party registration certificates** (Article 8, Annex V), and to keep records for ten years (Article 10).

**Working-label note.** This register cross-refers to 2025/848 as the "EU Digital Identity Wallet trust-mark" framework (see [2026/1730](eu-reg_impl-2026-1730.md)). The **official English text retrieved uses the subject "registration of wallet-relying parties"** and does not itself define or operate a wallet trust mark; the rows below assess what the retrieved act actually says.

**Relevance to Autogram: None (context).** Every operative provision addresses a **Member State, registrar, certificate authority, wallet provider, wallet-relying party or PID/EAA provider**. Autogram is a desktop application for signing and verifying documents, with a localhost HTTP API and CLI (**E1**, **E4**). It is not a wallet, wallet provider, wallet-relying party, registrar, certificate authority or PID/EAA provider, and no wallet/trust-mark/relying-party code exists at baseline (**E5**). The individual duties below are therefore scoped out, not reported as completed or breached. The one genuine technical overlap — Autogram loading the EU LOTL and national trusted lists for **signature validation** (**E3**) — is a different purpose from Annex III's use of those lists to verify wallet-relying-party entitlements.

**Dates.** Adopted **6 May 2025**; published in the OJ on **7 May 2025**; enters into force the twentieth day after publication, **27 May 2025**; **applies from 24 December 2026** (Article 11). That application date is a legal deadline for registers and wallet infrastructure, **not an Autogram release deadline**. The act was amended by [2026/1730](eu-reg_impl-2026-1730.md) (adopted 15 July 2026, published 22 July 2026, in force 11 August 2026), which changes Article 6(3)(a), deletes 6(3)(c), inserts 6(3a), replaces Article 8(1)–(2) and amends Annexes I, IV and V.

**Major unknowns.** Whether Autogram is ever used in providing an eID/trust service, and any wallet integration or role assessment, remain unresolved. No wallet-certificate, register-API, ETSI-conformity or organisational audit was performed. This is an engineering/legal reading aid, **not legal advice, certification or a released-binary audit**.

## Source and version

| Item | Value |
| --- | --- |
| Act | Commission Implementing Regulation (EU) 2025/848 of 6 May 2025 laying down rules for the application of Regulation (EU) No 910/2014 as regards the registration of wallet-relying parties |
| Official text | [ELI / complete OJ text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj/eng) · CELEX **32025R0848** · ELI `http://data.europa.eu/eli/reg_impl/2025/848/oj` · OJ L, 2025/848, 7.5.2025 |
| Text obtained | Complete official English XHTML from the Publications Office/Cellar (`32025R0848?language=eng`, `Accept: application/xhtml+xml`), 8 October 2026; EUR-Lex HTTP fetch returned an empty response, as recorded in the sibling [2024/2977 document](eu-reg_impl-2024-2977.md) |
| Version assessed | **Original published act** (adopted 6 May 2025), not a consolidation. Amended by [2026/1730](eu-reg_impl-2026-1730.md); the amendment is cross-linked and flagged at the affected rows, not imported |
| Enumerated units | 15 recitals · Articles 1–11 · 16 definitions · Annexes I–V (Annex I points 1–15; Annex II sections 1–2; Annex III points 1–4; Annex IV points 1–5; Annex V points 1–6) |
| Adopted / published / in force / applies | Adopted 6 May 2025; published 7 May 2025; in force 27 May 2025 (Art. 11, 20th day after publication); applies from **24 December 2026** |
| Baseline code | No Autogram code implements this act; scope boundary follows the [register README](README.md) |

**Anchor scheme.** Every provision row links to the authoritative text at that unit on the act's ELI page: articles `#art_1`–`#art_11`, annexes `#anx_I`–`#anx_V`, recitals `#rct_1`–`#rct_15`. The retrieved Cellar source was checked and carries `id="art_N"` and `id="anx_R"` identifiers; the `#rct_N` recital scheme follows this register's convention for original OJ acts (Cellar uses internal `d1e…` ids for recitals, so it could not be re-verified on EUR-Lex because that fetch returned empty). Structural headings (article/annex titles) link to the act page and are not counted as separate duties.

### Legend

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or outside the confirmed scope; never "knowingly disregarded"). Full definitions are in the [register README](README.md#table-vocabulary).

### Evidence and table conventions

**E1:** [`README.md`, lines 4–19](../../README.md#L4-L19), inspected: desktop signing and verification of documents, HTTP API integration and command-line batch signing. **E2:** [`SigningKey.sign`, lines 24–29](../../src/main/java/digital/slovensko/autogram/core/SigningKey.java#L24-L29), inspected: signs the to-be-signed data through the selected token; it issues no wallet credential and registers no relying party. **E3:** [`SignatureValidator.initialize`, lines 55–133](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java#L55-L133), inspected: loads the EU LOTL and national trusted lists for **signature validation**, not for wallet-relying-party entitlement verification. **E4:** [`AutogramServer.start`, lines 35–62](../../src/main/java/digital/slovensko/autogram/server/AutogramServer.java#L35-L62) and [`SignEndpoint.handle`, lines 24–40](../../src/main/java/digital/slovensko/autogram/server/SignEndpoint.java#L24-L40), inspected: a localhost HTTP API that signs documents, not a public national-register API. **E5:** repository-wide absence: `grep -rniE "wallet|trust.?mark|eudi|relying.?part" src/main/java` returns **0** matches at baseline `5ca91d5c`.

**O** = the original official English act 32025R0848 defined above, cited with its exact provision identifier. **S** = the requirement addresses a Member State, registrar, wallet, wallet-relying party, certificate authority or PID/EAA provider, none of which is an established Autogram role (E1–E5). **F** = the evidence that would be needed *if* a future deployment introduced that role (registration policy, scheme/mandate, register API fixtures, certificate profiles/tests). Neither S nor F is a present compliance gap. Recitals and definitions are interpretative context rather than software tasks.

## Provision-by-provision assessment

### Recitals (1)–(15)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Recital 1](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_1) | Member States should establish and maintain national registers of wallet-relying parties established in their territory. | None | Ignored | O; E1–E5: state duty; no national register role. | Interpretative purpose. |
| [Recital 2](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_2) | Commission regularly assesses technology and updates the specifications (Reliance on Recommendation (EU) 2021/946 and the ARF; recital 75 of [2024/1183](eu-reg-2024-1183.md)). | Indirect | Ignored | O; Commission policy, no local-app task; note [2026/1730](eu-reg_impl-2026-1730.md) actually updates the specifications. | Context, not an additional duty. |
| [Recital 3](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_3) | Member States should set up human- and machine-readable interfaces; access-certificate providers may rely on them. | None | Ignored | O; E4: Autogram's localhost signing API is not a register interface. | State/CA duty. |
| [Recital 4](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_4) | Member States should publish registration policies for their national registers. | None | Ignored | O; S: no national registration policy role. | See Art. 4. |
| [Recital 5](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_5) | Transparency purpose; wallet-relying parties should provide the necessary information to the registers. | None | Ignored | O; S: Autogram is not a wallet-relying party. | See Art. 5. |
| [Recital 6](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_6) | Wallet-relying parties should declare whether they rely on electronic identification of natural persons. | None | Ignored | O; S: no wallet reliance. | Transparency declaration. |
| [Recital 7](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_7) | Registrars should set up online/automated registration processes and verify applications without undue delay. | None | Ignored | O; S: registrar duty. | See Art. 6. |
| [Recital 8](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_8) | Member States ensure wallets can authenticate wallet-relying parties; access certificates should follow common requirements (Annex); Commission develops harmonised certificate policies. | None | Ignored | O; S: no wallet/CA role; access certificates are not ordinary signing certificates. | See Art. 7 / Annex IV. |
| [Recital 9](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_9) | Wallet-relying parties must not request extra data; registration certificates and pseudonym rules. | None | Ignored | O; S: no relying-party registration; note [GDPR](eu-reg-2016-679.md) data-minimisation context. | See Art. 8. |
| [Recital 10](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_10) | Common access policies should let a wallet warn users about oversharing. | None | Ignored | O; S: wallet-solution duty. | Interpretative. |
| [Recital 11](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_11) | Registrars may suspend/cancel registrations; proportionality; supervisory bodies also empowered (Art. 46a(4)(f) eIDAS). | None | Ignored | O; S: registrar/supervisor duty. | See Art. 9. |
| [Recital 12](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_12) | Registrars should keep records of provided information for 10 years. | None | Ignored | O; S: registrar duty; not an application log-retention rule. | See Art. 10. |
| [Recital 13](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_13) | [GDPR](eu-reg-2016-679.md) and, where relevant, Directive 2002/58/EC apply to personal-data processing under this Regulation. | Indirect | Unknown | O; pointer to the separate [GDPR assessment](eu-reg-2016-679.md); no processing under this act is established. | Context, not a new legal basis. |
| [Recital 14](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_14) | EDPS consulted under Art. 42(1) of Regulation (EU) 2018/1725; opinion delivered 31 January 2025. | Indirect | Ignored | O; institutional procedural record. | Opinion 31 Jan 2025. |
| [Recital 15](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#rct_15) | Measures are in accordance with the opinion of the committee established by Art. 48 eIDAS. | Indirect | Ignored | O; institutional procedural record. | No software task. |

### Article 1 — subject matter and scope

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 1, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_1) | Rules for the registration of wallet-relying parties. | Indirect | Ignored | O; E1–E5: ordinary document signing is outside wallet registration. | Scope provision; applies from 24 December 2026. |

### Article 2 — definitions

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 2, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | Definitions apply for this Regulation. | Indirect | Ignored | O; interpretative rule. | No implementation measure. |
| [Art. 2(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'wallet-relying party' — a relying party intending to rely on wallet units for public/private services by digital interaction. | Indirect | Ignored | O; E1: a local signatory is not thereby a wallet-relying party. | Definition. |
| [Art. 2(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'wallet unit' — provider-supplied configuration of instances, secure applications and secure devices. | Indirect | Ignored | O; E1–E2 do not establish such a configuration. | Definition. |
| [Art. 2(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'wallet solution' — combination of software, hardware, services, settings and secure components. | Indirect | Ignored | O; not every cryptographic app is a wallet. | Definition. |
| [Art. 2(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'wallet instance' — application installed on a user's device and part of a wallet unit. | Indirect | Ignored | O; E1 Autogram is a document tool, not a wallet instance. | Definition. |
| [Art. 2(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'wallet secure cryptographic application' — manages critical assets through the secure device. | Indirect | Ignored | O; E2 token signing does not confer this role. | Definition. |
| [Art. 2(6)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'wallet secure cryptographic device' — tamper-resistant environment providing cryptographic functions. | Indirect | Ignored | O; no claim that Autogram is such a device. | Definition. |
| [Art. 2(7)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'critical assets' — assets whose compromise has a very serious effect on reliance on the wallet unit. | Indirect | Ignored | O; wallet-specific risk concept, not blanket app certification. | Definition. |
| [Art. 2(8)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'wallet provider' — natural/legal person providing wallet solutions. | Indirect | Ignored | O; E1–E5: no established wallet supply. | Definition. |
| [Art. 2(9)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'wallet user' — a user in control of the wallet unit. | Indirect | Ignored | O; definition. | Definition. |
| [Art. 2(10)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'national register of wallet-relying parties' — electronic register under Art. 5b(5) eIDAS. | Indirect | Ignored | O; S: no national register. | Definition. |
| [Art. 2(11)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'provider of wallet-relying party access certificates' — person mandated by a Member State. | Indirect | Ignored | O; S: no such mandate. | Definition. |
| [Art. 2(12)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'wallet-relying party access certificate' — certificate for electronic seals/signatures authenticating the relying party. | Indirect | Ignored | O; not the user's ordinary signing certificate (E2). | Definition. |
| [Art. 2(13)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'provider of person identification data' — issues/revokes PID and cryptographically binds it to a wallet unit. | Indirect | Ignored | O; E2 signs documents, not PID. | Definition. |
| [Art. 2(14)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'registrar of wallet-relying parties' — body designated by a Member State to maintain the register. | Indirect | Ignored | O; S: no registrar role. | Definition. |
| [Art. 2(15)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'wallet-relying party registration certificate' — data object describing intended use and registered attributes. | Indirect | Ignored | O; not a signed-document validation report. | Definition. |
| [Art. 2(16)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_2) | 'provider of wallet-relying party registration certificates' — person mandated by a Member State. | Indirect | Ignored | O; S: no mandate supplied or claimed. | Definition. |

### Articles 3–4 — national registers and registration policies

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 3(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_3) | Member States establish and maintain at least one national register of wallet-relying parties. | None | Ignored | S; O; future F: national register existence/contract. | State duty. |
| [Art. 3(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_3) | The register includes at least the information in Annex I. | None | Ignored | S; O; F: register schema fixtures. | Refers to Annex I. |
| [Art. 3(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_3) | Member States designate at least one registrar. | None | Ignored | S; O; F: registrar designation. | State duty. |
| [Art. 3(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_3) | Make Annex I information publicly available online, human-readable and machine-readable. | None | Ignored | S; O; E4: Autogram's localhost API is not a public register. | State duty. |
| [Art. 3(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_3) | Information available via a single common API and a national website; electronically signed/sealed per Annex II Section 1. | None | Ignored | S; O; F: register API signature fixtures. | See Annex II. |
| [Art. 3(6)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_3) | Ensure the API complies with Annex II Section 2. | None | Ignored | S; O; F: API conformance tests. | State duty. |
| [Art. 3(7)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_3) | Ensure registers comply with the common registration policies in Article 4. | None | Ignored | S; O; F: policy mapping. | State duty. |
| [Art. 4(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_4) | Member States lay down and publish one or more national registration policies. | None | Ignored | S; O; F: published policy. | State duty. |
| [Art. 4(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_4) | Member States may include or reuse existing sectoral/national policies. | None | Ignored | S; O; permission. | State choice. |
| [Art. 4(3), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_4) | The national registration policy shall include at least information on the following. | None | Ignored | S; O; trigger framing for letters (a)–(g). | Parent provision. |
| [Art. 4(3)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_4) | Identification and authentication procedures applicable during registration. | None | Ignored | S; O; F: national policy content. | — |
| [Art. 4(3)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_4) | Required supporting documentation (identity, business registration, entitlement(s), other relevant information). | None | Ignored | S; O; F: national policy content. | — |
| [Art. 4(3)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_4) | Authentic sources or other official electronic records and their reliability for accurate data. | None | Ignored | S; O; F: national policy content. | — |
| [Art. 4(3)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_4) | Any other information or evidence required in the registration process. | None | Ignored | S; O; F: national policy content. | — |
| [Art. 4(3)(e)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_4) | Where applicable, automated means to register or update a registration. | None | Ignored | S; O; F: automated-registration evidence. | — |
| [Art. 4(3)(f)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_4) | Redress mechanism available to wallet-relying parties. | None | Ignored | S; O; F: national redress procedure. | — |
| [Art. 4(3)(g)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_4) | Rules and procedures for verifying the identity of registered parties and other relevant information. | None | Ignored | S; O; F: verification procedure. | — |
| [Art. 4(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_4) | Procedures/documentation in 3(a)–(b) enable a party to indicate the specific entitlement(s) it acts under, as in Annex I. | None | Ignored | S; O; F: entitlement mapping. | Refers to Annex I. |
| [Art. 4(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_4) | Requirements shall not impede an automated registration process where appropriate. | None | Ignored | S; O; F: automation evidence. | State duty. |

### Article 5 — information to be provided to the registers

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 5(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_5) | Wallet-relying parties provide at least the Annex I information to national registers. | None | Ignored | S; O; F: relying-party registration payload. | Party duty. |
| [Art. 5(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_5) | Ensure the information provided is accurate at the time of registration. | None | Ignored | S; O; F: accuracy evidence. | Party duty. |
| [Art. 5(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_5) | Update previously registered information without undue delay. | None | Ignored | S; O; F: update process. | Party duty. |

### Articles 6–8 — registration processes, access certificates and registration certificates

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 6(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6) | Registrars establish easy-to-use electronic, and where possible automated, registration processes. | None | Ignored | S; O; F: registrar process. | Registrar duty. |
| [Art. 6(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6) | Process applications without undue delay and respond within the policy timeframe. | None | Ignored | S; O; F: service-level evidence. | Registrar duty. |
| [Art. 6(3), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6) | Where possible, registrars verify the following in an automated manner. | None | Ignored | S; O; framing for letters (a)–(d). | Parent provision. |
| [Art. 6(3)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6) | Verify accuracy, validity, authenticity and integrity of the Article 5 information. | None | Ignored | S; O; **amended by [2026/1730](eu-reg_impl-2026-1730.md)** to reference Annex I points 1–6 and 11–16. | Original wording assessed. |
| [Art. 6(3)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6) | Where applicable, verify the power of attorney of representatives. | None | Ignored | S; O; F: verification procedure. | Registrar duty. |
| [Art. 6(3)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6) | Verify the type of entitlement(s) as set out in Annex I. | None | Ignored | S; O; **deleted by [2026/1730](eu-reg_impl-2026-1730.md)**. | Original wording assessed. |
| [Art. 6(3)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6) | Verify the absence of an existing registration in another national register. | None | Ignored | S; O; F: cross-register check. | Registrar duty. |
| [Art. 6(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6) | Verify the paragraph 3 information against supporting documentation or authentic sources/official records. | None | Ignored | S; O; F: source-access evidence. | Registrar duty. |
| [Art. 6(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6) | Verification of entitlements under 3(c) is carried out in accordance with Annex III. | None | Ignored | S; O; F: entitlement-verification evidence. | See Annex III. |
| [Art. 6(6)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6) | Where the registrar cannot verify the information under paragraphs 3–5, it rejects the registration. | None | Ignored | S; O; F: rejection logic. | Registrar duty. |
| [Art. 6(7)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6) | A party no longer intending to rely on wallet units notifies the registrar and requests cancellation. | None | Ignored | S; O; F: cancellation request flow. | Party duty. |
| [Art. 7(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_7) | Member States authorise at least one certificate authority to issue access certificates. | None | Ignored | S; O; F: authorisation record. | State duty. |
| [Art. 7(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_7) | Ensure access certificates are issued exclusively to registered wallet-relying parties. | None | Ignored | S; O; F: issuance controls. | State duty. |
| [Art. 7(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_7) | Implement certificate policies and practice statements per Annex IV, syntactically/semantically harmonised. | None | Ignored | S; O; F: certificate policy/CP statement. | See Annex IV. |
| [Art. 8(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_8) | Member States **may** authorise at least one certificate authority to issue registration certificates. | None | Ignored | S; O; **replaced by [2026/1730](eu-reg_impl-2026-1730.md)** (automated issuance without undue delay). | Optional for states. |
| [Art. 8(2), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_8) | Where a Member State authorises registration certificates, it shall do the following. | None | Ignored | S; O; framing for letters (a)–(g); **paragraph replaced by [2026/1730](eu-reg_impl-2026-1730.md)**. | Parent provision. |
| [Art. 8(2)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_8) | Require providers to issue registration certificates exclusively to registered parties. | None | Ignored | S; O; F: issuance controls. | State duty. |
| [Art. 8(2)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_8) | Ensure each intended use is expressed in the registration certificate. | None | Ignored | S; O; F: certificate content. | State duty. |
| [Art. 8(2)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_8) | Ensure a Union-harmonised general access policy informing users of the registered permitted data. | None | Ignored | S; O; F: access-policy content. | State duty. |
| [Art. 8(2)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_8) | Ensure wallet-solution providers inform users when more data than specified is requested. | None | Ignored | S; O; F: wallet UI evidence. | Wallet-provider duty. |
| [Art. 8(2)(e)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_8) | Implement registration certificates harmonised per Annex V. | None | Ignored | S; O; F: certificate profile tests. | See Annex V. |
| [Art. 8(2)(f)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_8) | Implement dedicated certificate policies and practice statements per Annex V. | None | Ignored | S; O; F: policy/CP statement. | See Annex V. |
| [Art. 8(2)(g)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_8) | Ensure parties provide a URL to the privacy policy regarding the intended use. | None | Ignored | S; O; F: privacy-policy URL. | Links to [GDPR](eu-reg-2016-679.md) context. |
| [Art. 8(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_8) | The policy referred to in point (g) shall be expressed in the registration certificate. | None | Ignored | S; O; F: certificate content. | State duty. |

### Articles 9–11 — suspension/cancellation, record keeping and entry into force

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 9(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_9) | Registrars suspend or cancel a registration where requested by a supervisory body under Art. 46a(4)(f) eIDAS. | None | Ignored | S; O; F: supervisory-request handling. | Registrar duty. |
| [Art. 9(2), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_9) | Registrars may suspend/cancel where there are reasons to believe one of the following. | None | Ignored | S; O; framing for letters (a)–(d). | Parent provision. |
| [Art. 9(2)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_9) | Registration contains inaccurate, out-of-date or misleading information. | None | Ignored | S; O; F: registrar assessment. | Ground. |
| [Art. 9(2)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_9) | The party is not compliant with the registration policy. | None | Ignored | S; O. | Ground. |
| [Art. 9(2)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_9) | The party requests more attributes than registered under Articles 5 and 6. | None | Ignored | S; O. | Ground. |
| [Art. 9(2)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_9) | The party otherwise acts in breach of Union/national law related to its role. | None | Ignored | S; O. | Ground. |
| [Art. 9(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_9) | Registrars suspend/cancel where the same party requests it. | None | Ignored | S; O; F: request handling. | Registrar duty. |
| [Art. 9(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_9) | Proportionality assessment (fundamental rights, security, confidentiality, disruption, costs); may act with or without prior notice. | None | Ignored | S; O; F: proportionality record. | Registrar duty. |
| [Art. 9(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_9) | Inform access/registration certificate providers and the affected party without undue delay and within 24 hours, including reasons and redress. | None | Ignored | S; O; F: notification within 24 hours. | Explicit 24-hour deadline. |
| [Art. 9(6)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_9) | Where applicable, providers revoke the access/registration certificates without undue delay. | None | Ignored | S; O; F: revocation propagation. | Provider duty. |
| [Art. 10, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_10) | Registrars keep records of Annex I information and subsequent changes for **10 years**. | None | Ignored | S; O; not an application log-retention rule (E1–E4). | 10-year retention. |
| [Art. 11, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_11) | Entry into force on the twentieth day following OJ publication. | Indirect | Ignored | O; legal commencement, not a software control. | In force 27 May 2025. |
| [Art. 11, unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_11) | It shall apply from 24 December 2026. | Indirect | Ignored | O; deferred application for wallets/registers, not an Autogram deadline. | Application date 24 Dec 2026. |
| [Art. 11, unnumbered paragraph 3](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_11) | Binding in its entirety and directly applicable in all Member States. | Indirect | Ignored | O; territorial/legal effect does not remove activity/addressee limits. | Legal character. |

### Annex I — information regarding wallet-relying parties

The row headings are provision points; letters are enumerated individually. **S** applies: no registration of a wallet-relying party is established, and Autogram's localhost API (E4) is not a national register. Section/annex titles are structural, not additional duties.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex I(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Where applicable, the party's name as stated in an official record, with identification data of that record. | None | Ignored | S; O; F: register payload. | — |
| [Annex I(1)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | If none are applicable, paragraph 2 shall be used. | None | Ignored | S; O; fallback rule. | — |
| [Annex I(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Where applicable, a user-friendly name recognisable to the user (trade or service name). | None | Ignored | S; O. | — |
| [Annex I(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Where applicable, one or more identifiers from an official record, expressed as points (a)–(h). | None | Ignored | S; O; framing for letters. | — |
| [Annex I(3)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | EORI number (Implementing Regulation (EU) No 1352/2013). | None | Ignored | S; O; referenced instrument is separate. | Identifier type. |
| [Annex I(3)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Registration number in a national business register. | None | Ignored | S; O. | Identifier type. |
| [Annex I(3)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | LEI (Implementing Regulation (EU) 2022/1860). | None | Ignored | S; O; referenced instrument separate. | Identifier type. |
| [Annex I(3)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | VAT registration number. | None | Ignored | S; O. | Identifier type. |
| [Annex I(3)(e)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Excise number (Art. 2(12) of Regulation (EU) No 389/2012). | None | Ignored | S; O; referenced instrument separate. | Identifier type. |
| [Annex I(3)(f)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Tax reference number. | None | Ignored | S; O. | Identifier type. |
| [Annex I(3)(g)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | EUID (Implementing Regulation (EU) 2021/1042). | None | Ignored | S; O; referenced instrument separate. | Identifier type. |
| [Annex I(3)(h)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Other national identifier(s). | None | Ignored | S; O. | Identifier type. |
| [Annex I(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | The physical address where the party is established. | None | Ignored | S; O. | — |
| [Annex I(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Where applicable, a URL belonging to the party. | None | Ignored | S; O. | — |
| [Annex I(6)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | For identifiers under 3(a),(d),(f),(h), prefix the Member State country indicator in ISO 3166-1 alpha-2 (Greece = 'EL'). | None | Ignored | S; O. | Encoding constraint. |
| [Annex I(7)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Contact information: at least one of points (a)–(c). | None | Ignored | S; O; framing for letters. | — |
| [Annex I(7)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Website for helpdesk and support. | None | Ignored | S; O. | Contact channel. |
| [Annex I(7)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Phone number for registration and intended use matters. | None | Ignored | S; O. | Contact channel. |
| [Annex I(7)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Email address for registration and intended use matters. | None | Ignored | S; O. | Contact channel. |
| [Annex I(8)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | A description of the type of services the party provides. | None | Ignored | S; O. | — |
| [Annex I(9)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | For each intended use, the list of data (attestations/attributes), user-friendly and technical names, attestation type and syntaxes, machine-readable. | None | Ignored | S; O; F: attribute schema. | — |
| [Annex I(10)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | For each intended use, a description of the intended use of the requested data. | None | Ignored | S; O. | — |
| [Annex I(11)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | An indication whether the party is a public sector body. | None | Ignored | S; O. | — |
| [Annex I(12)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | The party's entitlement(s), expressed as points (a)–(j). | None | Ignored | S; O; framing for letters. | — |
| [Annex I(12)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | 'Service_Provider' — provider of services. | None | Ignored | S; O. | Entitlement. |
| [Annex I(12)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | 'QEAA_Provider' — qualified trust service provider issuing QEAAs. | None | Ignored | S; O. | Entitlement; distinct from ordinary signing. |
| [Annex I(12)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | 'Non_Q_EAA_Provider' — trust service provider issuing non-qualified EAAs. | None | Ignored | S; O. | Entitlement. |
| [Annex I(12)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | 'PUB_EAA_Provider' — EAA provider from/for a public sector body authentic source. | None | Ignored | S; O. | Entitlement. |
| [Annex I(12)(e)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | 'PID_Provider' — provider of person identification data. | None | Ignored | S; O; E2 signs documents, not PID. | Entitlement. |
| [Annex I(12)(f)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | 'QCert_for_ESeal_Provider' — QTSP issuing qualified certificates for electronic seals. | None | Ignored | S; O. | Entitlement. |
| [Annex I(12)(g)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | 'QCert_for_ESig_Provider' — QTSP issuing qualified certificates for electronic signatures. | None | Ignored | S; O; being a signing *user* does not make Autogram such a provider. | Entitlement. |
| [Annex I(12)(h)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | 'rQSigCDs_Provider' — QTSP managing remote qualified signature creation devices. | None | Ignored | S; O. | Entitlement. |
| [Annex I(12)(i)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | 'rQSealCDs_Provider' — QTSP managing remote qualified seal creation devices. | None | Ignored | S; O. | Entitlement. |
| [Annex I(12)(j)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | 'ESig_ESeal_Creation_Provider' — non-qualified remote e-signature/seal creation provider. | None | Ignored | S; O. | Entitlement. |
| [Annex I(13)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Regarding 12(c), Member States may provide additional sub-entitlements. | None | Ignored | S; O; permission for states. | — |
| [Annex I(14)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Where applicable, an indication that the party relies on an intermediary. | None | Ignored | S; O. | — |
| [Annex I(15)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I) | Where applicable, an association to the intermediary the party relies upon. | None | Ignored | S; O. | — |

### Annex II — signatures/seals and the single common API

Section titles are structural. Section 1 sets the electronic-signature/seal requirements for the information made available on registered parties; Section 2 sets the API requirements. **S** applies throughout (E4).

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex II Section 1, bullet 1](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_II) | Use JavaScript Object Notation (JSON). | None | Ignored | S; O; E4: localhost signing API is unrelated to register publication. | Format. |
| [Annex II Section 1, bullet 2](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_II) | Use IETF 7515 for JSON Web Signatures. | None | Ignored | S; O; external RFC not inspected. | Format. |
| [Annex II Section 2(1), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_II) | The single common API shall do the following. | None | Ignored | S; O; framing for letters (a)–(e). | Parent provision. |
| [Annex II Section 2(1)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_II) | Be a REST API supporting JSON and signed per Section 1. | None | Ignored | S; O. | API requirement. |
| [Annex II Section 2(1)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_II) | Allow any requestor, without prior authentication, to search/request complete lists, including partial matches on stated parameters. | None | Ignored | S; O. | Open-access API. |
| [Annex II Section 2(1)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_II) | Replies matching at least one party include statements with current and historic access/registration certificates, excluding Annex I point 4 contact information. | None | Ignored | S; O. | Privacy carve-out. |
| [Annex II Section 2(1)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_II) | Published as OpenAPI version 3 with documentation and technical specifications. | None | Ignored | S; O. | API requirement. |
| [Annex II Section 2(1)(e)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_II) | Provide security functions (security by default and by design), availability and integrity. | None | Ignored | S; O. | API requirement. |
| [Annex II Section 2(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_II) | Statements under point (c) are electronically signed/sealed JSON files per Section 1. | None | Ignored | S; O. | Format. |

### Annex III — source of documentary evidence for entitlement verification

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex III(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_III) | Verify QEAA / qualified-certificate / remote QSCD providers against national trusted lists under eIDAS Art. 22. | Indirect | Ignored | O; E3: Autogram loads EU LOTL/national TSLs for **signature validation**, not to verify wallet-relying-party entitlements; different purpose. | Trusted-list mechanism. |
| [Annex III(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_III) | Verify non-qualified EAA/remote-creation providers against trusted lists or, if unlisted, Member States' registration-policy procedures. | Indirect | Ignored | O; E3: same trusted-list technology, different verification purpose. | National policy fallback. |
| [Annex III(3)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_III) | Verify PID providers against the Commission list under eIDAS Art. 5a(18). | None | Ignored | S; O; not the eIDAS Art. 22 trusted list consumed by E3. | Separate list. |
| [Annex III(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_III) | Verify public-sector EAA providers against the Commission list under eIDAS Art. 45f(3). | None | Ignored | S; O. | Separate list. |

### Annex IV — wallet-relying party access certificates

The certificate policy/practice-statement requirements below govern **wallet-relying-party access certificates**, not the ordinary signature-validation certificates Autogram handles via DSS (E3). Nested dashes are enumerated individually.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex IV(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | The access-certificate policy describes security requirements and applicability rules for issuance/use. | None | Ignored | S; O; distinct from ordinary signing-certificate checks (E2, E3). | CA/policy duty. |
| [Annex IV(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | The practice statement describes issuing, managing, revoking and re-keying practices. | None | Ignored | S; O. | CA duty. |
| [Annex IV(3), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Policy/statement harmonised across the Union and complying at least with NCP in ETSI EN 319411-1 v1.4.1 (2023-10); shall include letters (a)–(k). | None | Ignored | S; O; ETSI standard is a separate work, not inspected. | External standard. |
| [Annex IV(3)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Clear description of the PKI hierarchy/certification paths and expected trust anchor(s); rely on the Art. 5a(18) trust framework. | None | Ignored | S; O. | Certification path. |
| [Annex IV(3)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Comprehensive issuance procedures, including identity/attribute verification. | None | Ignored | S; O. | CA duty. |
| [Annex IV(3)(c), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Obligation to verify, when issuing, the following. | None | Ignored | S; O; framing for dashes. | Parent provision. |
| [Annex IV(3)(c), dash 1](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | The party is in a national register with valid registration status. | None | Ignored | S; O. | Verification step. |
| [Annex IV(3)(c), dash 2](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Certificate information is accurate and consistent with the registration information. | None | Ignored | S; O. | Verification step. |
| [Annex IV(3)(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Comprehensive description of revocation procedures. | None | Ignored | S; O. | CA duty. |
| [Annex IV(3)(e), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Obligation to implement measures/processes on the following. | None | Ignored | S; O; framing for dashes. | Parent provision. |
| [Annex IV(3)(e), dash 1](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Continuously monitor changes in the relevant national register. | None | Ignored | S; O. | Process. |
| [Annex IV(3)(e), dash 2](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Revoke, when changes require, issued certificates (in particular inaccurate/inconsistent content or suspended/cancelled registration). | None | Ignored | S; O. | Process. |
| [Annex IV(3)(f)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Comprehensive procedures/mechanisms for harmonised validation of access certificates across the Union. | None | Ignored | S; O. | CA duty. |
| [Annex IV(3)(g)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Allow relevant stakeholders (parties, supervisory bodies, data protection authorities) to request revocation. | None | Ignored | S; O. | CA duty. |
| [Annex IV(3)(h)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Register all such revocations and publish revocation status in a timely manner, in any event within **24 hours** of the request. | None | Ignored | S; O. | Explicit 24-hour deadline. |
| [Annex IV(3)(i)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Provide information on validity/revocation status of issued certificates. | None | Ignored | S; O; E3 validates signatures against TSLs, not this CA status service. | CA duty. |
| [Annex IV(3)(j)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Where relevant, describe logging of issued certificates in compliance with IETF RFC 9162 (Certificate Transparency v2.0). | None | Ignored | S; O; external RFC not inspected. | External standard. |
| [Annex IV(3)(k), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Obligation for the access certificates to include the following. | None | Ignored | S; O; framing for dashes. | Parent provision. |
| [Annex IV(3)(k), dash 1](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Location of the certificate supporting the advanced signature/seal, for the full path to the expected trust anchor. | None | Ignored | S; O. | Certificate content. |
| [Annex IV(3)(k), dash 2](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Machine-processable reference to the applicable certificate policy/practice statement. | None | Ignored | S; O. | Certificate content. |
| [Annex IV(3)(k), dash 3](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Include the information in Annex I points 1, 2, 3, 5, 6 and 7(a),(b),(c). | None | Ignored | S; O. | Certificate content. |
| [Annex IV(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Revocation in point 3(g) becomes effective immediately upon publication. | None | Ignored | S; O. | CA duty. |
| [Annex IV(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) | Information in point 3(h) available per certificate, at any time, beyond the validity period, automated, reliable, free of charge. | None | Ignored | S; O. | CA duty. |

### Annex V — wallet-relying party registration certificates

These requirements govern the **optional** wallet-relying-party registration certificates under Article 8. Nested dashes are enumerated individually. **S** applies; Autogram does not issue relying-party registration certificates.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex V(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | The registration-certificate policy describes security requirements/applicability rules and is published human-readable. | None | Ignored | S; O. | CA/policy duty. |
| [Annex V(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | The practice statement describes issuing, managing, revoking, re-keying and relations to access certificates; published human-readable. | None | Ignored | S; O. | CA duty. |
| [Annex V(3), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Policy/statement harmonised and complying at least with NCP in ETSI EN 319411-1 v1.4.1 (2023-10); shall include letters (a)–(j). | None | Ignored | S; O; ETSI standard is a separate work, not inspected. | External standard. |
| [Annex V(3)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Clear description of the PKI hierarchy/certification paths and expected trust anchor(s). | None | Ignored | S; O. | Certification path. |
| [Annex V(3)(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Comprehensive issuance procedures, including identity/attribute verification. | None | Ignored | S; O. | CA duty. |
| [Annex V(3)(c), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Obligation to verify, when issuing, the following. | None | Ignored | S; O; framing for dashes. | Parent provision. |
| [Annex V(3)(c), dash 1](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | The party is in a national register with valid registration status. | None | Ignored | S; O. | Verification step. |
| [Annex V(3)(c), dash 2](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Certificate information is accurate and consistent with the registration information. | None | Ignored | S; O. | Verification step. |
| [Annex V(3)(c), dash 3](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | The wallet-relying party access certificate is valid. | None | Ignored | S; O. | Verification step. |
| [Annex V(3)(c), dash 4](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | The description of the procedures for revocation of registration certificates is comprehensive. | None | Ignored | S; O. | Verification step. |
| [Annex V(3)(d), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Obligation to implement measures/processes on the following. | None | Ignored | S; O; framing for dashes. | Parent provision. |
| [Annex V(3)(d), dash 1](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Continuously monitor, in an automated manner, changes in the relevant national register. | None | Ignored | S; O. | Process. |
| [Annex V(3)(d), dash 2](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Reissue the registration certificate. | None | Ignored | S; O. | Process. |
| [Annex V(3)(d), dash 3](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Revoke issued certificates when changes require (inaccurate/inconsistent content or modified/suspended/cancelled registration). | None | Ignored | S; O. | Process. |
| [Annex V(3)(e)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Comprehensive procedures/mechanisms for harmonised validation of registration certificates. | None | Ignored | S; O. | CA duty. |
| [Annex V(3)(f)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Allow relevant stakeholders (parties, supervisory bodies, data protection authorities) to request revocation. | None | Ignored | S; O. | CA duty. |
| [Annex V(3)(g)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Register all such revocations and publish revocation status in a timely manner, in any event within **24 hours** of the request. | None | Ignored | S; O. | Explicit 24-hour deadline. |
| [Annex V(3)(h)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Provide information on validity/revocation status of issued registration certificates. | None | Ignored | S; O. | CA duty. |
| [Annex V(3)(i)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Where relevant, describe logging of issued registration certificates. | None | Ignored | S; O. | CA duty. |
| [Annex V(3)(j), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Obligation for the registration certificates to do the following. | None | Ignored | S; O; framing for dashes. | Parent provision. |
| [Annex V(3)(j), dash 1](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Include the location of the validation data of the advanced signature/seal, for the entire trust chain to the trust anchor. | None | Ignored | S; O. | Certificate content. |
| [Annex V(3)(j), dash 2](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Include a machine-readable reference to the applicable certificate policy/practice statement. | None | Ignored | S; O. | Certificate content. |
| [Annex V(3)(j), dash 3](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Include the information in Annex I points 1, 2, 3, 5, 6 and 8–15. | None | Ignored | S; O. | Certificate content. |
| [Annex V(3)(j), dash 4](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Include the URL to the privacy policy referred to in Art. 8(2)(g). | None | Ignored | S; O; links to [GDPR](eu-reg-2016-679.md) context. | Certificate content. |
| [Annex V(3)(j), dash 5](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Include a general access policy as referred to in Art. 8(3). | None | Ignored | S; O. | Certificate content. |
| [Annex V(4)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Data exchange format: signed JSON Web Tokens (RFC 7519) and CBOR Web Tokens (RFC 8392). | None | Ignored | S; O; external RFCs not inspected. | Format. |
| [Annex V(5)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Revocation in point 3(g) becomes effective immediately upon publication. | None | Ignored | S; O. | CA duty. |
| [Annex V(6)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) | Information in point 3(h) available per certificate, at any time, beyond the validity period, automated, reliable, free of charge. | None | Ignored | S; O. | CA duty. |

## Amending act

[Commission Implementing Regulation (EU) 2026/1730](eu-reg_impl-2026-1730.md) (adopted 15 July 2026, published 22 July 2026, in force 11 August 2026) amends **this** act as regards applicable standards and specifications. Its Article 1 replaces [Art. 6(3)(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6), deletes [Art. 6(3)(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6), inserts [Art. 6(3a)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_6), replaces [Art. 8(1)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_8) and [Art. 8(2)](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#art_8), and amends [Annex I](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_I), [Annex IV](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_IV) and [Annex V](https://eur-lex.europa.eu/eli/reg_impl/2025/848/oj#anx_V) through its own Annexes I–III. The replacement text of Art. 6(3)(a) refers to "Annex I, points 1 to 6 and 11 to 16", indicating that the amended Annex I contains a point 16; the rows above assess the **original** act and do not import the amended text. The amendment is assessed in its own register document.

## Coverage and limitations

- **Texts obtained in full.** The complete official English original act (CELEX **32025R0848**) as published in OJ L, 2025/848, 7.5.2025, retrieved from the Publications Office/Cellar. No consolidation was retrieved; this document assesses the **original published version only**. EUR-Lex HTTP fetches returned empty responses (consistent with the sibling [2024/2977 document](eu-reg_impl-2024-2977.md)), so the recital anchor scheme could not be re-verified on EUR-Lex itself.
- **Enumerated units assessed.** **15 recitals** (1–15); **Articles 1–11**: Art. 1 (1 unnumbered paragraph), Art. 2 (introductory paragraph + **16 definitions**), Art. 3 (7 paragraphs), Art. 4 (5 paragraphs, with 4(3)(a)–(g)), Art. 5 (3 paragraphs), Art. 6 (7 paragraphs, with 6(3)(a)–(d)), Art. 7 (3 paragraphs), Art. 8 (3 paragraphs, with 8(2)(a)–(g)), Art. 9 (6 paragraphs, with 9(2)(a)–(d)), Art. 10 (1 unnumbered paragraph), Art. 11 (3 unnumbered paragraphs); **Annex I** points 1–15 with sub-points 1(a), 3(a)–(h), 7(a)–(c) and 12(a)–(j); **Annex II** sections 1 (2 bullets) and 2 (1(1) introductory + (a)–(e), and (2)); **Annex III** points 1–4; **Annex IV** points 1–5 with 3(a)–(k) and every nested dash; **Annex V** points 1–6 with 3(a)–(j) and every nested dash. Total **194 assessed rows** (15 recitals + 78 article rows + 101 annex rows). Where the source does not number a paragraph, it is labelled "unnumbered paragraph N". Article/annex titles are structural headings, not duplicated as duties.
- **Checking method.** The complete official XHTML was parsed to enumerate recitals, articles, numbered paragraphs, letters and annex points/dashes; the text was read in full rather than summarised. Anchors `#art_N` and `#anx_R` were confirmed in the retrieved source's identifiers; `#rct_N` follows the register convention. Dates were checked against the OJ publication line (7.5.2025) and Article 11's twentieth-day and 24 December 2026 clauses.
- **Amending act.** [2026/1730](eu-reg_impl-2026-1730.md) is cross-linked; its changes to Articles 6 and 8 and Annexes I, IV and V are flagged in the affected rows but its replacement text is assessed only in its own document. The rows here describe the original act as adopted.
- **External works not inspected.** The referenced ETSI standard (EN 319411-1 v1.4.1), IETF RFCs (7515, 7519, 8392, 9162), ISO 3166-1 and the cross-referenced Union instruments (No 1352/2013, 2022/1860, No 389/2012, 2021/1042, [2014/910](eu-reg-2014-910.md), [2024/1183](eu-reg-2024-1183.md), [2016/679](eu-reg-2016-679.md), 2002/58/EC, 2018/1725) are separate works; no conformity claim follows from these rows, and their complete requirements are not silently imported.
- **Grounding in code.** The "not a wallet actor" boundary rests on E1–E4 and the zero-match repository search in E5 at baseline `5ca91d5c`. That proves specific current behaviours and the absence of wallet/trust-mark/relying-party code, not organisational compliance or every release. The only overlap with the act's subject matter is Autogram's use of the EU LOTL and national trusted lists for **signature validation** (E3), which is a different purpose from Annex III.
- **Outstanding evidence.** Any wallet integration or role assessment, national register API/policy if ever consumed, wallet-relying-party certificate profiles, and organisational/contract facts. None is an established present missing statutory measure for Autogram. The known broader uncertainties (eIDAS Art. 15 end-user product, CRA commercial supply, all-country signature-validation correctness) are not resolved by this wallet-registration exclusion. This is a reading aid, not legal advice or certification.
