# Commission Implementing Regulation (EU) 2025/1929 — qualified electronic time stamps (time binding and time-source accuracy)

## Executive summary

This act specifies, for qualified electronic time stamp **services**, the reference standards and specifications referred to in Article 42(2) of Regulation (EU) No 910/2014 and the adaptations under which the Article 42(1a) presumption of compliance applies. It names **ETSI EN 319 421 V1.3.1** (policy and security requirements for trust service providers issuing time stamps) and **ETSI EN 319 422 V1.1.1** (time-stamping protocol and time-stamp token profiles), each as adapted in the single Annex. Its addressees are trust service providers operating a time-stamping authority (TSA) and the supervisory bodies that grant or confirm qualified status; it is not a signature-creation or validation-application regulation.

Autogram is a desktop signing/validation application operated locally by the signer and by the verifier. It **consumes** timestamps but is **not a TSA and not a qualified trust-service provider**, and it operates no hosted time-stamping or document service. As a time-stamping *client* it requests RFC 3161 tokens for `BASELINE_T` signatures from a user-configured TSA URL, and it parses, classifies and displays the tokens it validates. Consequently the TSA-facing operational and security requirements in point 1 of the Annex are **None/Ignored** under the confirmed scope, while the protocol/token-profile and transport provisions in point 2 are **Indirect** interoperation/input criteria — except point 2(9), which expressly names the time-stamping *client* and is therefore **Conditional** on Autogram requesting a timestamp.

Inspected source establishes a real client path and a real consumption path: configuration of one or more TSA URLs into a DSS `CompositeTSPSource`, attaching that source to the signature service at `BASELINE_T`, and reading `getSignatureTimestamps`, timestamp indications and `TimestampQualification` (QTSA/TSA) from the DSS simple report in the UI. It does **not** establish that requested tokens are qualified, that they conform to the adapted EN 319 422 profile, or that the client uses HTTPS exclusively. The default and pre-defined TSA URLs are plain `http://`. Overall fulfilment remains **Unknown**; missing proof is not a demonstrated statutory violation and the cited ETSI standards are separate works that were not inspected.

Assessment/source access: **8 October 2026**; repository baseline **5ca91d5c**. Adopted **29 September 2025**, published **30 September 2025**, in force **20 October 2025**.

## Source and version

- Official complete English act, including the single Annex and its footnotes: [EUR-Lex ELI](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj/eng), [OJ HTML](https://eur-lex.europa.eu/legal-content/EN/TXT/HTML/?uri=OJ:L_202501929), [authentic OJ PDF](https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=OJ%3AL_202501929).
- **CELEX 32025R1929**, OJ L, 2025/1929, 30.9.2025; ELI `http://data.europa.eu/eli/reg_impl/2025/1929/oj`. No C/2025 catalogue number appears in the retrieved text. English is an official language version; the tables below are engineering/legal paraphrases, not a replacement for the legal text.
- Original published version, not a consolidation. Article 2 provides entry on the twentieth day following publication: **20 October 2025**; no separate deferred application date appears.
- Implements Regulation (EU) No 910/2014, Article 42(2), in the framework amended by Regulation (EU) 2024/1183. It does not itself amend or repeal those acts. No amendment/repeal/corrigendum was identified in the obtained original text; a complete subsequent amendment-history check remains outstanding.
- Retrieval qualification: the requested Cellar resource (`application/xhtml+xml`, English) was returned in full by `curl` (200, complete XHTML including the Annex, the point 1/point 2 adaptations and footnotes 1–5). The full act body, both adaptation sections and the footnotes were read from that response. No authentic PDF was downloaded or independently verified.
- **Provision-link scheme (verified against the retrieved XHTML):** act anchors `#art_1`, `#art_2`; recital anchors `#rct_1`–`#rct_7`; the act's single annex is `#anx_1` (the retrieved identifier, not a roman numeral). All rows link to these anchors on [the official OJ page](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj); preamble rows without a unit anchor link the page itself.

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or out of the confirmed scope; never "knowingly disregarded").

