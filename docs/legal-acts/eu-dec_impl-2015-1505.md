# Commission Implementing Decision (EU) 2015/1505 — trusted lists

**Register:** [Autogram legal-act register](README.md) · **Assessment date:** 8 October 2026 · **Repository baseline:** `5ca91d5c` · **Scope:** Autogram desktop application, locally hosted API and CLI. **Amended by:** [Decision (EU) 2025/2164](eu-dec_impl-2025-2164.md) (TLv6 / ETSI TS 119 612 update).

## Executive summary

**What this act is.** Decision 2015/1505 lays down the common template and technical specifications for the **Member States' trusted lists** and the template for notifying them to the Commission. It is adopted under eIDAS Article 22(5) and is the operational foundation of the trusted lists that every validator — including Autogram — consumes.

**Who it addresses.** The duties fall on **Member States** (establish, publish, maintain and sign the lists) and on the **Commission** (publish the central list of pointers). It does **not** impose a direct obligation on an independent signing/validation application. Its relevance to Autogram is **Indirect**: the app must correctly ingest and interpret the lists, including their historical status and qualification qualifiers.

**What is evidenced.** Autogram loads the EU LOTL and selected national lists through DSS, filters by selected country, and now derives "lists complete" from the LOTL plus every selected list's validated status (`SignatureValidator`, `SignatureValidatorTrustedListTest`). A live test on 3 October 2026 validated the EU LOTL and the current **Slovak TLv6**. That proves one list's processing, not correct interpretation of every qualifier, historical status or territory.

**Key technical point for the app.** The general interpretation rules in Annex I determine whether a certificate is to be treated as qualified: the `CA/QC` Service type identifier, the `Qualifications Extension` qualifiers (`QCForESig`, `QCForESeal`, `QCWithQSCD`, `QCNoQSCD`, `QCQSCDManagedOnBehalf`, `NotQualified`, etc.), and the rule that absent the required statement/OID/extension the certificate is **not** qualified. Correctly handling these qualifiers is what makes QES-versus-AdES-QC display accurate — the same result duties as [eIDAS Articles 32/32a](eu-reg-2014-910.md). Whether DSS 6.5 in Autogram interprets all of them correctly is **Unknown** without fixtures.

**Dates.** Adopted and published **9 September 2015**; in force **29 September 2015**; the trusted-list status migration tied to the application of eIDAS is **1 July 2016**. The later [Decision 2025/2164](eu-dec_impl-2025-2164.md) updates the cited ETSI TS 119 612 profile (TLv6); its list-template change applies to publishers from **29 April 2026**. None of these is a vendor release deadline.

---

## Source and version

| Item | Value |
| --- | --- |
| Act | Commission Implementing Decision (EU) 2015/1505 of 8 September 2015 laying down technical specifications and formats relating to trusted lists pursuant to Article 22(5) of Regulation (EU) No 910/2014 |
| Official text | [ELI/OJ](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj) · CELEX **32015D1505** · OJ L 235, 9.9.2015, p. 26 |
| Text obtained | Official English XHTML from the EU Publications Office/Cellar (`32015D1505?language=eng`, `Accept: application/xhtml+xml`), 8 October 2026 |
| Version assessed | Original published Decision (not a consolidation). Subsequent amendment by [Decision (EU) 2025/2164](eu-dec_impl-2025-2164.md) is assessed in its own document and cross-linked, not merged as anonymous edits. |
| Enumerated units | 6 recitals · Articles 1–5 (with numbered paragraphs) · Annex I (Chapters I–IV) · Annex II (template points 1–6 and sub-points) |
| In force | Adopted 8 Sep 2015; published 9 Sep 2015; in force 29 Sep 2015 (20th day). Trusted-list migration date 1 July 2016. |
| Baseline code | [`SignatureValidator`](../../src/main/java/digital/slovensko/autogram/core/SignatureValidator.java) (LOTL load, selected-country filter, status), `SignatureValidatorTrustedListTest`, `TrustedListLiveSmokeTest` |

**Legend and provision links.** **Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or out of the confirmed scope; never "knowingly disregarded"). Every provision row links to the official text at that unit — act anchors `#art_1`–`#art_5`, `#anx_I`, `#anx_II` and recitals `#rct_1`–`#rct_6`.

