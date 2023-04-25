package digital.slovensko.autogram.core.errors;

public class UnrecognizedException extends AutogramException {
    public UnrecognizedException(Throwable e) {
        super("Neočakávaná chyba", "Nastala neznáma chyba", "Stalo sa niečo neočakávané. Skúste to, čo ste robili zopakovať a ak sa chyba opäť zopakuje, dajte nám vedieť na podpora@slovensko.digital aj s detailným popisom chyby, ktorý nájdete nižšie.\n\nZa nepríjmenosť sa ospravedľnujeme a ďakujeme za to, že nám pomáhate zlepšovať štátne IT.", e);
    }
}
