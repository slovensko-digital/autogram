# Commission Implementing Regulation (EU) 2025/1567 — management of remote qualified signature and seal creation devices

**Register:** [Autogram legal-act register](README.md) · **Assessment date:** 8 October 2026 · **Repository baseline:** `5ca91d5c` · **Scope:** Autogram desktop application, locally hosted API and CLI.

## Executive summary

**What this act is.** Regulation 2025/1567 sets out, in a single Annex, the **reference standards and specifications** for the management of **remote qualified electronic signature creation devices (remote QSCDs)** and remote qualified electronic seal creation devices **as qualified trust services**, under eIDAS (Regulation (EU) No 910/2014) Article 29a(2) and Article 39a as amended by Regulation (EU) 2024/1183. The Annex takes **ETSI TS 119 431-1 V1.3.1 (2024-12)** as the base standard, targeting the **EU Server Signing Application Service v2 Policy (EUSPv2)**, and adds seven groups of adaptations (normative references, publication, personnel, service termination, network security, cryptographic controls and practice-statement reference to the employed QSCD).

**Who it addresses.** Its addressees are **qualified trust service providers (QTSPs)** that operate a **Server Signing Application Service** and manage remote QSCDs on behalf of signatories and seal creators, together with the standards bodies and the Commission. There is no obligation on an independent desktop signing/validation application.

**Relevance to Autogram: None (context), with two indirect touches.** Autogram is a **local desktop application** that signs through a token connected to the user's machine: the signature value is produced by `token.sign(...)` on a PKCS#11/PKCS#12/PKCS#11-like token (`SigningKey`, `SigningJob`), and the drivers enumerate **local** smart-card libraries, a local keystore and a fake driver (`DefaultDriverDetector`). Its HTTP API defaults to **localhost:37200** and is a local signing endpoint, not a key-custody service. The repository contains **no** reference to SSASP, EUSPv2 or ETSI TS 119 431-1 and holds no remote-QSCD management component. Autogram is therefore neither a QTSP nor an SSASP and does not manage remote QSCDs for third parties. The indirect touches are recital (3)'s "sole control" context (relevant conceptually to how any signing application uses a QSCD, but addressed to the remote-management service) and recital (6), which points to data-protection rules assessed in the [GDPR document](eu-reg-2016-679.md). Autogram's only QSCD-related code surfaces **validation-result labels** (`UNKNOWN_QC_QSCD`, `NOT_ADES_QC_QSCD`), i.e. whether a received signature was made with a QSCD, not the management of one.

**Dates.** Adopted **29 July 2025**; published in OJ L, 2025/1567, **30 July 2025**. Entry into force on the twentieth day following publication, i.e. **19 August 2025** (Article 2, paragraph 1). It **applies from 19 August 2027** (Article 2, paragraph 2), which is 24 months after entry into force, as recital (4) explains. No vendor implementation deadline follows for a local application.

## Source and version

| Item | Value |
| --- | --- |
| Act | Commission Implementing Regulation (EU) 2025/1567 of 29 July 2025 laying down rules for the application of Regulation (EU) No 910/2014 as regards the management of remote qualified electronic signature creation devices and of remote qualified electronic seal creation devices as qualified trust services |
| Official text | [ELI/OJ page](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj) · CELEX **32025R1567** · ELI `http://data.europa.eu/eli/reg_impl/2025/1567/oj` · OJ L, 2025/1567, 30.7.2025 |
| Text obtained | Official English XHTML from the Publications Office/Cellar (`https://publications.europa.eu/resource/celex/32025R1567?language=eng`, `Accept: application/xhtml+xml`), retrieved 8 October 2026 |
| Version assessed | Original published Regulation (not a consolidation). Amends/repeals nothing; implements Regulation (EU) No 910/2014 Articles 29a(2) and 39a. No amendment, repeal or corrigendum was identified in the obtained text; a subsequent amendment-history check remains outstanding. |
| Enumerated units | Preamble (institutional opening; two having-regard paragraphs) · recitals (1)–(8) · Article 1 · Article 2 (three paragraphs) · closing adoption/signature formula · one unnumbered Annex with an opening paragraph and seven numbered adaptation groups |
| Adopted / in force / applies | Adopted 29 July 2025; in force 19 August 2025 (20th day after publication); applies from **19 August 2027** |
| Anchor scheme | Verified against the retrieved XHTML: recital anchors `#rct_1`–`#rct_8`, article anchors `#art_1`, `#art_2`, and the single **unnumbered** Annex carries anchor `#anx_1` (not a roman-numeral anchor) |
| Baseline code | No Autogram code implements this act; scope boundary follows [README](README.md) |

