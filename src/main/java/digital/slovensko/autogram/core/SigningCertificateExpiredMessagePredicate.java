package digital.slovensko.autogram.core;

import java.util.function.Predicate;

public class SigningCertificateExpiredMessagePredicate implements Predicate<String> {
    @Override
    public boolean test(String message) {
        if (message == null) {
            return false;
        }
        String regex = ".*The signing certificate.*is expired.*";
        return message.matches(regex);
    }
}
