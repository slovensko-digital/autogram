package digital.slovensko.autogram.core;

import com.google.gson.annotations.SerializedName;
import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.CertificateExtensionsUtils;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Type of a signing certificate as the central electronic registry of slovensko.sk (CEP) names it. */
public enum SigningCertificateType {
    @SerializedName("QESig") QESIG,
    @SerializedName("QESig-MQC") QESIG_MANDATE,
    @SerializedName("QESeal") QESEAL,
    @SerializedName("AdESig-QC") ADESIG_QC,
    @SerializedName("AdESeal-QC") ADESEAL_QC,
    @SerializedName("AdESig") ADESIG,
    @SerializedName("AdESeal") ADESEAL;

    private static final String MANDATE_POLICY_ARC = "1.3.158.36061701.1.1.";
    private static final String MANDANT_PREFIX = "MANDANT ";
    private static final Pattern MANDATE_IN_COMMON_NAME = Pattern.compile("\\bOPR[AÁ]VNENIE\\s+\\d+",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** Null when the qualification does not tell, e.g. the issuer is on no trusted list or the lists are not loaded. */
    public static SigningCertificateType of(CertificateQualification qualification, CertificateToken certificate) {
        if (qualification == null)
            return null;

        return switch (qualification) {
            case QCERT_FOR_ESIG_QSCD -> isMandate(certificate) ? QESIG_MANDATE : QESIG;
            case QCERT_FOR_ESEAL_QSCD -> QESEAL;
            case QCERT_FOR_ESIG -> ADESIG_QC;
            case QCERT_FOR_ESEAL -> ADESEAL_QC;
            case CERT_FOR_ESIG -> ADESIG;
            case CERT_FOR_ESEAL -> ADESEAL;
            default -> null;
        };
    }

    /**
     * Mandate certificates carry no eIDAS or ETSI flag, only Slovak markers that differ by issuer: SNCA and Ardaco add
     * a policy under NBÚ's mandate arc; the subject fields of the mandant start with "MANDANT " (NBÚ supervision
     * scheme); SNCA and Ardaco put "OPRÁVNENIE" with the mandate's number in the common name, Disig "Opravnenie".
     */
    static boolean isMandate(CertificateToken certificate) {
        var policies = CertificateExtensionsUtils.getCertificatePolicies(certificate);
        if (policies != null && policies.getPolicyList().stream()
                .anyMatch(policy -> policy.getOid() != null && policy.getOid().startsWith(MANDATE_POLICY_ARC)))
            return true;

        return subjectAttributes(certificate).anyMatch(attribute -> {
            var value = attribute.getValue() instanceof ASN1String string ? string.getString() : "";
            return value.startsWith(MANDANT_PREFIX)
                    || attribute.getType().equals(BCStyle.CN) && MANDATE_IN_COMMON_NAME.matcher(value).find();
        });
    }

    private static Stream<AttributeTypeAndValue> subjectAttributes(CertificateToken certificate) {
        var subject = X500Name.getInstance(certificate.getCertificate().getSubjectX500Principal().getEncoded());
        return Arrays.stream(subject.getRDNs()).map(RDN::getTypesAndValues).flatMap(Arrays::stream);
    }
}
