# Autogram legal-act register — consolidated overview

> **Branch scope:** this is a documentation-only branch. The trusted-list validation hardening and the three tests named in the text (SignatureValidatorTrustedListTest, TrustedListLiveSmokeTest, SignatureValidationPresentationTest) are delivered as a separate code change and are not present on this branch; those mentions describe that companion change.

**Assessment date:** 8 October 2026 · **Repository baseline:** `5ca91d5c` · **Scope:** Autogram desktop application, locally hosted API and CLI. **Purpose:** one readable entry point over the per-act documents in this register. It is an engineering/legal reading aid, **not legal advice, certification or a released-binary audit**.

This report summarises the register as it stands. **All 56 acts are documented** at provision level.

---

## 1. Executive summary

The register covers **56 distinct acts** linked from the [compliance report](../eidas-2-compliance-report.md). **All 56 are documented** at provision level (recital/article/paragraph/annex), producing **9 188 assessed rows**.

Across those rows:

| Relevance | Rows | Meaning |
| --- | --- | --- |
| **Direct** | 6 | Addresses the app/process or the publisher under established facts |
| **Conditional** | 1 240 | Turns on an unresolved role, deployment or triggering event |
| **Indirect** | 3 101 | Interoperability/input criteria or interpretative context |
| **None** | 4 841 | Addresses other actors (Member States, QTSPs, wallets, browsers) |

| Status | Rows | Meaning |
| --- | --- | --- |
| **Done** | 6 | A narrowly described applicable measure is evidenced |
| **Not done** | 0 | No applicable requirement with a demonstrable unfulfilled measure was established |
| **Unknown** | 2 074 | Applicability or fulfilment cannot be established from the evidence |
| **Ignored** | 7 139 | Deliberately scoped out with a reason (context / other actors) |

**Headline conclusion.** The overwhelming majority of the acts impose **no duty on an independent signing/validation publisher** — they bind Member States, supervisory bodies, qualified trust-service providers, wallet actors, browsers or public bodies. Those rows are **None / Ignored**. The compliance question for Autogram reduces to a small number of **Direct** and **Conditional** matters, all currently **Unknown** for lack of independent evidence:

1. **Validation-system correctness** — eIDAS Articles **32(2)/32a(2)** (and, via Articles 40/40a, seals), operationalised by [Regulation 2025/1945](eu-reg_impl-2025-1945.md) (ETSI TS 119 101, 24-hour revocation freshness, indeterminate reporting). **Direct, Unknown.**
2. **Conditional accessibility** — eIDAS **Article 15** ("end-user products used in the provision of" eID/trust services), plus the EAA and web-accessibility directives if in scope. **Conditional, Unknown** (depends on an unresolved deployment fact).
3. **CRA classification** — whether the publisher is a manufacturer or an open-source steward under [Regulation 2024/2847](eu-reg-2024-2847.md), given own-name releases and above-cost-recovery support. **Unknown, elevated manufacturer risk.**
4. **GDPR data flows** — controller/processor roles and actual update/LOTL/OCSP/TSA/support traffic. **Unknown** ([2016/679](eu-reg-2016-679.md)).
5. **2026/248 format interoperability** — XAdES/CAdES/PAdES/ASiC and the JAdES gap, a product target rather than an independent-vendor deadline ([2026/248](eu-reg_impl-2026-248.md)).

The register contains **no** entry marked **Not done**: no source-code review established an applicable requirement plus a demonstrable failure to meet it. That is a statement about evidence, not a clean bill of health.

---

## 2. The acts that actually matter for Autogram

### 2.1 Direct — the validation system (Unknown)

- **[eIDAS 2014/910](eu-reg-2014-910.md)** and its amendment **[2024/1183](eu-reg-2024-1183.md)**: Articles 32, 32a, 40, 40a require a validation system to return the correct result and expose security-relevant issues. DSS integration and the qualification badges exist; correct outcomes across countries, times and edge cases are not evidenced.
- **[2025/1945](eu-reg_impl-2025-1945.md)**: the annexes expressly require **signature validation applications** to comply with **ETSI TS 119 101 V1.1.1**, apply a **maximum 24-hour revocation freshness for the signing certificate**, and, on failed applicability checks, stop and yield an **indeterminate** result with intermediate findings. **Unknown** without fixtures.
- **[2015/1505](eu-dec_impl-2015-1505.md)** and **[2025/2164](eu-dec_impl-2025-2164.md)**: the trusted-list template the validator consumes; the interpretation qualifiers (`QCForESig`, `QCWithQSCD`, `NotQualified`, …) decide QES-versus-AdES-QC. A live test on 3 October 2026 validated the EU LOTL and Slovak TLv6; other lists, historical status and all qualifiers remain unverified.

### 2.2 Conditional — accessibility, cyber-resilience, liability, data protection

