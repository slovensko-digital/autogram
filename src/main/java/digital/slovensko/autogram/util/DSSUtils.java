package digital.slovensko.autogram.util;

import digital.slovensko.autogram.core.errors.AutogramException;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

public class DSSUtils {
    public static String parseCN(String rfc2253) throws AutogramException {
        try {
            var ldapName = new LdapName(rfc2253);
            for (Rdn rdn : ldapName.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    return rdn.getValue().toString();
                }
            }
        } catch (InvalidNameException e) {
            throw AutogramException.fromThrowable(e);
        }
        return null;
    }

    public static String buildTooltipLabel(DSSPrivateKeyEntry key) {
        var out = "";
        out += key.getCertificate().getSubject().getPrincipal().toString();
        if (key.getCertificate().getIssuer() != null) {
            out += "\n\n" + key.getCertificate().getIssuer().getPrincipal().toString();
        }
        return out;
    }
}
