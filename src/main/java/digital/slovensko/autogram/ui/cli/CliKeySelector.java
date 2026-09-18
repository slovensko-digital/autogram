package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.util.DSSUtils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

public final class CliKeySelector {
    private CliKeySelector() {
    }

    public static boolean matches(DSSPrivateKeyEntry key, String selector) {
        var certificate = key.getCertificate();
        return matches(certificate.getSerialNumber().toString(),
                DSSUtils.parseCN(certificate.getSubject().getRFC2253()), selector);
    }

    public static boolean matches(String serial, String commonName, String selector) {
        if (selector == null || selector.isBlank())
            return false;

        var normalizedSelector = selector.trim();
        return normalizedSelector.equals(serial) || normalizedSelector.equals(commonName);
    }

    public static String serial(DSSPrivateKeyEntry key) {
        return key.getCertificate().getSerialNumber().toString();
    }

    public static String commonName(DSSPrivateKeyEntry key) {
        return DSSUtils.parseCN(key.getCertificate().getSubject().getRFC2253());
    }
}