---

## Part A — Recitals

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [(1)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#rct_1) | Trusted lists are essential to trust and show provider status at supervision time. | Indirect | Ignored | Context; authoritative for why the app must read lists correctly. | 2015. |
| [(2)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#rct_2) | Decision 2009/767/EC established trusted lists; continuity. | Indirect | Ignored | Historical context. | 2009 framework. |
| [(3)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#rct_3) | eIDAS Article 22 obliges Member States to publish and notify. | None | Ignored | Member-State duty. | — |
| [(4)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#rct_4) | Provider/service is qualified when qualified status is associated in the list; Member States **may** add non-qualified services if clearly marked. | Indirect | Ignored | Context for Annex I qualifiers the app must distinguish. | 2015. |
| [(5)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#rct_5) | Member States may add nationally defined trust services, clearly marked not qualified. | Indirect | Ignored | Context for `NotQualified` handling. | Aligns eIDAS recital 25. |
| [(6)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#rct_6) | Measures accord with the Article 48 committee. | None | Ignored | Procedure. | 2015. |

## Part B — Articles

### Article 1 — Establish, publish, maintain trusted lists

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 1](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#art_1) | Member States shall establish, publish and maintain trusted lists with qualified-provider/service information, complying with Annex I. | None | Ignored | Member-State publication duty; the app is a consumer. | — |

### Article 2 — Non-qualified services on the list

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 2](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#art_2) | Member States may include non-qualified providers/services, clearly indicating they are not qualified. | Indirect | Unknown | App must not treat clearly-marked non-qualified entries as qualified; no fixture. | — |

### Article 3 — Signing/sealing the list

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 3(1)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#art_3) | Member States shall sign/seal the machine-processable form per Annex I. | Indirect | Unknown | App must validate the list signature via the announced certificates; LOTL/SK live passed, others unverified. | — |
| [Art. 3(2)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#art_3) | Human-readable form must contain the same data and be signed/sealed. | None | Ignored | Member-State duty. | — |

### Article 4 — Notification to the Commission

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 4(1)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#art_4) | Notify Commission using the Annex II template. | None | Ignored | Member-State duty. | — |
| [Art. 4(2)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#art_4) | Provide ≥2 scheme-operator public-key certificates with validity shifted ≥3 months. | Indirect | Unknown | App relies on the announced signing certificates; rotation handling untested. | — |
| [Art. 4(3)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#art_4) | Commission publishes the information on an authenticated web server in signed/sealed form. | None | Ignored | Commission duty; this is the EU LOTL. | — |
| [Art. 4(4)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#art_4) | Commission may also publish a human-readable form. | None | Ignored | Commission duty. | — |

### Article 5 — Entry into force

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Art. 5, para. 1](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#art_5) | Enters into force on the 20th day after publication. | Indirect | Ignored | Legal date. | In force **29 Sep 2015**. |
| [Art. 5, binding formula](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#art_5) | Binding in entirety and directly applicable. | Indirect | Ignored | Legal character. | — |

## Part C — Annex I: technical specifications for the common template

### Chapter I — General requirements

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Ch. I, unnumbered para. 1](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Lists shall include current **and historical** status information. | **Indirect** | Unknown | Historical-status processing is required for validation at signing time; no historical fixture in Autogram. | — |
| [Ch. I, unnumbered para. 2](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | "Approved/accredited/supervised" cover national schemes; extra information on their nature. | Indirect | Ignored | Member-State content. | — |
| [Ch. I, unnumbered para. 3](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Information supports validation of qualified tokens (QES/QESeal, AdES-QC, timestamps, delivery evidence). | Indirect | Unknown | The app's purpose; correctness of interpretation unproven. | — |

### Chapter II — Detailed specifications for the common template

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Ch. II opening](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Specifications rely on **ETSI TS 119 612 v2.1.1**; where no specific requirement, clauses 5 and 6 apply in full; specific requirements prevail. | Indirect | Unknown | The cited base version is superseded by [2025/2164](eu-dec_impl-2025-2164.md) (TLv6); app uses DSS TSL parser; exact clause coverage not evidenced. | See amendment. |
| [Scheme name (cl. 5.3.6)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Field present; fixed scheme name text. | None | Ignored | Member-State field. | — |
| [Scheme information URI (cl. 5.3.7)(a)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Common introductory text and continuity text. | None | Ignored | Member-State field. | — |
| [Scheme information URI (cl. 5.3.7)(b)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Specific supervision/approval-scheme information, incl. sub-points (1)–(5) and the "at least for each scheme" items (1)–(5). | None | Ignored | Member-State field. | Enumerated in source. |
| [Scheme type/community/rules (cl. 5.3.9)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Field present; UK English URIs; at least two URIs: (1) common `EUcommon` URI and (2) Member-State `CC` URI; MAY add hierarchical URIs. | Indirect | Unknown | Descriptive text defines TL interpretation; app must apply it. | Contains the key qualified-status interpretation rules. |
| [Scheme type/community/rules — interpretation of `CA/QC`](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | A `CA/QC` entry means end-entity certificates under that CA are qualified **provided** they contain the `id-etsi-qcs-QcCompliance` statement or the QCP/QCP+ policy OID **and** a valid service status. | **Direct** | Unknown | Directly governs QES/AdES-QC classification; DSS handles this, but no fixture proves correct outcome in Autogram. | Core rule. |
| [Scheme type/community/rules — `Qualifications Extension`](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Where present, certificates are further qualified per filters (`QCStatement`, `QCForESig`, `QCForESeal`, `QCForWSA`, `NotQualified`, `QCWithSSCD`, `QCNoSSCD`, `QCWithQSCD`, `QCNoQSCD`, `QCQSCDManagedOnBehalf`, `QCForLegalPerson`). | **Direct** | Unknown | Determines QES vs AdES-QC and the QSCD indication required by eIDAS Art. 32(1)(f)/32a; no fixture. | Qualifiers listed individually. |
| [Scheme type/community/rules — default negative rule](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | If none of the statement/OID is present, no qualifying extension, or a `NotQualified` qualifier applies, the certificate is **not** qualified. | **Direct** | Unknown | Conservative classification is legally important; untested. | Must not over-claim qualified. |
| [Scheme type/community/rules — trust anchors](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | `Service digital identifiers` are trust anchors; multiple certificates for a key convey identical anchor information. | Indirect | Unknown | Trust-anchor handling in DSS; untested. | — |
| [Scheme type/community/rules — other `Sti` entries](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | General rule: listed service has the status per `Service current status` from the stated date. | Indirect | Unknown | General interpretation; untested. | — |
| [TSL policy/legal notice (cl. 5.3.11)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Field present; mandatory common framework text plus optional national text. | None | Ignored | Member-State field. | — |
| [Service current status (cl. 5.5.4)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Field present; migration of status values on **1 July 2016** per ETSI TS 119 612 Annex J. | Indirect | Unknown | Historical status interpretation matters for old signatures; no fixture. | 1 Jul 2016. |

### Chapter III — Continuity of trusted lists

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Certificates notified (cl. 5.7.1)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Notified certificates: final validity dates differ by ≥3 months; created on new key pairs (no re-certification). | None | Ignored | Member-State duty. | — |
| [Expiry of one key — dash 1](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Re-issue a new list signed with a non-expired notified key, without delay. | None | Ignored | Member-State duty. | — |
| [Expiry of one key — dash 2](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Generate new key pairs when required. | None | Ignored | Member-State duty. | — |
| [Expiry of one key — dash 3](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Promptly notify the Commission of the new certificate list. | None | Ignored | Member-State duty. | — |
| [Compromise/decommission of one key — dashes 1–3](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Re-issue with a non-compromised key; generate new keys when required; notify Commission. | Indirect | Unknown | The app must handle a rotated/revoked list-signing certificate; untested. | — |
| [Compromise/decommission of all keys — dashes 1–3](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Generate new keys, re-issue and notify. | None | Ignored | Member-State duty. | — |

### Chapter IV — Human-readable form of the trusted list

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [PDF/A requirement](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | If published, human-readable form is PDF/A (ISO 32000 / ISO 19005). | None | Ignored | Member-State duty. | — |
| [Content structure](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Reflects the TS 119 612 logical model; every field shows title, value, meaning and multiple language versions. | None | Ignored | Member-State duty. | — |
| [Minimum certificate fields](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | If present in `Service digital identity`, display at least: version; serial number; signature algorithm; issuer DN; validity; subject DN; public key; authority/subject key identifier; key usage; extended key usage; certificate policies; policy mappings; subject alternative name; subject directory attributes; basic constraints; policy constraints; CRL distribution points; authority/subject information access; qualified certificate statements; hash algorithm; certificate hash. | None | Ignored | Member-State duty; enumerated in source. | User-visible list. |
| [Printable](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Human-readable form easily printable. | None | Ignored | Member-State duty. | — |
| [Signed/sealed](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_I) | Signed/sealed by the scheme operator per Articles 1 and 3. | None | Ignored | Member-State duty. | — |

## Part D — Annex II: template for Member States' notifications

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Point 1](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_II) | Member State, ISO 3166-1 alpha-2, with `UK` for United Kingdom and `EL` for Greece. | None | Ignored | Member-State notification. | — |
| [Point 1(a)/(b)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_II) | Country-code exceptions (`UK`, `EL`). | None | Ignored | Member-State notification. | — |
| [Point 2](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_II) | Responsible body/bodies. | None | Ignored | Member-State notification. | — |
| [Point 2(a)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_II) | Scheme operator name identical (case-sensitive) to the list value. | None | Ignored | Member-State notification. | — |
| [Point 2(b)](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_II) | Optional contact details for the Commission only. | None | Ignored | Member-State notification. | — |
| [Point 3](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_II) | Location where the machine-processable list is published. | None | Ignored | Member-State notification. | — |
| [Point 4](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_II) | Location of the human-readable list, or indication it is no longer published. | None | Ignored | Member-State notification. | — |
| [Point 5](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_II) | Public-key certificates (PEM Base64 DER); replacement/addition indication. | Indirect | Unknown | App relies on announced signing certificates; rotation untested. | — |
| [Point 6](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_II) | Date of submission. | None | Ignored | Member-State notification. | — |
| [Final rule](https://eur-lex.europa.eu/eli/dec_impl/2015/1505/oj#anx_II) | Points (1),(2)(a),(3),(4),(5) replace previously notified information in the EC compiled list. | None | Ignored | Commission compilation. | — |

---

## Coverage and limitations

**Text obtained.** Complete English XHTML of Decision 2015/1505 from the Publications Office/Cellar, including preamble, **all 6 recitals**, **Articles 1–5**, **Annex I (Chapters I–IV)** and **Annex II (template points 1–6 with sub-points)**. The original published text is assessed; the later amendment [2025/2164](eu-dec_impl-2025-2164.md) is cross-linked rather than merged.

**Enumeration check.** Article headings and annex chapters were read from the extracted text; Annex I's fields (clauses 5.3.6, 5.3.7, 5.3.9, 5.3.11, 5.5.4) and the interpretation qualifiers are listed individually; Annex II points (1)–(6), their letters and the "minimum certificate fields" list are enumerated. The annex's long descriptive texts are treated as the content of their field rows, not as separate obligations.

**What is not evidenced.** (1) Whether Autogram/DSS 6.5 correctly interprets every `Qualifications Extension` qualifier and the historical-status model — no fixtures. (2) Whether the app handles scheme-operator key rotation/compromise and multiple list-signing certificates. (3) The exact effect of the [2025/2164](eu-dec_impl-2025-2164.md) update on the effective template. (4) Referenced external standards (ETSI TS 119 612, ISO 32000/19005, RFC 3739/5280) were not read clause by clause; they are separate works.

**Status rationale.** Member-State publication and Commission duties are **None / Ignored** for an independent validator. The provisions that determine *how the app must read the list* — Article 2 marking, Article 3(1) signature validation, and the Annex I interpretation qualifiers and default negative rule — are **Indirect/Direct** and **Unknown**, because correct qualified-status classification is exactly what the app's result must get right and it is not independently test-proven.
