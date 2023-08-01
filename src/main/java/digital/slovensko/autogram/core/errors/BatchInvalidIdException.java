package digital.slovensko.autogram.core.errors;

public class BatchInvalidIdException extends AutogramException {
    public BatchInvalidIdException() {
        super("Identifikátor hromadného podpisovania je nesprávny", "Nesprávne ID hromadného podpisovania", "Pokus o hromadné podpisovanie zlyhal. Ak ste nechceli hromadne podpisovať, overte bezpečnosť svojho zariadenia. Ak ste chceli hromadne podpisovať, skúste zopakovať akciu od začiatku.");
        
    }
}
