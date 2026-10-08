# Verification tracker

**Assessment date:** 8 October 2026 · **Repository baseline:** `5ca91d5c` · **Branch:** `eidas-2-compliance-audit` (documentation only).

This tracker turns the register's per-provision statuses into an act-by-act work list. **Done** below means "assessed: no Autogram duty was established". **To verify** means an open item needs external evidence or a decision, not that a breach exists. Check a box when the item is closed, and update the per-act document's statuses accordingly.

Legend: `[x]` closed · `[ ]` open · **A** no Autogram duty · **B** only a data-protection pointer · **C** verification required.

Summary: **56 acts** — **10 A**, **7 B**, **39 C**. Open rows overall: **2 075 Unknown**, **33 Direct**, **1 241 Conditional**.

---

## A. No Autogram duty established — assessed, no action

- [x] [EU 2015/1501](eu-reg_impl-2015-1501.md) — eID interoperability framework (scheme/node operators)
- [x] [EU 2015/1502](eu-reg_impl-2015-1502.md) — assurance levels for notified eID schemes
- [x] [EU 2024/2977](eu-reg_impl-2024-2977.md) — wallet PID/EAA issuance
- [x] [EU 2024/2979](eu-reg_impl-2024-2979.md) — wallet integrity/core functions
- [x] [EU 2024/2980](eu-reg_impl-2024-2980.md) — wallet notification/publication
- [x] [EU 2024/2982](eu-reg_impl-2024-2982.md) — wallet protocols and interfaces
- [x] [EU 2025/1571](eu-reg_impl-2025-1571.md) — supervisory-body annual reports
- [x] [EU 2025/847](eu-reg_impl-2025-847.md) — wallet security-breach reactions
- [x] [EU 2025/849](eu-reg_impl-2025-849.md) — certified-wallet list submissions
- [x] [EU 2026/798](eu-reg_impl-2026-798.md) — wallet remote-onboarding standards

## B. Data-protection pointer only

The only open item is the GDPR cross-reference. No act-specific duty; close when the [GDPR](eu-reg-2016-679.md) data-flow review is done.

- [ ] [EU 2025/1567](eu-reg_impl-2025-1567.md) — remote QSCD management (provider duty)
- [ ] [EU 2025/1568](eu-reg_impl-2025-1568.md) — eID cooperation/peer review (procedure)
- [ ] [EU 2025/1569](eu-reg_impl-2025-1569.md) — qualified attribute attestations (issuer duty)
- [ ] [EU 2025/848](eu-reg_impl-2025-848.md) — registration of wallet-relying parties
- [ ] [EU 2026/1730](eu-reg_impl-2026-1730.md) — amends 2025/848 (standards)
- [ ] [EU 2026/1731](eu-reg_impl-2026-1731.md) — amends wallet regulations
- [ ] [EU 2026/1735](eu-reg_impl-2026-1735.md) — amends 2025/1569 (attribute standards)

## C. Verification required — act by act

### Validation correctness (highest priority)

- [ ] [EU 2014/910 — eIDAS](eu-reg-2014-910.md) · Direct 25, Cond 17, Unk 95
  - [ ] Qualified/AdES-QC signature **and seal** fixtures across countries and fixed signing times; assert DSS **and** displayed result (Arts. 32, 32a, 40, 40a).
  - [ ] Pseudonym indication, QSCD-evidence handling, and the default negative rule (no over-claiming QES).
  - [ ] Accessibility (Art. 15): **applicable, Not done — findings tracked**. Hackathon raised accessibility issues; reported and to be fixed gradually ([detail](art15-accessibility.md)).
- [ ] [EU 2024/1183 — eIDAS 2.0 amendment](eu-reg-2024-1183.md) · Direct 4, Cond 9, Unk 22
  - [ ] Same validation fixtures; confirm no promotion of AdES-QC to QES.
  - [ ] Art. 15 deployment-scope decision.
- [ ] [EU 2025/1945 — validation application standards](eu-reg_impl-2025-1945.md) · Cond 38, Unk 38
  - [ ] **24-hour signing-certificate revocation freshness** (and no value for other certificates) with 23/24/25-hour fixtures.
  - [ ] Failed applicability → stop, **indeterminate**, intermediate findings in the report.
  - [ ] Application-level **ETSI TS 119 101** conformity; schema-valid TS 119 102-2 report.
