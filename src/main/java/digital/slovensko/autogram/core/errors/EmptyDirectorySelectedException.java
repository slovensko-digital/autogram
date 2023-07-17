package digital.slovensko.autogram.core.errors;

public class EmptyDirectorySelectedException extends AutogramException {
    public EmptyDirectorySelectedException(String filePath) {
        super("Zvolili ste prázdny priečinok", "Prázdny priečinok nevieme podpísať", "Priečinok \"" + filePath + "\" neobsahuje žiadne súbory.");
    }
}