- **None** below means the provision addresses the TSA/qualified trust service provider under the confirmed scope; Autogram is not that actor.
- **Indirect** means interoperability/input criteria or interpretative context for the timestamps Autogram requests or validates, not a direct obligation on this publisher.
- **Conditional** is used only where the act or the adapted standard expressly names the time-stamping client, which Autogram is when it makes a `BASELINE_T` signature.

## Evidence key

All evidence below was inspected on 8 October 2026. Source inspection is not a released-binary audit; no builds or tests were run, and no test assertions were executed.

- **C — timestamp client / signing:** [UserSettings.java](../../src/main/java/digital/slovensko/autogram/core/UserSettings.java), `DEFAULT_TSA_SERVER` line 50 — the defaults are `http://timestamp.sectigo.com/qualified,http://tsa.belgium.be/connect` (plain HTTP); `PROBLEMATIC_DEFAULT_TSA_SERVERS` lines 52–57; `setTsaServer`, lines 361–375, splits a comma-separated URL list and builds a DSS `CompositeTSPSource` of `OnlineTSPSource` instances with a `TimestampDataLoader`; `getTspSource`, lines 385–387. [SigningJob.java](../../src/main/java/digital/slovensko/autogram/core/SigningJob.java), `createSignatureService`, lines 131–149, attaches the TSP source only when the profile is `BASELINE_T` and a non-null source is supplied (lines 146–147). [Autogram.java](../../src/main/java/digital/slovensko/autogram/core/Autogram.java), `signCommonAndThen`, lines 107–109, passes `settings.getTspSource()` into the signing job. [DssSigningParametersFactory.java](../../src/main/java/digital/slovensko/autogram/core/DssSigningParametersFactory.java), lines 82–83, enlarges the PAdES content size for `BASELINE_T` to hold a token. [AppStarter.java](../../src/main/java/digital/slovensko/autogram/core/AppStarter.java), `--tsa-server` option line 28. [CliSettings.java](../../src/main/java/digital/slovensko/autogram/ui/cli/CliSettings.java), lines 40–41.
- **V — validation/consumption:** [SignatureValidator.java](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java), `validate`, lines 76–81, delegates to DSS `validateDocument()`; `initialize`, lines 91–133, configures LOTL/country filtering and online CRL/OCSP sources. [GUIValidationUtils.java](../../src/main/java/digital/slovensko/autogram/ui/gui/GUIValidationUtils.java), `createSignatureBox`, lines 99–144, reads `simple.getSignatureTimestamps(signatureId)` and computes `isTimestampInvalid` / `isTimestampIndeterminate` from DSS indications (lines 112–116); `createTimestampsBox`, lines 234–259, renders each timestamp's signing certificate and qualification; `validityToString`, lines 171–194, downgrades the result when a timestamp is invalid or indeterminate. [SignatureBadgeFactory.java](../../src/main/java/digital/slovensko/autogram/ui/gui/SignatureBadgeFactory.java), `createBadgeFromTSQualification`, lines 97–109, maps `TimestampQualification.QTSA`/`TSA`; `areTimestampsQualified`, lines 206–213, and `areTimestampsFailed`, lines 215–222. `SignatureValidator.java` contains no timestamp-validation logic of its own.
- **T — tests:** SignatureValidationPresentationTest.java, `indeterminateTimestampDoesNotProduceValidResult`, lines 35–41, asserts an indeterminate timestamp yields "Indeterminate". [SigningParametersResolverTests.java](../../src/test/java/digital/slovensko/autogram/core/SigningParametersResolverTests.java), `pdfForPadesWithTsaMustStayPades`, lines 404–415. [TestAutogramFactory.java](../../src/test/java/digital/slovensko/autogram/TestAutogramFactory.java), `TEST_TSA_URL = "https://freetsa.org/tsr"`, line 37 — the test TSA is HTTPS; the shipped defaults (C) are HTTP. These are mocked/structural tests, not interoperable token-profile conformance tests.
- **E — error handling:** [AutogramException.java](../../src/main/java/digital/slovensko/autogram/core/errors/AutogramException.java), lines 79–82, maps DSS external-resource failures to `TsaServerMisconfiguredException`; [TsaServerMisconfiguredException.java](../../src/main/java/digital/slovensko/autogram/core/errors/TsaServerMisconfiguredException.java), lines 3–14; [CliUI.java](../../src/main/java/digital/slovensko/autogram/ui/cli/CliUI.java), lines 279–280.
- [pom.xml](../../pom.xml), lines 18 and 44–46, selects DSS **6.5**. Dependency selection does not itself prove token-profile, algorithm or transport conformity.

