# Príkazový riadok (CLI)

Autogram vie podpisovať aj bez grafického rozhrania, čo sa hodí na podpisovanie veľkého počtu súborov naraz. Na Windows použite `autogram-cli`, na macOS a Linuxe `autogram`.

```sh
autogram --cli dokument.pdf
autogram --cli -s dokument.pdf -t podpisane/dokument.pdf --parents
autogram --cli -s priecinok -t priecinok_podpisane -f
```

- `-s`, `--source`: súbor alebo priečinok na podpis (dá sa zadať aj ako jediný argument).
- `-t`, `--target`: kam uložiť podpísaný súbor alebo priečinok. Bez neho sa súbor uloží vedľa pôvodného s príponou `_signed`.
- `-f`, `--force`: prepíše existujúce súbory.
- `--parents`: vytvorí chýbajúce priečinky cieľa.
- `-d`, `--driver`: úložisko certifikátov (`eid`, `cz_eid`, `secure_store`, `monet`, `gemalto`, `keystore`, `custom_pkcs11`).

Úplný zoznam prepínačov vypíše `autogram --help`, príklady `autogram --usage`.

## Kontajnery s viacerými dokumentmi

Každý vstupný súbor sa podpisuje samostatne. Existujúci kontajner ASiC-E s viacerými dokumentmi sa podpíše ako celok, rovnako ako v grafickom rozhraní: podpis pokryje všetky dokumenty v kontajneri.

Priečinok s viacerými súbormi CLI nespojí do jedného kontajnera, každý súbor dostane vlastný podpis. Nový kontajner z viacerých dokumentov vytvoríte cez HTTP API (pozri [Viac dokumentov v jednom kontajneri](multi-document.md#pre-integrátorov)).