- [ ] [EU 2025/1942 — qualified validation service standards](eu-reg_impl-2025-1942.md) · Cond 23, Unk 23
  - [ ] Confirm the local DSS report is **not** offered as a qualified validation service; policy mapping only if the presumption route is claimed.
- [ ] [EU 2015/1505 — trusted lists](eu-dec_impl-2015-1505.md) · Direct 3, Unk 15
  - [ ] Interpretation qualifiers (`QCForESig`, `QCWithQSCD`, `NotQualified`, …), historical status, list-signature rotation.
- [ ] [EU 2025/2164 — TLv6 trusted-list update](eu-dec_impl-2025-2164.md) · Cond 1, Unk 60
  - [ ] Current/historical transitions, pointer/rotation fixtures, supported `Sti` and extension cases.

### Formats and interoperability

- [ ] [EU 2026/248 — public-sector formats](eu-reg_impl-2026-248.md) · Cond 1, Unk 55
  - [ ] Version-specific output/validation conformance for XAdES/CAdES/PAdES/ASiC against Annex I (EN 319 132-1/122-1/142-1/162-1/162-2).
  - [ ] **JAdES** creation decision; Annex II legacy profiles (TS 103 171/173/172/174) for pre-2028 objects.
- [ ] [EU 2015/1506 — legacy public-sector formats](eu-dec_impl-2015-1506.md) · Unk 12
  - [ ] Exact-version legacy profile fixtures + receiving-service acceptance.
- [ ] [EU 2014/148 — cross-border signed documents](eu-dec-2014-148.md) · Unk 7
  - [ ] Exact-edition XAdES/CAdES/PAdES/ASiC conformance and receiving-system results.
- [ ] [EU 2013/662 — historical trusted-list amendments](eu-dec-2013-662.md) · Unk 80
  - [ ] Historical list-authentication and qualification fixtures only if such archived lists are consumed.

### Issuance and input standards (certificate/timestamp/profile inputs)

- [ ] [EU 2025/1943 — qualified certificate issuance standards](eu-reg_impl-2025-1943.md) · Cond 1, Unk 27
  - [ ] Input-profile mapping to EN 319 412 (policy OIDs, QCStatements) in the reading/validation paths.
- [ ] [EU 2025/1566 — identity/attribute verification standard](eu-reg_impl-2025-1566.md) · Cond 1, Unk 1
  - [ ] Input-only; close with the GDPR data-flow review.
- [ ] [EU 2025/2527 — website-authentication certificates](eu-reg_impl-2025-2527.md) · Cond 1, Unk 1
  - [ ] Confirm no issuance role; GDPR pointer.
- [ ] [EU 2025/1929 — qualified time-stamp standards](eu-reg_impl-2025-1929.md) · Cond 2, Unk 13
  - [ ] Token/algorithm fixtures; confirm whether the configured TSA is qualified; HTTPS transport per the act.
- [ ] [EU 2025/1946 — qualified preservation standards](eu-reg_impl-2025-1946.md) · Cond 1, Unk 10
  - [ ] Confirm no preservation service; validation-policy overlap only.
- [ ] [EU 2025/2532 — qualified archiving standards](eu-reg_impl-2025-2532.md) · Cond 6, Unk 7
  - [ ] Confirm no QEA service; TSA qualification check.
- [ ] [EU 2025/2531 — qualified ledger standards](eu-reg_impl-2025-2531.md) · Unk 30
  - [ ] Confirm no ledger service; ENISA algorithm-list conformity if a ledger link were claimed.
- [ ] [EU 2016/650 — QSCD certification standards](eu-dec_impl-2016-650.md) · Unk 12
  - [ ] Device certification dossiers for the tokens actually supported; the app itself is not certified.
- [ ] [EU 2025/1570 — certified device notifications](eu-reg_impl-2025-1570.md) · Cond 1, Unk 24
  - [ ] Match supported tokens to certified device scope/version.

### Provider-role and organisational decisions

- [ ] [EU 2025/2160 — risk management for non-qualified services](eu-reg_impl-2025-2160.md) · Cond 49, Unk 49
  - [ ] Documented decision whether the publisher operates any non-qualified trust service; paid support alone is not proof.