## Provision-by-provision assessment

`Indirect` context below means the provision describes or presumes the qualified time-stamping service whose tokens Autogram requests and validates; it does not create an Autogram duty. Parent point labels (point 1, point 2, and their numbered adaptations) are structural headings and carry no separate assessment row.

### Preamble and recitals

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Preamble — institutional opening](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj) | The Commission is the adopting institution. | None | Ignored | Official heading; no software duty. | Adopted 29 September 2025. |
| [Preamble — having-regard paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj) | Treaty on the Functioning of the European Union authority. | Indirect | Ignored | Official preamble; legal basis, not an implementation task. | Original version. |
| [Preamble — having-regard paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj) | Regulation (EU) No 910/2014, and in particular Article 42(2), provides the implementing authority. | Indirect | Ignored | Official preamble; identifies the qualified-time-stamp issuance route, not signature-application duties. | Footnote (1): OJ L 257, 28.8.2014, p. 73, ELI `http://data.europa.eu/eli/reg/2014/910/oj`. |
| [Recital (1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#rct_1) | Qualified electronic time stamps bind date and time to electronic data and help ensure the accuracy of the indicated time and the integrity of the bound documents. | Indirect | Ignored | Purpose/context; Autogram displays `timestamp.getProductionTime()` (V). | "Time binding / source-time accuracy" framing. No independent deadline. |
| [Recital (2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#rct_2) | The Article 42(1a) presumption applies only where qualified trust services for issuing time stamps comply with the standards in this Regulation, adapted with additional controls for security, trustworthiness, time binding and time-source accuracy. | Indirect | Ignored | Interpretative scope; the standards bind the QTSP/TSA. Autogram only consumes tokens (V). | Do not convert the presumption into a signature-application duty. |
| [Recital (3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#rct_3) | Where a trust service provider adheres to the Annex, supervisory bodies should presume compliance when granting or confirming qualified status; a provider may rely on other practices. | Indirect | Ignored | Supervisory-body / QTSP context; no Autogram obligation. | — |
| [Recital (4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#rct_4) | The Commission regularly assesses technologies and should review/update this Regulation, per Recital 75 of Regulation (EU) 2024/1183. | None | Ignored | Commission task, not a software task. | Footnote (2): Regulation (EU) 2024/1183. |
| [Recital (5)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#rct_5) | Regulation (EU) 2016/679 and, where relevant, Directive 2002/58/EC apply to personal-data processing under this Regulation. | Conditional | Unknown | Local certificate/timestamp token handling exists (C/V); the publisher receives no user documents. Need actor-specific data-flow inventory, legal basis, retention and support-data records. | Context only; substantive duties arise under the referenced acts. |
| [Recital (6)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#rct_6) | The EDPS was consulted under Article 42(1) of Regulation (EU) 2018/1725 and delivered its opinion. | None | Ignored | Institutional consultation recorded by the act. | Opinion 6 June 2025. |
| [Recital (7)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#rct_7) | The measures accord with the opinion of the committee established by Article 48 of Regulation (EU) No 910/2014. | None | Ignored | Institutional procedure, not an application requirement. | — |

### Articles and concluding formula

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 1 — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#art_1) | The reference standards and specifications referred to in Article 42(2) of Regulation (EU) No 910/2014 are set out in the Annex. | Indirect | Ignored | Scoping referral to the Annex; the Annex carries the technical rows. | Article 1 has a single paragraph. |
| [Article 2 — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#art_2) | Entry into force on the twentieth day following OJ publication. | Indirect | Ignored | Official publication date and Article 2; commencement, not a software measure. | 20 October 2025. |
| [Closing binding formula — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#fnp_1) | Binding in its entirety and directly applicable in all Member States. | Indirect | Ignored | Official concluding text; no transposition required. | — |
| [Closing adoption/signature formula](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#fnp_1) | Brussels adoption and Commission President attribution. | None | Ignored | Official text; documentary metadata, not a separate duty. | 29 September 2025; Ursula von der Leyen. |

### Annex — opening paragraph and reference footnotes

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex — unnumbered opening paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | ETSI EN 319 421 V1.3.1 and ETSI EN 319 422 V1.1.1 apply, with the following adaptations, to qualified time stamp services. | Indirect | Ignored | Defines the standards a QETS-issuing service must meet; it is a requirement on the TSA, not on a signature application. Autogram's requested/validated tokens are the consumption side (C/V). | The external standards are separate works, not inspected. |
| [Annex — footnote (1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | EN 319 421 — Electronic Signatures and Infrastructures (ESI); Policy and Security Requirements for Trust Service Providers issuing Time Stamps, V1.3.1. | Indirect | Ignored | Supplies the external specification identity; separate work, not inspected. | Fixed edition. |
| [Annex — footnote (2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | EN 319 422 — ESI; Time-stamping protocol and time-stamp token profiles, V1.1.1 (2016-03), with ETSI delivery URL. | Indirect | Ignored | Protocol/token-profile identity relevant to the client request and to token parsing (C/V). | Separate work; not reproduced. |
| [Annex — footnote (3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | ENISA "Agreed Cryptographic Mechanisms" publication URL. | None | Ignored | Citation for normative reference [9]; contextual. | — |
| [Annex — footnote (4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Commission Implementing Regulation (EU) 2024/482 (EUCC), OJ L, 2024/482, 7.2.2024. | None | Ignored | Citation for reference [10]; external act. | — |
| [Annex — footnote (5)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Commission Implementing Regulation (EU) 2024/3144, OJ L, 2024/3144, 19.12.2024. | None | Ignored | Citation for reference [11]; external act. | — |

### Annex, point 1 — adaptations to ETSI EN 319 421

Point 1 adapts EN 319 421, the policy and security requirements for trust service providers issuing time stamps. Under the confirmed scope these rows address the TSA/QTSP; Autogram is not a TSA, operates no time-stamping service and holds no TSU keys, so they carry no Autogram measure. Reference [5] is the one entry that names the protocol/token standard.

#### Point 1(1) — clause 2.1 Normative references

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(1) — reference [3]](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | ISO/IEC 15408:2022, parts 1 to 5, Information security evaluation criteria. | None | Ignored | TSA-side normative reference; external standard not inspected. | — |
| [1(1) — reference [4]](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | ETSI EN 319 401 V3.1.1 (2024-06), General Policy Requirements for Trust Service Providers. | None | Ignored | TSA-side normative reference; external standard. | Fixed edition. |
| [1(1) — reference [5]](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | ETSI EN 319 422 V1.1.1 (2016-03), time-stamping protocol and token profiles. | Indirect | Ignored | Protocol/token-profile criterion behind the client request and token parsing (C/V); external standard. | Overlaps Annex point 2. |
| [1(1) — reference [6]](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | None | Ignored | Normative reference removed; no task. | — |
| [1(1) — reference [9]](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | ENISA "Agreed Cryptographic Mechanisms" endorsed by the European Cybersecurity Certification Group. | None | Ignored | TSA-side algorithm guidance; external document. | — |
| [1(1) — reference [10]](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Commission Implementing Regulation (EU) 2024/482 (EUCC). | None | Ignored | TSA-side certification reference; external act. | — |
| [1(1) — reference [11]](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Commission Implementing Regulation (EU) 2024/3144 amending 2024/482. | None | Ignored | TSA-side; external act. | — |

#### Point 1(2) — clause 3.1 Terms

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(2) — definition of "certificate validity period"](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Time interval from notBefore to notAfter inclusive during which the CA warrants it will maintain the certificate status information. | None | Ignored | Definitional context for the TSA standard; no separate duty. | — |

#### Point 1(3) — clause 3.3 Abbreviations

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(3) — abbreviation "EUCC"](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | EUCC = European Common Criteria-based cybersecurity certification scheme. | None | Ignored | Definitional context. | — |

#### Point 1(4) — clause 6.2 Trust Service Practice Statement

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(4) — OVR-6.2-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The TSA shall include statements about the availability of its time-stamping service in its TSA disclosure statement. | None | Ignored | TSA/QTSP obligation; Autogram operates no TSA. | — |

#### Point 1(5) — clause 7.3 Personnel security

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(5) — OVR-7.3-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | TSA personnel and subcontractors in trusted roles shall have expert knowledge, experience and qualifications through training, credentials, actual experience or a combination. | None | Ignored | TSA obligation; no Autogram role. | — |
| [1(5) — OVR-7.3-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Compliance with OVR-7.3-02 shall include regular updates, at least every 12 months, on new threats and current security practices. | None | Ignored | TSA obligation. | Recurring (12 months). |

#### Point 1(6) — clause 7.6.2 TSU key generation

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(6) — TIS-7.6.2-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The generation of the TSU's key(s) shall be carried out within a secure cryptographic device certified as set out in (a), (b) or (c). | None | Ignored | TSA obligation; Autogram holds no TSU keys. | — |
| [1(6) — TIS-7.6.2-03(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Common Criteria (ISO/IEC 15408 or CC:2022 parts 1–5) certified to EAL 4 or higher. | None | Ignored | TSA certification route; external schemes. | — |
| [1(6) — TIS-7.6.2-03(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | EUCC [10][11] certified to EAL 4 or higher. | None | Ignored | TSA certification route. | — |
| [1(6) — TIS-7.6.2-03(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Until 31.12.2030, FIPS PUB 140-3 level 3. | None | Ignored | TSA certification route. | Sunset 31 December 2030. |
| [1(6) — TIS-7.6.2-03, unnumbered certification paragraph](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Certification shall be to a security target, protection profile, or module design and security documentation meeting the present document's requirements, based on risk analysis including physical and other non-technical measures. | None | Ignored | TSA obligation. | — |
| [1(6) — TIS-7.6.2-03, unnumbered EUCC configuration paragraph](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | If the secure cryptographic device benefits from an EUCC certification, it shall be configured and used in accordance with that certification. | None | Ignored | TSA obligation. | — |
| [1(6) — TIS-7.6.2-04](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | None | Ignored | Requirement removed; no task. | — |
| [1(6) — NOTE 3](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | None | Ignored | Note removed; no task. | — |
| [1(6) — TIS-7.6.2-05A](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The TSU key-generation algorithm, resulting signing key length and signature algorithms for signing time-stamps, and for signing TSU public-key certificates, shall comply with the ENISA-endorsed Agreed Cryptographic Mechanisms [9]. | None | Ignored | TSA obligation; external mechanisms document. | — |
| [1(6) — NOTE 4](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | None | Ignored | Note removed; no task. | — |
| [1(6) — TIS-7.6.2-06](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | A TSU's signing key shall be exported and imported into a different secure cryptographic device only where implemented securely and in accordance with the certification of those devices. | None | Ignored | TSA obligation. | — |

#### Point 1(7) — clause 7.6.3 TSU private key protection

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(7) — TIS-7.6.3-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The TSU private key shall be held and used within a secure cryptographic device certified as set out in (a), (b) or (c). | None | Ignored | TSA obligation; Autogram holds no TSU keys. | — |
| [1(7) — TIS-7.6.3-02(a)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Common Criteria as set out in ISO/IEC 15408 or in Common Criteria version CC:2002, parts 1–5, certified to EAL 4 or higher. | None | Ignored | TSA certification route. | The retrieved text prints "CC:2002" here, whereas point 1(6)(a) prints "CC:2022"; recorded as printed, not silently corrected. |
| [1(7) — TIS-7.6.3-02(b)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | EUCC [10][11] certified to EAL 4 or higher. | None | Ignored | TSA certification route. | — |
| [1(7) — TIS-7.6.3-02(c)](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Until 31.12.2030, FIPS PUB 140-3 level 3. | None | Ignored | TSA certification route. | Sunset 31 December 2030. |
| [1(7) — TIS-7.6.3-02, unnumbered certification paragraph](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Certification shall be to a security target, protection profile, or module design and security documentation meeting the present document's requirements, based on risk analysis including physical and other non-technical measures. | None | Ignored | TSA obligation. | — |
| [1(7) — TIS-7.6.3-02, unnumbered EUCC configuration paragraph](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | If the secure cryptographic device benefits from an EUCC certification, it shall be configured and used in accordance with that certification. | None | Ignored | TSA obligation. | — |
| [1(7) — TIS-7.6.3-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | None | Ignored | Requirement removed; no task. | — |
| [1(7) — NOTE 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | None | Ignored | Note removed; no task. | — |

#### Point 1(8) — clause 7.6.7 End of TSU key life cycle

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(8) — TIS-7.6.7-03A](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The expiration date for TSU private keys shall comply with the Agreed Cryptographic Mechanisms [9]. | None | Ignored | TSA obligation; external mechanisms document. | — |
| [1(8) — NOTE 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | None | Ignored | Note removed; no task. | — |

#### Point 1(9) — clause 7.10 Network security

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(9) — OVR-7.10-05](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The vulnerability scan requested by REQ-7.8-13 of ETSI EN 319 401 [1] shall be performed at least once per quarter. | None | Ignored | TSA obligation. | Recurring (quarterly). |
| [1(9) — OVR-7.10-06](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The penetration test requested by REQ-7.8-17X of ETSI EN 319 401 [1] shall be performed at least once per year. | None | Ignored | TSA obligation. | Recurring (annual). |
| [1(9) — OVR-7.10-07](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Firewalls shall be configured to prevent all protocols and accesses not required for the operation of the TSA. | None | Ignored | TSA obligation. | — |

#### Point 1(10) — clause 7.14 TSA termination and termination plans

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [1(10) — OVR-7.14-01A](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The TSP's termination plan shall comply with the requirements in the implementing acts adopted under Article 24(5) of Regulation (EU) No 910/2014. | None | Ignored | TSA/QTSP obligation. | — |

### Annex, point 2 — adaptations to ETSI EN 319 422

Point 2 adapts EN 319 422, the time-stamping protocol and token profiles. Autogram acts as a time-stamping *client* when it requests a token for a `BASELINE_T` signature and parses/classifies the tokens it validates, so these rows are interoperability/input criteria (**Indirect**) rather than Autogram duties; point 2(9) names the client expressly and is therefore **Conditional**. Fulfilment is not established for the token/profile criteria (Status **Unknown**); "void" notes remove standard text and carry no task (Status **Ignored**). None of the referenced external standards was inspected.

#### Point 2(1) — clause 2.1 Normative references

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(1) — reference [5]](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | Indirect | Ignored | Normative reference removed; no task. | — |
| [2(1) — reference [6]](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | Indirect | Ignored | Normative reference removed; no task. | — |
| [2(1) — reference [8]](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | European Cybersecurity Certification Group "Agreed Cryptographic Mechanisms". | Indirect | Ignored | External algorithm guidance behind token algorithms; not inspected. | Citation supplied by Annex footnote (3). |
| [2(1) — reference [9]](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | RFC 9110 HTTP Semantics. | Indirect | Ignored | Transport reference for the token protocol; external standard. | Used by point 2(9). |

#### Point 2(2) — clause 4.1.3 Hash algorithms to be used

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(2) — replacement clause](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Hash algorithms used to hash the information to be time-stamped, the expected duration of the time stamp and selected hash functions versus time shall comply with the Agreed Cryptographic Mechanisms [8]. | Indirect | Unknown | Token hash-algorithm profile consumed by Autogram (C/V); need interoperable token fixtures covering accepted/legacy algorithms. | Value depends on the external mechanisms document. |
| [2(2) — NOTE](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | Indirect | Ignored | Note removed; no task. | — |

#### Point 2(3) — clause 4.2.3 Algorithms to be supported

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(3) — replacement clause](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Time-stamp token signature algorithms to be supported shall comply with the Agreed Cryptographic Mechanisms [8]. | Indirect | Unknown | Signature algorithms in tokens Autogram parses/qualifies; need token fixtures and DSS algorithm-coverage evidence. | Declared as "supported" by the operator, not asserted by Autogram. |
| [2(3) — NOTE](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | Indirect | Ignored | Note removed; no task. | — |

#### Point 2(4) — clause 4.2.4 Key lengths to be supported

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(4) — replacement clause](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Signature-algorithm key lengths for the selected signature algorithm shall comply with the Agreed Cryptographic Mechanisms. | Indirect | Unknown | Key lengths inside consumed tokens; need fixtures and DSS validation evidence. | — |
| [2(4) — NOTE](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | Indirect | Ignored | Note removed; no task. | — |

#### Point 2(5) — clause 5.1.3 Algorithms to be supported

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(5) — replacement clause](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Hash algorithms for the time-stamp data to be supported, the expected duration of the time-stamp and selected hash functions versus time shall comply with the Agreed Cryptographic Mechanisms [8]. | Indirect | Unknown | Time-stamp data hashing in consumed tokens; need profile fixtures. | — |
| [2(5) — NOTE](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | Indirect | Ignored | Note removed; no task. | — |

#### Point 2(6) — clause 5.2.3 Algorithms to be used

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(6) — replacement clause](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Hash algorithms used to hash the information to be time-stamped and time-stamp token signature algorithms shall comply with the Agreed Cryptographic Mechanisms [8]. | Indirect | Unknown | Combined hash/signature criteria in consumed tokens; need fixtures and DSS evidence. | — |
| [2(6) — NOTE](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | Indirect | Ignored | Note removed; no task. | — |

#### Point 2(7) — clause 6.3 Key lengths requirements

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(7) — replacement clause](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The key length for the selected signature algorithm of the TSU certificate shall comply with the Agreed Cryptographic Mechanisms [8]. | Indirect | Unknown | TSU certificate key length affects consumed-token validation; need fixtures. | — |
| [2(7) — NOTE](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | Indirect | Ignored | Note removed; no task. | — |

#### Point 2(8) — clause 6.5 Algorithm requirements

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(8) — replacement clause](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The TSU public key and the TSU certificate signature shall use algorithms compliant with the Agreed Cryptographic Mechanisms [8]. | Indirect | Unknown | Consumed-token certificate algorithms; need fixtures and DSS evidence. | — |
| [2(8) — NOTE](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | Indirect | Ignored | Note removed; no task. | — |

#### Point 2(9) — clause 7 Profiles for the transport protocols to be supported

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(9) — replacement clause](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The time-stamping client and the time-stamping server shall support the time-stamping protocol via HTTPS [9] as defined in clause 3.4 of IETF RFC 3161 [1]. | Conditional | Unknown | Autogram is a time-stamping client only when `BASELINE_T`/TSA is configured (C). The shipped default and pre-defined URLs are `http://`, while the test TSA is `https://` (T); no policy or test proves HTTPS-only client transport. Need effective endpoint review and a transport-conformance fixture. | Expressly names the client, hence the only Conditional row. |

#### Point 2(10) — clause 8 Object identifiers of the cryptographic algorithms

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(10) — replacement clause](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The TSU public key and the TSU certificate signature shall use algorithms compliant with the Agreed Cryptographic Mechanisms [8]. | Indirect | Unknown | Algorithm OIDs inside consumed tokens; need fixtures. | — |
| [2(10) — NOTE](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | Void. | Indirect | Ignored | Note removed; no task. | — |

#### Point 2(11) — clause 9.1 Regulation compliance statement

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2(11) — qcStatements extension](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | A time-stamp token declared by the TSA to be a qualified electronic time stamp shall contain one instance of the qcStatements extension with the syntax of IETF RFC 3739 clause 3.2.6. | Indirect | Unknown | Autogram reads timestamp qualification and displays QTSA (V); it does not issue tokens, and no fixture proves the consumed token carries this extension. | Token-side criterion; determination of "declared qualified" rests with the TSA. |
| [2(11) — esi4-qtstStatement-1](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The qcStatements extension shall contain one instance of the statement 'esi4-qtstStatement-1' as defined in Annex B. | Indirect | Unknown | Consumed-token statement read indirectly via DSS `TimestampQualification`; no token fixture inspected (V/T). | Annex B is in the external standard, not this act. |
| [2(11) — criticality](https://eur-lex.europa.eu/eli/reg_impl/2025/1929/oj#anx_1) | The qcStatements extension shall not be marked as critical. | Indirect | Unknown | No Autogram-side check of the extension's criticality flag was found; DSS handles parsing. | Token-side criterion. |

## Coverage and limitations

- Enumerated independently from the official act, not copied from the existing compliance report: **7 recitals; 2 articles (Article 1's 1 unnumbered paragraph and Article 2's 1 unnumbered paragraph); 1 binding closing paragraph; 1 adoption/signature formula; 3 preamble units; the single annex — its opening paragraph, 5 footnotes, and both numbered adaptation sections (point 1 with adaptations (1)–(10) and point 2 with adaptations (1)–(11))**. These produce **81 assessment rows**: **10** preamble/recital rows, **4** article/concluding rows, **6** annex opening/footnote rows, **37** Annex point 1 rows and **24** Annex point 2 rows.
- Every identifiable unit is individually rowed: all seven recitals with `#rct_1`–`#rct_7`; both articles; the closing and adoption formulae; the annex opening paragraph and footnotes (1)–(5); point 1 adaptations (1)–(10) with each normative reference `[3]`,`[4]`,`[5]`,`[6]`,`[9]`,`[10]`,`[11]`, the term, the abbreviation, OVR-6.2-03, both OVR-7.3 items, every TIS-7.6.2 item including letters (a)–(c), both unnumbered certification paragraphs, all voids and notes, and every TIS-7.6.3 / TIS-7.6.7 item; point 2 adaptations (1)–(11) with each normative reference `[5]`,`[6]`,`[8]`,`[9]` and each replacement clause with its separate void NOTE, plus point 2(9)'s client/server requirement and point 2(11)'s three statements. Numbered adaptation labels and the "point 1"/"point 2" parents are structural headings and are not duplicated as duty rows.
- Reference coverage: **11 primary specification entries** are named by the act (EN 319 421 V1.3.1, EN 319 422 V1.1.1, ISO/IEC 15408:2022, EN 319 401 V3.1.1, ENISA Agreed Cryptographic Mechanisms, Regulations 2024/482 and 2024/3144, RFC 9110, RFC 3161, RFC 3739, EUCC/CC:2022), in addition to the amended base act 2024/1183. Each is represented with the row where it is invoked; the five annex footnotes are represented individually.
- Official English body text, the single Annex and footnotes 1–5 were obtained from the full Cellar XHTML response described in the source block; no act provision was deliberately omitted. No authentic PDF was downloaded and independently verified, and the annex anchor scheme in the retrieved source is `#anx_1` (not `#anx_1`), which this document records rather than normalises.
- **None of the full external standards was inspected.** Their cited titles/versions and the act's adaptations were inspected; the full EN 319 421, EN 319 422, EN 319 401, ISO/IEC 15408, the ENISA Agreed Cryptographic Mechanisms, RFC 9110/3161/3739 and the invoked Annex B remain separate works requiring a dedicated conformity assessment. Their unmodified clauses are not silently imported as text of this act.
- Repository grounding is limited to the timestamp client/consumption path (C/V/T/E). Inspected code establishes that Autogram configures TSA URLs, requests tokens for `BASELINE_T`, reads timestamp indications/qualification and displays them; it does **not** establish that requested tokens are qualified, that the token/transport profile matches the adapted EN 319 422, or that the publisher operates any trust service. The default TSA endpoints are `http://`, while point 2(9) requires HTTPS support; this is recorded as an unresolved observation, not a proved breach.
- Outstanding: a documented decision on whether/where Autogram asserts any qualified-timestamp reliance; an effective-endpoint/HTTPS-transport review; interoperable token-profile, algorithm and qcStatements fixtures; actor/data-processing records for the Recital (5) context; and a subsequent amendment-history check. No builds, test execution, implementation changes, commits or organisational compliance assertions accompany this reading register.
