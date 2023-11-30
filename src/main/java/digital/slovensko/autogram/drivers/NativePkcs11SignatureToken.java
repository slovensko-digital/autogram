package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.SignatureTokenSettings;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import digital.slovensko.autogram.errors.PasswordNotProvidedException;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.KSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
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
    private final PasswordManager passwordManager;
    private final SignatureTokenSettings settings;

    public NativePkcs11SignatureToken(String pkcsPath, PasswordManager pm, SignatureTokenSettings settings) {
        super(pkcsPath, pm, -1, settings.getSlotIndex(), null);
        this.passwordManager = pm;
        this.settings = settings;
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
        runContextSpecificLoginIfNeeded(signature, pk);
        signature.update(bytes);
        return signature.sign();
    }

    private void runContextSpecificLoginIfNeeded(Signature signature, PrivateKey pk) throws GeneralSecurityException {
        try {
            // TODO cache & short-circuit
            var p11 = getP11(signature);
            var sessionId = getSessionId(signature);

            if (isAlwaysAuthenticate(p11, sessionId, pk) && (settings.getForceContextSpecificLoginEnabled() || !isProtectedAuthenticationPath(p11, getSlotListIndex()))) {
                var password = passwordManager.getContextSpecificPassword();
                if (password == null) throw new PasswordNotProvidedException(); // handle password not provided
                p11.C_Login(sessionId, CKU_CONTEXT_SPECIFIC, password);
            }
        } catch (AutogramException e) {
            throw e; // rethrow autogram errors
        } catch (PKCS11Exception e) {
            if (e.getMessage().equals("CKR_PIN_INCORRECT"))
                throw new PINIncorrectException();

            throw new GeneralSecurityException(e);
        }
    }

    private static boolean isAlwaysAuthenticate(PKCS11 p11, long sessionId, PrivateKey pk) throws PKCS11Exception {
        var keyID = getKeyID(pk);
        var attrs = new CK_ATTRIBUTE[]{new CK_ATTRIBUTE(PKCS11Constants.CKA_ALWAYS_AUTHENTICATE)};

        p11.C_GetAttributeValue(sessionId, keyID, attrs);

        var result = attrs[0].pValue;
        if (result instanceof byte[]) {
            return ((byte[]) result)[0] == 1;
        } else {
            return false; // CKA_ALWAYS_AUTHENTICATE not found
        }
    }

    private static boolean isProtectedAuthenticationPath(PKCS11 p11, int slotIndex) throws PKCS11Exception {

        var slotList = p11.C_GetSlotList(false);
        if (slotList.length <= slotIndex || slotList.length < 1)
            return false;

        if (slotIndex < 0)
            slotIndex = 0;

        var slotId = slotList[slotIndex];
        return (p11.C_GetTokenInfo(slotId).flags & PKCS11Constants.CKF_PROTECTED_AUTHENTICATION_PATH) != 0;
    }

    private int getSlotListIndex() {
        try {
            Field f = getClass().getSuperclass().getDeclaredField("slotListIndex");
            f.setAccessible(true);
            return (int) f.get(this);

        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
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

    // mostly copy & paste just to call overridden private sign method
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
        } catch (AutogramException e) {
            throw e;
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