- **[eIDAS Article 15](eu-reg-2014-910.md)**, **[EAA 2019/882](eu-dir-2019-882.md)**, **[2016/2102](eu-dir-2016-2102.md)**: plain-language and accessibility duties attach only if Autogram is an end-user product used in providing an eID/trust service (unresolved).
- **[CRA 2024/2847](eu-reg-2024-2847.md)** and **[2025/2392](eu-reg_impl-2025-2392.md)**: manufacturer versus steward, product classification, support period and incident reporting (Art. 14 since 11 Sep 2026; most duties from 11 Dec 2027).
- **[PLD 2024/2853](eu-dir-2024-2853.md)**: commercial supply of defective software; own-name release and paid support raise the risk, classification unresolved.
- **[GDPR 2016/679](eu-reg-2016-679.md)**: actual processing, recipients and roles.
- **[NIS2 2022/2555](eu-dir-2022-2555.md)** and **[2024/903](eu-reg-2024-903.md)**: covered-entity and public-sector duties, relevant only if the same legal entity is separately designated or the app is used by a covered body.
- **[2026/248](eu-reg_impl-2026-248.md)**: public-sector recognition of formats; a capability target, with the JAdES creation gap noted.

### 2.3 None — the bulk (wallet, provider and Member-State acts)

Wallet-domain acts ([2024/2977](eu-reg_impl-2024-2977.md), [2024/2979](eu-reg_impl-2024-2979.md), [2024/2980](eu-reg_impl-2024-2980.md), [2024/2981](eu-reg_impl-2024-2981.md), [2024/2982](eu-reg_impl-2024-2982.md), [2025/846](eu-reg_impl-2025-846.md)–[849](eu-reg_impl-2025-849.md), [2026/798](eu-reg_impl-2026-798.md), and the amendments [2026/1730](eu-reg_impl-2026-1730.md)/[1731](eu-reg_impl-2026-1731.md)/[1735](eu-reg_impl-2026-1735.md)), provider/standards acts ([2025/1942](eu-reg_impl-2025-1942.md), [1943](eu-reg_impl-2025-1943.md), [1944](eu-reg_impl-2025-1944.md), [1946](eu-reg_impl-2025-1946.md), [1929](eu-reg_impl-2025-1929.md), [1566](eu-reg_impl-2025-1566.md), [1567](eu-reg_impl-2025-1567.md), [1569](eu-reg_impl-2025-1569.md), [1570](eu-reg_impl-2025-1570.md), [1571](eu-reg_impl-2025-1571.md), [1572](eu-reg_impl-2025-1572.md), [2160](eu-reg_impl-2025-2160.md), [2162](eu-reg_impl-2025-2162.md), [2527](eu-reg_impl-2025-2527.md), [2530](eu-reg_impl-2025-2530.md), [2531](eu-reg_impl-2025-2531.md), [2532](eu-reg_impl-2025-2532.md)) and eID/cooperation acts ([2015/296](eu-dec_impl-2015-296.md), [2025/1568](eu-reg_impl-2025-1568.md), [2015/1501](eu-reg_impl-2015-1501.md), [2015/1502](eu-reg_impl-2015-1502.md)) bind other actors. They are registered for completeness and cross-checked, not because they impose app duties.

---

## 3. Per-act index

Each entry links to the full provision-by-provision document. "Direct/Cond" and "Unk" are row counts in that document.

