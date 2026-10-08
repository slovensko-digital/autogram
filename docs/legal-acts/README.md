# Autogram legal-act reading register

> **Branch scope:** this is a documentation-only branch. The trusted-list validation hardening and the three tests named in the text (SignatureValidatorTrustedListTest, TrustedListLiveSmokeTest, SignatureValidationPresentationTest) are delivered as a separate code change and are not present on this branch; those mentions describe that companion change.

Assessment date: **8 October 2026**. Repository baseline: **5ca91d5c**. Scope: Autogram desktop application, local API and CLI, not the browser extension or a hosted service. This is a traceable engineering/legal reading aid, **not legal advice, certification or a released-binary audit**.

**Start here:** [OVERVIEW.md](OVERVIEW.md) is a one-page consolidated report across all acts (relevance tiers, headline findings, per-act index). [VERIFICATION.md](VERIFICATION.md) is the act-by-act work list of what is done and what still needs external verification.

Each act receives an English Markdown document with an executive summary, official complete-text links, version/date information and a provision-by-provision assessment. Complete legal text is **linked, not reproduced**. Consolidations are reading aids; the published acts and corrigenda remain authoritative. Amending acts receive their own documents, cross-linked to the base acts. Historical/repealed acts are retained and labelled.

## Shared facts

- The organisation is not a qualified trust-service provider and operates no Autogram service receiving users' documents/personal data.
- The organisation publishes Autogram under its name and charges above cost recovery for support. CRA commercial-supply/manufacturer classification remains **Unknown**, with elevated manufacturer risk; actual contracts and distribution facts have not been reviewed.
- Whether Autogram is an end-user product used in providing an eID/trust service is **Unknown**. Do not assume eIDAS Article 15 never applies merely because the developer is not a QTSP.
- Eight trusted-list countries remain selected by default. SK TLv6 and EU LOTL passed a live test on **3 October 2026**; that does not prove EU-wide or historical signature validation correctness.
- JAdES creation is unsupported on the evidenced creation path. The user requested analysis, not implementation. Public-sector recognition duties are not automatically independent-vendor deadlines.
- Source code and tests prove specific behaviours, not organisational compliance, complete ETSI conformity or all qualified-signature outcomes. Missing evidence is not proof of a statutory violation.

## Table vocabulary

Relevance and status are independent:

| Relevance | Meaning |
| --- | --- |
| Direct | Addresses the application/process or the organisation under established facts. |
| Conditional | Depends on an unresolved role, activity, deployment or triggering event; state that condition. |
| Indirect | Interoperability/input criteria or interpretative context, not a direct obligation on this publisher. |
| None | Addresses other actors/activities under the confirmed scope. |

| Status | Meaning |
| --- | --- |
| Done | Concrete evidence establishes the narrowly described applicable measure. Not general legal compliance. |
| Not done | An applicable requirement and a demonstrable unfulfilled measure are both established. |
| Unknown | Applicability or fulfilment cannot be established, or source/evidence is unavailable. |
| Ignored | Deliberately scoped out with a reason (not applicable, historical, or purely contextual). Never means knowingly disregarding an applicable obligation. |

An indirect technical gap may be described without assigning **Not done** to an inapplicable statutory duty. For partially proved requirements use **Unknown** overall and describe what is proved. Definitions, recitals and commencement provisions are not software tasks; classify their context and give a reason rather than marking them Done.

## Required document structure and coverage

1. Title and a short **Executive summary**: relevance, established evidence, major unknowns and dates.
2. **Source and version**: official full-text URL, CELEX/ELI or national identifier, language, adopted/published/entry/application dates when verified, consolidation date, amendments/repeal relationships, source-access date and repository baseline.
3. **Provision-by-provision assessment**, split into readable tables by chapter/annex. Columns: `Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes`.
4. Every article/section, numbered paragraph, lettered subpoint and annex requirement must be individually identifiable; do not collapse an article's numbered paragraphs into one blanket row. Include unnumbered operative paragraphs, definitions, amendment insertion/replacement blocks, transitional/commencement provisions, each recital and annex point. Use structural headings to avoid duplicating parent text as an extra duty. If a source does not number a paragraph, label it `unnumbered paragraph 1`, etc. Cross-links must not replace assessment rows.
5. **Coverage and limitations**: state exactly which text was obtained, enumerated units and how checked, unavailable/omitted portions, whether linked ETSI standards were inspected, and outstanding evidence. Never claim complete coverage if a source or annex was not accessible. Referenced technical standards are separate works, not part of the act's complete text; do not silently import or reproduce them.

