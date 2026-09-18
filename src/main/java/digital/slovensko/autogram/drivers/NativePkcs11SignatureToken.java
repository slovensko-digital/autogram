package digital.slovensko.autogram.drivers;

import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.SignatureTokenSettings;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import digital.slovensko.autogram.core.errors.PasswordNotProvidedException;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.KSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;

public class NativePkcs11SignatureToken extends Pkcs11SignatureToken {
    private static final long CKU_CONTEXT_SPECIFIC = 2L;
    private static final String PKCS11_EXCEPTION_CLASS_NAME = "sun.security.pkcs11.wrapper.PKCS11Exception";
    private static final String PKCS11_CONSTANTS_CLASS_NAME = "sun.security.pkcs11.wrapper.PKCS11Constants";
    private static final String CK_ATTRIBUTE_CLASS_NAME = "sun.security.pkcs11.wrapper.CK_ATTRIBUTE";

    private final PasswordManager passwordManager;
    private final SignatureTokenSettings settings;

    public NativePkcs11SignatureToken(String pkcsPath, PasswordManager pm, SignatureTokenSettings settings, int driverSlotIndex) {
        super(pkcsPath, pm, -1, driverSlotIndex, null);
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
                if (password == null) throw new PasswordNotProvidedException();
                invokeCLogin(p11, sessionId, CKU_CONTEXT_SPECIFIC, password);
            }
        } catch (AutogramException e) {
            throw e;
        } catch (Exception e) {
            var cause = unwrapInvocationException(e);
            if (isPkcs11Exception(cause)) {
                if ("CKR_PIN_INCORRECT".equals(cause.getMessage())) {
                    throw new PINIncorrectException();
                }
                throw new GeneralSecurityException(cause);
            }
            throw new GeneralSecurityException(cause);
        }
    }

    private static boolean isAlwaysAuthenticate(Object p11, long sessionId, PrivateKey pk) throws Exception {
        var keyID = getKeyID(pk);
        var attrs = newAttributeArray(getPkcs11Constant("CKA_ALWAYS_AUTHENTICATE"));

        invokeCGetAttributeValue(p11, sessionId, keyID, attrs);

        var attr = Array.get(attrs, 0);
        var result = getPublicField(attr, "pValue");
        if (result instanceof byte[]) {
            return ((byte[]) result)[0] == 1;
        }
        return false;
    }

    private static boolean isProtectedAuthenticationPath(Object p11, int slotIndex) throws Exception {
        var slotList = invokeCGetSlotList(p11, false);
        if (slotList.length <= slotIndex || slotList.length < 1) {
            return false;
        }

        if (slotIndex < 0) {
            slotIndex = 0;
        }

        var slotId = slotList[slotIndex];
        var tokenInfo = invokeCGetTokenInfo(p11, slotId);
        var flags = (long) getPublicField(tokenInfo, "flags");
        return (flags & getPkcs11Constant("CKF_PROTECTED_AUTHENTICATION_PATH")) != 0;
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
            var keyIDHolderField = pk.getClass().getSuperclass().getSuperclass().getDeclaredField("keyIDHolder");
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

    private static Object getP11(Signature signature) {
        try {
            Field sigSpiField = signature.getClass().getDeclaredField("sigSpi");
            sigSpiField.setAccessible(true);
            var sigSpi = sigSpiField.get(signature);

            var tokenField = sigSpi.getClass().getDeclaredField("token");
            tokenField.setAccessible(true);
            var token = tokenField.get(sigSpi);

            var p11Field = token.getClass().getDeclaredField("p11");
            p11Field.setAccessible(true);
            return p11Field.get(token);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object newAttributeArray(long attributeType) throws Exception {
        var attrClass = Class.forName(CK_ATTRIBUTE_CLASS_NAME);
        var array = Array.newInstance(attrClass, 1);
        var attribute = attrClass.getConstructor(long.class).newInstance(attributeType);
        Array.set(array, 0, attribute);
        return array;
    }

    private static long getPkcs11Constant(String fieldName) throws Exception {
        return Class.forName(PKCS11_CONSTANTS_CLASS_NAME).getField(fieldName).getLong(null);
    }

    private static void invokeCLogin(Object p11, long sessionId, long userType, char[] pin) throws Exception {
        invokeMethod(p11, "C_Login", new Class<?>[]{long.class, long.class, char[].class}, sessionId, userType, pin);
    }

    private static void invokeCGetAttributeValue(Object p11, long sessionId, long objectHandle, Object attrs) throws Exception {
        invokeMethod(p11, "C_GetAttributeValue", new Class<?>[]{long.class, long.class, attrs.getClass()}, sessionId, objectHandle, attrs);
    }

    private static long[] invokeCGetSlotList(Object p11, boolean tokenPresent) throws Exception {
        return (long[]) invokeMethod(p11, "C_GetSlotList", new Class<?>[]{boolean.class}, tokenPresent);
    }

    private static Object invokeCGetTokenInfo(Object p11, long slotId) throws Exception {
        return invokeMethod(p11, "C_GetTokenInfo", new Class<?>[]{long.class}, slotId);
    }

    private static Object invokeMethod(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getMethod(methodName, parameterTypes);
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            var cause = unwrapInvocationException(e);
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw new RuntimeException(cause);
        }
    }

    private static Object getPublicField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getField(fieldName);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Throwable unwrapInvocationException(Throwable throwable) {
        if (throwable instanceof InvocationTargetException invocationTargetException && invocationTargetException.getCause() != null) {
            return invocationTargetException.getCause();
        }
        return throwable;
    }

    private static boolean isPkcs11Exception(Throwable throwable) {
        return throwable != null && PKCS11_EXCEPTION_CLASS_NAME.equals(throwable.getClass().getName());
    }

    // mostly copy & paste just to call overridden private sign method
    @Override
    public SignatureValue sign(ToBeSigned toBeSigned, SignatureAlgorithm signatureAlgorithm, DSSPrivateKeyEntry keyEntry) throws DSSException {
        assertEncryptionAlgorithmValid(signatureAlgorithm, keyEntry);

		final String javaSignatureAlgorithm = signatureAlgorithm.getJCEId();
		final byte[] bytes = toBeSigned.getBytes();
		AlgorithmParameterSpec param = initParameters(signatureAlgorithm, signatureAlgorithm.getDigestAlgorithm());

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
