package digital.slovensko.autogram;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.BatchManager;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AutogramTests {
    @Test
    void testSignHappyScenario() {
        var newUI = new FakeUI();
        var autogram = new Autogram(newUI);

        var parameters = SigningParameters.buildForASiCWithXAdES("pom.xml");
        var document = new FileDocument("pom.xml");
        var responder = mock(Responder.class);

        autogram.pickSigningKeyAndThen(key
        -> autogram.sign(new SigningJob(document, parameters, responder), key));

        verify(responder).onDocumentSigned(any());
    }

    @Test
    void testSignCertificatePickFailed() {

    }

    @Test
    void testSignFailedAfterCertificatePick() {

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

    private static class FakeUI implements UI {
        @Override
        public void startSigning(SigningJob signingJob, Autogram autogram) {

        }
        
        @Override
        public void startBatch(BatchManager batchManager, Autogram autogram, Consumer<SigningKey> callback) {
        }

        @Override
        public void signBatch(SigningJob job, Autogram autogram) {
        }

        @Override
        public void pickTokenDriverAndThen(List<TokenDriver> drivers, Consumer<TokenDriver> callback) {
            callback.accept(new FakeTokenDriver("fake"));
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
