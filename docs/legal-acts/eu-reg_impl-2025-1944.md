# Commission Implementing Regulation (EU) 2025/1944 — qualified electronic registered delivery services

## Executive summary

This act sets out, for the eIDAS presumption of compliance in Article 44(1a) of Regulation (EU) No 910/2014, the reference standards for qualified electronic registered delivery services (QERDS) and their interoperability. Annex I adapts **ETSI EN 319 521** (QERDS policy and security requirements); Annex II lists the **ETSI EN 319 522** series as the interoperability reference standards. The addressees are trust service providers operating (or seeking the qualified status of) a QERDS, and, for Annex II, QERD service providers agreeing to interoperate.

Autogram is a **local desktop signing and verification application**, not a qualified trust service provider and not a delivery service; the organisation operates no QERDS and receives no users' documents. Its source registers no ERDS/QERDS or registered-delivery code or endpoint. The operative requirements of Annex I therefore address **other actors** and are assessed **None / Ignored** under the confirmed scope, with the act's context and its cross-references to signature, seal and certificate inputs assessed **Indirect / Ignored**. The single genuinely actor-dependent unit is recital (6) on GDPR/ePrivacy, which is **Conditional / Unknown**. No provision is marked **Not done**: Autogram is not a QERD service provider, so these duties are not applicable rather than breached.

Autogram nevertheless creates the qualified electronic signatures/seals and advanced signatures/seals that a QERDSP may rely on when verifying sender or recipient identity or when authenticating a sender (Annex I points 5.2.1.1 and 5.2.2). Those provisions are flagged **Indirect** and grounded in the creation code. Assessment/source access: **8 October 2026**; repository baseline **5ca91d5c**. Adopted **29 September 2025**, published in the Official Journal **30 September 2025**, entry into force **20 October 2025**.

## Source and version

- Official complete English act, including both annexes: [EUR-Lex ELI](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj/eng), [Cellar XHTML/HTML](https://publications.europa.eu/resource/celex/32025R1944?language=eng), [OJ HTML](https://eur-lex.europa.eu/legal-content/EN/TXT/HTML/?uri=OJ:L_202501944).
- **CELEX 32025R1944**, OJ L, 2025/1944, 30.9.2025; ELI `http://data.europa.eu/eli/reg_impl/2025/1944/oj`. English is an official language version; the tables below are engineering/legal paraphrases, not replacement legal text.
- Original published version, not a consolidation. Adopted **29 September 2025**, published **30 September 2025**; Article 3 provides entry into force on the twentieth day following publication, i.e. **20 October 2025**. No separate deferred application date appears in this act.
- Implements Regulation (EU) No 910/2014, **Article 44(2) and Article 44(2b)**, in the framework amended by Regulation (EU) 2024/1183. It does not itself amend or repeal those acts. No amendment, repeal or corrigendum was identified in the obtained original text; a complete subsequent amendment-history check remains outstanding.
- Retrieval qualification: the Cellar English XHTML (`Accept: application/xhtml+xml`, `Accept-Language: en`) was fetched directly and contains the full operative text, all eight recitals, Articles 1–3 and both annexes, including the annex footnotes. No PDF was independently verified; the Cellar XHTML is a rendering of the OJ act and was used as the authority for this reading.

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or out of the confirmed scope; never "knowingly disregarded"). Every provision row links to the official text at that unit — act anchors `#art_1`, `#art_2`, `#art_3`, annex anchors `#anx_I` and `#anx_II`, and recital anchors `#rct_1`–`#rct_8` on the [official OJ page](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj). The anchor scheme was verified against the retrieved Cellar XHTML, which carries `id="rct_1"…"rct_8"`, `id="art_1"…"art_3"`, `id="anx_I"` and `id="anx_II"`. Annex I subpoints and referenced standards share the single `#anx_I` anchor; Annex II likewise uses `#anx_II`.

## Evidence key

All evidence below was inspected on 8 October 2026. Source inspection is not a released-binary audit or proof that tests pass; no builds or tests were run.

