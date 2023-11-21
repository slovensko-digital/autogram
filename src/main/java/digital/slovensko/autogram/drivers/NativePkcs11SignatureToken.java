package digital.slovensko.autogram.drivers;

import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.KSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.token.PrefilledPasswordCallback;
import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;
import sun.security.pkcs11.wrapper.PKCS11Exception;

import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;

public class NativePkcs11SignatureToken extends Pkcs11SignatureToken {
    private static final long CKU_CONTEXT_SPECIFIC = 2L;

    public NativePkcs11SignatureToken(String pkcsPath, PrefilledPasswordCallback prefilledPasswordCallback, int slotId, Integer bogus) {
        super(pkcsPath, prefilledPasswordCallback, slotId, null);
    }

    private byte[] sign(final byte[] bytes, final String javaSignatureAlgorithm, final AlgorithmParameterSpec param, final DSSPrivateKeyEntry keyEntry) throws GeneralSecurityException {
        if (!(keyEntry instanceof KSPrivateKeyEntry)) {
            throw new IllegalArgumentException("Only KSPrivateKeyEntry are supported");
        }

        final Signature signature = getSignatureInstance(javaSignatureAlgorithm);
        if (param != null) {
            signature.setParameter(param);
        }
        var pk = ((KSPrivateKeyEntry) keyEntry).getPrivateKey();

        signature.initSign(pk);
        if (isAlwaysAuthenticate(signature, pk)) {
            var pin = "123456".toCharArray(); // TODO actually get this somehow from password protection
            runContextSpecificLogin(signature, pk, pin);
        }
        signature.update(bytes);
        return signature.sign();
    }

    private boolean isAlwaysAuthenticate(Signature signature, PrivateKey pk) {
        // TODO cache this?
        try {
            var p11 = getP11(signature);
            var sessionId = getSessionId(signature);
            var keyID = getKeyID(pk);
            var attrs = new CK_ATTRIBUTE[]{new CK_ATTRIBUTE(PKCS11Constants.CKA_ALWAYS_AUTHENTICATE)};

            p11.C_GetAttributeValue(sessionId, keyID, attrs);

            var result = attrs[0].pValue;
            if (result instanceof byte[]) {
                return ((byte[]) result)[0] == 1;
            } else {
                return false; // CKA_ALWAYS_AUTHENTICATE not found
            }
        } catch (PKCS11Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runContextSpecificLogin(Signature signature, PrivateKey pk, char[] pin) throws GeneralSecurityException {
        try {
            var p11 = getP11(signature);
            var sessionId = getSessionId(signature);

            p11.C_Login(sessionId, CKU_CONTEXT_SPECIFIC, pin);
        } catch (PKCS11Exception e) {
            throw new GeneralSecurityException(e);
        }
    }

    private static long getKeyID(PrivateKey pk) {
        try {
            var keyIDHolderField = pk.getClass().getSuperclass().getDeclaredField("keyIDHolder");
            keyIDHolderField.setAccessible(true);
            var keyIDHolder = keyIDHolderField.get(pk);

            var keyIDField = keyIDHolder.getClass().getDeclaredField("keyID");
            keyIDField.setAccessible(true);

            return (long) keyIDField.get(keyIDHolder);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static long getSessionId(Signature signature) {
        try {
            Field sigSpiField = signature.getClass().getDeclaredField("sigSpi");
            sigSpiField.setAccessible(true);
            var sigSpi = sigSpiField.get(signature);

            var sessionField = sigSpi.getClass().getDeclaredField("session");
            sessionField.setAccessible(true);
            var session = sessionField.get(sigSpi);

            var idField = session.getClass().getDeclaredField("id");
            idField.setAccessible(true);

            return (long) idField.get(session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PKCS11 getP11(Signature signature) {
        try {
            Field sigSpiField = signature.getClass().getDeclaredField("sigSpi");
            sigSpiField.setAccessible(true);
            var sigSpi = sigSpiField.get(signature);

            var tokenField = sigSpi.getClass().getDeclaredField("token");
            tokenField.setAccessible(true);
            var token = tokenField.get(sigSpi);

            var p11Field = token.getClass().getDeclaredField("p11");
            p11Field.setAccessible(true);
            var p11 = p11Field.get(token);

            return (PKCS11) p11;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // copy & paste just to call overridden private sign method
    @Override
    public SignatureValue sign(ToBeSigned toBeSigned, SignatureAlgorithm signatureAlgorithm, DSSPrivateKeyEntry keyEntry) throws DSSException {
        assertEncryptionAlgorithmValid(signatureAlgorithm, keyEntry);

        final String javaSignatureAlgorithm = signatureAlgorithm.getJCEId();
        final byte[] bytes = toBeSigned.getBytes();
        AlgorithmParameterSpec param = null;
        if (signatureAlgorithm.getMaskGenerationFunction() != null) {
            param = createPSSParam(signatureAlgorithm.getDigestAlgorithm());
        }

        try {
            final byte[] signatureValue = sign(bytes, javaSignatureAlgorithm, param, keyEntry);
            SignatureValue value = new SignatureValue();
            value.setAlgorithm(signatureAlgorithm);
            value.setValue(signatureValue);
            return value;
        } catch (Exception e) {
            throw new DSSException(String.format("Unable to sign : %s", e.getMessage()), e);
        }
    }

    // copy & paste
    private void assertEncryptionAlgorithmValid(SignatureAlgorithm signatureAlgorithm, DSSPrivateKeyEntry keyEntry) {
        Objects.requireNonNull(signatureAlgorithm, "SignatureAlgorithm shall be provided.");
        Objects.requireNonNull(signatureAlgorithm.getEncryptionAlgorithm(), "EncryptionAlgorithm shall be provided within the SignatureAlgorithm.");
        Objects.requireNonNull(keyEntry, "keyEntry shall be provided.");
        if (!signatureAlgorithm.getEncryptionAlgorithm().isEquivalent(keyEntry.getEncryptionAlgorithm())) {
            throw new IllegalArgumentException(String.format("The provided SignatureAlgorithm '%s' cannot be used to sign with " +
                    "the token's implied EncryptionAlgorithm '%s'", signatureAlgorithm.getName(), keyEntry.getEncryptionAlgorithm().getName()));
        }
    }
}
