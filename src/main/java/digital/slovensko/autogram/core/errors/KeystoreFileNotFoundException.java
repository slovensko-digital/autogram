package digital.slovensko.autogram.core.errors;

public class KeystoreFileNotFoundException extends AutogramException {
    public KeystoreFileNotFoundException(String filePath) {
        super(new Object[]{filePath});
    }
}