| Act | Rows | Direct/Cond | Unk | One-line finding |
| --- | --- | --- | --- | --- |
| [EU 2013/662](eu-dec-2013-662.md) | 244 | 0/0 | 0 | Amends Decision 2009/767/EC annex; historical public-sector list. |
| [EU 2014/148](eu-dec-2014-148.md) | 22 | 0/0 | 0 | Amends Decision 2011/130/EU (Services Directive documents). |
| [EU 2015/1505](eu-dec_impl-2015-1505.md) | 48 | 0/0 | 24 | Trusted-list template; the validator must read its qualifiers correctly. |
| [EU 2015/1506](eu-dec_impl-2015-1506.md) | 52 | 0/0 | 0 | Legacy public-sector format recognition; repealed by 2026/248. |
| [EU 2015/296](eu-dec_impl-2015-296.md) | 92 | 0/4 | 0 | Member-State eID cooperation/peer review; replaced by 2025/1568. |
| [EU 2016/650](eu-dec_impl-2016-650.md) | 28 | 0/0 | 0 | QSCD certification standards; not app certification. |
| [EU 2025/2164](eu-dec_impl-2025-2164.md) | 92 | 0/1 | 60 | Amends 2015/1505 (TLv6 / ETSI TS 119 612 update). |
| [EU 2016/2102](eu-dir-2016-2102.md) | 148 | 0/5 | 32 | Public-sector web/mobile accessibility; not a blanket app duty. |
| [EU 2019/882](eu-dir-2019-882.md) | 602 | 0/92 | 169 | EAA enumerated products/services; conditional on scope. |
| [EU 2022/2555](eu-dir-2022-2555.md) | 804 | 0/230 | 230 | NIS2 covered entities; conditional on entity designation. |
| [EU 2024/2853](eu-dir-2024-2853.md) | 257 | 0/89 | 91 | Product Liability Directive; commercial-supply risk. |
| [EU 2014/910 (eIDAS)](eu-reg-2014-910.md) | 484 | 1/16 | 94 | Founding act; Art. 32/32a validation duties and conditional Art. 15. |
| [EU 2016/679 (GDPR)](eu-reg-2016-679.md) | 401 | 1/162 | 163 | Controller/processor roles and data flows unknown. |
| [EU 2024/1183](eu-reg-2024-1183.md) | 196 | 4/9 | 22 | eIDAS 2.0 amendment; validation and conditional accessibility. |
| [EU 2024/2847 (CRA)](eu-reg-2024-2847.md) | 779 | 0/287 | 291 | Manufacturer vs steward; reporting since 11 Sep 2026. |
| [EU 2024/903](eu-reg-2024-903.md) | 293 | 0/62 | 82 | Interoperable Europe; public-sector addressees. |
| [EU 2015/1501](eu-reg_impl-2015-1501.md) | 76 | 0/0 | 0 | eID interoperability framework; scheme/node operators. |
| [EU 2015/1502](eu-reg_impl-2015-1502.md) | 215 | 0/0 | 0 | Assurance levels for notified eID schemes. |
| [EU 2015/806](eu-reg_impl-2015-806.md) | 20 | 0/12 | 12 | EU trust-mark format; not an ordinary logo. |
| [EU 2024/2977](eu-reg_impl-2024-2977.md) | 248 | 0/0 | 0 | Wallet PID/EAA issuance; not document signing. |
| [EU 2024/2979](eu-reg_impl-2024-2979.md) | 140 | 0/0 | 0 | Wallet integrity/core functions. |
| [EU 2024/2980](eu-reg_impl-2024-2980.md) | 113 | 0/0 | 0 | Wallet notification/publication rules. |
| [EU 2024/2981](eu-reg_impl-2024-2981.md) | 551 | 0/54 | 0 | Wallet certification. |
| [EU 2024/2982](eu-reg_impl-2024-2982.md) | 190 | 0/0 | 0 | Wallet protocols and interfaces. |
| [EU 2025/1566](eu-reg_impl-2025-1566.md) | 23 | 0/1 | 1 | Identity/attribute verification standard for issuance. |
| [EU 2025/1567](eu-reg_impl-2025-1567.md) | 28 | 0/0 | 0 | Remote QSCD management standards. |
| [EU 2025/1568](eu-reg_impl-2025-1568.md) | 87 | 0/0 | 0 | eID cooperation/peer review procedure. |
| [EU 2025/1569](eu-reg_impl-2025-1569.md) | 125 | 0/0 | 0 | Qualified attribute attestations. |
| [EU 2025/1570](eu-reg_impl-2025-1570.md) | 36 | 0/1 | 24 | Member-State QSCD notifications. |
| [EU 2025/1571](eu-reg_impl-2025-1571.md) | 61 | 0/0 | 0 | Supervisory-body annual reports. |
| [EU 2025/1572](eu-reg_impl-2025-1572.md) | 42 | 0/1 | 1 | QTSP notification of intention to provide a qualified service. |
| [EU 2025/1929](eu-reg_impl-2025-1929.md) | 81 | 0/2 | 13 | Qualified time-stamp standards; app is a client. |
| [EU 2025/1942](eu-reg_impl-2025-1942.md) | 88 | 0/23 | 23 | Qualified validation service standards; not a local report. |
| [EU 2025/1943](eu-reg_impl-2025-1943.md) | 128 | 0/1 | 27 | Qualified certificate issuance standards (inputs). |
| [EU 2025/1944](eu-reg_impl-2025-1944.md) | 82 | 0/1 | 1 | Qualified registered delivery standards. |
| [EU 2025/1945](eu-reg_impl-2025-1945.md) | 51 | 0/38 | 38 | **Validation-application standards: TS 119 101, 24h freshness, indeterminate.** |
| [EU 2025/1946](eu-reg_impl-2025-1946.md) | 61 | 0/1 | 10 | Qualified preservation standards. |
| [EU 2025/2160](eu-reg_impl-2025-2160.md) | 61 | 0/49 | 49 | Risk management for non-qualified trust services. |
| [EU 2025/2162](eu-reg_impl-2025-2162.md) | 158 | 0/13 | 13 | Accreditation/conformity assessment for QTSPs. |
| [EU 2025/2392](eu-reg_impl-2025-2392.md) | 54 | 0/5 | 10 | CRA important/critical product descriptions. |
| [EU 2025/2527](eu-reg_impl-2025-2527.md) | 21 | 0/1 | 1 | Qualified website-authentication certificate standards. |
| [EU 2025/2530](eu-reg_impl-2025-2530.md) | 75 | 0/1 | 9 | QTSP requirements. |
| [EU 2025/2531](eu-reg_impl-2025-2531.md) | 85 | 0/0 | 0 | Qualified electronic ledger standards. |
| [EU 2025/2532](eu-reg_impl-2025-2532.md) | 58 | 0/6 | 7 | Qualified electronic archiving standards. |
| [EU 2025/846](eu-reg_impl-2025-846.md) | 58 | 0/1 | 0 | Cross-border identity matching. |
| [EU 2025/847](eu-reg_impl-2025-847.md) | 115 | 0/0 | 0 | Wallet security-breach reactions. |
| [EU 2025/848](eu-reg_impl-2025-848.md) | 194 | 0/0 | 0 | Registration of wallet-relying parties. |
| [EU 2025/849](eu-reg_impl-2025-849.md) | 64 | 0/0 | 0 | Submission of certified-wallet list information. |
| [EU 2026/248](eu-reg_impl-2026-248.md) | 72 | 0/1 | 55 | Public-sector format recognition; JAdES gap. |
| [EU 2026/798](eu-reg_impl-2026-798.md) | 49 | 0/0 | 0 | Wallet remote-onboarding standards. |
| [EU 2026/1730](eu-reg_impl-2026-1730.md) | 8 | 0/0 | 0 | Amends 2025/848 (standards). |
| [EU 2026/1731](eu-reg_impl-2026-1731.md) | 14 | 0/0 | 0 | Amends wallet regulations (portrait, gatekeepers). |
| [EU 2026/1735](eu-reg_impl-2026-1735.md) | 10 | 0/0 | 0 | Amends 2025/1569 (attribute standards). |
| [SK 2016/272](sk-2016-272.md) | 200 | 0/57 | 88 | Slovak trust-services act; provider supervision. |
| [SK 2020/78](sk-2020-78.md) | 504 | 0/10 | 252 | Slovak public-administration IT standards; e-form profiles. |
| [SK 2013/305](sk-2013-305.md) | 400 | 0/0 | 0 | Slovak e-Government act; filer/authority duties, e-form interoperability. |