Evidence should use repository-relative Markdown links (from here, source paths start `../../src/`) and, where useful, immutable GitHub links at baseline 5ca91d5c. Cite the relevant symbol and line range after inspecting it. For organisational matters name the exact missing document, contract, test or decision; do not put an unrelated Java path in every row. Distinguish binding text, interpretative recitals and external guidance. Do not translate Slovak statutes as though an unofficial English paraphrase were an authoritative text.

**Provision links (required).** Every provision label — each article heading, each table row's `Provision` cell and each recital — must link to the authoritative text at that unit so a reviewer can open it directly. Use the publisher's own anchor identifiers, which for EUR-Lex/Cellar consolidated acts are `#art_<n>`, `#anx_<roman>` and, for the original Official Journal acts, `#rct_<n>` (for example `https://eur-lex.europa.eu/eli/reg/2014/910/2024-10-18/eng#art_32`, `.../eng#anx_II`, `https://eur-lex.europa.eu/eli/reg/2014/910/oj#rct_29`). Verify the anchor scheme against the retrieved source before using it and state the scheme in the document's source block. National acts use the publisher's own stable identifiers or section anchors.

## Inventory and consistency review

The inventory comprises **56 distinct acts** explicitly linked in the existing [report](../eidas-2-compliance-report.md), including the Slovak instruments and adjacent EU legislation. The Commission's trusted-list information notice and ETSI standards are supporting sources, not counted as additional legislative acts. Completeness is checked against retrieved official texts, **not inferred from the older report**.

An act-by-act index and consistency-review findings are maintained below. Unavailable official sources must be listed as blockers rather than filled with invented provisions.

**Definition of done:** a document marked **Done** has an executive summary, an official-source/version block, all recitals individually, per-provision tables `Provision / Requirement / Relevance / Status / Evidence / Dates`, a link on every provision to its authoritative anchor, and a Coverage section. Documents marked **Draft** exist but have not yet been reviewed to that standard. Structural/convention checks are run by [`scripts/check-legal-act-docs.py`](../../scripts/check-legal-act-docs.py).

### Act index