- [ ] [EU 2025/1572 — QTSP notification](eu-reg_impl-2025-1572.md) · Cond 1, Unk 1
  - [ ] Confirm no provider role.
- [ ] [EU 2025/2530 — QTSP requirements](eu-reg_impl-2025-2530.md) · Cond 1, Unk 9
  - [ ] Confirm no provider role; whether any component is supplied into a qualified service.
- [ ] [EU 2025/2162 — accreditation/CAB requirements](eu-reg_impl-2025-2162.md) · Cond 13, Unk 13
  - [ ] Confirm no CAB/QTSP role; whether an Autogram component sits in a QTSP's assessed service.
- [ ] [EU 2015/806 — EU trust mark](eu-reg_impl-2015-806.md) · Cond 12, Unk 12
  - [ ] Confirm the official mark is not used, or that its use is correct.
- [ ] [EU 2015/296 — historical eID cooperation](eu-dec_impl-2015-296.md) · Cond 4
  - [ ] Confirm no eID-scheme role; superseded by 2025/1568.

### Cyber-resilience, liability, data protection, accessibility

- [ ] [EU 2024/2847 — Cyber Resilience Act](eu-reg-2024-2847.md) · Cond 287, Unk 291
  - [ ] **Manufacturer vs open-source steward** classification (own-name release, above-cost-recovery support); contracts.
  - [ ] Product classification (Annex III / 2025/2392); support period; vulnerability handling; incident reporting (Art. 14 since 11 Sep 2026).
- [ ] [EU 2025/2392 — CRA product categories](eu-reg_impl-2025-2392.md) · Cond 5, Unk 10
  - [ ] Documented classification decision against the category tables.
- [ ] [EU 2016/679 — GDPR](eu-reg-2016-679.md) · Direct 1, Cond 162, Unk 163
  - [ ] Data-flow inventory (update check, LOTL/OCSP/TSA, form downloads, support/site), controller/processor roles, legal basis, retention, notices.
- [ ] [EU 2022/2555 — NIS2](eu-dir-2022-2555.md) · Cond 230, Unk 230
  - [ ] Covered-entity classification for the legal entity (not the app).
- [ ] [EU 2024/2853 — Product Liability Directive](eu-dir-2024-2853.md) · Cond 89, Unk 91
  - [ ] Commercial-supply dossier (licence, support linkage, consideration, economic context).
- [ ] [EU 2024/903 — Interoperable Europe](eu-reg-2024-903.md) · Cond 62, Unk 82
  - [ ] Public-sector participation/sharing evidence, only where a covered body deploys Autogram.
- [ ] [EU 2019/882 — European Accessibility Act](eu-dir-2019-882.md) · Cond 92, Unk 169
  - [ ] Scope decision (is Autogram an in-scope product/service?); accessibility audit of GUI/CLI.
- [ ] [EU 2016/2102 — web/mobile accessibility](eu-dir-2016-2102.md) · Cond 5, Unk 32
  - [ ] Confirm any public-body deployment; accessibility audit if in scope.

### Slovak national interoperability

- [ ] [SK 2013/305 — e-Government](sk-2013-305.md) · Cond 4, Unk 43
  - [ ] e-form/XDC format tests against current receiving-system profiles; filing-role decision.
- [ ] [SK 2020/78 — public-administration IT standards](sk-2020-78.md) · Cond 10, Unk 252
  - [ ] e-form/format profile tests for the receiving systems actually used.
- [ ] [SK 2016/272 — trust services](sk-2016-272.md) · Cond 57, Unk 88
  - [ ] Confirm no provider role; certificate/status fixtures.

---

## How to close the shared unknowns

Most C items reduce to a small set of evidence artefacts:

1. **Signature/seal fixture corpus** (qualified, AdES-QC, seals; covered and uncovered countries; fixed times; revoked/expired/pseudonymous/no-QSCD/tampered).
2. **Trusted-list snapshots** (territories, historical status, qualifiers, rotation).
3. **Format conformance tests** (version-specific output/validation; JAdES decision).
4. **Organisational decisions** (CRA role, GDPR roles, eIDAS Art. 15 end-user-product scope, any trust-service role).
5. **Accessibility audit**.
6. **National e-form/format tests**.

Closing these converts the relevant **Unknown** rows to **Done** or **Not done** and lets the compliance conclusion be stated with evidence.
