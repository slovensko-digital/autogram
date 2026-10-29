Release robí slovensko.digital, ale postup aj checklist sú verejné, aby mohol každý contributor samostatne
pretestovať zmeny pred tým, než urobí PR.

# Vydanie novej verzie

Číslo novej verzie a `CHANGELOG.md` pripravuje [release-please](https://github.com/googleapis/release-please) z názvov
zmergovaných PR: `fix:` zvýši patch, `feat:` minor a `!` major verziu, pravidlá sú v
[DEVELOPER.md](DEVELOPER.md#pull-request-titles-and-versions). Po každom merge do `main` release-please aktualizuje
release PR **chore: release X.Y.Z** so zvýšenou verziou v `pom.xml` a novou časťou `CHANGELOG.md`. Release PR ručne
neupravujte, pri ďalšom merge do `main` ho release-please prepíše.

## 1. Príprava

- [ ] V release PR skontrolovať číslo verzie a zmeny v `CHANGELOG.md`. Iné číslo sa dá vynútiť riadkom
  `Release-As: 3.0.0` v popise squash commitu niektorého PR.
- [ ] Skontrolovať nový kód od posledného releasu: či nepribudol škodlivý kód, exfiltrácia secrets cez GitHub Actions,
  či je nová core funkcionalita pokrytá testami.
- [ ] V samostatnom PR (`docs: …`) premenovať v `docs/docs/sk/changelog.md` a `docs/docs/en/changelog.md` sekciu
  **Pripravuje sa** / **Coming next** na číslo verzie z release PR a doplniť dátum a ID, napr. `{#v2-9-0}`, pozri
  [docs/DEVELOPER.md](docs/DEVELOPER.md#changelog).

## 2. Balíčky

- [ ] Mergnúť release PR. Ak na ňom nebežali testy, lebo ho release-please otvoril bez `RELEASE_PLEASE_TOKEN`, PR
  zatvoriť a znova otvoriť.
- [ ] release-please vytvorí tag `vX.Y.Z` a draft release s poznámkami z `CHANGELOG.md`. Workflow **Release** spustí
  **Package All**, ktorý čaká na schválenie prostredia `packaging`.
- [ ] Schváliť nasadenie (*Review deployments*). Schváliť môže len iný člen Autogram Release Team, než ten, kto
  mergol release PR.
- [ ] Po schválení sa inštalátory pre Windows podpíšu, pre macOS podpíšu a notarizujú a všetky sa nahrajú do draftu,
  ktorý sa označí ako pre-release. Ak niektorý job zlyhá, spustiť ho znova (*Re-run failed jobs*) alebo spustiť
  **Package All** ručne s tagom. Balíčky nahrá iba do draftu, publikovaný release odmietne.

## 3. Testovanie

- [ ] Doplniť popis releasu: zvýraznenia, pri bezpečnostnej oprave upozornenie, vid. existujúce
  [Releases](https://github.com/slovensko-digital/autogram/releases).
- [ ] Publikovať draft ako **pre-release**. Workflow **Pre-release** vytvorí issue s
  [checklistom](.github/ISSUE_TEMPLATE/release-checklist.md).
- [ ] Otestovať podľa checklistu.

## 4. Vydanie

- [ ] Nahrať `autogram-X.Y.Z-windows-store.msix` do Microsoft Store cez Partner Center a odoslať na certifikáciu.
- [ ] V releasi zrušiť **Set as a pre-release** a zaškrtnúť **Set as the latest release**. Až vtedy novú verziu
  ponúkne okno aktualizácie (verzie z `.msi`, macOS a Linux) a dokumentácia sa nasadí s ňou ako aktuálnou verziou.

# Nastavenie repozitára

Pri zavedení release-please:

- Settings → General → Pull Requests: povoliť iba **Allow squash merging** s predvolenou správou **Pull request title**
  alebo **Pull request title and description**.
- Settings → Actions → General → Workflow permissions: zapnúť **Allow GitHub Actions to create and approve pull
  requests**, inak release-please s `GITHUB_TOKEN` nevytvorí release PR.
- Voliteľne secret `RELEASE_PLEASE_TOKEN`: fine-grained token alebo GitHub App s právami *Contents* a *Pull requests*
  (read and write). Release PR otvorený týmto tokenom spustí testy, ktoré vyžaduje ochrana vetvy `main`.
- Voliteľne v ochrane vetvy `main` vyžadovať kontrolu **conventional-commit** z workflowu *PR title*.
- Prostredie `packaging` vyžaduje schválenie tímom Autogram Release Team a nedovolí schváliť vlastné spustenie.
  Balíčky sa nespúšťajú pushom tagu, ale z workflowu **Release** alebo ručne cez **Package All**.
