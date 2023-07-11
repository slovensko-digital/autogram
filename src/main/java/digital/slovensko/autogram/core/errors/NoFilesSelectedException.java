package digital.slovensko.autogram.core.errors;

public class NoFilesSelectedException extends AutogramException {
    public NoFilesSelectedException(){
        super("Nezvolili ste žiadne súbory", "", "Zvoľte súbory, ktoré chcete podpísať.");
    }
}
