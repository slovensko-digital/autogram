# Commission Implementing Regulation (EU) 2015/1502 — electronic identification assurance levels

## Executive summary

This act specifies **low, substantial and high assurance levels for electronic identification means issued under notified eID schemes**. It is not a specification for certificate-chain validation, qualified-signature validation, signing formats or qualified signature creation devices. A signing application using an eID card does not thereby issue an eID means, operate a notified scheme or perform that scheme's enrolment/authentication service.

Under the [register's shared facts and scope](README.md), no direct scheme/operator role is established for the local Autogram desktop application, API, CLI or publisher. The scheme requirements below are therefore **None / Ignored**, not completed compliance tasks and not demonstrated violations. If an operational scheme service or outsourced scheme duty is later established, applicability and fulfilment must be reassessed from scheme contracts, notification, procedures and assessment evidence. Whether the application is an end-user product used in an eID/trust service remains a separate unresolved eIDAS Article 15 question; this document does not decide it.

Assessment/source access: **8 October 2026**; repository baseline: **5ca91d5c**. The complete **adopted English text**, including the Annex, was obtained from the UK government's legislation archive. Direct EUR-Lex retrieval returned empty responses; current amendment/consolidation status was not independently established. No software implementation or released-binary compliance conclusion is made.

## Source and version

| Item | Record |
| --- | --- |
| Official EU complete text | [EUR-Lex English text, CELEX 32015R1502](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:32015R1502); [ELI adopted act](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj/eng); [OJ PDF](https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=CELEX:32015R1502). Direct retrieval produced empty responses during this assessment. |
| Complete text actually read | [legislation.gov.uk, adopted English text, including Annex](https://www.legislation.gov.uk/eur/2015/1502/adopted/data.xht?view=snippet&wrap=true). Official UK government reproduction of the adopted EU text, not an EU consolidation or a statement of present UK applicability. |
| Identifier / language | Commission Implementing Regulation (EU) 2015/1502; CELEX **32015R1502**; ELI **reg_impl/2015/1502/oj**; English. |
| Adoption / publication | Adopted **8 September 2015**; **OJ L 235, 9 September 2015, pp. 7–20**. Publication identification corroborated by EUR-Lex search metadata. |
| Entry into force / application | **29 September 2015**, calculated from Article 2: twentieth day following publication. No separate application date is specified in this act. Do not confuse this with the base regulation's application/recognition timetable. |
| Version / consolidation | **As adopted**. No consolidated version was obtained; no consolidation date is claimed. |
| Relationships | Implements Article 8(3) of Regulation (EU) No 910/2014. References Regulation (EC) No 765/2008 for conformity assessment bodies. Current amendments, corrigenda and repeal status require a current EUR-Lex document-information check; the archive alone does not establish them. |
| Assessment context | Accessed **8 October 2026**; baseline **5ca91d5c**; scope and vocabulary: [README](README.md). Not legal advice or certification. |

## Legend and provision links

**Relevance:** Direct · Conditional · Indirect · None. **Status:** Done · Not done · Unknown · Ignored (context or out of the confirmed scope; never "knowingly disregarded"). Every provision row links to the official text at that unit (act anchors `#art_N`, `#anx_R`, recitals `#rct_N`); structural/preamble labels link to the [act page](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj).

## Provision-by-provision assessment

The summaries below are reading aids, not reproduced legal text. **S** in evidence cells means the register's confirmed local-app/publisher scope establishes no notified-scheme issuance, enrolment, authentication, binding or operational provider role. Any future role requires the named scheme/service evidence; source code for signing or certificate validation cannot substitute for that evidence. **A** in date cells means the adopted act's entry into force, 29 September 2015; it is not a new vendor deadline. Inherited requirements are expanded into separate rows at each assurance level. Numbering added to unnumbered text is explicitly labelled.

### Recitals (interpretative, not independent obligations)

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Recital (1)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#rct_1) | Notified schemes specify low, substantial and high assurance for their means. | Indirect | Ignored | Adopted text; eIDAS Articles 8 and 9 context. | Interpretative; A. |
| [Recital (2)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#rct_2) | Common specifications support understanding and interoperability of national assurance mappings. | Indirect | Ignored | Scheme mapping, not a signature-validation claim. | Interpretative; A. |
| [Recital (3)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#rct_3) | ISO/IEC 29115 informed the act, but EU proofing arrangements differ; Annex does not incorporate its specific content. | Indirect | Ignored | Adopted text; ISO text not inspected. | No wholesale ISO incorporation. |
| [Recital (4)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#rct_4) | Outcome-based approach takes account of STORK and international assurance concepts. | Indirect | Ignored | Adopted text; STORK specifications not inspected. | Interpretative; A. |
| [Recital (5)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#rct_5) | Authoritative sources vary in form and among Member States. | Indirect | Ignored | National source designation would be needed for a scheme. | Interpretative; A. |
| [Recital (6)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#rct_6) | Reuse of earlier proofing depends on confirmed equivalent assurance. | Indirect | Ignored | Scheme equivalence assessment, not possession of a certificate. | Interpretative; A. |
| [Recital (7)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#rct_7) | Encourage additional authentication factors, particularly from different categories. | Indirect | Ignored | Scheme authentication model would be needed. | Recommendation/context; A. |
| [Recital (8)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#rct_8) | Preserve representation rights while specifying natural/legal-person binding. | Indirect | Ignored | National representation/binding rules would be needed. | Not a new representation right. |
| [Recital (9)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#rct_9) | Recognise information-security/service-management systems and recognised standards' principles. | Indirect | Ignored | ISO/IEC 27000 and 20000 series not inspected. | Context, not full standard incorporation. |
| [Recital (10)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#rct_10) | Consider Member State assurance-level good practices. | Indirect | Ignored | National scheme practices would be needed. | Interpretative; A. |
| [Recital (11)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#rct_11) | International-standard IT security certification can verify product security. | Indirect | Ignored | Scheme-relevant certification evidence would be needed. | Not blanket product certification. |
| [Recital (12)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#rct_12) | Article 48 committee delivered no opinion within the chair's time limit. | None | Ignored | Legislative procedural context only. | Historical adoption context. |

### Articles

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Article 1(1)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_1) | Determine assurance levels of means issued under notified schemes by the Annex. | None | Ignored | S; scheme notification and means-assurance determination if role changes. | A; not certificate/signature assurance. |
| [Article 1(2), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_1) | Use Annex procedures to determine reliability and quality of the listed elements. | None | Ignored | S; scheme assurance assessment. | A; four elements below. |
| [Article 1(2)(a)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_1) | Assess enrolment under Annex 2.1 and eIDAS 8(3)(a). | None | Ignored | S; enrolment/proofing procedures. | A. |
| [Article 1(2)(b)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_1) | Assess means management under Annex 2.2 and eIDAS 8(3)(b), (f). | None | Ignored | S; means lifecycle/design evidence. | A. |
| [Article 1(2)(c)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_1) | Assess authentication under Annex 2.3 and eIDAS 8(3)(c). | None | Ignored | S; scheme authentication assessment. | A. |
| [Article 1(2)(d)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_1) | Assess management/organisation under Annex 2.4 and eIDAS 8(3)(d), (e). | None | Ignored | S; provider governance evidence. | A. |
| [Article 1(3)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_1) | Meeting a higher-level requirement presumes fulfilment of its equivalent lower-level requirement. | None | Ignored | S; documented equivalence within the scheme. | A; not equivalence between eID and signatures. |
| [Article 1(4)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_1) | Meet all elements for the claimed level unless the Annex states otherwise. | None | Ignored | S; complete assurance matrix, including alternatives. | A; no picking isolated controls. |
| [Article 2, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Entry into force on twentieth day after OJ publication. | Indirect | Ignored | Adopted text and publication date. | 29 September 2015. |
| [Article 2, unnumbered paragraph 2 (closing binding formula)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Binding in entirety and directly applicable in all Member States. | Indirect | Ignored | Legal effect, not proof of publisher's scheme role. | A. |

### Annex 1 — Applicable definitions

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex 1, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#anx_1) | Apply the following definitions for this Annex. | Indirect | Ignored | Adopted Annex scope. | A; definitional. |
| [Annex 1(1)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#anx_1) | Authoritative source: reliable accurate identity data/information/evidence, irrespective of form. | Indirect | Ignored | Scheme's national authoritative sources would be needed. | Not synonymous with a trusted certificate list. |
| [Annex 1(2), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#anx_1) | Authentication factor is confirmed bound to a person and belongs to a listed category. | Indirect | Ignored | Scheme factor-binding model would be needed. | A; definitional. |
| [Annex 1(2)(a)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#anx_1) | Possession factor requires demonstration of possession. | Indirect | Ignored | Factor design and binding evidence if scheme role arises. | A. |
| [Annex 1(2)(b)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#anx_1) | Knowledge factor requires demonstration of knowledge. | Indirect | Ignored | Factor design and binding evidence if scheme role arises. | A. |
| [Annex 1(2)(c)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#anx_1) | Inherent factor requires demonstration of a natural person's physical attribute. | Indirect | Ignored | Biometric/inherent-factor evidence if scheme role arises. | A. |
| [Annex 1(3)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#anx_1) | Dynamic authentication generates on-demand, changing proof of control/possession of identification data. | Indirect | Ignored | Scheme authentication protocol, not a static signature check. | A. |
| [Annex 1(4)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#anx_1) | ISMS: processes/procedures managing information-security risks to acceptable levels. | Indirect | Ignored | Provider ISMS evidence if scheme role arises. | A. |

### Annex 2 — Scope of technical specifications

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [Annex 2, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#anx_2) | Use Annex elements to apply eIDAS Article 8 requirements/criteria to scheme-issued eID means. | None | Ignored | S; scheme assurance determination. | A; headings 2.1–2.4 organise the following duties. |

### Annex 2.1.1 — Application and registration

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.1.1 Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Ensure applicant knows means-use terms and conditions. | None | Ignored | S; scheme applicant notices/acknowledgements. | A. |
| [2.1.1 Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Ensure applicant knows recommended security precautions. | None | Ignored | S; applicant security instructions. | A. |
| [2.1.1 Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Collect identity data needed for proofing and verification. | None | Ignored | S; enrolment data/proofing specification. | A. |
| [2.1.1 Substantial, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Applicant awareness of means-use terms and conditions. | None | Ignored | S; applicant notices/acknowledgements. | Same as low. |
| [2.1.1 Substantial, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Applicant awareness of recommended security precautions. | None | Ignored | S; applicant security instructions. | Same as low. |
| [2.1.1 Substantial, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Collect necessary identity data. | None | Ignored | S; enrolment data/proofing specification. | Same as low. |
| [2.1.1 High, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Applicant awareness of means-use terms and conditions. | None | Ignored | S; applicant notices/acknowledgements. | Same as low. |
| [2.1.1 High, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Applicant awareness of recommended security precautions. | None | Ignored | S; applicant security instructions. | Same as low. |
| [2.1.1 High, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Collect necessary identity data. | None | Ignored | S; enrolment data/proofing specification. | Same as low. |

### Annex 2.1.2 — Natural-person identity proofing and verification

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.1.2 Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Assume possession of Member-State-recognised evidence representing claimed identity. | None | Ignored | S; accepted evidence and possession procedures. | A. |
| [2.1.2 Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Evidence assumed genuine or existent by authoritative source and appears valid. | None | Ignored | S; evidence/source validity checks. | A. |
| [2.1.2 Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Authoritative source knows identity exists; claimant may be assumed the same person. | None | Ignored | S; identity-existence and claimant linkage evidence. | A. |
| [2.1.2 Substantial, introductory/inheritance rule](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Meet Low 1–3 and one of alternatives 1–4. | None | Ignored | S; selected proofing pathway and assessment. | Alternatives, not four cumulative routes. |
| [2.1.2 Substantial, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Possession assumption for recognised identity evidence. | None | Ignored | S; evidence/possession procedures. | Inherited. |
| [2.1.2 Substantial, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Evidence genuineness/existence and apparent validity. | None | Ignored | S; source/evidence checks. | Inherited. |
| [2.1.2 Substantial, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Existing identity and assumed claimant linkage. | None | Ignored | S; source/claimant linkage. | Inherited. |
| [2.1.2 Substantial 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Verify possession; check recognised evidence genuine, or authoritative existence/real person; minimise impersonation risks including lost/stolen/suspended/revoked/expired evidence. | None | Ignored | S; documented checks and evidence-risk controls. | Alternative 1. |
| [2.1.2 Substantial 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Identity document presented in issuing Member State appears to relate to presenter; minimise identity mismatch/document-status risks. | None | Ignored | S; registration location, document/person checks and risk controls. | Alternative 2. |
| [2.1.2 Substantial 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reuse earlier same-Member-State public/private proofing for another purpose only with equivalent assurance confirmed by prescribed conformity assessment or equivalent body. | None | Ignored | S; earlier procedure and equivalence report. | Alternative 3; no automatic reuse. |
| [2.1.2 Substantial 4, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reuse valid notified substantial/high means without repeating proofing, considering identity-data-change risks. | None | Ignored | S; valid prior means/notification and change-risk assessment. | Alternative 4. |
| [2.1.2 Substantial 4, unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | For non-notified basis means, prescribed assessment/equivalent body must confirm substantial/high assurance. | None | Ignored | S; prior-means assurance report. | Qualification of alternative 4. |
| [2.1.2 High, introductory rule](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Meet route 1 or route 2 below. | None | Ignored | S; documented choice and complete route assessment. | Two alternative routes. |
| [2.1.2 High 1, introductory/inheritance rule](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Meet substantial plus one of alternatives (a)–(c). | None | Ignored | S; substantial evidence plus selected high route. | Not cumulative (a)–(c). |
| [2.1.2 High 1, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Possession assumption for recognised identity evidence. | None | Ignored | S; recognised evidence and possession checks. | Through substantial. |
| [2.1.2 High 1, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Evidence genuineness/existence and apparent validity. | None | Ignored | S; source/evidence checks. | Through substantial. |
| [2.1.2 High 1, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Existing identity and assumed claimant linkage. | None | Ignored | S; source/claimant linkage. | Through substantial. |
| [2.1.2 High 1, inherited Substantial 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Verified possession, genuine/existing recognised evidence and impersonation-risk minimisation. | None | Ignored | S; proofing checks and risk controls. | One of inherited substantial alternatives. |
| [2.1.2 High 1, inherited Substantial 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | In-issuing-State document presentation, person linkage and document-risk minimisation. | None | Ignored | S; document/person checks and risk controls. | One of inherited substantial alternatives. |
| [2.1.2 High 1, inherited Substantial 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reuse earlier equivalent same-State proofing with assessment-body confirmation. | None | Ignored | S; equivalence report. | One of inherited substantial alternatives. |
| [2.1.2 High 1, inherited Substantial 4 paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reuse valid notified substantial/high means, considering data-change risks. | None | Ignored | S; prior means/notification and change-risk assessment. | One of inherited substantial alternatives. |
| [2.1.2 High 1, inherited Substantial 4 paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Non-notified prior means requires confirmed substantial/high assurance. | None | Ignored | S; assurance report. | Qualification of inherited alternative. |
| [2.1.2 High 1(a)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Verify possession of recognised photo/biometric evidence; check authoritative validity and identify claimant by physical-characteristic comparison with authoritative source. | None | Ignored | S; evidence validity and physical comparison records. | Alternative (a); both checks. |
| [2.1.2 High 1(b)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reuse equivalent earlier same-State proofing only with assessment-body confirmation and proof results remain valid. | None | Ignored | S; equivalence report and continuing-validity evidence. | Alternative (b). |
| [2.1.2 High 1(c), unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reuse valid notified high means without repeating proofing, considering data-change risks. | None | Ignored | S; prior high means/notification and change-risk assessment. | Alternative (c). |
| [2.1.2 High 1(c), unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Non-notified basis requires confirmed high assurance and steps demonstrating prior issuance results remain valid. | None | Ignored | S; high-assurance report and continuing-validity checks. | Preserve the adopted wording's reference to prior notified issuance; do not silently correct it. |
| [2.1.2 High 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Without recognised photo/biometric evidence, apply the same national procedures used to obtain it in registration entity's Member State. | None | Ignored | S; national issuance procedures and proof of their application. | Alternative to High 1, not an extra duty on that route. |

### Annex 2.1.3 — Legal-person identity proofing and verification

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.1.3 Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Demonstrate claimed identity using Member-State-recognised evidence. | None | Ignored | S; accepted legal-person evidence. | A. |
| [2.1.3 Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Evidence appears valid, assumed genuine or existent in authoritative source; voluntary source inclusion is regulated by arrangement. | None | Ignored | S; evidence/source checks and inclusion arrangement. | A. |
| [2.1.3 Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Authoritative source does not know a status preventing action as the legal person. | None | Ignored | S; legal-person status checks. | A. |
| [2.1.3 Substantial, introductory/inheritance rule](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Meet low plus one of alternatives 1–3. | None | Ignored | S; chosen proofing route assessment. | Alternatives. |
| [2.1.3 Substantial, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Demonstrate identity with recognised evidence. | None | Ignored | S; recognised evidence. | Inherited. |
| [2.1.3 Substantial, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Apparent validity and assumed genuineness/source existence with applicable inclusion arrangement. | None | Ignored | S; source/evidence checks. | Inherited. |
| [2.1.3 Substantial, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | No known disqualifying legal-person status. | None | Ignored | S; authoritative status checks. | Inherited. |
| [2.1.3 Substantial 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Recognised evidence includes name, legal form and applicable registration number; verify genuineness or existence in sector-required authoritative source and minimise identity/document-status risks. | None | Ignored | S; registry/evidence and anti-impersonation controls. | Alternative 1. |
| [2.1.3 Substantial 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reuse earlier same-State equivalent proofing for another purpose with prescribed assessment/equivalent-body confirmation. | None | Ignored | S; earlier procedures and equivalence report. | Alternative 2. |
| [2.1.3 Substantial 3, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reuse valid notified substantial/high means without repeating proofing. | None | Ignored | S; prior-means validity/notification. | Alternative 3. |
| [2.1.3 Substantial 3, unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Non-notified basis requires confirmed substantial/high assurance by prescribed assessment/equivalent body. | None | Ignored | S; assurance assessment report. | Qualification of alternative 3. |
| [2.1.3 High, introductory/inheritance rule](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Meet substantial plus one of alternatives 1–3. | None | Ignored | S; selected routes and complete assurance assessment. | Alternatives. |
| [2.1.3 High, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Demonstrate identity with recognised evidence. | None | Ignored | S; recognised evidence. | Through substantial. |
| [2.1.3 High, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Apparent validity and assumed genuineness/source existence with applicable inclusion arrangement. | None | Ignored | S; source/evidence checks. | Through substantial. |
| [2.1.3 High, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | No known disqualifying legal-person status. | None | Ignored | S; authoritative status checks. | Through substantial. |
| [2.1.3 High, inherited Substantial 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Verify recognised evidence, required identity fields/source and minimise identity/document-status risks. | None | Ignored | S; registry/evidence and risk controls. | One inherited substantial alternative. |
| [2.1.3 High, inherited Substantial 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reuse earlier equivalent same-State procedures with assessment-body confirmation. | None | Ignored | S; equivalence report. | One inherited substantial alternative. |
| [2.1.3 High, inherited Substantial 3 paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reuse valid notified substantial/high means. | None | Ignored | S; prior-means validity/notification. | One inherited substantial alternative. |
| [2.1.3 High, inherited Substantial 3 paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Non-notified basis requires confirmed substantial/high assurance. | None | Ignored | S; assurance report. | Qualification of inherited alternative. |
| [2.1.3 High 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Recognised evidence includes name, legal form and at least one national unique identifier; check validity against authoritative source. | None | Ignored | S; identity fields and authoritative validity checks. | Alternative 1. |
| [2.1.3 High 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reuse equivalent earlier same-State proofing with assessment-body confirmation and steps demonstrating continuing validity. | None | Ignored | S; equivalence report and continuing-validity evidence. | Alternative 2. |
| [2.1.3 High 3, unnumbered paragraph 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reuse valid notified high means without repeating proofing. | None | Ignored | S; prior high-means validity/notification. | Alternative 3. |
| [2.1.3 High 3, unnumbered paragraph 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Non-notified basis requires confirmed high assurance and proof prior issuance results remain valid. | None | Ignored | S; high-assurance report and continuing-validity checks. | Adopted wording refers to previous issuance of notified means. |

### Annex 2.1.4 — Binding natural- and legal-person means

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.1.4, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Apply binding conditions where natural/legal-person means are bound. | None | Ignored | S; scheme binding function/role evidence. | Where applicable; not ordinary document signing. |
| [2.1.4(1)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Allow suspension/revocation and administer binding lifecycle through nationally recognised procedures. | None | Ignored | S; national binding lifecycle procedures. | A. |
| [2.1.4(2)](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Delegation may follow nationally recognised procedures; delegator remains accountable. | None | Ignored | S; delegation procedures and accountability records. | Permission plus accountability, not mandatory delegation. |
| [2.1.4(3), introductory text](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Perform binding according to the assurance-level table. | None | Ignored | S; binding assurance matrix. | A. |
| [2.1.4(3) Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Verify natural-person proofing at low or higher. | None | Ignored | S; proofing-level evidence. | A. |
| [2.1.4(3) Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Establish binding by nationally recognised procedures. | None | Ignored | S; national procedure/binding record. | A. |
| [2.1.4(3) Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | No authoritative-source-known status prevents natural person acting for legal person. | None | Ignored | S; representation status checks. | A. |
| [2.1.4(3) Substantial, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | No known disqualifying representative status. | None | Ignored | S; authoritative representation status. | Only Low 3 expressly inherited. |
| [2.1.4(3) Substantial 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Verify natural-person proofing at substantial/high. | None | Ignored | S; proofing-level evidence. | A. |
| [2.1.4(3) Substantial 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | National procedures establish binding registered in authoritative source. | None | Ignored | S; procedure and authoritative registration. | A. |
| [2.1.4(3) Substantial 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Verify binding using authoritative-source information. | None | Ignored | S; authoritative verification record. | A. |
| [2.1.4(3) High, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | No known disqualifying representative status. | None | Ignored | S; authoritative representation status. | Express inheritance. |
| [2.1.4(3) High, inherited Substantial 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Establish binding by national procedures and register in authoritative source. | None | Ignored | S; procedure and registration. | Express inheritance. |
| [2.1.4(3) High 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Verify natural-person proofing at high. | None | Ignored | S; high proofing evidence. | A. |
| [2.1.4(3) High 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Verify binding by national unique legal-person identifier and authoritative information uniquely identifying natural person. | None | Ignored | S; both identifiers/source verification. | Both limbs required. |

### Annex 2.2.1 — Means characteristics and design

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.2.1 Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Means uses at least one authentication factor. | None | Ignored | S; scheme means/factor specification. | A. |
| [2.2.1 Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Issuer takes reasonable steps to check use only under owner's control/possession. | None | Ignored | S; issuer design/control assessment. | A. |
| [2.2.1 Substantial 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Means uses at least two factors from different categories. | None | Ignored | S; factor-category and binding assessment. | Not simply two PIN prompts. |
| [2.2.1 Substantial 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Design permits assumption of use only under owner's control/possession. | None | Ignored | S; means design assessment. | A. |
| [2.2.1 High, inherited Substantial 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | At least two factors from different categories. | None | Ignored | S; factor-category and binding assessment. | Inherited. |
| [2.2.1 High, inherited Substantial 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Assumed use only under owner's control/possession. | None | Ignored | S; means design assessment. | Inherited. |
| [2.2.1 High 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Protect against duplication, tampering and high-attack-potential attackers. | None | Ignored | S; scheme means security evaluation. | A; not inferred from smart-card use. |
| [2.2.1 High 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Owner can reliably protect means against others' use. | None | Ignored | S; protection/usability security assessment. | A. |

### Annex 2.2.2 — Issuance, delivery and activation

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.2.2 Low, unnumbered requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Deliver issued means by mechanism assumed to reach only intended person. | None | Ignored | S; delivery procedures/evidence. | A. |
| [2.2.2 Substantial, unnumbered requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Deliver issued means by mechanism assumed to place it only in owner's possession. | None | Ignored | S; possession-focused delivery assessment. | A. |
| [2.2.2 High, unnumbered requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Activation verifies delivery only into owner's possession. | None | Ignored | S; activation/delivery verification. | A. |

### Annex 2.2.3 — Suspension, revocation and reactivation

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.2.3 Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Timely, effective suspension and/or revocation of means is possible. | None | Ignored | S; means lifecycle service/process tests. | Not certificate OCSP/CRL validation. |
| [2.2.3 Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Measures prevent unauthorised suspension, revocation and/or reactivation. | None | Ignored | S; lifecycle authorisation controls. | A. |
| [2.2.3 Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reactivate only if pre-suspension/revocation assurance requirements remain met. | None | Ignored | S; reactivation assurance checks. | A. |
| [2.2.3 Substantial, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Timely, effective suspension/revocation. | None | Ignored | S; lifecycle service/process tests. | Same as low. |
| [2.2.3 Substantial, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Prevent unauthorised lifecycle changes. | None | Ignored | S; lifecycle authorisation controls. | Same as low. |
| [2.2.3 Substantial, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Preserve assurance before reactivation. | None | Ignored | S; reactivation checks. | Same as low. |
| [2.2.3 High, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Timely, effective suspension/revocation. | None | Ignored | S; lifecycle service/process tests. | Same as low. |
| [2.2.3 High, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Prevent unauthorised lifecycle changes. | None | Ignored | S; lifecycle authorisation controls. | Same as low. |
| [2.2.3 High, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Preserve assurance before reactivation. | None | Ignored | S; reactivation checks. | Same as low. |

### Annex 2.2.4 — Renewal and replacement

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.2.4 Low, unnumbered requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Considering identity-data-change risks, meet initial proofing assurance or base renewal/replacement on valid same/higher-level means. | None | Ignored | S; renewal/replacement process and change-risk checks. | Alternative bases within one requirement. |
| [2.2.4 Substantial, inherited Low requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Same initial-proofing or valid same/higher-level basis, considering change risks. | None | Ignored | S; renewal/replacement assurance evidence. | Same as low. |
| [2.2.4 High, inherited Low requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Same initial-proofing or valid same/higher-level basis, considering change risks. | None | Ignored | S; renewal/replacement assurance evidence. | Inherited. |
| [2.2.4 High, additional unnumbered requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | If based on valid means, verify identity data with authoritative source. | None | Ignored | S; authoritative data verification. | Additional conditional check within high. |

### Annex 2.3 — Authentication; 2.3.1 mechanism

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.3, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Address authentication-mechanism threats with controls commensurate to each level's risks. | None | Ignored | S; scheme authentication threat/risk model. | A. |
| [2.3.1, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Table concerns mechanism by which persons confirm identity to relying party using means. | None | Ignored | S; relying-party authentication flow/role. | Scope; not document-signature validation. |
| [2.3.1 Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reliably verify means and validity before releasing person identification data. | None | Ignored | S; verification/release protocol evidence. | A. |
| [2.3.1 Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | If identification data stored in mechanism, secure against loss/compromise, including offline analysis. | None | Ignored | S; storage/data-protection threat assessment. | Applies where stored. |
| [2.3.1 Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Verification controls make guessing, eavesdropping, replay and communication manipulation highly unlikely to subvert mechanism at enhanced-basic attack potential. | None | Ignored | S; protocol security evaluation at stated attack potential. | A. |
| [2.3.1 Substantial, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reliable means/validity verification before data release. | None | Ignored | S; verification/release protocol. | Inherited. |
| [2.3.1 Substantial, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Protect stored data against loss/compromise/offline analysis. | None | Ignored | S; storage security assessment. | Inherited; where stored. |
| [2.3.1 Substantial, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Enhanced-basic attack-potential resistance. | None | Ignored | S; protocol attack assessment. | Inherited. |
| [2.3.1 Substantial 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Use dynamic authentication for reliable means/validity verification before data release. | None | Ignored | S; dynamic-authentication protocol assessment. | A. |
| [2.3.1 Substantial 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Controls resist listed subversion attacks at moderate attack potential. | None | Ignored | S; moderate-potential attack evaluation. | A. |
| [2.3.1 High, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Reliable means/validity verification before data release. | None | Ignored | S; verification/release protocol. | Through substantial. |
| [2.3.1 High, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Protect stored data against loss/compromise/offline analysis. | None | Ignored | S; storage security assessment. | Through substantial; where stored. |
| [2.3.1 High, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Enhanced-basic attack-potential resistance. | None | Ignored | S; protocol attack evaluation. | Through substantial. |
| [2.3.1 High, inherited Substantial 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Dynamic verification before data release. | None | Ignored | S; dynamic-authentication assessment. | Inherited. |
| [2.3.1 High, inherited Substantial 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Moderate attack-potential resistance. | None | Ignored | S; moderate-potential attack evaluation. | Inherited. |
| [2.3.1 High, additional unnumbered requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Controls resist listed subversion attacks at high attack potential. | None | Ignored | S; high-potential protocol security evaluation. | Not inferred from signature algorithm strength alone. |

### Annex 2.4 — Management and organisation

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.4, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Cross-border eID service participants (providers) document security management, policies, risk approaches and recognised controls to assure national scheme governance bodies; requirements proportionate to level's risks. | None | Ignored | S; operational provider/outsourcing contract and governance/ISMS documents. | Broad provider scope if such service established; not automatic software-publisher classification. |

### Annex 2.4.1 — General provisions

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.4.1 Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Operational providers are public authority or nationally recognised legal entity, established and operational in relevant parts. | None | Ignored | S; provider legal status and operational organisation. | A. |
| [2.4.1 Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Comply with service-related law, including data collection, proofing, retention and duration. | None | Ignored | S; provider legal obligations/retention assessment. | Not finding compliance with other laws. |
| [2.4.1 Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Demonstrate ability to bear damages liability and sufficient resources for continued service. | None | Ignored | S; financial/liability capacity evidence. | A. |
| [2.4.1 Low 4](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Retain responsibility for outsourced commitments and scheme-policy compliance as if performing duties. | None | Ignored | S; outsourcing agreements and oversight evidence. | A. |
| [2.4.1 Low 5](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Schemes not constituted by national law have effective termination plan covering discontinuation/continuation, notifications and protected retention/destruction of records under scheme policy. | None | Ignored | S; scheme constitution and termination plan. | Applies to specified schemes. |
| [2.4.1 Substantial, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Established, recognised and operational provider entity. | None | Ignored | S; provider legal/operational records. | Same as low. |
| [2.4.1 Substantial, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Comply with applicable service/data/proofing/retention law. | None | Ignored | S; legal/retention assessment. | Same as low. |
| [2.4.1 Substantial, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Liability and continued-operation financial capacity. | None | Ignored | S; financial/liability evidence. | Same as low. |
| [2.4.1 Substantial, inherited Low 4](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Responsibility for outsourced duties and scheme-policy compliance. | None | Ignored | S; outsourcing oversight. | Same as low. |
| [2.4.1 Substantial, inherited Low 5](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Effective termination/continuation, notification and record plan for non-statutory schemes. | None | Ignored | S; constitution and termination plan. | Same as low. |
| [2.4.1 High, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Established, recognised and operational provider entity. | None | Ignored | S; provider legal/operational records. | Same as low. |
| [2.4.1 High, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Comply with applicable service/data/proofing/retention law. | None | Ignored | S; legal/retention assessment. | Same as low. |
| [2.4.1 High, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Liability and continued-operation financial capacity. | None | Ignored | S; financial/liability evidence. | Same as low. |
| [2.4.1 High, inherited Low 4](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Responsibility for outsourced duties and scheme-policy compliance. | None | Ignored | S; outsourcing oversight. | Same as low. |
| [2.4.1 High, inherited Low 5](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Effective termination/continuation, notification and record plan for non-statutory schemes. | None | Ignored | S; constitution and termination plan. | Same as low. |

### Annex 2.4.2 — Published notices and user information

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.4.2 Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Publish service definition including terms, conditions, fees, use limitations and privacy policy. | None | Ignored | S; scheme service definition/privacy policy. | A. |
| [2.4.2 Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Policies/procedures ensure timely reliable notice of service/terms/privacy-policy changes. | None | Ignored | S; change-notification procedures/evidence. | A. |
| [2.4.2 Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Policies/procedures provide full correct responses to information requests. | None | Ignored | S; information-request policy/records. | A. |
| [2.4.2 Substantial, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Published complete service definition/privacy policy. | None | Ignored | S; service definition/privacy policy. | Same as low. |
| [2.4.2 Substantial, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Timely reliable notice of service/terms/privacy changes. | None | Ignored | S; change-notification evidence. | Same as low. |
| [2.4.2 Substantial, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Full correct information-request responses. | None | Ignored | S; request policy/records. | Same as low. |
| [2.4.2 High, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Published complete service definition/privacy policy. | None | Ignored | S; service definition/privacy policy. | Same as low. |
| [2.4.2 High, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Timely reliable notice of service/terms/privacy changes. | None | Ignored | S; change-notification evidence. | Same as low. |
| [2.4.2 High, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Full correct information-request responses. | None | Ignored | S; request policy/records. | Same as low. |

### Annex 2.4.3 — Information security management

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.4.3 Low, unnumbered requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Effective ISMS manages and controls information-security risks. | None | Ignored | S; provider ISMS and effectiveness assessment. | A. |
| [2.4.3 Substantial, inherited Low requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Effective risk-management/control ISMS. | None | Ignored | S; ISMS effectiveness evidence. | Inherited. |
| [2.4.3 Substantial, additional unnumbered requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | ISMS adheres to proven standards or principles for security-risk management/control. | None | Ignored | S; standards/principles mapping and assessment. | No specific mandatory ISO certificate stated here. |
| [2.4.3 High, inherited Low requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Effective risk-management/control ISMS. | None | Ignored | S; ISMS effectiveness evidence. | Same as substantial. |
| [2.4.3 High, inherited Substantial additional requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | ISMS adheres to proven security-risk standards/principles. | None | Ignored | S; mapping and assessment. | Same as substantial. |

### Annex 2.4.4 — Record keeping

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.4.4 Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Effective record management maintains relevant information with applicable law and data-protection/retention good practice. | None | Ignored | S; provider record policy/system and legal assessment. | A. |
| [2.4.4 Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Where nationally permitted, retain/protect records for audit, breach investigation and retention needs, then securely destroy. | None | Ignored | S; lawful retention schedule, access and destruction evidence. | No fixed EU retention period specified. |
| [2.4.4 Substantial, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Effective lawful/good-practice record management. | None | Ignored | S; record policy/system assessment. | Same as low. |
| [2.4.4 Substantial, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Lawfully retain/protect as required, then securely destroy. | None | Ignored | S; retention/destruction evidence. | Same as low. |
| [2.4.4 High, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Effective lawful/good-practice record management. | None | Ignored | S; record policy/system assessment. | Same as low. |
| [2.4.4 High, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Lawfully retain/protect as required, then securely destroy. | None | Ignored | S; retention/destruction evidence. | Same as low. |

### Annex 2.4.5 — Facilities and staff

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.4.5, unnumbered introductory paragraph](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Table covers facilities, staff and applicable subcontractors undertaking regulated duties; proportionate to assurance-level risk. | None | Ignored | S; provider/subcontractor scope and risk assessment. | A. |
| [2.4.5 Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Procedures ensure role-appropriate training, qualification and experience. | None | Ignored | S; competence procedures/records. | A. |
| [2.4.5 Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Sufficient staff/subcontractors resource and operate service under policies. | None | Ignored | S; resourcing and operational capacity evidence. | A. |
| [2.4.5 Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Continuously monitor/protect facilities from environmental damage, unauthorised access and other security-impacting factors. | None | Ignored | S; facility monitoring/protection assessment. | A. |
| [2.4.5 Low 4](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Restrict sensitive-information storage/processing area access to authorised staff/subcontractors. | None | Ignored | S; physical access controls/records. | A. |
| [2.4.5 Substantial, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Role-appropriate competence procedures. | None | Ignored | S; competence records. | Same as low. |
| [2.4.5 Substantial, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Adequate operational staffing/resources. | None | Ignored | S; capacity evidence. | Same as low. |
| [2.4.5 Substantial, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Continuous facility monitoring/protection. | None | Ignored | S; facility assessment. | Same as low. |
| [2.4.5 Substantial, inherited Low 4](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Authorised-only sensitive-area access. | None | Ignored | S; access controls/records. | Same as low. |
| [2.4.5 High, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Role-appropriate competence procedures. | None | Ignored | S; competence records. | Same as low. |
| [2.4.5 High, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Adequate operational staffing/resources. | None | Ignored | S; capacity evidence. | Same as low. |
| [2.4.5 High, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Continuous facility monitoring/protection. | None | Ignored | S; facility assessment. | Same as low. |
| [2.4.5 High, inherited Low 4](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Authorised-only sensitive-area access. | None | Ignored | S; access controls/records. | Same as low. |

### Annex 2.4.6 — Technical controls

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.4.6 Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Proportionate technical controls manage service security risks and protect information confidentiality, integrity and availability. | None | Ignored | S; service risk/control assessment. | A. |
| [2.4.6 Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Protect personal/sensitive-data communication channels against eavesdropping, manipulation and replay. | None | Ignored | S; channel/protocol security assessment. | Not blanket localhost API duty under this act. |
| [2.4.6 Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Restrict issuing/authentication cryptographic material to necessary roles/apps and never persistently store it in plaintext. | None | Ignored | S; scheme key management/storage/access evidence. | Where such material used. |
| [2.4.6 Low 4](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Procedures maintain security over time and respond to changing risks, incidents and breaches. | None | Ignored | S; security maintenance/incident procedures and exercises. | A. |
| [2.4.6 Low 5](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Safely store, transport and dispose of sensitive-information media. | None | Ignored | S; media handling/destruction evidence. | A. |
| [2.4.6 Substantial, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Risk-proportionate confidentiality/integrity/availability controls. | None | Ignored | S; service controls assessment. | Inherited. |
| [2.4.6 Substantial, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Anti-eavesdropping/manipulation/replay channels. | None | Ignored | S; channel security assessment. | Inherited. |
| [2.4.6 Substantial, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Restricted key access; no persistent plaintext storage. | None | Ignored | S; key management/storage evidence. | Inherited; where used. |
| [2.4.6 Substantial, inherited Low 4](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Ongoing security/risk/incident response procedures. | None | Ignored | S; maintenance/response evidence. | Inherited. |
| [2.4.6 Substantial, inherited Low 5](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Safe sensitive-media lifecycle. | None | Ignored | S; media handling evidence. | Inherited. |
| [2.4.6 Substantial, additional unnumbered requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Protect issuing/authentication cryptographic material against tampering. | None | Ignored | S; key tamper-protection evaluation. | Where used. |
| [2.4.6 High, inherited Low 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Risk-proportionate confidentiality/integrity/availability controls. | None | Ignored | S; service controls assessment. | Same as substantial. |
| [2.4.6 High, inherited Low 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Anti-eavesdropping/manipulation/replay channels. | None | Ignored | S; channel security assessment. | Same as substantial. |
| [2.4.6 High, inherited Low 3](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Restricted key access; no persistent plaintext storage. | None | Ignored | S; key management/storage evidence. | Same as substantial; where used. |
| [2.4.6 High, inherited Low 4](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Ongoing security/risk/incident response procedures. | None | Ignored | S; maintenance/response evidence. | Same as substantial. |
| [2.4.6 High, inherited Low 5](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Safe sensitive-media lifecycle. | None | Ignored | S; media handling evidence. | Same as substantial. |
| [2.4.6 High, inherited Substantial additional requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Protect issuing/authentication keys against tampering. | None | Ignored | S; key tamper-protection evaluation. | Same as substantial; where used. |

### Annex 2.4.7 — Compliance and audit

| Provision | Requirement / addressee | Relevance | Status | Evidence / source needed | Dates / notes |
| --- | --- | --- | --- | --- | --- |
| [2.4.7 Low, unnumbered requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Periodic internal audits cover all service-supply-relevant parts for policy compliance. | None | Ignored | S; internal audit plan/reports and service scope. | A; no interval specified. |
| [2.4.7 Substantial, unnumbered requirement](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Periodic independent internal or external audits cover all relevant service parts for policy compliance. | None | Ignored | S; independent audit reports/scope. | A. |
| [2.4.7 High 1](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Periodic independent external audits cover all relevant service parts for policy compliance. | None | Ignored | S; external audit reports/scope. | A. |
| [2.4.7 High 2](https://eur-lex.europa.eu/eli/reg_impl/2015/1502/oj#art_2) | Government-body-managed scheme is audited under national law. | None | Ignored | S; scheme management status, national audit law/reports. | Specific government-scheme condition. |

## Coverage and limitations

- **Obtained and checked:** the complete adopted English text from legislation.gov.uk, read from the preamble through the final Annex table and footnotes. Coverage was checked against that retrieved text's headings, numbering, assurance-level cells, explicit inheritance and alternatives, not inferred from an older report.
- **Enumerated:** all **12 recitals**; Article 1 paragraphs **1–4**, including the paragraph 2 introduction and **(a)–(d)**; both unnumbered Article 2/closing operative paragraphs; Annex 1 introductory text and definitions **(1)–(4)** including **(2)(a)–(c)**; Annex 2 introductory text; all **16 requirement subsections**: 2.1.1–2.1.4, 2.2.1–2.2.4, 2.3.1, 2.4.1–2.4.7. Each level's numbered/unnumbered requirement is separately identifiable, including inherited requirements, natural-person high route **1(a)–(c)** and **2**, legal-person alternatives, binding **(1)–(3)** and section introductions with operative scope/risk language. Unnumbered second paragraphs within prior-means alternatives have separate rows. Parent/inheritance rows describe routing, not extra technical duties.
- **Source footnotes:** the base regulation's OJ citation and Regulation (EC) No 765/2008 citation were obtained; they are source references, not additional Annex requirements. The archive renderer places the latter note near 2.1.2; it has not been treated as a proofing duty. Formal preamble treaty/legal-basis citations, adoption/signature formula and structural headings add no separately assessable compliance requirement.
- **Limits:** no adopted Annex portion was unavailable or omitted from requirement assessment. Direct EU authoritative-text retrieval and current consolidation/amendment/corrigendum/repeal verification remain unavailable in this run; completeness is therefore claimed **only for the adopted text retrieved**, not every subsequent legal change. The linked OJ/EUR-Lex act and any corrigenda remain authoritative over the archive reproduction and these summaries.
- **Standards:** no ISO/IEC 29115, 27000/20000-series, STORK specification or ETSI standard was inspected. Recitals do not silently incorporate their entire contents. No certification or complete technical conformity is inferred.
- **Evidence limits:** no new source-code, organisational-contract, scheme-notification or audit examination was performed. Scope classification uses the shared register facts, not an invented repository implementation claim. For a future scheme role, obtain the scheme notification/assurance mapping, national authoritative-source and representation rules, enrolment/issuance/lifecycle/authentication specifications, equivalence assessments, operational/outsourcing contracts, ISMS, finance/liability and termination records, user notices, retention and staff/facility/key-control evidence and level-appropriate audit reports. Until then, the table's Ignored statuses mean scoped out, not that a known applicable duty is disregarded.