| File | Act | Name | State |
| --- | --- | --- | --- |
| [`eu-dec-2013-662.md`](eu-dec-2013-662.md) | EU 2013/662 | historical trusted-list amendments | **Done** |
| [`eu-dec-2014-148.md`](eu-dec-2014-148.md) | EU 2014/148 | cross-border signed authority documents | **Done** |
| [`eu-dec_impl-2015-1505.md`](eu-dec_impl-2015-1505.md) | EU 2015/1505 | trusted lists | **Done** |
| [`eu-dec_impl-2015-1506.md`](eu-dec_impl-2015-1506.md) | EU 2015/1506 | historical public-sector signature and seal formats | **Done** |
| [`eu-dec_impl-2015-296.md`](eu-dec_impl-2015-296.md) | EU 2015/296 | historical eID-scheme cooperation | **Done** |
| [`eu-dec_impl-2016-650.md`](eu-dec_impl-2016-650.md) | EU 2016/650 | QSCD security-assessment standards | **Done** |
| [`eu-dec_impl-2025-2164.md`](eu-dec_impl-2025-2164.md) | EU 2025/2164 | trusted-list template update | **Done** |
| [`eu-dir-2016-2102.md`](eu-dir-2016-2102.md) | EU 2016/2102 | public-sector web and mobile accessibility | **Done** |
| [`eu-dir-2019-882.md`](eu-dir-2019-882.md) | EU 2019/882 | European Accessibility Act | **Done** |
| [`eu-dir-2022-2555.md`](eu-dir-2022-2555.md) | EU 2022/2555 | NIS2 | **Done** |
| [`eu-dir-2024-2853.md`](eu-dir-2024-2853.md) | EU 2024/2853 | revised Product Liability Directive | **Done** |
| [`eu-reg-2014-910.md`](eu-reg-2014-910.md) | EU 2014/910 | "eIDAS 2.0" | **Done** |
| [`eu-reg-2016-679.md`](eu-reg-2016-679.md) | EU 2016/679 | General Data Protection Regulation (GDPR) | **Done** |
| [`eu-reg-2024-1183.md`](eu-reg-2024-1183.md) | EU 2024/1183 | European Digital Identity Framework (eIDAS amendment) | **Done** |
| [`eu-reg-2024-2847.md`](eu-reg-2024-2847.md) | EU 2024/2847 | Cyber Resilience Act (CRA) | **Done** |
| [`eu-reg-2024-903.md`](eu-reg-2024-903.md) | EU 2024/903 | Interoperable Europe Act | **Done** |
| [`eu-reg_impl-2015-1501.md`](eu-reg_impl-2015-1501.md) | EU 2015/1501 | eID interoperability framework | **Done** |
| [`eu-reg_impl-2015-1502.md`](eu-reg_impl-2015-1502.md) | EU 2015/1502 | electronic identification assurance levels | **Done** |
| [`eu-reg_impl-2015-806.md`](eu-reg_impl-2015-806.md) | EU 2015/806 | EU trust mark for qualified trust services | **Done** |
| [`eu-reg_impl-2024-2977.md`](eu-reg_impl-2024-2977.md) | EU 2024/2977 | wallet identification data and attributes | **Done** |
| [`eu-reg_impl-2024-2979.md`](eu-reg_impl-2024-2979.md) | EU 2024/2979 | integrity and core functionalities of European Digital Identity Wallets | **Done** |
| [`eu-reg_impl-2024-2980.md`](eu-reg_impl-2024-2980.md) | EU 2024/2980 | notifications to the Commission concerning the European Digital Identity Wallet ecosystem | **Done** |
| [`eu-reg_impl-2024-2981.md`](eu-reg_impl-2024-2981.md) | EU 2024/2981 | certification of European Digital Identity Wallets | **Done** |
| [`eu-reg_impl-2024-2982.md`](eu-reg_impl-2024-2982.md) | EU 2024/2982 | wallet protocols and interfaces | **Done** |
| [`eu-reg_impl-2025-1566.md`](eu-reg_impl-2025-1566.md) | EU 2025/1566 | reference standards for verification of identity and attributes at issuance | **Done** |
| [`eu-reg_impl-2025-1567.md`](eu-reg_impl-2025-1567.md) | EU 2025/1567 | management of remote qualified signature and seal creation devices | **Done** |
| [`eu-reg_impl-2025-1568.md`](eu-reg_impl-2025-1568.md) | EU 2025/1568 | cooperation and peer review of eID schemes | **Done** |
| [`eu-reg_impl-2025-1569.md`](eu-reg_impl-2025-1569.md) | EU 2025/1569 | qualified electronic attestations of attributes | **Done** |
| [`eu-reg_impl-2025-1570.md`](eu-reg_impl-2025-1570.md) | EU 2025/1570 | certified signature/seal device notifications | **Done** |
| [`eu-reg_impl-2025-1571.md`](eu-reg_impl-2025-1571.md) | EU 2025/1571 | supervisory-body annual reports | **Done** |
| [`eu-reg_impl-2025-1572.md`](eu-reg_impl-2025-1572.md) | EU 2025/1572 | notification and verification of qualified trust services | **Done** |
| [`eu-reg_impl-2025-1929.md`](eu-reg_impl-2025-1929.md) | EU 2025/1929 | qualified electronic time stamps (time binding and time-source accuracy) | **Done** |
| [`eu-reg_impl-2025-1942.md`](eu-reg_impl-2025-1942.md) | EU 2025/1942 | reference standards for qualified validation services | **Done** |
| [`eu-reg_impl-2025-1943.md`](eu-reg_impl-2025-1943.md) | EU 2025/1943 | reference standards for qualified certificates for electronic signatures and seals | **Done** |
| [`eu-reg_impl-2025-1944.md`](eu-reg_impl-2025-1944.md) | EU 2025/1944 | qualified electronic registered delivery services | **Done** |
| [`eu-reg_impl-2025-1945.md`](eu-reg_impl-2025-1945.md) | EU 2025/1945 | signature and seal validation | **Done** |
| [`eu-reg_impl-2025-1946.md`](eu-reg_impl-2025-1946.md) | EU 2025/1946 | qualified preservation of qualified signatures and seals | **Done** |
| [`eu-reg_impl-2025-2160.md`](eu-reg_impl-2025-2160.md) | EU 2025/2160 | risk management by non-qualified trust-service providers | **Done** |
| [`eu-reg_impl-2025-2162.md`](eu-reg_impl-2025-2162.md) | EU 2025/2162 | accreditation of conformity assessment bodies and assessment of qualified trust service providers | **Done** |
| [`eu-reg_impl-2025-2392.md`](eu-reg_impl-2025-2392.md) | EU 2025/2392 | technical descriptions of important and critical products with digital elements (CRA) | **Done** |
| [`eu-reg_impl-2025-2527.md`](eu-reg_impl-2025-2527.md) | EU 2025/2527 | reference standards for qualified certificates for website authentication | **Done** |
| [`eu-reg_impl-2025-2530.md`](eu-reg_impl-2025-2530.md) | EU 2025/2530 | requirements for qualified trust service providers providing qualified trust services | **Done** |
| [`eu-reg_impl-2025-2531.md`](eu-reg_impl-2025-2531.md) | EU 2025/2531 | reference standards for qualified electronic ledgers | **Done** |
| [`eu-reg_impl-2025-2532.md`](eu-reg_impl-2025-2532.md) | EU 2025/2532 | reference standards and specifications for qualified electronic archiving services | **Done** |
| [`eu-reg_impl-2025-846.md`](eu-reg_impl-2025-846.md) | EU 2025/846 | cross-border identity matching of natural persons | **Done** |
| [`eu-reg_impl-2025-847.md`](eu-reg_impl-2025-847.md) | EU 2025/847 | reactions to security breaches of European Digital Identity Wallets | **Done** |
| [`eu-reg_impl-2025-848.md`](eu-reg_impl-2025-848.md) | EU 2025/848 | registration of wallet-relying parties (EU Digital Identity Wallet trust-mark framework) | **Done** |
| [`eu-reg_impl-2025-849.md`](eu-reg_impl-2025-849.md) | EU 2025/849 | submission of information on certified European Digital Identity Wallets | **Done** |
| [`eu-reg_impl-2026-1730.md`](eu-reg_impl-2026-1730.md) | EU 2026/1730 | wallet-relying-party registration standards update | **Done** |
| [`eu-reg_impl-2026-1731.md`](eu-reg_impl-2026-1731.md) | EU 2026/1731 | wallet standards update | **Done** |
| [`eu-reg_impl-2026-1735.md`](eu-reg_impl-2026-1735.md) | EU 2026/1735 | attribute-attestation standards update | **Done** |
| [`eu-reg_impl-2026-248.md`](eu-reg_impl-2026-248.md) | EU 2026/248 | public-sector recognition of signature and seal formats | **Done** |
| [`eu-reg_impl-2026-798.md`](eu-reg_impl-2026-798.md) | EU 2026/798 | remote onboarding of users to the European Digital Identity Wallets | **Done** |
| [`sk-2013-305.md`](sk-2013-305.md) | SK 2013/305 | Act No 305/2013 Coll. on the electronic form of exercising public authority (e-Government Act) | **Done** |
| [`sk-2016-272.md`](sk-2016-272.md) | SK 2016/272 | Act No 272/2016 Coll. on trust services | **Done** |
| [`sk-2020-78.md`](sk-2020-78.md) | SK 2020/78 | Decree 78/2020 Coll. on public-administration IT standards | **Done** |