- **D — delivery service absent:** [README.md](../../README.md), line 4, describes Autogram as a desktop JavaFX application for signing and verifying documents, integrable through a local HTTP API. [AutogramServer.java](../../src/main/java/digital/slovensko/autogram/server/AutogramServer.java), lines 37–67, registers only `/info`, `/certificates`, `/docs`, `/sign`, `/batch` and `/assets` (plus `/api/v1/...` aliases): no endpoint sends, receives, stores or forwards documents to an addressee. A search of `src/main/java` and `src/test/java` for "ERDS", "QERDS" and "registered delivery" returns no matches, so no registered-delivery integration is present.
- **S — local server only:** [configuration.properties](../../src/main/resources/digital/slovensko/autogram/core/configuration.properties), lines 4–7, binds the API to `server.defaultAddress=localhost`, `server.defaultPort=37200`, `server.defaultProtocol=http`, `server.defaultOrigin=*`. [LaunchParameters.java](../../src/main/java/digital/slovensko/autogram/core/LaunchParameters.java), lines 43–53, resolves and validates those defaults. This is a loopback integration API, not a network delivery service.
- **G — signature/seal creation:** [SigningJob.java](../../src/main/java/digital/slovensko/autogram/core/SigningJob.java), lines 97 and 105 (signature value creation) and lines 140–142 (XAdES, CAdES and PAdES services), selects the AdES form. [SigningKey.java](../../src/main/java/digital/slovensko/autogram/core/SigningKey.java), lines 24–32, signs via an `AbstractKeyStoreTokenConnection` and returns the certificate. [PKCS11TokenDriver.java](../../src/main/java/digital/slovensko/autogram/drivers/PKCS11TokenDriver.java), line 16, and the other `drivers/` classes hold the private key in an external token/secure cryptographic device. Nothing here claims a specific certificate policy (for example NCP) or qualified status.
- **V — validation:** [SignatureValidator.java](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java), `validate` (lines 76–81) and `initialize` (lines 91–133), delegate to DSS and configure trusted lists and online CRL/OCSP sources.
- **T — timestamps:** [UserSettings.java](../../src/main/java/digital/slovensko/autogram/core/UserSettings.java), line 50 (`DEFAULT_TSA_SERVER`) and lines 361–373 (`setTsaServer` builds a `CompositeTSPSource` from external TSA URLs). Timestamp tokens come from external authorities, not from a delivery service.
- **N — no ERDS evidence:** the negative finding above. Missing organisational evidence is named in the rows; an absent Autogram feature is not proof that a QERDSP breach occurred.

## Provision-by-provision assessment

`None` below means the unit addresses QERD service providers or supervised trust service providers and no Autogram activity under the confirmed scope. `Indirect` marks interoperability/input criteria or interpretative context. "Effective" refers to 20 October 2025, which is an entry-into-force date, not a vendor implementation deadline. Parent clause labels are structural headings; their individually identifiable children carry the assessment.