**Retrieval qualification.** The `eur-lex.europa.eu` ELI/OJ pages returned an AWS WAF JavaScript challenge (`HTTP 202`, empty body) to text clients, so the anchor scheme was verified against the official **Publications Office/Cellar** XHTML, which is produced from the same OJ source and uses the ids `rct_1`–`rct_8`, `art_1`, `art_2`, `anx_1`. The complete English text — preamble, all eight recitals, both articles, the five annex footnotes/marginal references as published, and the whole Annex — was read from that XHTML. Tables below are engineering paraphrases, not replacement legal text; the published act remains authoritative.

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or out of the confirmed scope; never "knowingly disregarded"). Every provision row links to the official text at that unit — recital anchors `#rct_1`–`#rct_8`, article anchors `#art_1` and `#art_2`, and the single Annex anchor `#anx_1` on the [official OJ page](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj). Preamble rows link the base act page because the preamble's unit ids (`pbl_1`, `cit_1`, `cit_2`) are stable only in the Cellar source and are not part of the requested `#art`/`#anx`/`#rct` scheme.

`None / Ignored` below means the provision addresses a QTSP/SSASP or an institutional actor and not a local signing application. It does **not** assert that the underlying technical subject is unimportant; it records that no Autogram activity, component or duty is established for that unit. Because the addressee is confirmed to be someone other than the publisher, no row is marked **Not done**.

## Evidence key

All evidence below was inspected on 8 October 2026. Source inspection is not a released-binary audit; no builds or tests were run.

- **A — local signing path:** [SigningJob.java](../../src/main/java/digital/slovensko/autogram/core/SigningJob.java), `signWithKeyAndRespond`, lines 86–107, calls `key.sign(dataToSign, ...)` and then `signDocument(...)` in-process. [SigningKey.java](../../src/main/java/digital/slovensko/autogram/core/SigningKey.java), `sign`, lines 24–28, delegates to `token.sign(dataToSign, algo, privateKey)` on a DSS `AbstractKeyStoreTokenConnection`. [Autogram.java](../../src/main/java/digital/slovensko/autogram/core/Autogram.java), `fetchKeysAndThen`, lines 230–243 (and `getCertificates`, lines 356–362), obtains keys from a driver-created `token` via `token.getKeys()`, i.e. a device held by the local user. This is client-side signing, not a remote QSCD management service.
- **T — local token drivers:** [DefaultDriverDetector.java](../../src/main/java/digital/slovensko/autogram/core/DefaultDriverDetector.java), lines 36–48 (Linux) and 51–73 (Windows/macOS), enumerates **local** PKCS#11 libraries for national eID cards plus `PKCS12KeystoreTokenDriver` and `FakeTokenDriver`. [PKCS11TokenDriver.java](../../src/main/java/digital/slovensko/autogram/drivers/PKCS11TokenDriver.java), lines 11–18, creates the token; [NativePkcs11SignatureToken.java](../../src/main/java/digital/slovensko/autogram/drivers/NativePkcs11SignatureToken.java), lines 58–66, performs context-specific login for `CKA_ALWAYS_AUTHENTICATE` tokens. This demonstrates that Autogram **consumes** a signer-held QSCD/smart card; it does not host or manage remote QSCDs.
- **S — local API server:** [configuration.properties](../../src/main/resources/digital/slovensko/autogram/core/configuration.properties), lines 4–7: `server.defaultAddress=localhost`, `server.defaultPort=37200`, HTTP. [LaunchParameters.java](../../src/main/java/digital/slovensko/autogram/core/LaunchParameters.java), lines 43–45, reads those defaults; [GUIApp.java](../../src/main/java/digital/slovensko/autogram/ui/gui/GUIApp.java), line 49, constructs `AutogramServer`; [AutogramServer.java](../../src/main/java/digital/slovensko/autogram/server/AutogramServer.java), `buildServer`, lines 74–81, binds `HttpServer`/`HttpsServer` to that host and port and registers `/sign`, `/batch`, `/certificates`. The API is a **local** signing endpoint, not a server-side signing service managing keys for remote signatories.
- **Q — QSCD status display:** [SignatureBadgeFactory.java](../../src/main/java/digital/slovensko/autogram/ui/gui/SignatureBadgeFactory.java), lines 81, 87 and 194, maps DSS qualification results `UNKNOWN_QC_QSCD`, `NOT_ADES_QC_QSCD` and `INDETERMINATE_UNKNOWN_QC_QSCD` to UI labels. These state whether a **received** signature was created by a QSCD; they are validation output, not remote-QSCD management.
- **Absence:** `grep -riIE "SSASP|EUSP|119 ?431|remote qualified QSCD|remote qscd" src --include=*.java` returns no match, so the repository contains no implementation of, or reference to, the ETSI TS 119 431-1 / EU Server Signing Application Service v2 route.