---

## 4. What would close the unknowns

1. **Validation fixtures** — licensed/consented qualified signatures and seals (QES, AdES-QC, seals), from covered and uncovered countries, at fixed signing times, including revoked, expired, pseudonymous, missing-QSCD, tampered and failed-applicability cases; assert the DSS **and** displayed results. Add the **24-hour revocation-freshness** and **indeterminate-reporting** boundary tests from [2025/1945](eu-reg_impl-2025-1945.md).
2. **Trusted-list coverage** — deterministic snapshots for more territories, historical status and every interpretation qualifier; the selected-country default is a product decision that must stay visible in the UI.
3. **Format interoperability** — version-specific output/validation tests against [2026/248](eu-reg_impl-2026-248.md) Annexes I–II, and a decision on JAdES creation.
4. **Organisational facts** — CRA manufacturer/steward contracts and classification; GDPR data-flow/role inventory; whether any release is an end-user product used in providing an eID/trust service (eIDAS Art. 15).

---

## 5. Coverage and limitations

- **Enumeration:** each per-act document enumerates recitals, articles with numbered paragraphs/letters, and annex points, with a link on every provision to its authoritative anchor. Structural and convention checks are run by [`scripts/check-legal-act-docs.py`](../../scripts/check-legal-act-docs.py); all current documents pass with 0 structural issues. The checker cannot verify remote anchor fragments or legal correctness.
- **Sources:** EU acts were read from official Publications Office/Cellar XHTML (EUR-Lex rendered pages are WAF-protected to scripted access); Slovak acts from Slov-Lex. Referenced external standards (ETSI, ISO, RFC) are separate works and were not read clause by clause.
- **Known follow-ups:** annex anchors were normalised to each act's official id scheme (`#anx_I` vs `#anx_1`) and the [2025/848](eu-reg_impl-2025-848.md) label was reconciled with the [2026/1730](eu-reg_impl-2026-1730.md) document (registration of wallet-relying parties, not a trust mark). Remaining: confirm a handful of remote anchors against the rendered EUR-Lex pages, which are WAF-protected to scripted access.
- **Statuses:** **Done** means a narrow evidenced measure, not legal compliance; **Unknown** is not an allegation; **Ignored** means deliberately out of scope with a reason, never knowingly disregarding an obligation. No row is **Not done** because no applicable requirement with a demonstrable failure was established.