### Preamble and recitals

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Preamble — institutional opening](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj) | Commission is the adopting institution. | None | Ignored | Official heading; no software duty. | Adopted 29 September 2025. |
| [Preamble — having-regard paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj) | TFEU authority. | Indirect | Ignored | Official preamble; legal basis, not an implementation task. | Original version. |
| [Preamble — having-regard paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj) | eIDAS Article 44(2) and 44(2b) provide implementing authority. | Indirect | Ignored | Official preamble; distinguishes reference-standard adoption from direct duties on a signing app. | Original version. |
| [Recital (1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#rct_1) | QERDS provide a secure transmission channel with proof of sending and receiving and high-confidence identification of sender/addressee. | Indirect | Ignored | Interpretative purpose; Autogram is not a delivery service (D, N). | No independent deadline. |
| [Recital (2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#rct_2) | The Article 44(1a) presumption applies only where the QERDS complies with the standards set out here, which must reflect established practice and add security/trustworthiness controls. | Indirect | Ignored | Interpretative scope; addresses QERDSPs. Do not turn the presumption into an unconditional software-publisher duty. | Original version. |
| [Recital (3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#rct_3) | Supervisory bodies should presume compliance with eIDAS where a provider adheres to Annex I, while a provider may rely on other practices. | Indirect | Ignored | Interpretative; supervisory/qualified-status context, not an Autogram obligation (D, N). | Original version. |
| [Recital (4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#rct_4) | Interoperable QERDSPs should adhere to Annex II so registered data can be transferred between providers and fair market practices promoted. | Indirect | Ignored | Interoperability context; no inter-provider delivery in Autogram (D, N). | Original version. |
| [Recital (5)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#rct_5) | The Commission should review and update the Regulation for technological developments. | None | Ignored | Commission task, not a software task. | Refers to Regulation 2024/1183 recital 75. |
| [Recital (6)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#rct_6) | GDPR and, where relevant, the ePrivacy Directive apply to all personal-data processing under this Regulation. | Conditional | Unknown | Autogram processes certificates, revocation data and documents locally (G, V); the publisher receives no user documents. Need an actor-specific data-flow inventory, legal basis, retention and any support-data processing records. | Context only; substantive duties arise under the referenced acts. |
| [Recital (7)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#rct_7) | EDPS consultation and opinion. | None | Ignored | Official recital records the institutional consultation. | Opinion 6 June 2025. |
| [Recital (8)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#rct_8) | Measures accord with the eIDAS Article 48 committee opinion. | None | Ignored | Institutional procedure, not an application requirement. | Original version. |

### Articles and concluding formula

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 1 — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#art_1) | The Article 44(2) reference standards for QERDS are set out in Annex I. | None | Ignored | Cross-reference to Annex I; addresses QERDSPs (D, N). Needs no Autogram evidence. | Effective; operative bridge to Annex I. |
| [Article 2 — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#art_2) | The Article 44(2b) reference standards for interoperability between QERDS are set out in Annex II. | None | Ignored | Cross-reference to Annex II; addresses QERDSPs (D, N). | Effective; operative bridge to Annex II. |
| [Article 3 — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#art_3) | Entry into force on the twentieth day following OJ publication. | Indirect | Ignored | Official publication date and Article 3; commencement, not a software measure. | 20 October 2025. |
| [Article 3 — unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#art_3) | Binding in its entirety and directly applicable in all Member States. | Indirect | Ignored | Official concluding text; establishes direct applicability, not which duty binds the publisher. | Effective; no transposition required. |
| [Closing adoption/signature formula](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#art_3) | Brussels adoption and Commission President attribution. | None | Ignored | Official text; documentary metadata, not a separate duty. | 29 September 2025; Ursula von der Leyen. |

### Annex I — reference standards for qualified electronic registered delivery services

Annex I adapts **ETSI EN 319 521 V1.1.1 (2019-02)**. The standard itself is a separate work and was not inspected; only the act's adaptations were read. All rows link to `#anx_I`.

#### Opening paragraph and adapted standard

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex I — unnumbered opening paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | ETSI EN 319 521 V1.1.1 (2019-02) applies with the adaptations in this annex. | Indirect | Ignored | Requires the external standard; the annex binds the QERDSP policy, not Autogram (D, N). | Effective; single adapted standard. |

#### Point 1(1) — clause 2.1, normative references

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(1) — reference [1]](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | ETSI EN 319 401 V3.1.1 (2024-06), general policy requirements for trust service providers. | None | Ignored | Addresses trust service providers; Autogram is not one (D). | Separate external work; fixed edition. |
| [I.1(1) — reference [2]](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | ETSI EN 319 411-1 V1.5.1 (2025-04), certificate policies including Normalised Certificate Policy (NCP). | Indirect | Ignored | NCP is invoked as an input criterion at I.1(4) and I.1(6); Autogram signs with externally issued certificates but does not assert a policy (G). Need certificate-policy evidence only if a QERDSP relies on the output. | Separate external work; fixed edition. |
| [I.1(1) — reference [3]](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | ETSI EN 319 522-1 V1.2.1 (2024-01), ERDS framework and architecture. | None | Ignored | ERDS architecture reference; no ERDS in Autogram (D, N). | Separate external work. |
| [I.1(1) — reference [4]](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | ETSI EN 319 522-2 V1.2.1 (2024-01), ERDS semantic content. | None | Ignored | ERDS content reference; no ERDS evidence messages in Autogram (D, N). | Separate external work. |
| [I.1(1) — reference [5]](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | ENISA/European Cybersecurity Certification Group "Agreed Cryptographic Mechanisms". | Indirect | Ignored | Cryptographic-mechanism context for the QERDS; Autogram selects digest/signature algorithms through DSS profiles (G), but no ENISA mapping is claimed. | External publication, cited by footnote (1). |
| [I.1(1) — reference [6]](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | ISO/IEC 15408-1:2022, IT security evaluation criteria (Common Criteria). | None | Ignored | Certifies the ERDS signing key device, not Autogram (D, N). | Separate external work; fixed edition. |
| [I.1(1) — reference [7]](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Commission Implementing Regulation (EU) 2024/482, EUCC scheme. | None | Ignored | EUCC certification of the ERDS device; addresses the trust service (D, N). | Cited by footnote (2). |
| [I.1(1) — reference [8]](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Commission Implementing Regulation (EU) 2024/3144 amending Implementing Regulation (EU) 2024/482. | None | Ignored | Amends reference [7]; addresses the trust service (D, N). | Cited by footnote (3). |
| [I.1(1) — reference [9]](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | FIPS PUB 140-3 (2019), security requirements for cryptographic modules. | None | Ignored | Certifies the ERDS signing key module until 31.12.2030; not an Autogram duty (D, N). | Separate external work. |

#### Point 1(2) — clause 3.1, terms

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(2) — "advanced electronic seal"](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Definition by reference to Regulation (EU) No 910/2014. | Indirect | Ignored | Terminology context for the seals a QERDSP may rely on; Autogram can create AdES/QESeals (G). | Definition, not a software task. |
| [I.1(2) — "advanced electronic signature"](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Definition by reference to Regulation (EU) No 910/2014. | Indirect | Ignored | Terminology context; Autogram can create AdES (G). | Definition, not a software task. |
| [I.1(2) — "qualified electronic seal"](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Definition by reference to Regulation (EU) No 910/2014. | Indirect | Ignored | Terminology context; Autogram can create qualified seals with a qualified token (G). | Definition, not a software task. |
| [I.1(2) — "qualified electronic signature"](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Definition by reference to Regulation (EU) No 910/2014. | Indirect | Ignored | Terminology context; Autogram can create qualified signatures with a qualified token (G). | Definition, not a software task. |
| [I.1(2) — "secure cryptographic device"](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | A device that holds the user's private key, protects it against compromise and performs signing/decryption on the user's behalf. | Indirect | Ignored | Autogram keeps private keys in external tokens through its `drivers/` classes (G); it does not itself supply or certify such a device. | Definition, not a software task. |

#### Point 1(3) — clause 5.1.1, common provisions

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(3), REQ-ERDS-5.1.1-01](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The ERDS shall adequately guarantee availability, integrity and confidentiality of user content while it handles it, using ENISA-approved cryptographic mechanisms. | None | Ignored | The handling entity is the ERDS; Autogram does not operate one (D, N). | Addressee: ERDS/ERDSP. |

#### Point 1(4) — clause 5.2.1.1, general identity-verification provisions

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(4), REQ-QERDS-5.2.1.1-01](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The QERDSP shall verify the recipient's identity with a very high level of confidence, directly or through a third party, using one of the listed means. | None | Ignored | Recipient identity verification is a QERDSP duty; Autogram is not one (D, N). | Addressee: QERDSP. |
| [I.1(4), REQ-QERDS-5.2.1.1-01(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Recipient identity may be verified through physical presence with appropriate evidence and procedures under national law. | None | Ignored | Offline identity proofing; no Autogram counterpart (D, N). | Alternative means. |
| [I.1(4), REQ-QERDS-5.2.1.1-01(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Recipient identity may be verified remotely by an eID means meeting eIDAS Article 8 assurance level "high", or the EU Digital Identity Wallet. | None | Ignored | Identity scheme operated by others; Autogram consumes certificates, not eID transactions (D, N). | Alternative means. |
| [I.1(4), REQ-QERDS-5.2.1.1-01(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Recipient identity may be verified by a certificate of a qualified electronic signature or a qualified electronic seal. | Indirect | Ignored | Input criterion: Autogram creates such signatures/seals (G). Whether a QERDSP relies on an Autogram-produced QES/QSeal for recipient identification is not established. | Alternative means; touchpoint row. |
| [I.1(4), REQ-QERDS-5.2.1.1-01(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Recipient identity may be verified by other methods ensuring very high confidence, confirmed by a conformity assessment body. | None | Ignored | Conformity-assessed QERDSP method; no Autogram role (D, N). | Alternative means. |
| [I.1(4), REQ-QERDS-5.2.1.1-01A](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The QERDSP shall verify the sender's identity by appropriate means, directly or through a third party, using one of the listed methods. | None | Ignored | Sender identity verification is a QERDSP duty (D, N). | Addressee: QERDSP. |
| [I.1(4), REQ-QERDS-5.2.1.1-01A(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Sender identity may be verified through physical presence with appropriate evidence and procedures under national law. | None | Ignored | Offline identity proofing; no Autogram counterpart (D, N). | Alternative means. |
| [I.1(4), REQ-QERDS-5.2.1.1-01A(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Sender identity may be verified remotely by the EU Digital Identity Wallet or a notified eID means at assurance level "substantial", issued based on prior physical presence. | None | Ignored | eID scheme operated by others (D, N). | Alternative means. |
| [I.1(4), REQ-QERDS-5.2.1.1-01A(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Sender identity may be verified by a certificate of an advanced electronic signature or advanced electronic seal issued under NCP as defined in ETSI EN 319 411-1 [2]. | Indirect | Ignored | Input criterion: Autogram creates advanced signatures/seals (G), but does not verify or assert NCP issuance; need certificate-policy evidence only if a QERDSP relies on the output. | Alternative means; touchpoint row. |
| [I.1(4), REQ-QERDS-5.2.1.1-01A(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Sender identity may be verified by other methods ensuring very high confidence, confirmed by a conformity assessment body. | None | Ignored | Conformity-assessed QERDSP method; no Autogram role (D, N). | Alternative means. |
| [I.1(4), NOTE](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The third party verifying sender and recipient identity may be another QERDSP where the parties subscribe to different QERDSPs. | None | Ignored | Explanatory note about inter-QERDSP arrangements; Autogram is not a QERDSP (D, N). | Context only. |

#### Point 1(5) — clause 5.2.1.2, recipient identification and handover of user content

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(5), REQ-QERDS-5.2.1.2-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | If recipient identification uses a QERDS-internal process, the QERDSP shall conduct the whole process in a secured and controlled environment. | None | Ignored | QERDS-internal process; Autogram operates no such service (D, N). | Addressee: QERDSP. |

#### Point 1(6) — clause 5.2.2, EU QERDS authentication provisions

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(6), REQ-QERDS-5.2.2-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | When binding authentication means to a sender identity verified under clause 5.2.1, the QERDSP shall use one of the listed means. | None | Ignored | Sender-authentication binding is a QERDSP duty (D, N). | Conditional requirement. |
| [I.1(6), REQ-QERDS-5.2.2-03(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Two-factor authentication mechanisms. | None | Ignored | QERDSP authentication method (D, N). | Alternative means. |
| [I.1(6), REQ-QERDS-5.2.2-03(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | EU Digital Identity Wallet or a notified eID means at assurance level "high" or "substantial". | None | Ignored | eID scheme operated by others (D, N). | Alternative means. |
| [I.1(6), REQ-QERDS-5.2.2-03(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Mutual TLS authentication including a certificate issued to the sender under NCP as defined in ETSI EN 319 411-1 [2]. | Indirect | Ignored | Input criterion touching certificates Autogram signs with (G); Autogram does not perform the mutual-TLS binding itself. | Alternative means; touchpoint row. |
| [I.1(6), REQ-QERDS-5.2.2-03(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | A digital signature supported by a certificate issued under NCP as defined in ETSI EN 319 411-1 [2]. | Indirect | Ignored | Input criterion: Autogram creates digital signatures (G), but no NCP assertion or QERDSP authentication integration is evidenced. | Alternative means; touchpoint row. |
| [I.1(6), REQ-QERDS-5.2.2-03(e)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Other means ensuring authentication of the identified sender, with conformity confirmed by a conformity assessment body. | None | Ignored | Conformity-assessed QERDSP method (D, N). | Alternative means. |
| [I.1(6), REQ-QERDS-5.2.2-03A](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | When binding authentication means to a recipient identity verified under clause 5.2.1, the QERDSP shall use one of the listed means ensuring very high confidence. | None | Ignored | Recipient-authentication binding is a QERDSP duty (D, N). | Conditional requirement. |
| [I.1(6), REQ-QERDS-5.2.2-03A(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | A multi-factor authentication mechanism. | None | Ignored | QERDSP authentication method (D, N). | Alternative means. |
| [I.1(6), REQ-QERDS-5.2.2-03A(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | EU Digital Identity Wallet or a notified eID means at assurance level "high" or "substantial". | None | Ignored | eID scheme operated by others (D, N). | Alternative means. |
| [I.1(6), REQ-QERDS-5.2.2-03A(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | A certificate of a qualified electronic signature or a qualified electronic seal. | Indirect | Ignored | Input criterion: Autogram creates QES/QSeal (G); reliance by a QERDSP is not established. | Alternative means; touchpoint row. |
| [I.1(6), REQ-QERDS-5.2.2-03A(d)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Other means ensuring recipient authentication, with conformity confirmed by a conformity assessment body. | None | Ignored | Conformity-assessed QERDSP method (D, N). | Alternative means. |
| [I.1(6), REQ-QERDS-5.2.2-04](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | After a machine-to-machine mutually authenticated connection based on NCP certificates, single-factor authentication may be adopted for a second sender-authentication phase if organisational procedures and security measures ensure confidence. | Indirect | Ignored | NCP-certificate and transport-authentication criterion (G); Autogram does not operate the QERDS connection. | Conditional requirement. |

#### Point 1(7) — clause 5.4.1, common evidence provisions

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(7), REQ-ERDS-5.4.1-06](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The ERDS shall generate and make available ERDS evidence about ERD events as defined in ETSI EN 319 522-1 [3]. | None | Ignored | ERDS evidence generation; Autogram produces signature evidence, not ERDS event evidence (D, N). | Addressee: ERDS. |
| [I.1(7), REQ-ERDS-5.4.1-07](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The ERDSP shall archive the evidence and/or evidence digests for each evidence it issued. | None | Ignored | ERDS evidence archiving; Autogram is not an ERDSP (D, N). | Addressee: ERDSP. |
| [I.1(7), REQ-ERDS-5.4.1-08](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The ERDS evidence shall comply with the evidence semantics defined in ETSI EN 319 522-2 [4], clause 8. | None | Ignored | ERDS evidence semantics; no ERDS evidence in Autogram (D, N). | Addressee: ERDS. |

#### Point 1(8) — clause 7.2.1, personnel and trusted roles

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(8), REQ-ERDS-7.2.1-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | ERDSP personnel in trusted roles shall have expert knowledge, experience and qualifications through training/credentials, experience, or both. | None | Ignored | ERDSP staff requirement; Autogram is a product, not an ERDSP organisation (D, N). | Addressee: ERDSP. |
| [I.1(8), REQ-ERDS-7.2.1-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Compliance with REQ-ERDS-7.2.1-02 shall include regular updates (at least every 12 months) on new threats and current security practices. | None | Ignored | ERDSP training cadence; no Autogram equivalent (D, N). | Addressee: ERDSP. |

#### Point 1(9) — clause 7.3.2, media handling

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(9), REQ-ERDS-7.3.1-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | All requirements of ETSI EN 319 401 [1], clause 7.3.3 shall apply (media handling). | None | Ignored | ERDSP media-handling control; addresses the trust service (D, N). | Addressee: ERDSP. |

#### Point 1(10) — clause 7.5, cryptographic controls

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(10), REQ-ERDS-7.5-01A](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The ERDS shall select and use suitable cryptographic techniques in accordance with the ENISA-approved Agreed Cryptographic Mechanisms [5]. | None | Ignored | ERDS cryptographic controls; Autogram's algorithms are selected via DSS profiles (G), not under this ERDS duty. | Addressee: ERDS. |
| [I.1(10), REQ-ERDSP-7.5-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The ERDS signing private key shall be held and used within a secure cryptographic device certified under one of the listed schemes. | None | Ignored | Addresses the ERDS signing key; Autogram holds user keys in external tokens (G), a distinct role. | Addressee: ERDSP. |
| [I.1(10), REQ-ERDSP-7.5-03(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Common Criteria (ISO/IEC 15408 or CC:2002) certified to EAL 4 or higher. | None | Ignored | Device-certification route for the ERDS key (D, N). | Alternative certification. |
| [I.1(10), REQ-ERDSP-7.5-03(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | EUCC [7][8] certified to EAL 4 or higher. | None | Ignored | Device-certification route for the ERDS key (D, N). | Alternative certification. |
| [I.1(10), REQ-ERDSP-7.5-03(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | FIPS PUB 140-3 [9] level 3 until 31.12.2030. | None | Ignored | Time-limited device-certification route for the ERDS key (D, N). | Sunset 31 December 2030. |
| [I.1(10), REQ-ERDSP-7.5-03 — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Certification shall be to a security target/protection profile or module design meeting the document's requirements, based on a risk analysis including physical and other non-technical measures. | None | Ignored | ERDS device certification basis (D, N). | Addressee: ERDSP. |
| [I.1(10), REQ-ERDSP-7.5-03 — unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | If the secure cryptographic device benefits from an EUCC certification, it shall be configured and used in accordance with that certification. | None | Ignored | ERDS device configuration duty (D, N). | Addressee: ERDSP. |

#### Point 1(11) — clause 7.8, network security

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(11), REQ-ERDSP-7.8-04](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The ERDSP shall use state-of-the-art transport-layer encryption protocols and algorithms compliant with the ENISA Agreed Cryptographic Mechanisms [5]. | None | Ignored | ERDSP transport security; Autogram's local server is not an ERDSP transport (S, N). | Addressee: ERDSP. |
| [I.1(11), REQ-ERDSP-7.8-06](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The vulnerability scan required by ETSI EN 319 401 [1], REQ-7.8-13 shall be performed at least once per quarter. | None | Ignored | Operational ERDSP scan cadence (D, N). | Addressee: ERDSP. |
| [I.1(11), REQ-ERDSP-7.8-07](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The penetration test required by ETSI EN 319 401 [1], REQ-7.8-17X shall be performed at least once per year. | None | Ignored | Operational ERDSP test cadence (D, N). | Addressee: ERDSP. |
| [I.1(11), REQ-ERDSP-7.8-08](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | Firewalls shall be configured to prevent all protocols and accesses not required for the operation of the trust service provider. | None | Ignored | ERDSP network-hardening duty (D, N). | Addressee: ERDSP. |

#### Point 1(12) — clause 7.12, ERDSP termination and ERDS termination plans

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(12), REQ-ERDS-7.12-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The ERDSP's termination plan shall comply with the implementing acts adopted under eIDAS Article 24(5). | None | Ignored | Service-termination plan; Autogram operates no service (D, N). | Addressee: ERDSP. |

#### Point 1(13) — clause 7.14, supply chain

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [I.1(13), REQ-ERDS-7.14-01](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_I) | The requirements of ETSI EN 319 401 [1], clause 7.14 shall apply (supply chain). | None | Ignored | ERDSP supply-chain control; addresses the trust service (D, N). | Addressee: ERDSP. |

### Annex II — reference standards for interoperability between QERDS

Annex II lists six standards that apply to interoperable QERD services. It contains no adaptations and no other operative paragraphs. The standards are separate works and were not inspected. All rows link to `#anx_II`.

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex II — unnumbered opening paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_II) | The six listed ETSI EN 319 522 series standards apply to interoperability between QERD services. | Indirect | Ignored | Interoperability context between QERDSPs; Autogram operates no such service (D, N). | Effective; no adaptations. |
| [Annex II — reference ETSI EN 319 522-1](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_II) | ETSI EN 319 522-1 V1.2.1 (2024-01) applies (ERDS framework and architecture). | None | Ignored | QERDSP interoperability standard; no Autogram role (D, N). | Separate external work. |
| [Annex II — reference ETSI EN 319 522-2](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_II) | ETSI EN 319 522-2 V1.2.1 (2024-01) applies (ERDS semantic content). | None | Ignored | QERDSP interoperability standard; no Autogram role (D, N). | Separate external work. |
| [Annex II — reference ETSI EN 319 522-3](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_II) | ETSI EN 319 522-3 V1.2.1 (2024-01) applies. | None | Ignored | QERDSP interoperability standard; no Autogram role (D, N). | Separate external work. |
| [Annex II — reference ETSI EN 319 522-4-1](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_II) | ETSI EN 319 522-4-1 V1.2.1 (2019-01) applies. | None | Ignored | QERDSP interoperability standard; no Autogram role (D, N). | Separate external work. |
| [Annex II — reference ETSI EN 319 522-4-2](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_II) | ETSI EN 319 522-4-2 V1.1.1 (2018-09) applies. | None | Ignored | QERDSP interoperability standard; no Autogram role (D, N). | Separate external work. |
| [Annex II — reference ETSI EN 319 522-4-3](https://eur-lex.europa.eu/eli/reg_impl/2025/1944/oj#anx_II) | ETSI EN 319 522-4-3 V1.1.1 (2018-09) applies. | None | Ignored | QERDSP interoperability standard; no Autogram role (D, N). | Separate external work. |

## Coverage and limitations

- Enumerated independently from the retrieved official Cellar English XHTML, not copied from the existing compliance report: **8 recitals; 3 articles** (Article 1 one unnumbered paragraph, Article 2 one unnumbered paragraph, Article 3 two unnumbered paragraphs); **1 binding closing paragraph** (Article 3, second paragraph, also assessed separately); **1 adoption/signature formula**; **3 preamble units**; **both complete annexes**. These produce **82 assessment rows**: **11 preamble/recital rows**, **5 article/concluding rows**, **59 Annex I rows** and **7 Annex II rows**.
- Annex I's sole numbered point **1** and its thirteen numbered adaptations **(1)–(13)** are structural headings, not duplicate duties. Every item is individually identified: all **nine** normative references **[1]–[9]**; all **five** defined terms; all **21** REQ-ERDS/REQ-QERDSP/REQ-ERDSP entries; all **20** lettered subpoints; the clause 5.2.1.1 **NOTE**; and both unnumbered paragraphs under REQ-ERDSP-7.5-03. Annex II's one operative sentence is split into its opening instruction plus its **six** named standards.
- Reference coverage: the annex footnotes supply the ENISA URL and the two EUCC-related implementing-regulations' ELIs; these are represented with reference rows [5], [7] and [8] rather than invented as additional obligations. ETSI EN 319 521 (adapted in Annex I) and the six ETSI EN 319 522 series standards (listed in Annex II) are cited but were **not inspected**; they are separate works, and their full clauses are not imported as text of this act. The invoked ETSI standards EN 319 401, EN 319 411-1, ISO/IEC 15408-1 and FIPS PUB 140-3 remain separate works requiring dedicated inspection.
- **Autogram-side limitation:** the code evidence establishes only that Autogram is a local signing/verification application with a loopback HTTP API, external key tokens, timestamp sources and DSS-based creation/validation. It establishes **no** ERDS/QERDS functionality, no registered-delivery endpoint, and no integration that would place Autogram within a QERDSP's identity-verification or authentication process. Whether any QERDSP relies on Autogram-produced signatures, seals or certificates is **not established**; that is the missing evidence behind the **Indirect** touchpoint rows.
- **Retrieval limitation:** the full operative text, recitals and annexes were obtained from the Cellar English XHTML; no authentic PDF was downloaded or independently verified, so pagination, formatting and any PDF-only annotations were not checked. A complete subsequent amendment/corrigendum history check remains outstanding.
- Outstanding: a documented decision whether any Autogram output is used in a QERD service (which would convert the **Indirect** touchpoint rows into a live input-criterion review); certificate-policy (NCP) evidence for certificates used in signing; and, for recital (6), an actor-specific data-processing inventory. Inspected source proves specific behaviours only, not organisational compliance. No builds, test execution, implementation changes, commits or organisational compliance assertions accompany this reading register.
