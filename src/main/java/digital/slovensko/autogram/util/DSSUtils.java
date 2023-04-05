package digital.slovensko.autogram.util;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

public class DSSUtils {
    public static String parseCN(String rfc2253) {
        try {
            var ldapName = new LdapName(rfc2253);
            for (Rdn rdn : ldapName.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    String cn = rdn.getValue().toString();
                    return cn;
                }
            }
        } catch (InvalidNameException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
