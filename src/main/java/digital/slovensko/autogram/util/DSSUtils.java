package digital.slovensko.autogram.util;

import digital.slovensko.autogram.core.NetworkClients;
import eu.europa.esig.dss.asic.cades.validation.ASiCContainerWithCAdESValidatorFactory;
import eu.europa.esig.dss.asic.xades.validation.ASiCContainerWithXAdESValidatorFactory;
import eu.europa.esig.dss.cades.validation.CMSDocumentValidatorFactory;
import eu.europa.esig.dss.enumerations.ExtendedKeyUsage;
import eu.europa.esig.dss.enumerations.KeyUsageBit;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidatorFactory;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.xades.validation.XMLDocumentValidatorFactory;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.security.cert.CertificateParsingException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DSSUtils {
    public static String parseCN(String rfc2253) {
        try {
            var ldapName = new LdapName(rfc2253);
            for (Rdn rdn : ldapName.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    return rdn.getValue().toString();
                }
            }
        } catch (InvalidNameException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static final Set<String> NOT_FOR_DOCUMENTS = Stream.of(ExtendedKeyUsage.SERVER_AUTH,
            ExtendedKeyUsage.TIMESTAMPING, ExtendedKeyUsage.OCSP_SIGNING).map(ExtendedKeyUsage::getOid).collect(Collectors.toSet());

    /**
     * Whether RFC 5280 allows the certificate to sign documents: key usage, if present, has digitalSignature or
     * nonRepudiation (4.2.1.3), and extended key usage, if present, has a purpose other than TLS server, timestamps and
     * OCSP (4.2.1.12). No other purpose excludes it, so that no signing certificate is left out. The qualified
     * certificate of the eID card read over NFC has nonRepudiation without digitalSignature (#367).
     */
    public static boolean isSigningCertificate(CertificateToken certificate) {
        var keyUsage = certificate.getKeyUsageBits();
        var signs = keyUsage.isEmpty()
                || keyUsage.contains(KeyUsageBit.DIGITAL_SIGNATURE)
                || keyUsage.contains(KeyUsageBit.NON_REPUDIATION);
        return signs && !isOnlyForOtherThanDocuments(certificate);
    }

    private static boolean isOnlyForOtherThanDocuments(CertificateToken certificate) {
        try {
            var purposes = certificate.getCertificate().getExtendedKeyUsage();
            return purposes != null && !purposes.isEmpty() && NOT_FOR_DOCUMENTS.containsAll(purposes);
        } catch (CertificateParsingException e) {
            return false;
        }
    }

    public static String buildTooltipLabel(DSSPrivateKeyEntry key) {
        var out = "";
        out += key.getCertificate().getSubject().getPrincipal().toString();
        if (key.getCertificate().getIssuer() != null) {
            out += "\n\n" + key.getCertificate().getIssuer().getPrincipal().toString();
        }
        return out;
    }

    public static SignedDocumentValidator createDocumentValidator(DSSDocument document) {
        if (new PDFDocumentValidatorFactory().isSupported(document))
            return NetworkClients.configureDocumentValidator(new PDFDocumentValidatorFactory().create(document));

        if (new XMLDocumentValidatorFactory().isSupported(document))
            return NetworkClients.configureDocumentValidator(new XMLDocumentValidatorFactory().create(document));

        if (new ASiCContainerWithXAdESValidatorFactory().isSupported(document))
            return NetworkClients.configureDocumentValidator(new ASiCContainerWithXAdESValidatorFactory().create(document));

        if (new ASiCContainerWithCAdESValidatorFactory().isSupported(document))
            return NetworkClients.configureDocumentValidator(new ASiCContainerWithCAdESValidatorFactory().create(document));

        if (new CMSDocumentValidatorFactory().isSupported(document))
            return NetworkClients.configureDocumentValidator(new CMSDocumentValidatorFactory().create(document));

        return null;
    }

    public static String getXdcfFilename(String filename) {
        if (filename == null)
            return "document.xdcf";

        if (filename.endsWith(".xml"))
            return filename.replace(".xml", ".xdcf");

        if (!filename.contains(".xdcf"))
            return filename + ".xdcf";

        return filename;
    }
}
