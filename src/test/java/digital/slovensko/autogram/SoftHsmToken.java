package digital.slovensko.autogram;

import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.PKCS11;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static sun.security.pkcs11.wrapper.PKCS11Constants.*;

/**
 * SoftHSM token with Jana Vzorová's key from {@link TestPki}, marked always-authenticate, so Autogram asks for the
 * signing PIN before each signature like with the Slovak eID card. A real PKCS#11 driver without a real card, safe to
 * get wrong PINs.
 * <p>
 * Needs SoftHSM 2 (library and softhsm2-util, e.g. {@code apt install softhsm2} or {@code brew install softhsm}) and
 * {@code SOFTHSM2_CONF}, which surefire sets to target/softhsm - SoftHSM reads it only when initialized, so the token
 * is created once per JVM, before the library is loaded.
 */
public final class SoftHsmToken {
    public static final String PIN = "1234";
    private static final String SO_PIN = "5678";
    private static final String LABEL = "autogram-test";
    private static final List<String> LIBRARIES = List.of(
            "/opt/homebrew/lib/softhsm/libsofthsm2.so", "/usr/local/lib/softhsm/libsofthsm2.so",
            "/usr/lib/softhsm/libsofthsm2.so", "/usr/lib/x86_64-linux-gnu/softhsm/libsofthsm2.so");

    private static Optional<Path> library;

    private SoftHsmToken() {
    }

    /**
     * @return path to the SoftHSM library with the token ready, empty if SoftHSM isn't installed - skip the test then
     */
    public static synchronized Optional<Path> library() throws Exception {
        if (library == null)
            library = create();

        return library;
    }

    private static Optional<Path> create() throws Exception {
        var conf = System.getenv("SOFTHSM2_CONF");
        var lib = LIBRARIES.stream().map(Path::of).filter(Files::isRegularFile).findFirst();
        var util = softhsmUtil(lib);
        if (conf == null || lib.isEmpty() || util.isEmpty())
            return Optional.empty();

        var tokens = Path.of(conf).resolveSibling("tokens");
        deleteRecursively(tokens);
        Files.createDirectories(tokens);
        Files.writeString(Path.of(conf), """
                directories.tokendir = %s
                objectstore.backend = file
                log.level = ERROR
                slots.removable = false
                """.formatted(tokens));

        var init = new ProcessBuilder(util.get().toString(), "--init-token", "--free", "--label", LABEL,
                "--pin", PIN, "--so-pin", SO_PIN).redirectErrorStream(true).start();
        var output = new String(init.getInputStream().readAllBytes());
        if (init.waitFor() != 0)
            throw new IllegalStateException("softhsm2-util failed: " + output);

        addKey(lib.get());
        return lib;
    }

    private static Optional<Path> softhsmUtil(Optional<Path> library) {
        var candidates = Stream.concat(
                library.stream().map(l -> l.getParent().getParent().getParent().resolve("bin/softhsm2-util")),
                Stream.of(System.getenv().getOrDefault("PATH", "").split(java.io.File.pathSeparator))
                        .filter(d -> !d.isEmpty()).map(d -> Path.of(d, "softhsm2-util")));
        return candidates.filter(Files::isExecutable).findFirst();
    }

    private static void addKey(Path library) throws Exception {
        var args = new CK_C_INITIALIZE_ARGS();
        args.flags = CKF_OS_LOCKING_OK;
        // the same module instance SunPKCS11 and Autogram get later
        var p11 = PKCS11.getInstance(library.toString(), "C_GetFunctionList", args, false);

        long slot = -1;
        for (var s : p11.C_GetSlotList(true))
            if (new String(p11.C_GetTokenInfo(s).label).trim().equals(LABEL))
                slot = s;
        if (slot < 0)
            throw new IllegalStateException("SoftHSM token " + LABEL + " not found");

        var signer = TestPki.shared().signer(TestPki.JANA);
        var keystore = KeyStore.getInstance("JKS");
        keystore.load(new ByteArrayInputStream(signer.keystore()), new char[0]);
        var alias = keystore.aliases().nextElement();
        var key = (RSAPrivateCrtKey) keystore.getKey(alias, new char[0]);
        var certificate = signer.certificate();
        var id = new byte[]{1};
        var label = "Jana Vzorova".toCharArray();

        var session = p11.C_OpenSession(slot, CKF_SERIAL_SESSION | CKF_RW_SESSION, null, null);
        try {
            p11.C_Login(session, CKU_USER, PIN.toCharArray());
            p11.C_CreateObject(session, new CK_ATTRIBUTE[]{
                    new CK_ATTRIBUTE(CKA_CLASS, CKO_PRIVATE_KEY),
                    new CK_ATTRIBUTE(CKA_KEY_TYPE, CKK_RSA),
                    new CK_ATTRIBUTE(CKA_TOKEN, true),
                    new CK_ATTRIBUTE(CKA_PRIVATE, true),
                    new CK_ATTRIBUTE(CKA_SENSITIVE, true),
                    new CK_ATTRIBUTE(CKA_SIGN, true),
                    // like eID: the signing PIN before each signature
                    new CK_ATTRIBUTE(CKA_ALWAYS_AUTHENTICATE, true),
                    new CK_ATTRIBUTE(CKA_ID, id),
                    new CK_ATTRIBUTE(CKA_LABEL, label),
                    new CK_ATTRIBUTE(CKA_MODULUS, key.getModulus()),
                    new CK_ATTRIBUTE(CKA_PUBLIC_EXPONENT, key.getPublicExponent()),
                    new CK_ATTRIBUTE(CKA_PRIVATE_EXPONENT, key.getPrivateExponent()),
                    new CK_ATTRIBUTE(CKA_PRIME_1, key.getPrimeP()),
                    new CK_ATTRIBUTE(CKA_PRIME_2, key.getPrimeQ()),
                    new CK_ATTRIBUTE(CKA_EXPONENT_1, key.getPrimeExponentP()),
                    new CK_ATTRIBUTE(CKA_EXPONENT_2, key.getPrimeExponentQ()),
                    new CK_ATTRIBUTE(CKA_COEFFICIENT, key.getCrtCoefficient()),
            });
            p11.C_CreateObject(session, new CK_ATTRIBUTE[]{
                    new CK_ATTRIBUTE(CKA_CLASS, CKO_CERTIFICATE),
                    new CK_ATTRIBUTE(CKA_CERTIFICATE_TYPE, CKC_X_509),
                    new CK_ATTRIBUTE(CKA_TOKEN, true),
                    new CK_ATTRIBUTE(CKA_ID, id),
                    new CK_ATTRIBUTE(CKA_LABEL, label),
                    new CK_ATTRIBUTE(CKA_SUBJECT, certificate.getSubjectX500Principal().getEncoded()),
                    new CK_ATTRIBUTE(CKA_VALUE, certificate.getEncoded()),
            });
            p11.C_Logout(session);
        } finally {
            p11.C_CloseSession(session);
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir))
            return;

        try (var paths = Files.walk(dir)) {
            for (var path : paths.sorted(Comparator.reverseOrder()).toList())
                Files.delete(path);
        }
    }
}
