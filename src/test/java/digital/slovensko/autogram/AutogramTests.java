package digital.slovensko.autogram;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.SigningWithExpiredCertificateException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AutogramTests {
    @Test
    void testSignHappyScenario() {
        var newUI = new FakeUI();
        var autogram = new Autogram(newUI);

        var parameters = SigningParameters.buildForASiCWithXAdES("pom.xml");
        var document = new FileDocument("pom.xml");
        var responder = mock(Responder.class);

        try (MockedStatic<TokenDriver> mockedStatic = mockStatic(TokenDriver.class)) {
            mockedStatic.when(TokenDriver::getAvailableDrivers).thenReturn(Arrays.asList(new FakeTokenDriver("fake")));

            autogram.pickSigningKeyAndThen(key
                    -> autogram.sign(new SigningJob(document, parameters, responder), key));

            verify(responder).onDocumentSigned(any());
        }
    }

    @Test
    void testSignCertificatePickFailed() {

    }

    @Test
    void testSignFailedAfterCertificatePick() {

    }

    @Test
    void testSignWithExpiredCertificate() {
        var newUI = new FakeUI();
        var autogram = new Autogram(newUI);

        var parameters = SigningParameters.buildForASiCWithXAdES("pom.xml");
        var document = new FileDocument("pom.xml");
        var responder = mock(Responder.class);

        try (MockedStatic<TokenDriver> mockedStatic = mockStatic(TokenDriver.class)) {
            mockedStatic.when(TokenDriver::getAvailableDrivers).thenReturn(Arrays.asList(new FakeTokenDriverWithExpiredCertificate()));

            Assertions.assertThrows(SigningWithExpiredCertificateException.class, () ->
                autogram.pickSigningKeyAndThen(key -> autogram.sign(new SigningJob(document, parameters, responder), key)
            ));
        }
    }

    private static class FakeTokenDriver extends TokenDriver {
        public FakeTokenDriver(String name) {
            super(name, Path.of(""), true);
        }

        @Override
        public AbstractKeyStoreTokenConnection createTokenWithPassword(char[] password) {
            try {
                var keystore = Objects.requireNonNull(this.getClass().getResource("test.keystore")).getFile();
                return new Pkcs12SignatureToken(keystore, new KeyStore.PasswordProtection("".toCharArray()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class FakeTokenDriverWithExpiredCertificate extends TokenDriver {

        public FakeTokenDriverWithExpiredCertificate() {
            super("fake-token-driver-with-expired-certificate", Path.of(""), true);
        }

        @Override
        public AbstractKeyStoreTokenConnection createTokenWithPassword(char[] password) {
            try {
                var keystore = Objects.requireNonNull(this.getClass().getResource("expired_certificate.keystore")).getFile();
                return new Pkcs12SignatureToken(keystore, new KeyStore.PasswordProtection("test123".toCharArray()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class FakeUI implements UI {
        @Override
        public void startSigning(SigningJob signingJob, Autogram autogram) {

        }

        @Override
        public void pickTokenDriverAndThen(List<TokenDriver> drivers, Consumer<TokenDriver> callback) {
            callback.accept(drivers.get(0));
        }

        @Override
        public void requestPasswordAndThen(TokenDriver driver, Consumer<char[]> callback) {
            callback.accept(null);
        }

        @Override
        public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, Consumer<DSSPrivateKeyEntry> callback) {
            callback.accept(keys.get(0));
        }

        @Override
        public void onWorkThreadDo(Runnable callback) {
            callback.run();
        }

        @Override
        public void onUIThreadDo(Runnable callback) {
            callback.run();
        }

        @Override
        public void onUpdateAvailable() {

        }

        @Override
        public void onAboutInfo() {

        }

        @Override
        public void onPDFAComplianceCheckFailed(SigningJob job) {

        }

        @Override
        public void onSigningSuccess(SigningJob signingJob) {

        }

        @Override
        public void onSigningFailed(AutogramException e) {
            throw e;
        }

        @Override
        public void onDocumentSaved(File targetFile) {

        }

        @Override
        public void onPickSigningKeyFailed(AutogramException ae) {
            throw new RuntimeException();
        }
    }
}
