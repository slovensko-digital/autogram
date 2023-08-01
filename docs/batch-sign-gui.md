# Scenare hromadneho podpisovania cez GUI

- moze existovat iba jedno aktivne hromadne podpisovanie
- pocas hromadneho podpisovania sa nesmie dat zmenit token driver
- pocas behu hromadneho podpisovania ostatne sign requesty cakaju
- pocas behu hromadneho podpisovania su odmietane nove hromadne podpisovania (?)
- hromadne podpisovanie viem ako pouzivatel prerusit zatvorenim okna alebo stlacenim tlacidla
- je dolezite aby pri driveroch/klucoch ktore vyzaduju pin pri kazdom podpisani bola informacia o tomto (v nejakej zatvorke/malym sedym) 
- kym je aktivny batch (session) tak je otvorene okno (+ stav pred potvrdenim, po dokonceni)

## 1. Happy path
z pohladu pouzivatela - UI

- vidim main dialog, stlacim button "vybrat subor", vyberiem viacero suborov
- otvori sa okno hromadneho podpisovania, ktore upozornuje na rizika, vyberiem podpis, dam podpisat
- pocas behu je ukazany progress bar
- po skonceni sa zatvori, a ukaze sa dialog s vypisom suborov, ktore boli podpisane (v summary-detail) a cestou kam boli zapisane


## 2. Nastane chyba
- pouzivatel vyberie dokumenty na podpisanie
- spusti podpisovanie
- niektore dokumenty sa nepodarilo podpisat
- uvidi na poslednej obrazovke oznam o tom, ktore dokumenty sa nepodarilo podpisat
- ma moznost spustit proces este raz s pridanim skip parametru (podpisat tie co neboli podpisane)

## 5. Možné nastavenia
- kam ukladat subory (target parameter)
- ci prepisovat existujuce subory (force parameter)
- ako postupovat pri chybe (zrusit, pokracovat)
- ci preskakovat subory ktore uz su vytvorene (skip parameter)
- PDF/A check
- da sa vybrat folder? ak vyberieme folder ma ist rekurzivne? (--recursive)