## Provision-by-provision assessment

### Preamble

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Preamble — institutional opening](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj) | The Commission is the adopting institution. | None | Ignored | Official heading; documentary metadata, not a software duty. | Adopted 29 July 2025. |
| [Preamble — having-regard paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj) | TFEU provides the treaty framework for the measure. | Indirect | Ignored | Official preamble; legal basis, not an implementation task. | Original version. |
| [Preamble — having-regard paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj) | Regulation (EU) No 910/2014, in particular Article 29a(2) and Article 39a, confers the implementing power for remote QSCD/remote seal-creation-device management as qualified trust services. | Indirect | Ignored | Official preamble; the same two articles are reflected in the act title and Article 1. Distinguishes the QTSP remote-management route from a local signing application. | Cites OJ L 257, 28.8.2014, p. 73 and eIDAS ELI. |

### Recitals

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Recital (1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#rct_1) | Qualified trust services for managing remote signature/seal creation devices facilitate the paper-to-digital transition and guarantee that the conditions for qualified electronic signatures and seals are met. | None | Ignored | Describes the trust service; no Autogram service provides remote QSCD management (A/T/S). | Interpretative purpose, no independent deadline. |
| [Recital (2)](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#rct_2) | QTSPs providing those management services should comply with the standards set out in this Regulation, to enhance legal certainty and trustworthiness. | None | Ignored | Addressee is a QTSP; the organisation is confirmed not to be a QTSP and operates no such service. Would become Conditional only on such a deployment. | Confirming fact in the [README](README.md) shared facts. |
| [Recital (3)](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#rct_3) | The standards should reflect established practice and be adapted with controls ensuring security/trustworthiness and signatories' **sole control** over their signature creation data (and seal creators' control over seal creation data). | Indirect | Ignored | Interpretative context for the Annex controls. Autogram enforces local token context-specific authentication (T) but does not manage remote QSCDs for others; the "sole control" duty here sits on the management service. | Do not convert this recital into a duty on a local application. |
| [Recital (4)](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#rct_4) | To allow an adequate audit timeframe, the Regulation should apply from **24 months after entry into force**. | Indirect | Ignored | Commencement context; matches Article 2, paragraph 2. | Entry into force 19 Aug 2025 → applies 19 Aug 2027. |
| [Recital (5)](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#rct_5) | The Commission should review and, if necessary, update the Regulation in line with global developments, new technologies, standards or technical specifications (recital 75 of Regulation (EU) 2024/1183). | None | Ignored | Commission task, not a software task. | Institutional. |
| [Recital (6)](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#rct_6) | Regulation (EU) 2016/679 and, where relevant, Directive 2002/58/EC apply to all personal-data processing under this Regulation. | Indirect | Unknown | Points to data-protection law; no evidenced processing by the publisher as a remote-QSCD service. Need an actor-specific data-flow and legal-basis inventory if the act's services were ever operated; see [GDPR document](eu-reg-2016-679.md). | Context only; substantive duties arise under the referenced acts. |
| [Recital (7)](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#rct_7) | The EDPS was consulted under Article 42(1) of Regulation (EU) 2018/1725 and delivered its opinion on 6 June 2025. | None | Ignored | Institutional procedure; not an application requirement. | Opinion 6 June 2025. |
| [Recital (8)](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#rct_8) | The measures accord with the opinion of the committee established by Article 48 of Regulation (EU) No 910/2014. | None | Ignored | Institutional procedure; not an application requirement. | Original version. |

### Articles and concluding formula

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 1 — unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#art_1) | The reference standards and specifications for managing remote QSCDs and remote seal creation devices as qualified trust services, referred to in eIDAS Articles 29a(2) and 39a, are set out in the Annex. | None | Ignored | Addressee is a QTSP/SSASP; the Annex standards are implemented nowhere in Autogram (A/T/S, absence check). Would become relevant only if the organisation operated such a service. | Effective; does not itself create an application deadline. |
| [Article 2 — paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#art_2) | Entry into force on the twentieth day following publication in the OJ. | Indirect | Ignored | Commencement provision. | Publication 30 July 2025 → in force **19 August 2025**. |
| [Article 2 — paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#art_2) | The Regulation applies from **19 August 2027**. | Indirect | Ignored | Application date read directly from the official act; equals 24 months after entry into force (recital (4)). | Applies **19 August 2027**; no separate vendor deadline. |
| [Article 2 — paragraph 3 (binding formula)](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#art_2) | Binding in its entirety and directly applicable in all Member States. | Indirect | Ignored | Legal character; no transposition and no product measure. | Effective. |
| [Closing adoption/signature formula](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj) | Brussels adoption and Commission President attribution. | None | Ignored | Official text; documentary metadata, not a separate duty. | 29 July 2025; Ursula von der Leyen. |

### Annex — list of reference standards and specifications

#### Opening paragraph and base standard

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex — unnumbered opening paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#anx_1) | ETSI TS 119 431-1 V1.3.1 (2024-12) applies for assessing conformance with the EU Server Signing Application Service v2 Policy (EUSPv2) in compliance with Annex A of that standard, subject to the following adaptations. The standard and the policy are separate external works, not reproduced by the act. | None | Ignored | Addressee is an SSASP; no Autogram code references SSASP/EUSPv2/TS 119 431-1 (absence check). Would become Conditional only on operating such a service. | Separate standards; fixed edition V1.3.1 (2024-12). |

#### Point (1) — 2.1 Normative references

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex (1) — reference [1]](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#anx_1) | ETSI EN 319 401 **V3.1.1 (2024-06)**, "Electronic Signatures and Trust Infrastructures (ESI); General Policy Requirements for Trust Service Providers". | None | Ignored | General TSP policy requirements, addressed to service providers; separate external standard, not inspected. | Fixed edition; part of the referenced standard set. |
| [Annex (1) — reference [7] and footnote (1)](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#anx_1) | European Cybersecurity Certification Group, Sub-group on Cryptography: "Agreed Cryptographic Mechanisms" published by **ENISA**; footnote (1) links the ENISA publication page. | None | Ignored | External cryptographic guidance for the SSASP; separate work, not inspected. Aligns with the cryptographic-controls adaptation under (6). | Footnote URL: `https://certification.enisa.europa.eu/publications/eucc-guidelines-cryptography_en`. |

#### Point (2) — 6.1 Publication and repository responsibilities

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex (2) — OVR-6.1-04](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#anx_1) | The information identified in OVR-6.1-01 shall be publicly and internationally available. | None | Ignored | SSASP repository/publication duty; the referenced OVR-6.1-01 text lives in the external standard. No Autogram component operates a trust-service repository. | External cross-reference; not restated by the act. |

#### Point (3) — 6.4.4 Personnel controls

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex (3) — OVR-6.4.4-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#anx_1) | The SSASP shall employ personnel in trusted roles, and, if applicable, subcontractors in trusted roles, with the necessary expert knowledge, experience and qualifications. | None | Ignored | Organisational staffing duty on an SSASP; not a software requirement. The publisher is not an SSASP. | Addressed to the service provider. |
| [Annex (3) — OVR-6.4.4-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#anx_1) | Compliance with OVR-6.4.4-02 shall include regular (at least every 12 months) updates on new threats and current security practices. | None | Ignored | Organisational training duty on an SSASP; not implemented by any local application. | At least every 12 months. |

#### Point (4) — 6.4.9 SSASP service termination

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex (4) — OVR-6.4.9-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#anx_1) | The SSASP's termination plan shall comply with the implementing acts adopted under Article 24(5) of Regulation (EU) No 910/2014 [i.1]. | None | Ignored | Service-termination duty on an SSASP; references a separate eIDAS implementing act. No Autogram service termination plan is required for a local app. | Cross-references eIDAS Article 24(5). |

#### Point (5) — 6.5.5 Network security controls

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex (5) — OVR-6.5.5-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#anx_1) | The vulnerability scan requested by REQ-7.8-13 of ETSI EN 319 401 [1] shall be performed at least once per quarter. | None | Ignored | Infrastructure duty on an SSASP; the referenced REQ-7.8-13 lives in the external standard. Autogram's API is a local endpoint, not a managed network service (S). | At least once per quarter. |
| [Annex (5) — OVR-6.5.5-03](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#anx_1) | Firewalls shall be configured to prevent all protocols and accesses not required for the operation of the TSP. | None | Ignored | Infrastructure/network configuration duty on a TSP operating the service; not a property of a desktop signing application. | Runtime environment, not application code. |

#### Point (6) — 6.8.5 Cryptographic controls

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex (6) — OVR-6.8.5-01](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#anx_1) | Appropriate security controls shall be in place for the management of any cryptographic techniques of the SSASP throughout their lifecycle. | None | Ignored | Crypto-lifecycle duty on an SSASP; Autogram's signing path (A/T) is not a service-side cryptographic-key-management function. | Applies to the server service. |
| [Annex (6) — OVR-6.8.5-02](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#anx_1) | The SSASP shall select and use cryptographic techniques compliant with the "Agreed Cryptographic Mechanisms" endorsed by the European Cybersecurity Certification Group and published by ENISA [7]. | None | Ignored | External-cryptography conformity duty on an SSASP. Autogram's DSS-based algorithms are a client-side choice, not compliance with this SSASP obligation. | Reuses reference [7] from point (1). |

#### Point (7) — Annex A, section A.3 General requirements

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex (7) — OVR-A.3-02 [EUSPv2]](https://eur-lex.europa.eu/eli/reg_impl/2025/1567/oj#anx_1) | The TSP's practice statement shall include the reference to the certification of the employed QSCD in accordance with Regulation (EU) No 910/2014 [i.1], Annex II. | None | Ignored | Practice-statement/documentation duty on a TSP operating an SSASP, referencing eIDAS Annex II QSCD certification. Autogram publishes no trust-service practice statement and employs, rather than manages, QSCDs (Q). | Cross-references eIDAS Annex II; external policy requirement. |

## Coverage and limitations

- **Text obtained.** Complete English XHTML of Regulation (EU) 2025/1567 from the Publications Office/Cellar, including the institutional preamble, **all 8 recitals**, **Article 1**, **Article 2** (three paragraphs), the closing adoption formula and the **entire single Annex** (opening paragraph, seven numbered adaptation groups and their OVR/REQ entries, plus the annex footnote). The act is the original published version.
- **Enumeration check.** Article and recital headings were extracted as standalone units from the XHTML; the Annex's seven numbered points were each read and their individually lettered/identified requirements split into rows. Counts: **3 preamble units; 8 recitals; 5 article/concluding rows (Article 1's 1 unnumbered paragraph, Article 2's 3 paragraphs, 1 closing formula); 12 Annex rows** — an opening paragraph plus 11 individually identified requirements across points (1)–(7) (`[1]`, `[7]`, OVR-6.1-04, OVR-6.4.4-02, OVR-6.4.4-03, OVR-6.4.9-02, OVR-6.5.5-02, OVR-6.5.5-03, OVR-6.8.5-01, OVR-6.8.5-02, OVR-A.3-02). **Total: 28 assessment rows.** The seven numbered adaptation headings are structural headings, not duplicate duties; the underlying OVR/REQ identifiers carry the rows. Where the act invokes the external standard's own requirement numbers (OVR-6.1-01, REQ-7.8-13), those clauses are identified but not reproduced.
- **Anchor verification.** The retrieved Cellar XHTML uses `#rct_1`–`#rct_8`, `#art_1`, `#art_2` and, for the single unnumbered Annex, `#anx_1`. This differs from acts with several annexes (which use roman-numeral anchors such as `#anx_1`); the numeric anchor is recorded here and stated in the source block rather than guessed. The `eur-lex.europa.eu` ELI/OJ pages were WAF-blocked to text clients, so the anchors were not re-confirmed against the rendered page; this is an access limitation, not a content gap.
- **Referenced standards are separate works.** The full **ETSI TS 119 431-1 V1.3.1**, **ETSI EN 319 401 V3.1.1**, the **ENISA Agreed Cryptographic Mechanisms** publication, the invoked eIDAS Article 24(5) implementing acts and the **EUSPv2** policy were **not inspected**. Their cited titles/editions and the act's adaptations were read; their unmodified clauses are not silently imported as text of this act and require a dedicated conformity assessment.
- **Why relevance is None.** Established facts: the organisation is not a QTSP and operates no remote QSCD or seal-creation-device management service; Autogram signs through a local token and its API defaults to localhost. No repository source references this act's subject matter. A row would shift to **Conditional** only if the organisation began operating a qualified remote-QSCD/SSASP service; nothing in the inspected code anticipates that.
- **Outstanding evidence.** A complete subsequent amendment/corrigendum history check; confirmation of the rendered ELI anchor scheme for the lone Annex; and, only in the conditional remote-service scenario, an actor/data-protection inventory (recital (6)), SSASP organisational records, cryptographic-control and EUSPv2 practice-statement evidence. Inspected code proves the negative architectural fact and the QSCD-validation-label behaviour; it does not establish organisational compliance, ETSI conformity or any service obligation. No builds, test execution, implementation changes, commits or organisational compliance assertions accompany this reading register.
